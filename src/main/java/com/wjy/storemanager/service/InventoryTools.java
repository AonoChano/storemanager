package com.wjy.storemanager.service;

import com.wjy.storemanager.entity.Product;
import com.wjy.storemanager.mapper.ProductMapper;
import com.wjy.storemanager.mapper.ReportMapper;
import com.wjy.storemanager.vo.TrendVo;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 给 AI 客服暴露的"查询工具"(Tool Calling)。
 * 每个 @Tool 方法: AI 判断需要数据时会自动调用, 拿到返回值后组织回答。
 */
@Component
public class InventoryTools {

    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private ReportMapper reportMapper;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    // 查商品信息: 进价/售价/库存/预警
    @Tool(description = "按商品名称查询商品信息，返回进价、售价、当前库存、单位、预警阈值。支持模糊匹配，例如'可乐'能查到可口可乐。")
    public String getProductInfo(String name) {
        ToolNotifier.notify("getProductInfo");
        List<Product> list = productMapper.selectAll(name, null, null);
        if (list.isEmpty()) return "没有找到名为「" + name + "」的商品。";
        StringBuilder sb = new StringBuilder();
        for (Product p : list) {
            sb.append("商品").append(p.getName())
              .append("：进价").append(p.getPurchasePrice()).append("元，售价")
              .append(p.getSalePrice()).append("元，当前库存").append(p.getStock())
              .append(p.getUnit() == null ? "" : p.getUnit())
              .append("，预警阈值").append(p.getWarningThreshold()).append("。\n");
        }
        return sb.toString().trim();
    }

    // 查低库存商品
    @Tool(description = "查询所有库存低于预警阈值的商品，返回低库存商品清单，用于库存预警。")
    public String getLowStockProducts() {
        ToolNotifier.notify("getLowStockProducts");
        List<Product> list = productMapper.selectAll(null, null, null);
        StringBuilder sb = new StringBuilder();
        int count = 0;
        for (Product p : list) {
            if (p.getStock() < p.getWarningThreshold()) {
                sb.append(p.getName()).append("：库存").append(p.getStock())
                  .append("，预警线").append(p.getWarningThreshold()).append("\n");
                count++;
            }
        }
        return count == 0 ? "当前没有库存低于预警线的商品。" : "低库存商品共" + count + "个：\n" + sb.toString().trim();
    }

    // 查今日销售
    @Tool(description = "查询今日的销售总额。返回今天的销售金额。")
    public String getTodaySales() {
        ToolNotifier.notify("getTodaySales");
        List<TrendVo> trend = reportMapper.saleTrend();
        String today = LocalDate.now().toString();   // yyyy-MM-dd
        for (TrendVo t : trend) {
            if (today.equals(t.getDay())) {
                return "今日销售额为 " + t.getAmount() + " 元。";
            }
        }
        return "今日还没有销售记录。";
    }

    // 通用只读SQL工具(Text-to-SQL): 让AI自己写SQL查任意统计, 带4把安全锁
    @Tool(description = "执行只读SQL查询(SELECT)获取业务数据并返回结果行。用于任意统计/聚合/排行/对比/多条件查询，例如'哪个商品毛利最高''按分类汇总库存''总采购额''销售Top5'。支持联表JOIN、分组GROUP BY、排序ORDER BY、LIMIT。凡是需要统计多行或整表数据的查询，必须用这个工具，不要用查单个商品的工具。")
    public String executeSql(String sql) {
        ToolNotifier.notify("executeSql");
        if (sql == null || sql.trim().isEmpty()) return "SQL不能为空";
        // 去掉开头注释, 防止绕过检查
        String clean = sql.trim().replaceFirst("^(/\\*.*?\\*/|--.*|#.*)", "").trim();
        String lower = clean.toLowerCase();
        // 锁① 只允许 SELECT
        if (!lower.startsWith("select")) return "只能执行SELECT查询语句";
        // 锁② 禁多条语句
        if (clean.contains(";")) return "不支持多条语句，一次只查一条";
        // 锁③ 表名白名单: 只允许查业务表
        Matcher m = Pattern.compile("\\b(?:from|join)\\s+([a-zA-Z_][a-zA-Z0-9_]*)", Pattern.CASE_INSENSITIVE).matcher(clean);
        List<String> allowed = Arrays.asList("user", "role", "product", "category", "supplier",
                "purchase_order", "purchase_order_detail", "sale_order", "sale_order_detail",
                "stock_record", "operation_log");
        while (m.find()) {
            if (!allowed.contains(m.group(1).toLowerCase())) return "无权查询表: " + m.group(1);
        }
        // 锁④ 强制 LIMIT, 防返回太多行
        String finalSql = lower.contains("limit") ? clean : clean + " LIMIT 100";
        try {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(finalSql);
            return rows.isEmpty() ? "查询无结果" : rows.toString();
        } catch (Exception e) {
            return "SQL执行出错: " + e.getMessage();
        }
    }
}

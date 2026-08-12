package com.wjy.storemanager.controller;

import com.wjy.storemanager.common.Result;
import com.wjy.storemanager.service.ReportService;
import com.wjy.storemanager.vo.CategoryStockVo;
import com.wjy.storemanager.vo.PurchaseVsSaleVo;
import com.wjy.storemanager.vo.TopProductVo;
import com.wjy.storemanager.vo.TrendVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/report")
public class ReportController {

    @Autowired
    private ReportService reportService;

    // 日销趋势(折线图数据)
    @GetMapping("/saleTrend")
    public Result<List<TrendVo>> saleTrend() {
        return Result.success(reportService.saleTrend());
    }

    // 采购 vs 销售 按日(柱状图数据)
    @GetMapping("/purchaseVsSale")
    public Result<List<PurchaseVsSaleVo>> purchaseVsSale() {
        return Result.success(reportService.purchaseVsSale());
    }

    // 商品销售 TOP5(条形图数据)
    @GetMapping("/topProducts")
    public Result<List<TopProductVo>> topProducts() {
        return Result.success(reportService.topProducts());
    }

    // 分类库存汇总(环形图数据)
    @GetMapping("/categoryStock")
    public Result<List<CategoryStockVo>> categoryStock() {
        return Result.success(reportService.categoryStock());
    }
}

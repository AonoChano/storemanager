# 商品/供应商管理改造计划

## 设计原则（行业标准）
主数据（商品/供应商）不物理删除有业务记录的对象——单据/流水需追溯。物理删除仅限无任何业务记录的误建数据，有记录时后端拒绝并引导「下架」。

## 一、商品页改造（src/main/resources/static/index.html）

**1. 新增/编辑商品 → 弹窗**
- 移除底部内嵌「新增商品」表单和批量导入块
- 工具栏加「+ 新增商品」按钮 → 打开商品表单弹窗（复用 .modal 样式）
- 弹窗内容：名称/条码/分类/规格/单位/预警阈值/进价/售价（复用 .form-row cols-3 布局）+ 批量导入折叠块
- 弹窗二态：新增（POST /product/add）/ 编辑（PUT /product/update，预填数据）
- 条码即时查重提示保留

**2. 商品表格加「操作」列**（第 12 列）
- 编辑（铅笔，打开弹窗预填）
- 下架⇄上架切换（按 status，复用 PUT /product/update）
- 删除（垃圾桶）：两步确认（点击变红「确认删除」，3 秒内再点执行）；下架商品行置灰
- 空状态 colspan 同步 12

**3. 「库存低于预警线标红」→ 表格图例**
- 从工具栏移除 tag
- 表格下方脚注：红点 + 「红字 = 库存低于预警线」（var(--faint) 小字）

**4. 前端分页（商品/流水/日志三处统一）**
- 通用分页器函数 renderPager(container, total, page, pageSize, onPage)：页码按钮 + 每页条数 select【10/20/50/80/100】，默认 10
- 数据仍全量拉取、本地切页渲染（当前量级足够，避免后端改动）
- 商品：loadProducts 改为数据数组 + 分页渲染（现有输入框本地过滤改为基于数组过滤），搜索/新增/删除后保持页码合法性
- 流水（loadRecords）、日志（loadLogs）同样接入
- 分页器样式复用现有 .btn ghost + select

## 二、后端删除保护（外键引用检查）

- `ProductServiceImpl.delete`：删除前检查引用——`purchase_order_detail` / `sale_order_detail` / `stock_record` 三表按 product_id 计数（用 JdbcTemplate，spring-jdbc 已在用）；有任一引用 → 抛业务异常「该商品已有业务记录，无法删除，可改为下架」；无引用 → 正常删除
- `SupplierServiceImpl.delete`：检查 `purchase_order.supplier_id` 引用，同理拒绝
- 前端 catch 后 toast 友好提示（现有 friendlyError 通道）

## 三、供应商弹窗加删除
- renderSupplierList 每行 hover 显示垃圾桶按钮（与铅笔编辑并列，复用 .picker-edit 样式）
- 两步确认（同商品删除）
- 删除后刷新列表；若采购单正选中该供应商 → 清空输入框

## 验证
1. 编译 + 重启，playwright 全流程：新增弹窗/编辑预填/下架切换/删除两步确认/被引用删除被拒提示
2. 分页：造 >10 条数据验证页码切换与每页条数选择
3. 供应商删除 + 引用检查
4. 提交推送

## 风险与注意
- 删除检查用 JdbcTemplate 查询三个明细表，与 MyBatis 混用无冲突
- 商品编辑 PUT 需传完整对象（前端持 products 数组可组装）
- 分页只改前端，不动后端接口
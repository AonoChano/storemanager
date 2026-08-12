# storemanager

仓储管理/物流表单 —— 采购 · 销售 · 库存 · 报表 · AI 客服

## 功能演示

<video src="src/docs/demo.mp4" controls width="100%"></video>

## 功能模块

- **采购入库 / 销售出库**：单据模式，多商品明细，扫码录入（条码 / 名称即时匹配），供应商/商品弹窗选择，批量粘贴导入
- **库存管理**：出入库流水、预警线标红、实时库存
- **报表统计**：日销趋势、采购 vs 销售、销量 TOP5、库存分类占比、毛利核算
- **AI 客服**：流式问答、多轮上下文、数据查询（库存/销售/报表）
- **主题**：亮色 / 暗色一键切换，登录页与主系统共用配色

## 快速启动

需要 Docker（MySQL + Redis）与 JDK 17+：

```bash
start.bat          # Windows 一键启动
```

或手动：

```bash
docker compose up -d        # 启动 MySQL + Redis
mvnw spring-boot:run        # 启动应用 → http://localhost:8080
```

默认账号 `admin / 123456`

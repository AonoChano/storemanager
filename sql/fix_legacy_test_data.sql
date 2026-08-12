-- 修复历史测试数据(夸张值 -> 正常值), 2026-08-13
-- 修改前快照见 backup_legacy_before_fix.txt
-- 核心逻辑:
--   商品1 飞机杯(999/1999)   -> 金龙鱼食用调和油 5L/桶 (55/65), 库存按流水重推 = 64
--   商品5 测试可乐           -> 统一绿茶 500ml/瓶 (价格不变), 采购数量 1->24, 库存 24
--   商品6/7 批量可乐/雪碧    -> 无任何单据/流水引用, 删除
--   供应商1 老陈成人          -> 武汉嘉禾粮油批发部 (被采购单1/2/4/5引用)
--   供应商2 测试供应商        -> 武汉汇源食品商行 (被采购单3引用)
--   供应商3/5-16 (老陈成人重复/提供商1-11) -> 无引用, 删除
--   单据金额按新进价/售价重算, 流水 after_stock 按时间顺序重推
--   单据时间从凌晨(02:42-05:10)调整到白天营业时段(09:10-15:30), 相对顺序不变
START TRANSACTION;

-- ===== 商品 =====
UPDATE product SET name='金龙鱼食用调和油', spec='5L/桶', unit='桶',
       purchase_price=55.00, sale_price=65.00, warning_threshold=10, stock=64, update_time=NOW()
WHERE id=1;
UPDATE product SET name='统一绿茶', spec='500ml/瓶', unit='瓶',
       warning_threshold=5, stock=24, update_time=NOW()
WHERE id=5;
DELETE FROM product WHERE id IN (6,7);

-- ===== 供应商 =====
UPDATE supplier SET name='武汉嘉禾粮油批发部', contact='王建军', phone='13871001122',
       address='武汉市硚口区汉正街粮油市场12号', remark='粮油批发'
WHERE id=1;
UPDATE supplier SET name='武汉汇源食品商行', contact='刘芳', phone='13986001234',
       address='武汉市江汉区食品工业园8号', remark='饮料零食批发'
WHERE id=2;
DELETE FROM supplier WHERE id IN (3,5,6,7,8,9,10,11,12,13,14,15,16);

-- ===== 采购单(数量/单价/金额/时间) =====
UPDATE purchase_order_detail SET price=55.00, amount=550.00  WHERE order_id IN (1,2);
UPDATE purchase_order SET total_amount=550.00,  create_time='2026-08-13 09:10:00' WHERE id=1;
UPDATE purchase_order SET total_amount=550.00,  create_time='2026-08-13 09:25:00' WHERE id=2;
UPDATE purchase_order_detail SET quantity=24, price=2.50, amount=60.00 WHERE order_id=3;
UPDATE purchase_order SET total_amount=60.00,   create_time='2026-08-13 09:40:00' WHERE id=3;
UPDATE purchase_order_detail SET quantity=20, price=55.00, amount=1100.00 WHERE order_id=4;
UPDATE purchase_order SET total_amount=1100.00, create_time='2026-08-13 11:00:00' WHERE id=4;
UPDATE purchase_order_detail SET quantity=30, price=55.00, amount=1650.00 WHERE order_id=5;
UPDATE purchase_order SET total_amount=1650.00, create_time='2026-08-13 14:00:00' WHERE id=5;

-- ===== 销售单(数量/单价/金额/时间) =====
UPDATE sale_order_detail SET quantity=1, price=65.00, amount=65.00  WHERE order_id=1;
UPDATE sale_order SET total_amount=65.00,  create_time='2026-08-13 10:30:00' WHERE id=1;
UPDATE sale_order_detail SET quantity=2, price=65.00, amount=130.00 WHERE order_id=2;
UPDATE sale_order SET total_amount=130.00, create_time='2026-08-13 11:20:00' WHERE id=2;
UPDATE sale_order_detail SET quantity=3, price=65.00, amount=195.00 WHERE order_id=3;
UPDATE sale_order SET total_amount=195.00, create_time='2026-08-13 15:30:00' WHERE id=3;

-- ===== 库存流水(after_stock 按时间序重推: 10/20/24/19/39/37/67/64) =====
UPDATE stock_record SET quantity=10, after_stock=10, create_time='2026-08-13 09:10:00' WHERE id=1;  -- 采购1 +10
UPDATE stock_record SET quantity=10, after_stock=20, create_time='2026-08-13 09:25:00' WHERE id=2;  -- 采购2 +10
UPDATE stock_record SET quantity=24, after_stock=24, create_time='2026-08-13 09:40:00' WHERE id=3;  -- 采购3 +24(绿茶)
UPDATE stock_record SET quantity=1,  after_stock=19, create_time='2026-08-13 10:30:00' WHERE id=4;  -- 销售1 -1
UPDATE stock_record SET quantity=20, after_stock=39, create_time='2026-08-13 11:00:00' WHERE id=5;  -- 采购4 +20
UPDATE stock_record SET quantity=2,  after_stock=37, create_time='2026-08-13 11:20:00' WHERE id=6;  -- 销售2 -2
UPDATE stock_record SET quantity=30, after_stock=67, create_time='2026-08-13 14:00:00' WHERE id=7;  -- 采购5 +30
UPDATE stock_record SET quantity=3,  after_stock=64, create_time='2026-08-13 15:30:00' WHERE id=8;  -- 销售3 -3

COMMIT;

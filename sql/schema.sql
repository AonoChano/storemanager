-- ============================================================
-- 进销存仓储管理系统 数据库脚本
-- 数据库: store_manager  字符集: utf8mb4
-- 用法: mysql -uroot -p < schema.sql  或 用 Navicat/DataGrip 直接运行
-- ============================================================

CREATE DATABASE IF NOT EXISTS store_manager DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE store_manager;

-- ------------------------------------------------------------
-- 1. 角色表
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `role`;
CREATE TABLE `role` (
    `id`          BIGINT AUTO_INCREMENT COMMENT '角色ID',
    `role_name`   VARCHAR(50)  NOT NULL COMMENT '角色名称',
    `description` VARCHAR(255) DEFAULT NULL COMMENT '角色描述',
    `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- ------------------------------------------------------------
-- 2. 用户表
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
    `id`          BIGINT AUTO_INCREMENT COMMENT '用户ID',
    `username`    VARCHAR(50)  NOT NULL COMMENT '登录用户名',
    `password`    VARCHAR(100) NOT NULL COMMENT '密码(BCrypt加密)',
    `real_name`   VARCHAR(50)  DEFAULT NULL COMMENT '真实姓名',
    `role_id`     BIGINT       NOT NULL COMMENT '角色ID',
    `status`      TINYINT      DEFAULT 1 COMMENT '状态:1启用 0禁用',
    `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    KEY `idx_role_id` (`role_id`),
    CONSTRAINT `fk_user_role` FOREIGN KEY (`role_id`) REFERENCES `role` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- ------------------------------------------------------------
-- 3. 操作日志表
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `operation_log`;
CREATE TABLE `operation_log` (
    `id`          BIGINT AUTO_INCREMENT COMMENT '日志ID',
    `user_id`     BIGINT       DEFAULT NULL COMMENT '操作人ID',
    `username`    VARCHAR(50)  DEFAULT NULL COMMENT '操作人',
    `module`      VARCHAR(50)  DEFAULT NULL COMMENT '模块:商品/采购/销售/库存...',
    `action`      VARCHAR(255) DEFAULT NULL COMMENT '操作内容',
    `ip`          VARCHAR(50)  DEFAULT NULL COMMENT '客户端IP',
    `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    PRIMARY KEY (`id`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

-- ------------------------------------------------------------
-- 4. 商品分类表
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `category`;
CREATE TABLE `category` (
    `id`          BIGINT AUTO_INCREMENT COMMENT '分类ID',
    `name`        VARCHAR(50) NOT NULL COMMENT '分类名称',
    `create_time` DATETIME    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品分类表';

-- ------------------------------------------------------------
-- 5. 商品表(含库存量/预警阈值)
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `product`;
CREATE TABLE `product` (
    `id`                BIGINT AUTO_INCREMENT COMMENT '商品ID',
    `name`              VARCHAR(100) NOT NULL COMMENT '商品名称',
    `barcode`           VARCHAR(50)  DEFAULT NULL COMMENT '条码',
    `category_id`       BIGINT       DEFAULT NULL COMMENT '分类ID',
    `spec`              VARCHAR(100) DEFAULT NULL COMMENT '规格:如500ml/瓶',
    `unit`              VARCHAR(20)  DEFAULT NULL COMMENT '单位:件/箱/瓶',
    `purchase_price`    DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '进价',
    `sale_price`        DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '售价',
    `stock`             INT          NOT NULL DEFAULT 0 COMMENT '当前库存量',
    `warning_threshold` INT          NOT NULL DEFAULT 0 COMMENT '库存预警阈值,低于该值标红',
    `status`            TINYINT      DEFAULT 1 COMMENT '状态:1在售 0下架',
    `create_time`       DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`       DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_barcode` (`barcode`),
    KEY `idx_category_id` (`category_id`),
    KEY `idx_name` (`name`),
    CONSTRAINT `fk_product_category` FOREIGN KEY (`category_id`) REFERENCES `category` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品表';

-- ------------------------------------------------------------
-- 6. 供应商表
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `supplier`;
CREATE TABLE `supplier` (
    `id`          BIGINT AUTO_INCREMENT COMMENT '供应商ID',
    `name`        VARCHAR(100) NOT NULL COMMENT '供应商名称',
    `contact`     VARCHAR(50)  DEFAULT NULL COMMENT '联系人',
    `phone`       VARCHAR(20)  DEFAULT NULL COMMENT '联系电话',
    `address`     VARCHAR(255) DEFAULT NULL COMMENT '地址',
    `remark`      VARCHAR(255) DEFAULT NULL COMMENT '备注',
    `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='供应商表';

-- ------------------------------------------------------------
-- 7. 采购单表(主表)
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `purchase_order`;
CREATE TABLE `purchase_order` (
    `id`           BIGINT AUTO_INCREMENT COMMENT '采购单ID',
    `order_no`     VARCHAR(30)  NOT NULL COMMENT '采购单号:如PO20260810001',
    `supplier_id`  BIGINT       DEFAULT NULL COMMENT '供应商ID',
    `total_amount` DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '总金额',
    `status`       TINYINT      DEFAULT 0 COMMENT '状态:0待入库 1已入库 2已取消',
    `operator_id`  BIGINT       DEFAULT NULL COMMENT '操作人ID',
    `remark`       VARCHAR(255) DEFAULT NULL COMMENT '备注',
    `create_time`  DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    KEY `idx_supplier_id` (`supplier_id`),
    KEY `idx_create_time` (`create_time`),
    CONSTRAINT `fk_purchase_supplier` FOREIGN KEY (`supplier_id`) REFERENCES `supplier` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='采购单表';

-- ------------------------------------------------------------
-- 8. 采购明细表(子表)
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `purchase_order_detail`;
CREATE TABLE `purchase_order_detail` (
    `id`         BIGINT AUTO_INCREMENT COMMENT '明细ID',
    `order_id`   BIGINT NOT NULL COMMENT '采购单ID',
    `product_id` BIGINT NOT NULL COMMENT '商品ID',
    `quantity`   INT    NOT NULL DEFAULT 1 COMMENT '数量',
    `price`      DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '进货单价',
    `amount`     DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '小计金额=数量*单价',
    PRIMARY KEY (`id`),
    KEY `idx_order_id` (`order_id`),
    KEY `idx_product_id` (`product_id`),
    CONSTRAINT `fk_pdetail_order`  FOREIGN KEY (`order_id`)   REFERENCES `purchase_order` (`id`),
    CONSTRAINT `fk_pdetail_product` FOREIGN KEY (`product_id`) REFERENCES `product` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='采购明细表';

-- ------------------------------------------------------------
-- 9. 销售单表(主表)
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `sale_order`;
CREATE TABLE `sale_order` (
    `id`            BIGINT AUTO_INCREMENT COMMENT '销售单ID',
    `order_no`      VARCHAR(30)  NOT NULL COMMENT '销售单号:如SO20260810001',
    `customer_name` VARCHAR(100) DEFAULT NULL COMMENT '客户名称(小门店直接存名字)',
    `total_amount`  DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '总金额',
    `status`        TINYINT      DEFAULT 0 COMMENT '状态:0待出库 1已出库 2已取消',
    `operator_id`   BIGINT       DEFAULT NULL COMMENT '操作人ID',
    `remark`        VARCHAR(255) DEFAULT NULL COMMENT '备注',
    `create_time`   DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='销售单表';

-- ------------------------------------------------------------
-- 10. 销售明细表(子表)
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `sale_order_detail`;
CREATE TABLE `sale_order_detail` (
    `id`         BIGINT AUTO_INCREMENT COMMENT '明细ID',
    `order_id`   BIGINT NOT NULL COMMENT '销售单ID',
    `product_id` BIGINT NOT NULL COMMENT '商品ID',
    `quantity`   INT    NOT NULL DEFAULT 1 COMMENT '数量',
    `price`      DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '销售单价',
    `amount`     DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '小计金额=数量*单价',
    PRIMARY KEY (`id`),
    KEY `idx_order_id` (`order_id`),
    KEY `idx_product_id` (`product_id`),
    CONSTRAINT `fk_sdetail_order`   FOREIGN KEY (`order_id`)   REFERENCES `sale_order` (`id`),
    CONSTRAINT `fk_sdetail_product` FOREIGN KEY (`product_id`) REFERENCES `product` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='销售明细表';

-- ------------------------------------------------------------
-- 11. 出入库流水表
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `stock_record`;
CREATE TABLE `stock_record` (
    `id`          BIGINT AUTO_INCREMENT COMMENT '流水ID',
    `product_id`  BIGINT      NOT NULL COMMENT '商品ID',
    `type`        TINYINT     NOT NULL COMMENT '类型:1入库 2出库',
    `quantity`    INT         NOT NULL COMMENT '变动数量(正数)',
    `after_stock` INT         NOT NULL COMMENT '变动后库存快照',
    `biz_type`    VARCHAR(30) NOT NULL COMMENT '业务类型:采购入库/销售出库/盘点调整',
    `order_no`    VARCHAR(30) DEFAULT NULL COMMENT '关联单据号',
    `operator_id` BIGINT      DEFAULT NULL COMMENT '操作人ID',
    `create_time` DATETIME    DEFAULT CURRENT_TIMESTAMP COMMENT '发生时间',
    PRIMARY KEY (`id`),
    KEY `idx_product_id` (`product_id`),
    KEY `idx_create_time` (`create_time`),
    KEY `idx_biz_type` (`biz_type`),
    CONSTRAINT `fk_stock_product` FOREIGN KEY (`product_id`) REFERENCES `product` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='出入库流水表';

-- ============================================================
-- 初始化数据: 角色 + 默认管理员账号(密码 123456, 生产环境务必改)
-- ============================================================
INSERT INTO `role` (`role_name`, `description`) VALUES
('管理员',   '拥有全部权限'),
('采购员',   '采购、入库相关'),
('销售员',   '销售、出库相关'),
('库管',     '库存、盘点相关');

-- 默认管理员: admin / 123456 (BCrypt加密)
INSERT INTO `user` (`username`, `password`, `real_name`, `role_id`, `status`) VALUES
('admin', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', '系统管理员', 1, 1);

INSERT INTO `category` (`name`) VALUES ('饮料'), ('零食'), ('日用品'), ('生鲜');
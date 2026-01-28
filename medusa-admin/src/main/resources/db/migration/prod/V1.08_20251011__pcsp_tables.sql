-- ============================================================================
-- PCSP (Premium Customer Service Package) 数字产品功能
-- ============================================================================

-- 1. 创建会员PCSP服务记录表
CREATE TABLE IF NOT EXISTS `mall_member_pcsp` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `member_id` BIGINT(20) NOT NULL COMMENT '会员ID',
  `product_id` BIGINT(20) NULL COMMENT '购买的PCSP产品ID',
  `order_sn` VARCHAR(64) NULL COMMENT '订单号',
  `package_type` INT(2) NOT NULL COMMENT '套餐类型: 1-3个月, 2-6个月, 3-12个月',
  `start_date` DATETIME NOT NULL COMMENT '开始日期',
  `expiry_date` DATETIME NOT NULL COMMENT '过期日期',
  `status` INT(2) NOT NULL DEFAULT 1 COMMENT '状态: 0-已过期, 1-有效, 2-已取消',
  `create_by` VARCHAR(64) NULL COMMENT '创建者',
  `create_time` DATETIME NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` VARCHAR(64) NULL COMMENT '更新者',
  `update_time` DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` VARCHAR(500) NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  INDEX `idx_member_id` (`member_id`),
  INDEX `idx_expiry_date` (`expiry_date`),
  INDEX `idx_status` (`status`),
  INDEX `idx_order_sn` (`order_sn`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会员PCSP服务记录表';

-- 2. 修改产品表，支持数字产品标识
ALTER TABLE `mall_product`
ADD COLUMN `is_digital` TINYINT(1) DEFAULT 0 COMMENT '是否数字产品: 0-实体, 1-数字' AFTER `status`,
ADD COLUMN `digital_type` VARCHAR(50) NULL COMMENT '数字产品类型: PCSP, LICENSE, etc.' AFTER `is_digital`;

-- 3. 修改产品SKU表，添加有效期字段（可选，用于数字产品）
ALTER TABLE `mall_product2`
ADD COLUMN `validity_months` INT(3) NULL COMMENT '有效期月数（数字产品用）' AFTER `unit`;

-- 4. 如果您已经创建了PCSP产品，更新标识
-- UPDATE `mall_product`
-- SET is_digital = 1, digital_type = 'PCSP'
-- WHERE product_id = 'PCSP';

-- 5. 如果需要，将model字段的值同步到validity_months
-- UPDATE `mall_product2`
-- SET validity_months = CAST(model AS SIGNED)
-- WHERE product_id = 'PCSP' AND model IS NOT NULL;

-- 6. 创建PCSP产品示例数据（可选，如果您还未在Admin Panel创建）
-- INSERT INTO `mall_product` (
--     `product_name`,
--     `sku`,
--     `price`,
--     `category_id`,
--     `is_digital`,
--     `digital_type`,
--     `validity_months`,
--     `status`,
--     `create_time`,
--     `description`
-- ) VALUES 
-- (
--     'Premium Customer Service Package (3 Months)',
--     'PCSP-3M',
--     99.00,
--     (SELECT id FROM mall_category WHERE category_name = 'Digital Products' LIMIT 1),
--     1,
--     'PCSP_3M',
--     3,
--     1,
--     NOW(),
--     'Get instant access to tracking numbers for 3 months!'
-- ),
-- (
--     'Premium Customer Service Package (6 Months)',
--     'PCSP-6M',
--     179.00,
--     (SELECT id FROM mall_category WHERE category_name = 'Digital Products' LIMIT 1),
--     1,
--     'PCSP_6M',
--     6,
--     1,
--     NOW(),
--     'Get instant access to tracking numbers for 6 months! Save 10%'
-- ),
-- (
--     'Premium Customer Service Package (12 Months)',
--     'PCSP-12M',
--     299.00,
--     (SELECT id FROM mall_category WHERE category_name = 'Digital Products' LIMIT 1),
--     1,
--     'PCSP_12M',
--     12,
--     1,
--     NOW(),
--     'Get instant access to tracking numbers for 12 months! Save 20%'
-- );


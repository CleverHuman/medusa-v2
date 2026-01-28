-- =============================================
-- V1.15 Vendor Order Integration - Phase 0
-- =============================================
-- Add vendor_id and product_origin fields to support platform + vendor integration

START TRANSACTION;

-- Add vendor_id to mall_order table (for orders containing vendor products)
ALTER TABLE `mall_order`
    ADD COLUMN `vendor_id` BIGINT(20) DEFAULT NULL COMMENT 'Vendor ID (if order contains vendor products)' AFTER `member_id`,
    ADD INDEX `idx_vendor_id` (`vendor_id`);

-- Add product_origin to mall_order_item table (record origin for each item)
ALTER TABLE `mall_order_item`
    ADD COLUMN `product_origin` TINYINT(1) DEFAULT 0 COMMENT 'Product origin: 0=platform, 1=vendor' AFTER `product_id`,
    ADD COLUMN `origin_id` BIGINT(20) DEFAULT NULL COMMENT 'Origin ID (vendor_id if product_origin=1)' AFTER `product_origin`,
    ADD INDEX `idx_product_origin` (`product_origin`, `origin_id`);

-- Mark all existing orders as platform orders (product_origin = 0)
UPDATE `mall_order_item` 
SET `product_origin` = 0, `origin_id` = NULL 
WHERE `product_origin` IS NULL;

-- Create table to track vendor order actions
CREATE TABLE IF NOT EXISTS `mall_order_vendor_action` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'Action ID',
  `order_id` VARCHAR(64) NOT NULL COMMENT 'Order ID',
  `vendor_id` BIGINT(20) NOT NULL COMMENT 'Vendor ID',
  `action_type` VARCHAR(50) NOT NULL COMMENT 'Action type: accept/reject/ship',
  `action_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Action time',
  `action_by` VARCHAR(64) COMMENT 'Action by (vendor member username)',
  `notes` TEXT COMMENT 'Action notes',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_vendor_id` (`vendor_id`),
  KEY `idx_action_time` (`action_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Vendor order action history';

COMMIT;


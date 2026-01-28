-- =============================================
-- V1.16 Vendor Product Management
-- =============================================
-- Add product approval workflow for vendor products

START TRANSACTION;

-- Create vendor product approval table
CREATE TABLE IF NOT EXISTS `mall_vendor_product_approval` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'Approval record ID',
  `product_id` BIGINT(20) NOT NULL COMMENT 'Product ID',
  `vendor_id` BIGINT(20) NOT NULL COMMENT 'Vendor ID',
  `approval_status` VARCHAR(50) NOT NULL DEFAULT 'pending_approval' COMMENT 'Status: draft/pending_approval/approved/rejected',
  `approval_notes` TEXT COMMENT 'Approval notes',
  `approver_id` BIGINT(20) COMMENT 'Approver user ID',
  `approver_name` VARCHAR(100) COMMENT 'Approver name',
  `approved_time` DATETIME COMMENT 'Approval time',
  `rejection_reason` TEXT COMMENT 'Rejection reason',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_product_approval` (`product_id`),
  KEY `idx_vendor_id` (`vendor_id`),
  KEY `idx_approval_status` (`approval_status`),
  KEY `idx_approved_time` (`approved_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Vendor product approval records';

-- Add product_origin and origin_id to mall_product table
-- Note: If these columns already exist, this will fail - that's OK, just skip this part
ALTER TABLE `mall_product`
    ADD COLUMN `product_origin` TINYINT(1) DEFAULT 0 COMMENT 'Product origin: 0=platform, 1=vendor' AFTER `channel`,
    ADD COLUMN `origin_id` BIGINT(20) DEFAULT NULL COMMENT 'Origin ID (vendor_id if product_origin=1)' AFTER `product_origin`,
    ADD INDEX `idx_product_origin` (`product_origin`, `origin_id`);

-- Add approval status tracking to mall_product table (optional, for quick lookup)
-- Note: This can be derived from mall_vendor_product_approval, but adding for performance
ALTER TABLE `mall_product`
    ADD COLUMN `approval_status` VARCHAR(50) DEFAULT NULL COMMENT 'Approval status for vendor products: pending_approval/approved/rejected' AFTER `status`,
    ADD INDEX `idx_approval_status` (`approval_status`);

-- For existing vendor products (if any), set default approval status
UPDATE `mall_product` 
SET `approval_status` = 'approved' 
WHERE `product_origin` = 1 AND `approval_status` IS NULL;

COMMIT;


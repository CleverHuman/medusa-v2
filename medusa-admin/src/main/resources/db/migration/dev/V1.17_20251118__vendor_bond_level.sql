-- =============================================
-- Vendor Bond & Level System
-- =============================================
-- Migration: V1.17_20251118__vendor_bond_level.sql
-- Description: Add Bond, Level, and Sales Points to Vendor system

-- 1. Add Bond, Level, and Sales Points fields to mall_vendor table
ALTER TABLE `mall_vendor`
ADD COLUMN `bond` DECIMAL(18,2) DEFAULT 0.00 COMMENT 'Vendor Bond Amount (USD)',
ADD COLUMN `level` INT DEFAULT 1 COMMENT 'Vendor Level (starts at 1)',
ADD COLUMN `sales_points` BIGINT(20) DEFAULT 0 COMMENT 'Sales Points (1 point per $1 sold)';

-- 2. Create Vendor Level History Table (track level upgrades)
CREATE TABLE IF NOT EXISTS `mall_vendor_level_history` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'Record ID',
  `vendor_id` BIGINT(20) NOT NULL COMMENT 'Vendor ID',
  `old_level` INT NOT NULL COMMENT 'Old Level',
  `new_level` INT NOT NULL COMMENT 'New Level',
  `old_points` BIGINT(20) NOT NULL COMMENT 'Old Sales Points',
  `new_points` BIGINT(20) NOT NULL COMMENT 'New Sales Points',
  `trigger_order_id` VARCHAR(50) COMMENT 'Order ID that triggered the upgrade',
  `trigger_amount` DECIMAL(18,2) COMMENT 'Order amount that contributed to points',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Upgrade Time',
  
  PRIMARY KEY (`id`),
  KEY `idx_vendor_id` (`vendor_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Vendor Level Upgrade History';

-- 3. Initialize existing vendors with default values (if any exist)
-- Bond and Level should be set manually by admin during onboarding
-- Sales points start at 0 for all vendors

-- Note: Level calculation:
-- - Level 1: 0-999 points (default, requires $1,000 bond for $1,000 max sales)
-- - Level 2: 1,000-9,999 points (requires $1,000 bond for $2,000 max sales)
-- - Level 3: 10,000-99,999 points (requires $1,000 bond for $3,000 max sales)
-- - Level 4: 100,000-999,999 points (requires $1,000 bond for $4,000 max sales)
-- - Level 5: 1,000,000+ points (requires $1,000 bond for $5,000 max sales)
-- Maximum sales limit = Bond × Level

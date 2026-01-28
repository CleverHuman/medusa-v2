-- =============================================
-- Vendor Module Database Design
-- =============================================

-- 1. Vendor Application Table
CREATE TABLE IF NOT EXISTS `mall_vendor_application` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'Application ID',
  `application_id` VARCHAR(50) NOT NULL COMMENT 'Application Number (Unique Identifier)',
  `vendor_name` VARCHAR(200) NOT NULL COMMENT 'Vendor Name',
  
  -- Basic Information
  `has_market_experience` TINYINT(1) DEFAULT 0 COMMENT 'Has Market Experience (0=No, 1=Yes)',
  `existing_markets` VARCHAR(500) COMMENT 'Existing Market Names',
  `experience_years` INT COMMENT 'Years of Experience',
  
  -- PGP and Wallet Information
  `pgp_signature_url` TEXT COMMENT 'PGP Public Key (full key content, 1000-2000 chars)',
  `btc_wallet` VARCHAR(200) NULL DEFAULT NULL COMMENT 'BTC Wallet Address (collected after approval)',
  `xmr_wallet` VARCHAR(200) NULL DEFAULT NULL COMMENT 'XMR Wallet Address (collected after approval)',
  `usdt_wallet` VARCHAR(200) NULL DEFAULT NULL COMMENT 'USDT Wallet Address (collected after approval)',
  
  -- Location Information
  `location` VARCHAR(100) COMMENT 'Geographic Location',
  
  -- Product Information
  `product_categories` VARCHAR(1000) COMMENT 'Product Categories (JSON Array)',
  `stock_volume` VARCHAR(50) COMMENT 'Stock Volume (small/medium/large/xlarge)',
  `offline_delivery` TINYINT(1) DEFAULT 0 COMMENT 'Supports Offline Delivery (0=No, 1=Yes)',
  `product_description` TEXT COMMENT 'Product Description',
  
  -- Primary Contact Methods
  `primary_telegram` VARCHAR(100) COMMENT 'Primary Telegram Contact',
  `primary_signal` VARCHAR(100) COMMENT 'Primary Signal Contact',
  `primary_jabber` VARCHAR(100) COMMENT 'Primary Jabber Contact',
  `primary_email` VARCHAR(100) COMMENT 'Primary Email Contact',
  
  -- Secondary Contact Methods
  `secondary_telegram` VARCHAR(100) COMMENT 'Secondary Telegram Contact',
  `secondary_signal` VARCHAR(100) COMMENT 'Secondary Signal Contact',
  `secondary_jabber` VARCHAR(100) COMMENT 'Secondary Jabber Contact',
  `secondary_email` VARCHAR(100) COMMENT 'Secondary Email Contact',
  
  -- Review Status
  `status` VARCHAR(50) DEFAULT 'pending' COMMENT 'Application Status (pending/under_review/interview_required/approved/rejected)',
  `review_notes` TEXT COMMENT 'Review Notes',
  `reviewer_id` BIGINT(20) COMMENT 'Reviewer ID',
  `reviewed_time` DATETIME COMMENT 'Review Time',
  
  -- Review Progress Tracking
  `review_stage` VARCHAR(50) COMMENT 'Current Review Stage',
  `review_progress` INT DEFAULT 0 COMMENT 'Review Progress Percentage (0-100)',
  
  -- Associated Vendor ID (Created after approval)
  `vendor_id` BIGINT(20) COMMENT 'Associated Vendor ID',
  
  -- Standard Fields
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Create Time',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update Time',
  `create_by` VARCHAR(64) COMMENT 'Creator',
  `update_by` VARCHAR(64) COMMENT 'Updater',
  `remark` VARCHAR(500) COMMENT 'Remark',
  `del_flag` CHAR(1) DEFAULT '0' COMMENT 'Delete Flag (0=Exist 2=Deleted)',
  
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_application_id` (`application_id`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_vendor_name` (`vendor_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Vendor Application Table';

-- 2. Vendor Information Table
CREATE TABLE IF NOT EXISTS `mall_vendor` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'Vendor ID',
  `vendor_code` VARCHAR(50) NOT NULL COMMENT 'Vendor Code (Unique)',
  `vendor_name` VARCHAR(200) NOT NULL COMMENT 'Vendor Name',
  
  -- Basic Information
  `description` TEXT COMMENT 'Vendor Description',
  `logo_url` VARCHAR(500) COMMENT 'Logo Image URL',
  `banner_url` VARCHAR(500) COMMENT 'Banner Image URL',
  
  -- Contact Information
  `contact_telegram` VARCHAR(100) COMMENT 'Telegram Contact',
  `contact_signal` VARCHAR(100) COMMENT 'Signal Contact',
  `contact_jabber` VARCHAR(100) COMMENT 'Jabber Contact',
  `contact_email` VARCHAR(100) COMMENT 'Email Contact',
  
  -- PGP and Wallet Information
  `pgp_public_key_url` VARCHAR(500) COMMENT 'PGP Public Key URL',
  `btc_wallet` VARCHAR(200) COMMENT 'BTC Wallet Address',
  `xmr_wallet` VARCHAR(200) COMMENT 'XMR Wallet Address',
  `usdt_wallet` VARCHAR(200) COMMENT 'USDT Wallet Address',
  
  -- Business Information
  `product_categories` VARCHAR(1000) COMMENT 'Product Categories (JSON Array)',
  `location` VARCHAR(100) COMMENT 'Geographic Location',
  `rating` DECIMAL(3,2) DEFAULT 0.00 COMMENT 'Vendor Rating (0-5)',
  `total_sales` BIGINT(20) DEFAULT 0 COMMENT 'Total Sales Amount',
  `total_orders` INT DEFAULT 0 COMMENT 'Total Order Count',
  
  -- Associated Shop (if any)
  `shop_id` BIGINT(20) COMMENT 'Associated Shop ID',
  
  -- Status Management
  `status` TINYINT(1) DEFAULT 1 COMMENT 'Status (0=Disabled, 1=Enabled)',
  `is_featured` TINYINT(1) DEFAULT 0 COMMENT 'Is Featured (0=No, 1=Yes)',
  `sort_order` INT DEFAULT 0 COMMENT 'Sort Order',
  
  -- Application Information
  `application_id` BIGINT(20) COMMENT 'Source Application ID',
  `approved_time` DATETIME COMMENT 'Approval Time',
  `approved_by` VARCHAR(64) COMMENT 'Approved By',
  
  -- Standard Fields
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Create Time',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update Time',
  `create_by` VARCHAR(64) COMMENT 'Creator',
  `update_by` VARCHAR(64) COMMENT 'Updater',
  `remark` VARCHAR(500) COMMENT 'Remark',
  `del_flag` CHAR(1) DEFAULT '0' COMMENT 'Delete Flag (0=Exist 2=Deleted)',
  
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_vendor_code` (`vendor_code`),
  KEY `idx_vendor_name` (`vendor_name`),
  KEY `idx_status` (`status`),
  KEY `idx_application_id` (`application_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Vendor Information Table';

-- 3. Review History Table
CREATE TABLE IF NOT EXISTS `mall_vendor_review_history` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'Record ID',
  `application_id` BIGINT(20) NOT NULL COMMENT 'Application ID',
  `action` VARCHAR(50) NOT NULL COMMENT 'Action Type (submit/review/approve/reject/request_info)',
  `old_status` VARCHAR(50) COMMENT 'Old Status',
  `new_status` VARCHAR(50) COMMENT 'New Status',
  `notes` TEXT COMMENT 'Action Notes',
  `operator_id` BIGINT(20) COMMENT 'Operator ID',
  `operator_name` VARCHAR(100) COMMENT 'Operator Name',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Operation Time',
  
  PRIMARY KEY (`id`),
  KEY `idx_application_id` (`application_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Review History Table';

-- Insert initial permission menus (Note: adjust parent_id based on your actual Mall menu ID)
-- You may need to manually adjust the parent_id after execution
-- Assuming Mall menu parent_id is 2000, adjust accordingly

-- Get the Mall menu ID first, then insert
-- These are sample SQL, you need to adjust based on your actual menu structure

-- INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES
-- ('Vendor Management', 2000, 6, 'vendor', NULL, 1, 0, 'M', '0', '0', NULL, 'peoples', 'admin', NOW(), 'admin', NOW(), 'Vendor module menu');

-- After creating the parent menu, get its ID and use it for child menus
-- INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES
-- ('Vendor Application', PARENT_MENU_ID, 1, 'application', 'mall/vendor/application/index', 1, 0, 'C', '0', '0', 'mall:vendor:application:list', 'form', 'admin', NOW(), 'admin', NOW(), 'Vendor application management'),
-- ('Vendor List', PARENT_MENU_ID, 2, 'list', 'mall/vendor/list/index', 1, 0, 'C', '0', '0', 'mall:vendor:list', 'peoples', 'admin', NOW(), 'admin', NOW(), 'Vendor list management');


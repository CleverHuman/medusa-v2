-- Add LTC (Litecoin) to pay_type dictionary
-- Date: 2025-12-25
-- Description: Add LTC payment type with value '4' to support Litecoin payments

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark)
VALUES (3, 'LTC', '4', 'pay_type', NULL, 'default', 'N', '0', 'admin', NOW(), '', NULL, 'Litecoin');

-- =============================================
-- Vendor Module Database Design
-- =============================================
-- Step 1: create vendor tables

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


-- Step 2: batch vendor menu
-- 假设你的 Mall 菜单 ID 是 2000（需要根据实际情况调整）
SET @mall_menu_id = 1;

-- 1. 创建 Vendor Management 主菜单
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time) 
VALUES ('Vendor Management', @mall_menu_id, 6, 'vendor', NULL, 1, 0, 'M', '0', '0', NULL, 'peoples', 'admin', NOW());

SET @vendor_menu_id = LAST_INSERT_ID();

-- 2. 创建 Vendor Application 子菜单
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time) 
VALUES ('Vendor Application', @vendor_menu_id, 1, 'application', 'mall/vendor/application/index', 1, 0, 'C', '0', '0', 'mall:vendor:application:list', 'form', 'admin', NOW());

SET @app_menu_id = LAST_INSERT_ID();

-- 3. 创建 Vendor List 子菜单
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time) 
VALUES ('Vendor List', @vendor_menu_id, 2, 'list', 'mall/vendor/list/index', 1, 0, 'C', '0', '0', 'mall:vendor:list', 'peoples', 'admin', NOW());

SET @list_menu_id = LAST_INSERT_ID();

-- 4. 添加 Vendor Application 按钮权限
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time) VALUES
('Query Application', @app_menu_id, 1, '#', '', 1, 0, 'F', '0', '0', 'mall:vendor:application:query', '#', 'admin', NOW()),
('Add Application', @app_menu_id, 2, '#', '', 1, 0, 'F', '0', '0', 'mall:vendor:application:add', '#', 'admin', NOW()),
('Edit Application', @app_menu_id, 3, '#', '', 1, 0, 'F', '0', '0', 'mall:vendor:application:edit', '#', 'admin', NOW()),
('Delete Application', @app_menu_id, 4, '#', '', 1, 0, 'F', '0', '0', 'mall:vendor:application:remove', '#', 'admin', NOW()),
('Approve Application', @app_menu_id, 5, '#', '', 1, 0, 'F', '0', '0', 'mall:vendor:application:approve', '#', 'admin', NOW()),
('Reject Application', @app_menu_id, 6, '#', '', 1, 0, 'F', '0', '0', 'mall:vendor:application:reject', '#', 'admin', NOW()),
('Export Application', @app_menu_id, 7, '#', '', 1, 0, 'F', '0', '0', 'mall:vendor:application:export', '#', 'admin', NOW());

-- 5. 添加 Vendor List 按钮权限
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time) VALUES
('Query Vendor', @list_menu_id, 1, '#', '', 1, 0, 'F', '0', '0', 'mall:vendor:query', '#', 'admin', NOW()),
('Add Vendor', @list_menu_id, 2, '#', '', 1, 0, 'F', '0', '0', 'mall:vendor:add', '#', 'admin', NOW()),
('Edit Vendor', @list_menu_id, 3, '#', '', 1, 0, 'F', '0', '0', 'mall:vendor:edit', '#', 'admin', NOW()),
('Delete Vendor', @list_menu_id, 4, '#', '', 1, 0, 'F', '0', '0', 'mall:vendor:remove', '#', 'admin', NOW()),
('Export Vendor', @list_menu_id, 5, '#', '', 1, 0, 'F', '0', '0', 'mall:vendor:export', '#', 'admin', NOW());

-- 6. 给管理员角色分配权限（假设角色 ID 为 1）
INSERT INTO sys_role_menu (role_id, menu_id) 
SELECT 1, menu_id FROM sys_menu WHERE menu_name IN ('Vendor Management', 'Vendor Application', 'Vendor List');

-- Step 3: batch interview database
-- =============================================
-- Vendor Interview Schedule Table
-- =============================================

CREATE TABLE IF NOT EXISTS `mall_vendor_interview` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'Interview ID',
  `application_id` BIGINT(20) NOT NULL COMMENT 'Application ID',
  `application_number` VARCHAR(50) NOT NULL COMMENT 'Application Number',
  `vendor_name` VARCHAR(200) NOT NULL COMMENT 'Vendor Name',
  
  -- Interview Schedule
  `interview_date` DATE NOT NULL COMMENT 'Interview Date',
  `interview_time` TIME NOT NULL COMMENT 'Interview Time',
  `interview_datetime` DATETIME NOT NULL COMMENT 'Full Interview DateTime',
  `duration_minutes` INT DEFAULT 60 COMMENT 'Duration (minutes)',
  `timezone` VARCHAR(50) DEFAULT 'UTC' COMMENT 'Timezone',
  
  -- Interview Details
  `interview_type` VARCHAR(50) DEFAULT 'video' COMMENT 'Interview Type (video/audio/text)',
  `platform` VARCHAR(50) COMMENT 'Platform (Telegram/Signal/Jitsi/etc)',
  `meeting_link` VARCHAR(500) COMMENT 'Meeting Link',
  `meeting_id` VARCHAR(100) COMMENT 'Meeting ID',
  `meeting_password` VARCHAR(100) COMMENT 'Meeting Password',
  
  -- Interview Status
  `status` VARCHAR(50) DEFAULT 'scheduled' COMMENT 'Status (scheduled/confirmed/rescheduled/completed/cancelled/no_show)',
  `vendor_confirmed` TINYINT(1) DEFAULT 0 COMMENT 'Vendor Confirmed (0=No, 1=Yes)',
  `vendor_confirmed_time` DATETIME COMMENT 'Vendor Confirmation Time',
  
  -- Interviewer Information
  `interviewer_id` BIGINT(20) COMMENT 'Interviewer User ID',
  `interviewer_name` VARCHAR(100) COMMENT 'Interviewer Name',
  `interviewer_contact` VARCHAR(200) COMMENT 'Interviewer Contact',
  
  -- Notes and Results
  `preparation_notes` TEXT COMMENT 'Preparation Notes for Interviewer',
  `interview_notes` TEXT COMMENT 'Interview Notes',
  `interview_result` VARCHAR(50) COMMENT 'Result (passed/failed/needs_second_round)',
  `interview_score` INT COMMENT 'Interview Score (0-100)',
  
  -- Reminder and Notification
  `reminder_sent` TINYINT(1) DEFAULT 0 COMMENT 'Reminder Sent (0=No, 1=Yes)',
  `reminder_sent_time` DATETIME COMMENT 'Reminder Sent Time',
  `notification_sent` TINYINT(1) DEFAULT 0 COMMENT 'Notification Sent (0=No, 1=Yes)',
  `notification_sent_time` DATETIME COMMENT 'Notification Sent Time',
  
  -- Rescheduling
  `reschedule_count` INT DEFAULT 0 COMMENT 'Reschedule Count',
  `reschedule_reason` TEXT COMMENT 'Reschedule Reason',
  `previous_datetime` DATETIME COMMENT 'Previous DateTime (for rescheduling)',
  
  -- Standard Fields
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Create Time',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update Time',
  `create_by` VARCHAR(64) COMMENT 'Creator',
  `update_by` VARCHAR(64) COMMENT 'Updater',
  `remark` VARCHAR(500) COMMENT 'Remark',
  `del_flag` CHAR(1) DEFAULT '0' COMMENT 'Delete Flag (0=Exist 2=Deleted)',
  
  PRIMARY KEY (`id`),
  KEY `idx_application_id` (`application_id`),
  KEY `idx_interview_datetime` (`interview_datetime`),
  KEY `idx_status` (`status`),
  KEY `idx_interviewer_id` (`interviewer_id`),
  KEY `idx_interview_date` (`interview_date`),
  KEY `idx_calendar_view` (`interview_date`, `status`, `del_flag`),
  CONSTRAINT `fk_interview_application` FOREIGN KEY (`application_id`) 
    REFERENCES `mall_vendor_application` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Vendor Interview Schedule Table';

-- Add interview fields to application table
-- Check if columns exist before adding (use separate statements for compatibility)
SET @exist := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
               WHERE TABLE_SCHEMA = DATABASE() 
               AND TABLE_NAME = 'mall_vendor_application' 
               AND COLUMN_NAME = 'has_scheduled_interview');

SET @sqlstmt := IF(@exist > 0, 'SELECT ''Column has_scheduled_interview already exists''', 
                'ALTER TABLE `mall_vendor_application` ADD COLUMN `has_scheduled_interview` TINYINT(1) DEFAULT 0 COMMENT ''Has Scheduled Interview'' AFTER `review_progress`');
PREPARE stmt FROM @sqlstmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @exist := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
               WHERE TABLE_SCHEMA = DATABASE() 
               AND TABLE_NAME = 'mall_vendor_application' 
               AND COLUMN_NAME = 'next_interview_time');

SET @sqlstmt := IF(@exist > 0, 'SELECT ''Column next_interview_time already exists''', 
                'ALTER TABLE `mall_vendor_application` ADD COLUMN `next_interview_time` DATETIME COMMENT ''Next Interview Time'' AFTER `has_scheduled_interview`');
PREPARE stmt FROM @sqlstmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Create index for interview queries (check if exists)
SET @exist := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS 
               WHERE TABLE_SCHEMA = DATABASE() 
               AND TABLE_NAME = 'mall_vendor_application' 
               AND INDEX_NAME = 'idx_interview_status');

SET @sqlstmt := IF(@exist > 0, 'SELECT ''Index idx_interview_status already exists''', 
                'CREATE INDEX `idx_interview_status` ON `mall_vendor_application` (`status`, `has_scheduled_interview`)');
PREPARE stmt FROM @sqlstmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Step 4: add interview menu
-- =============================================
-- Add Interview Schedule Menu to System
-- =============================================

-- Step 1: Find Vendor Management Menu (wherever it is)
-- It could be under System or Mall, we'll find it dynamically
SELECT @vendor_menu_id := menu_id 
FROM sys_menu 
WHERE menu_name = 'Vendor Management' 
  AND menu_type = 'M' 
LIMIT 1;

-- Display where Vendor Management was found
SELECT CONCAT('Found Vendor Management menu with ID: ', IFNULL(@vendor_menu_id, 'NOT FOUND')) as Info;

-- If Vendor Management doesn't exist, find System menu and create it there
SELECT @system_menu_id := menu_id 
FROM sys_menu 
WHERE menu_name = 'System' 
  AND menu_type = 'M' 
LIMIT 1;

-- Step 2: Create Vendor Management if it doesn't exist (under System)
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT 'Vendor Management', 
       IFNULL(@system_menu_id, 0), 
       10, 
       'vendor', 
       NULL, 
       1, 0, 'M', '0', '0', NULL, 'peoples', 'admin', NOW(), 'admin', NOW(), 'Vendor Management'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_name = 'Vendor Management' AND menu_type = 'M');

-- Get Vendor Management menu ID again (in case we just created it)
SELECT @vendor_menu_id := menu_id 
FROM sys_menu 
WHERE menu_name = 'Vendor Management' 
  AND menu_type = 'M' 
LIMIT 1;

-- Step 3: Add Vendor Application Menu (if not exists)
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT 'Vendor Applications', @vendor_menu_id, 1, 'application', 'mall/vendor/application/index', 1, 0, 'C', '0', '0', 'mall:vendor:application:list', 'form', 'admin', NOW(), 'admin', NOW(), 'Vendor Application Management'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE path = 'application' AND parent_id = @vendor_menu_id);

-- Step 4: Add Interview Schedule Menu (NEW!)
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT 'Interview Schedule', @vendor_menu_id, 2, 'interview', 'mall/vendor/interview/index', 1, 0, 'C', '0', '0', 'mall:vendor:interview:list', 'date', 'admin', NOW(), 'admin', NOW(), 'Interview Schedule Management'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE path = 'interview' AND parent_id = @vendor_menu_id);

-- Get Interview Schedule menu ID for adding permissions
SELECT @interview_menu_id := menu_id FROM sys_menu WHERE path = 'interview' AND parent_id = @vendor_menu_id LIMIT 1;

-- Step 5: Add Interview Management Permissions (Sub-menus/Buttons)
-- Query Permission
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT 'Interview Query', @interview_menu_id, 1, '#', '', 1, 0, 'F', '0', '0', 'mall:vendor:interview:query', '#', 'admin', NOW(), 'admin', NOW(), 'Interview Query Permission'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'mall:vendor:interview:query' AND parent_id = @interview_menu_id);

-- Add Permission
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT 'Schedule Interview', @interview_menu_id, 2, '#', '', 1, 0, 'F', '0', '0', 'mall:vendor:interview:add', '#', 'admin', NOW(), 'admin', NOW(), 'Schedule Interview Permission'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'mall:vendor:interview:add' AND parent_id = @interview_menu_id);

-- Edit Permission
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT 'Edit Interview', @interview_menu_id, 3, '#', '', 1, 0, 'F', '0', '0', 'mall:vendor:interview:edit', '#', 'admin', NOW(), 'admin', NOW(), 'Edit Interview Permission'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'mall:vendor:interview:edit' AND parent_id = @interview_menu_id);

-- Remove Permission
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT 'Delete Interview', @interview_menu_id, 4, '#', '', 1, 0, 'F', '0', '0', 'mall:vendor:interview:remove', '#', 'admin', NOW(), 'admin', NOW(), 'Delete Interview Permission'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'mall:vendor:interview:remove' AND parent_id = @interview_menu_id);

-- Export Permission
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT 'Export Interview', @interview_menu_id, 5, '#', '', 1, 0, 'F', '0', '0', 'mall:vendor:interview:export', '#', 'admin', NOW(), 'admin', NOW(), 'Export Interview Permission'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'mall:vendor:interview:export' AND parent_id = @interview_menu_id);

-- Step 6: Add Schedule Interview permission to Application menu (for Schedule button)
SELECT @application_menu_id := menu_id FROM sys_menu WHERE path = 'application' AND parent_id = @vendor_menu_id LIMIT 1;

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT 'Schedule Interview', @application_menu_id, 10, '#', '', 1, 0, 'F', '0', '0', 'mall:vendor:interview:schedule', '#', 'admin', NOW(), 'admin', NOW(), 'Schedule Interview from Application'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'mall:vendor:interview:schedule' AND parent_id = @application_menu_id);

-- Step 7: Grant all Interview permissions to admin role
-- Find admin role ID
SELECT @admin_role_id := role_id FROM sys_role WHERE role_key = 'admin' LIMIT 1;

-- Grant Interview Schedule menu access
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT @admin_role_id, menu_id
FROM sys_menu
WHERE menu_name = 'Interview Schedule' AND parent_id = @vendor_menu_id
AND NOT EXISTS (
    SELECT 1 FROM sys_role_menu 
    WHERE role_id = @admin_role_id 
    AND menu_id = (SELECT menu_id FROM sys_menu WHERE menu_name = 'Interview Schedule' AND parent_id = @vendor_menu_id LIMIT 1)
);

-- Grant all Interview permissions to admin role
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT @admin_role_id, menu_id
FROM sys_menu
WHERE parent_id = @interview_menu_id
AND NOT EXISTS (
    SELECT 1 FROM sys_role_menu rm
    WHERE rm.role_id = @admin_role_id AND rm.menu_id = sys_menu.menu_id
);

-- Display results
SELECT '✅ Interview Schedule Menu Added Successfully!' as Status;
SELECT 'Mall > Vendor Management > Interview Schedule' as MenuPath;
SELECT menu_id, menu_name, path, perms, icon FROM sys_menu WHERE menu_name LIKE '%Interview%' OR perms LIKE '%interview%';


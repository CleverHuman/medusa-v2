-- Add Market and Other/Custom to source_type dictionary
-- Date: 2025-12-25
-- Description: Add Market (12) and Other/Custom (99) channel types for Admin Panel created orders
-- 
-- Channel Code Logic:
-- User-submitted orders: 0 (OS), 1 (Telegram)
-- Admin Panel created orders: 10 (OS), 11 (Telegram), 12 (Market), 99 (Other/Custom)
-- The first digit "1" or "9" indicates Admin Panel created order

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark)
VALUES 
  (10, 'OS (Admin)', '10', 'source_type', NULL, 'primary', 'N', '0', 'admin', NOW(), '', NULL, 'OS Source - Admin Panel Created'),
  (11, 'Telegram (Admin)', '11', 'source_type', NULL, 'success', 'N', '0', 'admin', NOW(), '', NULL, 'Telegram Source - Admin Panel Created'),
  (12, 'Market (Admin)', '12', 'source_type', NULL, 'warning', 'N', '0', 'admin', NOW(), '', NULL, 'Market Source - Admin Panel Created'),
  (99, 'Other/Custom (Admin)', '99', 'source_type', NULL, 'info', 'N', '0', 'admin', NOW(), '', NULL, 'Other/Custom Source - Admin Panel Created');

-- =============================================
-- V1.13 Vendor Interview Slot & Notification System
-- =============================================
-- This script adds slot management and notification features
-- for the vendor interview scheduling system

START TRANSACTION;

-- =============================================
-- Step 1: Create Interview Slot Table
-- =============================================
CREATE TABLE IF NOT EXISTS `mall_vendor_interview_slot` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'Slot ID',
  `cm_id` BIGINT(20) NOT NULL COMMENT 'CM (interviewer) user ID',
  `slot_start` DATETIME NOT NULL COMMENT 'Slot start time (1 hour block)',
  `slot_end` DATETIME NOT NULL COMMENT 'Slot end time',
  `status` ENUM('available','reserved','completed','cancelled') DEFAULT 'available' COMMENT 'Slot status',
  `reserved_interview_id` BIGINT(20) NULL COMMENT 'Linked interview ID when reserved',
  `reserved_vendor_id` BIGINT(20) NULL COMMENT 'Vendor ID who booked this slot',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Create Time',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update Time',
  `create_by` VARCHAR(64) COMMENT 'Creator',
  `update_by` VARCHAR(64) COMMENT 'Updater',
  `remark` VARCHAR(500) COMMENT 'Remark',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_cm_slot` (`cm_id`, `slot_start`),
  KEY `idx_cm_id` (`cm_id`),
  KEY `idx_slot_start` (`slot_start`),
  KEY `idx_status` (`status`),
  KEY `idx_reserved_vendor` (`reserved_vendor_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Vendor interview available slots (CM managed)';

-- =============================================
-- Step 2: Extend Interview Table with Slot Fields
-- =============================================
-- Check and add slot_id column
SET @exist := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
               WHERE TABLE_SCHEMA = DATABASE() 
               AND TABLE_NAME = 'mall_vendor_interview' 
               AND COLUMN_NAME = 'slot_id');

SET @sqlstmt := IF(@exist > 0, 
                'SELECT ''Column slot_id already exists'' as Info', 
                'ALTER TABLE `mall_vendor_interview` ADD COLUMN `slot_id` BIGINT(20) NULL COMMENT ''Linked slot ID'' AFTER `application_id`');
PREPARE stmt FROM @sqlstmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Check and add cm_id column
SET @exist := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
               WHERE TABLE_SCHEMA = DATABASE() 
               AND TABLE_NAME = 'mall_vendor_interview' 
               AND COLUMN_NAME = 'cm_id');

SET @sqlstmt := IF(@exist > 0, 
                'SELECT ''Column cm_id already exists'' as Info', 
                'ALTER TABLE `mall_vendor_interview` ADD COLUMN `cm_id` BIGINT(20) NULL COMMENT ''CM user ID'' AFTER `interviewer_id`');
PREPARE stmt FROM @sqlstmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Check and add notification columns
SET @exist := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
               WHERE TABLE_SCHEMA = DATABASE() 
               AND TABLE_NAME = 'mall_vendor_interview' 
               AND COLUMN_NAME = 'cm_notified');

SET @sqlstmt := IF(@exist > 0, 
                'SELECT ''Column cm_notified already exists'' as Info', 
                'ALTER TABLE `mall_vendor_interview` ADD COLUMN `cm_notified` TINYINT(1) DEFAULT 0 COMMENT ''Notification sent to CM'' AFTER `cm_id`');
PREPARE stmt FROM @sqlstmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @exist := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
               WHERE TABLE_SCHEMA = DATABASE() 
               AND TABLE_NAME = 'mall_vendor_interview' 
               AND COLUMN_NAME = 'vendor_notified');

SET @sqlstmt := IF(@exist > 0, 
                'SELECT ''Column vendor_notified already exists'' as Info', 
                'ALTER TABLE `mall_vendor_interview` ADD COLUMN `vendor_notified` TINYINT(1) DEFAULT 0 COMMENT ''Notification sent to vendor'' AFTER `cm_notified`');
PREPARE stmt FROM @sqlstmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Add foreign key for slot_id (optional, can be added later if needed)
-- ALTER TABLE `mall_vendor_interview` 
--   ADD CONSTRAINT `fk_interview_slot` FOREIGN KEY (`slot_id`) 
--   REFERENCES `mall_vendor_interview_slot` (`id`) ON DELETE SET NULL;

-- =============================================
-- Step 3: Create Notification Table
-- =============================================
CREATE TABLE IF NOT EXISTS `mall_vendor_interview_notification` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'Notification ID',
  `interview_id` BIGINT(20) NOT NULL COMMENT 'Interview ID',
  `recipient_type` ENUM('vendor','cm') NOT NULL COMMENT 'Recipient type',
  `recipient_id` BIGINT(20) NOT NULL COMMENT 'Recipient user/vendor ID',
  `message` TEXT COMMENT 'Notification message',
  `status` ENUM('pending','sent','failed') DEFAULT 'pending' COMMENT 'Notification status',
  `sent_at` DATETIME NULL COMMENT 'Sent time',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Create Time',
  PRIMARY KEY (`id`),
  KEY `idx_interview_id` (`interview_id`),
  KEY `idx_recipient` (`recipient_type`, `recipient_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Interview notification records';

-- =============================================
-- Step 4: Ensure Application Table Has Interview Fields
-- =============================================
-- These should already exist from V1.12, but check anyway
SET @exist := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
               WHERE TABLE_SCHEMA = DATABASE() 
               AND TABLE_NAME = 'mall_vendor_application' 
               AND COLUMN_NAME = 'has_scheduled_interview');

SET @sqlstmt := IF(@exist > 0, 
                'SELECT ''Column has_scheduled_interview already exists'' as Info', 
                'ALTER TABLE `mall_vendor_application` ADD COLUMN `has_scheduled_interview` TINYINT(1) DEFAULT 0 COMMENT ''Has scheduled interview'' AFTER `review_progress`');
PREPARE stmt FROM @sqlstmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @exist := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
               WHERE TABLE_SCHEMA = DATABASE() 
               AND TABLE_NAME = 'mall_vendor_application' 
               AND COLUMN_NAME = 'next_interview_time');

SET @sqlstmt := IF(@exist > 0, 
                'SELECT ''Column next_interview_time already exists'' as Info', 
                'ALTER TABLE `mall_vendor_application` ADD COLUMN `next_interview_time` DATETIME COMMENT ''Next interview time'' AFTER `has_scheduled_interview`');
PREPARE stmt FROM @sqlstmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

COMMIT;

-- =============================================
-- Verification Queries (for manual check)
-- =============================================
-- SELECT '✅ V1.13 Database Upgrade Completed!' as Status;
-- SELECT TABLE_NAME, COLUMN_NAME, DATA_TYPE 
-- FROM INFORMATION_SCHEMA.COLUMNS 
-- WHERE TABLE_SCHEMA = DATABASE() 
--   AND TABLE_NAME IN ('mall_vendor_interview_slot', 'mall_vendor_interview_notification')
-- ORDER BY TABLE_NAME, ORDINAL_POSITION;


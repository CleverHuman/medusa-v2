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


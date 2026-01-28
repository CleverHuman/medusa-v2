-- =============================================
-- V1.14 Vendor Member Accounts & Application Ownership
-- =============================================

START TRANSACTION;

CREATE TABLE IF NOT EXISTS `mall_vendor_member` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'Vendor member ID',
  `username` VARCHAR(64) NOT NULL COMMENT 'Login username',
  `password_hash` VARCHAR(255) NOT NULL COMMENT 'BCrypt password hash',
  `email` VARCHAR(128) DEFAULT NULL COMMENT 'Email',
  `phone` VARCHAR(32) DEFAULT NULL COMMENT 'Phone',
  `status` TINYINT(1) NOT NULL DEFAULT 1 COMMENT 'Status: 1=active, 0=disabled',
  `last_login_time` DATETIME DEFAULT NULL COMMENT 'Last login time',
  `remark` VARCHAR(255) DEFAULT NULL COMMENT 'Remark',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_vendor_member_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Vendor portal member accounts';

ALTER TABLE `mall_vendor_application`
    ADD COLUMN `member_id` BIGINT(20) DEFAULT NULL COMMENT 'Linked vendor member ID' AFTER `id`,
    ADD INDEX `idx_vendor_application_member` (`member_id`);

ALTER TABLE `mall_vendor_application`
    ADD CONSTRAINT `fk_vendor_application_member`
        FOREIGN KEY (`member_id`) REFERENCES `mall_vendor_member` (`id`)
        ON DELETE SET NULL ON UPDATE CASCADE;

COMMIT;


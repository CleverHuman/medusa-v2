-- ============================================
-- Vendor Bot System Database Migration
-- ============================================
-- Version: 1.24
-- Date: 2025-12-04
-- Description: 创建多Vendor ChatBot系统相关表
-- ============================================

START TRANSACTION;

-- 1. Vendor Bot配置表
CREATE TABLE IF NOT EXISTS `mall_vendor_bot` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'Bot配置ID',
  `vendor_id` BIGINT(20) NOT NULL COMMENT 'Vendor ID',
  `bot_token` VARCHAR(200) NOT NULL COMMENT 'Telegram Bot Token',
  `bot_username` VARCHAR(100) COMMENT 'Bot Username',
  `support_group_id` BIGINT(20) COMMENT 'Support Group ID (Forum Group)',
  `status` TINYINT(1) DEFAULT 1 COMMENT '状态: 0=禁用, 1=启用',
  `penalty_amount` DECIMAL(10,2) DEFAULT 50.00 COMMENT '单次违规罚金(USD)',
  `max_penalties_per_day` INT DEFAULT 3 COMMENT '每日最大罚金次数',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` VARCHAR(64) COMMENT '创建者',
  `update_by` VARCHAR(64) COMMENT '更新者',
  `remark` VARCHAR(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_vendor_id` (`vendor_id`),
  KEY `idx_status` (`status`),
  CONSTRAINT `fk_vendor_bot_vendor` FOREIGN KEY (`vendor_id`) REFERENCES `mall_vendor` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Vendor Bot配置表';

-- 2. 跳单关键字配置表
CREATE TABLE IF NOT EXISTS `mall_vendor_bot_keywords` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '关键字ID',
  `vendor_id` BIGINT(20) DEFAULT NULL COMMENT 'Vendor ID (NULL表示全局规则)',
  `keyword` VARCHAR(100) NOT NULL COMMENT '关键字',
  `keyword_type` VARCHAR(50) DEFAULT 'exact' COMMENT '匹配类型: exact/fuzzy/regex',
  `severity` TINYINT(1) DEFAULT 1 COMMENT '严重程度: 1=警告, 2=罚金',
  `is_active` TINYINT(1) DEFAULT 1 COMMENT '是否启用',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` VARCHAR(64) COMMENT '创建者',
  `update_by` VARCHAR(64) COMMENT '更新者',
  PRIMARY KEY (`id`),
  KEY `idx_vendor_id` (`vendor_id`),
  KEY `idx_keyword` (`keyword`),
  KEY `idx_is_active` (`is_active`),
  KEY `idx_severity` (`severity`),
  CONSTRAINT `fk_vendor_bot_keywords_vendor` FOREIGN KEY (`vendor_id`) REFERENCES `mall_vendor` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='跳单关键字配置表';

-- 插入默认全局关键字
INSERT INTO `mall_vendor_bot_keywords` (`vendor_id`, `keyword`, `keyword_type`, `severity`, `is_active`, `create_by`) VALUES
(NULL, 'outside', 'fuzzy', 2, 1, 'system'),
(NULL, 'direct', 'fuzzy', 2, 1, 'system'),
(NULL, 'bypass', 'fuzzy', 2, 1, 'system'),
(NULL, 'skip', 'fuzzy', 2, 1, 'system'),
(NULL, 'contact me directly', 'fuzzy', 2, 1, 'system'),
(NULL, 'wechat', 'exact', 2, 1, 'system'),
(NULL, 'whatsapp', 'exact', 2, 1, 'system'),
(NULL, 'signal', 'exact', 2, 1, 'system'),
(NULL, 'telegram', 'exact', 1, 1, 'system'),
(NULL, 'email', 'exact', 1, 1, 'system'),
(NULL, 'private chat', 'fuzzy', 2, 1, 'system'),
(NULL, 'personal contact', 'fuzzy', 2, 1, 'system'),
(NULL, 'off platform', 'fuzzy', 2, 1, 'system'),
(NULL, 'off-platform', 'fuzzy', 2, 1, 'system');

-- 3. 违规记录表
CREATE TABLE IF NOT EXISTS `mall_vendor_bot_violations` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '违规记录ID',
  `vendor_id` BIGINT(20) NOT NULL COMMENT 'Vendor ID',
  `order_id` VARCHAR(64) COMMENT '关联订单ID',
  `user_id` BIGINT(20) COMMENT '用户Telegram ID',
  `message_id` BIGINT(20) COMMENT '消息ID',
  `violation_type` VARCHAR(50) DEFAULT 'keyword' COMMENT '违规类型: keyword/other',
  `matched_keyword` VARCHAR(100) COMMENT '匹配的关键字',
  `message_content` TEXT COMMENT '违规消息内容（仅存储违规片段）',
  `penalty_amount` DECIMAL(10,2) DEFAULT 0.00 COMMENT '罚金金额',
  `penalty_status` VARCHAR(50) DEFAULT 'pending' COMMENT '罚金状态: pending/processed/failed',
  `violation_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '违规时间',
  `processed_time` DATETIME COMMENT '处理时间',
  `process_notes` TEXT COMMENT '处理备注',
  PRIMARY KEY (`id`),
  KEY `idx_vendor_id` (`vendor_id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_violation_time` (`violation_time`),
  KEY `idx_penalty_status` (`penalty_status`),
  KEY `idx_vendor_violation_time` (`vendor_id`, `violation_time`),
  CONSTRAINT `fk_vendor_bot_violations_vendor` FOREIGN KEY (`vendor_id`) REFERENCES `mall_vendor` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Vendor Bot违规记录表';

-- 4. Vendor Support会话表
CREATE TABLE IF NOT EXISTS `mall_vendor_support_session` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '会话ID',
  `vendor_id` BIGINT(20) NOT NULL COMMENT 'Vendor ID',
  `order_id` VARCHAR(64) COMMENT '关联订单ID',
  `user_id` BIGINT(20) NOT NULL COMMENT '用户Telegram ID',
  `topic_id` BIGINT(20) COMMENT 'Forum Topic ID',
  `status` VARCHAR(50) DEFAULT 'active' COMMENT '状态: active/closed/resolved',
  `start_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '开始时间',
  `close_time` DATETIME COMMENT '关闭时间',
  `last_message_time` DATETIME COMMENT '最后消息时间',
  `message_count` INT DEFAULT 0 COMMENT '消息数量',
  PRIMARY KEY (`id`),
  KEY `idx_vendor_id` (`vendor_id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_topic_id` (`topic_id`),
  KEY `idx_status` (`status`),
  KEY `idx_vendor_user` (`vendor_id`, `user_id`),
  CONSTRAINT `fk_vendor_support_session_vendor` FOREIGN KEY (`vendor_id`) REFERENCES `mall_vendor` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Vendor Support会话表';

-- 5. 在mall_vendor表中添加Bot相关字段（如果不存在）
SET @dbname = DATABASE();
SET @tablename = 'mall_vendor';
SET @columnname = 'has_bot_support';
SET @preparedStatement = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
   WHERE (table_name = @tablename) AND (table_schema = @dbname) AND (column_name = @columnname)) > 0,
  'SELECT 1',
  CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN ', @columnname, ' TINYINT(1) DEFAULT 0 COMMENT ''是否启用Bot Support''')
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

COMMIT;

SELECT '✅ Vendor Bot System tables created successfully!' as Status;


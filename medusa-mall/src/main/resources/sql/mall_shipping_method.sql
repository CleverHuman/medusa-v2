-- Create shipping method table
CREATE TABLE IF NOT EXISTS `mall_shipping_method` (
  `id` varchar(36) NOT NULL COMMENT 'Primary Key ID',
  `code` varchar(10) NOT NULL COMMENT 'Shipping Method Code',
  `name` varchar(50) NOT NULL COMMENT 'Shipping Method Name',
  `fee` decimal(10,2) NOT NULL COMMENT 'Shipping Fee',
  `status` tinyint(1) DEFAULT '1' COMMENT 'Status (0: Disabled, 1: Enabled)',
  `create_time` datetime DEFAULT NULL COMMENT 'Create Time',
  `update_time` datetime DEFAULT NULL COMMENT 'Update Time',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Shipping Method Table';

-- Insert initial data
INSERT INTO `mall_shipping_method` (`id`, `code`, `name`, `fee`, `status`, `create_time`, `update_time`) 
VALUES (UUID(), 'RP', 'Regular Post', 10.00, 1, NOW(), NOW());

INSERT INTO `mall_shipping_method` (`id`, `code`, `name`, `fee`, `status`, `create_time`, `update_time`) 
VALUES (UUID(), 'EP', 'Express Post', 17.00, 1, NOW(), NOW());

INSERT INTO `mall_shipping_method` (`id`, `code`, `name`, `fee`, `status`, `create_time`, `update_time`) 
VALUES (UUID(), 'RPG', 'Regular Post with Gift', 25.00, 1, NOW(), NOW());

INSERT INTO `mall_shipping_method` (`id`, `code`, `name`, `fee`, `status`, `create_time`, `update_time`) 
VALUES (UUID(), 'EPG', 'Express Post with Gift', 30.00, 1, NOW(), NOW());

INSERT INTO `mall_shipping_method` (`id`, `code`, `name`, `fee`, `status`, `create_time`, `update_time`) 
VALUES (UUID(), 'FP', 'Full Package', 35.00, 1, NOW(), NOW()); 
-- ============================================
-- Vendor 提现系统数据库表
-- ============================================
-- Version: 1.18
-- Date: 2025-11-18
-- Description: 创建 Vendor 提现地址管理和提现请求表
-- ============================================

-- 1. 在 mall_vendor 表中添加余额字段（检查列是否存在）
SET @dbname = DATABASE();
SET @tablename = 'mall_vendor';
SET @columnname = 'withdrawable_balance';
SET @preparedStatement = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
   WHERE (table_name = @tablename) AND (table_schema = @dbname) AND (column_name = @columnname)) > 0,
  'SELECT 1',
  CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN ', @columnname, ' DECIMAL(15,2) DEFAULT 0.00 COMMENT ''可提现余额''')
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

SET @columnname = 'pending_balance';
SET @preparedStatement = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
   WHERE (table_name = @tablename) AND (table_schema = @dbname) AND (column_name = @columnname)) > 0,
  'SELECT 1',
  CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN ', @columnname, ' DECIMAL(15,2) DEFAULT 0.00 COMMENT ''待确认余额（发货但未到期）''')
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

SET @columnname = 'total_withdrawn';
SET @preparedStatement = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
   WHERE (table_name = @tablename) AND (table_schema = @dbname) AND (column_name = @columnname)) > 0,
  'SELECT 1',
  CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN ', @columnname, ' DECIMAL(15,2) DEFAULT 0.00 COMMENT ''累计已提现金额''')
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- 2. 创建提现地址表
CREATE TABLE IF NOT EXISTS mall_vendor_withdrawal_address (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    vendor_id BIGINT NOT NULL COMMENT 'Vendor ID',
    currency VARCHAR(20) NOT NULL COMMENT '币种 (BTC/XMR/USDT_TRX/USDT_ERC)',
    address VARCHAR(255) NOT NULL COMMENT '提现地址',
    address_label VARCHAR(100) COMMENT '地址标签',
    is_active TINYINT(1) DEFAULT 1 COMMENT '是否激活 (0=否, 1=是)',
    verified_at DATETIME COMMENT '验证时间',
    verified_method VARCHAR(50) COMMENT '验证方式 (PGP/EMAIL)',
    verification_code VARCHAR(20) COMMENT '验证码（已使用后清空）',
    create_by VARCHAR(64) DEFAULT '' COMMENT '创建者',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(64) DEFAULT '' COMMENT '更新者',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    remark VARCHAR(500) COMMENT '备注',
    UNIQUE KEY uk_vendor_currency (vendor_id, currency),
    KEY idx_vendor_id (vendor_id),
    KEY idx_currency (currency),
    CONSTRAINT fk_withdrawal_address_vendor FOREIGN KEY (vendor_id) REFERENCES mall_vendor(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Vendor 提现地址表';

-- 3. 创建提现请求表
CREATE TABLE IF NOT EXISTS mall_vendor_withdrawal_request (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    request_code VARCHAR(50) NOT NULL UNIQUE COMMENT '提现请求编号',
    vendor_id BIGINT NOT NULL COMMENT 'Vendor ID',
    currency VARCHAR(20) NOT NULL COMMENT '币种 (BTC/XMR/USDT_TRX/USDT_ERC)',
    amount DECIMAL(15,2) NOT NULL COMMENT '提现金额',
    withdrawal_address VARCHAR(255) NOT NULL COMMENT '提现地址',
    request_status VARCHAR(20) DEFAULT 'PENDING' COMMENT '状态 (PENDING/APPROVED/REJECTED/PROCESSING/COMPLETED/FAILED)',
    request_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '请求时间',
    approve_by VARCHAR(64) COMMENT '审批人',
    approve_time DATETIME COMMENT '审批时间',
    approve_remark VARCHAR(500) COMMENT '审批备注',
    tx_hash VARCHAR(255) COMMENT '交易哈希',
    tx_time DATETIME COMMENT '交易时间',
    tx_fee DECIMAL(15,8) COMMENT '交易手续费',
    reject_reason VARCHAR(500) COMMENT '拒绝原因',
    create_by VARCHAR(64) DEFAULT '' COMMENT '创建者',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(64) DEFAULT '' COMMENT '更新者',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    remark VARCHAR(500) COMMENT '备注',
    KEY idx_request_code (request_code),
    KEY idx_vendor_id (vendor_id),
    KEY idx_status (request_status),
    KEY idx_request_time (request_time),
    CONSTRAINT fk_withdrawal_request_vendor FOREIGN KEY (vendor_id) REFERENCES mall_vendor(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Vendor 提现请求表';

-- 4. 创建余额变动记录表
CREATE TABLE IF NOT EXISTS mall_vendor_balance_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    vendor_id BIGINT NOT NULL COMMENT 'Vendor ID',
    change_type VARCHAR(50) NOT NULL COMMENT '变动类型 (ORDER_SHIPPED/WITHDRAWAL/REFUND/DISPUTE/BALANCE_RELEASED)',
    amount DECIMAL(15,2) NOT NULL COMMENT '变动金额（正数为增加，负数为减少）',
    before_balance DECIMAL(15,2) NOT NULL COMMENT '变动前余额',
    after_balance DECIMAL(15,2) NOT NULL COMMENT '变动后余额',
    related_order_id BIGINT COMMENT '关联订单ID',
    related_withdrawal_id BIGINT COMMENT '关联提现请求ID',
    available_date DATETIME COMMENT '资金可用日期（对于待确认余额）',
    description VARCHAR(500) COMMENT '描述',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    KEY idx_vendor_id (vendor_id),
    KEY idx_change_type (change_type),
    KEY idx_create_time (create_time),
    KEY idx_available_date (available_date),
    CONSTRAINT fk_balance_log_vendor FOREIGN KEY (vendor_id) REFERENCES mall_vendor(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Vendor 余额变动记录表';

-- 5. 在 mall_order 表中添加提现相关字段
SET @tablename = 'mall_order';
SET @columnname = 'balance_available_date';
SET @preparedStatement = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
   WHERE (table_name = @tablename) AND (table_schema = @dbname) AND (column_name = @columnname)) > 0,
  'SELECT 1',
  CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN ', @columnname, ' DATETIME COMMENT ''余额可用日期（发货后根据 vendor level 计算）''')
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

SET @columnname = 'is_balance_released';
SET @preparedStatement = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
   WHERE (table_name = @tablename) AND (table_schema = @dbname) AND (column_name = @columnname)) > 0,
  'SELECT 1',
  CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN ', @columnname, ' TINYINT(1) DEFAULT 0 COMMENT ''余额是否已释放 (0=否, 1=是)''')
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- 6. 初始化测试 Vendor 的提现地址（如果存在）
INSERT IGNORE INTO mall_vendor_withdrawal_address (vendor_id, currency, address, address_label, is_active, verified_at, verified_method)
SELECT 
    id,
    'BTC',
    'bc1qtest_btc_address_for_vendor_' || id,
    'Default BTC Address',
    1,
    NOW(),
    'INITIAL'
FROM mall_vendor 
WHERE id = 100;

INSERT IGNORE INTO mall_vendor_withdrawal_address (vendor_id, currency, address, address_label, is_active, verified_at, verified_method)
SELECT 
    id,
    'XMR',
    'xmr_test_address_for_vendor_' || id,
    'Default XMR Address',
    1,
    NOW(),
    'INITIAL'
FROM mall_vendor 
WHERE id = 100;

INSERT IGNORE INTO mall_vendor_withdrawal_address (vendor_id, currency, address, address_label, is_active, verified_at, verified_method)
SELECT 
    id,
    'USDT_TRX',
    'TRX_test_address_for_vendor_' || id,
    'Default USDT(TRC20) Address',
    1,
    NOW(),
    'INITIAL'
FROM mall_vendor 
WHERE id = 100;

INSERT IGNORE INTO mall_vendor_withdrawal_address (vendor_id, currency, address, address_label, is_active, verified_at, verified_method)
SELECT 
    id,
    'USDT_ERC',
    'ERC_test_address_for_vendor_' || id,
    'Default USDT(ERC20) Address',
    1,
    NOW(),
    'INITIAL'
FROM mall_vendor 
WHERE id = 100;


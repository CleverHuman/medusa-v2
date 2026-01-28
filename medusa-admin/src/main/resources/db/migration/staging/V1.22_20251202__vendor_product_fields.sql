-- V1.22_20251202__vendor_product_fields.sql
-- Vendor 产品功能 - 数据库字段更新
-- 这些更改不会影响现有的自营产品功能

-- ==========================================
-- 1. mall_product 表 - 添加 Vendor 产品相关字段
-- ==========================================

-- 添加品牌字段
-- 使用存储过程来处理 "IF NOT EXISTS" 逻辑（MySQL 5.7 兼容）
DELIMITER //
CREATE PROCEDURE add_product_columns()
BEGIN
    -- 添加 brand 字段
    IF NOT EXISTS (
        SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
        WHERE TABLE_SCHEMA = DATABASE() 
        AND TABLE_NAME = 'mall_product' 
        AND COLUMN_NAME = 'brand'
    ) THEN
        ALTER TABLE mall_product ADD COLUMN brand VARCHAR(100) DEFAULT NULL COMMENT 'Product brand' AFTER image_url;
    END IF;

    -- 添加 rejection_reason 字段
    IF NOT EXISTS (
        SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
        WHERE TABLE_SCHEMA = DATABASE() 
        AND TABLE_NAME = 'mall_product' 
        AND COLUMN_NAME = 'rejection_reason'
    ) THEN
        ALTER TABLE mall_product ADD COLUMN rejection_reason TEXT DEFAULT NULL COMMENT 'Rejection reason if rejected' AFTER approval_status;
    END IF;

    -- 添加 approved_time 字段
    IF NOT EXISTS (
        SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
        WHERE TABLE_SCHEMA = DATABASE() 
        AND TABLE_NAME = 'mall_product' 
        AND COLUMN_NAME = 'approved_time'
    ) THEN
        ALTER TABLE mall_product ADD COLUMN approved_time DATETIME DEFAULT NULL COMMENT 'Approval timestamp' AFTER rejection_reason;
    END IF;

    -- 添加 approved_by 字段
    IF NOT EXISTS (
        SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
        WHERE TABLE_SCHEMA = DATABASE() 
        AND TABLE_NAME = 'mall_product' 
        AND COLUMN_NAME = 'approved_by'
    ) THEN
        ALTER TABLE mall_product ADD COLUMN approved_by VARCHAR(64) DEFAULT NULL COMMENT 'Approver username' AFTER approved_time;
    END IF;
END //
DELIMITER ;

CALL add_product_columns();
DROP PROCEDURE add_product_columns;

-- ==========================================
-- 2. 添加索引（如果不存在）
-- ==========================================

-- 为 approval_status 添加索引
DELIMITER //
CREATE PROCEDURE add_product_indexes()
BEGIN
    -- 检查并添加 approval_status 索引
    IF NOT EXISTS (
        SELECT * FROM INFORMATION_SCHEMA.STATISTICS 
        WHERE TABLE_SCHEMA = DATABASE() 
        AND TABLE_NAME = 'mall_product' 
        AND INDEX_NAME = 'idx_approval_status'
    ) THEN
        CREATE INDEX idx_approval_status ON mall_product(approval_status);
    END IF;

    -- 检查并添加 vendor 来源索引
    IF NOT EXISTS (
        SELECT * FROM INFORMATION_SCHEMA.STATISTICS 
        WHERE TABLE_SCHEMA = DATABASE() 
        AND TABLE_NAME = 'mall_product' 
        AND INDEX_NAME = 'idx_vendor_origin'
    ) THEN
        CREATE INDEX idx_vendor_origin ON mall_product(product_origin, origin_id);
    END IF;
END //
DELIMITER ;

CALL add_product_indexes();
DROP PROCEDURE add_product_indexes;

-- ==========================================
-- 3. 确保 mall_vendor_product_approval 表存在
-- ==========================================

CREATE TABLE IF NOT EXISTS mall_vendor_product_approval (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL COMMENT 'Product ID (mall_product.id)',
    vendor_id BIGINT NOT NULL COMMENT 'Vendor ID',
    approval_status VARCHAR(50) NOT NULL DEFAULT 'PENDING_APPROVAL' COMMENT 'Status: PENDING_APPROVAL, APPROVED, REJECTED',
    approver_id BIGINT DEFAULT NULL COMMENT 'Approver user ID',
    approver_name VARCHAR(64) DEFAULT NULL COMMENT 'Approver username',
    approval_notes TEXT DEFAULT NULL COMMENT 'Approval notes',
    rejection_reason TEXT DEFAULT NULL COMMENT 'Rejection reason',
    approved_time DATETIME DEFAULT NULL COMMENT 'Approval timestamp',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
    del_flag CHAR(1) DEFAULT '0' COMMENT 'Delete flag',
    INDEX idx_product_id (product_id),
    INDEX idx_vendor_id (vendor_id),
    INDEX idx_status (approval_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Vendor product approval records';

-- ==========================================
-- 4. 验证更新结果
-- ==========================================

SELECT 'Vendor product fields migration completed!' as status;

-- 显示新增的字段
SELECT COLUMN_NAME, COLUMN_TYPE, COLUMN_DEFAULT, COLUMN_COMMENT
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = DATABASE() 
AND TABLE_NAME = 'mall_product'
AND COLUMN_NAME IN ('brand', 'rejection_reason', 'approved_time', 'approved_by')
ORDER BY ORDINAL_POSITION;


-- =====================================================
-- 添加订单查询性能优化索引
-- 用于支持每页500条数据的高性能查询
-- 注意：检查现有索引，避免重复创建
-- =====================================================

-- 使用存储过程检查并创建索引（兼容 MySQL 5.7）
DELIMITER //

CREATE PROCEDURE add_order_indexes()
BEGIN
    -- 1. mall_order 表索引
    
    -- 状态索引（用于状态过滤，最常用的查询条件）
    -- 检查是否存在 status 字段的索引
    IF NOT EXISTS (
        SELECT * FROM INFORMATION_SCHEMA.STATISTICS 
        WHERE TABLE_SCHEMA = DATABASE() 
        AND TABLE_NAME = 'mall_order' 
        AND COLUMN_NAME = 'status'
        AND INDEX_NAME != 'PRIMARY'
    ) THEN
        CREATE INDEX idx_order_status ON mall_order(status);
    END IF;

    -- 复合索引：状态 + 创建时间（用于最常见的查询场景：按状态过滤并按时间排序）
    -- 检查是否存在包含 status 和 create_time 的复合索引
    IF NOT EXISTS (
        SELECT * FROM INFORMATION_SCHEMA.STATISTICS 
        WHERE TABLE_SCHEMA = DATABASE() 
        AND TABLE_NAME = 'mall_order' 
        AND INDEX_NAME = 'idx_order_status_create_time'
    ) THEN
        CREATE INDEX idx_order_status_create_time ON mall_order(status, create_time DESC);
    END IF;

    -- 复合索引：供应商 + 状态 + 创建时间（用于供应商订单列表查询）
    -- 检查是否存在包含 vendor_id, status 和 create_time 的复合索引
    IF NOT EXISTS (
        SELECT * FROM INFORMATION_SCHEMA.STATISTICS 
        WHERE TABLE_SCHEMA = DATABASE() 
        AND TABLE_NAME = 'mall_order' 
        AND INDEX_NAME = 'idx_order_vendor_status_time'
    ) THEN
        CREATE INDEX idx_order_vendor_status_time ON mall_order(vendor_id, status, create_time DESC);
    END IF;

    -- 2. mall_order_item 表索引
    -- 订单ID索引（用于JOIN查询）
    -- 检查是否存在 order_id 字段的索引
    IF NOT EXISTS (
        SELECT * FROM INFORMATION_SCHEMA.STATISTICS 
        WHERE TABLE_SCHEMA = DATABASE() 
        AND TABLE_NAME = 'mall_order_item' 
        AND COLUMN_NAME = 'order_id'
        AND INDEX_NAME != 'PRIMARY'
    ) THEN
        CREATE INDEX idx_order_item_order_id ON mall_order_item(order_id);
    END IF;

    -- 3. mall_order_shipping 表索引
    -- 订单ID索引（用于JOIN查询）
    -- 检查是否存在 order_id 字段的索引
    IF NOT EXISTS (
        SELECT * FROM INFORMATION_SCHEMA.STATISTICS 
        WHERE TABLE_SCHEMA = DATABASE() 
        AND TABLE_NAME = 'mall_order_shipping' 
        AND COLUMN_NAME = 'order_id'
        AND INDEX_NAME != 'PRIMARY'
    ) THEN
        CREATE INDEX idx_order_shipping_order_id ON mall_order_shipping(order_id);
    END IF;

    -- 4. mall_order_payment 表索引
    -- 订单ID索引（用于JOIN查询）
    -- 检查是否存在 order_id 字段的索引
    IF NOT EXISTS (
        SELECT * FROM INFORMATION_SCHEMA.STATISTICS 
        WHERE TABLE_SCHEMA = DATABASE() 
        AND TABLE_NAME = 'mall_order_payment' 
        AND COLUMN_NAME = 'order_id'
        AND INDEX_NAME != 'PRIMARY'
    ) THEN
        CREATE INDEX idx_order_payment_order_id ON mall_order_payment(order_id);
    END IF;

END //

DELIMITER ;

-- 执行存储过程
CALL add_order_indexes();

-- 删除存储过程
DROP PROCEDURE add_order_indexes;

-- =====================================================
-- 索引检查结果（根据数据库截图）：
-- ✅ 已存在的索引（无需创建）：
--   1. idx_order_sn - 订单号索引（UNIQUE）
--   2. idx_create_time - 创建时间索引
--   3. idx_vendor_id - 供应商ID索引
--   4. idx_member_id - 会员ID索引
--   5. idx_isdispute - 争议状态索引
--   6. idx_order_type - 订单类型索引
--   7. idx_bond_application_id - BOND申请ID索引
--   8. idx_dispute_time - 争议时间索引
--
-- ❓ 需要检查/创建的索引：
--   1. idx_order_status - 状态索引（最常用，需要检查是否存在）
--   2. idx_order_status_create_time - 复合索引（状态+时间）
--   3. idx_order_vendor_status_time - 复合索引（供应商+状态+时间）
--   4. idx_order_item_order_id - 订单项表索引（JOIN优化）
--   5. idx_order_shipping_order_id - 订单配送表索引（JOIN优化）
--   6. idx_order_payment_order_id - 订单支付表索引（JOIN优化）
-- =====================================================

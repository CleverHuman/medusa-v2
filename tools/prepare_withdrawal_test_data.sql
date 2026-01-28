-- ============================================
-- Vendor 提现系统测试数据准备脚本
-- ============================================

-- 使用数据库
USE medusa;

-- 1. 确保测试 Vendor (ID=100) 的提现地址已初始化
-- 这应该在迁移脚本中已经完成，但为了确保，我们再次插入
INSERT IGNORE INTO mall_vendor_withdrawal_address (vendor_id, currency, address, address_label, is_active, verified_at, verified_method)
VALUES
(100, 'BTC', 'bc1qtest_btc_address_for_vendor_100', 'Default BTC Address', 1, NOW(), 'INITIAL'),
(100, 'XMR', 'xmr_test_address_for_vendor_100', 'Default XMR Address', 1, NOW(), 'INITIAL'),
(100, 'USDT_TRX', 'TRX_test_address_for_vendor_100', 'Default USDT(TRC20) Address', 1, NOW(), 'INITIAL'),
(100, 'USDT_ERC', 'ERC_test_address_for_vendor_100', 'Default USDT(ERC20) Address', 1, NOW(), 'INITIAL');

-- 2. 确保Vendor的余额字段已初始化
UPDATE mall_vendor 
SET withdrawable_balance = IFNULL(withdrawable_balance, 0.00),
    pending_balance = IFNULL(pending_balance, 0.00),
    total_withdrawn = IFNULL(total_withdrawn, 0.00)
WHERE id = 100;

-- 3. 创建一个测试订单（用于测试发货后余额更新）
-- 注意：订单ID必须是字符串格式，因为表结构中 id 是 varchar
SET @test_order_id = CONCAT('TEST_ORDER_', UNIX_TIMESTAMP());

INSERT INTO mall_order (
    id, order_sn, member_id, vendor_id, total_amount, freight_amount, 
    status, source_type, create_time, update_time
) VALUES (
    @test_order_id,
    CONCAT('ORD', DATE_FORMAT(NOW(), '%Y%m%d%H%i%s')),
    1,  -- 假设member_id=1存在
    100, -- Vendor ID
    500.00,  -- 订单总金额
    10.00,   -- 运费
    1,  -- status=1表示已支付，可以发货
    0,  -- source_type
    NOW(),
    NOW()
);

-- 4. 为测试订单创建订单项
INSERT INTO mall_order_item (
    id, order_id, order_sn, product_id, product_name, product_origin, origin_id,
    price, quantity, total_price, product_image, create_time
) VALUES (
    CONCAT('ITEM_', UNIX_TIMESTAMP()),
    @test_order_id,
    (SELECT order_sn FROM mall_order WHERE id = @test_order_id),
    'VENDOR-PROD-TEST-002',  -- 使用已存在的测试产品
    'Test Vendor Product',
    1,  -- product_origin=1表示vendor产品
    100,  -- origin_id指向vendor
    490.00,  -- 单价
    1,  -- 数量
    490.00,  -- 总价
    NULL,  -- product_image
    NOW()
);

-- 5. 显示测试数据
SELECT '=== Vendor 100 提现地址 ===' AS '';
SELECT * FROM mall_vendor_withdrawal_address WHERE vendor_id = 100;

SELECT '=== Vendor 100 余额信息 ===' AS '';
SELECT id, vendor_name, withdrawable_balance, pending_balance, total_withdrawn, bond, level
FROM mall_vendor WHERE id = 100;

SELECT '=== 测试订单 ===' AS '';
SELECT id, order_sn, vendor_id, total_amount, status, create_time
FROM mall_order WHERE id = @test_order_id;

SELECT '=== 测试订单项 ===' AS '';
SELECT id, order_id, product_id, product_name, product_origin, origin_id, price, quantity, total_price
FROM mall_order_item WHERE order_id = @test_order_id;


-- =============================================
-- Vendor Module Demo Data
-- 供应商模块演示数据
-- =============================================

-- 清理可能存在的演示数据（可选）
-- DELETE FROM mall_vendor_application WHERE vendor_name LIKE 'Demo%';

-- =============================================
-- 1. Pending Applications (待审核申请)
-- =============================================

INSERT INTO mall_vendor_application (
    application_id, vendor_name, has_market_experience, existing_markets, experience_years,
    pgp_signature_url, btc_wallet, xmr_wallet, usdt_wallet, location,
    product_categories, stock_volume, offline_delivery, product_description,
    primary_telegram, primary_email,
    secondary_signal, secondary_email,
    status, review_progress, create_time
) VALUES 
(
    'VA001DEMO',
    'Demo Electronics Supplier',
    1,
    'AlphaBay, Dream Market',
    3,
    'https://demo.pgp.example/keys/electronics',
    'bc1qxy2kgdygjrsqtzq2n0yrf2493p83kkfjhx0wlh',
    '8BKKDcRkZGVWtENPtJKVV7J6vJ5rPXDuJv9GJHVQQqJv',
    'TYW8Cp7ThJGQvVm5J8VQvPGZ5J8VQvPGZ5',
    'Europe',
    '["Electronics", "Fashion Accessories"]',
    'medium',
    0,
    'High-quality electronics and accessories. Specialized in smartphones, tablets, and wearable devices.',
    '@demo_electronics',
    'electronics@demo-vendor.example',
    '+1-555-0101',
    'backup@demo-vendor.example',
    'pending',
    0,
    DATE_SUB(NOW(), INTERVAL 2 DAY)
),
(
    'VA002DEMO',
    'Demo Fashion House',
    1,
    'Silk Road 2.0',
    5,
    'https://demo.pgp.example/keys/fashion',
    'bc1qar0srrr7xfkvy5l643lydnw9re59gtzzwf5mdq',
    '4AeJ8JGVWtENPtJKVV7J6vJ5rPXDuJv9GJHVQQqJv',
    'THa8Cp7ThJGQvVm5J8VQvPGZ5J8VQvPGZ5',
    'North America',
    '["Fashion Accessories", "Beauty & Personal Care"]',
    'large',
    1,
    'Premium fashion items and luxury accessories. Direct from manufacturers with authenticity guarantee.',
    '@demo_fashion',
    'contact@demo-fashion.example',
    '+1-555-0102',
    'support@demo-fashion.example',
    'pending',
    0,
    DATE_SUB(NOW(), INTERVAL 1 DAY)
),
(
    'VA003DEMO',
    'Demo Tech Solutions',
    0,
    NULL,
    NULL,
    'https://demo.pgp.example/keys/tech',
    'bc1qw508d6qejxtdg4y5r3zarvary0c5xw7kv8f3t4',
    '4BkJ8JGVWtENPtJKVV7J6vJ5rPXDuJv9GJHVQQqJv',
    'TRa8Cp7ThJGQvVm5J8VQvPGZ5J8VQvPGZ5',
    'Asia Pacific',
    '["Electronics", "Home & Garden"]',
    'small',
    0,
    'New vendor offering tech gadgets and smart home devices. Focus on quality and customer satisfaction.',
    '@demo_techsolutions',
    'info@demo-tech.example',
    NULL,
    'sales@demo-tech.example',
    'pending',
    0,
    DATE_SUB(NOW(), INTERVAL 3 HOUR)
);

-- =============================================
-- 2. Under Review Applications (审核中申请)
-- =============================================

INSERT INTO mall_vendor_application (
    application_id, vendor_name, has_market_experience, existing_markets, experience_years,
    pgp_signature_url, btc_wallet, xmr_wallet, usdt_wallet, location,
    product_categories, stock_volume, offline_delivery, product_description,
    primary_telegram, primary_signal, primary_email,
    status, review_stage, review_progress, create_time
) VALUES 
(
    'VA004DEMO',
    'Demo Sports & Outdoors',
    1,
    'Agora Marketplace',
    2,
    'https://demo.pgp.example/keys/sports',
    'bc1qrp33g0q5c5txsp9arysrx4k6zdkfs4nce4xj0gdcccefvpysxf3qccfmv3',
    '4CeJ8JGVWtENPtJKVV7J6vJ5rPXDuJv9GJHVQQqJv',
    'TLa8Cp7ThJGQvVm5J8VQvPGZ5J8VQvPGZ5',
    'North America',
    '["Sports & Outdoors"]',
    'medium',
    1,
    'Sports equipment and outdoor gear. From camping to fitness, we have it all.',
    '@demo_sports',
    '+1-555-0104',
    'sports@demo-vendor.example',
    'under_review',
    'Product Range Review',
    45,
    DATE_SUB(NOW(), INTERVAL 5 DAY)
),
(
    'VA005DEMO',
    'Demo Book Store',
    0,
    NULL,
    NULL,
    'https://demo.pgp.example/keys/books',
    'bc1qc7slrfxkknqcq2jevvvkdgvrt8080852dfjewde450xdlk4ugp7szw5tk9',
    '4DeJ8JGVWtENPtJKVV7J6vJ5rPXDuJv9GJHVQQqJv',
    'TMa8Cp7ThJGQvVm5J8VQvPGZ5J8VQvPGZ5',
    'Europe',
    '["Books & Stationery"]',
    'small',
    0,
    'Rare books, academic materials, and premium stationery. Worldwide shipping available.',
    '@demo_bookstore',
    NULL,
    'books@demo-vendor.example',
    'under_review',
    'Contact Verification',
    30,
    DATE_SUB(NOW(), INTERVAL 3 DAY)
);

-- =============================================
-- 3. Interview Required Applications (需要面试)
-- =============================================

INSERT INTO mall_vendor_application (
    application_id, vendor_name, has_market_experience, existing_markets, experience_years,
    pgp_signature_url, btc_wallet, xmr_wallet, usdt_wallet, location,
    product_categories, stock_volume, offline_delivery, product_description,
    primary_jabber, primary_email,
    secondary_telegram,
    status, review_stage, review_progress, review_notes, create_time
) VALUES 
(
    'VA006DEMO',
    'Demo Premium Goods',
    1,
    'Wall Street Market, Empire Market',
    4,
    'https://demo.pgp.example/keys/premium',
    'bc1q34aq5drpuwy3wgl9lhup9892qp6svr8ldzyy7c',
    '4EeJ8JGVWtENPtJKVV7J6vJ5rPXDuJv9GJHVQQqJv',
    'TNa8Cp7ThJGQvVm5J8VQvPGZ5J8VQvPGZ5',
    'Middle East & Africa',
    '["Fashion Accessories", "Home & Garden", "Beauty & Personal Care"]',
    'xlarge',
    1,
    'High-end luxury goods supplier. Extensive experience in multiple markets. Large inventory and fast shipping.',
    'premium@jabber.example.com',
    'premium@demo-vendor.example',
    '@demo_premium',
    'interview_required',
    'Final Review',
    75,
    'Large scale operation requires additional verification. Interview scheduled for next week.',
    DATE_SUB(NOW(), INTERVAL 7 DAY)
);

-- =============================================
-- 4. Approved Applications (已批准 - 已创建Vendor)
-- =============================================

-- 插入已批准的申请
INSERT INTO mall_vendor_application (
    application_id, vendor_name, has_market_experience, existing_markets, experience_years,
    pgp_signature_url, btc_wallet, xmr_wallet, usdt_wallet, location,
    product_categories, stock_volume, offline_delivery, product_description,
    primary_telegram, primary_email,
    status, review_progress, reviewed_time, review_notes, vendor_id, create_time
) VALUES 
(
    'VA007DEMO',
    'Demo Home Essentials',
    1,
    'Dream Market',
    3,
    'https://demo.pgp.example/keys/home',
    'bc1qeklep85ntjz4605drds6aww9u0qr46qzrv5xswd35uhjuj8ahfcqgf6hak',
    '4FeJ8JGVWtENPtJKVV7J6vJ5rPXDuJv9GJHVQQqJv',
    'TOa8Cp7ThJGQvVm5J8VQvPGZ5J8VQvPGZ5',
    'Asia Pacific',
    '["Home & Garden"]',
    'medium',
    0,
    'Home decoration and garden supplies. Quality products at competitive prices.',
    '@demo_home',
    'home@demo-vendor.example',
    'approved',
    100,
    DATE_SUB(NOW(), INTERVAL 10 DAY),
    'Approved after verification. Good reputation in existing markets.',
    NULL,  -- vendor_id will be set after inserting vendor
    DATE_SUB(NOW(), INTERVAL 15 DAY)
);

-- 获取刚插入的申请ID
SET @app_id = LAST_INSERT_ID();

-- 创建对应的Vendor记录
INSERT INTO mall_vendor (
    vendor_code, vendor_name, description, 
    contact_telegram, contact_email,
    pgp_public_key_url, btc_wallet, xmr_wallet, usdt_wallet,
    product_categories, location, rating, total_sales, total_orders,
    status, is_featured, sort_order,
    application_id, approved_time, approved_by,
    create_time
) VALUES (
    CONCAT('VD', UNIX_TIMESTAMP()),
    'Demo Home Essentials',
    'Home decoration and garden supplies. Quality products at competitive prices.',
    '@demo_home',
    'home@demo-vendor.example',
    'https://demo.pgp.example/keys/home',
    'bc1qeklep85ntjz4605drds6aww9u0qr46qzrv5xswd35uhjuj8ahfcqgf6hak',
    '4FeJ8JGVWtENPtJKVV7J6vJ5rPXDuJv9GJHVQQqJv',
    'TOa8Cp7ThJGQvVm5J8VQvPGZ5J8VQvPGZ5',
    '["Home & Garden"]',
    'Asia Pacific',
    4.5,
    125000,
    89,
    1,
    1,
    10,
    @app_id,
    DATE_SUB(NOW(), INTERVAL 10 DAY),
    'admin',
    DATE_SUB(NOW(), INTERVAL 10 DAY)
);

-- 更新申请记录的vendor_id
UPDATE mall_vendor_application 
SET vendor_id = LAST_INSERT_ID() 
WHERE id = @app_id;

-- =============================================
-- 5. Rejected Applications (已拒绝)
-- =============================================

INSERT INTO mall_vendor_application (
    application_id, vendor_name, has_market_experience, existing_markets, experience_years,
    pgp_signature_url, btc_wallet, xmr_wallet, usdt_wallet, location,
    product_categories, stock_volume, offline_delivery, product_description,
    primary_email,
    status, review_progress, reviewed_time, review_notes, create_time
) VALUES 
(
    'VA008DEMO',
    'Demo Suspicious Vendor',
    0,
    NULL,
    NULL,
    'https://demo.pgp.example/keys/suspicious',
    'invalid_btc_address',
    'invalid_xmr_address',
    'invalid_usdt_address',
    'Latin America',
    '["Others"]',
    'small',
    0,
    'Incomplete information.',
    'suspicious@demo.example',
    'rejected',
    100,
    DATE_SUB(NOW(), INTERVAL 5 DAY),
    'Rejected: Invalid wallet addresses and insufficient information provided. PGP signature could not be verified.',
    DATE_SUB(NOW(), INTERVAL 8 DAY)
),
(
    'VA009DEMO',
    'Demo Incomplete Application',
    0,
    NULL,
    NULL,
    'https://demo.pgp.example/keys/incomplete',
    'bc1qw508d6qejxtdg4y5r3zarvary0c5xw7kv8f3t4',
    '4GeJ8JGVWtENPtJKVV7J6vJ5rPXDuJv9GJHVQQqJv',
    'TPa8Cp7ThJGQvVm5J8VQvPGZ5J8VQvPGZ5',
    'Europe',
    '["Electronics"]',
    'small',
    0,
    'Test application.',
    'incomplete@demo.example',
    'rejected',
    100,
    DATE_SUB(NOW(), INTERVAL 12 DAY),
    'Rejected: Application does not meet our vendor criteria. Product description too vague.',
    DATE_SUB(NOW(), INTERVAL 14 DAY)
);

-- =============================================
-- 验证插入的数据
-- =============================================

-- 显示统计信息
SELECT 
    status,
    COUNT(*) AS count,
    GROUP_CONCAT(vendor_name SEPARATOR ', ') AS vendors
FROM mall_vendor_application 
WHERE vendor_name LIKE 'Demo%'
GROUP BY status;

-- 显示所有演示申请
SELECT 
    application_id,
    vendor_name,
    location,
    status,
    review_progress,
    DATE_FORMAT(create_time, '%Y-%m-%d %H:%i') AS created
FROM mall_vendor_application 
WHERE vendor_name LIKE 'Demo%'
ORDER BY create_time DESC;

SELECT '演示数据插入完成！' AS message;
SELECT 'Demo data inserted successfully!' AS message;


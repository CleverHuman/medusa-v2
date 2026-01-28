-- ============================================
-- Fix Vendor Product Channel Default Value
-- ============================================
-- Version: 1.25
-- Date: 2025-12-13
-- Description: 将Vendor产品的默认channel从"Vendor"改为"3"（OS/TG）
-- Channel映射: 0=OS, 1=TG, 3=OS/TG
-- ============================================

START TRANSACTION;

-- 更新所有Vendor产品的channel为"3"（OS/TG）
-- 只更新channel为"Vendor"或NULL的Vendor产品（product_origin=1）
UPDATE mall_product
SET channel = '3'
WHERE product_origin = 1
  AND (channel IS NULL OR channel = 'Vendor' OR channel = '');

-- 验证更新结果
SELECT 
  COUNT(*) as total_vendor_products,
  SUM(CASE WHEN channel = '3' THEN 1 ELSE 0 END) as os_tg_products,
  SUM(CASE WHEN channel = 'Vendor' THEN 1 ELSE 0 END) as vendor_channel_products,
  SUM(CASE WHEN channel IS NULL OR channel = '' THEN 1 ELSE 0 END) as null_channel_products
FROM mall_product
WHERE product_origin = 1;

COMMIT;

SELECT '✅ Vendor product channel updated to OS/TG (3)' as Status;


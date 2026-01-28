-- =============================================
-- Create Bond Digital Product (Simplified)
-- Version: V1.27
-- Date: 2025-12-20
-- Description: Create ONE bond product, use quantity for different levels
-- =============================================

START TRANSACTION;

-- 1. Insert Base Bond Product in mall_product
-- ==========================================
INSERT INTO `mall_product` (
  `product_id`, 
  `name`, 
  `description`, 
  `category`, 
  `status`, 
  `channel`, 
  `product_origin`,
  `sort_order`
) VALUES 
('BOND-BASE', 
 'Co-op Member Bond Payment', 
 'Bond payment for Co-op Membership. Base unit: $3,000 USD. Your assigned level determines the quantity (Level 1 = 1x, Level 2 = 2x, Level 3 = 3x, etc.).', 
 'Service', 
 1, 
 '0',  -- OS channel only
 0,    -- Platform product
 1000  -- High priority sort
) ON DUPLICATE KEY UPDATE 
  name = VALUES(name),
  description = VALUES(description),
  category = VALUES(category),
  status = VALUES(status),
  channel = VALUES(channel),
  product_origin = VALUES(product_origin),
  sort_order = VALUES(sort_order);


-- 2. Insert SKU in mall_product2 (NOT mall_product_sku)
-- ==========================================
-- Base Bond unit = $3,000 USD
-- Level 1 = 1 unit = $3,000
-- Level 2 = 2 units = $6,000
-- Level 3 = 3 units = $9,000
-- ... and so on

INSERT INTO `mall_product2` (
  `product_id`,
  `sku`,
  `model`,
  `unit`,
  `price`,
  `currency`,
  `inventory`,
  `status`
) 
VALUES (
  'BOND-BASE',
  'BOND-BASE-UNIT',
  3000,  -- Base amount per unit
  'USD',  -- Unit is USD (currency-based product)
  3000.00,  -- Price per unit: $3,000
  'USD',
  999999,  -- Unlimited inventory
  1  -- Active
) ON DUPLICATE KEY UPDATE 
  model = VALUES(model),
  unit = VALUES(unit),
  price = VALUES(price),
  currency = VALUES(currency),
  inventory = VALUES(inventory),
  status = VALUES(status);

COMMIT;

SELECT '✅ Bond Digital Product created successfully!' as Status;
SELECT 'Created 1 bond product (BOND-BASE) with base unit $3,000' as Info;
SELECT 'Use quantity to calculate total: Level 1 = qty 1, Level 2 = qty 2, etc.' as `Usage_Note`;

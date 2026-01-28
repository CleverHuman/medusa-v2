-- =============================================
-- Update Bond Base Amount: $3,000 USD → $1,000 AUD
-- Version: V1.31
-- Date: 2025-12-22
-- Description: Update initial bond amount from $3,000 USD to $1,000 AUD
-- =============================================

START TRANSACTION;

-- 1. Update Product Price (mall_product2)
-- ==========================================
UPDATE `mall_product2` 
SET 
    `model` = 1000,
    `unit` = 'AUD',
    `price` = 1000.00,
    `currency` = 'AUD'
WHERE `sku` = 'BOND-BASE-UNIT';

-- 2. Update Product Description (mall_product)
-- ==========================================
UPDATE `mall_product`
SET 
    `description` = 'Bond payment for Co-op Membership. Base unit: $1,000 AUD. Your assigned level determines the quantity (Level 1 = 1x, Level 2 = 2x, Level 3 = 3x, etc.).'
WHERE `product_id` = 'BOND-BASE';

-- 3. Update Bond Config - All Levels
-- ==========================================
-- Note: Bond amounts are calculated as Level × $1,000 AUD
-- Level 1: $1,000, Level 2: $2,000, ..., Level 10: $10,000

-- Level 1: $1,000 AUD
UPDATE `mall_vendor_bond_config` 
SET 
    `bond_amount` = 1000.00,
    `cumulative_trade_volume` = 1000.00,
    `rules_content` = REPLACE(REPLACE(REPLACE(REPLACE(`rules_content`, 
        '$3,000 USD', '$1,000 AUD'),
        '$3,000', '$1,000'),
        'USD', 'AUD'),
        '3000', '1000')
WHERE `level` = 1;

-- Level 2: $2,000 AUD
UPDATE `mall_vendor_bond_config` 
SET 
    `bond_amount` = 2000.00,
    `cumulative_trade_volume` = 4000.00,
    `rules_content` = REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(`rules_content`, 
        '$6,000 USD', '$2,000 AUD'),
        '$6,000', '$2,000'),
        '$12,000', '$4,000'),
        'USD', 'AUD'),
        '$3,000', '$1,000')
WHERE `level` = 2;

-- Level 3: $3,000 AUD
UPDATE `mall_vendor_bond_config` 
SET 
    `bond_amount` = 3000.00,
    `cumulative_trade_volume` = 9000.00,
    `rules_content` = REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(`rules_content`, 
        '$9,000 USD', '$3,000 AUD'),
        '$9,000', '$3,000'),
        '$27,000', '$9,000'),
        'USD', 'AUD'),
        '$3,000', '$1,000')
WHERE `level` = 3;

-- Level 4: $4,000 AUD
UPDATE `mall_vendor_bond_config` 
SET 
    `bond_amount` = 4000.00,
    `cumulative_trade_volume` = 16000.00,
    `rules_content` = REPLACE(REPLACE(REPLACE(REPLACE(`rules_content`, 
        '$12,000 USD', '$4,000 AUD'),
        '$12,000', '$4,000'),
        '$48,000', '$16,000'),
        'USD', 'AUD')
WHERE `level` = 4;

-- Level 5: $5,000 AUD
UPDATE `mall_vendor_bond_config` 
SET 
    `bond_amount` = 5000.00,
    `cumulative_trade_volume` = 25000.00,
    `rules_content` = REPLACE(REPLACE(REPLACE(REPLACE(`rules_content`, 
        '$15,000 USD', '$5,000 AUD'),
        '$15,000', '$5,000'),
        '$75,000', '$25,000'),
        'USD', 'AUD')
WHERE `level` = 5;

-- Level 6: $6,000 AUD
UPDATE `mall_vendor_bond_config` 
SET 
    `bond_amount` = 6000.00,
    `cumulative_trade_volume` = 36000.00,
    `rules_content` = REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(`rules_content`, 
        '$18,000 USD', '$6,000 AUD'),
        '$18,000', '$6,000'),
        '$108,000', '$36,000'),
        'USD', 'AUD'),
        '$3,000', '$1,000')
WHERE `level` = 6;

-- Level 7: $7,000 AUD
UPDATE `mall_vendor_bond_config` 
SET 
    `bond_amount` = 7000.00,
    `cumulative_trade_volume` = 49000.00,
    `rules_content` = REPLACE(REPLACE(REPLACE(REPLACE(`rules_content`, 
        '$21,000 USD', '$7,000 AUD'),
        '$21,000', '$7,000'),
        '$147,000', '$49,000'),
        'USD', 'AUD')
WHERE `level` = 7;

-- Level 8: $8,000 AUD
UPDATE `mall_vendor_bond_config` 
SET 
    `bond_amount` = 8000.00,
    `cumulative_trade_volume` = 64000.00,
    `rules_content` = REPLACE(REPLACE(REPLACE(REPLACE(`rules_content`, 
        '$24,000 USD', '$8,000 AUD'),
        '$24,000', '$8,000'),
        '$192,000', '$64,000'),
        'USD', 'AUD')
WHERE `level` = 8;

-- Level 9: $9,000 AUD
UPDATE `mall_vendor_bond_config` 
SET 
    `bond_amount` = 9000.00,
    `cumulative_trade_volume` = 81000.00,
    `rules_content` = REPLACE(REPLACE(REPLACE(REPLACE(`rules_content`, 
        '$27,000 USD', '$9,000 AUD'),
        '$27,000', '$9,000'),
        '$243,000', '$81,000'),
        'USD', 'AUD')
WHERE `level` = 9;

-- Level 10: $10,000 AUD
UPDATE `mall_vendor_bond_config` 
SET 
    `bond_amount` = 10000.00,
    `cumulative_trade_volume` = 100000.00,
    `rules_content` = REPLACE(REPLACE(REPLACE(REPLACE(`rules_content`, 
        '$30,000 USD', '$10,000 AUD'),
        '$30,000', '$10,000'),
        '$300,000', '$100,000'),
        'USD', 'AUD')
WHERE `level` = 10;

-- 4. Update table comment to reflect AUD currency
-- ==========================================
ALTER TABLE `mall_vendor_bond_config` 
MODIFY COLUMN `bond_amount` DECIMAL(10,2) NOT NULL COMMENT 'Required Bond Amount (AUD)';

ALTER TABLE `mall_vendor_bond_config` 
MODIFY COLUMN `cumulative_trade_volume` DECIMAL(12,2) COMMENT 'Cumulative XP/Lifetime Trade Volume (AUD)';

COMMIT;

SELECT '✅ Bond amount updated successfully from $3,000 USD to $1,000 AUD!' as Status;
SELECT 'Updated product price, product description, and all 10 bond level configurations' as Info;


-- =============================================
-- Vendor Bond Configuration System
-- Version: V1.25
-- Date: 2025-12-15
-- Description: Create bond level configuration table with 10 levels
-- =============================================

START TRANSACTION;

-- 1. Create Bond Configuration Table
-- ==========================================
CREATE TABLE IF NOT EXISTS `mall_vendor_bond_config` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'Primary Key',
  `level` INT NOT NULL COMMENT 'Vendor Level (1-10)',
  `bond_amount` DECIMAL(10,2) NOT NULL COMMENT 'Required Bond Amount (USD)',
  `account_life_days` INT COMMENT 'CM Account Life (days)',
  `cumulative_trade_volume` DECIMAL(12,2) COMMENT 'Cumulative XP/Lifetime Trade Volume (USD)',
  `trade_cap_multiplier` VARCHAR(20) COMMENT 'Trade Cap Multiplier (e.g., 100%, 200%)',
  `total_trade_cap_formula` VARCHAR(100) COMMENT 'Total Trade Cap formula description',
  `liquidation_payout` VARCHAR(50) COMMENT 'CM Liquidation Payout ratio',
  `payout_time` VARCHAR(20) COMMENT 'CM Payout Time (T+0, T+1, T+2, T+3)',
  `tax_discount_multiplier` DECIMAL(5,2) COMMENT 'Tax Discount Multiplier percentage',
  `am_bonus_description` TEXT COMMENT 'AM Bonus description',
  `level_perks` TEXT COMMENT 'Level Perks description',
  `rules_content` TEXT COMMENT 'Detailed Bond Rules (Markdown format)',
  `status` TINYINT(1) DEFAULT 1 COMMENT '0=Disabled, 1=Active',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
  `create_by` VARCHAR(64) COMMENT 'Creator',
  `update_by` VARCHAR(64) COMMENT 'Updater',
  `remark` VARCHAR(500) COMMENT 'Remark',
  UNIQUE KEY `uk_level` (`level`),
  INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Vendor Bond Level Configuration';

-- 2. Insert Initial Bond Level Configurations
-- ==========================================
-- Use INSERT ... ON DUPLICATE KEY UPDATE for idempotency

INSERT INTO `mall_vendor_bond_config` (
  `level`, 
  `bond_amount`, 
  `account_life_days`, 
  `cumulative_trade_volume`, 
  `trade_cap_multiplier`,
  `total_trade_cap_formula`,
  `liquidation_payout`,
  `payout_time`,
  `tax_discount_multiplier`,
  `am_bonus_description`,
  `level_perks`,
  `rules_content`,
  `create_by`
) VALUES 

-- Level 1
(
  1, 
  3000.00, 
  11, 
  3000.00, 
  '100%',
  '1 × bond amount',
  '',
  'T+3',
  0.00,
  '',
  '• T+3 payout\n• 0 tax discount',
  '### Level 1 Co-op Member Bond Rules

**Account Requirements:**
- Minimum Bond: **$3,000 USD**
- Account Active Period: **11 days**
- Required Lifetime Trade Volume: **$3,000**

**Trading Limits:**
- Trade Cap Multiplier: **100%** (1x)
- Total Trade Cap: **1 × bond amount** = **$3,000**
- Maximum single order: $3,000

**Payment Terms:**
- Payout Schedule: **T+3** (3 days after order completion)
- Tax Discount: **0%** (No discount at this level)

**Level Perks:**
- Entry level for new Co-op Members
- Basic trading privileges
- Standard support access

**Important Notes:**
- Bond ensures coverage for order disputes and chargebacks
- Progress to Level 2 after meeting trade volume requirements
- Violations may result in bond deductions
- Full bond refund available after 6 months of good standing',
  'system'
),

-- Level 2
(
  2, 
  6000.00, 
  26, 
  12000.00, 
  '200%',
  '2 × bond amount',
  '1/2 bond amount',
  'T+3',
  0.00,
  '',
  '• T+3 payout\n• 0 tax discount',
  '### Level 2 Co-op Member Bond Rules

**Account Requirements:**
- Minimum Bond: **$6,000 USD**
- Account Active Period: **26 days**
- Required Lifetime Trade Volume: **$12,000**

**Trading Limits:**
- Trade Cap Multiplier: **200%** (2x)
- Total Trade Cap: **2 × bond amount** = **$12,000**
- Maximum concurrent orders: $12,000 total

**Payment & Protection:**
- Payout Schedule: **T+3**
- CM Liquidation Payout: **1/2 of bond amount** ($3,000)
- Tax Discount: **0%**

**Level Perks:**
- Increased trading capacity (2x)
- Enhanced liquidation protection coverage
- Priority queue for support tickets

**Benefits:**
- Double the trading capacity of Level 1
- Better protection against market volatility
- Faster path to higher levels',
  'system'
),

-- Level 3
(
  3, 
  9000.00, 
  43, 
  27000.00, 
  '300%',
  '3 × bond amount',
  '1/3 bond amount',
  'T+3',
  0.00,
  '$1,000 sign-on bonus',
  '• T+3 payout\n• 0 tax discount\n• $1,000 sign-on bonus',
  '### Level 3 Co-op Member Bond Rules

**Account Requirements:**
- Minimum Bond: **$9,000 USD**
- Account Active Period: **43 days**
- Required Lifetime Trade Volume: **$27,000**

**Trading Limits:**
- Trade Cap Multiplier: **300%** (3x)
- Total Trade Cap: **3 × bond amount** = **$27,000**

**Payment & Benefits:**
- Payout Schedule: **T+3**
- CM Liquidation Payout: **1/3 of bond amount** ($3,000)
- **🎁 Affiliate Manager Bonus: $1,000 sign-on bonus**

**Special Perks:**
- 3x trading capacity
- Eligible for AM referral bonuses
- Priority support consideration
- Featured vendor badge (optional)

**Major Milestone:**
- First level with AM bonus program
- Significant trust and credibility boost
- Access to exclusive vendor channels',
  'system'
),

-- Level 4
(
  4, 
  12000.00, 
  60, 
  48000.00, 
  '400%',
  '4 × bond amount',
  '',
  'T+2',
  5.00,
  '5% of tax collected',
  '• T+2 Payout\n• 5% Tax discount Multiplier\n• 5% of tax collected',
  '### Level 4 Co-op Member Bond Rules

**Account Requirements:**
- Minimum Bond: **$12,000 USD**
- Account Active Period: **60 days** (2 months)
- Required Lifetime Trade Volume: **$48,000**

**Trading Limits:**
- Trade Cap Multiplier: **400%** (4x)
- Total Trade Cap: **4 × bond amount** = **$48,000**

**Enhanced Benefits:**
- **⚡ Faster Payout: T+2** (2 days after completion)
- **💰 Tax Discount Multiplier: 5%**
- **🎁 AM Bonus: 5% of tax collected**

**Major Upgrade:**
- Faster fund access (T+2 vs T+3)
- Tax benefits begin - save 5% on platform fees
- Revenue sharing with platform
- Professional vendor status
- Access to advanced analytics

**Revenue Example:**
If you generate $1,000 in platform tax:
- AM Bonus: $50 (5% of $1,000)
- Your tax: $950 (5% discount applied)',
  'system'
),

-- Level 5
(
  5, 
  15000.00, 
  79, 
  75000.00, 
  '500%',
  '5 × bond amount',
  '',
  'T+2',
  6.00,
  '6% of tax collected',
  '• T+2 Payout\n• 6% Tax discount Multiplier\n• 6% of tax collected',
  '### Level 5 Co-op Member Bond Rules

**Account Requirements:**
- Minimum Bond: **$15,000 USD**
- Account Active Period: **79 days**
- Required Lifetime Trade Volume: **$75,000**

**Trading Limits:**
- Trade Cap Multiplier: **500%** (5x)
- Total Trade Cap: **5 × bond amount** = **$75,000**

**Premium Benefits:**
- Payout Schedule: **T+2**
- **💰 Tax Discount Multiplier: 6%**
- **🎁 AM Bonus: 6% of tax collected**

**Status:**
- Mid-tier professional vendor
- Higher revenue share
- Increased trust and privileges
- Dedicated account manager (optional)
- Early access to new features

**Growth Milestone:**
- $75K+ in lifetime volume
- Proven reliability and trust
- Substantial business scale',
  'system'
),

-- Level 6
(
  6, 
  18000.00, 
  98, 
  108000.00, 
  '600%',
  '6 × bond amount',
  '1/6 bond amount',
  'T+2',
  7.00,
  '7% of tax collected',
  '• T+2 Payout\n• 7% Tax discount Multiplier\n• 7% of tax collected',
  '### Level 6 Co-op Member Bond Rules

**Account Requirements:**
- Minimum Bond: **$18,000 USD**
- Account Active Period: **98 days**
- Required Lifetime Trade Volume: **$108,000**

**Trading Limits:**
- Trade Cap Multiplier: **600%** (6x)
- Total Trade Cap: **6 × bond amount** = **$108,000**

**Advanced Benefits:**
- Payout Schedule: **T+2**
- CM Liquidation Payout: **1/6 of bond amount** ($3,000)
- **💰 Tax Discount Multiplier: 7%**
- **🎁 AM Bonus: 7% of tax collected**

**High-Volume Vendor:**
- Substantial trading capacity ($108K)
- Enhanced protection ratios
- Significant revenue sharing (7%)
- VIP support channel access
- Quarterly business reviews

**Elite Status Approaching:**
- Top 10% of vendors by volume
- Premium features unlocked
- Invitation to vendor advisory board',
  'system'
),

-- Level 7
(
  7, 
  21000.00, 
  118, 
  147000.00, 
  '700%',
  '7 × bond amount',
  '',
  'T+1',
  8.00,
  '8% of tax collected',
  '• T+1 Payout\n• 8% Tax discount Multiplier\n• 8% of tax collected',
  '### Level 7 Co-op Member Bond Rules

**Account Requirements:**
- Minimum Bond: **$21,000 USD**
- Account Active Period: **118 days**
- Required Lifetime Trade Volume: **$147,000**

**Trading Limits:**
- Trade Cap Multiplier: **700%** (7x)
- Total Trade Cap: **7 × bond amount** = **$147,000**

**Elite Benefits:**
- **⚡⚡ Ultra-Fast Payout: T+1** (Next business day!)
- **💰 Tax Discount Multiplier: 8%**
- **🎁 AM Bonus: 8% of tax collected**

**Elite Status:**
- Next-day fund access
- Top-tier revenue sharing (8%)
- VIP vendor status
- Priority dispute resolution
- Direct communication with leadership
- Exclusive networking events

**Game Changer:**
- T+1 payout is a major competitive advantage
- Nearly instant liquidity
- Maximum operational efficiency
- Top 5% of all vendors',
  'system'
),

-- Level 8
(
  8, 
  24000.00, 
  138, 
  192000.00, 
  '800%',
  '8 × bond amount',
  '',
  'T+1',
  8.00,
  '9% of tax collected',
  '• T+1 Payout\n• 8% Tax discount Multiplier\n• 9% of tax collected',
  '### Level 8 Co-op Member Bond Rules

**Account Requirements:**
- Minimum Bond: **$24,000 USD**
- Account Active Period: **138 days**
- Required Lifetime Trade Volume: **$192,000**

**Trading Limits:**
- Trade Cap Multiplier: **800%** (8x)
- Total Trade Cap: **8 × bond amount** = **$192,000**

**Premium Features:**
- Payout Schedule: **T+1**
- **💰 Tax Discount Multiplier: 8%**
- **🎁 AM Bonus: 9% of tax collected** ⬆️

**Senior Vendor:**
- Proven track record
- Near-instant payouts
- Maximum revenue sharing approaching
- Platinum tier privileges
- Strategic partner status
- Input on platform roadmap

**Excellence Recognized:**
- Nearly $200K in lifetime volume
- Consistent performance
- Top 3% of vendor community',
  'system'
),

-- Level 9
(
  9, 
  27000.00, 
  159, 
  243000.00, 
  '900%',
  '9 × bond amount',
  '',
  'T+1',
  8.00,
  '10% of tax collected',
  '• T+1 Payout\n• 8% Tax discount Multiplier\n• 10% of tax collected',
  '### Level 9 Co-op Member Bond Rules

**Account Requirements:**
- Minimum Bond: **$27,000 USD**
- Account Active Period: **159 days**
- Required Lifetime Trade Volume: **$243,000**

**Trading Limits:**
- Trade Cap Multiplier: **900%** (9x)
- Total Trade Cap: **9 × bond amount** = **$243,000**

**Top-Tier Benefits:**
- Payout Schedule: **T+1**
- **💰 Tax Discount Multiplier: 8%**
- **🎁 AM Bonus: 10% of tax collected** (MAXIMUM!)

**Master Vendor Status:**
- Near-maximum privileges
- Highest revenue share (10%)
- Diamond tier recognition
- Preferential treatment in all matters
- Exclusive partner benefits
- Co-marketing opportunities

**Pinnacle Achievement:**
- $243K+ in lifetime volume
- Top 1% of vendor community
- One step from maximum level
- Leadership within vendor ecosystem',
  'system'
),

-- Level 10 (Maximum Level)
(
  10, 
  30000.00, 
  180, 
  300000.00, 
  '1000%',
  '10 × bond amount',
  '',
  'T+0',
  9.00,
  '10% of tax collected + $10,000 annual bonus (caps at $200,000/year when annual volume exceeds $22M)',
  '• T+0 Instant Payout\n• 9% Tax discount Multiplier\n• 10% of tax collected\n• $10,000 annual bonus (volume-based)',
  '### Level 10 Co-op Member Bond Rules (MAXIMUM LEVEL) 👑

**Account Requirements:**
- Minimum Bond: **$30,000 USD**
- Account Active Period: **180 days** (6 months)
- Required Lifetime Trade Volume: **$300,000**

**Trading Limits:**
- Trade Cap Multiplier: **1000%** (10x)
- Total Trade Cap: **10 × bond amount** = **$300,000**
- Effectively unlimited for most operations

**Ultimate Benefits:**
- **⚡⚡⚡ INSTANT Payout: T+0** (Same day!)
- **💰 Tax Discount Multiplier: 9%** (Maximum!)
- **🎁 AM Bonus: 10% of tax collected**
- **🏆 Annual Bonus: Up to $10,000**

**Annual Bonus Calculation:**
- Base: $10,000 per year
- Caps at: $200,000 per year
- Requirement: Annual trade volume must exceed **$22 Million**
- Formula: $22M × 10% (tax) × 91% (after discount) × 10% (AM share) = $200,020

**Legendary Status:**
- **Immediate fund access (T+0)** - No waiting!
- Maximum tax benefits (9% discount)
- Substantial annual bonuses (up to $200K)
- Exclusive partnership tier
- Direct communication channels with C-suite
- Maximum trust and autonomy
- White-glove service
- Platform co-ownership opportunities

**Notes:**
- This is the highest achievable level
- Reserved for most trusted and highest-volume vendors
- Represents long-term partnership commitment
- Invitation-only level (must be nominated)
- Annual review to maintain status

**Requirements to Maintain:**
- Maintain minimum $300K annual volume
- Zero serious violations
- Active community participation
- Exemplary customer service',
  'system'
)
ON DUPLICATE KEY UPDATE
  `bond_amount` = VALUES(`bond_amount`),
  `account_life_days` = VALUES(`account_life_days`),
  `cumulative_trade_volume` = VALUES(`cumulative_trade_volume`),
  `trade_cap_multiplier` = VALUES(`trade_cap_multiplier`),
  `total_trade_cap_formula` = VALUES(`total_trade_cap_formula`),
  `liquidation_payout` = VALUES(`liquidation_payout`),
  `payout_time` = VALUES(`payout_time`),
  `tax_discount_multiplier` = VALUES(`tax_discount_multiplier`),
  `am_bonus_description` = VALUES(`am_bonus_description`),
  `level_perks` = VALUES(`level_perks`),
  `rules_content` = VALUES(`rules_content`),
  `status` = VALUES(`status`),
  `update_time` = CURRENT_TIMESTAMP,
  `update_by` = VALUES(`create_by`);

-- 3. Add Bond-related fields to VendorApplication table
-- ==========================================
SET @dbname = DATABASE();
SET @tablename = 'mall_vendor_application';

-- Add bond_level
SET @columnname = 'bond_level';
SET @preparedStatement = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
   WHERE (table_name = @tablename) AND (table_schema = @dbname) AND (column_name = @columnname)) > 0,
  'SELECT 1',
  CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN ', @columnname, ' INT DEFAULT 1 COMMENT ''Assigned Bond Level (1-10)'' AFTER status')
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- Add bond_order_id
SET @columnname = 'bond_order_id';
SET @preparedStatement = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
   WHERE (table_name = @tablename) AND (table_schema = @dbname) AND (column_name = @columnname)) > 0,
  'SELECT 1',
  CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN ', @columnname, ' VARCHAR(64) COMMENT ''Bond Payment Order ID'' AFTER bond_level')
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- Add bond_paid_time
SET @columnname = 'bond_paid_time';
SET @preparedStatement = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
   WHERE (table_name = @tablename) AND (table_schema = @dbname) AND (column_name = @columnname)) > 0,
  'SELECT 1',
  CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN ', @columnname, ' DATETIME COMMENT ''Bond Payment Completed Time'' AFTER bond_order_id')
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- Add wallet_btc_provided
SET @columnname = 'wallet_btc_provided';
SET @preparedStatement = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
   WHERE (table_name = @tablename) AND (table_schema = @dbname) AND (column_name = @columnname)) > 0,
  'SELECT 1',
  CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN ', @columnname, ' VARCHAR(255) COMMENT ''BTC Wallet for Withdrawal (Admin verified)'' AFTER bond_paid_time')
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- Add wallet_xmr_provided
SET @columnname = 'wallet_xmr_provided';
SET @preparedStatement = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
   WHERE (table_name = @tablename) AND (table_schema = @dbname) AND (column_name = @columnname)) > 0,
  'SELECT 1',
  CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN ', @columnname, ' VARCHAR(255) COMMENT ''XMR Wallet for Withdrawal (Admin verified)'' AFTER wallet_btc_provided')
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- Add wallet_usdt_provided
SET @columnname = 'wallet_usdt_provided';
SET @preparedStatement = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
   WHERE (table_name = @tablename) AND (table_schema = @dbname) AND (column_name = @columnname)) > 0,
  'SELECT 1',
  CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN ', @columnname, ' VARCHAR(255) COMMENT ''USDT Wallet for Withdrawal (Admin verified)'' AFTER wallet_xmr_provided')
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- Add wallet_verified_time
SET @columnname = 'wallet_verified_time';
SET @preparedStatement = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
   WHERE (table_name = @tablename) AND (table_schema = @dbname) AND (column_name = @columnname)) > 0,
  'SELECT 1',
  CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN ', @columnname, ' DATETIME COMMENT ''Wallet Verification Time'' AFTER wallet_usdt_provided')
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- Add wallet_verified_by
SET @columnname = 'wallet_verified_by';
SET @preparedStatement = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
   WHERE (table_name = @tablename) AND (table_schema = @dbname) AND (column_name = @columnname)) > 0,
  'SELECT 1',
  CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN ', @columnname, ' VARCHAR(64) COMMENT ''Verified By Admin Username'' AFTER wallet_verified_time')
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

COMMIT;

SELECT '✅ Vendor Bond Configuration System created successfully!' as Status;
SELECT 'Created mall_vendor_bond_config table with 10 levels' as Info;
SELECT 'Extended mall_vendor_application with bond-related fields' as Info2;

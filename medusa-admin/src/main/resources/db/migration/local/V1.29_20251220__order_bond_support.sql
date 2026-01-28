-- =============================================
-- Add Bond Payment Support to Orders
-- Version: V1.28
-- Date: 2025-12-20
-- Description: Add order_type and bond-related fields to orders table
-- =============================================

START TRANSACTION;

-- 1. Add order_type field to mall_order
-- ==========================================
SET @dbname = DATABASE();
SET @tablename = 'mall_order';
SET @columnname = 'order_type';
SET @preparedStatement = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
   WHERE (table_name = @tablename) AND (table_schema = @dbname) AND (column_name = @columnname)) > 0,
  'SELECT 1',
  CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN ', @columnname, ' VARCHAR(50) DEFAULT ''NORMAL'' COMMENT ''Order Type: NORMAL, BOND_PAYMENT'' AFTER source_type')
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- 2. Add bond_application_id field
-- ==========================================
SET @columnname = 'bond_application_id';
SET @preparedStatement = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
   WHERE (table_name = @tablename) AND (table_schema = @dbname) AND (column_name = @columnname)) > 0,
  'SELECT 1',
  CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN ', @columnname, ' BIGINT COMMENT ''Associated Vendor Application ID (for bond payments)'' AFTER order_type')
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- 3. Add bond_application_number field
-- ==========================================
SET @columnname = 'bond_application_number';
SET @preparedStatement = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
   WHERE (table_name = @tablename) AND (table_schema = @dbname) AND (column_name = @columnname)) > 0,
  'SELECT 1',
  CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN ', @columnname, ' VARCHAR(64) COMMENT ''Application Number (for bond payments)'' AFTER bond_application_id')
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- 4. Add index for bond_application_id
-- ==========================================
SET @indexname = 'idx_bond_application_id';
SET @preparedStatement = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS
   WHERE table_schema = @dbname AND table_name = @tablename AND index_name = @indexname) > 0,
  'SELECT 1',
  CONCAT('ALTER TABLE ', @tablename, ' ADD INDEX ', @indexname, ' (bond_application_id)')
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- 5. Add index for order_type
-- ==========================================
SET @indexname = 'idx_order_type';
SET @preparedStatement = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS
   WHERE table_schema = @dbname AND table_name = @tablename AND index_name = @indexname) > 0,
  'SELECT 1',
  CONCAT('ALTER TABLE ', @tablename, ' ADD INDEX ', @indexname, ' (order_type)')
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

COMMIT;

SELECT '✅ Order table extended for bond payments!' as Status;
SELECT 'Added order_type, bond_application_id, bond_application_number fields' as Info;

-- Add withdrawal lock fields to mall_vendor_withdrawal_address table
-- Lock period: 3 days after address change

SET @tablename = 'mall_vendor_withdrawal_address';
SET @dbname = DATABASE();

-- Add withdrawal_lock_until field
SET @columnname = 'withdrawal_lock_until';
SET @preparedStatement = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
   WHERE (table_name = @tablename) AND (table_schema = @dbname) AND (column_name = @columnname)) > 0,
  'SELECT 1',
  CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN ', @columnname, ' DATETIME COMMENT ''Withdrawal lock until this date (3 days after address change)'' AFTER previous_address')
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- Add address_changed_at field
SET @columnname = 'address_changed_at';
SET @preparedStatement = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
   WHERE (table_name = @tablename) AND (table_schema = @dbname) AND (column_name = @columnname)) > 0,
  'SELECT 1',
  CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN ', @columnname, ' DATETIME COMMENT ''Address last changed time'' AFTER withdrawal_lock_until')
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

SELECT 'Added withdrawal lock fields to mall_vendor_withdrawal_address table' as Info;

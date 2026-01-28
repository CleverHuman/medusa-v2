-- =============================================
-- Add Threema Contact Fields to Vendor Application
-- Version: V1.38
-- Date: 2025-01-05
-- Description: Add primary_threema and secondary_threema fields to mall_vendor_application table
-- =============================================

START TRANSACTION;

-- Add primary_threema field
ALTER TABLE `mall_vendor_application` 
ADD COLUMN `primary_threema` VARCHAR(100) NULL COMMENT 'Primary Threema Contact' 
AFTER `primary_email`;

-- Add secondary_threema field
ALTER TABLE `mall_vendor_application` 
ADD COLUMN `secondary_threema` VARCHAR(100) NULL COMMENT 'Secondary Threema Contact' 
AFTER `secondary_email`;

COMMIT;

SELECT '✅ Threema contact fields added successfully!' as Status;


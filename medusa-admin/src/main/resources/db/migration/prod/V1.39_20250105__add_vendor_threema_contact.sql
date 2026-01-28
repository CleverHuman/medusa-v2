-- =============================================
-- Add Threema and Secondary Contact Fields to Vendor Table
-- Version: V1.39
-- Date: 2025-01-05
-- Description: Add contact_threema and secondary contact fields to mall_vendor table
-- =============================================

START TRANSACTION;

-- Add contact_threema field
ALTER TABLE `mall_vendor` 
ADD COLUMN `contact_threema` VARCHAR(100) NULL COMMENT 'Threema Contact' 
AFTER `contact_email`;

-- Add secondary contact fields
ALTER TABLE `mall_vendor` 
ADD COLUMN `secondary_telegram` VARCHAR(100) NULL COMMENT 'Secondary Telegram Contact' 
AFTER `contact_threema`;

ALTER TABLE `mall_vendor` 
ADD COLUMN `secondary_signal` VARCHAR(100) NULL COMMENT 'Secondary Signal Contact' 
AFTER `secondary_telegram`;

ALTER TABLE `mall_vendor` 
ADD COLUMN `secondary_jabber` VARCHAR(100) NULL COMMENT 'Secondary Jabber Contact' 
AFTER `secondary_signal`;

ALTER TABLE `mall_vendor` 
ADD COLUMN `secondary_email` VARCHAR(100) NULL COMMENT 'Secondary Email Contact' 
AFTER `secondary_jabber`;

ALTER TABLE `mall_vendor` 
ADD COLUMN `secondary_threema` VARCHAR(100) NULL COMMENT 'Secondary Threema Contact' 
AFTER `secondary_email`;

COMMIT;

SELECT '✅ Vendor Threema and secondary contact fields added successfully!' as Status;


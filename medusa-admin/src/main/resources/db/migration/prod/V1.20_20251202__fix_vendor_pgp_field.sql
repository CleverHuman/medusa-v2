-- =============================================
-- Fix PGP Public Key Field Length Issue
-- =============================================
-- Change pgp_public_key_url from VARCHAR(500) to TEXT
-- to accommodate full PGP public key content
-- =============================================

-- Modify the column type to TEXT to store full PGP public keys
ALTER TABLE mall_vendor 
MODIFY COLUMN `pgp_public_key_url` TEXT COMMENT 'PGP Public Key (full key content)';

-- Also update the comment to clarify this stores the key content, not just a URL
-- The field name is kept for backward compatibility

-- Display success message
SELECT '✅ PGP Public Key field updated to TEXT type' as Status;
SELECT 'mall_vendor.pgp_public_key_url can now store full PGP keys (up to 64KB)' as Info;


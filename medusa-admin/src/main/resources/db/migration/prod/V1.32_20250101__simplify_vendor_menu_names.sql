-- =============================================
-- Simplify Vendor Management Menu Names
-- =============================================
-- Migration: V1.32_20250101__simplify_vendor_menu_names.sql
-- Description: Remove "Vendor" prefix from Vendor Management sub-menu items to make them more concise
-- =============================================

-- Find Vendor Management menu ID
SET @vendor_menu_id = (SELECT menu_id FROM sys_menu WHERE menu_name = 'Vendor Management' AND menu_type = 'M' LIMIT 1);

-- Update menu names to remove "Vendor" prefix
-- Only update menu items under "Vendor Management" (menu_type = 'C' for menu items, not 'F' for buttons)

-- 1. Update "Vendor Application" to "Application"
UPDATE sys_menu 
SET menu_name = 'Application',
    update_by = 'admin',
    update_time = NOW()
WHERE menu_name IN ('Vendor Application', 'Vendor Applications')
  AND menu_type = 'C'
  AND parent_id = @vendor_menu_id;

-- 2. Update "Vendor List" to "List"
UPDATE sys_menu 
SET menu_name = 'List',
    update_by = 'admin',
    update_time = NOW()
WHERE menu_name = 'Vendor List'
  AND menu_type = 'C'
  AND parent_id = @vendor_menu_id;

-- 3. Update "Vendor Product Approval" to "Product Approval"
UPDATE sys_menu 
SET menu_name = 'Product Approval',
    update_by = 'admin',
    update_time = NOW()
WHERE menu_name = 'Vendor Product Approval'
  AND menu_type = 'C'
  AND parent_id = @vendor_menu_id;

-- 4. Update "Vendor Withdrawal" to "Withdrawal"
UPDATE sys_menu 
SET menu_name = 'Withdrawal',
    update_by = 'admin',
    update_time = NOW()
WHERE menu_name = 'Vendor Withdrawal'
  AND menu_type = 'C'
  AND parent_id = @vendor_menu_id;

-- Verify the changes
SELECT '✅ Vendor Management menu names updated successfully!' as Status;
SELECT menu_id, menu_name, parent_id, order_num, path, menu_type
FROM sys_menu
WHERE parent_id = @vendor_menu_id
  AND menu_type = 'C'
ORDER BY order_num;


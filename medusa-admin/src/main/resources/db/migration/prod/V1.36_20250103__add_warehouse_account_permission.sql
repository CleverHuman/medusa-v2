-- =============================================
-- Add Warehouse Account Creation Permission
-- =============================================
-- Migration: V1.36_20250103__add_warehouse_account_permission.sql
-- Description: Add menu permission for creating vendor warehouse staff accounts
-- This permission allows admins to create warehouse accounts for vendors
-- =============================================

-- Find Vendor Management menu ID
SET @vendor_menu_id = (SELECT menu_id FROM sys_menu WHERE menu_name = 'Vendor Management' AND menu_type = 'M' LIMIT 1);

-- Find Vendor List menu ID (after V1.32 migration, it's now called "List")
SET @vendor_list_menu_id = (
    SELECT menu_id FROM sys_menu 
    WHERE menu_name = 'List' 
      AND menu_type = 'C' 
      AND parent_id = @vendor_menu_id
    LIMIT 1
);

-- If not found by new name, try old name
SET @vendor_list_menu_id = IFNULL(@vendor_list_menu_id,
    (SELECT menu_id FROM sys_menu 
     WHERE menu_name = 'Vendor List' 
       AND menu_type = 'C' 
       AND parent_id = @vendor_menu_id
     LIMIT 1)
);

-- Add permission button for creating warehouse accounts
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT 'Create Warehouse Account', @vendor_list_menu_id, 12, '#', '', 1, 0, 'F', '0', '0', 'mall:vendor:warehouse:create', '#', 'admin', NOW(), 'admin', NOW(), 'Create warehouse staff account for vendor'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'mall:vendor:warehouse:create' AND parent_id = @vendor_list_menu_id);

-- Assign this permission to Super Admin and Normal Admin roles
-- Super Admin (vendor_super_admin)
INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT r.role_id, m.menu_id
FROM sys_role r
CROSS JOIN sys_menu m
WHERE r.role_key = 'vendor_super_admin'
  AND r.del_flag = '0'
  AND m.perms = 'mall:vendor:warehouse:create';

-- Normal Admin (vendor_admin)
INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT r.role_id, m.menu_id
FROM sys_role r
CROSS JOIN sys_menu m
WHERE r.role_key = 'vendor_admin'
  AND r.del_flag = '0'
  AND m.perms = 'mall:vendor:warehouse:create';

-- Verify
SELECT '✅ Warehouse Account Permission Added Successfully!' as Status;

-- Show the new permission
SELECT 
    m.menu_id,
    m.menu_name,
    m.parent_id,
    m.perms,
    m.menu_type
FROM sys_menu m
WHERE m.perms = 'mall:vendor:warehouse:create';


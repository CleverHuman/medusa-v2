-- =============================================
-- Vendor Warehouse Staff Role and Permissions
-- =============================================
-- Migration: V1.35_20250103__vendor_warehouse_staff_role.sql
-- Description: Create vendor warehouse staff role for viewing and managing vendor orders
-- Features:
--   1. Add vendor_id field to sys_user table to link warehouse staff to vendors
--   2. Create vendor_warehouse_staff role
--   3. Assign order viewing and editing permissions (default has edit permission)
-- =============================================

-- =============================================
-- 1. Add vendor_id field to sys_user table
-- =============================================
ALTER TABLE sys_user 
ADD COLUMN vendor_id BIGINT(20) NULL COMMENT 'Vendor ID (for vendor warehouse staff accounts)' AFTER dept_id,
ADD INDEX idx_vendor_id (vendor_id);

-- =============================================
-- 2. Create Vendor Warehouse Staff Role
-- =============================================
INSERT INTO sys_role (role_name, role_key, role_sort, data_scope, menu_check_strictly, dept_check_strictly, status, del_flag, create_by, create_time, remark)
SELECT 'Vendor Warehouse Staff', 'vendor_warehouse_staff', 7, '2', 1, 1, '0', '0', 'admin', NOW(), 'Vendor warehouse staff for viewing and managing orders of their vendor. Default has edit permission.'
WHERE NOT EXISTS (SELECT 1 FROM sys_role WHERE role_key = 'vendor_warehouse_staff' AND del_flag = '0');

-- Get the role ID for permission assignment
SET @warehouse_staff_role_id = (SELECT role_id FROM sys_role WHERE role_key = 'vendor_warehouse_staff' AND del_flag = '0' LIMIT 1);

-- =============================================
-- 3. Assign Order Permissions to Warehouse Staff
-- =============================================
-- Find Orders menu (vorder) - menu_id 2009
-- Assign both view and edit permissions by default
INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT @warehouse_staff_role_id, m.menu_id
FROM sys_menu m
WHERE m.perms IN (
    -- Order viewing permissions
    'mall:vorder:list',
    'mall:vorder:query',
    'mall:order:list',
    'mall:order:query',
    -- Order editing permissions (default enabled)
    'mall:vorder:edit',
    'mall:order:edit'
)
OR (
    -- Orders menu (main menu)
    m.menu_id = 2009
);

-- =============================================
-- 4. Verify the setup
-- =============================================
SELECT '✅ Vendor Warehouse Staff Role Created Successfully!' as Status;

-- Show role information
SELECT 
    r.role_id,
    r.role_name,
    r.role_key,
    r.data_scope,
    r.remark
FROM sys_role r
WHERE r.role_key = 'vendor_warehouse_staff'
  AND r.del_flag = '0';

-- Show assigned permissions
SELECT 
    r.role_key,
    r.role_name,
    m.menu_id,
    m.menu_name,
    m.perms
FROM sys_role r
JOIN sys_role_menu rm ON r.role_id = rm.role_id
JOIN sys_menu m ON rm.menu_id = m.menu_id
WHERE r.role_key = 'vendor_warehouse_staff'
  AND r.del_flag = '0'
ORDER BY m.menu_id;


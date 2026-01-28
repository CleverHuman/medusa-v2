-- =============================================
-- Add Interview Schedule Menu to System
-- =============================================

-- Step 1: Find Vendor Management Menu (wherever it is)
-- It could be under System or Mall, we'll find it dynamically
SELECT @vendor_menu_id := menu_id 
FROM sys_menu 
WHERE menu_name = 'Vendor Management' 
  AND menu_type = 'M' 
LIMIT 1;

-- Display where Vendor Management was found
SELECT CONCAT('Found Vendor Management menu with ID: ', IFNULL(@vendor_menu_id, 'NOT FOUND')) as Info;

-- If Vendor Management doesn't exist, find System menu and create it there
SELECT @system_menu_id := menu_id 
FROM sys_menu 
WHERE menu_name = 'System' 
  AND menu_type = 'M' 
LIMIT 1;

-- Step 2: Create Vendor Management if it doesn't exist (under System)
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT 'Vendor Management', 
       IFNULL(@system_menu_id, 0), 
       10, 
       'vendor', 
       NULL, 
       1, 0, 'M', '0', '0', NULL, 'peoples', 'admin', NOW(), 'admin', NOW(), 'Vendor Management'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_name = 'Vendor Management' AND menu_type = 'M');

-- Get Vendor Management menu ID again (in case we just created it)
SELECT @vendor_menu_id := menu_id 
FROM sys_menu 
WHERE menu_name = 'Vendor Management' 
  AND menu_type = 'M' 
LIMIT 1;

-- Step 3: Add Vendor Application Menu (if not exists)
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT 'Vendor Applications', @vendor_menu_id, 1, 'application', 'mall/vendor/application/index', 1, 0, 'C', '0', '0', 'mall:vendor:application:list', 'form', 'admin', NOW(), 'admin', NOW(), 'Vendor Application Management'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE path = 'application' AND parent_id = @vendor_menu_id);

-- Step 4: Add Interview Schedule Menu (NEW!)
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT 'Interview Schedule', @vendor_menu_id, 2, 'interview', 'mall/vendor/interview/index', 1, 0, 'C', '0', '0', 'mall:vendor:interview:list', 'date', 'admin', NOW(), 'admin', NOW(), 'Interview Schedule Management'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE path = 'interview' AND parent_id = @vendor_menu_id);

-- Get Interview Schedule menu ID for adding permissions
SELECT @interview_menu_id := menu_id FROM sys_menu WHERE path = 'interview' AND parent_id = @vendor_menu_id LIMIT 1;

-- Step 5: Add Interview Management Permissions (Sub-menus/Buttons)
-- Query Permission
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT 'Interview Query', @interview_menu_id, 1, '#', '', 1, 0, 'F', '0', '0', 'mall:vendor:interview:query', '#', 'admin', NOW(), 'admin', NOW(), 'Interview Query Permission'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'mall:vendor:interview:query' AND parent_id = @interview_menu_id);

-- Add Permission
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT 'Schedule Interview', @interview_menu_id, 2, '#', '', 1, 0, 'F', '0', '0', 'mall:vendor:interview:add', '#', 'admin', NOW(), 'admin', NOW(), 'Schedule Interview Permission'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'mall:vendor:interview:add' AND parent_id = @interview_menu_id);

-- Edit Permission
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT 'Edit Interview', @interview_menu_id, 3, '#', '', 1, 0, 'F', '0', '0', 'mall:vendor:interview:edit', '#', 'admin', NOW(), 'admin', NOW(), 'Edit Interview Permission'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'mall:vendor:interview:edit' AND parent_id = @interview_menu_id);

-- Remove Permission
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT 'Delete Interview', @interview_menu_id, 4, '#', '', 1, 0, 'F', '0', '0', 'mall:vendor:interview:remove', '#', 'admin', NOW(), 'admin', NOW(), 'Delete Interview Permission'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'mall:vendor:interview:remove' AND parent_id = @interview_menu_id);

-- Export Permission
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT 'Export Interview', @interview_menu_id, 5, '#', '', 1, 0, 'F', '0', '0', 'mall:vendor:interview:export', '#', 'admin', NOW(), 'admin', NOW(), 'Export Interview Permission'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'mall:vendor:interview:export' AND parent_id = @interview_menu_id);

-- Step 6: Add Schedule Interview permission to Application menu (for Schedule button)
SELECT @application_menu_id := menu_id FROM sys_menu WHERE path = 'application' AND parent_id = @vendor_menu_id LIMIT 1;

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT 'Schedule Interview', @application_menu_id, 10, '#', '', 1, 0, 'F', '0', '0', 'mall:vendor:interview:schedule', '#', 'admin', NOW(), 'admin', NOW(), 'Schedule Interview from Application'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'mall:vendor:interview:schedule' AND parent_id = @application_menu_id);

-- Step 7: Grant all Interview permissions to admin role
-- Find admin role ID
SELECT @admin_role_id := role_id FROM sys_role WHERE role_key = 'admin' LIMIT 1;

-- Grant Interview Schedule menu access
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT @admin_role_id, menu_id
FROM sys_menu
WHERE menu_name = 'Interview Schedule' AND parent_id = @vendor_menu_id
AND NOT EXISTS (
    SELECT 1 FROM sys_role_menu 
    WHERE role_id = @admin_role_id 
    AND menu_id = (SELECT menu_id FROM sys_menu WHERE menu_name = 'Interview Schedule' AND parent_id = @vendor_menu_id LIMIT 1)
);

-- Grant all Interview permissions to admin role
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT @admin_role_id, menu_id
FROM sys_menu
WHERE parent_id = @interview_menu_id
AND NOT EXISTS (
    SELECT 1 FROM sys_role_menu rm
    WHERE rm.role_id = @admin_role_id AND rm.menu_id = sys_menu.menu_id
);

-- Display results
SELECT '✅ Interview Schedule Menu Added Successfully!' as Status;
SELECT 'Mall > Vendor Management > Interview Schedule' as MenuPath;
SELECT menu_id, menu_name, path, perms, icon FROM sys_menu WHERE menu_name LIKE '%Interview%' OR perms LIKE '%interview%';


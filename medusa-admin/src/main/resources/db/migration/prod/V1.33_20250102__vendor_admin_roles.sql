-- =============================================
-- Vendor Admin Roles and Permissions
-- =============================================
-- Migration: V1.33_20250102__vendor_admin_roles.sql
-- Description: Create 3 admin roles for vendor management:
--   1. Super Admin - can change wallet addresses
--   2. Pay Admin - can process withdrawals (send crypto, record TXID, mark transactions)
--   3. Normal Admin - can manage day-to-day vendor issues (approvals, etc.)
-- =============================================

-- Find Vendor Management menu ID
SET @vendor_menu_id = (SELECT menu_id FROM sys_menu WHERE menu_name = 'Vendor Management' AND menu_type = 'M' LIMIT 1);

-- Find Withdrawal menu ID
SET @withdrawal_menu_id = (SELECT menu_id FROM sys_menu WHERE menu_name = 'Vendor Withdrawal' AND menu_type = 'C' AND parent_id = @vendor_menu_id LIMIT 1);

-- If Vendor Withdrawal menu not found by old name, try new name (after V1.32 migration)
SET @withdrawal_menu_id = IFNULL(@withdrawal_menu_id, 
    (SELECT menu_id FROM sys_menu WHERE menu_name = 'Withdrawal' AND menu_type = 'C' AND parent_id = @vendor_menu_id LIMIT 1));

-- 1. Create Super Admin Role (vendor_super_admin)
INSERT INTO sys_role (role_name, role_key, role_sort, data_scope, menu_check_strictly, dept_check_strictly, status, del_flag, create_by, create_time, remark)
SELECT 'Vendor Super Admin', 'vendor_super_admin', 4, '1', 1, 1, '0', '0', 'admin', NOW(), 'Super admin with full vendor management access including wallet address modification'
WHERE NOT EXISTS (SELECT 1 FROM sys_role WHERE role_key = 'vendor_super_admin' AND del_flag = '0');

SET @super_admin_role_id = (SELECT role_id FROM sys_role WHERE role_key = 'vendor_super_admin' AND del_flag = '0' LIMIT 1);

-- 2. Create Pay Admin Role (vendor_pay_admin)
INSERT INTO sys_role (role_name, role_key, role_sort, data_scope, menu_check_strictly, dept_check_strictly, status, del_flag, create_by, create_time, remark)
SELECT 'Vendor Pay Admin', 'vendor_pay_admin', 5, '1', 1, 1, '0', '0', 'admin', NOW(), 'Pay admin for processing vendor withdrawals: view requests, verify addresses, send crypto, record TXID, mark transactions'
WHERE NOT EXISTS (SELECT 1 FROM sys_role WHERE role_key = 'vendor_pay_admin' AND del_flag = '0');

SET @pay_admin_role_id = (SELECT role_id FROM sys_role WHERE role_key = 'vendor_pay_admin' AND del_flag = '0' LIMIT 1);

-- 3. Create Normal Admin Role (vendor_admin)
INSERT INTO sys_role (role_name, role_key, role_sort, data_scope, menu_check_strictly, dept_check_strictly, status, del_flag, create_by, create_time, remark)
SELECT 'Vendor Admin', 'vendor_admin', 6, '1', 1, 1, '0', '0', 'admin', NOW(), 'Normal admin for day-to-day vendor management: approvals, applications, vendor list, etc.'
WHERE NOT EXISTS (SELECT 1 FROM sys_role WHERE role_key = 'vendor_admin' AND del_flag = '0');

SET @normal_admin_role_id = (SELECT role_id FROM sys_role WHERE role_key = 'vendor_admin' AND del_flag = '0' LIMIT 1);

-- 4. Add permission for wallet address modification (for super admin)
-- This will be a button permission under Vendor List or Withdrawal menu
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT 'Update Wallet Address', @withdrawal_menu_id, 11, '#', '', 1, 0, 'F', '0', '0', 'mall:vendor:withdrawal:address:update', '#', 'admin', NOW(), 'admin', NOW(), 'Update vendor withdrawal wallet address'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'mall:vendor:withdrawal:address:update' AND parent_id = @withdrawal_menu_id);

SET @wallet_address_update_menu_id = (SELECT menu_id FROM sys_menu WHERE perms = 'mall:vendor:withdrawal:address:update' AND parent_id = @withdrawal_menu_id LIMIT 1);

-- 5. Assign permissions to Super Admin
-- Super Admin gets all vendor permissions including wallet address update
INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT @super_admin_role_id, menu_id FROM sys_menu 
WHERE menu_id = @vendor_menu_id 
   OR parent_id = @vendor_menu_id
   OR perms LIKE 'mall:vendor:%';

-- Add wallet address update permission specifically
INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
VALUES (@super_admin_role_id, @wallet_address_update_menu_id);

-- 6. Assign permissions to Pay Admin
-- Pay Admin can:
--   - View withdrawal requests (list, query)
--   - Process withdrawals (process, complete, fail)
--   - View statistics
INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT @pay_admin_role_id, menu_id FROM sys_menu 
WHERE menu_id = @withdrawal_menu_id
   OR perms IN (
       'mall:vendor:withdrawal:list',
       'mall:vendor:withdrawal:query',
       'mall:vendor:withdrawal:process',
       'mall:vendor:withdrawal:complete',
       'mall:vendor:withdrawal:fail',
       'mall:vendor:withdrawal:stats',
       'mall:vendor:balance:query',
       'mall:vendor:balance:logs'
   );

-- 7. Assign permissions to Normal Admin
-- Normal Admin can:
--   - Manage vendor applications (approve, reject, edit)
--   - Manage vendor list (query, edit)
--   - Manage vendor products (approve, edit)
--   - View withdrawals and approve/reject (but cannot process)
INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT @normal_admin_role_id, menu_id FROM sys_menu 
WHERE perms LIKE 'mall:vendor:application:%'
   OR perms = 'mall:vendor:list'
   OR perms = 'mall:vendor:query'
   OR perms = 'mall:vendor:edit'
   OR perms LIKE 'mall:vendor:product:%'
   OR perms IN (
       'mall:vendor:withdrawal:list',
       'mall:vendor:withdrawal:query',
       'mall:vendor:withdrawal:approve',
       'mall:vendor:withdrawal:reject',
       'mall:vendor:withdrawal:stats'
   )
   OR menu_id = @vendor_menu_id
   OR (parent_id = @vendor_menu_id AND menu_type = 'C' AND menu_name IN ('Application', 'List', 'Product Approval', 'Withdrawal'));

-- 8. Verify the setup
SELECT '✅ Vendor Admin Roles Created Successfully!' as Status;
SELECT @super_admin_role_id as super_admin_role_id, @pay_admin_role_id as pay_admin_role_id, @normal_admin_role_id as normal_admin_role_id;

-- Show role permissions summary
SELECT 
    r.role_id,
    r.role_name,
    r.role_key,
    COUNT(rm.menu_id) as permission_count
FROM sys_role r
LEFT JOIN sys_role_menu rm ON r.role_id = rm.role_id
WHERE r.role_key IN ('vendor_super_admin', 'vendor_pay_admin', 'vendor_admin')
GROUP BY r.role_id, r.role_name, r.role_key;

-- =============================================
-- Create User Accounts for Vendor Admin Roles
-- =============================================

-- 9. Create Vendor Super Admin User
INSERT INTO sys_user (
    dept_id, 
    user_name, 
    nick_name, 
    user_type, 
    email, 
    phonenumber, 
    sex, 
    avatar, 
    password, 
    status, 
    del_flag, 
    login_ip, 
    login_date, 
    create_by, 
    create_time, 
    update_by, 
    update_time, 
    remark
)
SELECT 
    100,                                    -- dept_id (default department)
    'vendor_super_admin',                  -- user_name
    'Vendor Super Admin',                  -- nick_name
    '00',                                  -- user_type (00=system user)
    'vendor.super.admin@medusa.com',       -- email
    '',                                    -- phonenumber
    '0',                                   -- sex (0=male, 1=female, 2=unknown)
    '',                                    -- avatar
    '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', -- password (admin123)
    '0',                                   -- status (0=normal, 1=disabled)
    '0',                                   -- del_flag (0=exists, 2=deleted)
    '',                                    -- login_ip
    NULL,                                  -- login_date
    'admin',                               -- create_by
    NOW(),                                 -- create_time
    '',                                    -- update_by
    NULL,                                  -- update_time
    'Vendor Super Admin - Can change wallet addresses' -- remark
WHERE NOT EXISTS (SELECT 1 FROM sys_user WHERE user_name = 'vendor_super_admin' AND del_flag = '0');

SET @super_admin_user_id = (SELECT user_id FROM sys_user WHERE user_name = 'vendor_super_admin' AND del_flag = '0' LIMIT 1);

-- Associate user with role
INSERT IGNORE INTO sys_user_role (user_id, role_id)
VALUES (@super_admin_user_id, @super_admin_role_id);

-- 10. Create Vendor Pay Admin User
INSERT INTO sys_user (
    dept_id, 
    user_name, 
    nick_name, 
    user_type, 
    email, 
    phonenumber, 
    sex, 
    avatar, 
    password, 
    status, 
    del_flag, 
    login_ip, 
    login_date, 
    create_by, 
    create_time, 
    update_by, 
    update_time, 
    remark
)
SELECT 
    100,                                    -- dept_id
    'vendor_pay_admin',                    -- user_name
    'Vendor Pay Admin',                    -- nick_name
    '00',                                  -- user_type
    'vendor.pay.admin@medusa.com',         -- email
    '',                                    -- phonenumber
    '0',                                   -- sex
    '',                                    -- avatar
    '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', -- password (admin123)
    '0',                                   -- status
    '0',                                   -- del_flag
    '',                                    -- login_ip
    NULL,                                  -- login_date
    'admin',                               -- create_by
    NOW(),                                 -- create_time
    '',                                    -- update_by
    NULL,                                  -- update_time
    'Vendor Pay Admin - Can process withdrawals' -- remark
WHERE NOT EXISTS (SELECT 1 FROM sys_user WHERE user_name = 'vendor_pay_admin' AND del_flag = '0');

SET @pay_admin_user_id = (SELECT user_id FROM sys_user WHERE user_name = 'vendor_pay_admin' AND del_flag = '0' LIMIT 1);

-- Associate user with role
INSERT IGNORE INTO sys_user_role (user_id, role_id)
VALUES (@pay_admin_user_id, @pay_admin_role_id);

-- 11. Create Vendor Admin User (Normal Admin)
INSERT INTO sys_user (
    dept_id, 
    user_name, 
    nick_name, 
    user_type, 
    email, 
    phonenumber, 
    sex, 
    avatar, 
    password, 
    status, 
    del_flag, 
    login_ip, 
    login_date, 
    create_by, 
    create_time, 
    update_by, 
    update_time, 
    remark
)
SELECT 
    100,                                    -- dept_id
    'vendor_admin',                        -- user_name
    'Vendor Admin',                        -- nick_name
    '00',                                  -- user_type
    'vendor.admin@medusa.com',             -- email
    '',                                    -- phonenumber
    '0',                                   -- sex
    '',                                    -- avatar
    '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', -- password (admin123)
    '0',                                   -- status
    '0',                                   -- del_flag
    '',                                    -- login_ip
    NULL,                                  -- login_date
    'admin',                               -- create_by
    NOW(),                                 -- create_time
    '',                                    -- update_by
    NULL,                                  -- update_time
    'Vendor Admin - Day-to-day vendor management' -- remark
WHERE NOT EXISTS (SELECT 1 FROM sys_user WHERE user_name = 'vendor_admin' AND del_flag = '0');

SET @normal_admin_user_id = (SELECT user_id FROM sys_user WHERE user_name = 'vendor_admin' AND del_flag = '0' LIMIT 1);

-- Associate user with role
INSERT IGNORE INTO sys_user_role (user_id, role_id)
VALUES (@normal_admin_user_id, @normal_admin_role_id);

-- 12. Verify user creation
SELECT '✅ Vendor Admin Users Created Successfully!' as Status;

-- Show created users with their roles
SELECT 
    u.user_id,
    u.user_name,
    u.nick_name,
    u.email,
    u.status,
    r.role_name,
    r.role_key
FROM sys_user u
LEFT JOIN sys_user_role ur ON u.user_id = ur.user_id
LEFT JOIN sys_role r ON ur.role_id = r.role_id
WHERE u.user_name IN ('vendor_super_admin', 'vendor_pay_admin', 'vendor_admin')
ORDER BY u.user_name;

-- =============================================
-- IMPORTANT NOTES:
-- =============================================
-- Default password for all accounts: admin123
-- Users should change their passwords after first login
-- To generate a custom password hash, use Java code:
--    String hash = SecurityUtils.encryptPassword("your_password");
-- Then update the password in sys_user table:
--    UPDATE sys_user SET password = 'your_hash_here' WHERE user_name = 'vendor_super_admin';
-- =============================================


-- V1.21_20251202__vendor_product_menu.sql

-- This script adds the "Vendor Product Approval" menu item under "Vendor Management"

-- Insert Vendor Product Approval menu under Vendor Management (parent_id = 2050)
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES 
(
    'Vendor Product Approval',        -- menu_name
    2050,                              -- parent_id (Vendor Management menu_id)
    4,                                 -- order_num (after Withdrawal Management which is 3)
    'vendor-product',                  -- path
    'mall/vendor/product/index',      -- component
    1,                                 -- is_frame (0=yes, 1=no)
    0,                                 -- is_cache (0=cache, 1=no cache)
    'C',                               -- menu_type (M=directory, C=menu, F=button)
    '0',                               -- visible ('0'=visible, '1'=hidden)
    '0',                               -- status ('0'=enabled, '1'=disabled)
    'mall:vendor:product:list',        -- perms (permission identifier)
    'shopping',                        -- icon
    'admin',                           -- create_by
    sysdate(),                         -- create_time
    '',                                -- update_by
    NULL,                              -- update_time
    'Vendor product approval and management' -- remark
)
ON DUPLICATE KEY UPDATE menu_name = menu_name;

-- Get the menu_id of the newly inserted menu (or existing menu if duplicate)
SET @vendor_product_menu_id = (SELECT menu_id FROM sys_menu WHERE menu_name = 'Vendor Product Approval' AND parent_id = 2050);

-- Insert sub-menu items (buttons) for Vendor Product Approval
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES 
-- Query permission
(
    'Query Product',
    @vendor_product_menu_id,
    1,
    '#',
    '',
    1,
    0,
    'F',
    '0',
    '0',
    'mall:vendor:product:query',
    '#',
    'admin',
    sysdate(),
    '',
    NULL,
    ''
),
-- Approve permission
(
    'Approve Product',
    @vendor_product_menu_id,
    2,
    '#',
    '',
    1,
    0,
    'F',
    '0',
    '0',
    'mall:vendor:product:approve',
    '#',
    'admin',
    sysdate(),
    '',
    NULL,
    ''
),
-- Edit permission
(
    'Edit Product',
    @vendor_product_menu_id,
    3,
    '#',
    '',
    1,
    0,
    'F',
    '0',
    '0',
    'mall:vendor:product:edit',
    '#',
    'admin',
    sysdate(),
    '',
    NULL,
    ''
),
-- Export permission
(
    'Export Product',
    @vendor_product_menu_id,
    4,
    '#',
    '',
    1,
    0,
    'F',
    '0',
    '0',
    'mall:vendor:product:export',
    '#',
    'admin',
    sysdate(),
    '',
    NULL,
    ''
)
ON DUPLICATE KEY UPDATE menu_name = menu_name;

-- Verify insertion
SELECT 'Vendor Product Approval menu added successfully' AS Status;
SELECT menu_id, menu_name, parent_id, order_num, path, component, perms
FROM sys_menu
WHERE menu_name = 'Vendor Product Approval' OR parent_id = @vendor_product_menu_id;


-- =============================================
-- Add Dashboard Menu
-- =============================================
-- Migration: V1.42_20260121__add_dashboard_menu.sql
-- Description: Add Dashboard menu item at the top level, displayed before Customers menu
-- =============================================

-- Insert Dashboard menu as a top-level menu item
-- parent_id = 0 means it's a top-level menu
-- order_num = -1 ensures it appears before Customers (which has order_num = 0)
INSERT INTO sys_menu (
    menu_name, 
    parent_id, 
    order_num, 
    path, 
    component, 
    query,
    route_name,
    is_frame, 
    is_cache, 
    menu_type, 
    visible, 
    status, 
    perms, 
    icon, 
    create_by, 
    create_time, 
    update_by, 
    update_time, 
    remark
)
VALUES (
    'Dashboard',                    -- menu_name
    0,                              -- parent_id (0 = top-level menu)
    -1,                             -- order_num (display before Customers which has order_num = 0)
    'dashboard',                    -- path
    'dashboard/index',              -- component (corresponds to views/dashboard/index.vue)
    NULL,                           -- query
    '',                             -- route_name
    1,                              -- is_frame (0=yes, 1=no)
    0,                              -- is_cache (0=cache, 1=no cache)
    'C',                            -- menu_type (M=directory, C=menu, F=button)
    '0',                            -- visible ('0'=visible, '1'=hidden)
    '0',                            -- status ('0'=enabled, '1'=disabled)
    'mall:dashboard:view',          -- perms (permission identifier)
    'dashboard',                    -- icon
    'admin',                        -- create_by
    NOW(),                          -- create_time
    '',                             -- update_by
    NULL,                           -- update_time
    'Dashboard Menu'                -- remark
)
ON DUPLICATE KEY UPDATE menu_name = menu_name;

-- Verify the insertion
SELECT '✅ Dashboard menu added successfully!' AS Status;
SELECT menu_id, menu_name, parent_id, order_num, path, component, icon, visible, status
FROM sys_menu
WHERE menu_name = 'Dashboard' AND parent_id = 0;


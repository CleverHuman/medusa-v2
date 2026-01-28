-- 假设你的 Mall 菜单 ID 是 2000（需要根据实际情况调整）
SET @mall_menu_id = 1;

-- 1. 创建 Vendor Management 主菜单
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time) 
VALUES ('Vendor Management', @mall_menu_id, 6, 'vendor', NULL, 1, 0, 'M', '0', '0', NULL, 'peoples', 'admin', NOW());

SET @vendor_menu_id = LAST_INSERT_ID();

-- 2. 创建 Vendor Application 子菜单
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time) 
VALUES ('Vendor Application', @vendor_menu_id, 1, 'application', 'mall/vendor/application/index', 1, 0, 'C', '0', '0', 'mall:vendor:application:list', 'form', 'admin', NOW());

SET @app_menu_id = LAST_INSERT_ID();

-- 3. 创建 Vendor List 子菜单
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time) 
VALUES ('Vendor List', @vendor_menu_id, 2, 'list', 'mall/vendor/list/index', 1, 0, 'C', '0', '0', 'mall:vendor:list', 'peoples', 'admin', NOW());

SET @list_menu_id = LAST_INSERT_ID();

-- 4. 添加 Vendor Application 按钮权限
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time) VALUES
('Query Application', @app_menu_id, 1, '#', '', 1, 0, 'F', '0', '0', 'mall:vendor:application:query', '#', 'admin', NOW()),
('Add Application', @app_menu_id, 2, '#', '', 1, 0, 'F', '0', '0', 'mall:vendor:application:add', '#', 'admin', NOW()),
('Edit Application', @app_menu_id, 3, '#', '', 1, 0, 'F', '0', '0', 'mall:vendor:application:edit', '#', 'admin', NOW()),
('Delete Application', @app_menu_id, 4, '#', '', 1, 0, 'F', '0', '0', 'mall:vendor:application:remove', '#', 'admin', NOW()),
('Approve Application', @app_menu_id, 5, '#', '', 1, 0, 'F', '0', '0', 'mall:vendor:application:approve', '#', 'admin', NOW()),
('Reject Application', @app_menu_id, 6, '#', '', 1, 0, 'F', '0', '0', 'mall:vendor:application:reject', '#', 'admin', NOW()),
('Export Application', @app_menu_id, 7, '#', '', 1, 0, 'F', '0', '0', 'mall:vendor:application:export', '#', 'admin', NOW());

-- 5. 添加 Vendor List 按钮权限
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time) VALUES
('Query Vendor', @list_menu_id, 1, '#', '', 1, 0, 'F', '0', '0', 'mall:vendor:query', '#', 'admin', NOW()),
('Add Vendor', @list_menu_id, 2, '#', '', 1, 0, 'F', '0', '0', 'mall:vendor:add', '#', 'admin', NOW()),
('Edit Vendor', @list_menu_id, 3, '#', '', 1, 0, 'F', '0', '0', 'mall:vendor:edit', '#', 'admin', NOW()),
('Delete Vendor', @list_menu_id, 4, '#', '', 1, 0, 'F', '0', '0', 'mall:vendor:remove', '#', 'admin', NOW()),
('Export Vendor', @list_menu_id, 5, '#', '', 1, 0, 'F', '0', '0', 'mall:vendor:export', '#', 'admin', NOW());

-- 6. 给管理员角色分配权限（假设角色 ID 为 1）
INSERT INTO sys_role_menu (role_id, menu_id) 
SELECT 1, menu_id FROM sys_menu WHERE menu_name IN ('Vendor Management', 'Vendor Application', 'Vendor List');
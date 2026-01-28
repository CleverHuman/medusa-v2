-- 添加 Shipping Options 管理相关的菜单和权限配置

-- 1. 添加 Shipping Options 管理主菜单
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) 
VALUES (2045, 'Shipping Options', 0, 5, 'mall/shipping', 'mall/shipping/index', null, '', 1, 0, 'C', '0', '0', 'mall:shipping:list', 'truck', 'admin', NOW(), '', null, 'Shipping Options Management Menu');

-- 2. 添加 Shipping Options 管理的功能权限

-- 查询权限
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) 
VALUES (2046, 'shipping_query', 2045, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'mall:shipping:query', '#', 'admin', NOW(), '', null, '');

-- 新增权限
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) 
VALUES (2047, 'shipping_add', 2045, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'mall:shipping:add', '#', 'admin', NOW(), '', null, '');

-- 修改权限
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) 
VALUES (2048, 'shipping_edit', 2045, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'mall:shipping:edit', '#', 'admin', NOW(), '', null, '');

-- 删除权限
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) 
VALUES (2049, 'shipping_remove', 2045, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'mall:shipping:remove', '#', 'admin', NOW(), '', null, '');

-- 3. 为超级管理员角色分配权限
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 2045);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 2046);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 2047);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 2048);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 2049);

-- 更新 mall_shipping_method 表中的 code 字段
-- 执行前请先备份数据库！

-- 显示更新前的数据
SELECT 'Before Update:' as status, code, name FROM mall_shipping_method ORDER BY code;

-- 执行更新操作
UPDATE mall_shipping_method SET code = 'RB' WHERE code = 'RP';
UPDATE mall_shipping_method SET code = 'EB' WHERE code = 'EP';
UPDATE mall_shipping_method SET code = 'RBT' WHERE code = 'RSP';
UPDATE mall_shipping_method SET code = 'EBT' WHERE code = 'ESP';
UPDATE mall_shipping_method SET code = 'HVP' WHERE code = 'SSP';

-- 显示更新后的数据
SELECT 'After Update:' as status, code, name FROM mall_shipping_method ORDER BY code;

-- 显示更新统计
SELECT 
    'Update Summary:' as status,
    COUNT(*) as total_records,
    SUM(CASE WHEN code IN ('RB', 'EB', 'RBT', 'EBT', 'HVP') THEN 1 ELSE 0 END) as updated_records
FROM mall_shipping_method;

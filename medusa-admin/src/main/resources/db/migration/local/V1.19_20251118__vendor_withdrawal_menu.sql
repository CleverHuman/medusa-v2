-- =============================================
-- Vendor Withdrawal Management Menu
-- =============================================
-- 在 Vendor Management (menu_id=2050) 下添加提现管理菜单
-- =============================================

-- 1. 添加 Vendor Withdrawal Management 主菜单
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (2072, 'Vendor Withdrawal', 2050, 4, 'withdrawal', 'mall/vendor/withdrawal/index', 1, 0, 'C', '0', '0', 'mall:vendor:withdrawal:list', 'money', 'admin', NOW(), 'admin', NOW(), 'Vendor Withdrawal Management');

-- 2. 添加 Vendor Withdrawal 操作权限按钮
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES
-- 查询权限
(2073, 'Query Withdrawal', 2072, 1, '#', '', 1, 0, 'F', '0', '0', 'mall:vendor:withdrawal:query', '#', 'admin', NOW(), 'admin', NOW(), ''),
-- 审批权限
(2074, 'Approve Withdrawal', 2072, 2, '#', '', 1, 0, 'F', '0', '0', 'mall:vendor:withdrawal:approve', '#', 'admin', NOW(), 'admin', NOW(), ''),
-- 拒绝权限
(2075, 'Reject Withdrawal', 2072, 3, '#', '', 1, 0, 'F', '0', '0', 'mall:vendor:withdrawal:reject', '#', 'admin', NOW(), 'admin', NOW(), ''),
-- 处理权限
(2076, 'Process Withdrawal', 2072, 4, '#', '', 1, 0, 'F', '0', '0', 'mall:vendor:withdrawal:process', '#', 'admin', NOW(), 'admin', NOW(), ''),
-- 完成权限
(2077, 'Complete Withdrawal', 2072, 5, '#', '', 1, 0, 'F', '0', '0', 'mall:vendor:withdrawal:complete', '#', 'admin', NOW(), 'admin', NOW(), ''),
-- 失败权限
(2078, 'Fail Withdrawal', 2072, 6, '#', '', 1, 0, 'F', '0', '0', 'mall:vendor:withdrawal:fail', '#', 'admin', NOW(), 'admin', NOW(), ''),
-- 查询余额权限
(2079, 'Query Balance', 2072, 7, '#', '', 1, 0, 'F', '0', '0', 'mall:vendor:balance:query', '#', 'admin', NOW(), 'admin', NOW(), ''),
-- 查询余额日志权限
(2080, 'Query Balance Logs', 2072, 8, '#', '', 1, 0, 'F', '0', '0', 'mall:vendor:balance:logs', '#', 'admin', NOW(), 'admin', NOW(), ''),
-- 统计权限
(2081, 'Statistics', 2072, 9, '#', '', 1, 0, 'F', '0', '0', 'mall:vendor:withdrawal:stats', '#', 'admin', NOW(), 'admin', NOW(), ''),
-- 导出权限
(2082, 'Export Withdrawal', 2072, 10, '#', '', 1, 0, 'F', '0', '0', 'mall:vendor:withdrawal:export', '#', 'admin', NOW(), 'admin', NOW(), '');

-- 3. 给管理员角色(role_id=1)分配新菜单权限
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(1, 2072),  -- Vendor Withdrawal 主菜单
(1, 2073),  -- Query Withdrawal
(1, 2074),  -- Approve Withdrawal
(1, 2075),  -- Reject Withdrawal
(1, 2076),  -- Process Withdrawal
(1, 2077),  -- Complete Withdrawal
(1, 2078),  -- Fail Withdrawal
(1, 2079),  -- Query Balance
(1, 2080),  -- Query Balance Logs
(1, 2081),  -- Statistics
(1, 2082);  -- Export Withdrawal

-- 4. 显示结果
SELECT '✅ Vendor Withdrawal Menu Added Successfully!' as Status;
SELECT 'Mall > Vendor Management > Vendor Withdrawal' as MenuPath;
SELECT menu_id, menu_name, path, perms, icon 
FROM sys_menu 
WHERE menu_id BETWEEN 2072 AND 2082
ORDER BY menu_id;


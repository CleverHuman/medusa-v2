-- =============================================
-- Add Interview Permissions to Vendor Admin
-- =============================================
-- Migration: V1.37_20250104__add_interview_permissions_to_vendor_admin.sql
-- Description: 为 vendor_admin 角色添加 Interview Schedule 和订单查看相关权限
-- 问题1: vendor_admin 用户登录后在 Interview Schedule 标签显示无权限，
--        且 "Manage Available Slots" 按钮不显示
-- 问题2: 创建 SLOT 后显示 "Failed to load slots"
-- 问题3: 点击 List/Calendar 显示 "当前操作没有权限"
-- 问题4: vendor_admin 需要查看 vendor 付款的订单（特别是 BOND 订单）以便确认支付状态
-- 根本原因: 
--   - V1.12 migration 缺少独立的 mall:vendor:interview:list 权限菜单项（F类型）
--   - vendor_admin 缺少订单查看权限
-- 解决方案: 
--   1. 添加缺失的 Interview List 权限菜单项 (mall:vendor:interview:list, menu_type='F')
--   2. 为 vendor_admin 角色分配所有 Interview 相关的菜单和权限
--   3. 为 vendor_admin 角色添加订单查看权限（list, query）以便查看 BOND 付款订单
-- 包括的权限:
--   Interview 权限:
--   - mall:vendor:interview:list (查看面试列表、slots 和 calendar) ✅ 新增独立权限项
--   - mall:vendor:interview:query (查询面试详情)
--   - mall:vendor:interview:add (创建面试和 slots)
--   - mall:vendor:interview:edit (编辑面试和 slots)
--   - mall:vendor:interview:remove (删除面试和 slots)
--   - mall:vendor:interview:export (导出面试数据)
--   - mall:vendor:interview:schedule (从 Application 安排面试)
--   订单权限（新增）:
--   - mall:order:list (查看订单列表，包括 BOND 付款订单) ✅ 新增
--   - mall:order:query (查看订单详情) ✅ 新增
-- =============================================

-- 获取 Vendor Management 和 Interview Schedule 菜单 ID
SET @vendor_menu_id = (SELECT menu_id FROM sys_menu WHERE menu_name = 'Vendor Management' AND menu_type = 'M' LIMIT 1);
SET @interview_menu_id = (SELECT menu_id FROM sys_menu WHERE path = 'interview' AND menu_type = 'C' AND parent_id = @vendor_menu_id LIMIT 1);
SET @application_menu_id = (SELECT menu_id FROM sys_menu WHERE path = 'application' AND menu_type = 'C' AND parent_id = @vendor_menu_id LIMIT 1);

-- =============================================
-- 0. 添加缺失的 Interview List 权限菜单项（关键修复）
-- =============================================
-- 问题: V1.12 只将 mall:vendor:interview:list 分配给主菜单（C类型）
-- 解决: 创建独立的按钮级权限（F类型）供后端 API 权限检查使用
-- 后端 API /admin/mall/vendor/interview/list 和 /calendar 都需要这个权限
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT 'Interview List', @interview_menu_id, 0, '#', '', 1, 0, 'F', '0', '0', 'mall:vendor:interview:list', '#', 'admin', NOW(), 'admin', NOW(), 'Interview List and Calendar View Permission'
WHERE @interview_menu_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM sys_menu 
    WHERE perms = 'mall:vendor:interview:list' 
      AND parent_id = @interview_menu_id 
      AND menu_type = 'F'
  );

-- =============================================
-- 1. 为 vendor_admin 角色添加 Interview 相关权限
-- =============================================
-- 1.1 添加 Interview Schedule 主菜单访问权限
INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT r.role_id, @interview_menu_id
FROM sys_role r
WHERE r.role_key = 'vendor_admin'
  AND r.del_flag = '0'
  AND @interview_menu_id IS NOT NULL;

-- 1.2 添加所有 Interview 功能权限（子菜单/按钮）
INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT r.role_id, m.menu_id
FROM sys_role r
CROSS JOIN sys_menu m
WHERE r.role_key = 'vendor_admin'
  AND r.del_flag = '0'
  AND (
    -- Interview Schedule 下的所有子权限
    m.parent_id = @interview_menu_id
    -- 或所有 interview 相关权限（通过 perms 匹配）
    OR m.perms LIKE 'mall:vendor:interview:%'
  );

-- =============================================
-- 2. 为 vendor_super_admin 角色添加 Interview 相关权限（确保完整）
-- =============================================
-- 2.1 添加 Interview Schedule 主菜单访问权限
INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT r.role_id, @interview_menu_id
FROM sys_role r
WHERE r.role_key = 'vendor_super_admin'
  AND r.del_flag = '0'
  AND @interview_menu_id IS NOT NULL;

-- 2.2 添加所有 Interview 功能权限
INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT r.role_id, m.menu_id
FROM sys_role r
CROSS JOIN sys_menu m
WHERE r.role_key = 'vendor_super_admin'
  AND r.del_flag = '0'
  AND (
    -- Interview Schedule 下的所有子权限
    m.parent_id = @interview_menu_id
    -- 或所有 interview 相关权限
    OR m.perms LIKE 'mall:vendor:interview:%'
  );

-- =============================================
-- 3. 也为 admin 角色添加新的 Interview List 权限
-- =============================================
SET @admin_role_id = (SELECT role_id FROM sys_role WHERE role_key = 'admin' LIMIT 1);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT @admin_role_id, menu_id
FROM sys_menu
WHERE perms = 'mall:vendor:interview:list' 
  AND parent_id = @interview_menu_id 
  AND menu_type = 'F'
  AND @admin_role_id IS NOT NULL;

-- =============================================
-- 4. 为 vendor_admin 角色添加订单查看权限（新增）
-- =============================================
-- 目的: 让 vendor_admin 能够查看 vendor 的 BOND 付款订单
-- 用途: 确认 vendor 是否已支付 BOND，以便设置 BOND 和 LEVEL

-- 4.1 添加订单查看权限（list 和 query）
INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT r.role_id, m.menu_id
FROM sys_role r
CROSS JOIN sys_menu m
WHERE r.role_key = 'vendor_admin'
  AND r.del_flag = '0'
  AND m.perms IN ('mall:order:list', 'mall:order:query');

-- 4.2 也为 vendor_super_admin 添加订单查看权限
INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT r.role_id, m.menu_id
FROM sys_role r
CROSS JOIN sys_menu m
WHERE r.role_key = 'vendor_super_admin'
  AND r.del_flag = '0'
  AND m.perms IN ('mall:order:list', 'mall:order:query');

-- =============================================
-- 验证和诊断
-- =============================================
SELECT '✅ Interview & Order Permissions Added to Vendor Admin Successfully!' as Status;

-- 显示新添加的 Interview List 权限菜单项
SELECT 
    '=== New Interview List Permission ===' as Section,
    menu_id,
    menu_name,
    parent_id,
    perms,
    menu_type,
    order_num
FROM sys_menu
WHERE perms = 'mall:vendor:interview:list' 
  AND parent_id = @interview_menu_id 
  AND menu_type = 'F';

-- 显示 vendor_admin 角色的 Interview 权限详情
SELECT 
    '=== vendor_admin Interview Permissions ===' as Section,
    r.role_id,
    r.role_name,
    r.role_key,
    m.menu_id,
    m.menu_name,
    m.perms,
    m.menu_type,
    m.parent_id
FROM sys_role r
LEFT JOIN sys_role_menu rm ON r.role_id = rm.role_id
LEFT JOIN sys_menu m ON rm.menu_id = m.menu_id
WHERE r.role_key = 'vendor_admin'
  AND r.del_flag = '0'
  AND (m.perms LIKE 'mall:vendor:interview:%' OR m.path = 'interview')
ORDER BY m.order_num;

-- 统计每个角色的 Interview 权限数量
SELECT 
    '=== Interview Permission Count ===' as Section,
    r.role_key,
    r.role_name,
    COUNT(rm.menu_id) as interview_permission_count
FROM sys_role r
LEFT JOIN sys_role_menu rm ON r.role_id = rm.role_id
LEFT JOIN sys_menu m ON rm.menu_id = m.menu_id
WHERE r.role_key IN ('vendor_super_admin', 'vendor_pay_admin', 'vendor_admin')
  AND r.del_flag = '0'
  AND (m.perms LIKE 'mall:vendor:interview:%' OR m.path = 'interview')
GROUP BY r.role_key, r.role_name
ORDER BY r.role_key;

-- 显示所有可用的 Interview 权限菜单项
SELECT 
    '=== All Interview Menu Items ===' as Section,
    menu_id,
    menu_name,
    parent_id,
    perms,
    menu_type,
    path,
    order_num
FROM sys_menu
WHERE perms LIKE 'mall:vendor:interview:%' 
   OR path = 'interview'
ORDER BY parent_id, order_num;

-- 显示 vendor_admin 的订单权限详情（新增验证）
SELECT 
    '=== vendor_admin Order Permissions ===' as Section,
    r.role_id,
    r.role_name,
    r.role_key,
    m.menu_id,
    m.menu_name,
    m.perms,
    m.menu_type
FROM sys_role r
LEFT JOIN sys_role_menu rm ON r.role_id = rm.role_id
LEFT JOIN sys_menu m ON rm.menu_id = m.menu_id
WHERE r.role_key = 'vendor_admin'
  AND r.del_flag = '0'
  AND m.perms IN ('mall:order:list', 'mall:order:query')
ORDER BY m.perms;

-- 汇总统计
SELECT 
    '=== Permission Summary ===' as Section,
    r.role_key,
    SUM(CASE WHEN m.perms LIKE 'mall:vendor:interview:%' OR m.path = 'interview' THEN 1 ELSE 0 END) as interview_permissions,
    SUM(CASE WHEN m.perms IN ('mall:order:list', 'mall:order:query') THEN 1 ELSE 0 END) as order_permissions
FROM sys_role r
LEFT JOIN sys_role_menu rm ON r.role_id = rm.role_id
LEFT JOIN sys_menu m ON rm.menu_id = m.menu_id
WHERE r.role_key IN ('vendor_admin', 'vendor_super_admin')
  AND r.del_flag = '0'
GROUP BY r.role_key;


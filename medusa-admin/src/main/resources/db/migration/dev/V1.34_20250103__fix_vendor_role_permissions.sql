-- =============================================
-- Fix Vendor Admin Role Permissions
-- =============================================
-- Migration: V1.34_20250103__fix_vendor_role_permissions.sql
-- Description: 修复vendor admin角色的菜单权限分配问题
-- 问题原因：
--   1. V1.33脚本中使用的用户定义变量在执行时可能为NULL，导致权限分配失败
--   2. 缺少system菜单权限，导致Vendor Management菜单无法显示（因为Vendor Management是system的子菜单）
-- 解决方案：
--   1. 使用子查询替代变量，直接在查询中获取角色ID和菜单ID
--   2. 为所有vendor admin角色分配system菜单（menu_id=1）权限，确保菜单树能正确构建
-- =============================================

-- =============================================
-- 1. 为 Super Admin 分配权限
-- =============================================
-- Super Admin 拥有所有 vendor 相关权限
-- 注意：需要分配 system 菜单权限，因为 Vendor Management 是 system 的子菜单
INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT r.role_id, m.menu_id 
FROM sys_role r
CROSS JOIN sys_menu m
WHERE r.role_key = 'vendor_super_admin'
  AND r.del_flag = '0'
  AND (
    -- System 菜单（必须，因为 Vendor Management 是它的子菜单）
    m.menu_id = 1
    -- 父级菜单 Vendor Management
    OR m.menu_id = (SELECT menu_id FROM sys_menu WHERE menu_name = 'Vendor Management' AND menu_type = 'M' LIMIT 1)
    -- 所有 Vendor Management 下的子菜单
    OR m.parent_id = (SELECT menu_id FROM sys_menu WHERE menu_name = 'Vendor Management' AND menu_type = 'M' LIMIT 1)
    -- 所有 vendor 相关的权限（通过 perms 匹配）
    OR m.perms LIKE 'mall:vendor:%'
  );

-- 确保钱包地址更新权限也被分配
INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT r.role_id, m.menu_id
FROM sys_role r
CROSS JOIN sys_menu m
WHERE r.role_key = 'vendor_super_admin'
  AND r.del_flag = '0'
  AND m.perms = 'mall:vendor:withdrawal:address:update';

-- =============================================
-- 2. 为 Pay Admin 分配权限
-- =============================================
-- Pay Admin 可以处理提现相关操作
-- 注意：需要分配 system 菜单权限，因为 Vendor Management 是 system 的子菜单
INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT r.role_id, m.menu_id
FROM sys_role r
CROSS JOIN sys_menu m
WHERE r.role_key = 'vendor_pay_admin'
  AND r.del_flag = '0'
  AND (
    -- System 菜单（必须，因为 Vendor Management 是它的子菜单）
    m.menu_id = 1
    -- 父级菜单 Vendor Management（必须，否则看不到菜单树）
    OR m.menu_id = (SELECT menu_id FROM sys_menu WHERE menu_name = 'Vendor Management' AND menu_type = 'M' LIMIT 1)
    -- Withdrawal 子菜单
    OR m.menu_id = (
        SELECT menu_id FROM sys_menu 
        WHERE menu_name IN ('Vendor Withdrawal', 'Withdrawal') 
          AND menu_type = 'C' 
          AND parent_id = (SELECT menu_id FROM sys_menu WHERE menu_name = 'Vendor Management' AND menu_type = 'M' LIMIT 1)
        LIMIT 1
    )
    -- 提现相关权限
    OR m.perms IN (
        'mall:vendor:withdrawal:list',
        'mall:vendor:withdrawal:query',
        'mall:vendor:withdrawal:process',
        'mall:vendor:withdrawal:complete',
        'mall:vendor:withdrawal:fail',
        'mall:vendor:withdrawal:stats',
        'mall:vendor:balance:query',
        'mall:vendor:balance:logs'
    )
  );

-- =============================================
-- 3. 为 Normal Admin 分配权限
-- =============================================
-- Normal Admin 处理日常vendor管理
-- 注意：需要分配 system 菜单权限，因为 Vendor Management 是 system 的子菜单
INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT r.role_id, m.menu_id
FROM sys_role r
CROSS JOIN sys_menu m
WHERE r.role_key = 'vendor_admin'
  AND r.del_flag = '0'
  AND (
    -- System 菜单（必须，因为 Vendor Management 是它的子菜单）
    m.menu_id = 1
    -- 父级菜单 Vendor Management（必须）
    OR m.menu_id = (SELECT menu_id FROM sys_menu WHERE menu_name = 'Vendor Management' AND menu_type = 'M' LIMIT 1)
    -- Application 相关权限
    OR m.perms LIKE 'mall:vendor:application:%'
    -- Vendor 列表相关权限
    OR m.perms IN (
        'mall:vendor:list',
        'mall:vendor:query',
        'mall:vendor:edit'
    )
    -- Product 相关权限
    OR m.perms LIKE 'mall:vendor:product:%'
    -- Withdrawal 查看和审批权限
    OR m.perms IN (
        'mall:vendor:withdrawal:list',
        'mall:vendor:withdrawal:query',
        'mall:vendor:withdrawal:approve',
        'mall:vendor:withdrawal:reject',
        'mall:vendor:withdrawal:stats'
    )
    -- Vendor Management 下的主要子菜单
    OR (m.parent_id = (SELECT menu_id FROM sys_menu WHERE menu_name = 'Vendor Management' AND menu_type = 'M' LIMIT 1) 
        AND m.menu_type = 'C' 
        AND m.menu_name IN ('Application', 'List', 'Product Approval', 'Withdrawal'))
  );

-- =============================================
-- 验证
-- =============================================
SELECT '✅ Vendor Admin Role Permissions Fixed Successfully!' as Status;

-- 显示每个角色的权限数量
SELECT 
    r.role_id,
    r.role_name,
    r.role_key,
    COUNT(rm.menu_id) as permission_count
FROM sys_role r
LEFT JOIN sys_role_menu rm ON r.role_id = rm.role_id
WHERE r.role_key IN ('vendor_super_admin', 'vendor_pay_admin', 'vendor_admin')
  AND r.del_flag = '0'
GROUP BY r.role_id, r.role_name, r.role_key
ORDER BY r.role_key;


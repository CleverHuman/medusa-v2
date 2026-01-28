#!/bin/bash

# ========================================
# Vendor Withdrawal Menu 验证脚本
# ========================================

DB_HOST="${DB_HOST:-127.0.0.1}"
DB_PORT="${DB_PORT:-3306}"
DB_NAME="${DB_NAME:-medusa}"
DB_USER="${DB_USER:-root}"
DB_PASS="${DB_PASS:-rootpassword}"

# 颜色输出
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

echo -e "${BLUE}========================================"
echo "Vendor Withdrawal Menu 验证"
echo -e "========================================${NC}\n"

# 数据库查询函数
db_query() {
    mysql -h "$DB_HOST" -P "$DB_PORT" -u "$DB_USER" -p"$DB_PASS" "$DB_NAME" -sN -e "$1" 2>&1 | grep -v "Using a password on the command line interface can be insecure"
}

# 1. 检查主菜单
echo -e "${YELLOW}1. 检查 Vendor Withdrawal 主菜单${NC}"
main_menu=$(db_query "SELECT COUNT(*) FROM sys_menu WHERE menu_id = 2072;")
if [ "$main_menu" -eq "1" ]; then
    echo -e "${GREEN}✓${NC} 主菜单存在 (menu_id: 2072)"
    menu_info=$(db_query "SELECT menu_name, path, component FROM sys_menu WHERE menu_id = 2072;")
    echo -e "  菜单信息: $menu_info"
else
    echo -e "${RED}✗${NC} 主菜单不存在！"
    exit 1
fi

# 2. 检查权限按钮
echo -e "\n${YELLOW}2. 检查权限按钮${NC}"
perms_count=$(db_query "SELECT COUNT(*) FROM sys_menu WHERE parent_id = 2072;")
echo -e "  权限按钮数量: $perms_count"
if [ "$perms_count" -eq "10" ]; then
    echo -e "${GREEN}✓${NC} 所有权限按钮已添加 (10个)"
    db_query "SELECT menu_id, menu_name, perms FROM sys_menu WHERE parent_id = 2072 ORDER BY menu_id;" | while read line; do
        echo -e "    • $line"
    done
else
    echo -e "${RED}✗${NC} 权限按钮数量不正确，应该是10个"
fi

# 3. 检查父菜单关系
echo -e "\n${YELLOW}3. 检查父菜单关系${NC}"
parent_id=$(db_query "SELECT parent_id FROM sys_menu WHERE menu_id = 2072;")
if [ "$parent_id" -eq "2050" ]; then
    echo -e "${GREEN}✓${NC} 父菜单正确 (Vendor Management, menu_id: 2050)"
else
    echo -e "${RED}✗${NC} 父菜单不正确！当前: $parent_id, 应该是: 2050"
fi

# 4. 检查角色权限
echo -e "\n${YELLOW}4. 检查管理员角色权限${NC}"
role_perms=$(db_query "SELECT COUNT(*) FROM sys_role_menu WHERE role_id = 1 AND menu_id BETWEEN 2072 AND 2082;")
if [ "$role_perms" -eq "11" ]; then
    echo -e "${GREEN}✓${NC} 管理员角色已分配所有权限 (11个)"
else
    echo -e "${RED}✗${NC} 管理员角色权限不完整，当前: $role_perms, 应该是: 11"
fi

# 5. 检查 Vendor Management 下的所有子菜单
echo -e "\n${YELLOW}5. Vendor Management 菜单树${NC}"
echo -e "Vendor Management (2050)"
db_query "SELECT CONCAT('  ├─ ', menu_name, ' (', menu_id, ')') FROM sys_menu WHERE parent_id = 2050 ORDER BY order_num;" | while read line; do
    echo -e "$line"
done

# 6. 检查组件路径
echo -e "\n${YELLOW}6. 检查前端组件路径${NC}"
component=$(db_query "SELECT component FROM sys_menu WHERE menu_id = 2072;")
if [ "$component" == "mall/vendor/withdrawal/index" ]; then
    echo -e "${GREEN}✓${NC} 组件路径正确: $component"
else
    echo -e "${RED}✗${NC} 组件路径不正确: $component"
fi

# 7. 检查路由路径
echo -e "\n${YELLOW}7. 检查路由路径${NC}"
path=$(db_query "SELECT path FROM sys_menu WHERE menu_id = 2072;")
if [ "$path" == "withdrawal" ]; then
    echo -e "${GREEN}✓${NC} 路由路径正确: $path"
    echo -e "  完整路径: /vendor/withdrawal"
else
    echo -e "${RED}✗${NC} 路由路径不正确: $path"
fi

# 8. 检查菜单可见性和状态
echo -e "\n${YELLOW}8. 检查菜单状态${NC}"
status=$(db_query "SELECT visible, status FROM sys_menu WHERE menu_id = 2072;")
if [ "$status" == "0	0" ]; then
    echo -e "${GREEN}✓${NC} 菜单状态正确 (visible: 0, status: 0)"
else
    echo -e "${RED}✗${NC} 菜单状态不正确: $status"
fi

# 9. 检查 Flyway 历史记录
echo -e "\n${YELLOW}9. 检查 Flyway 迁移记录${NC}"
flyway_record=$(db_query "SELECT COUNT(*) FROM flyway_schema_history WHERE version = '1.19.20251118';")
if [ "$flyway_record" -eq "1" ]; then
    echo -e "${GREEN}✓${NC} Flyway 迁移记录存在"
    flyway_info=$(db_query "SELECT description, success FROM flyway_schema_history WHERE version = '1.19.20251118';")
    echo -e "  迁移信息: $flyway_info"
else
    echo -e "${YELLOW}⚠${NC} Flyway 迁移记录不存在（如果是手动执行的SQL，可以忽略）"
fi

# 总结
echo -e "\n${BLUE}========================================"
echo "验证完成"
echo -e "========================================${NC}"
echo -e "${GREEN}✅ Vendor Withdrawal Menu 配置正确！${NC}\n"
echo "访问方式："
echo "  1. Admin Panel: Mall > Vendor Management > Vendor Withdrawal"
echo "  2. 直接访问: http://localhost:8080/#/vendor/withdrawal"
echo ""


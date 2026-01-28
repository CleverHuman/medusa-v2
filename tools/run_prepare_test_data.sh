#!/bin/bash

# ============================================
# 测试数据准备脚本执行器
# ============================================

DB_HOST="127.0.0.1"
DB_PORT="3306"
DB_NAME="medusa"
DB_USER="root"

echo "=========================================="
echo "Vendor Integration 测试数据准备"
echo "=========================================="
echo "数据库: ${DB_USER}@${DB_HOST}:${DB_PORT}/${DB_NAME}"
echo ""

# 提示输入密码
read -sp "请输入 MySQL root 密码: " DB_PASS
echo ""

# 检查 SQL 文件是否存在
SQL_FILE="prepare_test_data.sql"
if [ ! -f "$SQL_FILE" ]; then
    echo "错误: 找不到 $SQL_FILE 文件"
    exit 1
fi

# 执行 SQL 脚本
echo "正在执行测试数据准备脚本..."
mysql -h ${DB_HOST} -P ${DB_PORT} -u ${DB_USER} -p${DB_PASS} ${DB_NAME} < ${SQL_FILE}

if [ $? -eq 0 ]; then
    echo ""
    echo "✓ 测试数据准备完成！"
    echo ""
    echo "可以使用以下账号进行测试:"
    echo "  Vendor 登录: testvendor / test123"
    echo "  客户登录: testcustomer / test123"
    echo ""
    echo "测试产品:"
    echo "  - PLATFORM-PROD-TEST-001 (平台产品)"
    echo "  - VENDOR-PROD-TEST-001 (Vendor 产品，待审批)"
    echo "  - VENDOR-PROD-TEST-002 (Vendor 产品，已审批)"
else
    echo ""
    echo "✗ 执行失败，请检查:"
    echo "  1. 数据库连接信息是否正确"
    echo "  2. 密码是否正确"
    echo "  3. 数据库是否存在"
    echo "  4. 是否有执行权限"
    exit 1
fi


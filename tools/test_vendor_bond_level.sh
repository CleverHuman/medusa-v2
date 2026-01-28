#!/bin/bash

# ============================================
# Vendor Bond & Level 系统自动化测试脚本
# ============================================
# 使用方法: ./test_vendor_bond_level.sh

# 配置
API_BASE="${API_BASE:-http://localhost:8080/api}"
VENDOR_USERNAME="${VENDOR_USERNAME:-testvendor}"
VENDOR_PASSWORD="${VENDOR_PASSWORD:-test123}"
VENDOR_ID=100
COOKIE_FILE="${COOKIE_FILE:-/tmp/vendor_bond_session.cookies}"

# 数据库配置
DB_HOST="${DB_HOST:-127.0.0.1}"
DB_PORT="${DB_PORT:-3306}"
DB_NAME="${DB_NAME:-medusa}"
DB_USER="${DB_USER:-root}"
DB_PASS="${DB_PASS:-}"

# 检查数据库连接（在开始测试前）
if [ -z "$DB_PASS" ]; then
    echo -e "${YELLOW}提示: 如果数据库需要密码，请设置 DB_PASS 环境变量${NC}"
    echo "  例如: export DB_PASS=your_password"
    echo ""
fi

# 颜色输出
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 测试结果统计
PASSED=0
FAILED=0
TOTAL=0

# 清理旧的 cookie
rm -f "$COOKIE_FILE"

# 辅助函数
log_test() {
    TOTAL=$((TOTAL + 1))
    if [ $1 -eq 0 ]; then
        echo -e "${GREEN}✓${NC} $2"
        PASSED=$((PASSED + 1))
    else
        echo -e "${RED}✗${NC} $2"
        FAILED=$((FAILED + 1))
    fi
}

check_response() {
    local response=$1
    local expected_code=${2:-200}
    if echo "$response" | jq -e ".code == $expected_code" > /dev/null 2>&1; then
        return 0
    else
        echo "Response: $response" >&2
        return 1
    fi
}

# 数据库查询函数
db_query() {
    local query=$1
    local skip_header=${2:-1}  # 默认跳过第一行（表头）
    local result
    if [ -z "$DB_PASS" ]; then
        result=$(mysql -h "$DB_HOST" -P "$DB_PORT" -u "$DB_USER" "$DB_NAME" -N -e "$query" 2>&1)
    else
        result=$(mysql -h "$DB_HOST" -P "$DB_PORT" -u "$DB_USER" -p"$DB_PASS" "$DB_NAME" -N -e "$query" 2>&1)
    fi
    # 过滤掉 MySQL 警告信息，只保留数据
    result=$(echo "$result" | grep -v "Using a password on the command line interface can be insecure")
    echo "$result"
}

# 安全数值计算函数
safe_calc() {
    local expr=$1
    local result=$(echo "$expr" | bc 2>/dev/null)
    if [ -z "$result" ] || [ "$result" = "" ]; then
        echo "0"
    else
        echo "$result"
    fi
}

# 安全数值比较函数
safe_compare() {
    local val1=$1
    local op=$2
    local val2=$3
    # 确保值是数字
    val1=${val1:-0}
    val2=${val2:-0}
    local result=$(echo "$val1 $op $val2" | bc 2>/dev/null)
    if [ "$result" = "1" ]; then
        return 0
    else
        return 1
    fi
}

# 开始测试
echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}Vendor Bond & Level 系统测试${NC}"
echo -e "${BLUE}========================================${NC}"
echo "API Base: $API_BASE"
echo "Vendor ID: $VENDOR_ID"
echo ""

# ============================================
# 第一部分: Vendor 登录
# ============================================
echo -e "${YELLOW}=== 第一部分: Vendor 登录 ===${NC}"

echo -e "\n${BLUE}TC-LOGIN: Vendor 登录${NC}"
LOGIN_RESPONSE=$(curl -s -X POST ${API_BASE}/mall/vendor/member/login \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "username=${VENDOR_USERNAME}&password=${VENDOR_PASSWORD}" \
  -c "${COOKIE_FILE}")

VENDOR_TOKEN=$(echo $LOGIN_RESPONSE | jq -r '.data.token // .token // empty' 2>/dev/null)
if [ -n "$VENDOR_TOKEN" ] && [ "$VENDOR_TOKEN" != "null" ]; then
    log_test 0 "Vendor 登录成功"
    echo "  Token: ${VENDOR_TOKEN:0:30}..."
else
    log_test 1 "Vendor 登录失败"
    echo "$LOGIN_RESPONSE" | jq . 2>/dev/null || echo "$LOGIN_RESPONSE"
    echo -e "${RED}无法继续测试，请检查登录信息${NC}"
    exit 1
fi

# ============================================
# 第二部分: Bond 和 Level 基础功能测试
# ============================================
echo -e "\n${YELLOW}=== 第二部分: Bond 和 Level 基础功能测试 ===${NC}"

# TC-001: 查询 Vendor Bond 和 Level 信息
echo -e "\n${BLUE}TC-001: 查询 Vendor Bond 和 Level 信息${NC}"
# 先检查 Vendor 是否存在（包括已删除的）
VENDOR_ALL=$(db_query "SELECT COUNT(*) FROM mall_vendor WHERE id = $VENDOR_ID")
VENDOR_ALL_COUNT=$(echo "$VENDOR_ALL" | awk '{print $1}' | head -n 1)
# 检查未删除的记录
VENDOR_EXISTS=$(db_query "SELECT COUNT(*) FROM mall_vendor WHERE id = $VENDOR_ID AND del_flag = '0'")
VENDOR_COUNT=$(echo "$VENDOR_EXISTS" | awk '{print $1}' | head -n 1)
if [ "$VENDOR_COUNT" = "0" ] || [ -z "$VENDOR_COUNT" ]; then
    if [ "$VENDOR_ALL_COUNT" = "1" ]; then
        log_test 1 "查询 Bond 和 Level 失败 - Vendor ID $VENDOR_ID 存在但已被删除（del_flag != '0'）"
        echo -e "  ${RED}提示: 请运行以下 SQL 恢复记录:${NC}"
        echo "  UPDATE mall_vendor SET del_flag = '0' WHERE id = $VENDOR_ID;"
    else
        log_test 1 "查询 Bond 和 Level 失败 - Vendor ID $VENDOR_ID 不存在"
        echo -e "  ${RED}提示: 请先运行测试数据准备脚本:${NC}"
        echo "  mysql -h $DB_HOST -P $DB_PORT -u $DB_USER -p $DB_NAME < prepare_vendor_bond_test_data.sql"
    fi
    BOND=0
    LEVEL=1
    POINTS=0
else
    BOND_INFO=$(db_query "SELECT bond, level, sales_points FROM mall_vendor WHERE id = $VENDOR_ID AND del_flag = '0'")
    if [ -n "$BOND_INFO" ]; then
        BOND=$(echo "$BOND_INFO" | awk '{print $1}' | head -n 1)
        LEVEL=$(echo "$BOND_INFO" | awk '{print $2}' | head -n 1)
        POINTS=$(echo "$BOND_INFO" | awk '{print $3}' | head -n 1)
        # 检查是否为空或 NULL
        if [ -z "$BOND" ] || [ "$BOND" = "NULL" ]; then
            BOND=0
        fi
        if [ -z "$LEVEL" ] || [ "$LEVEL" = "NULL" ]; then
            LEVEL=1
        fi
        if [ -z "$POINTS" ] || [ "$POINTS" = "NULL" ]; then
            POINTS=0
        fi
        log_test 0 "查询 Bond 和 Level 成功"
        echo "  Bond: \$$BOND"
        echo "  Level: $LEVEL"
        echo "  Sales Points: $POINTS"
    else
        log_test 1 "查询 Bond 和 Level 失败 - 查询结果为空"
        echo -e "  ${YELLOW}提示: Vendor 存在但 bond/level 字段可能为 NULL，请检查数据${NC}"
        BOND=0
        LEVEL=1
        POINTS=0
    fi
fi

# TC-002: 计算最大销售限额
echo -e "\n${BLUE}TC-002: 计算最大销售限额${NC}"
MAX_LIMIT_RAW=$(db_query "SELECT (bond * level) as max_limit FROM mall_vendor WHERE id = $VENDOR_ID AND del_flag = '0'")
MAX_LIMIT=$(echo "$MAX_LIMIT_RAW" | awk '{print $1}' | head -n 1)
MAX_LIMIT=${MAX_LIMIT:-0}
EXPECTED_LIMIT=$(safe_calc "$BOND * $LEVEL")
if [ -n "$MAX_LIMIT" ] && [ -n "$EXPECTED_LIMIT" ]; then
    # 使用数值比较而不是字符串比较
    MAX_LIMIT_INT=$(echo "$MAX_LIMIT" | awk '{printf "%.0f", $1}')
    EXPECTED_LIMIT_INT=$(echo "$EXPECTED_LIMIT" | awk '{printf "%.0f", $1}')
    if [ "$MAX_LIMIT_INT" = "$EXPECTED_LIMIT_INT" ]; then
        log_test 0 "最大销售限额计算正确"
        echo "  Max Sales Limit: \$$MAX_LIMIT (Bond: \$$BOND × Level: $LEVEL)"
    else
        log_test 1 "最大销售限额计算错误"
        echo "  预期: \$$EXPECTED_LIMIT, 实际: \$$MAX_LIMIT"
    fi
else
    log_test 1 "最大销售限额计算失败（数据为空）"
    MAX_LIMIT=0
fi

# ============================================
# 第三部分: 销售限额检查测试
# ============================================
echo -e "\n${YELLOW}=== 第三部分: 销售限额检查测试 ===${NC}"

# TC-003: 计算当前待处理销售额
echo -e "\n${BLUE}TC-003: 计算当前待处理销售额${NC}"
PENDING_SALES_RAW=$(db_query "SELECT COALESCE(SUM(total_amount), 0) as pending FROM mall_order WHERE vendor_id = $VENDOR_ID AND status IN (0, 1, 6)")
PENDING_SALES=$(echo "$PENDING_SALES_RAW" | awk '{print $1}' | head -n 1)
PENDING_SALES=${PENDING_SALES:-0}
log_test 0 "当前待处理销售额: \$$PENDING_SALES"
echo "  当前待处理订单总额: \$$PENDING_SALES"

# TC-004: 验证销售限额检查逻辑
echo -e "\n${BLUE}TC-004: 验证销售限额检查逻辑${NC}"
if [ "$MAX_LIMIT" = "0" ] || [ -z "$MAX_LIMIT" ]; then
    log_test 1 "销售限额检查逻辑失败 - 最大限额为 0（可能 Bond 未设置）"
    echo -e "  ${YELLOW}提示: 请确保 Vendor 的 bond 字段已正确设置${NC}"
    echo "  最大销售限额: \$$MAX_LIMIT"
    echo "  当前待处理销售额: \$$PENDING_SALES"
else
    AVAILABLE_LIMIT=$(safe_calc "$MAX_LIMIT - $PENDING_SALES")
    if safe_compare "$AVAILABLE_LIMIT" ">" "0"; then
        log_test 0 "销售限额检查逻辑正确"
        echo "  可用销售限额: \$$AVAILABLE_LIMIT"
    else
        log_test 1 "销售限额已用完或计算错误"
        echo "  可用销售限额: \$$AVAILABLE_LIMIT"
        echo "  最大销售限额: \$$MAX_LIMIT"
        echo "  当前待处理销售额: \$$PENDING_SALES"
    fi
fi

# ============================================
# 第四部分: 等级升级历史测试
# ============================================
echo -e "\n${YELLOW}=== 第四部分: 等级升级历史测试 ===${NC}"

# TC-005: 查询等级升级历史
echo -e "\n${BLUE}TC-005: 查询等级升级历史${NC}"
LEVEL_HISTORY_RAW=$(db_query "SELECT COUNT(*) as count FROM mall_vendor_level_history WHERE vendor_id = $VENDOR_ID")
LEVEL_HISTORY=$(echo "$LEVEL_HISTORY_RAW" | awk '{print $1}' | head -n 1)
LEVEL_HISTORY=${LEVEL_HISTORY:-0}
# 验证是否为数字
if [ "$LEVEL_HISTORY" -ge 0 ] 2>/dev/null; then
    log_test 0 "查询等级升级历史成功"
    echo "  升级历史记录数: $LEVEL_HISTORY"
    
    if [ "$LEVEL_HISTORY" -gt 0 ]; then
        echo -e "\n  最近一次升级记录:"
        db_query "SELECT old_level, new_level, old_points, new_points, trigger_order_id, trigger_amount, create_time FROM mall_vendor_level_history WHERE vendor_id = $VENDOR_ID ORDER BY create_time DESC LIMIT 1"
    else
        echo "  （暂无升级历史记录）"
    fi
else
    log_test 1 "查询等级升级历史失败（无效的数值）"
fi

# ============================================
# 测试总结
# ============================================
echo -e "\n${BLUE}========================================${NC}"
echo -e "${BLUE}测试总结${NC}"
echo -e "${BLUE}========================================${NC}"
echo "总测试数: $TOTAL"
echo -e "通过: ${GREEN}$PASSED${NC}"
echo -e "失败: ${RED}$FAILED${NC}"
echo ""

if [ $FAILED -gt 0 ]; then
    echo -e "${RED}有测试失败，请检查上述输出${NC}"
    exit 1
else
    echo -e "${GREEN}所有测试通过！${NC}"
    exit 0
fi


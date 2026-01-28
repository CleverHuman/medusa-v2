#!/bin/bash

# ============================================
# Vendor 提现系统测试脚本
# ============================================
# 测试内容：
# 1. Vendor 余额查询
# 2. 提现地址管理
# 3. 提现地址更新（PGP验证流程）
# 4. 创建提现请求
# 5. 订单发货后余额更新
# ============================================

# API配置
API_BASE="${API_BASE:-http://localhost:8080/api}"
VENDOR_USERNAME="${VENDOR_USERNAME:-testvendor}"
VENDOR_PASSWORD="${VENDOR_PASSWORD:-test123}"
VENDOR_ID=100
COOKIE_FILE="${COOKIE_FILE:-/tmp/vendor_withdrawal_session.cookies}"

# 数据库配置
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
NC='\033[0m' # No Color

# 计数器
TOTAL_TESTS=0
PASSED_TESTS=0
FAILED_TESTS=0

echo -e "${BLUE}========================================"
echo "Vendor 提现系统测试"
echo -e "========================================${NC}"
echo "API Base: $API_BASE"
echo "Vendor: $VENDOR_USERNAME"
echo

# 清理旧的 cookie 文件
rm -f "$COOKIE_FILE"

# 辅助函数：执行API请求
api_call() {
    local method=$1
    local endpoint=$2
    local data=$3
    local description=$4
    
    if [ -z "$data" ]; then
        curl -s -X "$method" \
            -b "$COOKIE_FILE" -c "$COOKIE_FILE" \
            -H "Content-Type: application/x-www-form-urlencoded" \
            -H "Authorization: Bearer $VENDOR_TOKEN" \
            "$API_BASE$endpoint"
    else
        curl -s -X "$method" \
            -b "$COOKIE_FILE" -c "$COOKIE_FILE" \
            -H "Content-Type: application/x-www-form-urlencoded" \
            -H "Authorization: Bearer $VENDOR_TOKEN" \
            -d "$data" \
            "$API_BASE$endpoint"
    fi
}

# 辅助函数：数据库查询
db_query() {
    local query=$1
    local result
    if [ -z "$DB_PASS" ] || [ "$DB_PASS" = "" ]; then
        result=$(mysql -h "$DB_HOST" -P "$DB_PORT" -u "$DB_USER" "$DB_NAME" -N -e "$query" 2>&1)
    else
        result=$(mysql -h "$DB_HOST" -P "$DB_PORT" -u "$DB_USER" -p"$DB_PASS" "$DB_NAME" -N -e "$query" 2>&1)
    fi
    # 过滤掉 MySQL 警告信息
    result=$(echo "$result" | grep -v "Using a password on the command line interface can be insecure")
    echo "$result"
}

# ===========================================
# 第一部分：Vendor 登录
# ===========================================

echo -e "${YELLOW}=== 第一部分: Vendor 登录 ===${NC}"
echo

TOTAL_TESTS=$((TOTAL_TESTS + 1))
echo -e "${BLUE}TC-LOGIN: Vendor 登录${NC}"
LOGIN_RESPONSE=$(curl -s -X POST \
    -c "$COOKIE_FILE" \
    -H "Content-Type: application/x-www-form-urlencoded" \
    -d "username=$VENDOR_USERNAME&password=$VENDOR_PASSWORD" \
    "$API_BASE/mall/vendor/member/login")

LOGIN_CODE=$(echo "$LOGIN_RESPONSE" | jq -r '.code // empty')
if [ "$LOGIN_CODE" = "200" ]; then
    VENDOR_TOKEN=$(echo "$LOGIN_RESPONSE" | jq -r '.data.token // empty')
    echo -e "${GREEN}✓${NC} Vendor 登录成功"
    echo "  Token: ${VENDOR_TOKEN:0:30}..."
    PASSED_TESTS=$((PASSED_TESTS + 1))
else
    echo -e "${RED}✗${NC} Vendor 登录失败"
    echo "Response: $LOGIN_RESPONSE"
    FAILED_TESTS=$((FAILED_TESTS + 1))
    exit 1
fi

echo

# ===========================================
# 第二部分：余额查询
# ===========================================

echo -e "${YELLOW}=== 第二部分: 余额查询 ===${NC}"
echo

TOTAL_TESTS=$((TOTAL_TESTS + 1))
echo -e "${BLUE}TC-001: 查询 Vendor 余额${NC}"
BALANCE_RESPONSE=$(api_call GET "/mall/vendor/withdrawal/balance" "")

BALANCE_CODE=$(echo "$BALANCE_RESPONSE" | jq -r '.code // empty')
if [ "$BALANCE_CODE" = "200" ]; then
    WITHDRAWABLE=$(echo "$BALANCE_RESPONSE" | jq -r '.data.withdrawableBalance // 0')
    PENDING=$(echo "$BALANCE_RESPONSE" | jq -r '.data.pendingBalance // 0')
    TOTAL_WITHDRAWN=$(echo "$BALANCE_RESPONSE" | jq -r '.data.totalWithdrawn // 0')
    echo -e "${GREEN}✓${NC} 查询余额成功"
    echo "  可提现余额: \$$WITHDRAWABLE"
    echo "  待确认余额: \$$PENDING"
    echo "  累计已提现: \$$TOTAL_WITHDRAWN"
    PASSED_TESTS=$((PASSED_TESTS + 1))
else
    echo -e "${RED}✗${NC} 查询余额失败"
    echo "Response: $BALANCE_RESPONSE"
    FAILED_TESTS=$((FAILED_TESTS + 1))
fi

echo

# ===========================================
# 第三部分：提现地址管理
# ===========================================

echo -e "${YELLOW}=== 第三部分: 提现地址管理 ===${NC}"
echo

TOTAL_TESTS=$((TOTAL_TESTS + 1))
echo -e "${BLUE}TC-002: 查询所有提现地址${NC}"
ADDRESSES_RESPONSE=$(api_call GET "/mall/vendor/withdrawal/addresses" "")

ADDR_CODE=$(echo "$ADDRESSES_RESPONSE" | jq -r '.code // empty')
if [ "$ADDR_CODE" = "200" ]; then
    ADDR_COUNT=$(echo "$ADDRESSES_RESPONSE" | jq -r '.rows | length')
    echo -e "${GREEN}✓${NC} 查询提现地址成功 ($ADDR_COUNT 个地址)"
    echo "$ADDRESSES_RESPONSE" | jq -r '.rows[] | "  - \(.currency): \(.address) (激活: \(.isActive))"'
    PASSED_TESTS=$((PASSED_TESTS + 1))
else
    echo -e "${RED}✗${NC} 查询提现地址失败"
    echo "Response: $ADDRESSES_RESPONSE"
    FAILED_TESTS=$((FAILED_TESTS + 1))
fi

echo

TOTAL_TESTS=$((TOTAL_TESTS + 1))
echo -e "${BLUE}TC-003: 请求更新 BTC 地址（PGP验证流程）${NC}"
NEW_BTC_ADDRESS="bc1qnew_test_address_123456789"
UPDATE_REQUEST_RESPONSE=$(api_call POST "/mall/vendor/withdrawal/address/update" "currency=BTC&newAddress=$NEW_BTC_ADDRESS")

UPDATE_REQ_CODE=$(echo "$UPDATE_REQUEST_RESPONSE" | jq -r '.code // empty')
if [ "$UPDATE_REQ_CODE" = "200" ]; then
    VERIFICATION_CODE=$(echo "$UPDATE_REQUEST_RESPONSE" | jq -r '.data.verificationCode // empty')
    echo -e "${GREEN}✓${NC} 地址更新请求成功"
    echo "  验证码: $VERIFICATION_CODE"
    echo "  新地址: $NEW_BTC_ADDRESS"
    PASSED_TESTS=$((PASSED_TESTS + 1))
    
    # TC-004: 验证地址更新
    TOTAL_TESTS=$((TOTAL_TESTS + 1))
    echo
    echo -e "${BLUE}TC-004: 使用验证码验证并更新地址${NC}"
    VERIFY_RESPONSE=$(api_call POST "/mall/vendor/withdrawal/address/verify" "currency=BTC&verificationCode=$VERIFICATION_CODE")
    
    VERIFY_CODE=$(echo "$VERIFY_RESPONSE" | jq -r '.code // empty')
    if [ "$VERIFY_CODE" = "200" ]; then
        echo -e "${GREEN}✓${NC} 地址验证和更新成功"
        PASSED_TESTS=$((PASSED_TESTS + 1))
    else
        echo -e "${RED}✗${NC} 地址验证失败"
        echo "Response: $VERIFY_RESPONSE"
        FAILED_TESTS=$((FAILED_TESTS + 1))
    fi
else
    echo -e "${RED}✗${NC} 地址更新请求失败"
    echo "Response: $UPDATE_REQUEST_RESPONSE"
    FAILED_TESTS=$((FAILED_TESTS + 1))
fi

echo

# ===========================================
# 第四部分：订单发货测试（余额更新）
# ===========================================

echo -e "${YELLOW}=== 第四部分: 订单发货测试 ===${NC}"
echo

# 先查找一个状态为1（已支付）的测试订单
TEST_ORDER=$(db_query "SELECT id FROM mall_order WHERE vendor_id = $VENDOR_ID AND status = 1 AND is_balance_released = 0 LIMIT 1" | head -n 1)

if [ -n "$TEST_ORDER" ] && [ "$TEST_ORDER" != "" ]; then
    TOTAL_TESTS=$((TOTAL_TESTS + 1))
    echo -e "${BLUE}TC-005: 发货订单 $TEST_ORDER${NC}"
    
    # 调用发货API
    SHIP_RESPONSE=$(api_call POST "/mall/vendor/order/$TEST_ORDER/ship" "trackingNumber=TRACK$(date +%s)")
    
    SHIP_CODE=$(echo "$SHIP_RESPONSE" | jq -r '.code // empty')
    if [ "$SHIP_CODE" = "200" ]; then
        echo -e "${GREEN}✓${NC} 订单发货成功"
        
        # 等待1秒让数据库更新
        sleep 1
        
        # 查询余额变化
        BALANCE_AFTER=$(api_call GET "/mall/vendor/withdrawal/balance" "")
        PENDING_AFTER=$(echo "$BALANCE_AFTER" | jq -r '.data.pendingBalance // 0')
        echo "  发货后待确认余额: \$$PENDING_AFTER"
        
        # 查询余额日志
        ORDER_AMOUNT=$(db_query "SELECT total_amount FROM mall_order WHERE id = '$TEST_ORDER'" | awk '{print $1}' | head -n 1)
        echo "  订单金额: \$$ORDER_AMOUNT"
        
        # 检查 balance_available_date 是否已设置
        AVAILABLE_DATE=$(db_query "SELECT balance_available_date FROM mall_order WHERE id = '$TEST_ORDER'" | head -n 1)
        if [ -n "$AVAILABLE_DATE" ] && [ "$AVAILABLE_DATE" != "NULL" ]; then
            echo "  资金可用日期: $AVAILABLE_DATE"
        fi
        
        PASSED_TESTS=$((PASSED_TESTS + 1))
    else
        echo -e "${RED}✗${NC} 订单发货失败"
        echo "Response: $SHIP_RESPONSE"
        FAILED_TESTS=$((FAILED_TESTS + 1))
    fi
else
    echo -e "${YELLOW}⚠${NC} 没有找到可发货的测试订单，跳过此测试"
fi

echo

# ===========================================
# 第五部分：提现请求
# ===========================================

echo -e "${YELLOW}=== 第五部分: 提现请求 ===${NC}"
echo

# 首先确保有可提现余额（手动设置）
echo -e "${BLUE}设置测试可提现余额...${NC}"
db_query "UPDATE mall_vendor SET withdrawable_balance = 100.00 WHERE id = $VENDOR_ID" > /dev/null

TOTAL_TESTS=$((TOTAL_TESTS + 1))
echo -e "${BLUE}TC-006: 创建提现请求（BTC, \$50.00）${NC}"
CREATE_WITHDRAW_RESPONSE=$(api_call POST "/mall/vendor/withdrawal/request" "currency=BTC&amount=50.00")

CREATE_WITHDRAW_CODE=$(echo "$CREATE_WITHDRAW_RESPONSE" | jq -r '.code // empty')
if [ "$CREATE_WITHDRAW_CODE" = "200" ]; then
    REQUEST_CODE=$(echo "$CREATE_WITHDRAW_RESPONSE" | jq -r '.data.requestCode // empty')
    REQUEST_STATUS=$(echo "$CREATE_WITHDRAW_RESPONSE" | jq -r '.data.requestStatus // empty')
    echo -e "${GREEN}✓${NC} 提现请求创建成功"
    echo "  请求编号: $REQUEST_CODE"
    echo "  状态: $REQUEST_STATUS"
    PASSED_TESTS=$((PASSED_TESTS + 1))
else
    echo -e "${RED}✗${NC} 提现请求创建失败"
    echo "Response: $CREATE_WITHDRAW_RESPONSE"
    FAILED_TESTS=$((FAILED_TESTS + 1))
fi

echo

TOTAL_TESTS=$((TOTAL_TESTS + 1))
echo -e "${BLUE}TC-007: 查询我的提现请求列表${NC}"
REQUESTS_RESPONSE=$(api_call GET "/mall/vendor/withdrawal/requests" "")

REQUESTS_CODE=$(echo "$REQUESTS_RESPONSE" | jq -r '.code // empty')
if [ "$REQUESTS_CODE" = "200" ]; then
    REQUESTS_COUNT=$(echo "$REQUESTS_RESPONSE" | jq -r '.rows | length')
    echo -e "${GREEN}✓${NC} 查询提现请求列表成功 ($REQUESTS_COUNT 个请求)"
    echo "$REQUESTS_RESPONSE" | jq -r '.rows[] | "  - \(.requestCode): \(.currency) \(.amount) (\(.requestStatus))"'
    PASSED_TESTS=$((PASSED_TESTS + 1))
else
    echo -e "${RED}✗${NC} 查询提现请求列表失败"
    echo "Response: $REQUESTS_RESPONSE"
    FAILED_TESTS=$((FAILED_TESTS + 1))
fi

echo

# ===========================================
# 测试总结
# ===========================================

echo -e "${BLUE}========================================"
echo "测试总结"
echo -e "========================================${NC}"
echo "总测试数: $TOTAL_TESTS"
echo -e "通过: ${GREEN}$PASSED_TESTS${NC}"
echo -e "失败: ${RED}$FAILED_TESTS${NC}"
echo

if [ $FAILED_TESTS -eq 0 ]; then
    echo -e "${GREEN}所有测试通过！${NC}"
    exit 0
else
    echo -e "${RED}有测试失败，请检查上述输出${NC}"
    exit 1
fi


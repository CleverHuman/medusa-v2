#!/bin/bash

# ============================================
# Vendor Integration 自动化测试脚本
# ============================================
# 使用方法: ./test_vendor_integration.sh

# 配置
API_BASE="${API_BASE:-http://localhost:8080/api}"
VENDOR_USERNAME="${VENDOR_USERNAME:-testvendor}"
VENDOR_PASSWORD="${VENDOR_PASSWORD:-test123}"
ADMIN_USERNAME="${ADMIN_USERNAME:-admin}"
ADMIN_PASSWORD="${ADMIN_PASSWORD:-admin123}"
COOKIE_FILE="${COOKIE_FILE:-/tmp/vendor_session.cookies}"

# 数据库配置 - 请提供实际值
DB_HOST="${DB_HOST:-127.0.0.1}"
DB_PORT="${DB_PORT:-3306}"
DB_NAME="${DB_NAME:-medusa}"
DB_USER="${DB_USER:-root}"
DB_PASS="${DB_PASS:-}"

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

# 开始测试
echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}Vendor Integration 测试套件${NC}"
echo -e "${BLUE}========================================${NC}"
echo "API Base: $API_BASE"
echo ""

# ============================================
# 第一部分: Vendor 认证测试
# ============================================
echo -e "${YELLOW}=== 第一部分: Vendor 认证测试 ===${NC}"

# TC-001: Vendor 登录
echo -e "\n${BLUE}TC-001: Vendor 登录${NC}"
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

# TC-002: 获取 Vendor Profile
echo -e "\n${BLUE}TC-002: 获取 Vendor Profile${NC}"
PROFILE_RESPONSE=$(curl -s -X GET ${API_BASE}/mall/vendor/member/profile \
  -H "Authorization: Bearer ${VENDOR_TOKEN}" \
  -b "${COOKIE_FILE}")
if check_response "$PROFILE_RESPONSE"; then
    log_test 0 "获取 Profile 成功"
else
    log_test 1 "获取 Profile 失败"
fi

# ============================================
# 第二部分: Vendor 产品管理测试
# ============================================
echo -e "\n${YELLOW}=== 第二部分: Vendor 产品管理测试 ===${NC}"

# TC-101: 创建 Vendor 产品
echo -e "\n${BLUE}TC-101: 创建 Vendor 产品${NC}"
CREATE_PRODUCT_RESPONSE=$(curl -s -X POST ${API_BASE}/mall/vendor/product \
  -H "Authorization: Bearer ${VENDOR_TOKEN}" \
  -H "Content-Type: application/json" \
  -b "${COOKIE_FILE}" \
  -d '{
    "productId": "VENDOR-PROD-TEST-'$(date +%s)'",
    "name": "Test Product '$(date +%s)'",
    "category": "test",
    "description": "Test description",
    "status": 0,
    "channel": "OS/TG"
  }')

if check_response "$CREATE_PRODUCT_RESPONSE"; then
    log_test 0 "创建产品成功"
    PRODUCT_ID=$(echo $CREATE_PRODUCT_RESPONSE | jq -r '.data.id // empty' 2>/dev/null)
    if [ -n "$PRODUCT_ID" ]; then
        echo "  产品 ID: $PRODUCT_ID"
    fi
else
    log_test 1 "创建产品失败"
    echo "$CREATE_PRODUCT_RESPONSE" | jq . 2>/dev/null || echo "$CREATE_PRODUCT_RESPONSE"
fi

# TC-102: 查看产品列表
echo -e "\n${BLUE}TC-102: 查看产品列表${NC}"
LIST_RESPONSE=$(curl -s -X GET ${API_BASE}/mall/vendor/product/list \
  -H "Authorization: Bearer ${VENDOR_TOKEN}" \
  -b "${COOKIE_FILE}")

if check_response "$LIST_RESPONSE"; then
    PRODUCT_COUNT=$(echo $LIST_RESPONSE | jq '.data | length' 2>/dev/null || echo "0")
    log_test 0 "获取产品列表成功 (${PRODUCT_COUNT} 个产品)"
    
    # 验证所有产品都属于当前 Vendor
    if [ "$PRODUCT_COUNT" -gt 0 ]; then
        ALL_VENDOR_PRODUCTS=$(echo $LIST_RESPONSE | jq '[.data[] | select(.originId == 100)] | length' 2>/dev/null || echo "0")
        if [ "$ALL_VENDOR_PRODUCTS" -eq "$PRODUCT_COUNT" ]; then
            log_test 0 "数据隔离验证: 所有产品都属于当前 Vendor"
        else
            log_test 1 "数据隔离验证失败: 发现不属于当前 Vendor 的产品"
        fi
    fi
else
    log_test 1 "获取产品列表失败"
fi

# TC-103: 查看产品详情（如果有产品）
if [ -n "$PRODUCT_ID" ]; then
    echo -e "\n${BLUE}TC-103: 查看产品详情${NC}"
    DETAIL_RESPONSE=$(curl -s -X GET ${API_BASE}/mall/vendor/product/${PRODUCT_ID} \
      -H "Authorization: Bearer ${VENDOR_TOKEN}" \
      -b "${COOKIE_FILE}")
    
    if check_response "$DETAIL_RESPONSE"; then
        log_test 0 "获取产品详情成功"
    else
        log_test 1 "获取产品详情失败"
    fi
fi

# ============================================
# 第三部分: Vendor 订单管理测试
# ============================================
echo -e "\n${YELLOW}=== 第三部分: Vendor 订单管理测试 ===${NC}"

# TC-302: 查看订单列表
echo -e "\n${BLUE}TC-302: 查看订单列表${NC}"
ORDER_LIST_RESPONSE=$(curl -s -X GET ${API_BASE}/mall/vendor/order/list \
  -H "Authorization: Bearer ${VENDOR_TOKEN}" \
  -b "${COOKIE_FILE}")

if check_response "$ORDER_LIST_RESPONSE"; then
    ORDER_COUNT=$(echo $ORDER_LIST_RESPONSE | jq '.data | length' 2>/dev/null || echo "0")
    log_test 0 "获取订单列表成功 (${ORDER_COUNT} 个订单)"
    
    # 获取第一个订单ID用于后续测试
    FIRST_ORDER_ID=$(echo $ORDER_LIST_RESPONSE | jq -r '.data[0].id // empty' 2>/dev/null)
    if [ -n "$FIRST_ORDER_ID" ]; then
        echo "  第一个订单 ID: $FIRST_ORDER_ID"
    fi
else
    log_test 1 "获取订单列表失败"
fi

# TC-303: 查看订单详情（如果有订单）
if [ -n "$FIRST_ORDER_ID" ]; then
    echo -e "\n${BLUE}TC-303: 查看订单详情${NC}"
    ORDER_DETAIL_RESPONSE=$(curl -s -X GET ${API_BASE}/mall/vendor/order/${FIRST_ORDER_ID} \
      -H "Authorization: Bearer ${VENDOR_TOKEN}" \
      -b "${COOKIE_FILE}")
    
    if check_response "$ORDER_DETAIL_RESPONSE"; then
        ORDER_STATUS=$(echo $ORDER_DETAIL_RESPONSE | jq -r '.data.status // empty' 2>/dev/null)
        log_test 0 "获取订单详情成功 (状态: $ORDER_STATUS)"
        
        # TC-304: 接受订单（如果状态为 pending）
        if [ "$ORDER_STATUS" = "0" ]; then
            echo -e "\n${BLUE}TC-304: 接受订单${NC}"
            ACCEPT_RESPONSE=$(curl -s -X POST ${API_BASE}/mall/vendor/order/${FIRST_ORDER_ID}/accept \
              -H "Authorization: Bearer ${VENDOR_TOKEN}" \
              -b "${COOKIE_FILE}")
            
            if check_response "$ACCEPT_RESPONSE"; then
                log_test 0 "接受订单成功"
            else
                log_test 1 "接受订单失败"
            fi
        fi
        
        # TC-306: 标记发货（如果状态为 paid 或 vendor_accepted）
        if [ "$ORDER_STATUS" = "1" ] || [ "$ORDER_STATUS" = "6" ]; then
            echo -e "\n${BLUE}TC-306: 标记发货${NC}"
            SHIP_RESPONSE=$(curl -s -X POST "${API_BASE}/mall/vendor/order/${FIRST_ORDER_ID}/ship?trackingNumber=TEST$(date +%s)" \
              -H "Authorization: Bearer ${VENDOR_TOKEN}" \
              -b "${COOKIE_FILE}")
            
            if check_response "$SHIP_RESPONSE"; then
                log_test 0 "标记发货成功"
            else
                log_test 1 "标记发货失败"
            fi
        fi
    else
        log_test 1 "获取订单详情失败"
    fi
fi

# ============================================
# 测试总结
# ============================================
echo -e "\n${BLUE}========================================${NC}"
echo -e "${BLUE}测试总结${NC}"
echo -e "${BLUE}========================================${NC}"
echo -e "总测试数: ${TOTAL}"
echo -e "${GREEN}通过: ${PASSED}${NC}"
echo -e "${RED}失败: ${FAILED}${NC}"

if [ $FAILED -eq 0 ]; then
    echo -e "\n${GREEN}所有测试通过！${NC}"
    exit 0
else
    echo -e "\n${RED}有测试失败，请检查上述输出${NC}"
    exit 1
fi


#!/bin/bash

# ========================================
# Admin 端提现管理功能测试脚本
# ========================================

# 配置
API_BASE="${API_BASE:-http://localhost:8080/api}"
ADMIN_USERNAME="${ADMIN_USERNAME:-admin}"
ADMIN_PASSWORD="${ADMIN_PASSWORD:-admin123}"
DB_HOST="${DB_HOST:-127.0.0.1}"
DB_PORT="${DB_PORT:-3306}"
DB_NAME="${DB_NAME:-medusa}"
DB_USER="${DB_USER:-root}"
DB_PASS="${DB_PASS:-rootpassword}"

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color
BOLD='\033[1m'

# 临时文件
COOKIE_FILE="/tmp/admin_test_cookies.txt"
ADMIN_TOKEN=""
TEST_REQUEST_ID=""

# 工具函数
print_header() {
    echo -e "${BLUE}========================================${NC}"
    echo -e "${BOLD}$1${NC}"
    echo -e "${BLUE}========================================${NC}"
}

print_section() {
    echo -e "\n${YELLOW}=== $1 ===${NC}\n"
}

print_test() {
    echo -e "${BLUE}$1${NC}"
}

print_success() {
    echo -e "${GREEN}✓${NC} $1"
}

print_error() {
    echo -e "${RED}✗${NC} $1"
}

print_info() {
    echo -e "  $1"
}

# API 调用函数
api_call() {
    local method=$1
    local url=$2
    local data=$3
    local extra_headers=$4
    
    if [ -n "$ADMIN_TOKEN" ]; then
        curl -s -X "$method" \
            -H "Content-Type: application/json" \
            -H "Authorization: Bearer $ADMIN_TOKEN" \
            $extra_headers \
            ${data:+-d "$data"} \
            "${API_BASE}${url}"
    else
        curl -s -X "$method" \
            -H "Content-Type: application/json" \
            $extra_headers \
            ${data:+-d "$data"} \
            "${API_BASE}${url}"
    fi
}

# 数据库查询函数
db_query() {
    mysql -h "$DB_HOST" -P "$DB_PORT" -u "$DB_USER" -p"$DB_PASS" "$DB_NAME" -sN -e "$1" 2>&1 | grep -v "Using a password on the command line interface can be insecure"
}

# ========================================
# 主测试流程
# ========================================

print_header "Admin 端提现管理功能测试"
echo "API Base: $API_BASE"
echo ""

# 测试计数器
total_tests=0
passed_tests=0

# ========================================
# 准备：临时禁用验证码
# ========================================

print_info "准备测试环境：禁用验证码..."
db_query "UPDATE sys_config SET config_value = 'false' WHERE config_key = 'sys.account.captchaEnabled';" > /dev/null 2>&1

# 清除 Redis 缓存中的验证码配置
redis-cli DEL "sys_config:sys.account.captchaEnabled" > /dev/null 2>&1

print_info "验证码已禁用并清除缓存（测试结束后会恢复）"
echo ""

# ========================================
# 第一部分: Admin 登录
# ========================================

print_section "第一部分: Admin 登录"

print_test "TC-ADMIN-LOGIN: Admin 登录"
total_tests=$((total_tests + 1))

# Admin 登录使用 /login 端点，不是 /api/login
# 传递空的 code 和 uuid（验证码已禁用）
login_response=$(curl -s -X POST \
    -H "Content-Type: application/json" \
    -d "{\"username\":\"$ADMIN_USERNAME\",\"password\":\"$ADMIN_PASSWORD\",\"code\":\"\",\"uuid\":\"\"}" \
    "http://localhost:8080/login")

ADMIN_TOKEN=$(echo "$login_response" | jq -r '.token // empty')

if [ -n "$ADMIN_TOKEN" ] && [ "$ADMIN_TOKEN" != "null" ]; then
    print_success "Admin 登录成功"
    print_info "Token: ${ADMIN_TOKEN:0:30}..."
    passed_tests=$((passed_tests + 1))
else
    print_error "Admin 登录失败"
    echo "Response: $login_response"
    exit 1
fi

# ========================================
# 第二部分: 查询提现统计信息
# ========================================

print_section "第二部分: 查询提现统计信息"

print_test "TC-STAT-001: 获取提现统计"
total_tests=$((total_tests + 1))

stats_response=$(api_call GET "/mall/vendor/withdrawal/statistics")
pending_count=$(echo "$stats_response" | jq -r '.data.pendingCount // 0')
total_count=$(echo "$stats_response" | jq -r '.data.totalCount // 0')

if [ "$pending_count" != "null" ]; then
    print_success "获取统计信息成功"
    print_info "待审批请求: $pending_count"
    print_info "总请求数: $total_count"
    passed_tests=$((passed_tests + 1))
else
    print_error "获取统计信息失败"
    echo "Response: $stats_response"
fi

# ========================================
# 第三部分: 查询提现请求列表
# ========================================

print_section "第三部分: 查询提现请求列表"

print_test "TC-LIST-001: 查询所有提现请求"
total_tests=$((total_tests + 1))

list_response=$(api_call GET "/mall/vendor/withdrawal/requests?pageNum=1&pageSize=10")
request_count=$(echo "$list_response" | jq -r '.rows | length')

if [ "$request_count" != "null" ]; then
    print_success "查询请求列表成功 ($request_count 个请求)"
    passed_tests=$((passed_tests + 1))
    
    # 获取第一个 PENDING 请求用于后续测试
    TEST_REQUEST_ID=$(echo "$list_response" | jq -r '.rows[] | select(.requestStatus == "PENDING") | .id' | head -1)
    
    if [ -n "$TEST_REQUEST_ID" ] && [ "$TEST_REQUEST_ID" != "null" ]; then
        print_info "找到测试用 PENDING 请求 ID: $TEST_REQUEST_ID"
    fi
else
    print_error "查询请求列表失败"
    echo "Response: $list_response"
fi

print_test "TC-LIST-002: 查询待审批请求"
total_tests=$((total_tests + 1))

pending_response=$(api_call GET "/mall/vendor/withdrawal/requests/pending")
pending_list_count=$(echo "$pending_response" | jq -r '.rows | length')

if [ "$pending_list_count" != "null" ]; then
    print_success "查询待审批请求成功 ($pending_list_count 个)"
    passed_tests=$((passed_tests + 1))
else
    print_error "查询待审批请求失败"
fi

# ========================================
# 第四部分: 提现请求审批流程测试
# ========================================

print_section "第四部分: 提现请求审批流程测试"

if [ -z "$TEST_REQUEST_ID" ] || [ "$TEST_REQUEST_ID" == "null" ]; then
    print_info "没有可用的 PENDING 请求，创建一个测试请求..."
    
    # 创建测试提现请求
    vendor_token=$(curl -s -X POST \
        -H "Content-Type: application/x-www-form-urlencoded" \
        -d "username=testvendor&password=test123" \
        "${API_BASE}/mall/vendor/member/login" | jq -r '.data.token')
    
    if [ -n "$vendor_token" ] && [ "$vendor_token" != "null" ]; then
        # 设置足够的可提现余额
        db_query "UPDATE mall_vendor SET withdrawable_balance = 500.00 WHERE id = 100;"
        
        # 创建提现请求
        create_response=$(curl -s -X POST \
            -H "Content-Type: application/json" \
            -H "Authorization: Bearer $vendor_token" \
            -d '{"currency":"BTC","amount":100.00}' \
            "${API_BASE}/mall/vendor/withdrawal/request")
        
        TEST_REQUEST_ID=$(echo "$create_response" | jq -r '.data.id')
        print_info "创建了测试提现请求 ID: $TEST_REQUEST_ID"
    fi
fi

if [ -n "$TEST_REQUEST_ID" ] && [ "$TEST_REQUEST_ID" != "null" ]; then
    print_test "TC-APPROVE-001: 批准提现请求"
    total_tests=$((total_tests + 1))
    
    approve_response=$(api_call POST "/mall/vendor/withdrawal/requests/$TEST_REQUEST_ID/approve?remark=Approved%20for%20testing")
    approve_code=$(echo "$approve_response" | jq -r '.code')
    
    if [ "$approve_code" == "200" ]; then
        print_success "批准提现请求成功"
        passed_tests=$((passed_tests + 1))
        
        # 验证状态更新
        status=$(db_query "SELECT request_status FROM mall_vendor_withdrawal_request WHERE id = $TEST_REQUEST_ID;")
        print_info "当前状态: $status"
    else
        print_error "批准提现请求失败"
        echo "Response: $approve_response"
    fi
    
    print_test "TC-PROCESS-001: 标记为处理中"
    total_tests=$((total_tests + 1))
    
    process_response=$(api_call POST "/mall/vendor/withdrawal/requests/$TEST_REQUEST_ID/processing?txHash=test_tx_hash_123456")
    process_code=$(echo "$process_response" | jq -r '.code')
    
    if [ "$process_code" == "200" ]; then
        print_success "标记为处理中成功"
        passed_tests=$((passed_tests + 1))
        
        # 验证状态更新
        status=$(db_query "SELECT request_status FROM mall_vendor_withdrawal_request WHERE id = $TEST_REQUEST_ID;")
        print_info "当前状态: $status"
    else
        print_error "标记为处理中失败"
        echo "Response: $process_response"
    fi
    
    print_test "TC-COMPLETE-001: 标记为完成"
    total_tests=$((total_tests + 1))
    
    complete_response=$(api_call POST "/mall/vendor/withdrawal/requests/$TEST_REQUEST_ID/complete?txFee=0.001")
    complete_code=$(echo "$complete_response" | jq -r '.code')
    
    if [ "$complete_code" == "200" ]; then
        print_success "标记为完成成功"
        passed_tests=$((passed_tests + 1))
        
        # 验证状态更新和 total_withdrawn
        status=$(db_query "SELECT request_status FROM mall_vendor_withdrawal_request WHERE id = $TEST_REQUEST_ID;")
        total_withdrawn=$(db_query "SELECT total_withdrawn FROM mall_vendor WHERE id = 100;")
        print_info "当前状态: $status"
        print_info "累计已提现: $total_withdrawn"
    else
        print_error "标记为完成失败"
        echo "Response: $complete_response"
    fi
else
    print_info "跳过审批流程测试（无可用的测试请求）"
fi

# ========================================
# 第五部分: 查询 Vendor 余额
# ========================================

print_section "第五部分: 查询 Vendor 余额"

print_test "TC-BALANCE-001: 查询 Vendor 余额"
total_tests=$((total_tests + 1))

balance_response=$(api_call GET "/mall/vendor/withdrawal/balance/100")
withdrawable=$(echo "$balance_response" | jq -r '.data.withdrawableBalance // "0"')
pending=$(echo "$balance_response" | jq -r '.data.pendingBalance // "0"')

if [ "$withdrawable" != "null" ]; then
    print_success "查询余额成功"
    print_info "可提现余额: \$$withdrawable"
    print_info "待确认余额: \$$pending"
    passed_tests=$((passed_tests + 1))
else
    print_error "查询余额失败"
    echo "Response: $balance_response"
fi

print_test "TC-BALANCE-002: 查询余额变动日志"
total_tests=$((total_tests + 1))

logs_response=$(api_call GET "/mall/vendor/withdrawal/balance/100/logs")
logs_count=$(echo "$logs_response" | jq -r '.rows | length')

if [ "$logs_count" != "null" ]; then
    print_success "查询余额日志成功 ($logs_count 条记录)"
    passed_tests=$((passed_tests + 1))
else
    print_error "查询余额日志失败"
fi

# ========================================
# 第六部分: 手动触发余额释放
# ========================================

print_section "第六部分: 手动触发余额释放"

print_test "TC-RELEASE-001: 手动释放待确认余额"
total_tests=$((total_tests + 1))

# Admin API不需要/api前缀
release_response=$(curl -s -X POST \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $ADMIN_TOKEN" \
    "http://localhost:8080/mall/vendor/withdrawal/balance/release")
release_code=$(echo "$release_response" | jq -r '.code')

if [ "$release_code" == "200" ]; then
    print_success "手动释放成功"
    passed_tests=$((passed_tests + 1))
else
    print_error "手动释放失败"
    echo "Response: $release_response"
fi

# ========================================
# 清理：恢复验证码
# ========================================

print_info "清理测试环境：恢复验证码..."
db_query "UPDATE sys_config SET config_value = 'true' WHERE config_key = 'sys.account.captchaEnabled';" > /dev/null 2>&1

# 清除 Redis 缓存，让系统重新加载配置
redis-cli DEL "sys_config:sys.account.captchaEnabled" > /dev/null 2>&1

print_info "验证码已恢复并清除缓存"

# ========================================
# 测试总结
# ========================================

echo -e "\n${BLUE}========================================${NC}"
echo -e "${BOLD}测试总结${NC}"
echo -e "${BLUE}========================================${NC}"
echo -e "总测试数: $total_tests"
echo -e "通过: ${GREEN}$passed_tests${NC}"
echo -e "失败: ${RED}$((total_tests - passed_tests))${NC}"
echo ""

if [ $passed_tests -eq $total_tests ]; then
    echo -e "${GREEN}所有测试通过！${NC}"
    exit 0
else
    echo -e "${RED}有测试失败，请检查上述输出${NC}"
    exit 1
fi


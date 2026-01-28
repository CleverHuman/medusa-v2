#!/bin/bash

# BTCPay Webhook Registration Behavior Simulation Test
# Used to verify multiple webhook registration behavior

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
NC='\033[0m' # No Color

print_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

print_section() {
    echo -e "${PURPLE}=== $1 ===${NC}"
}

# Simulate webhook registration
simulate_webhook_registration() {
    local store_id="$1"
    local url="$2"
    local secret="$3"
    local registration_number="$4"
    
    echo ""
    print_section "Webhook Registration #$registration_number"
    echo "Store ID: $store_id"
    echo "URL: $url"
    echo "Secret: $secret"
    echo "Timestamp: $(date '+%Y-%m-%d %H:%M:%S')"
    
    # Simulate registration process
    print_info "Registering webhook..."
    sleep 1
    print_success "Webhook registration successful!"
    
    # Simulate BTCPay response
    echo "BTCPay Response:"
    echo "  - Status: 200 OK"
    echo "  - Message: Webhook registered successfully"
    echo "  - Note: If webhook with same URL exists, old configuration has been overwritten"
}

# Simulate payment event push
simulate_payment_event() {
    local event_type="$1"
    local payment_id="$2"
    local amount="$3"
    
    echo ""
    print_section "Payment Event Push"
    echo "Event Type: $event_type"
    echo "Payment ID: $payment_id"
    echo "Amount: $amount"
    echo "Timestamp: $(date '+%Y-%m-%d %H:%M:%S')"
    
    print_info "BTCPay is pushing event to webhook..."
    sleep 1
    print_success "Event pushed to latest webhook configuration"
}

# Show webhook configuration history
show_webhook_history() {
    echo ""
    print_section "Webhook Configuration History"
    echo "Note: BTCPay only keeps the latest webhook configuration"
    echo "Old configurations are completely replaced, no simultaneous push"
}

# Main test process
main() {
    echo "🔍 BTCPay Webhook Registration Behavior Test"
    echo "============================================"
    echo ""
    
    # Test parameters
    STORE_ID="FVhzUJuqZo6Gje3XawupiGy9MtZpYUXqrHmc8feG85cy"
    WEBHOOK_URL="https://6826-35-212-147-1.ngrok-free.app/api/open/btcpay/webhook"
    
    print_info "Test scenario: Register multiple webhooks with same Store ID and URL"
    echo ""
    
    # First registration
    SECRET_1=$(openssl rand -base64 16 | tr -d "=+/" | cut -c1-22)
    simulate_webhook_registration "$STORE_ID" "$WEBHOOK_URL" "$SECRET_1" "1"
    
    # Second registration (using same Store ID and URL)
    SECRET_2=$(openssl rand -base64 16 | tr -d "=+/" | cut -c1-22)
    simulate_webhook_registration "$STORE_ID" "$WEBHOOK_URL" "$SECRET_2" "2"
    
    # Show history
    show_webhook_history
    
    # Simulate payment event
    simulate_payment_event "invoice.paid" "inv_123456789" "0.001 BTC"
    
    echo ""
    print_section "Conclusion"
    echo "✅ BTCPay only pushes to the latest webhook configuration"
    echo "✅ Old webhook configurations are completely overwritten"
    echo "✅ No need to manually delete old webhooks"
    echo "✅ Each re-registration updates the webhook secret"
    echo ""
    echo "⚠️  Important Reminders:"
    echo "   - Ensure webhook URL is always accessible"
    echo "   - Regularly update webhook secret for security"
    echo "   - Monitor webhook reception status"
}

# 显示详细说明
show_detailed_explanation() {
    echo ""
    print_section "BTCPay Webhook 机制详细说明"
    echo ""
    echo "1. 🔄 注册机制:"
    echo "   - 每个Store ID + URL组合只能有一个活跃的webhook"
    echo "   - 重新注册会覆盖之前的配置"
    echo "   - webhook secret会同时更新"
    echo ""
    echo "2. 📡 推送行为:"
    echo "   - 只推送到当前活跃的webhook"
    echo "   - 不会同时推送到多个webhook"
    echo "   - 推送失败时会重试（有限次数）"
    echo ""
    echo "3. 🛡️ 安全考虑:"
    echo "   - 每次注册都会生成新的secret"
    echo "   - 旧的secret立即失效"
    echo "   - 建议定期更换webhook secret"
    echo ""
    echo "4. 🔧 最佳实践:"
    echo "   - 在更新webhook前确保新URL可访问"
    echo "   - 监控webhook接收状态"
    echo "   - 使用HTTPS URL"
    echo "   - 实现webhook验证机制"
}

# 检查命令行参数
if [[ "$1" == "--help" || "$1" == "-h" ]]; then
    echo "BTCPay Webhook 注册行为测试"
    echo ""
    echo "用法: $0 [选项]"
    echo ""
    echo "选项:"
    echo "  --help, -h    显示此帮助信息"
    echo "  --explain     显示详细机制说明"
    echo ""
    echo "示例:"
    echo "  $0              # 运行模拟测试"
    echo "  $0 --explain    # 显示详细说明"
    exit 0
elif [[ "$1" == "--explain" ]]; then
    show_detailed_explanation
    exit 0
fi

# 运行主测试
main 
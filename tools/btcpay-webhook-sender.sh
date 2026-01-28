#!/bin/bash

# BTCPay Webhook Actual Sending Test Script
# Actually send webhook requests to your application

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

# Default webhook URL
DEFAULT_WEBHOOK_URL="http://localhost:8080/api/open/btcpay/webhook"

# Generate normal InvoiceSettled event
generate_normal_invoice_settled() {
    cat << 'EOF'
{
  "deliveryId": "G6TWALefkMNjkde4hJ4fB",
  "webhookId": "SyiwQxJkUfX6nvcHW23mzJ",
  "originalDeliveryId": "G6TWALefkMNjkde4hJ4fB",
  "isRedelivery": false,
  "type": "InvoiceSettled",
  "timestamp": 1753263814,
  "storeId": "FVhzUJuqZo6Gje3XawupiGy9MtZpYUXqrHmc8feG85cy",
  "invoiceId": "8Y2snkdMWRoGx7ys7NhYLa",
  "metadata": {
    "orderId": "202507231738010000"
  }
}
EOF
}

# Generate incomplete event (simulate your problem scenario)
generate_incomplete_invoice_settled() {
    cat << 'EOF'
{
  "deliveryId": "G6TWALefkMNjkde4hJ4fB",
  "webhookId": "SyiwQxJkUfX6nvcHW23mzJ",
  "originalDeliveryId": "G6TWALefkMNjkde4hJ4fB",
  "isRedelivery": false,
  "type": "InvoiceSettled",
  "timestamp": 1753263814,
  "storeId": "FVhzUJuqZo6Gje3XawupiGy9MtZpYUXqrHmc8feG85cy",
  "invoiceId": "8Y2snkdMWRoGx7ys7NhYLa",
  "metadata": {
    "orderId": "202507231738010000"
  }
}
EOF
}

# Generate event with missing fields
generate_missing_fields_event() {
    cat << 'EOF'
{
  "deliveryId": "G6TWALefkMNjkde4hJ4fB",
  "webhookId": "SyiwQxJkUfX6nvcHW23mzJ",
  "originalDeliveryId": "G6TWALefkMNjkde4hJ4fB",
  "isRedelivery": false,
  "type": "InvoiceSettled",
  "timestamp": 1753263814,
  "storeId": "FVhzUJuqZo6Gje3XawupiGy9MtZpYUXqrHmc8feG85cy",
  "invoiceId": "",
  "metadata": {
    "orderId": ""
  }
}
EOF
}

# Generate empty event
generate_empty_event() {
    cat << 'EOF'
{
  "type": "InvoiceSettled"
}
EOF
}

# Send webhook request
send_webhook() {
    local webhook_url="$1"
    local event_data="$2"
    local event_name="$3"
    
    print_section "Sending $event_name Event"
    echo "Target URL: $webhook_url"
    echo "Event Data:"
    echo "$event_data"
    echo ""
    
    # Check if curl is available
    if ! command -v curl &> /dev/null; then
        print_error "curl command not available, please install curl"
        return 1
    fi
    
    # Send POST request
    print_info "Sending webhook request..."
    
    response=$(curl -s -w "\n%{http_code}" \
        -X POST \
        -H "Content-Type: application/json" \
        -H "User-Agent: BTCPay-Webhook-Test/1.0" \
        -d "$event_data" \
        "$webhook_url")
    
    # Separate response body and status code
    http_code=$(echo "$response" | tail -n1)
    response_body=$(echo "$response" | sed '$d')
    
    echo ""
    print_info "Response Status Code: $http_code"
    print_info "Response Content:"
    echo "$response_body"
    echo ""
    
    # Judge response
    if [[ "$http_code" -eq 200 ]]; then
        print_success "✅ Webhook request sent successfully!"
    elif [[ "$http_code" -eq 0 ]]; then
        print_error "❌ Cannot connect to webhook URL, please check:"
        echo "   - Is the application running?"
        echo "   - Is the URL correct?"
        echo "   - Is the network connection normal?"
    else
        print_warning "⚠️  Webhook request returned status code: $http_code"
        echo "Please check application logs for detailed information"
    fi
}

# Show help information
show_help() {
    echo "BTCPay Webhook Actual Sending Test Script"
    echo ""
    echo "Usage: $0 [options]"
    echo ""
    echo "Options:"
    echo "  --help, -h          Show this help information"
    echo "  --url URL           Specify webhook URL (default: $DEFAULT_WEBHOOK_URL)"
    echo "  --normal            Send normal event"
    echo "  --incomplete        Send incomplete event (simulate problem scenario)"
    echo "  --missing           Send event with missing fields"
    echo "  --empty             Send empty event"
    echo "  --all               Send all types of events"
    echo ""
    echo "Examples:"
    echo "  $0 --url http://localhost:8080/api/open/btcpay/webhook --normal"
    echo "  $0 --incomplete     # Send incomplete event to default URL"
    echo "  $0 --all            # Send all types of events"
}

# 主函数
main() {
    local webhook_url="$DEFAULT_WEBHOOK_URL"
    local test_type=""
    
    # 解析命令行参数
    while [[ $# -gt 0 ]]; do
        case $1 in
            --help|-h)
                show_help
                exit 0
                ;;
            --url)
                webhook_url="$2"
                shift 2
                ;;
            --normal)
                test_type="normal"
                shift
                ;;
            --incomplete)
                test_type="incomplete"
                shift
                ;;
            --missing)
                test_type="missing"
                shift
                ;;
            --empty)
                test_type="empty"
                shift
                ;;
            --all)
                test_type="all"
                shift
                ;;
            *)
                print_error "未知选项: $1"
                show_help
                exit 1
                ;;
        esac
    done
    
    # If no test type specified, show interactive menu
    if [[ -z "$test_type" ]]; then
        echo "🔍 BTCPay Webhook Actual Sending Test"
        echo "====================================="
        echo ""
        echo "⚠️  Warning: This will send real webhook requests to your application!"
        echo ""
        echo "Please select the event type to send:"
        echo "1) Normal InvoiceSettled event"
        echo "2) Incomplete InvoiceSettled event (simulate your problem scenario)"
        echo "3) Event with missing required fields"
        echo "4) Empty event"
        echo "5) Send all types of events"
        echo "6) Exit"
        echo ""
        read -p "Please enter option (1-6): " choice
        
        case $choice in
            1) test_type="normal" ;;
            2) test_type="incomplete" ;;
            3) test_type="missing" ;;
            4) test_type="empty" ;;
            5) test_type="all" ;;
            6) echo "👋 Goodbye!"; exit 0 ;;
            *) print_error "Invalid option"; exit 1 ;;
        esac
    fi
    
    # Confirm sending
    echo ""
    print_warning "About to send webhook request to $webhook_url"
    read -p "Continue? (y/N): " -n 1 -r
    echo ""
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        echo "❌ Cancelled"
        exit 0
    fi
    
    # 根据测试类型发送请求
    case $test_type in
        normal)
            event_data=$(generate_normal_invoice_settled)
            send_webhook "$webhook_url" "$event_data" "正常InvoiceSettled"
            ;;
        incomplete)
            event_data=$(generate_incomplete_invoice_settled)
            send_webhook "$webhook_url" "$event_data" "不完整InvoiceSettled"
            ;;
        missing)
            event_data=$(generate_missing_fields_event)
            send_webhook "$webhook_url" "$event_data" "缺少字段事件"
            ;;
        empty)
            event_data=$(generate_empty_event)
            send_webhook "$webhook_url" "$event_data" "空事件"
            ;;
        all)
            print_section "Sending All Types of Events"
            
            # Send normal event
            event_data=$(generate_normal_invoice_settled)
            send_webhook "$webhook_url" "$event_data" "Normal InvoiceSettled"
            sleep 2
            
            # Send incomplete event
            event_data=$(generate_incomplete_invoice_settled)
            send_webhook "$webhook_url" "$event_data" "Incomplete InvoiceSettled"
            sleep 2
            
            # Send missing fields event
            event_data=$(generate_missing_fields_event)
            send_webhook "$webhook_url" "$event_data" "Missing Fields Event"
            sleep 2
            
            # Send empty event
            event_data=$(generate_empty_event)
            send_webhook "$webhook_url" "$event_data" "Empty Event"
            
            print_section "All Events Sent"
            ;;
        *)
            print_error "Unknown test type: $test_type"
            exit 1
            ;;
    esac
    
    echo ""
    print_info "💡 Tip: Check your application logs to see webhook processing results"
    print_info "💡 Tip: If connection issues occur, ensure the application is running"
}

# 运行主函数
main "$@" 
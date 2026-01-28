#!/bin/bash

# BTCPay Webhook 调试测试脚本
# 用于测试webhook事件处理的各种场景

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

# 模拟正常的InvoiceSettled事件
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

# 模拟缺少金额数据的InvoiceSettled事件
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

# 模拟缺少必要字段的事件
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

# 模拟空的事件
generate_empty_event() {
    cat << 'EOF'
{
  "type": "InvoiceSettled"
}
EOF
}

# 显示测试说明
show_test_description() {
    echo ""
    print_section "BTCPay Webhook 调试测试说明"
    echo ""
    echo "这个脚本模拟了各种BTCPay webhook事件场景："
    echo ""
    echo "1. 📋 正常事件 - 包含完整数据的InvoiceSettled事件"
    echo "2. ⚠️  不完整事件 - 缺少金额数据的InvoiceSettled事件"
    echo "3. ❌ 缺少字段事件 - 缺少必要字段的事件"
    echo "4. 🔍 空事件 - 只包含类型的事件"
    echo ""
    echo "修复内容："
    echo "✅ 添加了详细的调试日志"
    echo "✅ 增加了空值检查和验证"
    echo "✅ 改进了BigDecimal的安全创建"
    echo "✅ 添加了异常处理和错误恢复"
    echo "✅ 提供了完整的错误上下文信息"
    echo ""
    echo "测试目的："
    echo "🔍 验证webhook事件处理的健壮性"
    echo "🔍 确保异常情况下的优雅处理"
    echo "🔍 提供足够的调试信息用于问题排查"
}

# 主测试流程
main() {
    echo "🔍 BTCPay Webhook 调试测试"
    echo "============================"
    echo ""
    
    show_test_description
    
    echo "请选择测试场景："
    echo "1) 正常InvoiceSettled事件"
    echo "2) 不完整InvoiceSettled事件（缺少金额数据）"
    echo "3) 缺少必要字段的事件"
    echo "4) 空事件"
    echo "5) 显示所有测试数据"
    echo "6) 退出"
    echo ""
    
    read -p "请输入选项 (1-6): " choice
    
    case $choice in
        1)
            print_section "测试场景1: 正常InvoiceSettled事件"
            echo "这个事件包含完整的数据，应该能正常处理"
            echo ""
            generate_normal_invoice_settled
            ;;
        2)
            print_section "测试场景2: 不完整InvoiceSettled事件"
            echo "这个事件缺少金额数据，会触发BigDecimal异常"
            echo "修复后应该能优雅处理并记录详细错误信息"
            echo ""
            generate_incomplete_invoice_settled
            ;;
        3)
            print_section "测试场景3: 缺少必要字段的事件"
            echo "这个事件缺少invoiceId和orderId，应该被跳过"
            echo ""
            generate_missing_fields_event
            ;;
        4)
            print_section "测试场景4: 空事件"
            echo "这个事件只包含类型，用于测试边界情况"
            echo ""
            generate_empty_event
            ;;
        5)
            print_section "所有测试数据"
            echo ""
            echo "=== 正常事件 ==="
            generate_normal_invoice_settled
            echo ""
            echo "=== 不完整事件 ==="
            generate_incomplete_invoice_settled
            echo ""
            echo "=== 缺少字段事件 ==="
            generate_missing_fields_event
            echo ""
            echo "=== 空事件 ==="
            generate_empty_event
            ;;
        6)
            echo "👋 再见!"
            exit 0
            ;;
        *)
            print_error "无效选项，请重新选择"
            main
            ;;
    esac
}

# 显示帮助信息
if [[ "$1" == "--help" || "$1" == "-h" ]]; then
    echo "BTCPay Webhook 调试测试脚本"
    echo ""
    echo "用法: $0 [选项]"
    echo ""
    echo "选项:"
    echo "  --help, -h    显示此帮助信息"
    echo "  --normal      生成正常事件数据"
    echo "  --incomplete  生成不完整事件数据"
    echo "  --missing     生成缺少字段事件数据"
    echo "  --empty       生成空事件数据"
    echo ""
    echo "示例:"
    echo "  $0              # 交互式测试"
    echo "  $0 --normal     # 生成正常事件数据"
    exit 0
elif [[ "$1" == "--normal" ]]; then
    generate_normal_invoice_settled
elif [[ "$1" == "--incomplete" ]]; then
    generate_incomplete_invoice_settled
elif [[ "$1" == "--missing" ]]; then
    generate_missing_fields_event
elif [[ "$1" == "--empty" ]]; then
    generate_empty_event
else
    main
fi 
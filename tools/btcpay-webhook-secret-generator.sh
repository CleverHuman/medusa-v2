#!/bin/bash

# BTCPay Webhook Secret Generator
# Generate webhook-secret based on btcpay configuration parameters in application.yml

set -e

# Default configuration file path
CONFIG_FILE="medusa-admin/src/main/resources/application.yml"
BACKUP_FILE=""

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 打印带颜色的消息
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

# Generate random webhook secret
generate_secret() {
    local length=${1:-22}
    # Use openssl to generate random string
    openssl rand -base64 $((length * 3 / 4)) | tr -d "=+/" | cut -c1-${length}
}

# 显示当前配置
show_current_config() {
    if [[ ! -f "$CONFIG_FILE" ]]; then
        print_error "配置文件不存在: $CONFIG_FILE"
        exit 1
    fi
    
    print_info "当前活跃的BTCPay配置:"
    echo "========================"
    
    # 找到活跃的btcpay配置（未注释的）
    local active_btcpay_line=$(grep -n "^btcpay:" "$CONFIG_FILE" | head -1 | cut -d: -f1)
    
    if [[ -z "$active_btcpay_line" ]]; then
        print_error "未找到活跃的btcpay配置"
        exit 1
    fi
    
    print_info "Active configuration location: Line $active_btcpay_line"
    
    # Find next btcpay configuration or end of file to determine active configuration range
    local next_btcpay_line=$(grep -n "^btcpay:" "$CONFIG_FILE" | tail -n +2 | head -1 | cut -d: -f1)
    local end_line=$(wc -l < "$CONFIG_FILE")
    
    if [[ -n "$next_btcpay_line" ]]; then
        local range_end=$((next_btcpay_line - 1))
    else
        local range_end=$end_line
    fi
    
    # Extract btcpay configuration within active range
    local active_config=$(sed -n "${active_btcpay_line},${range_end}p" "$CONFIG_FILE")
    
    # Display URL
    url=$(echo "$active_config" | grep "url:" | head -1 | sed 's/.*url:\s*//')
    echo "URL: $url"
    
    # Display API Key
    api_key=$(echo "$active_config" | grep "key:" | head -1 | sed 's/.*key:\s*//')
    echo "API Key: $api_key"
    
    # Display Store ID
    store_id=$(echo "$active_config" | grep "id:" | head -1 | sed 's/.*id:\s*//')
    echo "Store ID: $store_id"
    
    # Display Webhook Secret
    webhook_secret=$(echo "$active_config" | grep "webhook-secret:" | head -1 | sed 's/.*webhook-secret:\s*//')
    echo "Webhook Secret: $webhook_secret"
    
    # Display Default Currency
    currency=$(echo "$active_config" | grep "currency:" | head -1 | sed 's/.*currency:\s*//')
    echo "Default Currency: $currency"
    
    # Display Notification URL
    notification_url=$(echo "$active_config" | grep "notification:" -A 5 | grep "url:" | head -1 | sed 's/.*url:\s*//')
    echo "Notification URL: $notification_url"
    
    # Display Redirect URL
    redirect_url=$(echo "$active_config" | grep "redirect:" -A 5 | grep "url:" | head -1 | sed 's/.*url:\s*//')
    echo "Redirect URL: $redirect_url"
    
    # Check for commented configurations
    local commented_btcpay_count=$(grep -c "^\s*#.*btcpay:" "$CONFIG_FILE" 2>/dev/null || echo "0")
    if [[ "$commented_btcpay_count" -gt 0 ]]; then
        echo ""
        print_warning "Found $commented_btcpay_count commented btcpay configuration(s), skipped"
    fi
}

# Update webhook secret
update_webhook_secret() {
    local new_secret="$1"
    
    if [[ -z "$new_secret" ]]; then
        new_secret=$(generate_secret)
    fi
    
    if [[ ! -f "$CONFIG_FILE" ]]; then
        print_error "Configuration file not found: $CONFIG_FILE"
        exit 1
    fi
    
    # Backup original file
    local backup_file="${CONFIG_FILE}.backup"
    cp "$CONFIG_FILE" "$backup_file"
    print_success "Backup created at: $backup_file"
    
    # Find active btcpay configuration (not commented)
    local active_btcpay_line=$(grep -n "^btcpay:" "$CONFIG_FILE" | head -1 | cut -d: -f1)
    
    if [[ -z "$active_btcpay_line" ]]; then
        print_error "No active btcpay configuration found"
        exit 1
    fi
    
    print_info "Found active btcpay configuration at line $active_btcpay_line"
    
    # Find next btcpay configuration or end of file to determine active configuration range
    local next_btcpay_line=$(grep -n "^btcpay:" "$CONFIG_FILE" | tail -n +2 | head -1 | cut -d: -f1)
    local end_line=$(wc -l < "$CONFIG_FILE")
    
    if [[ -n "$next_btcpay_line" ]]; then
        local range_end=$((next_btcpay_line - 1))
    else
        local range_end=$end_line
    fi
    
    print_info "Active configuration range: Line $active_btcpay_line to $range_end"
    
    # Update webhook-secret within active configuration range
    if [[ "$OSTYPE" == "darwin"* ]]; then
        # macOS - only replace within active configuration range
        sed -i '' "${active_btcpay_line},${range_end}s/^  webhook-secret:.*/  webhook-secret: $new_secret/" "$CONFIG_FILE"
    else
        # Linux - only replace within active configuration range
        sed -i "${active_btcpay_line},${range_end}s/^  webhook-secret:.*/  webhook-secret: $new_secret/" "$CONFIG_FILE"
    fi
    
    print_success "Updated active configuration webhook-secret to: $new_secret"
}

# Show help information
show_help() {
    echo "BTCPay Webhook Secret Generator"
    echo "================================"
    echo ""
    echo "Usage: $0 [options]"
    echo ""
    echo "Options:"
    echo "  -h, --help              Show this help information"
    echo "  -c, --config FILE       Specify configuration file path (default: $CONFIG_FILE)"
    echo "  -g, --generate          Generate new webhook-secret"
    echo "  -u, --update            Update webhook-secret in configuration file"
    echo "  -s, --secret SECRET     Specify custom webhook-secret"
    echo "  -l, --length LENGTH     Length of generated secret (default: 22)"
    echo "  --show                  Show current configuration"
    echo ""
    echo "Examples:"
    echo "  $0 --show                                    # Show current configuration"
    echo "  $0 --generate                               # Generate new secret"
    echo "  $0 --generate --update                      # Generate and update configuration file"
    echo "  $0 --secret MY_SECRET --update              # Update with custom secret"
    echo "  $0 --generate --length 30 --update          # Generate 30-character secret and update"
}

# Main function
main() {
    local generate=false
    local update=false
    local show=false
    local custom_secret=""
    local length=22
    
    # Parse command line arguments
    while [[ $# -gt 0 ]]; do
        case $1 in
            -h|--help)
                show_help
                exit 0
                ;;
            -c|--config)
                CONFIG_FILE="$2"
                shift 2
                ;;
            -g|--generate)
                generate=true
                shift
                ;;
            -u|--update)
                update=true
                shift
                ;;
            -s|--secret)
                custom_secret="$2"
                shift 2
                ;;
            -l|--length)
                length="$2"
                shift 2
                ;;
            --show)
                show=true
                shift
                ;;
            *)
                print_error "Unknown option: $1"
                show_help
                exit 1
                ;;
        esac
    done
    
    # Check configuration file
    if [[ ! -f "$CONFIG_FILE" ]]; then
        print_error "Configuration file not found: $CONFIG_FILE"
        exit 1
    fi
    
    # Show current configuration
    if [[ "$show" == true ]]; then
        show_current_config
        exit 0
    fi
    
    # Generate new webhook-secret
    if [[ "$generate" == true ]]; then
        local new_secret
        if [[ -n "$custom_secret" ]]; then
            new_secret="$custom_secret"
        else
            new_secret=$(generate_secret "$length")
        fi
        
        echo ""
        print_info "=== Generated Webhook Secret ==="
        echo "Secret: $new_secret"
        echo "Length: ${#new_secret}"
        echo ""
        
        if [[ "$update" == true ]]; then
            update_webhook_secret "$new_secret"
            echo ""
            print_info "Updated configuration:"
            show_current_config
        else
            print_warning "Tip: Use --update parameter to automatically update configuration file"
        fi
        exit 0
    fi
    
    # Update only mode
    if [[ "$update" == true ]]; then
        local new_secret
        if [[ -n "$custom_secret" ]]; then
            new_secret="$custom_secret"
        else
            new_secret=$(generate_secret "$length")
        fi
        
        update_webhook_secret "$new_secret"
        echo ""
        print_info "Updated configuration:"
        show_current_config
        exit 0
    fi
    
    # Default: show current configuration
    show_current_config
    echo ""
    print_info "Usage:"
    echo "  $0 --generate --update"
    echo "  $0 --secret YOUR_SECRET --update"
}

# 运行主函数
main "$@" 
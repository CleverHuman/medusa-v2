#!/bin/bash

# BTCPay Webhook Secret Generator Quick Start Script

echo "🚀 BTCPay Webhook Secret Generator Quick Start"
echo "============================================="
echo ""

# Check if running in correct directory
if [[ ! -f "btcpay-webhook-secret-generator.sh" ]]; then
    echo "❌ Error: Please run this script in the tools directory"
    echo "   Current directory: $(pwd)"
    echo "   Please run: cd tools && ./quick-start.sh"
    exit 1
fi

# Set configuration file path
CONFIG_FILE="../medusa-admin/src/main/resources/application.yml"

# Check if configuration file exists
if [[ ! -f "$CONFIG_FILE" ]]; then
    echo "❌ Error: Configuration file not found $CONFIG_FILE"
    echo "   Please ensure you're running from the project root directory"
    exit 1
fi

echo "✅ Configuration file found: $CONFIG_FILE"
echo ""

# Show menu
while true; do
    echo "Please select an operation:"
    echo "1) Show current BTCPay configuration"
    echo "2) Generate new webhook-secret (display only, no update)"
    echo "3) Generate new webhook-secret and update configuration file"
    echo "4) Update configuration file with custom secret"
    echo "5) Show help information"
    echo "6) Exit"
    echo ""
    read -p "Please enter option (1-6): " choice
    
            case $choice in
            1)
                echo ""
                echo "📋 Current BTCPay Configuration:"
                echo "----------------------------------------"
                ./btcpay-webhook-secret-generator.sh --config "$CONFIG_FILE" --show
                echo ""
                ;;
            2)
                echo ""
                echo "🔐 Generate New Webhook Secret:"
                echo "----------------------------------------"
                ./btcpay-webhook-secret-generator.sh --config "$CONFIG_FILE" --generate
                echo ""
                ;;
            3)
                echo ""
                echo "⚠️  Warning: This will modify the application.yml file!"
                echo "   It's recommended to backup the configuration file first"
                read -p "Continue? (y/N): " -n 1 -r
                echo ""
                if [[ $REPLY =~ ^[Yy]$ ]]; then
                    echo "🔄 Generating and updating webhook-secret..."
                    ./btcpay-webhook-secret-generator.sh --config "$CONFIG_FILE" --generate --update
                    echo ""
                    echo "✅ Update completed! New configuration:"
                    ./btcpay-webhook-secret-generator.sh --config "$CONFIG_FILE" --show
                else
                    echo "❌ Operation cancelled"
                fi
                echo ""
                ;;
            4)
                echo ""
                read -p "Please enter custom secret: " custom_secret
                if [[ -n "$custom_secret" ]]; then
                    echo "🔄 Updating configuration file with custom secret..."
                    ./btcpay-webhook-secret-generator.sh --config "$CONFIG_FILE" --secret "$custom_secret" --update
                    echo ""
                    echo "✅ Update completed! New configuration:"
                    ./btcpay-webhook-secret-generator.sh --config "$CONFIG_FILE" --show
                else
                    echo "❌ No secret entered, operation cancelled"
                fi
                echo ""
                ;;
            5)
                echo ""
                echo "📖 Help Information:"
                echo "----------------------------------------"
                ./btcpay-webhook-secret-generator.sh --help
                echo ""
                ;;
            6)
                echo ""
                echo "👋 Goodbye!"
                exit 0
                ;;
            *)
                echo ""
                echo "❌ Invalid option, please select again"
                echo ""
                ;;
        esac
done 
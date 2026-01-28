#!/bin/bash

# BTCPay Webhook Secret Generator 使用示例
# 这个脚本展示了如何使用webhook-secret生成工具

echo "=== BTCPay Webhook Secret Generator 使用示例 ==="
echo ""

# 设置配置文件路径
CONFIG_FILE="../medusa-admin/src/main/resources/application.yml"

echo "1. 显示当前配置:"
echo "----------------------------------------"
./btcpay-webhook-secret-generator.sh --config "$CONFIG_FILE" --show
echo ""

echo "2. 生成新的webhook-secret (不更新配置文件):"
echo "----------------------------------------"
./btcpay-webhook-secret-generator.sh --config "$CONFIG_FILE" --generate
echo ""

echo "3. 生成新的webhook-secret并更新配置文件:"
echo "----------------------------------------"
echo "注意: 这将修改application.yml文件，请确保已备份!"
read -p "是否继续? (y/N): " -n 1 -r
echo ""
if [[ $REPLY =~ ^[Yy]$ ]]; then
    ./btcpay-webhook-secret-generator.sh --config "$CONFIG_FILE" --generate --update
    echo ""
    echo "更新后的配置:"
    ./btcpay-webhook-secret-generator.sh --config "$CONFIG_FILE" --show
else
    echo "已取消操作"
fi

echo ""
echo "4. 使用自定义secret更新配置文件:"
echo "----------------------------------------"
echo "示例: ./btcpay-webhook-secret-generator.sh --config \"$CONFIG_FILE\" --secret \"MY_CUSTOM_SECRET_123\" --update"

echo ""
echo "5. 生成30位长度的secret:"
echo "----------------------------------------"
echo "示例: ./btcpay-webhook-secret-generator.sh --config \"$CONFIG_FILE\" --generate --length 30 --update"

echo ""
echo "=== 其他有用的命令 ==="
echo "显示帮助信息:"
echo "  ./btcpay-webhook-secret-generator.sh --help"
echo ""
echo "指定不同的配置文件:"
echo "  ./btcpay-webhook-secret-generator.sh --config /path/to/application.yml --show" 
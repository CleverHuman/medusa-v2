# BTCPay Webhook Secret Generator

This is a toolset for generating and updating BTCPay webhook-secret.

## File Description

- `btcpay-webhook-secret-generator.sh` - Shell script version (recommended)
- `btcpay-webhook-secret-generator.py` - Python script version
- `quick-start.sh` - Interactive quick start script
- `example-usage.sh` - Usage example script
- `webhook-test-simulation.sh` - BTCPay webhook registration behavior test
- `btcpay-webhook-debug-test.sh` - BTCPay webhook debug test
- `btcpay-webhook-sender.sh` - BTCPay webhook actual sending test


## Quick Start

### Interactive Menu (Recommended for beginners)

```bash
# Enter tools directory
cd tools

# Run interactive menu
./quick-start.sh
```

This will start an interactive menu where you can choose:
1. Show current BTCPay configuration
2. Generate new webhook-secret (display only, no update)
3. Generate new webhook-secret and update configuration file
4. Update configuration file with custom secret
5. Show help information
6. Exit

### Command Line Usage

```bash

# Generate new webhook-secret (no configuration file update)
./tools/btcpay-webhook-secret-generator.sh --generate

# Generate new webhook-secret and update configuration file
./tools/btcpay-webhook-secret-generator.sh --generate --update

# Update configuration file with custom secret
./tools/btcpay-webhook-secret-generator.sh --secret "MY_CUSTOM_SECRET" --update

# Generate 30-character secret and update configuration file
./tools/btcpay-webhook-secret-generator.sh --generate --length 30 --update

# Specify different configuration file path
./tools/btcpay-webhook-secret-generator.sh --config "path/to/application.yml" --show
```

### Python Script Version

Requires Python 3 and PyYAML:

```bash
# Install dependencies
pip install pyyaml

# Show current configuration
python tools/btcpay-webhook-secret-generator.py --show

# Generate new webhook-secret and update configuration file
python tools/btcpay-webhook-secret-generator.py --generate --update
```

## Features

1. **Show Current Configuration** - Read and display btcpay configuration in application.yml
2. **Generate Random Secret** - Use cryptographically secure random number generator
3. **Auto Backup** - Automatically create backup before updating configuration file
4. **Custom Secret Support** - Can specify custom webhook-secret
5. **Configurable Length** - Can specify the length of generated secret
6. **Cross-Platform Support** - Support macOS and Linux
7. **Interactive Menu** - Provide friendly interactive interface

## Configuration File Format

The tool reads btcpay configuration from `application.yml`:

```yaml
btcpay:
  url: ${BTCPAY_URL:https://btcpay697645.lndyn.com}
  api:
    key: ${BTCPAY_API_KEY:45f2e9ad0866db09f90523f2fcaa88cf0f2ca0b8}
  store:
    id: ${BTCPAY_STORE_ID:FVhzUJuqZo6Gje3XawupiGy9MtZpYUXqrHmc8feG85cy}
  webhook-secret: ${BTCPAY_WEBHOOK_SECRET:5u2CMVU1DQ5cRPvz2GjXqT}
  default:
    currency: AUD
  notification:
    url: ${BTCPAY_NOTIFICATION_URL:https://6826-35-212-147-1.ngrok-free.app/api/open/btcpay/webhook}
  redirect:
    url: ${BTCPAY_REDIRECT_URL:http://your-domain.com/payment/result}
```

## Security Notes

1. Generated secrets use cryptographically secure random number generator
2. Automatic backup is created before updating configuration file
3. It's recommended to regularly change webhook-secret for security

## Important Notes

1. Ensure to backup important configuration files before running scripts
2. After updating webhook-secret, you need to synchronize the update on BTCPay server side
3. It's recommended to use environment variables instead of hardcoded secrets in production environment

## Troubleshooting

### Common Issues

1. **Configuration file not found**
   ```
   [ERROR] Configuration file not found: medusa-admin/src/main/resources/application.yml
   ```
   Solution: Ensure running script from project root directory, or use `--config` parameter to specify correct configuration file path.

2. **Permission denied**
   ```
   Permission denied
   ```
   Solution: Ensure script has execution permission: `chmod +x tools/btcpay-webhook-secret-generator.sh`

3. **找不到btcpay配置**
   ```
   [ERROR] 未找到btcpay配置
   ```
   解决：检查application.yml文件中是否包含btcpay配置部分。

### BTCPay Webhook 调试

如果遇到webhook处理异常，可以使用以下工具进行调试：

1. **webhook注册行为测试**
   ```bash
   ./tools/webhook-test-simulation.sh
   ```
   模拟BTCPay webhook注册过程，验证多次注册的行为。

2. **webhook事件调试测试**
   ```bash
   ./tools/btcpay-webhook-debug-test.sh
   ```
   生成各种测试场景的webhook事件数据，用于验证事件处理的健壮性。

3. **webhook实际发送测试**
   ```bash
   ./tools/btcpay-webhook-sender.sh --incomplete
   ```
   真正发送webhook请求到你的应用，用于测试修复后的异常处理。

3. **常见webhook异常**
   - `NumberFormatException`: 金额字段为空或格式错误
   - `NullPointerException`: 必要字段缺失
   - `ArrayIndexOutOfBoundsException`: JSON解析错误

   修复后的代码包含详细的调试日志和安全的空值处理。

## 开发说明

如果需要修改或扩展工具功能，可以编辑以下文件：

- `btcpay-webhook-secret-generator.sh` - 主要的Shell脚本
- `btcpay-webhook-secret-generator.py` - Python版本
- `quick-start.sh` - 交互式菜单脚本

所有脚本都包含详细的注释，便于理解和修改。 

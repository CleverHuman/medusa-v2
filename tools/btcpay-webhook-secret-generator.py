#!/usr/bin/env python3
"""
BTCPay Webhook Secret Generator
根据application.yml中的btcpay配置参数生成webhook-secret
"""

import yaml
import secrets
import string
import argparse
import os
import sys
from pathlib import Path


def load_application_yml(config_path):
    """加载application.yml配置文件"""
    try:
        with open(config_path, 'r', encoding='utf-8') as file:
            config = yaml.safe_load(file)
        return config
    except FileNotFoundError:
        print(f"错误: 找不到配置文件 {config_path}")
        return None
    except yaml.YAMLError as e:
        print(f"错误: 解析YAML文件失败: {e}")
        return None


def generate_webhook_secret(length=22):
    """生成webhook secret"""
    # 使用字母和数字生成随机字符串
    characters = string.ascii_letters + string.digits
    return ''.join(secrets.choice(characters) for _ in range(length))


def update_webhook_secret(config_path, new_secret=None):
    """更新application.yml中的webhook-secret"""
    if new_secret is None:
        new_secret = generate_webhook_secret()
    
    try:
        # 读取原始文件
        with open(config_path, 'r', encoding='utf-8') as file:
            content = file.read()
        
        # 备份原文件
        backup_path = config_path + '.backup'
        with open(backup_path, 'w', encoding='utf-8') as file:
            file.write(content)
        print(f"已备份原配置文件到: {backup_path}")
        
        # 替换webhook-secret
        import re
        # 匹配webhook-secret行
        pattern = r'(webhook-secret:\s*)(\$\{BTCPAY_WEBHOOK_SECRET:[^}]*\}|[^\s]+)'
        replacement = rf'\1{new_secret}'
        
        new_content = re.sub(pattern, replacement, content)
        
        # 写回文件
        with open(config_path, 'w', encoding='utf-8') as file:
            file.write(new_content)
        
        print(f"已更新webhook-secret为: {new_secret}")
        return True
        
    except Exception as e:
        print(f"错误: 更新配置文件失败: {e}")
        return False


def display_current_config(config):
    """显示当前btcpay配置"""
    if not config or 'btcpay' not in config:
        print("未找到btcpay配置")
        return
    
    btcpay_config = config['btcpay']
    print("\n=== 当前BTCPay配置 ===")
    print(f"URL: {btcpay_config.get('url', 'N/A')}")
    print(f"API Key: {btcpay_config.get('api', {}).get('key', 'N/A')}")
    print(f"Store ID: {btcpay_config.get('store', {}).get('id', 'N/A')}")
    print(f"Webhook Secret: {btcpay_config.get('webhook-secret', 'N/A')}")
    print(f"Default Currency: {btcpay_config.get('default', {}).get('currency', 'N/A')}")
    print(f"Notification URL: {btcpay_config.get('notification', {}).get('url', 'N/A')}")
    print(f"Redirect URL: {btcpay_config.get('redirect', {}).get('url', 'N/A')}")


def main():
    parser = argparse.ArgumentParser(description='BTCPay Webhook Secret Generator')
    parser.add_argument('--config', '-c', 
                       default='medusa-admin/src/main/resources/application.yml',
                       help='application.yml配置文件路径')
    parser.add_argument('--generate', '-g', action='store_true',
                       help='生成新的webhook-secret')
    parser.add_argument('--update', '-u', action='store_true',
                       help='更新配置文件中的webhook-secret')
    parser.add_argument('--secret', '-s',
                       help='指定自定义的webhook-secret')
    parser.add_argument('--length', '-l', type=int, default=22,
                       help='生成secret的长度 (默认: 22)')
    parser.add_argument('--show', action='store_true',
                       help='显示当前配置')
    
    args = parser.parse_args()
    
    # 检查配置文件是否存在
    config_path = Path(args.config)
    if not config_path.exists():
        print(f"错误: 配置文件不存在: {config_path}")
        sys.exit(1)
    
    # 加载配置
    config = load_application_yml(config_path)
    if config is None:
        sys.exit(1)
    
    # 显示当前配置
    if args.show:
        display_current_config(config)
        return
    
    # 生成新的webhook-secret
    if args.generate:
        if args.secret:
            new_secret = args.secret
        else:
            new_secret = generate_webhook_secret(args.length)
        
        print(f"\n=== 生成的Webhook Secret ===")
        print(f"Secret: {new_secret}")
        print(f"长度: {len(new_secret)}")
        
        # 如果指定了更新，则更新配置文件
        if args.update:
            if update_webhook_secret(config_path, new_secret):
                print("\n配置文件已更新!")
                # 重新加载并显示更新后的配置
                updated_config = load_application_yml(config_path)
                if updated_config:
                    display_current_config(updated_config)
            else:
                print("\n配置文件更新失败!")
        else:
            print("\n提示: 使用 --update 参数可以自动更新配置文件")
    
    # 仅更新模式
    elif args.update:
        if args.secret:
            new_secret = args.secret
        else:
            new_secret = generate_webhook_secret(args.length)
        
        if update_webhook_secret(config_path, new_secret):
            print(f"\n已更新webhook-secret为: {new_secret}")
            # 重新加载并显示更新后的配置
            updated_config = load_application_yml(config_path)
            if updated_config:
                display_current_config(updated_config)
        else:
            print("\n配置文件更新失败!")
    
    else:
        # 默认显示当前配置
        display_current_config(config)
        print("\n使用方法:")
        print("  python btcpay-webhook-secret-generator.py --generate --update")
        print("  python btcpay-webhook-secret-generator.py --secret YOUR_SECRET --update")


if __name__ == "__main__":
    main() 
# Vendor Integration 测试指南

## 📋 快速开始

### 1. 准备测试数据

```bash
./run_prepare_test_data.sh
```

输入 MySQL root 密码，脚本会自动创建测试数据。

### 2. 运行自动化测试

```bash
./test_vendor_integration.sh
```

## 📁 测试文件说明

- **QUICK_TEST_START.md** - 快速测试指南（推荐从这里开始）
- **TEST_CASES_VENDOR_INTEGRATION.md** - 完整测试用例文档
- **prepare_test_data.sql** - 数据库测试数据准备脚本
- **run_prepare_test_data.sh** - 数据准备脚本执行器
- **test_vendor_integration.sh** - 自动化测试脚本

## 🔧 环境配置

- **数据库**: localhost:3306/medusa (root)
- **API 地址**: http://localhost:8080/api
- **Admin**: admin / admin123
- **Vendor 测试账号**: testvendor / test123

## ✅ 测试检查清单

### 基础功能
- [ ] Vendor 登录
- [ ] 创建产品
- [ ] 查看产品列表
- [ ] 产品审批流程
- [ ] 创建订单（包含 Vendor 产品）
- [ ] 查看订单列表
- [ ] 接受/拒绝订单
- [ ] 标记发货

### 数据验证
- [ ] 产品 `product_origin` 和 `origin_id` 正确设置
- [ ] 订单 `vendor_id` 正确设置
- [ ] 订单项 `product_origin` 正确设置
- [ ] 数据隔离（Vendor 只能看到自己的数据）

## 🐛 问题排查

### 数据库连接失败
```bash
# 检查 MySQL 服务
mysql -h localhost -P 3306 -u root -p -e "SELECT 1"
```

### API 请求失败
```bash
# 检查应用是否运行
curl http://localhost:8080/api/mall/product/list
```

### 认证失败
- 检查 Vendor Application 是否已审批
- 检查 Session 是否正确保存
- 检查 token 是否有效

## 📚 详细文档

- 完整测试用例: `TEST_CASES_VENDOR_INTEGRATION.md`
- 快速测试指南: `QUICK_TEST_START.md`


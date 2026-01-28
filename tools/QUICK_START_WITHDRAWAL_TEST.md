# Vendor 提现系统 - 快速测试指南

## ✅ 已完成的工作

1. **数据库迁移** ✓
   - 创建了 `mall_vendor_withdrawal_address` (提现地址表)
   - 创建了 `mall_vendor_withdrawal_request` (提现请求表)
   - 创建了 `mall_vendor_balance_log` (余额变动记录表)
   - 为 `mall_vendor` 添加了余额字段
   - 为 `mall_order` 添加了余额释放字段

2. **测试数据准备** ✓
   - Vendor ID 100 的提现地址已初始化（BTC, XMR, USDT_TRX, USDT_ERC）
   - 创建了测试订单（可用于测试发货后余额更新）
   - 余额字段已初始化为 0.00

## 🚀 如何开始测试

### 前提条件

1. **应用必须正在运行**
   ```bash
   # 在项目根目录
   cd /Users/jc/Documents/workshop/medusa-developOS3
   
   # 启动应用（如果还未启动）
   # mvn spring-boot:run -pl medusa-admin
   # 或使用您的启动方式
   ```

2. **确认应用可访问**
   ```bash
   curl http://localhost:8080/api/health
   # 如果返回200或其他成功响应，说明应用正常运行
   ```

### 方式1: 使用自动化测试脚本（推荐）

```bash
cd /Users/jc/Documents/workshop/medusa-developOS3/tools

# 运行提现系统完整测试
DB_PASS=rootpassword ./test_withdrawal_system.sh
```

**测试内容**：
- ✓ Vendor Member 登录
- ✓ 查询余额信息
- ✓ 查询提现地址列表
- ✓ 请求更新 BTC 地址（PGP 验证流程）
- ✓ 验证并更新地址
- ✓ 订单发货测试（余额更新）
- ✓ 创建提现请求
- ✓ 查询提现请求列表

### 方式2: 手动测试API

#### Step 1: Vendor Member 登录

```bash
# 登录获取 token
curl -X POST 'http://localhost:8080/api/mall/vendor/member/login' \
  -H 'Content-Type: application/x-www-form-urlencoded' \
  -d 'username=testvendor&password=test123' \
  -c cookies.txt

# 提取返回的 token，后续请求需要使用
```

#### Step 2: 查询余额

```bash
curl -X GET 'http://localhost:8080/api/mall/vendor/withdrawal/balance' \
  -b cookies.txt \
  -H 'Authorization: Bearer YOUR_TOKEN'
```

**预期响应**：
```json
{
  "code": 200,
  "data": {
    "withdrawableBalance": 0.00,
    "pendingBalance": 0.00,
    "totalWithdrawn": 0.00
  }
}
```

#### Step 3: 查询提现地址

```bash
curl -X GET 'http://localhost:8080/api/mall/vendor/withdrawal/addresses' \
  -b cookies.txt \
  -H 'Authorization: Bearer YOUR_TOKEN'
```

#### Step 4: 发货测试订单

```bash
# 查找测试订单ID
TEST_ORDER=$(DB_PASS=rootpassword mysql -h localhost -P 3306 -u root medusa -N -e \
  "SELECT id FROM mall_order WHERE vendor_id=100 AND status=1 LIMIT 1" | grep -v "Using a password")

# 发货订单
curl -X POST "http://localhost:8080/api/mall/vendor/order/ship" \
  -b cookies.txt \
  -H 'Authorization: Bearer YOUR_TOKEN' \
  -H 'Content-Type: application/x-www-form-urlencoded' \
  -d "orderId=$TEST_ORDER&trackingNumber=TRACK$(date +%s)"
```

#### Step 5: 再次查询余额（应该看到 pending_balance 增加）

```bash
curl -X GET 'http://localhost:8080/api/mall/vendor/withdrawal/balance' \
  -b cookies.txt \
  -H 'Authorization: Bearer YOUR_TOKEN'
```

**预期**：`pendingBalance` 应该是 500.00（订单金额）

#### Step 6: 创建提现请求

```bash
# 首先手动设置一些可提现余额（用于测试）
DB_PASS=rootpassword mysql -h localhost -P 3306 -u root medusa -e \
  "UPDATE mall_vendor SET withdrawable_balance=100.00 WHERE id=100"

# 创建提现请求
curl -X POST 'http://localhost:8080/api/mall/vendor/withdrawal/request' \
  -b cookies.txt \
  -H 'Authorization: Bearer YOUR_TOKEN' \
  -H 'Content-Type: application/x-www-form-urlencoded' \
  -d 'currency=BTC&amount=50.00'
```

### 方式3: 使用浏览器/Postman

1. 导入以下环境变量：
   - `BASE_URL`: http://localhost:8080/api
   - `VENDOR_USERNAME`: testvendor
   - `VENDOR_PASSWORD`: test123

2. 按照以下顺序测试API：
   - POST `/api/mall/vendor/member/login` - 登录
   - GET `/api/mall/vendor/withdrawal/balance` - 查询余额
   - GET `/api/mall/vendor/withdrawal/addresses` - 查询地址
   - POST `/api/mall/vendor/withdrawal/address/update` - 更新地址
   - POST `/api/mall/vendor/withdrawal/request` - 创建提现请求

## 📊 验证结果

### 数据库验证

#### 1. 查看 Vendor 余额

```sql
SELECT 
    id, 
    vendor_name, 
    withdrawable_balance, 
    pending_balance, 
    total_withdrawn, 
    bond, 
    level
FROM mall_vendor 
WHERE id = 100;
```

#### 2. 查看余额变动记录

```sql
SELECT 
    id,
    vendor_id,
    change_type,
    amount,
    before_balance,
    after_balance,
    related_order_id,
    available_date,
    description,
    create_time
FROM mall_vendor_balance_log
WHERE vendor_id = 100
ORDER BY create_time DESC;
```

#### 3. 查看订单的余额释放信息

```sql
SELECT 
    id,
    order_sn,
    vendor_id,
    total_amount,
    status,
    balance_available_date,
    is_balance_released
FROM mall_order
WHERE vendor_id = 100
ORDER BY create_time DESC;
```

#### 4. 查看提现请求

```sql
SELECT 
    id,
    request_code,
    vendor_id,
    currency,
    amount,
    withdrawal_address,
    request_status,
    request_time
FROM mall_vendor_withdrawal_request
WHERE vendor_id = 100
ORDER BY request_time DESC;
```

## ✅ 成功标准

测试成功的标志：

1. **登录成功** - 返回 token
2. **余额查询成功** - 返回三个余额字段
3. **地址列表正常** - 返回4个币种的地址
4. **发货后余额更新** - `pending_balance` 增加，`balance_available_date` 已设置
5. **提现请求创建** - 返回请求编号和状态 PENDING
6. **数据库一致** - 余额变动记录和订单状态正确更新

## 🔧 故障排查

### 问题1: 应用未启动

**症状**：`curl: (7) Failed to connect to localhost port 8080`

**解决**：
```bash
cd /Users/jc/Documents/workshop/medusa-developOS3
# 启动应用
```

### 问题2: "Not logged in or vendor not approved"

**症状**：API返回此错误

**原因**：
1. Vendor Member 未登录
2. Vendor未审批

**解决**：
```sql
-- 检查 Vendor 状态
SELECT id, vendor_name, status FROM mall_vendor WHERE id = 100;
-- 如果 status != 1，更新它
UPDATE mall_vendor SET status = 1 WHERE id = 100;
```

### 问题3: "Insufficient withdrawable balance"

**症状**：创建提现请求失败

**解决**：
```sql
-- 手动设置可提现余额
UPDATE mall_vendor SET withdrawable_balance = 100.00 WHERE id = 100;
```

## 📝 测试报告格式

测试完成后，请提供以下信息：

1. **测试环境**：
   - 数据库版本：
   - Java版本：
   - 应用启动成功：是/否

2. **测试结果**：
   - 总测试数：
   - 通过：
   - 失败：

3. **失败用例详情**（如有）：
   - 用例编号：
   - 错误信息：
   - 请求/响应详情：

4. **数据库验证**：
   - 余额是否正确更新：
   - 余额日志是否记录：
   - 订单状态是否正确：

## 📚 更多信息

- 详细测试用例：`WITHDRAWAL_SYSTEM_TEST_GUIDE.md`
- API文档：查看 `ApiVendorWithdrawalController.java`
- 数据库设计：查看 `V1.18_20251118__vendor_withdrawal_system.sql`


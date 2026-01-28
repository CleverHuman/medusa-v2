# Vendor 提现系统测试指南

## 快速开始

### 1. 准备测试数据

首先，运行数据库迁移和准备测试数据：

```bash
cd /Users/jc/Documents/workshop/medusa-developOS3/tools

# 准备测试数据
DB_PASS=rootpassword mysql -h localhost -P 3306 -u root medusa < prepare_withdrawal_test_data.sql
```

### 2. 启动应用

确保 Medusa 应用正在运行：
- 默认端口：8080
- API Base URL: `http://localhost:8080/api`

### 3. 运行测试

```bash
cd /Users/jc/Documents/workshop/medusa-developOS3/tools

# 运行提现系统测试
DB_PASS=rootpassword ./test_withdrawal_system.sh
```

## 测试覆盖的功能

### ✅ 测试用例列表

1. **TC-LOGIN**: Vendor Member 登录
   - 验证：testvendor / test123 可以成功登录
   - 获取会话 token

2. **TC-001**: 查询 Vendor 余额
   - 可提现余额（withdrawableBalance）
   - 待确认余额（pendingBalance）
   - 累计已提现（totalWithdrawn）

3. **TC-002**: 查询所有提现地址
   - 验证：BTC, XMR, USDT_TRX, USDT_ERC 地址是否已初始化

4. **TC-003**: 请求更新 BTC 地址（发起 PGP 验证流程）
   - 生成验证码
   - 更新为新的 BTC 地址

5. **TC-004**: 使用验证码验证并更新地址
   - 验证验证码
   - 激活新地址

6. **TC-005**: 订单发货测试
   - 查找已支付的订单
   - 调用发货 API
   - 验证待确认余额是否更新
   - 检查 balance_available_date 是否已设置

7. **TC-006**: 创建提现请求
   - 币种：BTC
   - 金额：$50.00
   - 验证提现请求创建成功

8. **TC-007**: 查询我的提现请求列表
   - 验证提现请求列表返回正确

## 核心功能说明

### 余额管理流程

1. **订单发货** → **待确认余额**
   - 订单发货后，订单金额加入 `pending_balance`
   - 根据 Vendor Level 计算可用日期：
     - Level 1: 14 天后可用
     - Level 2: 10 天后可用
     - Level 3: 7 天后可用
     - Level 4: 5 天后可用
     - Level 5+: 3 天后可用

2. **定时释放** → **可提现余额**
   - 定时任务扫描到期的待确认余额
   - 从 `pending_balance` 转移到 `withdrawable_balance`

3. **提现请求**
   - Vendor 创建提现请求
   - 从 `withdrawable_balance` 冻结金额
   - Admin 审批后执行转账
   - 完成后加入 `total_withdrawn`

### 提现地址管理

1. **查询地址**：支持 BTC, XMR, USDT_TRX, USDT_ERC 四种币种

2. **更新地址**（PGP 验证流程）：
   - 步骤 1：请求更新，系统生成6位验证码
   - 步骤 2：（实际应用中）使用 PGP 公钥加密验证码或发送到注册邮箱
   - 步骤 3：Vendor 提供验证码
   - 步骤 4：系统验证通过后激活新地址

3. **地址状态**：
   - `is_active = 1`：已激活，可用于提现
   - `is_active = 0`：未验证或已停用

## 数据库验证

### 查询 Vendor 余额

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

### 查询余额变动记录

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
ORDER BY create_time DESC
LIMIT 10;
```

### 查询提现请求

```sql
SELECT 
    id,
    request_code,
    vendor_id,
    currency,
    amount,
    withdrawal_address,
    request_status,
    request_time,
    approve_time
FROM mall_vendor_withdrawal_request
WHERE vendor_id = 100
ORDER BY request_time DESC;
```

### 查询订单余额释放状态

```sql
SELECT 
    id,
    order_sn,
    vendor_id,
    total_amount,
    status,
    balance_available_date,
    is_balance_released,
    create_time
FROM mall_order
WHERE vendor_id = 100
ORDER BY create_time DESC
LIMIT 10;
```

## 预期结果

✅ **成功指标**：
- 所有测试用例通过
- Vendor 可以查询余额和历史记录
- 提现地址管理功能正常
- PGP 验证流程工作正常
- 订单发货后余额正确更新
- 提现请求创建成功

⚠️ **注意事项**：
- 测试前确保数据库迁移已完成
- 确保 testvendor 用户和 Vendor ID 100 存在
- 测试订单需要处于 status=1（已支付）状态
- 数据库密码根据实际情况设置 `DB_PASS` 环境变量

## 故障排查

### 问题 1: "Not logged in or vendor not approved"

**原因**：Vendor Member 未登录或对应的 Vendor 未审批

**解决**：
1. 检查 testvendor 是否登录成功
2. 检查 Vendor ID 100 的状态是否为 `status=1`
3. 检查 vendor_application 是否已审批

### 问题 2: "Insufficient withdrawable balance"

**原因**：可提现余额不足

**解决**：
```sql
UPDATE mall_vendor 
SET withdrawable_balance = 100.00 
WHERE id = 100;
```

### 问题 3: "No active withdrawal address found"

**原因**：提现地址未激活

**解决**：
```sql
UPDATE mall_vendor_withdrawal_address 
SET is_active = 1, verified_at = NOW() 
WHERE vendor_id = 100 AND currency = 'BTC';
```

## 下一步

完成测试后，可以：

1. **测试定时任务**：手动调用 `releaseExpiredPendingBalance()` 方法
2. **测试 Admin 审批**：实现 Admin 端提现审批 API
3. **完整流程测试**：从订单创建到提现完成的完整流程
4. **性能测试**：测试大量订单和提现请求的场景

## API 端点参考

### Vendor 端 API

- `GET /api/mall/vendor/withdrawal/balance` - 查询余额
- `GET /api/mall/vendor/withdrawal/balance/logs` - 查询余额变动历史
- `GET /api/mall/vendor/withdrawal/addresses` - 查询所有提现地址
- `GET /api/mall/vendor/withdrawal/address/{currency}` - 查询特定币种地址
- `POST /api/mall/vendor/withdrawal/address/update` - 请求更新地址
- `POST /api/mall/vendor/withdrawal/address/verify` - 验证并更新地址
- `POST /api/mall/vendor/withdrawal/request` - 创建提现请求
- `GET /api/mall/vendor/withdrawal/requests` - 查询我的提现请求列表

### 参数说明

**创建提现请求**：
- `currency`: BTC / XMR / USDT_TRX / USDT_ERC
- `amount`: 提现金额（BigDecimal）

**更新提现地址**：
- `currency`: BTC / XMR / USDT_TRX / USDT_ERC
- `newAddress`: 新的提现地址

**验证地址**：
- `currency`: BTC / XMR / USDT_TRX / USDT_ERC
- `verificationCode`: 6位数验证码


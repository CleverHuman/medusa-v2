# Vendor Bond & Level 系统快速测试指南

## 快速开始

### 1. 准备测试数据

```bash
cd /Users/jc/Documents/workshop/medusa-developOS3/tools
mysql -h localhost -P 3306 -u root -p medusa < prepare_vendor_bond_test_data.sql
```

### 2. 运行自动化测试脚本

```bash
./test_vendor_bond_level.sh
```

### 3. 手动测试步骤

#### 步骤 1: 验证基础数据

```sql
-- 查询 Vendor Bond 和 Level
SELECT id, vendor_code, vendor_name, bond, level, sales_points, (bond * level) as max_sales_limit
FROM mall_vendor
WHERE id = 100;
```

**预期结果**:
- `bond = 1000.00`
- `level = 1`
- `sales_points = 0`
- `max_sales_limit = 1000.00`

#### 步骤 2: 测试销售限额检查

创建一个小金额订单（在限额内）：
```sql
-- 创建测试订单（金额 $500，在限额内）
INSERT INTO mall_order (id, order_sn, vendor_id, total_amount, status, create_time)
VALUES ('TEST_BOND_001', 'ORD_BOND_001', 100, 500.00, 0, NOW());
```

验证订单创建成功：
```sql
SELECT id, order_sn, vendor_id, total_amount, status
FROM mall_order
WHERE id = 'TEST_BOND_001';
```

#### 步骤 3: 测试超过限额的情况

尝试创建超过限额的订单：
```sql
-- 创建另一个订单（金额 $600，总计 $1,100，超过限额）
-- 注意：这应该通过 API 测试，因为 Service 层会检查限额
```

#### 步骤 4: 测试点数更新

标记订单为已发货，验证点数更新：
```sql
-- 1. 先将订单状态改为 paid
UPDATE mall_order SET status = 1 WHERE id = 'TEST_BOND_001';

-- 2. 标记为已发货（通过 API 或直接调用 Service）
-- 通过 API:
curl -X POST "http://localhost:8080/api/mall/vendor/order/TEST_BOND_001/ship?trackingNumber=TRACK123" \
  -H "Authorization: Bearer ${TOKEN}" \
  -b cookie.txt

-- 3. 验证点数更新
SELECT id, vendor_name, sales_points, level
FROM mall_vendor
WHERE id = 100;
```

**预期结果**:
- `sales_points = 500` (订单金额 $500 = 500 点)
- `level = 1` (500 点 < 1000，仍为 Level 1)

#### 步骤 5: 测试等级升级

创建并发货一个 $500 的订单，使总点数达到 1,000：
```sql
-- 1. 创建订单
INSERT INTO mall_order (id, order_sn, vendor_id, total_amount, status, create_time)
VALUES ('TEST_BOND_002', 'ORD_BOND_002', 100, 500.00, 1, NOW());

-- 2. 标记为已发货（通过 API）
curl -X POST "http://localhost:8080/api/mall/vendor/order/TEST_BOND_002/ship?trackingNumber=TRACK456" \
  -H "Authorization: Bearer ${TOKEN}" \
  -b cookie.txt

-- 3. 验证等级升级
SELECT id, vendor_name, sales_points, level
FROM mall_vendor
WHERE id = 100;
```

**预期结果**:
- `sales_points = 1000`
- `level = 2` (自动升级)
- 最大销售限额变为 $2,000 (1000 × 2)

#### 步骤 6: 验证升级历史

```sql
SELECT 
    id,
    vendor_id,
    old_level,
    new_level,
    old_points,
    new_points,
    trigger_order_id,
    trigger_amount,
    create_time
FROM mall_vendor_level_history
WHERE vendor_id = 100
ORDER BY create_time DESC;
```

**预期结果**:
- 有一条升级记录
- `old_level = 1`, `new_level = 2`
- `old_points = 500`, `new_points = 1000`

## 测试检查清单

- [ ] 测试数据准备完成
- [ ] Vendor Bond 和 Level 信息正确
- [ ] 最大销售限额计算正确
- [ ] 订单创建时销售限额检查生效
- [ ] 订单发货后销售点数正确更新
- [ ] 等级自动升级功能正常
- [ ] 升级历史记录正确

## 常见问题

### Q: 测试脚本执行失败，提示数据库连接错误
A: 检查数据库配置：
```bash
export DB_HOST=localhost
export DB_PORT=3306
export DB_USER=root
export DB_PASS=your_password
```

### Q: 订单创建时没有检查销售限额
A: 确保：
1. 订单包含 vendor 产品（`product_origin = 1`）
2. `OrderServiceImpl` 中已注入 `IVendorBondService`
3. Vendor 的 `bond` 字段已设置

### Q: 订单发货后点数没有更新
A: 确保：
1. 订单状态为 `1` (paid) 或 `6` (vendor_accepted)
2. 通过 `VendorOrderServiceImpl.shipOrder()` 方法标记发货
3. 订单 `total_amount` 不为空

## 相关文档

- 完整测试用例: `TEST_CASES_VENDOR_BOND_LEVEL.md`
- 测试数据准备: `prepare_vendor_bond_test_data.sql`
- 自动化测试脚本: `test_vendor_bond_level.sh`


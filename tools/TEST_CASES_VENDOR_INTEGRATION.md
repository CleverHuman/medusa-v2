# Vendor Integration 完整测试用例

## 测试环境准备

### 数据库连接信息
请提供以下信息：
- 数据库地址: `localhost:3306` (或您的实际地址)
- 数据库名称: `medusa` (或您的实际数据库名)
- 用户名: `[待提供]`
- 密码: `[待提供]`

### 应用信息
- API 基础地址: `http://localhost:8080/api`
- Admin 用户名: `admin`
- Admin 密码: `admin123`

---

## 第一部分：数据库准备

### 1.1 检查数据库表结构

```sql
-- 检查 mall_product 表是否有必要字段
DESC mall_product;
-- 应该包含: product_origin, origin_id, approval_status

-- 检查 mall_order 表是否有 vendor_id 字段
DESC mall_order;
-- 应该包含: vendor_id

-- 检查 mall_order_item 表是否有 product_origin 和 origin_id
DESC mall_order_item;
-- 应该包含: product_origin, origin_id

-- 检查审批表是否存在
SHOW TABLES LIKE 'mall_vendor_product_approval';
```

### 1.2 准备测试数据

执行以下 SQL 脚本准备测试数据：

```sql
-- ============================================
-- 测试数据准备脚本
-- ============================================

-- 1. 创建测试 Vendor Member (密码: test123)
-- BCrypt hash for "test123": $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy
INSERT INTO mall_vendor_member (username, password_hash, email, phone, status) 
VALUES ('testvendor', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'vendor@test.com', '1234567890', 1)
ON DUPLICATE KEY UPDATE 
    password_hash = '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    status = 1;

-- 获取刚创建的 member_id (假设为 1，如果已存在则使用现有ID)
SET @vendor_member_id = (SELECT id FROM mall_vendor_member WHERE username = 'testvendor' LIMIT 1);

-- 2. 创建测试 Vendor
INSERT INTO mall_vendor (id, vendor_code, vendor_name, status) 
VALUES (100, 'VENDOR_TEST_001', 'Test Vendor Company', 1)
ON DUPLICATE KEY UPDATE 
    vendor_name = 'Test Vendor Company',
    status = 1;

-- 3. 创建测试 Vendor Application (已审批状态)
INSERT INTO mall_vendor_application (
    member_id, application_id, vendor_name, status, vendor_id,
    has_market_experience, experience_years, location, product_categories,
    primary_email, status
) VALUES (
    @vendor_member_id, 
    'APP_TEST_001', 
    'Test Vendor Company', 
    'approved', 
    100,
    1, 
    5, 
    'Test Location', 
    '["electronics", "clothing"]',
    'vendor@test.com',
    'approved'
)
ON DUPLICATE KEY UPDATE 
    status = 'approved',
    vendor_id = 100;

-- 4. 创建测试平台产品 (用于对比)
INSERT INTO mall_product (
    product_id, name, category, description, status, 
    product_origin, origin_id, channel
) VALUES (
    'PLATFORM-PROD-001', 
    'Platform Product 1', 
    'test', 
    'This is a platform product', 
    1,
    0, 
    NULL, 
    'OS/TG'
)
ON DUPLICATE KEY UPDATE name = 'Platform Product 1';

-- 5. 创建测试 Vendor 产品 (待审批)
INSERT INTO mall_product (
    product_id, name, category, description, status, 
    product_origin, origin_id, channel, approval_status
) VALUES (
    'VENDOR-PROD-001', 
    'Vendor Product 1', 
    'test', 
    'This is a vendor product waiting for approval', 
    0,
    1, 
    100, 
    'OS/TG',
    'pending_approval'
)
ON DUPLICATE KEY UPDATE name = 'Vendor Product 1';

-- 6. 创建对应的 Product2 (SKU) 记录
INSERT INTO mall_product2 (product_id, sku, price, model, unit, inventory) 
VALUES 
    ('PLATFORM-PROD-001', 'PLATFORM-SKU-001', 50.00, 50.00, 'amount', 100),
    ('VENDOR-PROD-001', 'VENDOR-SKU-001', 100.00, 100.00, 'amount', 50)
ON DUPLICATE KEY UPDATE price = VALUES(price);

-- 7. 创建测试会员 (用于创建订单)
INSERT INTO mall_member (member_id, username, password, email, status) 
VALUES (9999, 'testcustomer', 'test123', 'customer@test.com', 1)
ON DUPLICATE KEY UPDATE username = 'testcustomer';

-- 验证数据
SELECT 'Vendor Member' AS type, id, username FROM mall_vendor_member WHERE username = 'testvendor'
UNION ALL
SELECT 'Vendor' AS type, id, vendor_name FROM mall_vendor WHERE id = 100
UNION ALL
SELECT 'Vendor Application' AS type, id, application_id FROM mall_vendor_application WHERE application_id = 'APP_TEST_001'
UNION ALL
SELECT 'Product' AS type, id, name FROM mall_product WHERE product_id IN ('PLATFORM-PROD-001', 'VENDOR-PROD-001');
```

---

## 第二部分：API 测试用例

### 2.1 Vendor 认证测试

#### TC-001: Vendor 登录
**测试目的**: 验证 Vendor Member 可以成功登录

**请求**:
```bash
curl -X POST http://localhost:8080/api/mall/vendor/member/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testvendor",
    "password": "test123"
  }'
```

**预期结果**:
- HTTP 200
- 返回 token 和用户信息
- Session 中保存 vendorMemberId 和 vendorMemberToken

**验证点**:
- [ ] 返回 code = 200
- [ ] 返回 token 不为空
- [ ] 返回 username = "testvendor"

---

#### TC-002: 获取 Vendor Profile
**测试目的**: 验证登录后可以获取个人信息

**请求**:
```bash
curl -X GET http://localhost:8080/api/mall/vendor/member/profile \
  -H "Authorization: Bearer {TOKEN_FROM_TC001}"
```

**预期结果**:
- HTTP 200
- 返回用户信息

---

### 2.2 Vendor 产品管理测试

#### TC-101: 创建 Vendor 产品
**测试目的**: 验证 Vendor 可以创建产品，产品状态为待审批

**请求**:
```bash
curl -X POST http://localhost:8080/api/mall/vendor/product \
  -H "Authorization: Bearer {TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "productId": "VENDOR-PROD-NEW-001",
    "name": "New Vendor Product",
    "category": "electronics",
    "description": "A new product from vendor",
    "imageUrl": "https://example.com/product.jpg",
    "status": 0,
    "channel": "OS/TG"
  }'
```

**预期结果**:
- HTTP 200
- 产品创建成功
- 数据库中 `product_origin = 1`, `origin_id = 100`
- 数据库中 `status = 0` (未激活)
- `mall_vendor_product_approval` 表中有记录，`approval_status = 'pending_approval'`

**验证 SQL**:
```sql
SELECT id, product_id, product_origin, origin_id, status, approval_status 
FROM mall_product 
WHERE product_id = 'VENDOR-PROD-NEW-001';

SELECT * FROM mall_vendor_product_approval 
WHERE product_id = (SELECT id FROM mall_product WHERE product_id = 'VENDOR-PROD-NEW-001');
```

---

#### TC-102: 查看自己的产品列表
**测试目的**: 验证 Vendor 只能看到自己的产品

**请求**:
```bash
curl -X GET http://localhost:8080/api/mall/vendor/product/list \
  -H "Authorization: Bearer {TOKEN}"
```

**预期结果**:
- HTTP 200
- 返回产品列表
- 只包含 `origin_id = 100` 的产品
- 不包含平台产品 (`product_origin = 0`)
- 不包含其他 Vendor 的产品

**验证点**:
- [ ] 返回的产品列表中所有产品的 `origin_id = 100`
- [ ] 不包含 `product_origin = 0` 的产品

---

#### TC-103: 查看产品详情
**测试目的**: 验证 Vendor 可以查看自己的产品详情

**请求**:
```bash
# 先获取产品ID
PRODUCT_ID=$(curl -s -X GET http://localhost:8080/api/mall/vendor/product/list \
  -H "Authorization: Bearer {TOKEN}" | jq -r '.data[0].id')

curl -X GET http://localhost:8080/api/mall/vendor/product/${PRODUCT_ID} \
  -H "Authorization: Bearer {TOKEN}"
```

**预期结果**:
- HTTP 200
- 返回产品详情

---

#### TC-104: 更新产品
**测试目的**: 验证 Vendor 可以更新自己的产品，已审批产品更新后变为待审批

**请求**:
```bash
curl -X PUT http://localhost:8080/api/mall/vendor/product \
  -H "Authorization: Bearer {TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "id": {PRODUCT_ID},
    "name": "Updated Product Name",
    "description": "Updated description"
  }'
```

**预期结果**:
- HTTP 200
- 产品信息更新
- 如果产品之前是 `approved`，更新后 `approval_status` 变为 `pending_approval`

---

#### TC-105: 删除产品
**测试目的**: 验证 Vendor 可以删除自己的产品（软删除）

**请求**:
```bash
curl -X DELETE http://localhost:8080/api/mall/vendor/product/{PRODUCT_ID} \
  -H "Authorization: Bearer {TOKEN}"
```

**预期结果**:
- HTTP 200
- 产品状态变为 2 (已删除)

---

#### TC-106: 尝试访问其他 Vendor 的产品（安全测试）
**测试目的**: 验证 Vendor 无法访问其他 Vendor 的产品

**前提**: 创建另一个 Vendor 的产品

**请求**:
```bash
# 尝试访问其他 Vendor 的产品ID
curl -X GET http://localhost:8080/api/mall/vendor/product/{OTHER_VENDOR_PRODUCT_ID} \
  -H "Authorization: Bearer {TOKEN}"
```

**预期结果**:
- HTTP 500 或 403
- 返回错误信息 "Product not found or access denied"

---

### 2.3 产品审批测试（Admin）

#### TC-201: 查看待审批产品列表
**测试目的**: 验证 Admin 可以查看所有待审批的产品

**请求**:
```bash
curl -X GET http://localhost:8080/admin/mall/vendor/product/pending \
  -H "Authorization: Bearer {ADMIN_TOKEN}"
```

**预期结果**:
- HTTP 200
- 返回待审批产品列表
- 所有产品的 `approval_status = 'pending_approval'`

---

#### TC-202: 审批通过产品
**测试目的**: 验证 Admin 可以审批通过产品

**请求**:
```bash
curl -X POST "http://localhost:8080/admin/mall/vendor/product/approve/{PRODUCT_ID}?notes=Product looks good" \
  -H "Authorization: Bearer {ADMIN_TOKEN}"
```

**预期结果**:
- HTTP 200
- 产品状态变为 1 (激活)
- `approval_status = 'approved'`
- `mall_vendor_product_approval` 表中记录审批人信息

**验证 SQL**:
```sql
SELECT p.id, p.status, p.approval_status, a.approval_status, a.approver_name, a.approved_time
FROM mall_product p
LEFT JOIN mall_vendor_product_approval a ON p.id = a.product_id
WHERE p.id = {PRODUCT_ID};
```

---

#### TC-203: 审批拒绝产品
**测试目的**: 验证 Admin 可以拒绝产品

**请求**:
```bash
curl -X POST "http://localhost:8080/admin/mall/vendor/product/reject/{PRODUCT_ID}?reason=Product does not meet requirements" \
  -H "Authorization: Bearer {ADMIN_TOKEN}"
```

**预期结果**:
- HTTP 200
- 产品状态保持 0 (未激活)
- `approval_status = 'rejected'`
- 记录拒绝原因

---

### 2.4 Vendor 订单管理测试

#### TC-301: 创建包含 Vendor 产品的订单
**测试目的**: 验证创建包含 Vendor 产品的订单时，正确设置 vendor_id 和 product_origin

**请求**:
```bash
curl -X POST http://localhost:8080/api/mall/order/create \
  -H "Content-Type: application/json" \
  -d '{
    "items": [
      {
        "sku": "VENDOR-SKU-001",
        "quantity": 2
      }
    ],
    "memberId": 9999,
    "sourceType": 0,
    "shippingAddress": {
      "receiverName": "Test Customer",
      "addressLine1": "123 Test Street",
      "city": "Test City",
      "state": "TS",
      "postCode": "12345",
      "country": "AU"
    }
  }'
```

**预期结果**:
- HTTP 200
- 订单创建成功
- `mall_order.vendor_id = 100`
- `mall_order_item.product_origin = 1`
- `mall_order_item.origin_id = 100`

**验证 SQL**:
```sql
SELECT o.id, o.order_sn, o.vendor_id, oi.product_id, oi.product_origin, oi.origin_id
FROM mall_order o
JOIN mall_order_item oi ON o.id = oi.order_id
WHERE o.id = '{ORDER_ID}';
```

---

#### TC-302: 查看 Vendor 订单列表
**测试目的**: 验证 Vendor 可以查看自己的订单列表

**请求**:
```bash
curl -X GET http://localhost:8080/api/mall/vendor/order/list \
  -H "Authorization: Bearer {VENDOR_TOKEN}"
```

**预期结果**:
- HTTP 200
- 返回订单列表
- 所有订单的 `vendor_id = 100`
- 不包含其他 Vendor 的订单
- 不包含平台订单 (`vendor_id IS NULL`)

---

#### TC-303: 查看订单详情
**测试目的**: 验证 Vendor 可以查看订单详情

**请求**:
```bash
curl -X GET http://localhost:8080/api/mall/vendor/order/{ORDER_ID} \
  -H "Authorization: Bearer {VENDOR_TOKEN}"
```

**预期结果**:
- HTTP 200
- 返回订单详情
- 包含订单项信息

---

#### TC-304: 接受订单
**测试目的**: 验证 Vendor 可以接受待处理订单

**前提**: 订单状态为 0 (pending)

**请求**:
```bash
curl -X POST http://localhost:8080/api/mall/vendor/order/{ORDER_ID}/accept \
  -H "Authorization: Bearer {VENDOR_TOKEN}"
```

**预期结果**:
- HTTP 200
- 订单状态变为 6 (vendor_accepted)

**验证 SQL**:
```sql
SELECT id, order_sn, status FROM mall_order WHERE id = '{ORDER_ID}';
-- status 应该是 6
```

---

#### TC-305: 拒绝订单
**测试目的**: 验证 Vendor 可以拒绝订单

**前提**: 订单状态为 0 (pending)

**请求**:
```bash
curl -X POST "http://localhost:8080/api/mall/vendor/order/{ORDER_ID}/reject?reason=Out of stock" \
  -H "Authorization: Bearer {VENDOR_TOKEN}"
```

**预期结果**:
- HTTP 200
- 订单状态变为 4 (cancelled)
- 备注中包含拒绝原因

---

#### TC-306: 标记发货
**测试目的**: 验证 Vendor 可以标记订单为已发货

**前提**: 订单状态为 1 (paid) 或 6 (vendor_accepted)

**请求**:
```bash
curl -X POST "http://localhost:8080/api/mall/vendor/order/{ORDER_ID}/ship?trackingNumber=TRACK123456" \
  -H "Authorization: Bearer {VENDOR_TOKEN}"
```

**预期结果**:
- HTTP 200
- 订单状态变为 3 (shipped)
- 运单号更新到 `mall_order_shipping` 表

**验证 SQL**:
```sql
SELECT o.id, o.status, s.shipping_number
FROM mall_order o
LEFT JOIN mall_order_shipping s ON o.id = s.order_id
WHERE o.id = '{ORDER_ID}';
```

---

#### TC-307: 尝试访问其他 Vendor 的订单（安全测试）
**测试目的**: 验证 Vendor 无法访问其他 Vendor 的订单

**请求**:
```bash
curl -X GET http://localhost:8080/api/mall/vendor/order/{OTHER_VENDOR_ORDER_ID} \
  -H "Authorization: Bearer {VENDOR_TOKEN}"
```

**预期结果**:
- HTTP 500 或 403
- 返回错误信息 "Order not found or access denied"

---

### 2.5 混合订单测试

#### TC-401: 创建混合订单（平台 + Vendor 产品）
**测试目的**: 验证包含平台和 Vendor 产品的混合订单处理

**请求**:
```bash
curl -X POST http://localhost:8080/api/mall/order/create \
  -H "Content-Type: application/json" \
  -d '{
    "items": [
      {
        "sku": "PLATFORM-SKU-001",
        "quantity": 1
      },
      {
        "sku": "VENDOR-SKU-001",
        "quantity": 1
      }
    ],
    "memberId": 9999,
    "sourceType": 0,
    "shippingAddress": {
      "receiverName": "Test Customer",
      "addressLine1": "123 Test Street",
      "city": "Test City",
      "state": "TS",
      "postCode": "12345",
      "country": "AU"
    }
  }'
```

**预期结果**:
- HTTP 200
- 订单创建成功
- `mall_order.vendor_id = 100` (因为包含 Vendor 产品)
- 订单项中：
  - Platform 产品: `product_origin = 0`, `origin_id = NULL`
  - Vendor 产品: `product_origin = 1`, `origin_id = 100`

---

## 第三部分：自动化测试脚本

### 3.1 测试脚本模板

创建一个测试脚本文件 `test_vendor_integration.sh`:

```bash
#!/bin/bash

# 配置
API_BASE="http://localhost:8080"
VENDOR_USERNAME="testvendor"
VENDOR_PASSWORD="test123"
ADMIN_USERNAME="admin"
ADMIN_PASSWORD="admin"

# 颜色输出
GREEN='\033[0;32m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo "=== Vendor Integration Test Suite ==="

# 1. Vendor 登录
echo -e "\n${GREEN}TC-001: Vendor Login${NC}"
LOGIN_RESPONSE=$(curl -s -X POST ${API_BASE}/api/mall/vendor/member/login \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"${VENDOR_USERNAME}\",\"password\":\"${VENDOR_PASSWORD}\"}")

VENDOR_TOKEN=$(echo $LOGIN_RESPONSE | jq -r '.data.token // .token')
if [ "$VENDOR_TOKEN" != "null" ] && [ -n "$VENDOR_TOKEN" ]; then
  echo -e "${GREEN}✓ Login successful, token: ${VENDOR_TOKEN:0:20}...${NC}"
else
  echo -e "${RED}✗ Login failed${NC}"
  echo $LOGIN_RESPONSE | jq .
  exit 1
fi

# 2. 创建产品
echo -e "\n${GREEN}TC-101: Create Vendor Product${NC}"
CREATE_PRODUCT_RESPONSE=$(curl -s -X POST ${API_BASE}/api/mall/vendor/product \
  -H "Authorization: Bearer ${VENDOR_TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "productId": "VENDOR-PROD-TEST-001",
    "name": "Test Product",
    "category": "test",
    "description": "Test description",
    "status": 0,
    "channel": "OS/TG"
  }')

if echo $CREATE_PRODUCT_RESPONSE | jq -e '.code == 200' > /dev/null; then
  echo -e "${GREEN}✓ Product created successfully${NC}"
  PRODUCT_ID=$(echo $CREATE_PRODUCT_RESPONSE | jq -r '.data.id // .data')
  echo "Product ID: $PRODUCT_ID"
else
  echo -e "${RED}✗ Product creation failed${NC}"
  echo $CREATE_PRODUCT_RESPONSE | jq .
fi

# 3. 查看产品列表
echo -e "\n${GREEN}TC-102: List Vendor Products${NC}"
LIST_RESPONSE=$(curl -s -X GET ${API_BASE}/api/mall/vendor/product/list \
  -H "Authorization: Bearer ${VENDOR_TOKEN}")

if echo $LIST_RESPONSE | jq -e '.code == 200' > /dev/null; then
  PRODUCT_COUNT=$(echo $LIST_RESPONSE | jq '.data | length')
  echo -e "${GREEN}✓ Retrieved ${PRODUCT_COUNT} products${NC}"
else
  echo -e "${RED}✗ Failed to retrieve products${NC}"
  echo $LIST_RESPONSE | jq .
fi

# 4. 查看订单列表
echo -e "\n${GREEN}TC-302: List Vendor Orders${NC}"
ORDER_LIST_RESPONSE=$(curl -s -X GET ${API_BASE}/api/mall/vendor/order/list \
  -H "Authorization: Bearer ${VENDOR_TOKEN}")

if echo $ORDER_LIST_RESPONSE | jq -e '.code == 200' > /dev/null; then
  ORDER_COUNT=$(echo $ORDER_LIST_RESPONSE | jq '.data | length')
  echo -e "${GREEN}✓ Retrieved ${ORDER_COUNT} orders${NC}"
else
  echo -e "${RED}✗ Failed to retrieve orders${NC}"
  echo $ORDER_LIST_RESPONSE | jq .
fi

echo -e "\n=== Test Suite Completed ==="
```

---

## 第四部分：数据库验证查询

### 4.1 数据完整性检查

```sql
-- 检查所有 Vendor 产品
SELECT 
    p.id,
    p.product_id,
    p.name,
    p.product_origin,
    p.origin_id,
    p.status,
    p.approval_status,
    a.approval_status AS approval_table_status,
    a.approver_name
FROM mall_product p
LEFT JOIN mall_vendor_product_approval a ON p.id = a.product_id
WHERE p.product_origin = 1;

-- 检查所有包含 Vendor 产品的订单
SELECT 
    o.id,
    o.order_sn,
    o.vendor_id,
    o.status,
    COUNT(oi.id) AS item_count,
    SUM(CASE WHEN oi.product_origin = 1 THEN 1 ELSE 0 END) AS vendor_item_count
FROM mall_order o
JOIN mall_order_item oi ON o.id = oi.order_id
WHERE o.vendor_id IS NOT NULL
GROUP BY o.id, o.order_sn, o.vendor_id, o.status;

-- 检查订单项的 product_origin
SELECT 
    oi.order_id,
    oi.product_id,
    oi.product_origin,
    oi.origin_id,
    p.name AS product_name
FROM mall_order_item oi
LEFT JOIN mall_product p ON oi.product_id = p.product_id
WHERE oi.product_origin = 1;
```

---

## 测试执行清单

### 准备阶段
- [ ] 数据库连接成功
- [ ] 执行测试数据准备脚本
- [ ] 验证测试数据创建成功

### 认证测试
- [ ] TC-001: Vendor 登录
- [ ] TC-002: 获取 Vendor Profile

### 产品管理测试
- [ ] TC-101: 创建 Vendor 产品
- [ ] TC-102: 查看产品列表
- [ ] TC-103: 查看产品详情
- [ ] TC-104: 更新产品
- [ ] TC-105: 删除产品
- [ ] TC-106: 安全测试 - 访问其他 Vendor 产品

### 产品审批测试
- [ ] TC-201: 查看待审批产品
- [ ] TC-202: 审批通过产品
- [ ] TC-203: 审批拒绝产品

### 订单管理测试
- [ ] TC-301: 创建包含 Vendor 产品的订单
- [ ] TC-302: 查看订单列表
- [ ] TC-303: 查看订单详情
- [ ] TC-304: 接受订单
- [ ] TC-305: 拒绝订单
- [ ] TC-306: 标记发货
- [ ] TC-307: 安全测试 - 访问其他 Vendor 订单

### 混合订单测试
- [ ] TC-401: 创建混合订单

### 数据验证
- [ ] 执行数据库验证查询
- [ ] 检查数据完整性
- [ ] 检查数据隔离性

---

## 问题报告模板

如果测试失败，请记录以下信息：

```
测试用例: TC-XXX
测试时间: YYYY-MM-DD HH:MM:SS
测试环境: [开发/测试/生产]
错误信息: 
请求: 
响应: 
数据库状态: 
预期结果: 
实际结果: 
```

---

请提供数据库连接信息，我可以帮您生成更具体的测试脚本。


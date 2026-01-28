# Vendor Integration Testing Guide

## 测试前准备

### 1. 数据库迁移

确保运行以下 Flyway 迁移脚本：
- `V1.15_20251117__vendor_order_integration.sql` - 添加订单和订单项的 vendor 相关字段
- `V1.16_20251117__vendor_product_management.sql` - 创建产品审批表

### 2. 测试数据准备

#### 2.1 创建 Vendor Member 账户
```sql
INSERT INTO mall_vendor_member (username, password_hash, email, status) 
VALUES ('testvendor', '$2a$10$...', 'vendor@test.com', 1);
-- 注意：password_hash 需要使用 BCrypt 加密
```

#### 2.2 创建 Vendor Application（已审批）
```sql
-- 假设 member_id = 1, vendor_id = 1
INSERT INTO mall_vendor_application (member_id, application_id, vendor_name, status, vendor_id) 
VALUES (1, 'APP001', 'Test Vendor', 'approved', 1);
```

#### 2.3 创建 Vendor 记录
```sql
INSERT INTO mall_vendor (id, vendor_code, vendor_name, status) 
VALUES (1, 'V001', 'Test Vendor', 1);
```

## 测试场景

### 场景 1: Vendor 产品管理

#### 1.1 创建产品（需要登录）
**API**: `POST /api/mall/vendor/product`
**Headers**: 
```
Authorization: Bearer {token}
Content-Type: application/json
```

**Request Body**:
```json
{
  "productId": "VENDOR-PROD-001",
  "name": "Test Vendor Product",
  "category": "test",
  "description": "Test product description",
  "imageUrl": "https://example.com/image.jpg",
  "status": 0,
  "channel": "OS/TG"
}
```

**预期结果**:
- 返回成功，产品创建
- `product_origin = 1`
- `origin_id = vendor_id`
- `status = 0` (待审批)
- `mall_vendor_product_approval` 表中创建记录，`approval_status = 'pending_approval'`

#### 1.2 查看自己的产品列表
**API**: `GET /api/mall/vendor/product/list`
**Headers**: 
```
Authorization: Bearer {token}
```

**预期结果**:
- 返回该 vendor 的所有产品列表
- 只包含 `product_origin = 1` 且 `origin_id = vendor_id` 的产品

#### 1.3 更新产品
**API**: `PUT /api/mall/vendor/product`
**Headers**: 
```
Authorization: Bearer {token}
Content-Type: application/json
```

**Request Body**:
```json
{
  "id": 1,
  "name": "Updated Product Name",
  "description": "Updated description"
}
```

**预期结果**:
- 如果产品已审批，更新后状态变为 `pending_approval`
- 如果产品未审批，直接更新

#### 1.4 删除产品
**API**: `DELETE /api/mall/vendor/product/{id}`
**Headers**: 
```
Authorization: Bearer {token}
```

**预期结果**:
- 产品状态变为 2 (软删除)
- 只能删除自己的产品

### 场景 2: 产品审批流程（Admin）

#### 2.1 查看待审批产品
**API**: `GET /admin/mall/vendor/product/pending`
**Headers**: 
```
Authorization: Bearer {admin_token}
```

**预期结果**:
- 返回所有 `approval_status = 'pending_approval'` 的产品

#### 2.2 审批通过
**API**: `POST /admin/mall/vendor/product/approve/{productId}?notes=Approved`
**Headers**: 
```
Authorization: Bearer {admin_token}
```

**预期结果**:
- 产品状态变为 1 (激活)
- `approval_status = 'approved'`
- 记录审批人信息

#### 2.3 审批拒绝
**API**: `POST /admin/mall/vendor/product/reject/{productId}?reason=Not suitable`
**Headers**: 
```
Authorization: Bearer {admin_token}
```

**预期结果**:
- 产品状态保持 0 (未激活)
- `approval_status = 'rejected'`
- 记录拒绝原因

### 场景 3: Vendor 订单管理

#### 3.1 创建包含 Vendor 产品的订单
**API**: `POST /api/mall/order/create`
**Request Body**:
```json
{
  "items": [
    {
      "sku": "VENDOR-SKU-001",
      "quantity": 2
    }
  ],
  "memberId": 10001,
  "sourceType": 0,
  "shippingAddress": {...}
}
```

**预期结果**:
- 订单创建成功
- `mall_order.vendor_id` 设置为对应的 vendor_id
- `mall_order_item.product_origin = 1`
- `mall_order_item.origin_id = vendor_id`

#### 3.2 查看自己的订单列表
**API**: `GET /api/mall/vendor/order/list`
**Headers**: 
```
Authorization: Bearer {vendor_token}
```

**预期结果**:
- 返回该 vendor 的所有订单
- 只包含 `vendor_id = current_vendor_id` 的订单

#### 3.3 查看订单详情
**API**: `GET /api/mall/vendor/order/{orderId}`
**Headers**: 
```
Authorization: Bearer {vendor_token}
```

**预期结果**:
- 返回订单详情
- 只能查看自己的订单

#### 3.4 接受订单
**API**: `POST /api/mall/vendor/order/{orderId}/accept`
**Headers**: 
```
Authorization: Bearer {vendor_token}
```

**预期结果**:
- 订单状态变为 6 (vendor_accepted)
- 只能接受状态为 0 (pending) 的订单

#### 3.5 拒绝订单
**API**: `POST /api/mall/vendor/order/{orderId}/reject?reason=Out of stock`
**Headers**: 
```
Authorization: Bearer {vendor_token}
```

**预期结果**:
- 订单状态变为 4 (cancelled)
- 备注中包含拒绝原因
- 只能拒绝状态为 0 (pending) 的订单

#### 3.6 标记发货
**API**: `POST /api/mall/vendor/order/{orderId}/ship?trackingNumber=TRACK123`
**Headers**: 
```
Authorization: Bearer {vendor_token}
```

**预期结果**:
- 订单状态变为 3 (shipped)
- 运单号更新到 `mall_order_shipping`
- 只能对状态为 1 (paid) 或 6 (vendor_accepted) 的订单发货

## 测试检查清单

### 数据库检查

1. **检查表结构**:
```sql
-- 检查 mall_order 表是否有 vendor_id 字段
DESC mall_order;

-- 检查 mall_order_item 表是否有 product_origin 和 origin_id 字段
DESC mall_order_item;

-- 检查 mall_vendor_product_approval 表是否存在
SHOW TABLES LIKE 'mall_vendor_product_approval';
```

2. **检查数据**:
```sql
-- 检查产品审批记录
SELECT * FROM mall_vendor_product_approval;

-- 检查订单的 vendor_id
SELECT id, order_sn, vendor_id FROM mall_order WHERE vendor_id IS NOT NULL;

-- 检查订单项的 product_origin
SELECT order_id, product_id, product_origin, origin_id FROM mall_order_item WHERE product_origin = 1;
```

### 功能检查

- [ ] Vendor 可以创建产品
- [ ] Vendor 只能查看自己的产品
- [ ] Vendor 可以更新自己的产品
- [ ] Vendor 可以删除自己的产品
- [ ] Admin 可以查看待审批产品
- [ ] Admin 可以审批通过产品
- [ ] Admin 可以拒绝产品
- [ ] 创建包含 Vendor 产品的订单时，正确设置 vendor_id 和 product_origin
- [ ] Vendor 可以查看自己的订单
- [ ] Vendor 可以接受订单
- [ ] Vendor 可以拒绝订单
- [ ] Vendor 可以标记发货
- [ ] Vendor 无法访问其他 Vendor 的订单

### 安全检查

- [ ] 未登录的 Vendor 无法访问 API
- [ ] Vendor 无法访问未审批账户的 API
- [ ] Vendor 无法访问其他 Vendor 的数据
- [ ] 产品审批需要 Admin 权限

## 常见问题排查

### 1. 认证失败
- 检查 Vendor Member 是否已登录
- 检查 Session 中是否有 `vendorMemberId` 和 `vendorMemberToken`
- 检查 Authorization header 格式是否正确

### 2. Vendor ID 获取失败
- 检查 Vendor Application 状态是否为 'approved'
- 检查 Vendor Application 是否有 vendor_id
- 检查 Vendor Member 和 Vendor Application 的关联

### 3. 订单查询为空
- 检查订单的 vendor_id 是否正确设置
- 检查 OrderMapper.xml 中的 vendor_id 查询条件

### 4. 产品审批不生效
- 检查产品状态是否正确更新
- 检查审批记录是否正确创建/更新
- 检查审批人信息是否正确记录

## 测试工具

可以使用以下工具进行测试：
- Postman
- curl
- 前端页面（如果已实现）

### curl 示例

```bash
# 登录 Vendor
curl -X POST http://localhost:8080/api/mall/vendor/member/login \
  -H "Content-Type: application/json" \
  -d '{"username":"testvendor","password":"password123"}'

# 创建产品
curl -X POST http://localhost:8080/api/mall/vendor/product \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{
    "productId": "VENDOR-PROD-001",
    "name": "Test Product",
    "category": "test",
    "description": "Test description"
  }'
```


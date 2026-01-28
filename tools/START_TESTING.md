# 🚀 开始测试 - Vendor Integration

## ✅ 测试数据已准备完成！

您现在可以使用以下账号：
- **Vendor**: `testvendor` / `test123`
- **Customer**: `testcustomer` / `test123`
- **Admin**: `admin` / `admin123`

---

## 📍 方式一：Web 界面登录（最简单）

### 1. 访问 Vendor Portal 首页

在浏览器中打开：
```
http://localhost:8080/mall/static/vendor
```

### 2. 点击 "VENDOR LOGIN" 按钮

或直接访问登录页面：
```
http://localhost:8080/mall/static/vendor/login
```

### 3. 输入登录信息

- **用户名**: `testvendor`
- **密码**: `test123`

### 4. 登录后

登录成功后会跳转到状态页面，您可以看到：
- 申请状态
- 面试信息
- 日历视图

---

## 🧪 方式二：运行自动化测试（推荐）

直接运行测试脚本，会自动测试所有功能：

```bash
./test_vendor_integration.sh
```

脚本会自动：
1. ✅ Vendor 登录
2. ✅ 创建产品
3. ✅ 查看产品列表
4. ✅ 查看订单列表
5. ✅ 执行订单操作

---

## 🔧 方式三：使用 API 测试（最全面）

### 步骤 1: Vendor 登录

打开浏览器开发者工具（F12）→ Console，执行：

```javascript
// 登录
fetch('http://localhost:8080/api/mall/vendor/member/login', {
  method: 'POST',
  headers: {'Content-Type': 'application/json'},
  credentials: 'include',
  body: JSON.stringify({username: 'testvendor', password: 'test123'})
})
.then(r => r.json())
.then(data => {
  console.log('✅ 登录成功:', data);
  window.vendorToken = data.data?.token;
});
```

### 步骤 2: 创建产品

```javascript
fetch('http://localhost:8080/api/mall/vendor/product', {
  method: 'POST',
  headers: {'Content-Type': 'application/json'},
  credentials: 'include',
  body: JSON.stringify({
    productId: 'VENDOR-PROD-' + Date.now(),
    name: 'My Test Product',
    category: 'test',
    description: 'This is a test product',
    status: 0,
    channel: 'OS/TG'
  })
})
.then(r => r.json())
.then(data => console.log('✅ 创建产品:', data));
```

### 步骤 3: 查看产品列表

```javascript
fetch('http://localhost:8080/api/mall/vendor/product/list', {
  credentials: 'include'
})
.then(r => r.json())
.then(data => {
  console.log('✅ 我的产品:', data);
  console.table(data.data);
});
```

### 步骤 4: 查看订单列表

```javascript
fetch('http://localhost:8080/api/mall/vendor/order/list', {
  credentials: 'include'
})
.then(r => r.json())
.then(data => {
  console.log('✅ 我的订单:', data);
  console.table(data.data);
});
```

---

## 📊 方式四：使用 Postman 测试

### 导入测试集合

1. 打开 Postman
2. 创建新的 Collection: "Vendor Integration Tests"
3. 设置环境变量：
   - `base_url`: `http://localhost:8080/api`
   - `vendor_username`: `testvendor`
   - `vendor_password`: `test123`

### 测试请求

#### 1. Vendor 登录
- **Method**: POST
- **URL**: `{{base_url}}/mall/vendor/member/login`
- **Body** (form-urlencoded):
  - username: `{{vendor_username}}`
  - password: `{{vendor_password}}`

#### 2. 创建产品
- **Method**: POST
- **URL**: `{{base_url}}/mall/vendor/product`
- **Headers**: 
  - `Content-Type: application/json`
- **Body** (raw JSON):
```json
{
  "productId": "VENDOR-PROD-001",
  "name": "Test Product",
  "category": "test",
  "description": "Test description",
  "status": 0,
  "channel": "OS/TG"
}
```

#### 3. 查看产品列表
- **Method**: GET
- **URL**: `{{base_url}}/mall/vendor/product/list`

#### 4. 查看订单列表
- **Method**: GET
- **URL**: `{{base_url}}/mall/vendor/order/list`

---

## 🎯 推荐测试流程

### 快速测试（5分钟）

1. **运行自动化脚本**
   ```bash
   ./test_vendor_integration.sh
   ```

2. **查看结果**
   - 脚本会显示每个测试的结果
   - 绿色 ✓ 表示通过
   - 红色 ✗ 表示失败

### 完整测试（15分钟）

1. **Web 界面测试**
   - 访问 Vendor Portal
   - 登录并查看状态

2. **API 功能测试**
   - 使用浏览器 Console 或 Postman
   - 测试所有 API 端点

3. **数据库验证**
   ```sql
   -- 检查产品
   SELECT * FROM mall_product WHERE product_origin = 1;
   
   -- 检查订单
   SELECT * FROM mall_order WHERE vendor_id IS NOT NULL;
   ```

---

## 🔍 验证检查点

### ✅ 登录验证
- [ ] 可以成功登录
- [ ] 登录后跳转到状态页面
- [ ] Session 正确保存

### ✅ 产品管理验证
- [ ] 可以创建产品
- [ ] 产品状态为 `pending_approval`
- [ ] 只能看到自己的产品
- [ ] 产品审批后状态变为 `approved`

### ✅ 订单管理验证
- [ ] 可以查看订单列表
- [ ] 只能看到自己的订单
- [ ] 可以接受订单
- [ ] 可以标记发货

---

## 🐛 如果遇到问题

### 问题 1: 无法登录
- 检查应用是否运行在 8080 端口
- 检查用户名密码是否正确
- 检查 Vendor Application 是否已审批

### 问题 2: API 返回 401/403
- 确保已登录（Session 有效）
- 检查 Vendor Application 状态为 `approved`
- 检查 Vendor 是否有对应的 `vendor_id`

### 问题 3: 看不到数据
- 检查数据库中的数据是否存在
- 检查 `vendor_id` 是否正确关联
- 检查产品/订单的 `origin_id` 是否正确

---

## 📞 需要帮助？

如果测试过程中遇到任何问题，请：
1. 查看 `HOW_TO_TEST.md` 获取详细说明
2. 查看 `TEST_CASES_VENDOR_INTEGRATION.md` 获取完整测试用例
3. 检查应用日志查看错误信息

---

**现在就开始测试吧！** 🎉

建议顺序：
1. 先运行 `./test_vendor_integration.sh` 快速验证
2. 然后访问 Web 界面体验完整流程
3. 最后使用 API 进行详细测试


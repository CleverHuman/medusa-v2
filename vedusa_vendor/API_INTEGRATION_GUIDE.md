# Vendor Portal - API Integration Guide

## 🎉 API 集成完成！

静态页面已成功连接到后端 API，现在可以真实提交申请和查询状态。

---

## 📁 修改的文件

```
vedusa_vendor/
├── api-config.js           ✅ 新建 - API配置和请求工具
├── application.html        ✅ 修改 - 引入api-config.js
├── application.js          ✅ 修改 - 调用真实API提交
├── status.html             ✅ 修改 - 引入api-config.js
└── status.js               ✅ 修改 - 调用真实API查询
```

---

## 🔧 API 配置

### api-config.js 功能

**自动环境检测：**
- `localhost` / `127.0.0.1` → 开发环境
- 其他域名 → 生产环境

**配置内容：**
```javascript
// 开发环境
baseURL: 'http://localhost:8080'

// 生产环境
baseURL: 'https://your-production-domain.com'
```

**提供的API方法：**
```javascript
API.submitVendorApplication(data)    // 提交申请
API.getApplicationStatus(id)          // 查询状态
API.getApplicationDetail(id)          // 查询详情
```

### 修改配置

编辑 `api-config.js` 第 4-12 行：

```javascript
const API_CONFIG = {
    development: {
        baseURL: 'http://localhost:8080',  // 修改为你的后端地址
        timeout: 10000
    },
    production: {
        baseURL: 'https://your-domain.com',  // 修改为生产环境地址
        timeout: 10000
    }
}
```

---

## 🚀 使用方法

### 1. 启动后端服务

```bash
cd /Users/jc/Documents/workshop/medusa-developOS3
mvn clean install
# 启动 Spring Boot 应用（端口8080）
```

### 2. 启动前端页面

**方式A：本地服务器（推荐）**
```bash
cd /Users/jc/Documents/workshop/medusa-developOS3/vedusa_vendor
python3 -m http.server 8000
```

访问：
- 申请表单：`http://localhost:8000/application.html`
- 状态查询：`http://localhost:8000/status.html`

**方式B：直接打开文件**
```bash
open /Users/jc/Documents/workshop/medusa-developOS3/vedusa_vendor/application.html
```

**注意：** 使用本地服务器可以避免 CORS 问题。

---

## 📋 功能说明

### Application.html - 申请表单

#### 提交流程

1. **用户填写表单** → 点击 "Submit Application"
2. **前端验证** → 检查必填字段和格式
3. **调用API** → `API.submitVendorApplication(data)`
4. **后端处理** → `ApiVendorApplicationController.submit()`
5. **返回结果** → Application ID
6. **显示成功** → Success Modal

#### API 请求示例

```javascript
// 准备数据
const data = {
    vendorName: "Test Vendor",
    pgpSignatureUrl: "-----BEGIN PGP PUBLIC KEY BLOCK-----...",
    location: "New South Wales",
    productCategories: "[\"Electronics\",\"Home & Garden\"]",
    stockVolume: "medium",
    offlineDelivery: 0,
    primaryTelegram: "@testvendor",
    primaryEmail: "test@vendor.com",
    status: "pending",
    reviewProgress: 0
}

// 提交到后端
const response = await API.submitVendorApplication(data)
// 返回：{ code: 200, data: { id: 123, applicationId: "VA1234567890" }}
```

#### 成功响应

```json
{
    "code": 200,
    "msg": "Application submitted successfully",
    "data": {
        "id": 123,
        "applicationId": "VA1234567890",
        "vendorName": "Test Vendor",
        "status": "pending",
        ...
    }
}
```

#### 错误处理

- **网络错误** → 显示Toast："Failed to submit application"
- **验证失败** → 显示错误信息，按钮恢复
- **后端错误** → 显示返回的错误消息

### Status.html - 状态查询

#### 查询流程

1. **输入 Application ID** → 点击 "Search Application"
2. **调用API** → `API.getApplicationStatus(id)`
3. **后端查询** → `ApiVendorApplicationController.getStatus()`
4. **数据转换** → `convertApiDataToDisplayFormat()`
5. **显示详情** → 详情卡片 + 时间线

#### API 请求示例

```javascript
// 查询状态
const response = await API.getApplicationStatus('VA1234567890')
// 返回申请详细信息
```

#### 成功响应

```json
{
    "code": 200,
    "data": {
        "id": 123,
        "applicationId": "VA1234567890",
        "vendorName": "Test Vendor",
        "status": "under_review",
        "reviewProgress": 45,
        "reviewStage": "Product Range Review",
        "location": "New South Wales",
        "productCategories": "[\"Electronics\"]",
        "createTime": "2024-01-15 09:15:00",
        "updateTime": "2024-01-16 14:20:00"
    }
}
```

#### Fallback 机制

如果API调用失败，会：
1. 检查是否有对应的 mock 数据
2. 如果有 → 显示 mock 数据 + 警告提示
3. 如果没有 → 显示错误提示

---

## 🧪 测试步骤

### 完整测试流程（10分钟）

#### 1. 准备工作

```bash
# 确保数据库已初始化
mysql -u root -p medusa < sql/create_vendor_module.sql

# 启动后端（端口8080）
cd /Users/jc/Documents/workshop/medusa-developOS3
mvn spring-boot:run

# 新开终端，启动前端
cd /Users/jc/Documents/workshop/medusa-developOS3/vedusa_vendor
python3 -m http.server 8000
```

#### 2. 测试提交申请

**访问：** `http://localhost:8000/application.html`

**填写表单：**
- Step 1: Vendor Name = "API Test Vendor"
- Step 1: PGP Public Key = 粘贴测试公钥
- Step 1: Location = "Victoria"
- Step 2: Product Categories = 选择"Electronics"
- Step 2: Stock Volume = "medium"
- Step 3: Primary Contact = Telegram "@api_test"
- Step 3: Secondary Contact = Email "test@api.com"
- Step 4: 勾选同意条款
- 点击 "Submit Application"

**预期结果：**
- ✅ 显示加载状态："Submitting..."
- ✅ 成功后显示 Application ID（如："VA1730123456"）
- ✅ 浏览器控制台显示：
  ```
  API Config loaded: development http://localhost:8080
  ```

**如果出错：**
- 检查控制台错误信息
- 检查后端是否运行（访问 `http://localhost:8080`）
- 检查CORS配置

#### 3. 测试状态查询

**访问：** `http://localhost:8000/status.html`

**操作：**
1. 输入刚获得的 Application ID
2. 点击 "Search Application"

**预期结果：**
- ✅ 显示申请详情（Vendor Name, Location等）
- ✅ 显示状态："Pending"
- ✅ 显示时间线
- ✅ 控制台显示API调用日志

**如果出错：**
- 检查Application ID是否正确
- 检查后端API是否可访问
- 查看控制台错误信息

#### 4. 测试后台审核

**登录后台管理：**
1. 进入：Vendor Management → Vendor Application
2. 找到刚提交的 "API Test Vendor"
3. 点击 "Approve" 批准申请
4. 输入审核意见："Test approval"

**返回状态查询页面：**
1. 刷新或重新搜索
2. 状态应变为 "Approved"
3. 进度变为 100%
4. 时间线显示 "Application Approved"

---

## 🔌 API 端点详情

### 后端接口

| 端点 | 方法 | 认证 | 说明 |
|------|------|------|------|
| `/api/mall/vendor/application/submit` | POST | ❌ 公开 | 提交申请 |
| `/api/mall/vendor/application/status/{id}` | GET | ❌ 公开 | 查询状态 |
| `/api/mall/vendor/application/detail/{id}` | GET | ❌ 公开 | 查询详情 |

### 请求格式

#### 提交申请

**URL:** `POST http://localhost:8080/api/mall/vendor/application/submit`

**Headers:**
```
Content-Type: application/json
```

**Body:**
```json
{
    "vendorName": "Test Vendor",
    "hasMarketExperience": 1,
    "existingMarkets": "AlphaBay",
    "experienceYears": 3,
    "pgpSignatureUrl": "-----BEGIN PGP PUBLIC KEY BLOCK-----...",
    "location": "New South Wales",
    "productCategories": "[\"Electronics\",\"Home & Garden\"]",
    "stockVolume": "medium",
    "offlineDelivery": 0,
    "productDescription": "High quality products",
    "primaryTelegram": "@vendor",
    "primaryEmail": "vendor@example.com",
    "secondarySignal": "+61412345678",
    "status": "pending",
    "reviewProgress": 0
}
```

#### 查询状态

**URL:** `GET http://localhost:8080/api/mall/vendor/application/status/VA1234567890`

**Headers:**
```
Content-Type: application/json
```

**Response:**
```json
{
    "code": 200,
    "msg": "success",
    "data": {
        "id": 123,
        "applicationId": "VA1234567890",
        "vendorName": "Test Vendor",
        "status": "pending",
        "reviewProgress": 0,
        "location": "New South Wales",
        ...
    }
}
```

---

## 🐛 常见问题

### Q1: 提交时报 CORS 错误

**错误信息：**
```
Access to fetch at 'http://localhost:8080/api/mall/vendor/application/submit' 
from origin 'http://localhost:8000' has been blocked by CORS policy
```

**解决方案：**

在后端添加 CORS 配置。创建或修改配置类：

```java
// CorsConfig.java
@Configuration
public class CorsConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/mall/vendor/application/**")
                .allowedOrigins("http://localhost:8000", "http://127.0.0.1:8000")
                .allowedMethods("GET", "POST", "PUT", "DELETE")
                .allowedHeaders("*")
                .allowCredentials(false)
                .maxAge(3600);
    }
}
```

### Q2: 提交后显示 "Failed to submit application"

**可能原因：**
1. 后端服务未启动
2. 端口不对（检查是否8080）
3. API路径错误
4. 数据库未初始化

**调试步骤：**
```bash
# 检查后端是否运行
curl http://localhost:8080/api/mall/vendor/application/status/test

# 查看后端日志
tail -f medusa-admin/logs/sys-info.log
```

### Q3: 状态查询返回 "Application not found"

**可能原因：**
1. Application ID 输入错误
2. 数据库中没有该记录
3. API 路径不对

**验证：**
```sql
-- 直接查询数据库
SELECT * FROM mall_vendor_application 
WHERE application_id = 'VA1234567890';
```

### Q4: Mock 数据和真实数据混合显示

**这是正常的！** 设计了 fallback 机制：
- API成功 → 显示真实数据
- API失败 → 尝试显示mock数据（仅用于演示）
- 会显示警告："Demo data loaded (API unavailable)"

---

## 🔐 安全配置

### 后端 CORS 设置

**推荐配置：**

```java
// SecurityConfig.java 或 WebConfig.java
@Bean
public CorsFilter corsFilter() {
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    CorsConfiguration config = new CorsConfiguration();
    
    // 允许的域名
    config.addAllowedOrigin("http://localhost:8000");
    config.addAllowedOrigin("http://localhost:8080");
    config.addAllowedOrigin("https://your-production-domain.com");
    
    // 允许的方法
    config.addAllowedMethod("*");
    
    // 允许的头部
    config.addAllowedHeader("*");
    
    // 是否允许凭证
    config.setAllowCredentials(false);
    
    source.registerCorsConfiguration("/api/mall/vendor/application/**", config);
    return new CorsFilter(source);
}
```

### Spring Boot application.yml

```yaml
# CORS配置（可选）
spring:
  web:
    cors:
      allowed-origins:
        - http://localhost:8000
        - http://localhost:8080
      allowed-methods:
        - GET
        - POST
      allowed-headers: "*"
```

---

## 📊 数据流程

### 提交申请流程

```
用户填写表单
    ↓
点击 Submit Application
    ↓
application.js: handleFormSubmit()
    ↓
application.js: prepareSubmissionData()
    ↓
api-config.js: API.submitVendorApplication(data)
    ↓
HTTP POST → http://localhost:8080/api/mall/vendor/application/submit
    ↓
ApiVendorApplicationController.submit()
    ↓
VendorApplicationService.insertVendorApplication()
    ↓
数据库 (mall_vendor_application)
    ↓
返回 Application ID
    ↓
显示 Success Modal
```

### 查询状态流程

```
用户输入 Application ID
    ↓
点击 Search Application
    ↓
status.js: searchApplication()
    ↓
api-config.js: API.getApplicationStatus(id)
    ↓
HTTP GET → http://localhost:8080/api/mall/vendor/application/status/{id}
    ↓
ApiVendorApplicationController.getStatus()
    ↓
VendorApplicationService.selectByApplicationId()
    ↓
数据库查询
    ↓
返回申请数据
    ↓
status.js: convertApiDataToDisplayFormat()
    ↓
显示详情、时间线、进度
```

---

## 🧪 测试用例

### 测试用例 1: 成功提交申请

**前置条件：**
- 后端服务运行正常
- 数据库已初始化

**步骤：**
1. 访问 `http://localhost:8000/application.html`
2. 完整填写所有步骤
3. 提交申请

**预期结果：**
- ✅ 显示成功对话框
- ✅ 获得 Application ID
- ✅ 数据库中有新记录
- ✅ 控制台无错误

**SQL 验证：**
```sql
SELECT * FROM mall_vendor_application 
ORDER BY create_time DESC 
LIMIT 1;
```

### 测试用例 2: 查询已存在的申请

**前置条件：**
- 数据库中有测试数据

**步骤：**
1. 执行演示数据：`mysql -u root -p medusa < sql/vendor_demo_data.sql`
2. 访问 `http://localhost:8000/status.html`
3. 输入：`VA001DEMO`
4. 点击 Search

**预期结果：**
- ✅ 显示 "Demo Electronics Supplier"
- ✅ 状态为 "Pending"
- ✅ 显示时间线
- ✅ 显示基本信息

### 测试用例 3: 查询不存在的申请

**步骤：**
1. 输入：`INVALID-ID-123`
2. 点击 Search

**预期结果：**
- ✅ 显示错误Toast
- ✅ 不显示详情区域
- ✅ 示例申请卡片仍然显示

### 测试用例 4: 后端未启动

**步骤：**
1. 停止后端服务
2. 提交申请

**预期结果：**
- ✅ 显示网络错误
- ✅ 控制台显示错误日志
- ✅ 按钮恢复可点击

---

## 🔍 调试技巧

### 浏览器控制台调试

打开控制台（F12），查看日志：

```javascript
// API 配置加载
API Config loaded: development http://localhost:8080

// 提交申请
POST http://localhost:8080/api/mall/vendor/application/submit
{vendorName: "Test", ...}

// 查询状态
GET http://localhost:8080/api/mall/vendor/application/status/VA123
Response: {code: 200, data: {...}}
```

### Network 面板检查

1. 打开 DevTools → Network 标签
2. 提交表单或搜索
3. 查看 API 请求：
   - Request URL
   - Request Method
   - Status Code (应该是 200)
   - Response

### 后端日志检查

```bash
# 查看实时日志
tail -f medusa-admin/logs/sys-info.log

# 查看错误日志
tail -f medusa-admin/logs/sys-error.log
```

---

## 💡 高级配置

### 自定义 API 超时

修改 `api-config.js`：

```javascript
const API_CONFIG = {
    development: {
        baseURL: 'http://localhost:8080',
        timeout: 20000  // 改为20秒
    }
}
```

### 添加请求拦截器

在 `api-config.js` 中添加：

```javascript
// Request interceptor
API.beforeRequest = function(endpoint, data) {
    console.log('API Request:', endpoint, data);
    // 可以添加token等
    return data;
}

// Response interceptor  
API.afterResponse = function(response) {
    console.log('API Response:', response);
    return response;
}
```

### 添加重试机制

```javascript
API.postWithRetry = async function(endpoint, data, retries = 3) {
    for (let i = 0; i < retries; i++) {
        try {
            return await this.post(endpoint, data);
        } catch (error) {
            if (i === retries - 1) throw error;
            await new Promise(r => setTimeout(r, 1000 * (i + 1)));
        }
    }
}
```

---

## 📝 字段映射

### 前端表单 → 后端API

| 前端字段名 | 后端字段名 | 类型 | 转换 |
|-----------|-----------|------|------|
| vendorName | vendorName | String | 直接 |
| hasMarketExperience | hasMarketExperience | Integer | 'yes'→1, 其他→0 |
| existingMarkets | existingMarkets | String | 直接 |
| experienceYears | experienceYears | Integer | parseInt() |
| pgpSignature | pgpSignatureUrl | String | 直接（存储完整公钥） |
| location | location | String | 直接 |
| productCategories | productCategories | String | JSON.stringify(array) |
| stockVolume | stockVolume | String | 直接 |
| offlineDelivery | offlineDelivery | Integer | 'yes'→1, 其他→0 |
| telegramId | primaryTelegram | String | 直接 |
| emailAddress | primaryEmail | String | 直接 |
| ... | ... | ... | ... |

---

## ✅ 完成检查清单

部署前检查：
- ✅ `api-config.js` 已创建
- ✅ `application.html` 引入了 api-config.js
- ✅ `application.js` 使用真实API
- ✅ `status.html` 引入了 api-config.js
- ✅ `status.js` 使用真实API
- ✅ 后端Controller已创建
- ✅ 数据库表已初始化
- ✅ CORS已配置

---

## 🎯 生产环境部署

### 1. 修改API配置

编辑 `api-config.js`：

```javascript
const API_CONFIG = {
    production: {
        baseURL: 'https://api.your-domain.com',  // 修改为实际域名
        timeout: 10000
    }
}
```

### 2. 部署静态文件

```bash
# 上传到Web服务器
scp -r vedusa_vendor/* user@server:/var/www/html/vendor-portal/
```

### 3. Nginx 配置

```nginx
server {
    listen 80;
    server_name vendor.your-domain.com;
    
    # 静态文件
    location / {
        root /var/www/html/vendor-portal;
        index index.html application.html;
        try_files $uri $uri/ =404;
    }
    
    # 后端API代理（可选）
    location /api/ {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    }
}
```

---

## 🎊 完成！

静态页面现在已完全连接到后端API：

- ✅ 真实的数据提交
- ✅ 真实的状态查询
- ✅ 数据库持久化
- ✅ 错误处理完善
- ✅ Fallback到mock数据
- ✅ 生产环境就绪

**立即测试：**
```bash
# 启动服务
python3 -m http.server 8000

# 访问
open http://localhost:8000/application.html
```

享受真实的API集成吧！🚀


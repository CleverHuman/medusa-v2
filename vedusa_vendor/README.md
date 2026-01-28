# Vendor Application Portal - 使用指南

## 🎯 概述

这是一个完整的供应商申请门户，包含申请表单和状态查询功能，已连接到后端API实现真实的数据提交和查询。

---

## 🚀 快速开始（3步）

### 1. 确保后端服务运行

```bash
cd /Users/jc/Documents/workshop/medusa-developOS3
mvn spring-boot:run
# 或者直接运行已编译的jar
```

后端应在端口 **8080** 运行。

### 2. 启动前端服务

```bash
cd /Users/jc/Documents/workshop/medusa-developOS3/vedusa_vendor
python3 -m http.server 8000
```

### 3. 访问页面

**申请表单：**
```
http://localhost:8000/application.html
```

**状态查询：**
```
http://localhost:8000/status.html
```

**主页（可选）：**
```
http://localhost:8000/index.html
```

---

## 📁 文件说明

| 文件 | 说明 | 状态 |
|------|------|------|
| **application.html** | 申请表单页面 | ✅ 已连接API |
| **application.js** | 表单逻辑 | ✅ 已连接API |
| **status.html** | 状态查询页面 | ✅ 已连接API |
| **status.js** | 状态查询逻辑 | ✅ 已连接API |
| **api-config.js** | API配置和请求工具 | ✅ 新建 |
| **admin.html** | 管理员页面（独立） | ⚠️ 仅演示用 |
| **index.html** | 主页/欢迎页 | ℹ️ 静态页面 |
| design.md, outline.md | 设计文档 | 📝 参考文档 |
| CHANGELOG.md | 变更日志 | 📝 完整记录 |
| API_INTEGRATION_GUIDE.md | API集成指南 | 📖 本指南 |

---

## 🎨 功能特性

### Application.html - 申请表单

#### 多步骤表单（4步）

1. **Basic Information** ✅
   - Vendor Name（供应商名称）
   - PGP Public Key（PGP公钥 - textarea输入）
   - Location（澳大利亚州）
   - Market Experience（市场经验 - 可选）

2. **Product Details** ✅
   - Product Categories（产品类别 - 多选）
   - Stock Volume（库存规模）
   - Offline Delivery（是否线下交付）
   - Product Description（产品描述）

3. **Contact Information** ✅
   - Primary Contact（主要联系方式）
   - Secondary Contact（次要联系方式）
   - 支持：Telegram, Signal, Jabber, Email

4. **Review & Submit** ✅
   - 信息摘要
   - 同意条款复选框
   - 提交按钮

#### 核心功能

- ✅ **实时验证** - 字段失焦时验证
- ✅ **PGP格式检查** - 验证BEGIN/END标记和长度
- ✅ **草稿保存** - localStorage自动保存
- ✅ **API提交** - 真实后端API调用
- ✅ **错误处理** - 完善的错误提示
- ✅ **加载状态** - 提交时显示Loading

### Status.html - 状态查询

#### 功能

- ✅ **Application ID 搜索**
- ✅ **实时状态查询** - 调用后端API
- ✅ **详细信息展示** - 供应商信息、地区、产品类别
- ✅ **处理进度** - 队列位置、预计时间、审核人员
- ✅ **时间线展示** - 提交→接收→审核→批准/拒绝
- ✅ **审核详情** - 评分、优势、劣势、建议
- ✅ **示例申请** - 点击可查看Demo数据
- ✅ **Fallback机制** - API失败时使用Mock数据

---

## 🔌 API 配置

### 环境自动检测

`api-config.js` 会自动检测环境：

- **开发环境** (localhost) → `http://localhost:8080`
- **生产环境** (其他域名) → `https://your-production-domain.com`

### 修改API地址

编辑 `api-config.js` 第 4-12 行：

```javascript
const API_CONFIG = {
    development: {
        baseURL: 'http://localhost:8080',  // ← 修改这里
        timeout: 10000
    },
    production: {
        baseURL: 'https://api.your-domain.com',  // ← 修改这里
        timeout: 10000
    }
}
```

### 使用的API端点

| API | URL | 说明 |
|-----|-----|------|
| 提交申请 | `POST /api/mall/vendor/application/submit` | 提交新申请 |
| 查询状态 | `GET /api/mall/vendor/application/status/{id}` | 按ID查询 |
| 查询详情 | `GET /api/mall/vendor/application/detail/{id}` | 获取详细信息 |

---

## 🧪 完整测试流程

### 1. 提交申请测试（5分钟）

```bash
# 启动服务
cd vedusa_vendor && python3 -m http.server 8000
```

**操作步骤：**
1. 访问：`http://localhost:8000/application.html`
2. 填写表单各步骤
3. 提交申请
4. 记下 Application ID（如：VA1730123456）

**验证：**
```sql
-- 查询数据库验证
SELECT * FROM mall_vendor_application 
WHERE application_id LIKE 'VA%' 
ORDER BY create_time DESC 
LIMIT 1;
```

### 2. 状态查询测试（2分钟）

**操作步骤：**
1. 访问：`http://localhost:8000/status.html`
2. 输入刚获得的 Application ID
3. 点击 Search

**验证：**
- 应显示刚提交的申请信息
- 状态为 "Pending"
- 时间线显示 "Application Submitted"

### 3. 后台审核测试（3分钟）

**操作步骤：**
1. 登录后台管理系统
2. 进入：Vendor Management → Vendor Application
3. 找到刚提交的申请
4. 点击 "Approve" 批准

**返回状态页面：**
1. 刷新或重新搜索
2. 状态应变为 "Approved"
3. 时间线添加 "Application Approved"

---

## 🐛 故障排查

### 问题1: CORS错误

**现象：**
```
Access to fetch ... has been blocked by CORS policy
```

**解决：**
在后端添加CORS配置（参考 `API_INTEGRATION_GUIDE.md`）

### 问题2: 提交失败

**现象：** 显示 "Failed to submit application"

**检查：**
```bash
# 1. 后端是否运行
curl http://localhost:8080/api/mall/vendor/application/status/test

# 2. 查看控制台错误
打开浏览器F12查看Console

# 3. 查看Network标签
检查请求URL和响应
```

### 问题3: 状态查询找不到

**原因：**
- Application ID错误
- 数据库中无记录
- API路径不对

**验证：**
```sql
SELECT application_id FROM mall_vendor_application;
```

### 问题4: 后端返回403/401

**原因：** API需要认证但没有配置为公开

**解决：** 确认 `ApiVendorApplicationController` 的mapping是 `/api/mall/vendor/application`（公开路径）

---

## 📞 支持

### 文档索引

1. **README.md** - 本文件（快速开始）
2. **API_INTEGRATION_GUIDE.md** - API集成详细文档
3. **CHANGELOG.md** - 所有变更记录
4. **设计文档**：
   - design.md - 设计风格
   - outline.md - 项目概述
   - interaction.md - 交互设计

### 相关文档

- 后台管理使用：`sql/VENDOR_MODULE_USER_GUIDE_CN.md`
- 测试指南：`sql/VENDOR_TESTING_GUIDE_CN.md`
- Vue集成：`medusa-mall-vue/VENDOR_INTEGRATION_GUIDE.md`

---

## 🎊 总结

### 现在可以做什么

1. ✅ **真实提交申请** - 数据保存到数据库
2. ✅ **实时查询状态** - 从数据库获取最新状态
3. ✅ **后台审核** - 管理员可以批准/拒绝
4. ✅ **状态同步** - 前端立即反映后端变化
5. ✅ **草稿保存** - 防止数据丢失
6. ✅ **错误处理** - 友好的错误提示

### 架构优势

- ✅ 前后端分离
- ✅ 真实API集成
- ✅ 数据持久化
- ✅ 支持生产部署
- ✅ 良好的用户体验

**开始使用吧！** 🚀

---

**上次更新：** 2025-11-03
**版本：** v1.0 (API Integrated)


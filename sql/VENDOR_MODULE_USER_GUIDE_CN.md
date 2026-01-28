# Vendor 模块使用指南

## 📋 目录
1. [初始化设置](#初始化设置)
2. [菜单配置](#菜单配置)
3. [供应商申请管理](#供应商申请管理)
4. [供应商列表管理](#供应商列表管理)
5. [产品关联](#产品关联)
6. [常见操作示例](#常见操作示例)

---

## 初始化设置

### 步骤 1: 执行数据库脚本

```bash
cd /Users/jc/Documents/workshop/medusa-developOS3/sql
mysql -u root -p medusa < create_vendor_module.sql
```

输入数据库密码后，脚本将创建以下表：
- `mall_vendor_application` - 供应商申请表
- `mall_vendor` - 供应商信息表  
- `mall_vendor_review_history` - 审核历史表

**验证表是否创建成功：**
```sql
USE medusa;
SHOW TABLES LIKE 'mall_vendor%';
```

应该看到 3 个表。

### 步骤 2: 重启后端服务

```bash
cd /Users/jc/Documents/workshop/medusa-developOS3
mvn clean install
# 然后重启你的 Spring Boot 应用
```

### 步骤 3: 访问前端

如果前端正在运行，刷新浏览器即可。如果需要重新构建：

```bash
cd /Users/jc/Documents/workshop/medusa-developOS3/medusa-admin-vue
npm run dev  # 开发模式
# 或
npm run build:prod  # 生产模式
```

---

## 菜单配置

### 方法 1: 通过管理后台配置（推荐）

1. **登录系统**
   - 访问后台管理系统
   - 使用管理员账号登录

2. **添加主菜单**
   - 进入：系统管理 > 菜单管理
   - 点击"新增"按钮
   - 填写以下信息：
     ```
     菜单名称: Vendor Management
     父菜单: Mall (选择你的 Mall 菜单)
     显示排序: 6
     路由地址: vendor
     菜单图标: peoples
     菜单类型: 目录
     菜单状态: 正常
     ```
   - 点击"确定"

3. **添加"供应商申请"子菜单**
   - 点击"新增"
   - 填写信息：
     ```
     菜单名称: Vendor Application
     父菜单: Vendor Management (刚创建的菜单)
     显示排序: 1
     路由地址: application
     组件路径: mall/vendor/application/index
     菜单图标: form
     菜单类型: 菜单
     权限标识: mall:vendor:application:list
     菜单状态: 正常
     ```

4. **添加"供应商列表"子菜单**
   - 点击"新增"
   - 填写信息：
     ```
     菜单名称: Vendor List
     父菜单: Vendor Management
     显示排序: 2
     路由地址: list
     组件路径: mall/vendor/list/index
     菜单图标: peoples
     菜单类型: 菜单
     权限标识: mall:vendor:list
     菜单状态: 正常
     ```

5. **添加按钮权限**
   
   为"Vendor Application"菜单添加按钮：
   - 查询: `mall:vendor:application:query`
   - 新增: `mall:vendor:application:add`
   - 修改: `mall:vendor:application:edit`
   - 删除: `mall:vendor:application:remove`
   - 批准: `mall:vendor:application:approve`
   - 拒绝: `mall:vendor:application:reject`
   - 导出: `mall:vendor:application:export`

   为"Vendor List"菜单添加按钮：
   - 查询: `mall:vendor:query`
   - 新增: `mall:vendor:add`
   - 修改: `mall:vendor:edit`
   - 删除: `mall:vendor:remove`
   - 导出: `mall:vendor:export`

6. **分配权限给角色**
   - 进入：系统管理 > 角色管理
   - 编辑需要访问 vendor 模块的角色
   - 在权限分配树中，勾选"Vendor Management"及其子项
   - 点击"确定"保存

### 方法 2: 直接执行 SQL（快速方式）

```sql
-- 假设你的 Mall 菜单 ID 是 2000（需要根据实际情况调整）
SET @mall_menu_id = 2000;

-- 1. 创建 Vendor Management 主菜单
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time) 
VALUES ('Vendor Management', @mall_menu_id, 6, 'vendor', NULL, 1, 0, 'M', '0', '0', NULL, 'peoples', 'admin', NOW());

SET @vendor_menu_id = LAST_INSERT_ID();

-- 2. 创建 Vendor Application 子菜单
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time) 
VALUES ('Vendor Application', @vendor_menu_id, 1, 'application', 'mall/vendor/application/index', 1, 0, 'C', '0', '0', 'mall:vendor:application:list', 'form', 'admin', NOW());

SET @app_menu_id = LAST_INSERT_ID();

-- 3. 创建 Vendor List 子菜单
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time) 
VALUES ('Vendor List', @vendor_menu_id, 2, 'list', 'mall/vendor/list/index', 1, 0, 'C', '0', '0', 'mall:vendor:list', 'peoples', 'admin', NOW());

SET @list_menu_id = LAST_INSERT_ID();

-- 4. 添加 Vendor Application 按钮权限
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time) VALUES
('Query Application', @app_menu_id, 1, '#', '', 1, 0, 'F', '0', '0', 'mall:vendor:application:query', '#', 'admin', NOW()),
('Add Application', @app_menu_id, 2, '#', '', 1, 0, 'F', '0', '0', 'mall:vendor:application:add', '#', 'admin', NOW()),
('Edit Application', @app_menu_id, 3, '#', '', 1, 0, 'F', '0', '0', 'mall:vendor:application:edit', '#', 'admin', NOW()),
('Delete Application', @app_menu_id, 4, '#', '', 1, 0, 'F', '0', '0', 'mall:vendor:application:remove', '#', 'admin', NOW()),
('Approve Application', @app_menu_id, 5, '#', '', 1, 0, 'F', '0', '0', 'mall:vendor:application:approve', '#', 'admin', NOW()),
('Reject Application', @app_menu_id, 6, '#', '', 1, 0, 'F', '0', '0', 'mall:vendor:application:reject', '#', 'admin', NOW()),
('Export Application', @app_menu_id, 7, '#', '', 1, 0, 'F', '0', '0', 'mall:vendor:application:export', '#', 'admin', NOW());

-- 5. 添加 Vendor List 按钮权限
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time) VALUES
('Query Vendor', @list_menu_id, 1, '#', '', 1, 0, 'F', '0', '0', 'mall:vendor:query', '#', 'admin', NOW()),
('Add Vendor', @list_menu_id, 2, '#', '', 1, 0, 'F', '0', '0', 'mall:vendor:add', '#', 'admin', NOW()),
('Edit Vendor', @list_menu_id, 3, '#', '', 1, 0, 'F', '0', '0', 'mall:vendor:edit', '#', 'admin', NOW()),
('Delete Vendor', @list_menu_id, 4, '#', '', 1, 0, 'F', '0', '0', 'mall:vendor:remove', '#', 'admin', NOW()),
('Export Vendor', @list_menu_id, 5, '#', '', 1, 0, 'F', '0', '0', 'mall:vendor:export', '#', 'admin', NOW());

-- 6. 给管理员角色分配权限（假设角色 ID 为 1）
INSERT INTO sys_role_menu (role_id, menu_id) 
SELECT 1, menu_id FROM sys_menu WHERE menu_name IN ('Vendor Management', 'Vendor Application', 'Vendor List');
```

**注意**: 执行 SQL 后需要：
1. 清除缓存（如果有）
2. 重新登录系统
3. 刷新页面

---

## 供应商申请管理

### 访问路径
```
登录后台 → Mall → Vendor Management → Vendor Application
```

### 功能说明

#### 1. 查看申请列表

**筛选功能：**
- Application ID: 按申请编号搜索
- Vendor Name: 按供应商名称搜索
- Status: 按状态筛选
  - Pending (待审核)
  - Under Review (审核中)
  - Interview Required (需要面试)
  - Approved (已批准)
  - Rejected (已拒绝)
- Location: 按地区筛选
  - North America
  - Europe
  - Asia Pacific
  - Latin America
  - Middle East & Africa

**列表显示内容：**
- Application ID (申请编号)
- Vendor Name (供应商名称)
- Location (所在地区)
- Stock Volume (库存规模)
- Status (申请状态)
- Review Progress (审核进度条 0-100%)
- Applied Time (申请时间)
- Actions (操作按钮)

#### 2. 查看申请详情

**操作步骤：**
1. 在列表中点击"View"按钮
2. 弹出详情对话框

**显示的信息包括：**
- **基本信息**
  - Application ID (申请编号)
  - Vendor Name (供应商名称)
  - Market Experience (是否有市场经验)
  - Existing Markets (现有市场)
  - Experience Years (经验年限)
  - Location (地理位置)
  - Stock Volume (库存规模)
  - Offline Delivery (是否支持线下交付)

- **加密钱包信息**
  - BTC Wallet Address
  - XMR Wallet Address
  - USDT Wallet Address
  - PGP Signature URL (点击可访问)

- **联系方式**
  - Primary Contact (主要联系方式)
    - Telegram
    - Signal
    - Jabber
    - Email
  - Secondary Contact (次要联系方式)

- **产品信息**
  - Product Description (产品描述)

- **申请状态**
  - Status (当前状态)
  - Applied Time (申请时间)
  - Review Notes (审核备注，如有)

#### 3. 批准申请

**操作步骤：**
1. 在列表中找到待审核的申请（状态为 Pending 或 Under Review）
2. 点击"Approve"按钮
3. 在弹出的对话框中输入审核意见（可选）
4. 点击"Confirm"确认

**系统自动操作：**
- 申请状态变更为"Approved"
- 审核进度设置为 100%
- 自动创建对应的 Vendor 记录
- 记录审核人和审核时间
- 将申请与新创建的 Vendor 关联

**生成的 Vendor 信息：**
- Vendor Code: 自动生成（格式：VD + 时间戳）
- Vendor Name: 来自申请
- Contact Info: 来自申请的主要联系方式
- Wallets: 来自申请
- Location: 来自申请
- Status: 默认启用 (Enabled)

#### 4. 拒绝申请

**操作步骤：**
1. 在列表中找到待审核的申请
2. 点击"Reject"按钮
3. 在对话框中输入拒绝原因（推荐填写）
4. 点击"Confirm"确认

**系统操作：**
- 申请状态变更为"Rejected"
- 审核进度设置为 100%
- 记录审核人、审核时间和拒绝原因

#### 5. 删除申请

**操作步骤：**
1. 选择要删除的申请（可多选）
2. 点击"Delete"按钮
3. 确认删除操作

**注意：** 删除是软删除（del_flag = '2'），数据仍保留在数据库中。

#### 6. 导出申请数据

**操作步骤：**
1. 设置筛选条件（可选）
2. 点击"Export"按钮
3. 系统生成 Excel 文件自动下载

**导出内容：**
包含所有筛选后的申请记录及其详细信息。

---

## 供应商列表管理

### 访问路径
```
登录后台 → Mall → Vendor Management → Vendor List
```

### 功能说明

#### 1. 查看供应商列表

**筛选功能：**
- Vendor Code: 按供应商代码搜索
- Vendor Name: 按供应商名称搜索
- Status: 启用/禁用
- Location: 按地区筛选

**列表显示内容：**
- Vendor Code (供应商代码)
- Vendor Name (供应商名称)
- Location (所在地区)
- Rating (评分，星级显示)
- Total Orders (总订单数)
- Contact (联系方式)
- Status (状态开关)
- Featured (是否推荐)
- Created Time (创建时间)
- Actions (操作按钮)

#### 2. 添加新供应商

**操作步骤：**
1. 点击"New Vendor"按钮
2. 在弹出的表单中填写信息：

**必填字段：**
- Vendor Name (供应商名称)
- Location (所在地区)

**可选字段：**
- Vendor Code (留空则自动生成)
- Description (描述)
- Contact Telegram
- Contact Email
- Contact Signal
- Contact Jabber
- PGP Public Key URL
- BTC Wallet
- XMR Wallet
- USDT Wallet
- Status (状态: Enabled/Disabled)
- Is Featured (是否推荐: Yes/No)
- Sort Order (排序顺序)
- Remark (备注)

3. 点击"Confirm"保存

**自动生成内容：**
- Vendor Code: 格式为 VD + 时间戳
- Status: 默认为 Enabled
- Rating: 默认为 0.00
- Total Orders/Sales: 默认为 0

#### 3. 编辑供应商信息

**操作步骤：**
1. 在列表中点击"Edit"按钮
2. 修改需要更新的字段
3. 点击"Confirm"保存

**注意：**
- Vendor Code 创建后不可修改
- 其他所有字段都可以修改

#### 4. 查看供应商详情

**操作步骤：**
1. 点击"View"按钮
2. 查看完整的供应商信息

**显示内容包括：**
- 基本信息（代码、名称、位置、评分）
- 统计信息（总销售额、总订单数）
- 状态信息（启用/禁用、推荐/非推荐）
- 联系方式（所有联系方式）
- 加密钱包地址
- PGP 公钥链接
- 时间信息（创建时间、批准时间、批准人）

#### 5. 启用/禁用供应商

**操作步骤：**
1. 在列表的"Status"列找到开关按钮
2. 点击开关切换状态
3. 确认操作

**效果：**
- Enabled: 供应商可以正常使用，其产品可以展示
- Disabled: 供应商被禁用，可能影响其产品的显示

#### 6. 删除供应商

**操作步骤：**
1. 选择要删除的供应商（可多选）
2. 点击"Delete"按钮
3. 确认删除操作

**注意事项：**
- 删除是软删除，数据保留在数据库
- 删除供应商前建议先处理其相关产品

#### 7. 导出供应商数据

**操作步骤：**
1. 设置筛选条件（可选）
2. 点击"Export"按钮
3. 下载生成的 Excel 文件

---

## 产品关联

### 如何将产品关联到 Vendor

#### 方法 1: 在产品管理中设置

1. 进入产品管理页面
2. 编辑需要关联的产品
3. 设置以下字段：
   - **Product Origin**: 选择 "Vendor" (值为 1)
   - **Origin ID**: 输入 Vendor 的 ID

#### 方法 2: 通过 SQL 批量关联

```sql
-- 将产品 ID 456 关联到 Vendor ID 123
UPDATE mall_product 
SET product_origin = 1, origin_id = 123 
WHERE id = 456;

-- 批量关联多个产品到同一个 Vendor
UPDATE mall_product 
SET product_origin = 1, origin_id = 123 
WHERE id IN (456, 457, 458);

-- 查询某个 Vendor 的所有产品
SELECT * FROM mall_product 
WHERE product_origin = 1 AND origin_id = 123;
```

### 查询示例

```sql
-- 查看产品与 Vendor 的关联关系
SELECT 
    p.id AS product_id,
    p.name AS product_name,
    p.product_origin,
    v.vendor_code,
    v.vendor_name,
    v.location
FROM mall_product p
LEFT JOIN mall_vendor v ON p.origin_id = v.id AND p.product_origin = 1
WHERE p.product_origin = 1;

-- 统计每个 Vendor 的产品数量
SELECT 
    v.vendor_code,
    v.vendor_name,
    COUNT(p.id) AS product_count
FROM mall_vendor v
LEFT JOIN mall_product p ON v.id = p.origin_id AND p.product_origin = 1
GROUP BY v.id, v.vendor_code, v.vendor_name;
```

---

## 常见操作示例

### 示例 1: 处理新的供应商申请

**场景：** 收到一个新的供应商申请需要审核

**步骤：**
1. 登录系统 → Mall → Vendor Management → Vendor Application
2. 在列表中找到新申请（状态为 "Pending"）
3. 点击"View"按钮查看详细信息
4. 检查以下内容：
   - 供应商基本信息是否完整
   - 联系方式是否有效
   - 钱包地址格式是否正确
   - PGP 签名是否可访问
   - 产品描述是否符合要求
5. 如果审核通过，点击"Approve"
6. 输入审核意见（如："All information verified, approved"）
7. 点击"Confirm"
8. 系统自动创建 Vendor 记录
9. 在 Vendor List 中可以看到新创建的供应商

### 示例 2: 管理供应商信息

**场景：** 供应商更换了联系方式，需要更新

**步骤：**
1. 进入 Vendor List
2. 搜索供应商名称或代码
3. 点击"Edit"按钮
4. 更新联系方式字段（如新的 Telegram 账号）
5. 点击"Confirm"保存
6. 系统显示"Updated successfully"

### 示例 3: 禁用违规供应商

**场景：** 发现供应商有违规行为，需要临时禁用

**步骤：**
1. 进入 Vendor List
2. 找到目标供应商
3. 点击"Status"列的开关，从绿色变为灰色
4. 确认操作
5. 供应商状态变为 Disabled
6. 其产品可能不再显示在前台（取决于产品筛选逻辑）

### 示例 4: 导出供应商报表

**场景：** 需要导出所有欧洲地区的供应商信息

**步骤：**
1. 进入 Vendor List
2. 在"Location"筛选器中选择"Europe"
3. 点击"Search"按钮
4. 查看筛选结果
5. 点击"Export"按钮
6. 等待文件生成并下载
7. 打开 Excel 文件查看数据

### 示例 5: 批量处理申请

**场景：** 有多个待处理的申请

**步骤：**
1. 进入 Vendor Application
2. 设置状态筛选为"Pending"
3. 逐个查看申请详情
4. 对每个申请做出决定（Approve/Reject）
5. 批准的申请会自动创建 Vendor
6. 可以在 Vendor List 中查看所有新批准的供应商

---

## 💡 使用技巧

### 1. 快速筛选
- 使用组合筛选条件快速找到目标记录
- 使用 Application ID 或 Vendor Code 进行精确搜索

### 2. 批量操作
- 使用复选框选择多个记录进行批量删除
- 导出前先设置好筛选条件，只导出需要的数据

### 3. 审核流程
- 批准申请前仔细查看所有信息
- 在审核意见中记录重要的决策理由
- 对于不确定的申请，可以先联系申请人确认

### 4. 数据维护
- 定期检查和更新供应商联系方式
- 关注供应商的评分和订单数据
- 及时处理禁用或问题供应商

### 5. 安全建议
- 验证 PGP 签名的真实性
- 核实钱包地址的有效性
- 对重要操作（如批准大型供应商）保持记录

---

## ❓ 常见问题

**Q: 批准申请后可以撤销吗？**
A: 批准后无法自动撤销，但可以：
1. 在 Vendor List 中禁用该供应商
2. 或手动修改数据库中的状态

**Q: 如何查看某个供应商的所有产品？**
A: 使用 SQL 查询：
```sql
SELECT * FROM mall_product 
WHERE product_origin = 1 AND origin_id = [vendor_id];
```

**Q: 供应商可以自己提交申请吗？**
A: 当前版本需要管理员手动创建申请。未来版本可以开发供应商门户让其自助申请。

**Q: 删除的申请可以恢复吗？**
A: 可以，删除是软删除。需要手动修改数据库：
```sql
UPDATE mall_vendor_application 
SET del_flag = '0' 
WHERE id = [application_id];
```

**Q: 如何导出所有申请的历史记录？**
A: 在 Vendor Application 页面，不设置任何筛选条件，直接点击"Export"按钮。

---

## 📞 技术支持

如遇到问题：
1. 查看系统日志: `medusa-admin/logs/`
2. 检查浏览器控制台错误
3. 确认数据库连接正常
4. 验证菜单权限配置正确
5. 参考详细文档: `VENDOR_MODULE_SETUP.md`

---

## 🚀 下一步

现在你已经了解如何使用 Vendor 模块，可以开始：
1. 测试申请提交和审核流程
2. 管理供应商信息
3. 将产品关联到供应商
4. 导出报表进行数据分析
5. 根据需求定制和扩展功能

祝使用愉快！


# Vendor Application 审批流程完善说明

## 完成的工作

### 1. 后端更新

#### 新增接口
- **IVendorApplicationService.java**
  - 添加 `requireInterview(Long id, String notes)` 接口方法

#### 新增实现
- **VendorApplicationServiceImpl.java**
  - 实现 `requireInterview()` 方法
  - 将申请状态更改为 `interview_required`
  - 设置审核进度为 50%

#### 新增 API 端点
- **VendorApplicationController.java**
  - `POST /admin/mall/vendor/application/interview/{id}` - 要求面试
  - `POST /admin/mall/vendor/application/startReview/{id}` - 开始审核

### 2. 前端更新

#### API 方法（src/api/mall/vendor.js）
- `startReview(id)` - 开始审核，将状态从 pending 改为 under_review
- `requireInterview(id, notes)` - 要求面试

#### Vue 页面（src/views/mall/vendor/application/index.vue）

**新增按钮：**
1. **Start Review** - 仅在 pending 状态显示
   - 点击后将状态改为 under_review
   - 自动设置进度为 10%

2. **Progress** - 仅在 under_review 状态显示
   - 更新审核阶段和进度百分比

3. **Interview** - 在 pending 和 under_review 状态显示
   - 要求供应商进行面试
   - 状态改为 interview_required

**新增对话框：**
1. **Update Progress Dialog**
   - 选择审核阶段（Initial Review、Document Check、Background Check、Final Review）
   - 使用滑块调整进度百分比（0-100%，步长 5%）

2. **Interview Dialog**
   - 输入要求面试的原因
   - 必填字段验证

## 完整的审批流程

### 流程图

```
Pending (待审核)
    ↓
    ↓ [点击 Start Review]
    ↓
Under Review (审核中)
    ↓
    ├─→ [点击 Progress] → 更新进度 (10% → 90%)
    ├─→ [点击 Interview] → Interview Required (需要面试)
    ├─→ [点击 Approve] → Approved (已批准) → 创建供应商记录
    └─→ [点击 Reject] → Rejected (已拒绝)

Interview Required (需要面试)
    ├─→ [面试后点击 Approve] → Approved (已批准)
    └─→ [面试后点击 Reject] → Rejected (已拒绝)
```

### 状态说明

1. **pending** (待审核)
   - 新提交的申请
   - 可用操作：Start Review、Interview、Approve、Reject、Delete

2. **under_review** (审核中)
   - 正在审核的申请
   - 可用操作：Progress、Interview、Approve、Reject、Delete

3. **interview_required** (需要面试)
   - 需要进一步面试确认
   - 可用操作：View、Delete

4. **approved** (已批准)
   - 审批通过，自动创建供应商记录
   - 可用操作：View、Delete

5. **rejected** (已拒绝)
   - 审批拒绝
   - 可用操作：View、Delete

## 使用说明

### 开始审核
1. 在申请列表中找到状态为 **Pending** 的申请
2. 点击 **Start Review** 按钮
3. 确认后，状态自动变为 **Under Review**，进度设为 10%

### 更新审核进度
1. 对于 **Under Review** 状态的申请，点击 **Progress** 按钮
2. 在对话框中：
   - 选择当前审核阶段
   - 使用滑块设置进度百分比
3. 点击确认

### 要求面试
1. 对于 **Pending** 或 **Under Review** 状态的申请，点击 **Interview** 按钮
2. 输入要求面试的原因（必填）
3. 点击确认，状态变为 **Interview Required**

### 批准申请
1. 点击 **Approve** 按钮
2. 输入审核意见（可选）
3. 确认后：
   - 状态变为 **Approved**
   - 进度设为 100%
   - 自动创建供应商记录

### 拒绝申请
1. 点击 **Reject** 按钮
2. 输入拒绝原因（可选）
3. 确认后：
   - 状态变为 **Rejected**
   - 进度设为 100%

## 测试步骤

### 1. 测试开始审核功能
```
1. 确保有状态为 pending 的申请
2. 点击 Start Review 按钮
3. 确认对话框
4. 验证状态变为 under_review
5. 验证进度显示为 10%
```

### 2. 测试更新进度功能
```
1. 对 under_review 状态的申请点击 Progress 按钮
2. 选择审核阶段（如 Document Check）
3. 设置进度为 50%
4. 确认并验证更新成功
```

### 3. 测试要求面试功能
```
1. 对 pending 或 under_review 状态的申请点击 Interview 按钮
2. 输入面试原因
3. 确认并验证状态变为 interview_required
4. 验证进度显示为 50%
```

### 4. 测试完整流程
```
1. 创建测试申请（pending）
2. Start Review → under_review (10%)
3. Update Progress → document_check (30%)
4. Update Progress → background_check (60%)
5. Update Progress → final_review (90%)
6. Approve → approved (100%) + 创建供应商记录
```

## 权限要求

- `mall:vendor:application:list` - 查看申请列表
- `mall:vendor:application:query` - 查看申请详情
- `mall:vendor:application:edit` - 编辑申请（Start Review、Progress、Interview）
- `mall:vendor:application:approve` - 批准申请
- `mall:vendor:application:reject` - 拒绝申请
- `mall:vendor:application:remove` - 删除申请

## 注意事项

1. **Start Review** 会自动将状态从 pending 改为 under_review，进度设为 10%
2. **Progress** 功能只在 under_review 状态下可用
3. **Interview** 功能在 pending 和 under_review 状态下可用
4. **Approve** 和 **Reject** 只在 pending 和 under_review 状态下可用
5. 批准申请后会自动创建供应商记录（Vendor）
6. 所有审核操作都会记录操作时间和操作人

## 相关文件

### 后端
- `/medusa-mall/src/main/java/com/medusa/mall/controller/VendorApplicationController.java`
- `/medusa-mall/src/main/java/com/medusa/mall/service/IVendorApplicationService.java`
- `/medusa-mall/src/main/java/com/medusa/mall/service/impl/VendorApplicationServiceImpl.java`

### 前端
- `/medusa-admin-vue/src/api/mall/vendor.js`
- `/medusa-admin-vue/src/views/mall/vendor/application/index.vue`

## 参考

参考了 `vedusa_vendor` 项目中的 Admin Panel 设计：
- `/vedusa_vendor/admin.html`
- `/vedusa_vendor/admin.js`

实现了完整的审批工作流，包括状态管理、进度跟踪和面试要求等功能。


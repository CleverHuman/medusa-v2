# Category Backend Implementation

## 概述

本次更新实现了product-category.html页面的后端数据获取功能，包括创建Category相关的实体类、服务层和数据库操作。

## 新增文件

### 1. Category实体类
- **文件**: `medusa-mall/src/main/java/com/medusa/mall/domain/Category.java`
- **功能**: 对应mall_category表的实体类
- **字段**: id, categoryName, categoryCode, description, imageUrl, iconUrl, status, sortOrder, isFeatured等

### 2. CategoryMapper接口
- **文件**: `medusa-mall/src/main/java/com/medusa/mall/mapper/CategoryMapper.java`
- **功能**: 定义Category数据库操作方法
- **方法**: selectCategoryById, selectCategoryByCode, selectActiveCategories等

### 3. CategoryMapper.xml
- **文件**: `medusa-mall/src/main/resources/mapper/mall/CategoryMapper.xml`
- **功能**: MyBatis SQL映射文件
- **查询**: 支持按ID、代码、状态等条件查询Category

### 4. CategoryService接口
- **文件**: `medusa-mall/src/main/java/com/medusa/mall/service/ICategoryService.java`
- **功能**: 定义Category业务逻辑接口

### 5. CategoryServiceImpl实现类
- **文件**: `medusa-mall/src/main/java/com/medusa/mall/service/impl/CategoryServiceImpl.java`
- **功能**: Category业务逻辑实现

## 修改的文件

### 1. StaticPageController.java
- **新增方法**: `productCategory()` - 处理product-category页面请求
- **修改方法**: `products()` - 添加categoryId参数支持
- **新增依赖**: 注入ICategoryService

### 2. Product.java
- **新增字段**: categoryId - 外键关联mall_category表
- **新增方法**: getCategoryId(), setCategoryId()

### 3. ProductMapper.java
- **新增方法**: selectProductsByCategoryId(Long categoryId)

### 4. ProductMapper.xml
- **新增字段映射**: categoryId
- **新增查询**: selectProductsByCategoryId
- **更新SQL**: 所有相关查询都包含category_id字段

### 5. IProductService.java
- **新增方法**: selectProductsByCategoryId(Long categoryId)

### 6. ProductServiceImpl.java
- **新增方法**: selectProductsByCategoryId(Long categoryId)实现

### 7. product-category.html
- **动态数据**: 使用Thymeleaf语法从后端获取分类数据
- **动态链接**: 点击分类卡片跳转到对应的产品列表页面
- **条件渲染**: 根据分类代码显示不同的特色功能

## 数据库表结构

### mall_category表
```sql
CREATE TABLE `mall_category` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `category_name` varchar(100) NOT NULL,
  `category_code` varchar(50) NOT NULL,
  `description` text,
  `image_url` varchar(255) DEFAULT NULL,
  `icon_url` varchar(255) DEFAULT NULL,
  `status` tinyint(1) DEFAULT '1',
  `sort_order` int(11) DEFAULT '0',
  `is_featured` tinyint(1) DEFAULT '0',
  `create_by` varchar(64) DEFAULT '',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(64) DEFAULT '',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `remark` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_category_code` (`category_code`),
  UNIQUE KEY `uk_category_name` (`category_name`)
);
```

### mall_product表更新
- **新增字段**: `category_id` bigint(20) - 外键关联mall_category表
- **新增索引**: `idx_category_id` (`category_id`)

## 功能特性

### 1. 动态分类显示
- 从数据库获取所有激活的分类
- 支持分类图片、描述、特色功能显示
- 根据分类代码显示不同的特色功能

### 2. 产品查询优化
- 支持通过categoryId查询产品
- 支持通过categoryCode查询产品
- 支持通过categoryName查询产品（自动映射到categoryCode）
- 保持向后兼容性

### 3. 页面交互
- 点击分类卡片跳转到对应产品列表
- 支持分类图片加载失败时的备用显示
- 响应式设计，支持移动端

## API端点

### 1. 产品分类页面
- **URL**: `/mall/static/product-category`
- **方法**: GET
- **功能**: 显示所有激活的产品分类

### 2. 产品列表页面
- **URL**: `/mall/static/products`
- **参数**: 
  - `categoryId` (可选): 分类ID
  - `category` (可选): 分类代码或分类名称
  - `name` (可选): 产品名称搜索
- **功能**: 根据分类显示产品列表
- **映射规则**: 
  - "Digital Product" → "DIGITAL"
  - "Physical Product" → "PHYSICAL"

## 使用流程

1. **用户访问**: 用户点击导航栏的"PRODUCTS"链接
2. **分类页面**: 显示所有激活的产品分类
3. **选择分类**: 用户点击任意分类卡片
4. **产品列表**: 跳转到对应分类的产品列表页面
5. **浏览产品**: 用户可以浏览和搜索该分类下的产品

## 技术实现

### 1. 后端架构
- **Controller层**: StaticPageController处理页面请求
- **Service层**: CategoryService和ProductService处理业务逻辑
- **Mapper层**: CategoryMapper和ProductMapper处理数据库操作
- **实体层**: Category和Product实体类

### 2. 前端模板
- **模板引擎**: Thymeleaf
- **动态渲染**: 使用Thymeleaf语法动态显示数据
- **条件渲染**: 根据分类代码显示不同内容

### 3. 数据库设计
- **外键关联**: product.category_id -> category.id
- **索引优化**: 为category_id添加索引
- **数据完整性**: 使用外键约束保证数据一致性

## 注意事项

1. **数据库迁移**: 需要执行SQL脚本创建mall_category表并更新mall_product表
2. **数据初始化**: 需要插入默认的分类数据（Digital Product和Physical Product）
3. **数据更新**: 需要执行update_product_categories.sql更新现有产品的分类
4. **图片资源**: 需要准备分类图片或使用默认图片
5. **向后兼容**: 保持原有的category字段查询功能

## 数据迁移步骤

1. **执行分类表创建脚本**:
   ```sql
   -- 执行 create_category_complete.sql
   ```

2. **更新现有产品分类**:
   ```sql
   -- 执行 update_product_categories.sql
   ```

3. **验证数据**:
   ```sql
   SELECT 
       p.id, p.name, p.category, p.category_id,
       c.category_name, c.category_code
   FROM mall_product p
   LEFT JOIN mall_category c ON p.category_id = c.id
   ORDER BY p.id;
   ```

## 测试建议

1. **功能测试**: 验证分类页面正常显示
2. **数据测试**: 验证从数据库正确获取分类数据
3. **链接测试**: 验证点击分类卡片正确跳转
4. **兼容性测试**: 验证原有功能不受影响 
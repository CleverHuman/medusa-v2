# Product Category Page

## 功能说明

新创建的产品分类页面 (`product-category.html`) 为用户提供了一个直观的产品分类选择界面。

## 页面特性

### 🎨 设计特点
- **响应式布局**：支持桌面端和移动端
- **现代化UI**：深色主题，卡片式设计
- **交互效果**：悬停动画，图片缩放效果
- **渐变覆盖层**：鼠标悬停时显示详细信息

### 📱 页面结构

#### 1. 导航栏
- 统一的导航设计
- 条件显示（登录/未登录状态）
- PRODUCTS链接指向新的分类页面

#### 2. 欢迎区域
- 页面标题：Choose Your Product Category
- 欢迎信息：介绍产品分类

#### 3. 分类卡片
- **左侧**：Physical Products（实体产品）
- **右侧**：Digital Products（数字产品）

### 🎯 交互功能

#### Physical Products 卡片
- **图片**：实体产品相关图片
- **描述**：High-quality physical goods delivered to your doorstep
- **特色**：Fast Shipping, Quality Guaranteed, Easy Returns
- **链接**：点击跳转到 `/mall/static/products?category=PHYSICAL`

#### Digital Products 卡片
- **图片**：数字产品相关图片
- **描述**：Instant digital downloads and online services
- **特色**：Instant Download, Digital Access, Secure Delivery
- **链接**：点击跳转到 `/mall/static/products?category=DIGITAL`

### 🔧 技术实现

#### 图片处理
- 使用 `onerror` 属性提供备用图片
- 内联SVG作为默认图片
- 支持外部图片路径

#### 响应式设计
```css
@media (max-width: 900px) {
    .categories-container {
        grid-template-columns: 1fr;
    }
}
```

#### 动画效果
- 卡片悬停：`transform: translateY(-8px)`
- 图片缩放：`transform: scale(1.05)`
- 覆盖层显示：`opacity: 0` → `opacity: 1`

### 🚀 使用流程

1. **用户登录后**点击导航栏的 "PRODUCTS"
2. **进入分类页面**，看到两个产品分类卡片
3. **点击任意卡片**，跳转到对应的产品列表页面
4. **产品列表页面**会根据URL参数显示对应分类的产品

### 📝 更新内容

#### 导航栏更新
- 所有页面的导航栏都已更新
- PRODUCTS链接指向 `/mall/static/product-category`
- 添加了条件显示逻辑

#### 页面列表
- `home.html` - 首页
- `products.html` - 产品列表页
- `cart.html` - 购物车
- `orders.html` - 订单页面
- `profile.html` - 用户资料

### 🎨 设计规范

#### 颜色方案
- 背景：`#181818`
- 导航栏：`#111`
- 卡片背景：`#222`
- 主色调：`#ff9800`（橙色）

#### 字体
- 主字体：-apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto
- 标题：2.5rem, bold
- 正文：1rem-1.1rem

#### 间距
- 卡片间距：3rem
- 内边距：2rem
- 圆角：16px（卡片）, 8px（按钮）

这个新页面为用户提供了更好的产品浏览体验，让用户能够清楚地了解产品分类并做出选择。 
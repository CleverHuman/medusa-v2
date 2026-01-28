# Redis Order Cache Service

## 概述

`RedisOrderCacheService` 是一个用于在 Redis 中存储和获取订单金额信息的服务。它提供了完整的 CRUD 操作，支持过期时间设置，并包含错误处理机制。

## 功能特性

- ✅ 存储订单金额信息到 Redis
- ✅ 支持设置过期时间
- ✅ 通过 orderId 检索金额信息
- ✅ 检查数据是否存在
- ✅ 删除缓存数据
- ✅ 获取剩余生存时间 (TTL)
- ✅ 完整的错误处理

## 核心组件

### 1. CartAmountInfo 实体类

包含以下字段：
- `subtotal` - 小计金额
- `couponDiscount` - 优惠券折扣
- `memberDiscount` - 会员折扣
- `currency` - 币种
- `total` - 总金额
- `dueTime` - 到期时间
- `payType` - 支付类型
- `rate` - 汇率
- `totalCoin` - 虚拟币总额

### 2. RedisOrderCacheService 接口

提供以下方法：
- `storeCartAmountInfo(String orderId, CartAmountInfo cartAmountInfo)` - 存储金额信息
- `storeCartAmountInfo(String orderId, CartAmountInfo cartAmountInfo, long timeout)` - 存储金额信息并设置过期时间
- `getCartAmountInfo(String orderId)` - 获取金额信息
- `hasCartAmountInfo(String orderId)` - 检查是否存在
- `removeCartAmountInfo(String orderId)` - 删除金额信息
- `getTimeToLive(String orderId)` - 获取剩余生存时间

### 3. RedisOrderCacheServiceImpl 实现类

使用 `RedisCache` 工具类实现 Redis 操作，包含完整的错误处理。

## 使用示例

### 1. 在 Service 中使用

```java
@Autowired
private RedisOrderCacheService redisOrderCacheService;

// 创建 CartAmountInfo
CartAmountInfo cartAmountInfo = new CartAmountInfo();
cartAmountInfo.setSubtotal(new BigDecimal("100.00"));
cartAmountInfo.setCouponDiscount(new BigDecimal("10.00"));
cartAmountInfo.setMemberDiscount(new BigDecimal("5.00"));
cartAmountInfo.setCurrency("AUD");
cartAmountInfo.setTotal(new BigDecimal("85.00"));
cartAmountInfo.setDueTime(new Date());
cartAmountInfo.setPayType("CRYPTO");
cartAmountInfo.setRate(new BigDecimal("1.0"));
cartAmountInfo.setTotalCoin(new BigDecimal("85.00"));

// 存储到 Redis（1小时过期）
boolean success = redisOrderCacheService.storeCartAmountInfo("ORDER123", cartAmountInfo, 3600);

// 从 Redis 获取
CartAmountInfo retrieved = redisOrderCacheService.getCartAmountInfo("ORDER123");

// 检查是否存在
boolean exists = redisOrderCacheService.hasCartAmountInfo("ORDER123");

// 删除
boolean removed = redisOrderCacheService.removeCartAmountInfo("ORDER123");
```

### 2. 在 StaticPageController 中的集成

已在 `StaticPageController.processPayment()` 方法中集成，在创建订单后自动存储金额信息到 Redis。

在 `StaticPageController.checkoutConfirm()` 方法中，从 Redis 获取金额信息用于显示。

## 缓存键格式

Redis 中的键格式为：`order:amount:{orderId}`

例如：`order:amount:ORDER123`

## 错误处理

所有方法都包含 try-catch 错误处理，失败时会：
1. 记录错误日志
2. 返回 false 或 null
3. 不会抛出异常影响主流程

## 测试

使用 `RedisOrderCacheTestService` 进行功能测试：

```java
@Autowired
private RedisOrderCacheTestService testService;

String result = testService.testCartAmountInfoStorage("TEST_ORDER_123");
System.out.println(result);
```

## 注意事项

1. 确保 Redis 服务正常运行
2. 合理设置过期时间，避免内存泄漏
3. 在生产环境中监控 Redis 内存使用情况
4. 定期清理过期的缓存数据
5. 考虑添加缓存预热和降级机制

## 扩展功能

可以考虑添加以下功能：
- 批量操作支持
- 缓存统计信息
- 自动清理过期数据
- 缓存命中率监控
- 分布式锁支持 
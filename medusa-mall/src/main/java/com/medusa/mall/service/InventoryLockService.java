package com.medusa.mall.service;

import java.util.List;
import java.util.Map;

/**
 * 库存锁定服务
 * 处理订单创建时的库存锁定和超时恢复
 */
public interface InventoryLockService {
    
    /**
     * 锁定库存
     * @param orderId 订单ID
     * @param items 订单商品列表，包含SKU和数量
     * @return 是否锁定成功
     */
    boolean lockInventory(String orderId, List<Map<String, Object>> items);
    
    /**
     * 释放库存
     * @param orderId 订单ID
     * @return 是否释放成功
     */
    boolean releaseInventory(String orderId);
    
    /**
     * 确认库存扣减（支付成功后）
     * @param orderId 订单ID
     * @return 是否确认成功
     */
    boolean confirmInventory(String orderId);
    
    /**
     * 检查库存是否足够
     * @param items 商品列表
     * @return 是否足够
     */
    boolean checkInventory(List<Map<String, Object>> items);
    
    /**
     * 获取订单的库存锁定信息
     * @param orderId 订单ID
     * @return 锁定信息
     */
    Map<String, Object> getInventoryLockInfo(String orderId);
} 
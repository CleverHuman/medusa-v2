package com.medusa.mall.service.impl;

import com.medusa.mall.domain.Product2;
import com.medusa.mall.mapper.Product2Mapper;
import com.medusa.mall.service.InventoryLockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 库存锁定服务实现
 */
@Service
public class InventoryLockServiceImpl implements InventoryLockService {
    
    private static final Logger log = LoggerFactory.getLogger(InventoryLockServiceImpl.class);
    
    // 内存中存储库存锁定信息：orderId -> {sku -> lockedQuantity}
    private static final Map<String, Map<String, Integer>> INVENTORY_LOCKS = new ConcurrentHashMap<>();
    
    // 内存中存储订单锁定时间：orderId -> lockTime
    private static final Map<String, Long> ORDER_LOCK_TIMES = new ConcurrentHashMap<>();
    
    // 锁定超时时间：60分钟（1小时）- 适应网络延迟和支付确认时间
    private static final long LOCK_TIMEOUT_MS = 60 * 60 * 1000; // 60分钟（1小时）
    
    @Autowired
    private Product2Mapper product2Mapper;
    
    @Override
    @Transactional
    public boolean lockInventory(String orderId, List<Map<String, Object>> items) {
        try {
            log.info("Starting inventory lock, Order ID: {}, product count: {}", orderId, items.size());
            
            // 检查库存是否足够
            if (!checkInventory(items)) {
                log.warn("Insufficient inventory, cannot lock, Order ID: {}", orderId);
                return false;
            }
            
            // 锁定库存
            Map<String, Integer> lockedItems = new HashMap<>();
            for (Map<String, Object> item : items) {
                String sku = (String) item.get("sku");
                Integer quantity = (Integer) item.get("quantity");
                
                if (sku != null && quantity != null && quantity > 0) {
                    // 更新数据库中的库存
                    Product2 product = new Product2();
                    product.setSku(sku);
                    List<Product2> products = product2Mapper.selectProduct2List(product);
                    
                    if (!products.isEmpty()) {
                        Product2 targetProduct = products.get(0);
                        if (targetProduct.getInventory() >= quantity) {
                            // 扣减库存
                            targetProduct.setInventory(targetProduct.getInventory() - quantity);
                            int updateResult = product2Mapper.updateProduct2(targetProduct);
                            
                            if (updateResult > 0) {
                                lockedItems.put(sku, quantity);
                                log.info("Successfully locked inventory, SKU: {}, quantity: {}", sku, quantity);
                            } else {
                                log.error("Failed to update inventory, SKU: {}", sku);
                                // 回滚已锁定的库存
                                releaseInventory(orderId);
                                return false;
                            }
                        } else {
                            log.error("Insufficient inventory, SKU: {}, required: {}, available: {}", sku, quantity, targetProduct.getInventory());
                            releaseInventory(orderId);
                            return false;
                        }
                    } else {
                        log.error("Product not found, SKU: {}", sku);
                        releaseInventory(orderId);
                        return false;
                    }
                }
            }
            
            // 记录锁定信息
            INVENTORY_LOCKS.put(orderId, lockedItems);
            ORDER_LOCK_TIMES.put(orderId, System.currentTimeMillis());
            
            log.info("Inventory lock successful, Order ID: {}, locked products: {}", orderId, lockedItems);
            return true;
            
        } catch (Exception e) {
            log.error("Exception occurred while locking inventory, Order ID: {}", orderId, e);
            releaseInventory(orderId);
            return false;
        }
    }
    
    @Override
    @Transactional
    public boolean releaseInventory(String orderId) {
        try {
            log.info("Starting inventory release, Order ID: {}", orderId);
            
            Map<String, Integer> lockedItems = INVENTORY_LOCKS.get(orderId);
            if (lockedItems == null || lockedItems.isEmpty()) {
                log.warn("No inventory lock information found, Order ID: {}", orderId);
                return true;
            }
            
            // 恢复库存
            for (Map.Entry<String, Integer> entry : lockedItems.entrySet()) {
                String sku = entry.getKey();
                Integer quantity = entry.getValue();
                
                Product2 product = new Product2();
                product.setSku(sku);
                List<Product2> products = product2Mapper.selectProduct2List(product);
                
                if (!products.isEmpty()) {
                    Product2 targetProduct = products.get(0);
                    targetProduct.setInventory(targetProduct.getInventory() + quantity);
                    int updateResult = product2Mapper.updateProduct2(targetProduct);
                    
                    if (updateResult > 0) {
                        log.info("Successfully restored inventory, SKU: {}, quantity: {}", sku, quantity);
                    } else {
                        log.error("Failed to restore inventory, SKU: {}", sku);
                    }
                }
            }
            
            // 清除锁定记录
            INVENTORY_LOCKS.remove(orderId);
            ORDER_LOCK_TIMES.remove(orderId);
            
            log.info("Inventory release completed, Order ID: {}", orderId);
            return true;
            
        } catch (Exception e) {
            log.error("Exception occurred while releasing inventory, Order ID: {}", orderId, e);
            return false;
        }
    }
    
    @Override
    @Transactional
    public boolean confirmInventory(String orderId) {
        try {
            log.info("Confirming inventory deduction, Order ID: {}", orderId);
            
            // 支付成功后，只required清除锁定记录，库存已经扣减
            INVENTORY_LOCKS.remove(orderId);
            ORDER_LOCK_TIMES.remove(orderId);
            
            log.info("Inventory confirmation completed, Order ID: {}", orderId);
            return true;
            
        } catch (Exception e) {
            log.error("Exception occurred while confirming inventory, Order ID: {}", orderId, e);
            return false;
        }
    }
    
    @Override
    public boolean checkInventory(List<Map<String, Object>> items) {
        try {
            for (Map<String, Object> item : items) {
                String sku = (String) item.get("sku");
                Integer quantity = (Integer) item.get("quantity");
                
                if (sku != null && quantity != null && quantity > 0) {
                    Product2 product = new Product2();
                    product.setSku(sku);
                    List<Product2> products = product2Mapper.selectProduct2List(product);
                    
                    if (products.isEmpty()) {
                        log.warn("Product not found, SKU: {}", sku);
                        return false;
                    }
                    
                    Product2 targetProduct = products.get(0);
                    if (targetProduct.getInventory() < quantity) {
                        log.warn("Insufficient inventory, SKU: {}, required: {}, available: {}", sku, quantity, targetProduct.getInventory());
                        return false;
                    }
                }
            }
            return true;
        } catch (Exception e) {
            log.error("Exception occurred while checking inventory", e);
            return false;
        }
    }
    
    @Override
    public Map<String, Object> getInventoryLockInfo(String orderId) {
        Map<String, Object> result = new HashMap<>();
        
        Map<String, Integer> lockedItems = INVENTORY_LOCKS.get(orderId);
        Long lockTime = ORDER_LOCK_TIMES.get(orderId);
        
        result.put("lockedItems", lockedItems != null ? lockedItems : new HashMap<>());
        result.put("lockTime", lockTime);
        result.put("isLocked", lockedItems != null && !lockedItems.isEmpty());
        
        if (lockTime != null) {
            long currentTime = System.currentTimeMillis();
            long remainingTime = LOCK_TIMEOUT_MS - (currentTime - lockTime);
            result.put("remainingTime", Math.max(0, remainingTime));
            result.put("isExpired", remainingTime <= 0);
        }
        
        return result;
    }
    
    /**
     * 获取所有超时的Order ID
     */
    public List<String> getExpiredOrderIds() {
        List<String> expiredOrders = new ArrayList<>();
        long currentTime = System.currentTimeMillis();
        
        for (Map.Entry<String, Long> entry : ORDER_LOCK_TIMES.entrySet()) {
            String orderId = entry.getKey();
            Long lockTime = entry.getValue();
            
            if (lockTime != null && (currentTime - lockTime) > LOCK_TIMEOUT_MS) {
                expiredOrders.add(orderId);
            }
        }
        
        return expiredOrders;
    }
    
    /**
     * 清理超时的库存锁定
     */
    @Transactional
    public void cleanupExpiredLocks() {
        List<String> expiredOrders = getExpiredOrderIds();
        
        for (String orderId : expiredOrders) {
            log.info("Cleaning up expired inventory locks, Order ID: {}", orderId);
            releaseInventory(orderId);
        }
        
        if (!expiredOrders.isEmpty()) {
            log.info("Cleanup completed, cleaned {} expired order inventory locks", expiredOrders.size());
        }
    }
} 
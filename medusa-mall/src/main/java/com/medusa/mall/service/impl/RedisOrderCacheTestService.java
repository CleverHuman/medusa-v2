package com.medusa.mall.service.impl;

import com.medusa.mall.domain.vo.CartAmountInfo;
import com.medusa.mall.service.RedisOrderCacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Test service to demonstrate RedisOrderCacheService usage
 * This is for testing purposes only
 */
@Service
public class RedisOrderCacheTestService {
    
    @Autowired
    private RedisOrderCacheService redisOrderCacheService;
    
    /**
     * Test storing and retrieving cart amount info
     * 
     * @param orderId the order ID to test with
     * @return test result message
     */
    public String testCartAmountInfoStorage(String orderId) {
        try {
            // Create sample CartAmountInfo
            CartAmountInfo cartAmountInfo = new CartAmountInfo();
            cartAmountInfo.setSubtotal(new BigDecimal("150.00"));
            cartAmountInfo.setCouponDiscount(new BigDecimal("15.00"));
            cartAmountInfo.setMemberDiscount(new BigDecimal("7.50"));
            cartAmountInfo.setCurrency("AUD");
            cartAmountInfo.setTotal(new BigDecimal("127.50"));
            cartAmountInfo.setDueTime(new Date());
            cartAmountInfo.setPayType("CRYPTO");
            cartAmountInfo.setRate(new BigDecimal("1.0"));
            cartAmountInfo.setTotalCoin(new BigDecimal("127.50"));
            
            // Store in Redis with 1 hour expiration
            boolean stored = redisOrderCacheService.storeCartAmountInfo(orderId, cartAmountInfo, 3600);
            if (!stored) {
                return "Failed to store cart amount info";
            }
            
            // Check if exists
            boolean exists = redisOrderCacheService.hasCartAmountInfo(orderId);
            if (!exists) {
                return "Cart amount info not found after storing";
            }
            
            // Retrieve from Redis
            CartAmountInfo retrieved = redisOrderCacheService.getCartAmountInfo(orderId);
            if (retrieved == null) {
                return "Failed to retrieve cart amount info";
            }
            
            // Verify data
            if (!retrieved.getSubtotal().equals(cartAmountInfo.getSubtotal()) ||
                !retrieved.getTotal().equals(cartAmountInfo.getTotal()) ||
                !retrieved.getCurrency().equals(cartAmountInfo.getCurrency())) {
                return "Retrieved data doesn't match stored data";
            }
            
            // Get TTL
            long ttl = redisOrderCacheService.getTimeToLive(orderId);
            
            // Clean up
            boolean removed = redisOrderCacheService.removeCartAmountInfo(orderId);
            
            return String.format("Test successful! TTL: %d seconds, Cleanup: %s", 
                               ttl, removed ? "successful" : "failed");
            
        } catch (Exception e) {
            return "Test failed with exception: " + e.getMessage();
        }
    }
    
    /**
     * Test storing cart amount info without expiration
     * 
     * @param orderId the order ID to test with
     * @return test result message
     */
    public String testCartAmountInfoStorageWithoutExpiration(String orderId) {
        try {
            // Create sample CartAmountInfo
            CartAmountInfo cartAmountInfo = new CartAmountInfo();
            cartAmountInfo.setSubtotal(new BigDecimal("200.00"));
            cartAmountInfo.setCouponDiscount(new BigDecimal("20.00"));
            cartAmountInfo.setMemberDiscount(new BigDecimal("10.00"));
            cartAmountInfo.setCurrency("USD");
            cartAmountInfo.setTotal(new BigDecimal("170.00"));
            cartAmountInfo.setDueTime(new Date());
            cartAmountInfo.setPayType("BANK_TRANSFER");
            cartAmountInfo.setRate(new BigDecimal("0.75"));
            cartAmountInfo.setTotalCoin(new BigDecimal("127.50"));
            
            // Store in Redis without expiration
            boolean stored = redisOrderCacheService.storeCartAmountInfo(orderId, cartAmountInfo);
            if (!stored) {
                return "Failed to store cart amount info";
            }
            
            // Retrieve and verify
            CartAmountInfo retrieved = redisOrderCacheService.getCartAmountInfo(orderId);
            if (retrieved == null) {
                return "Failed to retrieve cart amount info";
            }
            
            // Clean up
            redisOrderCacheService.removeCartAmountInfo(orderId);
            
            return "Test successful! Cart amount info stored and retrieved without expiration";
            
        } catch (Exception e) {
            return "Test failed with exception: " + e.getMessage();
        }
    }
} 
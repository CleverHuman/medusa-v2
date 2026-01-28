package com.medusa.mall.service;

import com.medusa.mall.domain.vo.CartAmountInfo;

/**
 * Redis Order Cache Service Interface
 * Provides methods to store and retrieve order amount information in Redis
 */
public interface RedisOrderCacheService {
    
    /**
     * Store cart amount information in Redis with orderId as key
     * 
     * @param orderId the order ID as the cache key
     * @param cartAmountInfo the cart amount information to store
     * @return true if successful, false otherwise
     */
    boolean storeCartAmountInfo(String orderId, CartAmountInfo cartAmountInfo);
    
    /**
     * Store cart amount information in Redis with expiration time
     * 
     * @param orderId the order ID as the cache key
     * @param cartAmountInfo the cart amount information to store
     * @param timeout expiration time in seconds
     * @return true if successful, false otherwise
     */
    boolean storeCartAmountInfo(String orderId, CartAmountInfo cartAmountInfo, long timeout);
    
    /**
     * Retrieve cart amount information from Redis by orderId
     * 
     * @param orderId the order ID as the cache key
     * @return CartAmountInfo object, or null if not found
     */
    CartAmountInfo getCartAmountInfo(String orderId);
    
    /**
     * Check if cart amount information exists in Redis
     * 
     * @param orderId the order ID as the cache key
     * @return true if exists, false otherwise
     */
    boolean hasCartAmountInfo(String orderId);
    
    /**
     * Remove cart amount information from Redis
     * 
     * @param orderId the order ID as the cache key
     * @return true if successful, false otherwise
     */
    boolean removeCartAmountInfo(String orderId);
    
    /**
     * Get the remaining time to live for the cache entry
     * 
     * @param orderId the order ID as the cache key
     * @return time to live in seconds, -1 if key doesn't exist, -2 if key has no expiration
     */
    long getTimeToLive(String orderId);
} 
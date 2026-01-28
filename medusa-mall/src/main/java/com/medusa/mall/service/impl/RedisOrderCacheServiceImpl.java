package com.medusa.mall.service.impl;

import com.medusa.common.core.redis.RedisCache;
import com.medusa.mall.domain.vo.CartAmountInfo;
import com.medusa.mall.service.RedisOrderCacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Redis Order Cache Service Implementation
 * Provides Redis storage and retrieval functionality for order amount information
 */
@Service
public class RedisOrderCacheServiceImpl implements RedisOrderCacheService {
    
    private static final Logger log = LoggerFactory.getLogger(RedisOrderCacheServiceImpl.class);
    
    @Autowired
    private RedisCache redisCache;
    
    private static final String CACHE_KEY_PREFIX = "order:amount:";
    
    /**
     * Generate the full cache key with prefix
     * 
     * @param orderId the order ID
     * @return the full cache key
     */
    private String getCacheKey(String orderId) {
        return CACHE_KEY_PREFIX + orderId;
    }
    
    @Override
    public boolean storeCartAmountInfo(String orderId, CartAmountInfo cartAmountInfo) {
        try {
            String cacheKey = getCacheKey(orderId);
            redisCache.setCacheObject(cacheKey, cartAmountInfo);
            return true;
        } catch (Exception e) {
            log.error("Error storing cart amount info for order " + orderId + ": " + e.getMessage());
            return false;
        }
    }
    
    @Override
    public boolean storeCartAmountInfo(String orderId, CartAmountInfo cartAmountInfo, long timeout) {
        try {
            String cacheKey = getCacheKey(orderId);
            redisCache.setCacheObject(cacheKey, cartAmountInfo, (int) timeout, TimeUnit.SECONDS);
            return true;
        } catch (Exception e) {
            log.error("Error storing cart amount info for order " + orderId + " with timeout: " + e.getMessage());
            return false;
        }
    }
    
    @Override
    public CartAmountInfo getCartAmountInfo(String orderId) {
        try {
            String cacheKey = getCacheKey(orderId);
            return redisCache.getCacheObject(cacheKey);
        } catch (Exception e) {
            log.error("Error retrieving cart amount info for order " + orderId + ": " + e.getMessage());
            return null;
        }
    }
    
    @Override
    public boolean hasCartAmountInfo(String orderId) {
        try {
            String cacheKey = getCacheKey(orderId);
            return redisCache.hasKey(cacheKey);
        } catch (Exception e) {
            log.error("Error checking cart amount info existence for order " + orderId + ": " + e.getMessage());
            return false;
        }
    }
    
    @Override
    public boolean removeCartAmountInfo(String orderId) {
        try {
            String cacheKey = getCacheKey(orderId);
            return redisCache.deleteObject(cacheKey);
        } catch (Exception e) {
            log.error("Error removing cart amount info for order " + orderId + ": " + e.getMessage());
            return false;
        }
    }
    
    @Override
    public long getTimeToLive(String orderId) {
        try {
            String cacheKey = getCacheKey(orderId);
            return redisCache.getExpire(cacheKey);
        } catch (Exception e) {
            log.error("Error getting TTL for order " + orderId + ": " + e.getMessage());
            return -1;
        }
    }
} 
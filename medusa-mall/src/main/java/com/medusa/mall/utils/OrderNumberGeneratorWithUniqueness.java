package com.medusa.mall.utils;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.security.SecureRandom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 订单号生成器 - 带唯一性检查版本
 * 平台自营：0 + 6位随机数字 (如：0123456, 0789012)
 * VENDOR产品：3位VENDOR_ID + 6位随机数字 (如：001123456, 002789012)
 * 
 * 特性：
 * 1. 复杂算法生成，避免暴露订单数量信息
 * 2. 唯一性检查，避免重复订单号
 * 3. 重试机制，确保生成成功
 * 4. 线程安全
 */
public class OrderNumberGeneratorWithUniqueness {
    
    private static final Logger log = LoggerFactory.getLogger(OrderNumberGeneratorWithUniqueness.class);
    
    // 平台自营产品计数器
    private static final AtomicLong platformSequence = new AtomicLong(1);
    
    // VENDOR产品计数器 (VENDOR_ID -> 计数器)
    private static final Map<Integer, AtomicLong> vendorSequences = new ConcurrentHashMap<>();
    
    // 随机数生成器
    private static final SecureRandom secureRandom = new SecureRandom();
    
    // 最大序号（6位数字：1-999999）
    private static final long MAX_SEQUENCE = 999999;
    
    // 最大重试次数
    private static final int MAX_RETRY_COUNT = 10;
    
    // 用于混淆的常量
    private static final long MIXING_CONSTANT = 0x5DEECE66DL;
    private static final long ADDEND = 0xBL;
    private static final long MULTIPLIER = 0x5DEECE66DL;
    private static final long MASK = (1L << 48) - 1;

    // 订单号检查接口
    public interface OrderIdChecker {
        /**
         * 检查订单号是否已存在
         * @param orderId 订单号
         * @return 存在返回true，不存在返回false
         */
        boolean isOrderIdExists(String orderId);
    }

    /**
     * 生成平台自营订单号（带唯一性检查）
     * 格式：0 + 6位随机数字
     * 例如：0123456, 0789012, 0456789
     * @param checker 订单号检查器
     * @return 唯一的订单号
     */
    public static String generatePlatformOrderNumber(OrderIdChecker checker) {
        return generatePlatformOrderNumber(checker, MAX_RETRY_COUNT);
    }
    
    /**
     * 生成平台自营订单号（带唯一性检查和重试次数限制）
     * @param checker 订单号检查器
     * @param maxRetries 最大重试次数
     * @return 唯一的订单号
     */
    public static String generatePlatformOrderNumber(OrderIdChecker checker, int maxRetries) {
        for (int retryCount = 0; retryCount < maxRetries; retryCount++) {
            String orderNumber = generateSinglePlatformOrderNumber();
            
            if (!checker.isOrderIdExists(orderNumber)) {
                log.debug("Generated unique platform order number: {} (attempt: {})", orderNumber, retryCount + 1);
                return orderNumber;
            }
            
            log.warn("Generated duplicate platform order number: {} (attempt: {}), retrying...", orderNumber, retryCount + 1);
        }
        
        throw new RuntimeException("Failed to generate unique platform order number after " + maxRetries + " attempts");
    }
    
    /**
     * 生成VENDOR订单号（带唯一性检查）
     * 格式：3位VENDOR_ID + 6位随机数字
     * 例如：001123456, 001789012, 002345678
     * @param vendorId VENDOR ID (1-999)
     * @param checker 订单号检查器
     * @return 唯一的订单号
     */
    public static String generateVendorOrderNumber(int vendorId, OrderIdChecker checker) {
        return generateVendorOrderNumber(vendorId, checker, MAX_RETRY_COUNT);
    }
    
    /**
     * 生成VENDOR订单号（带唯一性检查和重试次数限制）
     * @param vendorId VENDOR ID (1-999)
     * @param checker 订单号检查器
     * @param maxRetries 最大重试次数
     * @return 唯一的订单号
     */
    public static String generateVendorOrderNumber(int vendorId, OrderIdChecker checker, int maxRetries) {
        if (vendorId < 1 || vendorId > 999) {
            throw new IllegalArgumentException("VENDOR ID must be between 1 and 999, got: " + vendorId);
        }
        
        for (int retryCount = 0; retryCount < maxRetries; retryCount++) {
            String orderNumber = generateSingleVendorOrderNumber(vendorId);
            
            if (!checker.isOrderIdExists(orderNumber)) {
                log.debug("Generated unique vendor order number: {} for vendor {} (attempt: {})", orderNumber, vendorId, retryCount + 1);
                return orderNumber;
            }
            
            log.warn("Generated duplicate vendor order number: {} for vendor {} (attempt: {}), retrying...", orderNumber, vendorId, retryCount + 1);
        }
        
        throw new RuntimeException("Failed to generate unique vendor order number for vendor " + vendorId + " after " + maxRetries + " attempts");
    }
    
    /**
     * 生成订单号（兼容旧接口，带唯一性检查）
     * 默认生成平台自营订单号
     * @param checker 订单号检查器
     * @return 唯一的订单号
     */
    public static String generateOrderNumber(OrderIdChecker checker) {
        return generatePlatformOrderNumber(checker);
    }
    
    /**
     * 根据产品来源生成订单号（带唯一性检查）
     * @param productOrigin 产品来源：0-平台自营，1-VENDOR产品
     * @param originId VENDOR ID（如果是VENDOR产品）
     * @param checker 订单号检查器
     * @return 唯一的订单号
     */
    public static String generateOrderNumber(int productOrigin, Integer originId, OrderIdChecker checker) {
        if (productOrigin == 0) {
            return generatePlatformOrderNumber(checker);
        } else {
            if (originId == null) {
                throw new IllegalArgumentException("VENDOR ID cannot be null for VENDOR products");
            }
            return generateVendorOrderNumber(originId, checker);
        }
    }
    
    /**
     * 生成单个平台自营订单号（不检查唯一性）
     * @return 订单号
     */
    private static String generateSinglePlatformOrderNumber() {
        long currentSequence = platformSequence.getAndIncrement();
        if (currentSequence > MAX_SEQUENCE) {
            platformSequence.set(1);
            currentSequence = platformSequence.getAndIncrement();
        }
        
        // 使用复杂算法生成6位数字，避免暴露序列信息
        long randomNumber = generateComplexNumber(currentSequence);
        
        return "0" + String.format("%06d", randomNumber);
    }
    
    /**
     * 生成单个VENDOR订单号（不检查唯一性）
     * @param vendorId VENDOR ID
     * @return 订单号
     */
    private static String generateSingleVendorOrderNumber(int vendorId) {
        AtomicLong sequence = vendorSequences.computeIfAbsent(vendorId, k -> new AtomicLong(1));
        
        long currentSequence = sequence.getAndIncrement();
        if (currentSequence > MAX_SEQUENCE) {
            sequence.set(1);
            currentSequence = sequence.getAndIncrement();
        }
        
        // 使用复杂算法生成6位数字，避免暴露序列信息
        long randomNumber = generateComplexNumber(currentSequence, vendorId);
        
        return String.format("%03d%06d", vendorId, randomNumber);
    }
    
    /**
     * 使用复杂算法生成6位数字（平台自营）
     * 结合序列号、时间戳、随机数等多种因素
     * @param sequence 序列号
     * @return 6位随机数字
     */
    private static long generateComplexNumber(long sequence) {
        // 获取当前时间戳
        long timestamp = System.currentTimeMillis();
        
        // 使用线性同余法生成伪随机数
        long seed = (sequence * MIXING_CONSTANT + timestamp) & MASK;
        
        // 多次迭代增加复杂度
        for (int i = 0; i < 5; i++) {
            seed = (seed * MULTIPLIER + ADDEND) & MASK;
        }
        
        // 结合安全随机数
        long secureRandomPart = secureRandom.nextInt(1000);
        
        // 最终计算
        long result = (seed + secureRandomPart + sequence * 7) % 1000000;
        
        // 确保结果在1-999999范围内
        if (result == 0) result = 1;
        if (result > 999999) result = result % 999999 + 1;
        
        return result;
    }
    
    /**
     * 使用复杂算法生成6位数字（VENDOR产品）
     * 结合序列号、VENDOR_ID、时间戳、随机数等多种因素
     * @param sequence 序列号
     * @param vendorId VENDOR ID
     * @return 6位随机数字
     */
    private static long generateComplexNumber(long sequence, int vendorId) {
        // 获取当前时间戳
        long timestamp = System.currentTimeMillis();
        
        // 使用VENDOR_ID作为额外的混淆因子
        long vendorSeed = vendorId * 1000000L + sequence;
        
        // 使用线性同余法生成伪随机数
        long seed = (vendorSeed * MIXING_CONSTANT + timestamp) & MASK;
        
        // 多次迭代增加复杂度
        for (int i = 0; i < 7; i++) {
            seed = (seed * MULTIPLIER + ADDEND) & MASK;
        }
        
        // 结合安全随机数
        long secureRandomPart = secureRandom.nextInt(1000);
        
        // 最终计算
        long result = (seed + secureRandomPart + sequence * 13 + vendorId * 17) % 1000000;
        
        // 确保结果在1-999999范围内
        if (result == 0) result = 1;
        if (result > 999999) result = result % 999999 + 1;
        
        return result;
    }
    
    /**
     * 验证订单号格式
     * @param orderNumber 订单号
     * @return 是否有效
     */
    public static boolean isValidOrderNumber(String orderNumber) {
        if (orderNumber == null || orderNumber.isEmpty()) {
            return false;
        }
        
        char firstChar = orderNumber.charAt(0);
        if (firstChar == '0') {
            // 平台自营产品：0 + 6位数字（总共7位）
            return orderNumber.length() == 7 && orderNumber.substring(1).matches("\\d{6}");
        } else {
            // VENDOR产品：3位VENDOR_ID + 6位数字（总共9位）
            return orderNumber.length() == 9 && orderNumber.matches("\\d{9}");
        }
    }
} 
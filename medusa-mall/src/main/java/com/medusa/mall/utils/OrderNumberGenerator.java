package com.medusa.mall.utils;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.security.SecureRandom;
import java.util.Random;

/**
 * 订单号生成器 - 扩展版本（防猜测）
 * 平台自营：0 + 6位随机数字 (如：0123456, 0789012)
 * VENDOR产品：3位VENDOR_ID + 6位随机数字 (如：001123456, 002789012)
 * 
 * 6位数字使用复杂算法生成，避免暴露订单数量信息
 */
public class OrderNumberGenerator {
    // 平台自营产品计数器
    private static final AtomicLong platformSequence = new AtomicLong(1);
    
    // VENDOR产品计数器 (VENDOR_ID -> 计数器)
    private static final Map<Integer, AtomicLong> vendorSequences = new ConcurrentHashMap<>();
    
    // 随机数生成器
    private static final SecureRandom secureRandom = new SecureRandom();
    
    // 最大序号（6位数字：1-999999）
    private static final long MAX_SEQUENCE = 999999;
    
    // 用于混淆的常量
    private static final long MIXING_CONSTANT = 0x5DEECE66DL;
    private static final long ADDEND = 0xBL;
    private static final long MULTIPLIER = 0x5DEECE66DL;
    private static final long MASK = (1L << 48) - 1;

    /**
     * 生成平台自营订单号
     * 格式：0 + 6位随机数字
     * 例如：0123456, 0789012, 0456789
     * @return 订单号
     */
    public static String generatePlatformOrderNumber() {
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
     * 生成VENDOR订单号
     * 格式：3位VENDOR_ID + 6位随机数字
     * 例如：001123456, 001789012, 002345678
     * @param vendorId VENDOR ID (1-999)
     * @return 订单号
     */
    public static String generateVendorOrderNumber(int vendorId) {
        if (vendorId < 1 || vendorId > 999) {
            throw new IllegalArgumentException("VENDOR ID must be between 1 and 999, got: " + vendorId);
        }
        
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
     * 生成订单号（兼容旧接口）
     * 默认生成平台自营订单号
     * @return 订单号
     */
    public static String generateOrderNumber() {
        return generatePlatformOrderNumber();
    }
    
    /**
     * 根据产品来源生成订单号
     * @param productOrigin 产品来源：0-平台自营，1-VENDOR产品
     * @param originId VENDOR ID（如果是VENDOR产品）
     * @return 订单号
     */
    public static String generateOrderNumber(int productOrigin, Integer originId) {
        if (productOrigin == 0) {
            return generatePlatformOrderNumber();
        } else {
            if (originId == null) {
                throw new IllegalArgumentException("VENDOR ID cannot be null for VENDOR products");
            }
            return generateVendorOrderNumber(originId);
        }
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
     * 解析订单号（注意：由于使用了复杂算法，无法准确解析出原始序列号）
     * @param orderNumber 订单号
     * @return 订单号信息
     */
    public static OrderNumberInfo parseOrderNumber(String orderNumber) {
        if (orderNumber == null) {
            throw new IllegalArgumentException("Order number cannot be null");
        }
        
        char firstChar = orderNumber.charAt(0);
        if (firstChar == '0') {
            // 平台自营产品：0 + 6位数字（总共7位）
            if (orderNumber.length() != 7) {
                throw new IllegalArgumentException("Invalid platform order number format: " + orderNumber + 
                    " (expected 7 digits)");
            }
            String sequenceStr = orderNumber.substring(1);
            try {
                long sequence = Long.parseLong(sequenceStr);
                return new OrderNumberInfo(0, null, sequence, orderNumber);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid platform order number format: " + orderNumber);
            }
        } else {
            // VENDOR产品：3位VENDOR_ID + 6位数字（总共9位）
            if (orderNumber.length() != 9) {
                throw new IllegalArgumentException("Invalid vendor order number format: " + orderNumber + 
                    " (expected 9 digits)");
            }
            try {
                int vendorId = Integer.parseInt(orderNumber.substring(0, 3));
                long sequence = Long.parseLong(orderNumber.substring(3));
                return new OrderNumberInfo(1, vendorId, sequence, orderNumber);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid vendor order number format: " + orderNumber);
            }
        }
    }
    
    /**
     * 验证订单号格式
     * @param orderNumber 订单号
     * @return 是否有效
     */
    public static boolean isValidOrderNumber(String orderNumber) {
        try {
            parseOrderNumber(orderNumber);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 获取订单号类型
     * @param orderNumber 订单号
     * @return 0-平台自营，1-VENDOR产品
     */
    public static int getOrderNumberType(String orderNumber) {
        if (orderNumber == null) {
            throw new IllegalArgumentException("Order number cannot be null");
        }
        
        char firstChar = orderNumber.charAt(0);
        if (firstChar == '0') {
            // 平台自营：7位数字
            if (orderNumber.length() != 7) {
                throw new IllegalArgumentException("Invalid platform order number length: " + orderNumber);
            }
            return 0;
        } else {
            // VENDOR产品：9位数字
            if (orderNumber.length() != 9) {
                throw new IllegalArgumentException("Invalid vendor order number length: " + orderNumber);
            }
            return 1;
        }
    }
    
    /**
     * 订单号信息
     */
    public static class OrderNumberInfo {
        private final int productOrigin;
        private final Integer originId;
        private final long sequence;
        private final String fullOrderNumber;
        
        public OrderNumberInfo(int productOrigin, Integer originId, long sequence, String fullOrderNumber) {
            this.productOrigin = productOrigin;
            this.originId = originId;
            this.sequence = sequence;
            this.fullOrderNumber = fullOrderNumber;
        }
        
        public int getProductOrigin() {
            return productOrigin;
        }
        
        public Integer getOriginId() {
            return originId;
        }
        
        public long getSequence() {
            return sequence;
        }
        
        public String getFullOrderNumber() {
            return fullOrderNumber;
        }
        
        @Override
        public String toString() {
            if (productOrigin == 0) {
                return "PlatformOrder{sequence=" + sequence + ", orderNumber='" + fullOrderNumber + "'}";
            } else {
                return "VendorOrder{vendorId=" + originId + ", sequence=" + sequence + ", orderNumber='" + fullOrderNumber + "'}";
            }
        }
    }
} 
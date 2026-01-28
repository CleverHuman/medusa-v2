package com.medusa.mall.service;

import java.math.BigDecimal;

/**
 * Vendor Bond & Level Service Interface
 */
public interface IVendorBondService {
    
    /**
     * Calculate maximum sales limit for vendor (Bond × Level)
     * 
     * @param vendorId Vendor ID
     * @return Maximum sales limit in USD
     */
    BigDecimal calculateMaxSalesLimit(Long vendorId);

    /**
     * Check if order amount exceeds vendor's sales limit
     * 
     * @param vendorId Vendor ID
     * @param orderAmount Order amount in USD
     * @return true if within limit, false if exceeds
     */
    boolean checkSalesLimit(Long vendorId, BigDecimal orderAmount);

    /**
     * Calculate current pending sales amount for vendor
     * (sum of all pending/paid orders that haven't been shipped yet)
     * 
     * @param vendorId Vendor ID
     * @return Current pending sales amount
     */
    BigDecimal calculateCurrentPendingSales(Long vendorId);

    /**
     * Update sales points and level after order is shipped
     * 1 point per $1 sold
     * Level calculation:
     *   Level 1: 0-999 points
     *   Level 2: 1,000-9,999 points
     *   Level 3: 10,000-99,999 points
     *   Level 4: 100,000-999,999 points
     *   Level 5: 1,000,000+ points
     * 
     * @param vendorId Vendor ID
     * @param orderId Order ID
     * @param orderAmount Order amount in USD
     * @return true if level upgraded, false otherwise
     */
    boolean updateSalesPointsAndLevel(Long vendorId, String orderId, BigDecimal orderAmount);

    /**
     * Get vendor level
     * 
     * @param vendorId Vendor ID
     * @return Vendor level (default 1 if not set)
     */
    Integer getVendorLevel(Long vendorId);

    /**
     * Get vendor bond amount
     * 
     * @param vendorId Vendor ID
     * @return Bond amount in USD
     */
    BigDecimal getVendorBond(Long vendorId);

    /**
     * Get vendor sales points
     * 
     * @param vendorId Vendor ID
     * @return Sales points
     */
    Long getVendorSalesPoints(Long vendorId);
}


package com.medusa.mall.service;

import com.medusa.mall.domain.order.ShippingMethod;
import java.util.List;

/**
 * Shipping Method Service interface
 */
public interface IShippingMethodService {
    /**
     * Query shipping method list
     * 
     * @param shippingMethod Shipping method filter
     * @return Shipping method list
     */
    public List<ShippingMethod> selectShippingMethodList(ShippingMethod shippingMethod);
    
    /**
     * Query shipping method by code
     * 
     * @param code Shipping method code
     * @return Shipping method
     */
    public ShippingMethod selectShippingMethodByCode(String code);
    
    /**
     * Get all active shipping methods
     * 
     * @return Active shipping method list
     */
    public List<ShippingMethod> selectActiveShippingMethods();
    
    /**
     * Insert shipping method
     * 
     * @param shippingMethod Shipping method
     * @return Result
     */
    public int insertShippingMethod(ShippingMethod shippingMethod);
    
    /**
     * Update shipping method
     * 
     * @param shippingMethod Shipping method
     * @return Result
     */
    public int updateShippingMethod(ShippingMethod shippingMethod);
    
    /**
     * Delete shipping method by code
     * 
     * @param code Shipping method code
     * @return Result
     */
    public int deleteShippingMethodByCode(String code);
} 
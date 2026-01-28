package com.medusa.mall.mapper;

import com.medusa.mall.domain.order.ShippingMethod;
import java.util.List;

public interface ShippingMethodMapper {
    public List<ShippingMethod> selectShippingMethodList(ShippingMethod shippingMethod);
    
    public ShippingMethod selectShippingMethodByCode(String code);
    
    public int insertShippingMethod(ShippingMethod shippingMethod);
    
    public int updateShippingMethod(ShippingMethod shippingMethod);
    
    public int deleteShippingMethodByCode(String code);
} 
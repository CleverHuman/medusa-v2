package com.medusa.mall.service.impl;

import com.medusa.mall.domain.order.ShippingMethod;
import com.medusa.mall.mapper.ShippingMethodMapper;
import com.medusa.mall.service.IShippingMethodService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Shipping Method Service implementation
 */
@Service
public class ShippingMethodServiceImpl implements IShippingMethodService {

    @Autowired
    private ShippingMethodMapper shippingMethodMapper;

    @Override
    public List<ShippingMethod> selectShippingMethodList(ShippingMethod shippingMethod) {
        return shippingMethodMapper.selectShippingMethodList(shippingMethod);
    }

    @Override
    public ShippingMethod selectShippingMethodByCode(String code) {
        return shippingMethodMapper.selectShippingMethodByCode(code);
    }

    @Override
    public List<ShippingMethod> selectActiveShippingMethods() {
        ShippingMethod filter = new ShippingMethod();
        filter.setStatus(1); // Only active methods
        return shippingMethodMapper.selectShippingMethodList(filter);
    }

    @Override
    public int insertShippingMethod(ShippingMethod shippingMethod) {
        return shippingMethodMapper.insertShippingMethod(shippingMethod);
    }

    @Override
    public int updateShippingMethod(ShippingMethod shippingMethod) {
        return shippingMethodMapper.updateShippingMethod(shippingMethod);
    }

    @Override
    public int deleteShippingMethodByCode(String code) {
        return shippingMethodMapper.deleteShippingMethodByCode(code);
    }
} 
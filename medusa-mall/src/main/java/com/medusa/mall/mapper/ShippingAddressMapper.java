package com.medusa.mall.mapper;

import com.medusa.mall.domain.order.ShippingAddress;
import org.apache.ibatis.annotations.Param;

public interface ShippingAddressMapper {
    int insertShippingAddress(ShippingAddress addresse);
    ShippingAddress selectShippingAddressByOrderId(String orderId);

    int updateShippingInfoByOrderId(ShippingAddress shippingAddress);
}
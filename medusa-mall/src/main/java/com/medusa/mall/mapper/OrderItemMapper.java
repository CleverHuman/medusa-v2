package com.medusa.mall.mapper;

import com.medusa.mall.domain.order.OrderItem;
import java.util.List;

public interface OrderItemMapper {
    int insertOrderItem(OrderItem orderItem);
    List<OrderItem> selectOrderItemsByOrderId(String orderId);
    int updateOrderItem(OrderItem orderItem);
    // 可根据需要添加批量插入、查询等方法
} 
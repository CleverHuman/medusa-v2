package com.medusa.mall.service;

import com.medusa.mall.domain.order.Order;

/**
 * Customer Order Notification Service Interface
 * 客户订单通知服务接口
 * 
 * @author medusa
 * @date 2025-12-04
 */
public interface ICustomerOrderNotificationService {
    
    /**
     * 通知客户订单被Vendor拒绝
     * 
     * @param order 订单信息
     * @param rejectionReason 拒绝原因
     */
    void notifyOrderRejected(Order order, String rejectionReason);
    
    /**
     * 通知客户订单被Vendor接受
     * 
     * @param order 订单信息
     */
    void notifyOrderAccepted(Order order);
    
    /**
     * 通知客户订单已发货
     * 
     * @param order 订单信息
     * @param trackingNumber 跟踪号
     */
    void notifyOrderShipped(Order order, String trackingNumber);
    
    /**
     * 通知客户订单状态变更
     * 
     * @param order 订单信息
     * @param oldStatus 旧状态
     * @param newStatus 新状态
     * @param reason 变更原因（可选）
     */
    void notifyOrderStatusChanged(Order order, Integer oldStatus, Integer newStatus, String reason);
}


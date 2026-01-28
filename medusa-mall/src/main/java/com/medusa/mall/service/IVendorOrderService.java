package com.medusa.mall.service;

import com.medusa.mall.domain.order.Order;
import java.util.List;

/**
 * Vendor Order Service Interface
 */
public interface IVendorOrderService {
    /**
     * Query vendor's own orders
     * 
     * @param vendorId Vendor ID
     * @return Order list
     */
    List<Order> selectVendorOrderList(Long vendorId);

    /**
     * Query order by ID (with vendor authorization check)
     * 
     * @param orderId Order ID
     * @param vendorId Vendor ID
     * @return Order
     */
    Order selectVendorOrderById(String orderId, Long vendorId);

    /**
     * Accept order (vendor)
     * 
     * @param orderId Order ID
     * @param vendorId Vendor ID
     * @return Result
     */
    int acceptOrder(String orderId, Long vendorId);

    /**
     * Reject order (vendor)
     * 
     * @param orderId Order ID
     * @param vendorId Vendor ID
     * @param reason Rejection reason
     * @return Result
     */
    int rejectOrder(String orderId, Long vendorId, String reason);

    /**
     * Mark order as shipped (vendor)
     * 
     * @param orderId Order ID
     * @param vendorId Vendor ID
     * @param trackingNumber Tracking number
     * @return Result
     */
    int shipOrder(String orderId, Long vendorId, String trackingNumber);
}


package com.medusa.mall.service.impl;

import com.medusa.mall.domain.order.Order;
import com.medusa.mall.domain.order.ShippingAddress;
import com.medusa.mall.mapper.OrderMapper;
import com.medusa.mall.service.IOrderService;
import com.medusa.mall.service.IVendorOrderService;
import com.medusa.mall.service.IVendorBondService;
import com.medusa.mall.service.IVendorWithdrawalService;
import com.medusa.mall.service.ICustomerOrderNotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Vendor Order Service Implementation
 */
@Service
public class VendorOrderServiceImpl implements IVendorOrderService {

    private static final Logger log = LoggerFactory.getLogger(VendorOrderServiceImpl.class);

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private IOrderService orderService;

    @Autowired
    private IVendorBondService vendorBondService;

    @Autowired
    private IVendorWithdrawalService vendorWithdrawalService;
    
    @Autowired
    private ICustomerOrderNotificationService customerNotificationService;

    @Override
    public List<Order> selectVendorOrderList(Long vendorId) {
        Order query = new Order();
        query.setVendorId(vendorId);
        return orderMapper.selectOrderList(query);
    }

    @Override
    public Order selectVendorOrderById(String orderId, Long vendorId) {
        Order order = orderMapper.selectOrderById(orderId);
        if (order == null) {
            return null;
        }
        // Check if order belongs to vendor
        if (order.getVendorId() != null && order.getVendorId().equals(vendorId)) {
            return order;
        }
        return null;
    }

    @Override
    @Transactional
    public int acceptOrder(String orderId, Long vendorId) {
        Order order = orderMapper.selectOrderById(orderId);
        if (order == null || order.getVendorId() == null || !order.getVendorId().equals(vendorId)) {
            throw new RuntimeException("Order not found or access denied");
        }
        
        // Allow accepting orders in Pending (0) or Paid (1) status
        // Pending (0): order created but not yet paid
        // Paid (1): order confirmed/paid, vendor can still accept it
        Integer status = order.getStatus();
        if (status == null || (status != 0 && status != 1)) {
            throw new RuntimeException("Order can only be accepted when status is Pending (0) or Paid (1). Current status: " + status);
        }
        
        // Update order status to vendor_accepted (using status 6 as vendor_accepted)
        order.setStatus(6); // vendor_accepted
        log.info("Vendor {} accepted order {}, previous status: {}", vendorId, orderId, status);
        return orderMapper.updateOrder(order);
    }

    @Override
    @Transactional
    public int rejectOrder(String orderId, Long vendorId, String reason) {
        Order order = orderMapper.selectOrderById(orderId);
        if (order == null || order.getVendorId() == null || !order.getVendorId().equals(vendorId)) {
            throw new RuntimeException("Order not found or access denied");
        }
        
        // Allow rejecting orders in Pending (0) or Paid (1) status
        // Pending (0): order created but not yet paid
        // Paid (1): order confirmed/paid, vendor can still reject it (will trigger refund process)
        Integer status = order.getStatus();
        if (status == null || (status != 0 && status != 1)) {
            throw new RuntimeException("Order can only be rejected when status is Pending (0) or Paid (1). Current status: " + status);
        }
        
        // Update order status to cancelled (4)
        order.setStatus(4); // cancelled
        String rejectionNote = "Vendor rejection" + (reason != null && !reason.trim().isEmpty() ? ": " + reason : "");
        order.setRemark((order.getRemark() != null ? order.getRemark() + "\n" : "") + rejectionNote);
        log.info("Vendor {} rejected order {}, previous status: {}, reason: {}", vendorId, orderId, status, reason);
        
        int result = orderMapper.updateOrder(order);
        
        // Notify customer that order has been rejected
        if (result > 0) {
            try {
                customerNotificationService.notifyOrderRejected(order, reason);
                log.info("Sent rejection notification to customer for order {}", orderId);
            } catch (Exception e) {
                log.error("Failed to send rejection notification to customer for order {}: {}", orderId, e.getMessage(), e);
                // Don't fail the rejection if notification fails, but log the error
            }
        }
        
        return result;
    }

    @Override
    @Transactional
    public int shipOrder(String orderId, Long vendorId, String trackingNumber) {
        Order order = orderMapper.selectOrderById(orderId);
        if (order == null || order.getVendorId() == null || !order.getVendorId().equals(vendorId)) {
            throw new RuntimeException("Order not found or access denied");
        }
        
        // Check if order is paid or vendor_accepted
        if (order.getStatus() != 1 && order.getStatus() != 6) {
            throw new RuntimeException("Order cannot be shipped in current status");
        }
        
        // Update shipping info
        ShippingAddress shipping = new ShippingAddress();
        shipping.setOrderId(orderId);
        shipping.setShippingNumber(trackingNumber);
        orderService.updateShippingInfoByOrderId(shipping);
        
        // Update order status to shipped (3)
        order.setStatus(3); // shipped
        int result = orderMapper.updateOrder(order);
        
        // Update vendor sales points and level after order is shipped
        if (result > 0 && order.getTotalAmount() != null) {
            BigDecimal orderAmount = order.getTotalAmount();
            
            // Update sales points and level
            vendorBondService.updateSalesPointsAndLevel(vendorId, orderId, orderAmount);
            
            // Add order amount to pending balance (will be released based on vendor level)
            try {
                vendorWithdrawalService.addPendingBalanceOnShipment(vendorId, Long.parseLong(orderId), orderAmount);
                log.info("Added pending balance for vendor {}, order {}, amount {}", vendorId, orderId, orderAmount);
            } catch (Exception e) {
                log.error("Failed to add pending balance for vendor {}, order {}", vendorId, orderId, e);
                // Don't fail the shipment if balance update fails, but log the error
            }
        }
        
        return result;
    }
}


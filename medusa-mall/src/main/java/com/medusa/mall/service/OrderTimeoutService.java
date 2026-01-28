package com.medusa.mall.service;

import com.medusa.mall.domain.order.Order;
import com.medusa.mall.domain.order.Payment;
import com.medusa.mall.service.impl.InventoryLockServiceImpl;
import com.medusa.mall.service.IOrderService;
import com.medusa.mall.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Date;

import com.medusa.mall.mapper.CouponMapper;
import com.medusa.mall.mapper.CouponHistoryMapper;
import com.medusa.mall.domain.coupon.CouponHistory;

/**
 * 订单超时服务
 * Processing expired order的库存释放和订单状态更新
 */
@Service
public class OrderTimeoutService {
    
    private static final Logger log = LoggerFactory.getLogger(OrderTimeoutService.class);
    
    @Autowired
    private InventoryLockServiceImpl inventoryLockService;
    
    @Autowired
    private IOrderService orderService;
    
    @Autowired
    private PaymentService paymentService;
    
    @Autowired
    private CouponMapper couponMapper;
    
    @Autowired
    private CouponHistoryMapper couponHistoryMapper;
    
    @Autowired
    private WebClient webClient;
    
    @Value("${btcpay.url}")
    private String btcpayUrl;
    
    @Value("${btcpay.api.key}")
    private String btcpayApiKey;
    
    @Value("${btcpay.store.id}")
    private String btcpayStoreId;
    
    /**
     * Processing expired order
     * 1. 释放库存锁定
     * 2. 更新订单状态为已取消
     * 3. 更新支付状态为已取消
     */
    @Transactional
    public void processExpiredOrders() {
        try {
            log.info("Starting to process expired orders...");
            
            // 获取超时的订单ID列表
            List<String> expiredOrderIds = inventoryLockService.getExpiredOrderIds();
            
            if (expiredOrderIds.isEmpty()) {
                log.info("No expired orders to process");
                return;
            }
            
            log.info("Found {} expired orders to process", expiredOrderIds.size());
            
            for (String orderId : expiredOrderIds) {
                try {
                    processExpiredOrder(orderId);
                } catch (Exception e) {
                    log.error("Failed to process expired order，订单ID: {}", orderId, e);
                }
            }
            
            log.info("Expired order processing completed");
            
        } catch (Exception e) {
            log.error("Exception occurred while processing expired orders", e);
        }
    }
    
    /**
     * 处理单个超时订单
     */
    @Transactional
    public void processExpiredOrder(String orderId) {
        log.info("Processing expired order: {}", orderId);
        
        try {
            // 首先检查支付状态
            Payment payment = paymentService.getPaymentByOrderId(orderId);
            if (payment != null && payment.getPayStatus() == 1) {
                log.info("订单 {} 已支付，跳过取消处理", orderId);
                return;
            }
            
            // 检查订单状态
            Order order = orderService.selectOrderById(orderId);
            if (order != null && order.getStatus() == 1) {
                log.info("订单 {} 状态已为已支付，跳过取消处理", orderId);
                return;
            }
            
            // Checking BTCPay invoice status
            if (payment != null && payment.getPaymentNo() != null) {
                try {
                    String invoiceId = payment.getPaymentNo();
                    log.info("Checking BTCPay invoice status: {}", invoiceId);
                    
                    // 调用BTCPay API检查发票状态
                    Map<String, Object> invoice = webClient.get()
                        .uri(btcpayUrl + "/api/v1/stores/" + btcpayStoreId + "/invoices/" + invoiceId)
                        .header("Authorization", "token " + btcpayApiKey)
                        .retrieve()
                        .bodyToMono(Map.class)
                        .block();
                    
                    if (invoice != null) {
                        String status = (String) invoice.get("status");
                        log.info("BTCPay invoice status: {}", status);
                        
                        if ("Settled".equals(status)) {
                            log.info("BTCPay invoice settled, skipping order cancellation");
                            return;
                        }
                        
                        // 检查是否正在处理中
                        if ("Processing".equals(status) || "New".equals(status)) {
                            log.info("BTCPay invoice processing, skipping order cancellation");
                            return;
                        }
                        
                        // 检查是否已确认
                        if ("Confirmed".equals(status)) {
                            log.info("BTCPay invoice confirmed, skipping order cancellation");
                            return;
                        }
                    } else {
                        log.warn("Cannot get BTCPay invoice information: {}", invoiceId);
                    }
                } catch (Exception e) {
                    log.warn("Checking BTCPay invoice status失败，继续处理订单取消: {}", e.getMessage());
                }
            }
            
            // 1. 释放库存锁定
            boolean inventoryReleased = inventoryLockService.releaseInventory(orderId);
            if (!inventoryReleased) {
                log.warn("Failed to release inventory，订单ID: {}", orderId);
            }
            
            // 2. 更新订单状态为已取消
            if (order != null) {
                order.setStatus(4); // 4表示已取消
                int orderUpdateResult = orderService.updateOrder(order);
                if (orderUpdateResult > 0) {
                    log.info("Order status updated to cancelled, Order ID: {}", orderId);
                } else {
                    log.warn("Order status update failed, Order ID: {}", orderId);
                }
                
                // 3. 如果订单使用了优惠券，恢复优惠券使用次数
                if (order.getCouponId() != null) {
                    try {
                        // 减少优惠券使用次数
                        int updateResult = couponMapper.decrementUsedCount(order.getCouponId());
                        if (updateResult > 0) {
                            log.info("Successfully reduced coupon usage count, Coupon ID: {}", order.getCouponId());
                        } else {
                            log.warn("Failed to reduce coupon usage count, Coupon ID: {}", order.getCouponId());
                        }
                        
                        // 更新优惠券历史记录状态为已过期
                        CouponHistory couponHistory = new CouponHistory();
                        couponHistory.setCouponId(order.getCouponId());
                        couponHistory.setMemberId(order.getMemberId());
                        couponHistory.setOrderId(Long.valueOf(order.getId()));
                        couponHistory.setOrderSn(order.getOrderSn());
                        couponHistory.setUseStatus(2); // 2表示已过期
                        couponHistory.setUseTime(new Date());
                        couponHistory.setCreateTime(new Date());
                        couponHistory.setUpdateTime(new Date());
                        couponHistory.setRemark("Order cancelled - coupon restored");
                        
                        int historyResult = couponHistoryMapper.insertCouponHistory(couponHistory);
                        if (historyResult > 0) {
                            log.info("Successfully added coupon expiry history, Coupon ID: {}", order.getCouponId());
                        } else {
                            log.warn("Failed to add coupon expiry history, Coupon ID: {}", order.getCouponId());
                        }
                        
                    } catch (Exception e) {
                        log.error("Exception occurred while processing coupon restoration, Order ID: {}, Coupon ID: {}", orderId, order.getCouponId(), e);
                        // 不抛出异常，避免影响订单取消流程
                    }
                }
            } else {
                log.warn("Order not found, Order ID: {}", orderId);
            }
            
            // 4. 更新支付状态为已取消
            if (payment != null) {
                payment.setPayStatus(2); // 2表示已取消
                Payment updatedPayment = paymentService.updatePaymentStatus(payment);
                if (updatedPayment != null) {
                    log.info("Payment status updated to cancelled, Order ID: {}", orderId);
                } else {
                    log.warn("Payment status update failed, Order ID: {}", orderId);
                }
            } else {
                log.warn("Payment record not found, Order ID: {}", orderId);
            }
            
            log.info("Expired order processing completed，订单ID: {}", orderId);
            
        } catch (Exception e) {
            log.error("Exception occurred while processing expired orders，订单ID: {}", orderId, e);
            throw e;
        }
    }
    
    /**
     * 检查订单是否超时
     */
    public boolean isOrderExpired(String orderId) {
        return inventoryLockService.getInventoryLockInfo(orderId).get("isExpired") == Boolean.TRUE;
    }
    
    /**
     * 获取订单剩余支付时间（毫秒）
     */
    public long getRemainingTime(String orderId) {
        Map<String, Object> lockInfo = inventoryLockService.getInventoryLockInfo(orderId);
        Object remainingTime = lockInfo.get("remainingTime");
        return remainingTime != null ? (Long) remainingTime : 0;
    }
} 
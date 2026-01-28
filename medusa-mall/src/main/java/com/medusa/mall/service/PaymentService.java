package com.medusa.mall.service;

import com.medusa.mall.domain.order.Payment;
import java.util.List;

public interface PaymentService {
    /**
     * 创建支付记录
     */
    Payment createPayment(Payment payment);

    /**
     * 根据订单ID查询支付记录
     */
    Payment getPaymentByOrderId(String orderId);

    /**
     * 更新支付状态
     */
    Payment updatePaymentStatus(Payment payment);
    
    /**
     * 获取待支付的支付记录
     */
    List<Payment> getPendingPayments();
    
    /**
     * 获取过期的支付记录
     */
    List<Payment> getExpiredPayments();
    
    /**
     * 获取待支付的NOWPayments支付记录
     */
    List<Payment> getPendingNowPaymentsPayments();
    
    /**
     * 获取过期的NOWPayments支付记录
     */
    List<Payment> getExpiredNowPaymentsPayments();

    /**
     * 验证NOWPayments支付状态并更新订单
     */
    boolean verifyNowPaymentsPayment(String paymentId, String orderId);
} 
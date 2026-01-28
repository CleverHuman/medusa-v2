package com.medusa.mall.service;

import com.medusa.mall.domain.order.Payment;
import java.util.Map;

public interface NowPaymentsService {
    /**
     * 创建 NOWPayments 支付
     */
    Map<String, Object> createPayment(Payment payment);
    
    /**
     * 获取支付状态
     */
    Map<String, Object> getPaymentStatus(String paymentId);
    
    /**
     * 获取支付历史
     */
    Map<String, Object> getPaymentHistory(String paymentId);
    
    // 移除验证方法，业务逻辑移到PaymentServiceImpl
} 
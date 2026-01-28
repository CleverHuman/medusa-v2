package com.medusa.mall.mapper;

import com.medusa.mall.domain.order.Payment;
import java.util.List;
import java.util.List;

public interface PaymentMapper {
    /**
     * 根据订单ID查询支付信息
     * 
     * @param orderId 订单ID
     * @return 支付信息
     */
    public Payment selectPaymentByOrderId(String orderId);

    /**
     * 新增支付信息
     * 
     * @param payment 支付信息
     * @return 结果
     */
    public int insertPayment(Payment payment);

    /**
     * 修改支付信息
     * 
     * @param payment 支付信息
     * @return 结果
     */
    public int updatePayment(Payment payment);

    /**
     * 删除支付信息
     * 
     * @param id 支付信息主键
     * @return 结果
     */
    public int deletePaymentById(String id);

    /**
     * 根据订单ID删除支付信息
     * 
     * @param orderId 订单ID
     * @return 结果
     */
    public int deletePaymentByOrderId(String orderId);
    
    /**
     * 查询待支付的支付记录
     * 
     * @return 待支付的支付记录列表
     */
    public List<Payment> selectPendingPayments();
    
    /**
     * 查询过期的支付记录
     * 
     * @return 过期的支付记录列表
     */
    public List<Payment> selectExpiredPayments();
    
    /**
     * 查询待支付的NOWPayments支付记录
     * 
     * @return 待支付的NOWPayments支付记录列表
     */
    public List<Payment> selectPendingNowPaymentsPayments();
    
    /**
     * 查询过期的NOWPayments支付记录
     * 
     * @return 过期的NOWPayments支付记录列表
     */
    public List<Payment> selectExpiredNowPaymentsPayments();
} 
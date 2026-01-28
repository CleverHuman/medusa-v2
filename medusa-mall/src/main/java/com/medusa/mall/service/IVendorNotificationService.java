package com.medusa.mall.service;

import com.medusa.mall.domain.vendor.Vendor;
import com.medusa.mall.domain.vendor.VendorWithdrawalRequest;

/**
 * Vendor 通知服务接口
 * 
 * @author medusa
 * @date 2025-11-18
 */
public interface IVendorNotificationService {
    
    /**
     * 发送提现请求状态变更通知
     * 
     * @param vendor Vendor信息
     * @param request 提现请求
     * @param newStatus 新状态
     */
    void notifyWithdrawalStatusChange(Vendor vendor, VendorWithdrawalRequest request, String newStatus);
    
    /**
     * 发送余额变动通知
     * 
     * @param vendorId Vendor ID
     * @param changeType 变动类型（INCREASE, DECREASE, RELEASE）
     * @param amount 变动金额
     * @param description 描述
     */
    void notifyBalanceChange(Long vendorId, String changeType, java.math.BigDecimal amount, String description);
    
    /**
     * 发送订单发货提醒
     * 
     * @param vendorId Vendor ID
     * @param orderId 订单ID
     * @param orderSn 订单编号
     */
    void notifyOrderShipped(Long vendorId, Long orderId, String orderSn);
    
    /**
     * 发送纠纷通知
     * 
     * @param vendorId Vendor ID
     * @param orderId 订单ID
     * @param orderSn 订单编号
     * @param reason 纠纷原因
     */
    void notifyDispute(Long vendorId, Long orderId, String orderSn, String reason);
}


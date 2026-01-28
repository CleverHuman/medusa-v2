package com.medusa.mall.service.impl;

import com.medusa.mall.domain.vendor.Vendor;
import com.medusa.mall.domain.vendor.VendorWithdrawalRequest;
import com.medusa.mall.service.IVendorNotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * Vendor 通知服务实现
 * 
 * 当前版本：记录日志
 * TODO: 后续可集成 Telegram Bot, Email, SMS 等通知渠道
 * 
 * @author medusa
 * @date 2025-11-18
 */
@Service
public class VendorNotificationServiceImpl implements IVendorNotificationService {
    
    private static final Logger log = LoggerFactory.getLogger(VendorNotificationServiceImpl.class);
    
    @Override
    public void notifyWithdrawalStatusChange(Vendor vendor, VendorWithdrawalRequest request, String newStatus) {
        log.info("========== Withdrawal Status Change Notification ==========");
        log.info("Vendor: {} ({})", vendor.getVendorName(), vendor.getId());
        log.info("Request Code: {}", request.getRequestCode());
        log.info("Amount: {} {}", request.getAmount(), request.getCurrency());
        log.info("Old Status: {} -> New Status: {}", request.getRequestStatus(), newStatus);
        
        // TODO: 实际发送通知
        // 1. Telegram: 通过 Telegram Bot API 发送消息
        // 2. Email: 发送邮件通知
        // 3. System Message: 在系统内留言
        
        switch (newStatus) {
            case "APPROVED":
                log.info("Notification: Your withdrawal request has been APPROVED.");
                break;
            case "REJECTED":
                log.info("Notification: Your withdrawal request has been REJECTED. Reason: {}", 
                    request.getRejectReason());
                break;
            case "PROCESSING":
                log.info("Notification: Your withdrawal is being PROCESSED. TX Hash: {}", 
                    request.getTxHash());
                break;
            case "COMPLETED":
                log.info("Notification: Your withdrawal has been COMPLETED successfully.");
                break;
            case "FAILED":
                log.info("Notification: Your withdrawal FAILED. Reason: {}", 
                    request.getRejectReason());
                break;
            default:
                log.info("Notification: Your withdrawal status changed to {}", newStatus);
        }
        
        log.info("===========================================================");
    }
    
    @Override
    public void notifyBalanceChange(Long vendorId, String changeType, BigDecimal amount, String description) {
        log.info("========== Balance Change Notification ==========");
        log.info("Vendor ID: {}", vendorId);
        log.info("Change Type: {}", changeType);
        log.info("Amount: {}", amount);
        log.info("Description: {}", description);
        log.info("=================================================");
        
        // TODO: 实际发送通知
    }
    
    @Override
    public void notifyOrderShipped(Long vendorId, Long orderId, String orderSn) {
        log.info("========== Order Shipped Notification ==========");
        log.info("Vendor ID: {}", vendorId);
        log.info("Order ID: {}", orderId);
        log.info("Order SN: {}", orderSn);
        log.info("Message: Order has been marked as shipped. Balance will be available for withdrawal after the lock period.");
        log.info("================================================");
        
        // TODO: 实际发送通知
    }
    
    @Override
    public void notifyDispute(Long vendorId, Long orderId, String orderSn, String reason) {
        log.info("========== Dispute Notification ==========");
        log.info("Vendor ID: {}", vendorId);
        log.info("Order ID: {}", orderId);
        log.info("Order SN: {}", orderSn);
        log.info("Dispute Reason: {}", reason);
        log.info("Message: A dispute has been raised for this order. The order amount will be withheld pending resolution.");
        log.info("==========================================");
        
        // TODO: 实际发送通知
    }
}


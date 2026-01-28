package com.medusa.mall.service.impl;

import com.medusa.mall.domain.order.Order;
import com.medusa.mall.domain.member.MemberInfo;
import com.medusa.mall.service.ICustomerOrderNotificationService;
import com.medusa.mall.service.IMemberService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Customer Order Notification Service Implementation
 * 客户订单通知服务实现
 * 
 * 当前版本：记录日志
 * TODO: 后续可集成 Telegram Bot, Email, SMS 等通知渠道
 * 
 * @author medusa
 * @date 2025-12-04
 */
@Service
public class CustomerOrderNotificationServiceImpl implements ICustomerOrderNotificationService {
    
    private static final Logger log = LoggerFactory.getLogger(CustomerOrderNotificationServiceImpl.class);
    
    @Autowired
    private IMemberService memberService;
    
    @Override
    public void notifyOrderRejected(Order order, String rejectionReason) {
        log.info("========== Order Rejected Notification ==========");
        log.info("Order ID: {}", order.getId());
        log.info("Order SN: {}", order.getOrderSn());
        log.info("Member ID: {}", order.getMemberId());
        
        // Get member information if available
        if (order.getMemberId() != null) {
            try {
                MemberInfo memberInfo = memberService.selectMemberInfoById(order.getMemberId());
                if (memberInfo != null) {
                    log.info("Member: {} ({})", memberInfo.getUsername(), order.getMemberId());
                    log.info("Contact: primary={}, secondary={}, tgId={}", 
                        memberInfo.getPrimaryContact(), 
                        memberInfo.getSecondaryContact(),
                        memberInfo.getTgId());
                }
            } catch (Exception e) {
                log.warn("Failed to fetch member info for notification: {}", e.getMessage());
            }
        }
        
        log.info("Order Amount: {}", order.getTotalAmount());
        log.info("Rejection Reason: {}", rejectionReason != null ? rejectionReason : "Not provided");
        log.info("Message: Your order #{} has been rejected by the vendor. {}", 
            order.getOrderSn(), 
            rejectionReason != null && !rejectionReason.trim().isEmpty() 
                ? "Reason: " + rejectionReason 
                : "");
        
        // TODO: 实际发送通知
        // 1. Telegram: 通过 Telegram Bot API 发送消息给客户
        // 2. Email: 发送邮件通知
        // 3. System Message: 在系统内留言
        // 4. Push Notification: 如果客户有移动应用
        
        log.info("===============================================");
    }
    
    @Override
    public void notifyOrderAccepted(Order order) {
        log.info("========== Order Accepted Notification ==========");
        log.info("Order ID: {}", order.getId());
        log.info("Order SN: {}", order.getOrderSn());
        log.info("Member ID: {}", order.getMemberId());
        log.info("Message: Your order #{} has been accepted by the vendor.", order.getOrderSn());
        log.info("=================================================");
        
        // TODO: 实际发送通知
    }
    
    @Override
    public void notifyOrderShipped(Order order, String trackingNumber) {
        log.info("========== Order Shipped Notification ==========");
        log.info("Order ID: {}", order.getId());
        log.info("Order SN: {}", order.getOrderSn());
        log.info("Member ID: {}", order.getMemberId());
        log.info("Tracking Number: {}", trackingNumber);
        log.info("Message: Your order #{} has been shipped. Tracking number: {}", 
            order.getOrderSn(), trackingNumber);
        log.info("================================================");
        
        // TODO: 实际发送通知
    }
    
    @Override
    public void notifyOrderStatusChanged(Order order, Integer oldStatus, Integer newStatus, String reason) {
        log.info("========== Order Status Changed Notification ==========");
        log.info("Order ID: {}", order.getId());
        log.info("Order SN: {}", order.getOrderSn());
        log.info("Member ID: {}", order.getMemberId());
        log.info("Status Changed: {} -> {}", oldStatus, newStatus);
        if (reason != null && !reason.trim().isEmpty()) {
            log.info("Reason: {}", reason);
        }
        log.info("Message: Your order #{} status has been updated.", order.getOrderSn());
        log.info("======================================================");
        
        // TODO: 实际发送通知
    }
}


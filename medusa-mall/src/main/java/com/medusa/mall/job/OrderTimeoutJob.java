package com.medusa.mall.job;

import com.medusa.mall.service.OrderTimeoutService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 订单超时处理定时任务
 * 每5分钟执行一次，处理超时的订单
 */
@Component
public class OrderTimeoutJob {
    
    @Autowired
    private OrderTimeoutService orderTimeoutService;
    
    /**
     * 处理超时订单
     * 定时任务调用此方法
     */
    public void processExpiredOrders() {
        try {
            orderTimeoutService.processExpiredOrders();
        } catch (Exception e) {
            // 记录异常但不抛出，避免影响定时任务调度
            e.printStackTrace();
        }
    }
} 
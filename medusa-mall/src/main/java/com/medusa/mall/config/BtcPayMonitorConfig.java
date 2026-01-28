package com.medusa.mall.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * BTCPay监控配置
 */
@Configuration
@EnableScheduling
public class BtcPayMonitorConfig {
    
    @Value("${btcpay.monitor.enabled:true}")
    private boolean monitorEnabled;
    
    @Value("${btcpay.monitor.pending-check-interval:60000}")
    private long pendingCheckInterval;
    
    @Value("${btcpay.monitor.expired-check-interval:60000}")
    private long expiredCheckInterval;
    
    public boolean isMonitorEnabled() {
        return monitorEnabled;
    }
    
    public long getPendingCheckInterval() {
        return pendingCheckInterval;
    }
    
    public long getExpiredCheckInterval() {
        return expiredCheckInterval;
    }
} 
package com.medusa.mall.job;

import com.medusa.mall.service.PaymentMonitorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 统一支付监控定时任务
 */
@Component
public class PaymentMonitorJob {
    
    private static final Logger log = LoggerFactory.getLogger(PaymentMonitorJob.class);
    
    @Autowired
    private PaymentMonitorService monitorService;
    
    /**
     * 检测待支付的发票 - 每60秒执行一次
     */
    @Scheduled(fixedRate = 60000)
    public void monitorPendingInvoices() {
        try {
            log.debug("Starting pending invoices monitoring...");
            monitorService.checkPendingInvoices();
            log.debug("Completed pending invoices monitoring");
        } catch (Exception e) {
            log.error("Error during pending invoices monitoring", e);
        }
    }
    
    /**
     * 检测过期发票 - 每60秒执行一次
     */
    @Scheduled(fixedRate = 60000)
    public void monitorExpiredInvoices() {
        try {
            log.debug("Starting expired invoices monitoring...");
            monitorService.checkExpiredInvoices();
            log.debug("Completed expired invoices monitoring");
        } catch (Exception e) {
            log.error("Error during expired invoices monitoring", e);
        }
    }
} 
package com.medusa.web.task;

import com.medusa.mall.service.IVendorWithdrawalService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Vendor 余额定时任务
 * 
 * @author medusa
 * @date 2025-11-18
 */
@Component
public class VendorBalanceTask {
    
    private static final Logger log = LoggerFactory.getLogger(VendorBalanceTask.class);
    
    @Autowired
    private IVendorWithdrawalService withdrawalService;
    
    /**
     * 自动释放到期的待确认余额
     * 每小时执行一次
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void releaseExpiredPendingBalance() {
        log.info("========== Starting scheduled task: Release Expired Pending Balance ==========");
        
        try {
            withdrawalService.releaseExpiredPendingBalance();
            log.info("========== Scheduled task completed successfully ==========");
        } catch (Exception e) {
            log.error("========== Scheduled task failed ==========", e);
        }
    }
    
    /**
     * 测试用：每5分钟执行一次（仅开发环境启用）
     * 生产环境应该注释掉此方法
     */
    // @Scheduled(cron = "0 */5 * * * ?")
    public void releaseExpiredPendingBalanceTest() {
        log.info("========== [TEST] Starting scheduled task: Release Expired Pending Balance ==========");
        
        try {
            withdrawalService.releaseExpiredPendingBalance();
            log.info("========== [TEST] Scheduled task completed successfully ==========");
        } catch (Exception e) {
            log.error("========== [TEST] Scheduled task failed ==========", e);
        }
    }
}


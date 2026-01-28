package com.medusa.mall.controller.api;

import java.math.BigDecimal;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import com.medusa.common.annotation.Anonymous;
import com.medusa.common.core.controller.BaseController;
import com.medusa.common.core.domain.AjaxResult;
import com.medusa.mall.domain.vendor.Vendor;
import com.medusa.mall.mapper.VendorMapper;
import com.medusa.mall.service.IVendorBotViolationService;

/**
 * Vendor Penalty API Controller
 */
@RestController
@RequestMapping("/api/mall/vendor")
public class ApiVendorPenaltyController extends BaseController {
    
    @Autowired
    private VendorMapper vendorMapper;

    @Autowired
    private IVendorBotViolationService vendorBotViolationService;

    /**
     * Deduct penalty from vendor balance
     */
    @Anonymous
    @PostMapping("/penalty/deduct")
    @Transactional
    public AjaxResult deductPenalty(@RequestBody Map<String, Object> request) {
        try {
            Long vendorId = Long.valueOf(request.get("vendorId").toString());
            BigDecimal amount = new BigDecimal(request.get("amount").toString());
            Long violationId = request.get("violationId") != null 
                ? Long.valueOf(request.get("violationId").toString()) 
                : null;

            // Get vendor
            Vendor vendor = vendorMapper.selectVendorById(vendorId);
            if (vendor == null) {
                return AjaxResult.error("Member account not found");
            }

            // Check balance
            BigDecimal withdrawableBalance = vendor.getWithdrawableBalance();
            if (withdrawableBalance == null) {
                withdrawableBalance = BigDecimal.ZERO;
            }

            if (withdrawableBalance.compareTo(amount) < 0) {
                return AjaxResult.error("Insufficient balance. Available: " + withdrawableBalance);
            }

            // Deduct penalty
            BigDecimal newBalance = withdrawableBalance.subtract(amount);
            vendor.setWithdrawableBalance(newBalance);
            vendorMapper.updateVendor(vendor);

            // Update violation status if violationId provided
            if (violationId != null) {
                vendorBotViolationService.updateVendorBotViolationStatus(violationId, "processed");
            }

            // TODO: Record to balance log table
            // vendorBalanceLogService.insertBalanceLog(...)

            Map<String, Object> result = new java.util.HashMap<>();
            result.put("success", true);
            result.put("newBalance", newBalance);
            return AjaxResult.success(result);
        } catch (Exception e) {
            return AjaxResult.error("Failed to deduct penalty: " + e.getMessage());
        }
    }
}

package com.medusa.mall.controller.api;

import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.medusa.common.annotation.Anonymous;
import com.medusa.common.core.controller.BaseController;
import com.medusa.common.core.domain.AjaxResult;
import com.medusa.mall.domain.vendor.VendorBotViolation;
import com.medusa.mall.service.IVendorBotViolationService;

/**
 * Vendor Bot Violation API Controller
 */
@RestController
@RequestMapping("/api/mall/vendor-bot")
public class ApiVendorBotViolationController extends BaseController {
    
    @Autowired
    private IVendorBotViolationService vendorBotViolationService;

    /**
     * Record violation
     */
    @Anonymous
    @PostMapping("/violations")
    public AjaxResult recordViolation(@RequestBody VendorBotViolation violation) {
        try {
            int result = vendorBotViolationService.insertVendorBotViolation(violation);
            if (result > 0) {
                Map<String, Object> data = new HashMap<>();
                data.put("id", violation.getId());
                return AjaxResult.success(data);
            }
            return AjaxResult.error("Failed to record violation");
        } catch (Exception e) {
            return AjaxResult.error("Failed to record violation: " + e.getMessage());
        }
    }

    /**
     * Get today's penalty count for a vendor
     */
    @Anonymous
    @GetMapping("/violations/today-count")
    public AjaxResult getTodayPenaltyCount(@RequestParam Long vendorId) {
        try {
            int count = vendorBotViolationService.countTodayViolationsByVendorId(vendorId);
            return AjaxResult.success(count);
        } catch (Exception e) {
            return AjaxResult.error("Failed to get today penalty count: " + e.getMessage());
        }
    }

    /**
     * Update violation status
     */
    @Anonymous
    @PutMapping("/violations/{id}/status")
    public AjaxResult updateViolationStatus(@PathVariable Long id, @RequestBody Map<String, String> request) {
        try {
            String status = request.get("status");
            int result = vendorBotViolationService.updateVendorBotViolationStatus(id, status);
            return toAjax(result);
        } catch (Exception e) {
            return AjaxResult.error("Failed to update violation status: " + e.getMessage());
        }
    }
}

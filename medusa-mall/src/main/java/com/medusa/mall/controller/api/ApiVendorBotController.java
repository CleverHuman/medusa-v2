package com.medusa.mall.controller.api;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.medusa.common.annotation.Anonymous;
import com.medusa.common.core.controller.BaseController;
import com.medusa.common.core.domain.AjaxResult;
import com.medusa.mall.domain.vendor.VendorBot;
import com.medusa.mall.service.IVendorBotService;

/**
 * Vendor Bot API Controller
 */
@RestController
@RequestMapping("/api/mall/vendor-bot")
public class ApiVendorBotController extends BaseController {
    
    @Autowired
    private IVendorBotService vendorBotService;

    /**
     * Get all bot configs (for bot initialization)
     */
    @Anonymous
    @GetMapping("/configs")
    public AjaxResult getConfigs(@RequestParam(required = false) Integer status) {
        VendorBot query = new VendorBot();
        if (status != null) {
            query.setStatus(status);
        }
        List<VendorBot> configs = vendorBotService.selectVendorBotList(query);
        return AjaxResult.success(configs);
    }

    /**
     * Get bot config by vendor ID
     */
    @Anonymous
    @GetMapping("/config/{vendorId}")
    public AjaxResult getConfig(@PathVariable Long vendorId) {
        VendorBot config = vendorBotService.selectVendorBotByVendorId(vendorId);
        if (config == null) {
            return AjaxResult.error("Bot configuration not found for your account");
        }
        return AjaxResult.success(config);
    }

    /**
     * Save or update bot config
     */
    @Anonymous
    @PostMapping("/config")
    public AjaxResult saveConfig(@RequestBody VendorBot config) {
        try {
            VendorBot existing = vendorBotService.selectVendorBotByVendorId(config.getVendorId());
            if (existing != null) {
                config.setId(existing.getId());
                int result = vendorBotService.updateVendorBot(config);
                return toAjax(result);
            } else {
                int result = vendorBotService.insertVendorBot(config);
                return toAjax(result);
            }
        } catch (Exception e) {
            return AjaxResult.error("Failed to save bot config: " + e.getMessage());
        }
    }
}

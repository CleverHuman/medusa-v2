package com.medusa.mall.controller.api;

import com.medusa.common.annotation.Anonymous;
import com.medusa.common.core.domain.AjaxResult;
import com.medusa.mall.domain.order.ShippingMethod;
import com.medusa.mall.service.IShippingMethodService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Shipping Method Public API Controller
 * For Telegram Bot and other clients
 */
@RestController
@RequestMapping("/api/mall/shipping")
public class ApiShippingMethodController {
    
    @Autowired
    private IShippingMethodService shippingMethodService;

    /**
     * Get all active shipping methods
     * Public endpoint, no authentication required
     * Added @Anonymous annotation to support guest/temporary users
     */
    @Anonymous
    @GetMapping("/active")
    public AjaxResult getActiveShippingMethods() {
        List<ShippingMethod> activeList = shippingMethodService.selectActiveShippingMethods();
        return AjaxResult.success(activeList);
    }

    /**
     * Get shipping method by code
     * Public endpoint, no authentication required
     */
    @Anonymous
    @GetMapping("/{code}")
    public AjaxResult getByCode(@PathVariable("code") String code) {
        ShippingMethod shippingMethod = shippingMethodService.selectShippingMethodByCode(code);
        if (shippingMethod == null) {
            return AjaxResult.error("Shipping method not found");
        }
        return AjaxResult.success(shippingMethod);
    }
}



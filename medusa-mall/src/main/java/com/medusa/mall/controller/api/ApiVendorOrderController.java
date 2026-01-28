package com.medusa.mall.controller.api;

import com.medusa.common.annotation.Log;
import com.medusa.common.core.controller.BaseController;
import com.medusa.common.core.domain.AjaxResult;
import com.medusa.common.enums.BusinessType;
import com.medusa.mall.domain.order.Order;
import com.medusa.mall.service.IOrderService;
import com.medusa.mall.service.IVendorOrderService;
import com.medusa.mall.service.IVendorProductService;
import com.medusa.mall.util.VendorAuthUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Vendor Order API Controller
 */
@RestController
@RequestMapping("/api/mall/vendor/order")
public class ApiVendorOrderController extends BaseController {

    @Autowired
    private IVendorOrderService vendorOrderService;

    @Autowired
    private IVendorProductService vendorProductService;

    /**
     * Get vendor's own orders
     */
    @GetMapping("/list")
    public AjaxResult list() {
        Long memberId = VendorAuthUtils.getCurrentVendorMemberId();
        if (memberId == null) {
            return AjaxResult.error("Please login first");
        }
        
        Long vendorId = vendorProductService.getVendorIdByMemberId(memberId);
        if (vendorId == null) {
            return AjaxResult.error("Your Co-op Member account is not approved yet");
        }
        
        List<Order> orders = vendorOrderService.selectVendorOrderList(vendorId);
        return AjaxResult.success(orders);
    }

    @Autowired
    private IOrderService orderService;

    /**
     * Get order by ID
     */
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable("id") String id) {
        Long memberId = VendorAuthUtils.getCurrentVendorMemberId();
        if (memberId == null) {
            return AjaxResult.error("Please login first");
        }
        
        Long vendorId = vendorProductService.getVendorIdByMemberId(memberId);
        if (vendorId == null) {
            return AjaxResult.error("Your Co-op Member account is not approved yet");
        }
        
        Order order = vendorOrderService.selectVendorOrderById(id, vendorId);
        if (order == null) {
            return AjaxResult.error("Order not found or access denied");
        }
        
        // Get full order details including items, payment, shipping, and member info
        Object orderDetail = orderService.getOrderDetailById(id);
        return AjaxResult.success(orderDetail);
    }

    /**
     * Accept order
     */
    @Log(title = "Accept Vendor Order", businessType = BusinessType.UPDATE)
    @PostMapping("/{id}/accept")
    public AjaxResult accept(@PathVariable("id") String id) {
        Long memberId = VendorAuthUtils.getCurrentVendorMemberId();
        if (memberId == null) {
            return AjaxResult.error("Please login first");
        }
        
        Long vendorId = vendorProductService.getVendorIdByMemberId(memberId);
        if (vendorId == null) {
            return AjaxResult.error("Your Co-op Member account is not approved yet");
        }
        
        try {
            int result = vendorOrderService.acceptOrder(id, vendorId);
            return toAjax(result);
        } catch (Exception e) {
            return AjaxResult.error("Failed to accept order: " + e.getMessage());
        }
    }

    /**
     * Reject order
     */
    @Log(title = "Reject Vendor Order", businessType = BusinessType.UPDATE)
    @PostMapping("/{id}/reject")
    public AjaxResult reject(@PathVariable("id") String id, @RequestParam(required = false) String reason) {
        Long memberId = VendorAuthUtils.getCurrentVendorMemberId();
        if (memberId == null) {
            return AjaxResult.error("Please login first");
        }
        
        Long vendorId = vendorProductService.getVendorIdByMemberId(memberId);
        if (vendorId == null) {
            return AjaxResult.error("Your Co-op Member account is not approved yet");
        }
        
        try {
            int result = vendorOrderService.rejectOrder(id, vendorId, reason);
            return toAjax(result);
        } catch (Exception e) {
            return AjaxResult.error("Failed to reject order: " + e.getMessage());
        }
    }

    /**
     * Ship order
     */
    @Log(title = "Ship Vendor Order", businessType = BusinessType.UPDATE)
    @PostMapping("/{id}/ship")
    public AjaxResult ship(@PathVariable("id") String id, @RequestParam String trackingNumber) {
        Long memberId = VendorAuthUtils.getCurrentVendorMemberId();
        if (memberId == null) {
            return AjaxResult.error("Please login first");
        }
        
        Long vendorId = vendorProductService.getVendorIdByMemberId(memberId);
        if (vendorId == null) {
            return AjaxResult.error("Your Co-op Member account is not approved yet");
        }
        
        try {
            int result = vendorOrderService.shipOrder(id, vendorId, trackingNumber);
            return toAjax(result);
        } catch (Exception e) {
            return AjaxResult.error("Failed to ship order: " + e.getMessage());
        }
    }
}


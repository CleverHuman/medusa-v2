package com.medusa.mall.controller.api;

import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.medusa.common.annotation.Anonymous;
import com.medusa.common.core.controller.BaseController;
import com.medusa.common.core.domain.AjaxResult;
import com.medusa.mall.domain.vendor.VendorSupportSession;
import com.medusa.mall.service.IVendorSupportSessionService;

/**
 * Vendor Support Session API Controller
 */
@RestController
@RequestMapping("/api/mall/vendor-support")
public class ApiVendorSupportSessionController extends BaseController {
    
    @Autowired
    private IVendorSupportSessionService vendorSupportSessionService;

    /**
     * Create or get session
     */
    @Anonymous
    @PostMapping("/session")
    public AjaxResult createSession(@RequestBody VendorSupportSession session) {
        try {
            // Try to find existing active session
            VendorSupportSession existing = vendorSupportSessionService.selectActiveSession(session);
            if (existing != null) {
                Map<String, Object> data = new HashMap<>();
                data.put("id", existing.getId());
                data.put("topicId", existing.getTopicId());
                return AjaxResult.success(data);
            }

            // Create new session
            int result = vendorSupportSessionService.insertVendorSupportSession(session);
            if (result > 0) {
                Map<String, Object> data = new HashMap<>();
                data.put("id", session.getId());
                data.put("topicId", session.getTopicId());
                return AjaxResult.success(data);
            }
            return AjaxResult.error("Failed to create session");
        } catch (Exception e) {
            return AjaxResult.error("Failed to create session: " + e.getMessage());
        }
    }

    /**
     * Get session
     */
    @Anonymous
    @GetMapping("/session")
    public AjaxResult getSession(
            @RequestParam(required = false) Long vendorId,
            @RequestParam(required = false) String orderId,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long topicId,
            @RequestParam(required = false) String status) {
        try {
            VendorSupportSession query = new VendorSupportSession();
            query.setVendorId(vendorId);
            query.setOrderId(orderId);
            query.setUserId(userId);
            query.setTopicId(topicId);
            query.setStatus(status);

            if (vendorId != null && topicId != null) {
                // Get by topic ID
                VendorSupportSession session = vendorSupportSessionService.selectVendorSupportSessionByTopicId(vendorId, topicId);
                if (session != null) {
                    return AjaxResult.success(session);
                }
            } else {
                // Get active session
                VendorSupportSession session = vendorSupportSessionService.selectActiveSession(query);
                if (session != null) {
                    return AjaxResult.success(session);
                }
            }

            return AjaxResult.error("Session not found");
        } catch (Exception e) {
            return AjaxResult.error("Failed to get session: " + e.getMessage());
        }
    }

    /**
     * Update last message time
     */
    @Anonymous
    @PutMapping("/session/{id}/last-message")
    public AjaxResult updateLastMessageTime(@PathVariable Long id) {
        try {
            int result = vendorSupportSessionService.updateLastMessageTime(id);
            return toAjax(result);
        } catch (Exception e) {
            return AjaxResult.error("Failed to update last message time: " + e.getMessage());
        }
    }

    /**
     * Close session
     */
    @Anonymous
    @PutMapping("/session/close")
    public AjaxResult closeSession(@RequestBody Map<String, Object> request) {
        try {
            Long vendorId = Long.valueOf(request.get("vendorId").toString());
            Long topicId = Long.valueOf(request.get("topicId").toString());
            int result = vendorSupportSessionService.closeSession(vendorId, topicId);
            return toAjax(result);
        } catch (Exception e) {
            return AjaxResult.error("Failed to close session: " + e.getMessage());
        }
    }
}

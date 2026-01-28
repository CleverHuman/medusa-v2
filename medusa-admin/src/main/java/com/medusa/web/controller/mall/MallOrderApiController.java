package com.medusa.web.controller.mall;

import com.medusa.common.core.controller.BaseController;
import com.medusa.mall.domain.order.Order;
import com.medusa.mall.service.IOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.UUID;

/**
 * Mall Order API Controller
 * Provides order-related APIs for mall static pages (no admin permission required)
 * 
 * @author medusa
 * @date 2025-01-14
 */
@Controller
@RequestMapping("/mall/api/order")
public class MallOrderApiController extends BaseController {
    
    @Autowired
    private IOrderService orderService;

    /**
     * Update customer comment for an order
     * Accessible by customers (including guests) who own the order
     * Security: Validates that the user owns the order before allowing updates
     * Returns: Redirects back to checkout confirm page to stay on the same page
     */
    @PostMapping("/updateCustomerComment")
    public String updateCustomerComment(@RequestParam("orderId") String orderId,
                                        @RequestParam("customerComment") String customerComment,
                                        HttpServletRequest request,
                                        org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        try {
            HttpSession session = request.getSession(false);
            if (session == null) {
                logger.warn("No session found for order comment update: orderId={}", orderId);
                redirectAttributes.addFlashAttribute("error", "Session expired. Please refresh the page.");
                return "redirect:/mall/static/checkout/confirm";
            }
            
            // Step 1: Determine user identity (Member or Guest)
            Long memberId = (Long) session.getAttribute("memberId");
            String memberToken = (String) session.getAttribute("memberToken");
            boolean isGuest = (memberToken == null);
            
            if (isGuest) {
                // Guest user - use guestId to generate memberId
                String guestId = getGuestSessionId(request);
                if (guestId == null || guestId.isEmpty()) {
                    logger.warn("No guest session ID found for order comment update: orderId={}", orderId);
                    redirectAttributes.addFlashAttribute("error", "Session expired. Please refresh the page.");
                    return "redirect:/mall/static/checkout/confirm";
                }
                memberId = generateGuestMemberId(guestId);
                logger.info("Guest user updating comment: guestId={}, generatedMemberId={}, orderId={}", 
                           guestId, memberId, orderId);
            } else {
                logger.info("Member user updating comment: memberId={}, orderId={}", memberId, orderId);
            }
            
            // Step 2: Verify the order exists
            Order existingOrder = orderService.selectOrderById(orderId);
            if (existingOrder == null) {
                logger.warn("Order not found: orderId={}", orderId);
                redirectAttributes.addFlashAttribute("error", "Order not found");
                return "redirect:/mall/static/checkout/confirm";
            }
            
            // Step 3: Verify ownership
            if (!existingOrder.getMemberId().equals(memberId)) {
                logger.warn("Unauthorized attempt to update order comment: orderId={}, requestMemberId={}, orderMemberId={}, isGuest={}", 
                           orderId, memberId, existingOrder.getMemberId(), isGuest);
                redirectAttributes.addFlashAttribute("error", "You don't have permission to update this order");
                return "redirect:/mall/static/checkout/confirm";
            }
            
            // Step 4: Validate comment length
            if (customerComment != null && customerComment.length() > 1000) {
                redirectAttributes.addFlashAttribute("error", "Comment is too long (max 1000 characters)");
                return "redirect:/mall/static/checkout/confirm";
            }
            
            // Step 5: Update the customer comment
            Order order = new Order();
            order.setId(orderId);
            order.setCustomerComment(customerComment);
            
            int rows = orderService.updateOrder(order);
            if (rows > 0) {
                logger.info("Customer comment updated successfully: orderId={}, memberId={}, isGuest={}", 
                           orderId, memberId, isGuest);
                // Store in flash attributes for display after redirect
                redirectAttributes.addFlashAttribute("message", "✓ Comment saved successfully!");
                // Store the updated comment so it displays immediately
                redirectAttributes.addFlashAttribute("customerCommentUpdated", customerComment);
                return "redirect:/mall/static/checkout/confirm";
            } else {
                redirectAttributes.addFlashAttribute("error", "Failed to save comment");
                return "redirect:/mall/static/checkout/confirm";
            }
        } catch (Exception e) {
            logger.error("Failed to save customer comment: orderId=" + orderId, e);
            redirectAttributes.addFlashAttribute("error", "Failed to save comment: " + e.getMessage());
            return "redirect:/mall/static/checkout/confirm";
        }
    }
    
    /**
     * Get or create guest session ID
     * Same logic as StaticPageController
     */
    private String getGuestSessionId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        
        String guestId = (String) session.getAttribute("guestId");
        if (guestId == null) {
            guestId = UUID.randomUUID().toString().substring(0, 8);
            session.setAttribute("guestId", guestId);
            logger.info("Generated new guest session ID: {}", guestId);
        }
        return guestId;
    }
    
    /**
     * Generate consistent guest member ID from guest session ID
     * Same logic as StaticPageController
     */
    private Long generateGuestMemberId(String guestId) {
        try {
            // 与TG Bot保持一致：TEMP_USER_ID_PREFIX - telegramUserId
            Long simulatedTgUserId = Long.parseLong(guestId);
            return -1000000L - simulatedTgUserId;
        } catch (NumberFormatException e) {
            // 如果guestId不是数字格式（旧版本兼容），使用hashCode方式
            int hashCode = guestId.hashCode();
            return -(1000000L + Math.abs(hashCode));
        }
    }
}


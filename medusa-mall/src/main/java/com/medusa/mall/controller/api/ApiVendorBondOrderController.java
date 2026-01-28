package com.medusa.mall.controller.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.medusa.common.core.controller.BaseController;
import com.medusa.common.core.domain.AjaxResult;
import com.medusa.mall.domain.order.Order;
import com.medusa.mall.domain.order.OrderItem;
import com.medusa.mall.domain.Product2;
import com.medusa.mall.domain.Product;
import com.medusa.mall.domain.vendor.VendorApplication;
import com.medusa.mall.service.IProduct2Service;
import com.medusa.mall.service.IProductService;
import com.medusa.mall.service.IVendorApplicationService;
import com.medusa.mall.mapper.OrderMapper;
import com.medusa.mall.mapper.OrderItemMapper;
import com.medusa.mall.util.VendorAuthUtils;
import com.medusa.mall.utils.OrderNumberGeneratorWithUniqueness;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Vendor Bond Order API Controller
 * Specialized endpoint for creating Bond payment orders
 * Uses Vendor Member session authentication
 */
@RestController
@RequestMapping("/api/mall/vendor/bond")
public class ApiVendorBondOrderController extends BaseController {
    
    private static final Logger logger = LoggerFactory.getLogger(ApiVendorBondOrderController.class);
    
    @Autowired
    private IVendorApplicationService applicationService;
    
    @Autowired
    private IProduct2Service product2Service;
    
    @Autowired
    private IProductService productService;
    
    @Autowired
    private OrderMapper orderMapper;
    
    @Autowired
    private OrderItemMapper orderItemMapper;
    
    /**
     * Create Bond Payment Order
     * Specialized endpoint for Vendor Members to pay Bond
     */
    @PostMapping("/create-order")
    @Transactional
    public AjaxResult createBondOrder(@RequestBody BondOrderRequest request) {
        try {
            // 1. Verify vendor member authentication
            Long vendorMemberId = VendorAuthUtils.getCurrentVendorMemberId();
            if (vendorMemberId == null) {
                return AjaxResult.error("Please login first");
            }
            
            logger.info("[Bond Order] Creating bond order for vendor member: {}", vendorMemberId);
            
            // 2. Validate application
            VendorApplication application = applicationService.selectApprovedByMemberId(vendorMemberId);
            if (application == null) {
                return AjaxResult.error("No approved application found");
            }
            
            if (application.getBondOrderId() != null) {
                return AjaxResult.error("Bond already paid for this application");
            }
            
            // 3. Validate level and quantity
            Integer level = request.getLevel();
            if (level == null || level < 1 || level > 10) {
                return AjaxResult.error("Invalid bond level");
            }
            
            // 4. Get Bond product SKU
            Product2 bondProduct = product2Service.selectProduct2BySku("BOND-BASE-UNIT");
            if (bondProduct == null) {
                return AjaxResult.error("Bond product not found");
            }
            
            Product product = productService.selectProductByProductId(bondProduct.getProductId());
            if (product == null) {
                return AjaxResult.error("Bond product configuration error");
            }
            
            // 5. Generate order number
            String orderNumber = OrderNumberGeneratorWithUniqueness.generatePlatformOrderNumber(
                orderId -> orderMapper.checkOrderIdExists(orderId) > 0
            );
            
            // 6. Create Order
            Order order = new Order();
            order.setId(orderNumber);
            order.setOrderSn(orderNumber);
            order.setMemberId(vendorMemberId);  // Use vendor member ID directly
            order.setSourceType(0);  // OS channel
            order.setStatus(0);  // Pending
            order.setCreateTime(new Date());
            order.setMemberLevel(0);  // Default level
            
            // Set Bond-specific fields
            order.setOrderType("BOND_PAYMENT");
            order.setBondApplicationId(application.getId());
            order.setBondApplicationNumber(application.getApplicationId());
            
            // Calculate total amount
            BigDecimal unitPrice = bondProduct.getPrice();
            BigDecimal quantity = new BigDecimal(level);
            BigDecimal totalAmount = unitPrice.multiply(quantity);
            
            order.setTotalAmount(totalAmount);
            order.setFreightAmount(BigDecimal.ZERO);  // No shipping for digital product
            order.setDiscountAmount(BigDecimal.ZERO);
            order.setCouponAmount(BigDecimal.ZERO);
            
            // Insert order
            int result = orderMapper.insertOrder(order);
            if (result <= 0) {
                throw new RuntimeException("Failed to create bond order");
            }
            
            logger.info("[Bond Order] Order created: {}", orderNumber);
            
            // 7. Create Order Item
            OrderItem orderItem = new OrderItem();
            orderItem.setId(UUID.randomUUID().toString());
            orderItem.setOrderId(orderNumber);
            orderItem.setOrderSn(orderNumber);
            orderItem.setSku(bondProduct.getSku());
            orderItem.setProductId(bondProduct.getProductId());
            orderItem.setProductName(product.getName());
            orderItem.setProductImage(product.getImageUrl());
            orderItem.setProductSpec(bondProduct.getModel().setScale(2, java.math.RoundingMode.HALF_UP).toString());
            orderItem.setQuantity(level);
            orderItem.setPrice(unitPrice);
            orderItem.setTotalPrice(totalAmount);
            orderItem.setProductOrigin(0);  // Platform product
            orderItem.setOriginId(null);
            orderItem.setCreateTime(new Date());  // Set create_time
            orderItem.setUpdateTime(new Date());  // Set update_time
            
            orderItemMapper.insertOrderItem(orderItem);
            
            logger.info("[Bond Order] Order item created for order: {}", orderNumber);
            
            // 8. Payment will be created on payment page
            // When user reaches payment page and selects payment method (BTC/USDT/XMR),
            // the payment page will create the BTCPay invoice
            logger.info("[Bond Order] Payment will be created when user selects payment method on payment page");
            
            // 9. Return order info
            Map<String, Object> orderData = new HashMap<>();
            orderData.put("orderId", orderNumber);
            orderData.put("orderSn", orderNumber);
            orderData.put("totalAmount", totalAmount);
            orderData.put("level", level);
            orderData.put("applicationId", application.getId());
            orderData.put("applicationNumber", application.getApplicationId());
            
            return AjaxResult.success(orderData);
            
        } catch (Exception e) {
            logger.error("[Bond Order] Failed to create bond order", e);
            return AjaxResult.error("Failed to create bond order: " + e.getMessage());
        }
    }
    
    /**
     * Bond Order Request DTO
     */
    public static class BondOrderRequest {
        private Integer level;  // Bond level (1-10)
        private Long applicationId;  // Application ID
        private String applicationNumber;  // Application number
        
        public Integer getLevel() {
            return level;
        }
        
        public void setLevel(Integer level) {
            this.level = level;
        }
        
        public Long getApplicationId() {
            return applicationId;
        }
        
        public void setApplicationId(Long applicationId) {
            this.applicationId = applicationId;
        }
        
        public String getApplicationNumber() {
            return applicationNumber;
        }
        
        public void setApplicationNumber(String applicationNumber) {
            this.applicationNumber = applicationNumber;
        }
    }
}

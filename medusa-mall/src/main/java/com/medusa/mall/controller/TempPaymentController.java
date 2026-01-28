package com.medusa.mall.controller;

import com.medusa.mall.domain.order.Order;
import com.medusa.mall.domain.order.Payment;
import com.medusa.mall.domain.order.OrderItem;
import com.medusa.mall.service.IOrderService;
import com.medusa.mall.service.PaymentService;
import com.medusa.mall.service.OrderTimeoutService;
import com.medusa.mall.service.InventoryLockService;
import com.medusa.mall.service.RedisOrderCacheService;
import com.medusa.mall.domain.vo.CartAmountInfo;
import com.medusa.mall.mapper.OrderItemMapper;
import com.medusa.mall.mapper.ShippingAddressMapper;
import com.medusa.mall.domain.order.ShippingAddress;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.math.BigDecimal;
import com.medusa.mall.domain.Product2;
import com.medusa.mall.service.IMallPageConfigService;
import com.medusa.mall.service.IProduct2Service;
import com.medusa.mall.service.ICouponService;
import com.medusa.mall.service.IMemberService;
import com.medusa.mall.service.IMemberBenefitService;
import com.medusa.mall.service.IShippingMethodService;
import com.medusa.mall.domain.member.MemberInfo;
import com.medusa.mall.domain.member.MemberBenefit;
import com.medusa.mall.domain.coupon.Coupon;
import com.medusa.mall.domain.order.ShippingMethod;

/**
 * 临时付款页面控制器
 * 处理通过paymentlink访问的临时付款页面
 */
@Controller
@RequestMapping("/mall/temp-payment")
public class TempPaymentController {
    
    @Autowired
    private IOrderService orderService;
    
    @Autowired
    private PaymentService paymentService;
    
    @Autowired
    private OrderTimeoutService orderTimeoutService;
    
    @Autowired
    private InventoryLockService inventoryLockService;
    
    @Autowired
    private RedisOrderCacheService redisOrderCacheService;
    
    @Autowired
    private OrderItemMapper orderItemMapper;
    
    @Autowired
    private ShippingAddressMapper shippingAddressMapper;
    
    @Autowired
    private com.medusa.mall.service.IMallPageConfigService pageConfigService;
    
    @Autowired
    private IProduct2Service product2Service;
    
    @Autowired
    private ICouponService couponService;
    
    @Autowired
    private IMemberService memberService;
    
    @Autowired
    private IMemberBenefitService memberBenefitService;
    
    @Autowired
    private IShippingMethodService shippingMethodService;
    
    /**
     * 显示临时付款页面
     */
    @GetMapping("/pay")
    public String showTempPaymentPage(@RequestParam String orderId, Model model) {
        try {
            // 查询所有 home 页配置
            com.medusa.mall.domain.MallPageConfig query = new com.medusa.mall.domain.MallPageConfig();
            query.setPage("home");
            java.util.List<com.medusa.mall.domain.MallPageConfig> configs = pageConfigService.selectMallPageConfigList(query);

            // 解析配置
            String logoUrl = null;
            String footerLogo = null;
            java.util.List<java.util.Map<String, Object>> footerLinks = new java.util.ArrayList<>();

            for (com.medusa.mall.domain.MallPageConfig c : configs) {
                if ("logo".equals(c.getSection()) && "images".equals(c.getConfigKey())) {
                    // 假设 configValue 是图片数组，取第一个
                    java.util.List<java.util.Map<String, Object>> imgs = parseJsonArray(c.getConfigValue());
                    if (imgs != null && !imgs.isEmpty()) logoUrl = (String) imgs.get(0).get("url");
                }
            }

            // 查询footer配置
            com.medusa.mall.domain.MallPageConfig footerQuery = new com.medusa.mall.domain.MallPageConfig();
            footerQuery.setPage("footer");
            java.util.List<com.medusa.mall.domain.MallPageConfig> footerConfigs = pageConfigService.selectMallPageConfigList(footerQuery);

            for (com.medusa.mall.domain.MallPageConfig c : footerConfigs) {
                if ("contacts".equals(c.getSection()) && "list".equals(c.getConfigKey())) {
                    footerLinks = parseJsonArray(c.getConfigValue());
                }
                if ("logo".equals(c.getSection()) && "images".equals(c.getConfigKey())) {
                    // 解析JSON数组并提取第一个图片的URL
                    java.util.List<java.util.Map<String, Object>> imgs = parseJsonArray(c.getConfigValue());
                    if (imgs != null && !imgs.isEmpty()) {
                        footerLogo = (String) imgs.get(0).get("url");
                    }
                }
            }

            model.addAttribute("logoUrl", logoUrl);
            model.addAttribute("footerLogo", footerLogo);
            model.addAttribute("footerLinks", footerLinks);

            // 检查订单是否存在
            Order order = orderService.selectOrderById(orderId);
            if (order == null) {
                model.addAttribute("error", "Order not found");
                return "mall/error";
            }
            
            // 检查订单是否超时
            if (orderTimeoutService.isOrderExpired(orderId)) {
                model.addAttribute("error", "Order has expired. Please create a new order.");
                return "mall/error";
            }
            
            // 获取支付信息
            Payment payment = paymentService.getPaymentByOrderId(orderId);
            if (payment == null) {
                model.addAttribute("error", "Payment information not found");
                return "mall/error";
            }
            
            // 检查支付状态
            if (payment.getPayStatus() != null && payment.getPayStatus() == 1) {
                model.addAttribute("message", "Order has already been paid");
                return "mall/success";
            }
            
            if (payment.getPayStatus() != null && payment.getPayStatus() == 2) {
                model.addAttribute("error", "Order has been cancelled");
                return "mall/error";
            }
            
            // 获取订单商品
            List<OrderItem> items = orderItemMapper.selectOrderItemsByOrderId(orderId);
            
            // 构建多产品显示字符串（每行一个产品）
            StringBuilder itemsDisplayStr = new StringBuilder();
            if (items != null && !items.isEmpty()) {
                for (int i = 0; i < items.size(); i++) {
                    OrderItem item = items.get(i);
                    // 获取 Product2 信息以获取 unit 并进行智能格式化
                    String formattedSpec = item.getProductSpec();
                    try {
                        // 尝试从 SKU 获取 Product2 信息
                        Product2 product2 = product2Service.selectProduct2BySku(item.getSku());
                        if (product2 != null) {
                            // 使用智能格式化的 model 和 unit
                            String modelStr = product2.getModel() != null ? product2.getModel().setScale(2, java.math.RoundingMode.HALF_UP).toString() : item.getProductSpec();
                            String unitStr = product2.getUnit() != null ? product2.getUnit() : "";
                            
                            // 应用智能格式化逻辑
                            if (modelStr.matches("\\d+\\.\\d+")) {
                                // Remove trailing zeros from decimals (0.50 -> 0.5, 1.20 -> 1.2)
                                modelStr = modelStr.replaceAll("0+$", "").replaceAll("\\.$", "");
                            } else if (modelStr.matches("\\d+\\.00")) {
                                // Remove .00 for integers (200.00 -> 200)
                                modelStr = modelStr.replaceAll("\\.00$", "");
                            }
                            
                            formattedSpec = modelStr + unitStr;
                        }
                    } catch (Exception e) {
                        // 如果获取失败，使用原始规格
                    }
                    itemsDisplayStr.append(item.getProductName())
                            .append(" - ")
                            .append(formattedSpec)
                            .append(" ×")
                            .append(item.getQuantity());
                    if (i != items.size() - 1) {
                        itemsDisplayStr.append("\n");
                    }
                }
            } else {
                itemsDisplayStr.append("Product - Default Spec × 1");
            }
            
            // 获取配送地址
            ShippingAddress shippingAddress = shippingAddressMapper.selectShippingAddressByOrderId(orderId);
            
            // 计算剩余支付时间
            long remainingTime = orderTimeoutService.getRemainingTime(orderId);
            int remainingMinutes = (int) (remainingTime / (1000 * 60));
            int remainingSeconds = (int) ((remainingTime % (1000 * 60)) / 1000);
            
            // 获取订单创建时间并添加到model
            long orderCreateTime = 0;
            if (order.getCreateTime() != null) {
                orderCreateTime = order.getCreateTime().getTime();
            } else {
                orderCreateTime = System.currentTimeMillis(); // 使用当前时间作为备用
            }
            
            // 添加到model
            model.addAttribute("order", order);
            model.addAttribute("payment", payment);
            model.addAttribute("items", items);
            model.addAttribute("itemsDisplayStr", itemsDisplayStr.toString());
            model.addAttribute("shippingAddress", shippingAddress);
            model.addAttribute("remainingMinutes", remainingMinutes);
            model.addAttribute("remainingSeconds", remainingSeconds);
            model.addAttribute("orderId", orderId);
            model.addAttribute("orderCreateTime", orderCreateTime);
            
            // 添加产品信息
            if (items != null && !items.isEmpty()) {
                OrderItem firstItem = items.get(0);
                model.addAttribute("productName", firstItem.getProductName());
                model.addAttribute("productSpec", firstItem.getProductSpec());
                model.addAttribute("productQuantity", firstItem.getQuantity());
            } else {
                model.addAttribute("productName", "Product");
                model.addAttribute("productSpec", "Default Spec");
                model.addAttribute("productQuantity", 1);
            }
            
            // 添加配送方式信息
            String shippingName = "Regular Post"; // 默认值
            if (shippingAddress != null && shippingAddress.getShippingMethod() != null) {
                String shippingCode = shippingAddress.getShippingMethod();
                
                // 优先从数据库查询shipping method获取name
                ShippingMethod selectedMethod = shippingMethodService.selectShippingMethodByCode(shippingCode);
                if (selectedMethod != null && selectedMethod.getName() != null) {
                    shippingName = selectedMethod.getName();
                } else {
                    // 如果数据库查询失败，使用代码映射作为备用
                    switch (shippingCode) {
                        case "RB":
                            shippingName = "Regular Post";
                            break;
                        case "EB":
                            shippingName = "Express Post";
                            break;
                        case "RBT":
                            shippingName = "Regular Stealth Post";
                            break;
                        case "EBT":
                            shippingName = "Express Stealth Post";
                            break;
                        case "HVP":
                            shippingName = "Super Stealth Post (Recommended)";
                            break;
                        default:
                            // 如果代码不匹配，使用代码本身
                            shippingName = shippingCode;
                            break;
                    }
                }
            }
            model.addAttribute("shippingMethod", shippingName);
            
            // 提取BTC地址和虚拟币金额
            String paymentInfo = payment.getRemark() != null ? payment.getRemark() : "";
            String btcAddress = "";
            String cryptoAmount = payment.getTotalCoin() != null ? payment.getTotalCoin().toString() : "";
            String cryptoCurrency = getCryptoCurrencyFromPayType(payment.getPayType());
            
            
            if (paymentInfo != null && !paymentInfo.trim().isEmpty()) {
                // 解析格式: "USDT Address: TNLgDVzBF26FXXdCzszscFf5HQUkKTbFWW | Converted via CoinGecko from 32.00 AUD (rate: 0.662252)"
                if (paymentInfo.contains("Address:")) {
                    String[] parts = paymentInfo.split("Address:");
                    if (parts.length > 1) {
                        String fullAddress = parts[1].trim();
                        // 提取纯净地址：去掉 " | " 后面的描述信息
                        if (fullAddress.contains(" | ")) {
                            btcAddress = fullAddress.split(" \\| ")[0].trim();
                        } else {
                            btcAddress = fullAddress;
                        }
                    }
                }
                // 如果没有找到 "Address:" 格式，尝试其他格式
                else if (paymentInfo.contains("address：")) {
                    int addrStart = paymentInfo.indexOf("address：");
                    int amtStart = paymentInfo.indexOf("amount：");
                    if (addrStart != -1 && amtStart != -1 && amtStart > addrStart) {
                        String fullAddress = paymentInfo.substring(addrStart + 8, amtStart).trim();
                        // 提取纯净地址：去掉 " | " 后面的描述信息
                        if (fullAddress.contains(" | ")) {
                            btcAddress = fullAddress.split(" \\| ")[0].trim();
                        } else {
                            btcAddress = fullAddress;
                        }
                    }
                }
            }
            
            
            // 统一使用纯净地址，用于QR码生成和页面显示
            model.addAttribute("btcAddress", btcAddress);
            model.addAttribute("cryptoAmount", cryptoAmount);
            model.addAttribute("cryptoCurrency", cryptoCurrency);
            
            // 添加订单金额信息
             
            model.addAttribute("orderSubtotal", order.getTotalAmount());
            model.addAttribute("orderTotal", order.getTotalAmount());
            //model.addAttribute("orderCurrency", order.getCurrency() != null ? order.getCurrency() : "AUD");
            model.addAttribute("shippingFee", order.getFreightAmount() != null ? order.getFreightAmount() : BigDecimal.ZERO);
            model.addAttribute("orderDiscount", order.getDiscountAmount() != null ? order.getDiscountAmount() : BigDecimal.ZERO);
            
            // 尝试从 Redis 获取更准确的订单金额信息
            CartAmountInfo cartAmountInfo = redisOrderCacheService.getCartAmountInfo(orderId);
            if (cartAmountInfo != null) {
                
                // 使用 Redis 中的准确数据
                model.addAttribute("orderSubtotal", cartAmountInfo.getSubtotal() != null ? cartAmountInfo.getSubtotal() : order.getTotalAmount());
                model.addAttribute("orderTotal", cartAmountInfo.getTotal() != null ? cartAmountInfo.getTotal() : order.getTotalAmount());
                model.addAttribute("orderCurrency", cartAmountInfo.getCurrency() != null ? cartAmountInfo.getCurrency() : "AUD");
                model.addAttribute("shippingFee", cartAmountInfo.getShippingFee() != null ? cartAmountInfo.getShippingFee() : BigDecimal.ZERO);
                
                // 计算总折扣（优惠券折扣 + 会员折扣）
                BigDecimal couponDiscount = cartAmountInfo.getCouponDiscount() != null ? cartAmountInfo.getCouponDiscount() : BigDecimal.ZERO;
                BigDecimal memberDiscount = cartAmountInfo.getMemberDiscount() != null ? cartAmountInfo.getMemberDiscount() : BigDecimal.ZERO;
                BigDecimal totalDiscount = couponDiscount.add(memberDiscount);
                model.addAttribute("orderDiscount", totalDiscount);
                model.addAttribute("memberDiscount", memberDiscount);
                model.addAttribute("couponDiscount", couponDiscount);
                
                // 添加优惠券类型信息
                Long couponId = cartAmountInfo.getCouponId();
                if (couponId != null) {
                    try {
                        Coupon coupon = couponService.selectCouponById(couponId);
                        if (coupon != null) {
                            model.addAttribute("couponType", coupon.getType());
                            model.addAttribute("couponCode", coupon.getCode());
                        }
                    } catch (Exception e) {
                    }
                }
                
            } else {
                // 使用订单数据作为备选
                model.addAttribute("orderSubtotal", order.getTotalAmount());
                model.addAttribute("orderTotal", order.getTotalAmount());
                model.addAttribute("orderCurrency","AUD");
                model.addAttribute("shippingFee", order.getFreightAmount() != null ? order.getFreightAmount() : BigDecimal.ZERO);
                model.addAttribute("orderDiscount", order.getDiscountAmount() != null ? order.getDiscountAmount() : BigDecimal.ZERO);
            }
            
            // 获取会员等级信息
            String memberLevel = null;
            if (order.getMemberId() != null && order.getMemberId() > 0) {
                try {
                    MemberInfo memberInfo = memberService.selectMemberInfoById(order.getMemberId());
                    if (memberInfo != null && memberInfo.getCurrentLevel() != null) {
                        MemberBenefit memberBenefit = memberBenefitService.selectMemberBenefitByLevelId(memberInfo.getCurrentLevel().longValue());
                        if (memberBenefit != null) {
                            memberLevel = memberBenefit.getLevelName() + " - " + memberBenefit.getDes();
                        }
                    }
                } catch (Exception e) {
                    // 如果获取会员等级信息失败，不影响订单确认功能
                }
            }
            model.addAttribute("memberLevel", memberLevel);
            
            return "mall/temp-payment";
            
        } catch (Exception e) {
            model.addAttribute("error", "Failed to load payment page: " + e.getMessage());
            return "mall/error";
        }
    }
    
    /**
     * 处理付款确认
     */
    @PostMapping("/confirm")
    public String confirmPayment(@RequestParam String orderId, 
                                @RequestParam String paymentMethod,
                                RedirectAttributes redirectAttributes) {
        try {
            // 检查订单是否存在
            Order order = orderService.selectOrderById(orderId);
            if (order == null) {
                redirectAttributes.addFlashAttribute("error", "Order not found");
                return "redirect:/mall/temp-payment/pay?orderId=" + orderId;
            }
            
            // 检查订单是否超时
            if (orderTimeoutService.isOrderExpired(orderId)) {
                redirectAttributes.addFlashAttribute("error", "Order has expired. Please create a new order.");
                return "redirect:/mall/temp-payment/pay?orderId=" + orderId;
            }
            
            // 获取支付信息
            Payment payment = paymentService.getPaymentByOrderId(orderId);
            if (payment == null) {
                redirectAttributes.addFlashAttribute("error", "Payment information not found");
                return "redirect:/mall/temp-payment/pay?orderId=" + orderId;
            }
            
            // 更新支付方式
            payment.setPayType(paymentMethod);
            
            // 更新支付状态为已支付
            payment.setPayStatus(1);
            payment.setPayTime(new java.util.Date());
            
            Payment updatedPayment = paymentService.updatePaymentStatus(payment);
            if (updatedPayment == null) {
                redirectAttributes.addFlashAttribute("error", "Failed to update payment status");
                return "redirect:/mall/temp-payment/pay?orderId=" + orderId;
            }
            
            // 更新订单状态为待发货
            order.setStatus(1); // 1表示待发货
            int orderUpdateResult = orderService.updateOrder(order);
            if (orderUpdateResult <= 0) {
                redirectAttributes.addFlashAttribute("error", "Failed to update order status");
                return "redirect:/mall/temp-payment/pay?orderId=" + orderId;
            }
            
            // 确认库存（支付成功）
            boolean inventoryConfirmed = inventoryLockService.confirmInventory(orderId);
            if (!inventoryConfirmed) {
                redirectAttributes.addFlashAttribute("error", "Failed to confirm inventory");
                return "redirect:/mall/temp-payment/pay?orderId=" + orderId;
            }
            
            redirectAttributes.addFlashAttribute("message", "Payment confirmed successfully!");
            return "redirect:/mall/temp-payment/success?orderId=" + orderId;
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to confirm payment: " + e.getMessage());
            return "redirect:/mall/temp-payment/pay?orderId=" + orderId;
        }
    }
    
    /**
     * 取消订单
     */
    @PostMapping("/cancel")
    public String cancelOrder(@RequestParam String orderId, RedirectAttributes redirectAttributes) {
        try {
            // 检查订单是否存在
            Order order = orderService.selectOrderById(orderId);
            if (order == null) {
                redirectAttributes.addFlashAttribute("error", "Order not found");
                return "redirect:/mall/temp-payment/pay?orderId=" + orderId;
            }
            
            // 处理订单取消（释放库存等）
            orderTimeoutService.processExpiredOrder(orderId);
            
            redirectAttributes.addFlashAttribute("message", "Order cancelled successfully");
            return "redirect:/mall/static/home";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to cancel order: " + e.getMessage());
            return "redirect:/mall/temp-payment/pay?orderId=" + orderId;
        }
    }
    
    /**
     * 支付成功页面
     */
    @GetMapping("/success")
    public String paymentSuccess(@RequestParam String orderId, Model model) {
        try {
            // 查询所有 home 页配置
            com.medusa.mall.domain.MallPageConfig query = new com.medusa.mall.domain.MallPageConfig();
            query.setPage("home");
            java.util.List<com.medusa.mall.domain.MallPageConfig> configs = pageConfigService.selectMallPageConfigList(query);

            // 解析配置
            String logoUrl = null;
            String footerLogo = null;
            java.util.List<java.util.Map<String, Object>> footerLinks = new java.util.ArrayList<>();

            for (com.medusa.mall.domain.MallPageConfig c : configs) {
                if ("logo".equals(c.getSection()) && "images".equals(c.getConfigKey())) {
                    // 假设 configValue 是图片数组，取第一个
                    java.util.List<java.util.Map<String, Object>> imgs = parseJsonArray(c.getConfigValue());
                    if (imgs != null && !imgs.isEmpty()) logoUrl = (String) imgs.get(0).get("url");
                }
            }

            // 查询footer配置
            com.medusa.mall.domain.MallPageConfig footerQuery = new com.medusa.mall.domain.MallPageConfig();
            footerQuery.setPage("footer");
            java.util.List<com.medusa.mall.domain.MallPageConfig> footerConfigs = pageConfigService.selectMallPageConfigList(footerQuery);

            for (com.medusa.mall.domain.MallPageConfig c : footerConfigs) {
                if ("contacts".equals(c.getSection()) && "list".equals(c.getConfigKey())) {
                    footerLinks = parseJsonArray(c.getConfigValue());
                }
                if ("logo".equals(c.getSection()) && "images".equals(c.getConfigKey())) {
                    // 解析JSON数组并提取第一个图片的URL
                    java.util.List<java.util.Map<String, Object>> imgs = parseJsonArray(c.getConfigValue());
                    if (imgs != null && !imgs.isEmpty()) {
                        footerLogo = (String) imgs.get(0).get("url");
                    }
                }
            }

            model.addAttribute("logoUrl", logoUrl);
            model.addAttribute("footerLogo", footerLogo);
            model.addAttribute("footerLinks", footerLinks);

            Order order = orderService.selectOrderById(orderId);
            Payment payment = paymentService.getPaymentByOrderId(orderId);
            
            model.addAttribute("order", order);
            model.addAttribute("payment", payment);
            model.addAttribute("message", "Payment completed successfully!");
            
            return "mall/payment-success";
            
        } catch (Exception e) {
            model.addAttribute("error", "Failed to load success page: " + e.getMessage());
            return "mall/error";
        }
    }

    /**
     * 根据支付类型获取对应的虚拟币类型
     */
    private String getCryptoCurrencyFromPayType(String payType) {
        if (payType == null) {
            return "BTC";
        }
        
        switch (payType.toLowerCase()) {
            case "btc":
            case "0":
                return "BTC";
            case "usdt":
            case "1":
                return "USDT";
            case "xmr":
            case "2":
                return "XMR";
            case "eth":
            case "3":
                return "ETH";
            default:
                return "BTC"; // 默认返回 BTC
        }
    }

    // 工具方法：解析JSON数组
    private java.util.List<java.util.Map<String, Object>> parseJsonArray(String json) {
        if (json == null) return null;
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper()
                .readValue(json, new com.fasterxml.jackson.core.type.TypeReference<java.util.List<java.util.Map<String, Object>>>(){});
        } catch (Exception e) {
            return null;
        }
    }
} 
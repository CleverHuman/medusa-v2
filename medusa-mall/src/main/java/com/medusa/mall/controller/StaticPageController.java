package com.medusa.mall.controller;

import com.medusa.common.core.controller.BaseController;
import com.medusa.common.core.domain.AjaxResult;
import com.medusa.common.core.domain.entity.Member;
import com.medusa.mall.domain.Product;
import com.medusa.mall.domain.Product2;
import com.medusa.mall.domain.cart.Cart;
import com.medusa.mall.domain.cart.CartItem;
import com.medusa.mall.domain.vo.CartItemVO;
import com.medusa.mall.domain.vo.ProductDisplayVO;
import com.medusa.mall.domain.order.OrderAddRequest;
import com.medusa.mall.domain.order.ShippingAddress;
import com.medusa.mall.domain.order.Payment;
import com.medusa.mall.domain.order.ShippingMethod;
import com.medusa.mall.domain.order.OrderItem;
import com.medusa.mall.mapper.OrderItemMapper;
import com.medusa.mall.mapper.PaymentMapper;
import com.medusa.mall.domain.order.Order;
import com.medusa.mall.domain.member.MemberInfo;
import com.medusa.mall.service.IProductService;
import com.medusa.mall.service.ICartService;
import com.medusa.mall.service.IShippingMethodService;
import com.medusa.mall.service.IOrderService;
import com.medusa.mall.service.IMemberService;
import com.medusa.mall.service.IProduct2Service;
import com.medusa.mall.service.ICouponService;
import com.medusa.mall.domain.coupon.Coupon;
import com.medusa.mall.service.ICategoryService;
import com.medusa.mall.domain.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.http.MediaType;
import reactor.core.publisher.Mono;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Cookie;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Date;

import com.medusa.mall.service.RedisOrderCacheService;
import com.medusa.mall.domain.vo.CartAmountInfo;
import com.medusa.mall.domain.MallPageConfig;
import com.medusa.mall.service.IMallPageConfigService;
import com.medusa.mall.domain.PgpKey;
import com.medusa.mall.service.IPgpKeyService;
import com.medusa.mall.service.IMemberBenefitService;
import com.medusa.mall.domain.member.MemberBenefit;
import com.medusa.mall.util.VendorAuthUtils;
import com.medusa.mall.service.PaymentService;
import com.medusa.mall.service.IVendorBondConfigService;
import com.medusa.mall.service.IVendorApplicationService;
import com.medusa.mall.domain.vendor.VendorApplication;
import com.medusa.mall.domain.vendor.VendorBondConfig;
import com.medusa.mall.mapper.OrderMapper;
import com.medusa.mall.utils.OrderNumberGeneratorWithUniqueness;
import java.util.UUID;

/**
 * Static page controller for mall frontend
 * Handles server-side rendering of static pages without JavaScript
 */
@Controller
@RequestMapping("/mall/static")
public class StaticPageController {
    
    private static final Logger log = LoggerFactory.getLogger(StaticPageController.class);

    @Autowired
    private IProductService productService;

    @Autowired
    private ICartService cartService;

    @Autowired
    private IShippingMethodService shippingMethodService;

    @Autowired
    private IOrderService orderService;

    @Autowired
    private IMemberService memberService;

    @Autowired
    private IProduct2Service product2Service;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private PaymentMapper paymentMapper;

    @Autowired
    private WebClient webClient;

    @Autowired
    private ICouponService couponService;

    @Autowired
    private RedisOrderCacheService redisOrderCacheService;

    @Autowired
    private ICategoryService categoryService;

    @Autowired
    private IMallPageConfigService pageConfigService;
    
    @Autowired
    private IPgpKeyService pgpKeyService;

    @Autowired
    private IMemberBenefitService memberBenefitService;

    @Autowired
    private com.medusa.mall.mapper.ShippingAddressMapper shippingAddressMapper;

    @Autowired
    private com.medusa.mall.service.member.IMemberPcspService memberPcspService;
    
    @Autowired
    private PaymentService paymentService;
    
    @Autowired
    private OrderMapper orderMapper;
    
    @Autowired
    private IVendorApplicationService applicationService;
    
    @Autowired
    private IVendorBondConfigService bondConfigService;
    
    @Autowired
    @Lazy
    private VendorStaticPageController vendorStaticPageController;

    /**
     * 获取Guest Session ID - 使用Cookie持久化，确保同一浏览器使用相同ID
     */
    private String getGuestSessionId(HttpServletRequest request, HttpServletResponse response) {
        // 首先检查Cookie中是否有guestId
        String guestId = null;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("guestId".equals(cookie.getName())) {
                    guestId = cookie.getValue();
                    break;
                }
            }
        }
        
        // 如果Cookie中没有，检查Session
        if (guestId == null) {
            HttpSession session = request.getSession(true);
            guestId = (String) session.getAttribute("guestId");
        }
        
        // 如果都没有，生成新的guestId
        if (guestId == null) {
            // 生成一个模拟的Telegram用户ID格式的guestId
            // 使用时间戳的后9位作为模拟的用户ID（确保与真实TG用户ID不冲突）
            long timestamp = System.currentTimeMillis();
            long simulatedTgUserId = 900000000L + (timestamp % 100000000L); // 9开头，确保不与真实TG用户ID冲突
            guestId = String.valueOf(simulatedTgUserId);
            
            // 保存到Session
            HttpSession session = request.getSession(true);
            session.setAttribute("guestId", guestId);
            
            // 保存到Cookie（30天有效期）
            Cookie guestCookie = new Cookie("guestId", guestId);
            guestCookie.setMaxAge(30 * 24 * 60 * 60); // 30天
            guestCookie.setPath("/");
            guestCookie.setHttpOnly(true); // 防止XSS攻击
            if (response != null) {
                response.addCookie(guestCookie);
            }
        } else {
            // 如果找到了guestId，确保Session中也有
            HttpSession session = request.getSession(true);
            session.setAttribute("guestId", guestId);
        }
        
        return guestId;
    }
    
    /**
     * 重载方法，兼容没有response参数的调用
     */
    private String getGuestSessionId(HttpServletRequest request) {
        return getGuestSessionId(request, null);
    }

    /**
     * 生成Guest Member ID - 与TG Bot保持一致的逻辑
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

    /**
     * Login page
     */
    @GetMapping("/login")
    public String login(@RequestParam(value = "error", required = false) String error,
                       @RequestParam(value = "message", required = false) String message,
                       Model model) {
        if (error != null) {
            model.addAttribute("error", error);
        }
        if (message != null) {
            model.addAttribute("message", message);
        }
        return "mall/login";
    }

    /**
     * Signup page
     */
    @GetMapping("/signup")
    public String signup(@RequestParam(value = "error", required = false) String error,
                        @RequestParam(value = "message", required = false) String message,
                        Model model) {
        if (error != null) {
            model.addAttribute("error", error);
        }
        if (message != null) {
            model.addAttribute("message", message);
        }
        return "mall/signup";
    }

    /**
     * Process signup form submission
     */
    @PostMapping("/signup")
    public String processSignup(@RequestParam String username,
                               @RequestParam String password,
                               @RequestParam String confirmPassword,
                               @RequestParam String primaryContact,
                               @RequestParam(required = false) String secondaryContact,
                               RedirectAttributes redirectAttributes) {
        try {
            // 验证密码
            if (!password.equals(confirmPassword)) {
                redirectAttributes.addFlashAttribute("error", "Passwords do not match");
                return "redirect:/mall/static/signup";
            }
            
            if (password.length() < 6) {
                redirectAttributes.addFlashAttribute("error", "Password must be at least 6 characters");
                return "redirect:/mall/static/signup";
            }
            
            // 创建Member对象
            Member member = new Member();
            member.setUsername(username);
            member.setPassword(password);
            member.setPrimaryContact(primaryContact);
            member.setSecondaryContact(secondaryContact);
            member.setSourceType(0); // 默认来源类型
            
            // 调用注册服务
            String result = memberService.register(member);
            
            if (result == null || result.isEmpty()) {
                // 注册成功
                redirectAttributes.addFlashAttribute("message", "Registration successful! Please login.");
                return "redirect:/mall/static/login";
            } else {
                // 注册失败
                redirectAttributes.addFlashAttribute("error", result);
                return "redirect:/mall/static/signup";
            }
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Registration failed: " + e.getMessage());
            return "redirect:/mall/static/signup";
        }
    }

    /**
     * 通用会话验证方法
     */
    private boolean validateSession(HttpServletRequest request, Model model) {
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("memberToken") != null) {
            try {
                String token = (String) session.getAttribute("memberToken");
                Long memberId = (Long) session.getAttribute("memberId");
                
                // 记录页面访问日志
                
                if (memberId != null) {
                    // 强制刷新会话，防止会话过期
                    session.setAttribute("lastAccessTime", System.currentTimeMillis());
                    
                    // 验证会员状态
                    Member member = memberService.selectMemberByMemberId(memberId);
                    if (member != null && member.getStatus() != null && member.getStatus() == 2) {
                        // 会员被暂停，清除会话并重定向到登录页面
                        session.invalidate();
                        model.addAttribute("redirectToLogin", true);
                        return false;
                    } else if (member != null) {
                        // 会话有效，添加会话状态到模型
                        model.addAttribute("sessionValid", true);
                        model.addAttribute("memberId", memberId);
                        model.addAttribute("username", member.getUsername());
                        return true;
                    }
                } else {
                    session.invalidate();
                    model.addAttribute("redirectToLogin", true);
                    return false;
                }
            } catch (Exception e) {
                // 如果验证失败，清除会话
                session.invalidate();
                model.addAttribute("redirectToLogin", true);
                return false;
            }
        } else {
            return false;
        }
        return false;
    }

    /**
     * Home page
     */
    @GetMapping("/home")
    public String home(Model model, HttpServletRequest request, HttpServletResponse response) {
        // 确保guest用户ID被正确设置到Cookie（如果是guest用户）
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("memberToken") == null) {
            // Guest模式：确保guestId被设置到Cookie
            getGuestSessionId(request, response);
        }
        
        // 查询所有 home 页配置
        MallPageConfig query = new MallPageConfig();
        query.setPage("home");
        List<MallPageConfig> configs = pageConfigService.selectMallPageConfigList(query);

        // 解析配置
        String logoUrl = null;
        List<Map<String, Object>> bannerImages = new ArrayList<>();
        String footerLogo = null;
        List<Map<String, Object>> footerLinks = new ArrayList<>();
        String digitalImageUrl = null;
        String digitalTitle = null;
        String digitalUrl = null;
        String physicalImageUrl = null;
        String physicalTitle = null;
        String physicalUrl = null;

        for (MallPageConfig c : configs) {
            if ("logo".equals(c.getSection()) && "images".equals(c.getConfigKey())) {
                try {
                    // 尝试解析新格式（包含images和url）
                    Map<String, Object> configData = new com.fasterxml.jackson.databind.ObjectMapper()
                        .readValue(c.getConfigValue(), new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>(){});
                    
                    List<Map<String, Object>> images = (List<Map<String, Object>>) configData.get("images");
                    if (images != null && !images.isEmpty()) {
                        logoUrl = (String) images.get(0).get("url");
                    }
                } catch (Exception e) {
                    // 向后兼容：如果解析失败，尝试旧格式
                    List<Map<String, Object>> imgs = parseJsonArray(c.getConfigValue());
                    if (imgs != null && !imgs.isEmpty()) {
                        logoUrl = (String) imgs.get(0).get("url");
                    }
                }
            }
            if ("banner".equals(c.getSection()) && "images".equals(c.getConfigKey())) {
                try {
                    // 尝试解析新格式（包含images和url）
                    Map<String, Object> configData = new com.fasterxml.jackson.databind.ObjectMapper()
                        .readValue(c.getConfigValue(), new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>(){});
                    
                    List<Map<String, Object>> images = (List<Map<String, Object>>) configData.get("images");
                    if (images != null && !images.isEmpty()) {
                        bannerImages = images;
                    }
                } catch (Exception e) {
                    // 向后兼容：如果解析失败，尝试旧格式
                    List<Map<String, Object>> imgs = parseJsonArray(c.getConfigValue());
                    if (imgs != null && !imgs.isEmpty()) {
                        bannerImages = imgs;
                    }
                }
            }
            if ("digital".equals(c.getSection()) && "images".equals(c.getConfigKey())) {
                try {
                    Map<String, Object> configData = new com.fasterxml.jackson.databind.ObjectMapper()
                        .readValue(c.getConfigValue(), new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>(){});
                    
                    List<Map<String, Object>> images = (List<Map<String, Object>>) configData.get("images");
                    if (images != null && !images.isEmpty()) {
                        digitalImageUrl = (String) images.get(0).get("url");
                    }
                    digitalTitle = (String) configData.get("title");
                    digitalUrl = (String) configData.get("url");
                    
                } catch (Exception e) {
                }
            }
            if ("physical".equals(c.getSection()) && "images".equals(c.getConfigKey())) {
                try {
                    Map<String, Object> configData = new com.fasterxml.jackson.databind.ObjectMapper()
                        .readValue(c.getConfigValue(), new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>(){});
                    
                    List<Map<String, Object>> images = (List<Map<String, Object>>) configData.get("images");
                    if (images != null && !images.isEmpty()) {
                        physicalImageUrl = (String) images.get(0).get("url");
                    }
                    physicalTitle = (String) configData.get("title");
                    physicalUrl = (String) configData.get("url");
                    
                } catch (Exception e) {
                }
            }
        }

        // 会话验证和刷新逻辑 - 使用通用方法
        validateSession(request, model);

        // 加载footer数据
        loadFooterData(model);

        model.addAttribute("logoUrl", logoUrl);
        model.addAttribute("bannerImages", bannerImages);
        model.addAttribute("digitalImageUrl", digitalImageUrl);
        model.addAttribute("digitalTitle", digitalTitle);
        model.addAttribute("digitalUrl", digitalUrl);
        model.addAttribute("physicalImageUrl", physicalImageUrl);
        model.addAttribute("physicalTitle", physicalTitle);
        model.addAttribute("physicalUrl", physicalUrl);

        return "mall/home";
    }



    /**
     * Product category page
     */
    @GetMapping("/product-category")
    public String productCategory(Model model) {
        try {
            // 查询所有 home 页配置
            MallPageConfig query = new MallPageConfig();
            query.setPage("home");
            List<MallPageConfig> configs = pageConfigService.selectMallPageConfigList(query);

            // 解析配置
            String logoUrl = null;
            String footerLogo = null;
            List<Map<String, Object>> footerLinks = new ArrayList<>();

            for (MallPageConfig c : configs) {
                if ("logo".equals(c.getSection()) && "images".equals(c.getConfigKey())) {
                    try {
                    // 尝试解析新格式（包含images和url）
                    Map<String, Object> configData = new com.fasterxml.jackson.databind.ObjectMapper()
                        .readValue(c.getConfigValue(), new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>(){});
                    
                    List<Map<String, Object>> images = (List<Map<String, Object>>) configData.get("images");
                    if (images != null && !images.isEmpty()) {
                        logoUrl = (String) images.get(0).get("url");
                    }
                } catch (Exception e) {
                    // 向后兼容：如果解析失败，尝试旧格式
                    List<Map<String, Object>> imgs = parseJsonArray(c.getConfigValue());
                    if (imgs != null && !imgs.isEmpty()) {
                        logoUrl = (String) imgs.get(0).get("url");
                    }
                }
                }
            }

            // 加载footer数据
            loadFooterData(model);

            model.addAttribute("logoUrl", logoUrl);

            // Get all active categories from database
            List<Category> categories = categoryService.selectActiveCategories();
            model.addAttribute("categories", categories);
        } catch (Exception e) {
            model.addAttribute("error", "Failed to load categories: " + e.getMessage());
        }
        return "mall/product-category";
    }

    /**
     * Digital products page - redirect to products with Digital Product category
     */
    @GetMapping("/digital-products")
    public String digitalProducts(Model model) {
        return "redirect:/mall/static/products?category=Digital%20Product";
    }

    /**
     * Physical products page - redirect to products with Physical Product category
     */
    @GetMapping("/physical-products")
    public String physicalProducts(Model model) {
        return "redirect:/mall/static/products?category=Physical%20Product";
    }

    /**
     * Products page
     */
    @GetMapping("/products")
    public String products(@RequestParam(value = "category", required = false) String category,
                          @RequestParam(value = "categoryId", required = false) Long categoryId,
                          @RequestParam(value = "name", required = false) String name,
                          Model model) {
        try {
            // 查询所有 home 页配置
            MallPageConfig query = new MallPageConfig();
            query.setPage("home");
            List<MallPageConfig> configs = pageConfigService.selectMallPageConfigList(query);

            // 解析配置
            String logoUrl = null;
            String footerLogo = null;
            List<Map<String, Object>> footerLinks = new ArrayList<>();

            for (MallPageConfig c : configs) {
                if ("logo".equals(c.getSection()) && "images".equals(c.getConfigKey())) {
                    try {
                        // 尝试解析新格式（包含images和url）
                        Map<String, Object> configData = new com.fasterxml.jackson.databind.ObjectMapper()
                            .readValue(c.getConfigValue(), new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>(){});
                        
                        List<Map<String, Object>> images = (List<Map<String, Object>>) configData.get("images");
                        if (images != null && !images.isEmpty()) {
                            logoUrl = (String) images.get(0).get("url");
                        }
                    } catch (Exception e) {
                        // 向后兼容：如果解析失败，尝试旧格式
                        List<Map<String, Object>> imgs = parseJsonArray(c.getConfigValue());
                        if (imgs != null && !imgs.isEmpty()) {
                            logoUrl = (String) imgs.get(0).get("url");
                        }
                    }
                }
            }

            // 加载footer数据
            loadFooterData(model);

            model.addAttribute("logoUrl", logoUrl);

            // 获取产品列表（只包含图片和名字）
            // 获取产品列表（只包含图片和名字）
            List<ProductDisplayVO> products;
            if (categoryId != null) {
                // 如果提供了categoryId，先通过categoryId查询产品
                List<Product> productsByCategoryId = productService.selectProductsByCategoryId(categoryId);
                // 转换为ProductDisplayVO
                products = new ArrayList<>();
                for (Product product : productsByCategoryId) {
                    ProductDisplayVO vo = new ProductDisplayVO();
                    vo.setProductId(product.getProductId());
                    vo.setName(product.getName());
                    vo.setImageUrl(product.getImageUrl());
                    vo.setCategory(product.getCategory());
                    products.add(vo);
                }
            } else {
                // 处理category参数，可能是categoryName或categoryCode
                String categoryFilter = category;
                if (category != null) {
                    // 数据库中的category字段存储的是完整的类别名称，不是类别代码
                    // 所以直接使用传入的category参数，不需要映射
                    categoryFilter = category;
                }
                
                // 添加调试日志
                
                // 使用映射后的category参数查询
                products = productService.selectProductDisplayList(categoryFilter, name);
                
                // 添加调试日志
            }
            
            // 后端去重，只保留每个产品名的第一个
            List<ProductDisplayVO> uniqueProducts = new ArrayList<>();
            java.util.Set<String> seenNames = new java.util.HashSet<>();
            for (ProductDisplayVO prod : products) {
                if (!seenNames.contains(prod.getName())) {
                    uniqueProducts.add(prod);
                    seenNames.add(prod.getName());
                }
            }
            // Get all categories for filter
            List<String> categories = productService.selectAllCategories();
            // Get product names by category for dropdown
            List<String> productNames = productService.selectProductNamesByCategory(category);
            model.addAttribute("products", uniqueProducts);
            model.addAttribute("categories", categories);
            model.addAttribute("productNames", productNames);
            model.addAttribute("selectedCategory", category);
            model.addAttribute("searchName", name);
        } catch (Exception e) {
            model.addAttribute("error", "Failed to load products: " + e.getMessage());
        }
        return "mall/products";
    }

    /**
     * Cart page
     */
    @GetMapping("/cart")
    public String cart(@RequestParam(value = "error", required = false) String error,
                      @RequestParam(value = "message", required = false) String message,
                      @RequestParam(value = "couponCode", required = false) String couponCode,
                      HttpServletRequest request,
                      Model model) {
        try {
            // 查询所有 home 页配置
            MallPageConfig query = new MallPageConfig();
            query.setPage("home");
            List<MallPageConfig> configs = pageConfigService.selectMallPageConfigList(query);

            // 解析配置
            String logoUrl = null;

            for (MallPageConfig c : configs) {
                if ("logo".equals(c.getSection()) && "images".equals(c.getConfigKey())) {
                    try {
                        // 尝试解析新格式（包含images和url）
                        Map<String, Object> configData = new com.fasterxml.jackson.databind.ObjectMapper()
                            .readValue(c.getConfigValue(), new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>(){});
                        
                        List<Map<String, Object>> images = (List<Map<String, Object>>) configData.get("images");
                        if (images != null && !images.isEmpty()) {
                            logoUrl = (String) images.get(0).get("url");
                        }
                    } catch (Exception e) {
                        // 向后兼容：如果解析失败，尝试旧格式
                        List<Map<String, Object>> imgs = parseJsonArray(c.getConfigValue());
                        if (imgs != null && !imgs.isEmpty()) {
                            logoUrl = (String) imgs.get(0).get("url");
                        }
                    }
                }
            }

            // 加载footer数据
            loadFooterData(model);

            model.addAttribute("logoUrl", logoUrl);

            HttpSession session = request.getSession(false);
            
            if (session == null || session.getAttribute("memberToken") == null) {
                // Guest模式
                String guestId = getGuestSessionId(request);
                Cart cart = cartService.getGuestCart(guestId);
                
                if (cart.getDetailedItems() == null || cart.getDetailedItems().isEmpty()) {
                    model.addAttribute("error", "Your cart is empty");
                    model.addAttribute("isGuest", true);
                    return "mall/cart";
                }
                
                BigDecimal memberDiscount = BigDecimal.ZERO; // Guest没有会员折扣
                BigDecimal couponDiscount = BigDecimal.ZERO;
                Long couponId = null;
                String currency = "AUD";
                if (cart.getDetailedItems() != null && !cart.getDetailedItems().isEmpty()) {
                    Object firstItem = cart.getDetailedItems().get(0);
                    if (firstItem instanceof CartItemVO) {
                        String c = ((CartItemVO) firstItem).getCurrency();
                        if (c != null) currency = c;
                    }
                }

                // 处理优惠码
                if (couponCode != null && !couponCode.isEmpty()) {
                    try {
                        Coupon coupon = couponService.selectCouponByCode(couponCode);
                        if (coupon != null && coupon.getStatus() == 1) {
                            couponId = coupon.getCouponId();
                            int type = coupon.getType();
                            if (type == 1) {
                                // 固定金额优惠券
                                couponDiscount = coupon.getAmount();
                            } else if (type == 2) {
                                // 百分比优惠券
                                couponDiscount = cart.getSubtotal().multiply(coupon.getDiscount()).setScale(2, java.math.RoundingMode.HALF_UP);
                            } else if (type == 3) {
                                // Free Shipping优惠券 - 显示为0，因为它是抵消运费而不是直接减去金额
                                couponDiscount = BigDecimal.ZERO;
                            }
                            
                            // 对于非Free Shipping优惠券，确保折扣不超过商品总价
                            if (type != 3 && couponDiscount.compareTo(cart.getSubtotal()) > 0) {
                                couponDiscount = cart.getSubtotal();
                            }
                            
                            session.setAttribute("applied_coupon_id", couponId);
                            session.setAttribute("applied_coupon_code", couponCode);
                            session.setAttribute("applied_coupon_type", type);
                        } else {
                            model.addAttribute("error", "Invalid or inactive coupon");
                        }
                    } catch (Exception e) {
                        model.addAttribute("error", "Failed to apply coupon: " + e.getMessage());
                    }
                } else {
                    // 如果没有新的优惠码，尝试从session中获取之前应用的优惠券
                    couponId = (Long) session.getAttribute("applied_coupon_id");
                    String appliedCouponCode = (String) session.getAttribute("applied_coupon_code");
                    Integer appliedCouponType = (Integer) session.getAttribute("applied_coupon_type");
                    if (couponId != null && appliedCouponCode != null) {
                        try {
                            Coupon coupon = couponService.selectCouponById(couponId);
                            if (coupon != null && coupon.getStatus() == 1) {
                                int type = coupon.getType();
                                if (type == 1) {
                                    // 固定金额优惠券
                                    couponDiscount = coupon.getAmount();
                                } else if (type == 2) {
                                    // 百分比优惠券
                                    couponDiscount = cart.getSubtotal().multiply(coupon.getDiscount()).setScale(2, java.math.RoundingMode.HALF_UP);
                                } else if (type == 3) {
                                    // Free Shipping优惠券 - 显示为0，因为它是抵消运费而不是直接减去金额
                                    couponDiscount = BigDecimal.ZERO;
                                }
                                
                                // 对于非Free Shipping优惠券，确保折扣不超过商品总价
                                if (type != 3 && couponDiscount.compareTo(cart.getSubtotal()) > 0) {
                                    couponDiscount = cart.getSubtotal();
                                }
                            } else {
                                // 优惠券无效，清除session中的信息
                                session.removeAttribute("applied_coupon_id");
                                session.removeAttribute("applied_coupon_code");
                                session.removeAttribute("applied_coupon_type");
                            }
                        } catch (Exception e) {
                            // 清除session中的信息
                            session.removeAttribute("applied_coupon_id");
                            session.removeAttribute("applied_coupon_code");
                            session.removeAttribute("applied_coupon_type");
                        }
                    }
                }

                BigDecimal total = cart.getSubtotal().subtract(memberDiscount).subtract(couponDiscount);
                if (total.compareTo(BigDecimal.ZERO) < 0) {
                    total = BigDecimal.ZERO;
                }

                cart.setCouponDiscount(couponDiscount);
                cart.setCouponId(couponId);
                cart.setCurrency(currency);
                cart.setTotal(total);
                model.addAttribute("cart", cart);
                model.addAttribute("isGuest", true);

            } else {
                // 会员模式
                Long memberId = (Long) session.getAttribute("memberId");
                if (memberId == null) {
                    model.addAttribute("error", "Member ID not found in session");
                    return "mall/cart";
                }

                // 获取购物车信息
                Cart cart = cartService.getCartWithDetails(memberId);
                BigDecimal memberDiscount = cart.getDiscount() != null ? cart.getDiscount() : BigDecimal.ZERO;
                BigDecimal couponDiscount = BigDecimal.ZERO;
                Long couponId = null;
                String currency = "AUD";
                if (cart.getDetailedItems() != null && !cart.getDetailedItems().isEmpty()) {
                    Object firstItem = cart.getDetailedItems().get(0);
                    if (firstItem instanceof CartItemVO) {
                        String c = ((CartItemVO) firstItem).getCurrency();
                        if (c != null) currency = c;
                    }
                }

                // 获取会员等级信息
                String memberLevel = null;
                try {
                    MemberInfo memberInfo = memberService.selectMemberInfoById(memberId);
                    if (memberInfo != null && memberInfo.getCurrentLevel() != null) {
                        MemberBenefit memberBenefit = memberBenefitService.selectMemberBenefitByLevelId(memberInfo.getCurrentLevel().longValue());
                        if (memberBenefit != null) {
                            memberLevel = memberBenefit.getLevelName() + " - " + memberBenefit.getDes();
                        }
                    }
                } catch (Exception e) {
                    // 如果获取会员等级信息失败，不影响购物车功能
                }

                // 处理优惠码
                if (couponCode != null && !couponCode.isEmpty()) {
                    try {
                        Coupon coupon = couponService.selectCouponByCode(couponCode);
                        if (coupon != null && coupon.getStatus() == 1) {
                            couponId = coupon.getCouponId();
                            int type = coupon.getType();
                            if (type == 1) {
                                // 固定金额优惠券
                                couponDiscount = coupon.getAmount();
                            } else if (type == 2) {
                                // 百分比优惠券
                                couponDiscount = cart.getSubtotal().multiply(coupon.getDiscount()).setScale(2, java.math.RoundingMode.HALF_UP);
                            } else if (type == 3) {
                                // Free Shipping优惠券 - 显示为0，因为它是抵消运费而不是直接减去金额
                                couponDiscount = BigDecimal.ZERO;
                            }
                            
                            // 对于非Free Shipping优惠券，确保折扣不超过商品总价
                            if (type != 3 && couponDiscount.compareTo(cart.getSubtotal()) > 0) {
                                couponDiscount = cart.getSubtotal();
                            }
                            
                            session.setAttribute("applied_coupon_id", couponId);
                            session.setAttribute("applied_coupon_code", couponCode);
                            session.setAttribute("applied_coupon_type", type);
                        } else {
                            model.addAttribute("error", "Invalid or inactive coupon");
                        }
                    } catch (Exception e) {
                        model.addAttribute("error", "Failed to apply coupon: " + e.getMessage());
                    }
                } else {
                    // 如果没有新的优惠码，尝试从session中获取之前应用的优惠券
                    couponId = (Long) session.getAttribute("applied_coupon_id");
                    String appliedCouponCode = (String) session.getAttribute("applied_coupon_code");
                    Integer appliedCouponType = (Integer) session.getAttribute("applied_coupon_type");
                    if (couponId != null && appliedCouponCode != null) {
                        try {
                            Coupon coupon = couponService.selectCouponById(couponId);
                            if (coupon != null && coupon.getStatus() == 1) {
                                int type = coupon.getType();
                                if (type == 1) {
                                    // 固定金额优惠券
                                    couponDiscount = coupon.getAmount();
                                } else if (type == 2) {
                                    // 百分比优惠券
                                    couponDiscount = cart.getSubtotal().multiply(coupon.getDiscount()).setScale(2, java.math.RoundingMode.HALF_UP);
                                } else if (type == 3) {
                                    // Free Shipping优惠券 - 显示为0，因为它是抵消运费而不是直接减去金额
                                    couponDiscount = BigDecimal.ZERO;
                                }
                                
                                // 对于非Free Shipping优惠券，确保折扣不超过商品总价
                                if (type != 3 && couponDiscount.compareTo(cart.getSubtotal()) > 0) {
                                    couponDiscount = cart.getSubtotal();
                                }
                            } else {
                                session.removeAttribute("applied_coupon_id");
                                session.removeAttribute("applied_coupon_code");
                                session.removeAttribute("applied_coupon_type");
                            }
                        } catch (Exception e) {
                            session.removeAttribute("applied_coupon_id");
                            session.removeAttribute("applied_coupon_code");
                            session.removeAttribute("applied_coupon_type");
                        }
                    }
                }

                BigDecimal total = cart.getSubtotal().subtract(memberDiscount).subtract(couponDiscount);
                if (total.compareTo(BigDecimal.ZERO) < 0) {
                    total = BigDecimal.ZERO;
                }

                cart.setCouponDiscount(couponDiscount);
                cart.setCouponId(couponId);
                cart.setCurrency(currency);
                cart.setTotal(total);
                model.addAttribute("cart", cart);
                model.addAttribute("isGuest", false);
                model.addAttribute("memberLevel", memberLevel);
            }

            // 从Redis中获取CartAmountInfo并填充到model

            //
        } catch (Exception e) {
            model.addAttribute("error", "Failed to load cart: " + e.getMessage());
        }
        if (error != null) {
            model.addAttribute("error", error);
        }
        if (message != null) {
            model.addAttribute("message", message);
        }
        return "mall/cart";
    }

    /**
     * Update cart item quantity
     */
    @PostMapping("/cart/update")
    public String updateCartItem(@RequestParam Long productId,
                                @RequestParam(required = false) Integer quantity,
                                @RequestParam(required = false) String action,
                                HttpServletRequest request,
                                RedirectAttributes redirectAttributes) {
        try {
            HttpSession session = request.getSession(false);
            if (session == null || session.getAttribute("memberToken") == null) {
                // Guest模式
                String guestId = getGuestSessionId(request);
                
                // Get current cart item to determine current quantity
                Cart cart = cartService.getGuestCart(guestId);
                int currentQuantity = 1;
                if (cart != null && cart.getDetailedItems() != null) {
                    for (Object itemObj : cart.getDetailedItems()) {
                        if (itemObj instanceof CartItemVO) {
                            CartItemVO item = (CartItemVO) itemObj;
                            if (item.getProductId().equals(productId)) {
                                currentQuantity = item.getQuantity();
                                break;
                            }
                        }
                    }
                }
                
                // Calculate new quantity based on action or direct input
                int newQuantity = currentQuantity;
                if ("increase".equals(action)) {
                    newQuantity = currentQuantity + 1;
                } else if ("decrease".equals(action)) {
                    newQuantity = Math.max(1, currentQuantity - 1);
                } else if (quantity != null) {
                    newQuantity = Math.max(1, quantity);
                }
                
                CartItem cartItem = new CartItem();
                cartItem.setProductId(productId);
                cartItem.setQuantity(newQuantity);

                cartService.addToGuestCart(guestId, cartItem);
                
                redirectAttributes.addFlashAttribute("message", "Cart updated successfully!");
                
            } else {
                // 会员模式
                Long memberId = (Long) session.getAttribute("memberId"); 
                
                // Get current cart item to determine current quantity
                Cart cart = cartService.getCartWithDetails(memberId);
                int currentQuantity = 1;
                if (cart != null && cart.getDetailedItems() != null) {
                    for (Object itemObj : cart.getDetailedItems()) {
                        if (itemObj instanceof CartItemVO) {
                            CartItemVO item = (CartItemVO) itemObj;
                            if (item.getProductId().equals(productId)) {
                                currentQuantity = item.getQuantity();
                                break;
                            }
                        }
                    }
                }
                
                // Calculate new quantity based on action or direct input
                int newQuantity = currentQuantity;
                if ("increase".equals(action)) {
                    newQuantity = currentQuantity + 1;
                } else if ("decrease".equals(action)) {
                    newQuantity = Math.max(1, currentQuantity - 1);
                } else if (quantity != null) {
                    newQuantity = Math.max(1, quantity);
                }
                
                CartItem cartItem = new CartItem();
                cartItem.setProductId(productId);
                cartItem.setQuantity(newQuantity);


                cartService.addOrUpdateCartItem(memberId, cartItem);
                
                redirectAttributes.addFlashAttribute("message", "Cart updated successfully!");
            }
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to update cart: " + e.getMessage());
        }
        
        return "redirect:/mall/static/cart";
    }

    /**
     * Remove item from cart
     */
    @PostMapping("/cart/remove")
    public String removeCartItem(@RequestParam Long productId,
                                HttpServletRequest request,
                                RedirectAttributes redirectAttributes) {
        try {
            HttpSession session = request.getSession(false);
            if (session == null || session.getAttribute("memberToken") == null) {
                // Guest模式
                String guestId = getGuestSessionId(request);
                cartService.removeFromGuestCart(guestId, productId);
                redirectAttributes.addFlashAttribute("message", "Item removed from cart successfully!");
            } else {
                // 会员模式
                Long memberId = (Long) session.getAttribute("memberId"); 
                cartService.removeCartItem(memberId, productId);
                redirectAttributes.addFlashAttribute("message", "Item removed from cart successfully!");
            }
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to remove item: " + e.getMessage());
        }
        
        return "redirect:/mall/static/cart";
    }

    /**
     * Checkout address page
     */
    @GetMapping("/checkout")
    public String checkoutAddress(HttpServletRequest request, Model model) {
        try {
            // 检查是否为Guest模式
            HttpSession session = request.getSession(false);
            boolean isGuest = (session == null || session.getAttribute("memberToken") == null);
            
            if (isGuest) {
                // Guest模式：检查购物车是否为空
                String guestId = getGuestSessionId(request);
                Cart cart = cartService.getGuestCart(guestId);
                if (cart.getDetailedItems() == null || cart.getDetailedItems().isEmpty()) {
                    return "redirect:/mall/static/cart?error=Your cart is empty";
                }
            } else {
                // 会员模式：检查是否登录
                if (session.getAttribute("memberId") == null) {
                    return "redirect:/mall/static/login";
                }
            }
            
            // ✅ Check if all items in cart are digital products
            boolean allDigital = isCartAllDigital(session, request);
            
            if (allDigital) {
                log.info("📦 [checkoutAddress] Cart contains only digital products/services, skipping address and shipping");
                
                // Set default "Digital Delivery" shipping method
                session.setAttribute("checkout_shipping", "DIGITAL");
                
                // Set a virtual address for digital products
                Map<String, Object> virtualAddress = new HashMap<>();
                virtualAddress.put("firstName", "Digital");
                virtualAddress.put("lastName", "Delivery");
                virtualAddress.put("address1", "No Physical Address Required");
                virtualAddress.put("address2", "");
                virtualAddress.put("address3", "");
                virtualAddress.put("city", "Digital");
                virtualAddress.put("state", "N/A");
                virtualAddress.put("postalCode", "00000");
                virtualAddress.put("country", "Digital");
                virtualAddress.put("pgpKey", "");
                virtualAddress.put("pgpEmail", "");
                virtualAddress.put("isEncrypted", false);
                virtualAddress.put("isGuest", isGuest);
                session.setAttribute("checkout_address", virtualAddress);
                
                // Skip address and shipping pages, go directly to payment
                return "redirect:/mall/static/checkout/payment";
            }
            
            // 查询所有 home 页配置
            MallPageConfig query = new MallPageConfig();
            query.setPage("home");
            List<MallPageConfig> configs = pageConfigService.selectMallPageConfigList(query);

            // 解析配置
            String logoUrl = null;

            for (MallPageConfig c : configs) {
                if ("logo".equals(c.getSection()) && "images".equals(c.getConfigKey())) {
                    try {
                        // 尝试解析新格式（包含images和url）
                        Map<String, Object> configData = new com.fasterxml.jackson.databind.ObjectMapper()
                            .readValue(c.getConfigValue(), new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>(){});
                        
                        List<Map<String, Object>> images = (List<Map<String, Object>>) configData.get("images");
                        if (images != null && !images.isEmpty()) {
                            logoUrl = (String) images.get(0).get("url");
                        }
                    } catch (Exception e) {
                        // 向后兼容：如果解析失败，尝试旧格式
                        List<Map<String, Object>> imgs = parseJsonArray(c.getConfigValue());
                        if (imgs != null && !imgs.isEmpty()) {
                            logoUrl = (String) imgs.get(0).get("url");
                        }
                    }
                }
            }

            // 加载footer数据
            loadFooterData(model);

            // 获取默认公钥
            String publicKeyData = null;
            try {
                PgpKey defaultKey = pgpKeyService.selectDefaultPgpKey();
                if (defaultKey != null && "public".equals(defaultKey.getKeyType())) {
                    // 将Base64格式转换为PGP格式
                    publicKeyData = com.medusa.mall.utils.PgpEncryptionUtil.convertBase64ToPgpFormat(defaultKey.getKeyData(), false);
                } else {
                    // 如果没有默认公钥，获取第一个激活的公钥
                    List<PgpKey> activeKeys = pgpKeyService.selectActivePgpKeys();
                    for (PgpKey key : activeKeys) {
                        if ("public".equals(key.getKeyType())) {
                            publicKeyData = com.medusa.mall.utils.PgpEncryptionUtil.convertBase64ToPgpFormat(key.getKeyData(), false);
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                publicKeyData = "No public key available";
            }

            model.addAttribute("logoUrl", logoUrl);
            model.addAttribute("publicKeyData", publicKeyData);
            model.addAttribute("isGuest", isGuest);
        } catch (Exception e) {
            // 忽略错误，继续显示页面
        }
        return "mall/checkout-address";
    }

    /**
     * Checkout shipping page
     */
    @GetMapping("/checkout/shipping")
    public String checkoutShipping(HttpServletRequest request, Model model) {
        try {
            // 查询所有 home 页配置
            MallPageConfig query = new MallPageConfig();
            query.setPage("home");
            List<MallPageConfig> configs = pageConfigService.selectMallPageConfigList(query);

            // 解析配置
            String logoUrl = null;

            for (MallPageConfig c : configs) {
                if ("logo".equals(c.getSection()) && "images".equals(c.getConfigKey())) {
                    try {
                        // 尝试解析新格式（包含images和url）
                        Map<String, Object> configData = new com.fasterxml.jackson.databind.ObjectMapper()
                            .readValue(c.getConfigValue(), new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>(){});
                        
                        List<Map<String, Object>> images = (List<Map<String, Object>>) configData.get("images");
                        if (images != null && !images.isEmpty()) {
                            logoUrl = (String) images.get(0).get("url");
                        }
                    } catch (Exception e) {
                        // 向后兼容：如果解析失败，尝试旧格式
                        List<Map<String, Object>> imgs = parseJsonArray(c.getConfigValue());
                        if (imgs != null && !imgs.isEmpty()) {
                            logoUrl = (String) imgs.get(0).get("url");
                        }
                    }
                }
            }

            // 加载footer数据
            loadFooterData(model);

            model.addAttribute("logoUrl", logoUrl);

            
            // Temporarily skip session and cart validation for testing
            /*
            HttpSession session = request.getSession(false);
            if (session == null || session.getAttribute("memberToken") == null) {
                return "redirect:/mall/static/login";
            }
            */

            HttpSession session = request.getSession(false);
            Long memberId = 1L;
            memberId = (Long) session.getAttribute("memberId"); 
            
            // Get shipping methods from database
            List<ShippingMethod> shippingMethods = shippingMethodService.selectActiveShippingMethods();
            
            if (shippingMethods != null) {
                for (ShippingMethod method : shippingMethods) {
                }
            }
            
            // Create mock cart data for testing
            Map<String, Object> cartSummary = new HashMap<>();
            cartSummary.put("subtotal", 100.0);
            cartSummary.put("tax", 10.0);
            cartSummary.put("total", 110.0);
            
            // Calculate shipping fee (default to 0, will be updated when method is selected)
            double shippingFee = 0.0;
            double totalAmount = 110.0; // subtotal + tax + shipping
            
            model.addAttribute("cart", cartSummary);
            model.addAttribute("shippingMethods", shippingMethods);
            model.addAttribute("shippingFee", shippingFee);
            model.addAttribute("totalAmount", totalAmount);

           
            
            return "mall/checkout-shipping";
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/mall/static/cart";
        }
    }

    /**
     * Checkout payment page
     */
    @GetMapping("/checkout/payment")
    public String checkoutPayment(HttpServletRequest request, Model model) {
        try {
            // 查询所有 home 页配置
            MallPageConfig query = new MallPageConfig();
            query.setPage("home");
            List<MallPageConfig> configs = pageConfigService.selectMallPageConfigList(query);

            // 解析配置
            String logoUrl = null;

            for (MallPageConfig c : configs) {
                if ("logo".equals(c.getSection()) && "images".equals(c.getConfigKey())) {
                    try {
                        // 尝试解析新格式（包含images和url）
                        Map<String, Object> configData = new com.fasterxml.jackson.databind.ObjectMapper()
                            .readValue(c.getConfigValue(), new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>(){});
                        
                        List<Map<String, Object>> images = (List<Map<String, Object>>) configData.get("images");
                        if (images != null && !images.isEmpty()) {
                            logoUrl = (String) images.get(0).get("url");
                        }
                    } catch (Exception e) {
                        // 向后兼容：如果解析失败，尝试旧格式
                        List<Map<String, Object>> imgs = parseJsonArray(c.getConfigValue());
                        if (imgs != null && !imgs.isEmpty()) {
                            logoUrl = (String) imgs.get(0).get("url");
                        }
                    }
                }
            }

            // 加载footer数据
            loadFooterData(model);

            model.addAttribute("logoUrl", logoUrl);

            
            HttpSession session = request.getSession(false);
            if (session == null) {
                return "redirect:/mall/static/login";
            }
            
            // Get address information from session
            @SuppressWarnings("unchecked")
            Map<String, Object> addressInfo = (Map<String, Object>) session.getAttribute("checkout_address");
            if (addressInfo == null) {
                // Create mock address data for testing
                addressInfo = new HashMap<>();
                addressInfo.put("firstName", "John");
                addressInfo.put("lastName", "Doe");
                addressInfo.put("address1", "123 Test Street");
                addressInfo.put("address2", "");
                addressInfo.put("city", "Sydney");
                addressInfo.put("state", "NSW");
                addressInfo.put("postalCode", "2000");
                addressInfo.put("country", "Australia");
                addressInfo.put("pgpKey", "");
                addressInfo.put("pgpEmail", "");
            }
            
            // Get shipping method from session
            String shippingMethod = (String) session.getAttribute("checkout_shipping");
            if (shippingMethod == null) {
                shippingMethod = "RP"; // Default shipping method
            }
            
            // Get shipping method details
            double shippingFee;
            if ("DIGITAL".equals(shippingMethod)) {
                // Digital products and services have no shipping fee
                shippingFee = 0.0;
                log.info("📦 [checkoutPayment] Digital product/service order, shipping fee = $0.00");
            } else {
                // Physical products: get fee from database
                ShippingMethod selectedShippingMethod = shippingMethodService.selectShippingMethodByCode(shippingMethod);
                shippingFee = selectedShippingMethod != null ? selectedShippingMethod.getFee().doubleValue() : 15.0;
            }

            //store shipping fee to session
            session.setAttribute("checkout_shipping_fee", shippingFee);
            // 检查是否为Guest用户
            Long memberId = null;
            String username = null;
            boolean isGuest = false;
            Cart cart = null;
            
            if (session.getAttribute("memberToken") == null) {
                // Guest模式：使用Guest session ID
                String guestId = getGuestSessionId(request);
                memberId = generateGuestMemberId(guestId);
                username = "Guest_" + guestId;
                isGuest = true;
                cart = cartService.getGuestCart(guestId);
            } else {
                // 会员模式：使用session中的memberId
                memberId = (Long) session.getAttribute("memberId");
                username = (String) session.getAttribute("username");
                cart = cartService.getCartWithDetails(memberId);
            }            
            if (cart == null || cart.getDetailedItems() == null || cart.getDetailedItems().isEmpty()) {
                model.addAttribute("error", "Cart is empty");
                return "redirect:/mall/static/cart";
            }
            
            // Calculate totals from cart
            BigDecimal subtotal = cart.getSubtotal();
            BigDecimal couponDiscount = cart.getCouponDiscount() != null ? cart.getCouponDiscount() : BigDecimal.ZERO;  
            BigDecimal memberDiscount = cart.getMemberDiscount() != null ? cart.getMemberDiscount() : BigDecimal.ZERO;
            BigDecimal tax = subtotal.multiply(new BigDecimal("0.0")); // 10% tax
            BigDecimal totalAmount = subtotal.add(tax).add(new BigDecimal(shippingFee)).subtract(couponDiscount).subtract(memberDiscount);
            
            // Add all data to model
            model.addAttribute("subtotal", subtotal.doubleValue());
            model.addAttribute("couponDiscount", couponDiscount.doubleValue());
            model.addAttribute("memberDiscount", memberDiscount.doubleValue());
            model.addAttribute("tax", tax.doubleValue());
            model.addAttribute("cart", cart);
            model.addAttribute("shippingMethod", shippingMethod);
            model.addAttribute("shippingFee", shippingFee);
            model.addAttribute("totalAmount", totalAmount.doubleValue());
            
            model.addAttribute("isGuest", isGuest);            // Add address information to model
            model.addAttribute("firstName", addressInfo.get("firstName"));
            model.addAttribute("lastName", addressInfo.get("lastName"));
            model.addAttribute("address1", addressInfo.get("address1"));
            model.addAttribute("address2", addressInfo.get("address2"));
            model.addAttribute("city", addressInfo.get("city"));
            model.addAttribute("state", addressInfo.get("state"));
            model.addAttribute("postalCode", addressInfo.get("postalCode"));
            model.addAttribute("country", addressInfo.get("country"));
            model.addAttribute("pgpKey", addressInfo.get("pgpKey"));
            model.addAttribute("pgpEmail", addressInfo.get("pgpEmail"));
            
         
            return "mall/checkout-payment";
            
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/mall/static/checkout/shipping";
        }
    }

    /**
     * Checkout confirm page
     */
    @GetMapping("/checkout/confirm")
    public String checkoutConfirm(HttpServletRequest request, Model model) {
        try {
            // 查询所有 home 页配置
            MallPageConfig query = new MallPageConfig();
            query.setPage("home");
            List<MallPageConfig> configs = pageConfigService.selectMallPageConfigList(query);

            // 解析配置
            String logoUrl = null;

            for (MallPageConfig c : configs) {
                if ("logo".equals(c.getSection()) && "images".equals(c.getConfigKey())) {
                    try {
                        // 尝试解析新格式（包含images和url）
                        Map<String, Object> configData = new com.fasterxml.jackson.databind.ObjectMapper()
                            .readValue(c.getConfigValue(), new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>(){});
                        
                        List<Map<String, Object>> images = (List<Map<String, Object>>) configData.get("images");
                        if (images != null && !images.isEmpty()) {
                            logoUrl = (String) images.get(0).get("url");
                        }
                    } catch (Exception e) {
                        // 向后兼容：如果解析失败，尝试旧格式
                        List<Map<String, Object>> imgs = parseJsonArray(c.getConfigValue());
                        if (imgs != null && !imgs.isEmpty()) {
                            logoUrl = (String) imgs.get(0).get("url");
                        }
                    }
                }
            }

            // 加载footer数据
            loadFooterData(model);

            model.addAttribute("logoUrl", logoUrl);

            
            HttpSession session = request.getSession(false);
            
            // 检查是否有有效的订单ID（Guest用户和会员用户都应该有）
            String orderId = (String) session.getAttribute("created_order");
            if (orderId == null) {
                return "redirect:/mall/static/checkout/payment";
            }
            
            // 对于会员用户，进行会话验证
            if (session.getAttribute("memberToken") != null) {
                boolean sessionValid = validateSession(request, model);
                if (!sessionValid) {
                    return "redirect:/mall/static/login";
                }
            } else {
                // Guest用户：不需要会员会话验证，但需要确保有订单ID
            }
            
            // 订单ID已经在上面检查过了，这里直接使用
            
            // 获取 payment 信息
            Payment payment = PaymentSessionStore.store.get(orderId);
            if (payment == null) {
                return "redirect:/mall/static/checkout/payment";
            }
            
            // 创建简化的订单数据
            Map<String, Object> order = new HashMap<>();
            order.put("id", orderId);
            order.put("totalAmount", payment.getAmount());
            order.put("freightAmount", new BigDecimal("25.00"));
            order.put("couponAmount", BigDecimal.ZERO);
            order.put("currency", payment.getCurrency());

            

            
            // 获取真实的订单项数据
            List<OrderItem> orderItems = orderItemMapper.selectOrderItemsByOrderId(orderId);
            List<Map<String, Object>> items = new ArrayList<>();
            
            // 构建多产品显示字符串（每行一个产品）
            StringBuilder itemsDisplayStr = new StringBuilder();
            
            if (orderItems != null && !orderItems.isEmpty()) {
                for (OrderItem orderItem : orderItems) {
                    Map<String, Object> item = new HashMap<>();
                    item.put("productName", orderItem.getProductName());
                    item.put("productSpec", orderItem.getProductSpec());
                    item.put("sku", orderItem.getSku());
                    item.put("price", orderItem.getPrice());
                    item.put("quantity", orderItem.getQuantity());
                    item.put("imageUrl", orderItem.getProductImage());
                    items.add(item);
                }
                
                // 构建多产品显示字符串
                for (int i = 0; i < orderItems.size(); i++) {
                    OrderItem item = orderItems.get(i);
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
                    if (i != orderItems.size() - 1) {
                        itemsDisplayStr.append("\n");
                    }
                }
                
                // 使用第一个订单项的信息作为主要产品信息
                OrderItem firstItem = orderItems.get(0);
                model.addAttribute("productName", firstItem.getProductName());
                model.addAttribute("productSpec", firstItem.getProductSpec());
                model.addAttribute("productAmount", firstItem.getPrice());
                model.addAttribute("productQuantity", firstItem.getQuantity());
            } else {
                // 如果没有订单项，使用默认值
                Map<String, Object> item = new HashMap<>();
                item.put("productName", "Sample Product");
                item.put("productSpec", "Default Spec");
                item.put("sku", "SKU123");
                item.put("price", new BigDecimal("99.99"));
                item.put("quantity", 1);
                item.put("imageUrl", "/images/product-placeholder.jpg");
                items.add(item);
                
                itemsDisplayStr.append("Product - Default Spec × 1");
                
                model.addAttribute("productName", "Sample Product");
                model.addAttribute("productSpec", "Default Spec");
                model.addAttribute("productAmount", new BigDecimal("99.99"));
                model.addAttribute("productQuantity", 1);
            }
            
            // 添加到model
            model.addAttribute("payment", payment);
            model.addAttribute("order", order);
            model.addAttribute("items", items);
            model.addAttribute("itemsDisplayStr", itemsDisplayStr.toString());
            
            // 获取配送信息
            String shippingCode = (String) session.getAttribute("checkout_shipping");
            String shippingName = "Regular Post"; // 默认值
            
            if (shippingCode != null) {
                // 从数据库查询shipping method获取name
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
            
            // 添加配送信息
            model.addAttribute("shippingMethod", shippingName);
            model.addAttribute("estimatedDelivery", "3-5 business days");
            model.addAttribute("shippingAddress", "123 Main St, City, State 12345, Country");

            // 提取加密货币地址和金额
            String paymentInfo = payment.getPaymentInfo();
        
            String cryptoAddress = "";
            String cryptoAmount = "";
            String cryptoCurrency = getCryptoCurrencyFromPayType(payment.getPayType());
            
            if (paymentInfo != null) {
                int addrStart = paymentInfo.indexOf("address：");
                int amtStart = paymentInfo.indexOf("amount：");
                
                
                if (addrStart != -1 && amtStart != -1 && amtStart > addrStart) {
                    cryptoAddress = paymentInfo.substring(addrStart + 8, amtStart).trim();
                    // 移除可能的换行符
                    cryptoAddress = cryptoAddress.replaceAll("[\\r\\n]", "");
                }
                
                if (amtStart != -1) {
                    // 提取金额，不依赖货币类型
                    String remainingText = paymentInfo.substring(amtStart + 7);
                    // 查找第一个空格或换行符的位置
                    int endIndex = remainingText.indexOf(' ');
                    if (endIndex == -1) {
                        endIndex = remainingText.indexOf('\n');
                    }
                    if (endIndex == -1) {
                        endIndex = remainingText.indexOf('\r');
                    }
                    if (endIndex == -1) {
                        endIndex = remainingText.length();
                    }
                    cryptoAmount = remainingText.substring(0, endIndex).trim();
                }
                
            }
            model.addAttribute("cryptoAddress", cryptoAddress);
            model.addAttribute("cryptoAmount", cryptoAmount);
            model.addAttribute("cryptoCurrency", cryptoCurrency);


             // 获取 memberId
    Long cartMemberId = null;
   
    if (session != null) {
        cartMemberId = (Long) session.getAttribute("memberId");
    }
    if (cartMemberId == null && payment != null) {
        cartMemberId = payment.getUserId();
    }

    CartAmountInfo cartAmountInfo = redisOrderCacheService.getCartAmountInfo(orderId);

    // 获取 cart 并传递金额相关字段
    if (cartAmountInfo != null) {
        
            BigDecimal couponDiscount = cartAmountInfo.getCouponDiscount() != null ? cartAmountInfo.getCouponDiscount() : BigDecimal.ZERO;
            BigDecimal memberDiscount = cartAmountInfo.getMemberDiscount() != null ? cartAmountInfo.getMemberDiscount() : BigDecimal.ZERO;
            BigDecimal shippingFee = cartAmountInfo.getShippingFee() != null ? cartAmountInfo.getShippingFee() : BigDecimal.ZERO;

            model.addAttribute("shippingFee", shippingFee);
            model.addAttribute("orderSubtotal", cartAmountInfo.getSubtotal());
            model.addAttribute("orderDiscount", couponDiscount.add(memberDiscount));
            model.addAttribute("memberDiscount", memberDiscount);
            model.addAttribute("couponDiscount", couponDiscount);
            model.addAttribute("orderTotal", cartAmountInfo.getTotal());
            model.addAttribute("orderCurrency", cartAmountInfo.getCurrency());
            
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
        
    }

    // 获取会员等级信息
    String memberLevel = null;
    if (cartMemberId != null && cartMemberId > 0) {
        try {
            MemberInfo memberInfo = memberService.selectMemberInfoById(cartMemberId);
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

            // Load customer comment from database if not provided by flash attribute
            if (!model.containsAttribute("customerCommentUpdated")) {
                try {
                    Order existingOrder = orderService.selectOrderById(orderId);
                    if (existingOrder != null && existingOrder.getCustomerComment() != null) {
                        model.addAttribute("customerComment", existingOrder.getCustomerComment());
                    } else {
                        model.addAttribute("customerComment", "");
                    }
                } catch (Exception e) {
                    model.addAttribute("customerComment", "");
                }
            } else {
                // Use the updated comment from flash attribute
                model.addAttribute("customerComment", model.getAttribute("customerCommentUpdated"));
            }
            
            return "mall/checkout-confirm";
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/mall/static/checkout/payment";
        }
    }

    /**
     * Process address form submission
     */
    @PostMapping("/checkout/shipping")
    public String processAddress(@RequestParam(required = false) String firstName,
                                @RequestParam(required = false) String lastName,
                                @RequestParam(required = false) String address1,
                                @RequestParam(required = false) String address2,
                                @RequestParam(required = false) String address3,
                                @RequestParam(required = false) String city,
                                @RequestParam(required = false) String state,
                                @RequestParam(required = false) String postalCode,
                                @RequestParam(required = false) String country,
                                @RequestParam(required = false) String pgpKey,
                                @RequestParam(required = false) String pgpEmail,
                                @RequestParam(required = false) String pgpEncrypted,
                                @RequestParam(required = false) String encryptedAddress,
                                HttpServletRequest request,
                                RedirectAttributes redirectAttributes) {
        try {
            HttpSession session = request.getSession(true);
            
            // 检查是否为Guest模式
            boolean isGuest = (session.getAttribute("memberToken") == null);
            
            // Store address information in session
            Map<String, Object> addressInfo = new HashMap<>();
            
            // 如果这是PGP加密的提交
            if ("true".equals(pgpEncrypted)) {
                // 验证加密地址是否提供
                if (encryptedAddress == null || encryptedAddress.trim().isEmpty()) {
                    redirectAttributes.addFlashAttribute("error", "Please provide your encrypted address.");
                    return "redirect:/mall/static/checkout";
                }
                
                // 存储加密地址信息
                addressInfo.put("encryptedAddress", encryptedAddress);
                addressInfo.put("isEncrypted", true);
                addressInfo.put("firstName", "Encrypted");
                addressInfo.put("lastName", "User");
                addressInfo.put("address1", "Encrypted Address");
                addressInfo.put("city", "Encrypted");
                addressInfo.put("state", "Encrypted");
                addressInfo.put("postalCode", "Encrypted");
                addressInfo.put("country", "AU");
                
                
            } else {
                // 普通提交，验证必需字段
                if (firstName == null || firstName.trim().isEmpty() ||
                    lastName == null || lastName.trim().isEmpty() ||
                    address1 == null || address1.trim().isEmpty() ||
                    city == null || city.trim().isEmpty() ||
                    state == null || state.trim().isEmpty() ||
                    postalCode == null || postalCode.trim().isEmpty()) {
                    redirectAttributes.addFlashAttribute("error", "Please fill in all required address fields.");
                    return "redirect:/mall/static/checkout";
                }
                
                addressInfo.put("firstName", firstName);
                addressInfo.put("lastName", lastName);
                addressInfo.put("address1", address1);
                addressInfo.put("address2", address2);
                addressInfo.put("address3", address3);
                addressInfo.put("city", city);
                addressInfo.put("state", state);
                addressInfo.put("postalCode", postalCode);
                addressInfo.put("country", country != null ? country : "AU");
                addressInfo.put("pgpKey", pgpKey);
                addressInfo.put("pgpEmail", pgpEmail);
                addressInfo.put("isEncrypted", false);
            }
            
            // 为Guest模式添加标识
            addressInfo.put("isGuest", isGuest);
            
            session.setAttribute("checkout_address", addressInfo);
            
            // ✅ Check if all items in cart are digital products
            boolean allDigital = isCartAllDigital(session, request);
            
            if (allDigital) {
                log.info("📦 [processAddress] Cart contains only digital products/services, skipping shipping selection");
                
                // Set default "Digital Delivery" shipping method
                session.setAttribute("checkout_shipping", "DIGITAL");
                
                // Skip shipping page and go directly to payment
                return "redirect:/mall/static/checkout/payment";
            }
            
            return "redirect:/mall/static/checkout/shipping";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to process address: " + e.getMessage());
            return "redirect:/mall/static/checkout";
        }
    }

    /**
     * Process shipping method selection
     */
    @PostMapping("/checkout/payment")
    public String processShipping(@RequestParam String shippingMethod,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        try {
            // Store shipping method in session
            session.setAttribute("checkout_shipping", shippingMethod);
            
            return "redirect:/mall/static/checkout/payment";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to process shipping: " + e.getMessage());
            return "redirect:/mall/static/checkout/shipping";
        }
    }

    /**
     * Process payment method selection and create order
     */
    @PostMapping("/checkout/confirm")
    public String processPayment(@RequestParam String paymentMethod,
                                @RequestParam String shippingMethod,
                                HttpServletRequest request,
                                RedirectAttributes redirectAttributes) {
        try {
            
            HttpSession session = request.getSession(false);
            if (session == null) {
                return "redirect:/mall/static/login";
            }
            
            // 检查是否为Guest模式
            boolean isGuest = (session.getAttribute("memberToken") == null);
            
            Long memberId;
            String username;
            Cart cart;
            
            if (isGuest) {
                // Guest模式
                String guestId = getGuestSessionId(request);
                memberId = generateGuestMemberId(guestId);
                username = "Guest_" + guestId; // 直接使用guestId，保持一致性
                cart = cartService.getGuestCart(guestId);
            } else {
                // 会员模式
                memberId = (Long) session.getAttribute("memberId");
                username = (String) session.getAttribute("username");
                cart = cartService.getCartWithDetails(memberId);                // 会员模式
            }
            
            
            if (cart == null || cart.getDetailedItems() == null || cart.getDetailedItems().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Cart is empty");
                return "redirect:/mall/static/cart";
            }
            
            // 获取应用的优惠券信息
            Long couponId = (Long) session.getAttribute("applied_coupon_id");
            BigDecimal couponDiscount = BigDecimal.ZERO;
            

            // 如果有应用的优惠券，重新计算折扣
            if (couponId != null) {
                try {
                    Coupon coupon = couponService.selectCouponById(couponId);
                    if (coupon != null && coupon.getStatus() == 1) {
                        int type = coupon.getType();
                        if (type == 1) {
                            couponDiscount = coupon.getAmount();
                        } else if (type == 2) {
                            // 百分比折扣
                            couponDiscount = cart.getSubtotal().multiply(coupon.getDiscount()).setScale(2, java.math.RoundingMode.HALF_UP);
                        }
                        // 限制最大不超过小计
                        if (couponDiscount.compareTo(cart.getSubtotal()) > 0) {
                            couponDiscount = cart.getSubtotal();
                        }
                    }
                } catch (Exception ex) {
                }
            }

            // Create OrderAddRequest
            OrderAddRequest orderRequest = new OrderAddRequest();
            orderRequest.setUserId(memberId.toString()); // 转换为String
            orderRequest.setUserName(username); // 使用真实用户名
            orderRequest.setCurrency("AUD");
            orderRequest.setStatus("0"); // Pending
            orderRequest.setCreatedAt(new Date());
            orderRequest.setShippingMethod(shippingMethod);
            orderRequest.setCouponId(couponId != null ? couponId.toString() : null); // 使用实际的优惠券ID


            orderRequest.setPayType(paymentMethod); // 使用传入的支付方式
            orderRequest.setSourceType("0"); // 使用普通订单类型，会进行汇率转换
         
            
            // Get address information from session
            Map<String, Object> addressInfo = (Map<String, Object>) session.getAttribute("checkout_address");
            if (addressInfo == null) {
                redirectAttributes.addFlashAttribute("error", "Address information not found. Please go back to checkout.");
                return "redirect:/mall/static/checkout";
            }
            
            // Create shipping address from session data
            ShippingAddress shippingAddress = new ShippingAddress();
            shippingAddress.setName((String) addressInfo.get("firstName") + " " + (String) addressInfo.get("lastName"));
            shippingAddress.setAddress1((String) addressInfo.get("address1"));
            shippingAddress.setAddress2((String) addressInfo.get("address2"));
            shippingAddress.setAddress3((String) addressInfo.get("address3"));
            shippingAddress.setCity((String) addressInfo.get("city"));
            shippingAddress.setState((String) addressInfo.get("state"));
            shippingAddress.setPostalCode((String) addressInfo.get("postalCode"));
            shippingAddress.setCountry((String) addressInfo.get("country"));
            
            // 如果是加密地址，添加加密信息
            Boolean isEncrypted = (Boolean) addressInfo.get("isEncrypted");
            if (isEncrypted != null && isEncrypted) {
                String encryptedAddress = (String) addressInfo.get("encryptedAddress");
                if (encryptedAddress != null && !encryptedAddress.trim().isEmpty()) {
                    // 将加密地址存储到收货地址的备注字段，而不是订单的remark字段
                    // 因为收货地址表的remark字段更适合存储这种信息
                    shippingAddress.setRemark("PGP Encrypted Address: " + encryptedAddress);
                    // 在订单remark中只存储一个标识
                    orderRequest.setRemark("PGP Encrypted Address Used");
                }
            }
            
            orderRequest.setShippingAddress(shippingAddress);
            
            // Create order items from cart
            List<OrderAddRequest.Item> items = new ArrayList<>();
            BigDecimal totalAmount = BigDecimal.ZERO;
            
            for (Object cartItemObj : cart.getDetailedItems()) {
                if (cartItemObj instanceof CartItemVO) {
                    CartItemVO cartItem = (CartItemVO) cartItemObj;
                    
                    OrderAddRequest.Item item = new OrderAddRequest.Item();
                    item.setSku(cartItem.getSku());
                    item.setQuantity(cartItem.getQuantity());
                    item.setPrice(cartItem.getPrice());
                    item.setCurrency(cartItem.getCurrency());
                    item.setTotalPrice(cartItem.getTotalPrice());
                    
                    items.add(item);
                    totalAmount = totalAmount.add(cartItem.getTotalPrice());
                }
            }
            
            orderRequest.setItems(items);
            orderRequest.setTotal(totalAmount);
            
            
            // 调用订单服务创建订单
            Payment payment = orderService.createOrder(orderRequest);
            
            // 保存购物车金额相关信息
            CartAmountInfo cartAmountInfo = new CartAmountInfo();
            cartAmountInfo.setSubtotal(cart.getSubtotal());
            cartAmountInfo.setCouponDiscount(couponDiscount);
    
            Object shippingFeeObj = session.getAttribute("checkout_shipping_fee");
            BigDecimal shippingFee;
            if (shippingFeeObj instanceof Double) {
                shippingFee = new BigDecimal((Double) shippingFeeObj);
            } else if (shippingFeeObj instanceof String) {
                shippingFee = new BigDecimal((String) shippingFeeObj);
            } else {
                shippingFee = BigDecimal.ZERO; // 默认值
            }
            
            // 检查是否为数字产品订单
            if ("DIGITAL".equals(shippingMethod)) {
                shippingFee = BigDecimal.ZERO; // 数字产品无运费
                log.info("📦 [processPayment] Digital product order, final shipping fee = $0.00");
            } else {
                // 检查会员等级，Platinum和Diamond享受免运费
                if (!isGuest && memberId != null && memberId > 0) {
                    try {
                        MemberInfo memberInfo = memberService.selectMemberInfoById(memberId);
                        if (memberInfo != null && memberInfo.getCurrentLevel() != null) {
                            MemberBenefit memberBenefit = memberBenefitService.selectMemberBenefitByLevelId(memberInfo.getCurrentLevel().longValue());
                            if (memberBenefit != null) {
                                String levelName = memberBenefit.getLevelName();
                                if (levelName != null && (levelName.equalsIgnoreCase("Platinum") || levelName.equalsIgnoreCase("Diamond"))) {
                                    shippingFee = BigDecimal.ZERO; // 免运费
                                }
                            }
                        }
                    } catch (Exception e) {
                    }
                }
            }
            
            cartAmountInfo.setShippingFee(shippingFee);
            cartAmountInfo.setMemberDiscount(cart.getMemberDiscount() != null ? cart.getMemberDiscount() : BigDecimal.ZERO);
            cartAmountInfo.setCurrency(cart.getCurrency());
            cartAmountInfo.setTotal(cart.getSubtotal().subtract(cart.getMemberDiscount()).subtract(couponDiscount).add(shippingFee));
            cartAmountInfo.setDueTime(new Date());
            cartAmountInfo.setPayType(paymentMethod);
            cartAmountInfo.setRate(new BigDecimal("1.0"));
            cartAmountInfo.setTotalCoin(cart.getSubtotal().subtract(cart.getMemberDiscount()).subtract(couponDiscount).add(shippingFee));
            cartAmountInfo.setCouponId(couponId);

            // 将 CartAmountInfo 存储到 Redis 中，设置 1 小时过期时间
            boolean stored = redisOrderCacheService.storeCartAmountInfo(payment.getOrderId(), cartAmountInfo, 3600);
            if (stored) {
            } else {
            }

            // 清空购物车
            if (isGuest) {
                // Guest模式：清空Guest购物车
                String guestId = getGuestSessionId(request);
                for (Object cartItemObj : cart.getDetailedItems()) {
                    if (cartItemObj instanceof CartItemVO) {
                        CartItemVO cartItem = (CartItemVO) cartItemObj;
                        cartService.removeFromGuestCart(guestId, cartItem.getProductId());
                    }
                }
            } else {
                // 会员模式：清空会员购物车
                for (Object cartItemObj : cart.getDetailedItems()) {
                    if (cartItemObj instanceof CartItemVO) {
                        CartItemVO cartItem = (CartItemVO) cartItemObj;
                        cartService.removeCartItem(memberId, cartItem.getProductId());
                    }
                }
            }

            // 清除session中的优惠券信息
            session.removeAttribute("applied_coupon_id");
            session.removeAttribute("applied_coupon_code");
            session.removeAttribute("checkout_shipping_fee");

            // 存储订单ID到session
            session.setAttribute("created_order", payment.getOrderId());
            
            // 存储payment信息到静态map
            PaymentSessionStore.store.put(payment.getOrderId(), payment);
            
            return "redirect:/mall/static/checkout/confirm";
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Failed to process payment: " + e.getMessage());
            return "redirect:/mall/static/checkout/payment";
        }
    }

    /**
     * Profile page
     */
    @GetMapping("/profile")
    public String profile(HttpServletRequest request, Model model) {
        try {
            // 查询所有 home 页配置
            MallPageConfig query = new MallPageConfig();
            query.setPage("home");
            List<MallPageConfig> configs = pageConfigService.selectMallPageConfigList(query);

            // 解析配置
            String logoUrl = null;

            for (MallPageConfig c : configs) {
                if ("logo".equals(c.getSection()) && "images".equals(c.getConfigKey())) {
                    try {
                        // 尝试解析新格式（包含images和url）
                        Map<String, Object> configData = new com.fasterxml.jackson.databind.ObjectMapper()
                            .readValue(c.getConfigValue(), new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>(){});
                        
                        List<Map<String, Object>> images = (List<Map<String, Object>>) configData.get("images");
                        if (images != null && !images.isEmpty()) {
                            logoUrl = (String) images.get(0).get("url");
                        }
                    } catch (Exception e) {
                        // 向后兼容：如果解析失败，尝试旧格式
                        List<Map<String, Object>> imgs = parseJsonArray(c.getConfigValue());
                        if (imgs != null && !imgs.isEmpty()) {
                            logoUrl = (String) imgs.get(0).get("url");
                        }
                    }
                }
            }

            // 加载footer数据
            loadFooterData(model);

            model.addAttribute("logoUrl", logoUrl);

            // 会话验证和刷新逻辑 - 使用通用方法
            boolean sessionValid = validateSession(request, model);
            
            HttpSession session = request.getSession(false);
            Long memberId = null;
            boolean isGuest = false;
            
            if (!sessionValid) {
                // 会话无效，使用Guest模式
                String guestId = getGuestSessionId(request);
                memberId = generateGuestMemberId(guestId);
                isGuest = true;
            } else {
                // 会员模式：使用session中的memberId
                memberId = (Long) session.getAttribute("memberId");
                if (memberId == null) {
                    model.addAttribute("error", "Member ID not found in session");
                    return "mall/profile";
                }
            }
            
            // 如果是Guest用户，创建简化的profile信息
            if (isGuest) {
                String guestId = getGuestSessionId(request); // 重新获取guestId
                Map<String, Object> profile = new HashMap<>();
                profile.put("username", "Guest_" + guestId);
                profile.put("currentPoint", 0);
                profile.put("currentLevel", "Guest");
                
                model.addAttribute("profile", profile);
                model.addAttribute("isGuest", true);
                
                // 创建简化的levels数据
                List<Map<String, Object>> levels = createLevelsData();
                model.addAttribute("levels", levels);
                
                // 创建协议段落
                List<Map<String, Object>> agreementParagraphs = createAgreementParagraphs();
                model.addAttribute("agreementParagraphs", agreementParagraphs);
                
                return "mall/profile";
            }
            
            // Get member info from service
            MemberInfo memberInfo = memberService.selectMemberInfoById(memberId);
            if (memberInfo == null) {
                model.addAttribute("error", "Member not found");
                return "mall/profile";
            }
            
            // Create profile data matching API format
            Map<String, Object> profile = new HashMap<>();
            profile.put("username", memberInfo.getUsername());
            profile.put("currentPoint", memberInfo.getCurrentPoint() != null ? memberInfo.getCurrentPoint().intValue() : 0);
            
            // Convert level number to level name
            String currentLevel = getLevelName(memberInfo.getCurrentLevel());
            profile.put("currentLevel", currentLevel);
            
            model.addAttribute("profile", profile);
            
            // Create levels data
            List<Map<String, Object>> levels = createLevelsData();
            model.addAttribute("levels", levels);
            
            // Create agreement paragraphs
            List<Map<String, Object>> agreementParagraphs = createAgreementParagraphs();
            model.addAttribute("agreementParagraphs", agreementParagraphs);
            
        } catch (Exception e) {
            model.addAttribute("error", "Failed to load profile: " + e.getMessage());
        }
        
        return "mall/profile";
    }
    
    /**
     * Convert level number to level name
     */
    private String getLevelName(Integer level) {
        if (level == null) return "Bronze";
        switch (level) {
            case 1: return "Bronze";
            case 2: return "Silver";
            case 3: return "Gold";
            case 4: return "Platinum";
            case 5: return "Diamond";
            default: return "Bronze";
        }
    }
    
    /**
     * Create levels data matching the Vue component
     */
    private List<Map<String, Object>> createLevelsData() {
        List<Map<String, Object>> levels = new ArrayList<>();
        
        Map<String, Object> bronze = new HashMap<>();
        bronze.put("name", "Bronze");
        bronze.put("points", "0");
        bronze.put("desc", "Exclusive discounts\nAccess to community support");
        bronze.put("color", "#cd7f32");
        bronze.put("textColor", "#ffffff");
        levels.add(bronze);
        
        Map<String, Object> silver = new HashMap<>();
        silver.put("name", "Silver");
        silver.put("points", "50");
        silver.put("desc", "$10 Discount\nLive chat support");
        silver.put("color", "#c0c0c0");
        silver.put("textColor", "#000000");
        levels.add(silver);
        
        Map<String, Object> gold = new HashMap<>();
        gold.put("name", "Gold");
        gold.put("points", "100");
        gold.put("desc", "$20 Discount\nLive chat support");
        gold.put("color", "#ffd700");
        gold.put("textColor", "#000000");
        levels.add(gold);
        
        Map<String, Object> platinum = new HashMap<>();
        platinum.put("name", "Platinum");
        platinum.put("points", "200");
        platinum.put("desc", "3% Discount\nFree shipping\nDedicated Account Manager");
        platinum.put("color", "#e5e4e2");
        platinum.put("textColor", "#000000");
        levels.add(platinum);
        
        Map<String, Object> diamond = new HashMap<>();
        diamond.put("name", "Diamond");
        diamond.put("points", "500");
        diamond.put("desc", "5% Discount\nFree shipping\nDedicated Account Manager");
        diamond.put("color", "#b9f2ff");
        diamond.put("textColor", "#000000");
        levels.add(diamond);
        
        return levels;
    }
    
    /**
     * Create agreement paragraphs from database configuration
     */
    private List<Map<String, Object>> createAgreementParagraphs() {
        List<Map<String, Object>> paragraphs = new ArrayList<>();
        
        try {
            // 从数据库读取profile页面的agreement配置
            MallPageConfig query = new MallPageConfig();
            query.setPage("profile");
            query.setSection("agreement");
            query.setConfigKey("content");
            
            List<MallPageConfig> configs = pageConfigService.selectMallPageConfigList(query);
            
            String longText = "";
            if (!configs.isEmpty()) {
                // 使用数据库中的配置
                longText = configs.get(0).getConfigValue();
            } else {
                // 如果数据库中没有配置，使用默认文本
                longText = "What's a loyalty program?\n" +
                    "Our loyalty program rewards customers for choosing us as your preferred vendor. You'll earn points based on your activity throughout the year, which will determine your loyalty level. Each year, your points reset, and different loyalty levels come with various privileges, including exclusive discounts and services. For more details, please refer to the information above.\n\n" +
                    "How can I earn points?\n" +
                    "There are several ways to earn points:\n" +
                    "Complete the beginner's package.\n" +
                    "Make Purchases: Earn 1% of your total purchases from our Tor or Telegram shop in points.\n" +
                    "Refer a Friend:\n" +
                    "Share our Tor or Telegram shop with a friend: you'll earn 5% of your friend's total AUD spend on their first purchase in points.\n" +
                    "Refer a friend to our Telegram Channel: Earn 1 point.\n\n" +
                    "How long are my points valid for?\n" +
                    "Points are valid until 31st December of each year. Your accumulated points will determine your loyalty level for the following year.\n\n" +
                    "Can I use my points?\n" +
                    "Currently TP points are only used to determine your loyalty level for the next year. However, we are working on a new feature called \"Store Credit.\" which will allow you to convert points into AUD value for additional discounts. Stay tuned for more information!\n\n" +
                    "Can I cash out my points?\n" +
                    "No, points cannot be cashed out.\n\n" +
                    "Do I need to sign up to join the program?\n" +
                    "Yes, you must sign up to participate in the program. While we understand some customers prefer to order as guests for anonymity, registering allows us to track your order history and volume, which is essential for the program.\n\n" +
                    "How can I see my points?\n" +
                    "You can view your current points on this page. These points reflect your earnings for the current year and will influence your level for the following year. Points are automatically added to your account with each purchase. Any extra points earned through other activities will be manually added by our staff after verification.\n\n" +
                    "How can I get discounts?\n" +
                    "We are currently working on automating this process. In the meantime, you will receive a unique discount code that only you can use. For more details, please reach out to our friendly staff via Telegram at @TheProfessionals_CS1";
            }
            
            String[] lines = longText.split("\n");
            for (String line : lines) {
                if (line.trim().isEmpty()) continue;
                
                Map<String, Object> paragraph = new HashMap<>();
                paragraph.put("text", line.trim());
                paragraph.put("isQuestion", line.trim().endsWith("?"));
                paragraphs.add(paragraph);
            }
            
        } catch (Exception e) {
            // 如果出现异常，返回空列表或默认内容
        }
        
        return paragraphs;
    }

    /**
     * Orders page
     */
    @GetMapping("/orders")
    public String orders(HttpServletRequest request, Model model,
                        @RequestParam(value = "pageNum", required = false, defaultValue = "1") int pageNum,
                        @RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize) {
        try {
            // 查询所有 home 页配置
            MallPageConfig query = new MallPageConfig();
            query.setPage("home");
            List<MallPageConfig> configs = pageConfigService.selectMallPageConfigList(query);

            // 解析配置
            String logoUrl = null;

            for (MallPageConfig c : configs) {
                if ("logo".equals(c.getSection()) && "images".equals(c.getConfigKey())) {
                    try {
                        // 尝试解析新格式（包含images和url）
                        Map<String, Object> configData = new com.fasterxml.jackson.databind.ObjectMapper()
                            .readValue(c.getConfigValue(), new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>(){});
                        
                        List<Map<String, Object>> images = (List<Map<String, Object>>) configData.get("images");
                        if (images != null && !images.isEmpty()) {
                            logoUrl = (String) images.get(0).get("url");
                        }
                    } catch (Exception e) {
                        // 向后兼容：如果解析失败，尝试旧格式
                        List<Map<String, Object>> imgs = parseJsonArray(c.getConfigValue());
                        if (imgs != null && !imgs.isEmpty()) {
                            logoUrl = (String) imgs.get(0).get("url");
                        }
                    }
                }
            }

            // 加载footer数据
            loadFooterData(model);

            model.addAttribute("logoUrl", logoUrl);

            // 会话验证和刷新逻辑 - 使用通用方法
            boolean sessionValid = validateSession(request, model);
            
            HttpSession session = request.getSession(false);
            Long memberId = null;
            
            if (!sessionValid) {
                // 会话无效，使用Guest模式
                String guestId = getGuestSessionId(request);
                memberId = generateGuestMemberId(guestId);
            } else {
                // 会员模式：使用session中的memberId
                memberId = (Long) session.getAttribute("memberId");
                if (memberId == null) {
                    model.addAttribute("error", "Member ID not found in session");
                    return "mall/orders";
                }
            }
            
            // 获取当前会员信息，检查是否有关联账号
            Member currentMember = null;
            Long linkedAccountId = null;
            if (session != null && session.getAttribute("memberToken") != null) {
                currentMember = memberService.selectMemberByMemberId(memberId);
                if (currentMember != null && currentMember.getLinkedAccount() != null) {
                    linkedAccountId = currentMember.getLinkedAccount();
                }
            }
            
            // 查询订单：包括当前账号和关联账号的订单
            List<Order> orderList = new ArrayList<>();
            
            // 添加当前账号的订单
            List<Order> currentOrders = orderService.selectOrderListById(memberId);
            orderList.addAll(currentOrders);
            
            // 如果有关联账号，添加关联账号的订单
            if (linkedAccountId != null) {
                List<Order> linkedOrders = orderService.selectOrderListById(linkedAccountId);
                orderList.addAll(linkedOrders);
            }
            
            // 按创建时间倒序排序
            orderList.sort((o1, o2) -> {
                if (o1.getCreateTime() == null && o2.getCreateTime() == null) return 0;
                if (o1.getCreateTime() == null) return 1;
                if (o2.getCreateTime() == null) return -1;
                return o2.getCreateTime().compareTo(o1.getCreateTime());
            });
            // 分页
            int total = orderList.size();
            int fromIndex = Math.max(0, (pageNum - 1) * pageSize);
            int toIndex = Math.min(fromIndex + pageSize, total);
            List<Order> pagedOrderList = fromIndex < toIndex ? orderList.subList(fromIndex, toIndex) : new ArrayList<>();
            List<OrderListVO> voList = new ArrayList<>();
            for (Order order : pagedOrderList) {
                OrderListVO vo = new OrderListVO();
                vo.orderId = order.getOrderSn();
                vo.date = order.getCreateTime() != null ? new java.text.SimpleDateFormat("dd-MM-yyyy").format(order.getCreateTime()) : "";
                // 拼接items
                List<OrderItem> items = orderItemMapper.selectOrderItemsByOrderId(order.getId());
                StringBuilder itemsStr = new StringBuilder();
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
                    itemsStr.append(item.getProductName())
                            .append(" - ")
                            .append(formattedSpec)
                            .append(" ×")
                            .append(item.getQuantity()).append(" $")
                            .append(item.getPrice());
                    if (i != items.size() - 1) itemsStr.append(", ");
                }
                vo.items = itemsStr.toString();
                vo.shippingCost = order.getFreightAmount() != null ? "$" + order.getFreightAmount().setScale(2, java.math.RoundingMode.HALF_UP) : "$0.00";
                vo.total = order.getTotalAmount() != null ? "$" + order.getTotalAmount().setScale(2, java.math.RoundingMode.HALF_UP) : "$0.00";
                // 支付状态/链接
                Payment payment = paymentMapper.selectPaymentByOrderId(order.getId());
                if (payment != null) {
                    if (payment.getPayStatus() == null || payment.getPayStatus() == 0) {
                        vo.paymentStatus = "Awaiting";
                        vo.paymentLink = "/mall/temp-payment/pay?orderId=" + order.getId();
                    } else if (payment.getPayStatus() == 1) {
                        vo.paymentStatus = "Paid";
                        vo.paymentLink = "Order paid";
                    } else if (payment.getPayStatus() == 2) {
                        vo.paymentStatus = "Cancelled";
                        vo.paymentLink = "Order cancelled";
                    } else {
                        vo.paymentStatus = "Unknown";
                        vo.paymentLink = "-";
                    }
                } else {
                    vo.paymentStatus = "Awaiting";
                    vo.paymentLink = "/mall/static/checkout/payment?orderId=" + order.getId();
                }
                // 订单状态
                if (order.getStatus() != null) {
                    int status = order.getStatus();
                    if (status == 0) vo.orderStatus = "Pending";
                    else if (status == 1) vo.orderStatus = "Paid";
                    else if (status == 2) vo.orderStatus = "Fulfilled";
                    else if (status == 3) vo.orderStatus = "Shipped";
                    else if (status == 4) vo.orderStatus = "Cancelled";
                    else if (status == 5) vo.orderStatus = "isDeleted";
                    else vo.orderStatus = "Unknown";
                } else {
                    vo.orderStatus = "Unknown";
                }
                vo.disputeLink = "/mall/static/orders/dispute?orderId=" + order.getId();
                vo.isDisputed = order.getIsdispute() != null && order.getIsdispute() == 1;
                
                // 获取tracking number逻辑
                com.medusa.mall.domain.order.ShippingAddress shippingAddress = 
                    shippingAddressMapper.selectShippingAddressByOrderId(order.getId());
                
                if (shippingAddress != null && shippingAddress.getShippingNumber() != null 
                    && !shippingAddress.getShippingNumber().trim().isEmpty()
                    && order.getStatus() != null && order.getStatus() == 3) { // 订单状态为Shipped
                    
                    vo.trackingNumber = shippingAddress.getShippingNumber();
                    
                    // 获取会员信息
                    com.medusa.mall.domain.member.MemberInfo memberInfo = null;
                    if (session != null && session.getAttribute("memberToken") != null) {
                        memberInfo = memberService.selectMemberInfoById(memberId);
                    }
                    
                    // 判断是否显示tracking number
                    if (memberInfo != null) {
                        // 检查PCSP状态（优先使用memberInfo中已查询的PCSP状态）
                        boolean hasPcsp = false;
                        if (memberInfo.getPcspStatus() != null && memberInfo.getPcspStatus() == 1) {
                            hasPcsp = true;
                        } else {
                            // 备用：直接查询PCSP（包括关联账号）
                            try {
                                com.medusa.mall.domain.member.MemberPcsp activePcsp = 
                                    memberPcspService.selectActivePcspByMemberId(memberId);
                                
                                // 如果当前账号没有PCSP，且有关联账号，则查询关联账号的PCSP
                                if ((activePcsp == null || !activePcsp.isValid()) && memberInfo.getLinkedAccount() != null) {
                                    activePcsp = memberPcspService.selectActivePcspByMemberId(memberInfo.getLinkedAccount());
                                }
                                
                                hasPcsp = (activePcsp != null && activePcsp.isValid());
                            } catch (Exception e) {
                                // PCSP查询失败，默认无PCSP
                            }
                        }
                        
                        Integer currentLevel = memberInfo.getCurrentLevel();
                        
                        // 显示逻辑判断
                        if (hasPcsp) {
                            // PCSP用户：立即显示
                            vo.showTracking = true;
                            vo.trackingMessage = null;
                        } else if (currentLevel != null && currentLevel >= 4) {
                            // Platinum (4) 或 Diamond (5)：立即显示
                            vo.showTracking = true;
                            vo.trackingMessage = null;
                        } else {
                            // Bronze (1), Silver (2), Gold (3)：3天后显示
                            java.time.LocalDate shippedDate = null;
                            if (shippingAddress.getShippingTime() != null) {
                                shippedDate = shippingAddress.getShippingTime()
                                    .toInstant()
                                    .atZone(java.time.ZoneId.systemDefault())
                                    .toLocalDate();
                            }
                            
                            if (shippedDate != null) {
                                java.time.LocalDate availableDate = shippedDate.plusDays(3);
                                java.time.LocalDate today = java.time.LocalDate.now();
                                
                                if (today.isAfter(availableDate) || today.isEqual(availableDate)) {
                                    vo.showTracking = true;
                                    vo.trackingMessage = null;
                                } else {
                                    vo.showTracking = false;
                                    vo.trackingMessage = "Available after " + 
                                        availableDate.format(java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy"));
                                }
                            } else {
                                // 如果没有shipping_time，默认不显示
                                vo.showTracking = false;
                                vo.trackingMessage = "Tracking available soon";
                            }
                        }
                    } else {
                        // Guest用户：检查是否有PCSP
                        boolean hasPcsp = false;
                        try {
                            com.medusa.mall.domain.member.MemberPcsp activePcsp = 
                                memberPcspService.selectActivePcspByMemberId(memberId);
                            hasPcsp = (activePcsp != null && activePcsp.isValid());
                            
                            log.debug("Guest user {} PCSP status: {}", memberId, hasPcsp);
                        } catch (Exception e) {
                            log.debug("Failed to query PCSP for guest user {}: {}", memberId, e.getMessage());
                            // PCSP查询失败，默认无PCSP
                        }
                        
                        if (hasPcsp) {
                            // Guest用户有PCSP：立即显示tracking
                            vo.showTracking = true;
                            vo.trackingMessage = null;
                            log.info("Guest user {} with PCSP can view tracking immediately", memberId);
                        } else {
                            // Guest用户无PCSP：按照3天规则显示（和Bronze/Silver/Gold用户一样）
                            java.time.LocalDate shippedDate = null;
                            if (shippingAddress.getShippingTime() != null) {
                                shippedDate = shippingAddress.getShippingTime()
                                    .toInstant()
                                    .atZone(java.time.ZoneId.systemDefault())
                                    .toLocalDate();
                            }
                            
                            if (shippedDate != null) {
                                java.time.LocalDate availableDate = shippedDate.plusDays(3);
                                java.time.LocalDate today = java.time.LocalDate.now();
                                
                                if (today.isAfter(availableDate) || today.isEqual(availableDate)) {
                                    vo.showTracking = true;
                                    vo.trackingMessage = null;
                                } else {
                                    vo.showTracking = false;
                                    vo.trackingMessage = "Available after " + 
                                        availableDate.format(java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy"));
                                }
                            } else {
                                // 如果没有shipping_time，默认不显示
                                vo.showTracking = false;
                                vo.trackingMessage = "Tracking available soon";
                            }
                        }
                    }
                } else {
                    // 没有tracking number或订单未shipped
                    vo.showTracking = false;
                    vo.trackingNumber = null;
                    vo.trackingMessage = null;
                }
                
                // 提取拒绝原因（如果订单被取消且备注中包含"Vendor rejection"）
                vo.rejectionReason = null;
                if (order.getStatus() != null && order.getStatus() == 4 && order.getRemark() != null) {
                    String remark = order.getRemark();
                    // 检查是否包含 "Vendor rejection"
                    if (remark.contains("Vendor rejection")) {
                        // 提取拒绝原因：格式为 "Vendor rejection: [原因]" 或 "Vendor rejection"
                        String rejectionNote = null;
                        String[] lines = remark.split("\n");
                        for (String line : lines) {
                            String trimmedLine = line.trim();
                            if (trimmedLine.startsWith("Vendor rejection")) {
                                rejectionNote = trimmedLine;
                                break;
                            }
                        }
                        
                        if (rejectionNote != null) {
                            // 提取原因部分
                            int colonIndex = rejectionNote.indexOf(":");
                            if (colonIndex >= 0 && colonIndex < rejectionNote.length() - 1) {
                                String reason = rejectionNote.substring(colonIndex + 1).trim();
                                vo.rejectionReason = reason.isEmpty() ? null : reason;
                            }
                            // 如果没有冒号或冒号后没有内容，rejectionReason 保持为 null
                        }
                    }
                }
                
                voList.add(vo);
            }
            // 分页信息
            int totalPages = (int) Math.ceil((double) total / pageSize);
            model.addAttribute("orders", voList);
            model.addAttribute("pageNum", pageNum);
            model.addAttribute("pageSize", pageSize);
            model.addAttribute("total", total);
            model.addAttribute("totalPages", totalPages);
        } catch (Exception e) {
            model.addAttribute("error", "Failed to load orders: " + e.getMessage());
        }
        return "mall/orders";
    }

    /**
     * Logout page (GET method for static pages)
     */
    @GetMapping("/logout")
    public String logout(HttpServletRequest request, RedirectAttributes redirectAttributes) {
        try {
            // Clear session
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }
            
            redirectAttributes.addFlashAttribute("message", "You have been logged out successfully.");
            return "redirect:/mall/static/home";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Logout failed.");
            return "redirect:/mall/static/home";
        }
    }

    // 静态 map 用于演示跨请求存储 payment 信息
    public static class PaymentSessionStore {
        public static final java.util.Map<String, Payment> store = new java.util.concurrent.ConcurrentHashMap<>();
    }

    // VO for orders page
    public static class OrderListVO {
        public String orderId;
        public String date;
        public String items;
        public String shippingCost;
        public String total;
        public String paymentStatus;
        public String orderStatus;
        public String paymentLink;
        public String disputeLink;
        public boolean isDisputed;
        public String trackingNumber;
        public boolean showTracking;
        public String trackingMessage;
        public String rejectionReason;  // 拒绝原因（从 remark 中提取）
    }

    /**
     * Handle dispute request
     */
    @PostMapping("/orders/dispute")
    public String handleDispute(@RequestParam String orderId,
                               @RequestParam(required = false) String reason,
                               HttpServletRequest request,
                               RedirectAttributes redirectAttributes) {
        try {
            // 直接检查session，支持guest用户
            HttpSession session = request.getSession(false);
            Long memberId = null;
            boolean isGuest = false;
            
            if (session == null || session.getAttribute("memberToken") == null) {
                // Guest模式
                String guestId = getGuestSessionId(request);
                memberId = generateGuestMemberId(guestId);
                isGuest = true;
            } else {
                // 会员模式
                memberId = (Long) session.getAttribute("memberId");
                if (memberId == null) {
                    redirectAttributes.addFlashAttribute("error", "Member ID not found in session");
                    return "redirect:/mall/static/orders";
                }
            }
            
            // Get order by ID
            Order order = orderService.selectOrderById(orderId);
            if (order == null) {
                redirectAttributes.addFlashAttribute("error", "Order not found");
                return "redirect:/mall/static/orders";
            }
            
            // Check if order belongs to current user
            if (!order.getMemberId().equals(memberId)) {
                redirectAttributes.addFlashAttribute("error", "You can only dispute your own orders");
                return "redirect:/mall/static/orders";
            }
            
            // Update order dispute status
            order.setIsdispute(1);
            order.setDisputeTime(new Date());
            order.setDisputeReason(reason != null ? reason : "User initiated dispute");
            order.setDisputeBy(memberId.toString());
            
            int result = orderService.updateOrder(order);
            if (result > 0) {
                redirectAttributes.addFlashAttribute("message", "Dispute submitted successfully");
            } else {
                redirectAttributes.addFlashAttribute("error", "Failed to submit dispute");
            }
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to submit dispute: " + e.getMessage());
        }
        
        return "redirect:/mall/static/orders";
    }

    /**
     * Dispute page (GET method for form display)
     */
    @GetMapping("/orders/dispute")
    public String disputePage(@RequestParam String orderId,
                             HttpServletRequest request,
                             Model model) {
        try {
            // 查询所有 home 页配置
            MallPageConfig query = new MallPageConfig();
            query.setPage("home");
            List<MallPageConfig> configs = pageConfigService.selectMallPageConfigList(query);

            // 解析配置
            String logoUrl = null;

            for (MallPageConfig c : configs) {
                if ("logo".equals(c.getSection()) && "images".equals(c.getConfigKey())) {
                    try {
                        // 尝试解析新格式（包含images和url）
                        Map<String, Object> configData = new com.fasterxml.jackson.databind.ObjectMapper()
                            .readValue(c.getConfigValue(), new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>(){});
                        
                        List<Map<String, Object>> images = (List<Map<String, Object>>) configData.get("images");
                        if (images != null && !images.isEmpty()) {
                            logoUrl = (String) images.get(0).get("url");
                        }
                    } catch (Exception e) {
                        // 向后兼容：如果解析失败，尝试旧格式
                        List<Map<String, Object>> imgs = parseJsonArray(c.getConfigValue());
                        if (imgs != null && !imgs.isEmpty()) {
                            logoUrl = (String) imgs.get(0).get("url");
                        }
                    }
                }
            }

            // 加载footer数据
            loadFooterData(model);

            model.addAttribute("logoUrl", logoUrl);

            // 直接检查session，支持guest用户
            HttpSession session = request.getSession(false);
            Long memberId = null;
            boolean isGuest = false;
            
            if (session == null || session.getAttribute("memberToken") == null) {
                // Guest模式
                String guestId = getGuestSessionId(request);
                memberId = generateGuestMemberId(guestId);
                isGuest = true;
            } else {
                // 会员模式
                memberId = (Long) session.getAttribute("memberId");
                if (memberId == null) {
                    model.addAttribute("error", "Member ID not found in session");
                    return "redirect:/mall/static/orders";
                }
            }
            
            // Get order by ID
            Order order = orderService.selectOrderById(orderId);
            if (order == null) {
                model.addAttribute("error", "Order not found");
                return "redirect:/mall/static/orders";
            }
            
            // Check if order belongs to current user
            if (!order.getMemberId().equals(memberId)) {
                model.addAttribute("error", "You can only dispute your own orders");
                return "redirect:/mall/static/orders";
            }
            
            model.addAttribute("order", order);
            model.addAttribute("orderId", orderId);
            
        } catch (Exception e) {
            model.addAttribute("error", "Failed to load order: " + e.getMessage());
        }
        
        return "mall/dispute";
    }

    /**
     * Product detail page
     */
    @GetMapping("/product/{id}")
    public String productDetail(@PathVariable("id") Long id,
                               @RequestParam(value = "quantity", required = false) Integer quantity,
                               @RequestParam(value = "bondApplicationId", required = false) Long bondApplicationId,
                               HttpServletRequest request,
                               Model model) {
        try {
            // 查询所有 home 页配置
            MallPageConfig query = new MallPageConfig();
            query.setPage("home");
            List<MallPageConfig> configs = pageConfigService.selectMallPageConfigList(query);

            // 解析配置
            String logoUrl = null;

            for (MallPageConfig c : configs) {
                if ("logo".equals(c.getSection()) && "images".equals(c.getConfigKey())) {
                    try {
                        // 尝试解析新格式（包含images和url）
                        Map<String, Object> configData = new com.fasterxml.jackson.databind.ObjectMapper()
                            .readValue(c.getConfigValue(), new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>(){});
                        
                        List<Map<String, Object>> images = (List<Map<String, Object>>) configData.get("images");
                        if (images != null && !images.isEmpty()) {
                            logoUrl = (String) images.get(0).get("url");
                        }
                    } catch (Exception e) {
                        // 向后兼容：如果解析失败，尝试旧格式
                        List<Map<String, Object>> imgs = parseJsonArray(c.getConfigValue());
                        if (imgs != null && !imgs.isEmpty()) {
                            logoUrl = (String) imgs.get(0).get("url");
                        }
                    }
                }
            }

            // 加载footer数据
            loadFooterData(model);

            model.addAttribute("logoUrl", logoUrl);

            // 查询SKU信息
            Product2 sku = product2Service.selectProduct2ById(id);
            if (sku == null) {
                model.addAttribute("error", "Product not found");
                return "mall/product-detail";
            }
            // 查询主产品信息
            Product product = productService.selectProductByProductId(sku.getProductId());
            if (product == null) {
                model.addAttribute("error", "Product master not found");
                return "mall/product-detail";
            }
            // 查询该产品的所有规格（同productId下所有SKU）
            com.medusa.mall.domain.Product2 filter = new com.medusa.mall.domain.Product2();
            filter.setProductId(sku.getProductId());
            java.util.List<Product2> specEntities = product2Service.selectProduct2List(filter);
            
            // 转换为ProductDisplayVO以支持formattedModel
            java.util.List<ProductDisplayVO> specs = new java.util.ArrayList<>();
            for (Product2 spec : specEntities) {
                ProductDisplayVO vo = new ProductDisplayVO();
                vo.setId(spec.getId());
                vo.setProductId(spec.getProductId());
                vo.setName(product.getName());
                vo.setSku(spec.getSku());
                vo.setModel(spec.getModel() != null ? spec.getModel().setScale(2, java.math.RoundingMode.HALF_UP).toString() : "");
                vo.setUnit(spec.getUnit());
                vo.setPrice(spec.getPrice());
                vo.setCurrency(spec.getCurrency());
                vo.setInventory(spec.getInventory());
                vo.setStatus(spec.getStatus());
                specs.add(vo);
            }
            
            // ✅ Sort specs by model value (numerical order: small -> large)
            specs.sort((a, b) -> {
                try {
                    // Extract numeric value from model string
                    String modelA = a.getModel() != null ? a.getModel() : "0";
                    String modelB = b.getModel() != null ? b.getModel() : "0";
                    
                    // Parse as BigDecimal for accurate decimal comparison
                    java.math.BigDecimal valueA = new java.math.BigDecimal(modelA);
                    java.math.BigDecimal valueB = new java.math.BigDecimal(modelB);
                    
                    return valueA.compareTo(valueB);
                } catch (Exception e) {
                    // If parsing fails, fallback to string comparison
                    return a.getModel().compareTo(b.getModel());
                }
            });
            
            log.debug("📊 [productDetail] Sorted {} specs by model value (small -> large)", specs.size());
            
            model.addAttribute("product", product);
            model.addAttribute("sku", sku);
            model.addAttribute("specs", specs);
            
            // Set default quantity if provided
            if (quantity != null && quantity > 0) {
                model.addAttribute("defaultQuantity", quantity);
            }
            
            // Store bondApplicationId and bondApplicationNumber in session for order creation
            if (bondApplicationId != null) {
                HttpSession session = request.getSession(true);
                session.setAttribute("bondApplicationId", bondApplicationId);
                
                // Also get applicationNumber from request parameter
                String bondApplicationNumber = request.getParameter("bondApplicationNumber");
                if (bondApplicationNumber != null && !bondApplicationNumber.isEmpty()) {
                    session.setAttribute("bondApplicationNumber", bondApplicationNumber);
                }
                
                log.info("[Product Detail] Bond application info stored in session: id={}, number={}", 
                    bondApplicationId, bondApplicationNumber);
            }
            
        } catch (Exception e) {
            model.addAttribute("error", "Failed to load product detail: " + e.getMessage());
        }
        return "mall/product-detail";
    }

    // 工具方法：解析JSON数组
    private List<Map<String, Object>> parseJsonArray(String json) {
        if (json == null) return null;
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper()
                .readValue(json, new com.fasterxml.jackson.core.type.TypeReference<List<Map<String, Object>>>(){});
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * 使用PGP公钥加密地址信息
     */
    private String encryptAddressWithPGP(String address1, String address2, String city, String state, String postalCode, String country, String pgpKey) {
        try {
            // 构建要加密的地址信息
            StringBuilder addressData = new StringBuilder();
            addressData.append("Address Line 1: ").append(address1).append("\n");
            if (address2 != null && !address2.trim().isEmpty()) {
                addressData.append("Address Line 2: ").append(address2).append("\n");
            }
            addressData.append("City: ").append(city).append("\n");
            addressData.append("State/Province: ").append(state).append("\n");
            addressData.append("Postal Code: ").append(postalCode).append("\n");
            addressData.append("Country: ").append(country).append("\n");
            addressData.append("Encrypted at: ").append(new java.util.Date()).append("\n");
            
            String plainText = addressData.toString();
            
            // 这里应该使用真正的PGP加密库
            // 由于这是一个示例，我们使用一个简化的加密方法
            // 在实际生产环境中，应该使用BouncyCastle或其他PGP库
            
            // 模拟PGP加密（在实际应用中，这里应该调用真正的PGP加密）
            String encryptedData = simulatePGPEncryption(plainText, pgpKey);
            
            return encryptedData;
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to encrypt address with PGP: " + e.getMessage(), e);
        }
    }
    
    /**
     * 模拟PGP加密（在实际应用中应替换为真正的PGP加密）
     */
    private String simulatePGPEncryption(String plainText, String pgpKey) {
        try {
            // 这是一个简化的模拟实现
            // 在实际应用中，应该使用BouncyCastle库进行真正的PGP加密
            
            // 提取密钥ID（模拟）
            String keyId = extractKeyIdFromPGP(pgpKey);
            
            // 创建加密的头部信息
            StringBuilder encrypted = new StringBuilder();
            encrypted.append("-----BEGIN PGP MESSAGE-----\n");
            encrypted.append("Version: BCPG v1.68\n");
            encrypted.append("Comment: Encrypted with key ID: ").append(keyId).append("\n\n");
            
            // 模拟加密数据（Base64编码）
            String base64Data = java.util.Base64.getEncoder().encodeToString(plainText.getBytes("UTF-8"));
            
            // 将Base64数据分块显示
            int chunkSize = 64;
            for (int i = 0; i < base64Data.length(); i += chunkSize) {
                int end = Math.min(i + chunkSize, base64Data.length());
                encrypted.append(base64Data.substring(i, end)).append("\n");
            }
            
            encrypted.append("-----END PGP MESSAGE-----\n");
            
            return encrypted.toString();
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to simulate PGP encryption: " + e.getMessage(), e);
        }
    }
    
    /**
     * 从PGP公钥中提取密钥ID（模拟实现）
     */
    private String extractKeyIdFromPGP(String pgpKey) {
        try {
            // 这是一个简化的实现
            // 在实际应用中，应该正确解析PGP密钥来获取密钥ID
            
            // 生成一个基于密钥内容的哈希作为密钥ID
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(pgpKey.getBytes("UTF-8"));
            
            // 取前8个字节作为密钥ID
            StringBuilder keyId = new StringBuilder();
            for (int i = 0; i < 8; i++) {
                keyId.append(String.format("%02X", hash[i]));
            }
            
            return keyId.toString();
            
        } catch (Exception e) {
            // 如果出错，返回一个默认的密钥ID
            return "12345678";
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

    /**
     * Check if all items in cart are digital products
     */
    private boolean isCartAllDigital(HttpSession session, HttpServletRequest request) {
        try {
            // Get cart
            Cart cart;
            boolean isGuest = (session.getAttribute("memberToken") == null);
            
            if (isGuest) {
                String guestId = getGuestSessionId(request);
                cart = cartService.getGuestCart(guestId);
            } else {
                Long memberId = (Long) session.getAttribute("memberId");
                cart = cartService.getCartWithDetails(memberId);
            }
            
            if (cart == null || cart.getDetailedItems() == null || cart.getDetailedItems().isEmpty()) {
                return false;
            }
            
            // Check if all items are digital products or services (no physical shipping required)
            // Categories that don't need shipping: "Digital Product", "Service"
            for (Object item : cart.getDetailedItems()) {
                if (item instanceof CartItemVO) {
                    CartItemVO cartItem = (CartItemVO) item;
                    
                    // Query product to get category
                    // productId in CartItemVO is actually the Product2 SKU ID
                    Product2 product2 = product2Service.selectProduct2ById(cartItem.getProductId());
                    if (product2 != null) {
                        // Get parent product to check category (use productId which is String type)
                        Product product = productService.selectProductByProductId(product2.getProductId());
                        if (product != null) {
                            String category = product.getCategory();
                            
                            // Allow both "Digital Product" and "Service" to skip address
                            if (category == null || 
                                (!category.equals("Digital Product") && !category.equals("Service"))) {
                                return false; // Found a product that requires physical shipping
                            }
                        } else {
                            return false; // Parent product not found
                        }
                    } else {
                        return false; // Product2 not found
                    }
                }
            }
            
            return true;
            
        } catch (Exception e) {
            log.error("Error checking cart digital status:", e);
            return false;
        }
    }

    /**
     * Check if noscript version should be used
     */
    private boolean shouldUseNoscript(HttpServletRequest request) {
        // Method 1: Check URL parameter
        String noscriptParam = request.getParameter("noscript");
        if ("true".equalsIgnoreCase(noscriptParam) || "1".equals(noscriptParam)) {
            return true;
        }
        
        // Method 2: Check User-Agent for text-based browsers or bots
        String userAgent = request.getHeader("User-Agent");
        if (userAgent != null) {
            String ua = userAgent.toLowerCase();
            if (ua.contains("lynx") || ua.contains("w3m") || ua.contains("links") || 
                ua.contains("elinks") || ua.contains("curl") || ua.contains("wget") ||
                ua.contains("python-requests") || ua.contains("go-http-client") || 
                ua.contains("java/")) {
                return true;
            }
        }
        
        // Method 3: Check Accept header
        String acceptHeader = request.getHeader("Accept");
        if (acceptHeader != null && acceptHeader.contains("text/html") && 
            !acceptHeader.contains("application/javascript")) {
            if (!acceptHeader.contains("application/json") && !acceptHeader.contains("application/xml")) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Vendor application form (requires login)
     * Auto-detects noscript version and shows JavaScript required message
     */
    @GetMapping({"/vendor/application", "/vendor/application.html"})
    public String vendorApplication(HttpServletRequest request, Model model) {
        if (shouldUseNoscript(request)) {
            return "vendor/noscript/require-js";
        }
        return "vendor/application";
    }

    /**
     * Vendor status tracking page (requires login)
     * Auto-detects noscript version and shows JavaScript required message
     */
    @GetMapping({"/vendor/status", "/vendor/status.html"})
    public String vendorStatus(HttpServletRequest request, Model model) {
        if (shouldUseNoscript(request)) {
            return "vendor/noscript/require-js";
        }
        return "vendor/status";
    }

    /**
     * Vendor portal hub page
     */
    @GetMapping({"/vendor", "/vendor/index", "/vendor/index.html"})
    public String vendorIndex(HttpServletRequest request) {
        // If noscript detected, show JavaScript required message
        if (shouldUseNoscript(request)) {
            return "vendor/noscript/require-js";
        }
        // Redirect to login page instead of index page
        return "redirect:/mall/static/vendor/login";
    }

    /**
     * Vendor login page
     * Auto-detects noscript version and shows JavaScript required message
     */
    @GetMapping({"/vendor/login", "/vendor/login.html"})
    public String vendorLogin(HttpServletRequest request, Model model) {
        if (shouldUseNoscript(request)) {
            return "vendor/noscript/require-js";
        }
        return "vendor/login";
    }

    /**
     * Vendor register page
     * Auto-detects noscript version and shows JavaScript required message
     */
    @GetMapping({"/vendor/register", "/vendor/register.html"})
    public String vendorRegister(HttpServletRequest request, Model model) {
        if (shouldUseNoscript(request)) {
            return "vendor/noscript/require-js";
        }
        return "vendor/register";
    }

    /**
     * Vendor admin page (requires login)
     */
    @GetMapping({"/vendor/admin", "/vendor/admin.html"})
    public String vendorAdmin(HttpServletRequest request) {
        if (shouldUseNoscript(request)) {
            return "vendor/noscript/require-js";
        }
        return "vendor/admin";
    }

    /**
     * Vendor product management page (requires login)
     * Auto-detects noscript version and shows JavaScript required message
     */
    @GetMapping({"/vendor/products", "/vendor/products.html"})
    public String vendorProducts(HttpServletRequest request, Model model) {
        if (shouldUseNoscript(request)) {
            return "vendor/noscript/require-js";
        }
        return "vendor/products";
    }

    @GetMapping({"/vendor/bond-level", "/vendor/bond-level.html"})
    public String vendorBondLevel(HttpServletRequest request, Model model) {
        if (shouldUseNoscript(request)) {
            return "vendor/noscript/require-js";
        }
        return "vendor/bond-level";
    }

    /**
     * Vendor order management page (requires login)
     * Auto-detects noscript version and shows JavaScript required message
     */
    @GetMapping({"/vendor/orders", "/vendor/orders.html"})
    public String vendorOrders(HttpServletRequest request, Model model) {
        if (shouldUseNoscript(request)) {
            return "vendor/noscript/require-js";
        }
        return "vendor/orders";
    }

    /**
     * Vendor withdrawal management page (requires login)
     * Auto-detects noscript version and forwards if needed
     */
    @GetMapping({"/vendor/withdrawal", "/vendor/withdrawal.html"})
    public String vendorWithdrawal(HttpServletRequest request, Model model) {
        if (shouldUseNoscript(request)) {
            return "vendor/noscript/require-js";
        }
        return "vendor/withdrawal";
    }

    /**
     * 加载footer数据并添加到model中
     * @param model Spring MVC的Model对象
     */
    private void loadFooterData(Model model) {
        String footerLogo = null;
        List<Map<String, Object>> footerLinks = new ArrayList<>();

        try {
            // 查询footer配置
            MallPageConfig query = new MallPageConfig();
            query.setPage("footer");
            List<MallPageConfig> footerConfigs = pageConfigService.selectMallPageConfigList(query);

            for (MallPageConfig c : footerConfigs) {
                if ("contacts".equals(c.getSection()) && "list".equals(c.getConfigKey())) {
                    footerLinks = parseJsonArray(c.getConfigValue());
                }
                if ("logo".equals(c.getSection()) && "images".equals(c.getConfigKey())) {
                    try {
                        // 尝试解析新格式（包含images和url）
                        Map<String, Object> configData = new com.fasterxml.jackson.databind.ObjectMapper()
                            .readValue(c.getConfigValue(), new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>(){});
                        
                        List<Map<String, Object>> images = (List<Map<String, Object>>) configData.get("images");
                        if (images != null && !images.isEmpty()) {
                            footerLogo = (String) images.get(0).get("url");
                        }
                    } catch (Exception e) {
                        // 向后兼容：如果解析失败，尝试旧格式
                        List<Map<String, Object>> imgs = parseJsonArray(c.getConfigValue());
                        if (imgs != null && !imgs.isEmpty()) {
                            footerLogo = (String) imgs.get(0).get("url");
                        }
                    }
                }
            }

            // 将footer数据添加到model中
            model.addAttribute("footerLogo", footerLogo);
            model.addAttribute("footerLinks", footerLinks);
        } catch (Exception e) {
            // 即使出错也添加空的footer数据，避免模板渲染错误
            model.addAttribute("footerLogo", null);
            model.addAttribute("footerLinks", new ArrayList<>());
        }
    }
    
    /**
     * Bond Payment Page for Vendor Members
     * Allows vendor members to select payment method (BTC/USDT/XMR) before creating order
     */
    @GetMapping("/vendor/bond/payment")
    public String bondPayment(@RequestParam(value = "orderId", required = false) String orderId,
                              @RequestParam(value = "level", required = false) Integer level,
                              @RequestParam(value = "applicationId", required = false) Long applicationId,
                              @RequestParam(value = "applicationNumber", required = false) String applicationNumber,
                              HttpServletRequest request, 
                              Model model) {
        if (shouldUseNoscript(request)) {
            return "vendor/noscript/require-js";
        }
        try {
            // 1. Verify vendor member authentication
            Long vendorMemberId = VendorAuthUtils.getCurrentVendorMemberId();
            if (vendorMemberId == null) {
                return "redirect:/vendor/login";
            }
            
            // 2. If orderId is provided, load existing order (for re-payment scenarios)
            if (orderId != null && !orderId.isEmpty()) {
                Order order = orderService.selectOrderById(orderId);
                if (order != null && "BOND_PAYMENT".equals(order.getOrderType()) && order.getMemberId().equals(vendorMemberId)) {
                    Payment existingPayment = paymentService.getPaymentByOrderId(orderId);
                    if (existingPayment != null && existingPayment.getPayStatus() != null && existingPayment.getPayStatus() == 1) {
                        return "redirect:/vendor/status";
                    }
                    // Load order details
                    loadPaymentPageData(model, order, null);
                    return "mall/bond-payment";
                }
            }
            
            // 3. If application info is provided, prepare for new order creation
            if (level != null && applicationId != null && applicationNumber != null) {
                // Verify application
                VendorApplication application = 
                    applicationService.selectVendorApplicationById(applicationId);
                if (application == null || !application.getMemberId().equals(vendorMemberId)) {
                    model.addAttribute("error", "Application not found or access denied");
                    return "mall/bond-payment-error";
                }
                
                if (application.getBondOrderId() != null) {
                    model.addAttribute("error", "Bond already paid for this application");
                    return "mall/bond-payment-error";
                }
                
                // Get bond config to calculate amount
                VendorBondConfig bondConfig = 
                    bondConfigService.selectVendorBondConfigByLevel(level);
                if (bondConfig == null) {
                    model.addAttribute("error", "Invalid bond level");
                    return "mall/bond-payment-error";
                }
                
                // Get product price
                Product2 bondProduct = product2Service.selectProduct2BySku("BOND-BASE-UNIT");
                if (bondProduct == null) {
                    model.addAttribute("error", "Bond product not found");
                    return "mall/bond-payment-error";
                }
                
                BigDecimal totalAmount = bondProduct.getPrice().multiply(new BigDecimal(level));
                
                // Load page config
                loadPaymentPageData(model, null, totalAmount);
                
                // Pass application info for order creation
                model.addAttribute("level", level);
                model.addAttribute("applicationId", applicationId);
                model.addAttribute("applicationNumber", applicationNumber);
                model.addAttribute("totalAmount", totalAmount);
                
                return "mall/bond-payment";
            }
            
            model.addAttribute("error", "Missing required parameters");
            return "mall/bond-payment-error";
            
        } catch (Exception e) {
            log.error("Error loading bond payment page", e);
            model.addAttribute("error", "Failed to load payment page: " + e.getMessage());
            return "mall/bond-payment-error";
        }
    }
    
    /**
     * Helper method to load payment page data
     */
    private void loadPaymentPageData(Model model, Order order, BigDecimal totalAmount) {
        MallPageConfig query = new MallPageConfig();
        query.setPage("home");
        List<MallPageConfig> configs = pageConfigService.selectMallPageConfigList(query);
        String logoUrl = null;
        for (MallPageConfig c : configs) {
            if ("logo".equals(c.getSection()) && "images".equals(c.getConfigKey())) {
                try {
                    Map<String, Object> configData = new com.fasterxml.jackson.databind.ObjectMapper()
                        .readValue(c.getConfigValue(), new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>(){});
                    List<Map<String, Object>> images = (List<Map<String, Object>>) configData.get("images");
                    if (images != null && !images.isEmpty()) {
                        logoUrl = (String) images.get(0).get("url");
                    }
                } catch (Exception e) {
                    List<Map<String, Object>> imgs = parseJsonArray(c.getConfigValue());
                    if (imgs != null && !imgs.isEmpty()) {
                        logoUrl = (String) imgs.get(0).get("url");
                    }
                }
            }
        }
        
        loadFooterData(model);
        model.addAttribute("logoUrl", logoUrl);
        
        if (order != null) {
            model.addAttribute("order", order);
            model.addAttribute("orderId", order.getId());
            model.addAttribute("totalAmount", order.getTotalAmount());
            List<OrderItem> orderItems = orderItemMapper.selectOrderItemsByOrderId(order.getId());
            model.addAttribute("orderItems", orderItems != null ? orderItems : new ArrayList<>());
        } else {
            model.addAttribute("orderItems", new ArrayList<>());
        }
    }
    
    /**
     * Confirm Bond Payment - Create order and BTCPay invoice
     * This is called when user selects payment method
     */
    @PostMapping("/vendor/bond/payment/confirm")
    public String confirmBondPayment(@RequestParam("paymentMethod") String paymentMethod,
                                     @RequestParam("level") Integer level,
                                     @RequestParam("applicationId") Long applicationId,
                                     @RequestParam("applicationNumber") String applicationNumber,
                                     HttpServletRequest request,
                                     RedirectAttributes redirectAttributes) {
        try {
            // 1. Verify vendor member authentication
            Long vendorMemberId = VendorAuthUtils.getCurrentVendorMemberId();
            if (vendorMemberId == null) {
                redirectAttributes.addFlashAttribute("error", "Please login first");
                return "redirect:/vendor/login";
            }
            
            // 2. Verify application
            VendorApplication application = applicationService.selectVendorApplicationById(applicationId);
            if (application == null || !application.getMemberId().equals(vendorMemberId)) {
                redirectAttributes.addFlashAttribute("error", "Application not found or access denied");
                return "redirect:/vendor/status";
            }
            
            if (application.getBondOrderId() != null) {
                redirectAttributes.addFlashAttribute("error", "Bond already paid for this application");
                return "redirect:/vendor/status";
            }
            
            // 3. Validate level
            if (level == null || level < 1 || level > 10) {
                redirectAttributes.addFlashAttribute("error", "Invalid bond level");
                return "redirect:/vendor/status";
            }
            
            // 4. Get Bond product
            Product2 bondProduct = product2Service.selectProduct2BySku("BOND-BASE-UNIT");
            if (bondProduct == null) {
                redirectAttributes.addFlashAttribute("error", "Bond product not found");
                return "redirect:/vendor/status";
            }
            
            Product product = productService.selectProductByProductId(bondProduct.getProductId());
            if (product == null) {
                redirectAttributes.addFlashAttribute("error", "Bond product configuration error");
                return "redirect:/vendor/status";
            }
            
            // 5. Generate order number
            String orderNumber = OrderNumberGeneratorWithUniqueness.generatePlatformOrderNumber(
                orderId -> orderMapper.checkOrderIdExists(orderId) > 0
            );
            
            // 6. Calculate total amount
            BigDecimal unitPrice = bondProduct.getPrice();
            BigDecimal quantity = new BigDecimal(level);
            BigDecimal totalAmount = unitPrice.multiply(quantity);
            
            // 7. Create Order
            Order order = new Order();
            order.setId(orderNumber);
            order.setOrderSn(orderNumber);
            order.setMemberId(vendorMemberId);
            order.setSourceType(0);  // OS channel
            order.setStatus(0);  // Pending
            order.setCreateTime(new Date());
            order.setMemberLevel(0);
            order.setOrderType("BOND_PAYMENT");
            order.setBondApplicationId(application.getId());
            order.setBondApplicationNumber(application.getApplicationId());
            order.setTotalAmount(totalAmount);
            order.setFreightAmount(BigDecimal.ZERO);
            order.setDiscountAmount(BigDecimal.ZERO);
            order.setCouponAmount(BigDecimal.ZERO);
            
            int result = orderMapper.insertOrder(order);
            if (result <= 0) {
                throw new RuntimeException("Failed to create bond order");
            }
            
            log.info("[Bond Payment] Order created: {}", orderNumber);
            
            // 8. Create Order Item
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
            orderItem.setProductOrigin(0);
            orderItem.setOriginId(null);
            orderItem.setCreateTime(new Date());
            orderItem.setUpdateTime(new Date());
            
            orderItemMapper.insertOrderItem(orderItem);
            
            log.info("[Bond Payment] Order item created for order: {}", orderNumber);
            
            // 9. Create payment record and BTCPay invoice
            Payment payment = new Payment();
            payment.setOrderId(orderNumber);
            payment.setPayType(paymentMethod);  // "0" for BTC, "usdt" for USDT, "xmr" for XMR
            payment.setPayStatus(0);  // Pending
            payment.setAmount(totalAmount);
            payment.setCurrency("USD");
            payment.setUserId(vendorMemberId);
            
            // Create payment (this will create BTCPay invoice)
            Payment createdPayment = paymentService.createPayment(payment);
            
            if (createdPayment == null) {
                redirectAttributes.addFlashAttribute("error", "Failed to create payment");
                return "redirect:/mall/static/vendor/bond/payment?level=" + level + 
                       "&applicationId=" + applicationId + "&applicationNumber=" + applicationNumber;
            }
            
            log.info("[Bond Payment] Payment created for order: {}", orderNumber);
            
            // 10. Redirect to payment QR code page
            return "redirect:/mall/temp-payment/pay?orderId=" + orderNumber;
            
        } catch (Exception e) {
            log.error("Error confirming bond payment", e);
            redirectAttributes.addFlashAttribute("error", "Failed to process payment: " + e.getMessage());
            return "redirect:/vendor/status";
        }
    }

    /**
     * Refund Policy and PGP Key page
     */
    @GetMapping("/refund-policy")
    public String refundPolicy(Model model) {
        try {
            // 查询 home 页配置（获取 logo）
            MallPageConfig query = new MallPageConfig();
            query.setPage("home");
            List<MallPageConfig> configs = pageConfigService.selectMallPageConfigList(query);

            String logoUrl = null;
            for (MallPageConfig c : configs) {
                if ("logo".equals(c.getSection()) && "images".equals(c.getConfigKey())) {
                    try {
                        Map<String, Object> configData = new com.fasterxml.jackson.databind.ObjectMapper()
                            .readValue(c.getConfigValue(), new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>(){});
                        List<Map<String, Object>> images = (List<Map<String, Object>>) configData.get("images");
                        if (images != null && !images.isEmpty()) {
                            logoUrl = (String) images.get(0).get("url");
                        }
                    } catch (Exception e) {
                        List<Map<String, Object>> imgs = parseJsonArray(c.getConfigValue());
                        if (imgs != null && !imgs.isEmpty()) {
                            logoUrl = (String) imgs.get(0).get("url");
                        }
                    }
                }
            }

            // 加载 footer 数据
            loadFooterData(model);
            
            model.addAttribute("logoUrl", logoUrl);

            return "mall/refund-policy";
        } catch (Exception e) {
            log.error("Error loading refund policy page", e);
            model.addAttribute("error", "Unable to load page");
            return "mall/error";
        }
    }
} 
package com.medusa.mall.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.medusa.mall.domain.vendor.VendorApplication;
import com.medusa.mall.domain.vendor.VendorBondConfig;
import com.medusa.mall.domain.vendor.VendorWithdrawalAddress;
import com.medusa.mall.domain.vendor.VendorWithdrawalRequest;
import com.medusa.mall.domain.order.Order;
import com.medusa.mall.service.IVendorApplicationService;
import com.medusa.mall.service.IVendorBondConfigService;
import com.medusa.mall.service.IVendorWithdrawalService;
import com.medusa.mall.service.IVendorProductService;
import com.medusa.mall.service.IVendorOrderService;
import com.medusa.mall.service.IVendorMemberService;
import com.medusa.mall.service.IOrderService;
import com.medusa.mall.domain.vendor.VendorMember;
import com.medusa.common.utils.SecurityUtils;
import com.medusa.common.utils.StringUtils;
import com.medusa.common.utils.uuid.IdUtils;
import jakarta.servlet.http.HttpSession;
import com.medusa.mall.util.VendorAuthUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Vendor Static Page Controller for NoScript Support
 * Handles server-side rendering of vendor pages without JavaScript dependency
 * 
 * @author medusa
 */
@Controller
@RequestMapping("/mall/static/vendor")
public class VendorStaticPageController {
    
    private static final Logger log = LoggerFactory.getLogger(VendorStaticPageController.class);
    
    @Autowired
    private IVendorApplicationService applicationService;
    
    @Autowired
    private IVendorBondConfigService bondConfigService;
    
    @Autowired
    private IVendorProductService vendorProductService;
    
    @Autowired
    private IVendorOrderService vendorOrderService;
    
    @Autowired
    private IVendorWithdrawalService withdrawalService;
    
    @Autowired
    private IVendorMemberService vendorMemberService;
    
    @Autowired
    private IOrderService orderService;
    
    /**
     * Check if user is authenticated, redirect to login if not
     */
    private Long requireAuth(HttpServletRequest request) {
        Long memberId = VendorAuthUtils.getCurrentVendorMemberId();
        if (memberId == null) {
            log.warn("Unauthorized access attempt to vendor page");
            return null;
        }
        return memberId;
    }
    
    /**
     * Get current logged in username from session
     */
    private String getCurrentUsername(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            String username = (String) session.getAttribute(VendorAuthUtils.SESSION_VENDOR_MEMBER_USERNAME);
            return username;
        }
        return null;
    }
    
    /**
     * Add current user info to model for display in templates
     */
    private void addUserInfoToModel(HttpServletRequest request, Model model) {
        String username = getCurrentUsername(request);
        model.addAttribute("currentUsername", username);
        model.addAttribute("isLoggedIn", username != null);
    }
    
    /**
     * Get vendor application by member ID (latest one)
     */
    private VendorApplication getApplicationByMemberId(Long memberId) {
        VendorApplication query = new VendorApplication();
        query.setMemberId(memberId);
        List<VendorApplication> applications = applicationService.selectVendorApplicationList(query);
        
        log.info("Query applications for memberId={}, found {} applications", memberId, 
            applications != null ? applications.size() : 0);
        
        if (applications != null && !applications.isEmpty()) {
            // Return the most recent application
            VendorApplication app = applications.get(0);
            log.info("Selected application: id={}, applicationId={}, status={}, vendorName={}, location={}, productCategories={}", 
                app.getId(), app.getApplicationId(), app.getStatus(), app.getVendorName(), 
                app.getLocation(), app.getProductCategories());
            return app;
        }
        
        log.warn("No applications found for memberId={}", memberId);
        return null;
    }
    
    /**
     * Vendor Status/Dashboard Page (NoScript)
     * Displays application status, bond information, and wallet addresses
     * Note: This method is called by StaticPageController after noscript detection,
     * so it doesn't have a direct @GetMapping mapping to avoid route conflicts
     */
    public String vendorStatus(HttpServletRequest request, Model model) {
        Long memberId = requireAuth(request);
        if (memberId == null) {
            return "redirect:/mall/static/vendor/login";
        }
        
        log.info("[NoScript Status] Loading status page for memberId={}", memberId);
        
        // Add user info to model
        addUserInfoToModel(request, model);
        
        try {
            // Load application information
            VendorApplication application = getApplicationByMemberId(memberId);
            model.addAttribute("application", application);
            
            // Also add individual fields to model as backup for Thymeleaf
            if (application != null) {
                model.addAttribute("applicationId", application.getApplicationId());
                model.addAttribute("vendorName", application.getVendorName());
                model.addAttribute("location", application.getLocation());
            }
            
            log.info("[NoScript Status] Application loaded: {}", application != null ? 
                "id=" + application.getId() + ", applicationId=" + application.getApplicationId() + 
                ", vendorName=" + application.getVendorName() + ", location=" + application.getLocation() +
                ", status=" + application.getStatus() : "null");
            
            if (application != null) {
                // Parse product categories JSON string
                List<String> productCategoriesList = new ArrayList<>();
                if (application.getProductCategories() != null && !application.getProductCategories().isEmpty()) {
                    try {
                        ObjectMapper objectMapper = new ObjectMapper();
                        if (application.getProductCategories().startsWith("[")) {
                            // JSON array
                            productCategoriesList = objectMapper.readValue(
                                application.getProductCategories(),
                                new TypeReference<List<String>>() {}
                            );
                        } else {
                            // Single string or comma-separated
                            productCategoriesList = Arrays.asList(application.getProductCategories().split(","));
                        }
                    } catch (Exception e) {
                        log.warn("Failed to parse product categories: {}", e.getMessage());
                        productCategoriesList = Arrays.asList(application.getProductCategories());
                    }
                }
                model.addAttribute("productCategoriesList", productCategoriesList);
                log.info("[NoScript Status] Product categories: {}", productCategoriesList);
                
                // Format status display text
                String statusDisplay = formatStatusDisplay(application.getStatus());
                model.addAttribute("statusDisplay", statusDisplay);
                
                // If approved, load bond information
                if ("approved".equals(application.getStatus())) {
                    Integer bondLevel = application.getBondLevel() != null ? 
                        application.getBondLevel() : 1;
                    
                    log.info("[NoScript Status] Application approved, bond level={}", bondLevel);
                    
                    VendorBondConfig bondConfig = bondConfigService.selectVendorBondConfigByLevel(bondLevel);
                    model.addAttribute("bondConfig", bondConfig);
                    model.addAttribute("bondLevel", bondLevel);
                    
                    log.info("[NoScript Status] Bond config loaded: {}", bondConfig != null ? 
                        "amount=" + bondConfig.getBondAmount() : "null");
                    
                    // Load bond order information if bond order ID exists
                    if (application.getBondOrderId() != null && !application.getBondOrderId().isEmpty()) {
                        try {
                            // bondOrderId is String, use selectOrderById directly
                            Order bondOrder = orderService.selectOrderById(application.getBondOrderId());
                            model.addAttribute("bondOrder", bondOrder);
                            log.info("[NoScript Status] Bond order loaded: orderId={}, status={}", 
                                application.getBondOrderId(), bondOrder != null ? bondOrder.getStatus() : "null");
                        } catch (Exception e) {
                            log.warn("Failed to load bond order: {}", e.getMessage());
                        }
                    }
                    
                    // Load withdrawal addresses if vendor ID exists
                    if (application.getVendorId() != null) {
                        try {
                            List<VendorWithdrawalAddress> addresses = withdrawalService.getWithdrawalAddresses(application.getVendorId());
                            model.addAttribute("withdrawalAddresses", addresses);
                            log.info("[NoScript Status] Withdrawal addresses loaded: {} addresses", 
                                addresses != null ? addresses.size() : 0);
                        } catch (Exception e) {
                            log.warn("Failed to load withdrawal addresses: {}", e.getMessage());
                        }
                    }
                }
            } else {
                log.warn("[NoScript Status] No application found for memberId={}", memberId);
            }
            
            // Add noscript flag to indicate this is noscript version
            model.addAttribute("noscript", true);
            
            return "vendor/noscript/status";
            
        } catch (Exception e) {
            log.error("Error loading vendor status page", e);
            model.addAttribute("error", "Failed to load page: " + e.getMessage());
            return "vendor/noscript/error";
        }
    }
    
    /**
     * Format status for display
     */
    private String formatStatusDisplay(String status) {
        if (status == null) return "Pending";
        
        switch (status.toLowerCase()) {
            case "pending":
                return "Pending";
            case "received":
                return "Received";
            case "validating":
                return "Validating";
            case "reviewing":
                return "Under Review";
            case "interview_required":
                return "Interview Required";
            case "additional_info_required":
                return "Additional Info Required";
            case "approved":
                return "Approved";
            case "rejected":
                return "Rejected";
            default:
                return status;
        }
    }
    
    /**
     * Vendor Application Form Page (NoScript)
     * Note: This method is called by StaticPageController after noscript detection,
     * so it doesn't have a direct @GetMapping mapping to avoid route conflicts
     */
    public String vendorApplication(HttpServletRequest request, Model model) {
        Long memberId = requireAuth(request);
        if (memberId == null) {
            return "redirect:/mall/static/vendor/login";
        }

        // Add user info to model
        addUserInfoToModel(request, model);

        try {
            // Check if there's an existing application
            VendorApplication existing = getApplicationByMemberId(memberId);
            if (existing != null) {
                model.addAttribute("existingApplication", existing);
                // If application is already submitted, redirect to status page
                if (!"draft".equals(existing.getStatus())) {
                    return "redirect:/mall/static/vendor/status";
                }
            }
            
            model.addAttribute("noscript", true);
            return "vendor/noscript/application";
            
        } catch (Exception e) {
            log.error("Error loading vendor application page", e);
            model.addAttribute("error", "Failed to load page: " + e.getMessage());
            return "vendor/noscript/error";
        }
    }
    
    /**
     * Submit Application (POST - NoScript)
     */
    @PostMapping("/application/submit")
    public String submitApplication(
            @ModelAttribute VendorApplication application,
            @RequestParam(value = "productCategories", required = false) String[] productCategories,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {
        
        Long memberId = requireAuth(request);
        if (memberId == null) {
            redirectAttributes.addFlashAttribute("error", "Please login first");
            return "redirect:/mall/static/vendor/login";
        }
        
        try {
            // Set member ID
            application.setMemberId(memberId);
            
            // Process productCategories - convert array to JSON string
            if (productCategories != null && productCategories.length > 0) {
                java.util.List<String> categories = java.util.Arrays.asList(productCategories);
                com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
                application.setProductCategories(objectMapper.writeValueAsString(categories));
            }
            
            // Set default status
            if (application.getStatus() == null || application.getStatus().isEmpty()) {
                application.setStatus("pending");
            }
            
            // Set default progress
            if (application.getReviewProgress() == null) {
                application.setReviewProgress(0);
            }
            
            // Convert hasMarketExperience from String to Integer if needed
            if (application.getHasMarketExperience() == null) {
                String hasExp = request.getParameter("hasMarketExperience");
                if (hasExp != null) {
                    application.setHasMarketExperience("1".equals(hasExp) || "yes".equalsIgnoreCase(hasExp) ? 1 : 0);
                }
            }
            
            // Convert offlineDelivery from String to Integer if needed
            if (application.getOfflineDelivery() == null) {
                String offline = request.getParameter("offlineDelivery");
                if (offline != null) {
                    application.setOfflineDelivery("1".equals(offline) || "yes".equalsIgnoreCase(offline) ? 1 : 0);
                }
            }
            
            // Check if updating existing application
            VendorApplication existing = getApplicationByMemberId(memberId);
            if (existing != null && "draft".equals(existing.getStatus())) {
                // Update existing draft
                application.setId(existing.getId());
                application.setApplicationId(existing.getApplicationId());
                int result = applicationService.updateVendorApplication(application);
                if (result > 0) {
                    redirectAttributes.addFlashAttribute("success", "Application updated successfully");
                } else {
                    redirectAttributes.addFlashAttribute("error", "Failed to update application");
                }
            } else {
                // Insert new application
                int result = applicationService.insertVendorApplication(application);
                if (result > 0) {
                    redirectAttributes.addFlashAttribute("success", "Application submitted successfully");
                } else {
                    redirectAttributes.addFlashAttribute("error", "Failed to submit application");
                }
            }
            
            return "redirect:/mall/static/vendor/status";
            
        } catch (Exception e) {
            log.error("Error submitting vendor application", e);
            redirectAttributes.addFlashAttribute("error", "Failed to submit application: " + e.getMessage());
            return "redirect:/mall/static/vendor/application";
        }
    }
    
    /**
     * Vendor Products Page (NoScript)
     * Displays product list with pagination
     * Note: This method is called by StaticPageController after noscript detection,
     * so it doesn't have a direct @GetMapping mapping to avoid route conflicts
     */
    public String vendorProducts(
            HttpServletRequest request,
            Model model,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        
        Long memberId = requireAuth(request);
        if (memberId == null) {
            return "redirect:/mall/static/vendor/login";
        }
        
        try {
            // Get vendor ID from member ID
            Long vendorId = vendorProductService.getVendorIdByMemberId(memberId);
            if (vendorId == null) {
                model.addAttribute("error", "No approved vendor account found. Please complete your application first.");
                return "vendor/noscript/error";
            }
            
            // Load all products (no pagination in service yet, so we load all and paginate in memory)
            List<com.medusa.mall.domain.vo.VendorProductVO> allProducts = vendorProductService.selectVendorProductVOList(vendorId);
            
            // Manual pagination
            int total = allProducts != null ? allProducts.size() : 0;
            int totalPages = (total + size - 1) / size;
            int startIndex = (page - 1) * size;
            int endIndex = Math.min(startIndex + size, total);
            
            List<com.medusa.mall.domain.vo.VendorProductVO> products = new java.util.ArrayList<>();
            if (allProducts != null && startIndex < total) {
                products = allProducts.subList(startIndex, endIndex);
            }
            
            model.addAttribute("products", products);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", totalPages);
            model.addAttribute("total", total);
            model.addAttribute("hasNext", page < totalPages);
            model.addAttribute("hasPrev", page > 1);
            model.addAttribute("size", size);
            
            model.addAttribute("noscript", true);
            model.addAttribute("vendorId", vendorId);
            
            // Add user info to model
            addUserInfoToModel(request, model);
            
            return "vendor/noscript/products";
            
        } catch (Exception e) {
            log.error("Error loading vendor products page", e);
            model.addAttribute("error", "Failed to load products: " + e.getMessage());
            return "vendor/noscript/error";
        }
    }
    
    /**
     * Vendor Orders Page (NoScript)
     * Displays order list with filtering and pagination
     * Note: This method is called by StaticPageController after noscript detection,
     * so it doesn't have a direct @GetMapping mapping to avoid route conflicts
     */
    public String vendorOrders(
            HttpServletRequest request,
            Model model,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        
        Long memberId = requireAuth(request);
        if (memberId == null) {
            return "redirect:/mall/static/vendor/login";
        }
        
        try {
            // Get vendor ID
            Long vendorId = vendorProductService.getVendorIdByMemberId(memberId);
            if (vendorId == null) {
                model.addAttribute("error", "No approved vendor account found.");
                return "vendor/noscript/error";
            }
            
            // Load all orders (no pagination in service yet, so we load all and filter/paginate in memory)
            List<Order> allOrders = vendorOrderService.selectVendorOrderList(vendorId);
            
            // Filter by status if provided
            if (status != null && !status.isEmpty() && allOrders != null) {
                final String statusFilter = status;
                allOrders = allOrders.stream()
                    .filter(order -> order.getStatus() != null && statusFilter.equals(order.getStatus().toString()))
                    .collect(java.util.stream.Collectors.toList());
            }
            
            // Manual pagination
            int total = allOrders != null ? allOrders.size() : 0;
            int totalPages = (total + size - 1) / size;
            int startIndex = (page - 1) * size;
            int endIndex = Math.min(startIndex + size, total);
            
            List<Order> orders = new java.util.ArrayList<>();
            if (allOrders != null && startIndex < total) {
                orders = allOrders.subList(startIndex, endIndex);
            }
            
            model.addAttribute("orders", orders);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", totalPages);
            model.addAttribute("total", total);
            model.addAttribute("hasNext", page < totalPages);
            model.addAttribute("hasPrev", page > 1);
            model.addAttribute("size", size);
            
            model.addAttribute("noscript", true);
            model.addAttribute("vendorId", vendorId);
            model.addAttribute("statusFilter", status);
            
            // Add user info to model
            addUserInfoToModel(request, model);
            
            return "vendor/noscript/orders";
            
        } catch (Exception e) {
            log.error("Error loading vendor orders page", e);
            model.addAttribute("error", "Failed to load orders: " + e.getMessage());
            return "vendor/noscript/error";
        }
    }
    
    /**
     * Vendor Withdrawal Page (NoScript)
     * Note: This method is called by StaticPageController after noscript detection,
     * so it doesn't have a direct @GetMapping mapping to avoid route conflicts
     */
    public String vendorWithdrawal(HttpServletRequest request, Model model) {
        Long memberId = requireAuth(request);
        if (memberId == null) {
            return "redirect:/mall/static/vendor/login";
        }
        
        try {
            // Get vendor ID
            Long vendorId = vendorProductService.getVendorIdByMemberId(memberId);
            if (vendorId == null) {
                model.addAttribute("error", "No approved vendor account found.");
                return "vendor/noscript/error";
            }
            
            // Load withdrawal addresses
            List<VendorWithdrawalAddress> addresses = withdrawalService.getWithdrawalAddresses(vendorId);
            model.addAttribute("withdrawalAddresses", addresses);
            
            // Load withdrawal history
            List<VendorWithdrawalRequest> requests = withdrawalService.getWithdrawalRequests(vendorId);
            model.addAttribute("withdrawalRequests", requests);
            
            // Load balance
            java.util.Map<String, java.math.BigDecimal> balance = withdrawalService.getVendorBalance(vendorId);
            model.addAttribute("balance", balance);
            
            model.addAttribute("noscript", true);
            model.addAttribute("vendorId", vendorId);
            
            // Add user info to model
            addUserInfoToModel(request, model);
            
            return "vendor/noscript/withdrawal";
            
        } catch (Exception e) {
            log.error("Error loading vendor withdrawal page", e);
            model.addAttribute("error", "Failed to load withdrawal page: " + e.getMessage());
            return "vendor/noscript/error";
        }
    }
    
    /**
     * Submit Withdrawal Request (POST - NoScript)
     */
    @PostMapping("/withdrawal/submit")
    public String submitWithdrawal(
            @RequestParam String currency,
            @RequestParam java.math.BigDecimal amount,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {
        
        Long memberId = requireAuth(request);
        if (memberId == null) {
            redirectAttributes.addFlashAttribute("error", "Please login first");
            return "redirect:/mall/static/vendor/login";
        }
        
        try {
            // Get vendor ID
            Long vendorId = vendorProductService.getVendorIdByMemberId(memberId);
            if (vendorId == null) {
                redirectAttributes.addFlashAttribute("error", "Vendor account not approved yet");
                return "redirect:/mall/static/vendor/withdrawal";
            }
            
            // Create withdrawal request
            withdrawalService.createWithdrawalRequest(vendorId, currency, amount);
            redirectAttributes.addFlashAttribute("success", "Withdrawal request submitted successfully");
            
            return "redirect:/mall/static/vendor/withdrawal";
            
        } catch (Exception e) {
            log.error("Error submitting withdrawal request", e);
            redirectAttributes.addFlashAttribute("error", "Failed to submit withdrawal request: " + e.getMessage());
            return "redirect:/mall/static/vendor/withdrawal";
        }
    }
    
    /**
     * Vendor Login Page (NoScript)
     * Displays login form
     * Note: This method is called by StaticPageController after noscript detection,
     * so it doesn't have a direct @GetMapping mapping to avoid route conflicts
     */
    public String vendorLogin(HttpServletRequest request, Model model) {
        // If already logged in, redirect to status page
        Long memberId = VendorAuthUtils.getCurrentVendorMemberId();
        if (memberId != null) {
            return "redirect:/mall/static/vendor/status";
        }
        
        // Check for error/message from redirect
        String error = (String) request.getAttribute("error");
        String message = (String) request.getAttribute("message");
        if (error != null) {
            model.addAttribute("error", error);
        }
        if (message != null) {
            model.addAttribute("message", message);
        }
        
        model.addAttribute("noscript", true);
        return "vendor/noscript/login";
    }
    
    /**
     * Vendor Login Submit (POST - NoScript)
     * Processes login form submission
     */
    @PostMapping("/login")
    public String vendorLoginSubmit(
            @RequestParam String username,
            @RequestParam String password,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {
        
        try {
            // Validate input
            if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password)) {
                redirectAttributes.addFlashAttribute("error", "Username and password are required");
                redirectAttributes.addFlashAttribute("username", username);
                return "redirect:/mall/static/vendor/login";
            }
            
            // Find user by username
            VendorMember member = vendorMemberService.selectByUsername(username);
            if (member == null) {
                redirectAttributes.addFlashAttribute("error", "Invalid username or password");
                redirectAttributes.addFlashAttribute("username", username);
                return "redirect:/mall/static/vendor/login";
            }
            
            // Check account status
            if (member.getStatus() != null && member.getStatus() == 0) {
                redirectAttributes.addFlashAttribute("error", "Account is disabled");
                redirectAttributes.addFlashAttribute("username", username);
                return "redirect:/mall/static/vendor/login";
            }
            
            // Verify password
            boolean passwordMatch = SecurityUtils.matchesPassword(password, member.getPasswordHash());
            if (!passwordMatch) {
                redirectAttributes.addFlashAttribute("error", "Invalid username or password");
                redirectAttributes.addFlashAttribute("username", username);
                return "redirect:/mall/static/vendor/login";
            }
            
            // Create session
            HttpSession session = request.getSession(true);
            session.setAttribute(VendorAuthUtils.SESSION_VENDOR_MEMBER_ID, member.getId());
            session.setAttribute(VendorAuthUtils.SESSION_VENDOR_MEMBER_USERNAME, member.getUsername());
            String token = IdUtils.fastSimpleUUID();
            session.setAttribute(VendorAuthUtils.SESSION_VENDOR_MEMBER_TOKEN, token);
            
            log.info("Vendor login successful: {}", username);
            
            // Check if user has any application records
            boolean hasApplication = false;
            try {
                VendorApplication query = new VendorApplication();
                query.setMemberId(member.getId());
                List<VendorApplication> applications = applicationService.selectVendorApplicationList(query);
                hasApplication = (applications != null && !applications.isEmpty());
            } catch (Exception e) {
                log.warn("Failed to check application records for user {}: {}", username, e.getMessage());
            }
            
            // Redirect based on application status
            if (!hasApplication) {
                // New user: redirect to application page
                return "redirect:/mall/static/vendor/application";
            } else {
                // Existing user: redirect to status page
                return "redirect:/mall/static/vendor/status";
            }
            
        } catch (Exception e) {
            log.error("Error processing vendor login", e);
            redirectAttributes.addFlashAttribute("error", "Login failed: " + e.getMessage());
            redirectAttributes.addFlashAttribute("username", username);
            return "redirect:/mall/static/vendor/login";
        }
    }
    
    /**
     * Vendor Register Page (NoScript)
     * Displays registration form
     * Note: This method is called by StaticPageController after noscript detection,
     * so it doesn't have a direct @GetMapping mapping to avoid route conflicts
     */
    public String vendorRegister(HttpServletRequest request, Model model) {
        // If already logged in, redirect to status page
        Long memberId = VendorAuthUtils.getCurrentVendorMemberId();
        if (memberId != null) {
            return "redirect:/mall/static/vendor/status";
        }
        
        // Flash attributes are automatically added to model in Spring MVC
        // No need to manually get them from request
        
        model.addAttribute("noscript", true);
        return "vendor/noscript/register";
    }
    
    /**
     * Vendor Register Submit (POST - NoScript)
     * Processes registration form submission
     */
    @PostMapping("/register")
    public String vendorRegisterSubmit(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String passwordConfirm,
            @RequestParam(required = false) String email,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {
        
        try {
            // Validate input
            if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password)) {
                redirectAttributes.addFlashAttribute("error", "Username and password are required");
                redirectAttributes.addFlashAttribute("username", username);
                redirectAttributes.addFlashAttribute("email", email);
                return "redirect:/mall/static/vendor/register";
            }
            
            // Check password confirmation
            if (!password.equals(passwordConfirm)) {
                redirectAttributes.addFlashAttribute("error", "Passwords do not match");
                redirectAttributes.addFlashAttribute("username", username);
                redirectAttributes.addFlashAttribute("email", email);
                return "redirect:/mall/static/vendor/register";
            }
            
            // Check if username already exists
            VendorMember existing = vendorMemberService.selectByUsername(username);
            if (existing != null) {
                redirectAttributes.addFlashAttribute("error", "Username already exists");
                redirectAttributes.addFlashAttribute("username", username);
                redirectAttributes.addFlashAttribute("email", email);
                return "redirect:/mall/static/vendor/register";
            }
            
            // Create new member
            VendorMember member = new VendorMember();
            member.setUsername(username);
            member.setPasswordHash(password); // Service will encrypt it
            if (StringUtils.isNotEmpty(email)) {
                member.setEmail(email);
            }
            
            // Register user (service will encrypt password and set status)
            int rows = vendorMemberService.register(member);
            if (rows > 0) {
                log.info("Vendor registration successful: {}", username);
                redirectAttributes.addFlashAttribute("message", "Registration successful. Please login.");
                return "redirect:/mall/static/vendor/login";
            } else {
                redirectAttributes.addFlashAttribute("error", "Failed to register. Please try again.");
                redirectAttributes.addFlashAttribute("username", username);
                redirectAttributes.addFlashAttribute("email", email);
                return "redirect:/mall/static/vendor/register";
            }
            
        } catch (Exception e) {
            log.error("Error processing vendor registration", e);
            redirectAttributes.addFlashAttribute("error", "Registration failed: " + e.getMessage());
            redirectAttributes.addFlashAttribute("username", username);
            redirectAttributes.addFlashAttribute("email", email);
            return "redirect:/mall/static/vendor/register";
        }
    }
    
    /**
     * Vendor Bond Level Page (NoScript)
     * Displays bond level configurations
     * Note: This method is called by StaticPageController after noscript detection,
     * so it doesn't have a direct @GetMapping mapping to avoid route conflicts
     */
    public String vendorBondLevel(HttpServletRequest request, Model model) {
        Long memberId = requireAuth(request);
        if (memberId == null) {
            return "redirect:/mall/static/vendor/login";
        }
        
        try {
            // Get current bond level if application is approved
            VendorApplication application = getApplicationByMemberId(memberId);
            Integer currentBondLevel = null;
            if (application != null && "approved".equals(application.getStatus())) {
                currentBondLevel = application.getBondLevel() != null ? application.getBondLevel() : 1;
            }
            model.addAttribute("currentBondLevel", currentBondLevel);
            
            // Load all bond level configurations
            List<VendorBondConfig> bondLevels = bondConfigService.selectVendorBondConfigList(new VendorBondConfig());
            model.addAttribute("bondLevels", bondLevels);
            
            model.addAttribute("noscript", true);
            
            // Add user info to model
            addUserInfoToModel(request, model);
            
            return "vendor/noscript/bond-level";
            
        } catch (Exception e) {
            log.error("Error loading vendor bond level page", e);
            model.addAttribute("error", "Failed to load bond level page: " + e.getMessage());
            return "vendor/noscript/error";
        }
    }
    
    /**
     * Create Bond Order (POST - NoScript)
     * Handles form submission for bond order creation
     * Redirects to bond payment page where the order will be created
     */
    @PostMapping("/bond/create-order")
    public String createBondOrder(
            @RequestParam(required = false) String applicationId,
            @RequestParam(required = false) String applicationNumber,
            @RequestParam(required = false) Integer level,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {
        
        Long memberId = requireAuth(request);
        if (memberId == null) {
            redirectAttributes.addFlashAttribute("error", "Please login first");
            return "redirect:/mall/static/vendor/login";
        }
        
        try {
            // Get application to determine bond level if not provided
            VendorApplication application = getApplicationByMemberId(memberId);
            if (application == null || !"approved".equals(application.getStatus())) {
                redirectAttributes.addFlashAttribute("error", "No approved application found");
                return "redirect:/mall/static/vendor/status";
            }
            
            if (application.getBondOrderId() != null) {
                redirectAttributes.addFlashAttribute("error", "Bond already paid for this application");
                return "redirect:/mall/static/vendor/status";
            }
            
            // Use bond level from application if not provided
            Integer bondLevel = level != null ? level : (application.getBondLevel() != null ? application.getBondLevel() : 1);
            
            // Redirect to bond payment page with parameters
            // The payment page will handle order creation
            return "redirect:/mall/static/vendor/bond/payment?level=" + bondLevel +
                   "&applicationId=" + application.getId() +
                   "&applicationNumber=" + (applicationNumber != null ? applicationNumber : application.getApplicationId());
            
        } catch (Exception e) {
            log.error("Error creating bond order", e);
            redirectAttributes.addFlashAttribute("error", "Failed to create bond order: " + e.getMessage());
            return "redirect:/mall/static/vendor/status";
        }
    }
    
    /**
     * Vendor logout (POST - NoScript)
     * Handles logout form submission
     */
    @PostMapping("/logout")
    public String vendorLogout(HttpServletRequest request, RedirectAttributes redirectAttributes) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
            log.info("Vendor member logged out, session invalidated.");
        }
        redirectAttributes.addFlashAttribute("message", "You have been logged out successfully.");
        return "redirect:/mall/static/vendor/login";
    }
    
}


package com.medusa.mall.controller.api;

import com.medusa.common.core.controller.BaseController;
import com.medusa.common.core.domain.AjaxResult;
import com.medusa.common.core.redis.RedisCache;
import com.medusa.common.utils.StringUtils;
import com.medusa.common.utils.uuid.IdUtils;
import com.medusa.mall.domain.vendor.VendorMember;
import com.medusa.mall.domain.vendor.VendorApplication;
import com.medusa.mall.service.IVendorMemberService;
import com.medusa.mall.service.IVendorApplicationService;
import com.medusa.mall.util.VendorAuthUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

/**
 * Vendor member authentication API (register/login/logout).
 */
@RestController
@RequestMapping("/api/mall/vendor/member")
public class ApiVendorMemberController extends BaseController {

    @Autowired
    private IVendorMemberService vendorMemberService;
    
    @Autowired
    private IVendorApplicationService vendorApplicationService;
    
    @Autowired
    private RedisCache redisCache;
    
    /**
     * 密码最大错误次数
     */
    @Value(value = "${user.password.maxRetryCount:5}")
    private int maxRetryCount;
    
    /**
     * 密码锁定时间（分钟）
     */
    @Value(value = "${user.password.lockTime:10}")
    private int lockTime;
    
    /**
     * 获取登录密码错误次数缓存键名
     * 
     * @param username 用户名
     * @return 缓存键key
     */
    private String getLoginRetryCacheKey(String username) {
        return "vendor_login_retry:" + username;
    }

    /**
     * Get CSRF token for frontend
     * This endpoint allows frontend to retrieve CSRF token for subsequent requests
     */
    @GetMapping("/csrf-token")
    public AjaxResult getCsrfToken(HttpServletRequest request) {
        // Spring Security automatically provides CsrfToken in request attributes
        CsrfToken csrfToken = (CsrfToken) request.getAttribute("_csrf");
        if (csrfToken != null) {
            Map<String, String> tokenData = new HashMap<>();
            tokenData.put("token", csrfToken.getToken());
            tokenData.put("headerName", csrfToken.getHeaderName());
            tokenData.put("parameterName", csrfToken.getParameterName());
            return AjaxResult.success(tokenData);
        }
        return AjaxResult.error("CSRF token not available");
    }

    /**
     * Test endpoint for CSRF protection testing
     * This endpoint does NOT require authentication, only CSRF token
     * Used for testing CSRF protection functionality
     */
    @PostMapping("/test-csrf")
    public AjaxResult testCsrf(HttpServletRequest request) {
        // This endpoint is only for CSRF testing
        // It doesn't require authentication, but requires valid CSRF token
        logger.info("CSRF test endpoint called - CSRF protection is working!");
        return AjaxResult.success("CSRF protection test passed");
    }

    @PostMapping("/register")
    public AjaxResult register(@RequestBody VendorMember member, HttpServletRequest request) {
        logger.info("=== Vendor Registration Attempt ===");
        logger.info("Request URI: {}", request.getRequestURI());
        logger.info("Remote Address: {}", request.getRemoteAddr());
        logger.info("Request Origin: {}", request.getHeader("Origin"));
        
        try {
            if (member == null || StringUtils.isEmpty(member.getUsername()) || StringUtils.isEmpty(member.getPasswordHash())) {
                logger.warn("Registration failed: Username or password is empty");
                return AjaxResult.error("Username and password are required");
            }
            
            String username = member.getUsername();
            logger.info("Attempting to register user: {}", username);
            
            // Check if username already exists
            VendorMember existing = vendorMemberService.selectByUsername(username);
            if (existing != null) {
                logger.warn("Registration failed: Username '{}' already exists", username);
                return AjaxResult.error("Username already exists");
            }
            
            // Register user
            int rows = vendorMemberService.register(member);
            if (rows > 0) {
                logger.info("✅ Vendor registration successful: {}", username);
                return AjaxResult.success("Registered successfully");
            } else {
                logger.error("❌ Registration failed: Database insert returned 0 rows for user: {}", username);
                return AjaxResult.error("Failed to register. Please try again.");
            }
        } catch (Exception e) {
            logger.error("❌ Vendor registration exception for user {}: {}", 
                        member != null ? member.getUsername() : "unknown", e.getMessage(), e);
            return AjaxResult.error("Registration failed: " + e.getMessage());
        }
    }

    @PostMapping("/login")
    public AjaxResult login(@RequestParam String username,
                            @RequestParam String password,
                            HttpServletRequest request) {
        logger.info("=== Vendor Login Attempt ===");
        logger.info("Username: {}", username);
        logger.info("Request URI: {}", request.getRequestURI());
        logger.info("Remote Address: {}", request.getRemoteAddr());
        
        // Check if username is provided
        if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password)) {
            logger.warn("Login failed: Username or password is empty");
            return AjaxResult.error("Username and password are required");
        }
        
        // Check login retry count (rate limiting)
        String retryCacheKey = getLoginRetryCacheKey(username);
        Integer retryCount = redisCache.getCacheObject(retryCacheKey);
        
        if (retryCount == null) {
            retryCount = 0;
        }
        
        // Check if account is locked due to too many failed attempts
        if (retryCount >= maxRetryCount) {
            Long remainingTime = redisCache.getExpire(retryCacheKey);
            long remainingMinutes = remainingTime > 0 ? (remainingTime / 60) : lockTime;
            logger.warn("Login blocked: Account '{}' is locked after {} failed attempts. Remaining lock time: {} minutes", 
                       username, retryCount, remainingMinutes);
            return AjaxResult.error(String.format("Account locked due to too many failed login attempts. Please try again in %d minute(s).", remainingMinutes));
        }
        
        // Find user by username
        VendorMember existing = vendorMemberService.selectByUsername(username);
        if (existing == null) {
            // Increment retry count even if user doesn't exist (to prevent username enumeration)
            retryCount = retryCount + 1;
            redisCache.setCacheObject(retryCacheKey, retryCount, lockTime, TimeUnit.MINUTES);
            logger.warn("Login failed: User '{}' not found. Retry count: {}", username, retryCount);
            return AjaxResult.error("Invalid username or password");
        }
        
        logger.info("Found vendor member: ID={}, Username={}, Status={}", 
                    existing.getId(), existing.getUsername(), existing.getStatus());
        
        // Check account status
        if (existing.getStatus() != null && existing.getStatus() == 0) {
            logger.warn("Login failed: Account '{}' is disabled", username);
            return AjaxResult.error("Account is disabled");
        }
        
        // Verify password
        logger.debug("Verifying password for user '{}'", username);
        boolean passwordMatch = com.medusa.common.utils.SecurityUtils.matchesPassword(password, existing.getPasswordHash());
        if (!passwordMatch) {
            // Increment retry count on password mismatch
            retryCount = retryCount + 1;
            redisCache.setCacheObject(retryCacheKey, retryCount, lockTime, TimeUnit.MINUTES);
            logger.warn("Login failed: Invalid password for user '{}'. Retry count: {}/{}", 
                       username, retryCount, maxRetryCount);
            
            // Check if account should be locked now
            if (retryCount >= maxRetryCount) {
                logger.warn("Account '{}' locked after {} failed attempts", username, retryCount);
                return AjaxResult.error(String.format("Account locked after %d failed login attempts. Please try again in %d minute(s).", 
                                                      maxRetryCount, lockTime));
            }
            
            int remainingAttempts = maxRetryCount - retryCount;
            return AjaxResult.error(String.format("Invalid username or password. %d attempt(s) remaining.", remainingAttempts));
        }
        
        // Password verification successful - clear retry count
        clearLoginRetryCache(username);
        logger.info("Password verification successful for user '{}'", username);

        // Fix session fixation vulnerability - invalidate old session and create new one
        HttpSession oldSession = request.getSession(false);
        if (oldSession != null) {
            logger.info("Invalidating old session: ID={}", oldSession.getId());
            oldSession.invalidate();
        }
        
        // Create new session (prevents session fixation attack)
        HttpSession session = request.getSession(true);
        logger.info("New session created: ID={}", session.getId());
        
        session.setAttribute(VendorAuthUtils.SESSION_VENDOR_MEMBER_ID, existing.getId());
        session.setAttribute(VendorAuthUtils.SESSION_VENDOR_MEMBER_USERNAME, existing.getUsername());
        String token = IdUtils.fastSimpleUUID();
        session.setAttribute(VendorAuthUtils.SESSION_VENDOR_MEMBER_TOKEN, token);
        
        logger.info("Session attributes set:");
        logger.info("  - vendorMemberId: {}", existing.getId());
        logger.info("  - vendorMemberUsername: {}", existing.getUsername());
        logger.info("  - vendorMemberToken: {}", token);
        logger.info("✅ Vendor login successful: {}", username);

        // Check if user has any application records
        boolean hasApplication = false;
        try {
            VendorApplication query = new VendorApplication();
            query.setMemberId(existing.getId());
            List<VendorApplication> applications = vendorApplicationService.selectVendorApplicationList(query);
            hasApplication = (applications != null && !applications.isEmpty());
            logger.info("User {} has {} application(s)", username, hasApplication ? applications.size() : 0);
        } catch (Exception e) {
            logger.warn("Failed to check application records for user {}: {}", username, e.getMessage());
            // Continue with hasApplication = false
        }

        return AjaxResult.success()
                .put("token", token)
                .put("username", existing.getUsername())
                .put("memberId", existing.getId())
                .put("hasApplication", hasApplication);
    }

    @PostMapping("/logout")
    public AjaxResult logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.removeAttribute(VendorAuthUtils.SESSION_VENDOR_MEMBER_ID);
            session.removeAttribute(VendorAuthUtils.SESSION_VENDOR_MEMBER_USERNAME);
            session.removeAttribute(VendorAuthUtils.SESSION_VENDOR_MEMBER_TOKEN);
        }
        return AjaxResult.success("Logged out");
    }

    @GetMapping("/profile")
    public AjaxResult profile(HttpServletRequest request) {
        logger.info("=== Vendor Profile Request ===");
        logger.info("Request URI: {}", request.getRequestURI());
        
        // Check session
        HttpSession session = request.getSession(false);
        if (session == null) {
            logger.warn("Profile failed: No session found");
            return AjaxResult.error("Not logged in - no session");
        }
        
        logger.info("Session found: ID={}", session.getId());
        logger.info("Session attributes:");
        logger.info("  - vendorMemberId: {}", session.getAttribute(VendorAuthUtils.SESSION_VENDOR_MEMBER_ID));
        logger.info("  - vendorMemberUsername: {}", session.getAttribute(VendorAuthUtils.SESSION_VENDOR_MEMBER_USERNAME));
        logger.info("  - vendorMemberToken: {}", session.getAttribute(VendorAuthUtils.SESSION_VENDOR_MEMBER_TOKEN));
        
        // Get member ID from session
        Long memberId = VendorAuthUtils.getCurrentVendorMemberId();
        if (memberId == null) {
            logger.warn("Profile failed: getCurrentVendorMemberId returned null");
            logger.warn("Authorization header: {}", request.getHeader("Authorization"));
            return AjaxResult.error("Not logged in - invalid session");
        }
        
        logger.info("Current vendor member ID: {}", memberId);
        
        // Get member details
        VendorMember member = vendorMemberService.selectById(memberId);
        if (member == null) {
            logger.warn("Profile failed: Member {} not found in database", memberId);
            return AjaxResult.error("Account not found");
        }
        
        logger.info("✅ Profile retrieved successfully for: {}", member.getUsername());
        
        // Create user data object
        java.util.Map<String, Object> userData = new java.util.HashMap<>();
        userData.put("username", member.getUsername());
        userData.put("email", member.getEmail());
        userData.put("phone", member.getPhone());
        userData.put("memberId", member.getId());
        userData.put("token", session.getAttribute(VendorAuthUtils.SESSION_VENDOR_MEMBER_TOKEN));
        
        // Return in standard format with 'data' wrapper
        return AjaxResult.success(userData);
    }

    public static Long currentVendorMemberId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        
        // Get member ID from session
        Long memberId = (Long) session.getAttribute(VendorAuthUtils.SESSION_VENDOR_MEMBER_ID);
        if (memberId == null) {
            return null;
        }
        
        // If Authorization header is present, validate the token
        String header = request.getHeader("Authorization");
        if (StringUtils.isNotEmpty(header) && header.startsWith("Bearer ")) {
            String token = header.substring("Bearer ".length());
            String sessionToken = (String) session.getAttribute(VendorAuthUtils.SESSION_VENDOR_MEMBER_TOKEN);
            
            // Token must match if provided
            if (StringUtils.isEmpty(sessionToken) || !sessionToken.equals(token)) {
                return null;
            }
        }
        
        // If no Authorization header, session is enough (for browser-based access)
        return memberId;
    }
    
    /**
     * 清除登录重试记录缓存
     * 
     * @param username 用户名
     */
    private void clearLoginRetryCache(String username) {
        String cacheKey = getLoginRetryCacheKey(username);
        if (redisCache.hasKey(cacheKey)) {
            redisCache.deleteObject(cacheKey);
            logger.debug("Cleared login retry cache for user: {}", username);
        }
    }
}


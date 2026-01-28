package com.medusa.mall.util;

import com.medusa.common.utils.ServletUtils;
import com.medusa.common.utils.StringUtils;
import com.medusa.common.utils.spring.SpringUtils;
import com.medusa.mall.domain.vendor.Vendor;
import com.medusa.mall.domain.vendor.VendorApplication;
import com.medusa.mall.mapper.VendorApplicationMapper;
import com.medusa.mall.mapper.VendorMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper utilities for vendor portal authentication based on HttpSession.
 */
public final class VendorAuthUtils {

    private static final Logger log = LoggerFactory.getLogger(VendorAuthUtils.class);
    
    private static final String TOKEN_PREFIX = "Bearer ";
    public static final String SESSION_VENDOR_MEMBER_ID = "vendorMemberId";
    public static final String SESSION_VENDOR_MEMBER_USERNAME = "vendorMemberUsername";
    public static final String SESSION_VENDOR_MEMBER_TOKEN = "vendorMemberToken";
    public static final String SESSION_VENDOR_ID = "vendorId"; // Cached vendor ID

    private VendorAuthUtils() {
    }

    public static Long getCurrentVendorMemberId() {
        log.debug("=== getCurrentVendorMemberId called ===");
        
        HttpServletRequest request = ServletUtils.getRequest();
        if (request == null) {
            log.warn("No HttpServletRequest found");
            return null;
        }
        
        log.debug("Request URI: {}", request.getRequestURI());
        
        HttpSession session = request.getSession(false);
        if (session == null) {
            log.warn("No HTTP session found");
            return null;
        }
        
        log.debug("Session ID: {}", session.getId());
        
        // Get member ID from session
        Long memberId = (Long) session.getAttribute(SESSION_VENDOR_MEMBER_ID);
        log.debug("Session vendorMemberId: {}", memberId);
        
        if (memberId == null) {
            log.warn("No vendorMemberId in session");
            return null;
        }
        
        // If Authorization header is present, validate the token
        String header = request.getHeader("Authorization");
        log.debug("Authorization header: {}", header != null ? "present" : "absent");
        
        if (StringUtils.isNotEmpty(header) && header.startsWith(TOKEN_PREFIX)) {
            String token = header.substring(TOKEN_PREFIX.length());
            String sessionToken = (String) session.getAttribute(SESSION_VENDOR_MEMBER_TOKEN);
            
            log.debug("Validating token from Authorization header");
            log.debug("Header token: {}", token);
            log.debug("Session token: {}", sessionToken);
            
            // Token must match if provided
            if (StringUtils.isEmpty(sessionToken) || !sessionToken.equals(token)) {
                log.warn("Token mismatch or missing session token");
                return null;
            }
            
            log.debug("✅ Token validated successfully");
        } else {
            log.debug("No Authorization header, using session-only authentication");
        }
        
        // If no Authorization header, session is enough (for browser-based access)
        log.debug("✅ Returning vendorMemberId: {}", memberId);
        return memberId;
    }

    public static boolean isLoggedIn() {
        return getCurrentVendorMemberId() != null;
    }

    /**
     * Get the current vendor's ID (from mall_vendor table)
     * Returns null if vendor member is not logged in or not approved yet
     */
    public static Long getCurrentVendorId() {
        Long memberId = getCurrentVendorMemberId();
        if (memberId == null) {
            return null;
        }
        
        // Check if cached in session
        HttpSession session = ServletUtils.getRequest().getSession(false);
        if (session != null) {
            Long cachedVendorId = (Long) session.getAttribute(SESSION_VENDOR_ID);
            if (cachedVendorId != null) {
                return cachedVendorId;
            }
        }
        
        // Query from database
        try {
            VendorApplicationMapper applicationMapper = SpringUtils.getBean(VendorApplicationMapper.class);
            VendorMapper vendorMapper = SpringUtils.getBean(VendorMapper.class);
            
            // Find approved application for this member
            VendorApplication application = applicationMapper.selectApprovedByMemberId(memberId);
            if (application == null) {
                return null; // No approved application yet
            }
            
            // Find vendor by application ID
            Vendor vendor = vendorMapper.selectByApplicationId(application.getId());
            if (vendor == null || vendor.getStatus() == null || vendor.getStatus() != 1) {
                return null; // Vendor not found or not active
            }
            
            // Cache in session
            if (session != null) {
                session.setAttribute(SESSION_VENDOR_ID, vendor.getId());
            }
            
            return vendor.getId();
        } catch (Exception e) {
            return null;
        }
    }
}


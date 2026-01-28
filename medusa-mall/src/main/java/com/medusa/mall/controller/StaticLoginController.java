package com.medusa.mall.controller;

import com.medusa.common.core.domain.AjaxResult;
import com.medusa.common.core.domain.entity.Member;
import com.medusa.common.exception.ServiceException;
import com.medusa.framework.web.service.MemberTokenService;
import com.medusa.mall.domain.cart.CartItem;
import com.medusa.mall.service.ICartService;
import com.medusa.mall.service.IMemberService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Cookie;

/**
 * Static login controller for form-based authentication
 * Handles login form submissions from static pages
 */
@Controller
@RequestMapping("/mall")
public class StaticLoginController {

    private static final Logger log = LoggerFactory.getLogger(StaticLoginController.class);

    @Autowired
    private IMemberService memberService;

    @Autowired
    private MemberTokenService memberTokenService;

    @Autowired
    private ICartService cartService;

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
     * Handle login form submission
     */
    @PostMapping("/login")
    public String handleLogin(@RequestParam String username,
                            @RequestParam String password,
                            @RequestParam(value = "code", required = false) String code,
                            @RequestParam(value = "uuid", required = false) String uuid,
                            @RequestParam(value = "rememberMe", required = false) String rememberMe,
                            HttpServletRequest request,
                            HttpServletResponse response,
                            RedirectAttributes redirectAttributes) {
        
        try {
            // Create member object for authentication
            Member member = new Member();
            member.setUsername(username);
            member.setPassword(password);
            member.setSourceType(0); // OS login
            
            // Attempt to authenticate using MemberService
            String token = memberService.osLogin(member);

            
            
            if (token != null && !token.isEmpty()) {
                // Login successful - store token in session for static pages
                HttpSession session = request.getSession();
                member = memberService.selectMemberByUsername(username);
                if(member == null){
                    redirectAttributes.addFlashAttribute("error", "Invalid username or password");
                    return "redirect:/mall/static/login";
                }
                session.setAttribute("token", token);
                session.setAttribute("username", username);
                session.setAttribute("memberToken", token); // Store member token specifically
                log.debug("addToCart memberId: " + member.getMemberId());
                
                session.setAttribute("memberId", member.getMemberId());

                // Redirect to home page
                return "redirect:/mall/static/home";
            } else {
                // Login failed
                redirectAttributes.addFlashAttribute("error", "Invalid username or password");
                return "redirect:/mall/static/login";
            }
            
        } catch (ServiceException e) {
            // Handle suspension and other service exceptions
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/mall/static/login";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Login failed: " + e.getMessage());
            return "redirect:/mall/static/login";
        }
    }

    /**
     * Add item to cart
     */
    @PostMapping("/static/cart/add")
    public String addToCart(@RequestParam Long productId,
                           @RequestParam Integer quantity,
                           HttpServletRequest request,
                           RedirectAttributes redirectAttributes) {
        try {
            HttpSession session = request.getSession(false);
            if (session == null || session.getAttribute("memberToken") == null) {
                // Guest模式
                String guestId = getGuestSessionId(request);
                
                // Create cart item
                CartItem cartItem = new CartItem();
                cartItem.setProductId(productId);
                cartItem.setQuantity(quantity);

                // Add to guest cart
                cartService.addToGuestCart(guestId, cartItem);
                
                redirectAttributes.addFlashAttribute("message", "Product added to cart successfully!");
                return "redirect:/mall/static/products";
            } else {
                // 会员模式
                Long memberId = (Long) session.getAttribute("memberId"); 
                log.debug("addToCart memberId: " + memberId);

                // Create cart item
                CartItem cartItem = new CartItem();
                cartItem.setProductId(productId);
                cartItem.setQuantity(quantity);

                // Add to cart
                cartService.addOrUpdateCartItem(memberId, cartItem);
                
                redirectAttributes.addFlashAttribute("message", "Product added to cart successfully!");
                return "redirect:/mall/static/products";
            }
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to add to cart: " + e.getMessage());
            return "redirect:/mall/static/products";
        }
    }

    /**
     * Handle logout
     */
    @PostMapping("/logout")
    public String handleLogout(HttpServletRequest request, RedirectAttributes redirectAttributes) {
        try {
            // Clear session
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }
            
            redirectAttributes.addFlashAttribute("message", "You have been logged out successfully.");
            return "redirect:/mall/static/login";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Logout failed.");
            return "redirect:/mall/static/login";
        }
    }

    /**
     * Login form data class
     */
    public static class LoginForm {
        private String username;
        private String password;
        private boolean rememberMe;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public boolean isRememberMe() {
            return rememberMe;
        }

        public void setRememberMe(boolean rememberMe) {
            this.rememberMe = rememberMe;
        }
    }
} 
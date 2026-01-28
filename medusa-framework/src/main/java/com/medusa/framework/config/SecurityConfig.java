package com.medusa.framework.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.core.annotation.Order;
import org.springframework.web.filter.CorsFilter;
import com.medusa.framework.config.properties.PermitAllUrlProperties;
import com.medusa.framework.security.filter.JwtAuthenticationTokenFilter;
import com.medusa.framework.security.handle.AuthenticationEntryPointImpl;
import com.medusa.framework.security.handle.LogoutSuccessHandlerImpl;

/**
 * spring security配置
 * 
 * @author ruoyi
 */
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true)
@Configuration
public class SecurityConfig
{
    /**
     * 自定义用户认证逻辑
     */
    @Autowired
    private UserDetailsService userDetailsService;
    
    /**
     * 认证失败处理类
     */
    @Autowired
    private AuthenticationEntryPointImpl unauthorizedHandler;

    /**
     * 退出处理类
     */
    @Autowired
    private LogoutSuccessHandlerImpl logoutSuccessHandler;

    /**
     * api退出类
     * */
//    @Autowired
//    private ApiLogoutSuccessHandlerImpl apiLogoutSuccessHandler;

    /**
     * token认证过滤器
     */
    @Autowired
    private JwtAuthenticationTokenFilter authenticationTokenFilter;
    
    /**
     * 跨域过滤器
     */
    @Autowired
    private CorsFilter corsFilter;

    /**
     * 允许匿名访问的地址
     */
    @Autowired
    private PermitAllUrlProperties permitAllUrl;

    /**
     * 身份验证实现
     */
    @Bean
    public AuthenticationManager authenticationManager()
    {
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
        daoAuthenticationProvider.setUserDetailsService(userDetailsService);
        daoAuthenticationProvider.setPasswordEncoder(bCryptPasswordEncoder());
        return new ProviderManager(daoAuthenticationProvider);
    }

    /**
     * Vendor Portal Security Filter Chain
     * 专门为Co-op Member（Vendor）模块配置的安全过滤链
     * 使用基于HttpSession的认证机制，而不是JWT token
     * 使用 @Order(1) 确保这个 Filter Chain 优先执行
     */
    @Bean
    @Order(1)
    protected SecurityFilterChain vendorSecurityFilterChain(HttpSecurity httpSecurity) throws Exception
    {
        // 配置 CSRF Token Repository - 使用 Cookie 存储 CSRF token
        CookieCsrfTokenRepository csrfTokenRepository = CookieCsrfTokenRepository.withHttpOnlyFalse();
        csrfTokenRepository.setCookieName("XSRF-TOKEN");
        csrfTokenRepository.setHeaderName("X-XSRF-TOKEN");
        csrfTokenRepository.setCookiePath("/");
        
        // 使用新的 CSRF token 请求属性处理器（Spring Security 6+）
        // 注意：Spring Security 6 默认使用 XorCsrfTokenRequestAttributeHandler
        // 我们需要使用 CsrfTokenRequestAttributeHandler 来支持直接传递 token
        CsrfTokenRequestAttributeHandler requestHandler = new CsrfTokenRequestAttributeHandler();
        requestHandler.setCsrfRequestAttributeName("_csrf");
        
        return httpSecurity
            // 只匹配vendor相关路径
            .securityMatcher("/api/mall/vendor/**", "/mall/static/vendor/**")
            // 启用CSRF保护 - 使用Cookie存储CSRF token
            .csrf(csrf -> csrf
                .csrfTokenRepository(csrfTokenRepository)
                .csrfTokenRequestHandler(requestHandler)
                // 排除登录、注册和CSRF token获取端点
                .ignoringRequestMatchers(
                    "/api/mall/vendor/member/login",
                    "/api/mall/vendor/member/register",
                    "/api/mall/vendor/member/csrf-token",  // CSRF token获取端点
                    "/mall/static/vendor/login",
                    "/mall/static/vendor/register"
                )
            )
            // 设置异常处理，确保 CSRF 失败返回 403
            .exceptionHandling(exception -> exception
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    // 检查是否是 CSRF 异常
                    if (accessDeniedException.getMessage() != null && 
                        accessDeniedException.getMessage().contains("CSRF")) {
                        response.setStatus(403);
                        response.setContentType("application/json;charset=UTF-8");
                        response.getWriter().write("{\"code\":403,\"msg\":\"CSRF token validation failed\"}");
                    } else {
                        // 其他访问拒绝异常
                        response.setStatus(403);
                        response.setContentType("application/json;charset=UTF-8");
                        response.getWriter().write("{\"code\":403,\"msg\":\"Access denied\"}");
                    }
                })
            )
            // 允许frame嵌套（如果vendor页面需要）
            .headers((headersCustomizer) -> {
                headersCustomizer.cacheControl(cache -> cache.disable()).frameOptions(options -> options.sameOrigin());
            })
            // Vendor模块使用session认证，配置为IF_REQUIRED
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                .maximumSessions(10)  // 允许同一用户最多10个并发session
                .maxSessionsPreventsLogin(false)  // 不阻止新登录，而是踢掉旧session
                .expiredUrl("/mall/static/vendor/login?session_expired=true")  // session过期后跳转
            )
            // 所有vendor路径允许匿名访问（由VendorAuthUtils进行session验证）
            .authorizeHttpRequests((requests) -> {
                requests.anyRequest().permitAll();
            })
            .build();
    }

    /**
     * anyRequest          |   匹配所有请求路径
     * access              |   SpringEl表达式结果为true时可以访问
     * anonymous           |   匿名可以访问
     * denyAll             |   用户不能访问
     * fullyAuthenticated  |   用户完全认证可以访问（非remember-me下自动登录）
     * hasAnyAuthority     |   如果有参数，参数表示权限，则其中任何一个权限可以访问
     * hasAnyRole          |   如果有参数，参数表示角色，则其中任何一个角色可以访问
     * hasAuthority        |   如果有参数，参数表示权限，则其权限可以访问
     * hasIpAddress        |   如果有参数，参数表示IP地址，如果用户IP和参数匹配，则可以访问
     * hasRole             |   如果有参数，参数表示角色，则其角色可以访问
     * permitAll           |   用户可以任意访问
     * rememberMe          |   允许通过remember-me登录的用户访问
     * authenticated       |   用户登录后可访问
     */
    @Bean
    protected SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception
    {
        return httpSecurity
            // 排除vendor路径（由vendorSecurityFilterChain处理）
            .securityMatcher(request -> 
                !request.getServletPath().startsWith("/api/mall/vendor") && 
                !request.getServletPath().startsWith("/mall/static/vendor")
            )
            // CSRF禁用，因为不使用session
            .csrf(csrf -> csrf.disable())
            // 禁用HTTP响应标头
            .headers((headersCustomizer) -> {
                headersCustomizer.cacheControl(cache -> cache.disable()).frameOptions(options -> options.sameOrigin());
            })
            // 认证失败处理类
            .exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler))
            // 基于token，所以不需要session
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // 注解标记允许匿名访问的url
            .authorizeHttpRequests((requests) -> {
                permitAllUrl.getUrls().forEach(url -> requests.requestMatchers(url).permitAll());
                // 对于登录login 注册register 验证码captchaImage 允许匿名访问
                requests.requestMatchers("/mall/login", "/mall/register").permitAll();
                // 对于登录login 注册register 验证码captchaImage 允许匿名访问
                requests.requestMatchers("/login", "/register", "/captchaImage").permitAll()
                    // 静态资源，可匿名访问
                    .requestMatchers(HttpMethod.GET, "/", "/*.html", "/**.html", "/**.css", "/**.js", "/profile/**", "/images/**", "/static/**", "/vendor/**").permitAll()
                    .requestMatchers("/swagger-ui.html", "/swagger-resources/**", "/webjars/**", "/*/api-docs", "/druid/**").permitAll()
                    // BTC支付相关接口，允许匿名访问
                    .requestMatchers("/api/open/btcpay/**").permitAll()
                    // Static mall pages - allow anonymous access
                    .requestMatchers("/mall/static/**").permitAll()
                    // Product API - allow anonymous access for debugging
                    .requestMatchers("/api/mall/product/**").permitAll()
                    // PGP公钥API - 允许匿名访问
                    .requestMatchers("/api/mall/pgp/**").permitAll()
                    // Mall public order API - 允许订单相关的公开API访问
                    .requestMatchers("/mall/api/order/**").permitAll()
                    // TG首页配置API - 支持临时用户访问
                    .requestMatchers("/api/mall/tg-home-config").permitAll()
                    // Product2 API - 支持临时用户访问
                    .requestMatchers("/api/mall/product2/**").permitAll()
                    // 订单创建API - 支持临时用户创建订单
                    .requestMatchers("/api/mall/order/add").permitAll()
                    // 订单列表API - 支持临时用户查询订单历史
                    .requestMatchers("/api/mall/order/list/**").permitAll()
                    // Coupon验证API - 支持临时用户验证折扣码
                    .requestMatchers("/api/mall/coupon/**").permitAll()
                    // Shipping Method API - 支持临时用户和TG Bot获取配送选项
                    .requestMatchers("/api/mall/shipping/**").permitAll()
                    // Vendor member/login/profile endpoints rely on custom session auth
                    .requestMatchers("/api/mall/vendor/member/**").permitAll()
                    // Vendor product and order APIs perform their own session checks
                    .requestMatchers("/api/mall/vendor/product/**", "/api/mall/vendor/order/**").permitAll()
                    // Vendor withdrawal APIs perform their own session checks
                    .requestMatchers("/api/mall/vendor/withdrawal/**").permitAll()
                    // Vendor Bond & Level APIs perform their own session checks
                    .requestMatchers("/api/mall/vendor/bond/**").permitAll()
                    // Vendor Interview API - perform their own session checks
                    .requestMatchers("/api/mall/vendor/interview/**").permitAll()
                    // 临时付款页面 - 允许匿名访问
                    .requestMatchers("/mall/temp-payment/**").permitAll()
                    // Vendor Application API - 允许匿名提交申请和查询状态
                    .requestMatchers("/api/mall/vendor/application/**").permitAll()
                    // 除上面外的所有请求全部需要鉴权认证
                    .anyRequest().authenticated();
            })
            // 添加Logout filter
            // 统一配置退出接口
            .logout(logout -> logout
                    .logoutRequestMatcher(new OrRequestMatcher(
                            new AntPathRequestMatcher("/logout", "POST"),
                            new AntPathRequestMatcher("/api/mall/logout", "POST")
                    ))
                    .logoutSuccessHandler(logoutSuccessHandler) // 使用统一处理器
            )
            //.logout(logout -> logout.logoutUrl("/logout").logoutSuccessHandler(logoutSuccessHandler))
            // 添加JWT filter
            .addFilterBefore(authenticationTokenFilter, UsernamePasswordAuthenticationFilter.class)
            // 添加CORS filter
            .addFilterBefore(corsFilter, JwtAuthenticationTokenFilter.class)
            .addFilterBefore(corsFilter, LogoutFilter.class)
            .build();
    }

    /**s
     * 强散列哈希加密实现
     */
    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder()
    {
        return new BCryptPasswordEncoder();
    }
}

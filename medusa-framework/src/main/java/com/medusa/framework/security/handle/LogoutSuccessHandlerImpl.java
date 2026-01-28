package com.medusa.framework.security.handle;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import com.alibaba.fastjson2.JSON;
import com.medusa.common.constant.Constants;
import com.medusa.common.core.domain.AjaxResult;
import com.medusa.common.core.domain.model.LoginUser;
import com.medusa.common.utils.MessageUtils;
import com.medusa.common.utils.ServletUtils;
import com.medusa.common.utils.StringUtils;
import com.medusa.framework.manager.AsyncManager;
import com.medusa.framework.manager.factory.AsyncFactory;
import com.medusa.framework.web.service.TokenService;

/**
 * 自定义退出处理类 返回成功
 * 
 * @author ruoyi
 */
@Configuration
public class LogoutSuccessHandlerImpl implements LogoutSuccessHandler
{
    @Autowired
    private TokenService tokenService;

    /**
     * 退出处理
     * 
     * @return
     */
    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException
    {
        // 根据请求路径判断退出类型
        String requestUri = request.getRequestURI();
        if ("/api/mall/logout".equals(requestUri)) {
            ServletUtils.renderString(response, JSON.toJSONString(AjaxResult.success(MessageUtils.message("user.logout.success"))));

        }else{
            LoginUser loginUser = tokenService.getLoginUser(request);
            if (StringUtils.isNotNull(loginUser))
            {
                String userName = loginUser.getUsername();
                // 删除用户缓存记录
                tokenService.delLoginUser(loginUser.getToken());
                // 记录用户退出日志
                AsyncManager.me().execute(AsyncFactory.recordLogininfor(userName, Constants.LOGOUT, MessageUtils.message("user.logout.success")));
            }
            ServletUtils.renderString(response, JSON.toJSONString(AjaxResult.success(MessageUtils.message("user.logout.success"))));

        }
    }
}

package com.medusa.framework.web.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import com.medusa.common.constant.HttpStatus;
import com.medusa.common.core.domain.AjaxResult;
import com.medusa.common.core.text.Convert;
import com.medusa.common.exception.DemoModeException;
import com.medusa.common.exception.ServiceException;
import com.medusa.common.utils.StringUtils;
import com.medusa.common.utils.html.EscapeUtil;

/**
 * 全局异常处理器
 * 
 * @author ruoyi
 */
@RestControllerAdvice
public class GlobalExceptionHandler
{
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @Value("${spring.profiles.active:prod}")
    private String activeProfile;

    /**
     * 清理敏感信息，防止泄露IP地址、内网信息等
     * 
     * @param originalMessage 原始错误信息
     * @return 清理后的安全错误信息
     */
    private String sanitizeErrorMessage(String originalMessage) {
        // 生产环境（os2, os3, prod）：返回通用错误信息，不暴露任何细节
        if ("prod".equals(activeProfile) || "os2".equals(activeProfile) || "os3".equals(activeProfile)) {
            return "An error occurred. Please try again later.";
        }
        
        // 开发和测试环境：过滤敏感信息但保留调试信息
        if (originalMessage == null) {
            return "An unknown error occurred.";
        }
        
        // 移除可能包含敏感信息的模式
        String sanitized = originalMessage
            .replaceAll("\\b(?:[0-9]{1,3}\\.){3}[0-9]{1,3}\\b", "[IP_HIDDEN]")  // IPv4地址
            .replaceAll("\\b(?:[0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}\\b", "[IP_HIDDEN]")  // IPv6地址
            .replaceAll("(?i)host[:\\s]+[^\\s,]+", "host: [HIDDEN]")  // Host信息
            .replaceAll("(?i)server[:\\s]+[^\\s,]+", "server: [HIDDEN]")  // Server信息
            .replaceAll("(?i)jdbc:[^\\s]+", "jdbc:[HIDDEN]")  // JDBC连接串
            .replaceAll("(?i)from[:\\s]+[0-9\\.]+:[0-9]+", "from: [HIDDEN]")  // 源地址
            .replaceAll("(?i)to[:\\s]+[0-9\\.]+:[0-9]+", "to: [HIDDEN]")  // 目标地址
            .replaceAll("(?i)connect to [^\\s]+", "connect to [HIDDEN]")  // 连接目标
            .replaceAll("(?i)connection to [^\\s]+", "connection to [HIDDEN]")  // 连接信息
            .replaceAll("(?i)at [^\\s]+\\.[a-zA-Z]+\\([^)]+\\)", "at [HIDDEN]");  // 堆栈跟踪
        
        return sanitized;
    }

    /**
     * 权限校验异常
     */
    @ExceptionHandler(AccessDeniedException.class)
    @org.springframework.web.bind.annotation.ResponseStatus(org.springframework.http.HttpStatus.FORBIDDEN)
    public AjaxResult handleAccessDeniedException(AccessDeniedException e, HttpServletRequest request)
    {
        String requestURI = request.getRequestURI();
        String errorMessage = e.getMessage();
        log.error("请求地址'{}',权限校验失败'{}'", requestURI, errorMessage);
        
        // 检查是否是 CSRF 异常
        if (errorMessage != null && (errorMessage.contains("CSRF") || errorMessage.contains("Invalid CSRF"))) {
            return AjaxResult.error(HttpStatus.FORBIDDEN, "CSRF token validation failed");
        }
        
        return AjaxResult.error(HttpStatus.FORBIDDEN, "没有权限，请联系管理员授权");
    }

    /**
     * 请求方式不支持
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public AjaxResult handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException e,
            HttpServletRequest request)
    {
        String requestURI = request.getRequestURI();
        log.error("请求地址'{}',不支持'{}'请求", requestURI, e.getMethod());
        // 返回通用错误信息，不暴露请求细节
        return AjaxResult.error("Request method not supported.");
    }

    /**
     * 业务异常
     */
    @ExceptionHandler(ServiceException.class)
    public AjaxResult handleServiceException(ServiceException e, HttpServletRequest request)
    {
        log.error(e.getMessage(), e);
        Integer code = e.getCode();
        return StringUtils.isNotNull(code) ? AjaxResult.error(code, e.getMessage()) : AjaxResult.error(e.getMessage());
    }

    /**
     * 请求路径中缺少必需的路径变量
     */
    @ExceptionHandler(MissingPathVariableException.class)
    public AjaxResult handleMissingPathVariableException(MissingPathVariableException e, HttpServletRequest request)
    {
        String requestURI = request.getRequestURI();
        log.error("请求路径中缺少必需的路径变量'{}',发生系统异常.", requestURI, e);
        return AjaxResult.error(String.format("请求路径中缺少必需的路径变量[%s]", e.getVariableName()));
    }

    /**
     * 请求参数类型不匹配
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public AjaxResult handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e, HttpServletRequest request)
    {
        String requestURI = request.getRequestURI();
        String value = Convert.toStr(e.getValue());
        if (StringUtils.isNotEmpty(value))
        {
            value = EscapeUtil.clean(value);
        }
        log.error("请求参数类型不匹配'{}',发生系统异常.", requestURI, e);
        return AjaxResult.error(String.format("请求参数类型不匹配，参数[%s]要求类型为：'%s'，但输入值为：'%s'", e.getName(), e.getRequiredType().getName(), value));
    }

    /**
     * 拦截未知的运行时异常
     */
    @ExceptionHandler(RuntimeException.class)
    public AjaxResult handleRuntimeException(RuntimeException e, HttpServletRequest request)
    {
        String requestURI = request.getRequestURI();
        log.error("请求地址'{}',发生未知异常.", requestURI, e);
        
        // 清理敏感信息后返回
        String safeMessage = sanitizeErrorMessage(e.getMessage());
        return AjaxResult.error(safeMessage);
    }

    /**
     * 系统异常
     */
    @ExceptionHandler(Exception.class)
    public AjaxResult handleException(Exception e, HttpServletRequest request)
    {
        String requestURI = request.getRequestURI();
        log.error("请求地址'{}',发生系统异常.", requestURI, e);
        
        // 清理敏感信息后返回
        String safeMessage = sanitizeErrorMessage(e.getMessage());
        return AjaxResult.error(safeMessage);
    }

    /**
     * 自定义验证异常
     */
    @ExceptionHandler(BindException.class)
    public AjaxResult handleBindException(BindException e)
    {
        log.error(e.getMessage(), e);
        String message = e.getAllErrors().get(0).getDefaultMessage();
        return AjaxResult.error(message);
    }

    /**
     * 自定义验证异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Object handleMethodArgumentNotValidException(MethodArgumentNotValidException e)
    {
        log.error(e.getMessage(), e);
        String message = e.getBindingResult().getFieldError().getDefaultMessage();
        return AjaxResult.error(message);
    }

    /**
     * 演示模式异常
     */
    @ExceptionHandler(DemoModeException.class)
    public AjaxResult handleDemoModeException(DemoModeException e)
    {
        return AjaxResult.error("演示模式，不允许操作");
    }
}

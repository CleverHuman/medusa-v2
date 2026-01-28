package com.medusa.mall.exception;

import com.medusa.common.core.domain.AjaxResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;

/**
 * BTCPay异常处理器
 */
@RestControllerAdvice
public class BtcPayExceptionHandler {
    
    private static final Logger log = LoggerFactory.getLogger(BtcPayExceptionHandler.class);
    
    /**
     * 处理BTCPay API异常
     */
    @ExceptionHandler(BtcPayApiException.class)
    public ResponseEntity<String> handleBtcPayApiException(BtcPayApiException e, HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        log.error("BTCPay API Error - URI: {}, Error: {}, API URL: {}, HTTP Status: {}", 
                requestURI, e.getMessage(), e.getApiUrl(), e.getHttpStatus(), e);
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("BTCPay API Error: " + e.getMessage());
    }
    
    /**
     * 处理BTCPay Webhook异常
     */
    @ExceptionHandler(BtcPayWebhookException.class)
    public ResponseEntity<String> handleBtcPayWebhookException(BtcPayWebhookException e, HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        log.error("BTCPay Webhook Error - URI: {}, Error: {}, Event Type: {}", 
                requestURI, e.getMessage(), e.getEventType(), e);
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("BTCPay Webhook Error: " + e.getMessage());
    }
    
    /**
     * 处理BTCPay配置异常
     */
    @ExceptionHandler(BtcPayConfigException.class)
    public ResponseEntity<String> handleBtcPayConfigException(BtcPayConfigException e, HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        log.error("BTCPay Config Error - URI: {}, Error: {}, Config Key: {}", 
                requestURI, e.getMessage(), e.getConfigKey(), e);
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("BTCPay Config Error: " + e.getMessage());
    }
    
    /**
     * 处理BTCPay通用异常
     */
    @ExceptionHandler(BtcPayException.class)
    public ResponseEntity<String> handleBtcPayException(BtcPayException e, HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        log.error("BTCPay Error - URI: {}, Error: {}, Operation: {}, Error Code: {}", 
                requestURI, e.getMessage(), e.getOperation(), e.getErrorCode(), e);
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("BTCPay Error: " + e.getMessage());
    }
} 
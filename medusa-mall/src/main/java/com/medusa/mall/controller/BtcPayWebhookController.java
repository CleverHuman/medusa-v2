// ruoyi-modules-btc/src/main/java/com/xxx/btc/controller/BtcPayWebhookController.java

package com.medusa.mall.controller;

import com.medusa.mall.config.BtcPayProperties;
import com.medusa.mall.service.BtcPayEventService;
import com.medusa.mall.utils.SignatureUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

@RestController
@RequestMapping("/api/open/btcpay")
public class BtcPayWebhookController {
    private static final Logger log = LoggerFactory.getLogger(BtcPayWebhookController.class);
    
    // 🚫 注意：WEBHOOK功能已完全禁用，所有端点都不会暴露给外部访问

    private final BtcPayEventService eventService;
    private final BtcPayProperties prop;

    @Autowired
    public BtcPayWebhookController(BtcPayEventService eventService, BtcPayProperties prop) {
        this.eventService = eventService;
        this.prop = prop;
    }

    // @PostMapping("/webhook")  // 🚫 WEBHOOK端点已完全禁用 - 不暴露HTTP接口
    public ResponseEntity<String> webhook(@RequestBody String body, 
                                        @RequestHeader(value = "BTCPay-Sig", required = false) String signature) {
        // 🚫 WEBHOOK功能已完全禁用
        log.warn("BTCPAY webhook method called but functionality is disabled");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Webhook endpoint disabled");
        
        /*
        // 检查webhook是否启用
        if (!prop.getWebhook().isEnabled()) {
            log.info("BTCPAY webhook is disabled via configuration - returning OK");
            return ResponseEntity.ok("Webhook disabled");
        }
        
        /*
        log.info("Received BTCPay webhook request");
        
        // 临时禁用签名验证，用于调试
        boolean signatureValid = true; // 临时设置为 true
        
        // 原来的签名验证代码（暂时注释掉）
        /*
        if (signature == null) {
            log.warn("Missing signature header");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing signature");
        }
        
        boolean signatureValid = SignatureUtil.verify(btcPayProperties.getWebhookSecret(), body.getBytes(), signature);
        */
        /*
        
        if (!signatureValid) {
            log.warn("Invalid webhook signature");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid signature");
        }
        
        try {
            // 处理 webhook 事件
            eventService.dispatch(body);
            return ResponseEntity.ok("OK");
        } catch (Exception e) {
            log.error("Error processing webhook", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing webhook");
        }
        */
    }
}

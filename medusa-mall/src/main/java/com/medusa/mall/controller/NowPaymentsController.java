package com.medusa.mall.controller;

import com.medusa.mall.service.NowPaymentsService;
import com.medusa.mall.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Map;

@RestController
@RequestMapping("/api/nowpayments")
public class NowPaymentsController {
    private static final Logger log = LoggerFactory.getLogger(NowPaymentsController.class);
    
    @Autowired
    private NowPaymentsService nowPaymentsService;
    
    @Autowired
    private PaymentService paymentService;
    
    @GetMapping("/status/{paymentId}")
    public ResponseEntity<Map<String, Object>> getPaymentStatus(@PathVariable String paymentId) {
        try {
            Map<String, Object> status = nowPaymentsService.getPaymentStatus(paymentId);
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            log.error("Error getting payment status", e);
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
    
    @PostMapping("/verify/{paymentId}")
    public ResponseEntity<Map<String, Object>> verifyPayment(
            @PathVariable String paymentId,
            @RequestParam String orderId) {
        try {
            boolean updated = paymentService.verifyNowPaymentsPayment(paymentId, orderId);
            return ResponseEntity.ok(Map.of("updated", updated));
        } catch (Exception e) {
            log.error("Error verifying payment", e);
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping("/history/{paymentId}")
    public ResponseEntity<Map<String, Object>> getPaymentHistory(@PathVariable String paymentId) {
        try {
            Map<String, Object> history = nowPaymentsService.getPaymentHistory(paymentId);
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            log.error("Error getting payment history", e);
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
} 
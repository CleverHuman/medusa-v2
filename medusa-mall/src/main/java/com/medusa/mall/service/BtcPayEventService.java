package com.medusa.mall.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.medusa.mall.domain.order.Order;
import com.medusa.mall.domain.order.Payment;
import com.medusa.mall.service.IOrderService;
import com.medusa.mall.service.PaymentService;
import com.medusa.mall.service.InventoryLockService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.http.MediaType;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.Date;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.util.StringUtils;
import com.medusa.mall.exception.BtcPayException;
import com.medusa.mall.exception.BtcPayApiException;
import com.medusa.mall.exception.BtcPayWebhookException;

// 新增导入：优惠券相关
import com.medusa.mall.domain.coupon.CouponHistory;
import com.medusa.mall.mapper.CouponMapper;
import com.medusa.mall.mapper.CouponHistoryMapper;

// 新增导入：会员积分和等级相关
import com.medusa.mall.service.member.IMemberPointHistoryService;
import com.medusa.mall.service.IMemberLevelService;
import com.medusa.mall.domain.member.MemberLevel;
import com.medusa.mall.domain.member.MemberBenefit;
import com.medusa.mall.mapper.MemberBenefitMapper;

@Service
public class BtcPayEventService {
    private static final Logger log = LoggerFactory.getLogger(BtcPayEventService.class);
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Autowired
    private PaymentService paymentService;
    
    @Autowired
    private IOrderService orderService;
    
    @Autowired
    private InventoryLockService inventoryLockService;

    // 新增注入：优惠券相关
    @Autowired
    private CouponMapper couponMapper;
    
    @Autowired
    private CouponHistoryMapper couponHistoryMapper;

    // 新增注入：会员积分和等级相关
    @Autowired
    private IMemberPointHistoryService memberPointHistoryService;

    @Autowired
    private IMemberLevelService memberLevelService;

    @Autowired
    private MemberBenefitMapper memberBenefitMapper;


    @Autowired
    @Qualifier("btcpayWebClient")
    private WebClient btcpayWebClient;

  
    @Value("${btcpay.url}")
    private String btcpayUrl;

    @Value("${btcpay.api.key}")
    private String btcpayApiKey;

    @Value("${btcpay.store.id}")
    private String btcpayStoreId;

   // private final WebClient webClient;
    /**
     * Handle BTCPay webhook events
     * 
     * @param webhookBody webhook request body
     */
    @Transactional
    public void dispatch(String webhookBody) {
        try {
            log.info("=== BTCPay Webhook Dispatch Start ===");
            log.info("Raw webhook body: {}", webhookBody);
            if (StringUtils.isEmpty(webhookBody)) {
                throw new BtcPayWebhookException("Webhook body is null or empty", "UNKNOWN", webhookBody);
            }
            JsonNode event;
            try {
                event = objectMapper.readTree(webhookBody);
            } catch (Exception e) {
                throw new BtcPayWebhookException("Failed to parse webhook JSON", "UNKNOWN", webhookBody, e);
            }
            String type = event.path("type").asText();
            log.info("Webhook event type: '{}'", type);
            if (StringUtils.isEmpty(type)) {
                throw new BtcPayWebhookException("Event type is null or empty", "UNKNOWN", webhookBody);
            }
            switch (type) {
                case "InvoiceReceivedPayment":
                    handleInvoiceReceivedPayment(event);
                    break;
                case "InvoicePaymentSettled":
                    handleInvoicePaymentSettled(event);
                    break;
                case "InvoiceSettled":
                    handleInvoicePaymentSettled(event);
                    break;
                case "InvoiceExpired":
                    handleInvoiceExpired(event);
                    break;
                case "InvoiceInvalid":
                    handleInvoiceInvalid(event);
                    break;
                default:
                    log.warn("Unhandled webhook event type: '{}'", type);
                    log.info("Available event data: {}", event.toString());
            }
            log.info("=== BTCPay Webhook Dispatch Completed ===");
        } catch (BtcPayWebhookException e) {
            throw e;
        } catch (Exception e) {
            throw new BtcPayWebhookException("Failed to process webhook", "UNKNOWN", webhookBody, e);
        }
    }

    /**
     * Handle payment received event
     */
    private void handleInvoiceReceivedPayment(JsonNode event) {
        try {
            log.info("=== BTCPay InvoiceReceivedPayment Event Debug ===");
            log.info("Raw event data: {}", event.toString());
            JsonNode data = event.path("data");
            String invoiceId = data.path("id").asText();
            String orderId = data.path("metadata").path("orderId").asText();
            log.info("Extracted invoiceId: '{}', orderId: '{}'", invoiceId, orderId);
            if (StringUtils.isEmpty(invoiceId)) {
                throw new BtcPayWebhookException("InvoiceId is null or empty", "InvoiceReceivedPayment", event.toString());
            }
            if (StringUtils.isEmpty(orderId)) {
                throw new BtcPayWebhookException("OrderId is null or empty", "InvoiceReceivedPayment", event.toString(), invoiceId);
            }
            log.info("Invoice {} received payment for order {}", invoiceId, orderId);
            Payment payment = paymentService.getPaymentByOrderId(orderId);
            if (payment != null) {
                log.info("Found payment record for orderId: {}", orderId);
                payment.setPayStatus(1); // 1 means paid
                payment.setPaymentNo(invoiceId);
                payment.setPayTime(new Date());
                payment.setRemark("Payment received via BTCPay");
                paymentService.updatePaymentStatus(payment);
                log.info("Successfully updated payment status for orderId: {}", orderId);
                Order order = orderService.selectOrderById(orderId);
                if (order != null) {
                    log.info("Found order for orderId: {}", orderId);
                    order.setStatus(1); // 1 means paid
                    
                    // 确认库存扣减（支付成功）
                    boolean inventoryConfirmed = inventoryLockService.confirmInventory(orderId);
                    if (inventoryConfirmed) {
                        log.info("Successfully confirmed inventory for order: {}", orderId);
                    } else {
                        log.warn("Failed to confirm inventory for order: {}", orderId);
                    }
                    orderService.updateOrder(order);
                    log.info("Successfully updated order status for orderId: {}", orderId);
                } else {
                    log.warn("Order not found for orderId: {}", orderId);
                }
                log.info("Updated payment and order status for order: {}", orderId);
            } else {
                throw new BtcPayWebhookException("Payment record not found for order: " + orderId, "InvoiceReceivedPayment", event.toString(), invoiceId);
            }
            log.info("=== BTCPay InvoiceReceivedPayment Event Processing Completed ===");
        } catch (BtcPayWebhookException e) {
            throw e;
        } catch (Exception e) {
            throw new BtcPayWebhookException("Error handling InvoiceReceivedPayment", "InvoiceReceivedPayment", event.toString(), e);
        }
    }

    /**
     * Handle payment settled event
     */
    private void handleInvoicePaymentSettled(JsonNode event) {
        try {
            // 详细的调试信息
            log.info("=== BTCPay InvoicePaymentSettled Event Debug ===");
            log.info("Raw event data: {}", event.toString());
            
            JsonNode data = event;
            String invoiceId = data.path("invoiceId").asText();
            String orderId = data.path("metadata").path("orderId").asText();
            
            log.info("Extracted invoiceId: {}, orderId: {}", invoiceId, orderId);
            
            // 验证必要字段
            if (StringUtils.isEmpty(invoiceId)) {
                log.error("InvoiceId is null or empty, skipping payment settlement");
                return;
            }
            
            if (StringUtils.isEmpty(orderId)) {
                log.error("OrderId is null or empty, skipping payment settlement");
                return;
            }

            // 检查是否已经处理过
            Payment existingPayment = paymentService.getPaymentByOrderId(orderId);
            if (existingPayment != null && existingPayment.getPayStatus() == 1) {
                log.info("Payment for order {} already processed as paid, skipping duplicate processing", orderId);
                return;
            }

            
            String url = btcpayUrl + "/api/v1/stores/" + btcpayStoreId + "/invoices/" + invoiceId;
            log.info("Fetching invoice details from URL: {}", url);

            JsonNode invoiceDetail = btcpayWebClient.get()
                .uri(url)
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "token " + btcpayApiKey)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();

            log.info("Invoice detail response: {}", invoiceDetail.toString());

            // 2. 安全解析支付金额
            String amountStr = invoiceDetail.path("amount").asText();
            String paidAmountStr = invoiceDetail.path("paidAmount").asText();
            String currency = invoiceDetail.path("currency").asText();
            String status = invoiceDetail.path("status").asText();
            
            log.info("Parsed values - amount: {}, paidAmount: {}, currency: {}, status: {}", 
                    amountStr, paidAmountStr, currency, status);
            
            // 验证金额字段
            if (StringUtils.isEmpty(amountStr)) {
                log.error("Amount is null or empty in invoice detail, cannot process payment");
                return;
            }
            
            // 对于已结算的发票，如果paidAmount为空，使用amount作为实际支付金额
            if (StringUtils.isEmpty(paidAmountStr)) {
                log.warn("PaidAmount is null or empty, but invoice is settled. Using amount as paid amount.");
                paidAmountStr = amountStr;
            }
            
            // 安全创建BigDecimal
            BigDecimal amount;
            BigDecimal paidAmount;
            try {
                amount = new BigDecimal(amountStr);
                paidAmount = new BigDecimal(paidAmountStr);
                log.info("Successfully created BigDecimal - amount: {}, paidAmount: {}", amount, paidAmount);
            } catch (NumberFormatException e) {
                log.error("Failed to parse amount values - amount: {}, paidAmount: {}", amountStr, paidAmountStr, e);
                return;
            }
            
/* 
            // 临时测试数据 - 生成足够的金额用于测试积分和等级更新
            log.info("Using test data for development - BTCPay API call temporarily disabled");
            BigDecimal amount = new BigDecimal("1000.00"); // 1000 AUD
            BigDecimal paidAmount = new BigDecimal("1000.00"); // 1000 AUD
            String currency = "AUD";
            String status = "Settled";
            
            log.info("Test values - amount: {}, paidAmount: {}, currency: {}, status: {}", 
                    amount, paidAmount, currency, status);
*/
            // 3. 更新 payment 记录
            log.info("Processing payment for orderId: {}", orderId);
            Payment paymentRecord = paymentService.getPaymentByOrderId(orderId);
            if (paymentRecord != null) {
                log.info("Found payment record for orderId: {}", orderId);
                paymentRecord.setPayStatus(1); // 1 means paid
                paymentRecord.setPaymentNo(invoiceId);
                paymentRecord.setPayTime(new Date());
                paymentRecord.setRemark("Payment settled via BTCPay. invoiceId: " + invoiceId + ", paid: " + paidAmount + " " + currency);
                paymentRecord.setPaidcoin(paidAmount); // 实付金额

                paymentService.updatePaymentStatus(paymentRecord);
                log.info("Successfully updated payment status for orderId: {}", orderId);

                // 更新订单状态
                Order order = orderService.selectOrderById(orderId);
                if (order != null) {
                    log.info("Found order for orderId: {}", orderId);
                    order.setStatus(1); // 1 means paid
                    
                    // 确认库存扣减（支付成功）
                    boolean inventoryConfirmed = inventoryLockService.confirmInventory(orderId);
                    if (inventoryConfirmed) {
                        log.info("Successfully confirmed inventory for order: {}", orderId);
                    } else {
                        log.warn("Failed to confirm inventory for order: {}", orderId);
                    }
                    orderService.updateOrder(order);
                    log.info("Successfully updated order status for orderId: {}", orderId);

                    // BTCPay Webhook事件处理积分（作为PaymentMonitorService的备用机制）
                    // 幂等性检查已在addPointHistoryForOrder中实现，可以安全调用
                    processMemberPointsAndLevel(order, paidAmount);
                } else {
                    log.warn("Order not found for orderId: {}", orderId);
                }
            } else {
                log.warn("Payment record not found for orderId: {}", orderId);
            }
            
            log.info("=== BTCPay InvoicePaymentSettled Event Processing Completed ===");
        } catch (Exception e) {
            log.error("Error handling InvoicePaymentSettled for event: {}", event.toString(), e);
        }
    }

    /**
     * Handle invoice expired event
     */
    private void handleInvoiceExpired(JsonNode event) {
        try {
            JsonNode data = event.path("data");
            String invoiceId = data.path("id").asText();
            String orderId = data.path("metadata").path("orderId").asText();
            
            log.info("Invoice {} expired for order {}", invoiceId, orderId);
            
            // Update payment status to failed
            Payment payment = paymentService.getPaymentByOrderId(orderId);
            if (payment != null) {
                payment.setPayStatus(2); // 2 means payment failed
                payment.setRemark("Invoice expired");
                
                paymentService.updatePaymentStatus(payment);
                
                // Update order status
                Order order = orderService.selectOrderById(orderId);
                if (order != null) {
                    order.setStatus(4); // 4 means closed
                    
                    // 释放库存（订单取消）
                    boolean inventoryReleased = inventoryLockService.releaseInventory(orderId);
                    if (inventoryReleased) {
                        log.info("Successfully released inventory for expired/invalid order: {}", orderId);
                    } else {
                        log.warn("Failed to release inventory for expired/invalid order: {}", orderId);
                    }

                    // 恢复优惠券（幂等）：仅当订单存在couponId，且尚未记录过"已过期"历史时执行
                    if (order.getCouponId() != null) {
                        try {
                            int restoredCount = couponHistoryMapper.countByCouponOrderAndStatus(order.getCouponId(), Long.valueOf(order.getId()), 2);
                            if (restoredCount == 0) {
                                int dec = couponMapper.decrementUsedCount(order.getCouponId());
                                if (dec > 0) {
                                    log.info("Restored coupon used_count for couponId {} due to expiry of order {}", order.getCouponId(), orderId);
                                } else {
                                    log.warn("Failed to restore coupon used_count for couponId {} on order {}", order.getCouponId(), orderId);
                                }

                                CouponHistory ch = new CouponHistory();
                                ch.setCouponId(order.getCouponId());
                                ch.setMemberId(order.getMemberId());
                                ch.setOrderId(Long.valueOf(order.getId()));
                                ch.setOrderSn(order.getOrderSn());
                                ch.setUseStatus(2); // 已过期
                                ch.setUseTime(new Date());
                                ch.setCreateTime(new Date());
                                ch.setUpdateTime(new Date());
                                ch.setRemark("Invoice expired - coupon restored");
                                couponHistoryMapper.insertCouponHistory(ch);
                            } else {
                                log.info("Coupon already restored for order {}, skip duplicate restore", orderId);
                            }
                        } catch (Exception ex) {
                            log.error("Failed to restore coupon for expired order {}", orderId, ex);
                        }
                    }
                    orderService.updateOrder(order);
                }
                
                log.info("Updated payment and order status for expired invoice: {}", orderId);
            }
        } catch (Exception e) {
            log.error("Error handling InvoiceExpired", e);
        }
    }

    /**
     * Handle invoice invalid event
     */
    private void handleInvoiceInvalid(JsonNode event) {
        try {
            JsonNode data = event.path("data");
            String invoiceId = data.path("id").asText();
            String orderId = data.path("metadata").path("orderId").asText();
            
            log.info("Invoice {} invalid for order {}", invoiceId, orderId);
            
            // Update payment status to failed
            Payment payment = paymentService.getPaymentByOrderId(orderId);
            if (payment != null) {
                payment.setPayStatus(2); // 2 means payment failed
                payment.setRemark("Invoice invalid");
                
                paymentService.updatePaymentStatus(payment);
                
                // Update order status
                Order order = orderService.selectOrderById(orderId);
                if (order != null) {
                    order.setStatus(4); // 4 means closed
                    
                    // 释放库存（订单取消）
                    boolean inventoryReleased = inventoryLockService.releaseInventory(orderId);
                    if (inventoryReleased) {
                        log.info("Successfully released inventory for expired/invalid order: {}", orderId);
                    } else {
                        log.warn("Failed to release inventory for expired/invalid order: {}", orderId);
                    }

                    // 恢复优惠券（幂等）：仅当订单存在couponId，且尚未记录过"已过期"历史时执行
                    if (order.getCouponId() != null) {
                        try {
                            int restoredCount = couponHistoryMapper.countByCouponOrderAndStatus(order.getCouponId(), Long.valueOf(order.getId()), 2);
                            if (restoredCount == 0) {
                                int dec = couponMapper.decrementUsedCount(order.getCouponId());
                                if (dec > 0) {
                                    log.info("Restored coupon used_count for couponId {} due to invalidation of order {}", order.getCouponId(), orderId);
                                } else {
                                    log.warn("Failed to restore coupon used_count for couponId {} on order {}", order.getCouponId(), orderId);
                                }

                                CouponHistory ch = new CouponHistory();
                                ch.setCouponId(order.getCouponId());
                                ch.setMemberId(order.getMemberId());
                                ch.setOrderId(Long.valueOf(order.getId()));
                                ch.setOrderSn(order.getOrderSn());
                                ch.setUseStatus(2); // 已过期
                                ch.setUseTime(new Date());
                                ch.setCreateTime(new Date());
                                ch.setUpdateTime(new Date());
                                ch.setRemark("Invoice invalid - coupon restored");
                                couponHistoryMapper.insertCouponHistory(ch);
                            } else {
                                log.info("Coupon already restored for order {}, skip duplicate restore", orderId);
                            }
                        } catch (Exception ex) {
                            log.error("Failed to restore coupon for invalid order {}", orderId, ex);
                        }
                    }
                    orderService.updateOrder(order);
                }
                
                log.info("Updated payment and order status for invalid invoice: {}", orderId);
            }
        } catch (Exception e) {
            log.error("Error handling InvoiceInvalid", e);
        }
    }

    /**
     * 处理会员积分和等级更新
     * @param order 订单信息
     * @param paidAmount 实付金额
     */
    private void processMemberPointsAndLevel(Order order, BigDecimal paidAmount) {
        try {
            // 检查是否为有效会员订单（非Guest订单）
            if (order.getMemberId() == null || order.getMemberId() <= 0) {
                log.info("Order {} is a guest order, skipping member points and level update", order.getId());
                return;
            }
            
            log.info("Processing member points and level update for order: {}, memberId: {}, paidAmount: {}", 
                    order.getId(), order.getMemberId(), paidAmount);
            
            // 1. 添加积分历史记录（金额的1%作为积分）
            String formattedAmount = paidAmount.setScale(2, java.math.RoundingMode.HALF_UP).toString();
            String note = "Order payment: " + order.getOrderSn() + ", Amount: " + formattedAmount + " AUD";
            Integer platform = order.getSourceType(); // 0: OS, 1: TG
            
            int result = memberPointHistoryService.addPointHistoryForOrder(
                order.getMemberId(), 
                paidAmount, 
                note, 
                platform
            );
            
            if (result > 0) {
                log.info("Successfully added point history for member: {}, order: {}", order.getMemberId(), order.getId());
                
                // 2. 查询更新后的会员积分信息（等级不会实时更新）
                MemberLevel memberLevel = memberLevelService.selectMemberLevelByMemberId(order.getMemberId());
                if (memberLevel != null) {
                    log.info("Member {} points and total orders updated (level will be updated by scheduled task) - Current Level: {}, Current Points: {}, Total Orders: {}", 
                            order.getMemberId(), memberLevel.getCurrentLevel(), memberLevel.getCurrentPoint(), memberLevel.getTotalOrders());
                } else {
                    log.warn("Failed to retrieve member level for member: {}", order.getMemberId());
                }
            } else {
                log.error("Failed to add point history for member: {}, order: {}", order.getMemberId(), order.getId());
            }
            
        } catch (Exception e) {
            log.error("Error processing member points and level update for order: {}", order.getId(), e);
        }
    }
} 
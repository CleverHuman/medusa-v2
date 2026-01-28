package com.medusa.mall.service;

import com.medusa.mall.domain.order.Order;
import com.medusa.mall.domain.order.Payment;
import com.medusa.mall.utils.BtcPayRetryUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.databind.JsonNode;
import java.math.BigDecimal;
import java.util.Date;
import org.springframework.util.StringUtils;
import org.springframework.http.MediaType;
import com.medusa.mall.service.InventoryLockService;
import com.medusa.mall.service.NowPaymentsService;

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

// 新增导入：Vendor Application (for bond processing)
import com.medusa.mall.service.IVendorApplicationService;
import com.medusa.mall.domain.vendor.VendorApplication;

// 新增导入：OrderItem (for bond order detection)
import com.medusa.mall.domain.order.OrderItem;
import com.medusa.mall.mapper.OrderItemMapper;

/**
 * 统一支付监控服务
 */
@Service
public class PaymentMonitorService {

    private static final Logger log = LoggerFactory.getLogger(PaymentMonitorService.class);

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
    private NowPaymentsService nowPaymentsService;
    
    // 新增注入：Vendor Application Service (for bond processing)
    @Autowired
    private IVendorApplicationService vendorApplicationService;
    
    // 新增注入：OrderItem Mapper (for bond order detection)
    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    @Qualifier("btcpayWebClient")
    private WebClient btcpayWebClient;

    @Value("${btcpay.url}")
    private String btcpayUrl;

    @Value("${btcpay.api.key}")
    private String btcpayApiKey;

    // ✅ 修正：各自独立注入
    @Value("${btcpay.store.id}")
    private String btcpayStoreId;

    @Value("${nowpayments.payment.timeout-minutes:20}")
    private int nowPaymentsTimeoutMinutes;

    /**
     * 检测待支付的发票
     */
    public void checkPendingInvoices() {
        List<Payment> pendingPayments = paymentService.getPendingPayments();
        log.info("Found {} pending payments to check", pendingPayments.size());

        for (Payment payment : pendingPayments) {
            try {
                checkInvoiceStatus(payment);
            } catch (Exception e) {
                log.error("Failed to check invoice status for payment: {}", payment.getPaymentNo(), e);
            }
        }
    }

    /**
     * 检测过期发票
     */
    public void checkExpiredInvoices() {
        List<Payment> expiredPayments = paymentService.getExpiredPayments();
        log.info("Found {} expired payments to handle", expiredPayments.size());

        for (Payment payment : expiredPayments) {
            try {
                handleExpiredInvoice(payment);
            } catch (Exception e) {
                log.error("Failed to handle expired invoice: {}", payment.getPaymentNo(), e);
            }
        }
    }

    /**
     * 检查单个发票状态
     */
    private void checkInvoiceStatus(Payment payment) {
        String invoiceId = payment.getPaymentNo();
        log.debug("Checking invoice status for payment: {}, invoice: {}", payment.getOrderId(), invoiceId);

        // 跳过 LTC 手动支付（payType = "4"），这些订单没有连接支付网关
        if ("4".equals(payment.getPayType())) {
            log.debug("Skipping LTC manual payment (payType=4) for order: {}, no payment gateway integration", payment.getOrderId());
            return;
        }

        // 根据支付类型选择监控方式
        if (isNowPaymentsPayment(payment)) {
            checkNowPaymentsStatus(payment);
        } else {
            checkBtcPayStatus(payment);
        }
    }

    /**
     * 检查是否为NOWPayments支付
     */
    private boolean isNowPaymentsPayment(Payment payment) {
        return "1".equals(payment.getPayType()); // USDT支付使用NOWPayments
    }

    /**
     * 检查NOWPayments支付状态
     */
    private void checkNowPaymentsStatus(Payment payment) {
        String paymentId = payment.getPaymentNo();
        log.debug("Checking NOWPayments status for payment: {}, paymentId: {}", payment.getOrderId(), paymentId);

        try {
            Map<String, Object> paymentStatus = nowPaymentsService.getPaymentStatus(paymentId);

            if (paymentStatus != null) {
                String status = (String) paymentStatus.get("payment_status");
                log.debug("NOWPayments payment {} status: {}", paymentId, status);

                // 新增：检查是否超时
                if (isNowPaymentsPaymentExpired(payment, status)) {
                    handleNowPaymentsExpired(payment);
                    return;
                }

                log.debug("NOWPayments payment {} status: {}", paymentId, status);
                handleNowPaymentsStatusChange(payment, status, paymentStatus);
            } else {
                log.warn("No payment data returned for NOWPayments payment: {}", paymentId);
            }
        } catch (Exception e) {
            log.error("Error checking NOWPayments status for payment: {}", paymentId, e);
        }
    }

    /**
     * 检查BTCPay支付状态
     */
    /**
     * 检查NOWPayments支付是否超时
     */
    private boolean isNowPaymentsPaymentExpired(Payment payment, String status) {
        // 只有等待状态的支付才需要检查超时
        if (!"waiting".equals(status) && !"pending".equals(status)) {
            return false;
        }

        // 检查支付创建时间
        Date payTime = payment.getPayTime();
        if (payTime == null) {
            log.warn("Payment {} has no payTime, cannot check timeout", payment.getPaymentNo());
            return false;
        }

        long currentTime = System.currentTimeMillis();
        long paymentTime = payTime.getTime();
        long timeoutMillis = nowPaymentsTimeoutMinutes * 60 * 1000L;

        boolean isExpired = (currentTime - paymentTime) > timeoutMillis;

        if (isExpired) {
            log.info("NOWPayments payment {} expired after {} minutes",
                    payment.getPaymentNo(), nowPaymentsTimeoutMinutes);
        }

        return isExpired;
    }

    private void checkBtcPayStatus(Payment payment) {
        String invoiceId = payment.getPaymentNo();
        log.debug("Checking BTCPay status for payment: {}, invoice: {}", payment.getOrderId(), invoiceId);

        try {
            // 调用BTCPay API获取发票状态
            Map<String, Object> invoice = BtcPayRetryUtil.retryWithBackoff(
                    btcpayWebClient.get()
                            .uri(btcpayUrl + "/api/v1/stores/" + btcpayStoreId + "/invoices/" + invoiceId)
                            .header("Authorization", "token " + btcpayApiKey)
                            .retrieve()
                            .bodyToMono(Map.class),
                    "Get Invoice Status"
            ).block();

            if (invoice != null) {
                String status = (String) invoice.get("status");
                log.debug("Invoice {} status: {}", invoiceId, status);
                handleInvoiceStatusChange(payment, status, invoice);
            } else {
                log.warn("No invoice data returned for invoice: {}", invoiceId);
            }
        } catch (Exception e) {
            log.error("Error checking invoice status for invoice: {}", invoiceId, e);
        }
    }

    /**
     * 处理发票状态变化
     */
    private void handleInvoiceStatusChange(Payment payment, String status, Map<String, Object> invoice) {
        switch (status) {
            case "Settled":
                handleSettledInvoice(payment, invoice);
                break;
            case "Expired":
                handleExpiredInvoice(payment);
                break;
            case "Invalid":
                handleInvalidInvoice(payment);
                break;
            case "New":
            case "Processing":
                // ✅ 检查是否有部分支付（underpaid）
                checkAndUpdatePartialPaymentForAnyStatus(payment);
                log.debug("Invoice {} is still in {} status, continuing to wait", payment.getPaymentNo(), status);
                break;
            default:
                log.warn("Unknown invoice status: {} for payment: {}", status, payment.getPaymentNo());
        }
    }

    /**
     * 处理NOWPayments状态变化
     */
    private void handleNowPaymentsStatusChange(Payment payment, String status, Map<String, Object> paymentStatus) {
        switch (status) {
            case "finished":
            case "confirmed":
                handleNowPaymentsSettled(payment, paymentStatus);
                break;
            case "expired":
                handleNowPaymentsExpired(payment);
                break;
            case "waiting":
            case "pending":
                // 继续等待，不做处理
                log.debug("NOWPayments payment {} is still in {} status, continuing to wait", payment.getPaymentNo(), status);
                break;
            default:
                log.warn("Unknown NOWPayments status: {} for payment: {}", status, payment.getPaymentNo());
        }
    }

    /**
     * 处理已结算的发票
     */
    private void handleSettledInvoice(Payment payment, Map<String, Object> invoice) {
        log.info("=== BTCPay InvoiceMonitor Settled Invoice Debug ===");
        log.info("Processing settled invoice for payment: {}, order: {}", payment.getPaymentNo(), payment.getOrderId());

        // 检查是否已经处理过
        if (payment.getPayStatus() == 1) {
            log.info("Payment {} already processed as paid, skipping duplicate processing", payment.getPaymentNo());
            return;
        }

        try {
            String invoiceId = payment.getPaymentNo();
            String orderId = payment.getOrderId();

            // ✅ 改用 hasText
            if (!StringUtils.hasText(invoiceId)) {
                log.error("InvoiceId is null or empty, skipping payment settlement");
                return;
            }

            if (!StringUtils.hasText(orderId)) {
                log.error("OrderId is null or empty, skipping payment settlement");
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

            String amountStr = invoiceDetail.path("amount").asText();
            String paidAmountStr = invoiceDetail.path("paidAmount").asText();
            String currency = invoiceDetail.path("currency").asText();
            String status = invoiceDetail.path("status").asText();
            
            // 检查是否有payments数组，获取实际支付金额
            JsonNode paymentsNode = invoiceDetail.path("payments");
            BigDecimal actualPaidAmount = null;
            if (paymentsNode.isArray() && paymentsNode.size() > 0) {
                log.info("Found {} payments in BTCPay response", paymentsNode.size());
                BigDecimal totalReceived = BigDecimal.ZERO;
                for (JsonNode paymentNode : paymentsNode) {
                    String paymentStatus = paymentNode.path("status").asText();
                    String valueStr = paymentNode.path("value").asText();
                    log.info("Payment status: {}, value: {}", paymentStatus, valueStr);
                    
                    // 只统计已确认的支付
                    if ("Settled".equals(paymentStatus) || "confirmed".equals(paymentStatus)) {
                        if (StringUtils.hasText(valueStr)) {
                            try {
                                BigDecimal paymentValue = new BigDecimal(valueStr);
                                totalReceived = totalReceived.add(paymentValue);
                                log.info("Added confirmed payment value: {} to total", paymentValue);
                            } catch (NumberFormatException e) {
                                log.warn("Failed to parse payment value: {}", valueStr, e);
                            }
                        }
                    }
                }
                if (totalReceived.compareTo(BigDecimal.ZERO) > 0) {
                    actualPaidAmount = totalReceived;
                    log.info("Calculated total received from payments array: {}", actualPaidAmount);
                }
            } else {
                log.info("No payments array found or empty, using paidAmount field");
            }

            log.info("Parsed values - amount: {}, paidAmount: {}, currency: {}, status: {}, actualReceived: {}",
                    amountStr, paidAmountStr, currency, status, actualPaidAmount);

            // 优先使用从payments数组计算的实际支付金额
            BigDecimal finalPaidAmount = null;
            String amountSource = null;
            
            // 1️⃣ 尝试从payments数组获取
            if (actualPaidAmount != null && actualPaidAmount.compareTo(BigDecimal.ZERO) > 0) {
                finalPaidAmount = actualPaidAmount;
                amountSource = "payments_array";
                log.info("Using calculated amount from payments array: {}", finalPaidAmount);
            }
            
            // 2️⃣ 尝试从paidAmount字段获取（必须大于0）
            if (finalPaidAmount == null && StringUtils.hasText(paidAmountStr)) {
                try {
                    BigDecimal paidAmountValue = new BigDecimal(paidAmountStr);
                    if (paidAmountValue.compareTo(BigDecimal.ZERO) > 0) {
                        finalPaidAmount = paidAmountValue;
                        amountSource = "paidAmount_field";
                        log.info("Using paidAmount field: {}", finalPaidAmount);
                    } else {
                        log.warn("paidAmount field is zero or negative: {}, will try next fallback", paidAmountStr);
                    }
                } catch (NumberFormatException e) {
                    log.warn("Failed to parse paidAmount field: {}, will try fallback", paidAmountStr);
                }
            }
            
            // 3️⃣ 尝试从amount字段获取（发票金额）
            if (finalPaidAmount == null && StringUtils.hasText(amountStr)) {
                log.warn("No valid paid amount found, using invoice amount as fallback");
                try {
                    BigDecimal amountValue = new BigDecimal(amountStr);
                    if (amountValue.compareTo(BigDecimal.ZERO) > 0) {
                        finalPaidAmount = amountValue;
                        amountSource = "amount_fallback";
                        log.info("Using invoice amount as fallback: {}", finalPaidAmount);
                    } else {
                        log.warn("Invoice amount is zero or negative: {}, will use order totalAmount", amountStr);
                    }
                } catch (NumberFormatException e) {
                    log.warn("Failed to parse invoice amount: {}, will use order totalAmount", amountStr);
                }
            }
            
            // ✅ 最终回退：如果所有BTCPay金额都无法获取，使用订单的totalAmount
            if (finalPaidAmount == null) {
                log.warn("No valid amount found from BTCPay, will use order totalAmount as ultimate fallback");
                Payment tempPayment = paymentService.getPaymentByOrderId(orderId);
                if (tempPayment != null && tempPayment.getAmount() != null) {
                    finalPaidAmount = tempPayment.getAmount();
                    amountSource = "order_totalAmount_fallback";
                    log.info("Using order totalAmount as ultimate fallback: {}", finalPaidAmount);
                } else {
                    log.error("Cannot determine payment amount from any source, order: {}", orderId);
                    return;
                }
            }

            // ✅ amount字段用于日志记录，如果无法解析则使用finalPaidAmount
            BigDecimal amount = finalPaidAmount;
            if (StringUtils.hasText(amountStr)) {
                try {
                    amount = new BigDecimal(amountStr);
                    log.info("Invoice amount: {}, finalPaidAmount: {} (source: {})", 
                            amount, finalPaidAmount, amountSource);
                } catch (NumberFormatException e) {
                    log.warn("Failed to parse invoice amount: {}, using finalPaidAmount: {}", amountStr, finalPaidAmount);
                }
            } else {
                log.info("No invoice amount available, using finalPaidAmount: {} (source: {})", 
                        finalPaidAmount, amountSource);
            }

            log.info("Processing payment for orderId: {}", orderId);
            Payment paymentRecord = paymentService.getPaymentByOrderId(orderId);
            if (paymentRecord != null) {
                log.info("Found payment record for orderId: {}", orderId);
                
                // ✅ 调试日志：打印payment更新前的状态
                log.info("=== Payment BEFORE update - orderId: {}, payStatus: {}, paidcoin: {}, amount: {} ===",
                        orderId, paymentRecord.getPayStatus(), paymentRecord.getPaidcoin(), paymentRecord.getAmount());
                
                paymentRecord.setPayStatus(1);
                paymentRecord.setPaymentNo(invoiceId);
                paymentRecord.setPayTime(new Date());
                paymentRecord.setRemark("Payment settled via BTCPay Monitor. invoiceId: " + invoiceId + ", paid: " + finalPaidAmount + " " + currency + " (source: " + amountSource + ")");
                paymentRecord.setPaidcoin(finalPaidAmount);
                
                // ✅ 调试日志：打印payment更新后的状态
                log.info("=== Payment AFTER setPaidcoin - orderId: {}, payStatus: {}, paidcoin: {}, finalPaidAmount: {} ===",
                        orderId, paymentRecord.getPayStatus(), paymentRecord.getPaidcoin(), finalPaidAmount);

                paymentService.updatePaymentStatus(paymentRecord);
                log.info("Successfully updated payment status for orderId: {}", orderId);

                Order order = orderService.selectOrderById(orderId);
                if (order != null) {
                    log.info("Found order for orderId: {}", orderId);
                    
                    // ✅ 调试日志：打印order信息
                    log.info("=== Order info - orderId: {}, orderSn: {}, memberId: {}, totalAmount: {}, status: {} ===",
                            order.getId(), order.getOrderSn(), order.getMemberId(), order.getTotalAmount(), order.getStatus());
                    
                    order.setStatus(1);

                    boolean inventoryConfirmed = inventoryLockService.confirmInventory(orderId);
                    if (inventoryConfirmed) {
                        log.info("Successfully confirmed inventory for order: {}", orderId);
                    } else {
                        log.warn("Failed to confirm inventory for order: {}", orderId);
                    }

                    // ✅ 调试日志：打印即将传递给积分处理的金额
                    log.info("=== Calling processMemberPointsAndLevel with finalPaidAmount: {}, scale: {} ===",
                            finalPaidAmount, finalPaidAmount.scale());
                    
                    processMemberPointsAndLevel(order, finalPaidAmount);

                    orderService.updateOrder(order);
                    log.info("Successfully updated order status for orderId: {}", orderId);
                    
                    // ✅ 修改：增强 BOND 订单检查逻辑，支持多种 BOND SKU 格式
                    // 1. BOND-BASE-UNIT 或 BOND-BASE-UNIT- 开头
                    // 2. SKU-BOND-BASE- 开头（管理员修改后的格式，如 SKU-BOND-BASE-1000）
                    boolean isBondOrder = "BOND_PAYMENT".equals(order.getOrderType());
                    if (!isBondOrder) {
                        // 如果 orderType 不是 BOND_PAYMENT，检查订单项中是否包含 BOND 相关的 SKU
                        List<OrderItem> orderItems = orderItemMapper.selectOrderItemsByOrderId(orderId);
                        if (orderItems != null) {
                            for (OrderItem item : orderItems) {
                                String sku = item.getSku();
                                if (sku != null && (
                                    sku.equals("BOND-BASE-UNIT") || 
                                    sku.startsWith("BOND-BASE-UNIT-") || 
                                    sku.startsWith("SKU-BOND-BASE-")
                                )) {
                                    isBondOrder = true;
                                    log.warn("[Bond Payment] Order {} has BOND SKU but orderType is not BOND_PAYMENT. " +
                                            "Treating as BOND order. SKU: {}", orderId, sku);
                                    // 同时更新订单的 orderType 为 BOND_PAYMENT
                                    order.setOrderType("BOND_PAYMENT");
                                    orderService.updateOrder(order);
                                    break;
                                }
                            }
                        }
                    }
                    
                    if (isBondOrder && order.getBondApplicationId() != null) {
                        log.info("[Bond Payment] Processing bond payment for order: {}, applicationId: {}", 
                                orderId, order.getBondApplicationId());
                        processBondPaymentComplete(order);
                    } else if (isBondOrder && order.getBondApplicationId() == null) {
                        log.error("[Bond Payment] BOND order {} found but bondApplicationId is null. " +
                                "Cannot process bond payment completion. Please check order creation process.", orderId);
                    }
                } else {
                    log.warn("Order not found for orderId: {}", orderId);
                }
            } else {
                log.warn("Payment record not found for orderId: {}", orderId);
            }

            log.info("=== BTCPay InvoiceMonitor Settled Invoice Processing Completed ===");
        } catch (Exception e) {
            log.error("Error processing settled invoice for payment: {}", payment.getPaymentNo(), e);
        }
    }

    /**
     * 处理会员积分和等级更新
     */
    private void processMemberPointsAndLevel(Order order, BigDecimal paidAmount) {
        try {
            if (order.getMemberId() == null || order.getMemberId() <= 0) {
                log.info("Order {} is a guest order, skipping member points and level update", order.getId());
                return;
            }
            
            // 检测临时用户（负数member_id）
            if (isTempUser(order.getMemberId())) {
                log.info("Order {} is from a temporary user (memberId: {}), skipping member points and level update", 
                        order.getId(), order.getMemberId());
                return;
            }

            log.info("Processing member points and level update for order: {}, memberId: {}, paidAmount: {}",
                    order.getId(), order.getMemberId(), paidAmount);

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

                // 查询更新后的会员积分信息（等级不会实时更新）
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

    /**
     * 检测是否为临时用户
     * 临时用户使用负数member_id (< -1000000)
     */
    private boolean isTempUser(Long memberId) {
        return memberId != null && memberId < -1000000L;
    }

    private Integer calculateMemberLevel(BigDecimal totalPoints) {
        List<MemberBenefit> memberBenefits = memberBenefitMapper.selectAllMemberBenefitsOrderByPoint();

        Integer targetLevel = 1;

        for (MemberBenefit benefit : memberBenefits) {
            if (benefit.getPoint() != null && totalPoints.compareTo(benefit.getPoint()) >= 0) {
                targetLevel = benefit.getLevelId().intValue();
            } else {
                break;
            }
        }

        return targetLevel;
    }

    /**
     * 检查并更新部分支付（适用于任何状态，包括 New、Processing、Expired）
     * 即使发票状态不是 Settled，只要 payments 数组中有已确认的支付，就更新 paidcoin
     */
    private void checkAndUpdatePartialPaymentForAnyStatus(Payment payment) {
        // 跳过 LTC 手动支付（payType = "4"），这些订单没有连接支付网关
        if ("4".equals(payment.getPayType())) {
            return;
        }
        
        // 只处理 BTCPay 支付
        if (isNowPaymentsPayment(payment)) {
            // NOWPayments 支付由其他方法处理
            return;
        }
        
        try {
            String invoiceId = payment.getPaymentNo();
            if (!StringUtils.hasText(invoiceId)) {
                return;
            }
            
            log.info("Checking partial payment for invoice: {} (order: {})", invoiceId, payment.getOrderId());
            
            // 获取发票详情
            JsonNode invoiceDetail = btcpayWebClient.get()
                    .uri(btcpayUrl + "/api/v1/stores/" + btcpayStoreId + "/invoices/" + invoiceId)
                    .accept(MediaType.APPLICATION_JSON)
                    .header("Authorization", "token " + btcpayApiKey)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();
            
            if (invoiceDetail == null) {
                log.warn("Unable to get invoice details for {}", invoiceId);
                return;
            }
            
            String status = invoiceDetail.path("status").asText();
            String amountStr = invoiceDetail.path("amount").asText();
            String paidAmountStr = invoiceDetail.path("paidAmount").asText();
            String currency = invoiceDetail.path("currency").asText();
            
            // 检查 payments 数组，获取实际支付金额（币种金额）
            JsonNode paymentsNode = invoiceDetail.path("payments");
            BigDecimal actualPaidAmount = null; // 币种金额
            String paymentCurrency = null; // 记录币种
            
            if (paymentsNode.isArray() && paymentsNode.size() > 0) {
                log.info("Found {} payments in BTCPay response for invoice {}", paymentsNode.size(), invoiceId);
                BigDecimal totalReceived = BigDecimal.ZERO;
                
                for (JsonNode paymentNode : paymentsNode) {
                    String paymentStatus = paymentNode.path("status").asText();
                    String valueStr = paymentNode.path("value").asText();
                    String paymentCurrencyStr = paymentNode.path("currency").asText(); // 获取币种
                    
                    log.debug("Payment status: {}, value: {}, currency: {}", paymentStatus, valueStr, paymentCurrencyStr);
                    
                    // 只统计已确认的支付
                    if ("Settled".equals(paymentStatus) || "confirmed".equals(paymentStatus)) {
                        if (StringUtils.hasText(valueStr)) {
                            try {
                                BigDecimal paymentValue = new BigDecimal(valueStr);
                                totalReceived = totalReceived.add(paymentValue);
                                paymentCurrency = paymentCurrencyStr; // 记录币种
                                log.info("Added confirmed payment value: {} {} to total", paymentValue, paymentCurrencyStr);
                            } catch (NumberFormatException e) {
                                log.warn("Failed to parse payment value: {}", valueStr, e);
                            }
                        }
                    }
                }
                
                if (totalReceived.compareTo(BigDecimal.ZERO) > 0) {
                    actualPaidAmount = totalReceived; // ✅ 这是币种金额，可以直接使用
                    log.info("Calculated total received from payments array: {} {} for invoice {}", 
                            actualPaidAmount, paymentCurrency, invoiceId);
                }
            }
            
            // ⚠️ 如果没有从 payments 数组获取到，尝试从 paidAmount 字段获取
            // 但需要注意：paidAmount 可能是 AUD 金额，需要转换为币种金额
            if (actualPaidAmount == null && StringUtils.hasText(paidAmountStr)) {
                try {
                    BigDecimal paidAmountValue = new BigDecimal(paidAmountStr);
                    if (paidAmountValue.compareTo(BigDecimal.ZERO) > 0) {
                        // 检查 currency 字段，判断 paidAmount 是 AUD 还是币种金额
                        if ("AUD".equalsIgnoreCase(currency)) {
                            // paidAmount 是 AUD 金额，需要转换为币种金额
                            Payment paymentRecord = paymentService.getPaymentByOrderId(payment.getOrderId());
                            if (paymentRecord != null && paymentRecord.getRate() != null && 
                                paymentRecord.getRate().compareTo(BigDecimal.ZERO) > 0) {
                                // 转换为币种金额：AUD / rate = 币种金额
                                actualPaidAmount = paidAmountValue.divide(paymentRecord.getRate(), 8, java.math.RoundingMode.HALF_UP);
                                log.info("Converted paidAmount from AUD {} to coin amount: {} (rate: {}) for invoice {}", 
                                        paidAmountValue, actualPaidAmount, paymentRecord.getRate(), invoiceId);
                            } else {
                                log.warn("Cannot convert paidAmount to coin amount: rate is null or zero for order {}", 
                                        payment.getOrderId());
                                return; // 无法转换，退出
                            }
                        } else {
                            // paidAmount 已经是币种金额，直接使用
                            actualPaidAmount = paidAmountValue;
                            paymentCurrency = currency; // 记录币种
                            log.info("Using paidAmount field as coin amount: {} {} for invoice {}", 
                                    actualPaidAmount, currency, invoiceId);
                        }
                    }
                } catch (NumberFormatException e) {
                    log.warn("Failed to parse paidAmount field: {}", paidAmountStr, e);
                }
            }
            
            // 如果找到了实际支付金额，更新 paidcoin
            if (actualPaidAmount != null && actualPaidAmount.compareTo(BigDecimal.ZERO) > 0) {
                Payment paymentRecord = paymentService.getPaymentByOrderId(payment.getOrderId());
                if (paymentRecord != null) {
                    // 检查是否需要更新（避免重复更新）
                    boolean needsUpdate = false;
                    if (paymentRecord.getPaidcoin() == null) {
                        needsUpdate = true;
                        log.info("paidcoin is null, will update to {}", actualPaidAmount);
                    } else if (paymentRecord.getPaidcoin().compareTo(actualPaidAmount) != 0) {
                        needsUpdate = true;
                        log.info("paidcoin changed from {} to {}, will update", 
                                paymentRecord.getPaidcoin(), actualPaidAmount);
                    }
                    
                    if (needsUpdate) {
                        // 计算发票金额（用于判断是否 underpaid）
                        BigDecimal invoiceAmount = BigDecimal.ZERO;
                        if (StringUtils.hasText(amountStr)) {
                            try {
                                invoiceAmount = new BigDecimal(amountStr);
                            } catch (NumberFormatException e) {
                                log.warn("Failed to parse invoice amount: {}", amountStr, e);
                            }
                        }
                        
                        // 判断是否 underpaid（需要比较币种金额）
                        // 如果 invoiceAmount 是 AUD，需要转换为币种金额进行比较
                        boolean isUnderpaid = false;
                        if (invoiceAmount.compareTo(BigDecimal.ZERO) > 0) {
                            if ("AUD".equalsIgnoreCase(currency)) {
                                // invoiceAmount 是 AUD，需要转换为币种金额
                                if (paymentRecord.getRate() != null && 
                                    paymentRecord.getRate().compareTo(BigDecimal.ZERO) > 0) {
                                    BigDecimal invoiceCoinAmount = invoiceAmount.divide(
                                        paymentRecord.getRate(), 8, java.math.RoundingMode.HALF_UP);
                                    isUnderpaid = actualPaidAmount.compareTo(invoiceCoinAmount) < 0;
                                    log.debug("Comparing coin amounts - paid: {} vs invoice: {} (converted from {} AUD)", 
                                            actualPaidAmount, invoiceCoinAmount, invoiceAmount);
                                }
                            } else {
                                // invoiceAmount 已经是币种金额，直接比较
                                isUnderpaid = actualPaidAmount.compareTo(invoiceAmount) < 0;
                                log.debug("Comparing coin amounts - paid: {} vs invoice: {}", 
                                        actualPaidAmount, invoiceAmount);
                            }
                        }
                        
                        // 更新 paidcoin（币种金额）
                        paymentRecord.setPaidcoin(actualPaidAmount);
                        
                        // 更新备注，记录部分支付信息，并添加特殊标记表示这是币种金额
                        String finalCurrency = paymentCurrency != null ? paymentCurrency : currency;
                        String remark = String.format(
                            "Partial payment detected via BTCPay Monitor. invoiceId: %s, status: %s, paid: %s %s, invoice: %s %s%s [COIN_AMOUNT]",
                            invoiceId, status, actualPaidAmount, finalCurrency,
                            invoiceAmount.compareTo(BigDecimal.ZERO) > 0 ? invoiceAmount.toString() : "N/A", currency,
                            isUnderpaid ? " (UNDERPAID)" : ""
                        );
                        paymentRecord.setRemark(remark);
                        
                        paymentService.updatePaymentStatus(paymentRecord);
                        
                        log.info("✅ Updated paidcoin to {} {} for invoice {} (status: {}, underpaid: {})", 
                                actualPaidAmount, finalCurrency, invoiceId, status, isUnderpaid);
                    } else {
                        log.debug("paidcoin already set to {}, no update needed", actualPaidAmount);
                    }
                } else {
                    log.warn("Payment record not found for order: {}", payment.getOrderId());
                }
            } else {
                log.debug("No partial payment found for invoice {} (status: {})", invoiceId, status);
            }
            
        } catch (Exception e) {
            log.error("Error checking partial payment for invoice: {}", payment.getPaymentNo(), e);
        }
    }

    /**
     * 处理过期发票
     */
    private void handleExpiredInvoice(Payment payment) {
        log.info("Processing potentially expired invoice for payment: {}, order: {}", payment.getPaymentNo(), payment.getOrderId());

        // 跳过 LTC 手动支付（payType = "4"），这些订单没有连接支付网关
        if ("4".equals(payment.getPayType())) {
            log.debug("Skipping LTC manual payment (payType=4) for order: {}, no payment gateway integration", payment.getOrderId());
            return;
        }

        try {
            // ✅ 根据支付类型选择处理方式
            if (isNowPaymentsPayment(payment)) {
                // NOWPayments 支付 (USDT)
                log.info("Handling expired NOWPayments payment: {}", payment.getPaymentNo());
                handleNowPaymentsExpired(payment);
                return;
            }
            
            // BTCPay 支付 (BTC/XMR)
            String invoiceId = payment.getPaymentNo();
            
            // ✅ 先检查 BTCPay 发票的实际状态
            log.info("Checking actual BTCPay invoice status for {}", invoiceId);
            Map<String, Object> invoice = BtcPayRetryUtil.retryWithBackoff(
                    btcpayWebClient.get()
                            .uri(btcpayUrl + "/api/v1/stores/" + btcpayStoreId + "/invoices/" + invoiceId)
                            .header("Authorization", "token " + btcpayApiKey)
                            .retrieve()
                            .bodyToMono(Map.class),
                    "Check Expired Invoice Status"
            ).block();
            
            if (invoice != null) {
                String actualStatus = (String) invoice.get("status");
                log.info("Actual BTCPay invoice status for {}: {}", invoiceId, actualStatus);
                
                // ✅ 如果发票正在处理或已结算，不取消订单
                if ("Processing".equals(actualStatus)) {
                    log.info("Invoice {} is in Processing status (payment received, awaiting confirmation). Skipping cancellation.", invoiceId);
                    return; // 用户已支付，等待确认，不取消
                }
                
                if ("Settled".equals(actualStatus)) {
                    log.info("Invoice {} is already Settled. Triggering settled handler.", invoiceId);
                    handleSettledInvoice(payment, invoice); // 处理为已结算
                    return;
                }
                
                if ("Confirmed".equals(actualStatus) || "New".equals(actualStatus)) {
                    log.info("Invoice {} is in {} status. Skipping cancellation.", invoiceId, actualStatus);
                    return; // 继续等待
                }
                
                // ✅ 只有真正 Expired 或 Invalid 才取消订单
                if (!"Expired".equals(actualStatus) && !"Invalid".equals(actualStatus)) {
                    log.warn("Invoice {} has unexpected status: {}. Skipping cancellation for safety.", invoiceId, actualStatus);
                    return;
                }
                
                log.info("Invoice {} is truly {} (not Processing). Checking for partial payment before cancellation.", invoiceId, actualStatus);
                
                // ✅ 在取消订单前，检查是否有部分支付
                checkAndUpdatePartialPaymentForAnyStatus(payment);
            } else {
                log.warn("Unable to get invoice details for {}. Checking for partial payment before cancellation.", invoiceId);
                // ✅ 即使无法获取状态，也尝试检查部分支付
                checkAndUpdatePartialPaymentForAnyStatus(payment);
            }
            
            // 以下是原有的取消订单逻辑
            payment.setPayStatus(2); // 过期状态
            paymentService.updatePaymentStatus(payment);

            Order order = orderService.selectOrderById(payment.getOrderId());
            if (order != null) {
                order.setStatus(4); // Cancelled状态

                boolean inventoryReleased = inventoryLockService.releaseInventory(payment.getOrderId());
                if (inventoryReleased) {
                    log.info("Successfully released inventory for expired/invalid order: {}", payment.getOrderId());
                } else {
                    log.warn("Failed to release inventory for expired/invalid order: {}", payment.getOrderId());
                }

                if (order.getCouponId() != null) {
                    try {
                        int restoredCount = couponHistoryMapper.countByCouponOrderAndStatus(order.getCouponId(), Long.valueOf(order.getId()), 2);
                        if (restoredCount == 0) {
                            int dec = couponMapper.decrementUsedCount(order.getCouponId());
                            if (dec > 0) {
                                log.info("Restored coupon used_count for couponId {} due to expiry of order {}", order.getCouponId(), order.getId());
                            } else {
                                log.warn("Failed to restore coupon used_count for couponId {} on order {}", order.getCouponId(), order.getId());
                            }
                            CouponHistory ch = new CouponHistory();
                            ch.setCouponId(order.getCouponId());
                            ch.setMemberId(order.getMemberId());
                            ch.setOrderId(Long.valueOf(order.getId()));
                            ch.setOrderSn(order.getOrderSn());
                            ch.setUseStatus(2);
                            ch.setUseTime(new Date());
                            ch.setCreateTime(new Date());
                            ch.setUpdateTime(new Date());
                            ch.setRemark("Invoice expired - coupon restored");
                            couponHistoryMapper.insertCouponHistory(ch);
                        } else {
                            log.info("Coupon already restored for order {}, skip duplicate restore", order.getId());
                        }
                    } catch (Exception ex) {
                        log.error("Failed to restore coupon for expired order {}", order.getId(), ex);
                    }
                }
                orderService.updateOrder(order);
                log.info("Updated order {} status to Cancelled", order.getId());
            } else {
                log.warn("Order not found for payment: {}", payment.getOrderId());
            }

            log.info("Successfully processed expired payment for order: {}, invoice: {}",
                    payment.getOrderId(), payment.getPaymentNo());

        } catch (Exception e) {
            log.error("Error processing expired invoice for payment: {}", payment.getPaymentNo(), e);
        }
    }

    /**
     * 处理无效发票
     */
    private void handleInvalidInvoice(Payment payment) {
        log.info("Processing potentially invalid invoice for payment: {}, order: {}", payment.getPaymentNo(), payment.getOrderId());

        // 跳过 LTC 手动支付（payType = "4"），这些订单没有连接支付网关
        if ("4".equals(payment.getPayType())) {
            log.debug("Skipping LTC manual payment (payType=4) for order: {}, no payment gateway integration", payment.getOrderId());
            return;
        }

        try {
            // ✅ 根据支付类型选择处理方式
            if (isNowPaymentsPayment(payment)) {
                // NOWPayments 支付 (USDT)
                log.info("Handling invalid NOWPayments payment: {}", payment.getPaymentNo());
                handleNowPaymentsExpired(payment);
                return;
            }
            
            // BTCPay 支付 (BTC/XMR)
            String invoiceId = payment.getPaymentNo();
            
            // ✅ 先检查 BTCPay 发票的实际状态
            log.info("Checking actual BTCPay invoice status for {}", invoiceId);
            Map<String, Object> invoice = BtcPayRetryUtil.retryWithBackoff(
                    btcpayWebClient.get()
                            .uri(btcpayUrl + "/api/v1/stores/" + btcpayStoreId + "/invoices/" + invoiceId)
                            .header("Authorization", "token " + btcpayApiKey)
                            .retrieve()
                            .bodyToMono(Map.class),
                    "Check Invalid Invoice Status"
            ).block();
            
            if (invoice != null) {
                String actualStatus = (String) invoice.get("status");
                log.info("Actual BTCPay invoice status for {}: {}", invoiceId, actualStatus);
                
                // ✅ 如果发票正在处理或已结算，不取消订单
                if ("Processing".equals(actualStatus)) {
                    log.info("Invoice {} is in Processing status (payment received, awaiting confirmation). Skipping cancellation.", invoiceId);
                    return; // 用户已支付，等待确认，不取消
                }
                
                if ("Settled".equals(actualStatus)) {
                    log.info("Invoice {} is already Settled. Triggering settled handler.", invoiceId);
                    handleSettledInvoice(payment, invoice); // 处理为已结算
                    return;
                }
                
                if ("Confirmed".equals(actualStatus) || "New".equals(actualStatus)) {
                    log.info("Invoice {} is in {} status. Skipping cancellation.", invoiceId, actualStatus);
                    return; // 继续等待
                }
                
                // ✅ 只有真正 Expired 或 Invalid 才取消订单
                if (!"Expired".equals(actualStatus) && !"Invalid".equals(actualStatus)) {
                    log.warn("Invoice {} has unexpected status: {}. Skipping cancellation for safety.", invoiceId, actualStatus);
                    return;
                }
                
                log.info("Invoice {} is truly {} (not Processing). Checking for partial payment before cancellation.", invoiceId, actualStatus);
                
                // ✅ 在取消订单前，检查是否有部分支付
                checkAndUpdatePartialPaymentForAnyStatus(payment);
            } else {
                log.warn("Unable to get invoice details for {}. Checking for partial payment before cancellation.", invoiceId);
                // ✅ 即使无法获取状态，也尝试检查部分支付
                checkAndUpdatePartialPaymentForAnyStatus(payment);
            }
            
            // 以下是原有的取消订单逻辑
            payment.setPayStatus(3); // 无效状态
            paymentService.updatePaymentStatus(payment);

            Order order = orderService.selectOrderById(payment.getOrderId());
            if (order != null) {
                order.setStatus(4); // Cancelled状态

                boolean inventoryReleased = inventoryLockService.releaseInventory(payment.getOrderId());
                if (inventoryReleased) {
                    log.info("Successfully released inventory for expired/invalid order: {}", payment.getOrderId());
                } else {
                    log.warn("Failed to release inventory for expired/invalid order: {}", payment.getOrderId());
                }

                if (order.getCouponId() != null) {
                    try {
                        int restoredCount = couponHistoryMapper.countByCouponOrderAndStatus(order.getCouponId(), Long.valueOf(order.getId()), 2);
                        if (restoredCount == 0) {
                            int dec = couponMapper.decrementUsedCount(order.getCouponId());
                            if (dec > 0) {
                                log.info("Restored coupon used_count for couponId {} due to invalidation of order {}", order.getCouponId(), order.getId());
                            } else {
                                log.warn("Failed to restore coupon used_count for couponId {} on order {}", order.getCouponId(), order.getId());
                            }
                            CouponHistory ch = new CouponHistory();
                            ch.setCouponId(order.getCouponId());
                            ch.setMemberId(order.getMemberId());
                            ch.setOrderId(Long.valueOf(order.getId()));
                            ch.setOrderSn(order.getOrderSn());
                            ch.setUseStatus(2);
                            ch.setUseTime(new Date());
                            ch.setCreateTime(new Date());
                            ch.setUpdateTime(new Date());
                            ch.setRemark("Invoice invalid - coupon restored");
                            couponHistoryMapper.insertCouponHistory(ch);
                        } else {
                            log.info("Coupon already restored for order {}, skip duplicate restore", order.getId());
                        }
                    } catch (Exception ex) {
                        log.error("Failed to restore coupon for invalid order {}", order.getId(), ex);
                    }
                }
                orderService.updateOrder(order);
                log.info("Updated order {} status to Cancelled", order.getId());
            } else {
                log.warn("Order not found for payment: {}", payment.getOrderId());
            }

            log.info("Successfully processed invalid payment for order: {}, invoice: {}",
                    payment.getOrderId(), payment.getPaymentNo());

        } catch (Exception e) {
            log.error("Error processing invalid invoice for payment: {}", payment.getPaymentNo(), e);
        }
    }

    /**
     * 处理NOWPayments已结算的支付
     */
    private void handleNowPaymentsSettled(Payment payment, Map<String, Object> paymentStatus) {
        log.info("=== NOWPayments InvoiceMonitor Settled Payment Debug ===");
        log.info("Processing settled NOWPayments payment: {}, order: {}", payment.getPaymentNo(), payment.getOrderId());

        if (payment.getPayStatus() == 1) {
            log.info("Payment {} already processed as paid, skipping duplicate processing", payment.getPaymentNo());
            return;
        }

        try {
            String paymentId = payment.getPaymentNo();
            String orderId = payment.getOrderId();

            if (!StringUtils.hasText(paymentId)) {
                log.error("PaymentId is null or empty, skipping payment settlement");
                return;
            }

            if (!StringUtils.hasText(orderId)) {
                log.error("OrderId is null or empty, skipping payment settlement");
                return;
            }

            // 优先使用 outcome_amount 字段（Outcome Price），其次 actually_paid，最后 pay_amount
            Object outcomeAmountObj = paymentStatus.get("outcome_amount");
            Object actuallyPaidObj = paymentStatus.get("actually_paid");
            Object payAmountObj = paymentStatus.get("pay_amount");
            Object payCurrencyObj = paymentStatus.get("pay_currency");
            Object outcomeCurrencyObj = paymentStatus.get("outcome_currency");

            String payAmountStr = null;
            String effectiveCurrency = null;
            
            // 第一优先级：outcome_amount (Outcome Price)
            if (outcomeAmountObj != null) {
                if (outcomeAmountObj instanceof Number) {
                    payAmountStr = outcomeAmountObj.toString();
                } else if (outcomeAmountObj instanceof String) {
                    payAmountStr = (String) outcomeAmountObj;
                }
                // 使用outcome_currency作为币种
                if (outcomeCurrencyObj != null) {
                    effectiveCurrency = outcomeCurrencyObj.toString();
                }
                log.info("Using outcome_amount field (Outcome Price): {} {} - Direct USDT amount, no rate conversion needed", payAmountStr, effectiveCurrency);
            }
            // 第二优先级：actually_paid
            else if (actuallyPaidObj != null) {
                if (actuallyPaidObj instanceof Number) {
                    payAmountStr = actuallyPaidObj.toString();
                } else if (actuallyPaidObj instanceof String) {
                    payAmountStr = (String) actuallyPaidObj;
                }
                log.info("Using actually_paid field: {}", payAmountStr);
            }
            // 第三优先级：pay_amount
            else if (payAmountObj != null) {
                if (payAmountObj instanceof Number) {
                    payAmountStr = payAmountObj.toString();
                } else if (payAmountObj instanceof String) {
                    payAmountStr = (String) payAmountObj;
                }
                log.warn("outcome_amount and actually_paid not available, falling back to pay_amount: {}", payAmountStr);
            }

            // 如果没有获取到有效的币种，使用pay_currency作为备选
            String payCurrency = effectiveCurrency;
            if (payCurrency == null && payCurrencyObj != null) {
                payCurrency = payCurrencyObj.toString();
            }

            log.info("NOWPayments payment info - finalAmount: {}, effectiveCurrency: {} (outcome_currency优先)", payAmountStr, payCurrency);

            if (!StringUtils.hasText(payAmountStr)) {
                log.error("PayAmount is null or empty in NOWPayments response, cannot process payment");
                return;
            }

            BigDecimal paidAmount;
            try {
                paidAmount = new BigDecimal(payAmountStr);
                log.info("Successfully created BigDecimal - paidAmount: {}", paidAmount);
            } catch (NumberFormatException e) {
                log.error("Failed to parse payAmount value: {}", payAmountStr, e);
                return;
            }

            log.info("Processing NOWPayments payment for orderId: {}", orderId);
            Payment paymentRecord = paymentService.getPaymentByOrderId(orderId);
            if (paymentRecord != null) {
                log.info("Found payment record for orderId: {}", orderId);
                paymentRecord.setPayStatus(1);
                paymentRecord.setPaymentNo(paymentId);
                paymentRecord.setPayTime(new Date());
                // 根据使用的金额字段设置不同的remark标记
                String remark;
                if (outcomeAmountObj != null) {
                    remark = "Payment settled via NOWPayments Monitor (outcome_amount). paymentId: " + 
                             paymentId + ", paid: " + paidAmount + " " + payCurrency;
                } else if (actuallyPaidObj != null) {
                    remark = "Payment settled via NOWPayments Monitor (actually_paid). paymentId: " + 
                             paymentId + ", paid: " + paidAmount + " " + payCurrency;
                } else {
                    remark = "Payment settled via NOWPayments Monitor (pay_amount). paymentId: " + 
                             paymentId + ", paid: " + paidAmount + " " + payCurrency;
                }
                paymentRecord.setRemark(remark);
                paymentRecord.setPaidcoin(paidAmount);

                paymentService.updatePaymentStatus(paymentRecord);
                log.info("Successfully updated NOWPayments payment status for orderId: {}", orderId);

                Order order = orderService.selectOrderById(orderId);
                if (order != null) {
                    log.info("Found order for orderId: {}", orderId);
                    order.setStatus(1);

                    boolean inventoryConfirmed = inventoryLockService.confirmInventory(orderId);
                    if (inventoryConfirmed) {
                        log.info("Successfully confirmed inventory for order: {}", orderId);
                    } else {
                        log.warn("Failed to confirm inventory for order: {}", orderId);
                    }

                    orderService.updateOrder(order);
                    log.info("Successfully updated order status for orderId: {}", orderId);

                    processMemberPointsAndLevel(order, paidAmount);
                    
                    // ✅ 修改：增强 BOND 订单检查逻辑，支持多种 BOND SKU 格式
                    // 1. BOND-BASE-UNIT 或 BOND-BASE-UNIT- 开头
                    // 2. SKU-BOND-BASE- 开头（管理员修改后的格式，如 SKU-BOND-BASE-1000）
                    boolean isBondOrder = "BOND_PAYMENT".equals(order.getOrderType());
                    if (!isBondOrder) {
                        // 如果 orderType 不是 BOND_PAYMENT，检查订单项中是否包含 BOND 相关的 SKU
                        List<OrderItem> orderItems = orderItemMapper.selectOrderItemsByOrderId(orderId);
                        if (orderItems != null) {
                            for (OrderItem item : orderItems) {
                                String sku = item.getSku();
                                if (sku != null && (
                                    sku.equals("BOND-BASE-UNIT") || 
                                    sku.startsWith("BOND-BASE-UNIT-") || 
                                    sku.startsWith("SKU-BOND-BASE-")
                                )) {
                                    isBondOrder = true;
                                    log.warn("[Bond Payment] Order {} has BOND SKU but orderType is not BOND_PAYMENT. " +
                                            "Treating as BOND order. SKU: {}", orderId, sku);
                                    // 同时更新订单的 orderType 为 BOND_PAYMENT
                                    order.setOrderType("BOND_PAYMENT");
                                    orderService.updateOrder(order);
                                    break;
                                }
                            }
                        }
                    }
                    
                    if (isBondOrder && order.getBondApplicationId() != null) {
                        log.info("[Bond Payment] Processing bond payment for order: {}, applicationId: {}", 
                                orderId, order.getBondApplicationId());
                        processBondPaymentComplete(order);
                    } else if (isBondOrder && order.getBondApplicationId() == null) {
                        log.error("[Bond Payment] BOND order {} found but bondApplicationId is null. " +
                                "Cannot process bond payment completion. Please check order creation process.", orderId);
                    }
                } else {
                    log.error("Order not found for orderId: {}", orderId);
                }
            } else {
                log.error("Payment record not found for orderId: {}", orderId);
            }

            log.info("Successfully processed settled NOWPayments payment: {}, order: {}", payment.getPaymentNo(), payment.getOrderId());
        } catch (Exception e) {
            log.error("Error processing settled NOWPayments payment: {}, order: {}", payment.getPaymentNo(), payment.getOrderId(), e);
        }
    }

    /**
     * 处理NOWPayments过期的支付
     */
    private void handleNowPaymentsExpired(Payment payment) {
        log.info("Processing expired NOWPayments payment: {}, order: {}", payment.getPaymentNo(), payment.getOrderId());

        try {
            Payment paymentRecord = paymentService.getPaymentByOrderId(payment.getOrderId());
            if (paymentRecord != null && paymentRecord.getPayStatus() != 2) {
                paymentRecord.setPayStatus(2); // 2 means expired
                paymentRecord.setRemark("Payment expired via NOWPayments Monitor");
                paymentService.updatePaymentStatus(paymentRecord);
                log.info("Successfully marked NOWPayments payment as expired: {}", payment.getPaymentNo());

                Order order = orderService.selectOrderById(payment.getOrderId());
                if (order != null) {
                    order.setStatus(4); // Cancelled状态

                    boolean inventoryReleased = inventoryLockService.releaseInventory(payment.getOrderId());
                    if (inventoryReleased) {
                        log.info("Successfully released inventory for expired NOWPayments order: {}", payment.getOrderId());
                    } else {
                        log.warn("Failed to release inventory for expired NOWPayments order: {}", payment.getOrderId());
                    }

                    if (order.getCouponId() != null) {
                        try {
                            int restoredCount = couponHistoryMapper.countByCouponOrderAndStatus(order.getCouponId(), Long.valueOf(order.getId()), 2);
                            if (restoredCount == 0) {
                                int dec = couponMapper.decrementUsedCount(order.getCouponId());
                                if (dec > 0) {
                                    log.info("Restored coupon used_count for couponId {} due to expiry of NOWPayments order {}", order.getCouponId(), order.getId());
                                } else {
                                    log.warn("Failed to restore coupon used_count for couponId {} on NOWPayments order {}", order.getCouponId(), order.getId());
                                }
                                CouponHistory ch = new CouponHistory();
                                ch.setCouponId(order.getCouponId());
                                ch.setMemberId(order.getMemberId());
                                ch.setOrderId(Long.valueOf(order.getId()));
                                ch.setOrderSn(order.getOrderSn());
                                ch.setUseStatus(2);
                                ch.setUseTime(new Date());
                                ch.setCreateTime(new Date());
                                ch.setUpdateTime(new Date());
                                ch.setRemark("NOWPayments payment expired - coupon restored");
                                couponHistoryMapper.insertCouponHistory(ch);
                            } else {
                                log.info("Coupon already restored for NOWPayments order {}, skip duplicate restore", order.getId());
                            }
                        } catch (Exception ex) {
                            log.error("Failed to restore coupon for expired NOWPayments order {}", order.getId(), ex);
                        }
                    }

                    orderService.updateOrder(order);
                    log.info("Updated NOWPayments order {} status to Cancelled", order.getId());
                } else {
                    log.warn("Order not found for NOWPayments payment: {}", payment.getOrderId());
                }
            }
        } catch (Exception e) {
            // ✅ 修正：文案与 expired 场景一致
            log.error("Error processing expired NOWPayments payment: {}, order: {}",
                    payment.getPaymentNo(), payment.getOrderId(), e);
        }
    }
    
    /**
     * Process bond payment completion
     * Update vendor application with bond order information
     */
    private void processBondPaymentComplete(Order order) {
        try {
            Long applicationId = order.getBondApplicationId();
            String orderId = order.getId();
            
            log.info("[Bond Payment] Processing bond payment completion for application: {}, order: {}", 
                    applicationId, orderId);
            
            // Get application
            VendorApplication application = vendorApplicationService.selectVendorApplicationById(applicationId);
            
            if (application == null) {
                log.error("[Bond Payment] Application not found: {}", applicationId);
                return;
            }
            
            // Check if already processed (idempotency)
            if (application.getBondOrderId() != null && application.getBondOrderId().equals(orderId)) {
                log.info("[Bond Payment] Bond payment already processed for application: {}", applicationId);
                return;
            }
            
            // Update application with bond payment info
            application.setBondOrderId(orderId);
            application.setBondPaidTime(new Date());
            
            int result = vendorApplicationService.updateVendorApplication(application);
            
            if (result > 0) {
                log.info("[Bond Payment] Successfully updated application {} with bond payment info. OrderId: {}", 
                        applicationId, orderId);
                
                // TODO: Send notification to admin that bond has been paid
                // TODO: Update application status if needed (e.g., to 'awaiting_wallet_verification')
                
            } else {
                log.error("[Bond Payment] Failed to update application {} with bond payment info", applicationId);
            }
            
        } catch (Exception e) {
            log.error("[Bond Payment] Error processing bond payment for order: {}", order.getId(), e);
        }
    }
}

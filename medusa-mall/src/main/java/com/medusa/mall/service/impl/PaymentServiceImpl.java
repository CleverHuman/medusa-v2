package com.medusa.mall.service.impl;

import com.medusa.mall.domain.order.Payment;
import com.medusa.mall.mapper.PaymentMapper;
import com.medusa.mall.service.PaymentService;
import com.medusa.mall.service.NowPaymentsService;
import com.medusa.mall.service.IOrderService;
import com.medusa.mall.service.InventoryLockService;
import com.medusa.mall.domain.order.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.UUID;
import java.util.Date;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import java.math.BigDecimal;
import com.medusa.mall.utils.BtcPayRetryUtil;

@Service
public class PaymentServiceImpl implements PaymentService {
    private static final Logger log = LoggerFactory.getLogger(PaymentServiceImpl.class);

    @Autowired
    private PaymentMapper paymentMapper;

    @Value("${btcpay.url}")
    private String btcpayUrl;

    @Value("${btcpay.api.key}")
    private String btcpayApiKey;

    @Value("${btcpay.store.id}")
    private String btcpayStoreId;

    private final WebClient btcpayWebClient;

    @Autowired
    private NowPaymentsService nowPaymentsService;

    @Autowired
    private IOrderService orderService;

    @Autowired
    private InventoryLockService inventoryLockService;

    @Autowired
    public PaymentServiceImpl(@Qualifier("btcpayWebClient") WebClient btcpayWebClient) {
        this.btcpayWebClient = btcpayWebClient;
    }

    @Override
    @Transactional
    public Payment createPayment(Payment payment) {
        
        //  generate UUID as payment record ID
        // Set payment ID if not already set
        if (payment.getId() == null || payment.getId().isEmpty()) {
            payment.setId(UUID.randomUUID().toString());
        }
        //  set default value
        if (payment.getPayStatus() == null) {
            payment.setPayStatus(0); // 未支付状态
        }
        if (payment.getPayTime() == null) {
            payment.setPayTime(new Date());
        }
        if (payment.getPayType() == null) {
            payment.setPayType("0"); // 默认支付类型为BTC
        }

        try {
            // LTC 支付：只记录，不连接支付网关
            if (payment.getPayType().equals("4")) {
                return createManualPayment(payment);
            }
            // 检查是否为 USDT 支付且需要使用 NOWPayments
            else if (payment.getPayType().equals("1") && useNowPaymentsForUSDT()) {
                return createNowPaymentsPayment(payment);
            } else {
                return createBtcPayPayment(payment);
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Error creating payment: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create payment", e);
        }
    }

    /**
     * 创建手动支付记录（用于 LTC 等不需要自动支付网关的币种）
     * 只记录订单信息，需要手动确认收款
     */
    private Payment createManualPayment(Payment payment) {
        log.info("Creating manual payment record for LTC, amount: {}", payment.getAmount());
        
        // 设置支付状态为待支付（需要手动确认）
        payment.setPayStatus(0);
        payment.setPayTime(new Date());
        
        // 生成简单的支付ID
        String paymentId = "LTC-" + System.currentTimeMillis();
        payment.setPaymentNo(paymentId);
        
        // 设置币种信息
        payment.setCurrency("LTC");
        
        // ✅ 设置 totalcoin 字段（这是订单列表中显示的 Total Coin）
        if (payment.getAmount() != null) {
            payment.setTotalcoin(payment.getAmount());
            log.info("Set totalcoin for LTC payment: {}", payment.getAmount());
        } else {
            log.warn("Payment amount is null for LTC payment");
        }
        
        // ❌ 不设置 rate 字段，保持为 null
        // 这样 OrderServiceImpl 就不会重新计算 orderItem 的 totalCoin
        // 对于手动支付，totalCoin 已经在 OrderItem 创建时正确设置
        payment.setRate(null);
        log.info("LTC manual payment - rate set to null (no exchange rate calculation needed)");
        
        // 设置备注说明这是手动支付
        String originalRemark = payment.getRemark() != null ? payment.getRemark() : "";
        payment.setRemark(originalRemark + " [Manual Payment - LTC: Please manually verify payment receipt]");
        
        // 保存到数据库
        paymentMapper.insertPayment(payment);
        
        log.info("Manual payment record created successfully: {}, totalcoin: {}", paymentId, payment.getTotalcoin());
        return payment;
    }

    private Payment createNowPaymentsPayment(Payment payment) {
        Map<String, Object> nowPayment = nowPaymentsService.createPayment(payment);
        
        String paymentId = (String) nowPayment.get("payment_id");
        String payAddress = (String) nowPayment.get("pay_address");
        Object payAmountObj = nowPayment.get("pay_amount");
        String payCurrency = (String) nowPayment.get("pay_currency");
        Object rateObj = nowPayment.get("rate");
        
        // 安全地处理pay_amount，可能是String或Double
        String payAmount = null;
        if (payAmountObj != null) {
            if (payAmountObj instanceof String) {
                payAmount = (String) payAmountObj;
            } else if (payAmountObj instanceof Number) {
                payAmount = payAmountObj.toString();
            }
        }
        
        // 处理汇率信息 - 优先使用conversion_rate（CoinGecko AUD到USDT直接汇率）
        Object conversionRateObj = nowPayment.get("conversion_rate");
        Object combinedRateObj = nowPayment.get("combined_aud_usdt_rate");
        Object exchangeRateObj = nowPayment.get("exchange_rate");
        Object originalAmountObj = nowPayment.get("original_amount");
        Object originalCurrencyObj = nowPayment.get("original_currency");
        String conversionSource = (String) nowPayment.get("conversion_source");
        
        String rate = null;
        String rateDescription = "";
        
        if (conversionRateObj != null && "CoinGecko".equals(conversionSource)) {
            // 对于AUD到USDT的直接转换，使用CoinGecko提供的直接汇率
            if (conversionRateObj instanceof String) {
                rate = (String) conversionRateObj;
            } else if (conversionRateObj instanceof Number) {
                rate = conversionRateObj.toString();
            }
            rateDescription = " (CoinGecko AUD→USDT direct)";
            log.info("Using CoinGecko AUD→USDT direct conversion rate: {}", rate);
        } else if (combinedRateObj != null) {
            // 使用CoinGecko转换的组合汇率（fallback）
            if (combinedRateObj instanceof String) {
                rate = (String) combinedRateObj;
            } else if (combinedRateObj instanceof Number) {
                rate = combinedRateObj.toString();
            }
            rateDescription = " (via CoinGecko AUD/USD)";
            log.info("Using combined AUD/USDT rate from CoinGecko + NOWPayments: {}", rate);
        } else if (rateObj != null) {
            // 使用默认汇率
            if (rateObj instanceof String) {
                rate = (String) rateObj;
            } else if (rateObj instanceof Number) {
                rate = rateObj.toString();
            }
            log.info("Using standard NOWPayments rate: {}", rate);
        }
        
        // 更新支付记录
        payment.setPaymentNo(paymentId);
        
        // 生成详细的备注信息
        StringBuilder remarkBuilder = new StringBuilder("USDT Address: " + payAddress);
        if (conversionSource != null && "CoinGecko".equals(conversionSource)) {
            remarkBuilder.append(" | Converted via CoinGecko");
            if (originalAmountObj != null && originalCurrencyObj != null) {
                remarkBuilder.append(" from ").append(originalAmountObj).append(" ").append(originalCurrencyObj);
            }
            if (exchangeRateObj != null) {
                remarkBuilder.append(" (rate: ").append(exchangeRateObj).append(")");
            }
        }
        payment.setRemark(remarkBuilder.toString());
        
        if (payAmount != null) {
            payment.setTotalcoin(new BigDecimal(payAmount));
        }
        if (rate != null) {
            payment.setRate(new BigDecimal(rate));
        }
        
        // 插入支付记录
        paymentMapper.insertPayment(payment);
        
        // 设置返回给 Telegram 的支付信息
        String paymentInfoFormat = "please use the following %s address to pay:\n\n" +
            "address：%s\n" +
            "amount：%s %s";
        
        // 如果有汇率信息，添加到支付信息中
        if (rate != null) {
            paymentInfoFormat += "\nrate：%s\norderId：%s";
            
            // 如果是CoinGecko转换，显示更详细的信息
            String rateInfo = rate + rateDescription;
            if (conversionSource != null && "CoinGecko".equals(conversionSource) && originalAmountObj != null) {
                rateInfo += "\noriginal: " + originalAmountObj + " " + originalCurrencyObj;
            }
            
            payment.setPaymentInfo(String.format(
                paymentInfoFormat + "\n\nplease click the confirm button after the payment is completed.",
                payCurrency != null ? payCurrency.toUpperCase() : "USDT",
                payAddress,
                payAmount,
                payCurrency != null ? payCurrency.toUpperCase() : "USDT",
                rateInfo,
                payment.getOrderId()
            ));
        } else {
            paymentInfoFormat += "\norderId：%s";
            payment.setPaymentInfo(String.format(
                paymentInfoFormat + "\n\nplease click the confirm button after the payment is completed.",
                payCurrency != null ? payCurrency.toUpperCase() : "USDT",
                payAddress,
                payAmount,
                payCurrency != null ? payCurrency.toUpperCase() : "USDT",
                payment.getOrderId()
            ));
        }
        
        return payment;
    }

    private Payment createBtcPayPayment(Payment payment) {
        //  create BTCPay invoice
        Map<String, Object> invoiceRequest = new HashMap<>();
        invoiceRequest.put("amount", payment.getAmount().toString());
        invoiceRequest.put("currency",payment.getCurrency());
        
        Map<String, Object> checkout = new HashMap<>();
        if (payment.getPayType().equals("0")) {
            checkout.put("paymentMethods", new String[]{"BTC"});
        } else if (payment.getPayType().equals("1")) {
            checkout.put("paymentMethods", new String[]{"USDT-TRON"});
        } else if (payment.getPayType().equals("2")) {
            checkout.put("paymentMethods", new String[]{"XMR-CHAIN"});
        } else if (payment.getPayType().equals("3")) {
            checkout.put("paymentMethods", new String[]{"ETH-CHAIN"});
        }
        checkout.put("lazyPaymentMethods", false);
        checkout.put("expirationMinutes", 60);  // 设置发票过期时间为 60 分钟（与系统监控超时时间一致）
        invoiceRequest.put("checkout", checkout);
        
        Map<String, String> metadata = new HashMap<>();
        metadata.put("orderId", payment.getOrderId());
        //metadata.put("userId", payment.getUserId().toString());
        invoiceRequest.put("metadata", metadata);

        String invoiceUrl = btcpayUrl + "/api/v1/stores/" + btcpayStoreId + "/invoices";
        log.info("Creating BTCPay invoice with URL: {}", invoiceUrl);
        log.info("Invoice request payload: {}", invoiceRequest);

        //  create invoice with retry mechanism
        Map<String, Object> invoice = BtcPayRetryUtil.retryWithBackoff(
            btcpayWebClient.post()
                .uri(invoiceUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "token " + btcpayApiKey)
                .bodyValue(invoiceRequest)
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                    response -> response.bodyToMono(String.class)
                        .flatMap(errorBody -> {
                            log.error("BTCPay API error response: {}", errorBody);
                            return Mono.error(new RuntimeException("BTCPay API error: " + errorBody));
                        }))
                .bodyToMono(Map.class)
                .doOnSuccess(response -> log.info("Invoice created successfully: {}", response))
                .doOnError(error -> log.error("Failed to create invoice: {}", error.getMessage())),
            "Create Invoice"
        ).block();
        
    
        if (invoice != null) {
            String invoiceId = (String) invoice.get("id");
            
            
            //  get onchain payment method
            
            String paymentMethodUrl = btcpayUrl + "/api/v1/stores/" + btcpayStoreId + 
                                    "/invoices/" + invoiceId + "/payment-methods";
           
            
            List<Map<String, Object>> paymentMethods = BtcPayRetryUtil.retryWithBackoff(
                btcpayWebClient.get()
                    .uri(paymentMethodUrl)
                    .header("Authorization", "token " + btcpayApiKey)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<Map<String, Object>>>() {})
                    .doOnSuccess(response -> log.info("Payment methods retrieved successfully: {}", response))
                    .doOnError(error -> log.error("Failed to get payment methods: {}", error.getMessage())),
                "Get Payment Methods"
            ).block();
            
            if (paymentMethods != null && !paymentMethods.isEmpty()) {
                // 根据支付类型查找对应的支付方法
                String expectedPaymentMethodId = getPaymentMethodId(payment.getPayType());
                String coinName = getCoinName(payment.getPayType());
                
                log.debug("Looking for payment method ID: {}", expectedPaymentMethodId);
                log.debug("Available payment methods: {}", paymentMethods.stream()
                    .map(method -> method.get("paymentMethodId"))
                    .collect(java.util.stream.Collectors.toList()));
                
                Map<String, Object> onchainPayment = paymentMethods.stream()
                    .filter(m -> expectedPaymentMethodId.equals(m.get("paymentMethodId")))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("No " + expectedPaymentMethodId + " payment method found"));
                
                //  update payment record
                payment.setPaymentNo(invoiceId);
                payment.setRemark(coinName + " Address: " + onchainPayment.get("destination"));
                
                //  set rate and amount
                if (onchainPayment.get("rate") != null) {
                    log.debug("onchainPayment rate: {}", onchainPayment.get("rate"));
                    payment.setRate(new BigDecimal(onchainPayment.get("rate").toString()));
                    log.debug("after payment rate: {}", payment.getRate());
                }
                if (onchainPayment.get("amount") != null) {
                    payment.setTotalcoin(new BigDecimal(onchainPayment.get("amount").toString()));
                }
                
                //  insert payment record
                paymentMapper.insertPayment(payment);
                
                //  set return payment info to Telegram
                payment.setPaymentInfo(String.format(
                    "please use the following %s address to pay:\n\n" +
                    "address：%s\n" +
                    "amount：%s %s\n" +
                    "rate：%s\n" +
                    "orderId：%s\n\n" +
                    "please click the confirm button after the payment is completed.",
                    coinName,
                    onchainPayment.get("destination"),
                    onchainPayment.get("amount"),
                    coinName,
                    onchainPayment.get("rate"),
                    payment.getOrderId()
                ));
                log.debug("payment info: {}", payment.getPaymentInfo());
                
                return payment;
            }
        }
        
        throw new RuntimeException("Failed to create payment invoice");
    }

    @Override
    public Payment getPaymentByOrderId(String orderId) {
        return paymentMapper.selectPaymentByOrderId(orderId);
    }

    @Override
    @Transactional
    public Payment updatePaymentStatus(Payment payment) {
        // 先查询现有支付记录
        Payment existingPayment = paymentMapper.selectPaymentByOrderId(payment.getOrderId());
        if (existingPayment == null) {
            throw new RuntimeException("Payment record not found for order: " + payment.getOrderId());
        }
        
        // 更新支付状态
        existingPayment.setPayStatus(payment.getPayStatus());
        existingPayment.setPaymentNo(payment.getPaymentNo());
        existingPayment.setPayTime(payment.getPayTime());
        existingPayment.setRemark(payment.getRemark());
        
        // 处理paidcoin字段 - 改进逻辑以正确处理outcome_amount
        if (payment.getPaidcoin() != null) {
            log.info("Processing paidcoin calculation for order: {}, original paidcoin: {}", 
                    payment.getOrderId(), payment.getPaidcoin());
            
            // 检查是否为outcome_amount（NOWPayments已计算好的最终USDT金额）
            boolean isOutcomeAmount = payment.getRemark() != null && 
                                    (payment.getRemark().contains("outcome_amount") || 
                                     payment.getRemark().contains("Outcome Price"));
            
            // ✅ 新增：检查是否为币种金额（来自 BTCPay 的部分支付检测）
            boolean isCoinAmount = payment.getRemark() != null && 
                                  payment.getRemark().contains("[COIN_AMOUNT]");
            
            if (isOutcomeAmount || isCoinAmount) {
                // outcome_amount 或币种金额已经是最终金额，直接使用，无需汇率转换
                existingPayment.setPaidcoin(payment.getPaidcoin());
                log.info("Using {} directly: {} (no rate conversion needed)", 
                        isOutcomeAmount ? "outcome_amount" : "coin amount", 
                        payment.getPaidcoin());
            } else {
                // 需要汇率转换的情况（保持原有逻辑）
                // 幂等性检查：如果paidcoin已经计算过（不是AUD金额），则跳过计算
                if (existingPayment.getPaidcoin() != null && 
                    existingPayment.getRate() != null && 
                    existingPayment.getRate().compareTo(BigDecimal.ZERO) != 0) {
                    
                    // 检查当前paidcoin是否已经是计算后的值（通过乘以汇率验证）
                    BigDecimal expectedAudAmount = existingPayment.getPaidcoin().multiply(existingPayment.getRate());
                    BigDecimal inputAudAmount = payment.getPaidcoin();
                    
                    // 如果输入的是AUD金额，且与期望的AUD金额接近，说明paidcoin已经计算过
                    if (inputAudAmount.compareTo(expectedAudAmount) == 0 || 
                        inputAudAmount.subtract(expectedAudAmount).abs().compareTo(new BigDecimal("0.01")) < 0) {
                        log.info("Paidcoin already calculated for order: {}, skipping calculation. Current: {}, Input: {}", 
                                payment.getOrderId(), existingPayment.getPaidcoin(), payment.getPaidcoin());
                        // 保持现有的paidcoin值不变
                    } else {
                        // 需要重新计算paidcoin
                        BigDecimal calculatedPaidcoin = payment.getPaidcoin().divide(existingPayment.getRate(), 8, java.math.RoundingMode.HALF_UP);
                        existingPayment.setPaidcoin(calculatedPaidcoin);
                        log.info("Recalculated paidcoin: {} / {} = {}", 
                                payment.getPaidcoin(), existingPayment.getRate(), calculatedPaidcoin);
                    }
                } else {
                    // 首次计算paidcoin
                    if (existingPayment.getRate() != null && existingPayment.getRate().compareTo(BigDecimal.ZERO) != 0) {
                        // 计算paidcoin = paidcoin / rate
                        BigDecimal calculatedPaidcoin = payment.getPaidcoin().divide(existingPayment.getRate(), 8, java.math.RoundingMode.HALF_UP);
                        existingPayment.setPaidcoin(calculatedPaidcoin);
                        log.info("Calculated paidcoin: {} / {} = {}", 
                                payment.getPaidcoin(), existingPayment.getRate(), calculatedPaidcoin);
                    } else {
                        // rate为0或NULL时，直接使用paidcoin值
                        existingPayment.setPaidcoin(payment.getPaidcoin());
                        log.info("Rate is null or zero for order: {}, using paidcoin directly: {}", 
                                payment.getOrderId(), payment.getPaidcoin());
                    }
                }
            }
        }        
        // 更新数据库
        paymentMapper.updatePayment(existingPayment);
        return existingPayment;
    }
    
    /**
     * 根据支付类型获取对应的支付方法ID
     */
    private String getPaymentMethodId(String payType) {
        switch (payType) {
            case "0": // BTC
                return "BTC-CHAIN";
            case "1": // USDT
                return "USDT-TRON";
            case "2": // XMR
                return "XMR-CHAIN";
            case "3": // ETH
                return "ETH-CHAIN";
            case "4": // LTC (manual payment, no BTCPay integration)
                return "LTC-MANUAL";
            default:
                throw new IllegalArgumentException("Unsupported payment type: " + payType);
        }
    }
    
    /**
     * 根据支付类型获取对应的币种名称
     */
    private String getCoinName(String payType) {
        switch (payType) {
            case "0": // BTC
                return "BTC";
            case "1": // USDT
                return "USDT";
            case "2": // XMR
                return "XMR";
            case "3": // ETH
                return "ETH";
            case "4": // LTC
                return "LTC";
            default:
                throw new IllegalArgumentException("Unsupported payment type: " + payType);
        }
    }
    
    @Override
    public List<Payment> getPendingPayments() {
        return paymentMapper.selectPendingPayments();
    }
    
    @Override
    public List<Payment> getExpiredPayments() {
        return paymentMapper.selectExpiredPayments();
    }
    
    @Override
    public List<Payment> getPendingNowPaymentsPayments() {
        return paymentMapper.selectPendingNowPaymentsPayments();
    }
    
    @Override
    public List<Payment> getExpiredNowPaymentsPayments() {
        return paymentMapper.selectExpiredNowPaymentsPayments();
    }
    
    private boolean useNowPaymentsForUSDT() {
        // 可以通过配置控制是否使用 NOWPayments
        return true; // 或者从配置中读取
    }

    /**
     * 验证NOWPayments支付状态并更新订单
     */
    public boolean verifyNowPaymentsPayment(String paymentId, String orderId) {
        try {
            Map<String, Object> paymentStatus = nowPaymentsService.getPaymentStatus(paymentId);
            
            String status = (String) paymentStatus.get("payment_status");
            log.info("Payment {} status: {}", paymentId, status);
            
            if ("finished".equals(status) || "confirmed".equals(status)) {
                // 支付完成，更新订单状态
                Payment payment = getPaymentByOrderId(orderId);
                if (payment != null && payment.getPayStatus() != 1) {
                    // 优先获取 outcome_amount (Outcome Price)，其次 actually_paid，最后 pay_amount
                    Object outcomeAmount = paymentStatus.get("outcome_amount");
                    Object actuallyPaid = paymentStatus.get("actually_paid");
                    Object payAmount = paymentStatus.get("pay_amount");
                    Object payCurrency = paymentStatus.get("pay_currency");
                    Object outcomeCurrency = paymentStatus.get("outcome_currency");
                    
                    Object finalAmount = null;
                    String amountSource = null;
                    Object effectiveCurrency = payCurrency;
                    
                    if (outcomeAmount != null) {
                        finalAmount = outcomeAmount;
                        amountSource = "outcome_amount (Outcome Price)";
                        if (outcomeCurrency != null) {
                            effectiveCurrency = outcomeCurrency;
                        }
                    } else if (actuallyPaid != null) {
                        finalAmount = actuallyPaid;
                        amountSource = "actually_paid";
                    } else {
                        finalAmount = payAmount;
                        amountSource = "pay_amount";
                    }
                    
                    payment.setPayStatus(1); // 已支付
                    payment.setPaymentNo(paymentId);
                    payment.setPayTime(new Date());
                    payment.setRemark("Payment completed via NOWPayments. Amount: " + finalAmount + " " + effectiveCurrency + " (source: " + amountSource + ")");
                    
                    if (finalAmount != null) {
                        payment.setPaidcoin(new BigDecimal(finalAmount.toString()));
                        log.info("Using {} field for paidcoin: {}", amountSource, finalAmount);
                    }
                    
                    updatePaymentStatus(payment);
                    
                    // 更新订单状态
                    Order order = orderService.selectOrderById(orderId);
                    if (order != null) {
                        order.setStatus(1); // 已支付
                        
                        // 确认库存扣减
                        boolean inventoryConfirmed = inventoryLockService.confirmInventory(orderId);
                        if (inventoryConfirmed) {
                            log.info("Successfully confirmed inventory for order: {}", orderId);
                        } else {
                            log.warn("Failed to confirm inventory for order: {}", orderId);
                        }
                        
                        orderService.updateOrder(order);
                        log.info("Successfully updated order status for orderId: {}", orderId);
                    }
                    
                    return true;
                }
            }
            
            return false;
            
        } catch (Exception e) {
            log.error("Error verifying NOWPayments payment: {}", paymentId, e);
            return false;
        }
    }
} 
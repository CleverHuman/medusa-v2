package com.medusa.mall.service.impl;

import com.medusa.mall.service.NowPaymentsService;
import com.medusa.mall.service.CoinGeckoService;
import com.medusa.mall.domain.order.Payment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.http.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Map;
import java.util.HashMap;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class NowPaymentsServiceImpl implements NowPaymentsService {
    private static final Logger log = LoggerFactory.getLogger(NowPaymentsServiceImpl.class);
    
    @Value("${nowpayments.api.key}")
    private String nowPaymentsApiKey;
    
    @Value("${nowpayments.api.url:https://api.nowpayments.io}")
    private String nowPaymentsApiUrl;
    
    @Value("${tor.proxy.enabled:false}")
    private boolean torProxyEnabled;
    
    // 使用带 TOR 代理的 WebClient
    @Autowired
    @Qualifier("torWebClient")
    private WebClient torWebClient;
    
    @Autowired
    private CoinGeckoService coinGeckoService;
    
    @Override
    public Map<String, Object> createPayment(Payment payment) {
        try {
            Map<String, Object> request = new HashMap<>();
            
            // 获取原始金额和币种
            BigDecimal originalAmount = payment.getAmount();
            String originalCurrency = payment.getCurrency();
            
            BigDecimal finalAmount = originalAmount;
            String finalCurrency = originalCurrency.toLowerCase();
            
            // 如果原始币种是AUD，通过CoinGecko直接转换为USDTTRC20
            if ("AUD".equalsIgnoreCase(originalCurrency)) {
                BigDecimal usdtAmount = coinGeckoService.convertAudToUsdt(originalAmount);
                finalAmount = usdtAmount;
                finalCurrency = "usdttrc20";  // 直接使用NOWPayments接受的完整标识符
                
                log.info("Currency conversion via CoinGecko API: {} {} -> {} USDT (as {})", 
                         originalAmount, originalCurrency, finalAmount, finalCurrency.toUpperCase());
                
                // 将转换信息添加到请求中以便后续处理
                request.put("original_amount", originalAmount);
                request.put("original_currency", originalCurrency);
                request.put("conversion_rate", coinGeckoService.getAudToUsdtRate());
                request.put("conversion_source", "CoinGecko");
                request.put("conversion_target", "USDTTRC20");
            }
            
            // 创建发送给NOWPayments的清洁请求，只包含必要字段
            Map<String, Object> nowPaymentsRequest = new HashMap<>();
            
            // 确保price_amount格式正确 - 保留2位小数
            BigDecimal formattedAmount = finalAmount.setScale(2, RoundingMode.HALF_UP);
            nowPaymentsRequest.put("price_amount", formattedAmount);
            nowPaymentsRequest.put("price_currency", finalCurrency.toUpperCase()); // 现在可能是USDT或其他币种
            nowPaymentsRequest.put("pay_currency", "usdttrc20"); // USDT支付 - 使用完整标识符
            nowPaymentsRequest.put("order_id", payment.getOrderId());
            nowPaymentsRequest.put("order_description", "Order payment for " + payment.getOrderId());
            
            // 移除可选参数，保持与成功测试一致
            
            log.info("Sending to NOWPayments: {}", nowPaymentsRequest);
            log.info("Full conversion context: {}", request);
            
            Map<String, Object> response;
            try {
                // 使用带 TOR 代理的 WebClient
                log.info("Sending NOWPayments request via TOR proxy (enabled: {})", torProxyEnabled);
                response = torWebClient.post()
                    .uri(nowPaymentsApiUrl + "/v1/payment")
                    .header("x-api-key", nowPaymentsApiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(nowPaymentsRequest)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
            } catch (org.springframework.web.reactive.function.client.WebClientResponseException e) {
                log.error("NOWPayments API error - Status: {}, Body: {}", e.getStatusCode(), e.getResponseBodyAsString());
                log.error("NOWPayments request that failed: {}", nowPaymentsRequest);
                log.error("Full conversion context: {}", request);
                
                // 分析常见错误
                String errorBody = e.getResponseBodyAsString();
                
                // 记录具体的错误信息以便诊断
                log.error("Detailed error analysis: {}", errorBody);
                
                if (errorBody.contains("price_amount")) {
                    log.error("Possible issue with price_amount format: {}", formattedAmount);
                }
                if (errorBody.contains("price_currency")) {
                    log.error("Possible issue with price_currency: {}", finalCurrency.toUpperCase());
                }
                if (errorBody.contains("pay_currency")) {
                    log.error("Possible issue with pay_currency: usdttrc20");
                }
                if (errorBody.contains("order_id")) {
                    log.error("Possible issue with order_id format: {}", payment.getOrderId());
                }
                
                throw new RuntimeException("NOWPayments API error: " + e.getStatusCode() + " - " + e.getResponseBodyAsString(), e);
            }
                
            log.info("NOWPayments payment created: {}", response);
            
            // 添加汇率信息到响应中
            if (response != null) {
                Object priceAmountObj = response.get("price_amount");
                Object payAmountObj = response.get("pay_amount");
                
                // 如果使用了汇率转换，添加相关信息到响应中
                if (request.containsKey("conversion_source")) {
                    response.put("original_amount", request.get("original_amount"));
                    response.put("original_currency", request.get("original_currency"));
                    response.put("conversion_rate", request.get("conversion_rate")); // 直接传递CoinGecko AUD→USDT汇率
                    response.put("exchange_rate", request.get("conversion_rate"));
                    response.put("conversion_source", request.get("conversion_source"));
                    log.info("Added CoinGecko conversion info to response with direct AUD→USDT rate: {}", 
                             request.get("conversion_rate"));
                }
                
                if (priceAmountObj != null && payAmountObj != null) {
                    try {
                        double priceAmount = 0.0;
                        double payAmount = 0.0;
                        
                        // 安全地转换price_amount
                        if (priceAmountObj instanceof Number) {
                            priceAmount = ((Number) priceAmountObj).doubleValue();
                        } else if (priceAmountObj instanceof String) {
                            priceAmount = Double.parseDouble((String) priceAmountObj);
                        }
                        
                        // 安全地转换pay_amount (注意：创建支付时使用pay_amount，状态检查时优先使用actually_paid)
                        if (payAmountObj instanceof Number) {
                            payAmount = ((Number) payAmountObj).doubleValue();
                        } else if (payAmountObj instanceof String) {
                            payAmount = Double.parseDouble((String) payAmountObj);
                        }
                        
                        // 计算NOWPayments的USD到USDT汇率
                        if (payAmount > 0) {
                            double nowpaymentsRate = priceAmount / payAmount;
                            response.put("nowpayments_usd_usdt_rate", nowpaymentsRate);
                            
                            // 如果使用了汇率转换，计算完整的AUD到USDT汇率
                            if (request.containsKey("conversion_rate")) {
                                BigDecimal exchangeRate = (BigDecimal) request.get("conversion_rate");
                                double combinedRate = exchangeRate.doubleValue() * nowpaymentsRate;
                                response.put("combined_aud_usdt_rate", combinedRate);
                                log.info("Combined rate (CoinGecko AUD/USD: {} × NOWPayments USD/USDT: {} = AUD/USDT: {})", 
                                         exchangeRate, nowpaymentsRate, combinedRate);
                            } else {
                                response.put("rate", nowpaymentsRate);
                                log.info("NOWPayments USD/USDT rate: {} ({} / {})", nowpaymentsRate, priceAmount, payAmount);
                            }
                        }
                    } catch (Exception e) {
                        log.warn("Failed to calculate rate: {}", e.getMessage());
                    }
                }
            }
            
            return response;
            
        } catch (Exception e) {
            log.error("Error creating NOWPayments payment", e);
            throw new RuntimeException("Failed to create NOWPayments payment", e);
        }
    }
    
    @Override
    public Map<String, Object> getPaymentStatus(String paymentId) {
        try {
            // 使用带 TOR 代理的 WebClient
            Map<String, Object> response = torWebClient.get()
                .uri(nowPaymentsApiUrl + "/v1/payment/" + paymentId)
                .header("x-api-key", nowPaymentsApiKey)
                .retrieve()
                .bodyToMono(Map.class)
                .block();
                
            log.info("NOWPayments payment status via TOR: {}", response);
            return response;
            
        } catch (Exception e) {
            log.error("Error getting NOWPayments payment status via TOR", e);
            throw new RuntimeException("Failed to get NOWPayments payment status", e);
        }
    }
    
    @Override
    public Map<String, Object> getPaymentHistory(String paymentId) {
        try {
            // 使用带 TOR 代理的 WebClient
            Map<String, Object> response = torWebClient.get()
                .uri(nowPaymentsApiUrl + "/v1/payment/" + paymentId + "/history")
                .header("x-api-key", nowPaymentsApiKey)
                .retrieve()
                .bodyToMono(Map.class)
                .block();
                
            log.info("NOWPayments payment history via TOR: {}", response);
            return response;
            
        } catch (Exception e) {
            log.error("Error getting NOWPayments payment history via TOR", e);
            throw new RuntimeException("Failed to get NOWPayments payment history", e);
        }
    }
} 
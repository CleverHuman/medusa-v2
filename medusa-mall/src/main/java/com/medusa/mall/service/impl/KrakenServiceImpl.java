package com.medusa.mall.service.impl;

import com.medusa.mall.service.CoinGeckoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

/**
 * Kraken Exchange Rate Service Implementation
 * Uses Kraken Public API to fetch exchange rates
 * 
 * @author medusa
 * @date 2025-01-14
 */
@Service("krakenService")
@ConditionalOnProperty(name = "exchange-rate.provider", havingValue = "kraken", matchIfMissing = false)
public class KrakenServiceImpl implements CoinGeckoService {
    private static final Logger log = LoggerFactory.getLogger(KrakenServiceImpl.class);
    
    @Value("${exchange-rate.kraken.api.url:https://api.kraken.com/0/public}")
    private String krakenApiUrl;
    
    @Value("${exchange-rate.kraken.cache.duration:300000}")
    private long cacheDuration;
    
    @Value("${exchange-rate.fallback.aud-usd-rate:0.65}")
    private BigDecimal fallbackAudUsdRate;
    
    @Value("${exchange-rate.fallback.aud-usdt-rate:0.65}")
    private BigDecimal fallbackAudUsdtRate;
    
    @Autowired
    private WebClient webClient;
    
    // 汇率缓存（分别缓存 USD 和 USDT）
    private BigDecimal cachedAudUsdRate = null;
    private BigDecimal cachedAudUsdtRate = null;
    private long lastUsdUpdateTime = 0;
    private long lastUsdtUpdateTime = 0;
    
    @Override
    public BigDecimal getAudToUsdRate() {
        try {
            // 检查缓存是否有效
            long currentTime = System.currentTimeMillis();
            if (cachedAudUsdRate != null && (currentTime - lastUsdUpdateTime) < cacheDuration) {
                log.debug("Using cached AUD/USD rate from Kraken: {}", cachedAudUsdRate);
                return cachedAudUsdRate;
            }
            
            log.info("Fetching AUD/USD rate from Kraken API");
            
            // Kraken API: GET /0/public/Ticker?pair=AUDUSD
            Map<String, Object> response = webClient.get()
                .uri(krakenApiUrl + "/Ticker?pair=AUDUSD")
                .retrieve()
                .bodyToMono(Map.class)
                .block();
            
            log.debug("Kraken API response for AUD/USD: {}", response);
            
            if (response != null && response.containsKey("result")) {
                Map<String, Object> result = (Map<String, Object>) response.get("result");
                
                // Kraken 可能返回不同的交易对名称，如 "AUDUSD" 或 "ZAUDUSD"
                for (String key : result.keySet()) {
                    if (key.toUpperCase().contains("AUD") && key.toUpperCase().contains("USD") 
                        && !key.toUpperCase().contains("USDT")) {
                        Map<String, Object> tickerData = (Map<String, Object>) result.get(key);
                        
                        // Kraken 返回的数据格式：
                        // "c": ["0.65123", "1000.00"] - last trade closed [price, lot volume]
                        if (tickerData.containsKey("c")) {
                            List<String> lastTrade = (List<String>) tickerData.get("c");
                            if (lastTrade != null && !lastTrade.isEmpty()) {
                                BigDecimal rate = new BigDecimal(lastTrade.get(0));
                                
                                if (rate.compareTo(BigDecimal.ZERO) > 0) {
                                    cachedAudUsdRate = rate;
                                    lastUsdUpdateTime = currentTime;
                                    
                                    log.info("Successfully fetched AUD/USD rate from Kraken: {} (pair: {})", rate, key);
                                    return rate;
                                }
                            }
                        }
                    }
                }
            }
            
            log.error("Invalid response from Kraken API: {}", response);
            
            // 使用缓存或回退值
            if (cachedAudUsdRate != null) {
                log.warn("Failed to fetch fresh rate, using cached AUD/USD rate: {}", cachedAudUsdRate);
                return cachedAudUsdRate;
            }
            
            log.error("Failed to fetch AUD/USD rate from Kraken, using fallback rate: {}", fallbackAudUsdRate);
            return fallbackAudUsdRate;
            
        } catch (Exception e) {
            log.error("Error fetching AUD/USD rate from Kraken API", e);
            
            if (cachedAudUsdRate != null) {
                log.warn("Using cached AUD/USD rate due to error: {}", cachedAudUsdRate);
                return cachedAudUsdRate;
            }
            
            log.error("Using fallback AUD/USD rate due to error: {}", fallbackAudUsdRate);
            return fallbackAudUsdRate;
        }
    }
    
    @Override
    public BigDecimal getAudToUsdtRate() {
        try {
            // 检查缓存是否有效
            long currentTime = System.currentTimeMillis();
            if (cachedAudUsdtRate != null && (currentTime - lastUsdtUpdateTime) < cacheDuration) {
                log.debug("Using cached AUD/USDT rate from Kraken: {}", cachedAudUsdtRate);
                return cachedAudUsdtRate;
            }
            
            log.info("Fetching AUD/USDT rate from Kraken API");
            
            // Kraken API: GET /0/public/Ticker?pair=AUDUSDT
            // 注意：Kraken 可能使用 USDTZUSD 表示 USDT/USD
            Map<String, Object> response = webClient.get()
                .uri(krakenApiUrl + "/Ticker?pair=AUDUSDT")
                .retrieve()
                .bodyToMono(Map.class)
                .block();
            
            log.debug("Kraken API response for AUD/USDT: {}", response);
            
            if (response != null && response.containsKey("result")) {
                Map<String, Object> result = (Map<String, Object>) response.get("result");
                
                for (String key : result.keySet()) {
                    if (key.toUpperCase().contains("AUD") && key.toUpperCase().contains("USDT")) {
                        Map<String, Object> tickerData = (Map<String, Object>) result.get(key);
                        
                        if (tickerData.containsKey("c")) {
                            List<String> lastTrade = (List<String>) tickerData.get("c");
                            if (lastTrade != null && !lastTrade.isEmpty()) {
                                BigDecimal rate = new BigDecimal(lastTrade.get(0));
                                
                                if (rate.compareTo(BigDecimal.ZERO) > 0) {
                                    cachedAudUsdtRate = rate;
                                    lastUsdtUpdateTime = currentTime;
                                    
                                    log.info("Successfully fetched AUD/USDT rate from Kraken: {} (pair: {})", rate, key);
                                    return rate;
                                }
                            }
                        }
                    }
                }
            }
            
            log.warn("Could not find AUD/USDT pair in Kraken response, trying fallback method");
            
            // 如果 Kraken 没有直接的 AUD/USDT 交易对，尝试使用 USD 汇率（USDT ≈ USD）
            if (cachedAudUsdtRate != null) {
                log.warn("Failed to fetch fresh USDT rate, using cached: {}", cachedAudUsdtRate);
                return cachedAudUsdtRate;
            }
            
            // USDT 和 USD 汇率非常接近，可以作为回退
            log.warn("Failed to fetch AUD/USDT rate from Kraken, using AUD/USD rate as approximation");
            BigDecimal audUsdRate = getAudToUsdRate();
            
            // 缓存这个近似值
            cachedAudUsdtRate = audUsdRate;
            lastUsdtUpdateTime = currentTime;
            
            return audUsdRate;
            
        } catch (Exception e) {
            log.error("Error fetching AUD/USDT rate from Kraken API", e);
            
            if (cachedAudUsdtRate != null) {
                log.warn("Using cached AUD/USDT rate due to error: {}", cachedAudUsdtRate);
                return cachedAudUsdtRate;
            }
            
            // 使用 USD 汇率作为回退
            log.warn("Using AUD/USD rate as fallback for USDT");
            return getAudToUsdRate();
        }
    }
    
    @Override
    public BigDecimal convertAudToUsd(BigDecimal audAmount) {
        if (audAmount == null || audAmount.compareTo(BigDecimal.ZERO) <= 0) {
            log.warn("Invalid AUD amount: {}", audAmount);
            return BigDecimal.ZERO;
        }
        
        BigDecimal rate = getAudToUsdRate();
        BigDecimal usdAmount = audAmount.multiply(rate).setScale(2, RoundingMode.HALF_UP);
        
        log.info("Converted {} AUD to {} USD using Kraken rate {}", audAmount, usdAmount, rate);
        return usdAmount;
    }
    
    @Override
    public BigDecimal convertAudToUsdt(BigDecimal audAmount) {
        if (audAmount == null || audAmount.compareTo(BigDecimal.ZERO) <= 0) {
            log.warn("Invalid AUD amount: {}", audAmount);
            return BigDecimal.ZERO;
        }
        
        BigDecimal rate = getAudToUsdtRate();
        BigDecimal usdtAmount = audAmount.multiply(rate).setScale(2, RoundingMode.HALF_UP);
        
        log.info("Converted {} AUD to {} USDT using Kraken rate {}", audAmount, usdtAmount, rate);
        return usdtAmount;
    }
}


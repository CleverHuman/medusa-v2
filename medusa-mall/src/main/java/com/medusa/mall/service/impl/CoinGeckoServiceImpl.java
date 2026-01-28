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
import java.util.Map;

@Service("coinGeckoService")
@ConditionalOnProperty(name = "exchange-rate.provider", havingValue = "coingecko", matchIfMissing = false)
public class CoinGeckoServiceImpl implements CoinGeckoService {
    private static final Logger log = LoggerFactory.getLogger(CoinGeckoServiceImpl.class);
    
    @Value("${exchange-rate.coingecko.api.url:${coingecko.api.url:https://api.coingecko.com/api/v3}}")
    private String coinGeckoApiUrl;
    
    @Value("${exchange-rate.coingecko.cache.duration:${coingecko.cache.duration:300000}}")
    private long cacheDuration;
    
    @Value("${exchange-rate.fallback.aud-usd-rate:${coingecko.fallback.aud-usd-rate:0.65}}")
    private BigDecimal fallbackAudUsdRate;
    
    @Autowired
    private WebClient webClient;
    
    // 汇率缓存（简单缓存，实际使用中可以考虑Redis或其他缓存方案）
    private BigDecimal cachedRate = null;
    private long lastUpdateTime = 0;
    
    @Override
    public BigDecimal getAudToUsdRate() {
        try {
            // 检查缓存是否有效
            long currentTime = System.currentTimeMillis();
            if (cachedRate != null && (currentTime - lastUpdateTime) < cacheDuration) {
                log.debug("Using cached AUD/USD rate: {}", cachedRate);
                return cachedRate;
            }
            
            log.info("Fetching AUD/USD rate from CoinGecko API");
            
            // 使用CoinGecko API获取USD到AUD的汇率，然后计算倒数得到AUD到USD汇率
            // 参考: curl -s "https://api.coingecko.com/api/v3/simple/price?ids=usd&vs_currencies=aud" | jq '.usd.aud | 1 / .'
            Map<String, Object> response = webClient.get()
                .uri(coinGeckoApiUrl + "/simple/price?ids=usd&vs_currencies=aud")
                .retrieve()
                .bodyToMono(Map.class)
                .block();
            
            log.debug("CoinGecko API response: {}", response);
            
            if (response != null && response.containsKey("usd")) {
                Map<String, Object> usdData = (Map<String, Object>) response.get("usd");
                if (usdData != null && usdData.containsKey("aud")) {
                    Object usdToAudObj = usdData.get("aud");
                    
                    BigDecimal audToUsdRate = null;
                    if (usdToAudObj instanceof Number) {
                        // CoinGecko返回的是USD到AUD的汇率，我们需要计算倒数得到AUD到USD
                        double usdToAudRate = ((Number) usdToAudObj).doubleValue();
                        if (usdToAudRate > 0) {
                            audToUsdRate = BigDecimal.ONE.divide(new BigDecimal(usdToAudRate), 6, RoundingMode.HALF_UP);
                        }
                    } else if (usdToAudObj instanceof String) {
                        double usdToAudRate = Double.parseDouble((String) usdToAudObj);
                        if (usdToAudRate > 0) {
                            audToUsdRate = BigDecimal.ONE.divide(new BigDecimal(usdToAudRate), 6, RoundingMode.HALF_UP);
                        }
                    }
                    
                    if (audToUsdRate != null && audToUsdRate.compareTo(BigDecimal.ZERO) > 0) {
                        // 缓存汇率
                        cachedRate = audToUsdRate;
                        lastUpdateTime = currentTime;
                        
                        log.info("Successfully fetched AUD/USD rate from CoinGecko: {} (calculated from USD/AUD: {})", 
                                audToUsdRate, usdToAudObj);
                        return audToUsdRate;
                    }
                }
            }
            
            log.error("Invalid response from CoinGecko API: {}", response);
            
            // 如果获取失败但有缓存，使用缓存值
            if (cachedRate != null) {
                log.warn("Failed to fetch fresh rate, using cached AUD/USD rate: {}", cachedRate);
                return cachedRate;
            }
            
            // 如果完全失败，使用配置的默认汇率
            log.error("Failed to fetch AUD/USD rate, using configured fallback rate: {}", fallbackAudUsdRate);
            return fallbackAudUsdRate;
            
        } catch (Exception e) {
            log.error("Error fetching AUD/USD rate from CoinGecko API", e);
            
            // 如果有缓存，使用缓存
            if (cachedRate != null) {
                log.warn("Using cached AUD/USD rate due to error: {}", cachedRate);
                return cachedRate;
            }
            
            // 否则使用配置的默认值
            log.error("Using configured fallback AUD/USD rate due to error: {}", fallbackAudUsdRate);
            return fallbackAudUsdRate;
        }
    }
    
    @Override
    public BigDecimal getAudToUsdtRate() {
        try {
            // 检查缓存是否有效
            long currentTime = System.currentTimeMillis();
            if (cachedRate != null && (currentTime - lastUpdateTime) < cacheDuration) {
                log.debug("Using cached AUD/USDT rate: {}", cachedRate);
                return cachedRate;
            }
            
            log.info("Fetching AUD/USDT rate from CoinGecko API");
            
            // 使用CoinGecko API获取USDT到AUD的汇率，然后计算倒数得到AUD到USDT汇率
            // API: https://api.coingecko.com/api/v3/simple/price?ids=tether&vs_currencies=aud
            Map<String, Object> response = webClient.get()
                .uri(coinGeckoApiUrl + "/simple/price?ids=tether&vs_currencies=aud")
                .retrieve()
                .bodyToMono(Map.class)
                .block();
            
            log.debug("CoinGecko USDT/AUD response: {}", response);
            
            if (response != null && response.containsKey("tether")) {
                Map<String, Object> tetherData = (Map<String, Object>) response.get("tether");
                if (tetherData != null && tetherData.containsKey("aud")) {
                    Object usdtToAudObj = tetherData.get("aud");
                    
                    BigDecimal audToUsdtRate = null;
                    if (usdtToAudObj instanceof Number) {
                        // CoinGecko返回的是USDT到AUD的汇率，我们需要计算倒数得到AUD到USDT
                        double usdtToAudRate = ((Number) usdtToAudObj).doubleValue();
                        if (usdtToAudRate > 0) {
                            audToUsdtRate = BigDecimal.ONE.divide(new BigDecimal(usdtToAudRate), 6, RoundingMode.HALF_UP);
                        }
                    } else if (usdtToAudObj instanceof String) {
                        double usdtToAudRate = Double.parseDouble((String) usdtToAudObj);
                        if (usdtToAudRate > 0) {
                            audToUsdtRate = BigDecimal.ONE.divide(new BigDecimal(usdtToAudRate), 6, RoundingMode.HALF_UP);
                        }
                    }
                    
                    if (audToUsdtRate != null && audToUsdtRate.compareTo(BigDecimal.ZERO) > 0) {
                        // 缓存汇率
                        cachedRate = audToUsdtRate;
                        lastUpdateTime = currentTime;
                        
                        log.info("Successfully fetched AUD/USDT rate from CoinGecko: {} (calculated from USDT/AUD: {})", 
                                audToUsdtRate, usdtToAudObj);
                        return audToUsdtRate;
                    }
                }
            }
            
            log.error("Invalid response from CoinGecko USDT API: {}", response);
            
            // 如果获取失败但有缓存，使用缓存值
            if (cachedRate != null) {
                log.warn("Failed to fetch fresh USDT rate, using cached AUD/USDT rate: {}", cachedRate);
                return cachedRate;
            }
            
            // 如果完全失败，使用配置的默认汇率
            log.error("Failed to fetch AUD/USDT rate, using configured fallback rate: {}", fallbackAudUsdRate);
            return fallbackAudUsdRate;
            
        } catch (Exception e) {
            log.error("Error fetching AUD/USDT rate from CoinGecko API", e);
            
            // 如果有缓存，使用缓存
            if (cachedRate != null) {
                log.warn("Using cached AUD/USDT rate due to error: {}", cachedRate);
                return cachedRate;
            }
            
            // 否则使用配置的默认值
            log.error("Using configured fallback AUD/USDT rate due to error: {}", fallbackAudUsdRate);
            return fallbackAudUsdRate;
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
        
        log.info("Converted {} AUD to {} USD using rate {}", audAmount, usdAmount, rate);
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
        
        log.info("Converted {} AUD to {} USDT using rate {}", audAmount, usdtAmount, rate);
        return usdtAmount;
    }
}

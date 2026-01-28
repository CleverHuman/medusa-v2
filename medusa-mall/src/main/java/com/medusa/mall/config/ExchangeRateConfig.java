package com.medusa.mall.config;

import com.medusa.mall.service.CoinGeckoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.Map;

/**
 * Exchange Rate Service Configuration
 * Automatically selects the appropriate exchange rate provider based on configuration
 * 
 * @author medusa
 * @date 2025-01-14
 */
@Configuration
public class ExchangeRateConfig {
    private static final Logger log = LoggerFactory.getLogger(ExchangeRateConfig.class);
    
    @Value("${exchange-rate.provider:kraken}")
    private String provider;
    
    @Autowired(required = false)
    private Map<String, CoinGeckoService> exchangeRateServices;
    
    /**
     * 提供主要的汇率服务 Bean
     * 根据配置自动选择 Kraken 或 CoinGecko
     */
    @Bean
    @Primary
    public CoinGeckoService exchangeRateService() {
        if (exchangeRateServices == null || exchangeRateServices.isEmpty()) {
            throw new IllegalStateException("No exchange rate service implementation found. Please check your configuration.");
        }
        
        String serviceName = provider + "Service";
        CoinGeckoService service = exchangeRateServices.get(serviceName);
        
        if (service != null) {
            log.info("✓ Using {} exchange rate service", provider.toUpperCase());
            return service;
        }
        
        // 如果指定的服务不存在，尝试使用任何可用的服务
        log.warn("Configured provider '{}' not found, using first available service", provider);
        service = exchangeRateServices.values().iterator().next();
        log.info("✓ Fallback to {} exchange rate service", service.getClass().getSimpleName());
        
        return service;
    }
}


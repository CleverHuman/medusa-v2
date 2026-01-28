package com.medusa.mall.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "btcpay")
public class BtcPayProperties {
    private String url;
    private String apiKey;
    private String storeId;
    private String webhookSecret;
    private Webhook webhook = new Webhook(); // 新增webhook配置
    
    // Webhook配置内部类
    public static class Webhook {
        private boolean enabled = true; // 默认启用，向后兼容
        
        public boolean isEnabled() {
            return enabled;
        }
        
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }

    // Getters and Setters
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getStoreId() {
        return storeId;
    }

    public void setStoreId(String storeId) {
        this.storeId = storeId;
    }

    public String getWebhookSecret() {
        return webhookSecret;
    }

    public void setWebhookSecret(String webhookSecret) {
        this.webhookSecret = webhookSecret;
    }
    
    public Webhook getWebhook() {
        return webhook;
    }
    
    public void setWebhook(Webhook webhook) {
        this.webhook = webhook;
    }
} 
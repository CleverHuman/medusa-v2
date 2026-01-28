package com.medusa.mall.service.impl;

import com.medusa.mall.domain.TgPolicyConfig;
import com.medusa.mall.service.ITgPolicyConfigService;
import com.medusa.mall.mapper.TgPolicyConfigMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TgPolicyConfigServiceImpl implements ITgPolicyConfigService {
    
    @Autowired
    private TgPolicyConfigMapper tgPolicyConfigMapper;

    @Override
    public TgPolicyConfig getConfig() {
        try {
            TgPolicyConfig config = tgPolicyConfigMapper.selectConfig();
            if (config == null) {
                // 返回默认配置
                config = new TgPolicyConfig();
                config.setPrivacyPolicy("We respect your privacy and are committed to protecting your personal data. This privacy policy explains how we collect, use, and safeguard your information when you use our services.");
                config.setTermsOfService("By using our services, you agree to these terms and conditions. Please read them carefully before making any purchases.");
                config.setRefundPolicy("We offer refunds within 7 days of purchase for digital products. Physical products may be returned within 14 days in original condition.");
                config.setShippingPolicy("We ship worldwide with discreet packaging. Shipping times vary by location, typically 7-21 business days.");
                config.setContactInfo("For support, please contact us at support@yourdomain.com or through our Telegram bot.");
            }
            return config;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public int saveConfig(TgPolicyConfig config) {
        try {
            TgPolicyConfig existingConfig = tgPolicyConfigMapper.selectConfig();
            if (existingConfig != null) {
                // 如果配置已存在，则更新
                config.setId(existingConfig.getId());
                return tgPolicyConfigMapper.updateConfig(config);
            } else {
                // 如果配置不存在，则插入
                return tgPolicyConfigMapper.insertConfig(config);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public int updateConfig(TgPolicyConfig config) {
        try {
            return tgPolicyConfigMapper.updateConfig(config);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
} 
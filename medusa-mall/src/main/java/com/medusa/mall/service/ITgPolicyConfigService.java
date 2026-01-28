package com.medusa.mall.service;

import com.medusa.mall.domain.TgPolicyConfig;

/**
 * Telegram Policy配置Service接口
 */
public interface ITgPolicyConfigService {
    
    /**
     * 获取Telegram Policy配置
     */
    TgPolicyConfig getConfig();
    
    /**
     * 保存Telegram Policy配置
     */
    int saveConfig(TgPolicyConfig config);
    
    /**
     * 更新Telegram Policy配置
     */
    int updateConfig(TgPolicyConfig config);
} 
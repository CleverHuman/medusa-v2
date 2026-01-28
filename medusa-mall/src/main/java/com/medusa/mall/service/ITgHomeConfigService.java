package com.medusa.mall.service;

import com.medusa.mall.domain.TgHomeConfig;

/**
 * Telegram首页配置Service接口
 */
public interface ITgHomeConfigService {
    
    /**
     * 获取Telegram首页配置
     */
    TgHomeConfig getConfig();
    
    /**
     * 保存Telegram首页配置
     */
    int saveConfig(TgHomeConfig config);
    
    /**
     * 更新Telegram首页配置
     */
    int updateConfig(TgHomeConfig config);
} 
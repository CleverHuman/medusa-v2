package com.medusa.mall.service.impl;

import com.medusa.mall.domain.TgHomeConfig;
import com.medusa.mall.service.ITgHomeConfigService;
import com.medusa.mall.mapper.TgHomeConfigMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.ArrayList;

@Service
public class TgHomeConfigServiceImpl implements ITgHomeConfigService {
    
    @Autowired
    private TgHomeConfigMapper tgHomeConfigMapper;
    
    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public TgHomeConfig getConfig() {
        try {
            TgHomeConfig config = tgHomeConfigMapper.selectConfig();
            if (config == null) {
                // 返回默认配置
                config = new TgHomeConfig();
                config.setBannerImage("https://yourdomain.com/profile/upload/2025/07/26/home_banner.jpg");
                config.setTitle("Welcome to Telegram Store");
                config.setDescription("We support BTC / USDT / XMR payment, global anonymous direct shipping!");
                
                List<TgHomeConfig.TgHomeButton> buttons = new ArrayList<>();
                TgHomeConfig.TgHomeButton button1 = new TgHomeConfig.TgHomeButton();
                button1.setLabel("🛍 Browse Products");
                button1.setAction("browse_products");
                buttons.add(button1);
                
                TgHomeConfig.TgHomeButton button2 = new TgHomeConfig.TgHomeButton();
                button2.setLabel("📦 My Orders");
                button2.setAction("view_orders");
                buttons.add(button2);
                
                TgHomeConfig.TgHomeButton button3 = new TgHomeConfig.TgHomeButton();
                button3.setLabel("🎁 Limited Offers");
                button3.setAction("promo");
                buttons.add(button3);
                
                config.setButtons(buttons);
            }
            return config;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public int saveConfig(TgHomeConfig config) {
        try {
            TgHomeConfig existingConfig = tgHomeConfigMapper.selectConfig();
            if (existingConfig != null) {
                // 如果配置已存在，则更新
                config.setId(existingConfig.getId());
                return tgHomeConfigMapper.updateConfig(config);
            } else {
                // 如果配置不存在，则插入
                return tgHomeConfigMapper.insertConfig(config);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public int updateConfig(TgHomeConfig config) {
        try {
            return tgHomeConfigMapper.updateConfig(config);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
} 
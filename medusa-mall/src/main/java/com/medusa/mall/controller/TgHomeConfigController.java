package com.medusa.mall.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.medusa.common.annotation.Log;
import com.medusa.common.core.controller.BaseController;
import com.medusa.common.core.domain.AjaxResult;
import com.medusa.common.enums.BusinessType;
import com.medusa.mall.domain.TgHomeConfig;
import com.medusa.mall.service.ITgHomeConfigService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Telegram首页配置Controller
 */
@RestController
@RequestMapping("/admin/mall/tg-home-config")
public class TgHomeConfigController extends BaseController {
    
    @Autowired
    private ITgHomeConfigService tgHomeConfigService;

    /**
     * 获取Telegram首页配置
     */
    @PreAuthorize("@ss.hasPermi('mall:tg-home-config:query')")
    @GetMapping
    public AjaxResult getConfig() {
        TgHomeConfig config = tgHomeConfigService.getConfig();
        return success(config);
    }

    /**
     * 保存Telegram首页配置
     */
    @PreAuthorize("@ss.hasPermi('mall:tg-home-config:add')")
    @Log(title = "Telegram首页配置", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult saveConfig(@RequestBody TgHomeConfig config) {
        return toAjax(tgHomeConfigService.saveConfig(config));
    }

    /**
     * 更新Telegram首页配置
     */
    @PreAuthorize("@ss.hasPermi('mall:tg-home-config:edit')")
    @Log(title = "Telegram首页配置", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult updateConfig(@RequestBody TgHomeConfig config) {
        return toAjax(tgHomeConfigService.updateConfig(config));
    }
} 
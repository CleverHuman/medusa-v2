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
import com.medusa.mall.domain.TgPolicyConfig;
import com.medusa.mall.service.ITgPolicyConfigService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Telegram Policy配置Controller
 */
@RestController
@RequestMapping("/admin/mall/tg-policy-config")
public class TgPolicyConfigController extends BaseController {
    
    @Autowired
    private ITgPolicyConfigService tgPolicyConfigService;

    /**
     * 获取Telegram Policy配置
     */
    @PreAuthorize("@ss.hasPermi('mall:tg-policy-config:query')")
    @GetMapping
    public AjaxResult getConfig() {
        TgPolicyConfig config = tgPolicyConfigService.getConfig();
        return success(config);
    }

    /**
     * 保存Telegram Policy配置
     */
    @PreAuthorize("@ss.hasPermi('mall:tg-policy-config:add')")
    @Log(title = "Telegram Policy配置", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult saveConfig(@RequestBody TgPolicyConfig config) {
        return toAjax(tgPolicyConfigService.saveConfig(config));
    }

    /**
     * 更新Telegram Policy配置
     */
    @PreAuthorize("@ss.hasPermi('mall:tg-policy-config:edit')")
    @Log(title = "Telegram Policy配置", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult updateConfig(@RequestBody TgPolicyConfig config) {
        return toAjax(tgPolicyConfigService.updateConfig(config));
    }
} 
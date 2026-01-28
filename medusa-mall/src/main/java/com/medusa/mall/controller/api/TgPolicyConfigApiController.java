package com.medusa.mall.controller.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.medusa.common.core.domain.AjaxResult;
import com.medusa.mall.domain.TgPolicyConfig;
import com.medusa.mall.service.ITgPolicyConfigService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Telegram Policy配置公开API Controller
 * 供Telegram Bot等客户端使用
 */
@RestController
@RequestMapping("/api/mall/tg-policy-config")
public class TgPolicyConfigApiController {
    
    @Autowired
    private ITgPolicyConfigService tgPolicyConfigService;

    /**
     * 获取Telegram Policy配置
     * 公开接口，无需权限验证
     */
    @GetMapping
    public AjaxResult getConfig() {
        TgPolicyConfig config = tgPolicyConfigService.getConfig();
        return AjaxResult.success(config);
    }
} 
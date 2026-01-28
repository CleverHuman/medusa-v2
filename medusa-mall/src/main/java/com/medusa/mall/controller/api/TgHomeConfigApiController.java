package com.medusa.mall.controller.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.medusa.common.annotation.Anonymous;
import com.medusa.common.core.domain.AjaxResult;
import com.medusa.mall.domain.TgHomeConfig;
import com.medusa.mall.service.ITgHomeConfigService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Telegram首页配置公开API Controller
 * 供Telegram Bot等客户端使用
 */
@RestController
@RequestMapping("/api/mall/tg-home-config")
public class TgHomeConfigApiController {
    
    @Autowired
    private ITgHomeConfigService tgHomeConfigService;

    /**
     * 获取Telegram首页配置
     * 公开接口，无需权限验证
     * 添加@Anonymous注解以支持临时用户访问
     */
    @Anonymous
    @GetMapping
    public AjaxResult getConfig() {
        TgHomeConfig config = tgHomeConfigService.getConfig();
        return AjaxResult.success(config);
    }
} 
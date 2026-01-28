package com.medusa.mall.controller.api;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.medusa.common.annotation.Anonymous;
import com.medusa.common.core.controller.BaseController;
import com.medusa.common.core.domain.AjaxResult;
import com.medusa.mall.domain.vendor.VendorBotKeyword;
import com.medusa.mall.service.IVendorBotKeywordService;

/**
 * Vendor Bot Keyword API Controller
 */
@RestController
@RequestMapping("/api/mall/vendor-bot")
public class ApiVendorBotKeywordController extends BaseController {
    
    @Autowired
    private IVendorBotKeywordService vendorBotKeywordService;

    /**
     * Get keywords (global if vendorId is null, vendor-specific otherwise)
     */
    @Anonymous
    @GetMapping("/keywords")
    public AjaxResult getKeywords(@RequestParam(required = false) Long vendorId) {
        VendorBotKeyword query = new VendorBotKeyword();
        query.setVendorId(vendorId);
        query.setIsActive(1); // Only active keywords
        List<VendorBotKeyword> keywords = vendorBotKeywordService.selectVendorBotKeywordList(query);
        return AjaxResult.success(keywords);
    }

    /**
     * Add keyword
     */
    @Anonymous
    @PostMapping("/keywords")
    public AjaxResult addKeyword(@RequestBody VendorBotKeyword keyword) {
        try {
            int result = vendorBotKeywordService.insertVendorBotKeyword(keyword);
            return toAjax(result);
        } catch (Exception e) {
            return AjaxResult.error("Failed to add keyword: " + e.getMessage());
        }
    }
}

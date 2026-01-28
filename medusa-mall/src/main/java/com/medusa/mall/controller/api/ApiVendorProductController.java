package com.medusa.mall.controller.api;

import com.medusa.common.annotation.Log;
import com.medusa.common.core.controller.BaseController;
import com.medusa.common.core.domain.AjaxResult;
import com.medusa.common.enums.BusinessType;
import com.medusa.mall.domain.Product;
import com.medusa.mall.domain.dto.VendorProductDTO;
import com.medusa.mall.domain.vo.VendorProductVO;
import com.medusa.mall.service.IVendorProductService;
import com.medusa.mall.util.VendorAuthUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Vendor Product API Controller
 */
@RestController
@RequestMapping("/api/mall/vendor/product")
public class ApiVendorProductController extends BaseController {

    @Autowired
    private IVendorProductService vendorProductService;

    /**
     * Get vendor's own products (with full details including Product2)
     */
    @GetMapping("/list")
    public AjaxResult list() {
        Long memberId = VendorAuthUtils.getCurrentVendorMemberId();
        if (memberId == null) {
            return AjaxResult.error("Please login first");
        }
        
        Long vendorId = vendorProductService.getVendorIdByMemberId(memberId);
        if (vendorId == null) {
            return AjaxResult.error("Your Co-op Member account is not approved yet");
        }
        
        // Return full product info including model and unit
        List<VendorProductVO> products = vendorProductService.selectVendorProductVOList(vendorId);
        return AjaxResult.success(products);
    }

    /**
     * Get product by ID (with full details)
     */
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        Long memberId = VendorAuthUtils.getCurrentVendorMemberId();
        if (memberId == null) {
            return AjaxResult.error("Please login first");
        }
        
        Long vendorId = vendorProductService.getVendorIdByMemberId(memberId);
        if (vendorId == null) {
            return AjaxResult.error("Your Co-op Member account is not approved yet");
        }
        
        VendorProductVO product = vendorProductService.selectVendorProductVOById(id, vendorId);
        if (product == null) {
            return AjaxResult.error("Product not found or access denied");
        }
        
        return AjaxResult.success(product);
    }

    /**
     * Create vendor product
     */
    @Log(title = "Create Vendor Product", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody VendorProductDTO productDTO) {
        Long memberId = VendorAuthUtils.getCurrentVendorMemberId();
        if (memberId == null) {
            return AjaxResult.error("Please login first");
        }
        
        Long vendorId = vendorProductService.getVendorIdByMemberId(memberId);
        if (vendorId == null) {
            return AjaxResult.error("Your Co-op Member account is not approved yet");
        }
        
        try {
            logger.info("Creating vendor product: name={}, category={}, price={}, stock={}", 
                       productDTO.getName(), productDTO.getCategory(), productDTO.getPrice(), productDTO.getStock());
            int result = vendorProductService.createVendorProduct(productDTO, vendorId);
            return toAjax(result);
        } catch (Exception e) {
            logger.error("Failed to create vendor product", e);
            return AjaxResult.error("Failed to create product: " + e.getMessage());
        }
    }

    /**
     * Update vendor product
     */
    @Log(title = "Update Vendor Product", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody Product product) {
        Long memberId = VendorAuthUtils.getCurrentVendorMemberId();
        if (memberId == null) {
            return AjaxResult.error("Please login first");
        }
        
        Long vendorId = vendorProductService.getVendorIdByMemberId(memberId);
        if (vendorId == null) {
            return AjaxResult.error("Your Co-op Member account is not approved yet");
        }
        
        try {
            int result = vendorProductService.updateVendorProduct(product, vendorId);
            return toAjax(result);
        } catch (Exception e) {
            return AjaxResult.error("Failed to update product: " + e.getMessage());
        }
    }

    /**
     * Delete vendor product
     */
    @Log(title = "Delete Vendor Product", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        Long memberId = VendorAuthUtils.getCurrentVendorMemberId();
        if (memberId == null) {
            return AjaxResult.error("Please login first");
        }
        
        Long vendorId = vendorProductService.getVendorIdByMemberId(memberId);
        if (vendorId == null) {
            return AjaxResult.error("Your Co-op Member account is not approved yet");
        }
        
        try {
            for (Long id : ids) {
                vendorProductService.deleteVendorProduct(id, vendorId);
            }
            return AjaxResult.success();
        } catch (Exception e) {
            return AjaxResult.error("Failed to delete product: " + e.getMessage());
        }
    }
}


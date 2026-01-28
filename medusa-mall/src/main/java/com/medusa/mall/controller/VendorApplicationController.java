package com.medusa.mall.controller;

import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.medusa.common.annotation.Log;
import com.medusa.common.core.controller.BaseController;
import com.medusa.common.core.domain.AjaxResult;
import com.medusa.common.core.page.TableDataInfo;
import com.medusa.common.enums.BusinessType;
import com.medusa.common.utils.poi.ExcelUtil;
import com.medusa.mall.domain.vendor.VendorApplication;
import com.medusa.mall.service.IVendorApplicationService;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Vendor Application Controller
 */
@RestController
@RequestMapping("/admin/mall/vendor/application")
public class VendorApplicationController extends BaseController {
    
    @Autowired
    private IVendorApplicationService vendorApplicationService;

    /**
     * Query vendor application list
     */
    @PreAuthorize("@ss.hasPermi('mall:vendor:application:list')")
    @GetMapping("/list")
    public TableDataInfo list(VendorApplication vendorApplication) {
        startPage();
        List<VendorApplication> list = vendorApplicationService.selectVendorApplicationList(vendorApplication);
        return getDataTable(list);
    }

    /**
     * Export vendor application list
     */
    @PreAuthorize("@ss.hasPermi('mall:vendor:application:export')")
    @Log(title = "Vendor Application", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, VendorApplication vendorApplication) {
        List<VendorApplication> list = vendorApplicationService.selectVendorApplicationList(vendorApplication);
        ExcelUtil<VendorApplication> util = new ExcelUtil<VendorApplication>(VendorApplication.class);
        util.exportExcel(response, list, "Vendor Application Data");
    }

    /**
     * Get vendor application details
     */
    @PreAuthorize("@ss.hasPermi('mall:vendor:application:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return AjaxResult.success(vendorApplicationService.selectVendorApplicationById(id));
    }

    /**
     * Add vendor application
     */
    @PreAuthorize("@ss.hasPermi('mall:vendor:application:add')")
    @Log(title = "Vendor Application", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody VendorApplication vendorApplication) {
        return toAjax(vendorApplicationService.insertVendorApplication(vendorApplication));
    }

    /**
     * Update vendor application
     */
    @PreAuthorize("@ss.hasPermi('mall:vendor:application:edit')")
    @Log(title = "Vendor Application", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody VendorApplication vendorApplication) {
        return toAjax(vendorApplicationService.updateVendorApplication(vendorApplication));
    }

    /**
     * Delete vendor application
     */
    @PreAuthorize("@ss.hasPermi('mall:vendor:application:remove')")
    @Log(title = "Vendor Application", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(vendorApplicationService.deleteVendorApplicationByIds(ids));
    }

    /**
     * Approve application
     */
    @PreAuthorize("@ss.hasPermi('mall:vendor:application:approve')")
    @Log(title = "Approve Vendor Application", businessType = BusinessType.UPDATE)
    @PostMapping("/approve/{id}")
    public AjaxResult approve(@PathVariable Long id, @RequestParam(required = false) String notes) {
        return toAjax(vendorApplicationService.approveApplication(id, notes));
    }

    /**
     * Reject application
     */
    @PreAuthorize("@ss.hasPermi('mall:vendor:application:reject')")
    @Log(title = "Reject Vendor Application", businessType = BusinessType.UPDATE)
    @PostMapping("/reject/{id}")
    public AjaxResult reject(@PathVariable Long id, @RequestParam(required = false) String notes) {
        return toAjax(vendorApplicationService.rejectApplication(id, notes));
    }

    /**
     * Update review progress
     */
    @PreAuthorize("@ss.hasPermi('mall:vendor:application:edit')")
    @Log(title = "Update Review Progress", businessType = BusinessType.UPDATE)
    @PostMapping("/progress/{id}")
    public AjaxResult updateProgress(@PathVariable Long id, 
                                     @RequestParam String stage,
                                     @RequestParam Integer progress) {
        return toAjax(vendorApplicationService.updateReviewProgress(id, stage, progress));
    }

    /**
     * Require interview
     */
    @PreAuthorize("@ss.hasPermi('mall:vendor:application:edit')")
    @Log(title = "Require Interview", businessType = BusinessType.UPDATE)
    @PostMapping("/interview/{id}")
    public AjaxResult requireInterview(@PathVariable Long id, @RequestParam(required = false) String notes) {
        return toAjax(vendorApplicationService.requireInterview(id, notes));
    }

    /**
     * Start review
     */
    @PreAuthorize("@ss.hasPermi('mall:vendor:application:edit')")
    @Log(title = "Start Review", businessType = BusinessType.UPDATE)
    @PostMapping("/startReview/{id}")
    public AjaxResult startReview(@PathVariable Long id) {
        return toAjax(vendorApplicationService.updateReviewProgress(id, "initial_review", 10));
    }

    /**
     * Verify vendor wallets
     */
    @PreAuthorize("@ss.hasPermi('mall:vendor:application:edit')")
    @Log(title = "Verify Vendor Wallets", businessType = BusinessType.UPDATE)
    @PostMapping("/verify-wallets")
    public AjaxResult verifyWallets(@RequestBody VendorApplication application) {
        try {
            // Get current application
            VendorApplication existingApp = vendorApplicationService.selectVendorApplicationById(application.getId());
            if (existingApp == null) {
                return AjaxResult.error("Application not found");
            }
            
            // Update wallet verification info
            existingApp.setWalletBtcProvided(application.getWalletBtcProvided());
            existingApp.setWalletXmrProvided(application.getWalletXmrProvided());
            existingApp.setWalletUsdtProvided(application.getWalletUsdtProvided());
            existingApp.setWalletVerifiedTime(new java.util.Date());
            existingApp.setWalletVerifiedBy(application.getWalletVerifiedBy());
            
            // Add verification notes to review notes if provided
            if (application.getRemark() != null && !application.getRemark().isEmpty()) {
                String newNotes = "Wallet Verification: " + application.getRemark();
                String existingNotes = existingApp.getReviewNotes();
                if (existingNotes != null && !existingNotes.isEmpty()) {
                    existingApp.setReviewNotes(existingNotes + "\n" + newNotes);
                } else {
                    existingApp.setReviewNotes(newNotes);
                }
            }
            
            int result = vendorApplicationService.updateVendorApplication(existingApp);
            
            if (result > 0) {
                // TODO: Send notification to vendor that wallets are verified
                // TODO: Update vendor status if needed
                return AjaxResult.success("Wallets verified successfully");
            } else {
                return AjaxResult.error("Failed to verify wallets");
            }
            
        } catch (Exception e) {
            return AjaxResult.error("Error verifying wallets: " + e.getMessage());
        }
    }
}


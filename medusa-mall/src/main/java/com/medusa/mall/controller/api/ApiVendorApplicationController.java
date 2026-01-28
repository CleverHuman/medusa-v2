package com.medusa.mall.controller.api;

import com.medusa.common.annotation.Log;
import com.medusa.common.core.controller.BaseController;
import com.medusa.common.core.domain.AjaxResult;
import com.medusa.common.enums.BusinessType;
import com.medusa.mall.domain.vendor.VendorApplication;
import com.medusa.mall.service.IVendorApplicationService;
import com.medusa.mall.util.VendorAuthUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * Public API for Vendor Application (No Authentication Required)
 * For vendors to submit applications and check status
 */
@RestController
@RequestMapping("/api/mall/vendor/application")
public class ApiVendorApplicationController extends BaseController {
    
    @Autowired
    private IVendorApplicationService vendorApplicationService;

    /**
     * Submit vendor application (Public API - No auth required)
     */
    @Log(title = "Submit Vendor Application", businessType = BusinessType.INSERT)
    @PostMapping("/submit")
    public AjaxResult submit(@RequestBody VendorApplication vendorApplication) {
        try {
            Long memberId = VendorAuthUtils.getCurrentVendorMemberId();
            if (memberId == null) {
                return AjaxResult.error("Please login before submitting application");
            }
            vendorApplication.setMemberId(memberId);
            
            // ✅ Validate that primary and secondary contacts are different
            boolean contactsAreSame = false;
            String sameContactType = null;
            
            // Check Telegram
            if (vendorApplication.getPrimaryTelegram() != null && 
                vendorApplication.getSecondaryTelegram() != null &&
                !vendorApplication.getPrimaryTelegram().trim().isEmpty() &&
                !vendorApplication.getSecondaryTelegram().trim().isEmpty() &&
                vendorApplication.getPrimaryTelegram().trim().equals(vendorApplication.getSecondaryTelegram().trim())) {
                contactsAreSame = true;
                sameContactType = "Telegram";
            }
            
            // Check Signal
            if (!contactsAreSame && vendorApplication.getPrimarySignal() != null && 
                vendorApplication.getSecondarySignal() != null &&
                !vendorApplication.getPrimarySignal().trim().isEmpty() &&
                !vendorApplication.getSecondarySignal().trim().isEmpty() &&
                vendorApplication.getPrimarySignal().trim().equals(vendorApplication.getSecondarySignal().trim())) {
                contactsAreSame = true;
                sameContactType = "Signal";
            }
            
            // Check Jabber
            if (!contactsAreSame && vendorApplication.getPrimaryJabber() != null && 
                vendorApplication.getSecondaryJabber() != null &&
                !vendorApplication.getPrimaryJabber().trim().isEmpty() &&
                !vendorApplication.getSecondaryJabber().trim().isEmpty() &&
                vendorApplication.getPrimaryJabber().trim().equals(vendorApplication.getSecondaryJabber().trim())) {
                contactsAreSame = true;
                sameContactType = "Jabber";
            }
            
            // Check Email
            if (!contactsAreSame && vendorApplication.getPrimaryEmail() != null && 
                vendorApplication.getSecondaryEmail() != null &&
                !vendorApplication.getPrimaryEmail().trim().isEmpty() &&
                !vendorApplication.getSecondaryEmail().trim().isEmpty() &&
                vendorApplication.getPrimaryEmail().trim().equalsIgnoreCase(vendorApplication.getSecondaryEmail().trim())) {
                contactsAreSame = true;
                sameContactType = "Email";
            }
            
            // Check Threema
            if (!contactsAreSame && vendorApplication.getPrimaryThreema() != null && 
                vendorApplication.getSecondaryThreema() != null &&
                !vendorApplication.getPrimaryThreema().trim().isEmpty() &&
                !vendorApplication.getSecondaryThreema().trim().isEmpty() &&
                vendorApplication.getPrimaryThreema().trim().equals(vendorApplication.getSecondaryThreema().trim())) {
                contactsAreSame = true;
                sameContactType = "Threema";
            }
            
            if (contactsAreSame) {
                return AjaxResult.error("The primary and secondary contact cannot be the same. Please use different " + sameContactType + " contacts.");
            }
            
            // Set default status if not provided
            if (vendorApplication.getStatus() == null || vendorApplication.getStatus().isEmpty()) {
                vendorApplication.setStatus("pending");
            }
            
            // Set default progress
            if (vendorApplication.getReviewProgress() == null) {
                vendorApplication.setReviewProgress(0);
            }
            
            int result = vendorApplicationService.insertVendorApplication(vendorApplication);
            
            if (result > 0) {
                // Return the created application with ID
                return AjaxResult.success("Application submitted successfully", vendorApplication);
            } else {
                return AjaxResult.error("Failed to submit application");
            }
        } catch (Exception e) {
            logger.error("Failed to submit vendor application", e);
            return AjaxResult.error("Failed to submit application: " + e.getMessage());
        }
    }

    /**
     * Query application status by Application ID (Public API - No auth required)
     */
    @GetMapping("/status/{applicationId}")
    public AjaxResult getStatus(@PathVariable String applicationId) {
        try {
            Long memberId = VendorAuthUtils.getCurrentVendorMemberId();
            if (memberId == null) {
                return AjaxResult.error("Please login first");
            }
            VendorApplication application = vendorApplicationService.selectByApplicationIdAndMember(applicationId, memberId);
            
            if (application == null) {
                return AjaxResult.error("Application not found");
            }
            
            // Remove sensitive information for public query
            application.setReviewNotes(null);  // Don't expose internal review notes to public
            
            return AjaxResult.success(application);
        } catch (Exception e) {
            logger.error("Failed to query application status", e);
            return AjaxResult.error("Failed to query status: " + e.getMessage());
        }
    }

    /**
     * Get application details by Application ID (Public API - Limited info)
     */
    @GetMapping("/detail/{applicationId}")
    public AjaxResult getDetails(@PathVariable String applicationId) {
        try {
            Long memberId = VendorAuthUtils.getCurrentVendorMemberId();
            if (memberId == null) {
                return AjaxResult.error("Please login first");
            }
            VendorApplication application = vendorApplicationService.selectByApplicationIdAndMember(applicationId, memberId);
            
            if (application == null) {
                return AjaxResult.error("Application not found");
            }
            
            return AjaxResult.success(application);
        } catch (Exception e) {
            logger.error("Failed to query application details", e);
            return AjaxResult.error("Failed to query details: " + e.getMessage());
        }
    }

    /**
     * Get all applications for current vendor member
     */
    @GetMapping("/my-applications")
    public AjaxResult getMyApplications() {
        try {
            Long memberId = VendorAuthUtils.getCurrentVendorMemberId();
            if (memberId == null) {
                return AjaxResult.error("Please login first");
            }
            
            VendorApplication query = new VendorApplication();
            query.setMemberId(memberId);
            java.util.List<VendorApplication> applications = vendorApplicationService.selectVendorApplicationList(query);
            
            return AjaxResult.success(applications);
        } catch (Exception e) {
            logger.error("Failed to query vendor applications", e);
            return AjaxResult.error("Failed to query applications: " + e.getMessage());
        }
    }
}


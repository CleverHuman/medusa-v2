package com.medusa.web.controller.mall;

import com.medusa.common.annotation.Log;
import com.medusa.common.core.controller.BaseController;
import com.medusa.common.core.domain.AjaxResult;
import com.medusa.common.core.page.TableDataInfo;
import com.medusa.common.enums.BusinessType;
import com.medusa.common.utils.SecurityUtils;
import com.medusa.mall.domain.vendor.VendorBalanceLog;
import com.medusa.mall.domain.vendor.VendorWithdrawalAddress;
import com.medusa.mall.domain.vendor.VendorWithdrawalRequest;
import com.medusa.mall.service.IVendorWithdrawalService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Admin 端 Vendor 提现管理 Controller
 * 
 * @author medusa
 * @date 2025-11-18
 */
@Api(tags = "Admin - Vendor Withdrawal Management")
@RestController
@RequestMapping("/admin/mall/vendor/withdrawal")
public class VendorWithdrawalController extends BaseController {
    
    @Autowired
    private IVendorWithdrawalService withdrawalService;
    
    // ============================================
    // 提现请求管理
    // ============================================
    
    @ApiOperation("Get All Withdrawal Requests (Admin)")
    @PreAuthorize("@ss.hasPermi('mall:vendor:withdrawal:list')")
    @GetMapping("/requests")
    public TableDataInfo getAllRequests(VendorWithdrawalRequest request) {
        startPage();
        List<VendorWithdrawalRequest> list = withdrawalService.getAllWithdrawalRequests(request);
        return getDataTable(list);
    }
    
    @ApiOperation("Get Pending Withdrawal Requests (Admin)")
    @PreAuthorize("@ss.hasPermi('mall:vendor:withdrawal:list')")
    @GetMapping("/requests/pending")
    public TableDataInfo getPendingRequests() {
        startPage();
        List<VendorWithdrawalRequest> list = withdrawalService.getPendingWithdrawalRequests();
        return getDataTable(list);
    }
    
    @ApiOperation("Get Withdrawal Request by ID (Admin)")
    @PreAuthorize("@ss.hasPermi('mall:vendor:withdrawal:query')")
    @GetMapping("/requests/{id}")
    public AjaxResult getRequest(@PathVariable Long id) {
        VendorWithdrawalRequest request = withdrawalService.getWithdrawalRequestById(id);
        if (request != null) {
            return AjaxResult.success(request);
        } else {
            return AjaxResult.error("Withdrawal request not found");
        }
    }
    
    @ApiOperation("Approve Withdrawal Request")
    @PreAuthorize("@ss.hasPermi('mall:vendor:withdrawal:approve')")
    @Log(title = "Approve Withdrawal Request", businessType = BusinessType.UPDATE)
    @PostMapping("/requests/{id}/approve")
    public AjaxResult approveRequest(
            @ApiParam("Request ID") @PathVariable Long id,
            @ApiParam("Approval Remark") @RequestParam(required = false) String remark) {
        
        String approveBy = SecurityUtils.getUsername();
        boolean success = withdrawalService.approveWithdrawalRequest(id, true, approveBy, remark);
        
        if (success) {
            return AjaxResult.success("Withdrawal request approved successfully");
        } else {
            return AjaxResult.error("Failed to approve withdrawal request");
        }
    }
    
    @ApiOperation("Reject Withdrawal Request")
    @PreAuthorize("@ss.hasPermi('mall:vendor:withdrawal:reject')")
    @Log(title = "Reject Withdrawal Request", businessType = BusinessType.UPDATE)
    @PostMapping("/requests/{id}/reject")
    public AjaxResult rejectRequest(
            @ApiParam("Request ID") @PathVariable Long id,
            @ApiParam("Rejection Reason") @RequestParam String reason) {
        
        String approveBy = SecurityUtils.getUsername();
        boolean success = withdrawalService.approveWithdrawalRequest(id, false, approveBy, reason);
        
        if (success) {
            return AjaxResult.success("Withdrawal request rejected successfully");
        } else {
            return AjaxResult.error("Failed to reject withdrawal request");
        }
    }
    
    @ApiOperation("Mark Withdrawal as Processing")
    @PreAuthorize("@ss.hasPermi('mall:vendor:withdrawal:process')")
    @Log(title = "Mark Withdrawal Processing", businessType = BusinessType.UPDATE)
    @PostMapping("/requests/{id}/processing")
    public AjaxResult markProcessing(
            @ApiParam("Request ID") @PathVariable Long id,
            @ApiParam("Transaction Hash") @RequestParam String txHash) {
        
        boolean success = withdrawalService.markWithdrawalProcessing(id, txHash);
        
        if (success) {
            return AjaxResult.success("Withdrawal marked as processing");
        } else {
            return AjaxResult.error("Failed to mark withdrawal as processing");
        }
    }
    
    @ApiOperation("Mark Withdrawal as Completed")
    @PreAuthorize("@ss.hasPermi('mall:vendor:withdrawal:complete')")
    @Log(title = "Mark Withdrawal Completed", businessType = BusinessType.UPDATE)
    @PostMapping("/requests/{id}/complete")
    public AjaxResult markCompleted(
            @ApiParam("Request ID") @PathVariable Long id,
            @ApiParam("Transaction Fee") @RequestParam(required = false, defaultValue = "0") BigDecimal txFee) {
        
        boolean success = withdrawalService.markWithdrawalCompleted(id, txFee);
        
        if (success) {
            return AjaxResult.success("Withdrawal marked as completed");
        } else {
            return AjaxResult.error("Failed to mark withdrawal as completed");
        }
    }
    
    @ApiOperation("Mark Withdrawal as Failed")
    @PreAuthorize("@ss.hasPermi('mall:vendor:withdrawal:fail')")
    @Log(title = "Mark Withdrawal Failed", businessType = BusinessType.UPDATE)
    @PostMapping("/requests/{id}/fail")
    public AjaxResult markFailed(
            @ApiParam("Request ID") @PathVariable Long id,
            @ApiParam("Failure Reason") @RequestParam String reason) {
        
        boolean success = withdrawalService.markWithdrawalFailed(id, reason);
        
        if (success) {
            return AjaxResult.success("Withdrawal marked as failed");
        } else {
            return AjaxResult.error("Failed to mark withdrawal as failed");
        }
    }
    
    // ============================================
    // 余额查询和统计
    // ============================================
    
    @ApiOperation("Get Vendor Balance by Vendor ID (Admin)")
    @PreAuthorize("@ss.hasPermi('mall:vendor:balance:query')")
    @GetMapping("/balance/{vendorId}")
    public AjaxResult getVendorBalance(@PathVariable Long vendorId) {
        Map<String, BigDecimal> balance = withdrawalService.getVendorBalance(vendorId);
        return AjaxResult.success(balance);
    }
    
    @ApiOperation("Get Vendor Balance Logs (Admin)")
    @PreAuthorize("@ss.hasPermi('mall:vendor:balance:logs')")
    @GetMapping("/balance/{vendorId}/logs")
    public TableDataInfo getBalanceLogs(@PathVariable Long vendorId) {
        startPage();
        List<VendorBalanceLog> logs = withdrawalService.getBalanceLogs(vendorId);
        return getDataTable(logs);
    }
    
    @ApiOperation("Get Withdrawal Statistics")
    @PreAuthorize("@ss.hasPermi('mall:vendor:withdrawal:stats')")
    @GetMapping("/statistics")
    public AjaxResult getStatistics() {
        Map<String, Object> stats = withdrawalService.getWithdrawalStatistics();
        return AjaxResult.success(stats);
    }
    
    // ============================================
    // 手动触发余额释放
    // ============================================
    
    @ApiOperation("Manually Release Expired Pending Balance")
    @PreAuthorize("@ss.hasRole('admin')")
    @Log(title = "Release Pending Balance", businessType = BusinessType.OTHER)
    @PostMapping("/balance/release")
    public AjaxResult releasePendingBalance() {
        try {
            withdrawalService.releaseExpiredPendingBalance();
            return AjaxResult.success("Pending balance released successfully");
        } catch (Exception e) {
            return AjaxResult.error("Failed to release pending balance: " + e.getMessage());
        }
    }
    
    // ============================================
    // 钱包地址管理 (Super Admin only)
    // ============================================
    
    @ApiOperation("Get Vendor Withdrawal Addresses (Admin)")
    @PreAuthorize("@ss.hasPermi('mall:vendor:query')")
    @GetMapping("/addresses/{vendorId}")
    public AjaxResult getVendorAddresses(@ApiParam("Vendor ID") @PathVariable Long vendorId) {
        List<VendorWithdrawalAddress> addresses = withdrawalService.getWithdrawalAddresses(vendorId);
        return AjaxResult.success(addresses);
    }
    
    @ApiOperation("Update Vendor Withdrawal Address (Super Admin)")
    @PreAuthorize("@ss.hasPermi('mall:vendor:withdrawal:address:update') and @ss.hasRole('vendor_super_admin')")
    @Log(title = "Update Vendor Withdrawal Address", businessType = BusinessType.UPDATE)
    @PostMapping("/addresses/update")
    public AjaxResult updateWalletAddress(
            @ApiParam("Vendor ID") @RequestParam Long vendorId,
            @ApiParam("Currency") @RequestParam String currency,
            @ApiParam("New Address") @RequestParam String newAddress) {
        
        try {
            VendorWithdrawalAddress address = new VendorWithdrawalAddress();
            address.setVendorId(vendorId);
            address.setCurrency(currency);
            address.setAddress(newAddress);
            address.setIsActive(1);
            address.setVerifiedAt(new java.util.Date());
            address.setVerifiedMethod("ADMIN");
            address.setCreateBy(SecurityUtils.getUsername());
            
            boolean success = withdrawalService.createOrUpdateWithdrawalAddress(address);
            
            if (success) {
                return AjaxResult.success("Wallet address updated successfully");
            } else {
                return AjaxResult.error("Failed to update wallet address");
            }
        } catch (Exception e) {
            return AjaxResult.error("Failed to update wallet address: " + e.getMessage());
        }
    }
}


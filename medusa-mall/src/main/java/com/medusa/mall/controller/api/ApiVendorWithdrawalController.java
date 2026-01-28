package com.medusa.mall.controller.api;

import com.medusa.common.core.controller.BaseController;
import com.medusa.common.core.domain.AjaxResult;
import com.medusa.common.core.page.TableDataInfo;
import com.medusa.mall.domain.vendor.VendorBalanceLog;
import com.medusa.mall.domain.vendor.VendorWithdrawalAddress;
import com.medusa.mall.domain.vendor.VendorWithdrawalRequest;
import com.medusa.mall.service.IVendorWithdrawalService;
import com.medusa.mall.util.VendorAuthUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Vendor 提现 API Controller
 * 
 * @author medusa
 * @date 2025-11-18
 */
@Api(tags = "Vendor Withdrawal Management")
@RestController
@RequestMapping("/api/mall/vendor/withdrawal")
public class ApiVendorWithdrawalController extends BaseController {
    
    @Autowired
    private IVendorWithdrawalService withdrawalService;
    
    // ============================================
    // 余额查询
    // ============================================
    
    @ApiOperation("Get Vendor Balance")
    @GetMapping("/balance")
    public AjaxResult getBalance() {
        Long vendorId = VendorAuthUtils.getCurrentVendorId();
        if (vendorId == null) {
            return AjaxResult.error("Not logged in or your account is not approved yet");
        }
        
        Map<String, BigDecimal> balance = withdrawalService.getVendorBalance(vendorId);
        return AjaxResult.success(balance);
    }
    
    @ApiOperation("Get Balance Change History")
    @GetMapping("/balance/logs")
    public TableDataInfo getBalanceLogs() {
        Long vendorId = VendorAuthUtils.getCurrentVendorId();
        if (vendorId == null) {
            return getDataTable(List.of());
        }
        
        List<VendorBalanceLog> logs = withdrawalService.getBalanceLogs(vendorId);
        return getDataTable(logs);
    }
    
    // ============================================
    // 提现地址管理
    // ============================================
    
    @ApiOperation("Get Withdrawal Addresses")
    @GetMapping("/addresses")
    public TableDataInfo getAddresses() {
        Long vendorId = VendorAuthUtils.getCurrentVendorId();
        if (vendorId == null) {
            return getDataTable(List.of());
        }
        
        List<VendorWithdrawalAddress> addresses = withdrawalService.getWithdrawalAddresses(vendorId);
        return getDataTable(addresses);
    }
    
    @ApiOperation("Get Withdrawal Address by Currency")
    @GetMapping("/address/{currency}")
    public AjaxResult getAddress(@ApiParam("Currency (BTC/XMR/USDT_TRX/USDT_ERC)") @PathVariable String currency) {
        Long vendorId = VendorAuthUtils.getCurrentVendorId();
        if (vendorId == null) {
            return AjaxResult.error("Not logged in or your account is not approved yet");
        }
        
        // 获取完整地址信息（包括待验证地址），用于显示提示
        // 注意：这里需要获取完整信息，而不仅仅是激活的地址
        VendorWithdrawalAddress address = withdrawalService.getWithdrawalAddresses(vendorId).stream()
                .filter(addr -> currency.equals(addr.getCurrency()))
                .findFirst()
                .orElse(null);
        
        // 如果地址存在但未激活，仍然返回（用于显示待验证提示）
        // 如果地址不存在，返回 null
        return AjaxResult.success(address);
    }
    
    @ApiOperation("Request Address Update (initiate PGP verification)")
    @PostMapping("/address/update")
    public AjaxResult requestAddressUpdate(
            @ApiParam("Currency") @RequestParam String currency,
            @ApiParam("New Address") @RequestParam String newAddress) {
        Long vendorId = VendorAuthUtils.getCurrentVendorId();
        if (vendorId == null) {
            return AjaxResult.error("Not logged in or your account is not approved yet");
        }
        
        Map<String, Object> result = withdrawalService.requestAddressUpdate(vendorId, currency, newAddress);
        return AjaxResult.success(result);
    }
    
    @ApiOperation("Verify and Update Address")
    @PostMapping("/address/verify")
    public AjaxResult verifyAddress(
            @ApiParam("Currency") @RequestParam String currency,
            @ApiParam("Verification Code") @RequestParam String verificationCode) {
        Long vendorId = VendorAuthUtils.getCurrentVendorId();
        if (vendorId == null) {
            return AjaxResult.error("Not logged in or your account is not approved yet");
        }
        
        boolean success = withdrawalService.verifyAndUpdateAddress(vendorId, currency, verificationCode);
        if (success) {
            return AjaxResult.success("Address verified and updated");
        } else {
            return AjaxResult.error("Invalid verification code");
        }
    }
    
    // ============================================
    // 提现请求
    // ============================================
    
    @ApiOperation("Create Withdrawal Request")
    @PostMapping("/request")
    public AjaxResult createRequest(
            @ApiParam("Currency (BTC/XMR/USDT_TRX/USDT_ERC)") @RequestParam String currency,
            @ApiParam("Amount") @RequestParam BigDecimal amount) {
        Long vendorId = VendorAuthUtils.getCurrentVendorId();
        if (vendorId == null) {
            return AjaxResult.error("Not logged in or your account is not approved yet");
        }
        
        // Controller层验证：金额必须大于0
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return AjaxResult.error("Withdrawal amount must be greater than 0. Provided: " + amount);
        }
        
        try {
            VendorWithdrawalRequest request = withdrawalService.createWithdrawalRequest(vendorId, currency, amount);
            return AjaxResult.success(request);
        } catch (Exception e) {
            return AjaxResult.error(e.getMessage());
        }
    }
    
    @ApiOperation("Get My Withdrawal Requests")
    @GetMapping("/requests")
    public TableDataInfo getRequests() {
        Long vendorId = VendorAuthUtils.getCurrentVendorId();
        if (vendorId == null) {
            return getDataTable(List.of());
        }
        
        List<VendorWithdrawalRequest> requests = withdrawalService.getWithdrawalRequests(vendorId);
        return getDataTable(requests);
    }
}


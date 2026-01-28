package com.medusa.mall.controller;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.medusa.common.annotation.Log;
import com.medusa.common.core.controller.BaseController;
import com.medusa.common.core.domain.AjaxResult;
import com.medusa.common.core.page.TableDataInfo;
import com.medusa.common.enums.BusinessType;
import com.medusa.common.utils.SecurityUtils;
import com.medusa.common.utils.poi.ExcelUtil;
import com.medusa.common.core.domain.entity.SysUser;
import com.medusa.mall.domain.vendor.Vendor;
import com.medusa.mall.service.IVendorService;
import com.medusa.system.service.ISysUserService;
import com.medusa.system.mapper.SysRoleMapper;
import com.medusa.common.core.domain.entity.SysRole;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Vendor Controller
 */
@RestController
@RequestMapping("/admin/mall/vendor")
public class VendorController extends BaseController {
    
    @Autowired
    private IVendorService vendorService;

    @Autowired
    private ISysUserService sysUserService;

    @Autowired
    private SysRoleMapper sysRoleMapper;

    /**
     * Query vendor list
     */
    @PreAuthorize("@ss.hasPermi('mall:vendor:list')")
    @GetMapping("/list")
    public TableDataInfo list(Vendor vendor) {
        startPage();
        List<Vendor> list = vendorService.selectVendorList(vendor);
        return getDataTable(list);
    }

    /**
     * Export vendor list
     */
    @PreAuthorize("@ss.hasPermi('mall:vendor:export')")
    @Log(title = "Vendor", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, Vendor vendor) {
        List<Vendor> list = vendorService.selectVendorList(vendor);
        ExcelUtil<Vendor> util = new ExcelUtil<Vendor>(Vendor.class);
        util.exportExcel(response, list, "Vendor Data");
    }

    /**
     * Get vendor details
     */
    @PreAuthorize("@ss.hasPermi('mall:vendor:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return AjaxResult.success(vendorService.selectVendorById(id));
    }

    /**
     * Add vendor
     */
    @PreAuthorize("@ss.hasPermi('mall:vendor:add')")
    @Log(title = "Vendor", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody Vendor vendor) {
        return toAjax(vendorService.insertVendor(vendor));
    }

    /**
     * Update vendor
     */
    @PreAuthorize("@ss.hasPermi('mall:vendor:edit')")
    @Log(title = "Vendor", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody Vendor vendor) {
        return toAjax(vendorService.updateVendor(vendor));
    }

    /**
     * Delete vendor
     */
    @PreAuthorize("@ss.hasPermi('mall:vendor:remove')")
    @Log(title = "Vendor", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(vendorService.deleteVendorByIds(ids));
    }

    /**
     * Update vendor bond and level
     */
    @PreAuthorize("@ss.hasPermi('mall:vendor:edit')")
    @Log(title = "Update Vendor Bond & Level", businessType = BusinessType.UPDATE)
    @PutMapping("/{id}/bond-level")
    public AjaxResult updateBondAndLevel(
            @PathVariable("id") Long id,
            @RequestParam(required = false) BigDecimal bond,
            @RequestParam(required = false) Integer level,
            @RequestParam(required = false) Long salesPoints) {
        
        Vendor vendor = vendorService.selectVendorById(id);
        if (vendor == null) {
            return AjaxResult.error("Vendor not found");
        }
        
        // Update bond if provided
        if (bond != null) {
            if (bond.compareTo(BigDecimal.ZERO) < 0) {
                return AjaxResult.error("Bond amount cannot be negative");
            }
            vendor.setBond(bond);
        }
        
        // Update level if provided
        if (level != null) {
            if (level < 1 || level > 5) {
                return AjaxResult.error("Level must be between 1 and 5");
            }
            vendor.setLevel(level);
        }
        
        // Update sales points if provided
        if (salesPoints != null) {
            if (salesPoints < 0) {
                return AjaxResult.error("Sales points cannot be negative");
            }
            vendor.setSalesPoints(salesPoints);
        }
        
        int result = vendorService.updateVendor(vendor);
        if (result > 0) {
            return AjaxResult.success("Vendor bond and level updated successfully");
        } else {
            return AjaxResult.error("Failed to update vendor bond and level");
        }
    }

    /**
     * Create warehouse staff account for vendor
     */
    @PreAuthorize("@ss.hasPermi('mall:vendor:warehouse:create')")
    @Log(title = "Create Vendor Warehouse Account", businessType = BusinessType.INSERT)
    @PostMapping("/{vendorId}/warehouse-account")
    public AjaxResult createWarehouseAccount(
            @PathVariable("vendorId") Long vendorId,
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam(required = false) String email) {
        
        // 1. Verify Vendor exists and is enabled
        Vendor vendor = vendorService.selectVendorById(vendorId);
        if (vendor == null) {
            return AjaxResult.error("Vendor not found");
        }
        if (vendor.getStatus() != null && vendor.getStatus() != 1) {
            return AjaxResult.error("Vendor is not enabled");
        }
        
        // 2. Validate username length (must be 5-20 characters for login compatibility)
        if (username == null || username.length() < 5 || username.length() > 20) {
            return AjaxResult.error("Username length must be between 5 and 20 characters");
        }
        
        // 3. Validate password length (must be 6-20 characters)
        if (password == null || password.length() < 6 || password.length() > 20) {
            return AjaxResult.error("Password length must be between 6 and 20 characters");
        }
        
        // 4. Check username uniqueness
        SysUser checkUser = new SysUser();
        checkUser.setUserName(username);
        if (!sysUserService.checkUserNameUnique(checkUser)) {
            return AjaxResult.error("Username already exists: " + username);
        }
        
        // 5. Get vendor_warehouse_staff role ID
        SysRole warehouseStaffRole = sysRoleMapper.checkRoleKeyUnique("vendor_warehouse_staff");
        if (warehouseStaffRole == null || warehouseStaffRole.getRoleId() == null) {
            return AjaxResult.error("Vendor warehouse staff role not found. Please run database migration V1.35 first.");
        }
        Long warehouseStaffRoleId = warehouseStaffRole.getRoleId();
        
        // 6. Create user account
        SysUser user = new SysUser();
        user.setUserName(username);
        user.setPassword(SecurityUtils.encryptPassword(password));
        user.setVendorId(vendorId); // Link to vendor
        
        // Set nick_name, ensuring it doesn't exceed 30 characters (varchar(30) limit)
        String vendorName = vendor.getVendorName() != null ? vendor.getVendorName() : "";
        String suffix = " - WH Staff";
        int maxVendorNameLength = 30 - suffix.length();
        String nickName = (vendorName.length() > maxVendorNameLength) 
            ? vendorName.substring(0, maxVendorNameLength) + suffix
            : vendorName + suffix;
        user.setNickName(nickName);
        
        user.setEmail(email != null && !email.isEmpty() ? email : "");
        user.setStatus("0"); // Normal status
        user.setDeptId(100L); // Default department
        user.setCreateBy(getUsername());
        user.setRemark("Warehouse staff account for vendor: " + vendor.getVendorName());
        
        // 7. Assign role
        Long[] roleIds = {warehouseStaffRoleId};
        user.setRoleIds(roleIds);
        
        // 8. Save user and role association
        int result = sysUserService.insertUser(user);
        if (result > 0) {
            return AjaxResult.success("Warehouse staff account created successfully");
        } else {
            return AjaxResult.error("Failed to create warehouse staff account");
        }
    }
}


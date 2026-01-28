package com.medusa.mall.domain.vendor;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.medusa.common.core.domain.BaseEntity;

import java.util.Date;

/**
 * Vendor 提现地址对象 mall_vendor_withdrawal_address
 * 
 * @author medusa
 * @date 2025-11-18
 */
public class VendorWithdrawalAddress extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;

    /** Vendor ID */
    private Long vendorId;

    /** 币种 (BTC/XMR/USDT_TRX/USDT_ERC) */
    private String currency;

    /** 提现地址 */
    private String address;

    /** 地址标签 */
    private String addressLabel;

    /** 是否激活 (0=否, 1=是) */
    private Integer isActive;

    /** 验证时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date verifiedAt;

    /** 验证方式 (PGP/EMAIL) */
    private String verifiedMethod;

    /** 验证码（已使用后清空） */
    private String verificationCode;

    /** 待验证的新地址 */
    private String pendingAddress;

    /** 待验证的验证码 */
    private String pendingVerificationCode;

    /** 待验证地址请求时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date pendingRequestTime;

    /** 上一个地址（用于审计） */
    private String previousAddress;
    
    /** 提现锁定到期时间（地址更改后1天） */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date withdrawalLockUntil;
    
    /** 地址最后更改时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date addressChangedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getVendorId() {
        return vendorId;
    }

    public void setVendorId(Long vendorId) {
        this.vendorId = vendorId;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAddressLabel() {
        return addressLabel;
    }

    public void setAddressLabel(String addressLabel) {
        this.addressLabel = addressLabel;
    }

    public Integer getIsActive() {
        return isActive;
    }

    public void setIsActive(Integer isActive) {
        this.isActive = isActive;
    }

    public Date getVerifiedAt() {
        return verifiedAt;
    }

    public void setVerifiedAt(Date verifiedAt) {
        this.verifiedAt = verifiedAt;
    }

    public String getVerifiedMethod() {
        return verifiedMethod;
    }

    public void setVerifiedMethod(String verifiedMethod) {
        this.verifiedMethod = verifiedMethod;
    }

    public String getVerificationCode() {
        return verificationCode;
    }

    public void setVerificationCode(String verificationCode) {
        this.verificationCode = verificationCode;
    }

    public String getPendingAddress() {
        return pendingAddress;
    }

    public void setPendingAddress(String pendingAddress) {
        this.pendingAddress = pendingAddress;
    }

    public String getPendingVerificationCode() {
        return pendingVerificationCode;
    }

    public void setPendingVerificationCode(String pendingVerificationCode) {
        this.pendingVerificationCode = pendingVerificationCode;
    }

    public Date getPendingRequestTime() {
        return pendingRequestTime;
    }

    public void setPendingRequestTime(Date pendingRequestTime) {
        this.pendingRequestTime = pendingRequestTime;
    }

    public String getPreviousAddress() {
        return previousAddress;
    }

    public void setPreviousAddress(String previousAddress) {
        this.previousAddress = previousAddress;
    }
    
    public Date getWithdrawalLockUntil() {
        return withdrawalLockUntil;
    }
    
    public void setWithdrawalLockUntil(Date withdrawalLockUntil) {
        this.withdrawalLockUntil = withdrawalLockUntil;
    }
    
    public Date getAddressChangedAt() {
        return addressChangedAt;
    }
    
    public void setAddressChangedAt(Date addressChangedAt) {
        this.addressChangedAt = addressChangedAt;
    }
}


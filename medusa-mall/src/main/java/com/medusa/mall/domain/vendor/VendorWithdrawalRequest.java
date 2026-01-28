package com.medusa.mall.domain.vendor;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.medusa.common.core.domain.BaseEntity;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Vendor 提现请求对象 mall_vendor_withdrawal_request
 * 
 * @author medusa
 * @date 2025-11-18
 */
public class VendorWithdrawalRequest extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;

    /** 提现请求编号 */
    private String requestCode;

    /** Vendor ID */
    private Long vendorId;

    /** 币种 (BTC/XMR/USDT_TRX/USDT_ERC) */
    private String currency;

    /** 提现金额 */
    private BigDecimal amount;

    /** 提现地址 */
    private String withdrawalAddress;

    /** 状态 (PENDING/APPROVED/REJECTED/PROCESSING/COMPLETED/FAILED) */
    private String requestStatus;

    /** 请求时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date requestTime;

    /** 审批人 */
    private String approveBy;

    /** 审批时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date approveTime;

    /** 审批备注 */
    private String approveRemark;

    /** 交易哈希 */
    private String txHash;

    /** 交易时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date txTime;

    /** 交易手续费 */
    private BigDecimal txFee;

    /** 拒绝原因 */
    private String rejectReason;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRequestCode() {
        return requestCode;
    }

    public void setRequestCode(String requestCode) {
        this.requestCode = requestCode;
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

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getWithdrawalAddress() {
        return withdrawalAddress;
    }

    public void setWithdrawalAddress(String withdrawalAddress) {
        this.withdrawalAddress = withdrawalAddress;
    }

    public String getRequestStatus() {
        return requestStatus;
    }

    public void setRequestStatus(String requestStatus) {
        this.requestStatus = requestStatus;
    }

    public Date getRequestTime() {
        return requestTime;
    }

    public void setRequestTime(Date requestTime) {
        this.requestTime = requestTime;
    }

    public String getApproveBy() {
        return approveBy;
    }

    public void setApproveBy(String approveBy) {
        this.approveBy = approveBy;
    }

    public Date getApproveTime() {
        return approveTime;
    }

    public void setApproveTime(Date approveTime) {
        this.approveTime = approveTime;
    }

    public String getApproveRemark() {
        return approveRemark;
    }

    public void setApproveRemark(String approveRemark) {
        this.approveRemark = approveRemark;
    }

    public String getTxHash() {
        return txHash;
    }

    public void setTxHash(String txHash) {
        this.txHash = txHash;
    }

    public Date getTxTime() {
        return txTime;
    }

    public void setTxTime(Date txTime) {
        this.txTime = txTime;
    }

    public BigDecimal getTxFee() {
        return txFee;
    }

    public void setTxFee(BigDecimal txFee) {
        this.txFee = txFee;
    }

    public String getRejectReason() {
        return rejectReason;
    }

    public void setRejectReason(String rejectReason) {
        this.rejectReason = rejectReason;
    }
}


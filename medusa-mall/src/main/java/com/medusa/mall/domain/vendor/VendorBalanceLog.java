package com.medusa.mall.domain.vendor;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.medusa.common.core.domain.BaseEntity;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Vendor 余额变动记录对象 mall_vendor_balance_log
 * 
 * @author medusa
 * @date 2025-11-18
 */
public class VendorBalanceLog extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;

    /** Vendor ID */
    private Long vendorId;

    /** 变动类型 (ORDER_SHIPPED/WITHDRAWAL/REFUND/DISPUTE/BALANCE_RELEASED) */
    private String changeType;

    /** 变动金额（正数为增加，负数为减少） */
    private BigDecimal amount;

    /** 变动前余额 */
    private BigDecimal beforeBalance;

    /** 变动后余额 */
    private BigDecimal afterBalance;

    /** 关联订单ID */
    private Long relatedOrderId;

    /** 关联提现请求ID */
    private Long relatedWithdrawalId;

    /** 资金可用日期（对于待确认余额） */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date availableDate;

    /** 描述 */
    private String description;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    // Getters and Setters
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

    public String getChangeType() {
        return changeType;
    }

    public void setChangeType(String changeType) {
        this.changeType = changeType;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getBeforeBalance() {
        return beforeBalance;
    }

    public void setBeforeBalance(BigDecimal beforeBalance) {
        this.beforeBalance = beforeBalance;
    }

    public BigDecimal getAfterBalance() {
        return afterBalance;
    }

    public void setAfterBalance(BigDecimal afterBalance) {
        this.afterBalance = afterBalance;
    }

    public Long getRelatedOrderId() {
        return relatedOrderId;
    }

    public void setRelatedOrderId(Long relatedOrderId) {
        this.relatedOrderId = relatedOrderId;
    }

    public Long getRelatedWithdrawalId() {
        return relatedWithdrawalId;
    }

    public void setRelatedWithdrawalId(Long relatedWithdrawalId) {
        this.relatedWithdrawalId = relatedWithdrawalId;
    }

    public Date getAvailableDate() {
        return availableDate;
    }

    public void setAvailableDate(Date availableDate) {
        this.availableDate = availableDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public Date getCreateTime() {
        return createTime;
    }

    @Override
    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}


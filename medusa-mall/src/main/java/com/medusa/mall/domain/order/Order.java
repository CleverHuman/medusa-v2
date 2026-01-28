package com.medusa.mall.domain.order;

import java.math.BigDecimal;
import java.util.Date;
import com.medusa.common.core.domain.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;

public class Order extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private String id;

    private String orderSn;

    private Long memberId;

    private Integer memberLevel;

    private Long vendorId; // Vendor ID (if order contains vendor products)

    private BigDecimal totalAmount;

    private BigDecimal freightAmount;

    private BigDecimal couponAmount;

    private BigDecimal discountAmount;

    private Long couponId;

    private Integer status;

    private Integer sourceType;

    private String orderType; // Order Type: NORMAL, BOND_PAYMENT

    private Long bondApplicationId; // Associated Vendor Application ID (for bond payments)

    private String bondApplicationNumber; // Application Number (for bond payments)

    private Integer isdispute;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date disputeTime;

    private String disputeReason;

    private String disputeBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date closeTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date finishTime;

    /** 余额可用日期（发货后根据 vendor level 计算） */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date balanceAvailableDate;

    /** 余额是否已释放 (0=否, 1=是) */
    private Integer isBalanceReleased;

    /** 客户备注（顾客在下单时填写的订单说明） */
    private String customerComment;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOrderSn() {
        return orderSn;
    }

    public void setOrderSn(String orderSn) {
        this.orderSn = orderSn;
    }

    public Long getMemberId() {
        return memberId;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }

    public Integer getMemberLevel() {
        return memberLevel;
    }

    public void setMemberLevel(Integer memberLevel) {
        this.memberLevel = memberLevel;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public BigDecimal getFreightAmount() {
        return freightAmount;
    }

    public void setFreightAmount(BigDecimal freightAmount) {
        this.freightAmount = freightAmount;
    }

    public BigDecimal getCouponAmount() {
        return couponAmount;
    }

    public void setCouponAmount(BigDecimal couponAmount) {
        this.couponAmount = couponAmount;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }

    public Long getCouponId() {
        return couponId;
    }

    public void setCouponId(Long couponId) {
        this.couponId = couponId;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getSourceType() {
        return sourceType;
    }

    public void setSourceType(Integer sourceType) {
        this.sourceType = sourceType;
    }

    public String getOrderType() {
        return orderType;
    }

    public void setOrderType(String orderType) {
        this.orderType = orderType;
    }

    public Long getBondApplicationId() {
        return bondApplicationId;
    }

    public void setBondApplicationId(Long bondApplicationId) {
        this.bondApplicationId = bondApplicationId;
    }

    public String getBondApplicationNumber() {
        return bondApplicationNumber;
    }

    public void setBondApplicationNumber(String bondApplicationNumber) {
        this.bondApplicationNumber = bondApplicationNumber;
    }

    public Integer getIsdispute() {
        return isdispute;
    }

    public void setIsdispute(Integer isdispute) {
        this.isdispute = isdispute;
    }

    public Date getDisputeTime() {
        return disputeTime;
    }

    public void setDisputeTime(Date disputeTime) {
        this.disputeTime = disputeTime;
    }

    public String getDisputeReason() {
        return disputeReason;
    }

    public void setDisputeReason(String disputeReason) {
        this.disputeReason = disputeReason;
    }

    public String getDisputeBy() {
        return disputeBy;
    }

    public void setDisputeBy(String disputeBy) {
        this.disputeBy = disputeBy;
    }

    public Date getCloseTime() {
        return closeTime;
    }

    public void setCloseTime(Date closeTime) {
        this.closeTime = closeTime;
    }

    public Date getFinishTime() {
        return finishTime;
    }

    public void setFinishTime(Date finishTime) {
        this.finishTime = finishTime;
    }

    public Long getVendorId() {
        return vendorId;
    }

    public void setVendorId(Long vendorId) {
        this.vendorId = vendorId;
    }

    /**
     * 判断是否为Guest订单
     */
    public boolean isGuestOrder() {
        return this.memberId != null && this.memberId < 0;
    }

    /**
     * 获取Guest ID（如果是Guest订单）
     */
    public String getGuestId() {
        if (isGuestOrder()) {
            return "GUEST_" + Math.abs(this.memberId + 1000000L);
        }
        return null;
    }

    public Date getBalanceAvailableDate() {
        return balanceAvailableDate;
    }

    public void setBalanceAvailableDate(Date balanceAvailableDate) {
        this.balanceAvailableDate = balanceAvailableDate;
    }

    public Integer getIsBalanceReleased() {
        return isBalanceReleased;
    }

    public void setIsBalanceReleased(Integer isBalanceReleased) {
        this.isBalanceReleased = isBalanceReleased;
    }

    public String getCustomerComment() {
        return customerComment;
    }

    public void setCustomerComment(String customerComment) {
        this.customerComment = customerComment;
    }
}

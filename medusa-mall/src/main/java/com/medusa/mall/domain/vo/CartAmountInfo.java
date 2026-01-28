package com.medusa.mall.domain.vo;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Cart amount information for Redis storage
 * Contains all amount-related fields for order processing
 */
public class CartAmountInfo {
    
    private BigDecimal subtotal;           // 小计金额
    private BigDecimal couponDiscount;     // 优惠券折扣
    private BigDecimal memberDiscount;     // 会员折扣
    private String currency;               // 币种
    private BigDecimal total;              // 总金额
    private Date dueTime;                  // 到期时间
    private String payType;                // 支付类型
    private BigDecimal rate;               // 汇率
    private BigDecimal totalCoin;          // 虚拟币总额
    private Long couponId;                 // 优惠券ID
    private BigDecimal shippingFee;        // 运费
    
    // Default constructor
    public CartAmountInfo() {
    }
    
    // Constructor with all fields
    public CartAmountInfo(BigDecimal subtotal, BigDecimal couponDiscount, BigDecimal memberDiscount,
                         String currency, BigDecimal total, Date dueTime, String payType, 
                         BigDecimal rate, BigDecimal totalCoin, Long couponId, BigDecimal shippingFee) {
        this.subtotal = subtotal;
        this.couponDiscount = couponDiscount;
        this.memberDiscount = memberDiscount;
        this.currency = currency;
        this.total = total;
        this.dueTime = dueTime;
        this.payType = payType;
        this.rate = rate;
        this.totalCoin = totalCoin;
        this.couponId = couponId;
        this.shippingFee = shippingFee;
    }
    
    // Getters and Setters
    public BigDecimal getSubtotal() {
        return subtotal;
    }
    
    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }
    
    public BigDecimal getCouponDiscount() {
        return couponDiscount;
    }
    
    public void setCouponDiscount(BigDecimal couponDiscount) {
        this.couponDiscount = couponDiscount;
    }
    
    public BigDecimal getMemberDiscount() {
        return memberDiscount;
    }
    
    public void setMemberDiscount(BigDecimal memberDiscount) {
        this.memberDiscount = memberDiscount;
    }
    
    public String getCurrency() {
        return currency;
    }
    
    public void setCurrency(String currency) {
        this.currency = currency;
    }
    
    public BigDecimal getTotal() {
        return total;
    }
    
    public void setTotal(BigDecimal total) {
        this.total = total;
    }
    
    public Date getDueTime() {
        return dueTime;
    }
    
    public void setDueTime(Date dueTime) {
        this.dueTime = dueTime;
    }
    
    public String getPayType() {
        return payType;
    }
    
    public void setPayType(String payType) {
        this.payType = payType;
    }
    
    public BigDecimal getRate() {
        return rate;
    }
    
    public void setRate(BigDecimal rate) {
        this.rate = rate;
    }
    
    public BigDecimal getTotalCoin() {
        return totalCoin;
    }
    
    public void setTotalCoin(BigDecimal totalCoin) {
        this.totalCoin = totalCoin;
    }
    
    public Long getCouponId() {
        return couponId;
    }
    
    public void setCouponId(Long couponId) {
        this.couponId = couponId;
    }
    
    public BigDecimal getShippingFee() {
        return shippingFee;
    }
    
    public void setShippingFee(BigDecimal shippingFee) {
        this.shippingFee = shippingFee;
    }
    
    @Override
    public String toString() {
        return "CartAmountInfo{" +
                "subtotal=" + subtotal +
                ", couponDiscount=" + couponDiscount +
                ", memberDiscount=" + memberDiscount +
                ", currency='" + currency + '\'' +
                ", total=" + total +
                ", dueTime=" + dueTime +
                ", payType='" + payType + '\'' +
                ", rate=" + rate +
                ", totalCoin=" + totalCoin +
                ", couponId=" + couponId +
                ", shippingFee=" + shippingFee +
                '}';
    }
} 
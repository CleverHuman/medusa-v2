package com.medusa.mall.domain.cart;
import java.util.List;
import java.math.BigDecimal;

public class Cart {
    private List<CartItem> items;
    
    // Additional field for static pages with complete product information
    private List<Object> detailedItems;

    private BigDecimal subtotal;

    private BigDecimal discount;

    private BigDecimal total;

    // 新增：会员等级折扣、优惠码折扣、币种
    private BigDecimal memberDiscount;
    private BigDecimal couponDiscount;
    private String currency;

    private Long couponId;

    public List<CartItem> getItems() {
        return items;
    }

    public void setItems(List<CartItem> items) {
        this.items = items;
    }

    public List<Object> getDetailedItems() {
        return detailedItems;
    }

    public void setDetailedItems(List<Object> detailedItems) {
        this.detailedItems = detailedItems;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public BigDecimal getDiscount() {
        return discount;
    }

    public void setDiscount(BigDecimal discount) {
        this.discount = discount;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public BigDecimal getMemberDiscount() {
        return memberDiscount;
    }

    public void setMemberDiscount(BigDecimal memberDiscount) {
        this.memberDiscount = memberDiscount;
    }

    public BigDecimal getCouponDiscount() {
        return couponDiscount;
    }

    public void setCouponDiscount(BigDecimal couponDiscount) {
        this.couponDiscount = couponDiscount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Long getCouponId() {
        return couponId;
    }

    public void setCouponId(Long couponId) {
        this.couponId = couponId;
    }
}

package com.medusa.mall.domain.order;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public class OrderAddRequest {
    private String orderId;
    private String userId;//tgId
    private String userName;//userName
    private List<Item> items;
    private BigDecimal total;
    private String currency;
    private ShippingAddress shippingAddress;
    private String status;
    private Date createdAt;
    private String shippingMethod;
    private String couponId;
    private String payType;
    private String sourceType;
    private String remark;
    private String customerComment;  // Customer comment/notes for the order
    private Long bondApplicationId;  // For Bond payment orders
    private String bondApplicationNumber;  // For Bond payment orders

    // getter/setter
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public List<Item> getItems() { return items; }
    public void setItems(List<Item> items) { this.items = items; }
    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public ShippingAddress getShippingAddress() { return shippingAddress; }
    public void setShippingAddress(ShippingAddress shippingAddress) { this.shippingAddress = shippingAddress; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    public String getShippingMethod() { return shippingMethod; }
    public void setShippingMethod(String shippingMethod) { this.shippingMethod = shippingMethod; }
    public String getCouponId() { return couponId; }
    public void setCouponId(String couponId) { this.couponId = couponId; }
    public String getPayType() { return payType; }
    public void setPayType(String payType) { this.payType = payType; }
    public String getSourceType() {
        return sourceType;
    }
    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }
    
    public String getRemark() {
        return remark;
    }
    
    public void setRemark(String remark) {
        this.remark = remark;
    }
    
    public String getCustomerComment() {
        return customerComment;
    }
    
    public void setCustomerComment(String customerComment) {
        this.customerComment = customerComment;
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

    public static class Item {
        private String sku;
        private Integer quantity;
        private BigDecimal price;
        private String currency;
        private BigDecimal totalPrice;
        // getter/setter
        public String getSku() { return sku; }
        public void setSku(String sku) { this.sku = sku; }
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
        public BigDecimal getPrice() { return price; }
        public void setPrice(BigDecimal price) { this.price = price; }
        public String getCurrency() { return currency; }
        public void setCurrency(String currency) { this.currency = currency; }
        public BigDecimal getTotalPrice() { return totalPrice; }
        public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }
    }
} 
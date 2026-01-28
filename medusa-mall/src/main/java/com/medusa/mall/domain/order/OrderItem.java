package com.medusa.mall.domain.order;

import java.math.BigDecimal;
import java.util.Date;

public class OrderItem {
    private String id; // Order item ID
    private String orderId; // Order ID
    private String orderSn; // Order number
    private String productId; // Product ID (changed to string)
    private Integer productOrigin; // Product origin: 0=platform, 1=vendor
    private Long originId; // Origin ID (vendor_id if product_origin=1)
    private String sku;
    private String productName; // Product name
    private String productImage; // Product image
    private String productSpec; // Product specification
    
    private BigDecimal price; // Sale price
    private Integer quantity; // Purchase quantity
    private BigDecimal totalPrice; // Product total price (price*quantity)
    private BigDecimal totalCoin; // Product total price in coin
    private BigDecimal couponAmount; // Coupon allocation amount
    private BigDecimal discountAmount; // Discount allocation amount
    private Date createTime; // Create time
    private Date updateTime; // Update time
    private String createBy; // Creator
    private String updateBy; // Updater
    private String remark; // Remark

    // getter/setter
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public String getOrderSn() { return orderSn; }
    public void setOrderSn(String orderSn) { this.orderSn = orderSn; }
    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }
    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public String getProductImage() { return productImage; }
    public void setProductImage(String productImage) { this.productImage = productImage; }
    public String getProductSpec() { return productSpec; }
    public void setProductSpec(String productSpec) { this.productSpec = productSpec; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public BigDecimal getTotalPrice() { return totalPrice; }
    public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }
    public BigDecimal getTotalCoin() { return totalCoin; }
    public void setTotalCoin(BigDecimal totalCoin) { this.totalCoin = totalCoin; }
    public BigDecimal getCouponAmount() { return couponAmount; }
    public void setCouponAmount(BigDecimal couponAmount) { this.couponAmount = couponAmount; }
    public BigDecimal getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
    public Date getUpdateTime() { return updateTime; }
    public void setUpdateTime(Date updateTime) { this.updateTime = updateTime; }
    public String getCreateBy() { return createBy; }
    public void setCreateBy(String createBy) { this.createBy = createBy; }
    public String getUpdateBy() { return updateBy; }
    public void setUpdateBy(String updateBy) { this.updateBy = updateBy; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    
    public Integer getProductOrigin() { return productOrigin; }
    public void setProductOrigin(Integer productOrigin) { this.productOrigin = productOrigin; }
    
    public Long getOriginId() { return originId; }
    public void setOriginId(Long originId) { this.originId = originId; }
} 
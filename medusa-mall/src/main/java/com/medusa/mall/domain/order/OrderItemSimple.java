package com.medusa.mall.domain.order;

import java.math.BigDecimal;

public class OrderItemSimple {
    private String sku;
    private Integer quantity;
    private BigDecimal price;
    private String currency;

    // getter/setter
    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
} 
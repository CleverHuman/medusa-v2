package com.medusa.mall.domain.vo;

import java.math.BigDecimal;

/**
 * Cart item VO for display in static pages
 * Contains complete product and cart information
 */
public class CartItemVO {
    private Long productId; // SKU ID
    private String productName; // Product name
    private String imageUrl; // Product image
    private String sku; // SKU code
    private String model; // Model specification
    private String unit; // Unit for model
    private BigDecimal price; // Price
    private String currency; // Currency
    private Integer quantity; // Quantity in cart
    private BigDecimal totalPrice; // Total price for this item

    // Getters and Setters
    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getUnit() {
        return unit;
    }
    public void setUnit(String unit) {
        this.unit = unit;
    }

    /**
     * Get formatted model display (e.g., "100g", "500g")
     * @return formatted model string
     */
    public String getFormattedModel() {
        if ((model == null || model.trim().isEmpty()) && (unit == null || unit.trim().isEmpty())) {
            return "";
        }
        String m = (model == null ? "" : model.trim());
        String u = (unit == null ? "" : unit.trim());
        
        // Smart formatting: remove unnecessary .00 for integers and trailing zeros for decimals
        if (m.matches("\\d+\\.\\d+")) {
            // Remove trailing zeros from decimals (0.50 -> 0.5, 1.20 -> 1.2)
            m = m.replaceAll("0+$", "").replaceAll("\\.$", "");
        } else if (m.matches("\\d+\\.00")) {
            // Remove .00 for integers (200.00 -> 200)
            m = m.replaceAll("\\.00$", "");
        }
        
        return m + u;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }
} 
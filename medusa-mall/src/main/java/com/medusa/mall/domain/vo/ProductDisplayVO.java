package com.medusa.mall.domain.vo;

import java.math.BigDecimal;

/**
 * Product display VO for static pages
 * Combines product and SKU information
 */
public class ProductDisplayVO {
    private Long id; // SKU ID
    private String productId; // Product ID
    private String category; // Product category
    private String name; // Product name
    private String description; // Product description
    private String imageUrl; // Product image URL
    private String sku; // SKU code
    private String model; // Model specification
    private String unit; // Unit for model
    private BigDecimal price; // Price
    private String currency; // Currency
    private Integer inventory; // Inventory
    private Integer status; // Status

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public Integer getInventory() {
        return inventory;
    }

    public void setInventory(Integer inventory) {
        this.inventory = inventory;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
} 
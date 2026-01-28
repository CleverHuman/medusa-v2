package com.medusa.mall.domain.dto;

import java.math.BigDecimal;

/**
 * Vendor Product Data Transfer Object
 * 用于接收前端提交的完整产品数据
 */
public class VendorProductDTO {
    
    // Product basic info
    private Long id;
    private String name;
    private String category;
    private String description;
    private String imageUrl;
    private String brand;
    
    // Product2/SKU info
    private String sku;
    private BigDecimal price;
    private Integer stock;
    private BigDecimal model;   // amount (e.g., 0.5, 1, 100)
    private String unit;        // unit (e.g., g, ml, pills, ea)
    private BigDecimal weight;  // weight in kg (optional)
    
    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
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

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public BigDecimal getModel() {
        return model;
    }

    public void setModel(BigDecimal model) {
        this.model = model;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public BigDecimal getWeight() {
        return weight;
    }

    public void setWeight(BigDecimal weight) {
        this.weight = weight;
    }
}


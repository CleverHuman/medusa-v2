package com.medusa.mall.domain.vo;

import com.medusa.mall.domain.Product;
import com.medusa.mall.domain.Product2;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * Vendor Product View Object
 * 包含 Product 和 Product2 的完整产品信息
 */
public class VendorProductVO {
    
    // From Product
    private Long id;
    private String productId;
    private String name;
    private String category;
    private String description;
    private String imageUrl;
    private String brand;
    private Integer status;
    private String approvalStatus;
    private String rejectionReason;
    private Date approvedTime;
    private String approvedBy;
    private Integer productOrigin;
    private Long originId;
    private String vendorName;  // Vendor name (for display only, not persisted)
    
    // From Product2 (first/default SKU - kept for backward compatibility)
    private Long skuId;
    private String sku;
    private BigDecimal price;
    private Integer inventory;
    private BigDecimal model;   // amount
    private String unit;
    private String currency;
    
    // All SKUs for this product
    private List<Product2> skus;
    
    // Constructors
    public VendorProductVO() {}
    
    public VendorProductVO(Product product, Product2 product2) {
        if (product != null) {
            this.id = product.getId();
            this.productId = product.getProductId();
            this.name = product.getName();
            this.category = product.getCategory();
            this.description = product.getDescription();
            this.imageUrl = product.getImageUrl();
            this.brand = product.getBrand();
            this.status = product.getStatus();
            this.approvalStatus = product.getApprovalStatus();
            this.rejectionReason = product.getRejectionReason();
            this.approvedTime = product.getApprovedTime();
            this.approvedBy = product.getApprovedBy();
            this.productOrigin = product.getProductOrigin();
            this.originId = product.getOriginId();
        }
        
        if (product2 != null) {
            this.skuId = product2.getId();
            this.sku = product2.getSku();
            this.price = product2.getPrice();
            this.inventory = product2.getInventory();
            this.model = product2.getModel();
            this.unit = product2.getUnit();
            this.currency = product2.getCurrency();
        }
    }
    
    // Alias for inventory to match frontend
    public Integer getStock() {
        return inventory;
    }
    
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

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getApprovalStatus() {
        return approvalStatus;
    }

    public void setApprovalStatus(String approvalStatus) {
        this.approvalStatus = approvalStatus;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public Date getApprovedTime() {
        return approvedTime;
    }

    public void setApprovedTime(Date approvedTime) {
        this.approvedTime = approvedTime;
    }

    public String getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(String approvedBy) {
        this.approvedBy = approvedBy;
    }

    public Integer getProductOrigin() {
        return productOrigin;
    }

    public void setProductOrigin(Integer productOrigin) {
        this.productOrigin = productOrigin;
    }

    public Long getOriginId() {
        return originId;
    }

    public void setOriginId(Long originId) {
        this.originId = originId;
    }

    public String getVendorName() {
        return vendorName;
    }

    public void setVendorName(String vendorName) {
        this.vendorName = vendorName;
    }

    public Long getSkuId() {
        return skuId;
    }

    public void setSkuId(Long skuId) {
        this.skuId = skuId;
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

    public Integer getInventory() {
        return inventory;
    }

    public void setInventory(Integer inventory) {
        this.inventory = inventory;
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

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public List<Product2> getSkus() {
        return skus;
    }

    public void setSkus(List<Product2> skus) {
        this.skus = skus;
    }
}


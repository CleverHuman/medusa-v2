package com.medusa.mall.domain;

import com.medusa.common.annotation.Excel;
import com.medusa.common.core.domain.BaseEntity;
import java.util.Date;

/**
 * Product master object mall_product
 */
public class Product extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** Primary key ID */
    private Long id;

    /** Product ID (logical identifier) */
    @Excel(name = "Product ID")
    private String productId;

    /** Product category */
    @Excel(name = "Category")
    private String category;

    /** Category ID (foreign key to mall_category) */
    @Excel(name = "Category ID")
    private Long categoryId;

    /** Product name */
    @Excel(name = "Product Name")
    private String name;

    /** Product description */
    @Excel(name = "Description")
    private String description;

    /** Product image URL */
    @Excel(name = "Image URL")
    private String imageUrl;

    /** Product brand (for vendor products) */
    @Excel(name = "Brand")
    private String brand;

    /** Product status (0=inactive, 1=active) */
    @Excel(name = "Status", readConverterExp = "0=inactive,1=active")
    private Integer status;

    /** Approval status (PENDING_APPROVAL, APPROVED, REJECTED) */
    @Excel(name = "Approval Status")
    private String approvalStatus;

    /** Rejection reason if rejected */
    @Excel(name = "Rejection Reason")
    private String rejectionReason;

    /** Approval timestamp */
    @Excel(name = "Approved Time", dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date approvedTime;

    /** Approver username */
    @Excel(name = "Approved By")
    private String approvedBy;

    /** Shop ID */
    @Excel(name = "Shop ID")
    private Long shopId;

    /** Sales channel */
    @Excel(name = "Channel")
    private String channel;

    /** Product origin (0=platform, 1=vendor) */
    @Excel(name = "Product Origin", readConverterExp = "0=platform,1=vendor")
    private Integer productOrigin;

    /** Origin ID (VENDOR ID for vendor products) */
    @Excel(name = "Origin ID")
    private Long originId;

    /** Vendor name (transient, not persisted - for display only) */
    @Excel(name = "Vendor Name")
    private String vendorName;

    /** Sort order for display (smaller number = higher priority) */
    @Excel(name = "Sort Order")
    private Integer sortOrder;

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

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
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

    public Long getShopId() {
        return shopId;
    }

    public void setShopId(Long shopId) {
        this.shopId = shopId;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
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

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    /**
     * Check if this is a platform product
     */
    public boolean isPlatformProduct() {
        return productOrigin == null || productOrigin == 0;
    }

    /**
     * Check if this is a vendor product
     */
    public boolean isVendorProduct() {
        return productOrigin != null && productOrigin == 1;
    }
} 
package com.medusa.mall.domain;

import com.medusa.common.annotation.Excel;
import com.medusa.common.core.domain.BaseEntity;

/**
 * Product Category object mall_category
 */
public class Category extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** Primary key ID */
    private Long id;

    /** Category name */
    @Excel(name = "Category Name")
    private String categoryName;

    /** Category code */
    @Excel(name = "Category Code")
    private String categoryCode;

    /** Category description */
    @Excel(name = "Description")
    private String description;

    /** Category image URL */
    @Excel(name = "Image URL")
    private String imageUrl;

    /** Category icon URL */
    @Excel(name = "Icon URL")
    private String iconUrl;

    /** Status (0=disabled, 1=enabled) */
    @Excel(name = "Status", readConverterExp = "0=disabled,1=enabled")
    private Integer status;

    /** Sort order */
    @Excel(name = "Sort Order")
    private Integer sortOrder;

    /** Is featured (0=no, 1=yes) */
    @Excel(name = "Is Featured", readConverterExp = "0=no,1=yes")
    private Integer isFeatured;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getCategoryCode() {
        return categoryCode;
    }

    public void setCategoryCode(String categoryCode) {
        this.categoryCode = categoryCode;
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

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public Integer getIsFeatured() {
        return isFeatured;
    }

    public void setIsFeatured(Integer isFeatured) {
        this.isFeatured = isFeatured;
    }
} 
package com.medusa.mall.domain;

import java.math.BigDecimal;
import com.medusa.common.annotation.Excel;
import com.medusa.common.core.domain.BaseEntity;

/**
 * Product SKU object mall_product2
 */
public class Product2 extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** SKU ID */
    private Long id;

    /** Product ID (foreign key to mall_product) */
    @Excel(name = "Product ID")
    private String productId;

    /** SKU code (unique identifier for variant) */
    @Excel(name = "SKU Code")
    private String sku;

    /** 规格型号 - 支持小数 */
    @Excel(name = "productSpec")
    private BigDecimal model;

    /** Price */
    @Excel(name = "Price")
    private BigDecimal price;

    /** Currency code (e.g., USD, AUD) */
    @Excel(name = "Currency")
    private String currency;

    /** Inventory quantity */
    @Excel(name = "Inventory")
    private Integer inventory;

    /** SKU status (0=inactive, 1=active) */
    @Excel(name = "Status", readConverterExp = "0=inactive,1=active")
    private Integer status;

    /** 单位（g, ml, pills, ea） */
    private String unit;

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

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public BigDecimal getModel() {
        return model;
    }

    public void setModel(BigDecimal model) {
        this.model = model;
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

    public String getUnit() {
        return unit;
    }
    public void setUnit(String unit) {
        this.unit = unit;
    }
} 
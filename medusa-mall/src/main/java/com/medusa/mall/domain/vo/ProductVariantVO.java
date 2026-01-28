package com.medusa.mall.domain.vo;

import java.math.BigDecimal;

/**
 * Product Variant VO
 */
public class ProductVariantVO {
    private String sku;
    private String model;
    private String unit; // Unit for model
    private BigDecimal price;
    private String currency;
    private Integer inventory;

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
} 
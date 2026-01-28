package com.medusa.mall.domain.vo;

import java.util.List;

/**
 * Product VO
 */
public class ProductVO {
    private String name;
    private String id;
    private List<ProductVariantVO> variants;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<ProductVariantVO> getVariants() {
        return variants;
    }

    public void setVariants(List<ProductVariantVO> variants) {
        this.variants = variants;
    }
} 
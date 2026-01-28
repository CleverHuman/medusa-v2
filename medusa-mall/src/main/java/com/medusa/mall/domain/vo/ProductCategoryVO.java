package com.medusa.mall.domain.vo;

import java.util.List;

/**
 * Product Category VO
 */
public class ProductCategoryVO {
    private String name;
    private List<ProductVO> products;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<ProductVO> getProducts() {
        return products;
    }

    public void setProducts(List<ProductVO> products) {
        this.products = products;
    }
} 
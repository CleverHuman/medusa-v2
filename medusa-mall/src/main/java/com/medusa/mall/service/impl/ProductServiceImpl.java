package com.medusa.mall.service.impl;

import com.medusa.mall.domain.Product;
import com.medusa.mall.domain.Product2;
import com.medusa.mall.domain.vo.ProductDisplayVO;
import com.medusa.mall.mapper.ProductMapper;
import com.medusa.mall.mapper.Product2Mapper;
import com.medusa.mall.service.IProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Product Service implementation
 */
@Service
public class ProductServiceImpl implements IProductService {
    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private Product2Mapper product2Mapper;

    /**
     * Query product by ID
     * 
     * @param id Product ID
     * @return Product
     */
    @Override
    public Product selectProductById(Long id) {
        return productMapper.selectProductById(id);
    }

    /**
     * Query product by product ID
     * 
     * @param productId Product ID (logical identifier)
     * @return Product
     */
    @Override
    public Product selectProductByProductId(String productId) {
        return productMapper.selectProductByProductId(productId);
    }

    /**
     * Query product list (for users - only active products)
     * 
     * @param product Product filter
     * @return Product list
     */
    @Override
    public List<Product> selectProductList(Product product) {
        if (product == null) {
            product = new Product();
        }
        // User side only shows active products
        product.setStatus(1);
        return productMapper.selectProductList(product);
    }

    /**
     * Query product list (for admin - all products including inactive)
     * 
     * @param product Product filter
     * @param includeInactive Whether to include inactive products
     * @return Product list
     */
    @Override
    public List<Product> selectProductList(Product product, boolean includeInactive) {
        if (product == null) {
            product = new Product();
        }
        
        if (!includeInactive) {
            // If not including inactive products, only show active ones
            product.setStatus(1);
        }
        // If includeInactive is true, don't set status, show all products (except deleted)
        
        return productMapper.selectProductList(product);
    }

    /**
     * Query product list for admin (all non-deleted products)
     * 
     * @param product Product filter
     * @return Product list
     */
    public List<Product> selectProductListForAdmin(Product product) {
        if (product == null) {
            product = new Product();
        }
        // Admin side shows all non-deleted products (status 0 and 1)
        // Don't set status filter, let mapper handle it with status != '2'
        return productMapper.selectProductList(product);
    }

    /**
     * Insert product
     * 
     * @param product Product
     * @return Result
     */
    @Override
    public int insertProduct(Product product) {
        return productMapper.insertProduct(product);
    }

    /**
     * Update product
     * 
     * @param product Product
     * @return Result
     */
    @Override
    public int updateProduct(Product product) {
        return productMapper.updateProduct(product);
    }

    /**
     * Delete product by ID
     * 
     * @param id Product ID
     * @return Result
     */
    @Override
    public int deleteProductById(Long id) {
        return productMapper.deleteProductById(id);
    }

    /**
     * Batch delete products
     * 
     * @param ids Product ID array
     * @return Result
     */
    @Override
    public int deleteProductByIds(Long[] ids) {
        return productMapper.deleteProductByIds(ids);
    }

    /**
     * Get all product categories
     * 
     * @return Category list
     */
    @Override
    public List<String> selectAllCategories() {
        return productMapper.selectAllCategories();
    }

    /**
     * Query products by category
     * 
     * @param category Category
     * @return Product list
     */
    @Override
    public List<Product> selectProductsByCategory(String category) {
        return productMapper.selectProductsByCategory(category);
    }

    /**
     * Query products by category ID
     * 
     * @param categoryId Category ID
     * @return Product list
     */
    @Override
    public List<Product> selectProductsByCategoryId(Long categoryId) {
        return productMapper.selectProductsByCategoryId(categoryId);
    }

    /**
     * Get product names by category for dropdown
     * 
     * @param category Category (can be null for all categories)
     * @return List of product names
     */
    @Override
    public List<String> selectProductNamesByCategory(String category) {
        Product productFilter = new Product();
        productFilter.setCategory(category);
        productFilter.setStatus(1); // Only active products
        
        List<Product> products = productMapper.selectProductList(productFilter);
        return products.stream()
                .map(Product::getName)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * Get product display list with SKU information
     * 
     * @param category Category filter
     * @param name Name filter
     * @return Product display list
     */
    @Override
    public List<ProductDisplayVO> selectProductDisplayList(String category, String name) {
        List<ProductDisplayVO> result = new ArrayList<>();
        
        // Create product filter
        Product productFilter = new Product();
        productFilter.setCategory(category);
        productFilter.setName(name);
        productFilter.setStatus(1); // Only active products
        
        // Get products
        List<Product> products = productMapper.selectProductList(productFilter);
        
        // Filter out TG channel products for public display
        products = products.stream()
            .filter(product -> {
                String channel = product.getChannel();
                // Exclude products with channel "1" (TG - Telegram only)
                // Channel mapping: "0"=OS, "1"=TG, "3"=OS/TG
                return channel == null || !"1".equals(channel);
            })
            .collect(Collectors.toList());
        
        // For each product, get its SKUs and create display objects
        for (Product product : products) {
            Product2 skuFilter = new Product2();
            skuFilter.setProductId(product.getProductId());
            // Don't filter by SKU status to show all SKUs
            
            List<Product2> skus = product2Mapper.selectProduct2List(skuFilter);
            
            for (Product2 sku : skus) {
                ProductDisplayVO displayVO = new ProductDisplayVO();
                displayVO.setId(sku.getId());
                displayVO.setProductId(product.getProductId());
                displayVO.setCategory(product.getCategory());
                displayVO.setName(product.getName());
                displayVO.setDescription(product.getDescription());
                displayVO.setImageUrl(product.getImageUrl());
                displayVO.setSku(sku.getSku());
                displayVO.setModel(sku.getModel() != null ? sku.getModel().setScale(2, java.math.RoundingMode.HALF_UP).toString() : "");
                displayVO.setPrice(sku.getPrice());
                displayVO.setCurrency(sku.getCurrency());
                displayVO.setInventory(sku.getInventory());
                displayVO.setStatus(sku.getStatus());
                
                result.add(displayVO);
            }
        }
        
        return result;
    }

    /**
     * Query product list for Telegram (only TG and OS/TG channel products)
     * 
     * @param product Product filter
     * @return Product list
     */
    @Override
    public List<Product> selectProductListForTelegram(Product product) {
        // 获取所有产品
        List<Product> allProducts = productMapper.selectProductList(product);
        
        // 过滤产品，只保留TG (1) 和 OS/TG (3) 渠道的产品
        return allProducts.stream()
            .filter(p -> {
                String channel = p.getChannel();
                return "1".equals(channel) || "3".equals(channel);
            })
            .collect(Collectors.toList());
    }

    /**
     * Update single product sort order
     * 
     * @param product Product with updated sort order
     * @return Result
     */
    @Override
    public int updateProductSortOrder(Product product) {
        return productMapper.updateProductSortOrder(product);
    }
}

package com.medusa.mall.service;

import com.medusa.mall.domain.Product;
import com.medusa.mall.domain.vo.ProductDisplayVO;
import java.util.List;

/**
 * Product Service interface
 */
public interface IProductService {
    /**
     * Query product by ID
     * 
     * @param id Product ID
     * @return Product
     */
    public Product selectProductById(Long id);

    /**
     * Query product by product ID
     * 
     * @param productId Product ID (logical identifier)
     * @return Product
     */
    public Product selectProductByProductId(String productId);

    /**
     * Query product list (for users - only active products)
     * 
     * @param product Product filter
     * @return Product list
     */
    public List<Product> selectProductList(Product product);

    /**
     * Query product list (for admin - all products including inactive)
     * 
     * @param product Product filter
     * @param includeInactive Whether to include inactive products
     * @return Product list
     */
    public List<Product> selectProductList(Product product, boolean includeInactive);

    /**
     * Query product list for Telegram (only TG and OS/TG channel products)
     * 
     * @param product Product filter
     * @return Product list
     */
    public List<Product> selectProductListForTelegram(Product product);

    /**
     * Insert product
     * 
     * @param product Product
     * @return Result
     */
    public int insertProduct(Product product);

    /**
     * Update product
     * 
     * @param product Product
     * @return Result
     */
    public int updateProduct(Product product);

    /**
     * Delete product by ID
     * 
     * @param id Product ID
     * @return Result
     */
    public int deleteProductById(Long id);

    /**
     * Batch delete products
     * 
     * @param ids Product ID array
     * @return Result
     */
    public int deleteProductByIds(Long[] ids);

    /**
     * Get all product categories
     * 
     * @return Category list
     */
    public List<String> selectAllCategories();

    /**
     * Query products by category
     * 
     * @param category Category
     * @return Product list
     */
    public List<Product> selectProductsByCategory(String category);

    /**
     * Query products by category ID
     * 
     * @param categoryId Category ID
     * @return Product list
     */
    public List<Product> selectProductsByCategoryId(Long categoryId);

    /**
     * Get product display list with SKU information
     * 
     * @param category Category filter
     * @param name Name filter
     * @return Product display list
     */
    public List<ProductDisplayVO> selectProductDisplayList(String category, String name);

    /**
     * Get product names by category for dropdown
     * 
     * @param category Category (can be null for all categories)
     * @return List of product names
     */
    public List<String> selectProductNamesByCategory(String category);

    /**
     * Update single product sort order
     * 
     * @param product Product with updated sort order
     * @return Result
     */
    public int updateProductSortOrder(Product product);
}

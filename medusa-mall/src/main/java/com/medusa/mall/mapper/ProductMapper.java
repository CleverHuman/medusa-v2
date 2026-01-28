package com.medusa.mall.mapper;

import com.medusa.mall.domain.Product;
import java.util.List;

/**
 * Product Mapper interface
 */
public interface ProductMapper {
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
     * Query product list
     * 
     * @param product Product filter
     * @return Product list
     */
    public List<Product> selectProductList(Product product);

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
     * Update single product sort order
     * 
     * @param product Product with updated sort order
     * @return Result
     */
    public int updateProductSortOrder(Product product);
}

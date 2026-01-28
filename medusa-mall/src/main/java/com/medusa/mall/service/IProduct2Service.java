package com.medusa.mall.service;

import java.util.List;
import com.medusa.mall.domain.Product2;
import com.medusa.mall.domain.vo.ProductCategoryVO;

/**
 * 商品信息Service接口
 */
public interface IProduct2Service {
    /**
     * 查询商品信息
     * 
     * @param id 商品信息主键
     * @return 商品信息
     */
    public Product2 selectProduct2ById(Long id);

    /**
     * 查询商品信息列表
     * 
     * @param product2 商品信息
     * @return 商品信息集合
     */
    public List<Product2> selectProduct2List(Product2 product2);

    /**
     * 新增商品信息
     * 
     * @param product2 商品信息
     * @return 结果
     */
    public int insertProduct2(Product2 product2);

    /**
     * 修改商品信息
     * 
     * @param product2 商品信息
     * @return 结果
     */
    public int updateProduct2(Product2 product2);

    /**
     * 批量删除商品信息
     * 
     * @param ids 需要删除的商品信息主键集合
     * @return 结果
     */
    public int deleteProduct2ByIds(Long[] ids);

    /**
     * 删除商品信息信息
     * 
     * @param id 商品信息主键
     * @return 结果
     */
    public int deleteProduct2ById(Long id);

    /**
     * 获取所有商品类别
     * 
     * @return 商品类别列表
     */
    public List<String> selectAllCategories();

    /**
     * 根据类别查询商品列表
     * 
     * @param category 商品类别
     * @return 商品列表
     */
    public List<Product2> selectProduct2ByCategory(String category);

    /**
     * Get all product categories with their products and variants
     * 
     * @return List of product categories
     */
    List<ProductCategoryVO> getAllProductCategories();

    /**
     * 根据SKU查询商品信息
     * 
     * @param sku 商品SKU
     * @return 商品信息
     */
    public Product2 selectProduct2BySku(String sku);

    /**
     * 根据产品ID查询商品列表
     * 
     * @param productId 产品ID
     * @return 商品列表
     */
    public List<Product2> selectProduct2ListByProductId(Long productId);
} 
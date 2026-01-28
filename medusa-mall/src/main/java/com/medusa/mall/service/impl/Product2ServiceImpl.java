package com.medusa.mall.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.medusa.mall.mapper.Product2Mapper;
import com.medusa.mall.mapper.ProductMapper;
import com.medusa.mall.domain.Product2;
import com.medusa.mall.domain.Product;
import com.medusa.mall.domain.vo.ProductCategoryVO;
import com.medusa.mall.domain.vo.ProductVO;
import com.medusa.mall.domain.vo.ProductVariantVO;
import com.medusa.mall.service.IProduct2Service;
import com.medusa.mall.service.IProductService;

/**
 * 商品信息Service业务层处理
 */
@Service
public class Product2ServiceImpl implements IProduct2Service {
    @Autowired
    private Product2Mapper product2Mapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private IProductService productService;

    /**
     * 查询商品信息
     * 
     * @param id 商品信息主键
     * @return 商品信息
     */
    @Override
    public Product2 selectProduct2ById(Long id) {
        return product2Mapper.selectProduct2ById(id);
    }

    /**
     * Query product2 list (for users - only active products)
     * 
     * @param product2 Product2 filter
     * @return Product2 list
     */
    @Override
    public List<Product2> selectProduct2List(Product2 product2) {
        if (product2 == null) {
            product2 = new Product2();
        }
        // User side only shows active products
        product2.setStatus(1);
        return product2Mapper.selectProduct2List(product2);
    }

    /**
     * Query product2 list for admin (all non-deleted products)
     * 
     * @param product2 Product2 filter
     * @return Product2 list
     */
    public List<Product2> selectProduct2ListForAdmin(Product2 product2) {
        if (product2 == null) {
            product2 = new Product2();
        }
        // Admin side shows all non-deleted products (status 0 and 1)
        // Don't set status filter, let mapper handle it with status != '2'
        return product2Mapper.selectProduct2List(product2);
    }

    /**
     * 新增商品信息
     * 
     * @param product2 商品信息
     * @return 结果
     */
    @Override
    public int insertProduct2(Product2 product2) {
        return product2Mapper.insertProduct2(product2);
    }

    /**
     * 修改商品信息
     * 
     * @param product2 商品信息
     * @return 结果
     */
    @Override
    public int updateProduct2(Product2 product2) {
        return product2Mapper.updateProduct2(product2);
    }

    /**
     * 批量删除商品信息
     * 
     * @param ids 需要删除的商品信息主键
     * @return 结果
     */
    @Override
    public int deleteProduct2ByIds(Long[] ids) {
        return product2Mapper.deleteProduct2ByIds(ids);
    }

    /**
     * 删除商品信息信息
     * 
     * @param id 商品信息主键
     * @return 结果
     */
    @Override
    public int deleteProduct2ById(Long id) {
        return product2Mapper.deleteProduct2ById(id);
    }

    /**
     * 获取所有商品类别
     * 
     * @return 商品类别列表
     */
    @Override
    public List<String> selectAllCategories() {
        return product2Mapper.selectAllCategories();
    }

    /**
     * 根据类别查询商品列表
     * 
     * @param category 商品类别
     * @return 商品列表
     */
    @Override
    public List<Product2> selectProduct2ByCategory(String category) {
        return product2Mapper.selectProduct2ByCategory(category);
    }

    /**
     * 获取所有商品类别（Telegram专用 - 只显示TG和OS/TG渠道的产品）
     * 
     * @return 商品类别列表
     */
    @Override
    public List<ProductCategoryVO> getAllProductCategories() {
        // Get all active products from Product table
        Product productFilter = new Product();
        productFilter.setStatus(1); // Only active products
        List<Product> products = productMapper.selectProductList(productFilter);
        
        // Filter products by channel - only show TG (1) and OS/TG (3) products
        List<Product> filteredProducts = products.stream()
            .filter(product -> {
                String channel = product.getChannel();
                // Only include products with channel "1" (TG) or "3" (OS/TG)
                return "1".equals(channel) || "3".equals(channel);
            })
            .collect(Collectors.toList());
        
        // Group products by category
        Map<String, List<Product>> categoryMap = filteredProducts.stream()
            .collect(Collectors.groupingBy(Product::getCategory));
        
        // Convert to VO objects
        List<ProductCategoryVO> categories = new ArrayList<>();
        categoryMap.forEach((categoryName, productList) -> {
            ProductCategoryVO categoryVO = new ProductCategoryVO();
            categoryVO.setName(categoryName);
            
            List<ProductVO> productVOs = new ArrayList<>();
            for (Product product : productList) {
                // Get active SKUs for this product
                Product2 skuFilter = new Product2();
                skuFilter.setProductId(product.getProductId());
                skuFilter.setStatus(1); // Only active SKUs
                List<Product2> variants = product2Mapper.selectProduct2List(skuFilter);
                
                if (!variants.isEmpty()) {
                    ProductVO productVO = new ProductVO();
                    productVO.setName(product.getName());
                    productVO.setId(product.getProductId());
                    
                    // Convert variants
                    List<ProductVariantVO> variantVOs = variants.stream().map(variant -> {
                        ProductVariantVO variantVO = new ProductVariantVO();
                        variantVO.setSku(variant.getSku());
                        variantVO.setModel(variant.getModel() != null ? variant.getModel().setScale(2, java.math.RoundingMode.HALF_UP).toString() : "");
                        variantVO.setUnit(variant.getUnit());
                        variantVO.setPrice(variant.getPrice());
                        variantVO.setCurrency(variant.getCurrency());
                        variantVO.setInventory(variant.getInventory());
                        return variantVO;
                    }).collect(Collectors.toList());
                    
                    productVO.setVariants(variantVOs);
                    productVOs.add(productVO);
                }
            }
            
            categoryVO.setProducts(productVOs);
            categories.add(categoryVO);
        });
        
        return categories;
    }

    /**
     * 根据SKU查询商品信息
     * 
     * @param sku 商品SKU
     * @return 商品信息
     */
    @Override
    public Product2 selectProduct2BySku(String sku) {
        return product2Mapper.selectProduct2BySku(sku);
    }

    /**
     * 根据产品ID查询商品列表
     * 
     * @param productId 产品ID
     * @return 商品列表
     */
    @Override
    public List<Product2> selectProduct2ListByProductId(Long productId) {
        Product2 filter = new Product2();
        filter.setProductId(productId.toString());
        return product2Mapper.selectProduct2List(filter);
    }
} 
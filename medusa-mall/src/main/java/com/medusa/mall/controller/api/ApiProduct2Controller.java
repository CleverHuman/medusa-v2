package com.medusa.mall.controller.api;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;
import com.medusa.common.annotation.Anonymous;
import com.medusa.common.core.controller.BaseController;
import com.medusa.common.core.domain.AjaxResult;
import com.medusa.mall.domain.Product2;
import com.medusa.mall.domain.vo.ProductCategoryVO;
import com.medusa.mall.service.IProduct2Service;

/**
 * 商品信息API接口
 */
@RestController
@CrossOrigin
@RequestMapping("/api/mall/product2")
public class ApiProduct2Controller extends BaseController {
    @Autowired
    private IProduct2Service product2Service;

    /**
     * 查询商品信息列表（用户端 - 默认只显示激活的产品）
     */
    @GetMapping("/list")
    public AjaxResult list(
            @RequestParam(required = false) String productId,
            @RequestParam(required = false) String sku,
            @RequestParam(required = false) Integer status) {
        try {
            Product2 product2 = new Product2();
            product2.setProductId(productId);
            product2.setSku(sku);
            // For user side, only show active products by default
            product2.setStatus(status != null ? status : 1);
            List<Product2> list = product2Service.selectProduct2List(product2);
            return success(list);
        } catch (Exception e) {
            logger.error("Failed to get product list", e);
            return AjaxResult.error("Failed to get product list: " + e.getMessage());
        }
    }

    @GetMapping("/list2")
    public AjaxResult list2(
            @RequestParam(required = false) String productId,
            @RequestParam(required = false) String sku,
            @RequestParam(required = false) Integer status) {
        try {
            Product2 product2 = new Product2();
            product2.setProductId(productId);
            product2.setSku(sku);
            // For user side, only show active products by default
            product2.setStatus(status != null ? status : 1);
            List<Product2> list = product2Service.selectProduct2List(product2);
            return success(list);
        } catch (Exception e) {
            logger.error("Failed to get product list", e);
            return AjaxResult.error("Failed to get product list: " + e.getMessage());
        }
    }

    /**
     * 获取所有商品类别
     */
    @GetMapping("/categories")
    public AjaxResult getCategories() {
        try {
            return success(product2Service.selectAllCategories());
        } catch (Exception e) {
            logger.error("Failed to get categories", e);
            return AjaxResult.error("Failed to get categories: " + e.getMessage());
        }
    }

    /**
     * 根据类别查询商品列表
     */
    @GetMapping("/category/{category}")
    public AjaxResult getByCategory(@PathVariable("category") String category) {
        try {
            return success(product2Service.selectProduct2ByCategory(category));
        } catch (Exception e) {
            logger.error("Failed to get products by category", e);
            return AjaxResult.error("Failed to get products by category: " + e.getMessage());
        }
    }

    /**
     * 获取商品信息详细信息
     */
    @GetMapping("/detail/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        try {
            return success(product2Service.selectProduct2ById(id));
        } catch (Exception e) {
            logger.error("Failed to get product detail", e);
            return AjaxResult.error("Failed to get product detail: " + e.getMessage());
        }
    }

    /**
     * Get all product categories with their products and variants
     * 添加@Anonymous注解以支持临时用户访问
     */
    @Anonymous
    @GetMapping("/all")
    public AjaxResult getAllProductCategories() {
        try {
            List<ProductCategoryVO> categories = product2Service.getAllProductCategories();
            return success(categories);
        } catch (Exception e) {
            logger.error("Failed to get all product categories", e);
            return AjaxResult.error("Failed to get all product categories: " + e.getMessage());
        }
    }
} 
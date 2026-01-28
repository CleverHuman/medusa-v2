package com.medusa.web.controller.mall;

import java.util.List;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.medusa.common.annotation.Log;
import com.medusa.common.core.controller.BaseController;
import com.medusa.common.core.domain.AjaxResult;
import com.medusa.common.enums.BusinessType;
import com.medusa.mall.domain.Product;
import com.medusa.mall.service.IProductService;
import com.medusa.common.utils.poi.ExcelUtil;
import com.medusa.common.core.page.TableDataInfo;
import com.medusa.mall.service.impl.ProductServiceImpl;

/**
 * Product Controller for Admin Management
 */
@RestController
@RequestMapping("/admin/mall/product")
public class ProductController extends BaseController {
    @Autowired
    private IProductService productService;

    /**
     * 查询产品列表
     */
    @PreAuthorize("@ss.hasPermi('mall:product:list')")
    @GetMapping("/list")
    public TableDataInfo list(Product product) {
        startPage();
        // Admin can see all products (including inactive ones)
        List<Product> list = ((ProductServiceImpl) productService).selectProductListForAdmin(product);
        return getDataTable(list);
    }

    /**
     * 导出产品列表
     */
    @PreAuthorize("@ss.hasPermi('mall:product:export')")
    @Log(title = "产品管理", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, Product product) {
        List<Product> list = ((ProductServiceImpl) productService).selectProductListForAdmin(product);
        ExcelUtil<Product> util = new ExcelUtil<Product>(Product.class);
        util.exportExcel(response, list, "产品数据");
    }

    /**
     * 获取产品详细信息
     */
    @PreAuthorize("@ss.hasPermi('mall:product:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return success(productService.selectProductById(id));
    }

    /**
     * 新增产品
     */
    @PreAuthorize("@ss.hasPermi('mall:product:add')")
    @Log(title = "产品管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody Product product) {
        return toAjax(productService.insertProduct(product));
    }

    /**
     * 修改产品
     */
    @PreAuthorize("@ss.hasPermi('mall:product:edit')")
    @Log(title = "产品管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody Product product) {
        return toAjax(productService.updateProduct(product));
    }

    /**
     * 删除产品
     */
    @PreAuthorize("@ss.hasPermi('mall:product:remove')")
    @Log(title = "产品管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(productService.deleteProductByIds(ids));
    }

    /**
     * 获取所有产品类别
     */
    @GetMapping("/categories")
    public AjaxResult getCategories() {
        return success(productService.selectAllCategories());
    }

    /**
     * 根据类别查询产品列表
     */
    @GetMapping("/category/{category}")
    public AjaxResult getProductsByCategory(@PathVariable String category) {
        return success(productService.selectProductsByCategory(category));
    }

    /**
     * 批量更新产品排序
     */
    @PreAuthorize("@ss.hasPermi('mall:product:edit')")
    @Log(title = "产品排序", businessType = BusinessType.UPDATE)
    @PutMapping("/sort")
    public AjaxResult updateSortOrder(@RequestBody List<Product> products) {
        try {
            System.out.println("🔄 [ProductController] Received sort order update request:");
            for (Product product : products) {
                System.out.println("  - Product ID: " + product.getId() + ", Sort Order: " + product.getSortOrder());
            }
            
            // Update each product individually
            int totalUpdated = 0;
            for (Product product : products) {
                int result = productService.updateProductSortOrder(product);
                totalUpdated += result;
            }
            
            System.out.println("✅ [ProductController] Total updated: " + totalUpdated);
            
            return toAjax(totalUpdated);
        } catch (Exception e) {
            System.err.println("❌ [ProductController] Update failed: " + e.getMessage());
            e.printStackTrace();
            return error("更新排序失败: " + e.getMessage());
        }
    }
} 
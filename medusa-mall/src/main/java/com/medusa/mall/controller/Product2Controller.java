package com.medusa.mall.controller;

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
import com.medusa.mall.domain.Product2;
import com.medusa.mall.service.IProduct2Service;
import com.medusa.common.utils.poi.ExcelUtil;
import com.medusa.common.core.page.TableDataInfo;
import com.medusa.mall.service.impl.Product2ServiceImpl;

/**
 * 商品信息Controller
 */
@RestController
@RequestMapping("/mall/product2")
public class Product2Controller extends BaseController {
    @Autowired
    private IProduct2Service product2Service;

    /**
     * 查询商品信息列表
     */
    @PreAuthorize("@ss.hasPermi('mall:product2:list')")
    @GetMapping("/list")
    public TableDataInfo list(Product2 product2) {
        startPage();
        // Admin can see all products (including inactive ones)
        List<Product2> list = ((Product2ServiceImpl) product2Service).selectProduct2ListForAdmin(product2);
        return getDataTable(list);
    }

    /**
     * 导出商品信息列表
     */
    @PreAuthorize("@ss.hasPermi('mall:product2:export')")
    @Log(title = "商品信息", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, Product2 product2) {
        // Admin can see all products (including inactive ones)
        List<Product2> list = ((Product2ServiceImpl) product2Service).selectProduct2ListForAdmin(product2);
        ExcelUtil<Product2> util = new ExcelUtil<Product2>(Product2.class);
        util.exportExcel(response, list, "商品信息数据");
    }

    /**
     * 获取商品信息详细信息
     */
    @PreAuthorize("@ss.hasPermi('mall:product2:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return success(product2Service.selectProduct2ById(id));
    }

    /**
     * 新增商品信息
     */
    @PreAuthorize("@ss.hasPermi('mall:product2:add')")
    @Log(title = "商品信息", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody Product2 product2) {
        return toAjax(product2Service.insertProduct2(product2));
    }

    /**
     * 修改商品信息
     */
    @PreAuthorize("@ss.hasPermi('mall:product2:edit')")
    @Log(title = "商品信息", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody Product2 product2) {
        return toAjax(product2Service.updateProduct2(product2));
    }

    /**
     * 删除商品信息
     */
    @PreAuthorize("@ss.hasPermi('mall:product2:remove')")
    @Log(title = "商品信息", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(product2Service.deleteProduct2ByIds(ids));
    }

    /**
     * 获取所有商品类别
     */
    @PreAuthorize("@ss.hasPermi('mall:product2:list')")
    @GetMapping("/categories")
    public AjaxResult getCategories() {
        return success(product2Service.selectAllCategories());
    }

    /**
     * 根据类别查询商品列表
     */
    @PreAuthorize("@ss.hasPermi('mall:product2:list')")
    @GetMapping("/category/{category}")
    public AjaxResult getByCategory(@PathVariable("category") String category) {
        return success(product2Service.selectProduct2ByCategory(category));
    }
} 
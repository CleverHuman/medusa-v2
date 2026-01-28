package com.medusa.mall.controller;
import com.medusa.common.annotation.Anonymous;
import com.medusa.common.core.controller.BaseController;
import com.medusa.common.core.page.TableDataInfo;
import com.medusa.common.core.domain.AjaxResult;
import com.medusa.mall.domain.Product;
import com.medusa.mall.service.IProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/mall/product")
public class ApiProductController extends BaseController {
    @Autowired
    private IProductService productService;

    /**
     * 查询产品列表（用户端 - 只显示激活的产品）
     */
    @GetMapping("/list")
    public TableDataInfo list(Product product) {
        startPage();
        // 用户端只显示激活的产品
        List<Product> list = productService.selectProductList(product);
        return getDataTable(list);
    }

    /**
     * 查询产品列表（Telegram专用 - 只显示TG和OS/TG渠道的产品）
     */
    @Anonymous
    @GetMapping("/telegram/list")
    public TableDataInfo telegramList(Product product) {
        startPage();
        // 设置状态为激活
        product.setStatus(1);
        // 调用Telegram专用的产品查询方法
        List<Product> list = productService.selectProductListForTelegram(product);
        return getDataTable(list);
    }

    /**
     * 获取产品详细信息
     */
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return success(productService.selectProductById(id));
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
    public AjaxResult getByCategory(@PathVariable("category") String category) {
        return success(productService.selectProductsByCategory(category));
    }
}

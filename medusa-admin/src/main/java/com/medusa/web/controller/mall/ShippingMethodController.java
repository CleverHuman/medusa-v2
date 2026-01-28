package com.medusa.web.controller.mall;

import com.medusa.common.core.controller.BaseController;
import com.medusa.common.core.page.TableDataInfo;
import com.medusa.common.core.domain.AjaxResult;
import com.medusa.mall.domain.order.ShippingMethod;
import com.medusa.mall.service.IShippingMethodService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;
import java.util.UUID;
import java.util.Date;

/**
 * Shipping Method Controller
 */
@RestController
@RequestMapping("/admin/mall/shipping")
public class ShippingMethodController extends BaseController {
    
    @Autowired
    private IShippingMethodService shippingMethodService;

    /**
     * Get shipping method list
     */
    @PreAuthorize("@ss.hasPermi('mall:shipping:list')")
    @GetMapping("/list")
    public TableDataInfo list(ShippingMethod shippingMethod) {
        startPage();
        List<ShippingMethod> list = shippingMethodService.selectShippingMethodList(shippingMethod);
        return getDataTable(list);
    }

    /**
     * Get shipping method details by ID
     */
    @PreAuthorize("@ss.hasPermi('mall:shipping:query')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable("id") String id) {
        // Since we're using code as the unique identifier in most cases
        ShippingMethod shippingMethod = shippingMethodService.selectShippingMethodByCode(id);
        return success(shippingMethod);
    }

    /**
     * Add new shipping method
     */
    @PreAuthorize("@ss.hasPermi('mall:shipping:add')")
    @PostMapping
    public AjaxResult add(@RequestBody ShippingMethod shippingMethod) {
        // Generate UUID for new shipping method
        shippingMethod.setId(UUID.randomUUID().toString());
        shippingMethod.setCreateTime(new Date());
        shippingMethod.setUpdateTime(new Date());
        
        // Set default status if not provided
        if (shippingMethod.getStatus() == null) {
            shippingMethod.setStatus(1);
        }
        
        return toAjax(shippingMethodService.insertShippingMethod(shippingMethod));
    }

    /**
     * Update shipping method
     */
    @PreAuthorize("@ss.hasPermi('mall:shipping:edit')")
    @PutMapping
    public AjaxResult edit(@RequestBody ShippingMethod shippingMethod) {
        shippingMethod.setUpdateTime(new Date());
        return toAjax(shippingMethodService.updateShippingMethod(shippingMethod));
    }

    /**
     * Delete shipping method
     */
    @PreAuthorize("@ss.hasPermi('mall:shipping:remove')")
    @DeleteMapping("/{codes}")
    public AjaxResult remove(@PathVariable String[] codes) {
        int result = 0;
        for (String code : codes) {
            result += shippingMethodService.deleteShippingMethodByCode(code);
        }
        return toAjax(result);
    }

    /**
     * Get all active shipping methods (for dropdown)
     */
    @GetMapping("/active")
    public AjaxResult getActiveShippingMethods() {
        List<ShippingMethod> activeList = shippingMethodService.selectActiveShippingMethods();
        return success(activeList);
    }
}

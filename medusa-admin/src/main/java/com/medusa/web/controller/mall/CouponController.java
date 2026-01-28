package com.medusa.web.controller.mall;


import com.medusa.common.core.controller.BaseController;
import com.medusa.common.core.domain.AjaxResult;
import com.medusa.common.core.page.TableDataInfo;
import com.medusa.mall.domain.coupon.Coupon;
import com.medusa.mall.domain.coupon.BulkCouponRequest;
import com.medusa.mall.service.ICouponService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "后台优惠券管理")
@RestController
@RequestMapping("/admin/mall/coupon")
public class CouponController extends BaseController {

    @Autowired
    private ICouponService couponService;

    @ApiOperation("获取优惠券列表")
    @GetMapping("/list")
    public TableDataInfo list(Coupon coupon) {
        startPage();
        List<Coupon> list = couponService.selectCouponList(coupon);
        return getDataTable(list);
    }

    @ApiOperation("获取优惠券详情")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return AjaxResult.success(couponService.selectCouponById(id));
    }

    @ApiOperation("新增优惠券")
    @PostMapping
    public AjaxResult add(@RequestBody Coupon coupon) {
        return toAjax(couponService.insertCoupon(coupon));
    }

    @ApiOperation("批量新增优惠券")
    @PostMapping("/bulk")
    public AjaxResult bulkAdd(@RequestBody BulkCouponRequest request) {
        int createdCount = couponService.bulkCreateCoupons(request);
        return AjaxResult.success("Successfully created " + createdCount + " coupons", createdCount);
    }

    @ApiOperation("修改优惠券")
    @PutMapping
    public AjaxResult edit(@RequestBody Coupon coupon) {
        return toAjax(couponService.updateCoupon(coupon));
    }

    @ApiOperation("删除优惠券")
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(couponService.deleteCouponByIds(ids));
    }
}

package com.medusa.mall.controller;

import com.medusa.common.annotation.Anonymous;
import com.medusa.common.core.domain.AjaxResult;
import com.medusa.common.core.domain.model.LoginMember;
import com.medusa.common.utils.ServletUtils;
import com.medusa.framework.web.service.MemberTokenService;
import com.medusa.mall.domain.cart.Cart;
import com.medusa.mall.domain.coupon.Coupon;
import com.medusa.mall.service.ICartService;
import com.medusa.mall.service.ICouponHistoryService;
import com.medusa.mall.service.ICouponService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

@RestController
@RequestMapping("/api/mall/coupon")
public class ApiCouponController {
    @Autowired
    private ICouponService couponService;

    @Autowired
    private ICouponHistoryService couponHistoryService;

    @Autowired
    private ICartService cartService;

    @Autowired
    private MemberTokenService memberTokenService;


    @Anonymous
    @GetMapping("/{code}")
    public AjaxResult get(@PathVariable("code") String code) {
        Coupon coupon = couponService.selectCouponByCode(code);

        if (coupon == null) {
            return AjaxResult.error("coupon not exist");
        }else{
            Integer status = coupon.getStatus();
            if (status == 0) {
                return AjaxResult.error("coupon invalid");
            }
//            Date startTime = coupon.getStartTime();
//            Date endTime = coupon.getEndTime();

            Integer totalCount = coupon.getTotalCount();
            Integer usedCount = coupon.getUsedCount();
            if (usedCount >= totalCount) {
                return AjaxResult.error("coupon not enough");
            }
            return AjaxResult.success(coupon);
        }
    }
}




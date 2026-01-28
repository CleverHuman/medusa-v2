package com.medusa.mall.service;

import com.medusa.mall.domain.coupon.Coupon;
import com.medusa.mall.domain.coupon.BulkCouponRequest;
import java.util.List;

public interface ICouponService {

    Coupon selectCouponByCode(String code);

    Coupon selectCouponById(Long id);
    List<Coupon> selectCouponList(Coupon coupon);
    int insertCoupon(Coupon coupon);
    int updateCoupon(Coupon coupon);
    int deleteCouponById(Long id);
    int deleteCouponByIds(Long[] ids);
    
    /**
     * Bulk create coupons
     * @param request Bulk coupon creation request
     * @return Number of coupons created
     */
    int bulkCreateCoupons(BulkCouponRequest request);

}

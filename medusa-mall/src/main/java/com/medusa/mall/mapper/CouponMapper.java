package com.medusa.mall.mapper;

import com.medusa.mall.domain.coupon.Coupon;

import java.util.List;

public interface CouponMapper {

    Coupon selectCouponByCode(String code);

    List<Coupon> selectCouponList(Coupon coupon);

    Coupon selectCouponById(Long id);
    
    int insertCoupon(Coupon coupon);
    
    int updateCoupon(Coupon coupon);
    
    int deleteCouponById(Long id);
    
    int deleteCouponByIds(Long[] ids);

    /**
     * 增加优惠券使用次数
     * @param couponId 优惠券ID
     * @return 影响行数
     */
    int incrementUsedCount(Long couponId);

    /**
     * 减少优惠券使用次数
     * @param couponId 优惠券ID
     * @return 影响行数
     */
    int decrementUsedCount(Long couponId);

}

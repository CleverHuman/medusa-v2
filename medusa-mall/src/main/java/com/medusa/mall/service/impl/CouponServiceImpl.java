package com.medusa.mall.service.impl;

import com.medusa.mall.domain.coupon.Coupon;
import com.medusa.mall.domain.coupon.BulkCouponRequest;
import com.medusa.mall.mapper.CouponMapper;
import com.medusa.mall.service.ICouponService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Random;
import java.util.Date;

@Service
public class CouponServiceImpl implements ICouponService {

    @Autowired
    private CouponMapper couponMapper;

    @Override
    public Coupon selectCouponByCode(String code) {
        // check code exit
        Coupon exitCoupon = couponMapper.selectCouponByCode(code);
        if (exitCoupon == null) {
            return null;
        }
        return exitCoupon;
    }


    @Override
    public List<Coupon> selectCouponList(Coupon coupon) {
        return couponMapper.selectCouponList(coupon);
    }


    @Override
    public Coupon selectCouponById(Long id) {
        return couponMapper.selectCouponById(id);
    }

    @Override
    public int insertCoupon(Coupon coupon) {
        // 设置创建时间和更新时间
        Date now = new Date();
        coupon.setCreateTime(now);
        coupon.setUpdateTime(now);
        return couponMapper.insertCoupon(coupon);
    }

    @Override
    public int updateCoupon(Coupon coupon) {
        // 设置更新时间
        coupon.setUpdateTime(new Date());
        return couponMapper.updateCoupon(coupon);
    }

    @Override
    public int deleteCouponById(Long id) {
        return couponMapper.deleteCouponById(id);
    }

    @Override
    public int deleteCouponByIds(Long[] ids) {
        return couponMapper.deleteCouponByIds(ids);
    }

    @Override
    public int bulkCreateCoupons(BulkCouponRequest request) {
        int createdCount = 0;
        Random random = new Random();
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        
        for (int i = 0; i < request.getQuantity(); i++) {
            Coupon coupon = new Coupon();
            coupon.setName(request.getName());
            coupon.setType(request.getType());
            coupon.setAmount(request.getAmount());
            coupon.setDiscount(request.getDiscount());
            coupon.setMinPoint(request.getMinPoint());
            coupon.setStartTime(request.getStartTime());
            coupon.setEndTime(request.getEndTime());
            coupon.setTotalCount(request.getTotalCount());
            coupon.setUsedCount(0);
            coupon.setStatus(request.getStatus());
            coupon.setRemark(request.getRemark());
            
            // 设置创建时间和更新时间
            Date now = new Date();
            coupon.setCreateTime(now);
            coupon.setUpdateTime(now);
            
            // Generate unique code
            String codePrefix = request.getCodePrefix() != null ? request.getCodePrefix() : "";
            String uniqueCode;
            do {
                StringBuilder codeBuilder = new StringBuilder(codePrefix);
                for (int j = 0; j < 6; j++) {
                    codeBuilder.append(chars.charAt(random.nextInt(chars.length())));
                }
                uniqueCode = codeBuilder.toString();
            } while (selectCouponByCode(uniqueCode) != null);
            
            coupon.setCode(uniqueCode);
            
            if (insertCoupon(coupon) > 0) {
                createdCount++;
            }
        }
        
        return createdCount;
    }

}
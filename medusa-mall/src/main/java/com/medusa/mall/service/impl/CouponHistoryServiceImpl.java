package com.medusa.mall.service.impl;

import com.medusa.mall.domain.coupon.CouponHistory;
import com.medusa.mall.mapper.CouponHistoryMapper;
import com.medusa.mall.service.ICouponHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CouponHistoryServiceImpl implements ICouponHistoryService {

    @Autowired
    private CouponHistoryMapper couponHistoryMapper;

    @Override
    public CouponHistory selectCouponHistoryById(Long id) {
        return couponHistoryMapper.selectCouponHistoryById(id);
    }

    @Override
    public List<CouponHistory> selectCouponHistoryList(CouponHistory history) {
        return couponHistoryMapper.selectCouponHistoryList(history);
    }

    @Override
    public int insertCouponHistory(CouponHistory history) {
        return couponHistoryMapper.insertCouponHistory(history);
    }

    @Override
    public int updateCouponHistory(CouponHistory history) {
        return couponHistoryMapper.updateCouponHistory(history);
    }

    @Override
    public int deleteCouponHistoryById(Long id) {
        return couponHistoryMapper.deleteCouponHistoryById(id);
    }

    @Override
    public int deleteCouponHistoryByIds(Long[] ids) {
        return couponHistoryMapper.deleteCouponHistoryByIds(ids);
    }
}

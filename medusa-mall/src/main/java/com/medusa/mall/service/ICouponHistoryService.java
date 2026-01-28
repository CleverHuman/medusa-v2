package com.medusa.mall.service;

import com.medusa.mall.domain.coupon.CouponHistory;
import java.util.List;

public interface ICouponHistoryService {

    CouponHistory selectCouponHistoryById(Long id);

    List<CouponHistory> selectCouponHistoryList(CouponHistory history);

    int insertCouponHistory(CouponHistory history);

    int updateCouponHistory(CouponHistory history);

    int deleteCouponHistoryById(Long id);

    int deleteCouponHistoryByIds(Long[] ids);
}

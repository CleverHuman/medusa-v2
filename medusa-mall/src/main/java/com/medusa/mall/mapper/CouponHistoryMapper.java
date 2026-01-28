package com.medusa.mall.mapper;

import com.medusa.mall.domain.coupon.CouponHistory;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface CouponHistoryMapper {

    CouponHistory selectCouponHistoryById(Long id);

    List<CouponHistory> selectCouponHistoryList(CouponHistory history);

    int insertCouponHistory(CouponHistory history);

    int updateCouponHistory(CouponHistory history);

    int deleteCouponHistoryById(Long id);

    int deleteCouponHistoryByIds(Long[] ids);

    // 新增：按优惠券、订单和状态统计记录数，用于幂等检查
    int countByCouponOrderAndStatus(@Param("couponId") Long couponId,
                                    @Param("orderId") Long orderId,
                                    @Param("useStatus") Integer useStatus);
}

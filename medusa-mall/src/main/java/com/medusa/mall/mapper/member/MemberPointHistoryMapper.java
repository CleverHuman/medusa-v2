package com.medusa.mall.mapper.member;

import com.medusa.mall.domain.member.MemberPointHistory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 会员积分历史记录Mapper接口
 */
@Mapper
public interface MemberPointHistoryMapper {

    /**
     * 查询会员积分历史记录列表
     * @param memberId 会员ID
     * @return 积分历史记录列表
     */
    List<MemberPointHistory> selectPointHistoryByMemberId(@Param("memberId") Long memberId);

    /**
     * 新增积分历史记录
     * @param pointHistory 积分历史记录
     * @return 结果
     */
    int insertPointHistory(MemberPointHistory pointHistory);

    /**
     * 查询会员积分历史记录总数
     * @param memberId 会员ID
     * @return 记录总数
     */
    int countPointHistoryByMemberId(@Param("memberId") Long memberId);

    /**
     * 检查指定订单是否已经处理过积分
     * @param memberId 会员ID
     * @param orderSn 订单号
     * @return 记录数量
     */
    int countPointHistoryByOrderSn(@Param("memberId") Long memberId, @Param("orderSn") String orderSn);

    /**
     * 根据会员ID和年份统计积分总和
     * 
     * @param memberId 会员ID
     * @param year 年份
     * @return 积分总和
     */
    java.math.BigDecimal sumPointsByMemberIdAndYear(@Param("memberId") Long memberId, @Param("year") Integer year);
} 
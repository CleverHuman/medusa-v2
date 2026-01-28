package com.medusa.mall.service.member;

import com.medusa.mall.domain.member.MemberPointHistory;

import java.math.BigDecimal;
import java.util.List;

/**
 * 会员积分历史记录Service接口
 */
public interface IMemberPointHistoryService {

    /**
     * 查询会员积分历史记录列表
     * @param memberId 会员ID
     * @return 积分历史记录列表
     */
    List<MemberPointHistory> selectPointHistoryByMemberId(Long memberId);

    /**
     * 新增积分历史记录
     * @param memberId 会员ID
     * @param amount 金额
     * @param note 备注
     * @param platform 平台来源
     * @param year 年份
     * @return 结果
     */
    int addPointHistory(Long memberId, BigDecimal amount, String note, Integer platform, Integer year);

    /**
     * 新增订单支付积分历史记录（会同时更新订单总数）
     * @param memberId 会员ID
     * @param amount 金额
     * @param note 备注
     * @param platform 平台来源
     * @return 结果
     */
    int addPointHistoryForOrder(Long memberId, BigDecimal amount, String note, Integer platform);

    /**
     * 查询会员积分历史记录总数
     * @param memberId 会员ID
     * @return 记录总数
     */
    int countPointHistoryByMemberId(Long memberId);
    
    /**
     * 新增订单支付积分历史记录（更新积分和订单总数，但不更新等级）
     * 用于Admin手动标记订单为Paid的场景
     * @param memberId 会员ID
     * @param amount 金额
     * @param note 备注
     * @param platform 平台来源
     * @return 结果
     */
    int addPointHistoryWithoutLevelUpdate(Long memberId, BigDecimal amount, String note, Integer platform);

    /**
     * 根据会员ID和年份统计积分总和
     * 
     * @param memberId 会员ID
     * @param year 年份
     * @return 积分总和
     */
    BigDecimal sumPointsByMemberIdAndYear(Long memberId, Integer year);

    /**
     * 新增积分历史记录
     * 
     * @param memberPointHistory 积分历史记录
     * @return 结果
     */
    int insertMemberPointHistory(MemberPointHistory memberPointHistory);
} 
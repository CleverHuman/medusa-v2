package com.medusa.mall.service.member.impl;

import com.medusa.mall.domain.member.MemberPointHistory;
import com.medusa.mall.domain.member.MemberLevel;
import com.medusa.mall.domain.member.MemberBenefit;
import com.medusa.mall.mapper.member.MemberPointHistoryMapper;
import com.medusa.mall.mapper.MemberBenefitMapper;
import com.medusa.mall.service.member.IMemberPointHistoryService;
import com.medusa.mall.service.IMemberLevelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Member point history service business layer processing
 */
@Service
public class MemberPointHistoryServiceImpl implements IMemberPointHistoryService {

    private static final Logger log = LoggerFactory.getLogger(MemberPointHistoryServiceImpl.class);

    @Autowired
    private MemberPointHistoryMapper memberPointHistoryMapper;

    @Autowired
    private IMemberLevelService memberLevelService;

    @Autowired
    private MemberBenefitMapper memberBenefitMapper;

    /**
     * Query member point history list
     * @param memberId Member ID
     * @return Point history list
     */
    @Override
    public List<MemberPointHistory> selectPointHistoryByMemberId(Long memberId) {
        return memberPointHistoryMapper.selectPointHistoryByMemberId(memberId);
    }

    /**
     * Add point history record
     * @param memberId Member ID
     * @param amount Amount
     * @param note Note
     * @param platform Platform source
     * @return Result
     */
    @Override
    @Transactional
    public int addPointHistory(Long memberId, BigDecimal amount, String note, Integer platform, Integer year) {
        // 1. 添加积分历史记录
        MemberPointHistory pointHistory = new MemberPointHistory();
        pointHistory.setMemberId(memberId);
        pointHistory.setAmount(amount);
        // Calculate points: 1% of amount
        BigDecimal points = amount.multiply(new BigDecimal("0.01")).setScale(4, RoundingMode.HALF_UP);
        pointHistory.setPoints(points);
        pointHistory.setNote(note);
        pointHistory.setPlatform(platform);
        pointHistory.setYear(year); // 设置年份
        int result = memberPointHistoryMapper.insertPointHistory(pointHistory);
        
        if (result > 0) {
            // 2. 根据年份智能更新积分
            MemberLevel memberLevel = memberLevelService.selectMemberLevelByMemberId(memberId);
            if (memberLevel != null) {
                BigDecimal currentPoints = memberLevel.getCurrentPoint() != null ? memberLevel.getCurrentPoint() : BigDecimal.ZERO;
                BigDecimal lastPoint = memberLevel.getLastPoint() != null ? memberLevel.getLastPoint() : BigDecimal.ZERO;
                
                int currentYear = java.time.LocalDate.now().getYear();
                int previousYear = currentYear - 1;
                
                // ✅ 根据年份判断更新逻辑
                if (year != null && year == previousYear) {
                    // 如果是添加去年的积分，更新last_point并实时更新Current Level
                    BigDecimal newLastPoint = lastPoint.add(points);
                    memberLevel.setLastPoint(newLastPoint);
                    
                    // ✅ 关键：去年的积分决定当前的等级，因此需要实时更新等级
                    Integer oldLevel = memberLevel.getCurrentLevel();
                    Integer newLevel = calculateMemberLevel(newLastPoint);
                    memberLevel.setCurrentLevel(newLevel);
                    
                    log.info("Member {} previous year ({}) points updated - Last Point: {} → {}, Current Level: {} → {}, Current Point (unchanged): {}",
                            memberId, year, lastPoint, newLastPoint, oldLevel, newLevel, currentPoints);
                } else {
                    // 如果是添加当前年份的积分，只更新current_point，不更新等级
                    // 因为当前年份的积分要到明年才会影响等级
                    BigDecimal newCurrentPoint = currentPoints.add(points);
                    memberLevel.setCurrentPoint(newCurrentPoint);
                    
                    log.info("Member {} current year ({}) points updated - Current Point: {} → {}, Current Level (unchanged): {}, Last Point (unchanged): {}",
                            memberId, year != null ? year : currentYear, currentPoints, newCurrentPoint, memberLevel.getCurrentLevel(), lastPoint);
                }
                
                // 不调用 setTotalOrders，保持订单总数不变
                // 不调用 setLastLevel，保持去年等级快照不变
                
                memberLevelService.updateMemberLevel(memberLevel);
            }
        }
        
        return result;
    }

    /**
     * Add point history record for order payment (will also update total orders)
     * @param memberId Member ID
     * @param amount Amount
     * @param note Note
     * @param platform Platform source
     * @return Result
     */
    @Override
    @Transactional
    public int addPointHistoryForOrder(Long memberId, BigDecimal amount, String note, Integer platform) {
        // 0. 幂等性检查：从note中提取订单号并检查是否已经处理过
        String orderSn = extractOrderSnFromNote(note);
        if (orderSn != null) {
            int existingCount = memberPointHistoryMapper.countPointHistoryByOrderSn(memberId, orderSn);
            if (existingCount > 0) {
                log.warn("Points already added for order: {} (member: {}), skipping duplicate processing", 
                        orderSn, memberId);
                return 0; // 返回0表示未新增记录，但不是错误
            }
        }
        
        // 1. 添加积分历史记录
        MemberPointHistory pointHistory = new MemberPointHistory();
        pointHistory.setMemberId(memberId);
        pointHistory.setAmount(amount);
        // Calculate points: 1% of amount
        BigDecimal points = amount.multiply(new BigDecimal("0.01")).setScale(4, RoundingMode.HALF_UP);
        pointHistory.setPoints(points);
        pointHistory.setNote(note);
        pointHistory.setPlatform(platform);
        // ✅ 设置年份为当前年份（订单支付的积分记录为当前年份）
        pointHistory.setYear(java.time.LocalDate.now().getYear());
        int result = memberPointHistoryMapper.insertPointHistory(pointHistory);
        
        if (result > 0) {
            // 2. 更新会员等级表中的积分和订单总数，但不更新等级和last值
            MemberLevel memberLevel = memberLevelService.selectMemberLevelByMemberId(memberId);
            if (memberLevel != null) {
                BigDecimal currentPoints = memberLevel.getCurrentPoint() != null ? memberLevel.getCurrentPoint() : BigDecimal.ZERO;
                BigDecimal newTotalPoints = currentPoints.add(points);
                
                // 更新订单总数（+1）
                Long currentTotalOrders = memberLevel.getTotalOrders() != null ? memberLevel.getTotalOrders() : 0L;
                Long newTotalOrders = currentTotalOrders + 1;
                
                // ✅ 更新积分和订单总数，但不更新等级和last值（等级和last值由定时任务统一更新）
                memberLevel.setCurrentPoint(newTotalPoints);
                memberLevel.setTotalOrders(newTotalOrders);
                // 不调用 setCurrentLevel，保持等级不变
                // 不调用 setLastPoint，保持去年积分快照不变
                // 不调用 setLastLevel，保持去年等级快照不变
                
                memberLevelService.updateMemberLevel(memberLevel);
                
                log.info("Member {} points and total orders updated without level change - Current Level: {}, Current Points: {}, Total Orders: {}, Last Point (unchanged): {}",
                        memberId, memberLevel.getCurrentLevel(), newTotalPoints, newTotalOrders, memberLevel.getLastPoint());
            }
        }
        
        return result;
    }

    /**
     * Query member point history total
     * @param memberId Member ID
     * @return Total records
     */
    @Override
    public int countPointHistoryByMemberId(Long memberId) {
        return memberPointHistoryMapper.countPointHistoryByMemberId(memberId);
    }

    /**
     * Add point history record for order payment (only update points and total orders, not level)
     * Used for Admin manually marking orders as Paid
     * @param memberId Member ID
     * @param amount Amount
     * @param note Note
     * @param platform Platform source
     * @return Result
     */
    @Override
    @Transactional
    public int addPointHistoryWithoutLevelUpdate(Long memberId, BigDecimal amount, String note, Integer platform) {
        // 0. 幂等性检查：从note中提取订单号并检查是否已经处理过
        String orderSn = extractOrderSnFromNote(note);
        if (orderSn != null) {
            int existingCount = memberPointHistoryMapper.countPointHistoryByOrderSn(memberId, orderSn);
            if (existingCount > 0) {
                log.warn("Points already added for order: {} (member: {}), skipping duplicate processing", 
                        orderSn, memberId);
                return 0; // 返回0表示未新增记录，但不是错误
            }
        }
        
        // 1. 添加积分历史记录
        MemberPointHistory pointHistory = new MemberPointHistory();
        pointHistory.setMemberId(memberId);
        pointHistory.setAmount(amount);
        // Calculate points: 1% of amount
        BigDecimal points = amount.multiply(new BigDecimal("0.01")).setScale(4, RoundingMode.HALF_UP);
        pointHistory.setPoints(points);
        pointHistory.setNote(note);
        pointHistory.setPlatform(platform);
        // ✅ 设置年份为当前年份（订单支付的积分记录为当前年份）
        pointHistory.setYear(java.time.LocalDate.now().getYear());
        int result = memberPointHistoryMapper.insertPointHistory(pointHistory);
        
        if (result > 0) {
            // 2. 更新会员等级表中的积分和订单总数，但不更新等级和last值
            MemberLevel memberLevel = memberLevelService.selectMemberLevelByMemberId(memberId);
            if (memberLevel != null) {
                BigDecimal currentPoints = memberLevel.getCurrentPoint() != null ? memberLevel.getCurrentPoint() : BigDecimal.ZERO;
                BigDecimal newTotalPoints = currentPoints.add(points);
                
                // 更新订单总数（+1）
                Long currentTotalOrders = memberLevel.getTotalOrders() != null ? memberLevel.getTotalOrders() : 0L;
                Long newTotalOrders = currentTotalOrders + 1;
                
                // ✅ 关键：更新积分和订单总数，但不更新等级和last值
                memberLevel.setCurrentPoint(newTotalPoints);
                memberLevel.setTotalOrders(newTotalOrders); // ✅ 更新订单总数
                // 不调用 setCurrentLevel，保持等级不变
                // 不调用 setLastPoint，保持去年积分快照不变
                // 不调用 setLastLevel，保持去年等级快照不变
                
                memberLevelService.updateMemberLevel(memberLevel);
                
                log.info("Member {} points and total orders updated without level change - Current Level: {}, Current Points: {}, Total Orders: {}, Last Point (unchanged): {}",
                        memberId, memberLevel.getCurrentLevel(), newTotalPoints, newTotalOrders, memberLevel.getLastPoint());
            }
        }
        
        return result;
    }
    
    /**
     * 根据积分计算应该的会员等级
     * @param totalPoints 总积分
     * @return 应该的等级ID
     */
    private Integer calculateMemberLevel(BigDecimal totalPoints) {
        // 查询所有会员等级，按积分要求升序排列
        List<MemberBenefit> memberBenefits = memberBenefitMapper.selectAllMemberBenefitsOrderByPoint();
        
        Integer targetLevel = 1; // 默认等级为1
        
        // 遍历所有等级，找到积分要求最高的且不超过当前积分的等级
        for (MemberBenefit benefit : memberBenefits) {
            if (benefit.getPoint() != null && totalPoints.compareTo(benefit.getPoint()) >= 0) {
                targetLevel = benefit.getLevelId().intValue();
            } else {
                // 如果当前等级的积分要求超过了总积分，停止遍历
                break;
            }
        }
        
        return targetLevel;
    }

    /**
     * 从note中提取订单号
     * 格式: "Order payment: 0786705, Amount: 547.50 AUD"
     * @param note 备注信息
     * @return 订单号，如果提取失败返回null
     */
    private String extractOrderSnFromNote(String note) {
        if (note == null || note.trim().isEmpty()) {
            return null;
        }
        
        try {
            // 匹配格式: "Order payment: 订单号, Amount: ..."
            if (note.contains("Order payment: ")) {
                int startIndex = note.indexOf("Order payment: ") + "Order payment: ".length();
                int endIndex = note.indexOf(",", startIndex);
                if (endIndex > startIndex) {
                    return note.substring(startIndex, endIndex).trim();
                }
            }
        } catch (Exception e) {
            log.warn("Failed to extract order SN from note: {}", note, e);
        }
        
        return null;
    }

    /**
     * 根据会员ID和年份统计积分总和
     * 
     * @param memberId 会员ID
     * @param year 年份
     * @return 积分总和
     */
    @Override
    public BigDecimal sumPointsByMemberIdAndYear(Long memberId, Integer year) {
        return memberPointHistoryMapper.sumPointsByMemberIdAndYear(memberId, year);
    }

    /**
     * 新增积分历史记录
     * 
     * @param memberPointHistory 积分历史记录
     * @return 结果
     */
    @Override
    public int insertMemberPointHistory(MemberPointHistory memberPointHistory) {
        return memberPointHistoryMapper.insertPointHistory(memberPointHistory);
    }
} 
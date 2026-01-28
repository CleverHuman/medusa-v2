package com.medusa.quartz.task;

import com.medusa.common.core.domain.entity.Member;
import com.medusa.mall.domain.member.MemberLevel;
import com.medusa.mall.domain.member.MemberBenefit;
import com.medusa.mall.domain.member.MemberPointHistory;
import com.medusa.mall.service.IMemberService;
import com.medusa.mall.service.IMemberLevelService;
import com.medusa.mall.service.IMemberBenefitService;
import com.medusa.mall.service.member.IMemberPointHistoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;

/**
 * Annual Member Level Update Scheduled Task
 * Executes on January 1st every year, determines current year level based on previous year's points
 */
@Component("annualMemberLevelTask")
public class AnnualMemberLevelTask {
    
    private static final Logger log = LoggerFactory.getLogger(AnnualMemberLevelTask.class);
    
    @Autowired
    private IMemberService memberService;
    
    @Autowired
    private IMemberLevelService memberLevelService;
    
    @Autowired
    private IMemberBenefitService memberBenefitService;
    
    @Autowired
    private IMemberPointHistoryService memberPointHistoryService;
    
    /**
     * Annual Member Level Update Task
     * Execution time: January 1st 00:00:00 every year
     */
    public void executeAnnualLevelUpdate() {
        log.info("🚀 [AnnualMemberLevelTask] Starting annual member level update task");
        
        try {
            // Get current year
            int currentYear = LocalDate.now().getYear();
            int previousYear = currentYear - 1;
            
            log.info("📅 [AnnualMemberLevelTask] Current year: {}, Processing previous year: {}", currentYear, previousYear);
            
            // Get all active members
            List<Member> activeMembers = memberService.selectActiveMembers();
            log.info("👥 [AnnualMemberLevelTask] Found {} active members", activeMembers.size());
            
            int successCount = 0;
            int errorCount = 0;
            
            for (Member member : activeMembers) {
                try {
                    updateMemberLevelForYear(member, previousYear, currentYear);
                    successCount++;
                } catch (Exception e) {
                    log.error("❌ [AnnualMemberLevelTask] Failed to update member {} level: {}", member.getMemberId(), e.getMessage(), e);
                    errorCount++;
                }
            }
            
            log.info("✅ [AnnualMemberLevelTask] Annual level update completed - Success: {}, Failed: {}", successCount, errorCount);
            
        } catch (Exception e) {
            log.error("❌ [AnnualMemberLevelTask] Annual level update task execution failed", e);
        }
    }
    
    /**
     * Update annual level for a single member
     */
    private void updateMemberLevelForYear(Member member, int previousYear, int currentYear) {
        Long memberId = member.getMemberId();
        
        log.debug("🔄 [AnnualMemberLevelTask] Processing member ID: {}, username: {}", memberId, member.getUsername());
        
        // 1. Get member level info
        MemberLevel memberLevel = memberLevelService.selectMemberLevelByMemberId(memberId);
        if (memberLevel == null) {
            log.warn("⚠️ [AnnualMemberLevelTask] Member {} has no level record, skipping", memberId);
            return;
        }
        
        // 2. Read current year points directly (which is previous year's points)
        BigDecimal previousYearPoints = memberLevel.getCurrentPoint();
        if (previousYearPoints == null) {
            previousYearPoints = BigDecimal.ZERO;
        }
        log.debug("📊 [AnnualMemberLevelTask] Member {} year {} points: {}", memberId, previousYear, previousYearPoints);
        
        // 3. Determine level by points
        MemberBenefit newLevelBenefit = determineLevelByPoints(previousYearPoints);
        Integer newLevelId = newLevelBenefit != null ? newLevelBenefit.getLevelId().intValue() : 1; // Default Bronze (ID=1)
        String newLevelName = newLevelBenefit != null ? newLevelBenefit.getLevelName() : "Bronze";
        log.debug("🏆 [AnnualMemberLevelTask] Member {} new level: {} (ID: {})", memberId, newLevelName, newLevelId);
        
        // 4. Update member level info
        updateMemberLevelInfo(memberLevel, previousYearPoints, newLevelId, currentYear);
        
        // 5. Record point history
        recordLevelUpdateHistory(memberId, previousYearPoints, newLevelName, previousYear, currentYear);
        
        log.debug("✅ [AnnualMemberLevelTask] Member {} level update completed", memberId);
    }
    
    /**
     * Determine member level by points
     * Rules:
     * - 0 points = Bronze (default level)
     * - >= 50 = Silver
     * - >= 100 = Gold
     * - >= 200 = Platinum
     * - >= 500 = Diamond
     */
    private MemberBenefit determineLevelByPoints(BigDecimal points) {
        // If points is null, treat as 0 points
        if (points == null) {
            points = BigDecimal.ZERO;
        }
        
        // Get all level rules, sorted by point requirement in ascending order
        List<MemberBenefit> benefits = memberBenefitService.selectAllMemberBenefitsOrderByPoint();
        
        if (benefits == null || benefits.isEmpty()) {
            log.warn("⚠️ [AnnualMemberLevelTask] No level rules found");
            return null;
        }
        
        // Find the highest level that meets the point requirement
        MemberBenefit selectedBenefit = benefits.get(0); // Default to lowest level (Bronze, point requirement is 0)
        
        for (MemberBenefit benefit : benefits) {
            BigDecimal requiredPoint = benefit.getPoint();
            if (requiredPoint == null) {
                requiredPoint = BigDecimal.ZERO;
            }
            
            // If current points >= required points, this level qualifies
            if (points.compareTo(requiredPoint) >= 0) {
                selectedBenefit = benefit;
            } else {
                // Since sorted in ascending order, later levels require more points, no need to continue checking
                break;
            }
        }
        
        return selectedBenefit;
    }
    
    /**
     * Update member level info
     */
    private void updateMemberLevelInfo(MemberLevel memberLevel, BigDecimal previousYearPoints, Integer newLevelId, int currentYear) {
        // Update last_point (previous year's points)
        memberLevel.setLastPoint(previousYearPoints);
        
        // Update last_level (previous year's level - actually the current current_level)
        memberLevel.setLastLevel(memberLevel.getCurrentLevel());
        
        // Update current_level (current year's level, based on previous year's points)
        memberLevel.setCurrentLevel(newLevelId);
        
        // Reset current_point (current year's points to 0)
        memberLevel.setCurrentPoint(BigDecimal.ZERO);
        
        // Update modification time
        memberLevel.setUpdateTime(new Date());
        
        // Save to database
        memberLevelService.updateMemberLevel(memberLevel);
        
        log.debug("💾 [AnnualMemberLevelTask] Member level info updated - memberId: {}, lastPoint: {}, lastLevel: {}, currentLevel: {}, currentPoint: 0",
                memberLevel.getMemberId(), previousYearPoints, memberLevel.getLastLevel(), newLevelId);
    }
    
    /**
     * Record level update history
     */
    private void recordLevelUpdateHistory(Long memberId, BigDecimal previousYearPoints, String newLevelName, int previousYear, int currentYear) {
        MemberPointHistory history = new MemberPointHistory();
        history.setMemberId(memberId);
        history.setYear(currentYear);
        history.setPoints(previousYearPoints);
        // amount = points * 100 (because points = amount * 0.01)
        BigDecimal amount = previousYearPoints.multiply(new BigDecimal("100"));
        history.setAmount(amount);
        // Scheduled task record, platform set to 2 (OS/TG)
        history.setPlatform(2);
        history.setNote(String.format("Annual level update: Based on year %d points %.2f, determined year %d level as %s", 
            previousYear, 
            previousYearPoints, 
            currentYear, 
            newLevelName));
        history.setCreateTime(new Date());
        
        memberPointHistoryService.insertMemberPointHistory(history);
        
        log.debug("📝 [AnnualMemberLevelTask] Member {} level update history recorded", memberId);
    }
    
    /**
     * Test method - manually execute annual level update
     */
    public void testAnnualLevelUpdate() {
        log.info("🧪 [AnnualMemberLevelTask] Starting test of annual level update");
        executeAnnualLevelUpdate();
        log.info("🧪 [AnnualMemberLevelTask] Test completed");
    }
}
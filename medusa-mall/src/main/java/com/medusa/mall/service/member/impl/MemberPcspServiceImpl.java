package com.medusa.mall.service.member.impl;

import com.medusa.mall.domain.member.MemberPcsp;
import com.medusa.mall.mapper.member.MemberPcspMapper;
import com.medusa.mall.service.member.IMemberPcspService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * PCSP服务Service实现
 * 
 * @author medusa
 */
@Service
public class MemberPcspServiceImpl implements IMemberPcspService {
    
    private static final Logger log = LoggerFactory.getLogger(MemberPcspServiceImpl.class);
    
    @Autowired
    private MemberPcspMapper memberPcspMapper;
    
    /**
     * 查询会员当前有效的PCSP
     */
    @Override
    public MemberPcsp selectActivePcspByMemberId(Long memberId) {
        return memberPcspMapper.selectActivePcspByMemberId(memberId);
    }
    
    /**
     * 查询会员的PCSP历史记录
     */
    @Override
    public List<MemberPcsp> selectPcspHistoryByMemberId(Long memberId) {
        return memberPcspMapper.selectPcspHistoryByMemberId(memberId);
    }
    
    /**
     * 根据ID查询PCSP记录
     */
    @Override
    public MemberPcsp selectPcspById(Long id) {
        return memberPcspMapper.selectPcspById(id);
    }
    
    /**
     * 新增PCSP记录
     */
    @Override
    @Transactional
    public int insertMemberPcsp(MemberPcsp pcsp) {
        if (pcsp.getStatus() == null) {
            pcsp.setStatus(1); // 默认有效
        }
        
        int result = memberPcspMapper.insertMemberPcsp(pcsp);
        
        if (result > 0) {
            log.info("PCSP created: member={}, package={}, expiry={}", 
                    pcsp.getMemberId(), pcsp.getPackageTypeName(), pcsp.getExpiryDate());
        }
        
        return result;
    }
    
    /**
     * 更新PCSP记录
     */
    @Override
    @Transactional
    public int updateMemberPcsp(MemberPcsp pcsp) {
        return memberPcspMapper.updateMemberPcsp(pcsp);
    }
    
    /**
     * 检查PCSP是否有效
     */
    @Override
    public boolean isPcspValid(Long memberId) {
        MemberPcsp pcsp = selectActivePcspByMemberId(memberId);
        return pcsp != null && pcsp.isValid();
    }
    
    /**
     * 续费PCSP（叠加有效期）
     */
    @Override
    @Transactional
    public Long renewPcsp(Long memberId, Integer packageType, Integer validityMonths, Long productId, String orderSn) {
        // 检查是否已有有效的PCSP
        MemberPcsp existingPcsp = selectActivePcspByMemberId(memberId);
        
        MemberPcsp newPcsp = new MemberPcsp();
        newPcsp.setMemberId(memberId);
        newPcsp.setProductId(productId);
        newPcsp.setOrderSn(orderSn);
        newPcsp.setPackageType(packageType);
        newPcsp.setStatus(1); // 有效
        
        Date startDate;
        if (existingPcsp != null && existingPcsp.isValid()) {
            // 续费：从现有过期日期开始叠加
            startDate = existingPcsp.getExpiryDate();
            log.info("PCSP renewal: member={}, extending from {}", memberId, startDate);
        } else {
            // 首次购买或之前的已过期：从现在开始
            startDate = new Date();
            log.info("PCSP new purchase: member={}, starting from {}", memberId, startDate);
        }
        
        newPcsp.setStartDate(startDate);
        
        // 计算过期日期
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);
        calendar.add(Calendar.MONTH, validityMonths);
        Date expiryDate = calendar.getTime();
        newPcsp.setExpiryDate(expiryDate);
        
        // 插入新记录
        insertMemberPcsp(newPcsp);
        
        log.info("PCSP activated: member={}, type={} months, expiry={}", 
                memberId, validityMonths, expiryDate);
        
        return newPcsp.getId();
    }
    
    /**
     * 定时任务：更新过期的PCSP状态
     */
    @Override
    @Transactional
    public int updateExpiredPcspStatus() {
        int count = memberPcspMapper.updateExpiredPcspStatus();
        
        if (count > 0) {
            log.info("Updated {} expired PCSP records", count);
        }
        
        return count;
    }
    
    /**
     * 删除PCSP记录
     */
    @Override
    @Transactional
    public int deletePcspById(Long id) {
        return memberPcspMapper.deletePcspById(id);
    }
}




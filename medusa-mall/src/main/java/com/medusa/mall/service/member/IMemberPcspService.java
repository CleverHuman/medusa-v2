package com.medusa.mall.service.member;

import com.medusa.mall.domain.member.MemberPcsp;
import java.util.List;

/**
 * PCSP服务Service接口
 * Premium Customer Service Package
 * 
 * @author medusa
 */
public interface IMemberPcspService {
    
    /**
     * 查询会员当前有效的PCSP
     * @param memberId 会员ID
     * @return PCSP记录,如果没有有效的PCSP则返回null
     */
    MemberPcsp selectActivePcspByMemberId(Long memberId);
    
    /**
     * 查询会员的PCSP历史记录
     * @param memberId 会员ID
     * @return PCSP记录列表
     */
    List<MemberPcsp> selectPcspHistoryByMemberId(Long memberId);
    
    /**
     * 根据ID查询PCSP记录
     * @param id PCSP记录ID
     * @return PCSP记录
     */
    MemberPcsp selectPcspById(Long id);
    
    /**
     * 新增PCSP记录（购买后）
     * @param pcsp PCSP记录
     * @return 结果
     */
    int insertMemberPcsp(MemberPcsp pcsp);
    
    /**
     * 更新PCSP记录
     * @param pcsp PCSP记录
     * @return 结果
     */
    int updateMemberPcsp(MemberPcsp pcsp);
    
    /**
     * 检查PCSP是否有效
     * @param memberId 会员ID
     * @return true-有效, false-无效
     */
    boolean isPcspValid(Long memberId);
    
    /**
     * 续费PCSP（叠加有效期）
     * @param memberId 会员ID
     * @param packageType 套餐类型
     * @param validityMonths 有效期月数
     * @param productId 产品ID
     * @param orderSn 订单号
     * @return PCSP记录ID
     */
    Long renewPcsp(Long memberId, Integer packageType, Integer validityMonths, Long productId, String orderSn);
    
    /**
     * 定时任务：更新过期的PCSP状态
     * @return 更新的记录数
     */
    int updateExpiredPcspStatus();
    
    /**
     * 删除PCSP记录
     * @param id PCSP记录ID
     * @return 结果
     */
    int deletePcspById(Long id);
}




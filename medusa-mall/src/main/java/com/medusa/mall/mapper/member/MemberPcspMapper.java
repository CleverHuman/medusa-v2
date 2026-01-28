package com.medusa.mall.mapper.member;

import com.medusa.mall.domain.member.MemberPcsp;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * PCSP服务记录Mapper接口
 * 
 * @author medusa
 */
@Mapper
public interface MemberPcspMapper {
    
    /**
     * 查询会员当前有效的PCSP
     * @param memberId 会员ID
     * @return PCSP记录
     */
    MemberPcsp selectActivePcspByMemberId(@Param("memberId") Long memberId);
    
    /**
     * 查询会员的PCSP历史记录
     * @param memberId 会员ID
     * @return PCSP记录列表
     */
    List<MemberPcsp> selectPcspHistoryByMemberId(@Param("memberId") Long memberId);
    
    /**
     * 根据ID查询PCSP记录
     * @param id PCSP记录ID
     * @return PCSP记录
     */
    MemberPcsp selectPcspById(@Param("id") Long id);
    
    /**
     * 新增PCSP记录
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
     * 批量更新过期的PCSP状态
     * @return 更新的记录数
     */
    int updateExpiredPcspStatus();
    
    /**
     * 删除PCSP记录
     * @param id PCSP记录ID
     * @return 结果
     */
    int deletePcspById(@Param("id") Long id);
}




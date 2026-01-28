package com.medusa.mall.service;

import com.medusa.mall.domain.member.BatchUpdateLevelReq;
import com.medusa.mall.domain.member.MemberLevel;
import org.springframework.stereotype.Service;

import java.util.List;


public interface IMemberLevelService {
    public int insertMemberLevel(MemberLevel memberLevel);

    public int batchupdateMemberLevel(BatchUpdateLevelReq batchUpdateLevelReq);

    public int updateMemberLevel(MemberLevel memberLevel);

    /**
     * 根据会员ID查询会员等级信息
     * @param memberId 会员ID
     * @return 会员等级信息
     */
    public MemberLevel selectMemberLevelByMemberId(Long memberId);

    /**
     * 查询所有会员等级
     * 
     * @return 会员等级列表
     */
    public List<MemberLevel> selectAllMemberLevels();

}

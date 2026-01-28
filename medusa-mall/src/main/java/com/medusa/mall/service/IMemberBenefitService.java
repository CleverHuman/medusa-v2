package com.medusa.mall.service;

import com.medusa.mall.domain.member.MemberBenefit;
import java.util.List;


public interface IMemberBenefitService {

    public List<MemberBenefit> selectMemberBenefitList(MemberBenefit memberBenefit);

    /**
     * 根据会员等级ID查询会员权益
     * 
     * @param levelId 会员等级ID
     * @return 会员权益信息
     */
    public MemberBenefit selectMemberBenefitByLevelId(Long levelId);

    /**
     * 查询所有会员等级，按积分要求升序排列
     * 
     * @return 会员等级列表
     */
    public List<MemberBenefit> selectAllMemberBenefitsOrderByPoint();

    int insertMemberBenefit(MemberBenefit memberBenefit);
    int updateMemberBenefit(MemberBenefit memberBenefit);
}

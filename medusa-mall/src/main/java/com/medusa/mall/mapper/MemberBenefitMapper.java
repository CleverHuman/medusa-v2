package com.medusa.mall.mapper;

import com.medusa.mall.domain.member.MemberBenefit;

import java.util.List;

public interface MemberBenefitMapper {

    public List<MemberBenefit> selectMemberBenefitList(MemberBenefit memberBenefit);

    public MemberBenefit selectMemberBenefitByLevelId(Long levelId);

    /**
     * 查询所有会员等级，按积分要求升序排列
     * @return 会员等级列表
     */
    public List<MemberBenefit> selectAllMemberBenefitsOrderByPoint();

    int insertMemberBenefit(MemberBenefit memberBenefit);
    int updateMemberBenefit(MemberBenefit memberBenefit);
}

package com.medusa.mall.service.impl;


import com.medusa.mall.domain.member.MemberBenefit;
import com.medusa.mall.mapper.MemberBenefitMapper;
import com.medusa.mall.service.IMemberBenefitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MemberBenefitServiceImpl implements IMemberBenefitService {

    @Autowired
    private MemberBenefitMapper memberBenefitMapper;


    @Override
    public List<MemberBenefit> selectMemberBenefitList(MemberBenefit memberBenefit) {
        return memberBenefitMapper.selectMemberBenefitList(memberBenefit);
    }

    @Override
    public MemberBenefit selectMemberBenefitByLevelId(Long levelId) {
        return memberBenefitMapper.selectMemberBenefitByLevelId(levelId);
    }

    @Override
    public List<MemberBenefit> selectAllMemberBenefitsOrderByPoint() {
        return memberBenefitMapper.selectAllMemberBenefitsOrderByPoint();
    }

    @Override
    public int insertMemberBenefit(MemberBenefit memberBenefit) {
        return memberBenefitMapper.insertMemberBenefit(memberBenefit);
    }

    @Override
    public int updateMemberBenefit(MemberBenefit memberBenefit) {
        return memberBenefitMapper.updateMemberBenefit(memberBenefit);
    }
}

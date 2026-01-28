package com.medusa.mall.service.impl;

import com.medusa.mall.domain.member.BatchUpdateLevelReq;
import com.medusa.mall.domain.member.MemberLevel;
import com.medusa.mall.mapper.MemberLevelMapper;
import com.medusa.mall.service.IMemberLevelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MemberLevelServiceImpl implements IMemberLevelService {

    @Autowired
    private MemberLevelMapper memberLevelMapper;

    @Override
    public int insertMemberLevel(MemberLevel memberLevel) {
        return memberLevelMapper.insertMemberLevel(memberLevel);
    }


    @Override
    public int batchupdateMemberLevel(BatchUpdateLevelReq batchUpdateLevelReq){batchUpdateLevelReq.getIds();
        Long[] ids = batchUpdateLevelReq.getIds();
        Integer level = batchUpdateLevelReq.getLevel();
        return memberLevelMapper.batchupdateMemberLevel(level, ids);
    }

    @Override
    public int updateMemberLevel(MemberLevel memberLevel) {
        return memberLevelMapper.updateMemberLevel(memberLevel);
    }

    @Override
    public MemberLevel selectMemberLevelByMemberId(Long memberId) {
        return memberLevelMapper.selectMemberLevelByMemberId(memberId);
    }

    @Override
    public List<MemberLevel> selectAllMemberLevels() {
        return memberLevelMapper.selectAllMemberLevels();
    }
}

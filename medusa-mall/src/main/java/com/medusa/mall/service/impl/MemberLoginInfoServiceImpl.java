package com.medusa.mall.service.impl;

import com.medusa.mall.domain.member.MemberLoginInfo;
import com.medusa.mall.mapper.MemberLoginInfoMapper;
import com.medusa.mall.service.IMemberLoginInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;


@Service
public class MemberLoginInfoServiceImpl implements IMemberLoginInfoService {
    @Autowired
    private MemberLoginInfoMapper memberLoginInfoMapper;

    @Override
    public int insertMemberLoginInfo(MemberLoginInfo memberLoginInfo) {
        return memberLoginInfoMapper.insertMemberLoginInfo(memberLoginInfo);
    }

    @Override
    public int deleteMemberLoginInfoByIds(Long[] infoIds) {
        return memberLoginInfoMapper.deleteMemberLoginInfoByIds(infoIds);
    }

    @Override
    public List<MemberLoginInfo> selectMemberLoginInfoList(MemberLoginInfo memberLoginInfo) {
        return memberLoginInfoMapper.selectMemberLoginInfoList(memberLoginInfo);
    }
}

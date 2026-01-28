package com.medusa.mall.service;

import com.medusa.mall.domain.member.MemberLoginInfo;
import org.springframework.stereotype.Service;

import java.util.List;


public interface IMemberLoginInfoService {
    public int insertMemberLoginInfo(MemberLoginInfo memberLoginInfo);

    public int deleteMemberLoginInfoByIds(Long[] infoIds);

    public List<MemberLoginInfo> selectMemberLoginInfoList(MemberLoginInfo memberLoginInfo);

}

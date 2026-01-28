package com.medusa.mall.mapper;

import com.medusa.mall.domain.member.MemberLoginInfo;
import java.util.List;

public interface MemberLoginInfoMapper {
    public int insertMemberLoginInfo(MemberLoginInfo memberLoginInfo);

    public int deleteMemberLoginInfoByIds(Long[] infoIds);

    public List<MemberLoginInfo> selectMemberLoginInfoList(MemberLoginInfo memberLoginInfo);

}

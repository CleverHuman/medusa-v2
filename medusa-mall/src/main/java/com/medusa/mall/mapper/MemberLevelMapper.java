package com.medusa.mall.mapper;

import java.util.List;
import com.medusa.mall.domain.member.MemberLevel;
import org.apache.ibatis.annotations.Param;

public interface MemberLevelMapper {
    public int insertMemberLevel(MemberLevel memberLevel);

    public int batchupdateMemberLevel(@Param("level") int level, @Param("ids") Long[] ids);

    public int updateMemberLevel(MemberLevel memberLevel);

    /**
     * 根据会员ID查询会员等级信息
     * @param memberId 会员ID
     * @return 会员等级信息
     */
    public MemberLevel selectMemberLevelByMemberId(@Param("memberId") Long memberId);

    /**
     * 查询所有会员等级
     * 
     * @return 会员等级列表
     */
    public List<MemberLevel> selectAllMemberLevels();

}

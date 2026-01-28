package com.medusa.mall.mapper;

import com.medusa.common.core.domain.entity.Member;
import com.medusa.mall.domain.member.MemberInfo;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import org.springframework.transaction.annotation.Transactional;

public interface MemberMapper {

    public MemberInfo selectMemberInfoById(Long memberId);

    public Member checkUsernameUnique(String username);

    public Member checkTgIdUnique(String tgId);

    public Member selectMemberByUsername(String username);

    public Member selectMemberByTgId(String tgId);

    public Member selectMemberByTgId(Long tgId);

    public int insertMember(Member member);

    public int updateMember(Member member);

    public List<MemberInfo> selectMemberList(MemberInfo memberInfo);

    public int updateLevel(Long id, Integer level);

    public int updateMemberStatusByIds(@Param("status") int status, @Param("ids") Long[] ids);

    /**
     * 硬删除会员（物理删除）
     * 
     * @param ids 会员ID数组
     * @return 删除的记录数
     */
    public int deleteMemberByIds(@Param("ids") Long[] ids);

    /**
     * 更新会员备注
     * 
     * @param member 会员信息
     * @return 更新结果
     */
    public int updateMemberRemark(Member member);

    /**
     * 根据用户名搜索会员
     * 
     * @param username 用户名
     * @return 会员列表
     */
    public List<Member> searchMembersByUsername(String username);

    /**
     * 更新关联账号
     * 
     * @param params 包含memberId和linkedAccountId的Map
     * @return 更新结果
     */
    @Transactional
    public int updateLinkedAccount(Map<String, Object> params);

    /**
     * 删除关联账号
     * 
     * @param memberId 会员ID
     * @return 更新结果
     */
    @Transactional
    public int removeLinkedAccount(Long memberId);

    /**
     * 根据关联账号ID查询会员
     * 
     * @param linkedAccountId 关联账号ID
     * @return 会员信息
     */
    public Member selectMemberByLinkedAccount(Long linkedAccountId);

    /**
     * 根据会员ID查询会员基本信息
     * 
     * @param memberId 会员ID
     * @return 会员信息
     */
    public Member selectMemberByMemberId(Long memberId);

    /**
     * 查询所有活跃会员
     * 
     * @return 活跃会员列表
     */
    public List<Member> selectActiveMembers();

}

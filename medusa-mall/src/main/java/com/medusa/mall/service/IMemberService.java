package com.medusa.mall.service;

import com.medusa.common.core.domain.entity.Member;
import com.medusa.mall.domain.member.MemberInfo;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;


public interface IMemberService {

    public String register(Member member);

    public String osLogin(Member member);

    public String tgLogin(Member member, HttpServletRequest request);

    public boolean checkUsernameUnique(Member member);


    public int insertMember(Member member);

    public int updateMember(Member member);

    public List<MemberInfo> selectMemberList(MemberInfo memberInfo);

    public MemberInfo selectMemberInfoById(Long id);

    public Member selectMemberByTgId(Long tgId);

    /**
     * 根据用户名查询会员信息
     * 
     * @param username 用户名
     * @return 会员信息
     */
    public Member selectMemberInfoByUsername(String username);

    /**
     * 根据用户名查询会员基本信息
     * 
     * @param username 用户名
     * @return 会员基本信息
     */
    public Member selectMemberByUsername(String username);


    public int suspendMemberByIds(Long[] ids);

    public int resumeMemberByIds(Long[] ids);

    public int deleteMemberByIds(Long[] ids);

    /**
     * 更新会员备注
     * 
     * @param member 会员信息
     * @return 更新结果
     */
    public int updateMemberRemark(Member member);

    /**
     * 获取会员订单历史
     */
    List<com.medusa.mall.domain.member.OrderHistoryVO> getMemberOrderHistory(Long memberId);

    /**
     * 分页获取会员订单历史
     */
    List<com.medusa.mall.domain.member.OrderHistoryVO> getMemberOrderHistoryPaged(int page);

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
     * @param memberId 会员ID
     * @param linkedAccountId 关联账号ID
     * @return 更新结果
     */
    public int updateLinkedAccount(Long memberId, Long linkedAccountId);

    /**
     * 删除关联账号
     * 
     * @param memberId 会员ID
     * @return 更新结果
     */
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
     * @return 会员基本信息
     */
    public Member selectMemberByMemberId(Long memberId);

    /**
     * 合并两个账号的积分和订单数
     * @param memberId 主账号ID
     * @param linkedAccountId 关联账号ID
     * @return 合并结果信息
     */
    public Map<String, Object> mergeAccounts(Long memberId, Long linkedAccountId);

    /**
     * 查询所有活跃会员
     * 
     * @return 活跃会员列表
     */
    public List<Member> selectActiveMembers();

}

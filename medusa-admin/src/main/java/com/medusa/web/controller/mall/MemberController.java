package com.medusa.web.controller.mall;

import com.medusa.common.core.controller.BaseController;
import com.medusa.common.core.domain.AjaxResult;
import com.medusa.common.core.domain.model.LoginMember;
import com.medusa.common.core.page.TableDataInfo;
import com.medusa.common.utils.ServletUtils;
import com.medusa.mall.domain.IdsRequest;
import com.medusa.mall.domain.member.BatchUpdateLevelReq;
import com.medusa.mall.domain.member.MemberBenefit;
import com.medusa.mall.domain.member.MemberInfo;
import com.medusa.mall.domain.member.MemberLevel;
import com.medusa.mall.service.IMemberBenefitService;
import com.medusa.mall.service.IMemberLevelService;
import com.medusa.mall.service.IMemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.medusa.common.core.domain.entity.Member;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/mall/member")
public class MemberController extends BaseController {

    @Autowired
    private IMemberService memberService;

    @Autowired
    private IMemberLevelService memberLevelService;


    @GetMapping("/list")
    public TableDataInfo list(MemberInfo memberInfo) {
        startPage();
        List<MemberInfo> list = memberService.selectMemberList(memberInfo);
        return getDataTable(list);
    }

    @PutMapping("/level/update")
    public AjaxResult updateLevel(@RequestBody MemberLevel memberLevel) {
        return toAjax(memberLevelService.updateMemberLevel(memberLevel));
    }

    @PutMapping("/level/batchUpdate")
    public AjaxResult batchUpdate(@RequestBody BatchUpdateLevelReq batchUpdateLevelReq) {
        return toAjax(memberLevelService.batchupdateMemberLevel(batchUpdateLevelReq));
    }

    @PutMapping("/suspend")
    public AjaxResult suspend(@RequestBody IdsRequest req) {
        return toAjax(memberService.suspendMemberByIds(req.getIds()));
    }

    @PutMapping("/resume")
    public AjaxResult resume(@RequestBody IdsRequest req) {
        return toAjax(memberService.resumeMemberByIds(req.getIds()));
    }

    @DeleteMapping("")
    public AjaxResult remove(@RequestBody IdsRequest req) {
        return toAjax(memberService.deleteMemberByIds(req.getIds()));
    }

    /**
     * Get member info by member id
     */
    @GetMapping("/{memberId}")
    public AjaxResult getMemberInfo(@PathVariable("memberId") Long memberId) {
        MemberInfo memberInfo = memberService.selectMemberInfoById(memberId);
        return success(memberInfo);
    }

    /**
     * Add comment to member
     */
    @PostMapping("/comment")
    public AjaxResult addComment(@RequestBody Member member) {
        return toAjax(memberService.updateMemberRemark(member));
    }

    /**
     * Search members by username
     */
    @GetMapping("/search")
    public AjaxResult searchMembers(@RequestParam("username") String username) {
        List<Member> members = memberService.searchMembersByUsername(username);
        return success(members);
    }

    /**
     * Update linked account
     */
    @PutMapping("/linked-account")
    public AjaxResult updateLinkedAccount(@RequestParam("memberId") Long memberId, 
                                        @RequestParam("linkedAccountId") Long linkedAccountId) {
        return toAjax(memberService.updateLinkedAccount(memberId, linkedAccountId));
    }

    /**
     * Remove linked account
     */
    @DeleteMapping("/linked-account/{memberId}")
    public AjaxResult removeLinkedAccount(@PathVariable("memberId") Long memberId) {
        return toAjax(memberService.removeLinkedAccount(memberId));
    }

    /**
     * Get member by linked account
     */
    @GetMapping("/linked-account/{linkedAccountId}")
    public AjaxResult getMemberByLinkedAccount(@PathVariable("linkedAccountId") Long linkedAccountId) {
        Member member = memberService.selectMemberByLinkedAccount(linkedAccountId);
        return success(member);
    }

    /**
     * Merge two accounts (combine points and orders)
     */
    @PostMapping("/merge")
    public AjaxResult mergeAccounts(@RequestParam("memberId") Long memberId, 
                                   @RequestParam("linkedAccountId") Long linkedAccountId) {
        Map<String, Object> result = memberService.mergeAccounts(memberId, linkedAccountId);
        if ((Boolean) result.get("success")) {
            return success(result);
        } else {
            return error((String) result.get("message"));
        }
    }
}

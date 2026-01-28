package com.medusa.mall.controller;

import com.medusa.common.annotation.Anonymous;
import com.medusa.common.constant.Constants;
import com.medusa.common.exception.ServiceException;
import com.medusa.common.core.controller.BaseController;
import com.medusa.common.core.domain.AjaxResult;
import com.medusa.common.core.domain.entity.Member;
import com.medusa.common.core.domain.model.LoginMember;
import com.medusa.common.utils.StringUtils;
import com.medusa.framework.web.service.MemberTokenService;
import com.medusa.mall.domain.member.MemberBenefit;
import com.medusa.mall.domain.member.MemberInfo;
import com.medusa.mall.domain.member.OrderHistoryVO;
import com.medusa.mall.service.IMemberBenefitService;
import com.medusa.mall.service.IMemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.medusa.common.utils.ServletUtils;
import com.medusa.mall.service.member.IMemberPcspService;
import com.medusa.mall.domain.member.MemberPcsp;

import java.util.List;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/mall")
public class ApiMemberController extends BaseController {

    @Autowired
    private IMemberService memberService;

    @Autowired
    private MemberTokenService memberTokenService;

    @Autowired
    private IMemberBenefitService memberBenefitService;
    
    @Autowired
    private IMemberPcspService memberPcspService;

    @Anonymous
    @PostMapping("/register")
    public AjaxResult register(@RequestBody Member member){
        String msg = memberService.register(member);
        return StringUtils.isEmpty(msg) ? success() : error(msg);
    }

    @Anonymous
    @PostMapping("/login")
    public AjaxResult login(@RequestBody Member member)
    {
        try {
        String token ="";
        Integer sourceType = member.getSourceType();
        if (sourceType == 0) {
            token = memberService.osLogin(member);
        }else if (sourceType == 1){
            token = memberService.tgLogin(member,ServletUtils.getRequest());
        }
        else{
            return error("do not support the source");
        }
        if (StringUtils.isNotEmpty(token)){
            AjaxResult ajax = AjaxResult.success();
            ajax.put(Constants.TOKEN, token);
            return ajax;
        }else{
            return AjaxResult.error("system error");
            }
        } catch (ServiceException e) {
            return error(e.getMessage());
        }
    }

    @GetMapping("/getMemberInfo")
    public AjaxResult getMemberInfo() {
        LoginMember loginMember = memberTokenService.getLoginMember(ServletUtils.getRequest());
        Long memberId = loginMember.getMemberId();
        MemberInfo memberInfo = memberService.selectMemberInfoById(memberId);
        return success(memberInfo);
    }

    @GetMapping("/getMember/{memberId}")
public AjaxResult getMemberById(@PathVariable("memberId") Long memberId) {
    MemberInfo memberInfo = memberService.selectMemberInfoById(memberId);
    return success(memberInfo);
}

    @Anonymous
    @GetMapping("/getMemberBenefit")
    public AjaxResult getMemberBenefit() {
        MemberBenefit memberBenefit = new MemberBenefit();

        List<MemberBenefit> memberBenefits = memberBenefitService.selectMemberBenefitList(memberBenefit);
        return success(memberBenefits);
    }

    @GetMapping("/getMemberOrderHistory")
    public AjaxResult getMemberOrderHistory(@RequestParam(value = "page", required = false, defaultValue = "1") Integer page) {
        List<OrderHistoryVO> orderHistory = memberService.getMemberOrderHistoryPaged(page);
        return success(orderHistory);
    }
    
    /**
     * 检查PCSP状态（支持Guest用户）
     * Guest用户使用负数memberId，可以直接查询PCSP记录
     */
    @Anonymous
    @GetMapping("/member/pcsp/check/{memberId}")
    public AjaxResult checkPcspStatus(@PathVariable("memberId") Long memberId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            MemberPcsp activePcsp = memberPcspService.selectActivePcspByMemberId(memberId);
            
            if (activePcsp != null && activePcsp.isValid()) {
                result.put("hasPcsp", true);
                result.put("expiryDate", activePcsp.getExpiryDate());
                result.put("packageType", activePcsp.getPackageType());
            } else {
                result.put("hasPcsp", false);
                result.put("expiryDate", null);
                result.put("packageType", null);
            }
        } catch (Exception e) {
            result.put("hasPcsp", false);
            result.put("error", e.getMessage());
        }
        
        return success(result);
    }

}

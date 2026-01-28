package com.medusa.web.controller.mall;

import com.medusa.common.core.controller.BaseController;
import com.medusa.common.core.domain.AjaxResult;
import com.medusa.common.core.page.TableDataInfo;
import com.medusa.mall.domain.member.MemberBenefit;
import com.medusa.mall.domain.member.MemberInfo;
import com.medusa.mall.service.IMemberBenefitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequestMapping("/admin/mall/benefit")
public class BenefitController extends BaseController {

    @Autowired
    private IMemberBenefitService memberBenefitService;

    @GetMapping("/list")
    public TableDataInfo list(MemberBenefit memberBenefit) {
        startPage();
        List<MemberBenefit> list = memberBenefitService.selectMemberBenefitList(memberBenefit);
        return getDataTable(list);
    }

    /**
     * 获取会员权益详细信息
     */
    @GetMapping("/{levelId}")
    public AjaxResult getInfo(@PathVariable("levelId") Long levelId) {
        return success(memberBenefitService.selectMemberBenefitByLevelId(levelId));
    }

    /**
     * 新增会员权益
     */
    @PostMapping
    public AjaxResult add(@RequestBody MemberBenefit memberBenefit) {
        return toAjax(memberBenefitService.insertMemberBenefit(memberBenefit));
    }

    /**
     * 修改会员权益
     */
    @PutMapping
    public AjaxResult edit(@RequestBody MemberBenefit memberBenefit) {
        return toAjax(memberBenefitService.updateMemberBenefit(memberBenefit));
    }
}

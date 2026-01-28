package com.medusa.mall.controller;

import com.medusa.common.annotation.Anonymous;
import com.medusa.common.core.controller.BaseController;
import com.medusa.common.core.domain.AjaxResult;
import com.medusa.mall.domain.member.MemberPointHistory;
import com.medusa.mall.service.member.IMemberPointHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * 会员积分历史记录Controller
 */
@RestController
@RequestMapping("/api/mall/pointHistory")
public class ApiMemberPointHistoryController extends BaseController {

    @Autowired
    private IMemberPointHistoryService memberPointHistoryService;

    /**
     * 获取会员积分历史记录列表
     */
    @GetMapping("/list/{memberId}")
    @Anonymous
    public AjaxResult getPointHistoryList(@PathVariable("memberId") Long memberId) {
        List<MemberPointHistory> list = memberPointHistoryService.selectPointHistoryByMemberId(memberId);
        return AjaxResult.success(list);
    }

    /**
     * 新增积分历史记录
     */
    @PostMapping("/add")
    @Anonymous
    public AjaxResult addPointHistory(@RequestBody MemberPointHistory pointHistory) {
        int result = memberPointHistoryService.addPointHistory(
            pointHistory.getMemberId(),
            pointHistory.getAmount(),
            pointHistory.getNote(),
            pointHistory.getPlatform(),
            pointHistory.getYear() // 添加年份参数
        );
        return toAjax(result);
    }
} 
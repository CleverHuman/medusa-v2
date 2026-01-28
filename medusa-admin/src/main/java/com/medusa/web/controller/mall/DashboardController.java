package com.medusa.web.controller.mall;

import com.medusa.common.core.controller.BaseController;
import com.medusa.common.core.domain.AjaxResult;
import com.medusa.common.core.page.TableDataInfo;
import com.medusa.mall.domain.dashboard.TopStats;
import com.medusa.mall.service.IDashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/admin/mall/dashboard")
public class DashboardController extends BaseController {

    @Autowired
    private IDashboardService dashboradService;

    @GetMapping("/topStats")
    public AjaxResult topStats() {
        TopStats topStats = dashboradService.getTopStats();
        return AjaxResult.success(topStats);
    }


}

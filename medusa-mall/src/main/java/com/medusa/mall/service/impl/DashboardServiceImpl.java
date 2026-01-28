package com.medusa.mall.service.impl;

import com.medusa.mall.domain.dashboard.TopStats;
import com.medusa.mall.service.IDashboardService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class DashboardServiceImpl implements IDashboardService {

    @Override
    public TopStats getTopStats() {
        // test data
        TopStats topStats = new TopStats();
        topStats.setTotalMembers(8888);
        topStats.setWeeklyRevenue(new BigDecimal(999));
        topStats.setMonthlyRevenue(new BigDecimal(9999));
        topStats.setYearlyRevenue(new BigDecimal(99999));
        return topStats;
    }
}

package com.medusa.mall.domain.dashboard;

import java.math.BigDecimal;

public class TopStats {


    private int totalMembers;

    private BigDecimal weeklyRevenue;

    private BigDecimal MonthlyRevenue;

    private BigDecimal YearlyRevenue;


    public int getTotalMembers() {
        return totalMembers;
    }

    public void setTotalMembers(int totalMembers) {
        this.totalMembers = totalMembers;
    }

    public BigDecimal getWeeklyRevenue() {
        return weeklyRevenue;
    }

    public void setWeeklyRevenue(BigDecimal weeklyRevenue) {
        this.weeklyRevenue = weeklyRevenue;
    }

    public BigDecimal getMonthlyRevenue() {
        return MonthlyRevenue;
    }

    public void setMonthlyRevenue(BigDecimal monthlyRevenue) {
        MonthlyRevenue = monthlyRevenue;
    }

    public BigDecimal getYearlyRevenue() {
        return YearlyRevenue;
    }

    public void setYearlyRevenue(BigDecimal yearlyRevenue) {
        YearlyRevenue = yearlyRevenue;
    }
}

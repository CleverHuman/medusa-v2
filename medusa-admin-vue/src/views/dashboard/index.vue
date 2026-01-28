<template>
  <div class="app-container dashboard">
    <el-row :gutter="20" class="stats-row">
      <el-col :xs="24" :sm="12" :md="6">
        <stat-card label="Total Customer" :value="customerCount" />
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <stat-card label="Weekly Revenue" :value="weeklyRevenue" />
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <stat-card label="Monthly Revenue" :value="monthlyRevenue" />
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <stat-card label="Yearly Revenue" :value="yearlyRevenue" />
      </el-col>
    </el-row>

    <el-row :gutter="20" class="charts-row">
      <el-col :xs="24" :lg="24">
        <revenue-chart />
      </el-col>
    </el-row>

    <el-row :gutter="20" class="bottom-row">
      <el-col :xs="24" :lg="12">
        <order-percentage-chart />
      </el-col>
      <el-col :xs="24" :lg="12">
        <local-sales />
      </el-col>
    </el-row>
  </div>
</template>

<script>
import StatCard from "./StatCard.vue";
import RevenueChart from "./RevenueChart.vue";
import OrderPercentageChart from "./OrderPercentageChart.vue";
import LocalSales from "./LocalSales.vue";
import { listMembers } from "@/api/mall/member";
import { listVOrder } from "@/api/mall/vorder";

export default {
  components: {
    StatCard,
    RevenueChart,
    OrderPercentageChart,
    LocalSales
  },
  name: "Index",
  data() {
    return {
      version: "3.8.7",
      customerCount: 0,
      weeklyRevenue: "A$ 0.00",
      monthlyRevenue: "A$ 0.00",
      yearlyRevenue: "A$ 0.00"
    };
  },
  created() {
    this.fetchDashboardData();
  },
  methods: {
    goTarget(href) {
      window.open(href, "_blank");
    },
    async fetchDashboardData() {
      try {
        // 获取会员总数
        const customerResponse = await listMembers({});
        this.customerCount = customerResponse.total || 0;

        const now = new Date();
        const startOfWeek = this.getStartOfWeek(now);
        const startOfMonth = this.getStartOfMonth(now);
        const startOfYear = this.getStartOfYear(now);
        const today = this.formatDate(now);

        // 获取周收入（只统计已支付订单）
        const weeklyResponse = await listVOrder({
          beginTime: this.formatDate(startOfWeek),
          endTime: today
        });
        this.weeklyRevenue = this.formatCurrency(this.sumRevenue(weeklyResponse.rows));

        // 获取月收入（只统计已支付订单）
        const monthlyResponse = await listVOrder({
          beginTime: this.formatDate(startOfMonth),
          endTime: today
        });
        this.monthlyRevenue = this.formatCurrency(this.sumRevenue(monthlyResponse.rows));

        // 获取年收入（只统计已支付订单）
        const yearlyResponse = await listVOrder({
          beginTime: this.formatDate(startOfYear),
          endTime: today
        });
        this.yearlyRevenue = this.formatCurrency(this.sumRevenue(yearlyResponse.rows));
      } catch (error) {
        console.error("Error fetching dashboard data:", error);
      }
    },
    getStartOfWeek(date) {
      const d = new Date(date);
      const day = d.getDay();
      const diff = d.getDate() - day;
      return new Date(d.setDate(diff));
    },
    getStartOfMonth(date) {
      return new Date(date.getFullYear(), date.getMonth(), 1);
    },
    getStartOfYear(date) {
      return new Date(date.getFullYear(), 0, 1);
    },
    formatDate(date) {
      const year = date.getFullYear();
      const month = String(date.getMonth() + 1).padStart(2, '0');
      const day = String(date.getDate()).padStart(2, '0');
      return `${year}-${month}-${day}`;
    },
    sumRevenue(orders) {
      if (!orders || orders.length === 0) return 0;
      
      return orders.reduce((sum, order) => {
        // 只统计已支付的订单 (status: 1=Paid, 2=Fulfilled, 3=Shipped)
        // 不统计: 0=Pending, 4=Cancelled, 5=Deleted
        if (!order.status || order.status < 1 || order.status === 4 || order.status === 5) {
          return sum;
        }

        // tamt 是字符串，包含多个订单项价格用分号分隔，如 "19.99; 29.99; 9.99"
        if (!order.tamt) return sum;

        // 解析 tamt 字段：分割、转换为数字并求和
        const itemPrices = order.tamt.split(';').map(price => {
          const numPrice = parseFloat(price.trim());
          return isNaN(numPrice) ? 0 : numPrice;
        });

        const orderTotal = itemPrices.reduce((itemSum, price) => itemSum + price, 0);
        return sum + orderTotal;
      }, 0);
    },
    formatCurrency(value) {
      if (value === null || value === undefined) return "A$ 0.00";
      return `A$ ${Number(value).toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;
    }
  }
};
</script>

<style scoped lang="scss">
.dashboard {
  padding: 20px;

  .stats-row {
    margin-bottom: 20px;
  }

  .charts-row {
    margin-bottom: 20px;
  }

  .bottom-row {
    margin-bottom: 20px;
  }
}
</style>


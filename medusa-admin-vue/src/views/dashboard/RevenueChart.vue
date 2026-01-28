<template>
  <el-card class="revenue-chart-card" shadow="hover">
    <div slot="header">
      <span class="chart-title">Revenue</span>
    </div>
    <div class="chart-filters">
      <el-row :gutter="12">
        <el-col :span="12">
          <label class="filter-label">Timeline</label>
          <el-select v-model="timeline" placeholder="Select timeline" size="small" style="width: 120px; margin-right: 12px;">
            <el-option label="Weekly" value="weekly"></el-option>
            <el-option label="Monthly" value="monthly"></el-option>
            <!-- <el-option label="Yearly" value="yearly"></el-option> -->
          </el-select>
        </el-col>
        <el-col :span="12">
          <label class="filter-label">ProCode</label>
          <el-select v-model="proCode" placeholder="Select option" size="small" style="width: 150px;">
            <el-option label="All Products" value="all"></el-option>
            <el-option
              v-for="product in products"
              :key="product.productId"
              :label="product.name"
              :value="product.productId"
            ></el-option>
          </el-select>
        </el-col>
      </el-row>
    </div>
    <div class="legend-container">
      <div class="legend-item">
        <span class="legend-dot" style="background-color: #5470c6;"></span>
        <span class="legend-label">{{ currentYear }}</span>
      </div>
      <div class="legend-item">
        <span class="legend-dot" style="background-color: #91cc75;"></span>
        <span class="legend-label">{{ currentYear - 1 }}</span>
      </div>
      <div class="legend-item">
        <span class="legend-dot" style="background-color: #fac858;"></span>
        <span class="legend-label">{{ currentYear - 2 }}</span>
      </div>
    </div>
    <div :class="className" :style="{height:height,width:width}" />
  </el-card>
</template>

<script>
import * as echarts from 'echarts'
require('echarts/theme/macarons')
import resize from './mixins/resize'
import { listVOrder } from '@/api/mall/vorder'
import { listProduct } from '@/api/mall/product'

export default {
  name: 'RevenueChart',
  mixins: [resize],
  props: {
    className: {
      type: String,
      default: 'chart'
    },
    width: {
      type: String,
      default: '100%'
    },
    height: {
      type: String,
      default: '350px'
    }
  },
  data() {
    return {
      chart: null,
      timeline: 'weekly',
      proCode: 'all',
      products: [],
      currentYear: new Date().getFullYear(),
      loading: false
    }
  },
  watch: {
    timeline() {
      this.fetchAndUpdateChart()
    },
    proCode() {
      this.fetchAndUpdateChart()
    }
  },
  mounted() {
    this.$nextTick(() => {
      this.initChart()
      this.loadProducts()
    })
  },
  beforeDestroy() {
    if (!this.chart) {
      return
    }
    this.chart.dispose()
    this.chart = null
  },
  methods: {
    initChart() {
      this.chart = echarts.init(this.$el.querySelector('.chart'), 'macarons')
      this.fetchAndUpdateChart()
    },
    async loadProducts() {
      try {
        const response = await listProduct({})
        this.products = response.rows || []
      } catch (error) {
        console.error('Error loading products:', error)
      }
    },
    async fetchAndUpdateChart() {
      if (!this.chart) return

      this.loading = true
      try {
        const year1 = this.currentYear
        const year2 = this.currentYear - 1
        const year3 = this.currentYear - 2

        const orders1 = await this.fetchYearOrders(year1)
        const orders2 = await this.fetchYearOrders(year2)
        const orders3 = await this.fetchYearOrders(year3)

        let xAxisData = []
        let data1 = []
        let data2 = []
        let data3 = []

        if (this.timeline === 'weekly') {
          xAxisData = this.getWeeklyLabels()
          data1 = this.calculateWeeklyData(orders1, year1)
          data2 = this.calculateWeeklyData(orders2, year2)
          data3 = this.calculateWeeklyData(orders3, year3)
        } else if (this.timeline === 'monthly') {
          xAxisData = this.getMonthlyLabels()
          data1 = this.calculateMonthlyData(orders1, year1)
          data2 = this.calculateMonthlyData(orders2, year2)
          data3 = this.calculateMonthlyData(orders3, year3)
        } else if (this.timeline === 'yearly') {
          xAxisData = [year3.toString(), year2.toString(), year1.toString()]
          data1 = [this.calculateYearlyData(orders3)]
          data2 = [this.calculateYearlyData(orders2)]
          data3 = [this.calculateYearlyData(orders1)]
        }

        this.updateChartWithData(xAxisData, data1, data2, data3, year1, year2, year3)
      } catch (error) {
        console.error('Error fetching chart data:', error)
      } finally {
        this.loading = false
      }
    },
    async fetchYearOrders(year) {
      const yearStart = new Date(year, 0, 1)
      const yearEnd = new Date(year, 11, 31)
      const params = {
        beginTime: this.formatDate(yearStart),
        endTime: this.formatDate(yearEnd)
      }

      if (this.proCode !== 'all') {
        params.productId = this.proCode
      }

      try {
        const response = await listVOrder(params)
        return response.rows || []
      } catch (error) {
        console.error(`Error fetching orders for year ${year}:`, error)
        return []
      }
    },
    getWeeklyLabels() {
      return ['3 weeks ago', '2 weeks ago', '1 week ago', 'This week']
    },
    getMonthlyLabels() {
      const months = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec']
      const labels = []
      const now = new Date()
      const currentMonth = now.getMonth()

      for (let i = 11; i >= 0; i--) {
        let month = currentMonth - i
        if (month < 0) {
          month += 12
        }
        labels.push(months[month])
      }

      return labels
    },
    calculateWeeklyData(orders, year) {
      const data = new Array(4).fill(0)
      const now = new Date()
      const currentYear = now.getFullYear()

      for (let i = 0; i < 4; i++) {
        const weekEnd = new Date(now)
        weekEnd.setDate(now.getDate() - (3 - i) * 7)
        weekEnd.setFullYear(year)
        weekEnd.setHours(23, 59, 59, 999)

        const weekStart = new Date(weekEnd)
        weekStart.setDate(weekEnd.getDate() - 6)
        weekStart.setHours(0, 0, 0, 0)

        const weekOrders = orders.filter(order => {
          const orderDate = new Date(order.create_time || order.createTime || order.createdAt)
          return orderDate >= weekStart && orderDate <= weekEnd
        })

        const weekRevenue = this.sumRevenue(weekOrders)
        data[i] = weekRevenue / 1000
      }

      return data
    },
    calculateMonthlyData(orders, year) {
      const data = []
      const now = new Date()
      const currentMonth = now.getMonth()

      for (let i = 11; i >= 0; i--) {
        let month = currentMonth - i
        let yearToUse = year

        if (month < 0) {
          month += 12
          yearToUse = year - 1
        }

        const monthStart = new Date(yearToUse, month, 1)
        monthStart.setHours(0, 0, 0, 0)

        const monthEnd = new Date(yearToUse, month + 1, 0)
        monthEnd.setHours(23, 59, 59, 999)

        const monthOrders = orders.filter(order => {
          const orderDate = new Date(order.create_time || order.createTime || order.createdAt)
          return orderDate >= monthStart && orderDate <= monthEnd
        })

        const monthRevenue = this.sumRevenue(monthOrders)
        data.push(monthRevenue / 1000)
      }

      return data
    },
    calculateYearlyData(orders) {
      return this.sumRevenue(orders) / 1000
    },
    formatDate(date) {
      const year = date.getFullYear()
      const month = String(date.getMonth() + 1).padStart(2, '0')
      const day = String(date.getDate()).padStart(2, '0')
      return `${year}-${month}-${day}`
    },
    sumRevenue(orders) {
      if (!orders || orders.length === 0) return 0
      
      return orders.reduce((sum, order) => {
        // 只统计已支付的订单 (status: 1=Paid, 2=Fulfilled, 3=Shipped)
        // 不统计: 0=Pending, 4=Cancelled, 5=Deleted
        if (!order.status || order.status < 1 || order.status === 4 || order.status === 5) {
          return sum
        }

        // tamt 是字符串，包含多个订单项价格用分号分隔，如 "19.99; 29.99; 9.99"
        if (!order.tamt) return sum

        // 解析 tamt 字段：分割、转换为数字并求和
        const itemPrices = order.tamt.split(';').map(price => {
          const numPrice = parseFloat(price.trim())
          return isNaN(numPrice) ? 0 : numPrice
        })

        const orderTotal = itemPrices.reduce((itemSum, price) => itemSum + price, 0)
        return sum + orderTotal
      }, 0)
    },
    updateChartWithData(xAxisData, data1, data2, data3, year1, year2, year3) {
      const option = {
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          containLabel: true
        },
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'cross'
          },
          formatter: (params) => {
            let result = params[0].name + '<br/>'
            params.forEach(param => {
              result += `${param.marker}${param.seriesName}: A$ ${(param.value * 1000).toFixed(2)}<br/>`
            })
            return result
          }
        },
        xAxis: {
          type: 'category',
          boundaryGap: false,
          data: xAxisData,
          axisLine: {
            lineStyle: {
              color: '#e0e0e0'
            }
          },
          axisLabel: {
            color: '#999'
          }
        },
        yAxis: {
          type: 'value',
          axisLine: {
            show: false
          },
          axisTick: {
            show: false
          },
          splitLine: {
            lineStyle: {
              color: '#f0f0f0'
            }
          },
          axisLabel: {
            color: '#999',
            formatter: '{value}K'
          }
        },
        series: [
          {
            name: year1.toString(),
            type: 'line',
            smooth: true,
            data: data1,
            lineStyle: {
              color: '#5470c6',
              width: 2
            },
            itemStyle: {
              color: '#5470c6'
            },
            areaStyle: {
              color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                {
                  offset: 0,
                  color: 'rgba(84, 112, 198, 0.3)'
                },
                {
                  offset: 1,
                  color: 'rgba(84, 112, 198, 0.05)'
                }
              ])
            }
          },
          {
            name: year2.toString(),
            type: 'line',
            smooth: true,
            data: data2,
            lineStyle: {
              color: '#91cc75',
              width: 2
            },
            itemStyle: {
              color: '#91cc75'
            },
            areaStyle: {
              color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                {
                  offset: 0,
                  color: 'rgba(145, 204, 117, 0.3)'
                },
                {
                  offset: 1,
                  color: 'rgba(145, 204, 117, 0.05)'
                }
              ])
            }
          },
          {
            name: year3.toString(),
            type: 'line',
            smooth: true,
            data: data3,
            lineStyle: {
              color: '#fac858',
              width: 2
            },
            itemStyle: {
              color: '#fac858'
            },
            areaStyle: {
              color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                {
                  offset: 0,
                  color: 'rgba(250, 200, 88, 0.3)'
                },
                {
                  offset: 1,
                  color: 'rgba(250, 200, 88, 0.05)'
                }
              ])
            }
          }
        ]
      }
      this.chart.setOption(option)
    }
  }
}
</script>

<style lang="scss" scoped>
.revenue-chart-card {
  margin-bottom: 20px;

  ::v-deep .el-card__header {
    padding: 16px 20px;
    border-bottom: 1px solid #f0f0f0;
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  ::v-deep .el-card__body {
    padding: 20px;
  }

  .chart-title {
    font-size: 16px;
    font-weight: 600;
    color: #303133;
  }

  .chart-filters {
    display: flex;
    align-items: center;
    .filter-label {
      display: block;
      font-size: 13px;
      color: #606266;
      margin-bottom: 4px;
    }
  }

  .legend-container {
    margin-top: 12px;
    display: flex;
    gap: 24px;
    margin-bottom: 16px;

    .legend-item {
      display: flex;
      align-items: center;
      gap: 8px;

      .legend-dot {
        width: 12px;
        height: 12px;
        border-radius: 50%;
      }

      .legend-label {
        font-size: 14px;
        color: #606266;
      }
    }
  }
}
</style>

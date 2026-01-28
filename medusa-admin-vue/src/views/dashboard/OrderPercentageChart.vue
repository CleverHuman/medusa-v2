<template>
  <el-card class="order-chart-card" shadow="hover">
    <div slot="header">
      <span class="chart-title">Order Percentage</span>
    </div>
    <div class="chart-filters">
      <el-row :gutter="12">
        <el-col :span="12">
          <label class="filter-label">Timeline</label>
          <el-select v-model="timeline" placeholder="Select timeline" size="small" style="width: 120px; margin-right: 12px;">
            <el-option label="2025" value="2025"></el-option>
            <el-option label="2024" value="2024"></el-option>
            <el-option label="2023" value="2023"></el-option>
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
  name: 'OrderPercentageChart',
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
      default: '400px'
    }
  },
  data() {
    return {
      chart: null,
      timeline: '2025',
      proCode: 'all',
      products: [],
      loading: false,
      stateColors: {
        'TAS': '#5470c6',
        'WA': '#91cc75',
        'QLD': '#fac858',
        'NT': '#ee6666',
        'ACT': '#73c0de',
        'SA': '#3ba272',
        'NSW': '#fc8452',
        'VIC': '#9a60b4'
      }
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
        const year = parseInt(this.timeline)
        const yearStart = new Date(year, 0, 1)
        const yearEnd = new Date(year, 11, 31)

        const params = {
          beginTime: this.formatDate(yearStart),
          endTime: this.formatDate(yearEnd)
        }

        if (this.proCode !== 'all') {
          params.productId = this.proCode
        }

        const response = await listVOrder(params)
        const orders = response.rows || []

        const statesData = this.calculateStatePercentages(orders)
        this.updateChartWithData(statesData)
      } catch (error) {
        console.error('Error fetching chart data:', error)
      } finally {
        this.loading = false
      }
    },
    calculateStatePercentages(orders) {
      const stateCounts = {}
      let totalOrders = 0

      orders.forEach(order => {
        const state = (order.state || '').toUpperCase().trim()
        if (state) {
          stateCounts[state] = (stateCounts[state] || 0) + 1
          totalOrders++
        }
      })

      const statesData = []
      const states = ['TAS', 'WA', 'QLD', 'NT', 'ACT', 'SA', 'NSW', 'VIC']

      states.forEach(state => {
        const count = stateCounts[state] || 0
        const percentage = totalOrders > 0 ? (count / totalOrders * 100) : 0

        if (percentage > 0) {
          statesData.push({
            value: parseFloat(percentage.toFixed(1)),
            name: state,
            itemStyle: { color: this.stateColors[state] }
          })
        }
      })

      statesData.sort((a, b) => b.value - a.value)

      return statesData
    },
    formatDate(date) {
      const year = date.getFullYear()
      const month = String(date.getMonth() + 1).padStart(2, '0')
      const day = String(date.getDate()).padStart(2, '0')
      return `${year}-${month}-${day}`
    },
    updateChartWithData(statesData) {
      const option = {
        tooltip: {
          trigger: 'item',
          formatter: '{b}: {c}% ({d}%)'
        },
        legend: {
          show: false
        },
        series: [
          {
            name: 'Order Percentage',
            type: 'pie',
            radius: ['40%', '70%'],
            center: ['50%', '50%'],
            avoidLabelOverlap: true,
            itemStyle: {
              borderRadius: 8,
              borderColor: '#fff',
              borderWidth: 2
            },
            label: {
              show: true,
              position: 'outside',
              formatter: '{b}',
              fontSize: 14,
              fontWeight: 'bold'
            },
            emphasis: {
              label: {
                show: true,
                fontSize: 16,
                fontWeight: 'bold'
              },
              itemStyle: {
                shadowBlur: 10,
                shadowOffsetX: 0,
                shadowColor: 'rgba(0, 0, 0, 0.5)'
              }
            },
            labelLine: {
              show: true,
              length: 15,
              length2: 10
            },
            data: statesData
          }
        ]
      }
      this.chart.setOption(option)
    }
  }
}
</script>

<style lang="scss" scoped>
.order-chart-card {
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
}
</style>

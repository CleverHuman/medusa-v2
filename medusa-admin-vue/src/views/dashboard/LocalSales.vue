<template>
  <el-card class="local-sales-card" shadow="hover">
    <div slot="header">
      <span class="chart-title">Local Sales</span>
    </div>
    <div class="sales-filters">
      <el-row :gutter="12">
        <el-col :span="12">
          <div class="filter-group">
            <label class="filter-label">Start Date</label>
            <el-date-picker
              v-model="startDate"
              type="date"
              placeholder="DD-MM-YYYY"
              format="dd-MM-yyyy"
              value-format="yyyy-MM-dd"
              size="small"
              style="width: 100%;"
            />
          </div>
        </el-col>
        <el-col :span="12">
          <div class="filter-group">
            <label class="filter-label">End Date</label>
            <el-date-picker
              v-model="endDate"
              type="date"
              placeholder="DD-MM-YYYY"
              format="dd-MM-yyyy"
              value-format="yyyy-MM-dd"
              size="small"
              style="width: 100%;"
            />
          </div>
        </el-col>
      </el-row>
      <el-row :gutter="12" style="margin-top: 12px;">
        <el-col :span="12">
          <div class="filter-group">
            <label class="filter-label">ProCode</label>
            <el-select v-model="proCode" placeholder="Please select an option" size="small" style="width: 100%;">
              <el-option label="All Products" value="all"></el-option>
              <el-option
                v-for="product in products"
                :key="product.productId"
                :label="product.name"
                :value="product.productId"
              ></el-option>
            </el-select>
          </div>
        </el-col>
        <el-col :span="12">
          <div class="filter-group">
            <label class="filter-label">Postcode</label>
            <el-input
              v-model="postcode"
              placeholder="Enter a postcode"
              size="small"
              style="width: 100%;"
            >
              <i slot="suffix" class="el-icon-search"></i>
            </el-input>
          </div>
        </el-col>
      </el-row>
      <el-row style="margin-top: 16px;">
        <el-col :span="24">
          <el-button type="primary" size="small" style="width: 100%;" @click="handleSearch">Search</el-button>
        </el-col>
      </el-row>
    </div>
    <div class="sales-result">
      <div class="result-value">{{ salesCount }}</div>
    </div>
  </el-card>
</template>

<script>
import { listVOrder } from '@/api/mall/vorder'
import { listProduct } from '@/api/mall/product'

export default {
  name: 'LocalSales',
  data() {
    return {
      startDate: '',
      endDate: '',
      proCode: 'all',
      postcode: '',
      salesCount: 0,
      products: [],
      loading: false
    }
  },
  mounted() {
    this.loadProducts()
    this.initializeDates()
    this.handleSearch()
  },
  methods: {
    async loadProducts() {
      try {
        const response = await listProduct({})
        this.products = response.rows || []
      } catch (error) {
        console.error('Error loading products:', error)
      }
    },
    initializeDates() {
      const now = new Date()
      const firstDayOfMonth = new Date(now.getFullYear(), now.getMonth(), 1)

      this.startDate = this.formatDate(firstDayOfMonth)
      this.endDate = this.formatDate(now)
    },
    formatDate(date) {
      const year = date.getFullYear()
      const month = String(date.getMonth() + 1).padStart(2, '0')
      const day = String(date.getDate()).padStart(2, '0')
      return `${year}-${month}-${day}`
    },
    async handleSearch() {
      if (!this.startDate || !this.endDate) {
        this.$message.warning('Please select both start and end dates')
        return
      }

      this.loading = true
      try {
        const params = {
          beginTime: this.startDate,
          endTime: this.endDate
        }

        if (this.proCode && this.proCode !== 'all') {
          params.productId = this.proCode
        }

        if (this.postcode && this.postcode.trim() !== '') {
          params.postCode = this.postcode.trim()
        }

        const response = await listVOrder(params)
        const orders = response.rows || []

        this.salesCount = orders.length

        this.$message.success('Search completed successfully')
      } catch (error) {
        console.error('Error fetching sales data:', error)
        this.$message.error('Failed to fetch sales data')
        this.salesCount = 0
      } finally {
        this.loading = false
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.local-sales-card {
  height: 100%;

  ::v-deep .el-card__header {
    padding: 16px 20px;
    border-bottom: 1px solid #f0f0f0;
  }

  ::v-deep .el-card__body {
    padding: 20px;
  }

  .chart-title {
    font-size: 16px;
    font-weight: 600;
    color: #303133;
  }

  .sales-filters {
    margin-bottom: 24px;

    .filter-group {
      .filter-label {
        display: block;
        font-size: 13px;
        color: #606266;
        margin-bottom: 4px;
      }
    }
  }

  .sales-result {
    text-align: center;
    padding: 40px 0;

    .result-value {
      font-size: 48px;
      font-weight: 600;
      color: #303133;
    }
  }
}
</style>

<template>
  <div class="order-container">
    <h1 class="order-title">Your Orders</h1>

    <el-table
      :data="orderList"
      style="width: 100%"
      header-row-class-name="order-table-header"
      row-class-name="order-table-row"
    >
      <el-table-column prop="orderSn" label="Order No" width="180" />
      <el-table-column prop="createTime" label="Date" width="180">
        <template #default="{row}">
          {{ formatDate(row.createTime) }}
        </template>
      </el-table-column>
      <el-table-column label="Amount" width="150">
        <template #default="{row}">
          <div>Total: ¥{{ row.totalAmount }}</div>
          <div>Freight: ¥{{ row.freightAmount }}</div>
          <div>Coupon: ¥{{ row.couponAmount }}</div>
          <div>Discount: ¥{{ row.discountAmount }}</div>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="Status" width="120">
        <template #default="{row}">
          <el-tag :type="getStatusType(row.status)">
            {{ getStatusText(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="Operation" width="180">
        <template #default="{row}">
          <el-button
            v-if="row.status === 0"
            type="primary"
            size="small"
            @click="handlePay(row)"
          >
            Pay Now
          </el-button>
          <el-button
            v-if="row.status === 2"
            type="success"
            size="small"
            @click="handleConfirm(row)"
          >
            Confirm Receipt
          </el-button>
          <el-button
            v-if="row.status === 3"
            type="warning"
            size="small"
            @click="handleDispute(row)"
          >
            Dispute
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total > 0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="getList"
    />
  </div>
</template>

<script>
import { listOrder } from '@/api/order'

export default {
  data() {
    return {
      total: 0,
      orderList: [],
      queryParams: {
        pageNum: 1,
        pageSize: 10
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    getList() {
      listOrder(this.queryParams).then(response => {
        this.orderList = response.rows;
        this.total = response.total;
      }).catch(error => {
        console.error('Error fetching orders:', error)
        this.$message.error('Failed to load orders')
      })
    },
    formatDate(timestamp) {
      return new Date(timestamp).toLocaleString()
    },
    getStatusType(status) {
      const statusMap = {
        0: 'warning',   // 待付款
        1: 'info',      // 待发货
        2: 'primary',   // 已发货
        3: 'success',   // 已完成
        4: 'danger',    // 已关闭
        5: 'info'       // 无效订单
      }
      return statusMap[status] || 'info'
    },
    getStatusText(status) {
      const statusMap = {
        0: 'Pending Payment',
        1: 'Pending Shipment',
        2: 'Shipped',
        3: 'Completed',
        4: 'Closed',
        5: 'Invalid'
      }
      return statusMap[status] || 'Unknown'
    },
    handlePay(order) {
      this.$confirm('Are you sure to pay for this order?', 'Confirm', {
        confirmButtonText: 'Pay',
        cancelButtonText: 'Cancel',
        type: 'warning'
      }).then(() => {
        // TODO: 实现支付逻辑
        this.$message.success('Payment successful')
        this.getList()
      })
    },
    handleConfirm(order) {
      this.$confirm('Are you sure to confirm receipt of this order?', 'Confirm', {
        confirmButtonText: 'Confirm',
        cancelButtonText: 'Cancel',
        type: 'warning'
      }).then(() => {
        // TODO: 实现确认收货逻辑
        this.$message.success('Order confirmed')
        this.getList()
      })
    },
    handleDispute(order) {
      this.$confirm('Are you sure to open a dispute for this order?', 'Confirm', {
        confirmButtonText: 'Confirm',
        cancelButtonText: 'Cancel',
        type: 'warning'
      }).then(() => {
        // TODO: 实现纠纷处理逻辑
        this.$message.success('Dispute opened successfully')
      })
    }
  }
}
</script>

<style scoped>
.order-container {
  padding: 20px;
  background-color: #1a1a1a;
  width: 100%;
  min-height: 100vh;
}

.order-title {
  color: #fff;
  margin-bottom: 20px;
}

.order-table-header {
  background-color: #2c2c2c !important;
  color: #fff !important;
}

.order-table-row {
  background-color: #2c2c2c !important;
  color: #fff !important;
}

.el-table {
  background-color: transparent !important;
}

.el-table th,
.el-table tr {
  background-color: transparent !important;
}

.el-table td,
.el-table th.is-leaf {
  border-bottom: 1px solid #3c3c3c !important;
}

.el-table--border,
.el-table--group {
  border: 1px solid #3c3c3c !important;
}

.el-table--border::after,
.el-table--group::after {
  background-color: #3c3c3c !important;
}

.el-table--border .el-table__cell {
  border-right: 1px solid #3c3c3c !important;
}
</style>

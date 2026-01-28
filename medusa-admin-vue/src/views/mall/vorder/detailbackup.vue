<template>
  <div class="order-detail-container">
    <el-button type="text" icon="el-icon-arrow-left" @click="$router.back()">Back to order lists</el-button>
    <el-row :gutter="24">
      <!-- 左侧详情 -->
      <el-col :span="16">
        <!-- 基础信息 -->
        <el-card class="mb20">
          <div class="order-header">
            <span class="order-id">#{{ order.orderSn }}</span>
            <span class="order-status">{{ getStatusText(order.status) }}</span>
          </div>
          <div style="display: flex; flex-direction: row; justify-content: space-between; align-items: flex-start;">
            <div class="order-meta-vertical">
              <div><b>Username</b>: {{ memberInfo.username || order.username }}</div>
              <div><b>Member ID</b>: {{ order.memberId }}</div>
              <div><b>Customer Level</b>: {{ memberInfo.currentLevel || order.memberLevel }}</div>
              <div><b>Total Points</b>: {{ memberInfo.currentPoint }}</div>
              <div><b>Total Orders</b>: {{ memberInfo.totalOrders }}</div>
              <div><b>Last Seen</b>: {{ memberInfo.lastSeen }}</div>
              <div><b>Order Channel</b>: {{ order.sourceType }}</div>
              <div><b>Order Time</b>: {{ order.createTime }}</div>
            </div>
            <div style="flex:1"></div>
            <div class="shipping-meta" style="min-width:220px;max-width:300px;">
              <div class="section-title" style="margin-bottom: 6px;">Shipping Info</div>
              <div><b>Name</b>: {{ shipping.name }}</div>
              <div><b>Address1</b>: {{ shipping.address1 }}</div>
              <div v-if="shipping.address2"><b>Address2</b>: {{ shipping.address2 }}</div>
              <div><b>City</b>: {{ shipping.city }}</div>
              <div><b>State</b>: {{ shipping.state }}</div>
              <div><b>Post Code</b>: {{ shipping.postalCode }}</div>
              <div><b>Country</b>: {{ shipping.country }}</div>
              <div><b>Shipping Method</b>: {{ shipping.shippingMethod }}</div>
            </div>
          </div>
        </el-card>

        <!-- 商品明细 -->
        <el-card class="mb20">
          <div class="section-title">Summary</div>
          <el-table :data="items" :border="false" style="border:none;">
            <el-table-column label="ProCode" prop="productId" />
            <el-table-column label="Name" prop="productName" />
            <el-table-column label="Spec" prop="productSpec" />
            <el-table-column label="Price x Qty">
              <template slot-scope="scope">
                {{ scope.row.price }} x {{ scope.row.quantity }}
              </template>
            </el-table-column>
            <el-table-column label="Total" align="right">
              <template slot-scope="scope">
                {{ (scope.row.price * scope.row.quantity).toFixed(2) }} AUD
              </template>
            </el-table-column>
          </el-table>
          <div class="summary-total">
            <div class="summary-row"><span>Subtotal</span><span>{{ order.totalAmount }}</span></div>
            <div class="summary-row"><span>Discount</span><span>-{{ order.discountAmount }}</span></div>
            <div class="summary-row"><span>Shipping</span><span>{{ order.freightAmount }}</span></div>
            <div class="summary-row total"><span>Total</span><span>{{ order.totalAmount }}</span></div>
          </div>
        </el-card>

        <!-- 支付信息 -->
        <el-card class="mb20">
          <div class="section-title" style="display: flex; justify-content: space-between; align-items: center;">
            <span>Payment</span>
            <span>
              <template v-if="order.status === 0 && !paidFlag">
                <el-button type="success" size="mini" @click="markAsPaid">Mark as Paid</el-button>
              </template>
              <template v-else-if="order.status === 1 || paidFlag">
                <el-tag type="success">Order Paid</el-tag>
              </template>
            </span>
          </div>
          <div>Payment No: {{ payment.paymentNo }}</div>
          <div>Amount: {{ payment.payAmount }}</div>
          <div>Status: {{ payment.payStatus }}</div>
          <div style="display: flex; justify-content: space-between; align-items: center; margin-top: 12px;">
            <span style="font-weight: bold;">Total Paid</span>
            <span style="font-weight: bold; font-size: 18px;">{{ payment.payAmount }}</span>
          </div>
        </el-card>

        <!-- 发货信息 -->
        <el-card class="mb20">
          <div class="section-title" style="display: flex; justify-content: space-between; align-items: center;">
            <span>Fulfillment</span>
            <span>
              <template v-if="order.status === 1 && !fulfilledFlag">
                <el-button type="primary" size="mini" @click="markAsFulfilled">Mark as Fulfilled</el-button>
              </template>
              <template v-else-if="order.status === 2 || fulfilledFlag">
                <el-tag type="info">Order Fulfilled</el-tag>
              </template>
            </span>
          </div>
          <div>Fulfillment No: {{ order.fulfillmentNo }}</div>
          <div>Status: {{ getFulfillmentStatusText(order.fulfillmentStatus) }}</div>
        </el-card>

        <!-- 物流信息 -->
        <el-card>
          <div class="section-title" style="display: flex; justify-content: space-between; align-items: center;">
            <span>Shipping</span>
            <span>
              <template v-if="order.status === 2 && !shippedFlag">
                <el-button type="warning" size="mini" @click="markAsShipped">Mark as Shipped</el-button>
              </template>
              <template v-else-if="order.status === 3 || shippedFlag">
                <el-tag type="warning">Order Shipped</el-tag>
              </template>
            </span>
          </div>
          <div style="display: flex; align-items: center; justify-content: space-between; margin-bottom: 10px;">
            <div>
              <b>Shipping Method:</b> {{ shipping.shippingMethod }}
            </div>
            <div style="display: flex; align-items: center;">
              <span style="margin-right: 8px;">Upgrade Shipping:</span>
              <el-select v-model="upgradeShippingMethod" placeholder="Select" size="mini" style="width: 160px;">
                <el-option
                  v-for="item in upgradableShippingMethods"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </div>
          </div>
          <div>Tracking Number: {{ shipping.shippingNumber }}</div>
        </el-card>
      </el-col>

      <!-- 右侧编辑区 -->
      <el-col :span="8">
        <el-card>
          <el-form :model="editForm" label-width="120px">
            <el-form-item label="Admin comment">
              <el-input type="textarea" v-model="editForm.adminComment" placeholder="Comment" />
            </el-form-item>
            <el-form-item label="Tracking number">
              <el-input v-model="editForm.trackingNumber" placeholder="Tracking number" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="saveChanges">Save changes</el-button>
              <el-button type="danger" @click="deleteOrder">Delete</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import request from '@/utils/request';

export default {
  name: "OrderDetail",
  dicts: ['order_status', 'source_type', 'member_level','channel_type'],
  data() {
    return {
      order: {},
      items: [],
      shipping: {},
      payment: {},
      memberInfo: {},
      paidFlag: false,
      fulfilledFlag: false,
      shippedFlag: false,
      upgradeShippingMethod: '',
      upgradableShippingMethods: [
        { value: 'Standard', label: 'Standard' },
        { value: 'Express', label: 'Express' },
        { value: 'Overnight', label: 'Overnight' }
      ],
      editForm: {
        adminComment: '',
        trackingNumber: ''
      }
    };
  },
  created() {
    const id = this.$route.query.id;
    this.getInfo(id);
  },
  methods: {
    getInfo(id) {
      request({
        url: `/admin/mall/order/detail/${id}`,
        method: 'get'
      }).then(res => {
        this.order = res.data.order || {};
        this.items = res.data.items || [];
        this.shipping = res.data.shipping || {};
        this.payment = res.data.payment || {};
        this.editForm.adminComment = this.shipping.remark || '';
        this.editForm.trackingNumber = this.shipping.shippingNumber || '';
        this.upgradeShippingMethod = this.shipping.shippingMethod || '';
        
        if (this.order.memberId) {
          this.getMemberInfo(this.order.memberId);
        }
      });
    },

    getMemberInfo(memberId) {
      request({
        url: `/admin/mall/member/${memberId}`,
        method: 'get'
      }).then(res => {
        if (res.code === 200) {
          this.memberInfo = res.data;
        }
      });
    },

    computed: {
  formattedPayAmount() {
    return (this.payment.payAmount / 100).toFixed(2); // 保留两位小数
  }
},
    saveChanges() {
    request({
      url: '/admin/mall/order/updateShippingInfo',
      method: 'post',
      data: {
        orderId: this.shipping.orderId,
        remark: this.editForm.adminComment,
        shippingNumber: this.editForm.trackingNumber
      }
    }).then(res => {
      if (res.code === 200) {
        this.$message.success('Shipping info updated!');
      } else {
        this.$message.error('Update failed!');
      }
    });
  },
    getStatusText(status) {
      // 可根据status返回文本
      return '';
    },
    getPayStatusText(status) {
      return '';
    },
    getFulfillmentStatusText(status) {
      return '';
    },

    deleteOrder() {
      // 删除订单
    },

    markAsPaid() {
      // 假设有 updateOrder 接口
      request({
        url: '/admin/mall/order',
        method: 'put',
        data: {
          ...this.order,
          status: 1
        }
      }).then(res => {
        if (res.code === 200) {
          this.$message.success('Order marked as paid!');
          this.paidFlag = true;
          this.order.status = 1;
        } else {
          this.$message.error('Failed to update order status!');
        }
      });
    },

    markAsFulfilled() {
      request({
        url: '/admin/mall/order',
        method: 'put',
        data: {
          ...this.order,
          status: 2
        }
      }).then(res => {
        if (res.code === 200) {
          this.$message.success('Order marked as fulfilled!');
          this.fulfilledFlag = true;
          this.order.status = 2;
        } else {
          this.$message.error('Failed to update order status!');
        }
      });
    },

    markAsShipped() {
      request({
        url: '/admin/mall/order',
        method: 'put',
        data: {
          ...this.order,
          status: 3
        }
      }).then(res => {
        if (res.code === 200) {
          this.$message.success('Order marked as shipped!');
          this.shippedFlag = true;
          this.order.status = 3;
        } else {
          this.$message.error('Failed to update order status!');
        }
      });
    }
  }
};
</script>

<style scoped>
.order-detail-container {
  padding: 24px;
  background: #f5f6fa;
}
.mb20 {
  margin-bottom: 20px;
}
.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 20px;
  font-weight: bold;
}
.order-meta-vertical {
  margin-top: 10px;
  color: #333;
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.shipping-meta {
  margin-top: 10px;
  color: #333;
  background: #f8f8f8;
  border-radius: 6px;
  padding: 10px 14px;
  font-size: 13px;
}
.section-title {
  font-weight: bold;
  margin-bottom: 10px;
}
.summary-total {
  margin-top: 10px;
  text-align: right;
}
.summary-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2px;
}
.summary-row.total {
  font-size: 18px;
  font-weight: bold;
  color: #222;
}
</style> 
<template>
    <div class="order-detail">
      <el-button type="text" @click="$router.push('/orders')">&larr; Back to order lists</el-button>
  
      <el-row :gutter="20" class="mt-4">
        <!-- Left Column -->
        <el-col :span="16">
          <!-- Header Card -->
          <el-card class="mb-4">
            <div class="flex justify-between">
              <div>
                <div class="text-lg font-bold">#{{ order.orderSn }}</div>
                <div class="text-muted">{{ order.createTime }}</div>
              </div>
              <div class="text-success">● {{ getStatusText(order.status) }}</div>
            </div>
            <div class="meta-line text-muted mt-2">
              {{ order.username }} ｜
              {{ order.contact }} ｜
              {{ order.memberLevel }} ｜
              {{ order.sourceType }} ｜
              {{ order.address }}
            </div>
          </el-card>
  
          <!-- Summary Card -->
          <el-card class="mb-4">
            <div slot="header">Summary</div>
  
            <el-row v-for="(item, index) in summaryItems" :key="index" class="mb-2">
              <el-col :span="4"><div class="img-placeholder">Image</div></el-col>
              <el-col :span="12">
                <div class="font-medium">{{ item.name }}</div>
                <div class="text-muted text-xs">{{ item.code }}</div>
              </el-col>
              <el-col :span="4" class="text-right">A$ {{ item.price }} × {{ item.qty }}</el-col>
              <el-col :span="4" class="text-right">A$ {{ item.total }} AUD</el-col>
            </el-row>
  
            <el-divider></el-divider>
  
            <div class="flex justify-between"><span>Subtotal:</span><span>A$255.00 AUD</span></div>
            <div class="flex justify-between text-muted"><span>Discount: <el-tag size="mini">Gold level discount</el-tag></span><span>-A$20.00 AUD</span></div>
            <div class="flex justify-between"><span>Shipping:</span><span>A$17.00 AUD</span></div>
            <div class="flex justify-between"><span>Tax:</span><span>A$0.00 AUD</span></div>
            <div class="flex justify-between font-bold"><span>Total:</span><span>A$252.00 AUD</span></div>
          </el-card>
  
          <!-- Payment Card -->
          <el-card class="mb-4">
            <div slot="header">Payment</div>
            <p class="text-muted">{{ payment.paymentNo }}</p>
            <p class="text-muted">{{ payment.payTime }}</p>
            <div class="font-bold">Total Paid: A$ {{ payment.payAmount }} AUD</div>
          </el-card>
  
          <!-- Fulfillment Card -->
          <el-card class="mb-4">
            <div slot="header">Fulfillment</div>
            <p class="text-muted">{{ order.fulfillmentNo }}</p>
            <p>Status: {{ getFulfillmentStatusText(order.fulfillmentStatus) }}</p>
            <el-button type="primary" size="small">Mark as Fulfilled</el-button>
          </el-card>
  
          <!-- Shipping Card -->
          <el-card>
            <div slot="header">Shipping</div>
            <p class="text-muted">Shipping Method: <strong>{{ shipping.shippingMethod }}</strong></p>
            <p>Tracking Number: {{ shipping.trackingNumber }}</p>
            <el-button type="primary" size="small">Mark as Shipped</el-button>
          </el-card>
        </el-col>
  
        <!-- Right Column -->
        <el-col :span="8">
          <el-card class="mb-4">
            <div slot="header">Admin comment</div>
            <el-input type="textarea" rows="6" v-model="editForm.adminComment" placeholder="Comment"></el-input>
          </el-card>
  
          <el-card>
            <div slot="header">Tracking number</div>
            <el-input v-model="editForm.trackingNumber" placeholder="Tracking number" class="mb-3"></el-input>
            <div class="flex justify-between">
              <el-button type="primary" @click="saveChanges">Save changes</el-button>
              <el-button type="danger" @click="deleteOrder">Delete</el-button>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </template>
  
  <script>
  export default {
    name: 'OrderDetail',
    data() {
      return {
        order: {
          orderSn: '4971',
          createTime: '11 February 2025 12:36 am',
          username: 'buddog1',
          contact: 'buddog1@tg.com',
          memberLevel: 'Gold',
          sourceType: 'Telegram',
          address: 'Glen Birrell 9 Gray Place Curtin, ACT, 2605',
          fulfillmentNo: 'order_01JKS6YXFERCJMBQHNXPKNSRTPD',
          fulfillmentStatus: 0,
          status: 1
        },
        summaryItems: [
          { name: 'Hawaiian', code: 'HW0.5', price: '165.88', qty: 1, total: '165.88' },
          { name: 'Cold Brew', code: 'CB0.25', price: '69.12', qty: 1, total: '69.12' }
        ],
        payment: {
          paymentNo: 'order_01JKS6YXFERCJMBQHNXPKNSRTPD',
          payTime: '11 February 2025 12:50am',
          payAmount: 252.00
        },
        shipping: {
          shippingMethod: 'Super Stealth Post (Recommended)',
          trackingNumber: 'AU1234567890'
        },
        editForm: {
          adminComment: '',
          trackingNumber: ''
        }
      }
    },
    methods: {
      getStatusText(status) {
        return status === 1 ? 'Order Paid' : 'Pending';
      },
      getFulfillmentStatusText(status) {
        return status === 0 ? 'Awaiting fulfillment' : 'Fulfilled';
      },
      saveChanges() {
        this.$message.success('Changes saved (stub)');
      },
      deleteOrder() {
        this.$message.warning('Delete triggered (stub)');
      }
    }
  }
  </script>
  
  <style scoped>
  .img-placeholder {
    width: 100%;
    height: 64px;
    background-color: #f5f5f5;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 12px;
    color: #999;
  }
  .text-muted {
    color: #888;
  }
  .text-success {
    color: #21ba45;
  }
  .meta-line {
    font-size: 13px;
    line-height: 1.8;
  }
  </style>
  
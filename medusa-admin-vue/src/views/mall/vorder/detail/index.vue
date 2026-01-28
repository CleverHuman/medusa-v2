<template>
  <div class="order-detail-container">
    <el-button type="text" icon="el-icon-arrow-left" @click="$router.back()">Back to order lists</el-button>
    <el-row :gutter="24">
      <!-- Left Details -->
      <el-col :span="16">
        <!-- Basic Information -->
        <el-card class="mb20">
          <div class="order-header">
            <span class="order-id">#{{ (order && order.orderSn) || 'Loading...' }}</span>
            <div style="display: flex; align-items: center; gap: 10px;">
              <span class="order-status">{{ order && getStatusText(order.status) }}</span>
              <!-- isDispute 按钮 -->
              <el-button 
                v-if="order && order.isdispute === 1" 
                type="danger" 
                size="mini" 
                @click="resolveDispute"
              >
                Resolve Dispute
              </el-button>
              <el-button 
                v-if="order && order.isdispute === 1" 
                type="info" 
                size="mini" 
                @click="viewDisputeDetails"
              >
                View
              </el-button>
            </div>
          </div>
          <div style="display: flex; flex-direction: row; justify-content: space-between; align-items: flex-start;">
            <div class="order-meta">
              <div class="meta-row">
                <span class="label">Order Time:</span>
                <span class="value">{{ (order && order.createTime) || 'N/A' }}</span>
              </div>
              <div class="meta-row">
                <span class="label"><b>Username</b>:</span>
                <span class="value">{{ (memberInfo && memberInfo.username) || (order && order.username) || 'N/A' }}</span>
              </div>
              <div class="meta-row">
                <span class="label"><b>Contact</b>:</span>
                <span class="value">{{ (memberInfo && memberInfo.primaryContact) || 'N/A' }}</span>
              </div>
              <div class="meta-row">
                <span class="label"><b>Customer Level</b>:</span>
                <span class="value">
                  <dict-tag 
                    v-if="memberInfo && memberInfo.currentLevel" 
                    :options="dict.type.member_level" 
                    :value="memberInfo.currentLevel"
                  />
                  <span v-else>N/A</span>
                </span>
              </div>
              <div class="meta-row">
                <span class="label"><b>Order Channel</b>:</span>
                <span class="value">
                  <dict-tag 
                    v-if="order && order.sourceType" 
                    :options="dict.type.source_type" 
                    :value="order.sourceType"
                  />
                  <span v-else>N/A</span>
                </span>
              </div>
 
            </div>
            <div style="flex:1"></div>
            <div class="shipping-meta" style="min-width:220px;max-width:300px;">
              <div class="section-title" style="margin-bottom: 6px;">Shipping Info</div>
              <div><b>Name</b>: {{ (shipping && shipping.name) || 'N/A' }}</div>
              <div><b>Address1</b>: {{ (shipping && shipping.address1) || 'N/A' }}</div>
              <div v-if="shipping && shipping.address2"><b>Address2</b>: {{ shipping.address2 }}</div>
              <div><b>City</b>: {{ (shipping && shipping.city) || 'N/A' }}</div>
              <div><b>State</b>: {{ (shipping && shipping.state) || 'N/A' }}</div>
              <div><b>Post Code</b>: {{ (shipping && shipping.postalCode) || 'N/A' }}</div>
              <div><b>Country</b>: {{ (shipping && shipping.country) || 'N/A' }}</div>
              <div><b>Shipping Method</b>: {{ (shipping && shipping.shippingMethod) || 'N/A' }}</div>
            </div>
          </div>
        </el-card>

        <!-- Product Details -->
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
                {{ (scope.row.price * scope.row.quantity).toFixed(2) }}
              </template>
            </el-table-column>
          </el-table>
          <div class="summary-total">
            <div class="summary-row"><span>Subtotal</span><span>{{ calculateSubtotal() }} AUD</span></div>
            <div class="summary-row"><span>Discount</span><span>-{{ (order && order.discountAmount) || 0 }} AUD</span></div>
            <div class="summary-row"><span>Shipping</span><span>{{ (order && order.freightAmount) || 0 }} AUD</span></div>
            <div v-if="hasFreeShippingBenefit" class="summary-row member-benefit">
              <span>Member Free Shipping</span>
              <span>-{{ (order && order.freightAmount) || 0 }} AUD</span>
            </div>
            <div class="summary-row total"><span>Total</span><span>{{ (order && order.totalAmount) || 0 }} {{ getCurrencyUnit() }}</span></div>
          </div>
        </el-card>

        <!-- Payment Information -->
        <el-card class="mb20">
          <div class="section-title" style="display: flex; justify-content: space-between; align-items: center;">
            <span>Payment</span>
            <span>
              <template v-if="order && order.status === 0 && !paidFlag">
                <el-button type="success" size="mini" @click="markAsPaid">Mark as Paid</el-button>
              </template>
              <template v-else-if="order && (order.status === 1 || paidFlag)">
                <el-tag type="success">Order Paid</el-tag>
              </template>
            </span>
          </div>
          <div>Payment No: {{ (payment && payment.paymentNo) || 'N/A' }}</div>
          <div>Amount: {{ (payment && payment.payAmount) || '0' }}</div>
          <div>Status: {{ (payment && payment.payStatus) || 'N/A' }}</div>
          <div style="display: flex; justify-content: space-between; align-items: center; margin-top: 12px;">
            <span style="font-weight: bold;">Total Paid</span>
            <span style="font-weight: bold; font-size: 18px;">{{ (payment && payment.payAmount) || '0' }}</span>
          </div>
        </el-card>

        <!-- Fulfillment Information -->
        <el-card class="mb20">
          <div class="section-title" style="display: flex; justify-content: space-between; align-items: center;">
            <span>Fulfillment</span>
            <span>
              <template v-if="order && order.status === 1 && !fulfilledFlag">
                <el-button type="primary" size="mini" @click="markAsFulfilled">Mark as Fulfilled</el-button>
              </template>
              <template v-else-if="order && (order.status === 2 || fulfilledFlag)">
                <el-tag type="info">Order Fulfilled</el-tag>
              </template>
            </span>
          </div>
          <div>Fulfillment No: {{ (payment && payment.paymentNo) || 'N/A' }}</div>
          <div>Status: {{ order && getFulfillmentStatusText(order.fulfillmentStatus) }}</div>
        </el-card>

        <!-- Shipping Information -->
        <el-card>
          <div class="section-title" style="display: flex; justify-content: space-between; align-items: center;">
            <span>Shipping</span>
            <span>
              <template v-if="order && order.status === 2 && !shippedFlag">
                <el-button type="warning" size="mini" @click="markAsShipped">Mark as Shipped</el-button>
              </template>
              <template v-else-if="order && (order.status === 3 || shippedFlag)">
                <el-tag type="warning">Order Shipped</el-tag>
              </template>
            </span>
          </div>
            <div style="display: flex; align-items: center; justify-content: space-between; margin-bottom: 10px;">
            <div>
              <b>Shipping:</b> {{ shippingMethodName }}
            </div>
            <div style="display: flex; align-items: center;">
              <span style="margin-right: 8px;">Upgrade Shipping:</span>
              <el-select 
                v-model="upgradeShippingMethod" 
                placeholder="Select" 
                size="mini" 
                style="width: 250px;"
                :disabled="!upgradableShippingMethods.length"
              >
                <el-option
                  v-for="item in upgradableShippingMethods"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </div>
          </div>
          <div>Tracking Number: {{ (shipping && shipping.shippingNumber) || 'N/A' }}</div>
        </el-card>
      </el-col>

      <!-- Right Edit Area -->
      <el-col :span="8">
        <el-card>
          <el-form :model="editForm" label-width="120px">
            <!-- Customer Notes (Read-only) -->
            <el-form-item label="Customer Notes" v-if="order && order.customerComment">
              <el-alert
                type="info"
                :closable="false"
                show-icon>
                <div slot="title" style="white-space: pre-wrap; word-break: break-word; line-height: 1.6;">
                  {{ order.customerComment }}
                </div>
              </el-alert>
              <div style="font-size: 12px; color: #909399; margin-top: 8px;">
                📝 Customer left this note during checkout
              </div>
            </el-form-item>

            <!-- Admin Comment (Editable) -->
            <el-form-item label="Admin comment">
              <el-input 
                type="textarea" 
                v-model="editForm.adminComment" 
                placeholder="Comment" 
                :rows="4"
                :disabled="false"
                clearable
              />
            </el-form-item>
            <el-form-item label="Tracking number">
              <el-input 
                v-model="editForm.trackingNumber" 
                placeholder="Tracking number" 
                :disabled="false"
                clearable
              />
            </el-form-item>
            
            <!-- 地址编辑区域 -->
            <el-divider content-position="left">Shipping Address</el-divider>
            
            <el-form-item label="Receiver Name">
              <el-input v-model="editForm.receiverName" placeholder="Full name" />
            </el-form-item>
            
            <el-form-item label="Address Line 1">
              <el-input v-model="editForm.addressLine1" placeholder="Street address" />
            </el-form-item>
            
            <el-form-item label="Address Line 2">
              <el-input v-model="editForm.addressLine2" placeholder="Apartment, suite, etc. (optional)" />
            </el-form-item>
            
            <el-form-item label="Address Line 3">
              <el-input v-model="editForm.addressLine3" placeholder="Additional address info (optional)" />
            </el-form-item>
            
            <el-form-item label="City">
              <el-input v-model="editForm.city" placeholder="City" />
            </el-form-item>
            
            <el-form-item label="State">
              <el-input v-model="editForm.state" placeholder="State/Province" />
            </el-form-item>
            
            <el-form-item label="Post Code">
              <el-input v-model="editForm.postCode" placeholder="Post code" />
            </el-form-item>
            
            <el-form-item label="Country">
              <el-input v-model="editForm.country" placeholder="Country" />
            </el-form-item>
            
            <el-form-item>
              <el-button type="primary" @click="saveChanges">Save changes</el-button>
              <el-button type="danger" @click="deleteOrder">Delete</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
    </el-row>

    <!-- Dispute Details Dialog -->
    <el-dialog 
      title="Dispute Details" 
      :visible.sync="disputeDialogVisible" 
      width="600px"
      :close-on-click-modal="false"
    >
      <div v-if="order && order.isdispute === 1" class="dispute-details">
        <div class="dispute-section">
          <h4>Dispute Information</h4>
          <div class="dispute-row">
            <span class="dispute-label">Dispute Status:</span>
            <el-tag type="danger">Active Dispute</el-tag>
          </div>
          <div class="dispute-row" v-if="order && order.disputeTime">
            <span class="dispute-label">Dispute Time:</span>
            <span class="dispute-value">{{ formatDateTime(order.disputeTime) }}</span>
          </div>
          <div class="dispute-row" v-if="order && order.disputeBy">
            <span class="dispute-label">Disputed By:</span>
            <span class="dispute-value">{{ order.disputeBy }}</span>
          </div>
          <div class="dispute-row" v-if="order && order.disputeReason">
            <span class="dispute-label">Dispute Reason:</span>
            <div class="dispute-reason">{{ order.disputeReason }}</div>
          </div>
        </div>
        
        <div class="dispute-section">
          <h4>Order Information</h4>
          <div class="dispute-row">
            <span class="dispute-label">Order ID:</span>
            <span class="dispute-value">{{ (order && order.orderSn) || 'N/A' }}</span>
          </div>
          <div class="dispute-row">
            <span class="dispute-label">Order Date:</span>
            <span class="dispute-value">{{ order && formatDateTime(order.createTime) }}</span>
          </div>
          <div class="dispute-row">
            <span class="dispute-label">Order Status:</span>
            <span class="dispute-value">{{ order && getStatusText(order.status) }}</span>
          </div>
          <div class="dispute-row">
            <span class="dispute-label">Total Amount:</span>
            <span class="dispute-value">{{ (order && order.totalAmount) || 0 }} AUD</span>
          </div>
        </div>
      </div>
      
      <div slot="footer" class="dialog-footer">
        <el-button @click="disputeDialogVisible = false">Close</el-button>
        <el-button type="danger" @click="resolveDisputeFromDialog">Resolve Dispute</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import request from '@/utils/request';

export default {
  name: "OrderDetail",
  dicts: ['order_status', 'source_type', 'member_level', 'channel_type'],
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
      disputeDialogVisible: false,
      shippingMethodName: '', // 存储从数据库获取的shipping method名称
      upgradableShippingMethods: [],
      editForm: {
        adminComment: '',
        trackingNumber: '',
        receiverName: '',
        addressLine1: '',
        addressLine2: '',
        addressLine3: '',
        city: '',
        state: '',
        postCode: '',
        country: ''
      }
    };
  },
  computed: {
    // 检查是否有免邮费权益
    hasFreeShippingBenefit() {
      // 检查会员等级是否为Platinum或Diamond
      if (this.memberInfo && this.memberInfo.currentLevel) {
        const levelName = this.getMemberLevelName(this.memberInfo.currentLevel);
        console.log('Free shipping check:', {
          memberInfo: this.memberInfo,
          currentLevel: this.memberInfo.currentLevel,
          levelName: levelName,
          hasBenefit: levelName === 'Platinum' || levelName === 'Diamond'
        });
        return levelName === 'Platinum' || levelName === 'Diamond';
      }
      console.log('No member info for free shipping check:', this.memberInfo);
      return false;
    }
  },
  watch: {
    upgradeShippingMethod(newVal) {
      if (newVal) {
        // 当选择了新的物流方式时，更新显示
        this.shipping.shippingMethod = newVal;
      }
    }
  },
  created() {
    const id = this.$route.query.id;
    this.getInfo(id);
    this.getShippingMethods();
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
        this.shippingMethodName = res.data.shippingMethodName || this.shipping.shippingMethod || '';
        
        // 填充编辑表单
        this.editForm.adminComment = this.order.remark || '';
        this.editForm.trackingNumber = this.shipping.shippingNumber || '';
        
        // 填充地址信息到编辑表单
        this.editForm.receiverName = this.shipping.name || '';
        this.editForm.addressLine1 = this.shipping.address1 || '';
        this.editForm.addressLine2 = this.shipping.address2 || '';
        this.editForm.addressLine3 = this.shipping.address3 || '';
        this.editForm.city = this.shipping.city || '';
        this.editForm.state = this.shipping.state || '';
        this.editForm.postCode = this.shipping.postalCode || '';
        this.editForm.country = this.shipping.country || '';
        
        this.upgradeShippingMethod = this.shipping.shippingMethod || '';
        
        if (this.order.memberId) {
          this.getMemberInfo(this.order.memberId);
        }
      });
    },

    getShippingMethods() {
      request({
        url: '/admin/mall/shipping/list',
        method: 'get',
        params: { status: 1 }
      }).then(res => {
        if (res.rows && res.rows.length > 0) {
          this.upgradableShippingMethods = res.rows.map(method => ({
            value: method.code,
            label: method.name
          }));
        }
      }).catch(error => {
        console.error('Failed to get shipping methods:', error);
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
    saveChanges() {
      request({
        url: '/admin/mall/order/updateShippingInfo',
        method: 'post',
        data: {
          orderId: this.shipping.orderId,
          remark: this.editForm.adminComment,
          shippingNumber: this.editForm.trackingNumber,
          shippingMethod: this.upgradeShippingMethod || this.shipping.shippingMethod,
          // 添加地址信息（字段名需要与后端 ShippingAddress 对象匹配）
          name: this.editForm.receiverName,
          address1: this.editForm.addressLine1,
          address2: this.editForm.addressLine2,
          address3: this.editForm.addressLine3,
          city: this.editForm.city,
          state: this.editForm.state,
          postalCode: this.editForm.postCode,
          country: this.editForm.country
        }
      }).then(res => {
        if (res.code === 200) {
          this.$message.success('Changes saved successfully!');
          // 更新本地显示数据
          this.shipping.name = this.editForm.receiverName;
          this.shipping.address1 = this.editForm.addressLine1;
          this.shipping.address2 = this.editForm.addressLine2;
          this.shipping.address3 = this.editForm.addressLine3;
          this.shipping.city = this.editForm.city;
          this.shipping.state = this.editForm.state;
          this.shipping.postalCode = this.editForm.postCode;
          this.shipping.country = this.editForm.country;
          this.shipping.shippingNumber = this.editForm.trackingNumber;
          // 重新获取订单详情以显示最新数据
          this.getInfo(this.order.id);
        } else {
          this.$message.error('Failed to save changes!');
          // 即使失败也刷新页面，确保显示最新状态
          this.getInfo(this.order.id);
        }
      }).catch(error => {
        this.$message.error('Failed to save changes!');
        console.error('Save error:', error);
        // 发生错误时也刷新页面
        this.getInfo(this.order.id);
      });
    },
    getStatusText(status) {
      // Can return text based on status
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
      // Assuming there's an updateOrder interface
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
          // 重新获取订单详情以更新payment状态
          this.getInfo(this.order.id);
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
          // 重新获取订单详情
          this.getInfo(this.order.id);
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
          status: 3,
          shippingMethod: this.upgradeShippingMethod || this.shipping.shippingMethod
        }
      }).then(res => {
        if (res.code === 200) {
          this.$message.success('Order marked as shipped!');
          this.shippedFlag = true;
          this.order.status = 3;
          // 更新本地shipping信息
          this.shipping.shippingMethod = this.upgradeShippingMethod || this.shipping.shippingMethod;
          // 重新获取订单详情
          this.getInfo(this.order.id);
        } else {
          this.$message.error('Failed to update order status!');
        }
      });

      this.saveChanges();
    },

    calculateSubtotal() {
      return this.items.reduce((sum, item) => {
        return sum + (item.price * item.quantity);
      }, 0).toFixed(2);
    },
    
    getCurrencyUnit() {
      // 根据订单的货币类型返回正确的单位
      if (this.order.currency) {
        return this.order.currency;
      }
      // 如果没有货币信息，检查支付信息
      if (this.payment && this.payment.currency) {
        return this.payment.currency;
      }
      // 默认返回AUD
      return 'AUD';
    },
    
    // 根据会员等级ID获取等级名称
    getMemberLevelName(levelId) {
      const levelMap = {
        1: 'Bronze',
        2: 'Silver', 
        3: 'Gold',
        4: 'Platinum',
        5: 'Diamond'
      };
      return levelMap[levelId] || 'Unknown';
    },

    resolveDispute() {
      request({
        url: `/admin/mall/order/resolveDispute/${this.order.id}`,
        method: 'put'
      }).then(res => {
        if (res.code === 200) {
          this.$message.success('Dispute resolved!');
          this.order.isdispute = 0; // 0 means no dispute
                      this.getInfo(this.order.id); // Refresh order details
        } else {
          this.$message.error('Failed to resolve dispute!');
        }
      }).catch(error => {
        this.$message.error('Failed to resolve dispute!');
      });
    },

    viewDisputeDetails() {
      // 显示dispute详情对话框
      this.disputeDialogVisible = true;
    },

    formatDateTime(dateTime) {
      if (!dateTime) return '';
      const date = new Date(dateTime);
      return date.toLocaleString('en-US', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit'
      });
    },

    resolveDisputeFromDialog() {
      this.disputeDialogVisible = false;
      this.resolveDispute();
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
.order-meta {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.meta-row {
  display: flex;
  align-items: center;
  gap: 8px;
}
.label {
  min-width: 120px;
  color: #606266;
}
.value {
  color: #333;
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

.summary-row.member-benefit {
  color: #67C23A;
  font-weight: 600;
  font-style: italic;
}

.summary-row.member-benefit span:first-child {
  position: relative;
}

.summary-row.member-benefit span:first-child::before {
  content: "👑 ";
  margin-right: 4px;
}

/* Dispute Dialog Styles */
.dispute-details {
  padding: 0;
}

.dispute-section {
  margin-bottom: 24px;
}

.dispute-section h4 {
  margin: 0 0 12px 0;
  color: #333;
  font-size: 16px;
  font-weight: 600;
  border-bottom: 1px solid #e4e7ed;
  padding-bottom: 8px;
}

.dispute-row {
  display: flex;
  align-items: flex-start;
  margin-bottom: 12px;
  line-height: 1.5;
}

.dispute-label {
  min-width: 120px;
  font-weight: 600;
  color: #606266;
  flex-shrink: 0;
}

.dispute-value {
  color: #333;
  flex: 1;
}

.dispute-reason {
  color: #333;
  flex: 1;
  background: #f8f9fa;
  padding: 8px 12px;
  border-radius: 4px;
  border-left: 3px solid #f56c6c;
  white-space: pre-wrap;
  word-break: break-word;
}

.dialog-footer {
  text-align: right;
}

/* 确保输入框可编辑 */
.el-form-item .el-input,
.el-form-item .el-textarea__inner {
  pointer-events: auto !important;
  cursor: text !important;
}

.el-form-item .el-input.is-disabled .el-input__inner,
.el-form-item .el-textarea.is-disabled .el-textarea__inner {
  pointer-events: none;
  cursor: not-allowed;
}
</style> 
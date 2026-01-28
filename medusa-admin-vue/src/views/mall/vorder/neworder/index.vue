<template>
  <div class="app-container">
    <el-card class="box-card">
      <div slot="header" class="clearfix">
        <span class="page-title">New Order</span>
        <el-button style="float: right; padding: 3px 0" type="text" @click="$router.back()">Back</el-button>
      </div>
      
      <div v-for="(order, index) in orders" :key="index" class="order-form-container">
        <div class="order-header">
          <h3>Order #{{ index + 1 }}</h3>
          <el-button v-if="index > 0" type="danger" icon="el-icon-delete" circle @click="removeOrder(index)"></el-button>
        </div>
        
        <!-- Order Summary -->
        <div v-if="order.coinAmount && order.coinType" class="order-summary">
          <div v-if="order.proCode && order.amount" class="summary-item">
            <span class="summary-label">SKU:</span>
            <span class="summary-value">{{ generateSKU(order.proCode, order.amount) }}</span>
          </div>
          <div class="summary-item">
            <span class="summary-label">Total Amount:</span>
            <span class="summary-value">{{ order.coinAmount }} {{ order.coinType }}</span>
          </div>
          <div v-if="order.fullAddress" class="summary-item">
            <span class="summary-label">Shipping To:</span>
            <span class="summary-value">{{ formatAddress(order.fullAddress) }}</span>
          </div>
        </div>
        
        <el-form ref="elForm" :model="order" :rules="rules" label-width="120px" class="order-form" @input="onFormInput">
          <el-row>
            <el-col :span="24">
              <el-form-item label="ProCode" prop="proCode">
                <el-input v-model="order.proCode" placeholder="Please enter ProCode" clearable></el-input>
                <div class="form-tip">📦 Product code identifier (e.g., PRO001, PRO002)</div>
              </el-form-item>
            </el-col>
          </el-row>

          <el-row>
            <el-col :span="24">
              <el-form-item label="Amount" prop="amount">
                <el-input-number 
                  v-model="order.amount" 
                  :min="0.01" 
                  :max="999999" 
                  :precision="2"
                  :step="0.01"
                  placeholder="Please enter Amount" 
                  style="width: 100%;">
                </el-input-number>
                <div class="form-tip">💰 Product price amount (supports up to 2 decimals, e.g., 0.25, 100.50)</div>
              </el-form-item>
            </el-col>
          </el-row>

          <el-row>
            <el-col :span="24">
              <el-form-item label="Quantity" prop="quantity">
                <el-input-number 
                  v-model="order.quantity" 
                  :min="1" 
                  :max="999" 
                  :precision="0"
                  placeholder="Please enter Quantity" 
                  style="width: 100%;">
                </el-input-number>
                <div class="form-tip">🔢 Number of items to order (integer only, e.g., 1, 2, 5)</div>
              </el-form-item>
            </el-col>
          </el-row>

          <el-row>
            <el-col :span="24">
              <el-form-item label="Coin Amount" prop="coinAmount">
                <el-input v-model="order.coinAmount" placeholder="Please enter Coin Amount" clearable></el-input>
                <div class="form-tip">💎 Total cryptocurrency amount for this item (e.g., 0.001 BTC, 100 USDT)</div>
              </el-form-item>
            </el-col>
          </el-row>

          <el-row>
            <el-col :span="24">
              <el-form-item label="Coin Type" prop="coinType">
                <el-select v-model="order.coinType" placeholder="Please select Coin Type" clearable>
                  <el-option label="BTC (Bitcoin)" value="BTC"></el-option>
                  <el-option label="USDT (Tether TRC20)" value="USDT"></el-option>
                  <el-option label="XMR (Monero)" value="XMR"></el-option>
                  <el-option label="LTC (Litecoin)" value="LTC"></el-option>
                </el-select>
                <div class="form-tip">💡 Supported cryptocurrencies: BTC, USDT, XMR, LTC</div>
              </el-form-item>
            </el-col>
          </el-row>

          <el-row>
            <el-col :span="24">
              <el-form-item label="Username" prop="username">
                <el-input v-model="order.username" placeholder="Please enter Username" clearable></el-input>
                <div class="form-tip">👤 Customer username or identifier</div>
              </el-form-item>
            </el-col>
          </el-row>

          <el-row>
            <el-col :span="24">
              <el-form-item label="Order Channel" prop="orderChannel">
                <el-select v-model="order.orderChannel" placeholder="Please select Order Channel" clearable @change="handleChannelChange(index)">
                  <el-option label="OS" value="OS"></el-option>
                  <el-option label="Telegram" value="TG"></el-option>
                  <el-option label="Market" value="Market"></el-option>
                  <el-option label="Other" value="Other"></el-option>
                </el-select>
                <div class="form-tip">📱 Order source channel</div>
              </el-form-item>
            </el-col>
          </el-row>

          <!-- Custom Order Channel Input (shown when "Other" is selected) -->
          <el-row v-if="order.orderChannel === 'Other'">
            <el-col :span="24">
              <el-form-item label="Custom Channel" prop="customChannel">
                <el-input 
                  v-model="order.customChannel" 
                  placeholder="Please enter custom channel name" 
                  clearable
                ></el-input>
                <div class="form-tip">✏️ Specify the custom order channel name</div>
              </el-form-item>
            </el-col>
          </el-row>

          <el-row>
            <el-col :span="24">
              <el-form-item label="Full Name & Address" prop="fullAddress">
                <el-input
                  v-model="order.fullAddress"
                  type="textarea"
                  placeholder="Please DIVIDE address details with COMMAS (,)"
                  :autosize="{ minRows: 6, maxRows: 10 }"
                ></el-input>
                <div class="form-tip">
                  <strong>📍 Please DIVIDE address details with COMMAS (,)</strong><br>
                  <br>
                  <strong>Basic Format (5 fields):</strong><br>
                  Full name, Address1, City, State, Postcode<br>
                  Example: John Lennon, 67 Penny Lane, Liverpool, NSW, 2170<br>
                  <br>
                  <strong>Extended Format (6 fields):</strong><br>
                  Full name, Address1, Address2, City, State, Postcode<br>
                  Example: John Lennon, Parcel Locker 12345, 67 Penny Lane, Liverpool, NSW, 2170<br>
                  <br>
                  <strong>Full Format (7 fields):</strong><br>
                  Full name, Address1, Address2, Address3, City, State, Postcode<br>
                  Example: John Lennon, 59 Pitt St, Shopping Centre, Parcel Locker 12345, Sydney, NSW, 2000
                </div>
                <div v-if="order.fullAddress && !validateAddress(order.fullAddress)" class="form-tip form-tip-warning">
                  ⚠️ Address format incomplete. Please use comma-separated format with 5, 6, or 7 fields.
                </div>
              </el-form-item>
            </el-col>
          </el-row>
        
          <el-row>
            <el-col :span="24">
              <el-form-item label="Postage" prop="postage">
                <el-select v-model="order.postage" placeholder="Please select Postage Method" clearable>
                  <el-option label="Regular Post" value="RP"></el-option>
                  <el-option label="Express Post" value="EP"></el-option>
                  <el-option label="Regular Stealth Post" value="RSP"></el-option>
                  <el-option label="Express Stealth Post" value="ESP"></el-option>
                  <el-option label="Super Stealth Post (Recommended)" value="SSP"></el-option>
                </el-select>
                <div class="form-tip">🚚 Shipping methods: Regular Post, Express Post, Stealth Post options</div>
              </el-form-item>
            </el-col>
          </el-row>
       <el-row>
        <el-col :span="24" class="form-buttons">
          <el-form-item>
            <el-button type="info" @click="fillExampleData">Fill Example</el-button>
            <el-button type="success" @click="addMoreOrders">Add More Orders</el-button>
            <el-button type="primary" @click="saveOrder">Save Order</el-button>
            <el-button @click="resetForm">Cancel Order</el-button>
          </el-form-item>
        </el-col>
    </el-row>
      
        </el-form>
      </div>
    </el-card>
  </div>
</template>

<script>
import { addVOrder } from "@/api/mall/vorder";
import request from "@/utils/request";

export default {
  name: "NewOrder",
  data() {
    return {
      orders: [{
        proCode: undefined,
        amount: 1,
        quantity: 1,
        coinAmount: undefined,
        coinType: undefined,
        username: undefined,
        orderChannel: undefined,
        customChannel: undefined,
        fullAddress: undefined,
        postage: undefined
      }],
      rules: {
        proCode: [{
          required: true,
          message: 'Please enter ProCode',
          trigger: 'blur'
        }],
        amount: [{
          required: true,
          message: 'Please enter Amount',
          trigger: 'blur'
        }],
        quantity: [{
          required: true,
          message: 'Please enter Quantity',
          trigger: 'blur'
        }],
        coinAmount: [{
          required: true,
          message: 'Please enter Coin Amount',
          trigger: 'blur'
        }],
        coinType: [{
          required: true,
          message: 'Please enter Coin Type',
          trigger: 'blur'
        }],
        username: [{
          required: true,
          message: 'Please enter Username',
          trigger: 'blur'
        }],
        orderChannel: [{
          required: true,
          message: 'Please select Order Channel',
          trigger: 'change'
        }],
        customChannel: [{
          required: false,
          message: 'Please enter Custom Channel name',
          trigger: 'blur'
        }],
        fullAddress: [{
          required: true,
          message: 'Please enter Full Name & Address',
          trigger: 'blur'
        }],
        postage: [{
          required: true,
          message: 'Please enter Postage',
          trigger: 'blur'
        }]
      }
    }
  },
  methods: {

    
    // 验证地址格式 - 支持5、6、7个字段
    validateAddress(address) {
      if (!address) return false;
      const parts = address.split(',').map(p => p.trim()).filter(p => p.length > 0);
      // 支持5、6、7个字段的格式
      return parts.length === 5 || parts.length === 6 || parts.length === 7;
    },
    
    // 格式化地址显示
    formatAddress(address) {
      if (!address) return '';
      const parts = address.split(',').map(p => p.trim()).filter(p => p.length > 0);
      if (parts.length === 5) {
        // Basic Format: name, address1, city, state, postcode
        return `${parts[0]} - ${parts[2]}, ${parts[3]}`;
      } else if (parts.length === 6) {
        // Extended Format: name, address1, address2, city, state, postcode
        return `${parts[0]} - ${parts[3]}, ${parts[4]}`;
      } else if (parts.length === 7) {
        // Full Format: name, address1, address2, address3, city, state, postcode
        return `${parts[0]} - ${parts[4]}, ${parts[5]}`;
      }
      return address;
    },
    
    // 处理表单输入事件
    onFormInput() {
      // 表单输入事件处理
    },
    
    // 处理 Order Channel 变化
    handleChannelChange(index) {
      // 当切换到非 "Other" 选项时，清空 customChannel
      if (this.orders[index].orderChannel !== 'Other') {
        this.orders[index].customChannel = undefined;
      }
    },
    
    // 生成SKU
    generateSKU(proCode, amount) {
      if (!proCode || !amount) return '';
      return `SKU-${proCode}-${amount}`;
    },
    
    formatOrderData() {
      // Parse address information - 支持5、6、7个字段的格式
      const addressParts = this.orders[0].fullAddress.split(',').map(p => p.trim()).filter(p => p.length > 0);
      let shippingAddress = {};
      
      if (addressParts.length === 5) {
        // Basic Format (5 fields): Full name, Address1, City, State, Postcode
        shippingAddress = {
          name: addressParts[0] || '',
          address1: addressParts[1] || '',
          address2: '',
          address3: '',
          city: addressParts[2] || '',
          state: addressParts[3] || '',
          postalCode: addressParts[4] || '',
          country: 'Australia'
        };
      } else if (addressParts.length === 6) {
        // Extended Format (6 fields): Full name, Address1, Address2, City, State, Postcode
        shippingAddress = {
          name: addressParts[0] || '',
          address1: addressParts[1] || '',
          address2: addressParts[2] || '',
          address3: '',
          city: addressParts[3] || '',
          state: addressParts[4] || '',
          postalCode: addressParts[5] || '',
          country: 'Australia'
        };
      } else if (addressParts.length === 7) {
        // Full Format (7 fields): Full name, Address1, Address2, Address3, City, State, Postcode
        shippingAddress = {
          name: addressParts[0] || '',
          address1: addressParts[1] || '',
          address2: addressParts[2] || '',
          address3: addressParts[3] || '',
          city: addressParts[4] || '',
          state: addressParts[5] || '',
          postalCode: addressParts[6] || '',
          country: 'Australia'
        };
      } else {
        // 默认格式（向后兼容）
        shippingAddress = {
          name: addressParts[0] || '',
          address1: addressParts[1] || '',
          address2: addressParts[2] || '',
          address3: addressParts[3] || '',
          city: addressParts[4] || '',
          state: addressParts[5] || '',
          postalCode: addressParts[6] || '',
          country: addressParts[7] || 'Australia'
        };
      }

      // Build order items, concatenate SKU
      const items = this.orders.map(order => ({
        sku: `SKU-${order.proCode}-${order.amount}`, // Concatenate SKU
        quantity: parseInt(order.quantity) || 0,
        totalPrice: parseFloat(order.coinAmount) || 0,
        currency: order.coinType
      }));

      // Calculate total amount
      const total = items.reduce((sum, item) => sum + item.totalPrice, 0);

      // Set sourceType based on Order Channel
      let sourceType = "11"; // Default value (TG)
      let customChannelName = null;  // 存储自定义渠道名称
      
      if (this.orders[0].orderChannel === "OS") {
        sourceType = "10";
      } else if (this.orders[0].orderChannel === "TG") {
        sourceType = "11";
      } else if (this.orders[0].orderChannel === "Market") {
        sourceType = "12"; // Market channel
      } else if (this.orders[0].orderChannel === "Other" && this.orders[0].customChannel) {
        sourceType = "99"; // Other/Custom channel
        customChannelName = this.orders[0].customChannel; // 保存自定义渠道名称
      }

      // Map coinType to payType (backend expects numeric strings)
      const coinTypeToPayType = {
        'BTC': '0',
        'USDT': '1',
        'XMR': '2',
        'ETH': '3',
        'LTC': '4'
      };
      const payType = coinTypeToPayType[this.orders[0].coinType] || '0'; // Default to BTC if not found

      // Build final order data
      const orderData = {
        orderId: `order_${Date.now()}`, // Generate order ID
        userId: this.orders[0].username,
        userName: this.orders[0].username,
        items: items,
        total: total,
        currency: this.orders[0].coinType,
        shippingAddress: shippingAddress,
        status: "0", // Use string "0" to represent initial status
        createdAt: new Date().toISOString(),
        shippingMethod: this.orders[0].postage,
        sourceType: sourceType,  // Set source type
        payType: payType
      };

      // 如果是自定义渠道，将渠道名称添加到 remark
      if (customChannelName) {
        orderData.remark = `Custom Channel: ${customChannelName}`;
      }

      return orderData;
    },
    saveOrder() {
      this.$refs.elForm[0].validate(valid => {
        if (!valid) return;
        
        // Additional validation: if Order Channel is "Other", customChannel must be filled
        if (this.orders[0].orderChannel === 'Other' && !this.orders[0].customChannel) {
          this.$modal.msgError("Please enter Custom Channel name when 'Other' is selected");
          return;
        }
        
        const orderData = this.formatOrderData();
        
        // Call create order API
        request({
          url: '/admin/mall/order/add',
          method: 'post',
          data: orderData
        }).then(response => {
          if (response.code === 200) {
            this.$modal.msgSuccess("Order created successfully");
            this.$router.push('/mall/vorder');
          } else {
            this.$modal.msgError(response.msg || "Failed to create order");
          }
        }).catch(error => {
          this.$modal.msgError(error.message || "Failed to create order");
        });
      });
    },
    addMoreOrders() {
      this.$refs.elForm[0].validate(valid => {
        if (!valid) return;
        
        const lastOrder = this.orders[this.orders.length - 1];
        this.orders.push({
          proCode: undefined,
          amount: 1,
          quantity: 1,
          coinAmount: undefined,
          coinType: lastOrder.coinType,
          username: lastOrder.username,
          orderChannel: lastOrder.orderChannel,
          customChannel: lastOrder.customChannel,
          fullAddress: lastOrder.fullAddress,
          postage: lastOrder.postage
        });
        
        this.$modal.msgSuccess("Order saved, please add next order");
      });
    },
    removeOrder(index) {
      this.orders.splice(index, 1);
    },
    resetForm() {
      this.orders = [{
        proCode: undefined,
        amount: 1,
        quantity: 1,
        coinAmount: undefined,
        coinType: undefined,
        username: undefined,
        orderChannel: undefined,
        customChannel: undefined,
        fullAddress: undefined,
        postage: undefined
      }];
      this.$refs.elForm[0].resetFields();
    },
         fillExampleData() {
       this.orders = [{
         proCode: "PRO001",
         amount: 100,
         quantity: 1,
         coinAmount: "100.00",
         coinType: "USDT",
         username: "testuser123",
         orderChannel: "OS",
         fullAddress: "John Lennon, 67 Penny Lane, Liverpool, NSW, 2170",
         postage: "RP"
       }];
       // Example data filled
       this.$refs.elForm[0].resetFields(); // Reset form validation state
       this.$modal.msgSuccess("Example data filled successfully!");
     }
  }
}
</script>

<style scoped>
.app-container {
  padding: 20px;
}

.box-card {
  margin-bottom: 20px;
}

.page-title {
  font-size: 18px;
  font-weight: bold;
  color: #303133;
}

.order-form-container {
  margin-bottom: 30px;
  padding: 20px;
  border: 1px solid #EBEEF5;
  border-radius: 4px;
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.order-header h3 {
  margin: 0;
  color: #303133;
}

.order-form {
  margin-top: 20px;
}

.el-form-item {
  margin-bottom: 22px;
}

.form-buttons {
  text-align: center;
  margin-top: 20px;
  padding: 20px 0;
  border-top: 1px solid #EBEEF5;
}

.form-buttons .el-form-item {
  margin-bottom: 0;
}

.form-buttons .el-button {
  margin: 0 10px;
  min-width: 120px;
}

/* 响应式调整 */
@media screen and (max-width: 768px) {
  .el-form-item {
    margin-bottom: 18px;
  }
  
  .el-form-item__label {
    float: none;
    display: block;
    text-align: left;
    padding: 0 0 8px;
  }
  
  .el-form-item__content {
    margin-left: 0 !important;
  }
}

/* 美化输入框 */
.el-input__inner {
  border-radius: 4px;
}

.el-input__inner:focus {
  border-color: #409EFF;
  box-shadow: 0 0 0 2px rgba(64,158,255,.2);
}

/* 美化文本域 */
.el-textarea__inner {
  border-radius: 4px;
  resize: vertical;
}

/* 美化按钮 */
.el-button {
  padding: 10px 20px;
  border-radius: 4px;
  transition: all 0.3s;
  font-size: 14px;
  font-weight: 500;
}

.el-button--primary {
  background-color: #409EFF;
  border-color: #409EFF;
}

.el-button--primary:hover {
  background-color: #66b1ff;
  border-color: #66b1ff;
}

.el-button--success {
  background-color: #67C23A;
  border-color: #67C23A;
}

.el-button--success:hover {
  background-color: #85ce61;
  border-color: #85ce61;
}

.el-button--default {
  background-color: #fff;
  border-color: #dcdfe6;
}

.el-button--default:hover {
  color: #409EFF;
  border-color: #c6e2ff;
  background-color: #ecf5ff;
}

/* 表单提示信息样式 */
.form-tip {
  margin-top: 5px;
  font-size: 12px;
  color: #909399;
  line-height: 1.4;
  padding: 4px 8px;
  background-color: #f8f9fa;
  border-radius: 4px;
  border-left: 3px solid #409EFF;
}

.form-tip:hover {
  background-color: #ecf5ff;
  color: #606266;
}

/* 美化选择框 */
.el-select {
  width: 100%;
}

.el-select .el-input__inner {
  border-radius: 4px;
}

.el-select .el-input__inner:focus {
  border-color: #409EFF;
  box-shadow: 0 0 0 2px rgba(64,158,255,.2);
}

/* 美化选项 */
.el-select-dropdown__item {
  padding: 8px 12px;
  font-size: 14px;
}

.el-select-dropdown__item:hover {
  background-color: #f5f7fa;
}

.el-select-dropdown__item.selected {
  background-color: #409EFF;
  color: #fff;
}

/* 表单验证提示 */
.el-form-item.is-error .form-tip {
  border-left-color: #f56c6c;
  background-color: #fef0f0;
  color: #f56c6c;
}

/* 警告提示样式 */
.form-tip-warning {
  border-left-color: #e6a23c !important;
  background-color: #fdf6ec !important;
  color: #e6a23c !important;
}

.form-tip-warning:hover {
  background-color: #fef0e6 !important;
  color: #d48806 !important;
}

/* 响应式提示信息 */
@media screen and (max-width: 768px) {
  .form-tip {
    font-size: 11px;
    padding: 3px 6px;
  }
}

/* 数字输入框样式 */
.el-input-number {
  width: 100%;
}

.el-input-number .el-input__inner {
  border-radius: 4px;
  text-align: left;
}

.el-input-number .el-input__inner:focus {
  border-color: #409EFF;
  box-shadow: 0 0 0 2px rgba(64,158,255,.2);
}

.el-input-number .el-input-number__decrease,
.el-input-number .el-input-number__increase {
  border-radius: 4px;
  background-color: #f5f7fa;
  border-color: #dcdfe6;
}

.el-input-number .el-input-number__decrease:hover,
.el-input-number .el-input-number__increase:hover {
  background-color: #ecf5ff;
  border-color: #409EFF;
  color: #409EFF;
}

/* 订单总览样式 */
.order-summary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 15px;
  border-radius: 8px;
  margin-bottom: 20px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

.summary-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.summary-item:last-child {
  margin-bottom: 0;
}

.summary-label {
  font-weight: 600;
  font-size: 14px;
}

.summary-value {
  font-size: 14px;
  font-weight: 500;
  background: rgba(255, 255, 255, 0.2);
  padding: 4px 8px;
  border-radius: 4px;
  backdrop-filter: blur(10px);
}
</style>
  
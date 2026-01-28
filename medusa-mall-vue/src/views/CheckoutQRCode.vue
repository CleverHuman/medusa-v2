<template>
  <div class="checkout-container">
    <h1 class="checkout-title">Payment</h1>

    <div class="qrcode-container">
      <div class="qrcode-box">
        <div class="payment-method">
          <img :src="paymentIcon" :alt="paymentMethod" class="method-icon">
          <span class="method-name">{{ paymentMethodName }}</span>
        </div>

        <div class="qrcode-image">
          <!-- 这里显示动态生成的二维码 -->
          <img :src="qrcodeImage" alt="Payment QR Code" v-if="qrcodeImage">
          <div class="qrcode-placeholder" v-else>
            <i class="el-icon-loading"></i>
            <p>Generating QR Code...</p>
          </div>
        </div>

        <div class="amount-info">
          <p>Total Amount: <span class="amount">¥{{ totalAmount.toFixed(2) }}</span></p>
        </div>
      </div>

      <div class="instructions">
        <h3>Payment Instructions:</h3>
        <ol>
          <li>Open {{ paymentMethodName }} app on your phone</li>
          <li>Tap "Scan QR Code"</li>
          <li>Scan the QR code above to complete payment</li>
        </ol>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'CheckoutQRCode',
  data() {
    return {
      qrcodeImage: null,
      totalAmount: 0,
      paymentMethod: '',
      paymentMethodName: '',
      paymentIcon: ''
    }
  },
  created() {
    // 从store获取订单信息
    const order = this.$store.state.order.currentOrder
    this.totalAmount = order.totalAmount
    this.paymentMethod = order.paymentMethod

    // 设置支付方法显示信息
    this.setPaymentMethodInfo()

    // 模拟生成二维码
    this.generateQRCode()
  },
  methods: {
    setPaymentMethodInfo() {
      const methods = {
        // wechat: { name: 'WeChat Pay', icon: require('@/assets/wechat-pay.png') },
        // alipay: { name: 'Alipay', icon: require('@/assets/alipay.png') },
        // credit: { name: 'Credit Card', icon: require('@/assets/credit-card.png') }
      }

      this.paymentMethodName = methods[this.paymentMethod].name
      this.paymentIcon = methods[this.paymentMethod].icon
    },
    generateQRCode() {
      // 模拟API调用生成二维码
      setTimeout(() => {
        // 实际项目中这里应该调用后端API生成真正的支付二维码
        // this.qrcodeImage = require('@/assets/qrcode-placeholder.png')
      }, 1500)
    }
  }
}
</script>

<style scoped>
.qrcode-container {
  display: flex;
  justify-content: center;
  gap: 50px;
  margin-top: 30px;
}

.qrcode-box {
  width: 300px;
  padding: 20px;
  border: 1px solid #e6e6e6;
  border-radius: 8px;
  text-align: center;
}

.payment-method {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  margin-bottom: 20px;
}

.method-icon {
  width: 30px;
  height: 30px;
}

.method-name {
  font-size: 18px;
  font-weight: bold;
}

.qrcode-image {
  width: 250px;
  height: 250px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px dashed #dcdfe6;
}

.qrcode-image img {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.qrcode-placeholder {
  color: #909399;
}

.amount-info {
  margin-top: 20px;
}

.amount {
  font-size: 20px;
  font-weight: bold;
  color: #f56c6c;
}

.instructions {
  max-width: 400px;
}

.instructions ol {
  padding-left: 20px;
  line-height: 1.8;
}
</style>

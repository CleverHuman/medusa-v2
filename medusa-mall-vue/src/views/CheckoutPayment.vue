<template>
  <div class="checkout-container">
    <h1 class="checkout-title">Select Payment Method</h1>

    <div class="payment-methods">
      <el-radio-group v-model="paymentMethod" class="payment-options">
        <el-radio label="Bitcoin(BTC)" class="payment-option">
          <div class="payment-content">
<!--            <img src="@/assets/wechat-pay.png" alt="WeChat Pay" class="payment-icon">-->
            <span>Bitcoin(BTC)</span>
          </div>
        </el-radio>

        <el-radio label="USDT" class="payment-option">
          <div class="payment-content">
<!--            <img src="@/assets/alipay.png" alt="Alipay" class="payment-icon">-->
            <span>Tether(USDT TRC20)</span>
          </div>
        </el-radio>

        <el-radio label="XMR" class="payment-option">
          <div class="payment-content">
<!--            <img src="@/assets/credit-card.png" alt="Credit Card" class="payment-icon">-->
            <span>XMR</span>
          </div>
        </el-radio>
      </el-radio-group>
    </div>

    <div class="submit-section">
      <el-button type="primary" @click="submitPayment" :disabled="!paymentMethod">Submit</el-button>
    </div>
  </div>
</template>

<script>
export default {
  name: 'CheckoutPayment',
  data() {
    return {
      paymentMethod: ''
    }
  },
  methods: {
    submitPayment() {
      // 保存支付方式并跳转到支付二维码页面
      this.$store.dispatch('order/savePaymentMethod', this.paymentMethod)
      this.$router.push('/checkout/qrcode')
    }
  }
}
</script>

<style scoped>
.payment-methods {
  margin: 30px 0;
}

.payment-options {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.payment-option {
  display: flex;
  align-items: center;
  padding: 15px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  transition: all 0.3s;
}

.payment-option:hover {
  border-color: #c0c4cc;
}

.payment-option.is-checked {
  border-color: #409EFF;
  background-color: #f5f7fa;
}

.payment-content {
  display: flex;
  align-items: center;
  gap: 10px;
}

.payment-icon {
  width: 30px;
  height: 30px;
}

.submit-section {
  text-align: center;
  margin-top: 30px;
}
</style>

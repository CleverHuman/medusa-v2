<template>
  <div class="checkout-container">
    <h1 class="checkout-title">Select Shipping Method</h1>

    <el-form ref="shippingForm" :model="shippingForm" label-width="120px">
      <el-form-item label="Shipping Method" prop="method">
        <el-select v-model="shippingForm.method" placeholder="Select shipping method">
          <el-option label="Regular Post" value="regular"></el-option>
          <el-option label="Express Delivery" value="express"></el-option>
          <el-option label="Overnight Shipping" value="overnight"></el-option>
        </el-select>
      </el-form-item>

      <el-form-item>
        <el-button type="primary" @click="submitShipping">Submit</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script>
export default {
  name: 'CheckoutShipping',
  data() {
    return {
      shippingForm: {
        method: 'regular'
      }
    }
  },
  methods: {
    submitShipping() {
      // 保存配送方式并跳转到支付方式页面
      this.$store.dispatch('order/saveShippingMethod', this.shippingForm.method)
      this.$router.push('/checkout/payment')
    }
  }
}
</script>

<style scoped>
/* 使用与地址页面相同的样式 */
</style>

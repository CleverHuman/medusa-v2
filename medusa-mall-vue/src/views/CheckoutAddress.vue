<template>
  <div class="checkout-container">
    <h1 class="checkout-title">Checkout</h1>

    <el-form ref="addressForm" :model="addressForm" :rules="addressRules" label-width="120px">
      <!-- 第一行: First Name + Last Name -->
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="First Name" prop="firstName">
            <el-input v-model="addressForm.firstName"></el-input>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="Last Name" prop="lastName">
            <el-input v-model="addressForm.lastName"></el-input>
          </el-form-item>
        </el-col>
      </el-row>

      <!-- 第二行: Address Line 1 -->
      <el-form-item label="Address Line 1" prop="address1">
        <el-input v-model="addressForm.address1"></el-input>
      </el-form-item>

      <!-- 第三行: Address Line 2 -->
      <el-form-item label="Address Line 2">
        <el-input v-model="addressForm.address2"></el-input>
      </el-form-item>

      <!-- 第四行: City + State + Post Code -->
      <el-row :gutter="20">
        <el-col :span="8">
          <el-form-item label="City" prop="city">
            <el-input v-model="addressForm.city"></el-input>
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="State" prop="state">
            <el-input v-model="addressForm.state"></el-input>
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="Post Code" prop="postCode">
            <el-input v-model="addressForm.postCode"></el-input>
          </el-form-item>
        </el-col>
      </el-row>

      <!-- 第五行: Submit按钮 -->
      <el-form-item>
        <el-button type="primary" @click="submitAddress">Submit</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script>
export default {
  name: 'CheckoutAddress',
  data() {
    return {
      addressForm: {
        firstName: '',
        lastName: '',
        address1: '',
        address2: '',
        city: '',
        state: '',
        postCode: ''
      },
      addressRules: {
        firstName: [{ required: true, message: 'Please input first name', trigger: 'blur' }],
        lastName: [{ required: true, message: 'Please input last name', trigger: 'blur' }],
        address1: [{ required: true, message: 'Please input address line 1', trigger: 'blur' }],
        city: [{ required: true, message: 'Please input city', trigger: 'blur' }],
        state: [{ required: true, message: 'Please input state', trigger: 'blur' }],
        postCode: [{ required: true, message: 'Please input post code', trigger: 'blur' }]
      }
    }
  },
  methods: {
    submitAddress() {
      this.$refs.addressForm.validate(valid => {
        if (valid) {
          // 保存地址信息并跳转到配送方式页面
          this.$store.dispatch('order/saveShippingAddress', this.addressForm)
          this.$router.push('/checkout/shipping')
        }
      })
    }
  }
}
</script>

<style scoped>
.checkout-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.checkout-title {
  color: #fff;
  background-color: #333;
  padding: 15px;
  text-align: center;
  margin-bottom: 30px;
}
</style>

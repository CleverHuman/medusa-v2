<template>
  <div class="signup-container">
    <div class="signup-box">
      <h2>Sign Up</h2>
      <form @submit.prevent="handleSignup">
        <input
          type="text"
          placeholder="Username"
          v-model="form.username"
          required
        >
        <input
          type="text"
          placeholder="Primary Contact(Telegram or Email)"
          v-model="form.primaryContact"
          required
        >
        <input
          type="text"
          placeholder="Secondary Contact"
          v-model="form.secondaryContact"
        >
        <input
          type="password"
          placeholder="Password"
          v-model="form.password"
          required
          minlength="6"
        >
        <input
          type="password"
          placeholder="Confirm Password"
          v-model="form.confirmPassword"
          required
          @blur="validatePassword"
        >
        <div v-if="passwordError" class="error-message">
          {{ passwordError }}
        </div>

        <button type="submit" :disabled="isSubmitting">
          {{ isSubmitting ? 'Processing...' : 'SIGN UP' }}
        </button>

        <div class="login-link">
          <span>Already have an account? </span>
          <router-link to="/login">Log In</router-link>
        </div>
      </form>
    </div>
  </div>
</template>

<script>
import { register } from "@/api/login";

export default {
  data() {
    return {
      form: {
        username: '',
        primaryContact: '',
        secondaryContact: '',
        password: '',
        confirmPassword: '',
        sourceType: 0
      },
      passwordError: '',
      isSubmitting: false
    }
  },
  methods: {
    validatePassword() {
      if (this.form.password !== this.form.confirmPassword) {
        this.passwordError = 'Passwords do not match';
        return false;
      }
      if (this.form.password.length < 6) {
        this.passwordError = 'Password must be at least 6 characters';
        return false;
      }
      this.passwordError = '';
      return true;
    },
    handleSignup() {
      // 验证密码
      if (!this.validatePassword()) {
        return;
      }

      this.isSubmitting = true;

      // 准备提交数据
      const data = {
        username: this.form.username,
        password: this.form.password,
        primaryContact: this.form.primaryContact,
        secondaryContact: this.form.secondaryContact,
        sourceType: this.form.sourceType
      };

      register(data)
        .then(response => {
          this.$message.success('Registration successful!');
          // 注册成功后跳转到登录页面
          this.$router.push('/login');
        })
        .catch(error => {
          console.error('Error registering:', error);
          let errorMsg = 'Failed to register';
          if (error.response && error.response.data) {
            errorMsg = error.response.data.message || errorMsg;
          }
          this.$message.error(errorMsg);
        })
        .finally(() => {
          this.isSubmitting = false;
        });
    }
  }
}
</script>

<style scoped>
.signup-container {
  background: #1a1a1a;
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
}

.signup-box {
  background: #2d2d2d;
  padding: 2rem 3rem;
  border-radius: 8px;
  width: 400px;
}

h2 {
  display: flex;
  justify-content: center;
  color: white;
  margin-bottom: 1.5rem;
}

input {
  width: 100%;
  padding: 12px;
  margin: 0.5rem 0;
  background: #FFFFFF;
  border: 1px solid #444;
  color: #000000;
  border-radius: 4px;
}

button {
  width: 100%;
  padding: 12px;
  background: #504F4F;
  border: none;
  color: white;
  cursor: pointer;
  border-radius: 4px;
  margin-top: 1rem;
  transition: background 0.3s;
}

button:hover {
  background: #636262;
}

button:disabled {
  background: #3a3a3a;
  cursor: not-allowed;
}

.login-link {
  margin-top: 1.5rem;
  text-align: center;
  color: #888;
}

a {
  color: #00a8ff;
  text-decoration: none;
}

.error-message {
  color: #ff4d4f;
  font-size: 0.8rem;
  margin-top: -0.5rem;
  margin-bottom: 0.5rem;
}
</style>

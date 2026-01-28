<template>
  <div class="login">
    <el-form ref="loginForm" :model="loginForm" class="login-form">
      <h3 class="title">Sign in</h3>

      <!-- 用户名部分 -->
      <div class="form-row">
        <label class="input-label">Username</label>
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            type="text"
            auto-complete="off"
            placeholder="Enter username"
            class="full-width-input"
          >
<!--            <svg-icon slot="prefix" icon-class="user" class="el-input__icon input-icon" />-->
          </el-input>
        </el-form-item>
      </div>

      <!-- 密码部分 -->
      <div class="form-row">
        <label class="input-label">Password</label>
        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            auto-complete="off"
            placeholder="Enter password"
            class="full-width-input"
            @keyup.enter.native="handleLogin"
          >
<!--            <svg-icon slot="prefix" icon-class="password" class="el-input__icon input-icon" />-->
          </el-input>
        </el-form-item>
      </div>

      <!-- 操作按钮 -->
      <el-form-item style="width:100%;margin-top:30px;">
        <el-button
          :loading="loading"
          size="medium"
          type="primary"
          style="width:100%;"
          @click.native.prevent="handleLogin"
        >
          <span v-if="!loading">Sign in</span>
          <span v-else>Sign in...</span>
        </el-button>
        <div class="signup-link">
          <span>Don’t have an account? </span>
          <router-link class="link-type" :to="'/signup'">Sign up</router-link>
        </div>
      </el-form-item>
    </el-form>
  </div>
</template>

<script>
import Cookies from "js-cookie";

export default {
  name: "Login",
  data() {
    return {
      loginForm: {
        username: "",
        password: "",
        rememberMe: false,
      },
      loading: false,
      captchaEnabled: false,
    };
  },
  created() {
    this.getCookie();
  },
  methods: {
    getCookie() {
      const username = Cookies.get("username");
      const password = Cookies.get("password");
      const rememberMe = Cookies.get('rememberMe');
      this.loginForm = {
        username: username === undefined ? this.loginForm.username : username,
        password: password === undefined ? this.loginForm.password : decrypt(password),
        rememberMe: rememberMe === undefined ? false : Boolean(rememberMe),
      };
    },
    handleLogin() {
      this.$refs.loginForm.validate((valid) => {
        if (valid) {
          this.loading = true;
          if (this.loginForm.rememberMe) {
            Cookies.set("username", this.loginForm.username, { expires: 30 });
            Cookies.set("password", encrypt(this.loginForm.password), { expires: 30 });
            Cookies.set('rememberMe', this.loginForm.rememberMe, { expires: 30 });
          } else {
            Cookies.remove("username");
            Cookies.remove("password");
            Cookies.remove('rememberMe');
          }
          this.$store.dispatch("Login", this.loginForm).then(() => {
            this.$router.push({ path: this.redirect || "/" }).catch(() => {});
          }).catch(() => {
            this.loading = false;
            if (this.captchaEnabled) {
              this.getCode();
            }
          });
        }
      });
    },
  },
};
</script>

<style scoped>
.login {
  background: #1a1a1a;
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
}

.login-form {
  background: #2d2d2d;
  padding: 2rem 3rem;
  border-radius: 8px;
  width: 400px;

  .el-input__inner {
    background-color: #ffffff !important;
    border: 1px solid #dcdfe6;
    border-radius: 4px;
    color: #000000;
    width: 100% !important; /* 确保宽度占满 */

    &:focus {
      border-color: #409eff;
    }
  }
}

.form-row {
  margin-bottom: 20px;
}

.input-label {
  display: block;
  color: #ffffff;
  font-size: 14px;
  margin-bottom: 8px;
}

/* 输入框样式 */
.full-width-input {
  width: 100%;
}


.signup-link {
  margin-top: 15px;
  text-align: center;
  color: #888;

  a {
    color: #00a8ff;
    text-decoration: none;

    &:hover {
      text-decoration: underline;
    }
  }
}

.title {
  color: #ffffff;
  text-align: center;
  margin-bottom: 30px;
  font-size: 24px;
}
</style>

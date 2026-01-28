<template>
  <nav class="navbar">
    <!-- 顶部栏保持不变 -->
    <div class="top-bar">
      <img src="@/assets/logo.png" class="logo">
      <a href="#" class="channel-link">Click Here to join our Channel</a>
    </div>

    <!-- 主导航栏 -->
    <div class="main-nav">
      <!-- 左侧导航链接 -->
      <div class="nav-links">
        <router-link to="/">HOME</router-link>
        <router-link to="/product/digital">DIGITALPRODUCT</router-link>
        <router-link to="/product/phy">PHY</router-link>
      </div>

      <!-- 右侧图标操作区 -->
      <div class="nav-actions">
        <!-- 登录状态 -->
        <template v-if="isLoggedIn">
          <el-button type="text" @click="$router.push('/order')">
            <i class="el-icon-document"></i>
            <span>ORDERS</span>
          </el-button>
          <el-button type="text" @click="$router.push('/cart')">
            <i class="el-icon-shopping-cart"></i>
            <span>CART</span>
          </el-button>
          <el-button type="text" @click="$router.push('/profile')">
            <i class="el-icon-user"></i>
            <span>PROFILE</span>
          </el-button>
          <el-button type="text" @click="handleLogout">
            <i class="el-icon-switch-button"></i>
            <span>LOGOUT</span>
          </el-button>
        </template>

        <!-- 未登录状态 -->
        <template v-else>
          <el-button type="text" @click="$router.push('/cart')">
            <i class="el-icon-shopping-cart"></i>
            <span>CART</span>
          </el-button>
          <el-button type="text" @click="$router.push('/login')">
            <i class="el-icon-user"></i>
            <span>LOGIN</span>
          </el-button>
        </template>
      </div>
    </div>
  </nav>
</template>

<script>
import { mapGetters } from 'vuex'

export default {
  name: 'NavBar',
  computed: {
    ...mapGetters(['isLoggedIn'])
  },
  methods: {
    handleLogout() {
      this.$confirm('logout?', 'tips', {
        confirmButtonText: 'confirm',
        cancelButtonText: 'cancel',
        type: 'warning'
      }).then(() => {
        this.$store.dispatch('LogOut')
        this.$router.push('/login')
      })
    }
  }
}
</script>

<style scoped>
.navbar {
  padding: 1rem 5%;
  background: #2d2d2d;
  color: #fff;
}

.top-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 1rem;
  border-bottom: 1px solid #444;
}

.logo {
  height: 40px;
}

.channel-link {
  color: #00a8ff;
  text-decoration: none;
}

.main-nav {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem 0;
}

.nav-links {
  display: flex;
  gap: 2rem;
}

.nav-links a {
  color: #fff;
  text-decoration: none;
  font-weight: 500;
  transition: color 0.3s;
}

.nav-links a:hover {
  color: #00a8ff;
}

.nav-links a.router-link-exact-active {
  color: #00a8ff;
  border-bottom: 2px solid #00a8ff;
}

.nav-actions {
  display: flex;
  gap: 1.5rem;
  align-items: center;
}

.el-button {
  color: #fff !important;
  padding: 0 10px;
}

.el-button span {
  margin-left: 5px;
}

.el-button:hover {
  background: rgba(255,255,255,0.1) !important;
}

.el-icon {
  font-size: 18px;
  vertical-align: middle;
}
</style>

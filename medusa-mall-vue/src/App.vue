<template>
  <div id="app">
    <!-- 导航栏 -->
    <NavBar />

    <!-- 主内容区域 -->
    <router-view />

    <!-- 底部栏 -->
    <FooterSection />
  </div>
</template>

<script>
import NavBar from '@/components/NavBar.vue'
import FooterSection from '@/components/FooterSection.vue'

export default {
  components: { NavBar, FooterSection }
}
</script>

<style>
/* 全局样式 */
body {
  margin: 0;
  padding: 0;
  font-family: 'Arial', sans-serif;
  background-color: #1a1a1a; /* 黑暗主题背景 */
  color: #ffffff; /* 默认文字颜色 */
}

#app {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

/* 全局链接样式 */
a {
  color: #00a8ff; /* 链接颜色 */
  text-decoration: none;
}

a:hover {
  text-decoration: underline;
}

/* 全局按钮样式 */
button {
  background-color: #00a8ff;
  color: #ffffff;
  border: none;
  padding: 10px 20px;
  cursor: pointer;
  border-radius: 4px;
  transition: background-color 0.3s ease;
}

button:hover {
  background-color: #0097e6;
}

/* 全局输入框样式 */
input {
  background-color: #2d2d2d;
  border: 1px solid #444;
  color: #ffffff;
  padding: 10px;
  border-radius: 4px;
  outline: none;
}

input::placeholder {
  color: #888;
}
</style>

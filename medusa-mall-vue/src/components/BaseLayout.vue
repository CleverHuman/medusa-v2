<template>
  <div class="base-layout">
    <header class="layout-header">
      <!-- 您的头部内容 -->
    </header>

    <main class="layout-main">
      <router-view />
    </main>

    <footer class="layout-footer">
      <!-- 您的底部内容 -->
    </footer>
  </div>
</template>

<script>
export default {
  name: 'BaseLayout',
}
</script>

<style scoped>
.base-layout {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background-color: #1a1a1a;
  color: white;
}

.layout-header {
  /* 头部样式 */
  height: 60px;
  background: #2d2d2d;
}

.layout-main {
  flex: 1;
  padding: 20px 30px;
}

.layout-footer {
  /* 底部样式 */
  background: #2d2d2d;
  padding: 20px;
}
</style>

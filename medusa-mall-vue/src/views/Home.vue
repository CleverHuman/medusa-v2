<template>
  <div class="dark-theme">
    <!-- 大图区域 -->
    <div class="hero-section">
      <img src="@/assets/hero-dark.jpg" alt="Main Banner">
    </div>
    <!-- 双图区域 -->
      <div class="dual-images">
        <img src="@/assets/image-left.jpg" class="left-image">
        <img src="@/assets/image-right.jpg" class="right-image">
      </div>
  </div>
</template>

<script>
import NavBar from '@/components/NavBar.vue'
import FooterSection from '@/components/FooterSection.vue'



export default {
  components: { NavBar, FooterSection }
}
</script>

<style scoped>
.dark-theme {
  background-color: #1a1a1a;
  color: #ffffff;
}

.hero-section img {
  width: 100%;
  height: 600px;
  object-fit: cover;
}

.dual-images {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  padding: 40px 5%;
}

.dual-images img {
  width: 100%;
  height: 400px;
  object-fit: cover;
}
</style>

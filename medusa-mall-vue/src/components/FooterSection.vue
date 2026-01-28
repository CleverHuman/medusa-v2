<template>
  <footer class="footer">
    <div class="footer-content">
      <div class="left-section">
        <h4>ABOUT</h4>
        <p>THE PROFESSIONALS</p>
        <p>SHIPPING POLICY</p>
        <p>CONTACT</p>
      </div>

      <div class="right-section">
        <h4>CONTACT INFORMATION</h4>
        <p>@CS1</p>
        <p>@YU</p>
        <p>CS@.COM</p>
      </div>
    </div>
  </footer>
</template>

<script>
export default {
  name: 'FooterSection'
}
</script>

<style scoped>
.footer {
  background: #2d2d2d;
  padding: 3rem 5%;
  margin-top: 4rem;
}

.footer-content {
  display: flex;
  justify-content: space-between;
  max-width: 1200px;
  margin: 0 auto;
}

.left-section, .right-section {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

h4 {
  color: #00a8ff;
  margin-bottom: 1rem;
}
</style>

<template>
  <div class="app-container">
    <el-table :data="modules" border style="width: 100%">
      <el-table-column prop="name" label="Page" />
      <el-table-column label="Edit" width="100" align="center">
        <template slot-scope="scope">
          <el-link type="primary" @click="handleEdit(scope.row)">Edit</el-link>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script>
export default {
  name: "PageManager",
  data() {
    return {
      modules: [
        { name: "Home" },
        { name: "Footer" },
        { name: "Profile" }
      ]
    };
  },
  methods: {
    handleEdit(row) {
      if (row.name === "Home") {
        this.$router.push({ path: '/hide/page/home' });
      } else if (row.name === "Footer") {
        this.$router.push({ path: '/hide/page/footer' });
      } else if (row.name === "Profile") {
        this.$router.push({ path: '/hide/page/profile' });
      } else {
        this.$message.info(`Edit ${row.name} (Feature to be implemented)`);
      }
    }
  }
};
</script>

<style scoped>
.app-container {
  padding: 20px;
  background: #f5f7fa;
}
</style>

<template>
  <el-card class="stat-card" shadow="hover">
    <div class="stat-content">
      <div class="stat-value">{{ value }}</div>
      <div class="stat-label">{{ label }}</div>
    </div>
  </el-card>
</template>

<script>
export default {
  name: 'StatCard',
  props: {
    label: {
      type: String,
      required: true
    },
    value: {
      type: [String, Number],
      required: true
    }
  }
}
</script>

<style lang="scss" scoped>
.stat-card {
  height: 100%;

  ::v-deep .el-card__body {
    padding: 20px;
  }

  .stat-content {
    text-align: center;

    .stat-value {
      font-size: 24px;
      font-weight: 600;
      color: #303133;
      margin-bottom: 8px;
    }

    .stat-label {
      font-size: 14px;
      color: #909399;
    }
  }
}
</style>

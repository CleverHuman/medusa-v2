<script>
import { listVOrder, exportVOrder } from "@/api/mall/vorder";

export default {
  name: "VOrder",
  data() {
    return {
      // ... existing data ...
    };
  },
  methods: {
    /** 查询订单列表 */
    getList() {
      this.loading = true;
      listVOrder(this.queryParams).then(response => {
        this.vorderList = response.rows;
        this.total = response.total;
        this.loading = false;
      });
    },
    /** 导出按钮操作 */
    handleExport() {
      this.$confirm('是否确认导出所有订单数据项?', "警告", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning"
      }).then(() => {
        this.downloadLoading = true;
        return exportVOrder(this.queryParams);
      }).then(response => {
        this.download(response.msg);
        this.downloadLoading = false;
      }).catch(() => {});
    },
    // ... existing methods ...
  }
};
</script>

<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" :inline="true" v-show="showSearch" label-width="68px">
      <!-- ... existing form items ... -->
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['mall:vorder:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- ... rest of the template ... -->
  </div>
</template> 
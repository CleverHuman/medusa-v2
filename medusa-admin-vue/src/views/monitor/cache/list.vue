<template>
  <div class="app-container">
    <el-row :gutter="20">
      <el-col :span="8">
        <el-card class="box-card">
          <div slot="header" class="clearfix">
            <span><i class="el-icon-collection"></i> Cache List</span>
          </div>
          <el-table
            v-loading="loading"
            :data="cacheNameList"
            style="width: 100%"
            @selection-change="handleSelectionChange"
            @row-click="handleRowClick"
          >
            <el-table-column type="selection" width="55" align="center" />
            <el-table-column label="No." type="index" align="center" />
            <el-table-column
              label="Cache Name"
              align="center"
              prop="cacheName"
              :show-overflow-tooltip="true"
            />
            <el-table-column
              label="Remark"
              align="center"
              prop="cacheKey"
              :show-overflow-tooltip="true"
            />
            <el-table-column label="Actions" align="center" class-name="small-padding fixed-width">
              <template slot-scope="scope">
                <el-button
                  size="mini"
                  type="text"
                  icon="el-icon-refresh"
                  @click="handleRefreshCacheName(scope.row)"
                  v-hasPermi="['monitor:cache:list']"
                >Refresh</el-button>
                <el-button
                  size="mini"
                  type="text"
                  icon="el-icon-delete"
                  @click="handleClearCacheName(scope.row)"
                  v-hasPermi="['monitor:cache:list']"
                >Clear</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="box-card">
          <div slot="header" class="clearfix">
            <span><i class="el-icon-key"></i> Key List</span>
          </div>
          <el-table
            v-loading="loading"
            :data="cacheKeyList"
            style="width: 100%"
            @selection-change="handleSelectionChange"
            @row-click="handleRowClick"
          >
            <el-table-column type="selection" width="55" align="center" />
            <el-table-column label="No." type="index" align="center" />
            <el-table-column
              label="Cache Key"
              align="center"
              prop="cacheKey"
              :show-overflow-tooltip="true"
            />
            <el-table-column label="Actions" align="center" class-name="small-padding fixed-width">
              <template slot-scope="scope">
                <el-button
                  size="mini"
                  type="text"
                  icon="el-icon-delete"
                  @click="handleClearCacheKey(scope.row)"
                  v-hasPermi="['monitor:cache:list']"
                >Clear</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="box-card">
          <div slot="header" class="clearfix">
            <span><i class="el-icon-document"></i> Cache Content</span>
            <el-button
              style="float: right; padding: 3px 0"
              type="text"
              icon="el-icon-delete"
              @click="handleClearCacheAll"
              v-hasPermi="['monitor:cache:list']"
            >Clear All</el-button>
          </div>
          <el-form :model="cacheForm" label-width="120px">
            <el-form-item label="Cache Name:" prop="cacheName">
              <el-input v-model="cacheForm.cacheName" placeholder="Please enter cache name" />
            </el-form-item>
            <el-form-item label="Cache Key:" prop="cacheKey">
              <el-input v-model="cacheForm.cacheKey" placeholder="Please enter cache key" />
            </el-form-item>
            <el-form-item label="Cache Content:" prop="cacheValue">
              <el-input
                v-model="cacheForm.cacheValue"
                type="textarea"
                :rows="15"
                placeholder="Please enter cache content"
              />
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import { listCacheName, listCacheKey, getCacheValue, clearCacheName, clearCacheKey, clearCacheAll } from "@/api/monitor/cache";

export default {
  name: "Cache",
  data() {
    return {
      // Loading mask
      loading: true,
      // Selected array
      ids: [],
      // Non-single disabled
      single: true,
      // Non-multiple disabled
      multiple: true,
      // Show search condition
      showSearch: true,
      // Total number
      total: 0,
      // Cache name table data
      cacheNameList: [],
      // Cache key table data
      cacheKeyList: [],
      // Cache form
      cacheForm: {},
      // Query parameters
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        cacheName: undefined,
        cacheKey: undefined,
      },
    };
  },
  created() {
    this.getList();
  },
  methods: {
    /** Query cache name list */
    getList() {
      this.loading = true;
      listCacheName(this.queryParams).then(response => {
        this.cacheNameList = response.rows;
        this.total = response.total;
        this.loading = false;
      });
    },
    /** Refresh cache name list */
    handleRefreshCacheName(row) {
      this.getList();
      this.$modal.msgSuccess("Cache list refreshed successfully");
    },
    /** Clear specified name cache */
    handleClearCacheName(row) {
      this.$modal.confirm('Are you sure to clear cache "' + row.cacheName + '" ?').then(function() {
        return clearCacheName(row.cacheName);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("Cache [" + row.cacheName + "] cleared successfully");
      }).catch(() => {});
    },
    /** Query cache key list */
    handleRowClick(row) {
      this.loading = true;
      listCacheKey(row.cacheName).then(response => {
        this.cacheKeyList = response.rows;
        this.loading = false;
      });
    },
    /** Refresh cache key list */
    handleRefreshCacheKey(row) {
      this.handleRowClick(row);
    },
    /** 列表前缀去除 */
    nameFormatter(row) {
      return row.cacheName.replace(":", "");
    },
    /** 键名前缀去除 */
    keyFormatter(cacheKey) {
      return cacheKey.replace(this.nowCacheName, "");
    },
    /** 查询缓存内容详细 */
    handleCacheValue(cacheKey) {
      getCacheValue(this.nowCacheName, cacheKey).then(response => {
        this.cacheForm = response.data;
      });
    },
    /** 清理全部缓存 */
    handleClearCacheAll() {
      clearCacheAll().then(response => {
        this.$modal.msgSuccess("清理全部缓存成功");
      });
    }
  },
};
</script>

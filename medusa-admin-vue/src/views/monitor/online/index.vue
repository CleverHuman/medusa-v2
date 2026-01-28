<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="Login Address" prop="ipaddr">
        <el-input
          v-model="queryParams.ipaddr"
          placeholder="Please enter login address"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="Username" prop="userName">
        <el-input
          v-model="queryParams.userName"
          placeholder="Please enter username"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">Search</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">Reset</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="el-icon-delete"
          size="mini"
          :disabled="multiple"
          @click="handleForceLogout"
          v-hasPermi="['monitor:online:forceLogout']"
        >Force Logout</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="onlineList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="No." type="index" align="center">
        <template slot-scope="scope">
          <span>{{ (queryParams.pageNum - 1) * queryParams.pageSize + scope.$index + 1 }}</span>
        </template>
      </el-table-column>
      <el-table-column label="Session ID" align="center" prop="tokenId" :show-overflow-tooltip="true" />
      <el-table-column label="Login Name" align="center" prop="userName" :show-overflow-tooltip="true" />
      <el-table-column label="Department Name" align="center" prop="deptName" />
      <el-table-column label="Host" align="center" prop="ipaddr" :show-overflow-tooltip="true" />
      <el-table-column label="Login Location" align="center" prop="loginLocation" :show-overflow-tooltip="true" />
      <el-table-column label="Browser" align="center" prop="browser" />
      <el-table-column label="Operating System" align="center" prop="os" />
      <el-table-column label="Login Time" align="center" prop="loginTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.loginTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="Actions" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleForceLogout(scope.row)"
            v-hasPermi="['monitor:online:forceLogout']"
          >Force Logout</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total>0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="getList"
    />
  </div>
</template>

<script>
import { listOnline, forceLogout } from "@/api/monitor/online";

export default {
  name: "Online",
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
      // Online user table data
      onlineList: [],
      // Query parameters
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        ipaddr: undefined,
        userName: undefined,
      },
    };
  },
  created() {
    this.getList();
  },
  methods: {
    /** Query online user list */
    getList() {
      this.loading = true;
      listOnline(this.queryParams).then(response => {
        this.onlineList = response.rows;
        this.total = response.total;
        this.loading = false;
      });
    },
    /** Search button operation */
    handleQuery() {
      this.queryParams.pageNum = 1;
      this.getList();
    },
    /** Reset button operation */
    resetQuery() {
      this.resetForm("queryForm");
      this.handleQuery();
    },
    /** Force logout button operation */
    handleForceLogout(row) {
      const tokenIds = row.tokenId || this.ids;
      this.$modal.confirm('Are you sure to force logout user "' + row.userName + '" ?').then(function() {
        return forceLogout(tokenIds);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("Force logout successful");
      }).catch(() => {});
    },
    // Multiple selection
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.tokenId)
      this.single = selection.length!==1
      this.multiple = !selection.length
    },
  }
};
</script>


<template>
  <div class="app-container">
    <!-- Status Filter Buttons -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="24">
        <el-button-group>
          <el-button
            :type="queryParams.status === undefined ? 'primary' : 'default'"
            @click="handleStatusFilter(undefined)"
          >All</el-button>
          <el-button
            v-for="dict in dict.type.order_status"
            :key="dict.value"
            :type="queryParams.status === dict.value ? 'primary' : 'default'"
            @click="handleStatusFilter(dict.value)"
          >{{ dict.label }}</el-button>
        </el-button-group>
      </el-col>
    </el-row>

    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch">
      <el-form-item label="Order No" prop="orderSn">
        <el-input
          v-model="queryParams.orderSn"
          placeholder="Please enter order number"
          clearable
          style="width: 240px"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="Member ID" prop="memberId">
        <el-input
          v-model="queryParams.memberId"
          placeholder="Please enter member ID"
          clearable
          style="width: 240px"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="Order Status" prop="status">
        <el-select
          v-model="queryParams.status"
          placeholder="Please select order status"
          clearable
          style="width: 240px"
        >
          <el-option
            v-for="dict in dict.type.order_status"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="Create Time">
        <el-date-picker
          v-model="dateRange"
          style="width: 240px"
          value-format="yyyy-MM-dd"
          type="daterange"
          range-separator="-"
          start-placeholder="Start Date"
          end-placeholder="End Date"
        ></el-date-picker>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">Search</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">Reset</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['mall:order:export']"
        >Export</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="orderList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="Order No" align="center" prop="orderSn" width="180" />
      <el-table-column label="Member ID" align="center" prop="memberId" width="120" />
      <el-table-column label="Total Amount" align="center" prop="totalAmount" width="120">
        <template slot-scope="scope">
          <span>¥{{ scope.row.totalAmount }}</span>
        </template>
      </el-table-column>
      <el-table-column label="Freight Amount" align="center" prop="freightAmount" width="120">
        <template slot-scope="scope">
          <span>¥{{ scope.row.freightAmount }}</span>
        </template>
      </el-table-column>
      <el-table-column label="Coupon Amount" align="center" prop="couponAmount" width="120">
        <template slot-scope="scope">
          <span>¥{{ scope.row.couponAmount }}</span>
        </template>
      </el-table-column>
      <el-table-column label="Discount Amount" align="center" prop="discountAmount" width="120">
        <template slot-scope="scope">
          <span>¥{{ scope.row.discountAmount }}</span>
        </template>
      </el-table-column>
      <el-table-column label="Order Status" align="center" prop="status" width="100">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.order_status" :value="scope.row.status"/>
        </template>
      </el-table-column>
      <el-table-column label="Source Type" align="center" prop="sourceType" width="100">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.source_type" :value="scope.row.sourceType"/>
        </template>
      </el-table-column>
      <el-table-column label="Create Time" align="center" prop="createTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="Operation" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['mall:order:edit']"
          >Edit</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['mall:order:remove']"
          >Delete</el-button>
          <!-- Status Action Buttons -->
          <el-button
            v-if="scope.row.status === 0"
            size="mini"
            type="success"
            @click="handleStatusChange(scope.row, 1)"
            v-hasPermi="['mall:order:edit']"
          >Ship</el-button>
          <el-button
            v-if="scope.row.status === 1"
            size="mini"
            type="primary"
            @click="handleStatusChange(scope.row, 2)"
            v-hasPermi="['mall:order:edit']"
          >Complete</el-button>
          <el-button
            v-if="[0,1,2].includes(scope.row.status)"
            size="mini"
            type="danger"
            @click="handleStatusChange(scope.row, 4)"
            v-hasPermi="['mall:order:edit']"
          >Close</el-button>
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

    <!-- Add or modify order dialog -->
    <el-dialog :title="title" :visible.sync="open" width="500px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="Order Status" prop="status">
          <el-select v-model="form.status" placeholder="Please select order status">
            <el-option
              v-for="dict in dict.type.order_status"
              :key="dict.value"
              :label="dict.label"
              :value="dict.value"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="Remark" prop="remark">
          <el-input v-model="form.remark" type="textarea" placeholder="Please enter remark" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">Submit</el-button>
        <el-button @click="cancel">Cancel</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listOrder, getOrder, updateOrder, delOrder, exportOrder } from "@/api/mall/order";

export default {
  name: "Order",
  dicts: ['order_status', 'source_type'],
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
      // Order table data
      orderList: [],
      // Popup title
      title: "",
      // Whether to show popup
      open: false,
      // Date range
      dateRange: [],
      // Query parameters
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        orderSn: undefined,
        memberId: undefined,
        status: undefined
      },
      // Form parameters
      form: {},
      // Form validation
      rules: {
        status: [
          { required: true, message: "Order status cannot be empty", trigger: "change" }
        ]
      }
    };
  },
  created() {
    this.getList();
  },
  methods: {
    /** Query order list */
    getList() {
      this.loading = true;
      listOrder(this.addDateRange(this.queryParams, this.dateRange)).then(response => {
        this.orderList = response.rows;
        this.total = response.total;
        this.loading = false;
      });
    },
    // Cancel button
    cancel() {
      this.open = false;
      this.reset();
    },
    // Form reset
    reset() {
      this.form = {
        id: undefined,
        status: undefined,
        remark: undefined
      };
      this.resetForm("form");
    },
    /** Search button operation */
    handleQuery() {
      this.queryParams.pageNum = 1;
      this.getList();
    },
    /** Reset button operation */
    resetQuery() {
      this.dateRange = [];
      this.resetForm("queryForm");
      this.handleQuery();
    },
    // Multiple selection data
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.id);
      this.single = selection.length !== 1;
      this.multiple = !selection.length;
    },
    /** View button operation */
    handleView(row) {
      this.$router.push({ path: '/mall/order/detail', query: { id: row.id }});
    },
    /** Edit button operation */
    handleUpdate(row) {
      this.reset();
      const id = row.id || this.ids[0];
      getOrder(id).then(response => {
        this.form = response.data;
        this.open = true;
        this.title = "Edit Order";
      });
    },
    /** Submit button */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          updateOrder(this.form).then(response => {
            this.$modal.msgSuccess("Update successful");
            this.open = false;
            this.getList();
          });
        }
      });
    },
    /** Delete button operation */
    handleDelete(row) {
      const ids = row.id || this.ids;
      this.$modal.confirm('Are you sure to delete the order with number "' + ids + '"?').then(() => {
        return delOrder(ids);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("Delete successful");
      }).catch(() => {});
    },
    /** Export button operation */
    handleExport() {
      this.$modal.confirm('Are you sure to export all order data?').then(() => {
        this.$modal.loading("Exporting data, please wait...");
        return exportOrder(this.queryParams);
      }).then(response => {
        this.download(response.msg);
        this.$modal.closeLoading();
      }).catch(() => {});
    },
    /** Status change operation */
    handleStatusChange(row, newStatus) {
      const statusMap = {
        0: 'Pending Payment',
        1: 'Pending Shipment',
        2: 'Shipped',
        3: 'Completed',
        4: 'Closed',
        5: 'Invalid'
      };
      const statusText = statusMap[newStatus] || 'Unknown';
      this.$modal.confirm(`Are you sure to change the order status to "${statusText}"?`).then(() => {
        const form = {
          id: row.id,
          status: newStatus,
          updateBy: this.$store.state.user.name
        };
        return updateOrder(form);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("Status update successful");
      }).catch(() => {});
    },
    /** Status filter button operation */
    handleStatusFilter(status) {
      this.queryParams.status = status;
      this.handleQuery();
    }
  }
};
</script>

<style scoped>
.el-button-group {
  margin-bottom: 15px;
}
.el-button-group .el-button {
  margin-right: 0;
}
</style>

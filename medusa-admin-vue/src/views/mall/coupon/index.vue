<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch">
      <el-form-item label="Coupon Name" prop="name">
        <el-input
          v-model="queryParams.name"
          placeholder="Please enter coupon name"
          clearable
          style="width: 240px"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="Coupon Code" prop="code">
        <el-input
          v-model="queryParams.code"
          placeholder="Please enter coupon code"
          clearable
          style="width: 240px"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="Coupon Type" prop="type">
        <el-select
          v-model="queryParams.type"
          placeholder="Please select coupon type"
          clearable
          style="width: 240px"
        >
          <el-option
            v-for="dict in dict.type.coupon_type"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="Status" prop="status">
        <el-select
          v-model="queryParams.status"
          placeholder="Please select status"
          clearable
          style="width: 240px"
        >
          <el-option
            v-for="dict in dict.type.coupon_status"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">Search</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">Reset</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="el-icon-plus"
          size="mini"
          @click="handleAdd"
          v-hasPermi="['mall:coupon:add']"
        >Add</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="el-icon-edit"
          size="mini"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['mall:coupon:edit']"
        >Edit</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-plus"
          size="mini"
          @click="handleBulkAdd"
          v-hasPermi="['mall:coupon:add']"
        >Bulk Add</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="el-icon-delete"
          size="mini"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['mall:coupon:remove']"
        >Delete</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['mall:coupon:export']"
        >Export</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="couponList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="Coupon Name" align="center" prop="name" />
      <el-table-column label="Coupon Code" align="center" prop="code" />
      <el-table-column label="Description" align="center" prop="remark" />
      <el-table-column label="Coupon Type" align="center" prop="type">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.coupon_type" :value="scope.row.type"/>
        </template>
      </el-table-column>
      <el-table-column label="Discount Amount" align="center" prop="amount">
        <template slot-scope="scope">
          <span v-if="scope.row.type === 1">¥{{ scope.row.amount }}</span>
          <span v-else-if="scope.row.type === 2">{{ (scope.row.discount * 100).toFixed(0) }}%</span>
          <span v-else-if="scope.row.type === 3">Free Shipping</span>
        </template>
      </el-table-column>
      <el-table-column label="Total Count" align="center" prop="totalCount" />
      <el-table-column label="Used Count" align="center" prop="usedCount" />
      <el-table-column label="Create Time" align="center" prop="createTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="Start Time" align="center" prop="startTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.startTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="End Time" align="center" prop="endTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.endTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="Status" align="center" prop="status">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.coupon_status" :value="scope.row.status"/>
        </template>
      </el-table-column>
      <el-table-column label="Operation" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['mall:coupon:edit']"
          >Edit</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['mall:coupon:remove']"
          >Delete</el-button>
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

    <!-- Add or modify coupon dialog -->
    <el-dialog :title="title" :visible.sync="open" width="600px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="120px">
        <el-form-item label="Coupon Name" prop="name">
          <el-input v-model="form.name" placeholder="Please enter coupon name" />
        </el-form-item>
        <el-form-item label="Coupon Code" prop="code">
          <el-input 
            v-model="form.code" 
            placeholder="Please enter coupon code (Mix of numbers and letters)" 
            style="width: calc(100% - 100px)"
          />
          <el-button 
            type="primary" 
            size="mini" 
            @click="generateCouponCode" 
            style="margin-left: 10px; width: 90px"
          >
            Generate
          </el-button>
        </el-form-item>
        <el-form-item label="Description" prop="remark">
          <el-input v-model="form.remark" type="textarea" placeholder="Please enter description" />
        </el-form-item>
        <el-form-item label="Coupon Type" prop="type">
          <el-select v-model="form.type" placeholder="Please select coupon type" @change="handleTypeChange">
            <el-option
              v-for="dict in dict.type.coupon_type"
              :key="dict.value"
              :label="dict.label"
              :value="dict.value"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="Fixed Amount" prop="amount" v-if="form.type == 1">
          <el-input-number v-model="form.amount" :precision="2" :step="0.1" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="Percentage Discount" prop="discount" v-if="form.type == 2">
          <el-input-number v-model="form.discount" :precision="2" :step="0.1" :min="0" :max="100" style="width: 100%" />
        </el-form-item>
        <el-form-item label="Total Count" prop="totalCount">
          <el-input-number v-model="form.totalCount" :min="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="Valid Period" prop="validPeriod">
          <el-date-picker
            v-model="dateRange"
            type="datetimerange"
            range-separator="-"
            start-placeholder="Start Date"
            end-placeholder="End Date"
            value-format="yyyy-MM-dd HH:mm:ss"
            style="width: 100%"
          ></el-date-picker>
        </el-form-item>
        <el-form-item label="Status" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio
              v-for="dict in dict.type.coupon_status"
              :key="dict.value"
              :label="dict.value"
            >{{dict.label}}</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">Confirm</el-button>
        <el-button @click="cancel">Cancel</el-button>
      </div>
    </el-dialog>

    <!-- Bulk add coupon dialog -->
    <el-dialog title="Bulk Add Coupons" :visible.sync="bulkOpen" width="700px" append-to-body>
      <el-form ref="bulkForm" :model="bulkForm" :rules="bulkRules" label-width="120px">
        <el-form-item label="Coupon Name" prop="name">
          <el-input v-model="bulkForm.name" placeholder="Please enter coupon name" />
        </el-form-item>
        <el-form-item label="Code Prefix" prop="codePrefix">
          <el-input v-model="bulkForm.codePrefix" placeholder="Optional prefix for coupon codes" />
        </el-form-item>
        <el-form-item label="Quantity" prop="quantity">
          <el-input-number v-model="bulkForm.quantity" :min="1" :max="100" style="width: 100%" />
        </el-form-item>
        <el-form-item label="Description" prop="remark">
          <el-input v-model="bulkForm.remark" type="textarea" placeholder="Please enter description" />
        </el-form-item>
        <el-form-item label="Coupon Type" prop="type">
          <el-select v-model="bulkForm.type" placeholder="Please select coupon type" @change="handleBulkTypeChange">
            <el-option
              v-for="dict in dict.type.coupon_type"
              :key="dict.value"
              :label="dict.label"
              :value="dict.value"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="Fixed Amount" prop="amount" v-if="bulkForm.type == 1">
          <el-input-number v-model="bulkForm.amount" :precision="2" :step="0.1" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="Percentage Discount" prop="discount" v-if="bulkForm.type == 2">
          <el-input-number v-model="bulkForm.discount" :precision="2" :step="0.1" :min="0" :max="100" style="width: 100%" />
        </el-form-item>
        <el-form-item label="Total Count" prop="totalCount">
          <el-input-number v-model="bulkForm.totalCount" :min="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="Valid Period" prop="validPeriod">
          <el-date-picker
            v-model="bulkDateRange"
            type="datetimerange"
            range-separator="-"
            start-placeholder="Start Date"
            end-placeholder="End Date"
            value-format="yyyy-MM-dd HH:mm:ss"
            style="width: 100%"
          ></el-date-picker>
        </el-form-item>
        <el-form-item label="Status" prop="status">
          <el-radio-group v-model="bulkForm.status">
            <el-radio
              v-for="dict in dict.type.coupon_status"
              :key="dict.value"
              :label="dict.value"
            >{{dict.label}}</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitBulkForm">Confirm</el-button>
        <el-button @click="cancelBulk">Cancel</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listCoupon, getCoupon, addCoupon, updateCoupon, delCoupon, exportCoupon, bulkAddCoupon } from "@/api/mall/coupon";

export default {
  name: "Coupon",
  dicts: ['coupon_type', 'coupon_status'],
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
      // Coupon table data
      couponList: [],
      // Popup title
      title: "",
      // Whether to show popup
      open: false,
      // Whether to show bulk popup
      bulkOpen: false,
      // Date range
      dateRange: [],
      // Bulk date range
      bulkDateRange: [],
      // Query parameters
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        name: undefined,
        code: undefined,
        type: undefined,
        status: undefined
      },
      // Form parameters
      form: {},
      // Bulk form parameters
      bulkForm: {},
      // Form validation
      rules: {
        name: [
          { required: true, message: "Coupon name cannot be empty", trigger: "blur" }
        ],
        code: [
          { required: true, message: "Coupon code cannot be empty", trigger: "blur" },
          { 
            pattern: /^[A-Za-z0-9]+$/, 
            message: "Coupon code must contain only letters and numbers", 
            trigger: "blur" 
          },
          { 
            min: 6, 
            max: 12, 
            message: "Coupon code length must be between 6 and 12 characters", 
            trigger: "blur" 
          }
        ],
        type: [
          { required: true, message: "Coupon type cannot be empty", trigger: "change" }
        ],
        totalCount: [
          { required: true, message: "Total count cannot be empty", trigger: "blur" }
        ],
        status: [
          { required: true, message: "Status cannot be empty", trigger: "change" }
        ]
      },
      // Bulk form validation
      bulkRules: {
        name: [
          { required: true, message: "Coupon name cannot be empty", trigger: "blur" }
        ],
        quantity: [
          { required: true, message: "Quantity cannot be empty", trigger: "blur" },
          { type: 'number', min: 1, max: 100, message: "Quantity must be between 1 and 100", trigger: "blur" }
        ],
        type: [
          { required: true, message: "Coupon type cannot be empty", trigger: "change" }
        ],
        totalCount: [
          { required: true, message: "Total count cannot be empty", trigger: "blur" }
        ],
        status: [
          { required: true, message: "Status cannot be empty", trigger: "change" }
        ]
      }
    };
  },
  created() {
    this.getList();
  },
  methods: {
    /** Query coupon list */
    getList() {
      this.loading = true;
      listCoupon(this.addDateRange(this.queryParams, this.dateRange)).then(response => {
        this.couponList = response.rows;
        this.total = response.total;
        this.loading = false;
      });
    },
    // Cancel button
    cancel() {
      this.open = false;
      this.reset();
    },
    // Cancel bulk button
    cancelBulk() {
      this.bulkOpen = false;
      this.resetBulk();
    },
    // Form reset
    reset() {
      this.form = {
        couponId: undefined,
        name: undefined,
        code: undefined,
        type: "1", // 设置默认类型为字符串"1"，这样字典能正确显示
        amount: undefined,
        discount: undefined,
        minPoint: 0,
        startTime: undefined,
        endTime: undefined,
        totalCount: 1,
        usedCount: 0,
        status: "1",
        remark: undefined
      };
      this.dateRange = [];
      this.resetForm("form");
    },
    // Bulk form reset
    resetBulk() {
      this.bulkForm = {
        name: undefined,
        codePrefix: undefined,
        quantity: 10,
        type: "1",
        amount: undefined,
        discount: undefined,
        minPoint: 0,
        startTime: undefined,
        endTime: undefined,
        totalCount: 1,
        status: "1",
        remark: undefined
      };
      this.bulkDateRange = [];
      this.resetForm("bulkForm");
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
      this.ids = selection.map(item => item.couponId);
      this.single = selection.length !== 1;
      this.multiple = !selection.length;
    },
    /** Add button operation */
    handleAdd() {
      this.reset();
      this.open = true;
      this.title = "Add Coupon";
    },
    /** Bulk add button operation */
    handleBulkAdd() {
      this.resetBulk();
      this.bulkOpen = true;
    },
    /** Edit button operation */
    handleUpdate(row) {
      this.reset();
      const couponId = row.couponId || this.ids[0];
      getCoupon(couponId).then(response => {
        this.form = response.data;
        // Ensure type is converted to string for dictionary lookup
        if (this.form.type !== undefined) {
          this.form.type = String(this.form.type);
        }
        // Convert discount from decimal to percentage for display (type 2 = percentage coupon)
        if (this.form.type === "2" && this.form.discount !== undefined) {
          this.form.discount = this.form.discount * 100;
        }
        // Set date range for editing
        if (this.form.startTime && this.form.endTime) {
          this.dateRange = [this.form.startTime, this.form.endTime];
        }
        this.open = true;
        this.title = "Edit Coupon";
      });
    },
    /** Handle type change */
    handleTypeChange(value) {
      // Reset related fields when type changes
      if (value == 1) {
        this.form.discount = undefined;
      } else if (value == 2) {
        this.form.amount = undefined;
        this.form.minPoint = undefined;
      } else if (value == 3) {
        this.form.amount = undefined;
        this.form.discount = undefined;
        this.form.minPoint = undefined;
      }
    },
    /** Handle bulk type change */
    handleBulkTypeChange(value) {
      // Reset related fields when type changes
      if (value == 1) {
        this.bulkForm.discount = undefined;
      } else if (value == 2) {
        this.bulkForm.amount = undefined;
        this.bulkForm.minPoint = undefined;
      } else if (value == 3) {
        this.bulkForm.amount = undefined;
        this.bulkForm.discount = undefined;
        this.bulkForm.minPoint = undefined;
      }
    },
    /** Submit button */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          // Convert percentage discount to decimal for database storage (type 2 = percentage coupon)
          if (this.form.type === "2" && this.form.discount !== undefined) {
            this.form.discount = this.form.discount / 100;
          }
          
          // Set date range to form
          if (this.dateRange && this.dateRange.length === 2) {
            this.form.startTime = this.dateRange[0];
            this.form.endTime = this.dateRange[1];
          } else {
            // If no date range is set, default to 1 year from now
            const now = new Date();
            const oneYearLater = new Date(now.getFullYear() + 1, now.getMonth(), now.getDate());
            
            this.form.startTime = now.toISOString().slice(0, 19).replace('T', ' ');
            this.form.endTime = oneYearLater.toISOString().slice(0, 19).replace('T', ' ');
          }
          
          if (this.form.couponId != undefined) {
            updateCoupon(this.form).then(response => {
              this.$modal.msgSuccess("Update successful");
              this.open = false;
              this.getList();
            });
          } else {
            addCoupon(this.form).then(response => {
              this.$modal.msgSuccess("Add successful");
              this.open = false;
              this.getList();
            });
          }
        }
      });
    },
    /** Submit bulk form */
    submitBulkForm() {
      this.$refs["bulkForm"].validate(valid => {
        if (valid) {
          // Convert percentage discount to decimal for database storage (type 2 = percentage coupon)
          if (this.bulkForm.type === "2" && this.bulkForm.discount !== undefined) {
            this.bulkForm.discount = this.bulkForm.discount / 100;
          }
          
          // Set date range to form
          if (this.bulkDateRange && this.bulkDateRange.length === 2) {
            this.bulkForm.startTime = this.bulkDateRange[0];
            this.bulkForm.endTime = this.bulkDateRange[1];
          } else {
            // If no date range is set, default to 1 year from now
            const now = new Date();
            const oneYearLater = new Date(now.getFullYear() + 1, now.getMonth(), now.getDate());
            
            this.bulkForm.startTime = now.toISOString().slice(0, 19).replace('T', ' ');
            this.bulkForm.endTime = oneYearLater.toISOString().slice(0, 19).replace('T', ' ');
          }
          
          bulkAddCoupon(this.bulkForm).then(response => {
            this.$modal.msgSuccess("Successfully created " + response.data + " coupons");
            this.bulkOpen = false;
            this.getList();
          });
        }
      });
    },
    /** Delete button operation */
    handleDelete(row) {
      const couponIds = row.couponId || this.ids;
      this.$modal.confirm('Are you sure to delete the coupon with ID "' + couponIds + '"?').then(() => {
        return delCoupon(couponIds);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("Delete successful");
      }).catch(() => {});
    },
    /** Export button operation */
    handleExport() {
      this.$modal.confirm('Are you sure to export all coupon data?').then(() => {
        this.$modal.loading("Exporting data, please wait...");
        return exportCoupon(this.queryParams);
      }).then(response => {
        this.download(response.msg);
        this.$modal.closeLoading();
      }).catch(() => {});
    },
    /** Generate coupon code */
    generateCouponCode() {
      const chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789';
      let code = '';
      for (let i = 0; i < 6; i++) {
        code += chars[Math.floor(Math.random() * chars.length)];
      }
      this.form.code = code;
      this.$message.success('Coupon code generated successfully');
    }
  }
};
</script> 
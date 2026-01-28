<template>
  <div class="app-container">
    <!-- 搜索表单 -->
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch">
      <el-form-item label="Postage SKU" prop="code">
        <el-input
          v-model="queryParams.code"
          placeholder="Please enter shipping code"
          clearable
          style="width: 200px"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="Postage Option" prop="name">
        <el-input
          v-model="queryParams.name"
          placeholder="Please enter shipping name"
          clearable
          style="width: 200px"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="Status" prop="status">
        <el-select
          v-model="queryParams.status"
          placeholder="Please select status"
          clearable
          style="width: 200px"
        >
          <el-option label="Enabled" :value="1" />
          <el-option label="Disabled" :value="0" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">Search</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">Reset</el-button>
      </el-form-item>
    </el-form>

    <!-- 操作按钮 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="el-icon-plus"
          size="mini"
          @click="handleAdd"
          v-hasPermi="['mall:shipping:add']"
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
          v-hasPermi="['mall:shipping:edit']"
        >Edit</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="el-icon-delete"
          size="mini"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['mall:shipping:remove']"
        >Delete</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- 数据表格 -->
    <el-table v-loading="loading" :data="shippingList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="Postage SKU" align="center" prop="code" width="120">
        <template slot-scope="scope">
          <div class="postage-id">
            <span class="id-circle">{{ scope.row.code }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="Postage Option" align="center" prop="name" />
      <el-table-column label="Postage Price" align="center" prop="fee" width="120">
        <template slot-scope="scope">
          <span>${{ scope.row.fee }}</span>
        </template>
      </el-table-column>
      <el-table-column label="Status" align="center" prop="status" width="100">
        <template slot-scope="scope">
          <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
            {{ scope.row.status === 1 ? 'Enabled' : 'Disabled' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="Create Time" align="center" prop="createTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime, '{y}-{m}-{d} {h}:{i}:{s}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="Action" align="center" class-name="small-padding fixed-width" width="180">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['mall:shipping:edit']"
          >Edit</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['mall:shipping:remove']"
          >Delete</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <pagination
      v-show="total>0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="getList"
    />

    <!-- 添加或修改配送方式对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="500px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="120px">
        <el-form-item label="Postage SKU" prop="code">
          <el-input v-model="form.code" placeholder="Please enter shipping code" />
        </el-form-item>
        <el-form-item label="Postage Option" prop="name">
          <el-input v-model="form.name" placeholder="Please enter shipping name" />
        </el-form-item>
        <el-form-item label="Postage Price" prop="fee">
          <el-input-number
            v-model="form.fee"
            :precision="2"
            :step="0.1"
            :min="0"
            :max="999.99"
            placeholder="Please enter shipping price"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="Status" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">Enabled</el-radio>
            <el-radio :label="0">Disabled</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">Confirm</el-button>
        <el-button @click="cancel">Cancel</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listShipping, getShipping, delShipping, addShipping, updateShipping } from "@/api/mall/shipping";

export default {
  name: "Shipping",
  data() {
    return {
      // 遮罩层
      loading: true,
      // 选中数组
      ids: [],
      // 非单个禁用
      single: true,
      // 非多个禁用
      multiple: true,
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // 配送方式表格数据
      shippingList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        code: null,
        name: null,
        status: null
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        code: [
          { required: true, message: "Postage SKU cannot be empty", trigger: "blur" },
          { min: 2, max: 10, message: "Postage SKU length should be 2-10 characters", trigger: "blur" }
        ],
        name: [
          { required: true, message: "Postage Option cannot be empty", trigger: "blur" },
          { min: 2, max: 50, message: "Postage Option length should be 2-50 characters", trigger: "blur" }
        ],
        fee: [
          { required: true, message: "Postage price cannot be empty", trigger: "blur" },
          { type: 'number', min: 0, max: 999.99, message: "Postage Price should be between 0-999.99", trigger: "blur" }
        ]
      }
    };
  },
  created() {
    this.getList();
  },
  methods: {
    /** 查询配送方式列表 */
    getList() {
      this.loading = true;
      listShipping(this.queryParams).then(response => {
        this.shippingList = response.rows;
        this.total = response.total;
        this.loading = false;
      });
    },
    // 取消按钮
    cancel() {
      this.open = false;
      this.reset();
    },
    // 表单重置
    reset() {
      this.form = {
        id: null,
        code: null,
        name: null,
        fee: null,
        status: 1
      };
      this.resetForm("form");
    },
    /** 搜索按钮操作 */
    handleQuery() {
      this.queryParams.pageNum = 1;
      this.getList();
    },
    /** 重置按钮操作 */
    resetQuery() {
      this.resetForm("queryForm");
      this.handleQuery();
    },
    // 多选框选中数据
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.code)
      this.single = selection.length!==1
      this.multiple = !selection.length
    },
    /** 新增按钮操作 */
    handleAdd() {
      this.reset();
      this.open = true;
      this.title = "Add Shipping Method";
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset();
      const code = row.code || this.ids[0]
      getShipping(code).then(response => {
        this.form = response.data;
        this.open = true;
        this.title = "Edit Shipping Method";
      });
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.id != null) {
            updateShipping(this.form).then(response => {
              this.$modal.msgSuccess("Modified successfully");
              this.open = false;
              this.getList();
            });
          } else {
            addShipping(this.form).then(response => {
              this.$modal.msgSuccess("Added successfully");
              this.open = false;
              this.getList();
            });
          }
        }
      });
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      const codes = row.code || this.ids;
      this.$modal.confirm('Are you sure to delete the selected shipping method(s)?').then(function() {
        return delShipping(codes);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("Deleted successfully");
      }).catch(() => {});
    }
  }
};
</script>

<style scoped>
.postage-id {
  display: flex;
  align-items: center;
  justify-content: center;
}

.id-circle {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 50px;
  height: 40px;
  padding: 0 8px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-radius: 20px;
  font-weight: bold;
  font-size: 12px;
  text-transform: uppercase;
  white-space: nowrap;
}
</style>

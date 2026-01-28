<template>
  <div class="app-container">
    <!-- 操作按钮 -->
    <el-button type="primary" @click="handleAdd" style="margin-bottom: 16px;">Add Benefit</el-button>
    
    <!-- 表格 -->
    <el-table
      :data="benefitList"
      v-loading="loading"
      style="width: 100%"
      border
      @row-dblclick="handleEdit"
    >
      <el-table-column prop="levelId" label="Level ID" width="80" />
      <el-table-column prop="levelName" label="Level Name" width="100" />
      <el-table-column prop="point" label="Points" width="80"/>
      <el-table-column prop="des" label="Description" width="300"/>
      <el-table-column prop="fixedDiscount" label="Fixed Discount" />
      <el-table-column prop="percentDiscount" label="Percent Discount" />
      <el-table-column prop="shippingDiscount" label="Shipping Discount" />
      <el-table-column label="Actions" width="120">
        <template slot-scope="scope">
          <el-button size="mini" @click="handleEdit(scope.row)">Edit</el-button>
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

    <!-- 新增/编辑弹窗 -->
    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="600px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="140px" size="small">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="Level ID" prop="levelId">
              <el-input v-model="form.levelId" :disabled="dialogType==='edit'" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="Level Name" prop="levelName">
              <el-input v-model="form.levelName" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="Points" prop="point">
              <el-input v-model="form.point" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="Fixed Discount" prop="fixedDiscount">
              <el-input v-model="form.fixedDiscount" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="Percent Discount" prop="percentDiscount">
              <el-input v-model="form.percentDiscount" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="Shipping Discount" prop="shippingDiscount">
              <el-input v-model="form.shippingDiscount" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="Description" prop="des">
          <el-input v-model="form.des" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible=false">Cancel</el-button>
        <el-button type="primary" @click="submitForm">Save</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {listBenefit, getBenefit, addBenefit, updateBenefit} from "@/api/mall/benefit";

export default {
  name: "BenefitList",
  data() {
    return {
      loading :true,
      ids : [],
      single: true,
      multiple: true,
      showSearch: true,
      total: 0 ,
      benefitList: [],
      queryParams: {
        pageNum: 1,
        pageSize: 10
      },
      // 弹窗相关
      dialogVisible: false,
      dialogType: 'add', // add/edit
      dialogTitle: '',
      form: {
        levelId: '',
        levelName: '',
        point: '',
        des: '',
        fixedDiscount: '',
        percentDiscount: '',
        shippingDiscount: '',
        remark: ''
      },
      rules: {
        levelId: [{ required: true, message: 'Please enter Level ID', trigger: 'blur' }],
        levelName: [{ required: true, message: 'Please enter Level Name', trigger: 'blur' }],
        point: [{ required: true, message: 'Please enter Points', trigger: 'blur' }],
      }
    };
  },
  created() {
    this.getList();
  },
  methods: {
    getList() {
      this.loading = true;
      listBenefit().then(response => {
        this.benefitList = response.rows;
        this.total = response.total;
        this.loading = false;
      })
    },
    // 新增
    handleAdd() {
      this.dialogType = 'add';
      this.dialogTitle = 'Add Benefit';
      this.form = {
        levelId: '',
        levelName: '',
        point: '',
        des: '',
        fixedDiscount: '',
        percentDiscount: '',
        shippingDiscount: '',
        remark: ''
      };
      this.dialogVisible = true;
    },
    // 编辑
    handleEdit(row) {
      this.dialogType = 'edit';
      this.dialogTitle = 'Edit Benefit';
      getBenefit(row.levelId).then(res => {
        this.form = Object.assign({}, res.data);
        this.dialogVisible = true;
      });
    },
    // 提交表单
    submitForm() {
      this.$refs.formRef.validate(valid => {
        if (!valid) return;
        if (this.dialogType === 'add') {
          addBenefit(this.form).then(() => {
            this.$message.success('Added successfully');
            this.dialogVisible = false;
            this.getList();
          });
        } else {
          updateBenefit(this.form).then(() => {
            this.$message.success('Updated successfully');
            this.dialogVisible = false;
            this.getList();
          });
        }
      });
    },
    // 搜索
    handleQuery() {
      this.queryParams.pageNum = 1;
      this.getList();
    },
    resetQuery() {
      this.resetForm("queryForm")
      this.getList();
    },
  }
};
</script>

<style scoped>
.app-container {
  background: #fff;
  padding: 20px;
}
</style>

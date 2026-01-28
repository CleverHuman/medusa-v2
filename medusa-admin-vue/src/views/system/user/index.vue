<template>
  <div class="app-container">
    <el-row :gutter="20">
      <splitpanes :horizontal="this.$store.getters.device === 'mobile'" class="default-theme">
        <!--部门数据-->
        <!--Department Data-->
        <pane size="16">
          <el-col>
            <div class="head-container">
              <el-input v-model="deptName" placeholder="Please enter department name" clearable size="small" prefix-icon="el-icon-search" style="margin-bottom: 20px" />
            </div>
            <div class="head-container">
              <el-tree :data="deptOptions" :props="defaultProps" :expand-on-click-node="false" :filter-node-method="filterNode" ref="tree" node-key="id" default-expand-all highlight-current @node-click="handleNodeClick" />
            </div>
          </el-col>
        </pane>
        <!--用户数据-->
        <!--User Data-->
        <pane size="84">
          <el-col>
            <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
              <el-form-item label="Username" prop="userName">
                <el-input v-model="queryParams.userName" placeholder="Please enter username" clearable style="width: 240px" @keyup.enter.native="handleQuery" />
              </el-form-item>
              <el-form-item label="Phone Number" prop="phonenumber">
                <el-input v-model="queryParams.phonenumber" placeholder="Please enter phone number" clearable style="width: 240px" @keyup.enter.native="handleQuery" />
              </el-form-item>
              <el-form-item label="Status" prop="status">
                <el-select v-model="queryParams.status" placeholder="User status" clearable style="width: 240px">
                  <el-option v-for="dict in dict.type.sys_normal_disable" :key="dict.value" :label="dict.label" :value="dict.value" />
                </el-select>
              </el-form-item>
              <el-form-item label="Create Time">
                <el-date-picker v-model="dateRange" style="width: 240px" value-format="yyyy-MM-dd" type="daterange" range-separator="-" start-placeholder="Start Date" end-placeholder="End Date"></el-date-picker>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">Search</el-button>
                <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">Reset</el-button>
              </el-form-item>
            </el-form>

            <el-row :gutter="10" class="mb8">
              <el-col :span="1.5">
                <el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd" v-hasPermi="['system:user:add']">Add</el-button>
              </el-col>
              <el-col :span="1.5">
                <el-button type="success" plain icon="el-icon-edit" size="mini" :disabled="single" @click="handleUpdate" v-hasPermi="['system:user:edit']">Edit</el-button>
              </el-col>
              <el-col :span="1.5">
                <el-button type="danger" plain icon="el-icon-delete" size="mini" :disabled="multiple" @click="handleDelete" v-hasPermi="['system:user:remove']">Delete</el-button>
              </el-col>
              <el-col :span="1.5">
                <el-button type="info" plain icon="el-icon-upload2" size="mini" @click="handleImport" v-hasPermi="['system:user:import']">Import</el-button>
              </el-col>
              <el-col :span="1.5">
                <el-button type="warning" plain icon="el-icon-download" size="mini" @click="handleExport" v-hasPermi="['system:user:export']">Export</el-button>
              </el-col>
              <right-toolbar :showSearch.sync="showSearch" @queryTable="getList" :columns="columns"></right-toolbar>
            </el-row>

            <el-table v-loading="loading" :data="userList" @selection-change="handleSelectionChange">
              <el-table-column type="selection" width="50" align="center" />
              <el-table-column label="User ID" align="center" key="userId" prop="userId" v-if="columns[0].visible" />
              <el-table-column label="Username" align="center" key="userName" prop="userName" v-if="columns[1].visible" :show-overflow-tooltip="true" />
              <el-table-column label="Nickname" align="center" key="nickName" prop="nickName" v-if="columns[2].visible" :show-overflow-tooltip="true" />
              <el-table-column label="Department" align="center" key="deptName" prop="dept.deptName" v-if="columns[3].visible" :show-overflow-tooltip="true" />
              <el-table-column label="Phone Number" align="center" key="phonenumber" prop="phonenumber" v-if="columns[4].visible" width="120" />
              <el-table-column label="Status" align="center" key="status" v-if="columns[5].visible">
                <template slot-scope="scope">
                  <el-switch v-model="scope.row.status" active-value="0" inactive-value="1" @change="handleStatusChange(scope.row)"></el-switch>
                </template>
              </el-table-column>
              <el-table-column label="Create Time" align="center" prop="createTime" v-if="columns[6].visible" width="160">
                <template slot-scope="scope">
                  <span>{{ parseTime(scope.row.createTime) }}</span>
                </template>
              </el-table-column>
              <el-table-column label="Operation" align="center" width="160" class-name="small-padding fixed-width">
                <template slot-scope="scope" v-if="scope.row.userId !== 1">
                  <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)" v-hasPermi="['system:user:edit']">Edit</el-button>
                  <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)" v-hasPermi="['system:user:remove']">Delete</el-button>
                  <el-dropdown size="mini" @command="(command) => handleCommand(command, scope.row)" v-hasPermi="['system:user:resetPwd', 'system:user:edit']">
                    <el-button size="mini" type="text" icon="el-icon-d-arrow-right">More</el-button>
                    <el-dropdown-menu slot="dropdown">
                      <el-dropdown-item command="handleResetPwd" icon="el-icon-key" v-hasPermi="['system:user:resetPwd']">Reset Password</el-dropdown-item>
                      <el-dropdown-item command="handleAuthRole" icon="el-icon-circle-check" v-hasPermi="['system:user:edit']">Assign Role</el-dropdown-item>
                    </el-dropdown-menu>
                  </el-dropdown>
                </template>
              </el-table-column>
            </el-table>

            <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />
          </el-col>
        </pane>
      </splitpanes>
    </el-row>

    <!-- 添加或修改用户配置对话框 -->
    <!-- Add or Modify User Configuration Dialog -->
    <el-dialog :title="title" :visible.sync="open" width="600px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="Nickname" prop="nickName">
              <el-input v-model="form.nickName" placeholder="Please enter nickname" maxlength="30" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="Department" prop="deptId">
              <treeselect v-model="form.deptId" :options="enabledDeptOptions" :show-count="true" placeholder="Please select department" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="Phone Number" prop="phonenumber">
              <el-input v-model="form.phonenumber" placeholder="Please enter phone number" maxlength="11" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="Email" prop="email">
              <el-input v-model="form.email" placeholder="Please enter email" maxlength="50" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item v-if="form.userId == undefined" label="Username" prop="userName">
              <el-input v-model="form.userName" placeholder="Please enter username" maxlength="30" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item v-if="form.userId == undefined" label="Password" prop="password">
              <el-input v-model="form.password" placeholder="Please enter password" type="password" maxlength="20" show-password />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="Gender">
              <el-select v-model="form.sex" placeholder="Please select gender">
                <el-option v-for="dict in dict.type.sys_user_sex" :key="dict.value" :label="dict.label" :value="dict.value"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="Status">
              <el-radio-group v-model="form.status">
                <el-radio v-for="dict in dict.type.sys_normal_disable" :key="dict.value" :label="dict.value">{{ dict.label }}</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="Position">
              <el-select v-model="form.postIds" multiple placeholder="Please select position">
                <el-option v-for="item in postOptions" :key="item.postId" :label="item.postName" :value="item.postId" :disabled="item.status == 1" ></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="Role">
              <el-select v-model="form.roleIds" multiple placeholder="Please select role">
                <el-option v-for="item in roleOptions" :key="item.roleId" :label="item.roleName" :value="item.roleId" :disabled="item.status == 1"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="Remark">
              <el-input v-model="form.remark" type="textarea" placeholder="Please enter content"></el-input>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">Confirm</el-button>
        <el-button @click="cancel">Cancel</el-button>
      </div>
    </el-dialog>

    <!-- 用户导入对话框 -->
    <!-- User Import Dialog -->
    <el-dialog :title="upload.title" :visible.sync="upload.open" width="400px" append-to-body>
      <el-upload ref="upload" :limit="1" accept=".xlsx, .xls" :headers="upload.headers" :action="upload.url + '?updateSupport=' + upload.updateSupport" :disabled="upload.isUploading" :on-progress="handleFileUploadProgress" :on-success="handleFileSuccess" :auto-upload="false" drag>
        <i class="el-icon-upload"></i>
        <div class="el-upload__text">Drag file here, or <em>click to upload</em></div>
        <div class="el-upload__tip text-center" slot="tip">
          <div class="el-upload__tip" slot="tip">
            <el-checkbox v-model="upload.updateSupport" />Whether to update existing user data
          </div>
          <span>Only xls, xlsx format files are allowed.</span>
          <el-link type="primary" :underline="false" style="font-size: 12px; vertical-align: baseline" @click="importTemplate">Download Template</el-link>
        </div>
      </el-upload>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitFileForm">Confirm</el-button>
        <el-button @click="upload.open = false">Cancel</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listUser, getUser, delUser, addUser, updateUser, resetUserPwd, changeUserStatus, deptTreeSelect } from "@/api/system/user";
import { getToken } from "@/utils/auth";
import Treeselect from "@riophae/vue-treeselect";
import "@riophae/vue-treeselect/dist/vue-treeselect.css";
import { Splitpanes, Pane } from "splitpanes";
import "splitpanes/dist/splitpanes.css";

export default {
  name: "User",
  dicts: ['sys_normal_disable', 'sys_user_sex'],
  components: { Treeselect, Splitpanes, Pane },
  data() {
    return {
      // 遮罩层
      // Loading mask
      loading: true,
      // 选中数组
      // Selected array
      ids: [],
      // 非单个禁用
      // Non-single disabled
      single: true,
      // 非多个禁用
      // Non-multiple disabled
      multiple: true,
      // 显示搜索条件
      // Show search condition
      showSearch: true,
      // 总条数
      // Total number
      total: 0,
      // 用户表格数据
      // User table data
      userList: null,
      // 弹出层标题
      // Popup title
      title: "",
      // 部门树选项
      // Department tree options
      deptOptions: undefined,
      // 部门树选项（启用状态）
      // Department tree options (enabled status)
      enabledDeptOptions: undefined,
      // 是否显示弹出层
      // Whether to show popup
      open: false,
      // 部门名称
      // Department name
      deptName: undefined,
      // 默认密码
      // Default password
      initPassword: undefined,
      // 日期范围
      // Date range
      dateRange: [],
      // 岗位选项
      // Position options
      postOptions: [],
      // 角色选项
      // Role options
      roleOptions: [],
      // 表单参数
      // Form parameters
      form: {},
      // 表单校验
      // Form validation
      defaultProps: {
        children: "children",
        label: "label"
      },
      // 用户导入参数
      // User import parameters
      upload: {
        // 是否显示弹出层
        // Whether to show popup
        open: false,
        // 弹出层标题
        // Popup title
        title: "",
        // 是否禁用上传
        // Whether to disable upload
        isUploading: false,
        // 是否更新已经存在的用户数据
        // Whether to update existing user data
        updateSupport: 0,
        // 设置上传的请求头部
        // Set upload request headers
        headers: { Authorization: "Bearer " + getToken() },
        // 上传的地址
        // Upload URL
        url: process.env.VUE_APP_BASE_API + "/system/user/importData"
      },
      // 查询参数
      // Query parameters
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        userName: undefined,
        phonenumber: undefined,
        status: undefined,
        deptId: undefined
      },
      // 列信息
      // Column information
      columns: [
        { key: 0, label: `User ID`, visible: true },
        { key: 1, label: `Username`, visible: true },
        { key: 2, label: `Nickname`, visible: true },
        { key: 3, label: `Department`, visible: true },
        { key: 4, label: `Phone Number`, visible: true },
        { key: 5, label: `Status`, visible: true },
        { key: 6, label: `Create Time`, visible: true }
      ],
      // 表单校验
      // Form validation
      rules: {
        userName: [
          { required: true, message: "Username cannot be empty", trigger: "blur" },
          { min: 2, max: 20, message: 'Username length must be between 2 and 20', trigger: 'blur' }
        ],
        nickName: [
          { required: true, message: "Nickname cannot be empty", trigger: "blur" }
        ],
        password: [
          { required: true, message: "Password cannot be empty", trigger: "blur" },
          { min: 5, max: 20, message: 'Password length must be between 5 and 20', trigger: 'blur' },
          { pattern: /^[^<>"'|\\]+$/, message: "Cannot contain illegal characters: < > \" ' \\\ |", trigger: "blur" }
        ],
        email: [
          {
            type: "email",
            message: "Please enter a valid email address",
            trigger: ["blur", "change"]
          }
        ],
        phonenumber: [
          {
            pattern: /^1[3|4|5|6|7|8|9][0-9]\d{8}$/,
            message: "Please enter a valid phone number",
            trigger: "blur"
          }
        ]
      }
    };
  },

  watch: {
    // 根据名称筛选部门树
    // Filter department tree by name
    deptName(val) {
      this.$refs.tree.filter(val);
    }
  },
  created() {
    this.getList();
    this.getDeptTree();
    this.getConfigKey("sys.user.initPassword").then(response => {
      this.initPassword = response.msg;
    });
  },
  methods: {
    /** 查询用户列表 */
    /** Query user list */
    getList() {
      this.loading = true;
      listUser(this.addDateRange(this.queryParams, this.dateRange)).then(response => {
          this.userList = response.rows;
          this.total = response.total;
          this.loading = false;
        }
      );
    },
    /** 查询部门下拉树结构 */
    /** Query department dropdown tree structure */
    getDeptTree() {
      deptTreeSelect().then(response => {
        this.deptOptions = response.data;
        this.enabledDeptOptions = this.filterDisabledDept(JSON.parse(JSON.stringify(response.data)));
      });
    },
    // 筛选部门树
    // Filter department tree
    filterDisabledDept(deptList) {
      return deptList.filter(dept => {
        if (dept.disabled) {
          return false;
        }
        if (dept.children && dept.children.length) {
          dept.children = this.filterDisabledDept(dept.children);
        }
        return true;
      });
    },
    // 筛选节点
    // Filter node
    filterNode(value, data) {
      if (!value) return true;
      return data.label.indexOf(value) !== -1;
    },
    // 节点单击事件
    // Node click event
    handleNodeClick(data) {
      this.queryParams.deptId = data.id;
      this.handleQuery();
    },
    // 用户状态修改
    // User status modification
    handleStatusChange(row) {
      let text = row.status === "0" ? "Enable" : "Disable";
      this.$modal.confirm('Are you sure you want to "' + text + '" user "' + row.userName + '"?').then(function() {
        return changeUserStatus(row.userId, row.status);
      }).then(() => {
        this.$modal.msgSuccess(text + " successful");
      }).catch(function() {
        row.status = row.status === "0" ? "1" : "0";
      });
    },
    // 取消按钮
    // Cancel button
    cancel() {
      this.open = false;
      this.reset();
    },
    // 表单重置
    // Form reset
    reset() {
      this.form = {
        userId: undefined,
        deptId: undefined,
        userName: undefined,
        nickName: undefined,
        password: undefined,
        phonenumber: undefined,
        email: undefined,
        sex: undefined,
        status: "0",
        remark: undefined,
        postIds: [],
        roleIds: []
      };
      this.resetForm("form");
    },
    /** 搜索按钮操作 */
    /** Search button operation */
    handleQuery() {
      this.queryParams.pageNum = 1;
      this.getList();
    },
    /** 重置按钮操作 */
    /** Reset button operation */
    resetQuery() {
      this.dateRange = [];
      this.resetForm("queryForm");
      this.queryParams.deptId = undefined;
      this.$refs.tree.setCurrentKey(null);
      this.handleQuery();
    },
    // 多选框选中数据
    // Multiple selection data
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.userId);
      this.single = selection.length != 1;
      this.multiple = !selection.length;
    },
    // 更多操作触发
    // More operations trigger
    handleCommand(command, row) {
      switch (command) {
        case "handleResetPwd":
          this.handleResetPwd(row);
          break;
        case "handleAuthRole":
          this.handleAuthRole(row);
          break;
        default:
          break;
      }
    },
    /** 新增按钮操作 */
    /** Add button operation */
    handleAdd() {
      this.reset();
      getUser().then(response => {
        this.postOptions = response.posts;
        this.roleOptions = response.roles;
        this.open = true;
        this.title = "Add User";
        this.form.password = this.initPassword;
      });
    },
    /** 修改按钮操作 */
    /** Edit button operation */
    handleUpdate(row) {
      this.reset();
      const userId = row.userId || this.ids;
      getUser(userId).then(response => {
        this.form = response.data;
        this.postOptions = response.posts;
        this.roleOptions = response.roles;
        this.$set(this.form, "postIds", response.postIds);
        this.$set(this.form, "roleIds", response.roleIds);
        this.open = true;
        this.title = "Modify User";
        this.form.password = "";
      });
    },
    /** 重置密码按钮操作 */
    /** Reset password button operation */
    handleResetPwd(row) {
      this.$prompt('Please enter the new password for "' + row.userName + '"', "Prompt", {
        confirmButtonText: "Confirm",
        cancelButtonText: "Cancel",
        closeOnClickModal: false,
        inputPattern: /^.{5,20}$/,
        inputErrorMessage: "Password length must be between 5 and 20",
        inputValidator: (value) => {
          if (/<|>|"|'|\||\\/.test(value)) {
            return "Cannot contain illegal characters: < > \" ' \\\ |"
          }
        },
      }).then(({ value }) => {
          resetUserPwd(row.userId, value).then(response => {
            this.$modal.msgSuccess("Modification successful, new password is: " + value);
          });
        }).catch(() => {});
    },
    /** 分配角色操作 */
    /** Assign role operation */
    handleAuthRole: function(row) {
      const userId = row.userId;
      this.$router.push("/system/user-auth/role/" + userId);
    },
    /** 提交按钮 */
    /** Submit button */
    submitForm: function() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.userId != undefined) {
            updateUser(this.form).then(response => {
              this.$modal.msgSuccess("Modification successful");
              this.open = false;
              this.getList();
            });
          } else {
            addUser(this.form).then(response => {
              this.$modal.msgSuccess("Addition successful");
              this.open = false;
              this.getList();
            });
          }
        }
      });
    },
    /** 删除按钮操作 */
    /** Delete button operation */
    handleDelete(row) {
      const userIds = row.userId || this.ids;
      this.$modal.confirm('Are you sure you want to delete the data item with User ID "' + userIds + '"?').then(function() {
        return delUser(userIds);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("Deletion successful");
      }).catch(() => {});
    },
    /** 导出按钮操作 */
    /** Export button operation */
    handleExport() {
      this.download('system/user/export', {
        ...this.queryParams
      }, `user_${new Date().getTime()}.xlsx`)
    },
    /** 导入按钮操作 */
    /** Import button operation */
    handleImport() {
      this.upload.title = "User Import";
      this.upload.open = true;
    },
    /** 下载模板操作 */
    /** Download template operation */
    importTemplate() {
      this.download('system/user/importTemplate', {
      }, `user_template_${new Date().getTime()}.xlsx`)
    },
    // 文件上传中处理
    // File upload progress handling
    handleFileUploadProgress(event, file, fileList) {
      this.upload.isUploading = true;
    },
    // 文件上传成功处理
    // File upload success handling
    handleFileSuccess(response, file, fileList) {
      this.upload.open = false;
      this.upload.isUploading = false;
      this.$refs.upload.clearFiles();
      this.$alert("<div style='overflow: auto;overflow-x: hidden;max-height: 70vh;padding: 10px 20px 0;'>" + response.msg + "</div>", "Import Result", { dangerouslyUseHTMLString: true });
      this.getList();
    },
    // 提交上传文件
    // Submit upload file
    submitFileForm() {
      this.$refs.upload.submit();
    }
  }
};
</script>
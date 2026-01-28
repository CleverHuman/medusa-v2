<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch">
      <el-form-item label="Order ID" prop="orderId">
        <el-input
          v-model="queryParams.orderId"
          placeholder="Please enter order ID"
          clearable
          style="width: 240px"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="User ID" prop="userId">
        <el-input
          v-model="queryParams.userId"
          placeholder="Please enter user ID"
          clearable
          style="width: 240px"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="Key ID" prop="keyId">
        <el-input
          v-model="queryParams.keyId"
          placeholder="Please enter key ID"
          clearable
          style="width: 240px"
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
          type="primary"
          icon="el-icon-plus"
          size="mini"
          @click="handleAdd"
          v-hasPermi="['mall:pgpEncryptedAddress:add']"
        >Add</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          icon="el-icon-lock"
          size="mini"
          @click="handleEncrypt"
          v-hasPermi="['mall:pgpEncryptedAddress:encrypt']"
        >Encrypt Address</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          icon="el-icon-unlock"
          size="mini"
          @click="handleDecrypt"
          v-hasPermi="['mall:pgpEncryptedAddress:decrypt']"
        >Decrypt Address</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          icon="el-icon-delete"
          size="mini"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['mall:pgpEncryptedAddress:remove']"
        >Delete</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="info"
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['mall:pgpEncryptedAddress:export']"
        >Export</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="pgpEncryptedAddressList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="Order ID" align="center" prop="orderId" width="180" />
      <el-table-column label="User ID" align="center" prop="userId" />
      <el-table-column label="Key ID" align="center" prop="keyId" width="180" />
      <el-table-column label="Encrypted Address" align="center" prop="encryptedAddress" width="300">
        <template slot-scope="scope">
          <el-tooltip :content="scope.row.encryptedAddress" placement="top">
            <span>{{ scope.row.encryptedAddress ? scope.row.encryptedAddress.substring(0, 50) + '...' : '' }}</span>
          </el-tooltip>
        </template>
      </el-table-column>
      <el-table-column label="Encrypt Time" align="center" prop="encryptTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.encryptTime) }}</span>
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
            icon="el-icon-view"
            @click="handleView(scope.row)"
            v-hasPermi="['mall:pgpEncryptedAddress:query']"
          >View</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['mall:pgpEncryptedAddress:edit']"
          >Edit</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['mall:pgpEncryptedAddress:remove']"
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

    <!-- 添加或修改PGP加密地址对话框 -->
    <!-- Add or Modify PGP Encrypted Address Dialog -->
    <el-dialog :title="title" :visible.sync="open" width="800px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="120px">
        <el-form-item label="Order ID" prop="orderId">
          <el-input v-model="form.orderId" placeholder="Please enter order ID" />
        </el-form-item>
        <el-form-item label="User ID" prop="userId">
          <el-input v-model="form.userId" placeholder="Please enter user ID" />
        </el-form-item>
        <el-form-item label="Key ID" prop="keyId">
          <el-input v-model="form.keyId" placeholder="Please enter key ID" />
        </el-form-item>
        <el-form-item label="Encrypted Address" prop="encryptedAddress">
          <el-input
            v-model="form.encryptedAddress"
            type="textarea"
            :rows="6"
            placeholder="Please enter encrypted address"
          />
        </el-form-item>
        <el-form-item label="Remark" prop="remark">
          <el-input v-model="form.remark" type="textarea" placeholder="Please enter remark" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">Confirm</el-button>
        <el-button @click="cancel">Cancel</el-button>
      </div>
    </el-dialog>

    <!-- 加密地址对话框 -->
    <!-- Encrypt Address Dialog -->
    <el-dialog title="Encrypt Address" :visible.sync="encryptOpen" width="600px" append-to-body>
      <el-form ref="encryptForm" :model="encryptForm" :rules="encryptRules" label-width="120px">
        <el-form-item label="Order ID" prop="orderId">
          <el-input v-model="encryptForm.orderId" placeholder="Please enter order ID" />
        </el-form-item>
        <el-form-item label="User ID" prop="userId">
          <el-input v-model="encryptForm.userId" placeholder="Please enter user ID" />
        </el-form-item>
        <el-form-item label="Address" prop="address">
          <el-input
            v-model="encryptForm.address"
            type="textarea"
            :rows="4"
            placeholder="Please enter address to encrypt"
          />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitEncrypt">Confirm</el-button>
        <el-button @click="cancelEncrypt">Cancel</el-button>
      </div>
    </el-dialog>

    <!-- 解密地址对话框 -->
    <!-- Decrypt Address Dialog -->
    <el-dialog title="Decrypt Address" :visible.sync="decryptOpen" width="600px" append-to-body>
      <el-form ref="decryptForm" :model="decryptForm" :rules="decryptRules" label-width="120px">
        <el-form-item label="Encrypted Address" prop="encryptedAddress">
          <el-input
            v-model="decryptForm.encryptedAddress"
            type="textarea"
            :rows="6"
            placeholder="Please enter address to decrypt"
          />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitDecrypt">Confirm</el-button>
        <el-button @click="cancelDecrypt">Cancel</el-button>
      </div>
    </el-dialog>

    <!-- 解密结果对话框 -->
    <!-- Decrypt Result Dialog -->
    <el-dialog title="Decrypt Result" :visible.sync="decryptResultOpen" width="500px" append-to-body>
      <div>
        <p><strong>Decrypted Address:</strong></p>
        <el-input
          v-model="decryptResult"
          type="textarea"
          :rows="4"
          readonly
        />
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button @click="decryptResultOpen = false">Close</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listPgpEncryptedAddress, getPgpEncryptedAddress, addPgpEncryptedAddress, updatePgpEncryptedAddress, delPgpEncryptedAddress, exportPgpEncryptedAddress, encryptAddress, decryptAddress } from "@/api/mall/pgpEncryptedAddress";

export default {
  name: "PgpEncryptedAddress",
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
      // PGP加密地址表格数据
      // PGP encrypted address table data
      pgpEncryptedAddressList: [],
      // 弹出层标题
      // Popup title
      title: "",
      // 是否显示弹出层
      // Whether to show popup
      open: false,
      // 是否显示加密弹出层
      // Whether to show encrypt popup
      encryptOpen: false,
      // 是否显示解密弹出层
      // Whether to show decrypt popup
      decryptOpen: false,
      // 是否显示解密结果弹出层
      // Whether to show decrypt result popup
      decryptResultOpen: false,
      // 查询参数
      // Query parameters
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        orderId: undefined,
        userId: undefined,
        keyId: undefined
      },
      // 表单参数
      // Form parameters
      form: {},
      // 加密表单参数
      // Encrypt form parameters
      encryptForm: {
        orderId: "",
        userId: "",
        address: ""
      },
      // 解密表单参数
      // Decrypt form parameters
      decryptForm: {
        encryptedAddress: ""
      },
      // 解密结果
      // Decrypt result
      decryptResult: "",
      // 表单校验
      // Form validation
      rules: {
        orderId: [
          { required: true, message: "Order ID cannot be empty", trigger: "blur" }
        ],
        userId: [
          { required: true, message: "User ID cannot be empty", trigger: "blur" }
        ],
        keyId: [
          { required: true, message: "Key ID cannot be empty", trigger: "blur" }
        ]
      },
      // 加密表单校验
      // Encrypt form validation
      encryptRules: {
        orderId: [
          { required: true, message: "Order ID cannot be empty", trigger: "blur" }
        ],
        userId: [
          { required: true, message: "User ID cannot be empty", trigger: "blur" }
        ],
        address: [
          { required: true, message: "Address cannot be empty", trigger: "blur" }
        ]
      },
      // 解密表单校验
      // Decrypt form validation
      decryptRules: {
        encryptedAddress: [
          { required: true, message: "Encrypted address cannot be empty", trigger: "blur" }
        ]
      }
    };
  },
  created() {
    this.getList();
  },
  methods: {
    /** 查询PGP加密地址列表 */
    /** Query PGP encrypted address list */
    getList() {
      this.loading = true;
      listPgpEncryptedAddress(this.queryParams).then(response => {
        this.pgpEncryptedAddressList = response.rows;
        this.total = response.total;
        this.loading = false;
      });
    },
    // 取消按钮
    // Cancel button
    cancel() {
      this.open = false;
      this.reset();
    },
    // 取消加密按钮
    // Cancel encrypt button
    cancelEncrypt() {
      this.encryptOpen = false;
      this.resetEncrypt();
    },
    // 取消解密按钮
    // Cancel decrypt button
    cancelDecrypt() {
      this.decryptOpen = false;
      this.resetDecrypt();
    },
    // 表单重置
    // Form reset
    reset() {
      this.form = {
        id: undefined,
        orderId: undefined,
        userId: undefined,
        keyId: undefined,
        encryptedAddress: undefined,
        encryptTime: undefined,
        remark: undefined
      };
      this.resetForm("form");
    },
    // 加密表单重置
    // Encrypt form reset
    resetEncrypt() {
      this.encryptForm = {
        orderId: "",
        userId: "",
        address: ""
      };
      this.resetForm("encryptForm");
    },
    // 解密表单重置
    // Decrypt form reset
    resetDecrypt() {
      this.decryptForm = {
        encryptedAddress: ""
      };
      this.resetForm("decryptForm");
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
      this.resetForm("queryForm");
      this.handleQuery();
    },
    // 多选框选中数据
    // Multiple selection data
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.id)
      this.single = selection.length!==1
      this.multiple = !selection.length
    },
    /** 新增按钮操作 */
    /** Add button operation */
    handleAdd() {
      this.reset();
      this.open = true;
      this.title = "Add PGP Encrypted Address";
    },
    /** 修改按钮操作 */
    /** Edit button operation */
    handleUpdate(row) {
      this.reset();
      const id = row.id || this.ids
      getPgpEncryptedAddress(id).then(response => {
        this.form = response.data;
        this.open = true;
        this.title = "Modify PGP Encrypted Address";
      });
    },
    /** 查看按钮操作 */
    /** View button operation */
    handleView(row) {
      this.reset();
      const id = row.id || this.ids
      getPgpEncryptedAddress(id).then(response => {
        this.form = response.data;
        this.open = true;
        this.title = "View PGP Encrypted Address";
      });
    },
    /** 加密按钮操作 */
    /** Encrypt button operation */
    handleEncrypt() {
      this.resetEncrypt();
      this.encryptOpen = true;
    },
    /** 解密按钮操作 */
    /** Decrypt button operation */
    handleDecrypt() {
      this.resetDecrypt();
      this.decryptOpen = true;
    },
    /** 提交加密表单 */
    /** Submit encrypt form */
    submitEncrypt() {
      this.$refs["encryptForm"].validate(valid => {
        if (valid) {
          encryptAddress(this.encryptForm.orderId, this.encryptForm.userId, this.encryptForm.address).then(response => {
            this.$modal.msgSuccess("Encryption successful");
            this.encryptOpen = false;
            this.getList();
          });
        }
      });
    },
    /** 提交解密表单 */
    /** Submit decrypt form */
    submitDecrypt() {
      this.$refs["decryptForm"].validate(valid => {
        if (valid) {
          decryptAddress(this.decryptForm.encryptedAddress).then(response => {
            this.decryptResult = response.data;
            this.decryptOpen = false;
            this.decryptResultOpen = true;
          });
        }
      });
    },
    /** 提交按钮 */
    /** Submit button */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.id != null) {
            updatePgpEncryptedAddress(this.form).then(response => {
              this.$modal.msgSuccess("Modification successful");
              this.open = false;
              this.getList();
            });
          } else {
            addPgpEncryptedAddress(this.form).then(response => {
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
      const ids = row.id || this.ids;
      this.$modal.confirm('Are you sure you want to delete the PGP encrypted address with ID "' + ids + '"?').then(function() {
        return delPgpEncryptedAddress(ids);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("Deletion successful");
      }).catch(() => {});
    },
    /** 导出按钮操作 */
    /** Export button operation */
    handleExport() {
      this.$modal.confirm('Are you sure you want to export all PGP encrypted address data?').then(() => {
        this.$modal.loading("Exporting data, please wait...");
        exportPgpEncryptedAddress(this.queryParams);
      }).then(response => {
        this.download(response.msg);
        this.$modal.closeLoading();
      }).catch(() => {});
    }
  }
};
</script> 
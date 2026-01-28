<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch">
      <el-form-item label="Key Name" prop="keyName">
        <el-input
          v-model="queryParams.keyName"
          placeholder="Please enter key name"
          clearable
          style="width: 240px"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="Key Type" prop="keyType">
        <el-select
          v-model="queryParams.keyType"
          placeholder="Please select key type"
          clearable
          style="width: 240px"
        >
          <el-option label="Public Key" value="public" />
          <el-option label="Private Key" value="private" />
        </el-select>
      </el-form-item>
      <el-form-item label="Key Status" prop="isActive">
        <el-select
          v-model="queryParams.isActive"
          placeholder="Please select key status"
          clearable
          style="width: 240px"
        >
          <el-option label="Active" :value="1" />
          <el-option label="Inactive" :value="0" />
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
          icon="el-icon-plus"
          size="mini"
          @click="handleAdd"
          v-hasPermi="['mall:pgpKey:add']"
        >Add</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          icon="el-icon-magic-stick"
          size="mini"
          @click="handleGenerate"
          v-hasPermi="['mall:pgpKey:generate']"
        >Generate Key</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          icon="el-icon-delete"
          size="mini"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['mall:pgpKey:remove']"
        >Delete</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['mall:pgpKey:export']"
        >Export</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="pgpKeyList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="Key Name" align="center" prop="keyName" />
      <el-table-column label="Key Type" align="center" prop="keyType">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.keyType === 'public'" type="success">Public Key</el-tag>
          <el-tag v-else-if="scope.row.keyType === 'private'" type="danger">Private Key</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="Key ID" align="center" prop="keyId" width="180" />
      <el-table-column label="Key Size" align="center" prop="keySize" />
      <el-table-column label="Algorithm" align="center" prop="algorithm" />
      <el-table-column label="Status" align="center" prop="isActive">
        <template slot-scope="scope">
          <el-switch
            v-model="scope.row.isActive"
            :active-value="1"
            :inactive-value="0"
            @change="handleStatusChange(scope.row)"
          ></el-switch>
        </template>
      </el-table-column>
      <el-table-column label="Default Key" align="center" prop="isDefault">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.isDefault === 1" type="primary">Default</el-tag>
          <el-button
            v-else
            size="mini"
            type="text"
            @click="handleSetDefault(scope.row)"
            v-hasPermi="['mall:pgpKey:setDefault']"
          >Set as Default</el-button>
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
            v-hasPermi="['mall:pgpKey:query']"
          >View</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['mall:pgpKey:edit']"
          >Edit</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['mall:pgpKey:remove']"
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

    <!-- Add or Modify PGP Key Dialog -->
    <el-dialog :title="title" :visible.sync="open" width="800px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="120px">
        <el-form-item label="Key Name" prop="keyName">
          <el-input v-model="form.keyName" placeholder="Please enter key name" />
        </el-form-item>
        <el-form-item label="Key Type" prop="keyType">
          <el-select v-model="form.keyType" placeholder="Please select key type">
            <el-option label="Public Key" value="public" />
            <el-option label="Private Key" value="private" />
          </el-select>
        </el-form-item>
        <el-form-item label="Key ID" prop="keyId">
          <el-input v-model="form.keyId" placeholder="Please enter key ID" />
        </el-form-item>
        <el-form-item label="Key Fingerprint" prop="fingerprint">
          <el-input v-model="form.fingerprint" placeholder="Please enter key fingerprint" />
        </el-form-item>
        <el-form-item label="Key Size" prop="keySize">
          <el-input-number v-model="form.keySize" :min="1024" :max="4096" :step="512" />
        </el-form-item>
        <el-form-item label="Algorithm" prop="algorithm">
          <el-input v-model="form.algorithm" placeholder="Please enter algorithm" />
        </el-form-item>
        <el-form-item label="Key Data" prop="keyData">
          <el-input
            v-model="form.keyData"
            type="textarea"
            :rows="8"
            placeholder="Please enter key data"
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

    <!-- Generate Key Dialog -->
    <el-dialog title="Generate PGP Key Pair" :visible.sync="generateOpen" width="500px" append-to-body>
      <el-form ref="generateForm" :model="generateForm" :rules="generateRules" label-width="120px">
        <el-form-item label="Key Name" prop="keyName">
          <el-input v-model="generateForm.keyName" placeholder="Please enter key name" />
        </el-form-item>
        <el-form-item label="Key Size" prop="keySize">
          <el-select v-model="generateForm.keySize" placeholder="Please select key size">
            <el-option label="1024 bits" :value="1024" />
            <el-option label="2048 bits" :value="2048" />
            <el-option label="4096 bits" :value="4096" />
          </el-select>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitGenerate">Confirm</el-button>
        <el-button @click="cancelGenerate">Cancel</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listPgpKey, getPgpKey, addPgpKey, updatePgpKey, delPgpKey, exportPgpKey, generatePgpKey, setDefaultPgpKey, togglePgpKeyStatus } from "@/api/mall/pgpKey";

export default {
  name: "PgpKey",
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
      // PGP key table data
      pgpKeyList: [],
      // Popup title
      title: "",
      // Whether to show popup
      open: false,
      // Whether to show generate popup
      generateOpen: false,
      // Query parameters
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        keyName: undefined,
        keyType: undefined,
        isActive: undefined
      },
      // Form parameters
      form: {},
      // Generate form parameters
      generateForm: {
        keyName: "",
        keySize: 2048
      },
      // Form validation
      rules: {
        keyName: [
          { required: true, message: "Key name cannot be empty", trigger: "blur" }
        ],
        keyType: [
          { required: true, message: "Key type cannot be empty", trigger: "change" }
        ],
        keyId: [
          { required: true, message: "Key ID cannot be empty", trigger: "blur" }
        ]
      },
      // Generate form validation
      generateRules: {
        keyName: [
          { required: true, message: "Key name cannot be empty", trigger: "blur" }
        ],
        keySize: [
          { required: true, message: "Key size cannot be empty", trigger: "change" }
        ]
      }
    };
  },
  created() {
    this.getList();
  },
  methods: {
    /** Query PGP key list */
    getList() {
      this.loading = true;
      listPgpKey(this.queryParams).then(response => {
        this.pgpKeyList = response.rows;
        this.total = response.total;
        this.loading = false;
      });
    },
    // Cancel button
    cancel() {
      this.open = false;
      this.reset();
    },
    // Cancel generate button
    cancelGenerate() {
      this.generateOpen = false;
      this.resetGenerate();
    },
    // Form reset
    reset() {
      this.form = {
        id: undefined,
        keyName: undefined,
        keyType: undefined,
        keyId: undefined,
        fingerprint: undefined,
        keyData: undefined,
        keySize: 2048,
        algorithm: "RSA",
        isActive: 1,
        isDefault: 0,
        remark: undefined
      };
      this.resetForm("form");
    },
    // Generate form reset
    resetGenerate() {
      this.generateForm = {
        keyName: "",
        keySize: 2048
      };
      this.resetForm("generateForm");
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
    // Multiple selection data
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.id)
      this.single = selection.length!==1
      this.multiple = !selection.length
    },
    /** Add button operation */
    handleAdd() {
      this.reset();
      this.open = true;
      this.title = "Add PGP Key";
    },
    /** Edit button operation */
    handleUpdate(row) {
      this.reset();
      const id = row.id || this.ids
      getPgpKey(id).then(response => {
        this.form = response.data;
        this.open = true;
        this.title = "Edit PGP Key";
      });
    },
    /** View button operation */
    handleView(row) {
      this.reset();
      const id = row.id || this.ids
      getPgpKey(id).then(response => {
        this.form = response.data;
        this.open = true;
        this.title = "View PGP Key";
      });
    },
    /** Generate button operation */
    handleGenerate() {
      this.resetGenerate();
      this.generateOpen = true;
    },
    /** Submit generate form */
    submitGenerate() {
      this.$refs["generateForm"].validate(valid => {
        if (valid) {
          generatePgpKey(this.generateForm.keyName, this.generateForm.keySize).then(response => {
            this.$modal.msgSuccess("Generation successful");
            this.generateOpen = false;
            this.getList();
          });
        }
      });
    },
    /** Submit button */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.id != null) {
            updatePgpKey(this.form).then(response => {
              this.$modal.msgSuccess("Modification successful");
              this.open = false;
              this.getList();
            });
          } else {
            addPgpKey(this.form).then(response => {
              this.$modal.msgSuccess("Addition successful");
              this.open = false;
              this.getList();
            });
          }
        }
      });
    },
    /** Delete button operation */
    handleDelete(row) {
      const ids = row.id || this.ids;
      this.$modal.confirm('Are you sure to delete the PGP key with ID "' + ids + '"?').then(function() {
        return delPgpKey(ids);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("Deletion successful");
      }).catch(() => {});
    },
    /** Export button operation */
    handleExport() {
      this.$modal.confirm('Are you sure to export all PGP key data?').then(() => {
        this.$modal.loading("Exporting data, please wait...");
        exportPgpKey(this.queryParams);
      }).then(response => {
        this.download(response.msg);
        this.$modal.closeLoading();
      }).catch(() => {});
    },
    /** Set default key */
    handleSetDefault(row) {
      this.$modal.confirm('Are you sure to set "' + row.keyName + '" as the default key?').then(function() {
        return setDefaultPgpKey(row.id);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("Setting successful");
      }).catch(() => {});
    },
    /** Status modification */
    handleStatusChange(row) {
      let text = row.isActive === 1 ? "Enable" : "Disable";
      this.$modal.confirm('Are you sure you want to "' + text + '" key "' + row.keyName + '"?').then(function() {
        return togglePgpKeyStatus(row.id, row.isActive);
      }).then(() => {
        this.$modal.msgSuccess(text + " successful");
      }).catch(function() {
        row.isActive = row.isActive === 1 ? 0 : 1;
      });
    }
  }
};
</script> 
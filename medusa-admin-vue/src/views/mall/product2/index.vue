<template>
  <div class="app-container">
    <!-- Product Info Banner -->
    <el-alert
      v-if="queryParams.productId"
      :title="`Managing SKUs for Product ID: ${queryParams.productId}`"
      type="info"
      :closable="false"
      show-icon
      style="margin-bottom: 20px;">
      <template slot="default">
        <el-button 
          type="text" 
          size="small" 
          @click="goBackToProducts"
          style="margin-left: 10px;">
          ← Back to Products
        </el-button>
      </template>
    </el-alert>

    <el-form :model="queryParams" ref="queryForm" :inline="true" v-show="showSearch" label-width="100px">
      <el-form-item label="Product Name" prop="name">
        <el-input
          v-model="queryParams.name"
          placeholder="Please enter product name"
          clearable
          size="small"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="Status" prop="status">
        <el-select v-model="queryParams.status" placeholder="Product status" clearable size="small">
          <el-option
            v-for="dict in dict.type.product_status.filter(item => item.value !== '2')"
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
          v-hasPermi="['mall:product2:add']"
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
          v-hasPermi="['mall:product2:edit']"
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
          v-hasPermi="['mall:product2:remove']"
        >Delete</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="productList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="Product ID" align="center" prop="productId" />
      <el-table-column label="Product Name" align="center" prop="name" />

      <el-table-column label="ProCode" align="center" prop="productId" />
      <el-table-column label="Amount" align="center" prop="model">
        <template slot-scope="scope">
          {{ parseFloat(scope.row.model).toFixed(2) }}
        </template>
      </el-table-column>
      <el-table-column label="Price" align="center" prop="price" />
      <el-table-column label="Inventory" align="center" prop="inventory" />
      <el-table-column label="Status" align="center" prop="status">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.product_status" :value="scope.row.status"/>
        </template>
      </el-table-column>
      <el-table-column label="Image" align="center" prop="imageUrl" width="100">
        <template slot-scope="scope">
          <el-image 
            style="width: 50px; height: 50px"
            :src="scope.row.imageUrl"
            :preview-src-list="[scope.row.imageUrl]">
          </el-image>
        </template>
      </el-table-column>
      <el-table-column label="Description" align="center" prop="des" :show-overflow-tooltip="true" />
      <el-table-column label="Create Time" align="center" prop="createTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="Edit" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['mall:product2:edit']"
          >Edit</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['mall:product2:remove']"
          >Mark as Deleted</el-button>
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

    <!-- Add or Modify Product Dialog -->
    <el-dialog :title="title" :visible.sync="open" width="700px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="Product Name" prop="name">
          <el-input v-model="form.name" placeholder="Please enter product name" />
        </el-form-item>
        <el-form-item label="ProCode" prop="productId">
          <el-input v-model="form.productId" placeholder="Please enter ProCode" />
        </el-form-item>
        <el-form-item label="Amount" prop="model">
          <el-input-number 
            v-model="form.model" 
            :precision="2" 
            :step="0.25" 
            :min="0" 
            placeholder="Please enter amount (e.g., 0.25, 0.5, 100.00)" 
            style="width: 60%; display: inline-block; margin-right: 8px;" 
          />
        </el-form-item>
        <el-form-item label="Unit" prop="unit" style="display: inline-block; width: 35%; margin-left: 0;">
          <el-select v-model="form.unit" placeholder="Select unit">
            <el-option label="g" value="g" />
            <el-option label="ml" value="ml" />
            <el-option label="pills" value="pills" />
            <el-option label="ea" value="ea" />
          </el-select>
        </el-form-item>
        <el-form-item label="SKU" prop="sku">
          <el-input v-model="form.sku" placeholder="Auto-generated SKU" disabled>
            <template slot="prepend">SKU</template>
          </el-input>
        </el-form-item>
        <el-form-item label="Price" prop="price">
          <el-input-number v-model="form.price" :precision="2" :step="0.1" :min="0" />
        </el-form-item>
        <el-form-item label="Inventory" prop="inventory">
          <el-input-number v-model="form.inventory" :min="0" />
        </el-form-item>
        <el-form-item label="Status" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio
              v-for="dict in dict.type.product_status.filter(item => item.value !== '2')"
              :key="dict.value"
              :label="dict.value"
            >{{dict.label}}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="Image URL" prop="imageUrl">
          <el-input v-model="form.imageUrl" placeholder="Please enter image URL">
            <template slot="append">
              <el-button type="primary" @click="previewImage">Preview</el-button>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="Description" prop="des">
          <el-input v-model="form.des" type="textarea" :rows="4" placeholder="Please enter product description" />
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

    <!-- Image Preview Dialog -->
    <el-dialog title="Image Preview" :visible.sync="previewVisible" width="500px" append-to-body>
      <el-image 
        style="width: 100%"
        :src="previewUrl"
        fit="contain">
      </el-image>
    </el-dialog>
  </div>
</template>

<script>
import { listProduct2, getProduct2, delProduct2, addProduct2, updateProduct2 } from "@/api/mall/product2";

export default {
  name: "Product2",
  dicts: ['product_status'],
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
      // Show search conditions
      showSearch: true,
      // Total number
      total: 0,
      // Product table data
      productList: [],
      // Popup title
      title: "",
      // Whether to show popup
      open: false,
      // Image preview
      previewVisible: false,
      previewUrl: "",
      // Query parameters
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        name: null,
        category: null,
        status: null,  // Remove default status filter to show both normal and disabled products
        productId: null  // Add productId for filtering SKUs by product
      },
      // Form parameters
      form: {
        category: 'Fruit',  // Set default value for category
        model: null,
        unit: 'g', // 新增字段，默认g
        sku: null,
        price: null,
        inventory: null,
        status: "0",
        imageUrl: null,
        des: null,
        remark: null
      },
      // Form validation
      rules: {
        name: [
          { required: true, message: "Product name cannot be empty", trigger: "blur" }
        ],
        productId: [
          { required: true, message: "ProCode cannot be empty", trigger: "blur" }
        ],
        model: [
          { required: true, message: "Amount cannot be empty", trigger: "blur" },
          { type: 'number', min: 0, message: "Amount must be greater than 0", trigger: "blur" }
        ],
        price: [
          { required: true, message: "Price cannot be empty", trigger: "blur" }
        ],
        inventory: [
          { required: true, message: "Inventory cannot be empty", trigger: "blur" }
        ],
        imageUrl: [
          { required: true, message: "Image URL cannot be empty", trigger: "blur" }
        ],
        des: [
          { required: true, message: "Description cannot be empty", trigger: "blur" }
        ],
        unit: [
          { required: true, message: "Unit cannot be empty", trigger: "change" }
        ]
      }
    };
  },
  created() {
    // Get productId from route query parameters
    const productId = this.$route.query.productId;
    if (productId) {
      this.queryParams.productId = productId;
      // Add a breadcrumb or title to show which product's SKUs are being managed
      this.$message.info(`Managing SKUs for Product ID: ${productId}`);
    }
    this.getList();
  },
  methods: {
    /** Query product list */
    getList() {
      this.loading = true;
      listProduct2(this.queryParams).then(response => {
        this.productList = response.rows;
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
      const currentProductId = this.queryParams.productId; // Keep the current productId
      this.form = {
        productId: currentProductId, // Use the current productId
        name: null,
        category: 'Fruit',  // Keep default value when resetting
        model: null,
        unit: 'g', // 新增字段，默认g
        sku: null,
        price: null,
        inventory: null,
        status: "0",
        imageUrl: null,
        des: null,
        remark: null
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
      this.title = "Add Product";
    },
    /** Edit button operation */
    handleUpdate(row) {
      this.reset();
      const id = row.id || this.ids
      getProduct2(id).then(response => {
        console.log('Response data:', response.data);
        this.form = response.data;
        this.form.category = 'Fruit';
        this.form.status = String(this.form.status);
        console.log('Form status after conversion:', this.form.status);
        this.open = true;
        this.title = "Edit Product";
      });
    },
    /** Preview image */
    previewImage() {
      if (this.form.imageUrl) {
        this.previewUrl = this.form.imageUrl;
        this.previewVisible = true;
      } else {
        this.$modal.msgError("Please enter image URL first");
      }
    },
    /** Submit button */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          // Generate SKU by combining SKU, productId and Amount
          if (!this.form.productId || !this.form.model) {
            this.$modal.msgError("ProCode and Amount are required to generate SKU");
            return;
          }
          this.form.sku = `SKU-${this.form.productId}-${this.form.model}`;
          
          if (this.form.id != null) {
            updateProduct2(this.form).then(response => {
              this.$modal.msgSuccess("Edit successful");
              this.open = false;
              this.getList();
            });
          } else {
            addProduct2(this.form).then(response => {
              this.$modal.msgSuccess("Add successful");
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
      this.$modal.confirm('Are you sure to mark the product with ID "' + ids + '" as deleted?').then(function() {
        return updateProduct2({id: ids, status: "2"});
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("Product marked as deleted successfully");
      }).catch(() => {});
    },
    /** Back to products page */
    goBackToProducts() {
      this.$router.push({ path: '/mall/product' });
    }
  }
};
</script> 
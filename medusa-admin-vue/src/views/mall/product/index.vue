<template>
    <div class="app-container">
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
        <el-form-item label="Category" prop="category">
          <el-select v-model="queryParams.category" placeholder="Please select product category" clearable size="small">
            <el-option label="Digital Product" value="Digital Product" />
            <el-option label="Physical Product" value="Physical Product" />
          </el-select>
        </el-form-item>
        <el-form-item label="Channel" prop="channel">
          <el-select v-model="queryParams.channel" placeholder="Sales channel" clearable size="small">
            <el-option
              v-for="dict in dict.type.channel_type"
              :key="dict.value"
              :label="dict.label"
              :value="dict.value"
            />
          </el-select>
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
            v-hasPermi="['mall:product:add']"
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
            v-hasPermi="['mall:product:edit']"
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
            v-hasPermi="['mall:product:remove']"
          >Delete</el-button>
        </el-col>
        <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
      </el-row>
  
      <el-table v-loading="loading" :data="productList" @selection-change="handleSelectionChange" row-key="id" ref="productTable">
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column label="Sort" width="50" align="center">
          <template slot-scope="scope">
            <i class="el-icon-rank drag-handle" style="cursor: move; color: #999;"></i>
          </template>
        </el-table-column>
        <el-table-column label="ID" align="center" prop="id" width="80" />
        <el-table-column label="Product ID" align="center" prop="productId" width="120" />
        <el-table-column label="Product Name" align="center" prop="name" />
        <el-table-column label="Category" align="center" prop="category" width="120">
          <template slot-scope="scope">
            <el-tag :type="scope.row.category === 'Digital Product' ? 'success' : 'warning'">
              {{ scope.row.category }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="Channel" align="center" prop="channel" width="100">
          <template slot-scope="scope">
            <dict-tag :options="dict.type.channel_type" :value="scope.row.channel"/>
          </template>
        </el-table-column>
        <el-table-column label="Status" align="center" prop="status" width="100">
          <template slot-scope="scope">
            <dict-tag :options="dict.type.product_status" :value="scope.row.status"/>
          </template>
        </el-table-column>
        <el-table-column label="Image" align="center" prop="imageUrl" width="100">
          <template slot-scope="scope">
            <el-image 
              style="width: 50px; height: 50px"
              :src="scope.row.imageUrl"
              :preview-src-list="[scope.row.imageUrl]"
              lazy
              fit="cover"
            >
              <!-- 加载中的占位内容 -->
              <div slot="placeholder" class="image-slot">
                <i class="el-icon-loading"></i>
              </div>
              <!-- 加载失败的占位内容 -->
              <div slot="error" class="image-slot">
                <i class="el-icon-picture-outline"></i>
              </div>
            </el-image>
          </template>
        </el-table-column>
        <el-table-column label="Description" align="center" prop="description" :show-overflow-tooltip="true" />
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
              v-hasPermi="['mall:product:edit']"
            >Edit</el-button>
            <el-button
              size="mini"
              type="text"
              icon="el-icon-setting"
              @click="handleManageSKU(scope.row)"
              v-hasPermi="['mall:product2:list']"
            >Manage SKU</el-button>
            <el-button
              size="mini"
              type="text"
              icon="el-icon-delete"
              @click="handleDelete(scope.row)"
              v-hasPermi="['mall:product:remove']"
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
  
      <!-- Add or Modify Product Dialog -->
      <el-dialog :title="title" :visible.sync="open" width="700px" append-to-body>
        <el-form ref="form" :model="form" :rules="rules" label-width="100px">
          <el-form-item label="Product ID" prop="productId">
            <el-input v-model="form.productId" placeholder="Please enter product ID" />
          </el-form-item>
          <el-form-item label="Product Name" prop="name">
            <el-input v-model="form.name" placeholder="Please enter product name" />
          </el-form-item>
          <el-form-item label="Category" prop="category">
            <el-select v-model="form.category" placeholder="Please select product category">
              <el-option label="Digital Product" value="Digital Product" />
              <el-option label="Physical Product" value="Physical Product" />
            </el-select>
          </el-form-item>
          <el-form-item label="Channel" prop="channel">
            <el-select v-model="form.channel" placeholder="Please select sales channel">
              <el-option
                v-for="dict in dict.type.channel_type"
                :key="dict.value"
                :label="dict.label"
                :value="dict.value"
              />
            </el-select>
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
          <el-form-item label="Product Image" prop="imageUrl">
            <div class="upload-section">
              <div class="upload-tabs">
                <span 
                  :class="['tab', { active: uploadMethod === 'url' }]" 
                  @click="uploadMethod = 'url'"
                >
                  URL
                </span>
                <span 
                  :class="['tab', { active: uploadMethod === 'file' }]" 
                  @click="uploadMethod = 'file'"
                >
                  Upload File
                </span>
              </div>
              
              <!-- URL Input -->
              <div v-if="uploadMethod === 'url'" class="upload-content">
                <el-input v-model="form.imageUrl" placeholder="Please enter image URL">
                  <template slot="append">
                    <el-button type="primary" @click="previewImage">Preview</el-button>
                  </template>
                </el-input>
                <div class="el-upload__tip">Enter image URL and click "Preview" button</div>
              </div>
              
              <!-- File Upload -->
              <div v-if="uploadMethod === 'file'" class="upload-content">
                <el-upload
                  ref="upload"
                  :action="uploadUrl"
                  :headers="uploadHeaders"
                  :before-upload="beforeUpload"
                  :on-success="handleUploadSuccess"
                  :on-error="handleUploadError"
                  :show-file-list="false"
                  accept="image/*"
                  class="upload-demo"
                >
                  <el-button size="small" type="primary">Select File</el-button>
                  <div slot="tip" class="el-upload__tip">Only image files allowed. File will be added automatically after upload.</div>
                </el-upload>
                <div v-if="uploadForm.fileName" class="selected-file">
                  Selected: {{ uploadForm.fileName }}
                </div>
              </div>
            </div>
          </el-form-item>
          <el-form-item label="Description" prop="description">
            <el-input v-model="form.description" type="textarea" :rows="4" placeholder="Please enter product description" />
          </el-form-item>
          <el-form-item label="Comment" prop="remark">
            <el-input v-model="form.remark" type="textarea" placeholder="Please enter comment" />
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
  import { listProduct, getProduct, delProduct, addProduct, updateProduct, updateProductSortOrder } from "@/api/mall/product";
  import Sortable from 'sortablejs';
  
  export default {
    name: "Product",
    dicts: ['product_status', 'channel_type'],
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
        // File upload
        uploadMethod: 'url', // 'url' or 'file'
        uploadForm: {
          fileName: '' // For file upload
        },
        uploadUrl: process.env.VUE_APP_BASE_API + '/common/upload', // 文件上传的实际URL
        uploadHeaders: {
          Authorization: 'Bearer ' + this.$store.getters.token
        },
        // Drag and drop sorting
        sortable: null,
        // Query parameters
        queryParams: {
          pageNum: 1,
          pageSize: 10,
          name: null,
          category: null,
          channel: null,
          status: null
        },
        // Form parameters
        form: {},
        // Form validation
        rules: {
          productId: [
            { required: true, message: "Product ID cannot be empty", trigger: "blur" }
          ],
          name: [
            { required: true, message: "Product name cannot be empty", trigger: "blur" }
          ],
          category: [
            { required: true, message: "Category cannot be empty", trigger: "blur" }
          ],
          channel: [
            { required: true, message: "Channel cannot be empty", trigger: "blur" }
          ],
          status: [
            { required: true, message: "Status cannot be empty", trigger: "blur" }
          ],
          imageUrl: [
            { required: true, message: "Image URL cannot be empty", trigger: "blur" }
          ],
          description: [
            { required: true, message: "Description cannot be empty", trigger: "blur" }
          ]
        }
      };
    },
    created() {
      this.getList();
    },
    mounted() {
      this.initSortable();
    },
    beforeDestroy() {
      if (this.sortable) {
        this.sortable.destroy();
      }
    },
    methods: {
      /** Query product list */
      getList() {
        this.loading = true;
        listProduct(this.queryParams).then(response => {
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
        this.form = {
          productId: null,
          name: null,
          category: 'Digital Product',
          channel: 'OS',
          description: null,
          imageUrl: null,
          status: '1', // 改为字符串类型，匹配字典数据的value
          shopId: 0,
          remark: null
        };
        this.uploadMethod = 'url'; // Reset to URL tab
        this.uploadForm = { fileName: '' };
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
        getProduct(id).then(response => {
          this.form = response.data;
          this.form.status = String(this.form.status); // 确保是字符串类型
          this.open = true;
          this.title = "Edit Product";
        });
      },
      /** Preview image */
      previewImage() {
        if (this.form.imageUrl) {
          // 如果是相对路径，拼接成完整URL
          if (this.form.imageUrl.startsWith('/')) {
            this.previewUrl = window.location.origin + this.form.imageUrl;
          } else {
            this.previewUrl = this.form.imageUrl;
          }
          this.previewVisible = true;
        } else {
          this.$modal.msgError("Please enter image URL first");
        }
      },
      /** Submit button */
      submitForm() {
        this.$refs["form"].validate(valid => {
          if (valid) {
            if (this.form.id != null) {
              updateProduct(this.form).then(response => {
                this.$modal.msgSuccess("Edit successful");
                this.open = false;
                this.getList();
              });
            } else {
              addProduct(this.form).then(response => {
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
        this.$modal.confirm('Are you sure to delete the product with ID "' + ids + '"?').then(function() {
          return delProduct(ids);
        }).then(() => {
          this.getList();
          this.$modal.msgSuccess("Delete successful");
        }).catch(() => {});
      },
      /** Manage SKU button operation */
      handleManageSKU(row) {
        // Navigate to product2 management page and pass the product ID
        this.$router.push({
          path: '/hide/product/productitem',
          query: {
            productId: row.productId || row.id
          }
        });
      },

      beforeUpload(file) {
        const isLt2M = file.size / 1024 / 1024 < 2;
        if (!isLt2M) {
          this.$message.error('Image must be smaller than 2MB!');
        }
        return isLt2M;
      },

      handleUploadSuccess(response, file, fileList) {
        console.log('Upload response:', response);
        console.log('File:', file);
        
        if (response.code === 200) {
          this.uploadForm.fileName = file.name;
          let url = response.url;
          console.log('Original URL from backend:', url);
          
          // Convert absolute URL to relative path for onion network compatibility
          let relativeUrl = this.convertToRelativeUrl(url);
          console.log('Converted to relative URL:', relativeUrl);
          
          this.form.imageUrl = relativeUrl;
          console.log('Stored URL for upload:', this.form.imageUrl);
          
          this.$message.success('File uploaded successfully!');
        } else {
          this.$message.error('Upload failed: ' + response.msg);
        }
      },

      handleUploadError(err, file, fileList) {
        this.$message.error('Failed to upload file: ' + err);
      },

      // Convert absolute URL to relative path for onion network compatibility
      convertToRelativeUrl(url) {
        if (!url) return '';
        
        // If URL is already relative (starts with /), return as is
        if (url.startsWith('/')) {
          return url;
        }
        
        // If URL is absolute (starts with http:// or https://), extract the path
        if (url.startsWith('http://') || url.startsWith('https://')) {
          try {
            const urlObj = new URL(url);
            return urlObj.pathname;
          } catch (error) {
            console.error('Error parsing URL:', url, error);
            return url;
          }
        }
        
        // If URL doesn't start with /, assume it's a relative path and add /
        if (!url.startsWith('/')) {
          return '/' + url;
        }
        
        return url;
      },

      /** Initialize drag and drop sorting */
      initSortable() {
        this.$nextTick(() => {
          const tableBody = this.$refs.productTable.$el.querySelector('.el-table__body-wrapper tbody');
          if (tableBody) {
            this.sortable = Sortable.create(tableBody, {
              handle: '.drag-handle',
              animation: 150,
              onEnd: (evt) => {
                const { oldIndex, newIndex } = evt;
                if (oldIndex !== newIndex) {
                  this.handleSortChange(oldIndex, newIndex);
                }
              }
            });
          }
        });
      },

      /** Handle sort change */
      handleSortChange(oldIndex, newIndex) {
        console.log(`🔄 [handleSortChange] Moving item from index ${oldIndex} to ${newIndex}`);
        
        // Move item in array
        const movedItem = this.productList.splice(oldIndex, 1)[0];
        this.productList.splice(newIndex, 0, movedItem);
        
        console.log('📦 [handleSortChange] Moved item:', movedItem);
        console.log('📋 [handleSortChange] Updated product list:', this.productList.map(p => ({ id: p.id, name: p.name })));
        
        // Update sort order for all items
        const updatedProducts = this.productList.map((item, index) => ({
          id: item.id,
          sortOrder: index + 1
        }));
        
        console.log('🔢 [handleSortChange] Updated sort orders:', updatedProducts);
        
        // Call API to update sort order
        this.updateProductSortOrder(updatedProducts);
      },

      /** Update product sort order via API */
      updateProductSortOrder(products) {
        console.log('🔄 [updateProductSortOrder] Sending sort order update:', products);
        
        updateProductSortOrder(products).then(response => {
          console.log('✅ [updateProductSortOrder] API response:', response);
          
          if (response.code === 200) {
            this.$modal.msgSuccess("排序更新成功");
            // Don't refresh immediately, let the user see the change
            // Only refresh after a short delay to show the success message
            setTimeout(() => {
              this.getList();
            }, 1000);
          } else {
            this.$modal.msgError("排序更新失败: " + response.msg);
            // Revert the change on failure
            this.getList();
          }
        }).catch(error => {
          console.error('❌ [updateProductSortOrder] API error:', error);
          this.$modal.msgError("排序更新失败: " + error.message);
          // Revert the change on failure
          this.getList();
        });
      }
    }
  };
  </script>

<style scoped>
.upload-section {
  margin-bottom: 8px;
}

.upload-tabs {
  display: flex;
  border-bottom: 1px solid #dcdfe6;
  margin-bottom: 10px;
}

.tab {
  padding: 8px 15px;
  cursor: pointer;
  font-size: 14px;
  color: #606266;
  border-bottom: 2px solid transparent;
}

.tab.active {
  color: #409EFF;
  border-bottom-color: #409EFF;
  font-weight: bold;
}

.upload-content {
  margin-top: 10px;
}

.upload-demo {
  display: inline-block;
}

.selected-file {
  margin-top: 8px;
  font-size: 13px;
  color: #606266;
}

/* Drag and drop sorting styles */
.drag-handle {
  cursor: move;
  color: #999;
  font-size: 16px;
}

.drag-handle:hover {
  color: #409EFF;
}

.sortable-ghost {
  opacity: 0.5;
  background: #f5f7fa;
}

.sortable-chosen {
  background: #e6f7ff;
}

/* Image loading styles */
.image-slot {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 100%;
  background: #f5f7fa;
  color: #909399;
  font-size: 20px;
}
</style> 
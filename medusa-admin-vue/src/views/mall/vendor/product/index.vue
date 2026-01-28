<template>
  <div class="app-container">
    <el-tabs v-model="activeTab" type="card" @tab-click="handleTabChange">
      <el-tab-pane label="All Products" name="all">
        <div class="tab-pane-content">
    <el-form :model="queryParams" ref="queryForm" :inline="true" v-show="showSearch" label-width="100px">
      <el-form-item label="Product Name" prop="name">
        <el-input
          v-model="queryParams.name"
          placeholder="Please enter Product Name"
          clearable
          size="small"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="Vendor" prop="vendorId">
        <el-select v-model="queryParams.vendorId" placeholder="Please select Vendor" clearable size="small">
          <el-option
            v-for="vendor in vendorList"
            :key="vendor.id"
            :label="vendor.vendorName"
            :value="vendor.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="Category" prop="category">
        <el-select v-model="queryParams.category" placeholder="Please select Category" clearable size="small">
          <el-option
            v-for="cat in categoryList"
            :key="cat.categoryCode"
            :label="cat.categoryName"
            :value="cat.categoryCode"
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
          type="success"
          plain
          icon="el-icon-check"
          size="mini"
          :disabled="multiple"
          @click="handleBatchApprove"
          v-hasPermi="['mall:vendor:product:approve']"
        >Batch Approve</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="el-icon-close"
          size="mini"
          :disabled="multiple"
          @click="handleBatchReject"
          v-hasPermi="['mall:vendor:product:approve']"
        >Batch Reject</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['mall:vendor:product:export']"
        >Export</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- Statistics Cards -->
    <el-row :gutter="20" class="mb8">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card pending">
          <div class="stat-content">
            <div class="stat-icon">
              <i class="el-icon-time"></i>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.pending }}</div>
              <div class="stat-label">Pending Approval</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card approved">
          <div class="stat-content">
            <div class="stat-icon">
              <i class="el-icon-check"></i>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.approved }}</div>
              <div class="stat-label">Approved</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card rejected">
          <div class="stat-content">
            <div class="stat-icon">
              <i class="el-icon-close"></i>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.rejected }}</div>
              <div class="stat-label">Rejected</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card total">
          <div class="stat-content">
            <div class="stat-icon">
              <i class="el-icon-box"></i>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.total }}</div>
              <div class="stat-label">Total Products</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-table v-loading="loading" :data="productList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="Product Image" align="center" prop="imageUrl" width="100">
        <template slot-scope="scope">
          <el-image
            v-if="scope.row.imageUrl"
            :src="scope.row.imageUrl"
            :preview-src-list="[scope.row.imageUrl]"
            fit="cover"
            style="width: 60px; height: 60px; border-radius: 4px;"
          ></el-image>
          <div v-else class="no-image">
            <i class="el-icon-picture-outline"></i>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="Product Name" align="center" prop="name" :show-overflow-tooltip="true" width="200" />
      <el-table-column label="Vendor" align="center" prop="vendorName" width="150" :show-overflow-tooltip="true" />
      <el-table-column label="Category" align="center" prop="category" width="120" />
      <el-table-column label="Price" align="center" prop="price" width="120">
        <template slot-scope="scope">
          <span style="color: #409EFF; font-weight: 600;">AUD ${{ scope.row.price ? scope.row.price.toFixed(2) : '0.00' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="Stock" align="center" prop="stock" width="80">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.stock > 50" type="success" size="small">{{ scope.row.stock }}</el-tag>
          <el-tag v-else-if="scope.row.stock > 10" type="warning" size="small">{{ scope.row.stock }}</el-tag>
          <el-tag v-else type="danger" size="small">{{ scope.row.stock }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="Approval Status" align="center" prop="approvalStatus" width="140">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.approvalStatus === 'PENDING_APPROVAL'" type="warning" size="small">
            <i class="el-icon-time"></i> Pending
          </el-tag>
          <el-tag v-else-if="scope.row.approvalStatus === 'APPROVED'" type="success" size="small">
            <i class="el-icon-check"></i> Approved
          </el-tag>
          <el-tag v-else-if="scope.row.approvalStatus === 'REJECTED'" type="danger" size="small">
            <i class="el-icon-close"></i> Rejected
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="Submitted Time" align="center" prop="createTime" width="160">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime, '{y}-{m}-{d} {h}:{i}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="Actions" align="center" class-name="small-padding fixed-width" width="250">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-view"
            @click="handleView(scope.row)"
            v-hasPermi="['mall:vendor:product:query']"
          >View</el-button>
          
          <el-button
            v-if="scope.row.approvalStatus === 'PENDING_APPROVAL'"
            size="mini"
            type="text"
            icon="el-icon-check"
            @click="handleApprove(scope.row)"
            v-hasPermi="['mall:vendor:product:approve']"
            style="color: #67C23A"
          >Approve</el-button>
          
          <el-button
            v-if="scope.row.approvalStatus === 'PENDING_APPROVAL'"
            size="mini"
            type="text"
            icon="el-icon-close"
            @click="handleReject(scope.row)"
            v-hasPermi="['mall:vendor:product:approve']"
            style="color: #F56C6C"
          >Reject</el-button>

          <el-button
            v-if="scope.row.approvalStatus === 'APPROVED'"
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleEdit(scope.row)"
            v-hasPermi="['mall:vendor:product:edit']"
          >Edit</el-button>
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
      </el-tab-pane>
      
      <el-tab-pane label="Pending Approval" name="pending">
        <div class="tab-pane-content">
          <el-form :model="queryParams" ref="queryForm" :inline="true" v-show="showSearch" label-width="100px">
            <el-form-item label="Product Name" prop="name">
              <el-input
                v-model="queryParams.name"
                placeholder="Please enter Product Name"
                clearable
                size="small"
                @keyup.enter.native="handleQuery"
              />
            </el-form-item>
            <el-form-item label="Vendor" prop="vendorId">
              <el-select v-model="queryParams.vendorId" placeholder="Please select Vendor" clearable size="small">
                <el-option
                  v-for="vendor in vendorList"
                  :key="vendor.id"
                  :label="vendor.vendorName"
                  :value="vendor.id"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="Category" prop="category">
              <el-select v-model="queryParams.category" placeholder="Please select Category" clearable size="small">
                <el-option
                  v-for="cat in categoryList"
                  :key="cat.categoryCode"
                  :label="cat.categoryName"
                  :value="cat.categoryCode"
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
                type="success"
                plain
                icon="el-icon-check"
                size="mini"
                :disabled="multiple"
                @click="handleBatchApprove"
                v-hasPermi="['mall:vendor:product:approve']"
              >Batch Approve</el-button>
            </el-col>
            <el-col :span="1.5">
              <el-button
                type="danger"
                plain
                icon="el-icon-close"
                size="mini"
                :disabled="multiple"
                @click="handleBatchReject"
                v-hasPermi="['mall:vendor:product:approve']"
              >Batch Reject</el-button>
            </el-col>
            <el-col :span="1.5">
              <el-button
                type="warning"
                plain
                icon="el-icon-download"
                size="mini"
                @click="handleExport"
                v-hasPermi="['mall:vendor:product:export']"
              >Export</el-button>
            </el-col>
            <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
          </el-row>

          <el-table v-loading="loading" :data="productList" @selection-change="handleSelectionChange">
            <el-table-column type="selection" width="55" align="center" />
            <el-table-column label="Product Image" align="center" prop="imageUrl" width="100">
              <template slot-scope="scope">
                <el-image
                  v-if="scope.row.imageUrl"
                  :src="scope.row.imageUrl"
                  :preview-src-list="[scope.row.imageUrl]"
                  fit="cover"
                  style="width: 60px; height: 60px; border-radius: 4px;"
                ></el-image>
                <div v-else class="no-image">
                  <i class="el-icon-picture-outline"></i>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="Product Name" align="center" prop="name" :show-overflow-tooltip="true" width="200" />
            <el-table-column label="Vendor" align="center" prop="vendorName" width="150" :show-overflow-tooltip="true" />
            <el-table-column label="Category" align="center" prop="category" width="120" />
            <el-table-column label="Price" align="center" prop="price" width="120">
              <template slot-scope="scope">
                <span style="color: #409EFF; font-weight: 600;">AUD ${{ scope.row.price ? scope.row.price.toFixed(2) : '0.00' }}</span>
              </template>
            </el-table-column>
            <el-table-column label="Stock" align="center" prop="stock" width="80">
              <template slot-scope="scope">
                <el-tag v-if="scope.row.stock > 50" type="success" size="small">{{ scope.row.stock }}</el-tag>
                <el-tag v-else-if="scope.row.stock > 10" type="warning" size="small">{{ scope.row.stock }}</el-tag>
                <el-tag v-else type="danger" size="small">{{ scope.row.stock }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="Submitted Time" align="center" prop="createTime" width="160">
              <template slot-scope="scope">
                <span>{{ parseTime(scope.row.createTime, '{y}-{m}-{d} {h}:{i}') }}</span>
              </template>
            </el-table-column>
            <el-table-column label="Actions" align="center" class-name="small-padding fixed-width" width="250">
              <template slot-scope="scope">
                <el-button
                  size="mini"
                  type="text"
                  icon="el-icon-view"
                  @click="handleView(scope.row)"
                  v-hasPermi="['mall:vendor:product:query']"
                >View</el-button>
                <el-button
                  size="mini"
                  type="text"
                  icon="el-icon-check"
                  @click="handleApprove(scope.row)"
                  v-hasPermi="['mall:vendor:product:approve']"
                  style="color: #67C23A"
                >Approve</el-button>
                <el-button
                  size="mini"
                  type="text"
                  icon="el-icon-close"
                  @click="handleReject(scope.row)"
                  v-hasPermi="['mall:vendor:product:approve']"
                  style="color: #F56C6C"
                >Reject</el-button>
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
      </el-tab-pane>
      
      <el-tab-pane label="Approved" name="approved">
        <div class="tab-pane-content">
          <el-form :model="queryParams" ref="queryForm" :inline="true" v-show="showSearch" label-width="100px">
            <el-form-item label="Product Name" prop="name">
              <el-input
                v-model="queryParams.name"
                placeholder="Please enter Product Name"
                clearable
                size="small"
                @keyup.enter.native="handleQuery"
              />
            </el-form-item>
            <el-form-item label="Vendor" prop="vendorId">
              <el-select v-model="queryParams.vendorId" placeholder="Please select Vendor" clearable size="small">
                <el-option
                  v-for="vendor in vendorList"
                  :key="vendor.id"
                  :label="vendor.vendorName"
                  :value="vendor.id"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="Category" prop="category">
              <el-select v-model="queryParams.category" placeholder="Please select Category" clearable size="small">
                <el-option
                  v-for="cat in categoryList"
                  :key="cat.categoryCode"
                  :label="cat.categoryName"
                  :value="cat.categoryCode"
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
                type="warning"
                plain
                icon="el-icon-download"
                size="mini"
                @click="handleExport"
                v-hasPermi="['mall:vendor:product:export']"
              >Export</el-button>
            </el-col>
            <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
          </el-row>

          <el-table v-loading="loading" :data="productList" @selection-change="handleSelectionChange">
            <el-table-column type="selection" width="55" align="center" />
            <el-table-column label="Product Image" align="center" prop="imageUrl" width="100">
              <template slot-scope="scope">
                <el-image
                  v-if="scope.row.imageUrl"
                  :src="scope.row.imageUrl"
                  :preview-src-list="[scope.row.imageUrl]"
                  fit="cover"
                  style="width: 60px; height: 60px; border-radius: 4px;"
                ></el-image>
                <div v-else class="no-image">
                  <i class="el-icon-picture-outline"></i>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="Product Name" align="center" prop="name" :show-overflow-tooltip="true" width="200" />
            <el-table-column label="Vendor" align="center" prop="vendorName" width="150" :show-overflow-tooltip="true" />
            <el-table-column label="Category" align="center" prop="category" width="120" />
            <el-table-column label="Price" align="center" prop="price" width="120">
              <template slot-scope="scope">
                <span style="color: #409EFF; font-weight: 600;">AUD ${{ scope.row.price ? scope.row.price.toFixed(2) : '0.00' }}</span>
              </template>
            </el-table-column>
            <el-table-column label="Stock" align="center" prop="stock" width="80">
              <template slot-scope="scope">
                <el-tag v-if="scope.row.stock > 50" type="success" size="small">{{ scope.row.stock }}</el-tag>
                <el-tag v-else-if="scope.row.stock > 10" type="warning" size="small">{{ scope.row.stock }}</el-tag>
                <el-tag v-else type="danger" size="small">{{ scope.row.stock }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="Submitted Time" align="center" prop="createTime" width="160">
              <template slot-scope="scope">
                <span>{{ parseTime(scope.row.createTime, '{y}-{m}-{d} {h}:{i}') }}</span>
              </template>
            </el-table-column>
            <el-table-column label="Actions" align="center" class-name="small-padding fixed-width" width="200">
              <template slot-scope="scope">
                <el-button
                  size="mini"
                  type="text"
                  icon="el-icon-view"
                  @click="handleView(scope.row)"
                  v-hasPermi="['mall:vendor:product:query']"
                >View</el-button>
                <el-button
                  size="mini"
                  type="text"
                  icon="el-icon-edit"
                  @click="handleEdit(scope.row)"
                  v-hasPermi="['mall:vendor:product:edit']"
                >Edit</el-button>
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
      </el-tab-pane>
      
      <el-tab-pane label="Rejected" name="rejected">
        <div class="tab-pane-content">
          <el-form :model="queryParams" ref="queryForm" :inline="true" v-show="showSearch" label-width="100px">
            <el-form-item label="Product Name" prop="name">
              <el-input
                v-model="queryParams.name"
                placeholder="Please enter Product Name"
                clearable
                size="small"
                @keyup.enter.native="handleQuery"
              />
            </el-form-item>
            <el-form-item label="Vendor" prop="vendorId">
              <el-select v-model="queryParams.vendorId" placeholder="Please select Vendor" clearable size="small">
                <el-option
                  v-for="vendor in vendorList"
                  :key="vendor.id"
                  :label="vendor.vendorName"
                  :value="vendor.id"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="Category" prop="category">
              <el-select v-model="queryParams.category" placeholder="Please select Category" clearable size="small">
                <el-option
                  v-for="cat in categoryList"
                  :key="cat.categoryCode"
                  :label="cat.categoryName"
                  :value="cat.categoryCode"
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
                type="warning"
                plain
                icon="el-icon-download"
                size="mini"
                @click="handleExport"
                v-hasPermi="['mall:vendor:product:export']"
              >Export</el-button>
            </el-col>
            <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
          </el-row>

          <el-table v-loading="loading" :data="productList" @selection-change="handleSelectionChange">
            <el-table-column type="selection" width="55" align="center" />
            <el-table-column label="Product Image" align="center" prop="imageUrl" width="100">
              <template slot-scope="scope">
                <el-image
                  v-if="scope.row.imageUrl"
                  :src="scope.row.imageUrl"
                  :preview-src-list="[scope.row.imageUrl]"
                  fit="cover"
                  style="width: 60px; height: 60px; border-radius: 4px;"
                ></el-image>
                <div v-else class="no-image">
                  <i class="el-icon-picture-outline"></i>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="Product Name" align="center" prop="name" :show-overflow-tooltip="true" width="200" />
            <el-table-column label="Vendor" align="center" prop="vendorName" width="150" :show-overflow-tooltip="true" />
            <el-table-column label="Category" align="center" prop="category" width="120" />
            <el-table-column label="Price" align="center" prop="price" width="120">
              <template slot-scope="scope">
                <span style="color: #409EFF; font-weight: 600;">AUD ${{ scope.row.price ? scope.row.price.toFixed(2) : '0.00' }}</span>
              </template>
            </el-table-column>
            <el-table-column label="Rejection Reason" align="center" prop="rejectionReason" width="200" :show-overflow-tooltip="true" />
            <el-table-column label="Submitted Time" align="center" prop="createTime" width="160">
              <template slot-scope="scope">
                <span>{{ parseTime(scope.row.createTime, '{y}-{m}-{d} {h}:{i}') }}</span>
              </template>
            </el-table-column>
            <el-table-column label="Actions" align="center" class-name="small-padding fixed-width" width="150">
              <template slot-scope="scope">
                <el-button
                  size="mini"
                  type="text"
                  icon="el-icon-view"
                  @click="handleView(scope.row)"
                  v-hasPermi="['mall:vendor:product:query']"
                >View</el-button>
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
      </el-tab-pane>
    </el-tabs>

    <!-- View Product Dialog -->
    <el-dialog :title="'Product Details - ' + viewForm.name" :visible.sync="viewDialogVisible" width="900px" append-to-body>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="Product Image" :span="2">
          <el-image
            v-if="viewForm.imageUrl"
            :src="viewForm.imageUrl"
            :preview-src-list="[viewForm.imageUrl]"
            fit="contain"
            style="width: 200px; height: 200px; border-radius: 4px;"
          ></el-image>
          <span v-else class="text-muted">No image</span>
        </el-descriptions-item>
        <el-descriptions-item label="Product Name">{{ viewForm.name }}</el-descriptions-item>
        <el-descriptions-item label="Vendor">{{ viewForm.vendorName }}</el-descriptions-item>
        <el-descriptions-item label="Category">{{ viewForm.category }}</el-descriptions-item>
        <el-descriptions-item label="Brand">{{ viewForm.brand || 'N/A' }}</el-descriptions-item>
        <el-descriptions-item label="Price">AUD ${{ viewForm.price ? viewForm.price.toFixed(2) : '0.00' }}</el-descriptions-item>
        <el-descriptions-item label="Stock">{{ viewForm.stock }}</el-descriptions-item>
        <el-descriptions-item label="SKU">{{ viewForm.sku || 'N/A' }}</el-descriptions-item>
        <el-descriptions-item label="Weight">{{ viewForm.weight ? viewForm.weight + ' kg' : 'N/A' }}</el-descriptions-item>
        <el-descriptions-item label="Description" :span="2">
          <div style="white-space: pre-wrap;">{{ viewForm.description }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="Approval Status">
          <el-tag v-if="viewForm.approvalStatus === 'PENDING_APPROVAL'" type="warning">Pending Approval</el-tag>
          <el-tag v-else-if="viewForm.approvalStatus === 'APPROVED'" type="success">Approved</el-tag>
          <el-tag v-else-if="viewForm.approvalStatus === 'REJECTED'" type="danger">Rejected</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="Submitted Time">
          {{ parseTime(viewForm.createTime, '{y}-{m}-{d} {h}:{i}:{s}') }}
        </el-descriptions-item>
        <el-descriptions-item v-if="viewForm.approvalNotes" label="Approval Notes" :span="2">
          <el-alert :title="viewForm.approvalNotes" type="success" :closable="false"></el-alert>
        </el-descriptions-item>
        <el-descriptions-item v-if="viewForm.rejectionReason" label="Rejection Reason" :span="2">
          <el-alert :title="viewForm.rejectionReason" type="error" :closable="false"></el-alert>
        </el-descriptions-item>
      </el-descriptions>
      <div slot="footer" class="dialog-footer">
        <el-button @click="viewDialogVisible = false">Close</el-button>
        <el-button v-if="viewForm.approvalStatus === 'PENDING_APPROVAL'" type="success" @click="handleApprove(viewForm)">Approve</el-button>
        <el-button v-if="viewForm.approvalStatus === 'PENDING_APPROVAL'" type="danger" @click="handleReject(viewForm)">Reject</el-button>
      </div>
    </el-dialog>

    <!-- Approve Dialog -->
    <el-dialog title="Approve Product" :visible.sync="approveDialogVisible" width="600px" append-to-body>
      <el-form :model="approveForm" label-width="120px">
        <el-form-item label="Product Name">
          <span>{{ approveForm.name }}</span>
        </el-form-item>
        <el-form-item label="Approval Notes">
          <el-input v-model="approveForm.notes" type="textarea" :rows="4" placeholder="Optional approval notes"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="approveDialogVisible = false">Cancel</el-button>
        <el-button type="primary" @click="confirmApprove">Confirm Approve</el-button>
      </div>
    </el-dialog>

    <!-- Reject Dialog -->
    <el-dialog title="Reject Product" :visible.sync="rejectDialogVisible" width="600px" append-to-body>
      <el-form :model="rejectForm" :rules="rejectRules" ref="rejectForm" label-width="120px">
        <el-form-item label="Product Name">
          <span>{{ rejectForm.name }}</span>
        </el-form-item>
        <el-form-item label="Rejection Reason" prop="reason">
          <el-input v-model="rejectForm.reason" type="textarea" :rows="4" placeholder="Please provide a reason for rejection (required)"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="rejectDialogVisible = false">Cancel</el-button>
        <el-button type="danger" @click="confirmReject">Confirm Reject</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listVendorProducts, approveProduct, rejectProduct } from "@/api/mall/vendorProduct";
import { listVendor } from "@/api/mall/vendor";
import { getProductCategories } from "@/api/mall/product";
import { parseTime } from "@/utils/ruoyi";

export default {
  name: "VendorProductApproval",
  data() {
    return {
      // Active tab
      activeTab: 'all',
      // Loading state
      loading: true,
      // Selection
      ids: [],
      single: true,
      multiple: true,
      // Show search
      showSearch: true,
      // Total count
      total: 0,
      // Product list
      productList: [],
      // Vendor list
      vendorList: [],
      // Category list
      categoryList: [],
      // Statistics
      stats: {
        total: 0,
        pending: 0,
        approved: 0,
        rejected: 0
      },
      // Query params
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        name: null,
        vendorId: null,
        category: null,
        approvalStatus: null
      },
      // View dialog
      viewDialogVisible: false,
      viewForm: {},
      // Approve dialog
      approveDialogVisible: false,
      approveForm: {
        id: null,
        name: '',
        notes: ''
      },
      // Reject dialog
      rejectDialogVisible: false,
      rejectForm: {
        id: null,
        name: '',
        reason: ''
      },
      rejectRules: {
        reason: [
          { required: true, message: "Rejection reason is required", trigger: "blur" }
        ]
      }
    };
  },
  created() {
    this.getList();
    this.loadVendors();
    this.loadCategories();
  },
  methods: {
    /** Handle tab change */
    handleTabChange(tab) {
      // Reset page number when switching tabs
      this.queryParams.pageNum = 1;
      
      // Set approval status filter based on active tab
      switch(tab.name) {
        case 'all':
          this.queryParams.approvalStatus = null;
          break;
        case 'pending':
          this.queryParams.approvalStatus = 'PENDING_APPROVAL';
          break;
        case 'approved':
          this.queryParams.approvalStatus = 'APPROVED';
          break;
        case 'rejected':
          this.queryParams.approvalStatus = 'REJECTED';
          break;
        default:
          this.queryParams.approvalStatus = null;
      }
      
      // Reload data
      this.getList();
    },
    /** Query product list */
    getList() {
      this.loading = true;
      listVendorProducts(this.queryParams).then(response => {
        this.productList = response.rows;
        this.total = response.total;
        this.updateStats();
        this.loading = false;
      });
    },
    /** Load vendor list */
    loadVendors() {
      listVendor({}).then(response => {
        this.vendorList = response.rows || [];
      });
    },
    /** Load category list */
    loadCategories() {
      getProductCategories().then(response => {
        this.categoryList = response.data || [];
      });
    },
    /** Update statistics */
    updateStats() {
      this.stats.total = this.productList.length;
      this.stats.pending = this.productList.filter(p => p.approvalStatus === 'PENDING_APPROVAL').length;
      this.stats.approved = this.productList.filter(p => p.approvalStatus === 'APPROVED').length;
      this.stats.rejected = this.productList.filter(p => p.approvalStatus === 'REJECTED').length;
    },
    /** Search button */
    handleQuery() {
      this.queryParams.pageNum = 1;
      this.getList();
    },
    /** Reset button */
    resetQuery() {
      this.resetForm("queryForm");
      this.handleQuery();
    },
    /** Multi-selection changed */
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.id);
      this.single = selection.length !== 1;
      this.multiple = !selection.length;
    },
    /** View button */
    handleView(row) {
      this.viewForm = { ...row };
      this.viewDialogVisible = true;
    },
    /** Edit button */
    handleEdit(row) {
      this.$router.push({ path: '/mall/product/edit/' + row.id });
    },
    /** Approve button */
    handleApprove(row) {
      this.approveForm = {
        id: row.id,
        name: row.name,
        notes: ''
      };
      this.approveDialogVisible = true;
      this.viewDialogVisible = false;
    },
    /** Confirm approve */
    confirmApprove() {
      approveProduct(this.approveForm.id, this.approveForm.notes).then(response => {
        this.$modal.msgSuccess("Product approved successfully");
        this.approveDialogVisible = false;
        this.getList();
      });
    },
    /** Batch approve */
    handleBatchApprove() {
      const ids = this.ids;
      if (ids.length === 0) {
        this.$modal.msgWarning("Please select products to approve");
        return;
      }
      this.$modal.confirm('Are you sure to approve the selected products?').then(() => {
        const promises = ids.map(id => approveProduct(id, ''));
        return Promise.all(promises);
      }).then(() => {
        this.$modal.msgSuccess("Products approved successfully");
        this.getList();
      }).catch(() => {});
    },
    /** Reject button */
    handleReject(row) {
      this.rejectForm = {
        id: row.id,
        name: row.name,
        reason: ''
      };
      this.rejectDialogVisible = true;
      this.viewDialogVisible = false;
    },
    /** Confirm reject */
    confirmReject() {
      this.$refs["rejectForm"].validate(valid => {
        if (valid) {
          rejectProduct(this.rejectForm.id, this.rejectForm.reason).then(response => {
            this.$modal.msgSuccess("Product rejected successfully");
            this.rejectDialogVisible = false;
            this.getList();
          });
        }
      });
    },
    /** Batch reject */
    handleBatchReject() {
      const ids = this.ids;
      if (ids.length === 0) {
        this.$modal.msgWarning("Please select products to reject");
        return;
      }
      this.$prompt('Please provide a reason for rejection', 'Batch Reject', {
        confirmButtonText: 'Confirm',
        cancelButtonText: 'Cancel',
        inputType: 'textarea',
        inputValidator: (value) => {
          if (!value) {
            return 'Rejection reason is required';
          }
          return true;
        }
      }).then(({ value }) => {
        const promises = ids.map(id => rejectProduct(id, value));
        return Promise.all(promises);
      }).then(() => {
        this.$modal.msgSuccess("Products rejected successfully");
        this.getList();
      }).catch(() => {});
    },
    /** Export button */
    handleExport() {
      this.download('admin/mall/vendor/product/export', {
        ...this.queryParams
      }, `vendor_products_${new Date().getTime()}.xlsx`)
    }
  }
};
</script>

<style scoped>
.mb8 {
  margin-bottom: 8px;
}

.stat-card {
  margin-bottom: 16px;
}

.stat-card.pending >>> .el-card__body {
  background: linear-gradient(135deg, #FFF7E6 0%, #FFF1CC 100%);
}

.stat-card.approved >>> .el-card__body {
  background: linear-gradient(135deg, #E8F5E9 0%, #C8E6C9 100%);
}

.stat-card.rejected >>> .el-card__body {
  background: linear-gradient(135deg, #FFEBEE 0%, #FFCDD2 100%);
}

.stat-card.total >>> .el-card__body {
  background: linear-gradient(135deg, #E3F2FD 0%, #BBDEFB 100%);
}

.stat-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.stat-icon {
  font-size: 48px;
  opacity: 0.6;
}

.stat-card.pending .stat-icon {
  color: #E6A23C;
}

.stat-card.approved .stat-icon {
  color: #67C23A;
}

.stat-card.rejected .stat-icon {
  color: #F56C6C;
}

.stat-card.total .stat-icon {
  color: #409EFF;
}

.stat-info {
  text-align: right;
}

.stat-value {
  font-size: 32px;
  font-weight: bold;
  color: #303133;
}

.stat-label {
  font-size: 14px;
  color: #606266;
  margin-top: 4px;
}

.no-image {
  width: 60px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #F5F7FA;
  border-radius: 4px;
  color: #C0C4CC;
  font-size: 24px;
}

.text-muted {
  color: #909399;
}

.tab-pane-content {
  margin-top: 10px;
}
</style>


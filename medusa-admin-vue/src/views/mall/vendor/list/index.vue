<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" :inline="true" v-show="showSearch" label-width="100px">
      <el-form-item label="Vendor Code" prop="vendorCode">
        <el-input
          v-model="queryParams.vendorCode"
          placeholder="Please enter Vendor Code"
          clearable
          size="small"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="Vendor Name" prop="vendorName">
        <el-input
          v-model="queryParams.vendorName"
          placeholder="Please enter Vendor Name"
          clearable
          size="small"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="Status" prop="status">
        <el-select v-model="queryParams.status" placeholder="Please select Status" clearable size="small">
          <el-option label="Enabled" :value="1" />
          <el-option label="Disabled" :value="0" />
        </el-select>
      </el-form-item>
      <el-form-item label="Location" prop="location">
        <el-select v-model="queryParams.location" placeholder="Please select Location" clearable size="small">
          <el-option label="North America" value="North America" />
          <el-option label="Europe" value="Europe" />
          <el-option label="Asia Pacific" value="Asia Pacific" />
          <el-option label="Latin America" value="Latin America" />
          <el-option label="Middle East & Africa" value="Middle East & Africa" />
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
          v-hasPermi="['mall:vendor:add']"
        >New Vendor</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="el-icon-edit"
          size="mini"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['mall:vendor:edit']"
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
          v-hasPermi="['mall:vendor:remove']"
        >Delete</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['mall:vendor:export']"
        >Export</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="vendorList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="Vendor Code" align="center" prop="vendorCode" width="140" />
      <el-table-column label="Vendor Name" align="center" prop="vendorName" width="150" :show-overflow-tooltip="true" />
      <el-table-column label="Location" align="center" prop="location" width="130" />
      <el-table-column label="Rating" align="center" prop="rating" width="100">
        <template slot-scope="scope">
          <el-rate
            v-model="scope.row.rating"
            disabled
            show-score
            text-color="#ff9900"
            score-template="{value}">
          </el-rate>
        </template>
      </el-table-column>
      <el-table-column label="Total Orders" align="center" prop="totalOrders" width="110" />
      <el-table-column label="Contact" align="center" prop="contactTelegram" width="150" :show-overflow-tooltip="true">
        <template slot-scope="scope">
          <span v-if="scope.row.contactTelegram">TG: {{ scope.row.contactTelegram }}</span>
          <span v-else-if="scope.row.contactEmail">{{ scope.row.contactEmail }}</span>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="Status" align="center" prop="status" width="80">
        <template slot-scope="scope">
          <el-switch
            v-model="scope.row.status"
            :active-value="1"
            :inactive-value="0"
            @change="handleStatusChange(scope.row)"
          ></el-switch>
        </template>
      </el-table-column>
      <el-table-column label="Featured" align="center" prop="isFeatured" width="80">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.isFeatured === 1" type="success" size="small">Yes</el-tag>
          <el-tag v-else type="info" size="small">No</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="Bond" align="center" prop="bond" width="100">
        <template slot-scope="scope">
          <span>${{ (scope.row.bond || 0).toFixed(2) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="Level" align="center" prop="level" width="80">
        <template slot-scope="scope">
          <el-tag type="primary" size="small">Level {{ scope.row.level || 1 }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="Sales Points" align="center" prop="salesPoints" width="120">
        <template slot-scope="scope">
          <span>{{ (scope.row.salesPoints || 0).toLocaleString() }}</span>
        </template>
      </el-table-column>
      <el-table-column label="Created Time" align="center" prop="createTime" width="160">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime, '{y}-{m}-{d} {h}:{i}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="Actions" align="center" class-name="small-padding fixed-width" width="240">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-view"
            @click="handleView(scope.row)"
            v-hasPermi="['mall:vendor:query']"
          >View</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['mall:vendor:edit']"
          >Edit</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-money"
            @click="handleManageBondLevel(scope.row)"
            v-hasPermi="['mall:vendor:edit']"
          >Bond & Level</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-user"
            @click="handleCreateWarehouseAccount(scope.row)"
            v-hasPermi="['mall:vendor:warehouse:create']"
          >Create Warehouse Account</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['mall:vendor:remove']"
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

    <!-- Add/Edit Dialog -->
    <el-dialog :title="title" :visible.sync="open" width="800px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="140px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="Vendor Code" prop="vendorCode">
              <el-input v-model="form.vendorCode" placeholder="Auto-generated if empty" :disabled="form.id != null" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="Vendor Name" prop="vendorName">
              <el-input v-model="form.vendorName" placeholder="Please enter Vendor Name" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="Location" prop="location">
              <el-select v-model="form.location" placeholder="Please select Location" style="width: 100%">
                <el-option label="North America" value="North America" />
                <el-option label="Europe" value="Europe" />
                <el-option label="Asia Pacific" value="Asia Pacific" />
                <el-option label="Latin America" value="Latin America" />
                <el-option label="Middle East & Africa" value="Middle East & Africa" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="Status" prop="status">
              <el-radio-group v-model="form.status">
                <el-radio :label="1">Enabled</el-radio>
                <el-radio :label="0">Disabled</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="Description" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="Please enter description" />
        </el-form-item>
        <el-row>
          <el-col :span="12">
            <el-form-item label="Contact Telegram" prop="contactTelegram">
              <el-input v-model="form.contactTelegram" placeholder="@username" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="Contact Email" prop="contactEmail">
              <el-input v-model="form.contactEmail" placeholder="email@example.com" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="Contact Signal" prop="contactSignal">
              <el-input v-model="form.contactSignal" placeholder="Phone number" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="Contact Jabber" prop="contactJabber">
              <el-input v-model="form.contactJabber" placeholder="username@domain.com" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="Contact Threema" prop="contactThreema">
              <el-input v-model="form.contactThreema" placeholder="Threema ID" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-divider content-position="left">Secondary Contact</el-divider>
        <el-row>
          <el-col :span="12">
            <el-form-item label="Secondary Telegram" prop="secondaryTelegram">
              <el-input v-model="form.secondaryTelegram" placeholder="@username" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="Secondary Email" prop="secondaryEmail">
              <el-input v-model="form.secondaryEmail" placeholder="email@example.com" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="Secondary Signal" prop="secondarySignal">
              <el-input v-model="form.secondarySignal" placeholder="Phone number" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="Secondary Jabber" prop="secondaryJabber">
              <el-input v-model="form.secondaryJabber" placeholder="username@domain.com" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="Secondary Threema" prop="secondaryThreema">
              <el-input v-model="form.secondaryThreema" placeholder="Threema ID" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="PGP Public Key URL" prop="pgpPublicKeyUrl">
          <el-input v-model="form.pgpPublicKeyUrl" placeholder="https://..." />
        </el-form-item>
        <el-row>
          <el-col :span="8">
            <el-form-item label="BTC Wallet" prop="btcWallet">
              <el-input v-model="form.btcWallet" placeholder="BTC address" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="XMR Wallet" prop="xmrWallet">
              <el-input v-model="form.xmrWallet" placeholder="XMR address" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="USDT Wallet" prop="usdtWallet">
              <el-input v-model="form.usdtWallet" placeholder="USDT address" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="8">
            <el-form-item label="Is Featured" prop="isFeatured">
              <el-radio-group v-model="form.isFeatured">
                <el-radio :label="1">Yes</el-radio>
                <el-radio :label="0">No</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="Sort Order" prop="sortOrder">
              <el-input-number v-model="form.sortOrder" :min="0" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="Remark" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="Please enter remark" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">Confirm</el-button>
        <el-button @click="cancel">Cancel</el-button>
      </div>
    </el-dialog>

    <!-- View Details Dialog -->
    <el-dialog title="Vendor Details" :visible.sync="openView" width="900px" append-to-body>
      <el-descriptions :column="2" border size="medium">
        <el-descriptions-item label="Vendor Code">{{ viewForm.vendorCode }}</el-descriptions-item>
        <el-descriptions-item label="Vendor Name">{{ viewForm.vendorName }}</el-descriptions-item>
        <el-descriptions-item label="Location">{{ viewForm.location }}</el-descriptions-item>
        <el-descriptions-item label="Rating">
          <el-rate v-model="viewForm.rating" disabled show-score text-color="#ff9900"></el-rate>
        </el-descriptions-item>
        <el-descriptions-item label="Total Sales">{{ viewForm.totalSales || 0 }}</el-descriptions-item>
        <el-descriptions-item label="Total Orders">{{ viewForm.totalOrders || 0 }}</el-descriptions-item>
        <el-descriptions-item label="Status">
          <el-tag v-if="viewForm.status === 1" type="success" size="small">Enabled</el-tag>
          <el-tag v-else type="info" size="small">Disabled</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="Featured">
          <el-tag v-if="viewForm.isFeatured === 1" type="success" size="small">Yes</el-tag>
          <el-tag v-else type="info" size="small">No</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="Description" :span="2">
          <div v-if="viewForm.description" style="white-space: pre-wrap; word-wrap: break-word;">{{ viewForm.description }}</div>
          <span v-else>-</span>
        </el-descriptions-item>
        <el-descriptions-item label="Primary Contact" :span="2">
          <div v-if="viewForm.contactTelegram">Telegram: {{ viewForm.contactTelegram }}</div>
          <div v-if="viewForm.contactSignal">Signal: {{ viewForm.contactSignal }}</div>
          <div v-if="viewForm.contactJabber">Jabber: {{ viewForm.contactJabber }}</div>
          <div v-if="viewForm.contactEmail">Email: {{ viewForm.contactEmail }}</div>
          <div v-if="viewForm.contactThreema">Threema: {{ viewForm.contactThreema }}</div>
          <div v-if="!viewForm.contactTelegram && !viewForm.contactSignal && !viewForm.contactJabber && !viewForm.contactEmail && !viewForm.contactThreema">-</div>
        </el-descriptions-item>
        <el-descriptions-item label="Secondary Contact" :span="2">
          <div v-if="viewForm.secondaryTelegram">Telegram: {{ viewForm.secondaryTelegram }}</div>
          <div v-if="viewForm.secondarySignal">Signal: {{ viewForm.secondarySignal }}</div>
          <div v-if="viewForm.secondaryJabber">Jabber: {{ viewForm.secondaryJabber }}</div>
          <div v-if="viewForm.secondaryEmail">Email: {{ viewForm.secondaryEmail }}</div>
          <div v-if="viewForm.secondaryThreema">Threema: {{ viewForm.secondaryThreema }}</div>
          <div v-if="!viewForm.secondaryTelegram && !viewForm.secondarySignal && !viewForm.secondaryJabber && !viewForm.secondaryEmail && !viewForm.secondaryThreema">-</div>
        </el-descriptions-item>
        <el-descriptions-item label="PGP Public Key" :span="2">
          <div v-if="viewForm.pgpPublicKeyUrl" style="display: flex; align-items: center; gap: 10px;">
            <el-button 
              type="text" 
              icon="el-icon-download" 
              @click="downloadPgpPublicKey(viewForm)"
              style="padding: 0; color: #409EFF;"
            >
              Download PGP Public Key (.asc)
            </el-button>
            <el-button 
              type="text" 
              icon="el-icon-document-copy" 
              @click="copyPgpPublicKeyToClipboard(viewForm)"
              style="padding: 0; color: #67C23A;"
            >
              Copy to Clipboard
            </el-button>
          </div>
          <span v-else>-</span>
        </el-descriptions-item>
        <el-descriptions-item label="BTC Wallet" :span="2">
          <div style="display: flex; align-items: center; gap: 10px;">
            <span>{{ viewForm.btcWallet || '-' }}</span>
            <el-button 
              v-hasPermi="['mall:vendor:withdrawal:address:update']"
              type="text" 
              icon="el-icon-edit" 
              size="mini"
              @click="handleEditWalletAddress('BTC')"
              style="padding: 0; color: #409EFF;"
            >
              Edit
            </el-button>
          </div>
        </el-descriptions-item>
        <el-descriptions-item label="XMR Wallet" :span="2">
          <div style="display: flex; align-items: center; gap: 10px;">
            <span>{{ viewForm.xmrWallet || '-' }}</span>
            <el-button 
              v-hasPermi="['mall:vendor:withdrawal:address:update']"
              type="text" 
              icon="el-icon-edit" 
              size="mini"
              @click="handleEditWalletAddress('XMR')"
              style="padding: 0; color: #409EFF;"
            >
              Edit
            </el-button>
          </div>
        </el-descriptions-item>
        <el-descriptions-item label="USDT Wallet" :span="2">
          <div style="display: flex; align-items: center; gap: 10px;">
            <span>{{ viewForm.usdtWallet || '-' }}</span>
            <el-button 
              v-hasPermi="['mall:vendor:withdrawal:address:update']"
              type="text" 
              icon="el-icon-edit" 
              size="mini"
              @click="handleEditWalletAddress('USDT_TRX')"
              style="padding: 0; color: #409EFF;"
            >
              Edit
            </el-button>
          </div>
        </el-descriptions-item>
        <el-descriptions-item label="Created Time">{{ parseTime(viewForm.createTime) }}</el-descriptions-item>
        <el-descriptions-item label="Approved Time">{{ parseTime(viewForm.approvedTime) || '-' }}</el-descriptions-item>
        <el-descriptions-item label="Approved By" :span="2">{{ viewForm.approvedBy || '-' }}</el-descriptions-item>
        <el-descriptions-item label="Bond Amount">${{ (viewForm.bond || 0).toFixed(2) }}</el-descriptions-item>
        <el-descriptions-item label="Level">Level {{ viewForm.level || 1 }}</el-descriptions-item>
        <el-descriptions-item label="Sales Points">{{ (viewForm.salesPoints || 0).toLocaleString() }}</el-descriptions-item>
        <el-descriptions-item label="Max Sales Limit">${{ ((viewForm.bond || 0) * (viewForm.level || 1)).toFixed(2) }}</el-descriptions-item>
      </el-descriptions>
      <div slot="footer" class="dialog-footer">
        <el-button @click="openView = false">Close</el-button>
      </div>
    </el-dialog>

    <!-- Edit Wallet Address Dialog -->
    <el-dialog 
      title="Update Withdrawal Address" 
      :visible.sync="openWalletAddressEdit" 
      width="600px" 
      append-to-body
    >
      <el-form ref="walletAddressForm" :model="walletAddressForm" :rules="walletAddressRules" label-width="140px">
        <el-alert
          title="Security Warning"
          type="warning"
          :closable="false"
          show-icon
          style="margin-bottom: 20px">
          <div slot="description">
            <p><strong>⚠️ Important:</strong> Changing withdrawal addresses can affect fund security.</p>
            <p>Only Vendor Super Admin can perform this operation.</p>
            <p>Please verify the address carefully before submitting.</p>
          </div>
        </el-alert>
        
        <el-form-item label="Vendor">
          <el-input v-model="walletAddressForm.vendorName" disabled />
        </el-form-item>
        
        <el-form-item label="Currency">
          <el-input :value="walletAddressForm.currencyDisplay" disabled />
        </el-form-item>
        
        <el-form-item label="Current Address">
          <el-input 
            :value="walletAddressForm.currentAddress || 'Not set'" 
            disabled 
            type="textarea"
            :rows="2"
          />
        </el-form-item>
        
        <el-form-item label="New Address" prop="newAddress">
          <el-input 
            v-model="walletAddressForm.newAddress" 
            type="textarea"
            :rows="3"
            placeholder="Enter new withdrawal address"
            maxlength="255"
            show-word-limit
          />
        </el-form-item>
        
        <el-form-item label="Confirmation">
          <el-checkbox v-model="walletAddressForm.confirmed">
            I confirm that I have verified this address and understand the security implications
          </el-checkbox>
        </el-form-item>
      </el-form>
      
      <div slot="footer" class="dialog-footer">
        <el-button @click="openWalletAddressEdit = false">Cancel</el-button>
        <el-button 
          type="primary" 
          @click="submitWalletAddressUpdate"
          :disabled="!walletAddressForm.confirmed"
        >
          Update Address
        </el-button>
      </div>
    </el-dialog>

    <!-- Bond & Level Management Dialog -->
    <el-dialog title="Manage Bond & Level" :visible.sync="openBondLevel" width="600px" append-to-body>
      <el-form ref="bondLevelForm" :model="bondLevelForm" :rules="bondLevelRules" label-width="140px">
        <el-alert
          title="Manual Adjustment"
          type="warning"
          :closable="false"
          show-icon
          style="margin-bottom: 20px">
          <div slot="description">
            <p>You are manually adjusting the vendor's bond, level, and sales points.</p>
            <p><strong>Note:</strong> Level is normally calculated automatically based on sales points:</p>
            <ul style="margin: 5px 0; padding-left: 20px;">
              <li>Level 1: 0-999 points</li>
              <li>Level 2: 1,000-9,999 points</li>
              <li>Level 3: 10,000-99,999 points</li>
              <li>Level 4: 100,000-999,999 points</li>
              <li>Level 5: 1,000,000+ points</li>
            </ul>
            <p><strong>Max Sales Limit = Bond × Level</strong></p>
          </div>
        </el-alert>
        <el-form-item label="Vendor Name">
          <el-input v-model="bondLevelForm.vendorName" disabled />
        </el-form-item>
        <el-form-item label="Bond Amount (USD)" prop="bond">
          <el-input-number
            v-model="bondLevelForm.bond"
            :min="0"
            :precision="2"
            :step="100"
            controls-position="right"
            style="width: 100%"
            placeholder="Enter bond amount"
          />
          <div style="font-size: 12px; color: #909399; margin-top: 5px;">
            Current: ${{ (bondLevelForm.originalBond || 0).toFixed(2) }}
          </div>
        </el-form-item>
        <el-form-item label="Level" prop="level">
          <el-input-number
            v-model="bondLevelForm.level"
            :min="1"
            :max="5"
            :step="1"
            controls-position="right"
            style="width: 100%"
            placeholder="Enter level (1-5)"
          />
          <div style="font-size: 12px; color: #909399; margin-top: 5px;">
            Current: Level {{ bondLevelForm.originalLevel || 1 }}
          </div>
        </el-form-item>
        <el-form-item label="Sales Points" prop="salesPoints">
          <el-input-number
            v-model="bondLevelForm.salesPoints"
            :min="0"
            :step="100"
            controls-position="right"
            style="width: 100%"
            placeholder="Enter sales points"
          />
          <div style="font-size: 12px; color: #909399; margin-top: 5px;">
            Current: {{ (bondLevelForm.originalSalesPoints || 0).toLocaleString() }} points
          </div>
        </el-form-item>
        <el-form-item label="Max Sales Limit">
          <el-input
            :value="'$' + ((bondLevelForm.bond || 0) * (bondLevelForm.level || 1)).toFixed(2)"
            disabled
            style="width: 100%"
          />
          <div style="font-size: 12px; color: #909399; margin-top: 5px;">
            Calculated as: Bond × Level
          </div>
        </el-form-item>
        <el-form-item label="Remark" prop="remark">
          <el-input
            v-model="bondLevelForm.remark"
            type="textarea"
            :rows="3"
            placeholder="Optional: Add a note about this manual adjustment"
          />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitBondLevelForm">Confirm</el-button>
        <el-button @click="cancelBondLevel">Cancel</el-button>
      </div>
    </el-dialog>

    <!-- Create Warehouse Account Dialog -->
    <el-dialog title="Create Warehouse Account" :visible.sync="openWarehouseAccount" width="500px" append-to-body>
      <el-form ref="warehouseAccountForm" :model="warehouseAccountForm" :rules="warehouseAccountRules" label-width="120px">
        <el-form-item label="Vendor" prop="vendorName">
          <el-input v-model="warehouseAccountForm.vendorName" disabled />
        </el-form-item>
        <el-form-item label="Username" prop="username">
          <el-input v-model="warehouseAccountForm.username" placeholder="Enter username" />
        </el-form-item>
        <el-form-item label="Password" prop="password">
          <el-input type="password" v-model="warehouseAccountForm.password" placeholder="Enter password" show-password />
        </el-form-item>
        <el-form-item label="Email" prop="email">
          <el-input v-model="warehouseAccountForm.email" placeholder="Enter email (optional)" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="cancelWarehouseAccount">Cancel</el-button>
        <el-button type="primary" @click="submitWarehouseAccount">Create</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listVendor, getVendor, delVendor, addVendor, updateVendor, updateVendorBondAndLevel, createWarehouseAccount, getVendorWithdrawalAddresses } from "@/api/mall/vendor";
import { updateWalletAddress } from "@/api/mall/withdrawal";

export default {
  name: "Vendor",
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
      // Total count
      total: 0,
      // Vendor table data
      vendorList: [],
      // Dialog title
      title: "",
      // Show dialog
      open: false,
      // Show view dialog
      openView: false,
      // Show bond & level dialog
      openBondLevel: false,
      // Show warehouse account dialog
      openWarehouseAccount: false,
      // Warehouse Account form
      warehouseAccountForm: {
        vendorId: null,
        vendorName: '',
        username: '',
        password: '',
        email: ''
      },
      // Warehouse Account form validation
      warehouseAccountRules: {
        username: [
          { required: true, message: 'Username is required', trigger: 'blur' },
          { min: 5, max: 20, message: 'Username length must be between 5 and 20 characters', trigger: 'blur' }
        ],
        password: [
          { required: true, message: 'Password is required', trigger: 'blur' },
          { min: 6, max: 20, message: 'Password length must be between 6 and 20 characters', trigger: 'blur' }
        ],
        email: [
          { type: 'email', message: 'Please enter a valid email address', trigger: 'blur' }
        ]
      },
      // Wallet address edit dialog
      openWalletAddressEdit: false,
      walletAddressForm: {
        vendorId: null,
        vendorName: '',
        currency: '',
        currencyDisplay: '',
        currentAddress: '',
        newAddress: '',
        confirmed: false
      },
      walletAddressRules: {
        newAddress: [
          { required: true, message: 'New address is required', trigger: 'blur' },
          { min: 10, message: 'Address must be at least 10 characters', trigger: 'blur' }
        ]
      },
      // Bond & Level form
      bondLevelForm: {
        id: null,
        vendorName: '',
        bond: null,
        level: null,
        salesPoints: null,
        originalBond: null,
        originalLevel: null,
        originalSalesPoints: null,
        remark: ''
      },
      // Bond & Level form validation
      bondLevelRules: {
        bond: [
          { required: false, message: 'Bond amount is optional', trigger: 'blur' },
          { type: 'number', min: 0, message: 'Bond amount cannot be negative', trigger: 'blur' }
        ],
        level: [
          { required: false, message: 'Level is optional', trigger: 'blur' },
          { type: 'number', min: 1, max: 5, message: 'Level must be between 1 and 5', trigger: 'blur' }
        ],
        salesPoints: [
          { required: false, message: 'Sales points is optional', trigger: 'blur' },
          { type: 'number', min: 0, message: 'Sales points cannot be negative', trigger: 'blur' }
        ]
      },
      // Query params
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        vendorCode: null,
        vendorName: null,
        status: null,
        location: null
      },
      // Form params
      form: {},
      // View form
      viewForm: {},
      // Form validation
      rules: {
        vendorName: [
          { required: true, message: "Vendor Name is required", trigger: "blur" }
        ],
        location: [
          { required: true, message: "Location is required", trigger: "change" }
        ]
      }
    };
  },
  created() {
    this.getList();
  },
  methods: {
    /** Query vendor list */
    getList() {
      this.loading = true;
      listVendor(this.queryParams).then(response => {
        this.vendorList = response.rows;
        this.total = response.total;
        this.loading = false;
      });
    },
    /** Cancel button */
    cancel() {
      this.open = false;
      this.reset();
    },
    /** Form reset */
    reset() {
      this.form = {
        id: null,
        vendorCode: null,
        vendorName: null,
        description: null,
        location: null,
        contactTelegram: null,
        contactSignal: null,
        contactJabber: null,
        contactEmail: null,
        contactThreema: null,
        secondaryTelegram: null,
        secondarySignal: null,
        secondaryJabber: null,
        secondaryEmail: null,
        secondaryThreema: null,
        pgpPublicKeyUrl: null,
        btcWallet: null,
        xmrWallet: null,
        usdtWallet: null,
        status: 1,
        isFeatured: 0,
        sortOrder: 0,
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
    /** Multiple selection box change */
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.id);
      this.single = selection.length !== 1;
      this.multiple = !selection.length;
    },
    /** Add button operation */
    handleAdd() {
      this.reset();
      this.open = true;
      this.title = "Add Vendor";
    },
    /** Update button operation */
    handleUpdate(row) {
      this.reset();
      const id = row.id || this.ids[0];
      getVendor(id).then(response => {
        this.form = response.data;
        this.open = true;
        this.title = "Edit Vendor";
      });
    },
    /** View button operation */
    handleView(row) {
      const id = row.id;
      getVendor(id).then(response => {
        this.viewForm = response.data;
        // Get withdrawal addresses from mall_vendor_withdrawal_address table
        getVendorWithdrawalAddresses(id).then(addressResponse => {
          const addresses = addressResponse.data || [];
          // Map addresses by currency
          const addressMap = {};
          addresses.forEach(addr => {
            if (addr.address && addr.isActive === 1) {
              addressMap[addr.currency] = addr.address;
            }
          });
          // Update wallet addresses from withdrawal addresses
          this.viewForm.btcWallet = addressMap['BTC'] || this.viewForm.btcWallet || '-';
          this.viewForm.xmrWallet = addressMap['XMR'] || this.viewForm.xmrWallet || '-';
          this.viewForm.usdtWallet = addressMap['USDT_TRX'] || this.viewForm.usdtWallet || '-';
        }).catch(error => {
          console.error('Failed to load withdrawal addresses:', error);
          // If error, keep original wallet addresses from vendor table
        });
        this.openView = true;
      });
    },
    /** Submit button */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.id != null) {
            updateVendor(this.form).then(response => {
              this.$modal.msgSuccess("Updated successfully");
              this.open = false;
              this.getList();
            });
          } else {
            addVendor(this.form).then(response => {
              this.$modal.msgSuccess("Added successfully");
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
      this.$modal.confirm('Are you sure to delete the vendor with ID "' + ids + '"?').then(function() {
        return delVendor(ids);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("Deleted successfully");
      }).catch(() => {});
    },
    /** Status switch change */
    handleStatusChange(row) {
      let text = row.status === 1 ? "enable" : "disable";
      this.$modal.confirm('Are you sure to ' + text + ' vendor "' + row.vendorName + '"?').then(function() {
        return updateVendor(row);
      }).then(() => {
        this.$modal.msgSuccess(text.charAt(0).toUpperCase() + text.slice(1) + "d successfully");
      }).catch(function() {
        row.status = row.status === 0 ? 1 : 0;
      });
    },
    /** Export button operation */
    handleExport() {
      this.download('admin/mall/vendor/export', {
        ...this.queryParams
      }, `vendor_${new Date().getTime()}.xlsx`)
    },
    /** Manage Bond & Level button operation */
    handleManageBondLevel(row) {
      const id = row.id;
      getVendor(id).then(response => {
        const vendor = response.data;
        this.bondLevelForm = {
          id: vendor.id,
          vendorName: vendor.vendorName,
          bond: vendor.bond || 0,
          level: vendor.level || 1,
          salesPoints: vendor.salesPoints || 0,
          originalBond: vendor.bond || 0,
          originalLevel: vendor.level || 1,
          originalSalesPoints: vendor.salesPoints || 0,
          remark: ''
        };
        this.openBondLevel = true;
      });
    },
    /** Cancel Bond & Level dialog */
    cancelBondLevel() {
      this.openBondLevel = false;
      this.resetBondLevelForm();
    },
    /** Reset Bond & Level form */
    resetBondLevelForm() {
      this.bondLevelForm = {
        id: null,
        vendorName: '',
        bond: null,
        level: null,
        salesPoints: null,
        originalBond: null,
        originalLevel: null,
        originalSalesPoints: null,
        remark: ''
      };
      if (this.$refs.bondLevelForm) {
        this.$refs.bondLevelForm.resetFields();
      }
    },
    /** Handle Create Warehouse Account button click */
    handleCreateWarehouseAccount(row) {
      this.warehouseAccountForm = {
        vendorId: row.id,
        vendorName: row.vendorName,
        username: '',
        password: '',
        email: ''
      };
      this.openWarehouseAccount = true;
    },
    /** Cancel Warehouse Account dialog */
    cancelWarehouseAccount() {
      this.openWarehouseAccount = false;
      this.resetWarehouseAccountForm();
    },
    /** Reset Warehouse Account form */
    resetWarehouseAccountForm() {
      this.warehouseAccountForm = {
        vendorId: null,
        vendorName: '',
        username: '',
        password: '',
        email: ''
      };
      if (this.$refs.warehouseAccountForm) {
        this.$refs.warehouseAccountForm.resetFields();
      }
    },
    /** Submit Warehouse Account form */
    submitWarehouseAccount() {
      this.$refs.warehouseAccountForm.validate(valid => {
        if (valid) {
          createWarehouseAccount(
            this.warehouseAccountForm.vendorId,
            this.warehouseAccountForm.username,
            this.warehouseAccountForm.password,
            this.warehouseAccountForm.email || null
          ).then(response => {
            this.$modal.msgSuccess("Warehouse staff account created successfully");
            this.openWarehouseAccount = false;
            this.resetWarehouseAccountForm();
          }).catch(error => {
            // Error message will be shown by the API error handler
          });
        }
      });
    },
    /** Submit Bond & Level form */
    submitBondLevelForm() {
      this.$refs.bondLevelForm.validate(valid => {
        if (valid) {
          // Check if at least one field is being updated
          const bondChanged = this.bondLevelForm.bond !== this.bondLevelForm.originalBond;
          const levelChanged = this.bondLevelForm.level !== this.bondLevelForm.originalLevel;
          const pointsChanged = this.bondLevelForm.salesPoints !== this.bondLevelForm.originalSalesPoints;
          
          if (!bondChanged && !levelChanged && !pointsChanged) {
            this.$modal.msgWarning("No changes detected. Please modify at least one field.");
            return;
          }
          
          // Prepare update parameters (only send changed values)
          const params = {};
          if (bondChanged) {
            params.bond = this.bondLevelForm.bond;
          }
          if (levelChanged) {
            params.level = this.bondLevelForm.level;
          }
          if (pointsChanged) {
            params.salesPoints = this.bondLevelForm.salesPoints;
          }
          
          // Build confirmation message
          let changes = [];
          if (bondChanged) {
            changes.push(`Bond: $${this.bondLevelForm.originalBond.toFixed(2)} → $${this.bondLevelForm.bond.toFixed(2)}`);
          }
          if (levelChanged) {
            changes.push(`Level: ${this.bondLevelForm.originalLevel} → ${this.bondLevelForm.level}`);
          }
          if (pointsChanged) {
            changes.push(`Sales Points: ${this.bondLevelForm.originalSalesPoints.toLocaleString()} → ${this.bondLevelForm.salesPoints.toLocaleString()}`);
          }
          
          const confirmMsg = `Are you sure to update the following?\n\n${changes.join('\n')}\n\nMax Sales Limit will be: $${(this.bondLevelForm.bond * this.bondLevelForm.level).toFixed(2)}`;
          
          this.$modal.confirm(confirmMsg).then(() => {
            return updateVendorBondAndLevel(
              this.bondLevelForm.id,
              params.bond,
              params.level,
              params.salesPoints
            );
          }).then(() => {
            this.$modal.msgSuccess("Bond and level updated successfully");
            this.openBondLevel = false;
            this.getList();
          }).catch(() => {});
        }
      });
    },
    /** Download PGP Public Key as .asc file */
    downloadPgpPublicKey(vendor) {
      if (!vendor.pgpPublicKeyUrl) {
        this.$modal.msgWarning('PGP Public Key not available');
        return;
      }

      // pgpPublicKeyUrl field actually contains the full PGP public key content
      const pgpKeyContent = vendor.pgpPublicKeyUrl;
      
      // Generate filename based on vendor code/name or use default
      let filename = 'pgp_public_key.asc';
      if (vendor.vendorCode) {
        filename = `${vendor.vendorCode}_pgp_public_key.asc`;
      } else if (vendor.vendorName) {
        // Use vendor name if code is not available
        const safeName = vendor.vendorName.replace(/[^a-zA-Z0-9]/g, '_');
        filename = `${safeName}_pgp_public_key.asc`;
      }

      try {
        // Create blob with PGP public key content
        // Content type: application/pgp-keys
        const blob = new Blob([pgpKeyContent], { 
          type: 'application/pgp-keys' 
        });
        const blobUrl = window.URL.createObjectURL(blob);
        
        // Create a temporary anchor element to trigger download
        const link = document.createElement('a');
        link.href = blobUrl;
        link.download = filename;
        link.style.display = 'none';
        
        // Append to body, click, and remove
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        
        // Clean up blob URL after a short delay
        setTimeout(() => {
          window.URL.revokeObjectURL(blobUrl);
        }, 100);
        
        this.$modal.msgSuccess('PGP public key downloaded successfully');
      } catch (error) {
        console.error('Failed to download PGP public key:', error);
        this.$modal.msgError('Failed to download PGP public key');
      }
    },
    /** Copy PGP Public Key to clipboard */
    copyPgpPublicKeyToClipboard(vendor) {
      if (!vendor.pgpPublicKeyUrl) {
        this.$modal.msgWarning('PGP Public Key not available');
        return;
      }

      // pgpPublicKeyUrl field actually contains the full PGP public key content
      const pgpKeyContent = vendor.pgpPublicKeyUrl;
      
      try {
        // Create a temporary textarea element
        const textArea = document.createElement('textarea');
        textArea.value = pgpKeyContent;
        textArea.style.position = 'fixed';
        textArea.style.left = '-999999px';
        textArea.style.top = '-999999px';
        document.body.appendChild(textArea);
        textArea.select();
        textArea.setSelectionRange(0, 99999); // For mobile devices
        
        // Copy to clipboard
        const successful = document.execCommand('copy');
        document.body.removeChild(textArea);
        
        if (successful) {
          this.$modal.msgSuccess('PGP public key copied to clipboard');
        } else {
          // Fallback: Use Clipboard API if available
          if (navigator.clipboard && navigator.clipboard.writeText) {
            navigator.clipboard.writeText(pgpKeyContent).then(() => {
              this.$modal.msgSuccess('PGP public key copied to clipboard');
            }).catch(err => {
              console.error('Failed to copy to clipboard:', err);
              this.$modal.msgError('Failed to copy to clipboard');
            });
          } else {
            this.$modal.msgError('Copy to clipboard not supported in this browser');
          }
        }
      } catch (error) {
        console.error('Failed to copy PGP public key:', error);
        // Fallback: Use Clipboard API if available
        if (navigator.clipboard && navigator.clipboard.writeText) {
          navigator.clipboard.writeText(pgpKeyContent).then(() => {
            this.$modal.msgSuccess('PGP public key copied to clipboard');
          }).catch(err => {
            console.error('Failed to copy to clipboard:', err);
            this.$modal.msgError('Failed to copy to clipboard');
          });
        } else {
          this.$modal.msgError('Failed to copy to clipboard');
        }
      }
    },
    /** 编辑钱包地址 */
    handleEditWalletAddress(currency) {
      const currencyMap = {
        'BTC': 'Bitcoin (BTC)',
        'XMR': 'Monero (XMR)',
        'USDT_TRX': 'USDT (TRON)'
      };
      
      this.walletAddressForm = {
        vendorId: this.viewForm.id,
        vendorName: this.viewForm.vendorName,
        currency: currency,
        currencyDisplay: currencyMap[currency] || currency,
        currentAddress: '',
        newAddress: '',
        confirmed: false
      };
      
      // 根据币种设置当前地址
      if (currency === 'BTC') {
        this.walletAddressForm.currentAddress = this.viewForm.btcWallet || '';
      } else if (currency === 'XMR') {
        this.walletAddressForm.currentAddress = this.viewForm.xmrWallet || '';
      } else if (currency === 'USDT_TRX') {
        this.walletAddressForm.currentAddress = this.viewForm.usdtWallet || '';
      }
      
      this.openWalletAddressEdit = true;
    },
    /** 提交钱包地址更新 */
    submitWalletAddressUpdate() {
      this.$refs.walletAddressForm.validate(valid => {
        if (valid) {
          if (!this.walletAddressForm.confirmed) {
            this.$modal.msgWarning('Please confirm that you have verified the address');
            return;
          }
          
          // 二次确认
          this.$modal.confirm(
            `Are you sure you want to update the ${this.walletAddressForm.currencyDisplay} withdrawal address? This action cannot be undone.`
          ).then(() => {
            updateWalletAddress(
              this.walletAddressForm.vendorId,
              this.walletAddressForm.currency,
              this.walletAddressForm.newAddress
            ).then(response => {
              this.$modal.msgSuccess('Wallet address updated successfully');
              this.openWalletAddressEdit = false;
              // 刷新 View 对话框中的数据
              this.handleView({ id: this.viewForm.id });
            }).catch(error => {
              this.$modal.msgError('Failed to update wallet address: ' + (error.msg || error.message));
            });
          }).catch(() => {});
        }
      });
    }
  }
};
</script>


<template>
  <div class="app-container">
    <!-- 统计卡片 -->
    <el-row :gutter="20" style="margin-bottom: 20px;">
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <i class="el-icon-time stat-icon" style="color: #E6A23C"></i>
            <div class="stat-content">
              <div class="stat-value">{{ statistics.pendingCount || 0 }}</div>
              <div class="stat-label">Pending Requests</div>
              <div class="stat-amount">${{ formatAmount(statistics.pendingAmount) }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <i class="el-icon-loading stat-icon" style="color: #409EFF"></i>
            <div class="stat-content">
              <div class="stat-value">{{ statistics.processingCount || 0 }}</div>
              <div class="stat-label">Processing</div>
              <div class="stat-amount">${{ formatAmount(statistics.processingAmount) }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <i class="el-icon-success stat-icon" style="color: #67C23A"></i>
            <div class="stat-content">
              <div class="stat-value">{{ statistics.completedCount || 0 }}</div>
              <div class="stat-label">Completed</div>
              <div class="stat-amount">${{ formatAmount(statistics.completedAmount) }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <i class="el-icon-wallet stat-icon" style="color: #606266"></i>
            <div class="stat-content">
              <div class="stat-value">{{ statistics.totalCount || 0 }}</div>
              <div class="stat-label">Total Requests</div>
              <div class="stat-amount">${{ formatAmount(statistics.totalAmount) }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 查询表单 -->
    <el-form :model="queryParams" ref="queryForm" :inline="true" label-width="100px">
      <el-form-item label="Request Code" prop="requestCode">
        <el-input
          v-model="queryParams.requestCode"
          placeholder="Enter request code"
          clearable
          size="small"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="Vendor ID" prop="vendorId">
        <el-input
          v-model="queryParams.vendorId"
          placeholder="Enter vendor ID"
          clearable
          size="small"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="Status" prop="requestStatus">
        <el-select v-model="queryParams.requestStatus" placeholder="Select status" clearable size="small">
          <el-option label="Pending" value="PENDING" />
          <el-option label="Approved" value="APPROVED" />
          <el-option label="Processing" value="PROCESSING" />
          <el-option label="Completed" value="COMPLETED" />
          <el-option label="Rejected" value="REJECTED" />
          <el-option label="Failed" value="FAILED" />
        </el-select>
      </el-form-item>
      <el-form-item label="Currency" prop="currency">
        <el-select v-model="queryParams.currency" placeholder="Select currency" clearable size="small">
          <el-option label="BTC" value="BTC" />
          <el-option label="XMR" value="XMR" />
          <el-option label="USDT_TRX" value="USDT_TRX" />
          <el-option label="USDT_ERC" value="USDT_ERC" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">Search</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">Reset</el-button>
        <el-button type="warning" icon="el-icon-refresh-right" size="mini" @click="handleReleaseBalance">Release Pending Balance</el-button>
      </el-form-item>
    </el-form>

    <!-- 提现请求列表 -->
    <el-table v-loading="loading" :data="requestList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="Request Code" align="center" prop="requestCode" />
      <el-table-column label="Vendor ID" align="center" prop="vendorId" width="100" />
      <el-table-column label="Currency" align="center" prop="currency" width="100" />
      <el-table-column label="Amount" align="center" prop="amount" width="120">
        <template slot-scope="scope">
          <span>{{ formatAmount(scope.row.amount) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="Withdrawal Address" align="center" prop="withdrawalAddress" :show-overflow-tooltip="true" />
      <el-table-column label="Status" align="center" prop="requestStatus" width="120">
        <template slot-scope="scope">
          <el-tag :type="getStatusType(scope.row.requestStatus)">{{ scope.row.requestStatus }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="Request Time" align="center" prop="createTime" width="160">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="Actions" align="center" class-name="small-padding fixed-width" width="280">
        <template slot-scope="scope">
          <!-- Normal Admin: Approve/Reject PENDING requests -->
          <el-button
            v-if="scope.row.requestStatus === 'PENDING'"
            v-hasPermi="['mall:vendor:withdrawal:approve']"
            size="mini"
            type="success"
            icon="el-icon-check"
            @click="handleApprove(scope.row)"
          >Approve</el-button>
          <el-button
            v-if="scope.row.requestStatus === 'PENDING'"
            v-hasPermi="['mall:vendor:withdrawal:reject']"
            size="mini"
            type="danger"
            icon="el-icon-close"
            @click="handleReject(scope.row)"
          >Reject</el-button>
          <!-- Pay Admin: Process APPROVED requests -->
          <el-button
            v-if="scope.row.requestStatus === 'APPROVED'"
            v-hasPermi="['mall:vendor:withdrawal:process']"
            size="mini"
            type="primary"
            icon="el-icon-s-promotion"
            @click="handleProcessing(scope.row)"
          >Process</el-button>
          <!-- Pay Admin: Complete/Fail PROCESSING requests -->
          <el-button
            v-if="scope.row.requestStatus === 'PROCESSING'"
            v-hasPermi="['mall:vendor:withdrawal:complete']"
            size="mini"
            type="success"
            icon="el-icon-success"
            @click="handleComplete(scope.row)"
          >Complete</el-button>
          <el-button
            v-if="scope.row.requestStatus === 'PROCESSING'"
            v-hasPermi="['mall:vendor:withdrawal:fail']"
            size="mini"
            type="warning"
            icon="el-icon-error"
            @click="handleFail(scope.row)"
          >Fail</el-button>
          <el-button
            size="mini"
            icon="el-icon-view"
            @click="handleView(scope.row)"
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

    <!-- 审批对话框 -->
    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="500px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="120px">
        <el-form-item label="Request Code">
          <el-input v-model="currentRequest.requestCode" :disabled="true" />
        </el-form-item>
        <el-form-item label="Amount">
          <el-input :value="formatAmount(currentRequest.amount) + ' ' + currentRequest.currency" :disabled="true" />
        </el-form-item>
        <el-form-item v-if="dialogType === 'approve'" label="Remark" prop="remark">
          <el-input v-model="form.remark" type="textarea" placeholder="Enter approval remark (optional)" />
        </el-form-item>
        <el-form-item v-if="dialogType === 'reject'" label="Reason" prop="reason">
          <el-input v-model="form.reason" type="textarea" placeholder="Enter rejection reason" />
        </el-form-item>
        <el-form-item v-if="dialogType === 'processing'" label="Vendor Address">
          <el-alert
            :title="'Requested Address: ' + currentRequest.withdrawalAddress"
            type="info"
            :closable="false"
            style="margin-bottom: 10px;"
          />
          <el-alert
            v-if="form.verifiedAddress"
            :title="'Verified Address: ' + form.verifiedAddress"
            :type="form.addressMatch ? 'success' : 'warning'"
            :closable="false"
            style="margin-bottom: 10px;"
          >
            <template slot="title">
              <span>Verified Address: {{ form.verifiedAddress }}</span>
              <el-tag v-if="form.addressMatch" type="success" size="mini" style="margin-left: 10px;">Match</el-tag>
              <el-tag v-else type="warning" size="mini" style="margin-left: 10px;">Mismatch</el-tag>
            </template>
          </el-alert>
          <el-button size="small" @click="verifyAddress" style="margin-bottom: 10px;">Verify Address</el-button>
        </el-form-item>
        <el-form-item v-if="dialogType === 'processing'" label="TX Hash" prop="txHash">
          <el-input v-model="form.txHash" placeholder="Enter transaction hash" />
        </el-form-item>
        <el-form-item v-if="dialogType === 'complete'" label="TX Fee" prop="txFee">
          <el-input v-model="form.txFee" placeholder="Enter transaction fee (optional)" />
        </el-form-item>
        <el-form-item v-if="dialogType === 'fail'" label="Reason" prop="reason">
          <el-input v-model="form.reason" type="textarea" placeholder="Enter failure reason" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">Cancel</el-button>
        <el-button type="primary" @click="submitForm">Confirm</el-button>
      </div>
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog title="Withdrawal Request Details" :visible.sync="detailVisible" width="700px" append-to-body>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="Request Code">{{ currentRequest.requestCode }}</el-descriptions-item>
        <el-descriptions-item label="Vendor ID">{{ currentRequest.vendorId }}</el-descriptions-item>
        <el-descriptions-item label="Currency">{{ currentRequest.currency }}</el-descriptions-item>
        <el-descriptions-item label="Amount">{{ formatAmount(currentRequest.amount) }}</el-descriptions-item>
        <el-descriptions-item label="Status">
          <el-tag :type="getStatusType(currentRequest.requestStatus)">{{ currentRequest.requestStatus }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="Withdrawal Address" :span="2">{{ currentRequest.withdrawalAddress }}</el-descriptions-item>
        <el-descriptions-item label="Request Time" :span="2">{{ parseTime(currentRequest.createTime) }}</el-descriptions-item>
        <el-descriptions-item v-if="currentRequest.approveBy" label="Approved By">{{ currentRequest.approveBy }}</el-descriptions-item>
        <el-descriptions-item v-if="currentRequest.approveTime" label="Approve Time">{{ parseTime(currentRequest.approveTime) }}</el-descriptions-item>
        <el-descriptions-item v-if="currentRequest.approveRemark" label="Approve Remark" :span="2">{{ currentRequest.approveRemark }}</el-descriptions-item>
        <el-descriptions-item v-if="currentRequest.rejectReason" label="Reject Reason" :span="2">{{ currentRequest.rejectReason }}</el-descriptions-item>
        <el-descriptions-item v-if="currentRequest.txHash" label="TX Hash" :span="2">{{ currentRequest.txHash }}</el-descriptions-item>
        <el-descriptions-item v-if="currentRequest.txTime" label="TX Time">{{ parseTime(currentRequest.txTime) }}</el-descriptions-item>
        <el-descriptions-item v-if="currentRequest.txFee" label="TX Fee">{{ formatAmount(currentRequest.txFee) }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script>
import { getAllRequests, approveRequest, rejectRequest, markProcessing, markCompleted, markFailed, getStatistics, releasePendingBalance, getVendorAddresses, updateWalletAddress } from '@/api/mall/withdrawal'

export default {
  name: 'VendorWithdrawal',
  data() {
    return {
      loading: true,
      requestList: [],
      total: 0,
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        requestCode: null,
        vendorId: null,
        requestStatus: null,
        currency: null
      },
      statistics: {},
      dialogVisible: false,
      detailVisible: false,
      dialogType: '',
      dialogTitle: '',
      currentRequest: {},
      form: {
        verifiedAddress: null,
        addressMatch: false
      },
      rules: {
        reason: [
          { required: true, message: 'Reason is required', trigger: 'blur' }
        ],
        txHash: [
          { required: true, message: 'Transaction hash is required', trigger: 'blur' }
        ]
      }
    }
  },
  created() {
    this.getList()
    this.getStatistics()
  },
  methods: {
    getList() {
      this.loading = true
      getAllRequests(this.queryParams).then(response => {
        this.requestList = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    getStatistics() {
      getStatistics().then(response => {
        this.statistics = response.data
      })
    },
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    resetQuery() {
      this.resetForm('queryForm')
      this.handleQuery()
    },
    handleSelectionChange(selection) {
      // TODO: 批量操作
    },
    getStatusType(status) {
      const typeMap = {
        PENDING: 'warning',
        APPROVED: 'info',
        PROCESSING: 'primary',
        COMPLETED: 'success',
        REJECTED: 'danger',
        FAILED: 'danger'
      }
      return typeMap[status] || 'info'
    },
    formatAmount(amount) {
      return amount ? parseFloat(amount).toFixed(2) : '0.00'
    },
    handleApprove(row) {
      this.currentRequest = row
      this.dialogType = 'approve'
      this.dialogTitle = 'Approve Withdrawal Request'
      this.form = { remark: '', verifiedAddress: null, addressMatch: false }
      this.dialogVisible = true
    },
    handleReject(row) {
      this.currentRequest = row
      this.dialogType = 'reject'
      this.dialogTitle = 'Reject Withdrawal Request'
      this.form = { reason: '', verifiedAddress: null, addressMatch: false }
      this.dialogVisible = true
    },
    handleProcessing(row) {
      this.currentRequest = row
      this.dialogType = 'processing'
      this.dialogTitle = 'Mark as Processing (Verify Address & Record TXID)'
      this.form = { txHash: '', verifiedAddress: null, addressMatch: false }
      // Auto-load vendor address for verification
      this.verifyAddress()
      this.dialogVisible = true
    },
    async verifyAddress() {
      if (!this.currentRequest.vendorId || !this.currentRequest.currency) {
        this.$message.warning('Vendor ID or currency not available')
        return
      }
      try {
        const response = await getVendorAddresses(this.currentRequest.vendorId)
        if (response.code === 200 && response.data) {
          const addresses = response.data
          const addressForCurrency = addresses.find(addr => addr.currency === this.currentRequest.currency)
          if (addressForCurrency) {
            this.form.verifiedAddress = addressForCurrency.address
            this.form.addressMatch = addressForCurrency.address === this.currentRequest.withdrawalAddress
            if (!this.form.addressMatch) {
              this.$message.warning('Address mismatch! Please verify before processing.')
            } else {
              this.$message.success('Address verified and matches')
            }
          } else {
            this.$message.warning('No withdrawal address found for this currency')
          }
        }
      } catch (error) {
        this.$message.error('Failed to load vendor address: ' + (error.msg || error.message))
      }
    },
    handleComplete(row) {
      this.currentRequest = row
      this.dialogType = 'complete'
      this.dialogTitle = 'Mark as Completed'
      this.form = { txFee: '0', verifiedAddress: null, addressMatch: false }
      this.dialogVisible = true
    },
    handleFail(row) {
      this.currentRequest = row
      this.dialogType = 'fail'
      this.dialogTitle = 'Mark as Failed'
      this.form = { reason: '', verifiedAddress: null, addressMatch: false }
      this.dialogVisible = true
    },
    handleView(row) {
      this.currentRequest = row
      this.detailVisible = true
    },
    submitForm() {
      this.$refs.form.validate(valid => {
        if (valid) {
          let promise
          switch (this.dialogType) {
            case 'approve':
              promise = approveRequest(this.currentRequest.id, this.form.remark)
              break
            case 'reject':
              promise = rejectRequest(this.currentRequest.id, this.form.reason)
              break
            case 'processing':
              promise = markProcessing(this.currentRequest.id, this.form.txHash)
              break
            case 'complete':
              promise = markCompleted(this.currentRequest.id, this.form.txFee || 0)
              break
            case 'fail':
              promise = markFailed(this.currentRequest.id, this.form.reason)
              break
          }
          
          promise.then(() => {
            this.$message.success('Operation successful')
            this.dialogVisible = false
            this.getList()
            this.getStatistics()
          }).catch(() => {
            this.$message.error('Operation failed')
          })
        }
      })
    },
    handleReleaseBalance() {
      this.$confirm('This will release expired pending balance for all vendors. Continue?', 'Warning', {
        confirmButtonText: 'Confirm',
        cancelButtonText: 'Cancel',
        type: 'warning'
      }).then(() => {
        releasePendingBalance().then(() => {
          this.$message.success('Balance released successfully')
          this.getStatistics()
        }).catch(() => {
          this.$message.error('Failed to release balance')
        })
      })
    }
  }
}
</script>

<style scoped>
.stat-card {
  display: flex;
  align-items: center;
}

.stat-icon {
  font-size: 40px;
  margin-right: 15px;
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
}

.stat-label {
  font-size: 13px;
  color: #909399;
  margin-top: 5px;
}

.stat-amount {
  font-size: 14px;
  color: #67C23A;
  margin-top: 3px;
}
</style>


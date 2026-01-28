<template>
  <div class="app-container">
    <el-tabs v-model="activeTab" type="card" @tab-click="handleTabChange">
      <el-tab-pane label="Applications" name="applications">
        <div class="tab-pane-content">
    <el-form :model="queryParams" ref="queryForm" :inline="true" v-show="showSearch" label-width="120px">
      <el-form-item label="Application ID" prop="applicationId">
        <el-input
          v-model="queryParams.applicationId"
          placeholder="Please enter Application ID"
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
          <el-option label="Pending" value="pending" />
          <el-option label="Under Review" value="under_review" />
          <el-option label="Interview Required" value="interview_required" />
          <el-option label="Approved" value="approved" />
          <el-option label="Rejected" value="rejected" />
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
          v-hasPermi="['mall:vendor:application:add']"
        >New Application</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="el-icon-delete"
          size="mini"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['mall:vendor:application:remove']"
        >Delete</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['mall:vendor:application:export']"
        >Export</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="applicationList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="Application ID" align="center" prop="applicationId" width="160" :show-overflow-tooltip="true" />
      <el-table-column label="Vendor Name" align="center" prop="vendorName" width="150" :show-overflow-tooltip="true" />
      <el-table-column label="Location" align="center" prop="location" width="130" />
      <el-table-column label="Stock Volume" align="center" prop="stockVolume" width="110">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.stockVolume === 'small'" type="info" size="small">Small</el-tag>
          <el-tag v-else-if="scope.row.stockVolume === 'medium'" type="success" size="small">Medium</el-tag>
          <el-tag v-else-if="scope.row.stockVolume === 'large'" type="warning" size="small">Large</el-tag>
          <el-tag v-else-if="scope.row.stockVolume === 'xlarge'" type="danger" size="small">X-Large</el-tag>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="Status" align="center" prop="status" width="130">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.status === 'pending'" type="info" size="small">Pending</el-tag>
          <el-tag v-else-if="scope.row.status === 'under_review'" type="warning" size="small">Under Review</el-tag>
          <el-tag v-else-if="scope.row.status === 'interview_required'" size="small">Interview Required</el-tag>
          <el-tag v-else-if="scope.row.status === 'approved'" type="success" size="small">Approved</el-tag>
          <el-tag v-else-if="scope.row.status === 'rejected'" type="danger" size="small">Rejected</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="Review Progress" align="center" prop="reviewProgress" width="150">
        <template slot-scope="scope">
          <el-progress :percentage="scope.row.reviewProgress || 0" :color="customColors"></el-progress>
        </template>
      </el-table-column>
      <el-table-column label="BOND Status" align="center" prop="bondOrderId" width="180">
        <template slot-scope="scope">
          <div v-if="scope.row.bondOrderId" style="text-align: left; padding-left: 10px;">
            <div style="margin-bottom: 4px;">
              <el-tag size="mini" :type="getBondOrderStatusType(scope.row.bondOrderInfo)">
                {{ getBondOrderStatusText(scope.row.bondOrderInfo) }}
              </el-tag>
            </div>
            <div style="font-size: 12px; color: #606266;">
              <strong>Level:</strong> {{ scope.row.bondLevel || 'N/A' }}
            </div>
            <div v-if="scope.row.bondOrderInfo" style="font-size: 12px; color: #606266;">
              <strong>Amount:</strong> ${{ (scope.row.bondOrderInfo.totalAmount || 0).toFixed(2) }}
            </div>
            <div v-if="scope.row.bondPaidTime" style="font-size: 11px; color: #909399;">
              {{ parseTime(scope.row.bondPaidTime, '{y}-{m}-{d}') }}
            </div>
            <el-button 
              size="mini" 
              type="text" 
              @click="handleViewBondOrder(scope.row)"
              style="padding: 2px 0; font-size: 11px;"
            >
              View Order
            </el-button>
          </div>
          <div v-else style="color: #909399;">
            <el-tag size="mini" type="info">Not Paid</el-tag>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="Applied Time" align="center" prop="createTime" width="160">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime, '{y}-{m}-{d} {h}:{i}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="Actions" align="center" class-name="small-padding fixed-width" width="280">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-view"
            @click="handleView(scope.row)"
            v-hasPermi="['mall:vendor:application:query']"
          >View</el-button>
          
          <!-- Start Review button - only show for pending status -->
          <el-button
            v-if="scope.row.status === 'pending'"
            size="mini"
            type="text"
            icon="el-icon-s-promotion"
            @click="handleStartReview(scope.row)"
            v-hasPermi="['mall:vendor:application:edit']"
            style="color: #409EFF"
          >Start Review</el-button>
          
          <!-- Interview button -->
          <el-button
            v-if="scope.row.status === 'pending' || scope.row.status === 'under_review'"
            size="mini"
            type="text"
            icon="el-icon-user"
            @click="handleInterview(scope.row)"
            v-hasPermi="['mall:vendor:application:edit']"
            style="color: #E6A23C"
          >Interview</el-button>
          
          <!-- Note: Co-op Members book their own interview slots on the status page -->
          <!-- Admin should Approve/Reject after the interview is completed -->
          
          <!-- Approve button - now includes interview_required status -->
          <el-button
            v-if="scope.row.status === 'pending' || scope.row.status === 'under_review' || scope.row.status === 'interview_required'"
            size="mini"
            type="text"
            icon="el-icon-check"
            @click="handleApprove(scope.row)"
            v-hasPermi="['mall:vendor:application:approve']"
            style="color: #67C23A"
          >Approve</el-button>
          
          <!-- Reject button - now includes interview_required status -->
          <el-button
            v-if="scope.row.status === 'pending' || scope.row.status === 'under_review' || scope.row.status === 'interview_required'"
            size="mini"
            type="text"
            icon="el-icon-close"
            @click="handleReject(scope.row)"
            v-hasPermi="['mall:vendor:application:reject']"
            style="color: #F56C6C"
          >Reject</el-button>
          
          <!-- Delete button -->
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['mall:vendor:application:remove']"
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
        </div>
      </el-tab-pane>
      
      <!-- Interview Schedule Tab -->
      <el-tab-pane label="Interview Schedule" name="interviews">
        <div class="tab-pane-content">
          <!-- Interview Toolbar -->
          <el-row :gutter="10" class="mb8">
            <el-col :span="1.5">
              <el-button
                type="primary"
                plain
                icon="el-icon-plus"
                size="mini"
                @click="handleScheduleInterviewFromTab"
                v-hasPermi="['mall:vendor:interview:add']"
              >Schedule Interview</el-button>
            </el-col>
            <el-col :span="1.5">
              <el-button
                type="success"
                plain
                icon="el-icon-time"
                size="mini"
                @click="handleManageSlots"
                v-hasPermi="['mall:vendor:interview:add']"
              >Manage Available Slots</el-button>
            </el-col>
            <el-col :span="1.5">
              <el-button
                icon="el-icon-refresh"
                size="mini"
                @click="getInterviewList"
              >Refresh</el-button>
            </el-col>
            <el-col :span="1.5">
              <el-select v-model="interviewQueryParams.status" size="mini" @change="getInterviewList" clearable placeholder="Filter Status">
                <el-option label="All" value="" />
                <el-option label="Scheduled" value="scheduled" />
                <el-option label="Confirmed" value="confirmed" />
                <el-option label="Completed" value="completed" />
                <el-option label="Cancelled" value="cancelled" />
              </el-select>
            </el-col>
            <el-col :span="1.5">
              <el-radio-group v-model="interviewViewMode" size="mini" @change="changeInterviewViewMode">
                <el-radio-button label="list">
                  <i class="el-icon-s-order"></i> List
                </el-radio-button>
                <el-radio-button label="calendar">
                  <i class="el-icon-date"></i> Calendar
                </el-radio-button>
              </el-radio-group>
            </el-col>
            <right-toolbar :showSearch.sync="showInterviewSearch" @queryTable="getInterviewList"></right-toolbar>
          </el-row>

          <!-- Interview Filter Form -->
          <el-form :model="interviewQueryParams" ref="interviewQueryForm" :inline="true" v-show="showInterviewSearch" label-width="120px">
            <el-form-item label="Application ID" prop="applicationNumber">
              <el-input
                v-model="interviewQueryParams.applicationNumber"
                placeholder="Application ID"
                clearable
                size="small"
                @keyup.enter.native="handleInterviewQuery"
              />
            </el-form-item>
            <el-form-item label="Vendor Name" prop="vendorName">
              <el-input
                v-model="interviewQueryParams.vendorName"
                placeholder="Vendor Name"
                clearable
                size="small"
                @keyup.enter.native="handleInterviewQuery"
              />
            </el-form-item>
            <el-form-item label="Interview Date" prop="interviewDate">
              <el-date-picker
                v-model="interviewDateRange"
                size="small"
                style="width: 240px"
                value-format="yyyy-MM-dd"
                type="daterange"
                range-separator="-"
                start-placeholder="Start Date"
                end-placeholder="End Date"
              ></el-date-picker>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" icon="el-icon-search" size="mini" @click="handleInterviewQuery">Search</el-button>
              <el-button icon="el-icon-refresh" size="mini" @click="resetInterviewQuery">Reset</el-button>
            </el-form-item>
          </el-form>

          <!-- Calendar View -->
          <div v-if="interviewViewMode === 'calendar'" class="calendar-container">
            <el-calendar v-model="interviewCalendarDate">
              <template slot="dateCell" slot-scope="{date, data}">
                <div class="calendar-day">
                  <div class="day-number">{{ data.day.split('-').slice(2).join('-') }}</div>
                  <div class="interviews-list">
                    <div 
                      v-for="interview in getInterviewsForDate(date)" 
                      :key="interview.id"
                      :class="['interview-badge', `status-${interview.status}`]"
                      @click="handleViewInterview(interview)"
                    >
                      <div class="badge-time">{{ formatInterviewTimeShort(interview.interviewTime) }}</div>
                      <div class="badge-vendor">{{ truncateText(interview.vendorName, 12) }}</div>
                      <el-tag 
                        :type="getInterviewStatusType(interview.status)" 
                        size="mini"
                        class="badge-status"
                      >
                        {{ formatInterviewStatusShort(interview.status) }}
                      </el-tag>
                    </div>
                    <div v-if="getInterviewsForDate(date).length === 0" class="no-interviews"></div>
                  </div>
                </div>
              </template>
            </el-calendar>
          </div>

          <!-- Interview List Table -->
          <el-table v-if="interviewViewMode === 'list'" v-loading="interviewLoading" :data="interviewList">
            <el-table-column label="Application ID" align="center" prop="applicationNumber" width="160" />
            <el-table-column label="Vendor Name" align="center" prop="vendorName" width="150" />
            <el-table-column label="Interview Date" align="center" prop="interviewDatetime" width="110">
              <template slot-scope="scope">
                <span>{{ parseTime(scope.row.interviewDatetime, '{y}-{m}-{d}') }}</span>
              </template>
            </el-table-column>
            <el-table-column label="Time" align="center" prop="interviewTime" width="80" />
            <el-table-column label="Duration" align="center" prop="durationMinutes" width="80">
              <template slot-scope="scope">
                <span>{{ scope.row.durationMinutes }} min</span>
              </template>
            </el-table-column>
            <el-table-column label="Platform" align="center" prop="platform" width="100" />
            <el-table-column label="Interviewer" align="center" prop="interviewerName" width="100" />
            <el-table-column label="Status" align="center" prop="status" width="120">
              <template slot-scope="scope">
                <el-tag :type="getInterviewStatusType(scope.row.status)" size="small">
                  {{ formatInterviewStatus(scope.row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="Confirmed" align="center" prop="vendorConfirmed" width="90">
              <template slot-scope="scope">
                <el-tag v-if="scope.row.vendorConfirmed" type="success" size="small">Yes</el-tag>
                <el-tag v-else type="info" size="small">No</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="Actions" align="center" class-name="small-padding fixed-width" width="250">
              <template slot-scope="scope">
                <el-button
                  size="mini"
                  type="text"
                  icon="el-icon-view"
                  @click="handleViewInterview(scope.row)"
                >View</el-button>
                <el-button
                  v-if="scope.row.status === 'scheduled' || scope.row.status === 'confirmed'"
                  size="mini"
                  type="text"
                  icon="el-icon-check"
                  @click="handleCompleteInterview(scope.row)"
                  style="color: #67C23A"
                >Complete</el-button>
                <el-button
                  v-if="scope.row.status !== 'completed' && scope.row.status !== 'cancelled'"
                  size="mini"
                  type="text"
                  icon="el-icon-close"
                  @click="handleCancelInterview(scope.row)"
                  style="color: #F56C6C"
                >Cancel</el-button>
              </template>
            </el-table-column>
          </el-table>

          <pagination
            v-show="interviewTotal>0"
            :total="interviewTotal"
            :page.sync="interviewQueryParams.pageNum"
            :limit.sync="interviewQueryParams.pageSize"
            @pagination="getInterviewList"
          />
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- View Details Dialog -->
    <el-dialog :title="dialogTitle" :visible.sync="openView" width="900px" append-to-body>
      <el-descriptions :column="2" border size="medium">
        <el-descriptions-item label="Application ID">{{ form.applicationId }}</el-descriptions-item>
        <el-descriptions-item label="Vendor Name">{{ form.vendorName }}</el-descriptions-item>
        <el-descriptions-item label="Market Experience">{{ form.hasMarketExperience ? 'Yes' : 'No' }}</el-descriptions-item>
        <el-descriptions-item label="Existing Markets">{{ form.existingMarkets || '-' }}</el-descriptions-item>
        <el-descriptions-item label="Experience Years">{{ form.experienceYears || '-' }}</el-descriptions-item>
        <el-descriptions-item label="Location">{{ form.location }}</el-descriptions-item>
        <el-descriptions-item label="Stock Volume">{{ formatStockVolume(form.stockVolume) }}</el-descriptions-item>
        <el-descriptions-item label="Offline Delivery">{{ form.offlineDelivery ? 'Yes' : 'No' }}</el-descriptions-item>
        <el-descriptions-item label="PGP Signature" :span="2">
          <div v-if="form.pgpSignatureUrl" style="display: flex; align-items: center; gap: 10px;">
            <el-button 
              type="text" 
              icon="el-icon-download" 
              @click="downloadPgpSignature(form)"
              style="padding: 0; color: #409EFF;"
            >
              Download PGP Public Key (.asc)
            </el-button>
            <el-button 
              type="text" 
              icon="el-icon-document-copy" 
              @click="copyPgpSignatureToClipboard(form)"
              style="padding: 0; color: #67C23A;"
            >
              Copy to Clipboard
            </el-button>
          </div>
          <span v-else>-</span>
        </el-descriptions-item>
        <el-descriptions-item label="BTC Wallet" :span="2">{{ form.btcWallet || '-' }}</el-descriptions-item>
        <el-descriptions-item label="XMR Wallet" :span="2">{{ form.xmrWallet || '-' }}</el-descriptions-item>
        <el-descriptions-item label="USDT Wallet" :span="2">{{ form.usdtWallet || '-' }}</el-descriptions-item>
        <el-descriptions-item label="Primary Contact" :span="2">
          <div v-if="form.primaryTelegram">Telegram: {{ form.primaryTelegram }}</div>
          <div v-if="form.primarySignal">Signal: {{ form.primarySignal }}</div>
          <div v-if="form.primaryJabber">Jabber: {{ form.primaryJabber }}</div>
          <div v-if="form.primaryEmail">Email: {{ form.primaryEmail }}</div>
          <div v-if="form.primaryThreema">Threema: {{ form.primaryThreema }}</div>
          <div v-if="!form.primaryTelegram && !form.primarySignal && !form.primaryJabber && !form.primaryEmail && !form.primaryThreema">-</div>
        </el-descriptions-item>
        <el-descriptions-item label="Secondary Contact" :span="2">
          <div v-if="form.secondaryTelegram">Telegram: {{ form.secondaryTelegram }}</div>
          <div v-if="form.secondarySignal">Signal: {{ form.secondarySignal }}</div>
          <div v-if="form.secondaryJabber">Jabber: {{ form.secondaryJabber }}</div>
          <div v-if="form.secondaryEmail">Email: {{ form.secondaryEmail }}</div>
          <div v-if="form.secondaryThreema">Threema: {{ form.secondaryThreema }}</div>
          <div v-if="!form.secondaryTelegram && !form.secondarySignal && !form.secondaryJabber && !form.secondaryEmail && !form.secondaryThreema">-</div>
        </el-descriptions-item>
        <el-descriptions-item label="Product Description" :span="2">{{ form.productDescription || '-' }}</el-descriptions-item>
        <el-descriptions-item label="Status">{{ statusFormat(form) }}</el-descriptions-item>
        <el-descriptions-item label="Applied Time">{{ parseTime(form.createTime) }}</el-descriptions-item>
        <el-descriptions-item label="Review Notes" :span="2" v-if="form.reviewNotes">{{ form.reviewNotes }}</el-descriptions-item>
      </el-descriptions>
      <div slot="footer" class="dialog-footer">
        <el-button @click="openView = false">Close</el-button>
        <!-- Show Verify Wallets button if application is approved and bond is paid -->
        <el-button 
          v-if="form.status === 'approved' && form.bondOrderId && !form.walletVerifiedTime" 
          type="success" 
          @click="handleVerifyWallets">
          Verify Wallets
        </el-button>
      </div>
    </el-dialog>

    <!-- Verify Wallets Dialog -->
    <el-dialog title="Verify Co-op Member Withdrawal Wallets" :visible.sync="openVerifyWallets" width="700px" append-to-body>
      <el-alert
        title="Wallet Verification"
        type="warning"
        description="Please verify the provided wallet addresses before activating the Co-op Member account. Once verified, the member can start withdrawing funds to these wallets."
        :closable="false"
        show-icon
        style="margin-bottom: 20px;"
      />
      
      <el-form ref="verifyWalletsForm" :model="verifyWalletsForm" label-width="150px">
        <el-form-item label="BTC Wallet">
          <el-input v-model="verifyWalletsForm.btcWallet" readonly>
            <template slot="append">
              <el-button @click="copyToClipboard(verifyWalletsForm.btcWallet)" icon="el-icon-document-copy"></el-button>
            </template>
          </el-input>
        </el-form-item>
        
        <el-form-item label="XMR Wallet">
          <el-input v-model="verifyWalletsForm.xmrWallet" readonly>
            <template slot="append">
              <el-button @click="copyToClipboard(verifyWalletsForm.xmrWallet)" icon="el-icon-document-copy"></el-button>
            </template>
          </el-input>
        </el-form-item>
        
        <el-form-item label="USDT Wallet">
          <el-input v-model="verifyWalletsForm.usdtWallet" readonly>
            <template slot="append">
              <el-button @click="copyToClipboard(verifyWalletsForm.usdtWallet)" icon="el-icon-document-copy"></el-button>
            </template>
          </el-input>
        </el-form-item>
        
        <el-divider></el-divider>
        
        <el-form-item label="Bond Status">
          <el-tag type="success" v-if="verifyWalletsForm.bondOrderId">Paid (Order: {{ verifyWalletsForm.bondOrderId }})</el-tag>
          <el-tag type="warning" v-else>Not Paid</el-tag>
        </el-form-item>
        
        <el-form-item label="Bond Paid Time">
          {{ parseTime(verifyWalletsForm.bondPaidTime) || '-' }}
        </el-form-item>
        
        <el-form-item label="Verification Notes">
          <el-input 
            v-model="verifyWalletsForm.notes" 
            type="textarea" 
            :rows="3" 
            placeholder="Enter any notes about the wallet verification (optional)"
          />
        </el-form-item>
        
        <el-alert
          title="Confirmation Required"
          type="info"
          :closable="false"
          style="margin-top: 10px;"
        >
          <div slot="default">
            <el-checkbox v-model="verifyWalletsForm.confirmed" style="margin-top: 10px;">
              I have verified that the wallet addresses are valid and belong to the Co-op Member
            </el-checkbox>
          </div>
        </el-alert>
      </el-form>
      
      <div slot="footer" class="dialog-footer">
        <el-button @click="openVerifyWallets = false">Cancel</el-button>
        <el-button 
          type="success" 
          @click="submitVerifyWallets" 
          :disabled="!verifyWalletsForm.confirmed">
          <i class="el-icon-success"></i> Confirm Verification & Activate Account
        </el-button>
      </div>
    </el-dialog>

    <!-- Review Dialog -->
    <el-dialog :title="reviewDialogTitle" :visible.sync="openReview" width="600px" append-to-body>
      <el-form ref="reviewForm" :model="reviewForm" label-width="100px">
        <el-form-item label="Review Notes" prop="notes">
          <el-input v-model="reviewForm.notes" type="textarea" :rows="4" placeholder="Please enter review notes" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="openReview = false">Cancel</el-button>
        <el-button type="primary" @click="submitReview">Confirm</el-button>
      </div>
    </el-dialog>

<!-- Interview Dialog -->
    <el-dialog title="Require Interview" :visible.sync="openInterview" width="600px" append-to-body>
      <el-form ref="interviewForm" :model="interviewForm" label-width="120px">
        <el-form-item label="Interview Notes" prop="notes">
          <el-input v-model="interviewForm.notes" type="textarea" :rows="4" 
                    placeholder="Please enter the reason for requiring an interview" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="openInterview = false">Cancel</el-button>
        <el-button type="warning" @click="submitInterview">Confirm</el-button>
      </div>
    </el-dialog>

    <!-- Schedule Interview Dialog -->
    <el-dialog title="Schedule Interview" :visible.sync="openSchedule" width="650px" append-to-body>
      <el-form ref="scheduleForm" :model="scheduleInterviewForm" :rules="scheduleRules" label-width="130px">
        <el-form-item label="Vendor">
          <el-input v-model="scheduleInterviewForm.vendorName" disabled />
        </el-form-item>
        <el-form-item label="Interview Date" prop="interviewDate">
          <el-date-picker
            v-model="scheduleInterviewForm.interviewDate"
            type="date"
            placeholder="Select Date"
            value-format="yyyy-MM-dd"
            :picker-options="pickerOptions"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="Interview Time" prop="interviewTime">
          <el-time-select
            v-model="scheduleInterviewForm.interviewTime"
            :picker-options="{ start: '00:00', step: '00:15', end: '23:45' }"
            placeholder="Select Time"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="Duration" prop="durationMinutes">
          <el-input-number v-model="scheduleInterviewForm.durationMinutes" :min="15" :max="180" :step="15" /> minutes
        </el-form-item>
        <el-form-item label="Interview Type" prop="interviewType">
          <el-select v-model="scheduleInterviewForm.interviewType" placeholder="Select Type" style="width: 100%">
            <el-option label="Video Call" value="video" />
            <el-option label="Audio Call" value="audio" />
            <el-option label="Text Chat" value="text" />
          </el-select>
        </el-form-item>
        <el-form-item label="Platform" prop="platform">
          <el-select v-model="scheduleInterviewForm.platform" placeholder="Select Platform" style="width: 100%">
            <el-option label="Telegram" value="telegram" />
            <el-option label="Signal" value="signal" />
            <el-option label="Jitsi Meet" value="jitsi" />
            <el-option label="Zoom" value="zoom" />
            <el-option label="Other" value="other" />
          </el-select>
        </el-form-item>
        <el-form-item label="Meeting Link">
          <el-input v-model="scheduleInterviewForm.meetingLink" placeholder="Enter meeting link" />
        </el-form-item>
        <el-form-item label="Interviewer">
          <el-input v-model="scheduleInterviewForm.interviewerName" placeholder="Interviewer name" />
        </el-form-item>
        <el-form-item label="Notes">
          <el-input 
            v-model="scheduleInterviewForm.preparationNotes" 
            type="textarea" 
            :rows="3"
            placeholder="Preparation notes"
          />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="openSchedule = false">Cancel</el-button>
        <el-button type="primary" @click="submitScheduleInterview">Confirm</el-button>
      </div>
    </el-dialog>

    <!-- View Interview Dialog -->
    <el-dialog title="Interview Details" :visible.sync="openViewInterview" width="800px" append-to-body>
      <el-descriptions :column="2" border size="medium">
        <el-descriptions-item label="Application ID">{{ viewInterviewForm.applicationNumber }}</el-descriptions-item>
        <el-descriptions-item label="Vendor Name">{{ viewInterviewForm.vendorName }}</el-descriptions-item>
        <el-descriptions-item label="Interview Date">{{ parseTime(viewInterviewForm.interviewDatetime, '{y}-{m}-{d}') }}</el-descriptions-item>
        <el-descriptions-item label="Interview Time">{{ viewInterviewForm.interviewTime }}</el-descriptions-item>
        <el-descriptions-item label="Duration">{{ viewInterviewForm.durationMinutes }} minutes</el-descriptions-item>
        <el-descriptions-item label="Type">{{ formatInterviewType(viewInterviewForm.interviewType) }}</el-descriptions-item>
        <el-descriptions-item label="Platform">{{ viewInterviewForm.platform }}</el-descriptions-item>
        <el-descriptions-item label="Status">
          <el-tag :type="getInterviewStatusType(viewInterviewForm.status)">{{ formatInterviewStatus(viewInterviewForm.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="Meeting Link" :span="2">
          <el-link :href="viewInterviewForm.meetingLink" target="_blank" type="primary" v-if="viewInterviewForm.meetingLink">
            {{ viewInterviewForm.meetingLink }}
          </el-link>
          <span v-else>-</span>
        </el-descriptions-item>
        <el-descriptions-item label="Interviewer">{{ viewInterviewForm.interviewerName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="Vendor Confirmed">
          <el-tag v-if="viewInterviewForm.vendorConfirmed" type="success">Yes</el-tag>
          <el-tag v-else type="info">No</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="Preparation Notes" :span="2">
          {{ viewInterviewForm.preparationNotes || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="Interview Notes" :span="2" v-if="viewInterviewForm.interviewNotes">
          {{ viewInterviewForm.interviewNotes }}
        </el-descriptions-item>
        <el-descriptions-item label="Result" v-if="viewInterviewForm.interviewResult">
          {{ formatInterviewResult(viewInterviewForm.interviewResult) }}
        </el-descriptions-item>
        <el-descriptions-item label="Score" v-if="viewInterviewForm.interviewScore">
          {{ viewInterviewForm.interviewScore }}
        </el-descriptions-item>
      </el-descriptions>
      <div slot="footer" class="dialog-footer">
        <el-button @click="openViewInterview = false">Close</el-button>
      </div>
    </el-dialog>

    <!-- Complete Interview Dialog -->
    <el-dialog title="Complete Interview" :visible.sync="openCompleteInterview" width="600px" append-to-body>
      <el-form ref="completeInterviewForm" :model="completeInterviewForm" label-width="120px">
        <el-form-item label="Result" prop="result">
          <el-select v-model="completeInterviewForm.result" placeholder="Select Result" style="width: 100%">
            <el-option label="Passed" value="passed" />
            <el-option label="Failed" value="failed" />
            <el-option label="Need Second Round" value="needs_second_round" />
          </el-select>
        </el-form-item>
        <el-form-item label="Score (0-100)">
          <el-input-number v-model="completeInterviewForm.score" :min="0" :max="100" />
        </el-form-item>
        <el-form-item label="Interview Notes">
          <el-input 
            v-model="completeInterviewForm.notes" 
            type="textarea" 
            :rows="6"
            placeholder="Enter interview notes and feedback"
          />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="openCompleteInterview = false">Cancel</el-button>
        <el-button type="primary" @click="submitCompleteInterview">Confirm</el-button>
      </div>
    </el-dialog>

    <!-- Manage Available Slots Dialog -->
    <el-dialog title="Manage Available Interview Slots" :visible.sync="openManageSlots" width="1200px" append-to-body>
      <el-tabs v-model="slotsActiveTab" type="card">
        <!-- Batch Create Slots Tab -->
        <el-tab-pane label="Batch Create Slots" name="create">
          <el-alert
            title="Batch Create Available Interview Time Slots"
            type="info"
            description="Create hourly interview slots for the selected date range. All slots will be assigned to you as the interviewer."
            :closable="false"
            show-icon
            style="margin-bottom: 20px;"
          />
          <el-form ref="batchSlotsForm" :model="batchSlotsForm" :rules="batchSlotsRules" label-width="140px">
            <el-form-item label="Date Range" prop="dateRange">
              <el-date-picker
                v-model="batchSlotsForm.dateRange"
                type="daterange"
                range-separator="to"
                start-placeholder="Start Date"
                end-placeholder="End Date"
                value-format="yyyy-MM-dd"
                style="width: 320px"
              />
            </el-form-item>
            <el-form-item label="Time Range" prop="timeRange">
              <el-time-picker
                v-model="batchSlotsForm.startTime"
                placeholder="Start Time"
                format="HH:mm"
                value-format="HH:mm"
                style="width: 150px"
              />
              <span style="margin: 0 10px;">to</span>
              <el-time-picker
                v-model="batchSlotsForm.endTime"
                placeholder="End Time"
                format="HH:mm"
                value-format="HH:mm"
                style="width: 150px"
              />
              <span class="form-tip" style="margin-left: 10px; color: #909399; font-size: 12px;">
                Hourly slots will be created (e.g., 09:00, 10:00, 11:00...)
              </span>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="submitBatchCreateSlots" icon="el-icon-plus">
                Create Slots
              </el-button>
              <el-button @click="resetBatchSlotsForm">Reset</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- View Slots Tab -->
        <el-tab-pane label="View All Slots" name="list">
          <el-form :inline="true" class="mb8">
            <el-form-item label="Status">
              <el-select v-model="slotsQueryParams.status" placeholder="All" clearable style="width: 150px">
                <el-option label="All" value="" />
                <el-option label="Available" value="available" />
                <el-option label="Reserved" value="reserved" />
                <el-option label="Completed" value="completed" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" icon="el-icon-search" size="mini" @click="getSlotsList">Search</el-button>
              <el-button icon="el-icon-refresh" size="mini" @click="resetSlotsQuery">Reset</el-button>
            </el-form-item>
          </el-form>

          <el-table v-loading="slotsLoading" :data="slotsList" border>
            <el-table-column type="selection" width="55" align="center" />
            <el-table-column label="Slot ID" align="center" prop="id" width="80" />
            <el-table-column label="Date" align="center" prop="slotStart" width="130">
              <template slot-scope="scope">
                {{ parseTime(scope.row.slotStart, '{y}-{m}-{d}') }}
              </template>
            </el-table-column>
            <el-table-column label="Start Time" align="center" prop="slotStart" width="110">
              <template slot-scope="scope">
                {{ parseTime(scope.row.slotStart, '{h}:{i}') }}
              </template>
            </el-table-column>
            <el-table-column label="End Time" align="center" prop="slotEnd" width="110">
              <template slot-scope="scope">
                {{ parseTime(scope.row.slotEnd, '{h}:{i}') }}
              </template>
            </el-table-column>
            <el-table-column label="Status" align="center" prop="status" width="110">
              <template slot-scope="scope">
                <el-tag v-if="scope.row.status === 'available'" type="success" size="small">Available</el-tag>
                <el-tag v-else-if="scope.row.status === 'reserved'" type="warning" size="small">Reserved</el-tag>
                <el-tag v-else-if="scope.row.status === 'completed'" type="info" size="small">Completed</el-tag>
                <el-tag v-else type="info" size="small">{{ scope.row.status }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="Reserved By" align="center" prop="vendorId" width="150">
              <template slot-scope="scope">
                <span v-if="scope.row.vendorId">Vendor {{ scope.row.vendorId }}</span>
                <span v-else>-</span>
              </template>
            </el-table-column>
            <el-table-column label="Application ID" align="center" prop="applicationNumber" width="180">
              <template slot-scope="scope">
                <span v-if="scope.row.applicationNumber">{{ scope.row.applicationNumber }}</span>
                <span v-else>-</span>
              </template>
            </el-table-column>
            <el-table-column label="Actions" align="center" class-name="small-padding fixed-width" width="150">
              <template slot-scope="scope">
                <el-button
                  v-if="scope.row.status === 'available'"
                  size="mini"
                  type="text"
                  icon="el-icon-delete"
                  @click="handleDeleteSlot(scope.row)"
                  style="color: #F56C6C"
                >Delete</el-button>
                <el-button
                  v-if="scope.row.status === 'reserved'"
                  size="mini"
                  type="text"
                  icon="el-icon-refresh-left"
                  @click="handleReleaseSlot(scope.row)"
                  style="color: #E6A23C"
                >Release</el-button>
              </template>
            </el-table-column>
          </el-table>

          <pagination
            v-show="slotsTotal > 0"
            :total="slotsTotal"
            :page.sync="slotsQueryParams.pageNum"
            :limit.sync="slotsQueryParams.pageSize"
            @pagination="getSlotsList"
          />
        </el-tab-pane>
      </el-tabs>
      <div slot="footer" class="dialog-footer">
        <el-button @click="openManageSlots = false">Close</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { 
  listVendorApplication, 
  getVendorApplication, 
  delVendorApplication, 
  approveApplication, 
  rejectApplication,
  startReview,
  requireInterview,
  scheduleInterview,
  getInterviewsByApplication,
  listInterview,
  completeInterview,
  cancelInterview,
  getCalendarData,
  getInterview,
  listInterviewSlots,
  batchCreateInterviewSlots,
  deleteInterviewSlot,
  releaseInterviewSlot,
  verifyVendorWallets
} from "@/api/mall/vendor";
import { getOrder } from "@/api/mall/order";
import { parseTime } from "@/utils/ruoyi";

export default {
  name: "VendorApplication",
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
      // Vendor application table data
      applicationList: [],
      // Dialog title
      dialogTitle: "",
      // Show view dialog
      openView: false,
      // Show review dialog
      openReview: false,
      // Review dialog title
      reviewDialogTitle: "",
      // Review type (approve/reject)
      reviewType: "",
      // Query params
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        applicationId: null,
        vendorName: null,
        status: null,
        location: null
      },
      // Form params
      form: {},
      // Review form
      reviewForm: {
        id: null,
        notes: ""
      },
      // Active tab
      activeTab: 'applications',
      // Show interview dialog
      openInterview: false,
      // Interview form
      interviewForm: {
        id: null,
        notes: ""
      },
      // Show schedule dialog
      openSchedule: false,
      // Schedule interview form
      scheduleInterviewForm: {
        applicationId: null,
        applicationNumber: '',
        vendorName: '',
        interviewDate: '',
        interviewTime: '',
        durationMinutes: 60,
        interviewType: 'video',
        platform: 'telegram',
        meetingLink: '',
        interviewerName: '',
        preparationNotes: ''
      },
      scheduleRules: {
        interviewDate: [{ required: true, message: "Please select date", trigger: "change" }],
        interviewTime: [{ required: true, message: "Please select time", trigger: "change" }],
        interviewType: [{ required: true, message: "Please select type", trigger: "change" }],
        platform: [{ required: true, message: "Please select platform", trigger: "change" }]
      },
      pickerOptions: {
        disabledDate(time) {
          return time.getTime() < Date.now() - 8.64e7;
        }
      },
      // Progress bar colors
      customColors: [
        {color: '#f56c6c', percentage: 20},
        {color: '#e6a23c', percentage: 40},
        {color: '#5cb87a', percentage: 60},
        {color: '#1989fa', percentage: 80},
        {color: '#6f7ad3', percentage: 100}
      ],
      // Interview Schedule data
      interviewLoading: false,
      showInterviewSearch: true,
      interviewViewMode: 'list',
      interviewCalendarDate: new Date(),
      interviewList: [],
      interviewCalendarInterviews: [],
      interviewTotal: 0,
      interviewDateRange: [],
      interviewQueryParams: {
        pageNum: 1,
        pageSize: 10,
        applicationNumber: null,
        vendorName: null,
        status: null
      },
      openViewInterview: false,
      viewInterviewForm: {},
      openCompleteInterview: false,
      completeInterviewForm: {
        id: null,
        result: '',
        score: 75,
        notes: ''
      },
      // Slots Management
      openManageSlots: false,
      slotsActiveTab: 'create',
      batchSlotsForm: {
        dateRange: [],
        startTime: '09:00',
        endTime: '17:00'
      },
      batchSlotsRules: {
        dateRange: [
          { required: true, message: 'Please select date range', trigger: 'change' }
        ]
      },
      slotsLoading: false,
      slotsList: [],
      slotsTotal: 0,
      slotsQueryParams: {
        pageNum: 1,
        pageSize: 10,
        status: ''
      },
      
      // Verify Wallets Dialog
      openVerifyWallets: false,
      verifyWalletsForm: {
        id: null,
        applicationNumber: '',
        btcWallet: '',
        xmrWallet: '',
        usdtWallet: '',
        bondOrderId: '',
        bondPaidTime: null,
        notes: '',
        confirmed: false
      }
    };
  },
  created() {
    this.getList();
  },
  methods: {
    /** Query vendor application list */
    async getList() {
      this.loading = true;
      try {
        const response = await listVendorApplication(this.queryParams);
        this.applicationList = response.rows;
        this.total = response.total;
        
        // 使用 Promise.all 并行获取所有 BOND 订单信息
        const orderPromises = this.applicationList.map(async (app) => {
          if (app.bondOrderId) {
            try {
              console.log('[Bond Order] Fetching order for bondOrderId:', app.bondOrderId);
              const orderResponse = await getOrder(app.bondOrderId);
              console.log('[Bond Order] Response:', orderResponse);
              if (orderResponse && orderResponse.data) {
                // 使用 $set 确保 Vue 响应式追踪
                this.$set(app, 'bondOrderInfo', orderResponse.data);
                console.log('[Bond Order] Assigned bondOrderInfo:', app.bondOrderInfo, 'status:', app.bondOrderInfo.status);
              } else {
                this.$set(app, 'bondOrderInfo', null);
              }
            } catch (error) {
              console.error(`Failed to fetch order ${app.bondOrderId}:`, error);
              this.$set(app, 'bondOrderInfo', null);
            }
          } else {
            // 确保没有 bondOrderId 的也有一个明确的 null 值
            this.$set(app, 'bondOrderInfo', null);
          }
        });
        
        // 等待所有订单信息加载完成
        await Promise.all(orderPromises);
        console.log('[Bond Order] All orders loaded successfully');
        
      } catch (error) {
        console.error('Failed to load applications:', error);
        this.$modal.msgError('Failed to load vendor applications');
      } finally {
        this.loading = false;
      }
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
    // Multiple selection box change
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.id);
      this.single = selection.length !== 1;
      this.multiple = !selection.length;
    },
    /** Add button operation */
    handleAdd() {
      this.$message.info("Please use the vendor application portal to submit new applications");
    },
    /** View button operation */
    handleView(row) {
      this.reset();
      const id = row.id || this.ids;
      getVendorApplication(id).then(response => {
        this.form = response.data;
        this.openView = true;
        this.dialogTitle = "Vendor Application Details";
      });
    },
    /** Approve button operation */
    handleApprove(row) {
      this.reviewForm.id = row.id;
      this.reviewForm.notes = "";
      this.reviewType = "approve";
      this.reviewDialogTitle = "Approve Vendor Application";
      this.openReview = true;
    },
    /** Reject button operation */
    handleReject(row) {
      this.reviewForm.id = row.id;
      this.reviewForm.notes = "";
      this.reviewType = "reject";
      this.reviewDialogTitle = "Reject Vendor Application";
      this.openReview = true;
    },
    /** Submit review */
    submitReview() {
      const reviewFunc = this.reviewType === "approve" ? approveApplication : rejectApplication;
      reviewFunc(this.reviewForm.id, this.reviewForm.notes).then(response => {
        this.$modal.msgSuccess(this.reviewType === "approve" ? "Application approved successfully" : "Application rejected successfully");
        this.openReview = false;
        this.getList();
      });
    },
    /** Start Review button operation */
    handleStartReview(row) {
      this.$modal.confirm('Are you sure to start reviewing application "' + row.applicationId + '"?').then(function() {
        return startReview(row.id);
      }).then(() => {
        this.$modal.msgSuccess("Review started successfully, status changed to Under Review");
        this.getList();
      }).catch(() => {});
    },
    /** Interview button operation */
    handleInterview(row) {
      this.interviewForm.id = row.id;
      this.interviewForm.notes = "";
      this.openInterview = true;
    },
    /** Submit interview requirement */
    submitInterview() {
      if (!this.interviewForm.notes || !this.interviewForm.notes.trim()) {
        this.$modal.msgError("Please enter the reason for requiring an interview");
        return;
      }
      
      requireInterview(this.interviewForm.id, this.interviewForm.notes).then(response => {
        this.$modal.msgSuccess("Interview requirement set successfully");
        this.openInterview = false;
        this.getList();
      });
    },
    /** Handle tab change */
    handleTabChange(tab) {
      if (tab.name === 'interviews') {
        // Load interview data when switching to interview tab
        this.getInterviewList();
      }
    },
    /** Get interview list */
    getInterviewList() {
      this.interviewLoading = true;
      if (this.interviewViewMode === 'calendar') {
        this.loadInterviewCalendarData();
      } else {
        const params = Object.assign({}, this.interviewQueryParams);
        if (this.interviewDateRange && this.interviewDateRange.length === 2) {
          params.beginTime = this.interviewDateRange[0];
          params.endTime = this.interviewDateRange[1];
        }
        listInterview(params).then(response => {
          this.interviewList = response.rows || [];
          this.interviewTotal = response.total || 0;
          this.interviewLoading = false;
        }).catch(() => {
          this.interviewLoading = false;
        });
      }
    },
    /** Load interview calendar data */
    loadInterviewCalendarData() {
      this.interviewLoading = true;
      const startDate = this.getMonthStart(this.interviewCalendarDate);
      const endDate = this.getMonthEnd(this.interviewCalendarDate);
      
      getCalendarData(
        this.formatDate(startDate),
        this.formatDate(endDate),
        this.interviewQueryParams.status
      ).then(response => {
        this.interviewCalendarInterviews = response.data || [];
        this.interviewLoading = false;
      }).catch(() => {
        this.interviewLoading = false;
      });
    },
    /** Change interview view mode */
    changeInterviewViewMode() {
      if (this.interviewViewMode === 'calendar') {
        this.loadInterviewCalendarData();
      } else {
        this.getInterviewList();
      }
    },
    /** Handle interview query */
    handleInterviewQuery() {
      this.interviewQueryParams.pageNum = 1;
      this.getInterviewList();
    },
    /** Reset interview query */
    resetInterviewQuery() {
      this.interviewDateRange = [];
      this.resetForm("interviewQueryForm");
      this.handleInterviewQuery();
    },
    /** Handle schedule interview from tab */
    handleScheduleInterviewFromTab() {
      // Get pending applications for interview
      listVendorApplication({ status: 'interview_required' }).then(response => {
        if (response.rows && response.rows.length > 0) {
          // Use the first pending application or show selection dialog
          const app = response.rows[0];
          this.scheduleInterviewForm = {
            applicationId: app.id,
            applicationNumber: app.applicationId,
            vendorName: app.vendorName,
            interviewDate: '',
            interviewTime: '',
            durationMinutes: 60,
            interviewType: 'video',
            platform: 'telegram',
            meetingLink: '',
            interviewerName: '',
            preparationNotes: ''
          };
          this.openSchedule = true;
        } else {
          this.$modal.msgWarning("No applications require interview scheduling");
        }
      });
    },
    /** Get interviews for date */
    getInterviewsForDate(date) {
      const dateStr = this.formatDate(date);
      return this.interviewCalendarInterviews.filter(interview => {
        const interviewDate = this.formatDate(new Date(interview.interviewDatetime));
        return interviewDate === dateStr;
      });
    },
    /** Format date */
    formatDate(date) {
      const d = new Date(date);
      const year = d.getFullYear();
      const month = String(d.getMonth() + 1).padStart(2, '0');
      const day = String(d.getDate()).padStart(2, '0');
      return `${year}-${month}-${day}`;
    },
    /** Get month start */
    getMonthStart(date) {
      return new Date(date.getFullYear(), date.getMonth(), 1);
    },
    /** Get month end */
    getMonthEnd(date) {
      return new Date(date.getFullYear(), date.getMonth() + 1, 0);
    },
    /** Format interview time short */
    formatInterviewTimeShort(time) {
      if (!time) return '';
      return time.substring(0, 5); // HH:mm
    },
    /** Format interview status short */
    formatInterviewStatusShort(status) {
      const statusMap = {
        'scheduled': 'Scheduled',
        'confirmed': 'Confirmed',
        'rescheduled': 'Rescheduled',
        'completed': 'Done',
        'cancelled': 'Cancelled',
        'no_show': 'No Show'
      };
      return statusMap[status] || status;
    },
    /** Format interview status */
    formatInterviewStatus(status) {
      const statusMap = {
        'scheduled': 'Scheduled',
        'confirmed': 'Confirmed',
        'rescheduled': 'Rescheduled',
        'completed': 'Completed',
        'cancelled': 'Cancelled',
        'no_show': 'No Show'
      };
      return statusMap[status] || status;
    },
    /** Get interview status type */
    getInterviewStatusType(status) {
      const typeMap = {
        'scheduled': 'info',
        'confirmed': 'success',
        'rescheduled': 'warning',
        'completed': '',
        'cancelled': 'danger',
        'no_show': 'danger'
      };
      return typeMap[status] || 'info';
    },
    /** Truncate text */
    truncateText(text, maxLength) {
      if (!text) return '';
      if (text.length <= maxLength) return text;
      return text.substring(0, maxLength) + '...';
    },
    /** Handle view interview */
    handleViewInterview(row) {
      getInterview(row.id).then(response => {
        this.viewInterviewForm = response.data || {};
        this.openViewInterview = true;
      }).catch(() => {
        this.$modal.msgError("Failed to load interview details");
      });
    },
    /** Handle complete interview */
    handleCompleteInterview(row) {
      this.completeInterviewForm.id = row.id;
      this.completeInterviewForm.result = '';
      this.completeInterviewForm.score = 75;
      this.completeInterviewForm.notes = '';
      this.openCompleteInterview = true;
    },
    /** Submit complete interview */
    submitCompleteInterview() {
      if (!this.completeInterviewForm.result) {
        this.$modal.msgError("Please select a result");
        return;
      }
      
      completeInterview(
        this.completeInterviewForm.id,
        this.completeInterviewForm.result,
        this.completeInterviewForm.score,
        this.completeInterviewForm.notes
      ).then(response => {
        this.$modal.msgSuccess("Interview completed successfully");
        this.openCompleteInterview = false;
        this.getInterviewList();
      });
    },
    /** Handle cancel interview */
    handleCancelInterview(row) {
      this.$prompt('Please enter cancellation reason', 'Cancel Interview', {
        confirmButtonText: 'Confirm',
        cancelButtonText: 'Cancel',
        inputPattern: /.+/,
        inputErrorMessage: 'Reason cannot be empty'
      }).then(({ value }) => {
        cancelInterview(row.id, value).then(response => {
          this.$modal.msgSuccess("Interview cancelled successfully");
          this.getInterviewList();
        });
      }).catch(() => {});
    },
    /** Schedule Interview button operation */
    handleScheduleInterview(row) {
      this.scheduleInterviewForm = {
        applicationId: row.id,
        applicationNumber: row.applicationId,
        vendorName: row.vendorName,
        interviewDate: '',
        interviewTime: '',
        durationMinutes: 60,
        interviewType: 'video',
        platform: 'telegram',
        meetingLink: '',
        interviewerName: '',
        preparationNotes: ''
      };
      this.openSchedule = true;
    },
    /** Submit schedule interview */
    submitScheduleInterview() {
      this.$refs["scheduleForm"].validate(valid => {
        if (valid) {
          const datetime = `${this.scheduleInterviewForm.interviewDate} ${this.scheduleInterviewForm.interviewTime}:00`;
          this.scheduleInterviewForm.interviewDatetime = datetime;
          
          scheduleInterview(this.scheduleInterviewForm).then(response => {
            this.$modal.msgSuccess("Interview scheduled successfully");
            this.openSchedule = false;
            this.getList();
          });
        }
      });
    },
    /** Delete button operation */
    handleDelete(row) {
      const ids = row.id || this.ids;
      this.$modal.confirm('Are you sure to delete the vendor application with ID "' + ids + '"?').then(function() {
        return delVendorApplication(ids);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("Deleted successfully");
      }).catch(() => {});
    },
    /** Export button operation */
    handleExport() {
      this.download('admin/mall/vendor/application/export', {
        ...this.queryParams
      }, `vendor_application_${new Date().getTime()}.xlsx`)
    },
    // Form reset
    reset() {
      this.form = {};
      this.resetForm("form");
    },
    // Status format
    statusFormat(row) {
      const statusMap = {
        'pending': 'Pending',
        'under_review': 'Under Review',
        'interview_required': 'Interview Required',
        'approved': 'Approved',
        'rejected': 'Rejected'
      };
      return statusMap[row.status] || row.status;
    },
    // Stock volume format
    formatStockVolume(value) {
      const volumeMap = {
        'small': 'Small (< 100 units)',
        'medium': 'Medium (100-1,000 units)',
        'large': 'Large (1,000-10,000 units)',
        'xlarge': 'Very Large (> 10,000 units)'
      };
      return volumeMap[value] || value || '-';
    },
    /** Format interview type */
    formatInterviewType(type) {
      const typeMap = {
        'video': 'Video Call',
        'audio': 'Audio Call',
        'text': 'Text Chat'
      };
      return typeMap[type] || type;
    },
    /** Format interview result */
    formatInterviewResult(result) {
      const resultMap = {
        'passed': 'Passed',
        'failed': 'Failed',
        'needs_second_round': 'Needs Second Round'
      };
      return resultMap[result] || result;
    },
    
    // ==================== Slots Management Methods ====================
    
    /** Handle manage slots button */
    handleManageSlots() {
      this.openManageSlots = true;
      this.slotsActiveTab = 'create';
    },
    
    /** Submit batch create slots */
    submitBatchCreateSlots() {
      this.$refs['batchSlotsForm'].validate(valid => {
        if (valid) {
          if (!this.batchSlotsForm.dateRange || this.batchSlotsForm.dateRange.length !== 2) {
            this.$modal.msgError('Please select date range');
            return;
          }
          if (!this.batchSlotsForm.startTime || !this.batchSlotsForm.endTime) {
            this.$modal.msgError('Please select time range');
            return;
          }
          
          // Get current user ID as CM ID
          const cmId = this.$store.getters.userId;
          console.log('[Batch Create Slots] Current User ID:', cmId);
          
          if (!cmId) {
            console.error('[Batch Create Slots] User ID is null or undefined');
            this.$modal.msgError('Unable to get current user ID. Please login again.');
            return;
          }
          
          const params = {
            cmId: cmId,
            startDate: this.batchSlotsForm.dateRange[0],
            endDate: this.batchSlotsForm.dateRange[1],
            startTime: this.batchSlotsForm.startTime,
            endTime: this.batchSlotsForm.endTime
          };
          
          console.log('[Batch Create Slots] Submitting params:', params);
          
          batchCreateInterviewSlots(params).then(response => {
            console.log('[Batch Create Slots] Success:', response);
            this.$modal.msgSuccess(response.msg || 'Slots created successfully');
            this.slotsActiveTab = 'list';
            this.getSlotsList();
          }).catch(error => {
            console.error('[Batch Create Slots] Error:', error);
            this.$modal.msgError('Failed to create slots');
          });
        }
      });
    },
    
    /** Reset batch slots form */
    resetBatchSlotsForm() {
      this.batchSlotsForm = {
        dateRange: [],
        startTime: '09:00',
        endTime: '17:00'
      };
      this.$refs['batchSlotsForm'].clearValidate();
    },
    
    /** Get slots list */
    getSlotsList() {
      this.slotsLoading = true;
      // Add current user ID as cmId to query params
      const queryParams = {
        ...this.slotsQueryParams,
        cmId: this.$store.getters.userId
      };
      listInterviewSlots(queryParams).then(response => {
        this.slotsList = response.rows;
        this.slotsTotal = response.total;
        this.slotsLoading = false;
      }).catch(() => {
        this.slotsLoading = false;
        this.$modal.msgError('Failed to load slots');
      });
    },
    
    /** Reset slots query */
    resetSlotsQuery() {
      this.slotsQueryParams = {
        pageNum: 1,
        pageSize: 10,
        cmId: null,
        status: ''
      };
      this.getSlotsList();
    },
    
    /** Handle delete slot */
    handleDeleteSlot(row) {
      const slotId = row.id;
      this.$modal.confirm('Are you sure to delete this slot?').then(() => {
        return deleteInterviewSlot(slotId);
      }).then(() => {
        this.getSlotsList();
        this.$modal.msgSuccess('Slot deleted successfully');
      }).catch(() => {});
    },
    
    /** Handle release slot */
    handleReleaseSlot(row) {
      const slotId = row.id;
      this.$modal.confirm('Are you sure to release this reserved slot? The vendor will need to re-book.').then(() => {
        return releaseInterviewSlot(slotId);
      }).then(() => {
        this.getSlotsList();
        this.$modal.msgSuccess('Slot released successfully');
      }).catch(() => {});
    },

    /** Handle verify wallets */
    handleVerifyWallets() {
      this.verifyWalletsForm = {
        id: this.form.id,
        applicationNumber: this.form.applicationId,
        btcWallet: this.form.walletBtcProvided || this.form.btcWallet || '',
        xmrWallet: this.form.walletXmrProvided || this.form.xmrWallet || '',
        usdtWallet: this.form.walletUsdtProvided || this.form.usdtWallet || '',
        bondOrderId: this.form.bondOrderId || '',
        bondPaidTime: this.form.bondPaidTime || null,
        notes: '',
        confirmed: false
      };
      this.openVerifyWallets = true;
    },

    /** Submit wallet verification */
    submitVerifyWallets() {
      if (!this.verifyWalletsForm.confirmed) {
        this.$modal.msgWarning('Please confirm that you have verified the wallet addresses');
        return;
      }

      const payload = {
        id: this.verifyWalletsForm.id,
        walletBtcProvided: this.verifyWalletsForm.btcWallet,
        walletXmrProvided: this.verifyWalletsForm.xmrWallet,
        walletUsdtProvided: this.verifyWalletsForm.usdtWallet,
        walletVerifiedBy: this.$store.getters.name,
        verificationNotes: this.verifyWalletsForm.notes
      };

      this.$modal.loading('Verifying wallets...');
      
      verifyVendorWallets(payload).then(response => {
        this.$modal.closeLoading();
        this.$modal.msgSuccess('Wallets verified successfully! Co-op Member account activated.');
        this.openVerifyWallets = false;
        this.openView = false;
        this.getList();
      }).catch(error => {
        this.$modal.closeLoading();
        console.error('Wallet verification failed:', error);
      });
    },

    /** Copy to clipboard helper */
    copyToClipboard(text) {
      if (!text) {
        this.$modal.msgWarning('No content to copy');
        return;
      }
      
      const textArea = document.createElement('textarea');
      textArea.value = text;
      textArea.style.position = 'fixed';
      textArea.style.left = '-999999px';
      document.body.appendChild(textArea);
      textArea.select();
      
      try {
        document.execCommand('copy');
        this.$modal.msgSuccess('Copied to clipboard');
      } catch (err) {
        console.error('Copy failed:', err);
        this.$modal.msgError('Copy failed');
      }
      
      document.body.removeChild(textArea);
    },

    /** Download PGP Public Key as .asc file */
    downloadPgpSignature(application) {
      if (!application.pgpSignatureUrl) {
        this.$modal.msgWarning('PGP Public Key not available');
        return;
      }

      // pgp_signature_url field actually contains the full PGP public key content
      const pgpKeyContent = application.pgpSignatureUrl;
      
      // Generate filename based on application ID or use default
      let filename = 'pgp_public_key.asc';
      if (application.applicationId) {
        // Use application ID as filename
        filename = `${application.applicationId}_pgp_public_key.asc`;
      }

      try {
        // Create blob with PGP public key content
        // Content type: application/pgp-keys or text/plain
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

    /** Copy PGP Signature to clipboard */
    copyPgpSignatureToClipboard(application) {
      if (!application.pgpSignatureUrl) {
        this.$modal.msgWarning('PGP Public Key not available');
        return;
      }

      // pgpSignatureUrl field actually contains the full PGP public key content
      const pgpKeyContent = application.pgpSignatureUrl;
      
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

    /** Get BOND order status type for tag color */
    getBondOrderStatusType(orderInfo) {
      if (!orderInfo) return 'info';
      
      const status = orderInfo.status;
      const statusNum = parseInt(status);
      
      // Status >= 1 means Paid (status=1) or Completed (status=2)
      if (statusNum >= 1 && statusNum <= 2) return 'success';
      if (statusNum === 0) return 'warning';
      if (statusNum === 4) return 'danger';
      return 'info';
    },

    /** Get BOND order status text */
    getBondOrderStatusText(orderInfo) {
      if (!orderInfo) return 'Pending';
      
      const status = orderInfo.status;
      console.log('[Bond Status Debug] orderInfo:', orderInfo, 'status:', status, 'typeof status:', typeof status);
      
      // Convert to number for comparison
      const statusNum = parseInt(status);
      
      // Any status >= 1 should display as "Paid"
      if (statusNum >= 1 && statusNum <= 2) return 'Paid';
      if (statusNum === 0) return 'Pending Payment';
      if (statusNum === 4) return 'Cancelled';
      return 'Unknown';
    },

    /** Handle view BOND order details */
    handleViewBondOrder(row) {
      if (!row.bondOrderId) {
        this.$modal.msgWarning('No BOND order found for this application');
        return;
      }

      if (!row.bondOrderInfo) {
        this.$modal.msgWarning('Loading order information...');
        return;
      }

      const order = row.bondOrderInfo;
      const statusText = this.getBondOrderStatusText(order);
      
      let messageHtml = `
        <div style="text-align: left;">
          <h3 style="margin-top: 0; color: #409EFF;">BOND Order Details</h3>
          <table style="width: 100%; border-collapse: collapse;">
            <tr style="border-bottom: 1px solid #eee;">
              <td style="padding: 8px; font-weight: bold;">Order ID:</td>
              <td style="padding: 8px;">${order.id || order.orderSn}</td>
            </tr>
            <tr style="border-bottom: 1px solid #eee;">
              <td style="padding: 8px; font-weight: bold;">BOND Level:</td>
              <td style="padding: 8px;">${row.bondLevel || 'N/A'}</td>
            </tr>
            <tr style="border-bottom: 1px solid #eee;">
              <td style="padding: 8px; font-weight: bold;">Amount:</td>
              <td style="padding: 8px; color: #67C23A; font-size: 16px; font-weight: bold;">$${(order.totalAmount || 0).toFixed(2)}</td>
            </tr>
            <tr style="border-bottom: 1px solid #eee;">
              <td style="padding: 8px; font-weight: bold;">Status:</td>
              <td style="padding: 8px;"><span style="padding: 4px 12px; border-radius: 4px; background: ${
                order.status === 2 || order.status === 1 ? '#f0f9ff' : '#fff3e0'
              }; color: ${
                order.status === 2 || order.status === 1 ? '#67C23A' : '#E6A23C'
              };">${statusText}</span></td>
            </tr>
            <tr style="border-bottom: 1px solid #eee;">
              <td style="padding: 8px; font-weight: bold;">Order Created:</td>
              <td style="padding: 8px;">${this.parseTime(order.createTime, '{y}-{m}-{d} {h}:{i}')}</td>
            </tr>
            ${row.bondPaidTime ? `
            <tr style="border-bottom: 1px solid #eee;">
              <td style="padding: 8px; font-weight: bold;">Paid Time:</td>
              <td style="padding: 8px; color: #67C23A;">${this.parseTime(row.bondPaidTime, '{y}-{m}-{d} {h}:{i}')}</td>
            </tr>
            ` : ''}
            <tr>
              <td style="padding: 8px; font-weight: bold;">Vendor:</td>
              <td style="padding: 8px;">${row.vendorName}</td>
            </tr>
          </table>
        </div>
      `;

      this.$alert(messageHtml, 'BOND Order Information', {
        dangerouslyUseHTMLString: true,
        confirmButtonText: 'Close',
        customClass: 'bond-order-alert'
      });
    }
  }
};
</script>

<style scoped>
.tab-pane-content {
  margin-top: 10px;
}

/* Calendar Container */
.calendar-container {
  margin-top: 20px;
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

/* Calendar Day Cell */
.calendar-day {
  min-height: 100px;
  padding: 4px;
  position: relative;
}

.day-number {
  font-size: 14px;
  font-weight: 600;
  color: #606266;
  margin-bottom: 4px;
  padding: 2px 4px;
}

.interviews-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
  max-height: 120px;
  overflow-y: auto;
}

/* Interview Badge */
.interview-badge {
  background: linear-gradient(135deg, #e3f2fd 0%, #f5f5f5 100%);
  border-left: 3px solid #2196f3;
  padding: 6px 8px;
  font-size: 11px;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.interview-badge:hover {
  transform: translateX(2px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

.badge-time {
  font-weight: 700;
  color: #1976d2;
  margin-bottom: 2px;
  font-size: 12px;
}

.badge-vendor {
  color: #424242;
  margin-bottom: 3px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  font-weight: 500;
}

.badge-status {
  margin-top: 2px;
}

/* Status-based colors */
.interview-badge.status-scheduled {
  background: linear-gradient(135deg, #e3f2fd 0%, #bbdefb 100%);
  border-left-color: #2196f3;
}

.interview-badge.status-confirmed {
  background: linear-gradient(135deg, #e8f5e9 0%, #c8e6c9 100%);
  border-left-color: #4caf50;
}

.interview-badge.status-rescheduled {
  background: linear-gradient(135deg, #fff3e0 0%, #ffe0b2 100%);
  border-left-color: #ff9800;
}

.interview-badge.status-completed {
  background: linear-gradient(135deg, #f5f5f5 0%, #e0e0e0 100%);
  border-left-color: #9e9e9e;
}

.interview-badge.status-cancelled {
  background: linear-gradient(135deg, #ffebee 0%, #ffcdd2 100%);
  border-left-color: #f44336;
}

/* No interviews state */
.no-interviews {
  min-height: 20px;
}

/* Calendar styling */
::v-deep .el-calendar-table .el-calendar-day {
  padding: 4px;
}

::v-deep .el-calendar-table td.is-today {
  background-color: #f0f9ff;
}

::v-deep .el-calendar-table td.is-today .day-number {
  color: #2196f3;
  font-weight: 700;
}

/* Scrollbar for interviews list */
.interviews-list::-webkit-scrollbar {
  width: 4px;
}

.interviews-list::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 2px;
}

.interviews-list::-webkit-scrollbar-thumb {
  background: #888;
  border-radius: 2px;
}

.interviews-list::-webkit-scrollbar-thumb:hover {
  background: #555;
}
</style>


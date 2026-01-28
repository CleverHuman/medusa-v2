<template>
  <div class="app-container">
    <el-tabs v-model="activeTab" type="card" @tab-click="handleTabChange">
      <el-tab-pane label="Interview Schedule" name="interviews">
        <div class="tab-pane-content">
          <!-- Toolbar -->
          <el-row :gutter="10" class="mb8">
            <el-col :span="1.5">
              <el-button
                type="primary"
                plain
                icon="el-icon-plus"
                size="mini"
                @click="handleSchedule"
                v-hasPermi="['mall:vendor:interview:add']"
              >Schedule Interview</el-button>
            </el-col>
            <el-col :span="1.5">
              <el-button
                icon="el-icon-refresh"
                size="mini"
                @click="getInterviewList"
              >Refresh</el-button>
            </el-col>
            <el-col :span="1.5">
              <el-select v-model="queryParams.status" size="mini" @change="getInterviewList" clearable placeholder="Filter Status">
                <el-option label="All" value="" />
                <el-option label="Scheduled" value="scheduled" />
                <el-option label="Confirmed" value="confirmed" />
                <el-option label="Completed" value="completed" />
                <el-option label="Cancelled" value="cancelled" />
              </el-select>
            </el-col>
            <el-col :span="1.5">
              <el-radio-group v-model="viewMode" size="mini" @change="changeViewMode">
                <el-radio-button label="list">
                  <i class="el-icon-s-order"></i> List
                </el-radio-button>
                <el-radio-button label="calendar">
                  <i class="el-icon-date"></i> Calendar
                </el-radio-button>
              </el-radio-group>
            </el-col>
            <right-toolbar :showSearch.sync="showSearch" @queryTable="getInterviewList"></right-toolbar>
          </el-row>

          <!-- Filter Form -->
          <el-form :model="queryParams" ref="queryForm" :inline="true" v-show="showSearch" label-width="120px">
            <el-form-item label="Application ID" prop="applicationNumber">
              <el-input
                v-model="queryParams.applicationNumber"
                placeholder="Application ID"
                clearable
                size="small"
                @keyup.enter.native="handleQuery"
              />
            </el-form-item>
            <el-form-item label="Vendor Name" prop="vendorName">
              <el-input
                v-model="queryParams.vendorName"
                placeholder="Vendor Name"
                clearable
                size="small"
                @keyup.enter.native="handleQuery"
              />
            </el-form-item>
            <el-form-item label="Interview Date" prop="interviewDate">
              <el-date-picker
                v-model="dateRange"
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
              <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">Search</el-button>
              <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">Reset</el-button>
            </el-form-item>
          </el-form>

          <!-- Calendar View -->
          <div v-if="viewMode === 'calendar'" class="calendar-container">
            <el-calendar v-model="calendarDate">
              <template slot="dateCell" slot-scope="{date, data}">
                <div class="calendar-day">
                  <div class="day-number">{{ data.day.split('-').slice(2).join('-') }}</div>
                  <div class="interviews-list">
                    <div 
                      v-for="interview in getInterviewsForDate(date)" 
                      :key="interview.id"
                      :class="['interview-badge', `status-${interview.status}`]"
                      @click="handleView(interview)"
                    >
                      <div class="badge-time">{{ formatTimeShort(interview.interviewTime) }}</div>
                      <div class="badge-vendor">{{ truncateText(interview.vendorName, 12) }}</div>
                      <el-tag 
                        :type="getStatusType(interview.status)" 
                        size="mini"
                        class="badge-status"
                      >
                        {{ formatStatusShort(interview.status) }}
                      </el-tag>
                    </div>
                    <div v-if="getInterviewsForDate(date).length === 0" class="no-interviews">
                      <!-- Empty -->
                    </div>
                  </div>
                </div>
              </template>
            </el-calendar>
          </div>

          <!-- Interview List Table -->
          <el-table v-if="viewMode === 'list'" v-loading="loading" :data="interviewList" @selection-change="handleSelectionChange">
            <el-table-column type="selection" width="55" align="center" />
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
                <el-tag :type="getStatusType(scope.row.status)" size="small">
                  {{ formatStatus(scope.row.status) }}
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
                  @click="handleView(scope.row)"
                >View</el-button>
                <el-button
                  v-if="scope.row.status === 'scheduled' || scope.row.status === 'confirmed'"
                  size="mini"
                  type="text"
                  icon="el-icon-check"
                  @click="handleComplete(scope.row)"
                  style="color: #67C23A"
                >Complete</el-button>
                <el-button
                  v-if="scope.row.status !== 'completed' && scope.row.status !== 'cancelled'"
                  size="mini"
                  type="text"
                  icon="el-icon-close"
                  @click="handleCancel(scope.row)"
                  style="color: #F56C6C"
                >Cancel</el-button>
              </template>
            </el-table-column>
          </el-table>

          <pagination
            v-show="total>0"
            :total="total"
            :page.sync="queryParams.pageNum"
            :limit.sync="queryParams.pageSize"
            @pagination="getInterviewList"
          />
        </div>
      </el-tab-pane>
      <el-tab-pane label="Slot Management" name="slots">
        <div class="tab-pane-content">
          <el-row :gutter="10" class="mb8">
            <el-col :span="1.8">
              <el-button
                type="primary"
                size="mini"
                icon="el-icon-plus"
                @click="handleOpenSlotDialog"
                v-hasPermi="['mall:vendor:interview:add']"
              >Create Slot</el-button>
            </el-col>
            <el-col :span="2">
              <el-button
                type="primary"
                plain
                size="mini"
                icon="el-icon-timer"
                @click="handleOpenSlotBatch"
                v-hasPermi="['mall:vendor:interview:add']"
              >Batch Create</el-button>
            </el-col>
            <el-col :span="2">
              <el-button
                type="danger"
                size="mini"
                icon="el-icon-delete"
                :disabled="slotSelection.length === 0"
                @click="handleSlotBatchDelete"
                v-hasPermi="['mall:vendor:interview:remove']"
              >Delete Selected</el-button>
            </el-col>
            <el-col :span="1.5">
              <el-button
                icon="el-icon-refresh"
                size="mini"
                @click="refreshSlotView"
              >Refresh</el-button>
            </el-col>
            <el-col :span="2.5">
              <el-radio-group v-model="slotViewMode" size="mini" @change="handleSlotViewModeChange">
                <el-radio-button label="list">
                  <i class="el-icon-s-order"></i> List
                </el-radio-button>
                <el-radio-button label="calendar">
                  <i class="el-icon-date"></i> Calendar
                </el-radio-button>
              </el-radio-group>
            </el-col>
            <el-col :span="2">
              <el-select v-model="slotQueryParams.status" size="mini" clearable placeholder="Status" @change="handleSlotQuery">
                <el-option
                  v-for="item in slotStatusOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-col>
          </el-row>

          <el-form :model="slotQueryParams" ref="slotQueryForm" :inline="true" label-width="100px">
            <el-form-item label="Date Range">
              <el-date-picker
                v-model="slotDateRange"
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
              <el-button type="primary" icon="el-icon-search" size="mini" @click="handleSlotQuery">Search</el-button>
              <el-button icon="el-icon-refresh" size="mini" @click="resetSlotQuery">Reset</el-button>
            </el-form-item>
          </el-form>

          <div v-if="slotViewMode === 'list'">
            <el-table
              v-loading="slotLoading"
              :data="slotList"
              @selection-change="handleSlotSelectionChange"
            >
              <el-table-column type="selection" width="55" align="center" />
              <el-table-column label="Slot Date" prop="slotStart" align="center" width="140">
                <template slot-scope="scope">
                  <span>{{ parseTime(scope.row.slotStart, '{y}-{m}-{d}') }}</span>
                </template>
              </el-table-column>
              <el-table-column label="Time Range" align="center" width="180">
                <template slot-scope="scope">
                  <span>{{ formatSlotTimeRange(scope.row.slotStart, scope.row.slotEnd) }}</span>
                </template>
              </el-table-column>
              <el-table-column label="Status" prop="status" align="center" width="130">
                <template slot-scope="scope">
                  <el-tag :type="getSlotStatusType(scope.row.status)" size="small">
                    {{ formatSlotStatus(scope.row.status) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="Reserved Interview" align="center" width="180">
                <template slot-scope="scope">
                  <el-link
                    v-if="scope.row.reservedInterviewId"
                    type="primary"
                    @click="handleSlotViewInterview(scope.row)"
                  >
                    #{{ scope.row.reservedInterviewId }}
                  </el-link>
                  <span v-else>-</span>
                </template>
              </el-table-column>
              <el-table-column label="Reserved Vendor" align="center" width="200">
                <template slot-scope="scope">
                  <span v-if="scope.row.reservedVendorName" class="reserved-vendor-link" @click="handleSlotViewApplication(scope.row)">
                    {{ scope.row.reservedVendorName }}
                    <span v-if="scope.row.reservedApplicationCode" class="reserved-vendor-code"> ({{ scope.row.reservedApplicationCode }})</span>
                  </span>
                  <span v-else-if="scope.row.reservedVendorId">#{{ scope.row.reservedVendorId }}</span>
                  <span v-else>-</span>
                </template>
              </el-table-column>
              <el-table-column label="Actions" align="center" width="220">
                <template slot-scope="scope">
                  <el-button
                    v-if="scope.row.status === 'reserved'"
                    size="mini"
                    type="text"
                    icon="el-icon-unlock"
                    @click="handleSlotRelease(scope.row)"
                  >Release</el-button>
                  <el-button
                    v-if="canDeleteSlot(scope.row)"
                    size="mini"
                    type="text"
                    icon="el-icon-delete"
                    style="color: #F56C6C"
                    @click="handleSlotDelete(scope.row)"
                  >Delete</el-button>
                </template>
              </el-table-column>
            </el-table>

            <pagination
              v-show="slotTotal>0"
              :total="slotTotal"
              :page.sync="slotQueryParams.pageNum"
              :limit.sync="slotQueryParams.pageSize"
              @pagination="getSlotList"
            />
          </div>

          <div v-else class="slot-calendar-container">
            <div v-if="slotCalendarLoading" class="slot-calendar-loading">
              <i class="el-icon-loading"></i>
              <span>Loading calendar slots...</span>
            </div>
            <full-calendar
              v-if="slotViewMode === 'calendar'"
              ref="slotCalendar"
              :key="'slot-calendar-' + slotViewMode"
              :options="slotCalendarOptions"
            />
            <el-empty
              v-if="!slotCalendarLoading && slotViewMode === 'calendar' && slotCalendarEvents.length === 0"
              description="No slots available in the selected range"
            />
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- Create Slot Dialog -->
    <el-dialog title="Create Interview Slot" :visible.sync="openSlotDialog" width="500px" append-to-body>
      <el-form ref="slotFormRef" :model="slotForm" :rules="slotFormRules" label-width="130px">
        <el-form-item label="Slot Date" prop="slotDate">
          <el-date-picker
            v-model="slotForm.slotDate"
            type="date"
            placeholder="Select date"
            value-format="yyyy-MM-dd"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="Start Time" prop="slotTime">
          <el-time-select
            v-model="slotForm.slotTime"
            :picker-options="{ start: '07:00', step: '01:00', end: '23:00' }"
            placeholder="Select start time"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="Duration" prop="durationMinutes">
        <el-input-number v-model="slotForm.durationMinutes" :min="60" :max="180" :step="30" /> minutes
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="openSlotDialog = false">Cancel</el-button>
        <el-button type="primary" @click="submitSlotCreate">Create</el-button>
      </div>
    </el-dialog>

    <!-- Batch Create Slot Dialog -->
    <el-dialog title="Batch Create Interview Slots" :visible.sync="openSlotBatch" width="520px" append-to-body>
      <el-form ref="slotBatchFormRef" :model="slotBatchForm" :rules="slotBatchRules" label-width="140px">
        <el-form-item label="Date Range" prop="dateRange">
          <el-date-picker
            v-model="slotBatchForm.dateRange"
            type="daterange"
            value-format="yyyy-MM-dd"
            range-separator="-"
            start-placeholder="Start date"
            end-placeholder="End date"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="Start Time" prop="startTime">
          <el-time-select
            v-model="slotBatchForm.startTime"
            :picker-options="{ start: '07:00', step: '01:00', end: '23:00' }"
            placeholder="Select start time"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="End Time" prop="endTime">
          <el-time-select
            v-model="slotBatchForm.endTime"
            :picker-options="{ start: '08:00', step: '01:00', end: '23:59' }"
            placeholder="Select end time"
            style="width: 100%"
          />
        </el-form-item>
        <div class="form-tip">
          Slots will be generated in 1-hour blocks between the selected start and end times for each day.
        </div>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="openSlotBatch = false">Cancel</el-button>
        <el-button type="primary" @click="submitSlotBatch">Create Slots</el-button>
      </div>
    </el-dialog>

    <!-- Schedule Interview Dialog -->
    <el-dialog title="Schedule Interview" :visible.sync="openSchedule" width="700px" append-to-body>
      <el-form ref="scheduleForm" :model="scheduleForm" :rules="scheduleRules" label-width="140px">
        <el-form-item label="Application" prop="applicationId">
          <el-select 
            v-model="scheduleForm.applicationId" 
            placeholder="Select Application" 
            filterable
            style="width: 100%"
            @change="handleApplicationChange"
          >
            <el-option
              v-for="app in pendingApplications"
              :key="app.id"
              :label="`${app.applicationId} - ${app.vendorName}`"
              :value="app.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="Interview Date" prop="interviewDate">
          <el-date-picker
            v-model="scheduleForm.interviewDate"
            type="date"
            placeholder="Select Date"
            value-format="yyyy-MM-dd"
            :picker-options="pickerOptions"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="Interview Time" prop="interviewTime">
          <el-time-select
            v-model="scheduleForm.interviewTime"
            :picker-options="{ start: '00:00', step: '00:15', end: '23:45' }"
            placeholder="Select Time"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="Duration" prop="durationMinutes">
          <el-input-number v-model="scheduleForm.durationMinutes" :min="15" :max="180" :step="15" /> minutes
        </el-form-item>
        <el-form-item label="Interview Type" prop="interviewType">
          <el-select v-model="scheduleForm.interviewType" placeholder="Select Type" style="width: 100%">
            <el-option label="Video Call" value="video" />
            <el-option label="Audio Call" value="audio" />
            <el-option label="Text Chat" value="text" />
          </el-select>
        </el-form-item>
        <el-form-item label="Platform" prop="platform">
          <el-select v-model="scheduleForm.platform" placeholder="Select Platform" style="width: 100%">
            <el-option label="Telegram" value="telegram" />
            <el-option label="Signal" value="signal" />
            <el-option label="Jitsi Meet" value="jitsi" />
            <el-option label="Zoom" value="zoom" />
            <el-option label="Other" value="other" />
          </el-select>
        </el-form-item>
        <el-form-item label="Meeting Link" prop="meetingLink">
          <el-input v-model="scheduleForm.meetingLink" placeholder="Enter meeting link" />
        </el-form-item>
        <el-form-item label="Meeting ID">
          <el-input v-model="scheduleForm.meetingId" placeholder="Optional meeting ID" />
        </el-form-item>
        <el-form-item label="Meeting Password">
          <el-input v-model="scheduleForm.meetingPassword" placeholder="Optional password" />
        </el-form-item>
        <el-form-item label="Interviewer">
          <el-input v-model="scheduleForm.interviewerName" placeholder="Interviewer name" />
        </el-form-item>
        <el-form-item label="Preparation Notes">
          <el-input 
            v-model="scheduleForm.preparationNotes" 
            type="textarea" 
            :rows="4"
            placeholder="Notes for the interviewer"
          />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="openSchedule = false">Cancel</el-button>
        <el-button type="primary" @click="submitSchedule">Confirm</el-button>
      </div>
    </el-dialog>

    <!-- View Interview Dialog -->
    <el-dialog title="Interview Details" :visible.sync="openView" width="800px" append-to-body>
      <el-descriptions :column="2" border size="medium">
        <el-descriptions-item label="Application ID">{{ viewForm.applicationNumber }}</el-descriptions-item>
        <el-descriptions-item label="Vendor Name">{{ viewForm.vendorName }}</el-descriptions-item>
        <el-descriptions-item label="Interview Date">{{ parseTime(viewForm.interviewDatetime, '{y}-{m}-{d}') }}</el-descriptions-item>
        <el-descriptions-item label="Interview Time">{{ viewForm.interviewTime }}</el-descriptions-item>
        <el-descriptions-item label="Duration">{{ viewForm.durationMinutes }} minutes</el-descriptions-item>
        <el-descriptions-item label="Type">{{ formatType(viewForm.interviewType) }}</el-descriptions-item>
        <el-descriptions-item label="Platform">{{ viewForm.platform }}</el-descriptions-item>
        <el-descriptions-item label="Status">
          <el-tag :type="getStatusType(viewForm.status)">{{ formatStatus(viewForm.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="Meeting Link" :span="2">
          <el-link :href="viewForm.meetingLink" target="_blank" type="primary" v-if="viewForm.meetingLink">
            {{ viewForm.meetingLink }}
          </el-link>
          <span v-else>-</span>
        </el-descriptions-item>
        <el-descriptions-item label="Meeting ID">{{ viewForm.meetingId || '-' }}</el-descriptions-item>
        <el-descriptions-item label="Meeting Password">{{ viewForm.meetingPassword || '-' }}</el-descriptions-item>
        <el-descriptions-item label="Interviewer">{{ viewForm.interviewerName }}</el-descriptions-item>
        <el-descriptions-item label="Vendor Confirmed">
          <el-tag v-if="viewForm.vendorConfirmed" type="success">Yes</el-tag>
          <el-tag v-else type="info">No</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="Preparation Notes" :span="2">
          {{ viewForm.preparationNotes || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="Interview Notes" :span="2" v-if="viewForm.interviewNotes">
          {{ viewForm.interviewNotes }}
        </el-descriptions-item>
        <el-descriptions-item label="Result" v-if="viewForm.interviewResult">
          {{ formatResult(viewForm.interviewResult) }}
        </el-descriptions-item>
        <el-descriptions-item label="Score" v-if="viewForm.interviewScore">
          {{ viewForm.interviewScore }}
        </el-descriptions-item>
      </el-descriptions>
      <div slot="footer" class="dialog-footer">
        <el-button @click="openView = false">Close</el-button>
      </div>
    </el-dialog>

    <!-- Complete Interview Dialog -->
    <el-dialog title="Complete Interview" :visible.sync="openComplete" width="600px" append-to-body>
      <el-form ref="completeForm" :model="completeForm" label-width="120px">
        <el-form-item label="Result" prop="result">
          <el-select v-model="completeForm.result" placeholder="Select Result" style="width: 100%">
            <el-option label="Passed" value="passed" />
            <el-option label="Failed" value="failed" />
            <el-option label="Need Second Round" value="needs_second_round" />
          </el-select>
        </el-form-item>
        <el-form-item label="Score (0-100)">
          <el-input-number v-model="completeForm.score" :min="0" :max="100" />
        </el-form-item>
        <el-form-item label="Interview Notes">
          <el-input 
            v-model="completeForm.notes" 
            type="textarea" 
            :rows="6"
            placeholder="Enter interview notes and feedback"
          />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="openComplete = false">Cancel</el-button>
        <el-button type="primary" @click="submitComplete">Confirm</el-button>
      </div>
    </el-dialog>

    <!-- View Application Dialog -->
    <el-dialog title="Application Details" :visible.sync="openApplicationView" width="900px" append-to-body>
      <el-skeleton :loading="slotApplicationLoading" animated :rows="8">
        <el-descriptions :column="2" border size="medium">
          <el-descriptions-item label="Application ID">{{ slotApplicationForm.applicationId || '-' }}</el-descriptions-item>
          <el-descriptions-item label="Vendor Name">{{ slotApplicationForm.vendorName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="Market Experience">{{ formatBooleanText(slotApplicationForm.hasMarketExperience) }}</el-descriptions-item>
          <el-descriptions-item label="Existing Markets">{{ slotApplicationForm.existingMarkets || '-' }}</el-descriptions-item>
          <el-descriptions-item label="Experience Years">{{ slotApplicationForm.experienceYears || '-' }}</el-descriptions-item>
          <el-descriptions-item label="Location">{{ slotApplicationForm.location || '-' }}</el-descriptions-item>
          <el-descriptions-item label="Stock Volume">{{ formatApplicationStockVolume(slotApplicationForm.stockVolume) }}</el-descriptions-item>
          <el-descriptions-item label="Offline Delivery">{{ formatBooleanText(slotApplicationForm.offlineDelivery) }}</el-descriptions-item>
          <el-descriptions-item label="PGP Signature" :span="2">
            <el-link v-if="slotApplicationForm.pgpSignatureUrl" :href="slotApplicationForm.pgpSignatureUrl" target="_blank" type="primary">
              {{ slotApplicationForm.pgpSignatureUrl }}
            </el-link>
            <span v-else>-</span>
          </el-descriptions-item>
          <el-descriptions-item label="BTC Wallet" :span="2">{{ slotApplicationForm.btcWallet || '-' }}</el-descriptions-item>
          <el-descriptions-item label="XMR Wallet" :span="2">{{ slotApplicationForm.xmrWallet || '-' }}</el-descriptions-item>
          <el-descriptions-item label="USDT Wallet" :span="2">{{ slotApplicationForm.usdtWallet || '-' }}</el-descriptions-item>
          <el-descriptions-item label="Primary Contact" :span="2">
            <div v-if="slotApplicationForm.primaryTelegram">Telegram: {{ slotApplicationForm.primaryTelegram }}</div>
            <div v-if="slotApplicationForm.primarySignal">Signal: {{ slotApplicationForm.primarySignal }}</div>
            <div v-if="slotApplicationForm.primaryJabber">Jabber: {{ slotApplicationForm.primaryJabber }}</div>
            <div v-if="slotApplicationForm.primaryEmail">Email: {{ slotApplicationForm.primaryEmail }}</div>
            <div v-if="slotApplicationForm.primaryThreema">Threema: {{ slotApplicationForm.primaryThreema }}</div>
            <div v-if="!slotApplicationForm.primaryTelegram && !slotApplicationForm.primarySignal && !slotApplicationForm.primaryJabber && !slotApplicationForm.primaryEmail && !slotApplicationForm.primaryThreema">-</div>
          </el-descriptions-item>
          <el-descriptions-item label="Secondary Contact" :span="2">
            <div v-if="slotApplicationForm.secondaryTelegram">Telegram: {{ slotApplicationForm.secondaryTelegram }}</div>
            <div v-if="slotApplicationForm.secondarySignal">Signal: {{ slotApplicationForm.secondarySignal }}</div>
            <div v-if="slotApplicationForm.secondaryJabber">Jabber: {{ slotApplicationForm.secondaryJabber }}</div>
            <div v-if="slotApplicationForm.secondaryEmail">Email: {{ slotApplicationForm.secondaryEmail }}</div>
            <div v-if="slotApplicationForm.secondaryThreema">Threema: {{ slotApplicationForm.secondaryThreema }}</div>
            <div v-if="!slotApplicationForm.secondaryTelegram && !slotApplicationForm.secondarySignal && !slotApplicationForm.secondaryJabber && !slotApplicationForm.secondaryEmail && !slotApplicationForm.secondaryThreema">-</div>
          </el-descriptions-item>
          <el-descriptions-item label="Product Description" :span="2">{{ slotApplicationForm.productDescription || '-' }}</el-descriptions-item>
          <el-descriptions-item label="Status">{{ formatApplicationStatus(slotApplicationForm.status) }}</el-descriptions-item>
          <el-descriptions-item label="Applied Time">{{ slotApplicationForm.createTime ? parseTime(slotApplicationForm.createTime) : '-' }}</el-descriptions-item>
          <el-descriptions-item label="Review Notes" :span="2" v-if="slotApplicationForm.reviewNotes">{{ slotApplicationForm.reviewNotes }}</el-descriptions-item>
        </el-descriptions>
      </el-skeleton>
      <div slot="footer" class="dialog-footer">
        <el-button @click="openApplicationView = false">Close</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import FullCalendar from "@fullcalendar/vue";
import dayGridPlugin from "@fullcalendar/daygrid";
import timeGridPlugin from "@fullcalendar/timegrid";
import interactionPlugin from "@fullcalendar/interaction";
import listPlugin from "@fullcalendar/list";
import { 
  listInterview, 
  scheduleInterview, 
  completeInterview,
  cancelInterview,
  getCalendarData,
  listInterviewSlots,
  createInterviewSlot,
  batchCreateInterviewSlots,
  releaseInterviewSlot,
  deleteInterviewSlot,
  listVendorApplication,
  getVendorApplication,
  getInterview
} from "@/api/mall/vendor";
import { parseTime } from "@/utils/ruoyi";

export default {
  name: "InterviewManagement",
  components: {
    FullCalendar
  },
  data() {
    return {
      activeTab: 'interviews',
      loading: false,
      showSearch: true,
      viewMode: 'list', // 'list' or 'calendar'
      calendarDate: new Date(),
      interviewList: [],
      calendarInterviews: [], // For calendar view
      pendingApplications: [],
      total: 0,
      dateRange: [],
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        applicationNumber: null,
        vendorName: null,
        status: null
      },
      openSchedule: false,
      openView: false,
      openComplete: false,
      scheduleForm: {
        applicationId: null,
        applicationNumber: '',
        vendorName: '',
        interviewDate: '',
        interviewTime: '',
        durationMinutes: 60,
        interviewType: 'video',
        platform: 'telegram',
        meetingLink: '',
        meetingId: '',
        meetingPassword: '',
        interviewerName: '',
        preparationNotes: ''
      },
      scheduleRules: {
        applicationId: [{ required: true, message: "Please select application", trigger: "change" }],
        interviewDate: [{ required: true, message: "Please select date", trigger: "change" }],
        interviewTime: [{ required: true, message: "Please select time", trigger: "change" }],
        interviewType: [{ required: true, message: "Please select type", trigger: "change" }],
        platform: [{ required: true, message: "Please select platform", trigger: "change" }]
      },
      viewForm: {},
      completeForm: {
        id: null,
        result: '',
        score: 75,
        notes: ''
      },
      slotLoading: false,
      slotList: [],
      slotTotal: 0,
      slotSelection: [],
      slotDateRange: [],
      slotViewMode: 'list',
      slotCalendarEvents: [],
      slotCalendarLoading: false,
      slotCalendarOptions: {
        plugins: [],
        initialView: 'dayGridMonth',
        headerToolbar: {
          left: 'prev,next today',
          center: 'title',
          right: 'dayGridMonth,timeGridWeek,timeGridDay,listWeek'
        },
        height: 720,
        contentHeight: 680,
        selectable: false,
        selectMirror: false,
        eventTimeFormat: { hour: '2-digit', minute: '2-digit', hour12: false },
        events: [],
        datesSet: null
      },
      slotQueryParams: {
        pageNum: 1,
        pageSize: 10,
        cmId: null,
        status: ''
      },
      openSlotDialog: false,
      slotForm: {
        slotDate: '',
        slotTime: '',
        durationMinutes: 60
      },
      slotFormRules: {
        slotDate: [{ required: true, message: "Please select date", trigger: "change" }],
        slotTime: [{ required: true, message: "Please select start time", trigger: "change" }],
        durationMinutes: [{ type: 'number', min: 60, message: "Duration must be at least 60 minutes", trigger: "change" }]
      },
      openSlotBatch: false,
      slotBatchForm: {
        dateRange: [],
        startTime: '09:00',
        endTime: '18:00'
      },
      slotBatchRules: {
        dateRange: [{ required: true, message: "Please select date range", trigger: "change" }],
        startTime: [{ required: true, message: "Please select start time", trigger: "change" }],
        endTime: [{ required: true, message: "Please select end time", trigger: "change" }]
      },
      openApplicationView: false,
      slotApplicationLoading: false,
      slotApplicationForm: {},
      slotStatusOptions: [
        { label: 'Available', value: 'available' },
        { label: 'Reserved', value: 'reserved' },
        { label: 'Completed', value: 'completed' },
        { label: 'Cancelled', value: 'cancelled' }
      ],
      pickerOptions: {
        disabledDate(time) {
          return time.getTime() < Date.now() - 8.64e7;
        }
      }
    };
  },
  computed: {
    currentCmId() {
      return this.$store.state.user.id;
    }
  },
  created() {
    this.slotCalendarOptions.plugins = [dayGridPlugin, timeGridPlugin, interactionPlugin, listPlugin];
    this.slotCalendarOptions.eventClick = this.handleSlotEventClick;
    this.slotCalendarOptions.events = (info, successCallback, failureCallback) => {
      this.fetchSlotEvents(info, successCallback, failureCallback);
    };
    // 确保 datesSet 回调在初始化时设置，用于首次加载和视图切换
    // 注意：datesSet 会在视图改变或日期范围改变时触发
    // 但由于我们使用函数式 events，FullCalendar 会自动调用 events 函数
    // 这里我们不需要额外操作，只是确保回调存在以避免 FullCalendar 警告
    this.slotCalendarOptions.datesSet = (arg) => {
      // 视图切换时，FullCalendar 会自动调用 events 函数加载数据
      // 这里可以记录当前视图范围，但不强制刷新（events 函数会自动处理）
    };
    this.slotQueryParams.cmId = this.currentCmId;
    this.getInterviewList();
    this.getPendingApplications();
  },
  watch: {
    calendarDate(newVal) {
      if (this.viewMode === 'calendar') {
        this.loadCalendarData();
      }
    },
    slotViewMode(newVal) {
      this.handleSlotViewModeChange(newVal);
    }
  },
  methods: {
    getInterviewList() {
      this.loading = true;
      if (this.viewMode === 'calendar') {
        this.loadCalendarData();
      } else {
        const params = Object.assign({}, this.queryParams);
        if (this.dateRange && this.dateRange.length === 2) {
          params.beginTime = this.dateRange[0];
          params.endTime = this.dateRange[1];
        }
        listInterview(params).then(response => {
          this.interviewList = response.rows;
          this.total = response.total;
          this.loading = false;
        });
      }
    },
    loadCalendarData() {
      this.loading = true;
      const startDate = this.getMonthStart(this.calendarDate);
      const endDate = this.getMonthEnd(this.calendarDate);
      
      getCalendarData(
        this.formatDate(startDate),
        this.formatDate(endDate),
        this.queryParams.status
      ).then(response => {
        this.calendarInterviews = response.data || [];
        this.loading = false;
      }).catch(() => {
        this.loading = false;
      });
    },
    changeViewMode() {
      if (this.viewMode === 'calendar') {
        this.loadCalendarData();
      } else {
        this.getInterviewList();
      }
    },
    handleTabChange(tab) {
      if (tab.name === 'slots') {
        if (this.slotViewMode === 'calendar') {
          // 等待 FullCalendar 组件完全渲染和初始化
          this.$nextTick(() => {
            this.$nextTick(() => {
              const calendarApi = this.getSlotCalendarApi();
              if (calendarApi) {
                // 如果日历API已可用，直接刷新事件
                calendarApi.refetchEvents();
              } else {
                // 如果还未初始化，稍后再试
                setTimeout(() => {
                  const api = this.getSlotCalendarApi();
                  if (api) {
                    api.refetchEvents();
                  }
                }, 150);
              }
            });
          });
        } else {
          this.getSlotList();
        }
      } else if (tab.name === 'interviews') {
        this.getInterviewList();
      }
    },
    ensureSlotCmId() {
      if (!this.slotQueryParams.cmId && this.currentCmId) {
        this.slotQueryParams.cmId = this.currentCmId;
      }
      return !!this.slotQueryParams.cmId;
    },
    getSlotList() {
      if (!this.ensureSlotCmId()) {
        this.$modal.msgError("Unable to detect current CM user. Please re-login.");
        return;
      }
      this.slotLoading = true;
      const params = Object.assign({}, this.slotQueryParams);
      if (this.slotDateRange && this.slotDateRange.length === 2) {
        params.beginTime = this.slotDateRange[0];
        params.endTime = this.slotDateRange[1];
      }
      listInterviewSlots(params).then(response => {
        this.slotList = response.rows || [];
        this.slotTotal = response.total || 0;
        this.slotLoading = false;
      }).catch(() => {
        this.slotLoading = false;
      });
    },
    refreshSlotView() {
      if (this.slotViewMode === 'calendar') {
        this.refreshSlotCalendar();
      } else {
        this.getSlotList();
      }
    },
    handleSlotViewModeChange(value) {
      if (value === 'calendar') {
        // 等待 FullCalendar 组件完全渲染和初始化
        this.$nextTick(() => {
          // 使用双重 nextTick 确保 DOM 已更新且 FullCalendar 已初始化
          this.$nextTick(() => {
            const calendarApi = this.getSlotCalendarApi();
            if (calendarApi) {
              // 如果日历API已可用，直接刷新事件
              calendarApi.refetchEvents();
            } else {
              // 如果还未初始化，稍后再试
              setTimeout(() => {
                const api = this.getSlotCalendarApi();
                if (api) {
                  api.refetchEvents();
                }
              }, 100);
            }
          });
        });
      } else {
        this.getSlotList();
      }
    },
    handleSlotQuery() {
      this.slotQueryParams.pageNum = 1;
      if (this.slotViewMode === 'calendar') {
        this.refreshSlotCalendar();
      } else {
        this.getSlotList();
      }
    },
    resetSlotQuery() {
      this.slotDateRange = [];
      this.slotQueryParams.status = '';
      this.slotQueryParams.pageNum = 1;
      if (this.slotViewMode === 'calendar') {
        this.refreshSlotCalendar();
      } else {
        this.getSlotList();
      }
    },
    refreshSlotCalendar(force = false) {
      if (this.slotViewMode !== 'calendar') {
        return;
      }
      const calendarRef = this.$refs.slotCalendar;
      if (calendarRef && typeof calendarRef.getApi === 'function') {
        const calendarApi = calendarRef.getApi();
        if (calendarApi) {
          calendarApi.refetchEvents();
          return;
        }
      }
    },
    getSlotCalendarApi() {
      const calendarRef = this.$refs.slotCalendar;
      if (calendarRef && typeof calendarRef.getApi === 'function') {
        return calendarRef.getApi();
      }
      return null;
    },
    getSlotCalendarState() {
      const api = this.getSlotCalendarApi();
      if (!api) {
        return null;
      }
      const viewType = api.view && api.view.type ? api.view.type : null;
      const currentDate = typeof api.getDate === 'function' ? api.getDate() : null;
      return { api, viewType, currentDate };
    },
    restoreSlotCalendarState(state) {
      if (!state || !state.api) {
        this.refreshSlotCalendar();
        return;
      }
      const viewType = state.viewType || (state.api.view && state.api.view.type) || 'timeGridWeek';
      const currentDate = state.currentDate || new Date();
      state.api.refetchEvents();
      state.api.changeView(viewType, currentDate);
    },
    fetchSlotEvents(infoOrStart, maybeSuccess, maybeFailure) {
      const isCalendarFetch = typeof infoOrStart === 'object' && infoOrStart !== null && 'start' in infoOrStart && typeof maybeSuccess === 'function';
      if (isCalendarFetch) {
        const info = infoOrStart;
        const successCallback = maybeSuccess;
        const failureCallback = typeof maybeFailure === 'function' ? maybeFailure : null;
        if (!this.ensureSlotCmId()) {
          successCallback([]);
          return;
        }
        const params = {
          pageNum: 1,
          pageSize: 1000,
          cmId: this.slotQueryParams.cmId,
          status: this.slotQueryParams.status
        };
        const start = new Date(info.start);
        start.setHours(0, 0, 0, 0);
        params.slotStart = this.formatDateTimeString(start);
        const end = new Date(info.end);
        end.setMilliseconds(end.getMilliseconds() - 1);
        params.slotEnd = this.formatDateTimeString(end);
        this.slotCalendarLoading = true;
        listInterviewSlots(params).then(response => {
          const slots = response.rows || [];
          const events = this.updateSlotCalendarEvents(slots);
          successCallback(events);
        }).catch(() => {
          failureCallback && failureCallback([]);
          this.updateSlotCalendarEvents([]);
        }).finally(() => {
          this.slotCalendarLoading = false;
        });
      }
    },
    updateSlotCalendarEvents(slots) {
      const events = (slots || []).map(slot => {
        const startDate = this.toCalendarDateTime(slot.slotStart);
        const endDate = this.toCalendarDateTime(slot.slotEnd);
        const event = {
          id: slot.id,
          title: this.getSlotCalendarTitle(slot),
          start: startDate,
          end: endDate,
          backgroundColor: this.getSlotEventColor(slot.status),
          borderColor: this.getSlotEventColor(slot.status),
          textColor: '#fff',
          allDay: false,
          display: 'block', // 在 month 视图中确保事件正确显示
          extendedProps: { slot }
        };
        // 如果事件跨天，确保在 month 视图中能正确显示
        if (startDate && endDate) {
          const start = new Date(startDate);
          const end = new Date(endDate);
          if (start.toDateString() !== end.toDateString()) {
            // 跨天事件，确保在 month 视图中显示
            event.display = 'block';
          }
        }
        return event;
      });
      this.slotCalendarEvents = events;
      return events;
    },
    toCalendarDateTime(value) {
      if (!value) return null;
      if (value instanceof Date) return value;
      if (typeof value === 'string') {
        return value.replace(' ', 'T');
      }
      return value;
    },
    getSlotEventColor(status) {
      const colors = {
        available: '#67C23A',
        reserved: '#E6A23C',
        completed: '#909399',
        cancelled: '#F56C6C'
      };
      return colors[status] || '#409EFF';
    },
    getSlotCalendarTitle(slot) {
      const base = this.formatSlotStatus(slot.status);
      if (slot.status === 'reserved') {
        if (slot.reservedVendorName) {
          return `${base} ${slot.reservedVendorName}`;
        }
        if (slot.reservedVendorId) {
          return `${base} #${slot.reservedVendorId}`;
        }
      }
      return base;
    },
    handleSlotEventClick(info) {
      const slot = (info && info.event && info.event.extendedProps && info.event.extendedProps.slot) || null;
      if (!slot) {
        return;
      }
      const date = parseTime(slot.slotStart, '{y}-{m}-{d}');
      const timeRange = this.formatSlotTimeRange(slot.slotStart, slot.slotEnd);
      if (slot.status === 'reserved') {
        this.$modal.confirm(`Release reserved slot on ${date} ${timeRange}?`).then(() => {
          this.handleSlotRelease(slot);
        }).catch(() => {});
      } else if (this.canDeleteSlot(slot)) {
        this.$modal.confirm(`Delete slot on ${date} ${timeRange}?`).then(() => {
          this.handleSlotDelete(slot);
        }).catch(() => {});
      } else {
        this.$alert(`Slot on ${date} ${timeRange} is ${this.formatSlotStatus(slot.status)}.`, 'Slot Detail', {
          confirmButtonText: 'OK'
        });
      }
    },
    handleOpenSlotDialog() {
      if (!this.currentCmId) {
        this.$modal.msgError("Unable to detect current CM user. Please re-login.");
        return;
      }
      this.resetSlotForm();
      this.openSlotDialog = true;
    },
    resetSlotForm() {
      this.slotForm = {
        slotDate: '',
        slotTime: '',
        durationMinutes: 60
      };
      this.$nextTick(() => {
        this.$refs.slotFormRef && this.$refs.slotFormRef.clearValidate();
      });
    },
    submitSlotCreate() {
      this.$refs.slotFormRef.validate(valid => {
        if (!valid) {
          return;
        }
        if (!this.currentCmId) {
          this.$modal.msgError("Unable to detect current CM user. Please re-login.");
          return;
        }
        const slotStart = `${this.slotForm.slotDate} ${this.slotForm.slotTime}:00`;
        const slotEnd = this.calcSlotEnd(slotStart, this.slotForm.durationMinutes);
        const payload = {
          cmId: this.currentCmId,
          slotStart,
          slotEnd,
          status: 'available'
        };
        createInterviewSlot(payload).then(() => {
          this.$modal.msgSuccess("Slot created successfully");
          this.openSlotDialog = false;
          if (this.slotViewMode === 'calendar') {
            this.refreshSlotCalendar(true);
          } else {
            this.getSlotList();
          }
        });
      });
    },
    handleOpenSlotBatch() {
      if (!this.currentCmId) {
        this.$modal.msgError("Unable to detect current CM user. Please re-login.");
        return;
      }
      this.slotBatchForm = {
        dateRange: [],
        startTime: '09:00',
        endTime: '18:00'
      };
      this.$nextTick(() => {
        this.$refs.slotBatchFormRef && this.$refs.slotBatchFormRef.clearValidate();
      });
      this.openSlotBatch = true;
    },
    submitSlotBatch() {
      this.$refs.slotBatchFormRef.validate(valid => {
        if (!valid) {
          return;
        }
        const range = this.slotBatchForm.dateRange;
        if (!range || range.length !== 2) {
          this.$modal.msgError("Please select date range");
          return;
        }
        if (this.slotBatchForm.startTime >= this.slotBatchForm.endTime) {
          this.$modal.msgError("End time must be later than start time");
          return;
        }
        const params = {
          cmId: this.currentCmId,
          startDate: range[0],
          endDate: range[1],
          startTime: this.slotBatchForm.startTime,
          endTime: this.slotBatchForm.endTime
        };
        batchCreateInterviewSlots(params).then(response => {
          const message = response.msg || response.data || "Slots created successfully";
          this.$modal.msgSuccess(message);
          this.openSlotBatch = false;
          if (this.slotViewMode === 'calendar') {
            this.refreshSlotCalendar(true);
          } else {
            this.getSlotList();
          }
        });
      });
    },
    handleSlotSelectionChange(selection) {
      this.slotSelection = selection;
    },
    handleSlotBatchDelete() {
      const deletable = this.slotSelection.filter(item => this.canDeleteSlot(item));
      if (deletable.length === 0) {
        this.$modal.msgWarning("Please select available or cancelled slots to delete");
        return;
      }
      const ids = deletable.map(item => item.id).join(',');
      const calendarState = this.slotViewMode === 'calendar' ? this.getSlotCalendarState() : null;
      this.$modal.confirm(`Are you sure you want to delete ${deletable.length} slot(s)?`).then(() => {
        deleteInterviewSlot(ids).then(() => {
          this.$modal.msgSuccess("Selected slots deleted");
          this.slotSelection = [];
          if (this.slotViewMode === 'calendar') {
            this.restoreSlotCalendarState(calendarState);
          } else {
            this.getSlotList();
          }
        });
      }).catch(() => {});
    },
    handleSlotDelete(row) {
      if (!this.canDeleteSlot(row)) {
        this.$modal.msgWarning("Only available or cancelled slots can be deleted");
        return;
      }
      const timeRange = this.formatSlotTimeRange(row.slotStart, row.slotEnd);
      const calendarState = this.slotViewMode === 'calendar' ? this.getSlotCalendarState() : null;
      this.$modal.confirm(`Delete slot on ${parseTime(row.slotStart, '{y}-{m}-{d}')} ${timeRange}?`).then(() => {
        deleteInterviewSlot(row.id).then(() => {
          this.$modal.msgSuccess("Slot deleted");
          this.slotSelection = this.slotSelection.filter(item => item.id !== row.id);
          if (this.slotViewMode === 'calendar') {
            this.restoreSlotCalendarState(calendarState);
          } else {
            this.getSlotList();
          }
        });
      }).catch(() => {});
    },
    handleSlotViewInterview(row) {
      if (!row || !row.reservedInterviewId) {
        this.$modal.msgWarning("No linked interview found for this slot");
        return;
      }
      getInterview(row.reservedInterviewId).then(response => {
        this.viewForm = response.data || {};
        this.openView = true;
      }).catch(() => {
        this.$modal.msgError("Failed to load interview details");
      });
    },
    handleSlotViewApplication(row) {
      if (!row || !row.reservedApplicationId) {
        this.$modal.msgWarning("No linked application found for this slot");
        return;
      }
      this.slotApplicationLoading = true;
      this.openApplicationView = true;
      this.slotApplicationForm = {};
      getVendorApplication(row.reservedApplicationId).then(response => {
        this.slotApplicationForm = response.data || {};
      }).catch(() => {
        this.$modal.msgError("Failed to load application details");
        this.openApplicationView = false;
      }).finally(() => {
        this.slotApplicationLoading = false;
      });
    },
    handleSlotRelease(row) {
      if (row.status !== 'reserved') {
        this.$modal.msgWarning("Only reserved slots can be released");
        return;
      }
      this.$modal.confirm("Release this reserved slot? The associated interview will keep its record but the slot becomes available.").then(() => {
        const calendarState = this.slotViewMode === 'calendar' ? this.getSlotCalendarState() : null;
        releaseInterviewSlot(row.id).then(() => {
          this.$modal.msgSuccess("Slot released");
          if (this.slotViewMode === 'calendar') {
            this.restoreSlotCalendarState(calendarState);
          } else {
            this.getSlotList();
          }
        });
      }).catch(() => {});
    },
    formatSlotStatus(status) {
      const statusMap = {
        'available': 'Available',
        'reserved': 'Reserved',
        'completed': 'Completed',
        'cancelled': 'Cancelled'
      };
      return statusMap[status] || status;
    },
    getSlotStatusType(status) {
      const typeMap = {
        'available': 'info',
        'reserved': 'warning',
        'completed': 'success',
        'cancelled': 'danger'
      };
      return typeMap[status] || 'info';
    },
    formatSlotTimeRange(start, end) {
      if (!start || !end) {
        return '-';
      }
      const startDate = new Date(start);
      const endDate = new Date(end);
      if (Number.isNaN(startDate.getTime()) || Number.isNaN(endDate.getTime())) {
        return '-';
      }
      return `${this.padNumber(startDate.getHours())}:${this.padNumber(startDate.getMinutes())} - ${this.padNumber(endDate.getHours())}:${this.padNumber(endDate.getMinutes())}`;
    },
    padNumber(num) {
      return String(num).padStart(2, '0');
    },
    calcSlotEnd(slotStart, durationMinutes) {
      const startDate = new Date(slotStart.replace(/-/g, '/'));
      if (Number.isNaN(startDate.getTime())) {
        return slotStart;
      }
      startDate.setMinutes(startDate.getMinutes() + Number(durationMinutes || 60));
      return this.formatDateTimeString(startDate);
    },
    formatDateTimeString(date) {
      const d = date instanceof Date ? date : new Date(date);
      if (Number.isNaN(d.getTime())) {
        return '';
      }
      const year = d.getFullYear();
      const month = this.padNumber(d.getMonth() + 1);
      const day = this.padNumber(d.getDate());
      const hour = this.padNumber(d.getHours());
      const minute = this.padNumber(d.getMinutes());
      const second = this.padNumber(d.getSeconds());
      return `${year}-${month}-${day} ${hour}:${minute}:${second}`;
    },
    canDeleteSlot(row) {
      return row && (row.status === 'available' || row.status === 'cancelled');
    },
    getInterviewsForDate(date) {
      const dateStr = this.formatDate(date);
      return this.calendarInterviews.filter(interview => {
        const interviewDate = this.formatDate(new Date(interview.interviewDatetime));
        return interviewDate === dateStr;
      });
    },
    formatDate(date) {
      const d = new Date(date);
      const year = d.getFullYear();
      const month = String(d.getMonth() + 1).padStart(2, '0');
      const day = String(d.getDate()).padStart(2, '0');
      return `${year}-${month}-${day}`;
    },
    formatTimeShort(time) {
      if (!time) return '';
      return time.substring(0, 5); // HH:mm
    },
    formatStatusShort(status) {
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
    truncateText(text, maxLength) {
      if (!text) return '';
      if (text.length <= maxLength) return text;
      return text.substring(0, maxLength) + '...';
    },
    getMonthStart(date) {
      return new Date(date.getFullYear(), date.getMonth(), 1);
    },
    getMonthEnd(date) {
      return new Date(date.getFullYear(), date.getMonth() + 1, 0);
    },
    getPendingApplications() {
      listVendorApplication({ status: 'interview_required' }).then(response => {
        this.pendingApplications = response.rows;
      });
    },
    handleQuery() {
      this.queryParams.pageNum = 1;
      this.getInterviewList();
    },
    resetQuery() {
      this.dateRange = [];
      this.resetForm("queryForm");
      this.handleQuery();
    },
    handleSchedule() {
      this.reset();
      this.getPendingApplications();
      this.openSchedule = true;
    },
    handleApplicationChange(applicationId) {
      const app = this.pendingApplications.find(a => a.id === applicationId);
      if (app) {
        this.scheduleForm.applicationNumber = app.applicationId;
        this.scheduleForm.vendorName = app.vendorName;
      }
    },
    submitSchedule() {
      this.$refs["scheduleForm"].validate(valid => {
        if (valid) {
          const datetime = `${this.scheduleForm.interviewDate} ${this.scheduleForm.interviewTime}:00`;
          this.scheduleForm.interviewDatetime = datetime;
          
          scheduleInterview(this.scheduleForm).then(response => {
            this.$modal.msgSuccess("Interview scheduled successfully");
            this.openSchedule = false;
            this.getInterviewList();
          });
        }
      });
    },
    handleView(row) {
      this.viewForm = Object.assign({}, row);
      this.openView = true;
    },
    handleComplete(row) {
      this.completeForm.id = row.id;
      this.completeForm.result = '';
      this.completeForm.score = 75;
      this.completeForm.notes = '';
      this.openComplete = true;
    },
    submitComplete() {
      if (!this.completeForm.result) {
        this.$modal.msgError("Please select a result");
        return;
      }
      
      completeInterview(
        this.completeForm.id,
        this.completeForm.result,
        this.completeForm.score,
        this.completeForm.notes
      ).then(response => {
        this.$modal.msgSuccess("Interview completed successfully");
        this.openComplete = false;
        this.getInterviewList();
      });
    },
    handleCancel(row) {
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
    formatStatus(status) {
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
    formatType(type) {
      const typeMap = {
        'video': 'Video Call',
        'audio': 'Audio Call',
        'text': 'Text Chat'
      };
      return typeMap[type] || type;
    },
    formatResult(result) {
      const resultMap = {
        'passed': 'Passed',
        'failed': 'Failed',
        'needs_second_round': 'Needs Second Round'
      };
      return resultMap[result] || result;
    },
    getStatusType(status) {
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
    formatApplicationStatus(status) {
      const statusMap = {
        'pending': 'Pending',
        'under_review': 'Under Review',
        'interview_required': 'Interview Required',
        'approved': 'Approved',
        'rejected': 'Rejected'
      };
      return statusMap[status] || status || '-';
    },
    formatApplicationStockVolume(value) {
      const volumeMap = {
        'small': 'Small (< 100 units)',
        'medium': 'Medium (100-1,000 units)',
        'large': 'Large (1,000-10,000 units)',
        'xlarge': 'Very Large (> 10,000 units)'
      };
      return volumeMap[value] || value || '-';
    },
    formatBooleanText(value) {
      if (value === null || value === undefined || value === '') {
        return '-';
      }
      return value ? 'Yes' : 'No';
    },
    reset() {
      this.scheduleForm = {
        applicationId: null,
        applicationNumber: '',
        vendorName: '',
        interviewDate: '',
        interviewTime: '',
        durationMinutes: 60,
        interviewType: 'video',
        platform: 'telegram',
        meetingLink: '',
        meetingId: '',
        meetingPassword: '',
        interviewerName: '',
        preparationNotes: ''
      };
    },
    handleSelectionChange(selection) {
      // Handle multiple selection if needed
    }
  }
};
</script>

<style scoped>
.tab-pane-content {
  margin-top: 10px;
}

.slot-calendar-container {
  margin-top: 20px;
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  min-height: 720px;
}

.reserved-vendor-link {
  color: #409EFF;
  cursor: pointer;
}

.reserved-vendor-link:hover {
  text-decoration: underline;
}

.reserved-vendor-code {
  color: #909399;
  font-size: 12px;
}

.slot-calendar-loading {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #909399;
  margin-bottom: 12px;
  font-size: 14px;
}

.slot-calendar-loading .el-icon-loading {
  font-size: 18px;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin: 0 0 0 4px;
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

/* Calendar cell with today */
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

/* View mode toggle */
::v-deep .el-radio-button__inner {
  padding: 8px 15px;
}

/* Calendar header styling */
::v-deep .el-calendar__header {
  padding: 12px 20px;
  border-bottom: 2px solid #e4e7ed;
  margin-bottom: 10px;
}

::v-deep .el-calendar__title {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
}

/* Calendar table styling */
::v-deep .el-calendar-table {
  border-collapse: separate;
  border-spacing: 4px;
}

::v-deep .el-calendar-table thead th {
  font-weight: 600;
  color: #606266;
  padding: 10px 0;
}

::v-deep .el-calendar-table td {
  border: 1px solid #ebeef5;
  border-radius: 4px;
  transition: all 0.3s ease;
}

::v-deep .el-calendar-table td:hover {
  background-color: #f5f7fa;
}

/* Mobile responsive */
@media (max-width: 768px) {
  .calendar-day {
    min-height: 80px;
  }
  
  .interview-badge {
    padding: 4px 6px;
    font-size: 10px;
  }
  
  .badge-time {
    font-size: 11px;
  }
}
</style>


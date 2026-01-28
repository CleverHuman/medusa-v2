<template>
  <div class="interview-container">
    <div class="page-header">
      <h1 class="page-title">My Interview Schedule</h1>
      <p class="page-description">View and confirm your scheduled interviews</p>
    </div>

    <!-- Search by Application ID -->
    <el-card class="search-card" shadow="hover">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="Application ID">
          <el-input
            v-model="searchForm.applicationNumber"
            placeholder="Enter your Application ID"
            clearable
            style="width: 300px"
          >
            <el-button slot="append" icon="el-icon-search" @click="searchInterviews"></el-button>
          </el-input>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- Interview List -->
    <div v-if="interviewList.length > 0" class="interview-list">
      <el-card 
        v-for="interview in interviewList" 
        :key="interview.id"
        class="interview-card"
        shadow="hover"
      >
        <div class="interview-header">
          <div class="interview-status">
            <el-tag 
              :type="getStatusType(interview.status)" 
              size="large"
            >
              {{ formatStatus(interview.status) }}
            </el-tag>
            <el-tag 
              v-if="interview.vendorConfirmed" 
              type="success" 
              size="small"
              class="ml-10"
            >
              <i class="el-icon-check"></i> Confirmed
            </el-tag>
          </div>
          <div class="application-id">
            Application: <strong>{{ interview.applicationNumber }}</strong>
          </div>
        </div>

        <el-divider></el-divider>

        <div class="interview-details">
          <el-row :gutter="20">
            <el-col :span="12">
              <div class="detail-item">
                <i class="el-icon-date detail-icon"></i>
                <div class="detail-content">
                  <div class="detail-label">Interview Date</div>
                  <div class="detail-value">{{ formatDate(interview.interviewDatetime) }}</div>
                </div>
              </div>
            </el-col>
            <el-col :span="12">
              <div class="detail-item">
                <i class="el-icon-time detail-icon"></i>
                <div class="detail-content">
                  <div class="detail-label">Interview Time</div>
                  <div class="detail-value">{{ interview.interviewTime }}</div>
                </div>
              </div>
            </el-col>
          </el-row>

          <el-row :gutter="20" class="mt-20">
            <el-col :span="12">
              <div class="detail-item">
                <i class="el-icon-timer detail-icon"></i>
                <div class="detail-content">
                  <div class="detail-label">Duration</div>
                  <div class="detail-value">{{ interview.durationMinutes }} minutes</div>
                </div>
              </div>
            </el-col>
            <el-col :span="12">
              <div class="detail-item">
                <i class="el-icon-video-camera detail-icon"></i>
                <div class="detail-content">
                  <div class="detail-label">Platform</div>
                  <div class="detail-value">{{ interview.platform }}</div>
                </div>
              </div>
            </el-col>
          </el-row>

          <div class="detail-item mt-20" v-if="interview.meetingLink">
            <i class="el-icon-link detail-icon"></i>
            <div class="detail-content">
              <div class="detail-label">Meeting Link</div>
              <div class="detail-value">
                <el-link :href="interview.meetingLink" target="_blank" type="primary">
                  {{ interview.meetingLink }}
                </el-link>
              </div>
            </div>
          </div>

          <div class="detail-item mt-20" v-if="interview.meetingId">
            <i class="el-icon-key detail-icon"></i>
            <div class="detail-content">
              <div class="detail-label">Meeting ID</div>
              <div class="detail-value">{{ interview.meetingId }}</div>
            </div>
          </div>

          <div class="detail-item mt-20" v-if="interview.meetingPassword">
            <i class="el-icon-lock detail-icon"></i>
            <div class="detail-content">
              <div class="detail-label">Meeting Password</div>
              <div class="detail-value">{{ interview.meetingPassword }}</div>
            </div>
          </div>

          <div class="detail-item mt-20" v-if="interview.interviewerName">
            <i class="el-icon-user detail-icon"></i>
            <div class="detail-content">
              <div class="detail-label">Interviewer</div>
              <div class="detail-value">{{ interview.interviewerName }}</div>
            </div>
          </div>

          <el-alert
            v-if="interview.preparationNotes"
            class="mt-20"
            title="Preparation Notes"
            type="info"
            :closable="false"
            show-icon
          >
            {{ interview.preparationNotes }}
          </el-alert>

          <el-alert
            v-if="interview.interviewResult && interview.interviewNotes"
            class="mt-20"
            :title="`Interview Result: ${formatResult(interview.interviewResult)}`"
            :type="interview.interviewResult === 'passed' ? 'success' : 'warning'"
            :closable="false"
            show-icon
          >
            <div>{{ interview.interviewNotes }}</div>
            <div v-if="interview.interviewScore" class="mt-10">
              <strong>Score:</strong> {{ interview.interviewScore }}/100
            </div>
          </el-alert>
        </div>

        <el-divider></el-divider>

        <div class="interview-actions">
          <el-button
            v-if="!interview.vendorConfirmed && (interview.status === 'scheduled' || interview.status === 'rescheduled')"
            type="primary"
            @click="handleConfirm(interview)"
            :loading="confirmLoading"
          >
            <i class="el-icon-check"></i> Confirm Interview
          </el-button>
          <el-tag v-else-if="interview.vendorConfirmed" type="success" size="large">
            <i class="el-icon-circle-check"></i> You have confirmed this interview
          </el-tag>
        </div>
      </el-card>
    </div>

    <!-- Empty State -->
    <el-empty 
      v-else-if="searched && interviewList.length === 0"
      description="No interviews found for this Application ID"
      :image-size="200"
    >
      <el-button type="primary" @click="searched = false">Clear Search</el-button>
    </el-empty>

    <el-empty 
      v-else
      description="Enter your Application ID to view your interview schedule"
      :image-size="200"
    />
  </div>
</template>

<script>
import { getInterviewsByApplicationNumber, confirmInterview } from '@/api/vendor'

export default {
  name: 'VendorInterview',
  data() {
    return {
      searchForm: {
        applicationNumber: ''
      },
      interviewList: [],
      searched: false,
      confirmLoading: false
    }
  },
  created() {
    // Check if application ID is in URL query
    const applicationNumber = this.$route.query.applicationId
    if (applicationNumber) {
      this.searchForm.applicationNumber = applicationNumber
      this.searchInterviews()
    }
  },
  methods: {
    searchInterviews() {
      if (!this.searchForm.applicationNumber || this.searchForm.applicationNumber.trim() === '') {
        this.$message.warning('Please enter your Application ID')
        return
      }

      this.searched = true
      
      getInterviewsByApplicationNumber(this.searchForm.applicationNumber).then(response => {
        this.interviewList = response.data || []
        if (this.interviewList.length === 0) {
          this.$message.info('No interviews scheduled for this Application ID')
        }
      }).catch(error => {
        this.$message.error('Failed to load interviews. Please check your Application ID.')
        this.interviewList = []
      })
    },
    handleConfirm(interview) {
      this.$confirm('Are you sure to confirm this interview?', 'Confirm Interview', {
        confirmButtonText: 'Yes, Confirm',
        cancelButtonText: 'Cancel',
        type: 'info'
      }).then(() => {
        this.confirmLoading = true
        confirmInterview(interview.id).then(response => {
          this.$message.success('Interview confirmed successfully')
          interview.vendorConfirmed = 1
          interview.status = 'confirmed'
          this.confirmLoading = false
        }).catch(error => {
          this.$message.error('Failed to confirm interview')
          this.confirmLoading = false
        })
      }).catch(() => {
        // User cancelled
      })
    },
    formatDate(datetime) {
      if (!datetime) return '-'
      const date = new Date(datetime)
      const options = { 
        year: 'numeric', 
        month: 'long', 
        day: 'numeric',
        weekday: 'long'
      }
      return date.toLocaleDateString('en-US', options)
    },
    formatStatus(status) {
      const statusMap = {
        'scheduled': 'Scheduled',
        'confirmed': 'Confirmed',
        'rescheduled': 'Rescheduled',
        'completed': 'Completed',
        'cancelled': 'Cancelled',
        'no_show': 'No Show'
      }
      return statusMap[status] || status
    },
    formatResult(result) {
      const resultMap = {
        'passed': 'Passed',
        'failed': 'Failed',
        'needs_second_round': 'Needs Second Round'
      }
      return resultMap[result] || result
    },
    getStatusType(status) {
      const typeMap = {
        'scheduled': 'info',
        'confirmed': 'success',
        'rescheduled': 'warning',
        'completed': '',
        'cancelled': 'danger',
        'no_show': 'danger'
      }
      return typeMap[status] || 'info'
    }
  }
}
</script>

<style scoped>
.interview-container {
  max-width: 900px;
  margin: 0 auto;
  padding: 30px 20px;
}

.page-header {
  text-align: center;
  margin-bottom: 40px;
}

.page-title {
  font-size: 32px;
  font-weight: 700;
  color: #2c3e50;
  margin-bottom: 10px;
}

.page-description {
  font-size: 16px;
  color: #7f8c8d;
}

.search-card {
  margin-bottom: 30px;
}

.search-form {
  display: flex;
  justify-content: center;
  align-items: center;
}

.interview-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.interview-card {
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.interview-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.interview-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.interview-status {
  display: flex;
  align-items: center;
  gap: 10px;
}

.application-id {
  font-size: 14px;
  color: #606266;
}

.interview-details {
  padding: 10px 0;
}

.detail-item {
  display: flex;
  align-items: flex-start;
  margin-bottom: 15px;
}

.detail-icon {
  font-size: 20px;
  color: #409EFF;
  margin-right: 12px;
  margin-top: 2px;
}

.detail-content {
  flex: 1;
}

.detail-label {
  font-size: 12px;
  color: #909399;
  margin-bottom: 4px;
}

.detail-value {
  font-size: 16px;
  color: #303133;
  font-weight: 500;
}

.interview-actions {
  display: flex;
  justify-content: center;
  padding-top: 10px;
}

.mt-10 {
  margin-top: 10px;
}

.mt-20 {
  margin-top: 20px;
}

.ml-10 {
  margin-left: 10px;
}

.el-divider {
  margin: 20px 0;
}
</style>


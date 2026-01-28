<template>
  <div class="status-container">
    <div class="status-header">
      <h1>Application Status Tracking</h1>
      <p>Real-time tracking of your vendor application processing progress</p>
    </div>

    <!-- Search Section -->
    <div class="search-card">
      <h2>Find Application</h2>
      <div class="search-form">
        <el-input
          v-model="searchId"
          placeholder="Enter Application ID, e.g.: VA1234567890"
          class="search-input"
          @keyup.enter.native="searchApplication">
          <el-button slot="append" icon="el-icon-search" @click="searchApplication" :loading="searching">Search</el-button>
        </el-input>
        <div class="search-tip">
          Tip: Application ID can be found in the confirmation page when submitting your application
        </div>
      </div>
    </div>

    <!-- Application Details -->
    <div v-if="application" class="details-section">
      <!-- Application Info Card -->
      <el-card class="info-card">
        <div slot="header" class="card-header">
          <div>
            <h2>Application Details</h2>
            <p class="application-id">Application ID: <span class="id-value">{{ application.applicationId }}</span></p>
          </div>
          <el-tag :type="getStatusType(application.status)" size="medium">
            {{ getStatusText(application.status) }}
          </el-tag>
        </div>

        <el-row :gutter="40">
          <el-col :span="12">
            <h3 class="section-title">Basic Information</h3>
            <el-descriptions :column="1" size="medium">
              <el-descriptions-item label="Vendor Name">{{ application.vendorName }}</el-descriptions-item>
              <el-descriptions-item label="Location">{{ application.location }}</el-descriptions-item>
              <el-descriptions-item label="Product Categories">{{ getProductCategories(application) }}</el-descriptions-item>
              <el-descriptions-item label="Application Date">{{ application.createTime }}</el-descriptions-item>
            </el-descriptions>
          </el-col>
          
          <el-col :span="12">
            <h3 class="section-title">Processing Statistics</h3>
            <el-descriptions :column="1" size="medium">
              <el-descriptions-item label="Review Progress">
                <el-progress :percentage="application.reviewProgress || 0" :color="progressColor"></el-progress>
              </el-descriptions-item>
              <el-descriptions-item label="Review Stage">{{ application.reviewStage || 'Pending' }}</el-descriptions-item>
              <el-descriptions-item label="Reviewer">{{ application.reviewerId ? 'Assigned' : 'TBD' }}</el-descriptions-item>
              <el-descriptions-item label="Last Update">{{ application.updateTime || application.createTime }}</el-descriptions-item>
            </el-descriptions>
          </el-col>
        </el-row>
      </el-card>

      <!-- Processing Timeline -->
      <el-card class="timeline-card">
        <div slot="header">
          <h2>Processing Progress</h2>
        </div>
        <el-timeline>
          <el-timeline-item
            v-for="(item, index) in getTimeline(application)"
            :key="index"
            :timestamp="item.time"
            :type="item.type"
            :icon="item.icon"
            placement="top">
            <h4>{{ item.title }}</h4>
            <p>{{ item.description }}</p>
          </el-timeline-item>
        </el-timeline>
      </el-card>

      <!-- Review Details -->
      <el-card v-if="application.reviewNotes" class="review-card">
        <div slot="header">
          <h2>Review Details</h2>
        </div>
        <el-alert
          :title="application.status === 'approved' ? 'Application Approved' : application.status === 'rejected' ? 'Application Rejected' : 'Review Notes'"
          :type="application.status === 'approved' ? 'success' : application.status === 'rejected' ? 'error' : 'info'"
          :description="application.reviewNotes"
          :closable="false"
          show-icon>
        </el-alert>
      </el-card>
    </div>

    <!-- Sample Applications (shown when no search) -->
    <div v-else class="sample-applications">
      <h2 class="sample-title">Sample Application Status</h2>
      <el-row :gutter="20">
        <el-col :span="8" v-for="sample in sampleApplications" :key="sample.id">
          <el-card class="sample-card" shadow="hover" @click.native="loadSample(sample.id)">
            <div class="sample-header">
              <el-tag :type="getStatusType(sample.status)" size="small">{{ getStatusText(sample.status) }}</el-tag>
              <span class="sample-time">{{ sample.timeAgo }}</span>
            </div>
            <h3>{{ sample.vendorName }}</h3>
            <p class="sample-id">Application ID: {{ sample.id }}</p>
            <div class="sample-info">
              <div>Categories: {{ sample.categories }}</div>
              <div>{{ sample.note }}</div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script>
import { getApplicationStatus, getApplicationDetails } from '@/api/vendor'

export default {
  name: 'VendorApplicationStatus',
  data() {
    return {
      searchId: '',
      searching: false,
      application: null,
      
      progressColor: [
        { color: '#f56c6c', percentage: 20 },
        { color: '#e6a23c', percentage: 40 },
        { color: '#5cb87a', percentage: 60 },
        { color: '#1989fa', percentage: 80 },
        { color: '#6f7ad3', percentage: 100 }
      ],
      
      sampleApplications: [
        {
          id: 'VA001DEMO',
          vendorName: 'Demo Electronics Supplier',
          status: 'pending',
          categories: 'Electronics, Accessories',
          timeAgo: '2 days ago',
          note: 'Waiting for review'
        },
        {
          id: 'VA004DEMO',
          vendorName: 'Demo Sports & Outdoors',
          status: 'under_review',
          categories: 'Sports & Outdoors',
          timeAgo: '5 days ago',
          note: 'Under review - 45%'
        },
        {
          id: 'VA007DEMO',
          vendorName: 'Demo Home Essentials',
          status: 'approved',
          categories: 'Home & Garden',
          timeAgo: '10 days ago',
          note: 'Approved successfully'
        }
      ]
    }
  },
  
  mounted() {
    // Check if there's an ID in URL params
    const id = this.$route.query.id
    if (id) {
      this.searchId = id
      this.searchApplication()
    }
  },
  
  methods: {
    // Search application
    searchApplication() {
      if (!this.searchId || !this.searchId.trim()) {
        this.$message.error('Please enter Application ID')
        return
      }
      
      this.searching = true
      
      getApplicationStatus(this.searchId.trim()).then(response => {
        this.searching = false
        this.application = response.data
        
        if (!this.application) {
          this.$message.error('Application not found. Please check the Application ID')
        }
      }).catch(error => {
        this.searching = false
        this.$message.error('Application not found. Please check the Application ID')
        this.application = null
      })
    },
    
    // Load sample application
    loadSample(applicationId) {
      this.searchId = applicationId
      this.searchApplication()
    },
    
    // Get status type for el-tag
    getStatusType(status) {
      const typeMap = {
        'pending': 'info',
        'under_review': 'warning',
        'interview_required': '',
        'approved': 'success',
        'rejected': 'danger'
      }
      return typeMap[status] || 'info'
    },
    
    // Get status text
    getStatusText(status) {
      const textMap = {
        'pending': 'Pending',
        'under_review': 'Under Review',
        'interview_required': 'Interview Required',
        'approved': 'Approved',
        'rejected': 'Rejected'
      }
      return textMap[status] || status
    },
    
    // Get product categories
    getProductCategories(application) {
      if (!application.productCategories) return '-'
      try {
        const categories = typeof application.productCategories === 'string' 
          ? JSON.parse(application.productCategories) 
          : application.productCategories
        return Array.isArray(categories) ? categories.join(', ') : application.productCategories
      } catch (e) {
        return application.productCategories
      }
    },
    
    // Get timeline based on status
    getTimeline(application) {
      const timeline = []
      
      // Submitted
      timeline.push({
        title: 'Application Submitted',
        description: 'Application has been successfully submitted',
        time: application.createTime,
        type: 'success',
        icon: 'el-icon-check'
      })
      
      // Received
      if (application.status !== 'pending') {
        timeline.push({
          title: 'Application Received',
          description: 'System has received the application and started processing',
          time: application.createTime,
          type: 'success',
          icon: 'el-icon-check'
        })
      }
      
      // Under Review
      if (['under_review', 'interview_required', 'approved', 'rejected'].includes(application.status)) {
        timeline.push({
          title: 'Under Review',
          description: application.reviewStage || 'Reviewer is evaluating the application',
          time: application.updateTime,
          type: 'primary',
          icon: 'el-icon-view'
        })
      }
      
      // Interview Required
      if (application.status === 'interview_required') {
        timeline.push({
          title: 'Interview Required',
          description: 'Face-to-face interview needed for further evaluation',
          time: application.reviewedTime || application.updateTime,
          type: 'warning',
          icon: 'el-icon-user'
        })
      }
      
      // Approved
      if (application.status === 'approved') {
        timeline.push({
          title: 'Application Approved',
          description: 'Application has been approved. Welcome to our vendor network!',
          time: application.reviewedTime,
          type: 'success',
          icon: 'el-icon-circle-check'
        })
      }
      
      // Rejected
      if (application.status === 'rejected') {
        timeline.push({
          title: 'Application Rejected',
          description: application.reviewNotes || 'Application failed review',
          time: application.reviewedTime,
          type: 'danger',
          icon: 'el-icon-circle-close'
        })
      }
      
      return timeline
    }
  }
}
</script>

<style scoped lang="scss">
.status-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 40px 20px;
  background-color: #f5f7fa;
  min-height: 100vh;
}

.status-header {
  text-align: center;
  margin-bottom: 40px;
  
  h1 {
    font-size: 32px;
    font-weight: bold;
    color: #303133;
    margin-bottom: 10px;
  }
  
  p {
    font-size: 16px;
    color: #606266;
  }
}

.search-card {
  background: white;
  border-radius: 8px;
  padding: 30px;
  box-shadow: 0 2px 12px 0 rgba(0,0,0,0.1);
  margin-bottom: 30px;
  
  h2 {
    font-size: 20px;
    font-weight: 600;
    margin-bottom: 20px;
    color: #303133;
  }
}

.search-form {
  .search-input {
    width: 100%;
    max-width: 600px;
  }
  
  .search-tip {
    margin-top: 10px;
    font-size: 12px;
    color: #909399;
  }
}

.details-section {
  .info-card, .timeline-card, .review-card {
    margin-bottom: 20px;
  }
  
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    
    h2 {
      font-size: 20px;
      font-weight: 600;
      margin-bottom: 5px;
    }
    
    .application-id {
      font-size: 14px;
      color: #909399;
      
      .id-value {
        color: #409EFF;
        font-weight: 600;
      }
    }
  }
  
  .section-title {
    font-size: 16px;
    font-weight: 600;
    margin-bottom: 15px;
    color: #606266;
  }
}

.sample-applications {
  .sample-title {
    font-size: 24px;
    font-weight: 600;
    text-align: center;
    margin-bottom: 30px;
    color: #303133;
  }
  
  .sample-card {
    cursor: pointer;
    transition: all 0.3s;
    
    &:hover {
      transform: translateY(-4px);
    }
    
    .sample-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 15px;
      
      .sample-time {
        font-size: 12px;
        color: #909399;
      }
    }
    
    h3 {
      font-size: 16px;
      font-weight: 600;
      margin-bottom: 8px;
      color: #303133;
    }
    
    .sample-id {
      font-size: 13px;
      color: #606266;
      margin-bottom: 10px;
    }
    
    .sample-info {
      font-size: 12px;
      color: #909399;
      
      div {
        margin-bottom: 4px;
      }
    }
  }
}

::v-deep .el-timeline-item__timestamp {
  color: #909399;
  font-size: 13px;
}
</style>





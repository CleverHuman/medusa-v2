<template>
  <div class="vendor-application-container">
    <div class="application-header">
      <h1>Vendor Application</h1>
      <p>Please complete the following information. We will review your application as soon as possible.</p>
    </div>

    <div class="application-content">
      <!-- Progress Steps -->
      <el-steps :active="currentStep" finish-status="success" align-center class="steps-bar">
        <el-step title="Basic Information"></el-step>
        <el-step title="Product Details"></el-step>
        <el-step title="Contact Information"></el-step>
        <el-step title="Review & Submit"></el-step>
      </el-steps>

      <!-- Form -->
      <el-form ref="applicationForm" :model="form" :rules="rules" label-width="180px" class="application-form" @submit.native.prevent>
        
        <!-- Step 1: Basic Information -->
        <div v-show="currentStep === 0" class="form-step">
          <h2 class="step-title">Basic Information</h2>
          
          <el-form-item label="Vendor Name" prop="vendorName">
            <el-input v-model="form.vendorName" placeholder="Enter vendor name"></el-input>
          </el-form-item>

          <el-form-item label="Market Experience">
            <el-radio-group v-model="form.hasMarketExperience">
              <el-radio :label="false">No</el-radio>
              <el-radio :label="true">Yes</el-radio>
            </el-radio-group>
          </el-form-item>

          <div v-if="form.hasMarketExperience">
            <el-form-item label="Existing Market Names">
              <el-input v-model="form.existingMarkets" 
                        placeholder="e.g., AlphaBay, Dream Market"></el-input>
            </el-form-item>
            
            <el-form-item label="Years of Experience">
              <el-input-number v-model="form.experienceYears" :min="0" :max="50"></el-input-number>
            </el-form-item>
          </div>

          <el-form-item label="PGP Public Key" prop="pgpPublicKey">
            <el-input 
              type="textarea" 
              :rows="8" 
              v-model="form.pgpPublicKey" 
              placeholder="-----BEGIN PGP PUBLIC KEY BLOCK-----&#10;&#10;mQINBF...&#10;&#10;-----END PGP PUBLIC KEY BLOCK-----"
              style="font-family: monospace; font-size: 12px;">
            </el-input>
            <div class="field-tip">Paste your complete PGP public key here. Used for identity verification and secure communication.</div>
          </el-form-item>

          <el-form-item label="Location (State)" prop="location">
            <el-select v-model="form.location" placeholder="Select State" style="width: 100%">
              <el-option label="New South Wales (NSW)" value="New South Wales"></el-option>
              <el-option label="Victoria (VIC)" value="Victoria"></el-option>
              <el-option label="Queensland (QLD)" value="Queensland"></el-option>
              <el-option label="South Australia (SA)" value="South Australia"></el-option>
              <el-option label="Western Australia (WA)" value="Western Australia"></el-option>
              <el-option label="Tasmania (TAS)" value="Tasmania"></el-option>
              <el-option label="Australian Capital Territory (ACT)" value="Australian Capital Territory"></el-option>
              <el-option label="Northern Territory (NT)" value="Northern Territory"></el-option>
            </el-select>
          </el-form-item>
        </div>

        <!-- Step 2: Product Details -->
        <div v-show="currentStep === 1" class="form-step">
          <h2 class="step-title">Product Details</h2>
          
          <el-form-item label="Product Categories" prop="productCategories">
            <el-checkbox-group v-model="form.productCategories">
              <el-checkbox label="Electronics">Electronics</el-checkbox>
              <el-checkbox label="Fashion Accessories">Fashion Accessories</el-checkbox>
              <el-checkbox label="Home & Garden">Home & Garden</el-checkbox>
              <el-checkbox label="Beauty & Personal Care">Beauty & Personal Care</el-checkbox>
              <el-checkbox label="Food & Beverages">Food & Beverages</el-checkbox>
              <el-checkbox label="Sports & Outdoors">Sports & Outdoors</el-checkbox>
              <el-checkbox label="Books & Stationery">Books & Stationery</el-checkbox>
              <el-checkbox label="Others">Others</el-checkbox>
            </el-checkbox-group>
          </el-form-item>

          <el-form-item label="Estimated Stock Volume" prop="stockVolume">
            <el-select v-model="form.stockVolume" placeholder="Select Stock Volume" style="width: 100%">
              <el-option label="Small (< 100 units)" value="small"></el-option>
              <el-option label="Medium (100-1,000 units)" value="medium"></el-option>
              <el-option label="Large (1,000-10,000 units)" value="large"></el-option>
              <el-option label="Very Large (> 10,000 units)" value="xlarge"></el-option>
            </el-select>
          </el-form-item>

          <el-form-item label="Offline Delivery">
            <el-radio-group v-model="form.offlineDelivery">
              <el-radio :label="false">No</el-radio>
              <el-radio :label="true">Yes - Willing to do Face-to-Face Delivery</el-radio>
            </el-radio-group>
          </el-form-item>

          <el-form-item label="Product Description">
            <el-input 
              type="textarea" 
              :rows="4" 
              v-model="form.productDescription"
              placeholder="Please describe your main products, features, advantages, etc...">
            </el-input>
          </el-form-item>
        </div>

        <!-- Step 3: Contact Information -->
        <div v-show="currentStep === 2" class="form-step">
          <h2 class="step-title">Contact Information</h2>
          
          <h3 class="subsection-title">Primary Contact (Select at least one)</h3>
          
          <el-form-item label="Telegram" prop="primaryTelegram">
            <el-input v-model="form.primaryTelegram" placeholder="@username">
              <template slot="prepend">
                <el-checkbox v-model="primaryContactMethods.telegram"></el-checkbox>
              </template>
            </el-input>
          </el-form-item>

          <el-form-item label="Signal" prop="primarySignal">
            <el-input v-model="form.primarySignal" placeholder="Phone Number">
              <template slot="prepend">
                <el-checkbox v-model="primaryContactMethods.signal"></el-checkbox>
              </template>
            </el-input>
          </el-form-item>

          <el-form-item label="Jabber" prop="primaryJabber">
            <el-input v-model="form.primaryJabber" placeholder="username@domain.com">
              <template slot="prepend">
                <el-checkbox v-model="primaryContactMethods.jabber"></el-checkbox>
              </template>
            </el-input>
          </el-form-item>

          <el-form-item label="Email" prop="primaryEmail">
            <el-input v-model="form.primaryEmail" placeholder="example@domain.com">
              <template slot="prepend">
                <el-checkbox v-model="primaryContactMethods.email"></el-checkbox>
              </template>
            </el-input>
          </el-form-item>

          <h3 class="subsection-title">Secondary Contact (Select at least one)</h3>

          <el-form-item label="Telegram" prop="secondaryTelegram">
            <el-input v-model="form.secondaryTelegram" placeholder="@username">
              <template slot="prepend">
                <el-checkbox v-model="secondaryContactMethods.telegram"></el-checkbox>
              </template>
            </el-input>
          </el-form-item>

          <el-form-item label="Signal" prop="secondarySignal">
            <el-input v-model="form.secondarySignal" placeholder="Phone Number">
              <template slot="prepend">
                <el-checkbox v-model="secondaryContactMethods.signal"></el-checkbox>
              </template>
            </el-input>
          </el-form-item>

          <el-form-item label="Jabber" prop="secondaryJabber">
            <el-input v-model="form.secondaryJabber" placeholder="username@domain.com">
              <template slot="prepend">
                <el-checkbox v-model="secondaryContactMethods.jabber"></el-checkbox>
              </template>
            </el-input>
          </el-form-item>

          <el-form-item label="Email" prop="secondaryEmail">
            <el-input v-model="form.secondaryEmail" placeholder="example@domain.com">
              <template slot="prepend">
                <el-checkbox v-model="secondaryContactMethods.email"></el-checkbox>
              </template>
            </el-input>
          </el-form-item>
        </div>

        <!-- Step 4: Review & Submit -->
        <div v-show="currentStep === 3" class="form-step">
          <h2 class="step-title">Review Information</h2>
          
          <div class="summary-card">
            <h3>Application Summary</h3>
            <el-descriptions :column="2" border>
              <el-descriptions-item label="Vendor Name">{{ form.vendorName }}</el-descriptions-item>
              <el-descriptions-item label="Location">{{ form.location }}</el-descriptions-item>
              <el-descriptions-item label="Market Experience">{{ form.hasMarketExperience ? 'Yes' : 'No' }}</el-descriptions-item>
              <el-descriptions-item label="Experience Years" v-if="form.hasMarketExperience">{{ form.experienceYears || '-' }}</el-descriptions-item>
              <el-descriptions-item label="Product Categories" :span="2">{{ form.productCategories.join(', ') || '-' }}</el-descriptions-item>
              <el-descriptions-item label="Stock Volume">{{ getStockVolumeLabel(form.stockVolume) }}</el-descriptions-item>
              <el-descriptions-item label="Offline Delivery">{{ form.offlineDelivery ? 'Yes' : 'No' }}</el-descriptions-item>
              <el-descriptions-item label="Primary Contact" :span="2">{{ getPrimaryContactSummary() }}</el-descriptions-item>
              <el-descriptions-item label="Secondary Contact" :span="2">{{ getSecondaryContactSummary() }}</el-descriptions-item>
            </el-descriptions>
          </div>

          <el-alert
            title="Important Notice"
            type="info"
            :closable="false"
            show-icon
            class="notice-alert">
            <p>Please review the information carefully. After submission, your application will enter the review process. You can track the progress using your application ID during the review period.</p>
          </el-alert>

          <el-form-item prop="agreeTerms">
            <el-checkbox v-model="form.agreeTerms">
              I have read and agree to the <a href="#" class="link">Terms of Service</a> and <a href="#" class="link">Privacy Policy</a>
            </el-checkbox>
          </el-form-item>
        </div>

      </el-form>
      
      <!-- Navigation Buttons -->
      <div class="form-actions">
        <div class="action-left">
          <el-button v-show="currentStep > 0" @click="prevStep" size="large">
            <i class="el-icon-arrow-left"></i> Previous
          </el-button>
          <el-button @click="saveDraft" size="large">
            <i class="el-icon-document"></i> Save Draft
          </el-button>
        </div>
        <div class="action-right">
          <el-button v-show="currentStep < 3" type="primary" @click="nextStep" size="large">
            Next <i class="el-icon-arrow-right"></i>
          </el-button>
          <el-button v-show="currentStep === 3" type="success" @click="submitForm" :loading="submitting" size="large">
            <i class="el-icon-check"></i> Submit Application
          </el-button>
        </div>
      </div>
    </div>

    <!-- Success Dialog -->
    <el-dialog
      title="Application Submitted Successfully!"
      :visible.sync="successDialogVisible"
      width="500px"
      center
      :close-on-click-modal="false">
      <div class="success-content">
        <i class="el-icon-success success-icon"></i>
        <p class="success-message">Your application has been submitted.</p>
        <p class="application-id-label">Application ID:</p>
        <p class="application-id">{{ submittedApplicationId }}</p>
        <p class="success-tip">We will complete the review within 3-5 business days. Please be patient.</p>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="goHome">Back to Home</el-button>
        <el-button type="primary" @click="trackStatus">Track Status</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { submitVendorApplication, saveDraft as saveDraftApi, loadDraft as loadDraftApi, clearDraft as clearDraftApi } from '@/api/vendor'

export default {
  name: 'VendorApplication',
  data() {
    return {
      currentStep: 0,
      submitting: false,
      successDialogVisible: false,
      submittedApplicationId: '',
      
      primaryContactMethods: {
        telegram: false,
        signal: false,
        jabber: false,
        email: false
      },
      secondaryContactMethods: {
        telegram: false,
        signal: false,
        jabber: false,
        email: false
      },
      
      form: {
        vendorName: '',
        hasMarketExperience: false,
        existingMarkets: '',
        experienceYears: null,
        pgpPublicKey: '',
        location: '',
        productCategories: [],
        stockVolume: '',
        offlineDelivery: false,
        productDescription: '',
        primaryTelegram: '',
        primarySignal: '',
        primaryJabber: '',
        primaryEmail: '',
        secondaryTelegram: '',
        secondarySignal: '',
        secondaryJabber: '',
        secondaryEmail: '',
        agreeTerms: false
      },
      
      rules: {
        vendorName: [
          { required: true, message: 'Vendor name is required', trigger: 'blur' }
        ],
        pgpPublicKey: [
          { required: true, message: 'PGP Public Key is required', trigger: 'blur' },
          { validator: this.validatePGPKey, trigger: 'blur' }
        ],
        location: [
          { required: true, message: 'Please select your location', trigger: 'change' }
        ],
        productCategories: [
          { type: 'array', required: true, message: 'Please select at least one product category', trigger: 'change' }
        ],
        stockVolume: [
          { required: true, message: 'Please select estimated stock volume', trigger: 'change' }
        ],
        agreeTerms: [
          { validator: this.validateAgreeTerms, trigger: 'change' }
        ]
      }
    }
  },
  
  mounted() {
    this.loadDraft()
    // Debug: log current step
    console.log('Vendor Application mounted, current step:', this.currentStep)
  },
  
  watch: {
    currentStep(newVal) {
      // Debug: log step changes
      console.log('Step changed to:', newVal)
      // Scroll to top when step changes
      window.scrollTo({ top: 0, behavior: 'smooth' })
    }
  },
  
  methods: {
    // Validate PGP Public Key
    validatePGPKey(rule, value, callback) {
      if (!value) {
        callback(new Error('PGP Public Key is required'))
      } else if (!/-----BEGIN PGP PUBLIC KEY BLOCK-----/.test(value) || 
                 !/-----END PGP PUBLIC KEY BLOCK-----/.test(value)) {
        callback(new Error('Please enter a valid PGP public key (must include BEGIN and END markers)'))
      } else if (value.length < 200) {
        callback(new Error('PGP public key seems too short. Please paste the complete key.'))
      } else {
        callback()
      }
    },
    
    // Validate agree terms
    validateAgreeTerms(rule, value, callback) {
      if (!value) {
        callback(new Error('You must agree to the Terms of Service and Privacy Policy'))
      } else {
        callback()
      }
    },
    
    // Validate contact methods
    validateContactMethods(isPrimary) {
      const methods = isPrimary ? this.primaryContactMethods : this.secondaryContactMethods
      const hasSelected = Object.values(methods).some(v => v)
      
      if (!hasSelected) {
        return isPrimary ? 
          'Please select at least one primary contact method' : 
          'Please select at least one secondary contact method'
      }
      
      // Check if selected contact has value
      for (const [key, selected] of Object.entries(methods)) {
        if (selected) {
          const fieldName = isPrimary ? `primary${key.charAt(0).toUpperCase() + key.slice(1)}` : `secondary${key.charAt(0).toUpperCase() + key.slice(1)}`
          if (this.form[fieldName] && this.form[fieldName].trim()) {
            return true
          }
        }
      }
      
      return isPrimary ? 
        'Please fill in primary contact details' : 
        'Please fill in secondary contact details'
    },
    
    // Next step
    nextStep() {
      const fieldsToValidate = this.getStepFields(this.currentStep)
      
      // If no specific fields to validate, just go to next step
      if (fieldsToValidate.length === 0 && this.currentStep !== 2) {
        this.currentStep++
        this.saveDraft()
        return
      }
      
      // Validate fields
      this.$refs.applicationForm.validate((valid) => {
        if (valid) {
          // Additional validation for step 3 (contact methods)
          if (this.currentStep === 2) {
            const primaryValid = this.validateContactMethods(true)
            const secondaryValid = this.validateContactMethods(false)
            
            if (primaryValid !== true) {
              this.$message.error(primaryValid)
              return
            }
            if (secondaryValid !== true) {
              this.$message.error(secondaryValid)
              return
            }
          }
          
          this.currentStep++
          this.saveDraft()
        } else {
          this.$message.error('Please fill in all required fields')
        }
      })
    },
    
    // Previous step
    prevStep() {
      if (this.currentStep > 0) {
        this.currentStep--
      }
    },
    
    // Get fields to validate for each step
    getStepFields(step) {
      const fieldMap = {
        0: ['vendorName', 'pgpPublicKey', 'location'],
        1: ['productCategories', 'stockVolume'],
        2: [], // Contact methods validated separately
        3: ['agreeTerms']
      }
      return fieldMap[step] || []
    },
    
    // Submit form
    submitForm() {
      this.$refs.applicationForm.validate((valid) => {
        if (valid) {
          // Final validation for contact methods
          const primaryValid = this.validateContactMethods(true)
          const secondaryValid = this.validateContactMethods(false)
          
          if (primaryValid !== true) {
            this.$message.error(primaryValid)
            return
          }
          if (secondaryValid !== true) {
            this.$message.error(secondaryValid)
            return
          }
          
          this.submitting = true
          
          // Prepare data for submission
          const submitData = {
            ...this.form,
            hasMarketExperience: this.form.hasMarketExperience ? 1 : 0,
            offlineDelivery: this.form.offlineDelivery ? 1 : 0,
            productCategories: JSON.stringify(this.form.productCategories),
            pgpSignatureUrl: this.form.pgpPublicKey // Store PGP key in URL field for now
          }
          
          submitVendorApplication(submitData).then(response => {
            this.submitting = false
            this.submittedApplicationId = response.data.applicationId || response.data.id
            this.successDialogVisible = true
            clearDraftApi()
          }).catch(error => {
            this.submitting = false
            this.$message.error('Failed to submit application: ' + (error.msg || error.message))
          })
        } else {
          this.$message.error('Please fill in all required fields')
          return false
        }
      })
    },
    
    // Save draft
    saveDraft() {
      const draftData = {
        currentStep: this.currentStep,
        form: this.form,
        primaryContactMethods: this.primaryContactMethods,
        secondaryContactMethods: this.secondaryContactMethods
      }
      
      if (saveDraftApi(draftData)) {
        this.$message.success('Draft saved successfully')
      }
    },
    
    // Load draft
    loadDraft() {
      const draft = loadDraftApi()
      if (draft) {
        this.currentStep = draft.currentStep || 0
        this.form = { ...this.form, ...draft.form }
        this.primaryContactMethods = { ...this.primaryContactMethods, ...draft.primaryContactMethods }
        this.secondaryContactMethods = { ...this.secondaryContactMethods, ...draft.secondaryContactMethods }
      }
    },
    
    // Go to home
    goHome() {
      this.$router.push('/')
    },
    
    // Track status
    trackStatus() {
      this.$router.push('/vendor/status?id=' + this.submittedApplicationId)
    },
    
    // Get stock volume label
    getStockVolumeLabel(value) {
      const labels = {
        'small': 'Small (< 100 units)',
        'medium': 'Medium (100-1,000 units)',
        'large': 'Large (1,000-10,000 units)',
        'xlarge': 'Very Large (> 10,000 units)'
      }
      return labels[value] || value
    },
    
    // Get primary contact summary
    getPrimaryContactSummary() {
      const contacts = []
      if (this.primaryContactMethods.telegram && this.form.primaryTelegram) {
        contacts.push('Telegram: ' + this.form.primaryTelegram)
      }
      if (this.primaryContactMethods.signal && this.form.primarySignal) {
        contacts.push('Signal: ' + this.form.primarySignal)
      }
      if (this.primaryContactMethods.jabber && this.form.primaryJabber) {
        contacts.push('Jabber: ' + this.form.primaryJabber)
      }
      if (this.primaryContactMethods.email && this.form.primaryEmail) {
        contacts.push('Email: ' + this.form.primaryEmail)
      }
      return contacts.join(', ') || '-'
    },
    
    // Get secondary contact summary
    getSecondaryContactSummary() {
      const contacts = []
      if (this.secondaryContactMethods.telegram && this.form.secondaryTelegram) {
        contacts.push('Telegram: ' + this.form.secondaryTelegram)
      }
      if (this.secondaryContactMethods.signal && this.form.secondarySignal) {
        contacts.push('Signal: ' + this.form.secondarySignal)
      }
      if (this.secondaryContactMethods.jabber && this.form.secondaryJabber) {
        contacts.push('Jabber: ' + this.form.secondaryJabber)
      }
      if (this.secondaryContactMethods.email && this.form.secondaryEmail) {
        contacts.push('Email: ' + this.form.secondaryEmail)
      }
      return contacts.join(', ') || '-'
    }
  }
}
</script>

<style scoped lang="scss">
.vendor-application-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 40px 20px 60px;
  background-color: #f5f7fa;
  min-height: 100vh;
}

.application-header {
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

.application-content {
  background: white;
  border-radius: 8px;
  padding: 40px;
  box-shadow: 0 2px 12px 0 rgba(0,0,0,0.1);
  margin-bottom: 20px;
  display: flex;
  flex-direction: column;
}

.steps-bar {
  margin-bottom: 40px;
  flex-shrink: 0;
}

.application-form {
  flex: 1;
  margin-bottom: 0;
}

.form-step {
  min-height: auto;
  max-height: 60vh;
  overflow-y: auto;
  padding-right: 10px;
  margin-bottom: 20px;
  
  // 自定义滚动条样式
  &::-webkit-scrollbar {
    width: 6px;
  }
  
  &::-webkit-scrollbar-track {
    background: #f1f1f1;
    border-radius: 3px;
  }
  
  &::-webkit-scrollbar-thumb {
    background: #c1c1c1;
    border-radius: 3px;
    
    &:hover {
      background: #a8a8a8;
    }
  }
}

.step-title {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 30px;
  padding-bottom: 15px;
  border-bottom: 2px solid #409EFF;
}

.subsection-title {
  font-size: 18px;
  font-weight: 500;
  color: #606266;
  margin: 30px 0 20px 0;
}

.field-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}

.summary-card {
  background: #f5f7fa;
  padding: 20px;
  border-radius: 4px;
  margin-bottom: 20px;
  
  h3 {
    font-size: 18px;
    margin-bottom: 15px;
    color: #303133;
  }
}

.notice-alert {
  margin-bottom: 20px;
}

.link {
  color: #409EFF;
  text-decoration: none;
  
  &:hover {
    text-decoration: underline;
  }
}

.form-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 0;
  padding: 20px;
  background: #ffffff;
  border-radius: 4px;
  border: 2px solid #409EFF;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.2);
  flex-shrink: 0;
  position: sticky;
  bottom: 0;
  z-index: 10;
  
  .action-left,
  .action-right {
    display: flex;
    gap: 12px;
  }
  
  .el-button {
    min-width: 120px;
    font-weight: 500;
    
    &.is-loading {
      pointer-events: none;
    }
  }
}

.success-content {
  text-align: center;
  padding: 20px;
  
  .success-icon {
    font-size: 60px;
    color: #67C23A;
    margin-bottom: 20px;
  }
  
  .success-message {
    font-size: 16px;
    color: #606266;
    margin-bottom: 20px;
  }
  
  .application-id-label {
    font-size: 14px;
    color: #909399;
    margin-bottom: 5px;
  }
  
  .application-id {
    font-size: 20px;
    font-weight: bold;
    color: #409EFF;
    font-family: monospace;
    margin-bottom: 15px;
  }
  
  .success-tip {
    font-size: 13px;
    color: #909399;
  }
}

::v-deep .el-checkbox-group {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 15px;
  
  @media (max-width: 768px) {
    grid-template-columns: 1fr;
  }
}

::v-deep .el-checkbox {
  margin-right: 0;
  display: flex;
  align-items: center;
}
</style>


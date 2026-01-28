<template>
  <div class="app-container">
    <el-row :gutter="20">
      <!-- Configuration Form -->
      <el-col :span="12">
        <el-card class="box-card">
          <div slot="header" class="clearfix">
            <span>Telegram Policy Configuration</span>
          </div>
          
          <el-form ref="form" :model="form" :rules="rules" label-width="120px">
            <el-form-item label="Privacy Policy" prop="privacy_policy">
              <el-input 
                v-model="form.privacy_policy" 
                type="textarea"
                :rows="6"
                placeholder="Please enter privacy policy content"
                clearable
              />
            </el-form-item>
            
            <el-form-item label="Terms of Service" prop="terms_of_service">
              <el-input 
                v-model="form.terms_of_service" 
                type="textarea"
                :rows="6"
                placeholder="Please enter terms of service content"
                clearable
              />
            </el-form-item>
            
            <el-form-item label="Refund Policy" prop="refund_policy">
              <el-input 
                v-model="form.refund_policy" 
                type="textarea"
                :rows="6"
                placeholder="Please enter refund policy content"
                clearable
              />
            </el-form-item>
            
            <el-form-item label="Shipping Policy" prop="shipping_policy">
              <el-input 
                v-model="form.shipping_policy" 
                type="textarea"
                :rows="6"
                placeholder="Please enter shipping policy content"
                clearable
              />
            </el-form-item>
            
            <el-form-item label="Contact Information" prop="contact_info">
              <el-input 
                v-model="form.contact_info" 
                type="textarea"
                :rows="4"
                placeholder="Please enter contact information"
                clearable
              />
            </el-form-item>
          </el-form>
          
          <!-- Save button moved to bottom -->
          <div class="save-button-container">
            <el-button 
              type="primary" 
              size="medium" 
              icon="el-icon-check"
              @click="handleSave"
              :loading="loading"
            >Save Configuration</el-button>
          </div>
        </el-card>
      </el-col>
      
      <!-- Preview Area -->
      <el-col :span="12">
        <el-card class="box-card">
          <div slot="header" class="clearfix">
            <span>Preview Effect</span>
            <el-button style="float: right; padding: 3px 0" type="text" @click="refreshPreview">Refresh Preview</el-button>
          </div>
          
          <div class="preview-container">
            <div class="telegram-preview">
              <div class="preview-header">
                <div class="preview-avatar">TG</div>
                <div class="preview-title">Telegram Bot</div>
              </div>
              
              <div class="preview-content">
                <!-- Policy Content Preview -->
                <div class="policy-section" v-if="form.privacy_policy">
                  <h4>🔒 Privacy Policy</h4>
                  <div class="policy-content">{{ form.privacy_policy }}</div>
                </div>
                
                <div class="policy-section" v-if="form.terms_of_service">
                  <h4>📋 Terms of Service</h4>
                  <div class="policy-content">{{ form.terms_of_service }}</div>
                </div>
                
                <div class="policy-section" v-if="form.refund_policy">
                  <h4>💰 Refund Policy</h4>
                  <div class="policy-content">{{ form.refund_policy }}</div>
                </div>
                
                <div class="policy-section" v-if="form.shipping_policy">
                  <h4>🚚 Shipping Policy</h4>
                  <div class="policy-content">{{ form.shipping_policy }}</div>
                </div>
                
                <div class="policy-section" v-if="form.contact_info">
                  <h4>📞 Contact Information</h4>
                  <div class="policy-content">{{ form.contact_info }}</div>
                </div>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import { getTgPolicyConfig, saveTgPolicyConfig, updateTgPolicyConfig } from '@/api/mall/tgPolicyConfig'

export default {
  name: 'TgPolicyConfig',
  data() {
    return {
      form: {
        privacy_policy: '',
        terms_of_service: '',
        refund_policy: '',
        shipping_policy: '',
        contact_info: ''
      },
      rules: {
        privacy_policy: [
          { required: true, message: 'Please enter privacy policy content', trigger: 'blur' }
        ],
        terms_of_service: [
          { required: true, message: 'Please enter terms of service content', trigger: 'blur' }
        ]
      },
      loading: false
    }
  },
  created() {
    this.getConfig()
  },
  methods: {
    // 获取配置
    async getConfig() {
      try {
        this.loading = true
        const response = await getTgPolicyConfig()
        if (response.data) {
          this.form = response.data
        } else {
          // 如果没有配置，使用默认值
          this.form = {
            privacy_policy: 'We respect your privacy and are committed to protecting your personal data. This privacy policy explains how we collect, use, and safeguard your information when you use our services.',
            terms_of_service: 'By using our services, you agree to these terms and conditions. Please read them carefully before making any purchases.',
            refund_policy: 'We offer refunds within 7 days of purchase for digital products. Physical products may be returned within 14 days in original condition.',
            shipping_policy: 'We ship worldwide with discreet packaging. Shipping times vary by location, typically 7-21 business days.',
            contact_info: 'For support, please contact us at support@yourdomain.com or through our Telegram bot.'
          }
        }
      } catch (error) {
        console.error('Failed to get configuration:', error)
        this.$message.error('Failed to get configuration')
      } finally {
        this.loading = false
      }
    },
    
    // 保存配置
    async handleSave() {
      try {
        await this.$refs.form.validate()
        this.loading = true
        
        const response = await saveTgPolicyConfig(this.form)
        this.$message.success('Configuration saved successfully')
      } catch (error) {
        console.error('Failed to save configuration:', error)
        this.$message.error('Failed to save configuration')
      } finally {
        this.loading = false
      }
    },
    
    // 刷新预览
    refreshPreview() {
      // 预览会自动更新，这里可以添加额外的刷新逻辑
      this.$message.success('Preview refreshed')
    }
  }
}
</script>

<style scoped>
.save-button-container {
  text-align: center;
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
}

.preview-container {
  min-height: 400px;
}

.telegram-preview {
  max-width: 350px;
  margin: 0 auto;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  overflow: hidden;
  background: #fff;
}

.preview-header {
  background: #0088cc;
  color: white;
  padding: 10px;
  display: flex;
  align-items: center;
}

.preview-avatar {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: bold;
  margin-right: 10px;
}

.preview-title {
  font-size: 14px;
  font-weight: bold;
}

.preview-content {
  padding: 15px;
  max-height: 500px;
  overflow-y: auto;
}

.policy-section {
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #f0f0f0;
}

.policy-section:last-child {
  border-bottom: none;
}

.policy-section h4 {
  margin: 0 0 10px 0;
  color: #303133;
  font-size: 16px;
  font-weight: bold;
}

.policy-content {
  font-size: 14px;
  color: #606266;
  line-height: 1.6;
  white-space: pre-wrap;
  word-wrap: break-word;
}
</style> 
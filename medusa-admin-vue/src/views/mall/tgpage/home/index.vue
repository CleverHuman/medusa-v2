<template>
  <div class="app-container">
    <el-row :gutter="20">
      <!-- Configuration Form -->
      <el-col :span="12">
        <el-card class="box-card">
          <div slot="header" class="clearfix">
            <span>Telegram Home Configuration</span>
          </div>
          
          <el-form ref="form" :model="form" :rules="rules" label-width="120px">
            <el-form-item label="Banner Image" prop="banner_image">
              <div class="banner-upload-container">
                <el-input 
                  v-model="form.banner_image" 
                  placeholder="Please enter banner image URL or upload image"
                  clearable
                  style="margin-bottom: 10px;"
                />
                <div class="upload-section">
                  <span class="upload-label">Or upload from local:</span>
                  <ImageUpload 
                    v-model="form.banner_image"
                    :limit="1"
                    :file-size="5"
                    :file-type="['png', 'jpg', 'jpeg', 'gif']"
                    :is-show-tip="true"
                  />
                </div>
              </div>
              <div class="image-preview" v-if="bannerImageUrl">
                <el-image 
                  style="width: 200px; height: 100px; margin-top: 10px;"
                  :src="bannerImageUrl"
                  :preview-src-list="[bannerImageUrl]"
                  fit="cover">
                </el-image>
              </div>
            </el-form-item>
            
            <el-form-item label="Page Title" prop="title">
              <el-input 
                v-model="form.title" 
                placeholder="Please enter page title"
                clearable
              />
            </el-form-item>
            
            <el-form-item label="Page Description" prop="description">
              <el-input 
                v-model="form.description" 
                type="textarea"
                :rows="3"
                placeholder="Please enter page description"
                clearable
              />
            </el-form-item>
            
            <el-form-item label="Button Configuration">
              <div v-for="(button, index) in form.buttons" :key="index" class="button-item">
                <el-row :gutter="10">
                  <el-col :span="8">
                    <el-input 
                      v-model="button.label" 
                      placeholder="Button text"
                      size="small"
                    />
                  </el-col>
                  <el-col :span="8">
                    <el-input 
                      v-model="button.action" 
                      placeholder="Button action"
                      size="small"
                    />
                  </el-col>
                  <el-col :span="4">
                    <el-button 
                      type="danger" 
                      size="small" 
                      icon="el-icon-delete"
                      @click="removeButton(index)"
                    >Delete</el-button>
                  </el-col>
                </el-row>
              </div>
              <el-button 
                type="primary" 
                size="small" 
                icon="el-icon-plus"
                @click="addButton"
              >Add Button</el-button>
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
            
            <!-- Policy Configuration Entry -->
            <el-button 
              type="success" 
              size="medium" 
              icon="el-icon-document"
              @click="goToPolicyConfig"
              style="margin-left: 10px;"
            >Policy Configuration</el-button>
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
                <!-- Banner Image -->
                <div v-if="bannerImageUrl" class="preview-banner">
                  <img :src="bannerImageUrl" alt="Banner" />
                </div>
                
                <!-- Title -->
                <div v-if="form.title" class="preview-title-text">
                  {{ form.title }}
                </div>
                
                <!-- Description -->
                <div v-if="form.description" class="preview-description">
                  {{ form.description }}
                </div>
                
                <!-- Buttons - Changed to 2-column layout -->
                <div v-if="form.buttons && form.buttons.length > 0" class="preview-buttons">
                  <div class="button-grid">
                    <div 
                      v-for="(button, index) in form.buttons" 
                      :key="index"
                      class="preview-button"
                    >
                      {{ button.label }}
                    </div>
                  </div>
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
import { getTgHomeConfig, saveTgHomeConfig, updateTgHomeConfig } from '@/api/mall/tgHomeConfig'
import ImageUpload from '@/components/ImageUpload'

export default {
  name: 'TgHomeConfig',
  components: {
    ImageUpload
  },
  data() {
    return {
      form: {
        banner_image: '',
        title: '',
        description: '',
        buttons: []
      },
      rules: {
        title: [
          { required: true, message: 'Please enter page title', trigger: 'blur' }
        ],
        description: [
          { required: true, message: 'Please enter page description', trigger: 'blur' }
        ]
      },
      loading: false
    }
  },
  created() {
    this.getConfig()
  },
  computed: {
    // 处理Banner图片URL，确保能正确显示
    bannerImageUrl() {
      if (!this.form.banner_image) {
        return ''
      }
      // 如果已经是完整URL，直接返回
      if (this.form.banner_image.startsWith('http://') || this.form.banner_image.startsWith('https://')) {
        return this.form.banner_image
      }
      // 如果是相对路径，添加baseUrl
      return process.env.VUE_APP_BASE_API + this.form.banner_image
    }
  },
  methods: {
    // 获取配置
    async getConfig() {
      try {
        this.loading = true
        const response = await getTgHomeConfig()
        if (response.data) {
          this.form = response.data
        } else {
          // 如果没有配置，使用默认值
          this.form = {
            banner_image: 'https://yourdomain.com/profile/upload/2025/07/26/home_banner.jpg',
            title: 'Welcome to Telegram Store',
            description: 'We support BTC / USDT / XMR payment, global anonymous direct shipping!',
            buttons: [
              { label: '🛍 Browse Products', action: 'browse_products' },
              { label: '📦 My Orders', action: 'view_orders' },
              { label: '🎁 Limited Offers', action: 'promo' }
            ]
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
        
        const response = await saveTgHomeConfig(this.form)
        this.$message.success('Configuration saved successfully')
      } catch (error) {
        console.error('Failed to save configuration:', error)
        this.$message.error('Failed to save configuration')
      } finally {
        this.loading = false
      }
    },
    
    // 添加按钮
    addButton() {
      this.form.buttons.push({
        label: '',
        action: ''
      })
    },
    
    // 删除按钮
    removeButton(index) {
      this.form.buttons.splice(index, 1)
    },
    
    // 刷新预览
    refreshPreview() {
      // 预览会自动更新，这里可以添加额外的刷新逻辑
      this.$message.success('Preview refreshed')
    },
    
    // Navigate to Policy configuration page
    goToPolicyConfig() {
      this.$router.push('/mall/tgpage/policy')
    }
  }
}
</script>

<style scoped>
.button-item {
  margin-bottom: 10px;
  padding: 10px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  background-color: #fafafa;
}

.banner-upload-container {
  width: 100%;
}

.upload-section {
  margin-top: 10px;
  padding: 15px;
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  background-color: #fafafa;
}

.upload-label {
  display: block;
  margin-bottom: 10px;
  color: #606266;
  font-size: 14px;
}

.image-preview {
  margin-top: 10px;
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
}

.preview-banner {
  margin-bottom: 15px;
}

.preview-banner img {
  width: 100%;
  height: 120px;
  object-fit: cover;
  border-radius: 8px;
}

.preview-title-text {
  font-size: 18px;
  font-weight: bold;
  margin-bottom: 10px;
  color: #303133;
}

.preview-description {
  font-size: 14px;
  color: #606266;
  margin-bottom: 15px;
  line-height: 1.5;
}

.save-button-container {
  text-align: center;
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
}

.preview-buttons {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.button-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
}

.preview-button {
  background: #0088cc;
  color: white;
  padding: 10px 15px;
  border-radius: 6px;
  text-align: center;
  font-size: 14px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.preview-button:hover {
  background: #0077b3;
}
</style>

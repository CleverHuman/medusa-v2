<template>
  <div class="footer-page-manager">
    <el-link type="primary" @click="$router.back()">&lt; Back to page list</el-link>
    <h2>Footer</h2>
    
    <!-- Loading and Error States -->
    <div v-if="loading" class="loading-container">
      <el-loading-spinner></el-loading-spinner>
      <p>Loading footer data...</p>
    </div>
    
    <div v-if="error" class="error-message">
      <el-alert :title="error" type="error" show-icon></el-alert>
    </div>

    <el-form label-width="120px" class="form-block" v-if="!loading">

      <!-- Logo Image -->
      <div class="section">
        <div class="section-label">Logo image</div>
        <div class="image-list">
          <div 
            v-for="(image, index) in formData.logoImages" 
            :key="index"
            class="image-item"
          >
            <img 
              :src="getImageUrl(image.url)" 
              :alt="image.title || 'Logo'" 
              class="image-preview"
              @error="handleImageError(index)"
              @load="handleImageLoad(index)"
            />
            <div class="image-overlay">
              <div class="image-title">{{ image.title || 'Logo' }}</div>
              <div class="image-actions">
                <div class="preview" @click="previewImage(image.url, image.title || 'Logo')">Preview</div>
                <div class="remove" @click="removeLogoImage(index)">Remove</div>
              </div>
            </div>
          </div>
          <div v-if="formData.logoImages.length === 0" class="image-item red">
            <div class="image-text">No logo image</div>
            <div class="add-text">Click to add</div>
          </div>
        </div>
        <div class="add-link" @click="showLogoForm = true">+ add logo image</div>
        
        <el-input 
          v-model="formData.logoUrl" 
          placeholder="Logo URL (will be auto-generated)" 
          class="input-short" 
          disabled
        />
        <div class="el-upload__tip" style="color: #909399; font-size: 12px;">
          URL will be automatically generated based on current domain: {{ dynamicFooterLogoUrl }}
        </div>
        
        <!-- Logo Form -->
        <div v-if="showLogoForm" class="inline-form">
          <div class="upload-section">
            <div class="upload-tabs">
              <span 
                :class="['tab', { active: uploadMethod === 'url' }]" 
                @click="uploadMethod = 'url'"
              >
                URL
              </span>
              <span 
                :class="['tab', { active: uploadMethod === 'file' }]" 
                @click="uploadMethod = 'file'"
              >
                Upload File
              </span>
            </div>
            
            <!-- URL Input -->
            <div v-if="uploadMethod === 'url'" class="upload-content">
              <el-input 
                v-model="uploadForm.url" 
                placeholder="Image URL" 
                class="input-short"
                style="margin-bottom: 8px;"
              />
              <div class="el-upload__tip">Enter image URL and click "Add URL" button</div>
            </div>
            
            <!-- File Upload -->
            <div v-if="uploadMethod === 'file'" class="upload-content">
              <el-upload
                ref="upload"
                :action="uploadUrl"
                :headers="uploadHeaders"
                :before-upload="beforeUpload"
                :on-success="handleUploadSuccess"
                :on-error="handleUploadError"
                :show-file-list="false"
                accept="image/*"
                class="upload-demo"
              >
                <el-button size="small" type="primary">Select File</el-button>
                <div slot="tip" class="el-upload__tip">Only image files allowed. File will be added automatically after upload.</div>
              </el-upload>
              <div v-if="uploadForm.fileName" class="selected-file">
                Selected: {{ uploadForm.fileName }}
              </div>
            </div>
          </div>
          
          <el-input 
            v-model="uploadForm.title" 
            placeholder="Image title (optional)" 
            class="input-short"
            style="margin-bottom: 8px;"
          />
          <div class="form-buttons">
            <el-button size="small" type="primary" @click="addLogoImage" v-if="uploadMethod === 'url'">Add URL</el-button>
            <el-button size="small" @click="showLogoForm = false">Cancel</el-button>
          </div>
        </div>
      </div>

      <!-- Contacts -->
      <div
        class="section"
        v-for="(contact, idx) in formData.contacts"
        :key="idx"
      >
        <div class="section-label">Contact{{ idx + 1 }}</div>
        <el-input v-model="contact.name" placeholder="Contact name" class="input-short" />
        <el-input v-model="contact.url" placeholder="Contact URL" class="input-short" />
        <el-button 
          size="small" 
          type="danger" 
          @click="removeContact(idx)"
          style="margin-top: 4px;"
        >
          Remove Contact
        </el-button>
      </div>

      <!-- Add Contact Button -->
      <div class="section">
        <el-button type="primary" @click="addContact">+ Add Contact</el-button>
      </div>

      <!-- Save/Cancel -->
      <div class="btn-row">
        <el-button 
          type="primary" 
          class="btn-save" 
          @click="handleSave"
          :loading="saving"
          :disabled="loading"
        >
          <i class="el-icon-check"></i> Save
        </el-button>
        <el-button 
          class="btn-cancel" 
          @click="handleCancel"
          :disabled="loading || saving"
        >
          <i class="el-icon-refresh-left"></i> Reset
        </el-button>
      </div>
    </el-form>

    <!-- Image Preview Modal -->
    <el-dialog
      :visible.sync="previewVisible"
      :title="previewTitle"
      width="80%"
      center
      class="image-preview-dialog"
    >
      <div class="preview-container">
        <img :src="previewImageUrl" :alt="previewTitle" class="preview-image" />
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="previewVisible = false">Close</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { getMallPageConfigList, addMallPageConfig, updateMallPageConfig } from '@/api/mall/pageConfig';

export default {
  name: "FooterPageManager",
  data() {
    return {
      loading: false,
      saving: false,
      error: '',
      showLogoForm: false,
      uploadMethod: 'url', // 'url' or 'file'
      uploadForm: {
        url: '',
        title: '',
        fileName: '' // For file upload
      },
      formData: {
        logoImages: [],
        contacts: [
          { name: '', url: '' },
          { name: '', url: '' },
          { name: '', url: '' }
        ],
        logoUrl: ''
      },
      previewVisible: false,
      previewImageUrl: '',
      previewTitle: '',
      originalData: null,
      existingConfigs: {}, // Store existing config IDs
      uploadUrl: process.env.VUE_APP_BASE_API + '/common/upload', // Actual URL for file upload
      uploadHeaders: {
        Authorization: 'Bearer ' + this.$store.getters.token
      }
    };
  },
  created() {
    this.loadFooterData();
  },
  computed: {
    // Dynamic URL for Footer Logo
    dynamicFooterLogoUrl() {
      return this.generateDynamicUrl('/mall/static/home');
    }
  },
  methods: {
    // Method to dynamically generate URLs
    generateDynamicUrl(path) {
              // Get current page protocol and host
      const currentProtocol = window.location.protocol;
      const currentHost = window.location.host;
      
              // Build complete URL
      const fullUrl = `${currentProtocol}//${currentHost}${path}`;
      console.log('Generated dynamic URL:', fullUrl);
      return fullUrl;
    },
    
    async loadFooterData() {
      this.loading = true;
      this.error = '';
      
      try {
        const response = await getMallPageConfigList({
          page: 'footer'
        });
        
        if (response.rows) {
          this.processConfigData(response.rows);
        }
        
        // 保存原始数据用于重置
        this.originalData = JSON.parse(JSON.stringify(this.formData));
        
      } catch (error) {
        console.error('Failed to load footer data:', error);
        this.error = 'Failed to load footer page data. Please try again.';
      } finally {
        this.loading = false;
      }
    },

    processConfigData(configs) {
      console.log('Processing footer configs:', configs);
      
      // 重置表单数据
      this.formData = {
        logoImages: [],
        contacts: [
          { name: '', url: '' },
          { name: '', url: '' },
          { name: '', url: '' }
        ],
        logoUrl: ''
      };

      // Reset existing config IDs
      this.existingConfigs = {};

      // 处理配置数据
      for (const config of configs) {
        if (config.id) {
          this.existingConfigs[`${config.section}_${config.configKey}`] = config.id;
        }

        try {
          if (config.section === 'logo') {
            if (config.configKey === 'images') {
              const configData = JSON.parse(config.configValue || '{}');
              
              // Backward compatibility: if configData is an array, it's old format
              if (Array.isArray(configData)) {
                                  // Old format: direct image array
                this.formData.logoImages = configData;
                              } else {
                  // New format: contains images, url
                const images = configData.images || [];
                this.formData.logoImages = Array.isArray(images) ? images : [];
              }
            }
          } else if (config.section === 'contacts') {
            if (config.configKey === 'list') {
              const contacts = JSON.parse(config.configValue || '[]');
              this.formData.contacts = Array.isArray(contacts) && contacts.length > 0 
                ? contacts 
                : [{ name: '', url: '' }, { name: '', url: '' }, { name: '', url: '' }];
            }
          }
        } catch (error) {
          console.error('Error parsing config value:', config, error);
        }
      }
    },

    addLogoImage() {
      // Only handle manual URL input, file uploads are handled automatically
      if (!this.uploadForm.url.trim()) {
        this.$message.warning('Please enter an image URL');
        return;
      }

      // Convert URL to relative path for onion network compatibility
      let imageUrl = this.convertToRelativeUrl(this.uploadForm.url.trim());
      console.log('Adding logo image with relative URL:', imageUrl);
      
      this.formData.logoImages.push({
        url: imageUrl,
        title: this.uploadForm.title.trim() || 'Logo'
      });

      console.log('Current logo images:', this.formData.logoImages);

      // 重置表单
      this.uploadForm = { url: '', title: '', fileName: '' };
      this.showLogoForm = false;
      this.uploadMethod = 'url'; // Reset to URL tab
      this.$refs.upload.clearFiles(); // Clear file list after adding
    },

    removeLogoImage(index) {
      this.formData.logoImages.splice(index, 1);
    },

    addContact() {
      this.formData.contacts.push({ name: '', url: '' });
    },

    removeContact(index) {
      this.formData.contacts.splice(index, 1);
    },

    async handleSave() {
      this.saving = true;
      this.error = '';
      
      try {
        const configs = [];

        // Logo images config
        const logoConfig = {
          page: 'footer',
          section: 'logo',
          configKey: 'images',
          configValue: JSON.stringify({
            images: this.formData.logoImages,
            url: this.dynamicFooterLogoUrl
          }),
          sort: 1,
          updateBy: this.$store.getters.name || 'admin'
        };

        if (this.existingConfigs['logo_images']) {
          logoConfig.id = this.existingConfigs['logo_images'];
          configs.push(logoConfig);
        } else {
          configs.push(logoConfig);
        }

        // Contacts config
        const contactsConfig = {
          page: 'footer',
          section: 'contacts',
          configKey: 'list',
          configValue: JSON.stringify(this.formData.contacts),
          sort: 2,
          updateBy: this.$store.getters.name || 'admin'
        };

        if (this.existingConfigs['contacts_list']) {
          contactsConfig.id = this.existingConfigs['contacts_list'];
          configs.push(contactsConfig);
        } else {
          configs.push(contactsConfig);
        }

        // 保存所有配置
        for (const config of configs) {
          try {
            if (config.id) {
              // 更新现有配置
              console.log('Updating footer config:', config);
              await updateMallPageConfig(config);
            } else {
              // 创建新配置
              delete config.id; // Delete id field to avoid insertion errors
              console.log('Creating footer config:', config);
              await addMallPageConfig(config);
            }
          } catch (error) {
            console.error('Failed to save footer config:', config, error);
            throw error;
          }
        }
        
        this.$message.success('Footer data saved successfully!');
        
        // Reload data to update IDs
        await this.loadFooterData();
        
      } catch (error) {
        console.error('Failed to save footer data:', error);
        this.error = 'Failed to save footer page data. Please try again.';
      } finally {
        this.saving = false;
      }
    },

    handleCancel() {
      if (this.originalData) {
        this.formData = JSON.parse(JSON.stringify(this.originalData));
        this.$message.info('Data reset to last saved state');
      }
    },

    beforeUpload(file) {
      const isLt2M = file.size / 1024 / 1024 < 2;
      if (!isLt2M) {
        this.$message.error('Image must be smaller than 2MB!');
      }
      return isLt2M;
    },

    handleUploadSuccess(response, file, fileList) {
      console.log('Upload response:', response);
      console.log('File:', file);
      
      if (response.code === 200) {
        this.uploadForm.fileName = file.name;
        // Store the URL from the backend response (could be full URL or relative)
        let url = response.url;
        console.log('Original URL from backend:', url);
        
        // Convert absolute URL to relative path for onion network compatibility
        let relativeUrl = this.convertToRelativeUrl(url);
        console.log('Converted to relative URL:', relativeUrl);
        
        // Store the relative URL
        this.uploadForm.url = relativeUrl;
        console.log('Stored relative URL for upload:', this.uploadForm.url);
        
        // Automatically add the uploaded image to the list
        this.addUploadedImage(relativeUrl, file.name);
        
        this.$message.success('File uploaded successfully!');
      } else {
        this.$message.error('Upload failed: ' + response.msg);
      }
    },

    handleUploadError(err, file, fileList) {
      this.$message.error('Failed to upload file: ' + err);
    },

    handleImageError(index) {
      console.error('Image failed to load:', this.formData.logoImages[index]);
      this.$message.error('Failed to load image: ' + this.formData.logoImages[index].url);
    },

    handleImageLoad(index) {
      console.log('Image loaded successfully:', this.formData.logoImages[index]);
    },

    getImageUrl(url) {
      if (!url) return '';
      
      // Return the URL as is - backend handles full URLs
      return url;
    },

    addUploadedImage(url, fileName) {
      console.log('Adding uploaded image:', url, fileName);
      
      // Convert URL to relative path for onion network compatibility
      let imageUrl = this.convertToRelativeUrl(url);
      console.log('Converted uploaded image URL to relative:', imageUrl);
      
      // Add the image to the list
      this.formData.logoImages.push({
        url: imageUrl,
        title: fileName || 'Logo'
      });

      console.log('Current logo images after upload:', this.formData.logoImages);
      
      // Reset the upload form
      this.uploadForm = { url: '', title: '', fileName: '' };
      this.showLogoForm = false;
      this.uploadMethod = 'url'; // Reset to URL tab
      this.$refs.upload.clearFiles(); // Clear file list after adding
    },

    previewImage(url, title) {
      this.previewImageUrl = url;
      this.previewTitle = title;
      this.previewVisible = true;
    },

    // Convert absolute URL to relative path for onion network compatibility
    convertToRelativeUrl(url) {
      if (!url) return '';
      
      // If URL is already relative (starts with /), return as is
      if (url.startsWith('/')) {
        return url;
      }
      
      // If URL is absolute (starts with http:// or https://), extract the path
      if (url.startsWith('http://') || url.startsWith('https://')) {
        try {
          const urlObj = new URL(url);
          return urlObj.pathname;
        } catch (error) {
          console.error('Error parsing URL:', url, error);
          return url;
        }
      }
      
      // If URL doesn't start with /, assume it's a relative path and add /
      if (!url.startsWith('/')) {
        return '/' + url;
      }
      
      return url;
    }
  }
};
</script>

<style scoped>
.footer-page-manager {
  background: #f5f7fa;
  min-height: 100vh;
  padding: 30px 40px;
}

.loading-container {
  text-align: center;
  padding: 40px;
}

.error-message {
  margin-bottom: 20px;
}

h2 {
  margin-bottom: 20px;
}

.form-block {
  background: transparent;
}

.section {
  margin-bottom: 32px;
  display: flex;
  flex-direction: column;
}

.section-label {
  font-weight: bold;
  margin-bottom: 8px;
}

.image-list {
  display: flex;
  align-items: flex-end;
  margin-bottom: 4px;
  flex-wrap: wrap;
}

.image-item {
  width: 90px;
  height: 90px;
  margin-right: 16px;
  margin-bottom: 8px;
  border-radius: 6px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
  border: 2px solid #e0e0e0;
}

.image-item.red { 
  background: #ff7b7b; 
  border-color: #ff7b7b;
}

.image-preview {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.image-overlay {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: rgba(0, 0, 0, 0.7);
  color: white;
  padding: 4px;
  font-size: 11px;
  text-align: center;
}

.image-text {
  color: #fff;
  font-size: 15px;
  text-align: center;
}

.add-text {
  color: #fff;
  font-size: 12px;
  text-align: center;
  margin-top: 4px;
}

.remove {
  color: #ff3b3b;
  font-size: 13px;
  cursor: pointer;
  margin-top: 4px;
  text-align: center;
}

.add-link {
  color: #409EFF;
  font-size: 14px;
  cursor: pointer;
  margin-bottom: 8px;
  margin-top: 2px;
}

.inline-form {
  background: #f8f9fa;
  padding: 12px;
  border-radius: 6px;
  margin-top: 8px;
  border: 1px solid #e0e0e0;
}

.form-buttons {
  display: flex;
  gap: 8px;
}

.input-short {
  width: 320px;
  margin-bottom: 6px;
}

.btn-row {
  display: flex;
  justify-content: center;
  margin-top: 30px;
}

.btn-save, .btn-cancel {
  width: 160px;
  font-size: 18px;
  margin: 0 16px;
  height: 40px;
}

.btn-save {
  background: #333;
  color: #fff;
  border: none;
}

.btn-cancel {
  background: #888;
  color: #fff;
  border: none;
}

.upload-section {
  margin-bottom: 8px;
}

.upload-tabs {
  display: flex;
  border-bottom: 1px solid #dcdfe6;
  margin-bottom: 10px;
}

.tab {
  padding: 8px 15px;
  cursor: pointer;
  font-size: 14px;
  color: #606266;
  border-bottom: 2px solid transparent;
}

.tab.active {
  color: #409EFF;
  border-bottom-color: #409EFF;
  font-weight: bold;
}

.upload-content {
  margin-top: 10px;
}

.upload-demo {
  display: inline-block;
}

.selected-file {
  margin-top: 8px;
  font-size: 13px;
  color: #606266;
}

.image-actions {
  display: flex;
  gap: 8px;
  justify-content: center;
}

.preview {
  color: #409EFF;
  font-size: 11px;
  cursor: pointer;
  text-align: center;
  font-weight: bold;
}

.preview:hover {
  color: #66b1ff;
}

.image-preview-dialog .el-dialog__body {
  padding: 20px;
  text-align: center;
}

.preview-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400px;
}

.preview-image {
  max-width: 100%;
  max-height: 70vh;
  object-fit: contain;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}
</style>

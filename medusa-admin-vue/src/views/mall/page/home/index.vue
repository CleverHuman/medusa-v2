<template>
  <div class="home-page-manager">
    <el-link type="primary" @click="$router.back()">&lt; Back to page list</el-link>
    <h2>Home Page Manager</h2>
    
    <el-card>
      <div slot="header" class="clearfix">
        <span>Home Page Manager</span>
        <el-button style="float: right; padding: 3px 0" type="text" @click="refreshData">
          <i class="el-icon-refresh"></i> Refresh
        </el-button>
      </div>

      <el-alert
        v-if="error"
        :title="error"
        type="error"
        show-icon
        :closable="false"
        style="margin-bottom: 20px;"
      />

      <el-form label-width="120px" class="form-block" :model="formData" v-loading="loading">

        <!-- Logo Image -->
        <div class="section">
          <div class="section-label">Logo Image</div>
          <div class="image-list">
            <div
              v-for="(img, idx) in formData.logoImages"
              :key="idx"
              class="image-item"
              :style="{ backgroundImage: `url(${img.url})` }"
            >
              <div class="image-overlay">
                <div class="image-text">{{ img.title || 'Logo' }}</div>
                <div class="image-actions">
                  <div class="preview" @click="previewImage(img.url, img.title || 'Logo')">Preview</div>
                  <div class="remove" @click="removeImage('logo', idx)">Remove</div>
                </div>
              </div>
            </div>
          </div>
          
          <!-- Logo Inline Form -->
          <div v-if="showLogoForm" class="inline-form">
            <div class="form-header">
              <span class="form-title">Add Logo Image</span>
              <el-button type="text" @click="cancelAddImage('logo')" class="cancel-btn">
                <i class="el-icon-close"></i>
              </el-button>
            </div>
            <el-form :model="uploadForm" label-width="80px" class="add-image-form">
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
                    placeholder="Enter image URL"
                    @keyup.enter.native="confirmUpload"
                  />
                  <div class="el-upload__tip">Enter image URL and click "Add Image" button</div>
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
              <el-form-item>
                <el-button type="primary" @click="confirmUpload" :loading="uploading" v-if="uploadMethod === 'url'">
                  Add Image
                </el-button>
                <el-button @click="cancelAddImage('logo')">
                  Cancel
                </el-button>
              </el-form-item>
            </el-form>
          </div>
          
          <div class="add-link" @click="showAddImageForm('logo')" v-if="!showLogoForm">
            + Add Logo Image
          </div>
          
          <el-input 
            v-model="formData.logoUrl" 
            placeholder="Logo URL (will be auto-generated)" 
            class="input-short" 
            disabled
          />
          <div class="el-upload__tip" style="color: #909399; font-size: 12px;">
            URL will be automatically generated based on current domain: {{ dynamicLogoUrl }}
          </div>
        </div>

        <!-- Banner Images -->
        <div class="section">
          <div class="section-label">Banner Images</div>
          <div class="image-list">
            <div
              v-for="(img, idx) in formData.bannerImages"
              :key="idx"
              class="image-item"
              :style="{ backgroundImage: `url(${img.url})` }"
            >
              <div class="image-overlay">
                <div class="image-text">{{ img.title || 'Banner' }}</div>
                <div class="image-actions">
                  <div class="preview" @click="previewImage(img.url, img.title || 'Banner')">Preview</div>
                  <div class="remove" @click="removeImage('banner', idx)">Remove</div>
                </div>
              </div>
            </div>
          </div>
          
          <!-- Banner Inline Form -->
          <div v-if="showBannerForm" class="inline-form">
            <div class="form-header">
              <span class="form-title">Add Banner Image</span>
              <el-button type="text" @click="cancelAddImage('banner')" class="cancel-btn">
                <i class="el-icon-close"></i>
              </el-button>
            </div>
            <el-form :model="uploadForm" label-width="80px" class="add-image-form">
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
                    placeholder="Enter image URL"
                    @keyup.enter.native="confirmUpload"
                  />
                  <div class="el-upload__tip">Enter image URL and click "Add Image" button</div>
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
              <el-form-item>
                <el-button type="primary" @click="confirmUpload" :loading="uploading" v-if="uploadMethod === 'url'">
                  Add Image
                </el-button>
                <el-button @click="cancelAddImage('banner')">
                  Cancel
                </el-button>
              </el-form-item>
            </el-form>
          </div>
          
          <div class="add-link" @click="showAddImageForm('banner')" v-if="!showBannerForm">
            + Add Banner Image
          </div>
          
          <el-input 
            v-model="formData.bannerUrl" 
            placeholder="Banner URL (will be auto-generated)" 
            class="input-short" 
            disabled
          />
          <div class="el-upload__tip" style="color: #909399; font-size: 12px;">
            URL will be automatically generated based on current domain: {{ dynamicBannerUrl }}
          </div>
        </div>

        <!-- Digital Product -->
        <div class="section">
          <div class="section-label">Digital Product</div>
          <div class="image-list">
            <div
              v-for="(img, idx) in formData.digitalImages"
              :key="idx"
              class="image-item"
              :style="{ backgroundImage: `url(${img.url})` }"
            >
              <div class="image-overlay">
                <div class="image-text">{{ img.title || 'Digital' }}</div>
                <div class="image-actions">
                  <div class="preview" @click="previewImage(img.url, img.title || 'Digital')">Preview</div>
                  <div class="remove" @click="removeImage('digital', idx)">Remove</div>
                </div>
              </div>
            </div>
          </div>
          
          <!-- Digital Product Inline Form -->
          <div v-if="showDigitalForm" class="inline-form">
            <div class="form-header">
              <span class="form-title">Add Digital Product Image</span>
              <el-button type="text" @click="cancelAddImage('digital')" class="cancel-btn">
                <i class="el-icon-close"></i>
              </el-button>
            </div>
            <el-form :model="uploadForm" label-width="80px" class="add-image-form">
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
                    placeholder="Enter image URL"
                    @keyup.enter.native="confirmUpload"
                  />
                  <div class="el-upload__tip">Enter image URL and click "Add Image" button</div>
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
              <el-form-item>
                <el-button type="primary" @click="confirmUpload" :loading="uploading" v-if="uploadMethod === 'url'">
                  Add Image
                </el-button>
                <el-button @click="cancelAddImage('digital')">
                  Cancel
                </el-button>
              </el-form-item>
            </el-form>
          </div>
          
          <div class="add-link" @click="showAddImageForm('digital')" v-if="!showDigitalForm">
            + Add Digital Product Image
          </div>
          
          <el-input 
            v-model="formData.digitalTitle" 
            placeholder="Digital Product Title" 
            class="input-short" 
          />
          <el-input 
            v-model="formData.digitalUrl" 
            placeholder="Digital Product URL (will be auto-generated)" 
            class="input-short" 
            disabled
          />
          <div class="el-upload__tip" style="color: #909399; font-size: 12px;">
            URL will be automatically generated based on current domain: {{ dynamicDigitalUrl }}
          </div>
        </div>

        <!-- Physical Product -->
        <div class="section">
          <div class="section-label">Physical Product</div>
          <div class="image-list">
            <div
              v-for="(img, idx) in formData.physicalImages"
              :key="idx"
              class="image-item"
              :style="{ backgroundImage: `url(${img.url})` }"
            >
              <div class="image-overlay">
                <div class="image-text">{{ img.title || 'Physical' }}</div>
                <div class="image-actions">
                  <div class="preview" @click="previewImage(img.url, img.title || 'Physical')">Preview</div>
                  <div class="remove" @click="removeImage('physical', idx)">Remove</div>
                </div>
              </div>
            </div>
          </div>
          
          <!-- Physical Product Inline Form -->
          <div v-if="showPhysicalForm" class="inline-form">
            <div class="form-header">
              <span class="form-title">Add Physical Product Image</span>
              <el-button type="text" @click="cancelAddImage('physical')" class="cancel-btn">
                <i class="el-icon-close"></i>
              </el-button>
            </div>
            <el-form :model="uploadForm" label-width="80px" class="add-image-form">
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
                    placeholder="Enter image URL"
                    @keyup.enter.native="confirmUpload"
                  />
                  <div class="el-upload__tip">Enter image URL and click "Add Image" button</div>
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
              <el-form-item>
                <el-button type="primary" @click="confirmUpload" :loading="uploading" v-if="uploadMethod === 'url'">
                  Add Image
                </el-button>
                <el-button @click="cancelAddImage('physical')">
                  Cancel
                </el-button>
              </el-form-item>
            </el-form>
          </div>
          
          <div class="add-link" @click="showAddImageForm('physical')" v-if="!showPhysicalForm">
            + Add Physical Product Image
          </div>
          
          <el-input 
            v-model="formData.physicalTitle" 
            placeholder="Physical Product Title" 
            class="input-short" 
          />
          <el-input 
            v-model="formData.physicalUrl" 
            placeholder="Physical Product URL (will be auto-generated)" 
            class="input-short" 
            disabled
          />
          <div class="el-upload__tip" style="color: #909399; font-size: 12px;">
            URL will be automatically generated based on current domain: {{ dynamicPhysicalUrl }}
          </div>
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
    </el-card>

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
  name: "HomePageManager",
  data() {
    return {
      loading: false,
      saving: false,
      uploading: false,
      error: '',
      currentUploadType: '',
      showLogoForm: false,
      showBannerForm: false,
      showDigitalForm: false,
      showPhysicalForm: false,
      uploadMethod: 'url', // 'url' or 'file'
      uploadForm: {
        url: '',
        fileName: '' // For file upload
      },
      uploadUrl: process.env.VUE_APP_BASE_API + '/common/upload', // Actual URL for file upload
      uploadHeaders: {
        Authorization: 'Bearer ' + this.$store.getters.token
      },
      previewVisible: false,
      previewImageUrl: '',
      previewTitle: '',
      formData: {
        logoImages: [],
        bannerImages: [],
        digitalImages: [],
        physicalImages: [],
        digitalTitle: '',
        digitalUrl: '',
        physicalTitle: '',
        physicalUrl: '',
        logoUrl: '',
        bannerUrl: ''
      },
      originalData: null,
      existingConfigs: {} // Store existing config IDs
    };
  },
  created() {
    this.loadHomeData();
  },
  computed: {
    // Dynamically generate correct URLs
    dynamicDigitalUrl() {
      return this.generateDynamicUrl('/mall/static/products?category=Digital%20Product');
    },
    dynamicPhysicalUrl() {
      return this.generateDynamicUrl('/mall/static/products?category=Physical%20Product');
    },
    // Dynamic URLs for Banner and Logo
    dynamicBannerUrl() {
      return this.generateDynamicUrl('/mall/static/products');
    },
    dynamicLogoUrl() {
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
    
    async loadHomeData() {
      this.loading = true;
      this.error = '';
      
      try {
        const response = await getMallPageConfigList({
          page: 'home'
        });
        
        if (response.rows) {
          this.processConfigData(response.rows);
        }
        
        // 保存原始数据用于重置
        this.originalData = JSON.parse(JSON.stringify(this.formData));
        
      } catch (error) {
        console.error('Failed to load home data:', error);
        this.error = 'Failed to load home page data. Please try again.';
      } finally {
        this.loading = false;
      }
    },

    processConfigData(configs) {
      console.log('Processing configs:', configs);
      
      // 重置表单数据
      this.formData = {
        logoImages: [],
        bannerImages: [],
        digitalImages: [],
        physicalImages: [],
        digitalTitle: '',
        digitalUrl: '',
        physicalTitle: '',
        physicalUrl: '',
        logoUrl: '',
        bannerUrl: ''
      };

      // Reset existing config IDs
      this.existingConfigs = {};

      // 处理配置数据
      configs.forEach(config => {
        const { id, section, configKey, configValue, sort } = config;
        
        console.log(`Processing config: section=${section}, configKey=${configKey}, configValue=${configValue}`);
        
        // Save existing config IDs
        const key = `${section}_${configKey}`;
        this.existingConfigs[key] = id;
        
        if (section === 'logo' && configKey === 'images') {
          try {
            const configData = configValue ? JSON.parse(configValue) : {};
            
            // Backward compatibility: if configData is an array, it's old format
            if (Array.isArray(configData)) {
              // Old format: direct image array
              this.formData.logoImages = configData.sort((a, b) => (a.sort || 0) - (b.sort || 0));
            } else {
              // New format: contains images, url
              const images = configData.images || [];
              this.formData.logoImages = Array.isArray(images) ? images.sort((a, b) => (a.sort || 0) - (b.sort || 0)) : [];
            }
            console.log('Loaded logo images:', this.formData.logoImages);
          } catch (e) {
            console.error('Failed to parse logo images:', e);
            this.formData.logoImages = [];
          }
        } else if (section === 'banner' && configKey === 'images') {
          try {
            const configData = configValue ? JSON.parse(configValue) : {};
            
            // Backward compatibility: if configData is an array, it's old format
            if (Array.isArray(configData)) {
              // Old format: direct image array
              this.formData.bannerImages = configData.sort((a, b) => (a.sort || 0) - (b.sort || 0));
            } else {
              // New format: contains images, url
              const images = configData.images || [];
              this.formData.bannerImages = Array.isArray(images) ? images.sort((a, b) => (a.sort || 0) - (b.sort || 0)) : [];
            }
            console.log('Loaded banner images:', this.formData.bannerImages);
          } catch (e) {
            console.error('Failed to parse banner images:', e);
            this.formData.bannerImages = [];
          }
        } else if (section === 'digital' && configKey === 'images') {
          try {
            const configData = configValue ? JSON.parse(configValue) : {};
            
            // Backward compatibility: if configData is an array, it's old format
            if (Array.isArray(configData)) {
              // Old format: direct image array, remove title field from each image
              this.formData.digitalImages = configData.map(img => {
                const { title, ...rest } = img;
                return rest;
              }).sort((a, b) => (a.sort || 0) - (b.sort || 0));
              this.formData.digitalTitle = '';
              this.formData.digitalUrl = '';
            } else {
              // New format: contains images, title, url
              const images = configData.images || [];
              // Ensure image objects don't contain title field
              this.formData.digitalImages = Array.isArray(images) ? 
                images.map(img => {
                  const { title, ...rest } = img;
                  return rest;
                }).sort((a, b) => (a.sort || 0) - (b.sort || 0)) : [];
              this.formData.digitalTitle = configData.title || '';
              // Don't set digitalUrl, let it use dynamically generated URL
              this.formData.digitalUrl = '';
            }
            console.log('Loaded digital config:', { images: this.formData.digitalImages, title: this.formData.digitalTitle, url: this.formData.digitalUrl });
          } catch (e) {
            console.error('Failed to parse digital config:', e);
            this.formData.digitalImages = [];
            this.formData.digitalTitle = '';
            this.formData.digitalUrl = '';
          }
        } else if (section === 'physical' && configKey === 'images') {
          try {
            const configData = configValue ? JSON.parse(configValue) : {};
            
            // Backward compatibility: if configData is an array, it's old format
            if (Array.isArray(configData)) {
              // Old format: direct image array, remove title field from each image
              this.formData.physicalImages = configData.map(img => {
                const { title, ...rest } = img;
                return rest;
              }).sort((a, b) => (a.sort || 0) - (b.sort || 0));
              this.formData.physicalTitle = '';
              this.formData.physicalUrl = '';
            } else {
              // New format: contains images, title, url
              const images = configData.images || [];
              // Ensure image objects don't contain title field
              this.formData.physicalImages = Array.isArray(images) ? 
                images.map(img => {
                  const { title, ...rest } = img;
                  return rest;
                }).sort((a, b) => (a.sort || 0) - (b.sort || 0)) : [];
              this.formData.physicalTitle = configData.title || '';
              // Don't set physicalUrl, let it use dynamically generated URL
              this.formData.physicalUrl = '';
            }
            console.log('Loaded physical config:', { images: this.formData.physicalImages, title: this.formData.physicalTitle, url: this.formData.physicalUrl });
          } catch (e) {
            console.error('Failed to parse physical config:', e);
            this.formData.physicalImages = [];
            this.formData.physicalTitle = '';
            this.formData.physicalUrl = '';
          }
        }
      });
      
      console.log('Final formData:', this.formData);
    },

    showAddImageForm(type) {
      this.currentUploadType = type;
      this.uploadForm = { url: '', fileName: '' };
      this.uploadMethod = 'url'; // Reset to URL tab
      
      // 关闭所有其他表单
      this.showLogoForm = false;
      this.showBannerForm = false;
      this.showDigitalForm = false;
      this.showPhysicalForm = false;
      
      // 打开对应表单
      if (type === 'logo') {
        this.showLogoForm = true;
      } else if (type === 'banner') {
        this.showBannerForm = true;
      } else if (type === 'digital') {
        this.showDigitalForm = true;
      } else if (type === 'physical') {
        this.showPhysicalForm = true;
      }
    },

    cancelAddImage(type) {
      this.uploadForm = { url: '', fileName: '' };
      this.uploadMethod = 'url'; // Reset to URL tab
      this.showLogoForm = false;
      this.showBannerForm = false;
      this.showDigitalForm = false;
      this.showPhysicalForm = false;
      // Clear file upload component if it exists
      if (this.$refs.upload) {
        this.$refs.upload.clearFiles();
      }
    },



    removeImage(type, idx) {
      this.formData[`${type}Images`].splice(idx, 1);
    },

    async confirmUpload() {
      if (!this.uploadForm.url.trim()) {
        this.$message.warning('Please enter image URL');
        return;
      }

      this.uploading = true;
      
      try {
        // Convert URL to relative path for onion network compatibility
        let imageUrl = this.convertToRelativeUrl(this.uploadForm.url.trim());
        console.log('Adding image with relative URL:', imageUrl);
        
        const newImage = {
          url: imageUrl,
          sort: this.formData[`${this.currentUploadType}Images`].length
        };

        this.formData[`${this.currentUploadType}Images`].push(newImage);
        
        // 关闭内嵌表单
        this.cancelAddImage(this.currentUploadType);
        
        this.$message.success('Image added successfully');
        
      } catch (error) {
        console.error('Failed to add image:', error);
        this.$message.error('Failed to add image');
      } finally {
        this.uploading = false;
      }
    },

    async handleSave() {
      this.saving = true;
      this.error = '';
      
      try {
        const configs = [];
        
        // Logo images - 总是保存，即使为空数组
        configs.push({
          id: this.existingConfigs['logo_images'],
          page: 'home',
          section: 'logo',
          configKey: 'images',
          configValue: JSON.stringify({
            images: this.formData.logoImages,
            url: this.dynamicLogoUrl
          }),
          sort: 1
        });
        
        // Banner images - 总是保存，即使为空数组
        configs.push({
          id: this.existingConfigs['banner_images'],
          page: 'home',
          section: 'banner',
          configKey: 'images',
          configValue: JSON.stringify({
            images: this.formData.bannerImages,
            url: this.dynamicBannerUrl
          }),
          sort: 2
        });
        
        // Digital product images with title and url - 总是保存，即使为空数组
        configs.push({
          id: this.existingConfigs['digital_images'],
          page: 'home',
          section: 'digital',
          configKey: 'images',
          configValue: JSON.stringify({
            images: this.formData.digitalImages,
            title: this.formData.digitalTitle,
            url: this.dynamicDigitalUrl
          }),
          sort: 3
        });
        
        // Physical product images with title and url - 总是保存，即使为空数组
        configs.push({
          id: this.existingConfigs['physical_images'],
          page: 'home',
          section: 'physical',
          configKey: 'images',
          configValue: JSON.stringify({
            images: this.formData.physicalImages,
            title: this.formData.physicalTitle,
            url: this.dynamicPhysicalUrl
          }),
          sort: 4
        });
        
        console.log('Saving configs:', configs);
        
        // Save all configurations
        for (const config of configs) {
          try {
            if (config.id) {
              // 更新现有配置
              console.log('Updating config:', config);
              await updateMallPageConfig(config);
            } else {
              // 创建新配置
              delete config.id; // 删除 id 字段，避免插入时出错
              console.log('Creating config:', config);
              await addMallPageConfig(config);
            }
          } catch (error) {
            console.error('Failed to save config:', config, error);
            throw error;
          }
        }
        
        this.$message.success('Home page data saved successfully!');
        
        // Reload data to update IDs
        await this.loadHomeData();
        
      } catch (error) {
        console.error('Failed to save home data:', error);
        this.error = 'Failed to save home page data. Please try again.';
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

    refreshData() {
      this.loadHomeData();
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

    addUploadedImage(url, fileName) {
      console.log('Adding uploaded image:', url, fileName);
      
      // Convert URL to relative path for onion network compatibility
      let imageUrl = this.convertToRelativeUrl(url);
      console.log('Converted uploaded image URL to relative:', imageUrl);
      
      // Add the image to the list
      const newImage = {
        url: imageUrl,
        sort: this.formData[`${this.currentUploadType}Images`].length
      };

      this.formData[`${this.currentUploadType}Images`].push(newImage);
      
      console.log('Current images after upload:', this.formData[`${this.currentUploadType}Images`]);
      
      // Reset the upload form
      this.uploadForm = { url: '', fileName: '' };
      this.cancelAddImage(this.currentUploadType);
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
.home-page-manager {
  background: #f5f7fa;
  min-height: 100vh;
  padding: 30px 40px;
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
  margin-bottom: 16px;
  border-radius: 6px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  position: relative;
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  border: 2px solid #e4e7ed;
}

.image-item:not([style*="background-image"]) {
  background: #f5f7fa;
}

.image-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  border-radius: 4px;
  opacity: 0;
  transition: opacity 0.3s;
}

.image-item:hover .image-overlay {
  opacity: 1;
}

.image-text {
  color: #fff;
  font-size: 12px;
  text-align: center;
  margin-bottom: 4px;
}

.remove {
  color: #ff3b3b;
  font-size: 11px;
  cursor: pointer;
  text-align: center;
  font-weight: bold;
}

.add-link {
  color: #409EFF;
  font-size: 14px;
  cursor: pointer;
  margin-bottom: 8px;
  margin-top: 2px;
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

.dialog-footer {
  text-align: right;
}

.inline-form {
  background: #f8f9fa;
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  padding: 16px;
  margin: 12px 0;
}

.form-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.form-title {
  font-weight: bold;
  color: #333;
}

.cancel-btn {
  color: #909399;
}

.cancel-btn:hover {
  color: #f56c6c;
}

.add-image-form {
  margin: 0;
}

.add-image-form .el-form-item {
  margin-bottom: 16px;
}

.add-image-form .el-form-item:last-child {
  margin-bottom: 0;
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

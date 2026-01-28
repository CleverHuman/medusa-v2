<template>
  <div class="profile-agreement-editor">
    <el-link type="primary" @click="$router.back()">&lt; Back to page list</el-link>
    <h2>User Agreement Editor</h2>
    
    <el-card>
      <div slot="header" class="clearfix">
        <span>User Agreement Editor</span>
        <el-button style="float: right; padding: 3px 0" type="text" @click="refreshContent">
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
      
      <el-form :model="form" label-width="120px">
        <el-form-item label="Agreement Content:">
          <el-input
            type="textarea"
            v-model="form.content"
            :rows="20"
            placeholder="Edit agreement content here..."
            :disabled="loading"
          />
        </el-form-item>
        
        <el-form-item label="Comment:">
          <el-input
            v-model="form.remark"
            placeholder="Optional comment for this agreement"
            :disabled="loading"
          />
        </el-form-item>
      </el-form>
      
      <div class="button-group">
        <el-button type="primary" @click="saveAgreement" :loading="saving" :disabled="loading">
          <i class="el-icon-check"></i> Save Agreement
        </el-button>
        <el-button @click="resetForm" :disabled="loading || saving">
          <i class="el-icon-refresh-left"></i> Reset
        </el-button>
      </div>
    </el-card>
  </div>
</template>

<script>
import { getMallPageConfigList, addMallPageConfig, updateMallPageConfig } from '@/api/mall/pageConfig';

export default {
  name: 'ProfileAgreementEditor',
  data() {
    return {
      loading: false,
      saving: false,
      error: '',
      agreementId: null,
      form: {
        content: '',
        sort: 0,
        remark: ''
      }
    };
  },
  created() {
    this.fetchAgreement();
  },
  methods: {
    async fetchAgreement() {
      this.loading = true;
      this.error = '';
      
      try {
        // Query profile page agreement config
        const response = await getMallPageConfigList({
          page: 'profile',
          section: 'agreement'
        });
        
        if (response.rows && response.rows.length > 0) {
          // Find agreement config item
          const agreementConfig = response.rows.find(item => 
            item.configKey === 'content'
          );
          
          if (agreementConfig) {
            this.agreementId = agreementConfig.id;
            this.form.content = agreementConfig.configValue || '';
            this.form.sort = agreementConfig.sort || 0;
            this.form.remark = agreementConfig.remark || '';
          } else {
            // 如果没有找到，创建新的
            this.agreementId = null;
            this.form.content = '';
            this.form.sort = 0;
            this.form.remark = '';
          }
        } else {
          // 没有配置数据，初始化表单
          this.agreementId = null;
          this.form.content = '';
          this.form.sort = 0;
          this.form.remark = '';
        }
      } catch (error) {
        console.error('Failed to fetch agreement:', error);
        this.error = 'Failed to load agreement content. Please try again.';
      } finally {
        this.loading = false;
      }
    },
    
    async saveAgreement() {
      if (!this.form.content.trim()) {
        this.$message.warning('Please enter agreement content');
        return;
      }
      
      this.saving = true;
      this.error = '';
      
      try {
        const configData = {
          page: 'profile',
          section: 'agreement',
          configKey: 'content',
          configValue: this.form.content,
          sort: this.form.sort,
          remark: this.form.remark // Comment field
        };
        
        if (this.agreementId) {
          // 更新现有配置
          configData.id = this.agreementId;
          await updateMallPageConfig(configData);
        } else {
          // 创建新配置
          await addMallPageConfig(configData);
        }
        
        this.$message.success('Agreement saved successfully!');
        
        // Reload data to update ID
        await this.fetchAgreement();
        
      } catch (error) {
        console.error('Failed to save agreement:', error);
        this.error = 'Failed to save agreement. Please try again.';
      } finally {
        this.saving = false;
      }
    },
    
    resetForm() {
      this.form.content = '';
      this.form.sort = 0;
      this.form.remark = '';
      this.error = '';
    },
    
    refreshContent() {
      this.fetchAgreement();
    }
  }
};
</script>

<style scoped>
.profile-agreement-editor {
  background: #f5f7fa;
  min-height: 100vh;
  padding: 30px 40px;
}

h2 {
  margin-bottom: 20px;
}

.button-group {
  margin-top: 20px;
  text-align: center;
}

.button-group .el-button {
  margin: 0 10px;
}

.el-card {
  max-width: 800px;
  margin: 0 auto;
}
</style>
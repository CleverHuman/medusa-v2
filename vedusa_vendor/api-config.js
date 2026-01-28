// API Configuration for Vendor Portal
// 配置后端API地址

// API Base URL - 修改为你的后端地址
const API_CONFIG = {
    // 开发环境
    development: {
        baseURL: 'http://localhost:8080',
        timeout: 10000
    },
    // 生产环境
    production: {
        baseURL: 'https://your-production-domain.com',
        timeout: 10000
    }
}

// 自动检测环境
const ENV = window.location.hostname === 'localhost' || window.location.hostname === '127.0.0.1' 
    ? 'development' 
    : 'production'

// 当前配置
const currentConfig = API_CONFIG[ENV]

// API Endpoints
const API_ENDPOINTS = {
    // Vendor Application APIs (Public - No Auth Required)
    submitApplication: '/api/mall/vendor/application/submit',
    getApplicationStatus: '/api/mall/vendor/application/status/',
    getApplicationDetail: '/api/mall/vendor/application/detail/',
    
    // Interview APIs (Public - No Auth Required)
    getInterviewsByApplication: '/api/mall/vendor/interview/application/',
    confirmInterview: '/api/mall/vendor/interview/confirm/'
}

// API Helper Functions
const API = {
    // 获取完整URL
    getURL(endpoint) {
        return currentConfig.baseURL + endpoint
    },
    
    // POST 请求
    async post(endpoint, data) {
        try {
            const response = await fetch(this.getURL(endpoint), {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(data),
                timeout: currentConfig.timeout
            })
            
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`)
            }
            
            const result = await response.json()
            return result
        } catch (error) {
            console.error('API POST Error:', error)
            throw error
        }
    },
    
    // GET 请求
    async get(endpoint) {
        try {
            const response = await fetch(this.getURL(endpoint), {
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json',
                },
                timeout: currentConfig.timeout
            })
            
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`)
            }
            
            const result = await response.json()
            return result
        } catch (error) {
            console.error('API GET Error:', error)
            throw error
        }
    },
    
    // 提交申请
    async submitVendorApplication(applicationData) {
        return await this.post(API_ENDPOINTS.submitApplication, applicationData)
    },
    
    // 查询申请状态
    async getApplicationStatus(applicationId) {
        return await this.get(API_ENDPOINTS.getApplicationStatus + applicationId)
    },
    
    // 查询申请详情
    async getApplicationDetail(applicationId) {
        return await this.get(API_ENDPOINTS.getApplicationDetail + applicationId)
    },
    
    // 根据申请号查询面试信息
    async getInterviewsByApplicationNumber(applicationNumber) {
        return await this.get(API_ENDPOINTS.getInterviewsByApplication + applicationNumber)
    },
    
    // 确认面试
    async confirmInterview(interviewId) {
        return await this.post(API_ENDPOINTS.confirmInterview + interviewId, {})
    }
}

// 导出配置和API对象
window.API_CONFIG = currentConfig
window.API_ENDPOINTS = API_ENDPOINTS
window.API = API

console.log('API Config loaded:', ENV, currentConfig.baseURL)


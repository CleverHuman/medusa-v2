// API Configuration for Vendor Portal
// 配置后端API地址

// API Base URL - 修改为你的后端地址
const API_CONFIG = {
    // 开发环境
    development: {
        baseURL: 'http://localhost:8080',
        timeout: 10000
    },
    // Onion地址环境（使用相对路径）
    onion: {
        baseURL: '',  // 空字符串表示使用相对路径
        timeout: 10000
    },
    // 生产环境
    production: {
        baseURL: 'https://your-production-domain.com',
        timeout: 10000
    }
}

// 自动检测环境
const hostname = window.location.hostname;
let ENV = 'production';

if (hostname === 'localhost' || hostname === '127.0.0.1') {
    ENV = 'development';
} else if (hostname.endsWith('.onion')) {
    // Onion地址使用相对路径
    ENV = 'onion';
}

// 当前配置
const currentConfig = API_CONFIG[ENV]

// API Endpoints
const API_ENDPOINTS = {
    // Vendor Application APIs (Public - No Auth Required)
    submitApplication: '/api/mall/vendor/application/submit',
    getApplicationStatus: '/api/mall/vendor/application/status/',
    getApplicationDetail: '/api/mall/vendor/application/detail/',
    getMyApplications: '/api/mall/vendor/application/my-applications',
    
    // Interview APIs (Auth Required for some endpoints)
    getInterviewsByApplication: '/api/mall/vendor/interview/application/',
    confirmInterview: '/api/mall/vendor/interview/confirm/',
    getAvailableSlots: '/api/mall/vendor/interview/available-slots',
    bookSlot: '/api/mall/vendor/interview/book-slot',
    getMySlots: '/api/mall/vendor/interview/my-slots',

    // Authentication
    memberLogin: '/api/mall/vendor/member/login',
    memberLogout: '/api/mall/vendor/member/logout',
    memberProfile: '/api/mall/vendor/member/profile',
    
    // Product APIs (Auth Required)
    productList: '/api/mall/vendor/product/list',
    productDetail: '/api/mall/vendor/product/',
    createProduct: '/api/mall/vendor/product',
    updateProduct: '/api/mall/vendor/product',
    deleteProduct: '/api/mall/vendor/product/',
    
    // Order APIs (Auth Required)
    orderList: '/api/mall/vendor/order/list',
    orderDetail: '/api/mall/vendor/order/',
    acceptOrder: '/api/mall/vendor/order/',
    rejectOrder: '/api/mall/vendor/order/',
    shipOrder: '/api/mall/vendor/order/',
    
    // Category APIs (Public)
    categoryList: '/api/mall/product/categories'
}

// Resolve member token from global scope or meta tag
function resolveVendorToken() {
    if (typeof window === 'undefined') {
        return null
    }

    if (window.VENDOR_MEMBER_TOKEN === undefined) {
        const meta = document.querySelector('meta[name="vendor-member-token"]')
        if (meta && meta.getAttribute('content')) {
            const value = meta.getAttribute('content').trim()
            window.VENDOR_MEMBER_TOKEN = value ? value : null
        } else {
            window.VENDOR_MEMBER_TOKEN = null
        }
    }

    return window.VENDOR_MEMBER_TOKEN
}

// API Helper Functions
const API = {
    // 获取登录token
    getAuthToken() {
        return resolveVendorToken()
    },

    // 获取 CSRF Token（从 Cookie 中）
    getCsrfToken() {
        // CSRF token 存储在名为 XSRF-TOKEN 的 cookie 中
        const name = 'XSRF-TOKEN';
        const cookies = document.cookie.split(';');
        for (let i = 0; i < cookies.length; i++) {
            let cookie = cookies[i].trim();
            if (cookie.indexOf(name + '=') === 0) {
                return decodeURIComponent(cookie.substring(name.length + 1));
            }
        }
        return null;
    },

    // 构建请求头
    buildHeaders(extraHeaders = {}) {
        const headers = {
            'Content-Type': 'application/json',
            ...extraHeaders
        }

        const token = this.getAuthToken()
        if (token) {
            headers['Authorization'] = `Bearer ${token}`
        }

        // 自动添加 CSRF token（对于 POST/PUT/DELETE 请求）
        const csrfToken = this.getCsrfToken()
        if (csrfToken) {
            headers['X-XSRF-TOKEN'] = csrfToken
        }

        return headers
    },
    
    // 获取完整URL
    // 支持相对路径（适用于onion地址或同域访问）
    getURL(endpoint) {
        // 如果baseURL为空字符串，使用相对路径
        if (!currentConfig.baseURL || currentConfig.baseURL === '') {
            return endpoint;  // 直接返回相对路径，使用当前域名
        }
        
        // 其他情况使用配置的baseURL
        return currentConfig.baseURL + endpoint;
    },
    
    // POST 请求
    async post(endpoint, data, options = {}) {
        try {
            const fetchOptions = {
                method: 'POST',
                headers: this.buildHeaders(options.headers || {}),
                credentials: 'include',
                timeout: currentConfig.timeout,
                ...options
            }

            if (data !== undefined && data !== null) {
                fetchOptions.body = options.rawBody ? data : JSON.stringify(data)
            }

            const response = await fetch(this.getURL(endpoint), fetchOptions)
            
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
    async get(endpoint, options = {}) {
        try {
            const fetchOptions = {
                method: 'GET',
                headers: this.buildHeaders(options.headers || {}),
                credentials: 'include',
                timeout: currentConfig.timeout,
                ...options
            }
            const response = await fetch(this.getURL(endpoint), fetchOptions)
            
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`)
            }
            
            const result = await response.json()
            return result
        } catch (error) {
            if (window.Logger) {
                window.Logger.logApiError('GET', error);
            }
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
    
    // 获取我的所有申请
    async getMyApplications() {
        return await this.get(API_ENDPOINTS.getMyApplications)
    },
    
    // 根据申请号查询面试信息
    async getInterviewsByApplicationNumber(applicationNumber) {
        return await this.get(API_ENDPOINTS.getInterviewsByApplication + applicationNumber)
    },
    
    // 确认面试
    async confirmInterview(interviewId) {
        return await this.post(API_ENDPOINTS.confirmInterview + interviewId, {})
    },

    async logoutMember() {
        return await this.post(API_ENDPOINTS.memberLogout, {})
    }
}

// 导出配置和API对象
window.API_CONFIG = currentConfig
window.API_ENDPOINTS = API_ENDPOINTS
window.API = API

// 使用 Logger 替代 console.log（仅在开发环境显示）
if (window.Logger) {
    window.Logger.log('API Config loaded:', ENV);
    // 不输出 baseURL，避免泄露服务器信息
} else {
    // Logger 未加载时的降级处理（仅在开发环境）
    const isDev = window.location.hostname === 'localhost' || window.location.hostname === '127.0.0.1';
    if (isDev) {
        console.log('API Config loaded:', ENV);
    }
}


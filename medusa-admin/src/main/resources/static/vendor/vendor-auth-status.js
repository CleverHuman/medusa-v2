/**
 * Vendor Authentication Status Component
 * 通用的 Vendor 登录状态显示组件
 * 可在所有 Vendor 页面使用
 */

(function() {
    'use strict';

    // 登录状态管理
    const VendorAuthStatus = {
        currentUser: null,
        isLoggedIn: false,
        callbacks: [],

        // 初始化
        async init(options = {}) {
            this.options = {
                showInNav: options.showInNav !== false, // 默认在导航栏显示
                showFloating: options.showFloating || false, // 是否显示浮动状态
                autoRedirect: options.autoRedirect || false, // 未登录是否自动跳转
                requiredLogin: options.requiredLogin || false, // 是否要求必须登录
                ...options
            };

            await this.checkLoginStatus();
            this.render();
            this.attachEvents();
        },

        // 检查登录状态
        async checkLoginStatus() {
            if (window.Logger) {
                window.Logger.log('VendorAuthStatus: Checking login status...');
                window.Logger.log('Options:', this.options);
            }
            
            try {
                // 使用完整 URL 确保请求正确
                const profileUrl = window.API ? 
                    window.API.getURL('/api/mall/vendor/member/profile') : 
                    '/api/mall/vendor/member/profile';
                
                if (window.Logger) {
                    window.Logger.log('Profile API URL:', profileUrl);
                }
                
                const response = await fetch(profileUrl, {
                    method: 'GET',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    credentials: 'include'
                });
                
                if (window.Logger) {
                    window.Logger.log('Profile API response status:', response.status);
                }
                
                const result = await response.json();
                if (window.Logger) {
                    window.Logger.logResponse('/api/mall/vendor/member/profile', result, false);
                }
                
                // Check if login successful
                if (result.code === 200) {
                    // Handle both response formats:
                    // 1. Standard: {code: 200, data: {username, email, ...}}
                    // 2. Flat: {code: 200, username, email, ...}
                    if (result.data) {
                        // Standard format with data object
                        this.currentUser = result.data;
                    } else if (result.username) {
                        // Flat format - data at root level
                        this.currentUser = {
                            username: result.username,
                            email: result.email,
                            phone: result.phone,
                            memberId: result.memberId,
                            token: result.token
                        };
                    } else {
                        // Code 200 but no user data
                        this.isLoggedIn = false;
                        this.currentUser = null;
                        if (window.Logger) {
                            window.Logger.warn('API returned 200 but no user data');
                        }
                        return this.isLoggedIn;
                    }
                    
                    this.isLoggedIn = true;
                    if (window.Logger) {
                        window.Logger.log('Member logged in:', this.currentUser?.username || 'Unknown');
                    }
                    
                    // Check for active applications (pending, under_review, or approved)
                    await this.checkActiveApplications();
                } else {
                    this.isLoggedIn = false;
                    this.currentUser = null;
                    if (window.Logger) {
                        window.Logger.warn('Member not logged in - API returned:', result.code, result.msg);
                    }
                }
            } catch (error) {
                if (window.Logger) {
                    window.Logger.errorSafe(error, 'checkLoginStatus');
                }
                this.isLoggedIn = false;
                this.currentUser = null;
            }

            if (window.Logger) {
                window.Logger.log('Final login status:', this.isLoggedIn);
            }
            
            // 触发回调
            this.callbacks.forEach(cb => cb(this.isLoggedIn, this.currentUser));

            // 如果要求登录但未登录，处理跳转
            if (this.options.requiredLogin && !this.isLoggedIn && this.options.autoRedirect) {
                if (window.Logger) {
                    window.Logger.warn('Login required but not logged in. Redirecting to login page in 2 seconds...');
                }
                
                // 延迟 2 秒让用户看到日志
                setTimeout(() => {
                    this.redirectToLogin();
                }, 2000);
            }

            return this.isLoggedIn;
        },

        // 检查是否有活跃的申请（pending, under_review, 或 approved）
        async checkActiveApplications() {
            if (!this.isLoggedIn) {
                return;
            }
            
            try {
                const applicationsUrl = window.API ? 
                    window.API.getURL('/api/mall/vendor/application/my-applications') : 
                    '/api/mall/vendor/application/my-applications';
                
                const response = await fetch(applicationsUrl, {
                    method: 'GET',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    credentials: 'include'
                });
                
                if (response.ok) {
                    const result = await response.json();
                    if (result.code === 200 && result.data && Array.isArray(result.data)) {
                        const hasActiveApplication = result.data.some(app => {
                            const status = app.status || 'pending';
                            return status === 'pending' || status === 'under_review' || status === 'approved';
                        });
                        
                        if (hasActiveApplication) {
                            // Hide "Apply for Vendor" link in navigation
                            const applicationLink = document.querySelector('a[href="/mall/static/vendor/application"]');
                            if (applicationLink) {
                                applicationLink.style.display = 'none';
                                if (window.Logger) {
                                    window.Logger.log('Hidden Application link - user has active application');
                                }
                            }
                        }
                    }
                }
            } catch (error) {
                if (window.Logger) {
                    window.Logger.warn('Failed to check active applications:', error);
                }
                // Don't block the flow if this check fails
            }
        },

        // 渲染登录状态
        render() {
            if (this.options.showInNav) {
                this.renderNavStatus();
            }
            if (this.options.showFloating) {
                this.renderFloatingStatus();
            }
        },

        // 渲染导航栏状态
        renderNavStatus() {
            // 查找导航栏
            const nav = document.querySelector('nav');
            if (!nav) return;

            // 查找或创建状态容器
            let statusContainer = document.getElementById('vendorAuthStatusNav');
            if (!statusContainer) {
                statusContainer = document.createElement('div');
                statusContainer.id = 'vendorAuthStatusNav';
                statusContainer.className = 'flex items-center space-x-3';
                
                // 插入到导航栏合适位置
                const navContent = nav.querySelector('.flex.justify-between, .flex.items-center');
                if (navContent) {
                    const rightSection = navContent.querySelector('.flex.items-center.space-x-4, .flex.space-x-4');
                    if (rightSection) {
                        rightSection.prepend(statusContainer);
                    } else {
                        navContent.appendChild(statusContainer);
                    }
                }
            }

            // 渲染内容
            if (this.isLoggedIn && this.currentUser) {
                statusContainer.innerHTML = `
                    <div class="flex items-center space-x-3 px-3 py-1.5 bg-green-50 border border-green-200 rounded-lg">
                        <div class="flex items-center space-x-2">
                            <svg class="w-4 h-4 text-green-600" fill="currentColor" viewBox="0 0 20 20">
                                <path fill-rule="evenodd" d="M10 9a3 3 0 100-6 3 3 0 000 6zm-7 9a7 7 0 1114 0H3z" clip-rule="evenodd"></path>
                            </svg>
                            <span class="text-sm font-medium text-green-800">${this.currentUser.username || 'Member'}</span>
                        </div>
                        <button id="vendorLogoutBtn" class="text-xs font-semibold text-green-700 hover:text-green-900 underline transition-colors">
                            Logout
                        </button>
                    </div>
                `;
            } else {
                statusContainer.innerHTML = `
                    <div class="flex items-center space-x-2">
                        <button id="vendorLoginBtn" class="text-sm font-semibold text-blue-600 hover:text-blue-800 underline transition-colors">
                            Login
                        </button>
                        <span class="text-gray-400">|</span>
                        <button id="vendorRegisterBtn" class="text-sm font-semibold text-gray-600 hover:text-gray-800 transition-colors">
                            Register
                        </button>
                    </div>
                `;
            }

            this.attachNavEvents();
        },

        // 渲染浮动状态（用于特定页面如 application）
        renderFloatingStatus() {
            // 移除旧的浮动状态
            const oldFloating = document.getElementById('vendorAuthStatusFloating');
            if (oldFloating) {
                oldFloating.remove();
            }

            // 创建新的浮动状态
            const floating = document.createElement('div');
            floating.id = 'vendorAuthStatusFloating';
            floating.className = 'fixed top-20 right-4 z-40 px-4 py-3 rounded-lg shadow-lg transition-all';

            if (this.isLoggedIn && this.currentUser) {
                floating.className += ' bg-green-50 border border-green-200';
                floating.innerHTML = `
                    <div class="flex items-center space-x-2">
                        <svg class="w-5 h-5 text-green-600" fill="currentColor" viewBox="0 0 20 20">
                            <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd"></path>
                        </svg>
                        <span class="text-sm font-medium text-green-800">Logged in as: ${this.currentUser.username || 'Member'}</span>
                    </div>
                `;
            } else {
                floating.className += ' bg-yellow-50 border border-yellow-200';
                floating.innerHTML = `
                    <div class="flex items-center justify-between space-x-4">
                        <div class="flex items-center space-x-2">
                            <svg class="w-5 h-5 text-yellow-600" fill="currentColor" viewBox="0 0 20 20">
                                <path fill-rule="evenodd" d="M8.257 3.099c.765-1.36 2.722-1.36 3.486 0l5.58 9.92c.75 1.334-.213 2.98-1.742 2.98H4.42c-1.53 0-2.493-1.646-1.743-2.98l5.58-9.92zM11 13a1 1 0 11-2 0 1 1 0 012 0zm-1-8a1 1 0 00-1 1v3a1 1 0 002 0V6a1 1 0 00-1-1z" clip-rule="evenodd"></path>
                            </svg>
                            <span class="text-sm font-medium text-yellow-800">Please login to continue</span>
                        </div>
                        <button id="vendorFloatingLoginBtn" class="text-sm font-semibold text-yellow-800 hover:text-yellow-900 underline">
                            Login Now
                        </button>
                    </div>
                `;
            }

            document.body.appendChild(floating);
            this.attachFloatingEvents();
        },

        // 附加导航栏事件
        attachNavEvents() {
            const logoutBtn = document.getElementById('vendorLogoutBtn');
            if (logoutBtn) {
                logoutBtn.addEventListener('click', () => this.logout());
            }

            const loginBtn = document.getElementById('vendorLoginBtn');
            if (loginBtn) {
                loginBtn.addEventListener('click', () => this.redirectToLogin());
            }

            const registerBtn = document.getElementById('vendorRegisterBtn');
            if (registerBtn) {
                registerBtn.addEventListener('click', () => {
                    window.location.href = '/mall/static/vendor/register';
                });
            }
        },

        // 附加浮动状态事件
        attachFloatingEvents() {
            const floatingLoginBtn = document.getElementById('vendorFloatingLoginBtn');
            if (floatingLoginBtn) {
                floatingLoginBtn.addEventListener('click', () => this.redirectToLogin());
            }
        },

        // 附加所有事件
        attachEvents() {
            // 这里可以添加全局事件监听
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

        // 登出
        async logout() {
            try {
                // 获取 CSRF token
                const csrfToken = this.getCsrfToken();
                
                // 构建请求头
                const headers = {
                    'Content-Type': 'application/json'
                };
                
                // 如果存在 CSRF token，添加到请求头
                if (csrfToken) {
                    headers['X-XSRF-TOKEN'] = csrfToken;
                }
                
                const response = await fetch('/api/mall/vendor/member/logout', {
                    method: 'POST',
                    headers: headers,
                    credentials: 'include'
                });

                const result = await response.json();
                
                if (result.code === 200) {
                    if (window.Logger) {
                        window.Logger.log('Logged out successfully');
                    }
                    this.isLoggedIn = false;
                    this.currentUser = null;
                    
                    // 显示提示
                    this.showToast('Logged out successfully', 'success');
                    
                    // 延迟跳转到登录页
                    setTimeout(() => {
                        window.location.href = '/mall/static/vendor/login';
                    }, 1000);
                } else {
                    throw new Error(result.msg || 'Logout failed');
                }
            } catch (error) {
                if (window.Logger) {
                    window.Logger.errorSafe(error, 'logout');
                }
                const safeMessage = window.Logger ? window.Logger.getSafeMessage(error) : 'Logout failed. Please try again.';
                this.showToast(safeMessage, 'error');
            }
        },

        // 跳转到登录页
        redirectToLogin() {
            // 保存当前页面路径
            sessionStorage.setItem('vendorPortalRedirectTarget', window.location.pathname);
            
            // 跳转到登录页
            window.location.href = '/mall/static/vendor/login';
        },

        // 显示提示
        showToast(message, type = 'info') {
            // 创建 toast 元素
            const toast = document.createElement('div');
            toast.className = `fixed top-4 right-4 z-50 px-6 py-3 rounded-lg shadow-lg transition-all transform translate-x-0 ${
                type === 'success' ? 'bg-green-500 text-white' :
                type === 'error' ? 'bg-red-500 text-white' :
                type === 'warning' ? 'bg-yellow-500 text-white' :
                'bg-blue-500 text-white'
            }`;
            toast.textContent = message;

            document.body.appendChild(toast);

            // 自动移除
            setTimeout(() => {
                toast.classList.add('opacity-0', 'translate-x-full');
                setTimeout(() => toast.remove(), 300);
            }, 3000);
        },

        // 添加状态变化回调
        onStatusChange(callback) {
            this.callbacks.push(callback);
        },

        // 获取当前用户
        getCurrentUser() {
            return this.currentUser;
        },

        // 检查是否已登录
        checkLoggedIn() {
            return this.isLoggedIn;
        }
    };

    // 导出到全局
    window.VendorAuthStatus = VendorAuthStatus;

    // 自动初始化（如果页面配置了自动初始化）
    document.addEventListener('DOMContentLoaded', function() {
        const autoInit = document.querySelector('[data-vendor-auth-auto-init]');
        if (autoInit) {
            const options = {
                showInNav: autoInit.dataset.showInNav !== 'false',
                showFloating: autoInit.dataset.showFloating === 'true',
                autoRedirect: autoInit.dataset.autoRedirect === 'true',
                requiredLogin: autoInit.dataset.requiredLogin === 'true'
            };
            VendorAuthStatus.init(options);
        }
    });

})();


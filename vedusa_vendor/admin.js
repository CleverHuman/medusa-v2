// 模拟申请数据
const mockApplications = [
    {
        id: 'VENDOR-284756-XZAB12',
        vendorName: '科技电子有限公司',
        location: '华东地区',
        productCategories: ['电子产品', '配件'],
        status: 'approved',
        applicationDate: '2024-01-15',
        primaryContacts: 'Telegram: @techcompany',
        secondaryContacts: 'Email: contact@techcompany.com',
        reviewScore: 92,
        reviewer: '张审核员'
    },
    {
        id: 'VENDOR-392847-MNCD34',
        vendorName: '时尚服饰集团',
        location: '华南地区',
        productCategories: ['服装配饰'],
        status: 'reviewing',
        applicationDate: '2024-01-17',
        primaryContacts: 'Signal: +86-138-0000-0000',
        secondaryContacts: 'Telegram: @fashiongroup',
        reviewScore: null,
        reviewer: '李审核员'
    },
    {
        id: 'VENDOR-156789-EFGH56',
        vendorName: '美妆护肤品公司',
        location: '华北地区',
        productCategories: ['美妆护肤'],
        status: 'interview_required',
        applicationDate: '2024-01-13',
        primaryContacts: 'Email: beauty@company.com',
        secondaryContacts: 'Telegram: @beautycare',
        reviewScore: 85,
        reviewer: '王审核员'
    },
    {
        id: 'VENDOR-445678-IJKL78',
        vendorName: '家居用品供应商',
        location: '西南地区',
        productCategories: ['家居用品'],
        status: 'rejected',
        applicationDate: '2024-01-10',
        primaryContacts: 'Jabber: home@supplier.com',
        secondaryContacts: 'Email: info@homesupplier.com',
        reviewScore: 45,
        reviewer: '赵审核员'
    },
    {
        id: 'VENDOR-567890-MNOP90',
        vendorName: '食品饮料公司',
        location: '华中地区',
        productCategories: ['食品饮料'],
        status: 'pending',
        applicationDate: '2024-01-16',
        primaryContacts: 'Telegram: @foodcompany',
        secondaryContacts: 'Signal: +86-139-0000-0000',
        reviewScore: null,
        reviewer: '待定'
    },
    {
        id: 'VENDOR-678901-QRST12',
        vendorName: '运动装备公司',
        location: '东北地区',
        productCategories: ['运动户外'],
        status: 'additional_info_required',
        applicationDate: '2024-01-12',
        primaryContacts: 'Email: sports@equipment.com',
        secondaryContacts: 'Telegram: @sportsequipment',
        reviewScore: 65,
        reviewer: '孙审核员'
    }
];

let currentFilter = 'all';
let currentApplication = null;

// 页面加载完成后初始化
document.addEventListener('DOMContentLoaded', function() {
    initializePage();
    renderApplications();
    initializeAnimations();
});

// 初始化页面
function initializePage() {
    updateStatistics();
    setupEventListeners();
}

// 设置事件监听器
function setupEventListeners() {
    // 搜索功能
    const searchInput = document.querySelector('input[placeholder="搜索申请..."]');
    if (searchInput) {
        searchInput.addEventListener('input', function() {
            filterApplicationsBySearch(this.value);
        });
    }

    // 模态框关闭事件
    const modal = document.getElementById('applicationModal');
    if (modal) {
        modal.addEventListener('click', function(e) {
            if (e.target === modal) {
                closeModal();
            }
        });
    }
}

// 初始化动画
function initializeAnimations() {
    // 统计卡片动画
    anime({
        targets: '.stat-card',
        translateY: [50, 0],
        opacity: [0, 1],
        duration: 600,
        easing: 'easeOutQuad',
        delay: anime.stagger(100)
    });

    // 导航项目动画
    anime({
        targets: '.nav-item',
        translateX: [-30, 0],
        opacity: [0, 1],
        duration: 500,
        easing: 'easeOutQuad',
        delay: anime.stagger(50)
    });
}

// 更新统计数据
function updateStatistics() {
    const stats = calculateStatistics();
    
    document.getElementById('todayApplications').textContent = stats.todayApplications;
    document.getElementById('pendingApplications').textContent = stats.pendingApplications;
    document.getElementById('approvalRate').textContent = stats.approvalRate + '%';
    document.getElementById('activeVendors').textContent = stats.activeVendors;

    // 添加数字动画
    anime({
        targets: '#todayApplications, #pendingApplications, #activeVendors',
        innerHTML: [0, function(el) { return el.textContent; }],
        duration: 2000,
        easing: 'easeOutQuad',
        round: 1
    });
}

// 计算统计数据
function calculateStatistics() {
    const today = new Date().toISOString().split('T')[0];
    const todayApplications = mockApplications.filter(app => app.applicationDate === today).length;
    const pendingApplications = mockApplications.filter(app => app.status === 'pending' || app.status === 'reviewing').length;
    const approvedApplications = mockApplications.filter(app => app.status === 'approved').length;
    const totalApplications = mockApplications.length;
    const approvalRate = Math.round((approvedApplications / totalApplications) * 100);
    
    return {
        todayApplications: todayApplications,
        pendingApplications: pendingApplications,
        approvalRate: approvalRate,
        activeVendors: 1256
    };
}

// 渲染申请列表
function renderApplications() {
    const tbody = document.getElementById('applicationsTable');
    if (!tbody) return;

    const filteredApplications = filterApplicationsByStatus(currentFilter);
    
    tbody.innerHTML = '';

    filteredApplications.forEach((application, index) => {
        const row = document.createElement('tr');
        row.className = 'application-row';
        
        const statusConfig = getStatusConfig(application.status);
        
        row.innerHTML = `
            <td class="px-6 py-4 whitespace-nowrap">
                <div class="text-sm font-medium text-gray-900">${application.id}</div>
            </td>
            <td class="px-6 py-4 whitespace-nowrap">
                <div class="text-sm font-medium text-gray-900">${application.vendorName}</div>
                <div class="text-sm text-gray-500">${application.location}</div>
            </td>
            <td class="px-6 py-4 whitespace-nowrap">
                <div class="text-sm text-gray-900">${application.productCategories.join(', ')}</div>
            </td>
            <td class="px-6 py-4 whitespace-nowrap">
                <span class="px-3 py-1 rounded-full text-xs font-semibold ${statusConfig.class}">
                    ${statusConfig.text}
                </span>
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                ${application.applicationDate}
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-sm font-medium">
                <button onclick="viewApplicationDetails('${application.id}')" 
                        class="text-blue-600 hover:text-blue-900 mr-3 transition-colors">
                    查看详情
                </button>
                ${application.status === 'pending' || application.status === 'reviewing' ? 
                    `<button onclick="quickApprove('${application.id}')" 
                            class="text-green-600 hover:text-green-900 mr-3 transition-colors">
                        快速批准
                    </button>` : ''
                }
            </td>
        `;
        
        tbody.appendChild(row);
    });

    // 添加行动画
    anime({
        targets: '.application-row',
        translateY: [30, 0],
        opacity: [0, 1],
        duration: 400,
        easing: 'easeOutQuad',
        delay: anime.stagger(50)
    });
}

// 根据状态筛选申请
function filterApplicationsByStatus(status) {
    if (status === 'all') {
        return mockApplications;
    }
    
    return mockApplications.filter(application => {
        if (status === 'interview') {
            return application.status === 'interview_required';
        }
        return application.status === status;
    });
}

// 根据搜索关键词筛选申请
function filterApplicationsBySearch(keyword) {
    const tbody = document.getElementById('applicationsTable');
    if (!tbody) return;

    const filteredApplications = mockApplications.filter(application => {
        return application.id.toLowerCase().includes(keyword.toLowerCase()) ||
               application.vendorName.toLowerCase().includes(keyword.toLowerCase()) ||
               application.location.toLowerCase().includes(keyword.toLowerCase()) ||
               application.productCategories.some(cat => cat.toLowerCase().includes(keyword.toLowerCase()));
    });

    tbody.innerHTML = '';

    filteredApplications.forEach(application => {
        const row = document.createElement('tr');
        row.className = 'application-row';
        
        const statusConfig = getStatusConfig(application.status);
        
        row.innerHTML = `
            <td class="px-6 py-4 whitespace-nowrap">
                <div class="text-sm font-medium text-gray-900">${application.id}</div>
            </td>
            <td class="px-6 py-4 whitespace-nowrap">
                <div class="text-sm font-medium text-gray-900">${application.vendorName}</div>
                <div class="text-sm text-gray-500">${application.location}</div>
            </td>
            <td class="px-6 py-4 whitespace-nowrap">
                <div class="text-sm text-gray-900">${application.productCategories.join(', ')}</div>
            </td>
            <td class="px-6 py-4 whitespace-nowrap">
                <span class="px-3 py-1 rounded-full text-xs font-semibold ${statusConfig.class}">
                    ${statusConfig.text}
                </span>
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                ${application.applicationDate}
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-sm font-medium">
                <button onclick="viewApplicationDetails('${application.id}')" 
                        class="text-blue-600 hover:text-blue-900 mr-3 transition-colors">
                    查看详情
                </button>
                ${application.status === 'pending' || application.status === 'reviewing' ? 
                    `<button onclick="quickApprove('${application.id}')" 
                            class="text-green-600 hover:text-green-900 mr-3 transition-colors">
                        快速批准
                    </button>` : ''
                }
            </td>
        `;
        
        tbody.appendChild(row);
    });
}

// 获取状态配置
function getStatusConfig(status) {
    const statusConfig = {
        pending: { text: '待审核', class: 'bg-gray-100 text-gray-800' },
        received: { text: '已接收', class: 'bg-blue-100 text-blue-800' },
        validating: { text: '验证中', class: 'bg-yellow-100 text-yellow-800' },
        reviewing: { text: '审核中', class: 'bg-blue-100 text-blue-800' },
        interview_required: { text: '需要面试', class: 'bg-purple-100 text-purple-800' },
        additional_info_required: { text: '补充材料', class: 'bg-amber-100 text-amber-800' },
        approved: { text: '已批准', class: 'bg-green-100 text-green-800' },
        rejected: { text: '已拒绝', class: 'bg-red-100 text-red-800' }
    };

    return statusConfig[status] || statusConfig.pending;
}

// 筛选申请
function filterApplications(status) {
    currentFilter = status;
    
    // 更新筛选按钮状态
    document.querySelectorAll('.filter-chip').forEach(chip => {
        chip.classList.remove('active');
    });
    
    event.target.classList.add('active');
    
    // 重新渲染申请列表
    renderApplications();
}

// 查看申请详情
function viewApplicationDetails(applicationId) {
    const application = mockApplications.find(app => app.id === applicationId);
    if (!application) return;

    currentApplication = application;

    // 填充模态框内容
    document.getElementById('modalApplicationId').textContent = application.id;
    document.getElementById('modalVendorName').textContent = application.vendorName;
    document.getElementById('modalLocation').textContent = application.location;
    document.getElementById('modalProductCategories').textContent = application.productCategories.join(', ');
    document.getElementById('modalApplicationDate').textContent = application.applicationDate;
    document.getElementById('modalPrimaryContacts').textContent = application.primaryContacts;
    document.getElementById('modalSecondaryContacts').textContent = application.secondaryContacts;

    // 显示模态框
    const modal = document.getElementById('applicationModal');
    modal.classList.remove('hidden');
    modal.classList.add('flex');

    // 添加显示动画
    anime({
        targets: modal.querySelector('.bg-white'),
        scale: [0.8, 1],
        opacity: [0, 1],
        duration: 400,
        easing: 'easeOutBack'
    });
}

// 关闭模态框
function closeModal() {
    const modal = document.getElementById('applicationModal');
    
    anime({
        targets: modal.querySelector('.bg-white'),
        scale: [1, 0.8],
        opacity: [1, 0],
        duration: 300,
        easing: 'easeOutQuad',
        complete: function() {
            modal.classList.add('hidden');
            modal.classList.remove('flex');
            
            // 清空表单
            document.getElementById('reviewComments').value = '';
            document.getElementById('reviewScore').value = '';
        }
    });
}

// 快速批准申请
function quickApprove(applicationId) {
    const application = mockApplications.find(app => app.id === applicationId);
    if (!application) return;

    // 更新申请状态
    application.status = 'approved';
    application.reviewScore = 85;
    application.reviewer = '当前管理员';

    // 显示成功消息
    showToast(`申请 ${applicationId} 已快速批准`, 'success');

    // 重新渲染申请列表
    renderApplications();
    
    // 更新统计数据
    updateStatistics();
}

// 批准申请
function approveApplication() {
    if (!currentApplication) return;

    const comments = document.getElementById('reviewComments').value;
    const score = document.getElementById('reviewScore').value;

    if (!score) {
        showToast('请选择审核评分', 'error');
        return;
    }

    // 更新申请状态
    currentApplication.status = 'approved';
    currentApplication.reviewScore = parseInt(score);
    currentApplication.reviewer = '当前管理员';

    // 显示成功消息
    showToast(`申请 ${currentApplication.id} 已批准`, 'success');

    // 关闭模态框
    closeModal();

    // 重新渲染申请列表
    renderApplications();
    
    // 更新统计数据
    updateStatistics();
}

// 拒绝申请
function rejectApplication() {
    if (!currentApplication) return;

    const comments = document.getElementById('reviewComments').value;

    if (!comments.trim()) {
        showToast('请输入拒绝原因', 'error');
        return;
    }

    // 更新申请状态
    currentApplication.status = 'rejected';
    currentApplication.reviewer = '当前管理员';

    // 显示成功消息
    showToast(`申请 ${currentApplication.id} 已拒绝`, 'success');

    // 关闭模态框
    closeModal();

    // 重新渲染申请列表
    renderApplications();
    
    // 更新统计数据
    updateStatistics();
}

// 需要面试
function requireInterview() {
    if (!currentApplication) return;

    const comments = document.getElementById('reviewComments').value;

    if (!comments.trim()) {
        showToast('请输入面试原因', 'error');
        return;
    }

    // 更新申请状态
    currentApplication.status = 'interview_required';
    currentApplication.reviewer = '当前管理员';

    // 显示成功消息
    showToast(`申请 ${currentApplication.id} 已标记为需要面试`, 'success');

    // 关闭模态框
    closeModal();

    // 重新渲染申请列表
    renderApplications();
    
    // 更新统计数据
    updateStatistics();
}

// 刷新申请列表
function refreshApplications() {
    renderApplications();
    updateStatistics();
    showToast('申请列表已刷新', 'success');
}

// 导出申请数据
function exportApplications() {
    showToast('正在导出申请数据...', 'info');
    
    // 模拟导出过程
    setTimeout(() => {
        showToast('申请数据导出完成', 'success');
    }, 2000);
}

// 退出登录
function logout() {
    showToast('正在退出系统...', 'info');
    
    setTimeout(() => {
        window.location.href = 'index.html';
    }, 1000);
}

// 显示提示消息
function showToast(message, type = 'info') {
    const toast = document.createElement('div');
    toast.className = `fixed top-20 right-4 px-6 py-3 rounded-lg text-white z-50 ${
        type === 'success' ? 'bg-green-500' : 
        type === 'error' ? 'bg-red-500' : 'bg-blue-500'
    }`;
    toast.textContent = message;
    
    document.body.appendChild(toast);
    
    anime({
        targets: toast,
        translateX: [300, 0],
        opacity: [0, 1],
        duration: 300,
        easing: 'easeOutQuad',
        complete: function() {
            setTimeout(() => {
                anime({
                    targets: toast,
                    translateX: [0, 300],
                    opacity: [1, 0],
                    duration: 300,
                    easing: 'easeOutQuad',
                    complete: function() {
                        document.body.removeChild(toast);
                    }
                });
            }, 3000);
        }
    });
}
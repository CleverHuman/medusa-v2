const VENDOR_PORTAL_REDIRECT_KEY = 'vendorPortalRedirectTarget';

// ========= Authentication Utilities =========

function getMemberToken() {
    const meta = document.querySelector('meta[name="vendor-member-token"]');
    return meta ? meta.getAttribute('content') || '' : '';
}

function enforceApplicationAuth() {
    const token = getMemberToken();
    return !!token;
}

function renderAuthUI() {
    const token = getMemberToken();
    const welcomeContainer = document.getElementById('welcomeContainer');
    const welcomeMessage = document.getElementById('welcomeMessage');
    const logoutBtn = document.getElementById('logoutBtn');
    const unauthActions = document.getElementById('unauthActions');
    const searchSection = document.getElementById('searchSection');
    const newApplicationBtn = document.getElementById('newApplicationBtn');

    if (token) {
        if (welcomeContainer) welcomeContainer.classList.remove('hidden');
        if (unauthActions) unauthActions.classList.add('hidden');
        if (searchSection) searchSection.classList.remove('hidden');
        if (welcomeMessage) welcomeMessage.textContent = 'Welcome back';
        if (logoutBtn) {
            logoutBtn.onclick = async () => {
                try {
                    await API.logoutMember();
                } catch (e) {
                    if (window.Logger) {
                        window.Logger.warn('Logout failed', e);
                    }
                } finally {
                    window.location.href = '/mall/static/vendor';
                }
            };
        }
        if (newApplicationBtn) {
            newApplicationBtn.onclick = () => {
                window.location.href = '/mall/static/vendor/application';
            };
        }
    } else {
        if (welcomeContainer) welcomeContainer.classList.add('hidden');
        if (unauthActions) unauthActions.classList.remove('hidden');
        if (searchSection) searchSection.classList.add('hidden');
        if (logoutBtn) logoutBtn.onclick = null;

        const loginBtn = document.getElementById('loginBtn');
        if (loginBtn) {
            loginBtn.onclick = () => {
                window.location.href = '/mall/static/vendor/login?redirect=' + encodeURIComponent(window.location.href);
            };
        }
        if (newApplicationBtn) {
            newApplicationBtn.onclick = () => {
                window.location.href = '/mall/static/vendor/login?redirect=' + encodeURIComponent('/mall/static/vendor/application');
            };
        }
    }
}

// Fetch interviews for an application
async function fetchInterviews(applicationNumber) {
    try {
        const response = await API.getInterviewsByApplicationNumber(applicationNumber);
        if (response.code === 200 && response.data) {
            return response.data;
        }
        return [];
    } catch (error) {
        console.error('Failed to fetch interviews:', error);
        return [];
    }
}

// Mock application data
const mockApplications = {
    'VENDOR-284756-XZAB12': {
        status: 'approved',
        vendorName: 'Tech Electronics Ltd',
        location: 'New South Wales',
        productCategories: ['Electronics', 'Accessories'],
        applicationDate: '2024-01-15',
        queuePosition: 0,
        estimatedTime: 'Completed',
        lastUpdate: '2024-01-17 14:30',
        timeline: [
            { status: 'submitted', title: 'Application Submitted', description: 'Application has been successfully submitted', time: '2024-01-15 09:15', completed: true },
            { status: 'received', title: 'Application Received', description: 'System has received the application and started processing', time: '2024-01-15 09:16', completed: true },
            { status: 'validating', title: 'Information Validation', description: 'Validating application information completeness', time: '2024-01-15 10:00', completed: true },
            { status: 'reviewing', title: 'Under Review', description: 'Reviewer is evaluating the application', time: '2024-01-16 14:20', completed: true },
            { status: 'approved', title: 'Application Approved', description: 'Application has been approved. Welcome to our vendor network!', time: '2024-01-17 14:30', completed: true }
        ],
        reviewDetails: {
            score: 92,
            strengths: ['Excellent product quality', 'Rich market experience', 'Complete qualifications'],
            weaknesses: ['Slightly higher prices'],
            recommendation: 'Highly recommended, priority consideration for collaboration'
        }
    },
    'VENDOR-392847-MNCD34': {
        status: 'reviewing',
        vendorName: 'Fashion Apparel Group',
        location: 'Victoria',
        productCategories: ['Fashion Accessories'],
        applicationDate: '2024-01-17',
        queuePosition: 3,
        estimatedTime: 'Estimated completion in 2 days',
        lastUpdate: '2024-01-18 16:45',
        timeline: [
            { status: 'submitted', title: 'Application Submitted', description: 'Application has been successfully submitted', time: '2024-01-17 11:30', completed: true },
            { status: 'received', title: 'Application Received', description: 'System has received the application and started processing', time: '2024-01-17 11:31', completed: true },
            { status: 'validating', title: 'Information Validation', description: 'Validating application information completeness', time: '2024-01-17 14:00', completed: true },
            { status: 'reviewing', title: 'Under Review', description: 'Reviewer is evaluating the application', time: '2024-01-18 09:00', completed: true },
            { status: 'pending', title: 'Awaiting Final Review', description: 'Waiting for final review decision', time: '', completed: false }
        ],
        reviewDetails: {
            score: 78,
            strengths: ['Innovative design', 'Reasonable pricing'],
            weaknesses: ['Limited stock volume', 'Low brand awareness'],
            recommendation: 'Can consider collaboration, suggest starting with a small-scale pilot'
        }
    },
    'VENDOR-156789-EFGH56': {
        status: 'interview_required',
        vendorName: 'Beauty & Skincare Company',
        location: 'Queensland',
        productCategories: ['Beauty & Personal Care'],
        applicationDate: '2024-01-13',
        queuePosition: 0,
        estimatedTime: 'Waiting for interview arrangement',
        lastUpdate: '2024-01-16 10:20',
        timeline: [
            { status: 'submitted', title: 'Application Submitted', description: 'Application has been successfully submitted', time: '2024-01-13 15:45', completed: true },
            { status: 'received', title: 'Application Received', description: 'System has received the application and started processing', time: '2024-01-13 15:46', completed: true },
            { status: 'validating', title: 'Information Validation', description: 'Validating application information completeness', time: '2024-01-13 17:00', completed: true },
            { status: 'reviewing', title: 'Under Review', description: 'Reviewer is evaluating the application', time: '2024-01-15 09:30', completed: true },
            { status: 'interview_required', title: 'Interview Required', description: 'Face-to-face interview needed for further evaluation', time: '2024-01-16 10:20', completed: true }
        ],
        reviewDetails: {
            score: 85,
            strengths: ['Excellent product quality', 'Accurate market positioning'],
            weaknesses: ['Physical store verification needed'],
            recommendation: 'Face-to-face interview required to confirm physical business operations'
        }
    },
    'VENDOR-445678-IJKL78': {
        status: 'rejected',
        vendorName: 'Home Goods Supplier',
        location: 'South Australia',
        productCategories: ['Home & Garden'],
        applicationDate: '2024-01-10',
        queuePosition: 0,
        estimatedTime: 'Rejected',
        lastUpdate: '2024-01-12 11:15',
        timeline: [
            { status: 'submitted', title: 'Application Submitted', description: 'Application has been successfully submitted', time: '2024-01-10 08:20', completed: true },
            { status: 'received', title: 'Application Received', description: 'System has received the application and started processing', time: '2024-01-10 08:21', completed: true },
            { status: 'validating', title: 'Information Validation', description: 'Validating application information completeness', time: '2024-01-10 11:00', completed: true },
            { status: 'reviewing', title: 'Under Review', description: 'Reviewer is evaluating the application', time: '2024-01-11 14:30', completed: true },
            { status: 'rejected', title: 'Application Rejected', description: 'Application failed review. Reason: Qualification mismatch', time: '2024-01-12 11:15', completed: true }
        ],
        reviewDetails: {
            score: 45,
            strengths: [],
            weaknesses: ['Incomplete qualification documents', 'Product quality concerns', 'Lack of relevant experience'],
            recommendation: 'Not recommended for collaboration. Please improve qualifications and reapply'
        }
    },
    'VENDOR-567890-MNOP90': {
        status: 'pending',
        vendorName: 'Food & Beverage Company',
        location: 'Western Australia',
        productCategories: ['Food & Beverages'],
        applicationDate: '2024-01-16',
        queuePosition: 15,
        estimatedTime: 'Review expected to start in 3-5 days',
        lastUpdate: '2024-01-16 16:00',
        timeline: [
            { status: 'submitted', title: 'Application Submitted', description: 'Application has been successfully submitted', time: '2024-01-16 16:00', completed: true },
            { status: 'received', title: 'Application Received', description: 'System has received the application and started processing', time: '2024-01-16 16:01', completed: true },
            { status: 'pending', title: 'Awaiting Review', description: 'Application is queued and waiting for review', time: '', completed: false }
        ],
        reviewDetails: null
    },
    'VENDOR-678901-QRST12': {
        status: 'additional_info_required',
        vendorName: 'Sports Equipment Company',
        location: 'Tasmania',
        productCategories: ['Sports & Outdoors'],
        applicationDate: '2024-01-12',
        queuePosition: 0,
        estimatedTime: 'Waiting for additional documents',
        lastUpdate: '2024-01-16 09:45',
        timeline: [
            { status: 'submitted', title: 'Application Submitted', description: 'Application has been successfully submitted', time: '2024-01-12 13:30', completed: true },
            { status: 'received', title: 'Application Received', description: 'System has received the application and started processing', time: '2024-01-12 13:31', completed: true },
            { status: 'validating', title: 'Information Validation', description: 'Validating application information completeness', time: '2024-01-12 16:00', completed: true },
            { status: 'additional_info_required', title: 'Additional Documents Required', description: 'Additional qualification documents needed', time: '2024-01-16 09:45', completed: true }
        ],
        reviewDetails: {
            score: 65,
            strengths: ['Wide product range'],
            weaknesses: ['Missing quality certification', 'Business license expired'],
            recommendation: 'Please provide quality certification documents and update business license'
        }
    }
};

// Initialize after page load
document.addEventListener('DOMContentLoaded', function() {
    renderAuthUI();

    if (enforceApplicationAuth()) {
        initializePage();
        setupEventListeners();
        
        // Check URL parameters first
        const urlParams = new URLSearchParams(window.location.search);
        const applicationId = urlParams.get('id');
        
        if (applicationId) {
            // If URL has application ID, load that specific application
            loadApplicationDetail(applicationId);
            loadMyApplications(); // Also load history
        } else {
            // Load applications and auto-display the first one
            loadMyApplicationsAndShowFirst();
        }
    } else {
        showToast('Please login to submit or track your Co-op Member application.', 'info');
    }
});

// Initialize page
function initializePage() {
    // Add page load animation
    anime({
        targets: '.status-card',
        translateY: [50, 0],
        opacity: [0, 1],
        duration: 600,
        easing: 'easeOutQuad',
        delay: anime.stagger(100)
    });
}

// Setup event listeners
function setupEventListeners() {
    // Enter key search
    const searchInput = document.getElementById('applicationId');
    if (searchInput) {
        searchInput.addEventListener('keypress', function(e) {
            if (e.key === 'Enter') {
                searchApplication();
            }
        });
    }
}

// Load my applications and show the first one automatically
async function loadMyApplicationsAndShowFirst() {
    try {
        const response = await API.getMyApplications();
        
        if (response.code === 200 && response.data && Array.isArray(response.data) && response.data.length > 0) {
            const applications = response.data;
            
            // Render the application history list
            renderApplicationHistory(applications);
            
            // Auto-load the first application's details
            const firstApp = applications[0];
            const applicationId = firstApp.applicationId;
            if (applicationId) {
                loadApplicationDetail(applicationId);
            }
        } else {
            // No applications - just load the history section
            loadMyApplications();
        }
    } catch (error) {
        console.error('Failed to load applications:', error);
        loadMyApplications();
    }
}

// Search application
async function searchApplication() {
    const applicationId = document.getElementById('applicationId').value.trim();
    
    if (!applicationId) {
        showToast('Please enter Application ID', 'error');
        return;
    }

    if (!enforceApplicationAuth()) {
        showToast('Please login to track your application.', 'error');
        return;
    }

    // Validate application ID format (relaxed validation)
    if (!applicationId || applicationId.length < 5) {
        showToast('Please enter a valid Application ID', 'error');
        return;
    }

    // Show loading animation
    showLoading();

    try {
        // Call real API to get application status
        const response = await API.getApplicationStatus(applicationId);
        
        if (response.code === 200 && response.data) {
            // Success - convert API data to display format
            const application = convertApiDataToDisplayFormat(response.data);
            displayApplicationDetails(applicationId, application);
            hideLoading();
        } else {
            // API returned error or no data
            hideLoading();
            showToast(response.msg || 'Application not found. Please check the Application ID', 'error');
        }
    } catch (error) {
        console.error('Failed to fetch application:', error);
        hideLoading();
        
        // Fallback to mock data for demo purposes
        const mockApplication = mockApplications[applicationId];
        if (mockApplication) {
            console.log('Using mock data for demonstration');
            displayApplicationDetails(applicationId, mockApplication);
            showToast('Demo data loaded (API unavailable)', 'warning');
        } else {
            showToast('Application not found. Please check the Application ID or ensure backend is running', 'error');
        }
    }
}

// Convert API response data to display format
function convertApiDataToDisplayFormat(apiData) {
    // Parse product categories if it's a JSON string
    let productCategories = [];
    try {
        if (apiData.productCategories) {
            productCategories = typeof apiData.productCategories === 'string' 
                ? JSON.parse(apiData.productCategories) 
                : apiData.productCategories;
        }
    } catch (e) {
        productCategories = [apiData.productCategories];
    }
    
    // Build display format
    return {
        id: apiData.id,
        applicationId: apiData.applicationId,
        status: apiData.status || 'pending',
        vendorName: apiData.vendorName,
        location: apiData.location,
        productCategories: productCategories,
        applicationDate: formatDate(apiData.createTime),
        queuePosition: 0, // Not available from API
        estimatedTime: getEstimatedTime(apiData.status, apiData.reviewProgress),
        lastUpdate: formatDateTime(apiData.updateTime || apiData.createTime),
        timeline: generateTimelineFromStatus(apiData),
        reviewDetails: apiData.reviewNotes ? {
            score: apiData.reviewProgress || 0,
            strengths: [],
            weaknesses: [],
            recommendation: apiData.reviewNotes
        } : null,
        // Bond-related fields
        bondLevel: apiData.bondLevel,
        bondOrderId: apiData.bondOrderId,
        bondPaidTime: apiData.bondPaidTime,
        // Wallet fields
        walletBtcProvided: apiData.walletBtcProvided,
        walletXmrProvided: apiData.walletXmrProvided,
        walletUsdtProvided: apiData.walletUsdtProvided,
        walletVerifiedTime: apiData.walletVerifiedTime,
        walletVerifiedBy: apiData.walletVerifiedBy
    };
}

// Generate timeline from API status data
function generateTimelineFromStatus(apiData) {
    const timeline = [];
    const status = apiData.status;
    
    // Always show submitted
    timeline.push({
        status: 'submitted',
        title: 'Application Submitted',
        description: 'Application has been successfully submitted',
        time: formatDateTime(apiData.createTime),
        completed: true
    });
    
    // Received
    if (status !== 'pending') {
        timeline.push({
            status: 'received',
            title: 'Application Received',
            description: 'System has received the application and started processing',
            time: formatDateTime(apiData.createTime),
            completed: true
        });
    }
    
    // Under review
    if (['under_review', 'interview_required', 'approved', 'rejected'].includes(status)) {
        timeline.push({
            status: 'reviewing',
            title: 'Under Review',
            description: apiData.reviewStage || 'Reviewer is evaluating the application',
            time: formatDateTime(apiData.updateTime),
            completed: true
        });
    }
    
    // Interview required
    if (status === 'interview_required') {
        timeline.push({
            status: 'interview_required',
            title: 'Interview Required',
            description: 'Face-to-face interview needed for further evaluation',
            time: formatDateTime(apiData.reviewedTime || apiData.updateTime),
            completed: true
        });
    }
    
    // Final status
    if (status === 'approved') {
        timeline.push({
            status: 'approved',
            title: 'Application Approved',
            description: 'Application has been approved. Welcome to our vendor network!',
            time: formatDateTime(apiData.reviewedTime),
            completed: true
        });
    } else if (status === 'rejected') {
        timeline.push({
            status: 'rejected',
            title: 'Application Rejected',
            description: apiData.reviewNotes || 'Application failed review',
            time: formatDateTime(apiData.reviewedTime),
            completed: true
        });
    } else if (status === 'pending') {
        timeline.push({
            status: 'pending',
            title: 'Awaiting Review',
            description: 'Application is queued and waiting for review',
            time: '',
            completed: false
        });
    }
    
    return timeline;
}

// Format date (YYYY-MM-DD)
function formatDate(dateString) {
    if (!dateString) return '';
    const date = new Date(dateString);
    return date.toISOString().split('T')[0];
}

// Format datetime (YYYY-MM-DD HH:MM)
function formatDateTime(dateString) {
    if (!dateString) return '';
    const date = new Date(dateString);
    return date.toISOString().replace('T', ' ').substring(0, 16);
}

// Get estimated time based on status
function getEstimatedTime(status, progress) {
    const timeMap = {
        'pending': 'Review expected to start in 3-5 days',
        'under_review': progress > 50 ? 'Estimated completion in 1-2 days' : 'Under review',
        'interview_required': 'Waiting for interview arrangement',
        'approved': 'Completed',
        'rejected': 'Rejected'
    };
    return timeMap[status] || 'Processing';
}

// Validate application ID format
function isValidApplicationId(id) {
    const pattern = /^VENDOR-\d{6}-[A-Z0-9]{6}$/;
    return pattern.test(id);
}

// Show loading animation
function showLoading() {
    const searchBtn = document.querySelector('button[onclick="searchApplication()"]');
    if (searchBtn) {
        searchBtn.innerHTML = '<div class="animate-spin rounded-full h-5 w-5 border-b-2 border-white"></div>';
        searchBtn.disabled = true;
    }
}

// Hide loading animation
function hideLoading() {
    const searchBtn = document.querySelector('button[onclick="searchApplication()"]');
    if (searchBtn) {
        searchBtn.innerHTML = 'Search Application';
        searchBtn.disabled = false;
    }
}

// Display application details
function displayApplicationDetails(applicationId, application) {
    // Hide sample applications list
    const sampleApplications = document.getElementById('sampleApplications');
    const applicationDetails = document.getElementById('applicationDetails');
    
    if (sampleApplications) {
        anime({
            targets: sampleApplications,
            opacity: [1, 0],
            translateY: [0, 50],
            duration: 400,
            easing: 'easeOutQuad',
            complete: function() {
                sampleApplications.style.display = 'none';
                showApplicationDetailsContent(applicationId, application);
            }
        });
    } else {
        showApplicationDetailsContent(applicationId, application);
    }
}

// Show application details content
function showApplicationDetailsContent(applicationId, application) {
    const applicationDetails = document.getElementById('applicationDetails');
    if (!applicationDetails) return;

    // Fill basic information
    document.getElementById('detailApplicationId').textContent = applicationId;
    document.getElementById('vendorNameDetail').textContent = application.vendorName;
    document.getElementById('locationDetail').textContent = application.location;
    document.getElementById('productCategoriesDetail').textContent = application.productCategories.join(', ');
    document.getElementById('applicationDate').textContent = application.applicationDate;
    document.getElementById('queuePosition').textContent = application.queuePosition > 0 ? `#${application.queuePosition}` : '-';
    document.getElementById('estimatedTime').textContent = application.estimatedTime;
    document.getElementById('lastUpdate').textContent = application.lastUpdate;

    // Set current status
    const currentStatusElement = document.getElementById('currentStatus');
    if (currentStatusElement) {
        setStatusElement(currentStatusElement, application.status);
    }

    // Generate timeline
    generateTimeline(application.timeline);

    // Generate review details
    generateReviewDetails(application.reviewDetails);

    // Always try to load interview information so vendor can see booked slots
    loadInterviewInformation(applicationId, application.status);

    // Load bond requirements if application is approved
    loadBondRequirements(application);

    // Show details section
    applicationDetails.classList.remove('hidden');
    
    // Add display animation
    anime({
        targets: applicationDetails,
        opacity: [0, 1],
        translateY: [50, 0],
        duration: 600,
        easing: 'easeOutQuad'
    });
}

// Set status element style
function setStatusElement(element, status) {
    const statusConfig = {
        pending: { text: 'Pending', class: 'bg-gray-100 text-gray-800' },
        received: { text: 'Received', class: 'bg-blue-100 text-blue-800' },
        validating: { text: 'Validating', class: 'bg-yellow-100 text-yellow-800' },
        reviewing: { text: 'Under Review', class: 'bg-blue-100 text-blue-800' },
        interview_required: { text: 'Interview Required', class: 'bg-purple-100 text-purple-800' },
        additional_info_required: { text: 'Additional Info Required', class: 'bg-amber-100 text-amber-800' },
        approved: { text: 'Approved', class: 'bg-green-100 text-green-800' },
        rejected: { text: 'Rejected', class: 'bg-red-100 text-red-800' }
    };

    const config = statusConfig[status] || statusConfig.pending;
    element.textContent = config.text;
    element.className = `px-4 py-2 rounded-full text-sm font-semibold ${config.class}`;
}

// Generate timeline
function generateTimeline(timeline) {
    const timelineElement = document.getElementById('timeline');
    if (!timelineElement) return;

    timelineElement.innerHTML = '';

    timeline.forEach((item, index) => {
        const timelineItem = document.createElement('div');
        timelineItem.className = 'timeline-item flex items-start space-x-4';

        const dotClass = item.completed ? 'completed' : 
                        index === timeline.findIndex(t => !t.completed) ? 'active' : 'pending';

        timelineItem.innerHTML = `
            <div class="timeline-dot ${dotClass}">
                ${item.completed ? 
                    '<svg class="w-5 h-5" fill="currentColor" viewBox="0 0 20 20"><path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd"></path></svg>' :
                    index === timeline.findIndex(t => !t.completed) ?
                    '<svg class="w-5 h-5" fill="currentColor" viewBox="0 0 20 20"><path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd"></path></svg>' :
                    '<svg class="w-5 h-5" fill="currentColor" viewBox="0 0 20 20"><path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm1-12a1 1 0 10-2 0v4a1 1 0 00.293.707l2.828 2.829a1 1 0 101.415-1.415L11 9.586V6z" clip-rule="evenodd"></path></svg>'
                }
            </div>
            <div class="flex-1">
                <div class="flex items-center justify-between">
                    <h3 class="text-lg font-semibold text-gray-900">${item.title}</h3>
                    ${item.time ? `<span class="text-sm text-gray-500">${item.time}</span>` : ''}
                </div>
                <p class="text-gray-600 mt-1">${item.description}</p>
            </div>
        `;

        timelineElement.appendChild(timelineItem);
    });

    // Add timeline animation
    anime({
        targets: '.timeline-item',
        translateX: [-50, 0],
        opacity: [0, 1],
        duration: 600,
        easing: 'easeOutQuad',
        delay: anime.stagger(200)
    });
}

// Generate review details
function generateReviewDetails(reviewDetails) {
    const reviewElement = document.getElementById('reviewDetails');
    if (!reviewElement) return;

    if (!reviewDetails) {
        reviewElement.innerHTML = '<p class="text-gray-500">No review details available</p>';
        return;
    }

    let detailsHTML = '';

    // Score
    if (reviewDetails.score) {
        detailsHTML += `
            <div class="bg-gray-50 rounded-lg p-4">
                <h4 class="font-semibold text-gray-900 mb-2">Overall Score</h4>
                <div class="flex items-center space-x-4">
                    <div class="text-3xl font-bold text-blue-600">${reviewDetails.score}</div>
                    <div class="text-sm text-gray-600">out of 100</div>
                </div>
            </div>
        `;
    }

    // Strengths
    if (reviewDetails.strengths && reviewDetails.strengths.length > 0) {
        detailsHTML += `
            <div class="bg-green-50 rounded-lg p-4">
                <h4 class="font-semibold text-green-900 mb-2">Strengths</h4>
                <ul class="list-disc list-inside space-y-1 text-green-800">
                    ${reviewDetails.strengths.map(strength => `<li>${strength}</li>`).join('')}
                </ul>
            </div>
        `;
    }

    // Weaknesses
    if (reviewDetails.weaknesses && reviewDetails.weaknesses.length > 0) {
        detailsHTML += `
            <div class="bg-red-50 rounded-lg p-4">
                <h4 class="font-semibold text-red-900 mb-2">Areas for Improvement</h4>
                <ul class="list-disc list-inside space-y-1 text-red-800">
                    ${reviewDetails.weaknesses.map(weakness => `<li>${weakness}</li>`).join('')}
                </ul>
            </div>
        `;
    }

    // Recommendation
    if (reviewDetails.recommendation) {
        detailsHTML += `
            <div class="bg-blue-50 rounded-lg p-4">
                <h4 class="font-semibold text-blue-900 mb-2">Review Recommendation</h4>
                <p class="text-blue-800">${reviewDetails.recommendation}</p>
            </div>
        `;
    }

    reviewElement.innerHTML = detailsHTML;

    // Add review details animation
    anime({
        targets: '#reviewDetails > div',
        translateY: [30, 0],
        opacity: [0, 1],
        duration: 600,
        easing: 'easeOutQuad',
        delay: anime.stagger(100)
    });
}

// Load sample application
function loadSampleApplication(applicationId) {
    document.getElementById('applicationId').value = applicationId;
    searchApplication();
}

// Show toast notification
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

// Go back function
function goBack() {
    const applicationDetails = document.getElementById('applicationDetails');
    const sampleApplications = document.getElementById('sampleApplications');
    
    if (applicationDetails && sampleApplications) {
        anime({
            targets: applicationDetails,
            opacity: [1, 0],
            translateY: [0, 50],
            duration: 400,
            easing: 'easeOutQuad',
            complete: function() {
                applicationDetails.classList.add('hidden');
                sampleApplications.style.display = 'grid';
                
                anime({
                    targets: sampleApplications,
                    opacity: [0, 1],
                    translateY: [50, 0],
                    duration: 600,
                    easing: 'easeOutQuad'
                });
            }
        });
    }
    
    // Clear search input
    document.getElementById('applicationId').value = '';
    
    // Update URL
    window.history.replaceState({}, document.title, window.location.pathname);
}

// ========== Interview Display Functions ==========

// Load and display interview information
async function loadInterviewInformation(applicationNumber, applicationStatus = '') {
    const interviewSection = document.getElementById('interviewSection');
    const interviewCards = document.getElementById('interviewCards');
    const noInterview = document.getElementById('noInterview');
    
    if (!interviewSection) return;
    
    // Hide interview section if application status is 'approved'
    // Interview schedule should not be shown after application is approved
    if (applicationStatus === 'approved') {
        console.log('[loadInterviewInformation] Application is approved - hiding interview section');
        hideInterviewSection();
        return;
    }
    
    try {
        const interviews = await fetchInterviews(applicationNumber);
        
        // Also try to load booked slots
        let bookedSlots = [];
        console.log('[loadInterviewInformation] Attempting to load booked slots for applicationNumber:', applicationNumber);
        console.log('[loadInterviewInformation] InterviewAPI available:', typeof InterviewAPI !== 'undefined');
        console.log('[loadInterviewInformation] getMySlots method available:', typeof InterviewAPI !== 'undefined' && typeof InterviewAPI.getMySlots === 'function');
        
        try {
            if (typeof InterviewAPI !== 'undefined' && InterviewAPI.getMySlots) {
                console.log('[loadInterviewInformation] Calling InterviewAPI.getMySlots with applicationNumber:', applicationNumber);
                const slotsResponse = await InterviewAPI.getMySlots(applicationNumber);
                console.log('[loadInterviewInformation] getMySlots response:', slotsResponse);
                
                if (slotsResponse && slotsResponse.code === 200 && slotsResponse.data) {
                    bookedSlots = Array.isArray(slotsResponse.data) ? slotsResponse.data : [slotsResponse.data];
                    console.log('[loadInterviewInformation] Booked slots found:', bookedSlots.length, bookedSlots);
                } else {
                    console.log('[loadInterviewInformation] No booked slots - response code:', slotsResponse?.code, 'data:', slotsResponse?.data);
                }
            } else {
                console.warn('[loadInterviewInformation] InterviewAPI or getMySlots not available');
            }
        } catch (slotError) {
            console.error('[loadInterviewInformation] Failed to load booked slots:', slotError);
        }
        
        console.log('[loadInterviewInformation] Processing results - interviews:', interviews?.length || 0, 'bookedSlots:', bookedSlots.length, 'status:', applicationStatus);
        
        if (interviews && interviews.length > 0) {
            console.log('[loadInterviewInformation] Showing interview cards');
            // Show interview section
            interviewSection.classList.remove('hidden');
            noInterview.classList.add('hidden');
            
            // Clear existing cards
            interviewCards.innerHTML = '';
            
            // Generate interview cards
            interviews.forEach(interview => {
                const card = createInterviewCard(interview);
                interviewCards.appendChild(card);
            });
            
            // Animate cards
            anime({
                targets: '#interviewCards > div',
                translateY: [30, 0],
                opacity: [0, 1],
                duration: 600,
                easing: 'easeOutQuad',
                delay: anime.stagger(100)
            });
        } else if (bookedSlots.length > 0) {
            console.log('[loadInterviewInformation] Showing booked slots cards');
            // User has booked slots but interview not yet created - show booked slots
            interviewSection.classList.remove('hidden');
            noInterview.classList.add('hidden');
            
            // Show booked slots
            interviewCards.innerHTML = '';
            bookedSlots.forEach(slot => {
                const card = createBookedSlotCard(slot);
                interviewCards.appendChild(card);
            });
            
            // Animate cards
            anime({
                targets: '#interviewCards > div',
                translateY: [30, 0],
                opacity: [0, 1],
                duration: 600,
                easing: 'easeOutQuad',
                delay: anime.stagger(100)
            });
        } else if (applicationStatus === 'interview_required') {
            console.log('[loadInterviewInformation] Showing "no interview" message');
            // No interview scheduled yet but interview required
            interviewSection.classList.remove('hidden');
            interviewCards.innerHTML = '';
            noInterview.classList.remove('hidden');
        } else {
            console.log('[loadInterviewInformation] Hiding interview section');
            hideInterviewSection();
        }
    } catch (error) {
        console.error('Failed to load interview information:', error);
        hideInterviewSection();
    }
}

// Create booked slot card element
function createBookedSlotCard(slot) {
    const card = document.createElement('div');
    card.className = 'border-2 border-green-300 rounded-xl p-6 hover:shadow-lg transition-all bg-gradient-to-br from-green-50 to-white';
    
    const startDate = new Date(slot.slotStart);
    const endDate = new Date(slot.slotEnd);
    const dateStr = startDate.toLocaleDateString('en-US', { 
        weekday: 'long', 
        year: 'numeric', 
        month: 'long', 
        day: 'numeric' 
    });
    const timeStr = `${startDate.toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit' })} - ${endDate.toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit' })}`;
    
    card.innerHTML = `
        <div class="flex justify-between items-start mb-4">
            <div class="flex items-center space-x-3">
                <div class="w-12 h-12 bg-gradient-to-br from-green-500 to-emerald-500 rounded-lg flex items-center justify-center">
                    <svg class="w-6 h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"></path>
                    </svg>
                </div>
                <div>
                    <h3 class="text-lg font-semibold text-gray-900">Interview Slot Booked</h3>
                    <p class="text-sm text-gray-500">Awaiting confirmation from Co-op Admin</p>
                </div>
            </div>
            <div class="px-3 py-1 bg-green-100 text-green-800 rounded-full text-sm font-semibold">
                ✓ Booked
            </div>
        </div>

        <div class="grid md:grid-cols-2 gap-6">
            <div class="space-y-4">
                <div class="flex items-center space-x-3">
                    <svg class="w-5 h-5 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"></path>
                    </svg>
                    <div>
                        <div class="text-sm text-gray-600">Interview Date</div>
                        <div class="font-semibold text-gray-900">${dateStr}</div>
                    </div>
                </div>
                
                <div class="flex items-center space-x-3">
                    <svg class="w-5 h-5 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"></path>
                    </svg>
                    <div>
                        <div class="text-sm text-gray-600">Time</div>
                        <div class="font-semibold text-gray-900">${timeStr}</div>
                    </div>
                </div>
            </div>
        </div>

        <div class="mt-6 bg-yellow-50 border-l-4 border-yellow-400 p-4 rounded">
            <div class="flex">
                <div class="flex-shrink-0">
                    <svg class="h-5 w-5 text-yellow-400" fill="currentColor" viewBox="0 0 20 20">
                        <path fill-rule="evenodd" d="M8.257 3.099c.765-1.36 2.722-1.36 3.486 0l5.58 9.92c.75 1.334-.213 2.98-1.742 2.98H4.42c-1.53 0-2.493-1.646-1.743-2.98l5.58-9.92zM11 13a1 1 0 11-2 0 1 1 0 012 0zm-1-8a1 1 0 00-1 1v3a1 1 0 002 0V6a1 1 0 00-1-1z" clip-rule="evenodd"></path>
                    </svg>
                </div>
                <div class="ml-3">
                    <h4 class="text-sm font-semibold text-yellow-800">Next Steps</h4>
                    <p class="text-sm text-yellow-700 mt-1">A Co-op Admin will confirm your interview slot and provide meeting details. You will receive a notification once confirmed.</p>
                </div>
            </div>
        </div>
    `;
    
    return card;
}

// Create interview card element
function createInterviewCard(interview) {
    const card = document.createElement('div');
    card.className = 'border-2 border-purple-200 rounded-xl p-6 hover:shadow-lg transition-all bg-gradient-to-br from-purple-50 to-white';
    
    const statusConfig = getInterviewStatusConfig(interview.status);
    const interviewDate = new Date(interview.interviewDatetime);
    const isUpcoming = interviewDate > new Date();
    
    card.innerHTML = `
        <div class="flex justify-between items-start mb-4">
            <div class="flex items-center space-x-3">
                <div class="w-12 h-12 bg-gradient-to-br from-purple-500 to-pink-500 rounded-lg flex items-center justify-center">
                    <svg class="w-6 h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"></path>
                    </svg>
                </div>
                <div>
                    <h3 class="text-lg font-semibold text-gray-900">Interview Scheduled</h3>
                    <p class="text-sm text-gray-500">Interview #${interview.id}</p>
                </div>
            </div>
            <div class="px-3 py-1 ${statusConfig.class} rounded-full text-sm font-semibold">
                ${statusConfig.text}
            </div>
        </div>

        <div class="grid md:grid-cols-2 gap-6">
            <!-- Left Column -->
            <div class="space-y-4">
                <div class="flex items-center space-x-3">
                    <svg class="w-5 h-5 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"></path>
                    </svg>
                    <div>
                        <div class="text-sm text-gray-600">Interview Date</div>
                        <div class="font-semibold text-gray-900">${formatInterviewDate(interview.interviewDatetime)}</div>
                    </div>
                </div>
                
                <div class="flex items-center space-x-3">
                    <svg class="w-5 h-5 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"></path>
                    </svg>
                    <div>
                        <div class="text-sm text-gray-600">Time & Duration</div>
                        <div class="font-semibold text-gray-900">${interview.interviewTime} (${interview.durationMinutes} minutes)</div>
                    </div>
                </div>
                
                <div class="flex items-center space-x-3">
                    <svg class="w-5 h-5 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 10l4.553-2.276A1 1 0 0121 8.618v6.764a1 1 0 01-1.447.894L15 14M5 18h8a2 2 0 002-2V8a2 2 0 00-2-2H5a2 2 0 00-2 2v8a2 2 0 002 2z"></path>
                    </svg>
                    <div>
                        <div class="text-sm text-gray-600">Platform</div>
                        <div class="font-semibold text-gray-900">${formatPlatform(interview.platform)}</div>
                    </div>
                </div>
            </div>

            <!-- Right Column -->
            <div class="space-y-4">
                ${interview.meetingId ? `
                <div class="bg-gray-50 rounded-lg p-3">
                    <div class="text-sm text-gray-700 font-semibold">🔑 Meeting ID</div>
                    <div class="font-mono text-gray-900 mt-1">${interview.meetingId}</div>
                </div>
                ` : ''}
                
                ${interview.meetingPassword ? `
                <div class="bg-gray-50 rounded-lg p-3">
                    <div class="text-sm text-gray-700 font-semibold">🔒 Password</div>
                    <div class="font-mono text-gray-900 mt-1">${interview.meetingPassword}</div>
                </div>
                ` : ''}
            </div>
        </div>

        ${interview.preparationNotes ? `
        <div class="mt-6 bg-yellow-50 border-l-4 border-yellow-400 p-4 rounded">
            <div class="flex">
                <div class="flex-shrink-0">
                    <svg class="h-5 w-5 text-yellow-400" fill="currentColor" viewBox="0 0 20 20">
                        <path fill-rule="evenodd" d="M8.257 3.099c.765-1.36 2.722-1.36 3.486 0l5.58 9.92c.75 1.334-.213 2.98-1.742 2.98H4.42c-1.53 0-2.493-1.646-1.743-2.98l5.58-9.92zM11 13a1 1 0 11-2 0 1 1 0 012 0zm-1-8a1 1 0 00-1 1v3a1 1 0 002 0V6a1 1 0 00-1-1z" clip-rule="evenodd"></path>
                    </svg>
                </div>
                <div class="ml-3">
                    <h4 class="text-sm font-semibold text-yellow-800">📝 Preparation Notes</h4>
                    <p class="text-sm text-yellow-700 mt-1">${interview.preparationNotes}</p>
                </div>
            </div>
        </div>
        ` : ''}

        ${!interview.vendorConfirmed && isUpcoming ? `
        <div class="mt-6">
            <button onclick="confirmInterview(${interview.id})" 
                    class="w-full bg-gradient-to-r from-green-600 to-green-700 text-white py-3 px-6 rounded-lg hover:from-green-700 hover:to-green-800 transition-all font-semibold shadow-lg">
                ✓ Confirm Interview Attendance
            </button>
        </div>
        ` : interview.vendorConfirmed ? `
        <div class="mt-6 bg-green-50 border border-green-200 rounded-lg p-4 text-center">
            <div class="flex items-center justify-center text-green-700">
                <svg class="w-5 h-5 mr-2" fill="currentColor" viewBox="0 0 20 20">
                    <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd"></path>
                </svg>
                <span class="font-semibold">You have confirmed this interview</span>
            </div>
            ${interview.vendorConfirmedTime ? `<p class="text-sm text-green-600 mt-2">Confirmed on ${formatDateTime(interview.vendorConfirmedTime)}</p>` : ''}
        </div>
        ` : ''}

        ${interview.interviewResult ? `
        <div class="mt-6 bg-${interview.interviewResult === 'passed' ? 'green' : interview.interviewResult === 'failed' ? 'red' : 'yellow'}-50 border border-${interview.interviewResult === 'passed' ? 'green' : interview.interviewResult === 'failed' ? 'red' : 'yellow'}-200 rounded-lg p-4">
            <h4 class="font-semibold text-${interview.interviewResult === 'passed' ? 'green' : interview.interviewResult === 'failed' ? 'red' : 'yellow'}-900 mb-2">
                Interview Result: ${interview.interviewResult === 'passed' ? '✓ Passed' : interview.interviewResult === 'failed' ? '✗ Failed' : '⟳ Second Round Required'}
            </h4>
            ${interview.interviewNotes ? `<p class="text-sm text-gray-700">${interview.interviewNotes}</p>` : ''}
            ${interview.interviewScore ? `<div class="mt-2 text-sm">Score: <span class="font-bold text-lg">${interview.interviewScore}/100</span></div>` : ''}
        </div>
        ` : ''}
    `;
    
    return card;
}

// Hide interview section
function hideInterviewSection() {
    const interviewSection = document.getElementById('interviewSection');
    if (interviewSection) {
        interviewSection.classList.add('hidden');
    }
}

// Format interview date (user-friendly format)
function formatInterviewDate(datetime) {
    if (!datetime) return '';
    const date = new Date(datetime);
    const options = { 
        weekday: 'long', 
        year: 'numeric', 
        month: 'long', 
        day: 'numeric' 
    };
    return date.toLocaleDateString('en-US', options);
}

// Format platform name
function formatPlatform(platform) {
    const platformMap = {
        'telegram': 'Telegram',
        'signal': 'Signal',
        'jitsi': 'Jitsi Meet',
        'zoom': 'Zoom',
        'other': 'Other Platform'
    };
    return platformMap[platform] || platform || 'TBD';
}

// Get interview status config
function getInterviewStatusConfig(status) {
    const configs = {
        'scheduled': { 
            text: 'Scheduled', 
            class: 'bg-blue-100 text-blue-800' 
        },
        'confirmed': { 
            text: 'Confirmed', 
            class: 'bg-green-100 text-green-800' 
        },
        'rescheduled': { 
            text: 'Rescheduled', 
            class: 'bg-yellow-100 text-yellow-800' 
        },
        'completed': { 
            text: 'Completed', 
            class: 'bg-gray-100 text-gray-800' 
        },
        'cancelled': { 
            text: 'Cancelled', 
            class: 'bg-red-100 text-red-800' 
        },
        'no_show': { 
            text: 'No Show', 
            class: 'bg-red-100 text-red-800' 
        }
    };
    return configs[status] || configs.scheduled;
}

// Confirm interview attendance
async function confirmInterview(interviewId) {
    if (!confirm('Are you sure you want to confirm your attendance for this interview?')) {
        return;
    }
    
    try {
        const response = await API.confirmInterview(interviewId);
        
        if (response.code === 200) {
            showToast('Interview confirmed successfully!', 'success');
            
            // Reload interview information
            const applicationId = document.getElementById('applicationId').value;
            setTimeout(() => {
                loadInterviewInformation(applicationId);
            }, 500);
        } else {
            showToast(response.msg || 'Failed to confirm interview', 'error');
        }
    } catch (error) {
        console.error('Failed to confirm interview:', error);
        showToast('Failed to confirm interview. Please try again later.', 'error');
    }
}

// Render application history list
function renderApplicationHistory(applications) {
    const historySection = document.getElementById('applicationHistorySection');
    const historyList = document.getElementById('applicationHistoryList');
    const noApplicationsMessage = document.getElementById('noApplicationsMessage');
    
    if (!historySection || !historyList) {
        return;
    }
    
    // Check if user has pending or approved applications
    const hasActiveApplication = applications.some(app => {
        const status = app.status || 'pending';
        return status === 'pending' || status === 'under_review' || status === 'approved';
    });
    
    // Hide "Apply for Vendor" button in navigation if user has active application
    if (hasActiveApplication) {
        const applicationLink = document.querySelector('a[href="/mall/static/vendor/application"]');
        if (applicationLink) {
            applicationLink.style.display = 'none';
        }
    }
    
    if (applications.length === 0) {
        historySection.classList.remove('hidden');
        historyList.innerHTML = '';
        if (noApplicationsMessage) {
            noApplicationsMessage.classList.remove('hidden');
        }
        return;
    }
    
    // Hide no applications message
    if (noApplicationsMessage) {
        noApplicationsMessage.classList.add('hidden');
    }
    
    // Show history section
    historySection.classList.remove('hidden');
    
    // Render applications
    historyList.innerHTML = applications.map(app => {
        const status = app.status || 'pending';
        const statusConfig = getStatusConfig(status);
        const createTime = app.createTime ? new Date(app.createTime).toLocaleDateString('en-US', {
            year: 'numeric',
            month: 'short',
            day: 'numeric'
        }) : 'N/A';
        
        return `
            <div class="bg-gray-50 rounded-lg p-6 border border-gray-200 hover:shadow-md transition-shadow cursor-pointer" 
                 onclick="loadApplicationDetail('${app.applicationId || app.id}')">
                <div class="flex justify-between items-start mb-4">
                    <div class="flex-1">
                        <h3 class="text-lg font-semibold text-gray-900 mb-2">${app.vendorName || 'Unnamed Vendor'}</h3>
                        <p class="text-sm text-gray-600 mb-1">Application ID: <span class="font-mono font-semibold">${app.applicationId || 'N/A'}</span></p>
                        <p class="text-sm text-gray-500">Submitted: ${createTime}</p>
                    </div>
                    <div class="ml-4">
                        <span class="px-3 py-1 rounded-full text-sm font-semibold ${statusConfig.class}">
                            ${statusConfig.text}
                        </span>
                    </div>
                </div>
                <div class="grid grid-cols-2 gap-4 text-sm">
                    <div>
                        <span class="text-gray-600">Location:</span>
                        <span class="font-medium ml-2">${app.location || 'N/A'}</span>
                    </div>
                    <div>
                        <span class="text-gray-600">Categories:</span>
                        <span class="font-medium ml-2">${app.productCategories || 'N/A'}</span>
                    </div>
                </div>
                ${app.reviewProgress !== null && app.reviewProgress !== undefined ? `
                    <div class="mt-4">
                        <div class="flex justify-between items-center mb-1">
                            <span class="text-sm text-gray-600">Review Progress</span>
                            <span class="text-sm font-semibold text-gray-900">${app.reviewProgress}%</span>
                        </div>
                        <div class="w-full bg-gray-200 rounded-full h-2">
                            <div class="bg-blue-600 h-2 rounded-full" style="width: ${app.reviewProgress}%"></div>
                        </div>
                    </div>
                ` : ''}
            </div>
        `;
    }).join('');
}

// Load my applications history
async function loadMyApplications() {
    const historySection = document.getElementById('applicationHistorySection');
    const historyList = document.getElementById('applicationHistoryList');
    const noApplicationsMessage = document.getElementById('noApplicationsMessage');
    
    if (!historySection || !historyList) {
        return;
    }
    
    try {
        const response = await API.getMyApplications();
        
        if (response.code === 200 && response.data && Array.isArray(response.data)) {
            const applications = response.data;
            renderApplicationHistory(applications);
        } else {
            // No applications or error
            historySection.classList.remove('hidden');
            historyList.innerHTML = '';
            if (noApplicationsMessage) {
                noApplicationsMessage.classList.remove('hidden');
            }
        }
    } catch (error) {
        console.error('Failed to load applications:', error);
        // Don't show error if user is not logged in
        if (enforceApplicationAuth()) {
            historySection.classList.remove('hidden');
            historyList.innerHTML = '<p class="text-red-600 text-center py-4">Failed to load applications. Please try again later.</p>';
        }
    }
}

// Get status config
function getStatusConfig(status) {
    const configs = {
        'pending': { text: 'Pending', class: 'bg-gray-100 text-gray-800' },
        'reviewing': { text: 'Under Review', class: 'bg-blue-100 text-blue-800' },
        'approved': { text: 'Approved', class: 'bg-green-100 text-green-800' },
        'rejected': { text: 'Rejected', class: 'bg-red-100 text-red-800' },
        'interview_required': { text: 'Interview Required', class: 'bg-purple-100 text-purple-800' },
        'additional_info_required': { text: 'Additional Info Required', class: 'bg-yellow-100 text-yellow-800' }
    };
    return configs[status] || configs.pending;
}

// Load application detail directly via API
async function loadApplicationDetail(applicationId) {
    if (!applicationId) {
        return;
    }

    // Update hidden input if it exists (for compatibility)
    const searchInput = document.getElementById('applicationId');
    if (searchInput) {
        searchInput.value = applicationId;
    }

    try {
        // Call real API to get application status
        const response = await API.getApplicationStatus(applicationId);
        
        if (response.code === 200 && response.data) {
            // Success - convert API data to display format
            const application = convertApiDataToDisplayFormat(response.data);
            displayApplicationDetails(applicationId, application);
            
            // Scroll to application details
            setTimeout(() => {
                const detailsSection = document.getElementById('applicationDetails');
                if (detailsSection) {
                    detailsSection.scrollIntoView({ behavior: 'smooth', block: 'start' });
                }
            }, 300);
        } else {
            console.error('Failed to load application:', response.msg);
            showToast(response.msg || 'Application not found', 'error');
        }
    } catch (error) {
        console.error('Failed to fetch application:', error);
        showToast('Failed to load application details', 'error');
    }
}

// Export to global scope for interview.js
window.loadApplicationDetail = loadApplicationDetail;

// ========= Bond Payment Functions =========

// Monitor checkbox state
document.addEventListener('DOMContentLoaded', function() {
    const acceptTerms = document.getElementById('acceptBondTerms');
    const payBtn = document.getElementById('payBondBtn');
    
    if (acceptTerms && payBtn) {
        acceptTerms.addEventListener('change', function(e) {
            payBtn.disabled = !e.target.checked;
        });
    }
});

// Load bond requirements for current application
async function loadBondRequirements(application) {
    console.log('[Bond] Loading bond requirements for application:', application);
    
    // Check if Bond is paid
    if (application.status === 'approved' && application.bondOrderId && application.bondPaidTime) {
        // Bond is paid - show payment success status
        console.log('[Bond] Bond paid. Showing payment success status.');
        await showBondPaidStatus(application);
        
        // Check if wallet addresses are provided
        const hasWalletAddresses = application.walletBtcProvided || 
                                   application.walletXmrProvided || 
                                   application.walletUsdtProvided;
        
        if (!hasWalletAddresses) {
            // Show wallet address input form
            console.log('[Bond] Bond paid but wallet addresses not provided. Showing wallet input form.');
            showWalletAddressInput(application);
        } else {
            // Wallet addresses already provided - show complete status
            console.log('[Bond] Bond paid and wallet addresses provided.');
        }
        return;
    }
    
    // Only show bond payment if status is 'approved' and bond not yet paid
    if (application.status !== 'approved' || application.bondOrderId) {
        console.log('[Bond] Not showing bond payment section. Status:', application.status, 'BondOrderId:', application.bondOrderId);
        return;
    }
    
    try {
        const response = await fetch('/api/mall/vendor/bond/required-amount', {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json'
            },
            credentials: 'include'
        });
        
        const result = await response.json();
        console.log('[Bond] Required amount response:', result);
        
        if (result.code === 200 && result.data) {
            const bondData = result.data;
            
            // Update UI
            document.getElementById('bondLevel').textContent = `Level ${bondData.level}`;
            document.getElementById('bondAmount').textContent = bondData.bondAmount.toLocaleString();
            document.getElementById('bondPayoutTime').textContent = bondData.payoutTime || 'T+3';
            document.getElementById('bondTaxDiscount').textContent = bondData.taxDiscountMultiplier || '0';
            
            // Render markdown rules content
            if (bondData.rulesContent && typeof marked !== 'undefined') {
                document.getElementById('bondRulesContent').innerHTML = marked.parse(bondData.rulesContent);
            } else {
                document.getElementById('bondRulesContent').innerHTML = '<pre>' + (bondData.rulesContent || 'No rules available') + '</pre>';
            }
            
            // Show level perks
            document.getElementById('bondLevelPerks').textContent = bondData.levelPerks || 'Loading...';
            
            // Store application info for payment
            window.currentBondData = bondData;
            
            // Show bond payment section
            document.getElementById('bondPaymentSection').classList.remove('hidden');
            
            // Animate in
            anime({
                targets: '#bondPaymentSection',
                opacity: [0, 1],
                translateY: [30, 0],
                duration: 600,
                easing: 'easeOutQuad'
            });
            
        } else {
            console.error('[Bond] Failed to get bond requirements:', result.msg);
        }
    } catch (error) {
        console.error('[Bond] Error loading bond requirements:', error);
    }
}

// Proceed to bond payment
async function proceedToBondPayment() {
    console.log('[Bond Payment] Starting bond payment flow');
    
    if (!window.currentBondData) {
        showToast('Bond information not loaded. Please refresh the page.', 'error');
        return;
    }
    
    const bondData = window.currentBondData;
    const level = bondData.level;
    const productId = 'BOND-BASE';  // Use base product ID
    const quantity = level;  // Use level as quantity (Level 1 = qty 1, Level 2 = qty 2, etc.)
    
    console.log('[Bond Payment] Creating order for product:', productId, 'quantity:', quantity, 'level:', level);
    
    try {
        // Show loading state
        const payBtn = document.getElementById('payBondBtn');
        const originalText = payBtn.innerHTML;
        payBtn.disabled = true;
        payBtn.innerHTML = `
            <span class="flex items-center justify-center">
                <svg class="animate-spin h-5 w-5 mr-2" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                    <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                    <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                </svg>
                Creating Bond Payment Order...
            </span>
        `;
        
        // Get Bond product SKU ID and redirect to OS Store product page
        showToast('Redirecting to OS Store...', 'success');
        
        try {
            // Fetch Bond product SKU ID
            const skuResponse = await fetch('/api/mall/vendor/bond/product-sku-id', {
                method: 'GET',
                credentials: 'include'
            });
            
            const skuResult = await skuResponse.json();
            console.log('[Bond Payment] Product SKU ID result:', skuResult);
            
            if (skuResult.code === 200 && skuResult.data) {
                const skuId = skuResult.data.skuId;
                // Redirect to OS Store product page with quantity parameter
                // The quantity will be set to the bond level (Level 1 = qty 1, Level 2 = qty 2, etc.)
                // Also pass applicationNumber for order creation
                const params = new URLSearchParams({
                    quantity: level,
                    bondApplicationId: bondData.applicationId,
                    bondApplicationNumber: bondData.applicationNumber
                });
                window.location.href = `/mall/static/product/${skuId}?${params.toString()}`;
            } else {
                showToast('Failed to get Bond product information', 'error');
                payBtn.disabled = false;
                payBtn.innerHTML = originalText;
            }
        } catch (error) {
            console.error('[Bond Payment] Error getting product SKU ID:', error);
            showToast('Failed to redirect to OS Store. Please try again.', 'error');
            payBtn.disabled = false;
            payBtn.innerHTML = originalText;
        }
        
    } catch (error) {
        console.error('[Bond Payment] Error:', error);
        showToast('Failed to create bond payment order. Please try again.', 'error');
        
        // Restore button
        const payBtn = document.getElementById('payBondBtn');
        if (payBtn) {
            payBtn.disabled = false;
            payBtn.innerHTML = originalText;
        }
    }
}

// Show Bond paid status
async function showBondPaidStatus(application) {
    const bondPaidSection = document.getElementById('bondPaidSection');
    if (!bondPaidSection) {
        console.error('[Bond Paid] Bond paid section not found');
        return;
    }
    
    try {
        // Fetch bond order details
        let orderData = null;
        if (application.bondOrderId) {
            try {
                const response = await fetch(`/api/mall/vendor/bond/order/${application.bondOrderId}`, {
                    method: 'GET',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    credentials: 'include'
                });
                
                const result = await response.json();
                if (result.code === 200 && result.data) {
                    orderData = result.data;
                }
            } catch (error) {
                console.error('[Bond Paid] Failed to fetch order details:', error);
            }
        }
        
        // Get bond level and amount from application or order data
        const bondLevel = application.bondLevel || (orderData ? orderData.bondLevel : 1);
        // Use totalAmount (actual paid amount) instead of bondAmount (configured amount)
        const bondAmount = orderData ? (orderData.totalAmount || orderData.bondAmount) : null;
        
        // Update UI with order information
        document.getElementById('bondOrderNumber').textContent = application.bondOrderId || '-';
        
        // Format payment date
        if (application.bondPaidTime) {
            const paidDate = new Date(application.bondPaidTime);
            document.getElementById('bondPaidDate').textContent = formatDateTime(application.bondPaidTime);
        } else {
            document.getElementById('bondPaidDate').textContent = '-';
        }
        
        document.getElementById('bondPaidLevel').textContent = `Level ${bondLevel}`;
        
        if (bondAmount) {
            document.getElementById('bondPaidAmount').textContent = bondAmount.toLocaleString();
        } else {
            // Try to get from bond config
            try {
                const response = await fetch('/api/mall/vendor/bond/required-amount', {
                    method: 'GET',
                    credentials: 'include'
                });
                const result = await response.json();
                if (result.code === 200 && result.data) {
                    document.getElementById('bondPaidAmount').textContent = result.data.bondAmount.toLocaleString();
                } else {
                    document.getElementById('bondPaidAmount').textContent = '-';
                }
            } catch (error) {
                document.getElementById('bondPaidAmount').textContent = '-';
            }
        }
        
        // Update next steps based on wallet address status
        const hasWalletAddresses = application.walletBtcProvided || 
                                   application.walletXmrProvided || 
                                   application.walletUsdtProvided;
        
        const stepsList = document.getElementById('bondPaidStepsList');
        if (stepsList) {
            if (!hasWalletAddresses) {
                stepsList.innerHTML = `
                    <li>✓ Bond payment completed successfully</li>
                    <li>📝 Please provide your withdrawal wallet addresses below</li>
                    <li>⏳ A 1-day withdrawal lock will be applied after submitting addresses</li>
                `;
            } else {
                stepsList.innerHTML = `
                    <li>✓ Bond payment completed successfully</li>
                    <li>✓ Withdrawal wallet addresses provided</li>
                    <li>🎉 Your Co-op Member account is now fully activated!</li>
                `;
            }
        }
        
        // Show bond paid section
        bondPaidSection.classList.remove('hidden');
        
        // Animate in
        if (typeof anime !== 'undefined') {
            anime({
                targets: '#bondPaidSection',
                opacity: [0, 1],
                translateY: [30, 0],
                duration: 600,
                easing: 'easeOutQuad'
            });
        }
        
        console.log('[Bond Paid] Showing bond paid status for application:', application.id);
        
    } catch (error) {
        console.error('[Bond Paid] Error showing bond paid status:', error);
    }
}

// Show wallet address input form (after Bond is paid)
function showWalletAddressInput(application) {
    const walletSection = document.getElementById('walletAddressSection');
    if (!walletSection) {
        console.error('[Wallet Address] Wallet address section not found');
        return;
    }
    
    // Pre-fill existing addresses if available
    if (application.walletBtcProvided) {
        document.getElementById('btcAddress').value = application.walletBtcProvided;
    }
    if (application.walletUsdtProvided) {
        document.getElementById('usdtAddress').value = application.walletUsdtProvided;
    }
    if (application.walletXmrProvided) {
        document.getElementById('xmrAddress').value = application.walletXmrProvided;
    }
    
    // Show wallet address section
    walletSection.classList.remove('hidden');
    
    // Animate in
    if (typeof anime !== 'undefined') {
        anime({
            targets: '#walletAddressSection',
            opacity: [0, 1],
            translateY: [30, 0],
            duration: 600,
            easing: 'easeOutQuad'
        });
    }
    
    console.log('[Wallet Address] Showing wallet address input form for application:', application.id);
}

// Submit wallet addresses
async function submitWalletAddresses(event) {
    event.preventDefault();
    
    const submitBtn = document.getElementById('submitWalletBtn');
    const originalText = submitBtn.innerHTML;
    submitBtn.disabled = true;
    submitBtn.innerHTML = `
        <span class="flex items-center justify-center">
            <svg class="animate-spin h-5 w-5 mr-2" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
            </svg>
            Submitting...
        </span>
    `;
    
    try {
        const btcAddress = document.getElementById('btcAddress').value.trim();
        const usdtAddress = document.getElementById('usdtAddress').value.trim();
        const xmrAddress = document.getElementById('xmrAddress').value.trim();
        
        if (!btcAddress || !usdtAddress || !xmrAddress) {
            showToast('Please fill in all wallet addresses', 'error');
            submitBtn.disabled = false;
            submitBtn.innerHTML = originalText;
            return;
        }
        
        // 获取 CSRF token
        const getCsrfToken = () => {
            const name = 'XSRF-TOKEN';
            const cookies = document.cookie.split(';');
            for (let i = 0; i < cookies.length; i++) {
                let cookie = cookies[i].trim();
                if (cookie.indexOf(name + '=') === 0) {
                    return decodeURIComponent(cookie.substring(name.length + 1));
                }
            }
            return null;
        };
        
        const csrfToken = getCsrfToken();
        const headers = {
            'Content-Type': 'application/json'
        };
        if (csrfToken) {
            headers['X-XSRF-TOKEN'] = csrfToken;
        }
        
        const response = await fetch('/api/mall/vendor/bond/submit-wallet-addresses', {
            method: 'POST',
            headers: headers,
            credentials: 'include',
            body: JSON.stringify({
                btcAddress: btcAddress,
                usdtAddress: usdtAddress,
                xmrAddress: xmrAddress
            })
        });
        
        const result = await response.json();
        console.log('[Wallet Address] Submit result:', result);
        
        if (result.code === 200) {
            showToast('Wallet addresses saved successfully! A 1-day withdrawal lock has been applied.', 'success');
            
            // Hide wallet address section and reload application details
            setTimeout(() => {
                document.getElementById('walletAddressSection').classList.add('hidden');
                // Reload application details
                const applicationId = document.getElementById('detailApplicationId')?.textContent;
                if (applicationId) {
                    loadApplicationDetail(applicationId.trim());
                }
            }, 2000);
        } else {
            showToast(result.msg || 'Failed to save wallet addresses', 'error');
            submitBtn.disabled = false;
            submitBtn.innerHTML = originalText;
        }
        
    } catch (error) {
        console.error('[Wallet Address] Error submitting addresses:', error);
        showToast('Failed to save wallet addresses. Please try again.', 'error');
        submitBtn.disabled = false;
        submitBtn.innerHTML = originalText;
    }
}

// Export to global scope
window.submitWalletAddresses = submitWalletAddresses;
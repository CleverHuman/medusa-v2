/**
 * Vendor Bond & Level Management
 * 保证金和等级管理功能
 */

let bondData = null;
let levelHistory = [];

// Initialize
document.addEventListener('DOMContentLoaded', function() {
    loadBondSummary();
    loadLevelHistory();
});

// Load bond summary
async function loadBondSummary() {
    try {
        showLoading();
        const response = await API.get('/api/mall/vendor/bond/summary');
        
        if (response.code === 200 && response.data) {
            bondData = response.data;
            renderBondSummary(response.data);
            renderCapacityChart(response.data);
            hideLoading();
            document.getElementById('mainContent').classList.remove('hidden');
        } else {
            showError(response.msg || 'Failed to load bond information');
        }
    } catch (error) {
        console.error('Failed to load bond summary:', error);
        showError('Failed to load bond information: ' + error.message);
    }
}

// Load level history
async function loadLevelHistory() {
    try {
        const response = await API.get('/api/mall/vendor/bond/level/history');
        
        if (response.code === 200 && response.rows) {
            levelHistory = response.rows;
            renderLevelHistory(response.rows);
        }
    } catch (error) {
        console.error('Failed to load level history:', error);
    }
}

// Render bond summary
function renderBondSummary(data) {
    // Level badge
    const level = data.level || 1;
    const levelBadge = document.getElementById('levelBadge');
    levelBadge.className = `level-badge level-${level} mt-4 md:mt-0`;
    levelBadge.textContent = `Level ${level}`;
    
    // Stats
    document.getElementById('bondAmount').textContent = formatCurrency(data.bond || 0);
    document.getElementById('salesPoints').textContent = formatNumber(data.salesPoints || 0);
    document.getElementById('maxSalesLimit').textContent = formatCurrency(data.maxSalesLimit || 0);
    document.getElementById('availableCapacity').textContent = formatCurrency(data.availableCapacity || 0);
    document.getElementById('currentPendingSales').textContent = formatCurrency(data.currentPendingSales || 0);
    
    // Balance information
    document.getElementById('withdrawableBalance').textContent = formatCurrency(data.withdrawableBalance || 0);
    document.getElementById('pendingBalance').textContent = formatCurrency(data.pendingBalance || 0);
    document.getElementById('totalWithdrawn').textContent = formatCurrency(data.totalWithdrawn || 0);
    
    // Next level info
    const nextLevel = data.nextLevel || {};
    const isMaxLevel = nextLevel.isMaxLevel || level >= 5;
    
    if (isMaxLevel) {
        document.getElementById('nextLevelSection').classList.add('hidden');
        document.getElementById('maxLevelMessage').classList.remove('hidden');
    } else {
        document.getElementById('nextLevelSection').classList.remove('hidden');
        document.getElementById('maxLevelMessage').classList.add('hidden');
        
        document.getElementById('nextLevelNumber').textContent = nextLevel.nextLevel || 2;
        document.getElementById('nextLevelDisplay').textContent = nextLevel.nextLevel || 2;
        document.getElementById('currentPoints').textContent = formatNumber(data.salesPoints || 0);
        document.getElementById('nextLevelThreshold').textContent = formatNumber(nextLevel.nextLevelThreshold || 0);
        document.getElementById('pointsToNextLevel').textContent = formatNumber(nextLevel.pointsToNextLevel || 0);
        
        const progressPercentage = nextLevel.progressPercentage || 0;
        document.getElementById('progressPercentage').textContent = progressPercentage;
        document.getElementById('progressBar').style.width = progressPercentage + '%';
    }
}

// Render capacity chart
function renderCapacityChart(data) {
    const maxLimit = parseFloat(data.maxSalesLimit || 0);
    const currentPending = parseFloat(data.currentPendingSales || 0);
    const available = parseFloat(data.availableCapacity || 0);
    
    const chart = echarts.init(document.getElementById('capacityChart'));
    
    const option = {
        tooltip: {
            trigger: 'axis',
            axisPointer: { type: 'shadow' }
        },
        legend: {
            data: ['Used', 'Available'],
            bottom: 10
        },
        grid: {
            left: '3%',
            right: '4%',
            bottom: '15%',
            containLabel: true
        },
        xAxis: {
            type: 'category',
            data: ['Sales Capacity'],
            axisLabel: { fontSize: 12 }
        },
        yAxis: {
            type: 'value',
            axisLabel: {
                formatter: function(value) {
                    return '$' + formatNumber(value);
                }
            }
        },
        series: [
            {
                name: 'Used',
                type: 'bar',
                stack: 'capacity',
                data: [currentPending],
                itemStyle: { color: '#f56565' },
                label: {
                    show: true,
                    position: 'inside',
                    formatter: function(params) {
                        return formatCurrency(params.value);
                    }
                }
            },
            {
                name: 'Available',
                type: 'bar',
                stack: 'capacity',
                data: [available],
                itemStyle: { color: '#48bb78' },
                label: {
                    show: true,
                    position: 'inside',
                    formatter: function(params) {
                        return formatCurrency(params.value);
                    }
                }
            }
        ]
    };
    
    chart.setOption(option);
    
    // Resize chart on window resize
    window.addEventListener('resize', function() {
        chart.resize();
    });
}

// Render level history
function renderLevelHistory(history) {
    const container = document.getElementById('levelHistoryContainer');
    
    if (!history || history.length === 0) {
        container.innerHTML = `
            <div class="text-center py-8 text-gray-500">
                <svg class="w-16 h-16 mx-auto mb-4 text-gray-300" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"></path>
                </svg>
                <p>No level upgrade history yet.</p>
                <p class="text-sm mt-2">Your level upgrade history will appear here.</p>
            </div>
        `;
        return;
    }
    
    const historyHTML = `
        <div class="overflow-x-auto">
            <table class="min-w-full divide-y divide-gray-200">
                <thead class="bg-gray-50">
                    <tr>
                        <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Date</th>
                        <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Level Change</th>
                        <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Points Change</th>
                        <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Trigger Order</th>
                        <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Amount</th>
                    </tr>
                </thead>
                <tbody class="bg-white divide-y divide-gray-200">
                    ${history.map(item => `
                        <tr>
                            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                                ${formatDateTime(item.createTime)}
                            </td>
                            <td class="px-6 py-4 whitespace-nowrap">
                                <span class="px-2 py-1 inline-flex text-xs leading-5 font-semibold rounded-full bg-blue-100 text-blue-800">
                                    Level ${item.oldLevel} → Level ${item.newLevel}
                                </span>
                            </td>
                            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                                ${formatNumber(item.oldPoints)} → ${formatNumber(item.newPoints)}
                            </td>
                            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-600">
                                ${item.triggerOrderId || 'N/A'}
                            </td>
                            <td class="px-6 py-4 whitespace-nowrap text-sm font-semibold text-gray-900">
                                ${formatCurrency(item.triggerAmount || 0)}
                            </td>
                        </tr>
                    `).join('')}
                </tbody>
            </table>
        </div>
    `;
    
    container.innerHTML = historyHTML;
}

// Show loading
function showLoading() {
    document.getElementById('loadingState').classList.remove('hidden');
    document.getElementById('errorState').classList.add('hidden');
    document.getElementById('mainContent').classList.add('hidden');
}

// Hide loading
function hideLoading() {
    document.getElementById('loadingState').classList.add('hidden');
}

// Show error
function showError(message) {
    document.getElementById('errorState').classList.remove('hidden');
    document.getElementById('errorMessage').textContent = message;
    document.getElementById('loadingState').classList.add('hidden');
    document.getElementById('mainContent').classList.add('hidden');
}

// Format currency
function formatCurrency(amount) {
    if (typeof amount === 'string') {
        amount = parseFloat(amount);
    }
    return new Intl.NumberFormat('en-US', {
        style: 'currency',
        currency: 'USD',
        minimumFractionDigits: 2,
        maximumFractionDigits: 2
    }).format(amount || 0);
}

// Format number
function formatNumber(num) {
    if (typeof num === 'string') {
        num = parseInt(num);
    }
    return new Intl.NumberFormat('en-US').format(num || 0);
}

// Format date time
function formatDateTime(dateTime) {
    if (!dateTime) return 'N/A';
    const date = new Date(dateTime);
    return new Intl.DateTimeFormat('en-US', {
        year: 'numeric',
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
    }).format(date);
}


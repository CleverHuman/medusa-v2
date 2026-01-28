/**
 * Vendor Order Management
 * 订单管理功能：查看、接受、拒绝、发货
 */

let orders = [];
let currentRejectingOrderId = null;
let currentShippingOrderId = null;

// Order status mapping
const ORDER_STATUS = {
    0: { label: 'Pending', class: 'status-pending' },
    1: { label: 'Paid', class: 'status-paid' },
    2: { label: 'Fulfilled', class: 'status-paid' },
    3: { label: 'Shipped', class: 'status-shipped' },
    4: { label: 'Cancelled', class: 'status-cancelled' },
    6: { label: 'Vendor Accepted', class: 'status-vendor-accepted' }
};

// Initialize
document.addEventListener('DOMContentLoaded', function() {
    loadOrders();
    setupEventListeners();
});

// Setup event listeners
function setupEventListeners() {
    // Search input
    const searchInput = document.getElementById('searchInput');
    if (searchInput) {
        searchInput.addEventListener('input', debounce(applyFilters, 300));
    }

    // Status filter
    const statusFilter = document.getElementById('statusFilter');
    if (statusFilter) {
        statusFilter.addEventListener('change', applyFilters);
    }
}

// Load orders from API
async function loadOrders() {
    try {
        showLoading();
        const url = window.API ? window.API.getURL('/api/mall/vendor/order/list') : '/api/mall/vendor/order/list';
        const response = await fetch(url, {
            method: 'GET',
            headers: window.API ? window.API.buildHeaders() : { 'Content-Type': 'application/json' },
            credentials: 'include'
        });

        const result = await response.json();
        
        if (result.code === 200 && result.data) {
            orders = Array.isArray(result.data) ? result.data : [];
            // Sort orders by createTime descending (newest first)
            orders.sort((a, b) => {
                const dateA = a.createTime ? new Date(a.createTime).getTime() : 0;
                const dateB = b.createTime ? new Date(b.createTime).getTime() : 0;
                return dateB - dateA; // Descending order (newest first)
            });
            renderOrders();
            updateStats();
        } else {
            showError(result.msg || 'Failed to load orders');
        }
    } catch (error) {
        console.error('Error loading orders:', error);
        showError('Failed to load orders: ' + error.message);
    }
}

// Render orders list
function renderOrders() {
    const container = document.getElementById('ordersContainer');
    if (!container) return;

    if (orders.length === 0) {
        container.innerHTML = `
            <div class="bg-white rounded-xl shadow-md p-8 text-center text-gray-500">
                <svg class="w-16 h-16 mx-auto mb-4 text-gray-300" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 11V7a4 4 0 00-8 0v4M5 9h14l1 12H4L5 9z"></path>
                </svg>
                <p class="text-lg">No orders found</p>
            </div>
        `;
        return;
    }

    container.innerHTML = orders.map(order => createOrderCard(order)).join('');
}

// Create order card HTML
function createOrderCard(order) {
    const status = ORDER_STATUS[order.status] || { label: 'Unknown', class: 'status-pending' };
    const formattedDate = formatDate(order.createTime);
    const formattedAmount = formatCurrency(order.totalAmount || 0);

    // Determine available actions based on status
    const actions = getOrderActions(order);

    return `
        <div class="order-card bg-white rounded-xl shadow-md p-6">
            <div class="flex flex-col md:flex-row md:items-center md:justify-between gap-4">
                <div class="flex-1">
                    <div class="flex items-center gap-4 mb-2">
                        <h3 class="text-lg font-semibold text-gray-900">Order #${order.orderSn || order.id}</h3>
                        <span class="status-badge ${status.class}">${status.label}</span>
                    </div>
                    <div class="grid grid-cols-2 md:grid-cols-4 gap-4 text-sm text-gray-600">
                        <div>
                            <span class="font-medium">Date:</span>
                            <span class="ml-1">${formattedDate}</span>
                        </div>
                        <div>
                            <span class="font-medium">Amount:</span>
                            <span class="ml-1 font-semibold text-gray-900">${formattedAmount}</span>
                        </div>
                        ${order.itemsCount ? `
                        <div>
                            <span class="font-medium">Items:</span>
                            <span class="ml-1">${order.itemsCount}</span>
                        </div>
                        ` : ''}
                        ${order.shippingNumber ? `
                        <div>
                            <span class="font-medium">Tracking:</span>
                            <span class="ml-1 font-mono text-xs">${order.shippingNumber}</span>
                        </div>
                        ` : ''}
                    </div>
                </div>
                <div class="flex flex-wrap gap-2">
                    <button onclick="viewOrderDetail('${order.id}')" 
                            class="px-4 py-2 bg-gray-100 hover:bg-gray-200 text-gray-700 rounded-lg font-medium transition-colors text-sm">
                        View Details
                    </button>
                    ${actions}
                </div>
            </div>
        </div>
    `;
}

// Get order actions based on status
function getOrderActions(order) {
    const status = order.status;
    let actions = '';

    // Can accept/reject when status is Pending (0) or Paid (1)
    if (status === 0 || status === 1) {
        actions += `
            <button onclick="acceptOrder('${order.id}')" 
                    class="px-4 py-2 bg-green-600 hover:bg-green-700 text-white rounded-lg font-medium transition-colors text-sm">
                Accept
            </button>
            <button onclick="openRejectModal('${order.id}')" 
                    class="px-4 py-2 bg-red-600 hover:bg-red-700 text-white rounded-lg font-medium transition-colors text-sm">
                Reject
            </button>
        `;
    }

    // Can ship when status is Paid (1) or Vendor Accepted (6)
    if (status === 1 || status === 6) {
        actions += `
            <button onclick="openShipModal('${order.id}')" 
                    class="px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded-lg font-medium transition-colors text-sm">
                Ship
            </button>
        `;
    }

    return actions;
}

// View order details
async function viewOrderDetail(orderId) {
    try {
        const url = window.API ? window.API.getURL(`/api/mall/vendor/order/${orderId}`) : `/api/mall/vendor/order/${orderId}`;
        const response = await fetch(url, {
            method: 'GET',
            headers: window.API ? window.API.buildHeaders() : { 'Content-Type': 'application/json' },
            credentials: 'include'
        });

        const result = await response.json();
        
        if (result.code === 200 && result.data) {
            // Debug: log the response structure (仅在开发环境)
            if (window.Logger) {
                window.Logger.log('Order detail response received');
            }
            // result.data contains: { order, items, shipping, payment, member }
            showOrderDetailModal(result.data);
        } else {
            if (window.Logger) {
                window.Logger.error('Failed to load order details:', result);
            }
            alert(result.msg || 'Failed to load order details');
        }
    } catch (error) {
        if (window.Logger) {
            window.Logger.errorSafe(error, 'loadOrderDetails');
        }
        const safeMessage = window.Logger ? window.Logger.getSafeMessage(error) : 'Failed to load order details. Please try again.';
        alert(safeMessage);
    }
}

// Show order detail modal
function showOrderDetailModal(orderDetail) {
    const modal = document.getElementById('orderDetailModal');
    const content = document.getElementById('orderDetailContent');
    
    if (!modal || !content) return;

    // Debug: log the orderDetail structure
    console.log('showOrderDetailModal received:', orderDetail);

    // Handle both structures: { order, items, ... } or direct order object
    const order = orderDetail.order || orderDetail;
    const items = orderDetail.items || [];
    const payment = orderDetail.payment || {};
    const shipping = orderDetail.shipping || {};
    const member = orderDetail.member || {};

    // Debug: log extracted data
    console.log('Extracted order:', order);
    console.log('Extracted items:', items);
    console.log('Extracted payment:', payment);
    console.log('Extracted shipping:', shipping);
    console.log('Extracted member:', member);

    // Validate order data
    if (!order || (!order.id && !order.orderSn)) {
        console.error('Invalid order data:', order);
        content.innerHTML = `
            <div class="p-6 text-center text-red-600">
                <p class="text-lg font-semibold mb-2">Error loading order details</p>
                <p class="text-sm">Order data is invalid or missing.</p>
            </div>
        `;
        modal.classList.add('active');
        return;
    }

    const status = ORDER_STATUS[order.status] || { label: 'Unknown', class: 'status-pending' };
    const formattedDate = formatDate(order.createTime || order.create_time || null);
    const formattedAmount = formatCurrency(order.totalAmount || order.total_amount || 0);
    const formattedPostage = formatCurrency(order.freightAmount || order.freight_amount || 0);
    
    // Get order ID - try multiple possible field names
    const orderId = order.orderSn || order.order_sn || order.id || 'N/A';

    // Get username - try to get from member or use memberId
    let username = 'N/A';
    if (member && member.username) {
        username = member.username;
    } else if (order.memberId) {
        username = `Member_${order.memberId}`;
    }

    // Get channel - try multiple possible field names
    const sourceType = order.sourceType || order.source_type;
    const channelMap = {
        0: 'OS',
        1: 'Telegram',
        2: 'Web'
    };
    const channel = channelMap[sourceType] || sourceType || 'N/A';

    // Get coin type
    const coinTypeMap = {
        'BTC': 'BTC',
        'XMR': 'XMR',
        'USDT_TRX': 'USDT (TRX)',
        'USDT_ERC': 'USDT (ERC)'
    };
    const coinType = coinTypeMap[payment.payType] || payment.payType || 'N/A';

    // Build items table
    let itemsTable = '';
    if (items && items.length > 0) {
        itemsTable = `
            <div class="overflow-x-auto">
                <table class="min-w-full divide-y divide-gray-200">
                    <thead class="bg-gray-50">
                        <tr>
                            <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Product Name</th>
                            <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Amount</th>
                            <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Price</th>
                            <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Qty</th>
                            <th class="px-4 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">Total</th>
                        </tr>
                    </thead>
                    <tbody class="bg-white divide-y divide-gray-200">
                        ${items.map(item => {
                            // Get Amount (productSpec) - try both camelCase and snake_case
                            const amount = item.productSpec || item.product_spec || 'N/A';
                            // Get Price
                            const price = item.price || 0;
                            return `
                            <tr>
                                <td class="px-4 py-3 whitespace-nowrap text-sm text-gray-900">${item.productName || item.product_name || 'N/A'}</td>
                                <td class="px-4 py-3 whitespace-nowrap text-sm text-gray-600">${amount}</td>
                                <td class="px-4 py-3 whitespace-nowrap text-sm text-gray-600">${formatCurrency(price)}</td>
                                <td class="px-4 py-3 whitespace-nowrap text-sm text-gray-600">${item.quantity || 0}</td>
                                <td class="px-4 py-3 whitespace-nowrap text-sm text-gray-900 text-right font-medium">${formatCurrency(item.totalPrice || item.total_price || 0)}</td>
                            </tr>
                        `;
                        }).join('')}
                    </tbody>
                </table>
            </div>
        `;
    } else {
        itemsTable = '<p class="text-gray-500 text-sm">No items found</p>';
    }

    content.innerHTML = `
        <div class="space-y-6">
            <!-- Order Basic Info -->
            <div class="grid grid-cols-2 gap-4">
                <div>
                    <p class="text-sm text-gray-500">Order ID</p>
                    <p class="text-lg font-semibold text-gray-900">${orderId}</p>
                </div>
                <div>
                    <p class="text-sm text-gray-500">Status</p>
                    <span class="status-badge ${status.class}">${status.label}</span>
                </div>
                <div>
                    <p class="text-sm text-gray-500">Date</p>
                    <p class="text-lg font-semibold text-gray-900">${formattedDate}</p>
                </div>
                <div>
                    <p class="text-sm text-gray-500">Channel</p>
                    <p class="text-lg font-semibold text-gray-900">${channel}</p>
                </div>
                <div>
                    <p class="text-sm text-gray-500">Username</p>
                    <p class="text-lg font-semibold text-gray-900">${username}</p>
                </div>
                <div>
                    <p class="text-sm text-gray-500">Coin Type</p>
                    <p class="text-lg font-semibold text-gray-900">${coinType}</p>
                </div>
            </div>

            <!-- Order Items -->
            <div>
                <p class="text-sm text-gray-500 mb-3 font-medium">Order Items</p>
                ${itemsTable}
            </div>

            <!-- Order Summary -->
            <div class="bg-gray-50 p-4 rounded-lg">
                <div class="space-y-2">
                    <div class="flex justify-between">
                        <span class="text-sm text-gray-600">Postage:</span>
                        <span class="text-sm font-medium text-gray-900">${formattedPostage}</span>
                    </div>
                    <div class="flex justify-between border-t border-gray-200 pt-2">
                        <span class="text-base font-semibold text-gray-900">Total AUD:</span>
                        <span class="text-base font-bold text-gray-900">${formattedAmount}</span>
                    </div>
                </div>
            </div>

            ${order.remark ? `
            <div>
                <p class="text-sm text-gray-500 mb-2">Remarks</p>
                <p class="text-gray-700 bg-gray-50 p-3 rounded-lg">${order.remark}</p>
            </div>
            ` : ''}

            ${shipping && (shipping.receiverName || shipping.addressLine1) ? `
            <div>
                <p class="text-sm text-gray-500 mb-2">Shipping Address</p>
                <div class="bg-gray-50 p-4 rounded-lg">
                    <p class="text-gray-900 font-medium">${shipping.receiverName || 'N/A'}</p>
                    <p class="text-gray-700">${shipping.addressLine1 || ''}</p>
                    ${shipping.addressLine2 ? `<p class="text-gray-700">${shipping.addressLine2}</p>` : ''}
                    ${shipping.addressLine3 ? `<p class="text-gray-700">${shipping.addressLine3}</p>` : ''}
                    <p class="text-gray-700">${shipping.city || ''}, ${shipping.state || ''} ${shipping.postCode || ''}</p>
                    <p class="text-gray-700">${shipping.country || ''}</p>
                    ${shipping.shippingNumber ? `<p class="text-gray-700 mt-2"><span class="font-medium">Tracking:</span> ${shipping.shippingNumber}</p>` : ''}
                </div>
            </div>
            ` : ''}
        </div>
    `;

    modal.classList.add('active');
}

// Close order detail modal
function closeOrderDetailModal() {
    const modal = document.getElementById('orderDetailModal');
    if (modal) {
        modal.classList.remove('active');
    }
}

// Accept order
async function acceptOrder(orderId) {
    if (!confirm('Are you sure you want to accept this order?')) {
        return;
    }

    try {
        const url = window.API ? window.API.getURL(`/api/mall/vendor/order/${orderId}/accept`) : `/api/mall/vendor/order/${orderId}/accept`;
        const response = await fetch(url, {
            method: 'POST',
            headers: window.API ? window.API.buildHeaders() : { 'Content-Type': 'application/json' },
            credentials: 'include'
        });

        const result = await response.json();
        
        if (result.code === 200) {
            alert('Order accepted successfully!');
            loadOrders(); // Reload orders
        } else {
            alert(result.msg || 'Failed to accept order');
        }
    } catch (error) {
        console.error('Error accepting order:', error);
        alert('Failed to accept order: ' + error.message);
    }
}

// Open reject modal
function openRejectModal(orderId) {
    currentRejectingOrderId = orderId;
    const modal = document.getElementById('rejectOrderModal');
    const reasonInput = document.getElementById('rejectReason');
    
    if (modal) {
        modal.classList.add('active');
    }
    if (reasonInput) {
        reasonInput.value = '';
        reasonInput.focus();
    }
}

// Close reject modal
function closeRejectModal() {
    currentRejectingOrderId = null;
    const modal = document.getElementById('rejectOrderModal');
    if (modal) {
        modal.classList.remove('active');
    }
}

// Confirm reject order
async function confirmRejectOrder() {
    if (!currentRejectingOrderId) return;

    const reasonInput = document.getElementById('rejectReason');
    const reason = reasonInput ? reasonInput.value.trim() : '';

    if (!reason) {
        alert('Please provide a rejection reason');
        return;
    }

    try {
        const url = window.API ? window.API.getURL(`/api/mall/vendor/order/${currentRejectingOrderId}/reject`) : `/api/mall/vendor/order/${currentRejectingOrderId}/reject`;
        const response = await fetch(url + '?reason=' + encodeURIComponent(reason), {
            method: 'POST',
            headers: window.API ? window.API.buildHeaders() : { 'Content-Type': 'application/json' },
            credentials: 'include'
        });

        const result = await response.json();
        
        if (result.code === 200) {
            alert('Order rejected successfully!');
            closeRejectModal();
            loadOrders(); // Reload orders
        } else {
            alert(result.msg || 'Failed to reject order');
        }
    } catch (error) {
        console.error('Error rejecting order:', error);
        alert('Failed to reject order: ' + error.message);
    }
}

// Open ship modal
function openShipModal(orderId) {
    currentShippingOrderId = orderId;
    const modal = document.getElementById('shipOrderModal');
    const trackingInput = document.getElementById('trackingNumber');
    
    if (modal) {
        modal.classList.add('active');
    }
    if (trackingInput) {
        trackingInput.value = '';
        trackingInput.focus();
    }
}

// Close ship modal
function closeShipModal() {
    currentShippingOrderId = null;
    const modal = document.getElementById('shipOrderModal');
    if (modal) {
        modal.classList.remove('active');
    }
}

// Confirm ship order
async function confirmShipOrder() {
    if (!currentShippingOrderId) return;

    const trackingInput = document.getElementById('trackingNumber');
    const trackingNumber = trackingInput ? trackingInput.value.trim() : '';

    if (!trackingNumber) {
        alert('Please enter a tracking number');
        return;
    }

    try {
        const url = window.API ? window.API.getURL(`/api/mall/vendor/order/${currentShippingOrderId}/ship`) : `/api/mall/vendor/order/${currentShippingOrderId}/ship`;
        const response = await fetch(url + '?trackingNumber=' + encodeURIComponent(trackingNumber), {
            method: 'POST',
            headers: window.API ? window.API.buildHeaders() : { 'Content-Type': 'application/json' },
            credentials: 'include'
        });

        const result = await response.json();
        
        if (result.code === 200) {
            alert('Order shipped successfully!');
            closeShipModal();
            loadOrders(); // Reload orders
        } else {
            alert(result.msg || 'Failed to ship order');
        }
    } catch (error) {
        console.error('Error shipping order:', error);
        alert('Failed to ship order: ' + error.message);
    }
}

// Apply filters
function applyFilters() {
    const searchTerm = document.getElementById('searchInput').value.toLowerCase();
    const statusFilter = document.getElementById('statusFilter').value;

    const filteredOrders = orders.filter(order => {
        const matchesSearch = !searchTerm || 
            (order.orderSn && order.orderSn.toLowerCase().includes(searchTerm)) ||
            (order.id && order.id.toLowerCase().includes(searchTerm));
        
        const matchesStatus = !statusFilter || order.status.toString() === statusFilter;
        
        return matchesSearch && matchesStatus;
    });

    renderFilteredOrders(filteredOrders);
}

// Render filtered orders
function renderFilteredOrders(filteredOrders) {
    const container = document.getElementById('ordersContainer');
    if (!container) return;

    if (filteredOrders.length === 0) {
        container.innerHTML = `
            <div class="bg-white rounded-xl shadow-md p-8 text-center text-gray-500">
                <p class="text-lg">No orders match your filters</p>
            </div>
        `;
        return;
    }

    // Sort filtered orders by createTime descending (newest first)
    const sortedOrders = filteredOrders.sort((a, b) => {
        const dateA = a.createTime ? new Date(a.createTime).getTime() : 0;
        const dateB = b.createTime ? new Date(b.createTime).getTime() : 0;
        return dateB - dateA; // Descending order (newest first)
    });

    container.innerHTML = sortedOrders.map(order => createOrderCard(order)).join('');
}

// Update statistics
function updateStats() {
    const total = orders.length;
    const pending = orders.filter(o => o.status === 0).length;
    const paid = orders.filter(o => o.status === 1 || o.status === 6).length;
    const shipped = orders.filter(o => o.status === 3).length;

    const statTotal = document.getElementById('statTotal');
    const statPending = document.getElementById('statPending');
    const statPaid = document.getElementById('statPaid');
    const statShipped = document.getElementById('statShipped');

    if (statTotal) statTotal.textContent = total;
    if (statPending) statPending.textContent = pending;
    if (statPaid) statPaid.textContent = paid;
    if (statShipped) statShipped.textContent = shipped;
}

// Show loading state
function showLoading() {
    const container = document.getElementById('ordersContainer');
    if (container) {
        container.innerHTML = `
            <div class="bg-white rounded-xl shadow-md p-8 text-center text-gray-500">
                <p>Loading orders...</p>
            </div>
        `;
    }
}

// Show error message
function showError(message) {
    const container = document.getElementById('ordersContainer');
    if (container) {
        container.innerHTML = `
            <div class="bg-red-50 border border-red-200 rounded-xl shadow-md p-8 text-center">
                <p class="text-red-600">${message}</p>
            </div>
        `;
    }
}

// Format date
function formatDate(dateString) {
    if (!dateString) return 'N/A';
    try {
        const date = new Date(dateString);
        return date.toLocaleDateString('en-US', { 
            year: 'numeric', 
            month: 'short', 
            day: 'numeric',
            hour: '2-digit',
            minute: '2-digit'
        });
    } catch (e) {
        return dateString;
    }
}

// Format currency
function formatCurrency(amount) {
    if (!amount && amount !== 0) return '$0.00';
    return '$' + parseFloat(amount).toFixed(2);
}

// Debounce utility
function debounce(func, wait) {
    let timeout;
    return function executedFunction(...args) {
        const later = () => {
            clearTimeout(timeout);
            func(...args);
        };
        clearTimeout(timeout);
        timeout = setTimeout(later, wait);
    };
}

// Close modals when clicking outside
document.addEventListener('click', function(event) {
    const orderDetailModal = document.getElementById('orderDetailModal');
    const rejectOrderModal = document.getElementById('rejectOrderModal');
    const shipOrderModal = document.getElementById('shipOrderModal');

    if (event.target === orderDetailModal) {
        closeOrderDetailModal();
    }
    if (event.target === rejectOrderModal) {
        closeRejectModal();
    }
    if (event.target === shipOrderModal) {
        closeShipModal();
    }
});

// Export functions for global use
window.loadOrders = loadOrders;
window.viewOrderDetail = viewOrderDetail;
window.acceptOrder = acceptOrder;
window.openRejectModal = openRejectModal;
window.closeRejectModal = closeRejectModal;
window.confirmRejectOrder = confirmRejectOrder;
window.openShipModal = openShipModal;
window.closeShipModal = closeShipModal;
window.confirmShipOrder = confirmShipOrder;
window.closeOrderDetailModal = closeOrderDetailModal;


/**
 * Vendor Product Management
 * 产品上架和管理功能
 */

let products = [];
let currentEditingProduct = null;

// Fixed categories - only Physical and Digital products
const PRODUCT_CATEGORIES = [
    { value: 'Physical Product', label: 'Physical Product' },
    { value: 'Digital Product', label: 'Digital Product' }
];

// Initialize
document.addEventListener('DOMContentLoaded', function() {
    loadProducts();
    setupEventListeners();
    populateCategoryFilters();
});

// Setup event listeners
function setupEventListeners() {
    // Form submit
    const form = document.getElementById('productForm');
    if (form) {
        form.addEventListener('submit', handleProductSubmit);
    }

    // Search input
    const searchInput = document.getElementById('searchInput');
    if (searchInput) {
        searchInput.addEventListener('input', debounce(applyFilters, 300));
    }

    // Filter dropdowns
    ['filterCategory', 'filterStatus'].forEach(id => {
        const element = document.getElementById(id);
        if (element) {
            element.addEventListener('change', applyFilters);
        }
    });

    // Category change - show/hide amount section for Digital products
    const categorySelect = document.getElementById('productCategory');
    if (categorySelect) {
        categorySelect.addEventListener('change', toggleAmountSection);
    }
}

// Toggle amount section based on category
function toggleAmountSection() {
    const category = document.getElementById('productCategory').value;
    const amountSection = document.getElementById('amountSection');
    const amountInput = document.getElementById('productAmount');
    const unitInput = document.getElementById('productUnit');
    
    if (category === 'Digital Product') {
        // For digital products, amount section is optional
        if (amountSection) {
            amountSection.style.opacity = '0.6';
        }
        if (amountInput) amountInput.required = false;
        if (unitInput) unitInput.required = false;
    } else {
        // For physical products, amount is required
        if (amountSection) {
            amountSection.style.opacity = '1';
        }
        if (amountInput) amountInput.required = true;
        if (unitInput) unitInput.required = true;
    }
}

// Populate category filter dropdown
function populateCategoryFilters() {
    const filterSelect = document.getElementById('filterCategory');
    if (filterSelect) {
        // Keep first option (All Categories)
        const firstOption = filterSelect.options[0];
        filterSelect.innerHTML = '';
        filterSelect.appendChild(firstOption);
        
        // Add categories
        PRODUCT_CATEGORIES.forEach(cat => {
            const option = document.createElement('option');
            option.value = cat.value;
            option.textContent = cat.label;
            filterSelect.appendChild(option);
        });
    }
}

// Preview image
function previewImage() {
    const imageUrl = document.getElementById('productImageUrl').value;
    const previewContainer = document.getElementById('imagePreview');
    const previewImg = document.getElementById('previewImg');
    
    if (imageUrl) {
        previewImg.src = imageUrl;
        previewImg.onload = function() {
            previewContainer.classList.remove('hidden');
        };
        previewImg.onerror = function() {
            previewContainer.classList.add('hidden');
            showToast('Failed to load image. Please check the URL.', 'error');
        };
    } else {
        previewContainer.classList.add('hidden');
    }
}

// Load products
async function loadProducts() {
    try {
        showLoading();
        const response = await API.get('/api/mall/vendor/product/list');
        
        if (response.code === 200) {
            products = response.data || [];
            updateStats();
            renderProducts(products);
        } else {
            console.error('Failed to load products:', response.msg);
            showToast(response.msg || 'Failed to load products', 'error');
        }
    } catch (error) {
        console.error('Error loading products:', error);
        showToast('Failed to load products: ' + error.message, 'error');
    } finally {
        hideLoading();
    }
}

// Update statistics
function updateStats() {
    const total = products.length;
    const approved = products.filter(p => p.approvalStatus === 'APPROVED').length;
    const pending = products.filter(p => p.approvalStatus === 'PENDING_APPROVAL').length;
    const rejected = products.filter(p => p.approvalStatus === 'REJECTED').length;

    document.getElementById('statTotal').textContent = total;
    document.getElementById('statApproved').textContent = approved;
    document.getElementById('statPending').textContent = pending;
    document.getElementById('statRejected').textContent = rejected;
}

// Render products
function renderProducts(productsToRender) {
    const container = document.getElementById('productsList');
    const emptyState = document.getElementById('emptyState');

    if (!productsToRender || productsToRender.length === 0) {
        container.innerHTML = '';
        emptyState.classList.remove('hidden');
        return;
    }

    emptyState.classList.add('hidden');
    container.innerHTML = productsToRender.map(product => createProductCard(product)).join('');
}

// Create product card HTML
function createProductCard(product) {
    const statusClass = {
        'PENDING_APPROVAL': 'status-pending',
        'APPROVED': 'status-approved',
        'REJECTED': 'status-rejected'
    }[product.approvalStatus] || 'status-pending';

    const statusText = {
        'PENDING_APPROVAL': 'Pending Approval',
        'APPROVED': 'Approved',
        'REJECTED': 'Rejected'
    }[product.approvalStatus] || 'Unknown';

    // Format amount and unit
    const amountDisplay = product.model && product.unit ? 
        `${product.model} ${product.unit}` : '';

    return `
        <div class="product-card bg-white rounded-xl shadow-md overflow-hidden">
            <div class="aspect-w-16 aspect-h-9 bg-gray-200">
                ${product.imageUrl ? 
                    `<img src="${product.imageUrl}" alt="${product.name}" class="w-full h-48 object-cover">` :
                    `<div class="w-full h-48 flex items-center justify-center bg-gradient-to-br from-gray-100 to-gray-200">
                        <svg class="w-16 h-16 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z"></path>
                        </svg>
                    </div>`
                }
            </div>
            <div class="p-6">
                <div class="flex justify-between items-start mb-2">
                    <h3 class="text-lg font-semibold text-gray-900 line-clamp-2">${product.name || 'Untitled Product'}</h3>
                    <span class="status-badge ${statusClass}">${statusText}</span>
                </div>
                <div class="flex items-center gap-2 mb-3">
                    <span class="text-sm text-gray-600">${product.category || 'Uncategorized'}</span>
                    ${amountDisplay ? `<span class="text-xs bg-gray-100 text-gray-600 px-2 py-1 rounded">${amountDisplay}</span>` : ''}
                </div>
                <div class="flex justify-between items-center mb-4">
                    <div>
                        <p class="text-2xl font-bold text-blue-600">AUD $${(product.price || 0).toFixed(2)}</p>
                        <p class="text-sm text-gray-500">Stock: ${product.stock || product.inventory || 0}</p>
                    </div>
                </div>
                
                ${product.rejectionReason ? 
                    `<div class="mb-4 p-3 bg-red-50 border border-red-200 rounded-lg">
                        <p class="text-xs font-semibold text-red-800 mb-1">Rejection Reason:</p>
                        <p class="text-xs text-red-700">${product.rejectionReason}</p>
                    </div>` : ''
                }
                
                <div class="flex space-x-2">
                    <button onclick="viewProduct(${product.id})" 
                            class="flex-1 bg-gray-100 hover:bg-gray-200 text-gray-700 px-4 py-2 rounded-lg font-medium transition-colors text-sm">
                        View
                    </button>
                    ${product.approvalStatus !== 'APPROVED' ? 
                        `<button onclick="editProduct(${product.id})" 
                                class="flex-1 bg-blue-100 hover:bg-blue-200 text-blue-700 px-4 py-2 rounded-lg font-medium transition-colors text-sm">
                            Edit
                        </button>` : ''
                    }
                    <button onclick="deleteProduct(${product.id})" 
                            class="bg-red-100 hover:bg-red-200 text-red-700 px-4 py-2 rounded-lg font-medium transition-colors text-sm">
                        Delete
                    </button>
                </div>
            </div>
        </div>
    `;
}

// Apply filters
function applyFilters() {
    const searchTerm = document.getElementById('searchInput').value.toLowerCase();
    const categoryFilter = document.getElementById('filterCategory').value;
    const statusFilter = document.getElementById('filterStatus').value;

    const filtered = products.filter(product => {
        const matchesSearch = !searchTerm || 
            (product.name && product.name.toLowerCase().includes(searchTerm)) ||
            (product.description && product.description.toLowerCase().includes(searchTerm));
        
        const matchesCategory = !categoryFilter || product.category === categoryFilter;
        const matchesStatus = !statusFilter || product.approvalStatus === statusFilter;

        return matchesSearch && matchesCategory && matchesStatus;
    });

    renderProducts(filtered);
}

// Open add product modal
function openAddProductModal() {
    currentEditingProduct = null;
    document.getElementById('modalTitle').textContent = 'Add New Product';
    document.getElementById('productForm').reset();
    document.getElementById('productId').value = '';
    document.getElementById('productModal').classList.add('active');
}

// Open edit product modal
async function editProduct(id) {
    try {
        const response = await API.get(`/api/mall/vendor/product/${id}`);
        if (response.code === 200 && response.data) {
            currentEditingProduct = response.data;
            populateProductForm(response.data);
            document.getElementById('modalTitle').textContent = 'Edit Product';
            document.getElementById('productModal').classList.add('active');
        } else {
            showToast(response.msg || 'Failed to load product', 'error');
        }
    } catch (error) {
        console.error('Failed to load product:', error);
        showToast('Failed to load product: ' + error.message, 'error');
    }
}

// Populate form with product data
function populateProductForm(product) {
    document.getElementById('productId').value = product.id || '';
    document.getElementById('productName').value = product.name || '';
    document.getElementById('productCategory').value = product.category || '';
    document.getElementById('productAmount').value = product.model || '';
    document.getElementById('productUnit').value = product.unit || 'g';
    document.getElementById('productPrice').value = product.price || '';
    document.getElementById('productStock').value = product.stock || product.inventory || '';
    document.getElementById('productDescription').value = product.description || '';
    document.getElementById('productImageUrl').value = product.imageUrl || '';
    
    // Preview image if exists
    if (product.imageUrl) {
        previewImage();
    }
    
    // Toggle amount section based on category
    toggleAmountSection();
}

// Generate SKU from product name and amount
function generateSku(name, amount) {
    if (!name) return '';
    // Create SKU: first 3 chars of name (uppercase) + amount
    const prefix = name.replace(/[^a-zA-Z0-9]/g, '').substring(0, 3).toUpperCase();
    return `SKU-${prefix}-${amount || '1'}`;
}

// Handle form submit
async function handleProductSubmit(e) {
    e.preventDefault();

    const productId = document.getElementById('productId').value;
    const productName = document.getElementById('productName').value;
    const category = document.getElementById('productCategory').value;
    const amount = document.getElementById('productAmount').value;
    const unit = document.getElementById('productUnit').value;
    
    // Auto-generate SKU
    const sku = generateSku(productName, amount);
    
    const productData = {
        id: productId || null,
        name: productName,
        category: category,
        model: amount ? parseFloat(amount) : null,  // Amount/Model field
        unit: unit,
        price: parseFloat(document.getElementById('productPrice').value),
        stock: parseInt(document.getElementById('productStock').value),
        sku: sku,
        description: document.getElementById('productDescription').value,
        imageUrl: document.getElementById('productImageUrl').value || null
    };

    console.log('Submitting product data:', productData);

    try {
        let response;
        if (productId) {
            // Update existing product
            response = await API.put('/api/mall/vendor/product', productData);
        } else {
            // Create new product
            response = await API.post('/api/mall/vendor/product', productData);
        }

        if (response.code === 200) {
            showToast(productId ? 'Product updated successfully' : 'Product submitted for approval', 'success');
            closeProductModal();
            loadProducts(); // Reload list
        } else {
            showToast(response.msg || 'Failed to save product', 'error');
        }
    } catch (error) {
        console.error('Failed to save product:', error);
        showToast('Failed to save product: ' + error.message, 'error');
    }
}

// View product details
async function viewProduct(id) {
    try {
        const response = await API.get(`/api/mall/vendor/product/${id}`);
        if (response.code === 200 && response.data) {
            renderProductDetails(response.data);
            document.getElementById('viewProductModal').classList.add('active');
        } else {
            showToast(response.msg || 'Failed to load product', 'error');
        }
    } catch (error) {
        console.error('Failed to load product:', error);
        showToast('Failed to load product: ' + error.message, 'error');
    }
}

// Render product details
function renderProductDetails(product) {
    const statusClass = {
        'PENDING_APPROVAL': 'bg-yellow-100 text-yellow-800',
        'APPROVED': 'bg-green-100 text-green-800',
        'REJECTED': 'bg-red-100 text-red-800'
    }[product.approvalStatus] || 'bg-gray-100 text-gray-800';

    const statusText = {
        'PENDING_APPROVAL': 'Pending Approval',
        'APPROVED': 'Approved',
        'REJECTED': 'Rejected'
    }[product.approvalStatus] || 'Unknown';

    // Format amount and unit
    const amountDisplay = product.model && product.unit ? 
        `${product.model} ${product.unit}` : '';

    const content = `
        <div class="space-y-6">
            ${product.imageUrl ? 
                `<div class="w-full">
                    <img src="${product.imageUrl}" alt="${product.name}" class="w-full h-64 object-cover rounded-lg">
                </div>` : ''
            }
            
            <div>
                <div class="flex justify-between items-start mb-2">
                    <h3 class="text-2xl font-bold text-gray-900">${product.name}</h3>
                    <span class="px-3 py-1 rounded-full text-sm font-semibold ${statusClass}">${statusText}</span>
                </div>
                <div class="flex items-center gap-2">
                    <span class="text-gray-600">${product.category || 'Uncategorized'}</span>
                    ${amountDisplay ? `<span class="text-sm bg-gray-100 text-gray-600 px-2 py-1 rounded">${amountDisplay}</span>` : ''}
                </div>
            </div>

            <div>
                <h4 class="text-lg font-semibold text-gray-900 mb-4">SKUs (${product.skus && product.skus.length > 0 ? product.skus.length : 0})</h4>
                ${product.skus && product.skus.length > 0 ? 
                    `<div class="space-y-3">
                        ${product.skus.map(sku => {
                            const skuStatusClass = sku.status === 1 ? 'bg-green-100 text-green-800' : 'bg-gray-100 text-gray-800';
                            const skuStatusText = sku.status === 1 ? 'Active' : 'Inactive';
                            const productStatusText = product.approvalStatus === 'APPROVED' ? 'Approved' : 
                                                      product.approvalStatus === 'PENDING_APPROVAL' ? 'Pending Approval' : 
                                                      product.approvalStatus === 'REJECTED' ? 'Rejected' : 'Unknown';
                            return `
                                <div class="bg-gray-50 border border-gray-200 rounded-lg p-4">
                                    <div class="flex justify-between items-start mb-2">
                                        <div>
                                            <p class="font-semibold text-gray-900">${sku.sku || 'N/A'}</p>
                                            <p class="text-sm text-gray-600 mt-1">
                                                ${sku.model && sku.unit ? `${sku.model} ${sku.unit}` : ''}
                                                ${sku.model && sku.unit && sku.price ? ' • ' : ''}
                                                ${sku.price ? `AUD $${sku.price.toFixed(2)}` : ''}
                                            </p>
                                        </div>
                                        <div class="text-right">
                                            <span class="px-2 py-1 rounded text-xs font-semibold ${skuStatusClass}">${skuStatusText}</span>
                                            <p class="text-xs text-gray-500 mt-1">${productStatusText}</p>
                                        </div>
                                    </div>
                                    <div class="grid grid-cols-2 gap-4 mt-3 text-sm">
                                        <div>
                                            <span class="text-gray-500">Stock:</span>
                                            <span class="font-semibold ml-1">${sku.inventory || 0}</span>
                                        </div>
                                        <div>
                                            <span class="text-gray-500">Currency:</span>
                                            <span class="font-semibold ml-1">${sku.currency || 'AUD'}</span>
                                        </div>
                                    </div>
                                </div>
                            `;
                        }).join('')}
                    </div>` :
                    '<p class="text-gray-500">No SKUs available</p>'
                }
            </div>
            
            ${product.price ? `
            <div class="grid md:grid-cols-2 gap-6 border-t pt-4">
                <div>
                    <p class="text-sm text-gray-500 mb-1">Default Price</p>
                    <p class="text-2xl font-bold text-blue-600">AUD $${(product.price || 0).toFixed(2)}</p>
                </div>
                <div>
                    <p class="text-sm text-gray-500 mb-1">Default Stock</p>
                    <p class="text-xl font-semibold text-gray-900">${product.stock || product.inventory || 0} units</p>
                </div>
            </div>
            ` : ''}

            <div>
                <p class="text-sm text-gray-500 mb-2">Description</p>
                <p class="text-gray-700 whitespace-pre-wrap">${product.description || 'No description'}</p>
            </div>

            ${product.rejectionReason ? 
                `<div class="p-4 bg-red-50 border border-red-200 rounded-lg">
                    <p class="text-sm font-semibold text-red-800 mb-2">Rejection Reason:</p>
                    <p class="text-sm text-red-700">${product.rejectionReason}</p>
                </div>` : ''
            }

            ${product.approvalNotes ? 
                `<div class="p-4 bg-green-50 border border-green-200 rounded-lg">
                    <p class="text-sm font-semibold text-green-800 mb-2">Approval Notes:</p>
                    <p class="text-sm text-green-700">${product.approvalNotes}</p>
                </div>` : ''
            }

            <div class="flex space-x-2">
                ${product.approvalStatus !== 'APPROVED' ? 
                    `<button onclick="closeViewModal(); editProduct(${product.id})" 
                            class="flex-1 bg-blue-600 hover:bg-blue-700 text-white px-6 py-2 rounded-lg font-semibold transition-colors">
                        Edit Product
                    </button>` : ''
                }
                <button onclick="closeViewModal()" 
                        class="flex-1 bg-gray-200 hover:bg-gray-300 text-gray-700 px-6 py-2 rounded-lg font-semibold transition-colors">
                    Close
                </button>
            </div>
        </div>
    `;

    document.getElementById('viewProductContent').innerHTML = content;
}

// Delete product
async function deleteProduct(id) {
    if (!confirm('Are you sure you want to delete this product?')) {
        return;
    }

    try {
        const response = await API.delete(`/api/mall/vendor/product/${id}`);
        if (response.code === 200) {
            showToast('Product deleted successfully', 'success');
            loadProducts();
        } else {
            showToast(response.msg || 'Failed to delete product', 'error');
        }
    } catch (error) {
        console.error('Failed to delete product:', error);
        showToast('Failed to delete product: ' + error.message, 'error');
    }
}

// Close product modal
function closeProductModal() {
    document.getElementById('productModal').classList.remove('active');
    document.getElementById('productForm').reset();
    currentEditingProduct = null;
}

// Close view modal
function closeViewModal() {
    document.getElementById('viewProductModal').classList.remove('active');
}

// Utility: Debounce
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

// Show loading
function showLoading() {
    const container = document.getElementById('productsList');
    container.innerHTML = `
        <div class="col-span-full flex justify-center items-center py-12">
            <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
        </div>
    `;
}

// Hide loading
function hideLoading() {
    // Loading is replaced by products or empty state
}

// Show toast message
function showToast(message, type = 'info') {
    const toast = document.createElement('div');
    toast.className = `fixed top-20 right-4 z-50 px-6 py-3 rounded-lg shadow-lg transition-all transform ${
        type === 'success' ? 'bg-green-500 text-white' :
        type === 'error' ? 'bg-red-500 text-white' :
        type === 'warning' ? 'bg-yellow-500 text-white' :
        'bg-blue-500 text-white'
    }`;
    toast.textContent = message;

    document.body.appendChild(toast);

    setTimeout(() => {
        toast.classList.add('opacity-0', 'translate-x-full');
        setTimeout(() => toast.remove(), 300);
    }, 3000);
}

// Close modals when clicking outside
document.addEventListener('click', function(e) {
    if (e.target.id === 'productModal') {
        closeProductModal();
    }
    if (e.target.id === 'viewProductModal') {
        closeViewModal();
    }
});


/**
 * Vendor Withdrawal Management
 * 提现管理功能
 */

let balanceData = null;
let addresses = [];
let withdrawalRequests = [];
let balanceLogs = [];
let currentTab = 'balance';

// Initialize
document.addEventListener('DOMContentLoaded', function() {
    loadBalance();
    loadAddresses();
    loadWithdrawalHistory();
    loadBalanceLogs();
    setupFormHandlers();
});

// Load balance
async function loadBalance() {
    try {
        showLoading();
        const response = await API.get('/api/mall/vendor/withdrawal/balance');
        
        if (response.code === 200 && response.data) {
            balanceData = response.data;
            renderBalance(response.data);
            hideLoading();
            document.getElementById('mainContent').classList.remove('hidden');
        } else {
            showError(response.msg || 'Failed to load balance information');
        }
    } catch (error) {
        console.error('Failed to load balance:', error);
        showError('Failed to load balance information: ' + error.message);
    }
}

// Load addresses
async function loadAddresses() {
    try {
        const response = await API.get('/api/mall/vendor/withdrawal/addresses');
        
        if (response.code === 200 && response.rows) {
            addresses = response.rows;
            renderAddresses(response.rows);
        }
    } catch (error) {
        console.error('Failed to load addresses:', error);
    }
}

// Load withdrawal history
async function loadWithdrawalHistory() {
    try {
        const response = await API.get('/api/mall/vendor/withdrawal/requests');
        
        if (response.code === 200 && response.rows) {
            withdrawalRequests = response.rows;
            renderWithdrawalHistory(response.rows);
        }
    } catch (error) {
        console.error('Failed to load withdrawal history:', error);
    }
}

// Load balance logs
async function loadBalanceLogs() {
    try {
        const response = await API.get('/api/mall/vendor/withdrawal/balance/logs');
        
        if (response.code === 200 && response.rows) {
            balanceLogs = response.rows;
            renderBalanceLogs(response.rows);
        }
    } catch (error) {
        console.error('Failed to load balance logs:', error);
    }
}

// Render balance
function renderBalance(data) {
    const withdrawable = data.withdrawableBalance || 0;
    const pending = data.pendingBalance || 0;
    const total = data.totalWithdrawn || 0;
    
    document.getElementById('withdrawableBalance').textContent = formatCurrency(withdrawable);
    document.getElementById('pendingBalance').textContent = formatCurrency(pending);
    document.getElementById('totalWithdrawn').textContent = formatCurrency(total);
    document.getElementById('availableAmount').textContent = formatCurrency(withdrawable);
}

// Render addresses
function renderAddresses(addressList) {
    const container = document.getElementById('addressesList');
    if (!container) return;
    
    if (!addressList || addressList.length === 0) {
        container.innerHTML = '<p class="text-gray-500">No withdrawal addresses configured. Please add addresses for the currencies you want to withdraw.</p>';
        return;
    }
    
    const currencyMap = {
        'BTC': { name: 'Bitcoin', icon: '₿' },
        'XMR': { name: 'Monero', icon: 'ɱ' },
        'USDT_TRX': { name: 'USDT (TRON)', icon: '₮' },
        'USDT_ERC': { name: 'USDT (ERC20)', icon: '₮' }
    };
    
    container.innerHTML = addressList.map(addr => {
        const currency = currencyMap[addr.currency] || { name: addr.currency, icon: '●' };
        // 根据 isActive 判断状态（1=已激活，0=未激活/null=未设置）
        const isActive = addr.isActive === 1;
        const hasAddress = addr.address && addr.address.trim() !== '';
        const hasPendingAddress = addr.pendingAddress && addr.pendingAddress.trim() !== '';
        const isPending = hasAddress && !isActive;
        
        // 显示当前激活的地址
        const displayAddress = isActive ? (addr.address || 'Not configured') : (isPending ? '(Pending Verification)' : 'Not configured');
        
        return `
            <div class="bg-gray-50 rounded-lg p-4 border border-gray-200">
                <div class="flex items-center justify-between mb-2">
                    <div class="flex items-center space-x-3">
                        <div class="w-10 h-10 bg-blue-100 rounded-full flex items-center justify-center">
                            <span class="text-lg font-bold text-blue-600">${currency.icon}</span>
                        </div>
                        <div>
                            <h3 class="font-semibold text-gray-900">${currency.name}</h3>
                            <p class="text-sm text-gray-500">${addr.currency}</p>
                        </div>
                    </div>
                    <div>
                        ${isActive ? '<span class="status-badge status-completed">Verified</span>' : ''}
                        ${isPending ? '<span class="status-badge status-pending">Pending Verification</span>' : ''}
                        ${!hasAddress ? '<span class="status-badge status-rejected">Not Set</span>' : ''}
                    </div>
                </div>
                <div class="mt-3">
                    <p class="text-sm text-gray-600 mb-1">Current Address:</p>
                    <div class="flex items-center space-x-2">
                        <code class="flex-1 bg-white px-3 py-2 rounded border border-gray-300 text-sm font-mono break-all ${!isActive && hasAddress ? 'text-amber-600' : ''}">${displayAddress}</code>
                        <button onclick="updateAddress('${addr.currency}')" class="px-4 py-2 bg-gray-400 text-white rounded-lg cursor-not-allowed transition-colors text-sm" disabled>
                            ${hasAddress ? 'Update' : 'Add'}
                        </button>
                    </div>
                    ${isPending ? '<p class="text-xs text-amber-600 mt-1">Address is pending verification. Please complete the verification process.</p>' : ''}
                    ${hasPendingAddress && isActive ? `
                        <div class="mt-3 p-3 bg-blue-50 border border-blue-200 rounded-lg">
                            <p class="text-sm text-blue-800 font-medium mb-1">⏳ Pending Address Update</p>
                            <p class="text-xs text-blue-600 mb-2">A new address is waiting for verification:</p>
                            <code class="block bg-white px-3 py-2 rounded border border-blue-300 text-xs font-mono break-all text-blue-900">${addr.pendingAddress}</code>
                            <p class="text-xs text-blue-600 mt-2">Please complete the verification process to activate the new address. The current address will remain active until verification is complete.</p>
                        </div>
                    ` : ''}
                </div>
            </div>
        `;
    }).join('');
}

// Render withdrawal history
function renderWithdrawalHistory(requests) {
    const container = document.getElementById('withdrawalHistoryTable');
    if (!container) return;
    
    if (!requests || requests.length === 0) {
        container.innerHTML = '<tr><td colspan="6" class="px-6 py-4 text-center text-gray-500">No withdrawal requests found</td></tr>';
        return;
    }
    
    container.innerHTML = requests.map(req => {
        const statusClass = getStatusClass(req.requestStatus);
        return `
            <tr class="hover:bg-gray-50">
                <td class="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">${req.requestCode || 'N/A'}</td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">${req.currency}</td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">${formatCurrency(req.amount)}</td>
                <td class="px-6 py-4 whitespace-nowrap">
                    <span class="status-badge ${statusClass}">${req.requestStatus}</span>
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">${formatDateTime(req.createTime)}</td>
                <td class="px-6 py-4 whitespace-nowrap text-sm">
                    <button onclick="viewRequestDetail('${req.id}')" class="text-blue-600 hover:text-blue-800">View</button>
                </td>
            </tr>
        `;
    }).join('');
}

// Render balance logs
function renderBalanceLogs(logs) {
    const container = document.getElementById('balanceLogsTable');
    if (!container) return;
    
    if (!logs || logs.length === 0) {
        container.innerHTML = '<tr><td colspan="5" class="px-6 py-4 text-center text-gray-500">No balance logs found</td></tr>';
        return;
    }
    
    container.innerHTML = logs.map(log => {
        const amountClass = log.changeAmount >= 0 ? 'text-emerald-600' : 'text-red-600';
        const amountSign = log.changeAmount >= 0 ? '+' : '';
        return `
            <tr class="hover:bg-gray-50">
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">${formatDateTime(log.createTime)}</td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">${log.changeType || 'N/A'}</td>
                <td class="px-6 py-4 whitespace-nowrap text-sm font-medium ${amountClass}">${amountSign}${formatCurrency(log.changeAmount)}</td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">${formatCurrency(log.balanceAfter)}</td>
                <td class="px-6 py-4 text-sm text-gray-500">${log.description || 'N/A'}</td>
            </tr>
        `;
    }).join('');
}

// Setup form handlers
function setupFormHandlers() {
    const form = document.getElementById('withdrawalForm');
    if (form) {
        form.addEventListener('submit', handleWithdrawalSubmit);
    }
    
    const currencySelect = document.getElementById('withdrawalCurrency');
    if (currencySelect) {
        currencySelect.addEventListener('change', handleCurrencyChange);
    }
}

// Handle currency change
async function handleCurrencyChange(event) {
    const currency = event.target.value;
    const addressInput = document.getElementById('withdrawalAddress');
    const addressStatus = document.getElementById('addressStatus');
    
    if (!currency) {
        addressInput.value = '';
        addressStatus.textContent = 'Please select a currency first';
        addressStatus.className = 'text-sm text-gray-500 mt-1';
        return;
    }
    
    try {
        const response = await API.get(`/api/mall/vendor/withdrawal/address/${currency}`);
        
        if (response.code === 200 && response.data) {
            const data = response.data;
            // 检查地址是否已激活（isActive === 1）
            const isActive = data.isActive === 1;
            const hasPendingAddress = data.pendingAddress && data.pendingAddress.trim() !== '';
            
            if (isActive && data.address) {
                addressInput.value = data.address;
                if (hasPendingAddress) {
                    addressStatus.innerHTML = `
                        <span class="text-emerald-600">Address verified and ready</span>
                        <br>
                        <span class="text-xs text-blue-600 mt-1 block">Note: A new address (${data.pendingAddress.substring(0, 20)}...) is pending verification</span>
                    `;
                    addressStatus.className = 'text-sm mt-1';
                } else {
                    addressStatus.textContent = 'Address verified and ready';
                    addressStatus.className = 'text-sm text-emerald-600 mt-1';
                }
            } else if (data.address && !isActive) {
                // 地址存在但未激活（首次创建的情况）
                addressInput.value = '';
                addressStatus.textContent = 'Address update pending verification. Please verify the address first.';
                addressStatus.className = 'text-sm text-amber-600 mt-1';
            } else {
                addressInput.value = '';
                addressStatus.textContent = 'No address configured. Please add an address first.';
                addressStatus.className = 'text-sm text-amber-600 mt-1';
            }
        } else {
            addressInput.value = '';
            addressStatus.textContent = 'No address configured. Please add an address first.';
            addressStatus.className = 'text-sm text-amber-600 mt-1';
        }
    } catch (error) {
        console.error('Failed to load address:', error);
        addressInput.value = '';
        addressStatus.textContent = 'Failed to load address. Please try again.';
        addressStatus.className = 'text-sm text-red-600 mt-1';
    }
}

// Handle withdrawal submit
async function handleWithdrawalSubmit(event) {
    event.preventDefault();
    
    const currency = document.getElementById('withdrawalCurrency').value;
    const amount = parseFloat(document.getElementById('withdrawalAmount').value);
    const address = document.getElementById('withdrawalAddress').value;
    
    if (!currency) {
        alert('Please select a currency');
        return;
    }
    
    if (!amount || amount <= 0) {
        alert('Please enter a valid amount');
        return;
    }
    
    if (!address) {
        alert('Please configure a withdrawal address first');
        return;
    }
    
    const withdrawable = balanceData?.withdrawableBalance || 0;
    if (amount > withdrawable) {
        alert(`Insufficient balance. Available: ${formatCurrency(withdrawable)}`);
        return;
    }
    
    if (!confirm(`Confirm withdrawal request:\n\nCurrency: ${currency}\nAmount: ${formatCurrency(amount)}\nAddress: ${address}\n\nContinue?`)) {
        return;
    }
    
    try {
        const formData = new URLSearchParams();
        formData.append('currency', currency);
        formData.append('amount', amount.toString());
        
        const response = await API.post('/api/mall/vendor/withdrawal/request', formData.toString(), {
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            rawBody: true
        });
        
        if (response.code === 200) {
            alert('Withdrawal request submitted successfully!');
            // Reset form
            document.getElementById('withdrawalForm').reset();
            document.getElementById('withdrawalAddress').value = '';
            document.getElementById('addressStatus').textContent = 'Please select a currency first';
            // Reload data
            loadBalance();
            loadWithdrawalHistory();
            // Switch to history tab
            switchTab('history');
        } else {
            alert('Failed to submit withdrawal request: ' + (response.msg || 'Unknown error'));
        }
    } catch (error) {
        console.error('Failed to submit withdrawal:', error);
        alert('Failed to submit withdrawal request: ' + error.message);
    }
}

// Update address
async function updateAddress(currency) {
    const modal = document.getElementById('addressUpdateModal');
    const content = document.getElementById('addressUpdateContent');
    
    const currencyMap = {
        'BTC': 'Bitcoin',
        'XMR': 'Monero',
        'USDT_TRX': 'USDT (TRON)',
        'USDT_ERC': 'USDT (ERC20)'
    };
    
    content.innerHTML = `
        <div>
            <p class="text-gray-600 mb-4">Update withdrawal address for <strong>${currencyMap[currency] || currency}</strong>. This requires PGP verification.</p>
            <div class="mb-4">
                <label class="block text-sm font-medium text-gray-700 mb-2">New Address</label>
                <input type="text" id="newAddressInput" class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent" placeholder="Enter new address" required>
            </div>
            <div class="flex space-x-3">
                <button onclick="submitAddressUpdate('${currency}')" class="flex-1 bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700 transition-colors">
                    Request Update
                </button>
                <button onclick="closeAddressModal()" class="flex-1 bg-gray-200 text-gray-700 px-4 py-2 rounded-lg hover:bg-gray-300 transition-colors">
                    Cancel
                </button>
            </div>
        </div>
    `;
    
    modal.classList.add('active');
}

// Submit address update
async function submitAddressUpdate(currency) {
    const newAddress = document.getElementById('newAddressInput').value.trim();
    
    if (!newAddress) {
        alert('Please enter a new address');
        return;
    }
    
    try {
        const formData = new URLSearchParams();
        formData.append('currency', currency);
        formData.append('newAddress', newAddress);
        
        const response = await API.post('/api/mall/vendor/withdrawal/address/update', formData.toString(), {
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            rawBody: true
        });
        
        if (response.code === 200 && response.data) {
            const content = document.getElementById('addressUpdateContent');
            const verificationMethod = response.data.verificationMethod || 'PLAINTEXT';
            const encryptedMessage = response.data.encryptedMessage;
            const verificationCode = response.data.verificationCode;
            
            if (verificationMethod === 'PGP' && encryptedMessage) {
                // PGP加密模式：显示加密消息
                content.innerHTML = `
                    <div>
                        <div class="mb-4 p-3 bg-blue-50 border border-blue-200 rounded-lg">
                            <p class="text-sm text-blue-800 font-medium mb-1">🔐 PGP Verification Required</p>
                            <p class="text-xs text-blue-600">The verification code has been encrypted with your PGP public key. Please decrypt the message below using your PGP private key.</p>
                        </div>
                        
                        <div class="mb-4">
                            <label class="block text-sm font-medium text-gray-700 mb-2">Encrypted PGP Message:</label>
                            <div class="relative">
                                <textarea id="pgpMessageDisplay" readonly rows="12" 
                                    class="w-full px-4 py-3 border border-gray-300 rounded-lg bg-gray-50 font-mono text-xs"
                                    style="font-family: 'Courier New', monospace; white-space: pre-wrap; word-break: break-all;">${encryptedMessage}</textarea>
                                <button onclick="copyPgpMessage()" 
                                    class="absolute top-2 right-2 px-3 py-1 bg-gray-600 text-white text-xs rounded hover:bg-gray-700 transition-colors">
                                    Copy
                                </button>
                            </div>
                            <p class="text-xs text-gray-500 mt-1">Copy the encrypted message and decrypt it using your PGP private key.</p>
                        </div>
                        
                        <div class="mb-4">
                            <label class="block text-sm font-medium text-gray-700 mb-2">Decrypted Verification Code:</label>
                            <input type="text" id="verificationCodeInput" 
                                class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent" 
                                placeholder="Enter the decrypted verification code" required>
                            <p class="text-xs text-gray-500 mt-1">After decrypting the PGP message above, enter the verification code here.</p>
                        </div>
                        
                        <div class="flex space-x-3">
                            <button onclick="verifyAddressUpdate('${currency}')" 
                                class="flex-1 bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700 transition-colors">
                                Verify & Update
                            </button>
                            <button onclick="closeAddressModal()" 
                                class="flex-1 bg-gray-200 text-gray-700 px-4 py-2 rounded-lg hover:bg-gray-300 transition-colors">
                                Cancel
                            </button>
                        </div>
                    </div>
                `;
            } else if (verificationCode) {
                // 明文模式（fallback，用于测试或没有PGP公钥的情况）
                content.innerHTML = `
                    <div>
                        <div class="mb-4 p-3 bg-yellow-50 border border-yellow-200 rounded-lg">
                            <p class="text-sm text-yellow-800 font-medium mb-1">⚠️ Plaintext Verification</p>
                            <p class="text-xs text-yellow-600">PGP encryption is not available. Using plaintext verification code.</p>
                        </div>
                        
                        <div class="mb-4">
                            <label class="block text-sm font-medium text-gray-700 mb-2">Verification Code:</label>
                            <div class="flex items-center space-x-2">
                                <input type="text" id="verificationCodeInput" 
                                    value="${verificationCode}" readonly
                                    class="flex-1 px-4 py-2 border border-gray-300 rounded-lg bg-gray-50 font-mono text-lg font-bold text-center">
                                <button onclick="copyVerificationCode()" 
                                    class="px-3 py-2 bg-gray-600 text-white text-sm rounded hover:bg-gray-700 transition-colors">
                                    Copy
                                </button>
                            </div>
                        </div>
                        
                        <div class="mb-4">
                            <p class="text-sm text-gray-600">Please enter this verification code to confirm the address update:</p>
                            <input type="text" id="verificationCodeConfirm" 
                                class="w-full mt-2 px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent" 
                                placeholder="Re-enter verification code to confirm" required>
                        </div>
                        
                        <div class="flex space-x-3">
                            <button onclick="verifyAddressUpdatePlaintext('${currency}')" 
                                class="flex-1 bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700 transition-colors">
                                Verify & Update
                            </button>
                            <button onclick="closeAddressModal()" 
                                class="flex-1 bg-gray-200 text-gray-700 px-4 py-2 rounded-lg hover:bg-gray-300 transition-colors">
                                Cancel
                            </button>
                        </div>
                    </div>
                `;
            } else {
                alert('Address update request submitted. Please check your email for verification instructions.');
                closeAddressModal();
                // 不要立即重新加载地址列表，因为新地址还未验证，不应该显示
                // loadAddresses(); // 注释掉，避免显示未激活的地址
            }
        } else {
            alert('Failed to request address update: ' + (response.msg || 'Unknown error'));
        }
    } catch (error) {
        console.error('Failed to update address:', error);
        alert('Failed to update address: ' + error.message);
    }
}

// Verify address update
async function verifyAddressUpdate(currency) {
    const verificationCode = document.getElementById('verificationCodeInput').value.trim();
    
    if (!verificationCode) {
        alert('Please enter the verification code');
        return;
    }
    
    try {
        const formData = new URLSearchParams();
        formData.append('currency', currency);
        formData.append('verificationCode', verificationCode);
        
        const response = await API.post('/api/mall/vendor/withdrawal/address/verify', formData.toString(), {
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            rawBody: true
        });
        
        if (response.code === 200) {
            alert('Address updated successfully!');
            closeAddressModal();
            loadAddresses();
            // If on withdraw tab, reload address
            if (currentTab === 'withdraw') {
                const currencySelect = document.getElementById('withdrawalCurrency');
                if (currencySelect.value) {
                    handleCurrencyChange({ target: currencySelect });
                }
            }
        } else {
            alert('Verification failed: ' + (response.msg || 'Invalid verification code'));
        }
    } catch (error) {
        console.error('Failed to verify address:', error);
        alert('Failed to verify address: ' + error.message);
    }
}

// View request detail
async function viewRequestDetail(requestId) {
    const request = withdrawalRequests.find(r => r.id == requestId);
    if (!request) {
        alert('Request not found');
        return;
    }
    
    const modal = document.getElementById('requestDetailModal');
    const content = document.getElementById('requestDetailContent');
    
    content.innerHTML = `
        <div class="space-y-4">
            <div class="grid grid-cols-2 gap-4">
                <div>
                    <p class="text-sm text-gray-500">Request Code</p>
                    <p class="font-semibold text-gray-900">${request.requestCode || 'N/A'}</p>
                </div>
                <div>
                    <p class="text-sm text-gray-500">Status</p>
                    <p><span class="status-badge ${getStatusClass(request.requestStatus)}">${request.requestStatus}</span></p>
                </div>
                <div>
                    <p class="text-sm text-gray-500">Currency</p>
                    <p class="font-semibold text-gray-900">${request.currency}</p>
                </div>
                <div>
                    <p class="text-sm text-gray-500">Amount</p>
                    <p class="font-semibold text-gray-900">${formatCurrency(request.amount)}</p>
                </div>
                <div>
                    <p class="text-sm text-gray-500">Withdrawal Address</p>
                    <p class="font-mono text-sm text-gray-900 break-all">${request.withdrawalAddress || 'N/A'}</p>
                </div>
                <div>
                    <p class="text-sm text-gray-500">Request Time</p>
                    <p class="text-gray-900">${formatDateTime(request.createTime)}</p>
                </div>
            </div>
            ${request.approveTime ? `
                <div class="border-t pt-4">
                    <p class="text-sm text-gray-500 mb-2">Approval Information</p>
                    <div class="grid grid-cols-2 gap-4">
                        <div>
                            <p class="text-sm text-gray-500">Approved By</p>
                            <p class="text-gray-900">${request.approveBy || 'N/A'}</p>
                        </div>
                        <div>
                            <p class="text-sm text-gray-500">Approval Time</p>
                            <p class="text-gray-900">${formatDateTime(request.approveTime)}</p>
                        </div>
                        ${request.approveRemark ? `
                            <div class="col-span-2">
                                <p class="text-sm text-gray-500">Remark</p>
                                <p class="text-gray-900">${request.approveRemark}</p>
                            </div>
                        ` : ''}
                    </div>
                </div>
            ` : ''}
            ${request.txHash ? `
                <div class="border-t pt-4">
                    <p class="text-sm text-gray-500 mb-2">Transaction Information</p>
                    <div class="grid grid-cols-2 gap-4">
                        <div>
                            <p class="text-sm text-gray-500">Transaction Hash</p>
                            <p class="font-mono text-sm text-gray-900 break-all">${request.txHash}</p>
                        </div>
                        ${request.txFee ? `
                            <div>
                                <p class="text-sm text-gray-500">Transaction Fee</p>
                                <p class="text-gray-900">${formatCurrency(request.txFee)}</p>
                            </div>
                        ` : ''}
                        ${request.txTime ? `
                            <div>
                                <p class="text-sm text-gray-500">Transaction Time</p>
                                <p class="text-gray-900">${formatDateTime(request.txTime)}</p>
                            </div>
                        ` : ''}
                    </div>
                </div>
            ` : ''}
            ${request.rejectReason ? `
                <div class="border-t pt-4">
                    <p class="text-sm text-gray-500 mb-2">Rejection Reason</p>
                    <p class="text-gray-900">${request.rejectReason}</p>
                </div>
            ` : ''}
        </div>
    `;
    
    modal.classList.add('active');
}

// Switch tab
function switchTab(tab) {
    currentTab = tab;
    
    // Update tab buttons
    document.querySelectorAll('.tab-button').forEach(btn => {
        btn.classList.remove('active', 'text-blue-600', 'border-blue-600');
        btn.classList.add('text-gray-500');
    });
    
    const activeButton = document.getElementById(`tab${tab.charAt(0).toUpperCase() + tab.slice(1)}`);
    if (activeButton) {
        activeButton.classList.add('active', 'text-blue-600', 'border-blue-600');
        activeButton.classList.remove('text-gray-500');
    }
    
    // Update tab content
    document.querySelectorAll('.tab-content').forEach(content => {
        content.classList.add('hidden');
    });
    
    const activeContent = document.getElementById(`tabContent${tab.charAt(0).toUpperCase() + tab.slice(1)}`);
    if (activeContent) {
        activeContent.classList.remove('hidden');
    }
}

// Close address modal
function closeAddressModal() {
    document.getElementById('addressUpdateModal').classList.remove('active');
}

// Close request modal
function closeRequestModal() {
    document.getElementById('requestDetailModal').classList.remove('active');
}

// Utility functions
function formatCurrency(amount) {
    if (amount == null || amount === undefined) return '$0.00';
    return '$' + parseFloat(amount).toFixed(2);
}

function formatDateTime(dateString) {
    if (!dateString) return 'N/A';
    const date = new Date(dateString);
    return date.toLocaleString('en-US', {
        year: 'numeric',
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
    });
}

function getStatusClass(status) {
    const statusMap = {
        'PENDING': 'status-pending',
        'APPROVED': 'status-approved',
        'PROCESSING': 'status-processing',
        'COMPLETED': 'status-completed',
        'REJECTED': 'status-rejected',
        'FAILED': 'status-failed'
    };
    return statusMap[status] || 'status-pending';
}

function showLoading() {
    document.getElementById('loadingState').classList.remove('hidden');
    document.getElementById('errorState').classList.add('hidden');
    document.getElementById('mainContent').classList.add('hidden');
}

function hideLoading() {
    document.getElementById('loadingState').classList.add('hidden');
}

function showError(message) {
    document.getElementById('errorMessage').textContent = message;
    document.getElementById('errorState').classList.remove('hidden');
    document.getElementById('loadingState').classList.add('hidden');
}

// Copy PGP encrypted message to clipboard
function copyPgpMessage() {
    const pgpMessageDisplay = document.getElementById('pgpMessageDisplay');
    if (pgpMessageDisplay) {
        pgpMessageDisplay.select();
        document.execCommand('copy');
        
        // Show feedback
        const button = event.target;
        const originalText = button.textContent;
        button.textContent = 'Copied!';
        button.classList.add('bg-green-600');
        button.classList.remove('bg-gray-600');
        
        setTimeout(() => {
            button.textContent = originalText;
            button.classList.remove('bg-green-600');
            button.classList.add('bg-gray-600');
        }, 2000);
    }
}

// Copy verification code to clipboard
function copyVerificationCode() {
    const verificationCodeInput = document.getElementById('verificationCodeInput');
    if (verificationCodeInput) {
        verificationCodeInput.select();
        document.execCommand('copy');
        
        // Show feedback
        const button = event.target;
        const originalText = button.textContent;
        button.textContent = 'Copied!';
        button.classList.add('bg-green-600');
        button.classList.remove('bg-gray-600');
        
        setTimeout(() => {
            button.textContent = originalText;
            button.classList.remove('bg-green-600');
            button.classList.add('bg-gray-600');
        }, 2000);
    }
}

// Verify address update with plaintext verification code
async function verifyAddressUpdatePlaintext(currency) {
    const verificationCodeInput = document.getElementById('verificationCodeInput');
    const verificationCodeConfirm = document.getElementById('verificationCodeConfirm');
    
    if (!verificationCodeInput || !verificationCodeConfirm) {
        alert('Please enter the verification code');
        return;
    }
    
    const originalCode = verificationCodeInput.value.trim();
    const confirmCode = verificationCodeConfirm.value.trim();
    
    if (!originalCode || !confirmCode) {
        alert('Please enter the verification code');
        return;
    }
    
    if (originalCode !== confirmCode) {
        alert('Verification codes do not match. Please re-enter the code.');
        verificationCodeConfirm.value = '';
        verificationCodeConfirm.focus();
        return;
    }
    
    // Use the same verification function
    await verifyAddressUpdate(currency);
}

// Export functions for global use
window.copyPgpMessage = copyPgpMessage;
window.copyVerificationCode = copyVerificationCode;
window.verifyAddressUpdatePlaintext = verifyAddressUpdatePlaintext;

// Close modals when clicking outside
document.addEventListener('click', function(event) {
    const addressModal = document.getElementById('addressUpdateModal');
    const requestModal = document.getElementById('requestDetailModal');
    
    if (event.target === addressModal) {
        closeAddressModal();
    }
    if (event.target === requestModal) {
        closeRequestModal();
    }
});


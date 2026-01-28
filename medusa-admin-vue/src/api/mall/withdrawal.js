import request from '@/utils/request'

// ========================================
// Vendor Withdrawal Request APIs
// ========================================

// 查询所有提现请求
export function getAllRequests(query) {
  return request({
    url: '/mall/vendor/withdrawal/requests',
    method: 'get',
    params: query
  })
}

// 查询待审批的提现请求
export function getPendingRequests() {
  return request({
    url: '/mall/vendor/withdrawal/requests/pending',
    method: 'get'
  })
}

// 根据ID查询提现请求
export function getRequestById(id) {
  return request({
    url: `/mall/vendor/withdrawal/requests/${id}`,
    method: 'get'
  })
}

// 批准提现请求
export function approveRequest(id, remark) {
  return request({
    url: `/mall/vendor/withdrawal/requests/${id}/approve`,
    method: 'post',
    params: { remark }
  })
}

// 拒绝提现请求
export function rejectRequest(id, reason) {
  return request({
    url: `/mall/vendor/withdrawal/requests/${id}/reject`,
    method: 'post',
    params: { reason }
  })
}

// 标记提现为处理中
export function markProcessing(id, txHash) {
  return request({
    url: `/mall/vendor/withdrawal/requests/${id}/processing`,
    method: 'post',
    params: { txHash }
  })
}

// 标记提现为完成
export function markCompleted(id, txFee) {
  return request({
    url: `/mall/vendor/withdrawal/requests/${id}/complete`,
    method: 'post',
    params: { txFee }
  })
}

// 标记提现为失败
export function markFailed(id, reason) {
  return request({
    url: `/mall/vendor/withdrawal/requests/${id}/fail`,
    method: 'post',
    params: { reason }
  })
}

// ========================================
// Vendor Balance APIs
// ========================================

// 查询vendor余额
export function getVendorBalance(vendorId) {
  return request({
    url: `/mall/vendor/withdrawal/balance/${vendorId}`,
    method: 'get'
  })
}

// 查询vendor余额变动日志
export function getBalanceLogs(vendorId) {
  return request({
    url: `/mall/vendor/withdrawal/balance/${vendorId}/logs`,
    method: 'get'
  })
}

// ========================================
// Statistics APIs
// ========================================

// 获取提现统计信息
export function getStatistics() {
  return request({
    url: '/mall/vendor/withdrawal/statistics',
    method: 'get'
  })
}

// 手动触发余额释放
export function releasePendingBalance() {
  return request({
    url: '/mall/vendor/withdrawal/balance/release',
    method: 'post'
  })
}

// ========================================
// Wallet Address Management APIs (Super Admin)
// ========================================

// 获取vendor的提现地址列表
export function getVendorAddresses(vendorId) {
  return request({
    url: `/mall/vendor/withdrawal/addresses/${vendorId}`,
    method: 'get'
  })
}

// 更新vendor的提现地址 (Super Admin only)
export function updateWalletAddress(vendorId, currency, newAddress) {
  return request({
    url: '/admin/mall/vendor/withdrawal/addresses/update',
    method: 'post',
    params: { vendorId, currency, newAddress }
  })
}


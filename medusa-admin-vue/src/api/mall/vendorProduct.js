import request from '@/utils/request'

// Query vendor product approval list
export function listVendorProducts(query) {
  return request({
    url: '/admin/mall/vendor/product/list',
    method: 'get',
    params: query
  })
}

// Query vendor product pending approval list
export function listPendingProducts(query) {
  return request({
    url: '/admin/mall/vendor/product/pending',
    method: 'get',
    params: query
  })
}

// Get vendor product detail
export function getVendorProduct(id) {
  return request({
    url: '/admin/mall/vendor/product/' + id,
    method: 'get'
  })
}

// Approve vendor product
export function approveProduct(id, notes) {
  return request({
    url: '/admin/mall/vendor/product/approve/' + id,
    method: 'post',
    params: { notes }
  })
}

// Reject vendor product
export function rejectProduct(id, reason) {
  return request({
    url: '/admin/mall/vendor/product/reject/' + id,
    method: 'post',
    params: { reason }
  })
}

// Batch approve vendor products
export function batchApproveProducts(ids, notes) {
  return request({
    url: '/admin/mall/vendor/product/batch-approve',
    method: 'post',
    data: { ids, notes }
  })
}

// Batch reject vendor products
export function batchRejectProducts(ids, reason) {
  return request({
    url: '/admin/mall/vendor/product/batch-reject',
    method: 'post',
    data: { ids, reason }
  })
}

// Export vendor product list
export function exportVendorProducts(query) {
  return request({
    url: '/admin/mall/vendor/product/export',
    method: 'get',
    params: query
  })
}

// Get vendor product statistics
export function getProductStats() {
  return request({
    url: '/admin/mall/vendor/product/stats',
    method: 'get'
  })
}


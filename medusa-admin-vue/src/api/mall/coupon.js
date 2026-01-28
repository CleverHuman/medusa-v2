import request from '@/utils/request'

// Query coupon list
export function listCoupon(query) {
  return request({
    url: '/admin/mall/coupon/list',
    method: 'get',
    params: query
  })
}

// Query coupon details
export function getCoupon(couponId) {
  return request({
    url: '/admin/mall/coupon/' + couponId,
    method: 'get'
  })
}

// Add coupon
export function addCoupon(data) {
  return request({
    url: '/admin/mall/coupon',
    method: 'post',
    data: data
  })
}

// Bulk add coupons
export function bulkAddCoupon(data) {
  return request({
    url: '/admin/mall/coupon/bulk',
    method: 'post',
    data: data
  })
}

// Update coupon
export function updateCoupon(data) {
  return request({
    url: '/admin/mall/coupon',
    method: 'put',
    data: data
  })
}

// Delete coupon
export function delCoupon(couponId) {
  return request({
    url: '/admin/mall/coupon/' + couponId,
    method: 'delete'
  })
}

// Export coupon
export function exportCoupon(query) {
  return request({
    url: '/admin/mall/coupon/export',
    method: 'get',
    params: query
  })
}

import request from '@/utils/request'

// ==================== Vendor Application API ====================

// Query vendor application list
export function listVendorApplication(query) {
  return request({
    url: '/admin/mall/vendor/application/list',
    method: 'get',
    params: query
  })
}

// Query vendor application details
export function getVendorApplication(id) {
  return request({
    url: '/admin/mall/vendor/application/' + id,
    method: 'get'
  })
}

// Add vendor application
export function addVendorApplication(data) {
  return request({
    url: '/admin/mall/vendor/application',
    method: 'post',
    data: data
  })
}

// Update vendor application
export function updateVendorApplication(data) {
  return request({
    url: '/admin/mall/vendor/application',
    method: 'put',
    data: data
  })
}

// Delete vendor application
export function delVendorApplication(id) {
  return request({
    url: '/admin/mall/vendor/application/' + id,
    method: 'delete'
  })
}

// Approve application
export function approveApplication(id, notes) {
  return request({
    url: '/admin/mall/vendor/application/approve/' + id,
    method: 'post',
    params: { notes }
  })
}

// Reject application
export function rejectApplication(id, notes) {
  return request({
    url: '/admin/mall/vendor/application/reject/' + id,
    method: 'post',
    params: { notes }
  })
}

// Update review progress
export function updateReviewProgress(id, stage, progress) {
  return request({
    url: '/admin/mall/vendor/application/progress/' + id,
    method: 'post',
    params: { stage, progress }
  })
}

// Start review - change status from pending to under_review
export function startReview(id) {
  return request({
    url: '/admin/mall/vendor/application/startReview/' + id,
    method: 'post'
  })
}

// Require interview
export function requireInterview(id, notes) {
  return request({
    url: '/admin/mall/vendor/application/interview/' + id,
    method: 'post',
    params: { notes }
  })
}

// Export vendor application list
export function exportVendorApplication(query) {
  return request({
    url: '/admin/mall/vendor/application/export',
    method: 'post',
    params: query
  })
}

// ==================== Vendor Management API ====================

// Query vendor list
export function listVendor(query) {
  return request({
    url: '/admin/mall/vendor/list',
    method: 'get',
    params: query
  })
}

// Query vendor details
export function getVendor(id) {
  return request({
    url: '/admin/mall/vendor/' + id,
    method: 'get'
  })
}

// Add vendor
export function addVendor(data) {
  return request({
    url: '/admin/mall/vendor',
    method: 'post',
    data: data
  })
}

// Update vendor
export function updateVendor(data) {
  return request({
    url: '/admin/mall/vendor',
    method: 'put',
    data: data
  })
}

// Delete vendor
export function delVendor(id) {
  return request({
    url: '/admin/mall/vendor/' + id,
    method: 'delete'
  })
}

// Export vendor list
export function exportVendor(query) {
  return request({
    url: '/admin/mall/vendor/export',
    method: 'post',
    params: query
  })
}

// Update vendor bond and level
export function updateVendorBondAndLevel(id, bond, level, salesPoints) {
  return request({
    url: '/admin/mall/vendor/' + id + '/bond-level',
    method: 'put',
    params: { bond, level, salesPoints }
  })
}

// Create warehouse staff account for vendor
export function createWarehouseAccount(vendorId, username, password, email) {
  return request({
    url: '/admin/mall/vendor/' + vendorId + '/warehouse-account',
    method: 'post',
    params: { username, password, email }
  })
}

// Get vendor withdrawal addresses
export function getVendorWithdrawalAddresses(vendorId) {
  return request({
    url: '/admin/mall/vendor/withdrawal/addresses/' + vendorId,
    method: 'get'
  })
}

// ==================== Interview Management API ====================

// Query interview list
export function listInterview(query) {
  return request({
    url: '/admin/mall/vendor/interview/list',
    method: 'get',
    params: query
  })
}

// Get calendar data
export function getCalendarData(startDate, endDate, status) {
  return request({
    url: '/admin/mall/vendor/interview/calendar',
    method: 'get',
    params: { startDate, endDate, status }
  })
}

// Query interview details
export function getInterview(id) {
  return request({
    url: '/admin/mall/vendor/interview/' + id,
    method: 'get'
  })
}

// Get interviews by application ID
export function getInterviewsByApplication(applicationId) {
  return request({
    url: '/admin/mall/vendor/interview/application/' + applicationId,
    method: 'get'
  })
}

// Schedule interview
export function scheduleInterview(data) {
  return request({
    url: '/admin/mall/vendor/interview',
    method: 'post',
    data: data
  })
}

// Update interview
export function updateInterview(data) {
  return request({
    url: '/admin/mall/vendor/interview',
    method: 'put',
    data: data
  })
}

// Reschedule interview
export function rescheduleInterview(id, newDatetime, reason) {
  return request({
    url: '/admin/mall/vendor/interview/reschedule/' + id,
    method: 'post',
    params: { newDatetime, reason }
  })
}

// Confirm interview (by vendor)
export function confirmInterview(id) {
  return request({
    url: '/admin/mall/vendor/interview/confirm/' + id,
    method: 'post'
  })
}

// Complete interview
export function completeInterview(id, result, score, notes) {
  return request({
    url: '/admin/mall/vendor/interview/complete/' + id,
    method: 'post',
    params: { result, score, notes }
  })
}

// Cancel interview
export function cancelInterview(id, reason) {
  return request({
    url: '/admin/mall/vendor/interview/cancel/' + id,
    method: 'post',
    params: { reason }
  })
}

// Delete interview
export function delInterview(id) {
  return request({
    url: '/admin/mall/vendor/interview/' + id,
    method: 'delete'
  })
}

// ==================== Interview Slot Management API ====================

// Query slot list
export function listInterviewSlots(query) {
  return request({
    url: '/admin/mall/vendor/interview/slots',
    method: 'get',
    params: query
  })
}

// Create single slot
export function createInterviewSlot(data) {
  return request({
    url: '/admin/mall/vendor/interview/slots',
    method: 'post',
    data: data
  })
}

// Batch create slots
export function batchCreateInterviewSlots(params) {
  return request({
    url: '/admin/mall/vendor/interview/slots/batch',
    method: 'post',
    params: params
  })
}

// Update slot
export function updateInterviewSlot(data) {
  return request({
    url: '/admin/mall/vendor/interview/slots',
    method: 'put',
    data: data
  })
}

// Delete slot(s)
export function deleteInterviewSlot(ids) {
  return request({
    url: '/admin/mall/vendor/interview/slots/' + ids,
    method: 'delete'
  })
}

// Release reserved slot
export function releaseInterviewSlot(slotId) {
  return request({
    url: `/admin/mall/vendor/interview/slots/${slotId}/release`,
    method: 'post'
  })
}

// ==================== Vendor Wallet Verification API ====================

// Verify vendor wallets
export function verifyVendorWallets(data) {
  return request({
    url: '/admin/mall/vendor/application/verify-wallets',
    method: 'post',
    data: data
  })
}


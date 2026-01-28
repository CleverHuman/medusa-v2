import request from '@/utils/request'

// Submit vendor application (public API)
export function submitVendorApplication(data) {
  return request({
    url: '/api/mall/vendor/application/submit',
    method: 'post',
    data: data
  })
}

// Query application status by Application ID (public API)
export function getApplicationStatus(applicationId) {
  return request({
    url: '/api/mall/vendor/application/status/' + applicationId,
    method: 'get'
  })
}

// Get application details by Application ID (public API)
export function getApplicationDetails(applicationId) {
  return request({
    url: '/api/mall/vendor/application/detail/' + applicationId,
    method: 'get'
  })
}

// Save application draft to localStorage
export function saveDraft(draftData) {
  try {
    localStorage.setItem('vendorApplicationDraft', JSON.stringify(draftData))
    return true
  } catch (e) {
    console.error('Failed to save draft:', e)
    return false
  }
}

// Load application draft from localStorage
export function loadDraft() {
  try {
    const draft = localStorage.getItem('vendorApplicationDraft')
    return draft ? JSON.parse(draft) : null
  } catch (e) {
    console.error('Failed to load draft:', e)
    return null
  }
}

// Clear application draft
export function clearDraft() {
  try {
    localStorage.removeItem('vendorApplicationDraft')
    return true
  } catch (e) {
    console.error('Failed to clear draft:', e)
    return false
  }
}

// ==================== Interview API ====================

// Get interviews by application number (public API)
export function getInterviewsByApplicationNumber(applicationNumber) {
  return request({
    url: '/api/mall/vendor/interview/application/' + applicationNumber,
    method: 'get'
  })
}

// Confirm interview (by vendor)
export function confirmInterview(interviewId) {
  return request({
    url: '/api/mall/vendor/interview/confirm/' + interviewId,
    method: 'post'
  })
}

import request from "@/utils/request";

// Query order list
export function listOrder(query) {
  return request({
    url: '/admin/mall/order/list',
    method: 'get',
    params: query
  })
}

// Query order details
export function getOrder(orderId) {
  return request({
    url: '/admin/mall/order/' + orderId,
    method: 'get'
  })
}

// Update order
export function updateOrder(data) {
  return request({
    url: '/admin/mall/order',
    method: 'put',
    data: data
  })
}

// Delete order
export function delOrder(orderId) {
  return request({
    url: '/admin/mall/order/' + orderId,
    method: 'delete'
  })
}

// Export order
export function exportOrder(query) {
  return request({
    url: '/admin/mall/order/export',
    method: 'get',
    params: query
  })
}

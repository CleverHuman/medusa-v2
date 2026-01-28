import request from '@/utils/request'

export function listVOrder(query) {
  return request({
    url: '/admin/mall/vorder/list',
    method: 'get',
    params: query
  })
}

// 导出订单
export function exportVOrder(query) {
  return request({
    url: '/admin/mall/vorder/export',
    method: 'get',
    params: query,
    responseType: 'blob'
  })
}

export function exportVOrderAcc(query) {
  return request({
    url: '/admin/mall/vorder/export/acc',
    method: 'get',
    params: query,
    responseType: 'blob'
  })
}

export function exportVOrderPh(query) {
  return request({
    url: '/admin/mall/vorder/export/ph',
    method: 'get',
    params: query,
    responseType: 'blob'
  })
}
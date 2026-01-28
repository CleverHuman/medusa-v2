import request from '@/utils/request'

// 获取订单列表
export function listOrder(query) {
  return request({
    url: '/api/mall/order/list',
    method: 'get',
    params: query
  })
}

// 创建订单
export function addOrder(data) {
  return request({
    url: '/api/mall/order/add',
    method: 'post',
    data: data
  })
}

// 插入订单
export function insertOrder(data) {
  return request({
    url: '/api/mall/order/insert',
    method: 'post',
    data: data
  })
}

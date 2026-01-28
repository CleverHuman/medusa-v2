import request from '@/utils/request'

// 查询配送方式列表
export function listShipping(query) {
  return request({
    url: '/admin/mall/shipping/list',
    method: 'get',
    params: query
  })
}

// 查询配送方式详细
export function getShipping(id) {
  return request({
    url: '/admin/mall/shipping/' + id,
    method: 'get'
  })
}

// 新增配送方式
export function addShipping(data) {
  return request({
    url: '/admin/mall/shipping',
    method: 'post',
    data: data
  })
}

// 修改配送方式
export function updateShipping(data) {
  return request({
    url: '/admin/mall/shipping',
    method: 'put',
    data: data
  })
}

// 删除配送方式
export function delShipping(codes) {
  return request({
    url: '/admin/mall/shipping/' + codes,
    method: 'delete'
  })
}

// 获取所有激活的配送方式
export function getActiveShipping() {
  return request({
    url: '/admin/mall/shipping/active',
    method: 'get'
  })
}

import request from '@/utils/request'

// 查询商品列表
export function listProduct2(query) {
  return request({
    url: '/mall/product2/list',
    method: 'get',
    params: query
  })
}

// 查询商品详细
export function getProduct2(id) {
  return request({
    url: '/mall/product2/' + id,
    method: 'get'
  })
}

// 新增商品
export function addProduct2(data) {
  return request({
    url: '/mall/product2',
    method: 'post',
    data: data
  })
}

// 修改商品
export function updateProduct2(data) {
  return request({
    url: '/mall/product2',
    method: 'put',
    data: data
  })
}

// 删除商品
export function delProduct2(id) {
  return request({
    url: '/mall/product2/' + id,
    method: 'delete'
  })
} 
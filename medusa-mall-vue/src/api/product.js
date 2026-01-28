import request from '@/utils/request'

// 查询产品列表（用户端 - 只显示激活的产品）
export function listProduct(query) {
  return request({
    url: '/api/mall/product/list',
    method: 'get',
    params: query
  })
}

// 获取产品详细信息
export function getProduct(id) {
  return request({
    url: '/api/mall/product/' + id,
    method: 'get'
  })
}

// 获取所有产品类别
export function getProductCategories() {
  return request({
    url: '/api/mall/product/categories',
    method: 'get'
  })
}

// 根据类别查询产品列表
export function getProductsByCategory(category) {
  return request({
    url: '/api/mall/product/category/' + category,
    method: 'get'
  })
}

import request from '@/utils/request'

// 查询产品列表
export function listProduct(query) {
  return request({
    url: '/admin/mall/product/list',
    method: 'get',
    params: query
  })
}

// 查询产品详细
export function getProduct(id) {
  return request({
    url: '/admin/mall/product/' + id,
    method: 'get'
  })
}

// 新增产品
export function addProduct(data) {
  return request({
    url: '/admin/mall/product',
    method: 'post',
    data: data
  })
}

// 修改产品
export function updateProduct(data) {
  return request({
    url: '/admin/mall/product',
    method: 'put',
    data: data
  })
}

// 删除产品
export function delProduct(id) {
  return request({
    url: '/admin/mall/product/' + id,
    method: 'delete'
  })
}

// 获取所有产品类别
export function getProductCategories() {
  return request({
    url: '/admin/mall/product/categories',
    method: 'get'
  })
}

// 根据类别查询产品列表
export function getProductsByCategory(category) {
  return request({
    url: '/admin/mall/product/category/' + category,
    method: 'get'
  })
}

// 批量更新产品排序
export function updateProductSortOrder(products) {
  return request({
    url: '/admin/mall/product/sort',
    method: 'put',
    data: products
  })
} 
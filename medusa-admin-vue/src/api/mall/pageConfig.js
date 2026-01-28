import request from '@/utils/request'

// 获取页面配置列表
export function getMallPageConfigList(query) {
  return request({
    url: '/admin/mall/page-config/list',
    method: 'get',
    params: query
  })
}

// 获取页面配置详情
export function getMallPageConfig(id) {
  return request({
    url: '/admin/mall/page-config/' + id,
    method: 'get'
  })
}

// 新增页面配置
export function addMallPageConfig(data) {
  return request({
    url: '/admin/mall/page-config',
    method: 'post',
    data: data
  })
}

// 修改页面配置
export function updateMallPageConfig(data) {
  return request({
    url: '/admin/mall/page-config',
    method: 'put',
    data: data
  })
}

// 删除页面配置
export function delMallPageConfig(ids) {
  return request({
    url: '/admin/mall/page-config/' + ids,
    method: 'delete'
  })
} 
import request from '@/utils/request'

export function listBenefit(){
  return request({
    url: '/admin/mall/benefit/list',
    method: 'get',
  })
}

export function getBenefit(levelId) {
  return request({
    url: `/admin/mall/benefit/${levelId}`,
    method: 'get'
  })
}

export function addBenefit(data) {
  return request({
    url: '/admin/mall/benefit',
    method: 'post',
    data
  })
}

export function updateBenefit(data) {
  return request({
    url: '/admin/mall/benefit',
    method: 'put',
    data
  })
}

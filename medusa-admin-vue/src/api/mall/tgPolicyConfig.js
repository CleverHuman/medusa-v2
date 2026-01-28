import request from '@/utils/request'

// 获取Telegram Policy配置
export function getTgPolicyConfig() {
  return request({
    url: '/admin/mall/tg-policy-config',
    method: 'get'
  })
}

// 保存Telegram Policy配置
export function saveTgPolicyConfig(data) {
  return request({
    url: '/admin/mall/tg-policy-config',
    method: 'post',
    data: data
  })
}

// 更新Telegram Policy配置
export function updateTgPolicyConfig(data) {
  return request({
    url: '/admin/mall/tg-policy-config',
    method: 'put',
    data: data
  })
} 
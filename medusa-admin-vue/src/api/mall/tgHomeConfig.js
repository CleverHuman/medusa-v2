import request from '@/utils/request'

// 获取Telegram首页配置
export function getTgHomeConfig() {
  return request({
    url: '/admin/mall/tg-home-config',
    method: 'get'
  })
}

// 保存Telegram首页配置
export function saveTgHomeConfig(data) {
  return request({
    url: '/admin/mall/tg-home-config',
    method: 'post',
    data: data
  })
}

// 更新Telegram首页配置
export function updateTgHomeConfig(data) {
  return request({
    url: '/admin/mall/tg-home-config',
    method: 'put',
    data: data
  })
} 
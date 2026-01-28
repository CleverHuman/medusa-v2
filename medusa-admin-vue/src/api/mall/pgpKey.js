import request from '@/utils/request'

// 查询PGP密钥列表
export function listPgpKey(query) {
  return request({
    url: '/admin/mall/pgpKey/list',
    method: 'get',
    params: query
  })
}

// 查询PGP密钥详细
export function getPgpKey(id) {
  return request({
    url: '/admin/mall/pgpKey/' + id,
    method: 'get'
  })
}

// 新增PGP密钥
export function addPgpKey(data) {
  return request({
    url: '/admin/mall/pgpKey',
    method: 'post',
    data: data
  })
}

// 修改PGP密钥
export function updatePgpKey(data) {
  return request({
    url: '/admin/mall/pgpKey',
    method: 'put',
    data: data
  })
}

// 删除PGP密钥
export function delPgpKey(id) {
  return request({
    url: '/admin/mall/pgpKey/' + id,
    method: 'delete'
  })
}

// 导出PGP密钥
export function exportPgpKey(query) {
  return request({
    url: '/admin/mall/pgpKey/export',
    method: 'post',
    data: query
  })
}

// 生成PGP密钥对
export function generatePgpKey(keyName, keySize) {
  return request({
    url: '/admin/mall/pgpKey/generate',
    method: 'post',
    params: {
      keyName: keyName,
      keySize: keySize
    }
  })
}

// 设置默认密钥
export function setDefaultPgpKey(id) {
  return request({
    url: '/admin/mall/pgpKey/setDefault/' + id,
    method: 'put'
  })
}

// 切换密钥状态
export function togglePgpKeyStatus(id, isActive) {
  return request({
    url: '/admin/mall/pgpKey/toggle/' + id + '/' + isActive,
    method: 'put'
  })
}

// 获取默认密钥
export function getDefaultPgpKey() {
  return request({
    url: '/admin/mall/pgpKey/default',
    method: 'get'
  })
}

// 获取激活的密钥列表
export function getActivePgpKeys() {
  return request({
    url: '/admin/mall/pgpKey/active',
    method: 'get'
  })
}

// 获取第一个可用的公钥
export function getFirstPublicKey() {
  return request({
    url: '/admin/mall/pgpKey/first-public-key',
    method: 'get'
  })
}

// 获取默认公钥
export function getDefaultPublicKey() {
  return request({
    url: '/admin/mall/pgpKey/default-public-key',
    method: 'get'
  })
} 
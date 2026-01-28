import request from '@/utils/request'

// 查询PGP加密地址列表
export function listPgpEncryptedAddress(query) {
  return request({
    url: '/admin/mall/pgpEncryptedAddress/list',
    method: 'get',
    params: query
  })
}

// 查询PGP加密地址详细
export function getPgpEncryptedAddress(id) {
  return request({
    url: '/admin/mall/pgpEncryptedAddress/' + id,
    method: 'get'
  })
}

// 新增PGP加密地址
export function addPgpEncryptedAddress(data) {
  return request({
    url: '/admin/mall/pgpEncryptedAddress',
    method: 'post',
    data: data
  })
}

// 修改PGP加密地址
export function updatePgpEncryptedAddress(data) {
  return request({
    url: '/admin/mall/pgpEncryptedAddress',
    method: 'put',
    data: data
  })
}

// 删除PGP加密地址
export function delPgpEncryptedAddress(id) {
  return request({
    url: '/admin/mall/pgpEncryptedAddress/' + id,
    method: 'delete'
  })
}

// 导出PGP加密地址
export function exportPgpEncryptedAddress(query) {
  return request({
    url: '/admin/mall/pgpEncryptedAddress/export',
    method: 'post',
    data: query
  })
}

// 根据订单ID查询加密地址
export function getPgpEncryptedAddressByOrderId(orderId) {
  return request({
    url: '/admin/mall/pgpEncryptedAddress/order/' + orderId,
    method: 'get'
  })
}

// 根据用户ID查询加密地址列表
export function getPgpEncryptedAddressByUserId(userId) {
  return request({
    url: '/admin/mall/pgpEncryptedAddress/user/' + userId,
    method: 'get'
  })
}

// 根据密钥ID查询加密地址列表
export function getPgpEncryptedAddressByKeyId(keyId) {
  return request({
    url: '/admin/mall/pgpEncryptedAddress/key/' + keyId,
    method: 'get'
  })
}

// 加密地址
export function encryptAddress(orderId, userId, address) {
  return request({
    url: '/admin/mall/pgpEncryptedAddress/encrypt',
    method: 'post',
    params: {
      orderId: orderId,
      userId: userId,
      address: address
    }
  })
}

// 解密地址
export function decryptAddress(encryptedAddress) {
  return request({
    url: '/admin/mall/pgpEncryptedAddress/decrypt',
    method: 'post',
    params: {
      encryptedAddress: encryptedAddress
    }
  })
} 
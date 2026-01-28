import request from '@/utils/request'

// 获取购物车商品列表
export function getCartItems() {
  return request({
    url: '/cart',
    method: 'get'
  })
}

// 更新购物车商品
export function updateCartItem(data) {
  return request({
    url: '/cart/update',
    method: 'post',
    data: data
  })
}

// 删除购物车商品
export function removeCartItem(id) {
  return request({
    url: '/cart/remove/' + id,
    method: 'post'
  })
}

// 应用优惠券
export function applyCoupon(couponCode) {
  return request({
    url: '/cart/applyCoupon',
    method: 'post',
    params: { couponCode }
  })
}

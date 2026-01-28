import Vue from 'vue'
import Router from 'vue-router'

Vue.use(Router)

const constantRoutes = [
  {
    path: '/',
    component: () => import('@/views/Home.vue')
  },
  {
    path: '/login',
    component: () => import('@/views/Login.vue')
  },
  {
    path: '/signup',
    component: () => import('@/views/Signup.vue')
  },
  {
    path: '/profile',
    // name: 'Profile',
    component: () => import('@/views/Profile.vue'),
    // meta: { requiresAuth: true }
  },
  {
    path: '/order',
    component: () => import('@/views/Order.vue')
  },
  {
    path: "/product/:category", // DIGITAL/PHY
    component: () => import("@/views/Product.vue"),
    name: "ProductList",
    props: true,
  },
  {
    path: '/cart',
    component: () => import('@/views/Cart.vue')
  },
  {
    path: '/order/checkout',
    component: () => import('@/views/CheckoutAddress.vue')
  },
  {
    path: '/checkout/shipping',
    component: () => import('@/views/CheckoutShipping.vue')
  },
  {
    path: '/checkout/payment',
    component: () => import('@/views/CheckoutPayment.vue')
  },
  {
    path: '/checkout/qrcode',
    component: () => import('@/views/CheckoutQRCode.vue')
  },

]

export default new Router({
  base: "api/mall",
  mode: 'history', // 去掉url中的#
  scrollBehavior: () => ({ y: 0 }),
  routes: constantRoutes
})

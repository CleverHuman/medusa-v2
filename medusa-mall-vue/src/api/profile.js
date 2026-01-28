import request from '@/utils/request'

export function getMemberInfo(){
  return request({
    url:'/getMemberInfo',
    method:'get'
  })
}

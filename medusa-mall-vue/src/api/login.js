import request from '@/utils/request'

export function register(data){
  return request({
    url:'/register',
    method:'post',
    data: data
  })
}


export function login(username,password){
  var sourceType = 0
  const data ={
    username,
    password,
    sourceType
  }
  return request({
    url:'/login',
    method:'post',
    data: data
  })
}

export function logout(){
  return request({
    url:'/logout',
    method: 'post'
  })
}

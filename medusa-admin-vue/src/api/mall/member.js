import request from '@/utils/request'


export function listMembers(query) {
  return request({
    url: '/admin/mall/member/list',
    method: 'get',
    params: query
  })
}

export function getMemberInfo(memberId) {
  return request({
    url: `/admin/mall/member/${memberId}`,
    method: 'get'
  })
}

// Alias for getMemberInfo to maintain consistency
export function getMember(memberId) {
  return getMemberInfo(memberId);
}

export function batchUpdate(ids, level) {
  return request({
    url: `/admin/mall/member/level/batchUpdate`,
    method: 'put',
    data: {
      "ids" :ids,
      "level" :level,
    }
  })
}

export function suspendMembers(ids) {
  return request({
    url: `/admin/mall/member/suspend`,
    method: 'put',
    data: {ids}
  })
}

export function resumeMembers(ids) {
  return request({
    url: `/admin/mall/member/resume`,
    method: 'put',
    data: {ids}
  })
}

export function delMembers(ids) {
  return request({
    url: `/admin/mall/member`,
    method: 'delete',
    data: {ids}
  })
}

export function addComment(memberId, comment) {
  return request({
    url: `/admin/mall/member/comment`,
    method: 'post',
    data: {
      memberId: memberId,
      remark: comment
    }
  })
}

// 获取会员积分历史记录
export function getPointHistory(memberId) {
  return request({
    url: `/api/mall/pointHistory/list/${memberId}`,
    method: 'get'
  })
}

// 添加积分历史记录
export function addPointHistory(data) {
  return request({
    url: '/api/mall/pointHistory/add',
    method: 'post',
    data: data
  })
}

// 搜索会员
export function searchMembers(username) {
  return request({
    url: '/admin/mall/member/search',
    method: 'get',
    params: { username }
  })
}

// 更新关联账号
export function updateLinkedAccount(memberId, linkedAccountId) {
  return request({
    url: '/admin/mall/member/linked-account',
    method: 'put',
    params: { memberId, linkedAccountId }
  })
}

// 删除关联账号
export function removeLinkedAccount(memberId) {
  return request({
    url: `/admin/mall/member/linked-account/${memberId}`,
    method: 'delete'
  })
}

// 根据关联账号ID获取会员信息
export function getMemberByLinkedAccount(linkedAccountId) {
  return request({
    url: `/admin/mall/member/linked-account/${linkedAccountId}`,
    method: 'get'
  })
}

// 合并两个账号
export function mergeAccounts(memberId, linkedAccountId) {
  return request({
    url: '/admin/mall/member/merge',
    method: 'post',
    params: { memberId, linkedAccountId }
  })
}

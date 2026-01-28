import request from '@/utils/request';

export function getProfileAgreement() {
  return request({
    url: '/api/page/profile/agreement',
    method: 'get'
  });
}

export function saveProfileAgreement(data) {
  return request({
    url: '/api/page/profile/agreement',
    method: 'post',
    data
  });
} 
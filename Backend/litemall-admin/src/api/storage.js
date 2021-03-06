import request from '@/utils/request'

export function listStorage(query) {
  return request({
    url: '/storage/list',
    method: 'post',
    params: query
  })
}

export function createStorage(data) {
  return request({
    url: '/storage/create',
    method: 'post',
    data
  })
}

export function readStorage(data) {
  return request({
    url: '/storage/read',
    method: 'get',
    data
  })
}

export function updateStorage(data) {
  return request({
    url: '/storage/update',
    method: 'post',
    data
  })
}

export function deleteStorage(data) {
  return request({
    url: '/storage/delete',
    method: 'post',
    data
  })
}

const uploadPath = 'http://120.79.250.63:8080/wx' + '/storage/upload'
export { uploadPath }

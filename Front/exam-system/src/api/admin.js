import request from '../utils/request'

// 获取管理员列表
export function getAdminList(params) {
  return request({
    url: '/api/admin/admins',
    method: 'get',
    params
  })
}

// 更新管理员信息
export function updateAdmin(adminId, data) {
  return request({
    url: `/api/admin/admins/${adminId}`,
    method: 'put',
    data
  })
}

// 更新管理员状态
export function updateAdminStatus(adminId, status) {
  return request({
    url: `/api/admin/admins/${adminId}/status`,
    method: 'put',
    params: { status }
  })
}

// 删除管理员
export function deleteAdmin(adminId) {
  return request({
    url: `/api/admin/admins/${adminId}`,
    method: 'delete'
  })
}

// 添加管理员
export function addAdmin(data) {
  return request({
    url: '/api/auth/register/staff',
    method: 'post',
    data
  })
}
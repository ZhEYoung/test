import request from '@/utils/request'

// 获取操作日志列表
export function getOperationLogs(params) {
  return request({
    url: '/admin/logs/operation',
    method: 'get',
    params
  })
}

// 获取登录日志列表
export function getLoginLogs(params) {
  return request({
    url: '/admin/logs/login',
    method: 'get',
    params
  })
}

// 获取异常日志列表
export function getErrorLogs(params) {
  return request({
    url: '/admin/logs/error',
    method: 'get',
    params
  })
}

// 清空操作日志
export function clearOperationLogs() {
  return request({
    url: '/admin/logs/operation',
    method: 'delete'
  })
}

// 清空登录日志
export function clearLoginLogs() {
  return request({
    url: '/admin/logs/login',
    method: 'delete'
  })
}

// 清空异常日志
export function clearErrorLogs() {
  return request({
    url: '/admin/logs/error',
    method: 'delete'
  })
}

// 导出全部日志
export function exportAllLogs(params) {
  return request({
    url: '/admin/logs/all/export',
    method: 'get',
    params,
    responseType: 'blob',
    headers: {
      'Accept': 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
    }
  })
}

// 导出异常日志
export function exportErrorLogs(params) {
  return request({
    url: '/admin/logs/error/export',
    method: 'get',
    params,
    responseType: 'blob',
    headers: {
      'Accept': 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
    }
  })
}

// 获取操作类型统计
export function getOperationTypeStats() {
  return request({
    url: '/admin/logs/stats/operation-types',
    method: 'get'
  })
}

// 获取高频操作用户
export function getFrequentUsers(params) {
  return request({
    url: '/admin/logs/stats/frequent-users',
    method: 'get',
    params
  })
}

// 获取IP访问统计
export function getIpAccessStats() {
  return request({
    url: '/admin/logs/stats/ip-access',
    method: 'get'
  })
} 
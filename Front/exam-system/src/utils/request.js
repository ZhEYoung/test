import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '../router'

console.log('Initializing request utility...')

// 创建 axios 实例
const request = axios.create({
  baseURL: '/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json;charset=UTF-8'
  }
})

// 请求拦截器
request.interceptors.request.use(
  config => {
    console.log('Request interceptor - config:', {
      method: config.method,
      url: config.url,
      baseURL: config.baseURL,
      params: config.params,
      data: config.data
    })

    // 从 localStorage 获取 token
    const token = localStorage.getItem('token')
    if (token) {
      config.headers['token'] = `${token}`
      console.log('Token added to request:', token)
    } else {
      console.warn('No token found in localStorage')
    }
    
    // 打印请求信息
    console.log('Sending Request:', config.method.toUpperCase(), (config.baseURL || '') + config.url)
    
    return config
  },
  error => {
    console.error('Request interceptor error:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  response => {
    // 如果是文件下载请求
    if (response.config.responseType === 'blob') {
      return {
        data: response.data,
        headers: response.headers
      }
    }

    console.log('Response interceptor - response:', {
      status: response.status,
      statusText: response.statusText,
      data: response.data,
      config: {
        method: response.config.method,
        url: response.config.url
      }
    })
    
    const res = response.data
    
    if (res.code !== 200) {
      console.warn('Response code is not 200:', res)
      ElMessage.error(res.message || '请求失败')
      
      if (res.code === 401) {
        console.warn('Unauthorized access, redirecting to login')
        localStorage.removeItem('token')
        router.push('/login')
      }
      
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    
    return res
  },
  error => {
    console.error('Response interceptor error:', {
      message: error.message,
      config: error.config,
      response: error.response
    })
    
    if (error.response) {
      switch (error.response.status) {
        case 401:
          console.warn('401 error, redirecting to login')
          localStorage.removeItem('token')
          router.push('/login')
          ElMessage.error('登录已过期，请重新登录')
          break
        case 403:
          console.warn('403 error - Forbidden')
          ElMessage.error('没有权限访问该资源')
          break
        case 404:
          console.warn('404 error - Not Found')
          ElMessage.error('请求的资源不存在')
          break
        case 500:
          console.warn('500 error - Server Error')
          ElMessage.error('服务器内部错误')
          break
        default:
          console.warn(`Unhandled error status: ${error.response.status}`)
          ElMessage.error(error.response.data?.message || '请求失败')
      }
    } else if (error.request) {
      console.warn('No response received from server')
      ElMessage.error('服务器无响应，请检查网络连接')
    } else {
      console.error('Request configuration error:', error.message)
      ElMessage.error('请求配置错误：' + error.message)
    }
    
    return Promise.reject(error)
  }
)

export default request 
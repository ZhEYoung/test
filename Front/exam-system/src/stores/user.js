import { defineStore } from 'pinia'
import { ref } from 'vue'
import { login, logout, getCurrentUser } from '../api/auth'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import axios from 'axios'

// 角色常量
export const ROLE_ADMIN = 0
export const ROLE_TEACHER = 1
export const ROLE_STUDENT = 2

export const useUserStore = defineStore('user', () => {
  const router = useRouter()
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(null)

  // 登录
  const loginAction = async (loginForm) => {
    try {
      const res = await login(loginForm)
      if (res.code === 200) {
        const { token: newToken, user } = res.data
        token.value = newToken
        userInfo.value = user
        localStorage.setItem('token', newToken)
        
        // 根据用户角色跳转到对应的首页
        const roleRouteMap = {
          [ROLE_ADMIN]: '/admin/dashboard',
          [ROLE_TEACHER]: '/teacher/dashboard',
          [ROLE_STUDENT]: '/student/dashboard'
        }

        const targetRoute = roleRouteMap[user.role]
        if (!targetRoute) {
          ElMessage.error('未知的用户角色')
          return false
        }

        await router.push(targetRoute)
        return true
      }
      return false
    } catch (error) {
      console.error('Login error:', error)
      return false
    }
  }

  // 登出
  const logoutAction = async () => {
    try {
      const res = await logout()
      if (res.code === 200) {
        // 清除用户状态
        token.value = ''
        userInfo.value = null
        localStorage.removeItem('token')
        
        // 跳转到登录页面
        await router.push('/login')
        ElMessage.success('已安全退出登录')
      } else {
        ElMessage.error(res.message || '退出登录失败')
      }
    } catch (error) {
      console.error('Logout error:', error)
      // 即使请求失败，也要清除本地状态并跳转
      token.value = ''
      userInfo.value = null
      localStorage.removeItem('token')
      await router.push('/login')
      ElMessage.error('退出登录失败：' + error.message)
    }
  }

  // 获取用户信息
  const getUserInfo = async () => {
    try {
      const res = await getCurrentUser()
      if (res.code === 200) {
        userInfo.value = res.data
        return true
      }
      return false
    } catch (error) {
      console.error('Get user info error:', error)
      return false
    }
  }

  // 模拟登录
  const simulateLogin = async (userId) => {
    try {
      const response = await axios.post(`/api/admin/simulate/login/${userId}`)
      if (response.data.code === 200) {
        const { token: newToken, user } = response.data.data
        token.value = newToken
        userInfo.value = user
        localStorage.setItem('token', newToken)
        
        // 根据用户角色跳转到对应的首页
        const roleRouteMap = {
          [ROLE_ADMIN]: '/admin/dashboard',
          [ROLE_TEACHER]: '/teacher/dashboard',
          [ROLE_STUDENT]: '/student/dashboard'
        }

        const targetRoute = roleRouteMap[user.role]
        if (!targetRoute) {
          ElMessage.error('未知的用户角色')
          return false
        }

        await router.push(targetRoute)
        ElMessage.success(`已切换到用户 ${user.username}`)
        return true
      }
      ElMessage.error(response.data.message || '模拟登录失败')
      return false
    } catch (error) {
      console.error('Simulate login error:', error)
      ElMessage.error('模拟登录失败：' + (error.response?.data?.message || error.message))
      return false
    }
  }

  return {
    token,
    userInfo,
    loginAction,
    logoutAction,
    getUserInfo,
    simulateLogin
  }
}) 
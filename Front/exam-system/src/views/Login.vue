<template>
  <div class="login-container">
    <el-card class="login-card">
      <template #header>
        <div class="card-header">
          <h2>在线考试系统</h2>
          <p class="subtitle">请登录您的账号</p>
        </div>
      </template>
      
      <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="loginRules"
        label-width="0"
        @keyup.enter="handleLogin"
      >
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="请输入用户名"
            prefix-icon="User"
            clearable
            :disabled="loading"
            @keyup.enter="handleLogin"
          />
        </el-form-item>
        
        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            prefix-icon="Lock"
            show-password
            clearable
            :disabled="loading"
            @keyup.enter="handleLogin"
          />
        </el-form-item>
        
        <el-form-item>
          <el-button
            type="primary"
            :loading="loading"
            class="login-button"
            @click="handleLogin"
          >
            {{ loading ? '登录中...' : '登录' }}
          </el-button>
        </el-form-item>
        
        <div class="register-link">
          <el-link type="primary" :disabled="loading" @click="handleRegister" style="margin-right: 20px">
            学生注册
          </el-link>
          <el-link type="primary" :disabled="loading" @click="handleResetPassword">
            找回密码
          </el-link>
        </div>
      </el-form>
    </el-card>

    <!-- 找回密码对话框 -->
    <el-dialog
      v-model="resetDialogVisible"
      title="找回密码"
      width="400px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="resetFormRef"
        :model="resetForm"
        :rules="resetRules"
        label-width="80px"
      >
        <el-form-item label="用户名" prop="username">
          <el-input
            v-model="resetForm.username"
            placeholder="请输入用户名"
            clearable
          />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input
            v-model="resetForm.email"
            placeholder="请输入注册时的邮箱"
            clearable
          />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input
            v-model="resetForm.phone"
            placeholder="请输入注册时的手机号"
            clearable
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="resetDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="resetLoading" @click="handleResetSubmit">
            确认
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { ElMessage } from 'element-plus'
import axios from 'axios'

const router = useRouter()
const userStore = useUserStore()

// 登录表单
const loginFormRef = ref(null)
const loginForm = ref({
  username: '',
  password: ''
})

// 表单验证规则
const loginRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, message: '用户名长度不能小于3位', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能小于6位', trigger: 'blur' }
  ]
}

// 加载状态
const loading = ref(false)

// 处理登录
const handleLogin = async () => {
  if (!loginFormRef.value) return
  
  try {
    // 表单验证
    await loginFormRef.value.validate()
    loading.value = true
    
    // 调用登录接口
    const success = await userStore.loginAction(loginForm.value)
    if (success) {
      ElMessage.success('登录成功')
    }
  } catch (error) {
    console.error('Login error:', error)
  } finally {
    loading.value = false
  }
}

// 处理注册
const handleRegister = () => {
  if (loading.value) return
  router.push('/register')
}

// 找回密码相关
const resetDialogVisible = ref(false)
const resetLoading = ref(false)
const resetFormRef = ref(null)
const resetForm = ref({
  username: '',
  email: '',
  phone: ''
})

// 验证邮箱的正则表达式
const validateEmail = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请输入邮箱'))
  } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value)) {
    callback(new Error('请输入正确的邮箱格式'))
  } else {
    callback()
  }
}

// 验证手机号的正则表达式
const validatePhone = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请输入手机号'))
  } else if (!/^1[3-9]\d{9}$/.test(value)) {
    callback(new Error('请输入正确的手机号'))
  } else {
    callback()
  }
}

// 找回密码表单验证规则
const resetRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 4, max: 10, message: '用户名长度必须在4-10之间', trigger: 'blur' }
  ],
  email: [
    { validator: validateEmail, trigger: 'blur' }
  ],
  phone: [
    { validator: validatePhone, trigger: 'blur' }
  ]
}

// 处理打开找回密码对话框
const handleResetPassword = () => {
  resetForm.value = {
    username: '',
    email: '',
    phone: ''
  }
  resetDialogVisible.value = true
}

// 处理提交找回密码
const handleResetSubmit = async () => {
  if (!resetFormRef.value) return
  
  try {
    await resetFormRef.value.validate()
    resetLoading.value = true
    
    const response = await axios.post(`/api/auth/password/reset?username=${resetForm.value.username}&email=${resetForm.value.email}&phone=${resetForm.value.phone}`)
    
    if (response.data.code === 200) {
      ElMessage.success('新密码已发送到您的邮箱，请查收')
      resetDialogVisible.value = false
      
      // 将新密码自动填入登录表单
      loginForm.value.username = resetForm.value.username
      loginForm.value.password = response.data.data.newPassword
    } else {
      ElMessage.error(response.data.message || '重置密码失败')
    }
  } catch (error) {
    console.error('Reset password error:', error)
    ElMessage.error(error.response?.data?.message || '重置密码失败')
  } finally {
    resetLoading.value = false
  }
}
</script>

<style lang="scss" scoped>
.login-container {
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #8BC6EC 0%, #9599E2 100%);
  
  .login-card {
    width: 400px;
    
    .card-header {
      text-align: center;
      
      h2 {
        margin: 0;
        font-size: 24px;
        color: $text-primary;
      }
      
      .subtitle {
        margin-top: 8px;
        color: $text-secondary;
        font-size: 14px;
      }
    }
    
    .login-button {
      width: 100%;
      height: 40px;
      font-size: 16px;
    }
    
    .register-link {
      text-align: center;
      margin-top: 16px;
    }
  }
}
</style> 
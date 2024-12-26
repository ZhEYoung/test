<template>
  <div class="teacher-layout">
    <el-container class="layout-container">
      <el-aside :width="isCollapse ? '64px' : '200px'" class="aside">
        <div class="logo">
          <el-icon class="logo-icon"><School /></el-icon>
          <span v-show="!isCollapse">在线考试系统</span>
        </div>
        
        <el-menu
          :default-active="route.path"
          :collapse="isCollapse"
          :router="true"
          class="menu"
          background-color="#304156"
          text-color="#bfcbd9"
          active-text-color="#409EFF"
        >
          <el-menu-item index="/teacher/dashboard">
            <el-icon><Monitor /></el-icon>
            <span>控制台</span>
          </el-menu-item>
          <el-menu-item index="/teacher/exams">
            <el-icon><Calendar /></el-icon>
            <span>考试管理</span>
          </el-menu-item>
          <el-menu-item index="/teacher/papers">
            <el-icon><Files /></el-icon>
            <span>试卷管理</span>
          </el-menu-item>
          <el-menu-item index="/teacher/classes">
            <el-icon><Reading /></el-icon>
            <span>班级管理</span>
          </el-menu-item>
          <el-menu-item index="/teacher/questions">
            <el-icon><Document /></el-icon>
            <span>题库管理</span>
          </el-menu-item>
          <el-menu-item index="/teacher/scores">
            <el-icon><DataLine /></el-icon>
            <span>成绩管理</span>
          </el-menu-item>
          <el-menu-item index="/teacher/grading">
            <el-icon><Edit /></el-icon>
            <span>试题批改</span>
          </el-menu-item>
        </el-menu>
      </el-aside>
      
      <el-container>
        <el-header height="60px" class="header">
          <div class="header-left">
            <el-icon
              class="fold-btn"
              @click="isCollapse = !isCollapse"
            >
              <Fold v-if="!isCollapse" />
              <Expand v-else />
            </el-icon>
            <el-breadcrumb separator="/">
              <el-breadcrumb-item :to="{ path: '/teacher/dashboard' }">首页</el-breadcrumb-item>
              <el-breadcrumb-item v-for="(item, index) in breadcrumbs" :key="index">
                {{ item }}
              </el-breadcrumb-item>
            </el-breadcrumb>
          </div>
          
          <div class="header-right">
            <el-dropdown @command="handleCommand">
              <span class="user-info">
                {{ userStore.userInfo?.username }}
                <el-icon><ArrowDown /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">个人信息</el-dropdown-item>
                  <el-dropdown-item command="password">修改密码</el-dropdown-item>
                  <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </el-header>
        
        <el-main>
          <keep-alive>
            <router-view />
          </keep-alive>
        </el-main>
      </el-container>
    </el-container>

    <!-- 修改密码对话框 -->
    <el-dialog
      v-model="passwordDialogVisible"
      title="修改密码"
      width="400px"
    >
      <el-form
        ref="passwordFormRef"
        :model="passwordForm"
        :rules="passwordRules"
        label-width="100px"
      >
        <el-form-item label="原密码" prop="oldPassword">
          <el-input
            v-model="passwordForm.oldPassword"
            type="password"
            show-password
          />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input
            v-model="passwordForm.newPassword"
            type="password"
            show-password
          />
        </el-form-item>
        <el-form-item label="确认新密码" prop="confirmPassword">
          <el-input
            v-model="passwordForm.confirmPassword"
            type="password"
            show-password
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="passwordDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleChangePassword">确定</el-button>
      </template>
    </el-dialog>

    <!-- 个人信息对话框 -->
    <el-dialog
      v-model="profileDialogVisible"
      title="个人信息"
      width="500px"
    >
      <el-form
        ref="profileFormRef"
        :model="profileInfo"
        :rules="profileRules"
        label-width="100px"
      >
        <el-form-item label="用户名" prop="username">
          <el-input v-model="profileInfo.username" disabled />
        </el-form-item>
        <el-form-item label="姓名" prop="name">
          <el-input 
            v-model="profileInfo.name" 
            placeholder="请输入2-20位中文姓名"
          />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input 
            v-model="profileInfo.phone" 
            placeholder="请输入11位手机号码"
          />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input 
            v-model="profileInfo.email" 
            placeholder="请输入有效的邮箱地址"
          />
        </el-form-item>
        <el-form-item label="性别" prop="sex">
          <el-radio-group v-model="profileInfo.sex">
            <el-radio :label="true">男</el-radio>
            <el-radio :label="false">女</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="other">
          <el-input
            v-model="profileInfo.other"
            type="textarea"
            :rows="3"
            placeholder="请输入备注信息（不超过100字）"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="profileDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveProfile">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { ElMessageBox, ElMessage } from 'element-plus'
import { changePassword } from '../api/auth'
import { School, Monitor, Fold, Expand, ArrowDown, Reading, Document, Files, Calendar, DataLine } from '@element-plus/icons-vue'
import axios from 'axios'

// 配置axios默认值
axios.defaults.baseURL = import.meta.env.VITE_API_URL || ''
axios.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.token = token
  }
  return config
})

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 侧边栏折叠状态
const isCollapse = ref(false)

// 面包屑导航
const breadcrumbs = computed(() => {
  const matched = route.matched
  return matched.slice(1).map(item => item.meta.title || item.name)
})

// 修改密码相关
const passwordDialogVisible = ref(false)
const passwordFormRef = ref(null)
const passwordForm = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const passwordRules = {
  oldPassword: [
    { required: true, message: '请输入原密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能小于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== passwordForm.value.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// 个人信息相关
const profileDialogVisible = ref(false)
const profileInfo = ref({
  teacherId: null,
  username: '',
  name: '',
  phone: '',
  email: '',
  sex: null,
  status: true,
  other: ''
})

// 个人信息表单验证规则
const profileRules = {
  name: [
    { required: true, message: '请输入姓名', trigger: 'blur' },
    { min: 2, max: 20, message: '姓名长度必须在2-20之间', trigger: 'blur' },
    { pattern: /^[\u4e00-\u9fa5]{2,20}$/, message: '姓名必须为中文', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  sex: [
    { required: true, message: '请选择性别', trigger: 'change' }
  ],
  other: [
    { max: 100, message: '备注长度不能超过100个字符', trigger: 'blur' }
  ]
}

const profileFormRef = ref(null)

// 保存个人信息
const handleSaveProfile = async () => {
  if (!profileFormRef.value) return
  
  try {
    await profileFormRef.value.validate()
    const response = await axios.put(`/api/teacher/teachers/${profileInfo.value.teacherId}`, {
      name: profileInfo.value.name,
      phone: profileInfo.value.phone,
      email: profileInfo.value.email,
      sex: profileInfo.value.sex,
      other: profileInfo.value.other
    })
    
    if (response.data.code === 200) {
      ElMessage.success('更新成功')
      profileDialogVisible.value = false
      // 更新用户信息
      await userStore.getUserInfo()
    } else {
      ElMessage.error(response.data.message || '更新失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('更新个人信息失败:', error.response || error)
      ElMessage.error(error.response?.data?.message || '更新失败')
    }
  }
}

// 获取个人信息
const fetchProfileInfo = async () => {
  try {
    const userId = userStore.userInfo?.userId
    if (!userId) {
      ElMessage.error('获取用户信息失败')
      return
    }

    const response = await axios.get(`/api/teacher/teachers/${userId}`)
    
    if (response.data.code === 200 && response.data.data) {
      profileInfo.value = response.data.data
    } else {
      ElMessage.error(response.data.message || '获取个人信息失败')
    }
  } catch (error) {
    console.error('获取个人信息失败:', error.response || error)
    ElMessage.error(error.response?.data?.message || '获取个人信息失败')
  }
}

// 处理下拉菜单命令
const handleCommand = async (command) => {
  switch (command) {
    case 'profile':
      await fetchProfileInfo()
      profileDialogVisible.value = true
      break
    case 'password':
      passwordDialogVisible.value = true
      break
    case 'logout':
      try {
        await ElMessageBox.confirm('确认退出登录吗？', '提示', {
          type: 'warning'
        })
        await userStore.logoutAction()
      } catch (error) {
        console.error('Logout error:', error)
      }
      break
  }
}

// 处理修改密码
const handleChangePassword = async () => {
  if (!passwordFormRef.value) return
  
  try {
    await passwordFormRef.value.validate()
    await changePassword({
      oldPassword: passwordForm.value.oldPassword,
      newPassword: passwordForm.value.newPassword
    })
    ElMessage.success('密码修改成功')
    passwordDialogVisible.value = false
    passwordForm.value = {
      oldPassword: '',
      newPassword: '',
      confirmPassword: ''
    }
  } catch (error) {
    console.error('Change password error:', error)
  }
}
</script>

<style lang="scss" scoped>
.teacher-layout {
  height: 100vh;
  
  .layout-container {
    height: 100%;
    
    .aside {
      background-color: #304156;
      transition: width 0.3s;
      
      .logo {
        height: 60px;
        display: flex;
        align-items: center;
        justify-content: center;
        background-color: #2b2f3a;
        padding: 10px;
        overflow: hidden;
        
        .logo-icon {
          font-size: 24px;
          color: #409EFF;
          margin-right: 8px;
          flex-shrink: 0;
        }
        
        span {
          color: #fff;
          font-size: 16px;
          white-space: nowrap;
          transition: opacity 0.3s;
        }
      }
      
      .menu {
        border-right: none;
      }
    }
    
    .header {
      background-color: #fff;
      border-bottom: 1px solid #dcdfe6;
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 0 20px;
      
      .header-left {
        display: flex;
        align-items: center;
        
        .fold-btn {
          font-size: 20px;
          cursor: pointer;
          margin-right: 20px;
        }
      }
      
      .header-right {
        .user-info {
          display: flex;
          align-items: center;
          cursor: pointer;
          
          .el-icon {
            margin-left: 5px;
          }
        }
      }
    }
  }
}
</style> 
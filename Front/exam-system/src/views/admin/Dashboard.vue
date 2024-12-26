<template>
  <div class="dashboard">
    <!-- 欢迎模块 -->
    <el-row :gutter="20" class="welcome-section">
      <el-col :span="24">
        <el-card shadow="hover" class="welcome-card">
          <div class="welcome-content">
            <h1>{{ welcomeMessage }}</h1>
            <div class="info-section">
              <p class="current-time">{{ currentTime }}</p>
              <p class="welcome-tip">欢迎使用在线考试系统管理平台</p>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 原有的统计模块 -->
    <el-row :gutter="20" class="stats-section">
      <!-- 总用户数 -->
      <el-col :span="8">
        <el-card shadow="hover" class="stat-card">
          <template #header>
            <div class="card-header">
              <span>总用户数</span>
              <el-tag type="info" effect="plain">Total Users</el-tag>
            </div>
          </template>
          <div class="card-content">
            <h2>{{ userStats.totalUsers || 0 }}</h2>
            <div class="active-users">
              <span>活跃用户：{{ userStats.activeUsers || 0 }}</span>
              <el-progress 
                :percentage="calculateActivePercentage" 
                :format="format"
                status="success"
              />
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 用户分布 -->
      <el-col :span="16">
        <el-card shadow="hover" class="stat-card">
          <template #header>
            <div class="card-header">
              <span>用户分布</span>
              <el-tag type="info" effect="plain">User Distribution</el-tag>
            </div>
          </template>
          <div class="distribution-content">
            <div class="user-type">
              <el-icon color="#409EFF"><User /></el-icon>
              <span>管理员：{{ userStats.adminCount || 0 }}</span>
              <el-progress 
                :percentage="calculatePercentage(userStats.adminCount)" 
                type="dashboard"
                :stroke-width="10"
                :width="120"
                :show-text="true"
                :format="format"
                :color="'#409EFF'"
              />
            </div>
            <div class="user-type">
              <el-icon color="#67C23A"><School /></el-icon>
              <span>教师：{{ userStats.teacherCount || 0 }}</span>
              <el-progress 
                :percentage="calculatePercentage(userStats.teacherCount)" 
                type="dashboard"
                :stroke-width="10"
                :width="120"
                :show-text="true"
                :format="format"
                :color="'#67C23A'"
              />
            </div>
            <div class="user-type">
              <el-icon color="#E6A23C"><Reading /></el-icon>
              <span>学生：{{ userStats.studentCount || 0 }}</span>
              <el-progress 
                :percentage="calculatePercentage(userStats.studentCount)" 
                type="dashboard"
                :stroke-width="10"
                :width="120"
                :show-text="true"
                :format="format"
                :color="'#E6A23C'"
              />
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 系统资源统计 -->
      <el-col :span="24" style="margin-top: 20px;">
        <el-card shadow="hover" class="stat-card">
          <template #header>
            <div class="card-header">
              <span>系统资源统计</span>
              <el-tag type="info" effect="plain">System Resources</el-tag>
            </div>
          </template>
          <div class="resource-content">
            <div class="resource-item">
              <el-icon color="#409EFF"><Document /></el-icon>
              <div class="resource-info">
                <span class="resource-label">试题总数</span>
                <span class="resource-value">{{ resourceStats.totalQuestions || 0 }}</span>
              </div>
            </div>
            <div class="resource-item">
              <el-icon color="#67C23A"><List /></el-icon>
              <div class="resource-info">
                <span class="resource-label">考试总数</span>
                <span class="resource-value">{{ resourceStats.totalExams || 0 }}</span>
              </div>
            </div>
            <div class="resource-item">
              <el-icon color="#E6A23C"><Collection /></el-icon>
              <div class="resource-info">
                <span class="resource-label">科目总数</span>
                <span class="resource-value">{{ resourceStats.totalSubjects || 0 }}</span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, onUnmounted, onActivated } from 'vue'
import { User, School, Reading, Picture, Document, List, Collection } from '@element-plus/icons-vue'
import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const currentTime = ref('')

// 根据时间生成欢迎语
const welcomeMessage = computed(() => {
  const hour = new Date().getHours()
  const userInfo = userStore.userInfo
  const username = userInfo?.username || '管理员'
  
  if (hour < 6) {
    return `夜深了，${username}`
  } else if (hour < 9) {
    return `早安，${username}`
  } else if (hour < 12) {
    return `上午好，${username}`
  } else if (hour < 14) {
    return `中午好，${username}`
  } else if (hour < 18) {
    return `下午好，${username}`
  } else if (hour < 22) {
    return `晚上好，${username}`
  } else {
    return `晚安，${username}`
  }
})

// 更新当前时间
const updateCurrentTime = () => {
  const now = new Date()
  const options = {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    weekday: 'long',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  }
  currentTime.value = now.toLocaleDateString('zh-CN', options)
}

const userStats = ref({
  totalUsers: 0,
  activeUsers: 0,
  adminCount: 0,
  teacherCount: 0,
  studentCount: 0
})

// 计算活跃用户百分比
const calculateActivePercentage = computed(() => {
  if (!userStats.value.totalUsers) return 0
  return Math.round((userStats.value.activeUsers / userStats.value.totalUsers) * 100)
})

// 计算各类用户占比
const calculatePercentage = (count) => {
  if (!userStats.value.totalUsers) return 0
  return Math.round((count / userStats.value.totalUsers) * 100)
}

// 格式化百分比显示
const format = (percentage) => {
  return percentage + '%'
}

// 添加资源统计数据
const resourceStats = ref({
  totalQuestions: 0,
  totalExams: 0,
  totalSubjects: 0
})

// 修改获取统计数据的方法
const fetchDashboardData = async () => {
  try {
    // 检查是否有 token
    const token = localStorage.getItem('token')
    if (!token) {
      ElMessage.error('未登录或登录已过期')
      return
    }

    // 检查当前用户角色是否为管理员
    const currentRole = userStore.userInfo?.role
    if (currentRole !== 0) {
      console.log('当前用户不是管理员，跳过获取管理员dashboard数据')
      return
    }

    const response = await axios.get('/api/admin/dashboard', {
      headers: {
        'token': token
      }
    })

    if (response.data.code === 200) {
      const { userStats: stats, resourceStats: resources } = response.data.data
      // 更新用户统计数据
      userStats.value = {
        totalUsers: stats.totalUsers || 0,
        activeUsers: stats.activeUsers || 0,
        adminCount: stats.adminCount || 0,
        teacherCount: stats.teacherCount || 0,
        studentCount: stats.studentCount || 0
      }
      // 更新资源统计数据
      resourceStats.value = {
        totalQuestions: resources.totalQuestions || 0,
        totalExams: resources.totalExams || 0,
        totalSubjects: resources.totalSubjects || 0
      }
      console.log('Dashboard data fetched:', response.data)
    } else {
      ElMessage.error(response.data.message || '获取系统概览数据失败')
    }
  } catch (error) {
    console.error('获取系统概览数据失败:', error)
    ElMessage.error('获取系统概览数据失败：' + (error.response?.data?.message || error.message))
  }
}

// 修改 onMounted，添加自动刷新功能
onMounted(async () => {
  if (!userStore.userInfo) {
    await userStore.getUserInfo()
  }
  
  // 立即获取数据
  await fetchDashboardData()
  updateCurrentTime()
  
  // 设置定时器
  const timers = [
    setInterval(updateCurrentTime, 1000),      // 每秒更新时间
    setInterval(fetchDashboardData, 30000)     // 每30秒刷新一次数据
  ]

  // 组件卸载时清理定时器
  onUnmounted(() => {
    timers.forEach(timer => clearInterval(timer))
  })
})

// 添加组件激活时的处理
onActivated(() => {
  console.log('Dashboard activated')
  fetchDashboardData()
})
</script>

<style lang="scss" scoped>
.dashboard {
  padding: 20px;

  .welcome-section {
    margin-bottom: 20px;
  }

  .welcome-card {
    .welcome-content {
      padding: 20px;
      text-align: center;

      h1 {
        font-size: 28px;
        color: #303133;
        margin-bottom: 16px;
      }

      .info-section {
        .current-time {
          font-size: 16px;
          color: #606266;
          margin-bottom: 12px;
        }

        .welcome-tip {
          font-size: 14px;
          color: #909399;
        }
      }
    }
  }

  .stat-card {
    height: 100%;
    
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }

    .card-content {
      text-align: center;
      
      h2 {
        font-size: 36px;
        margin: 20px 0;
        color: #303133;
      }

      .active-users {
        text-align: left;
        
        span {
          display: block;
          margin-bottom: 10px;
          color: #606266;
        }
      }
    }

    .distribution-content {
      display: flex;
      justify-content: space-around;
      align-items: center;
      padding: 20px 0;

      .user-type {
        text-align: center;
        
        .el-icon {
          font-size: 24px;
          margin-bottom: 10px;
        }

        span {
          display: block;
          margin-bottom: 15px;
          color: #606266;
        }
      }
    }

    .resource-content {
      display: flex;
      justify-content: space-around;
      align-items: center;
      padding: 20px 0;

      .resource-item {
        display: flex;
        align-items: center;
        padding: 20px;

        .el-icon {
          font-size: 40px;
          margin-right: 16px;
        }

        .resource-info {
          display: flex;
          flex-direction: column;

          .resource-label {
            font-size: 14px;
            color: #606266;
            margin-bottom: 8px;
          }

          .resource-value {
            font-size: 24px;
            color: #303133;
            font-weight: bold;
          }
        }
      }
    }
  }
}
</style> 
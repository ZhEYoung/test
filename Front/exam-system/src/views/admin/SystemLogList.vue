# 创建系统日志管理页面
<template>
  <div class="system-log-list">
    <el-tabs v-model="activeTab" @tab-click="handleTabClick">
      <el-tab-pane label="操作日志" name="operation">
        <div class="search-bar">
          <el-form :inline="true" :model="operationSearchForm">
            <el-form-item label="用户名">
              <el-input v-model="operationSearchForm.username" placeholder="请输入用户名" clearable />
            </el-form-item>
            <el-form-item label="操作类型">
              <el-select 
                v-model="operationSearchForm.operation" 
                placeholder="请选择操作类型" 
                clearable
                style="width: 160px;"
              >
                <el-option label="新增" value="0" />
                <el-option label="更新" value="1" />
                <el-option label="删除" value="2" />
                <el-option label="登录" value="3" />
                <el-option label="提交考试" value="4" />
                <el-option label="系统异常" value="5" />
                <el-option label="模拟登陆" value="6" />
                <el-option label="信息查询" value="7" />
                <el-option label="表格导出" value="8" />
              </el-select>
            </el-form-item>
            <el-form-item label="时间范围">
              <el-date-picker
                v-model="operationSearchForm.timeRange"
                type="datetimerange"
                range-separator="至"
                start-placeholder="开始时间"
                end-placeholder="结束时间"
                value-format="YYYY-MM-DD HH:mm:ss"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="searchOperationLogs">
                <el-icon><Search /></el-icon>
                查询
              </el-button>
              <el-button @click="resetOperationSearch">
                <el-icon><Refresh /></el-icon>
                重置
              </el-button>
              <el-button type="danger" @click="handleClearOperationLogs">
                <el-icon><Delete /></el-icon>
                清空日志
              </el-button>
              <el-button type="success" @click="handleExportOperationLogs">
                <el-icon><Download /></el-icon>
                导出日志
              </el-button>
            </el-form-item>
          </el-form>
        </div>

        <el-table
          v-loading="operationLoading"
          :data="operationLogs"
          border
          style="width: 100%"
        >
          <el-table-column prop="user.username" label="用户名" width="120" />
          <el-table-column prop="actionType" label="操作类型" width="100">
            <template #default="scope">
              {{ getActionTypeText(scope.row.actionType) }}
            </template>
          </el-table-column>
          <el-table-column prop="actionDescription" label="操作描述" />
          <el-table-column prop="objectType" label="操作对象" width="120" />
          <el-table-column prop="ipAddress" label="IP地址" width="120" />
          <el-table-column prop="deviceInfo" label="设备信息" width="200" />
          <el-table-column prop="createdTime" label="操作时间" width="180" />
          <el-table-column prop="status" label="状态" width="100">
            <template #default="scope">
              <el-tag :type="getStatusType(scope.row.status)">
                {{ getStatusText(scope.row.status) }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>

        <div class="pagination">
          <el-pagination
            v-model:current-page="operationCurrentPage"
            v-model:page-size="operationPageSize"
            :total="operationTotal"
            :page-sizes="[10, 20, 30, 50]"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleOperationSizeChange"
            @current-change="handleOperationCurrentChange"
          />
        </div>
      </el-tab-pane>

      <el-tab-pane label="登录日志" name="login">
        <div class="search-bar">
          <el-form :inline="true" :model="loginSearchForm">
            <el-form-item label="用户名">
              <el-input v-model="loginSearchForm.username" placeholder="请输入用户名" clearable />
            </el-form-item>
            <el-form-item label="IP地址">
              <el-input v-model="loginSearchForm.ip" placeholder="请输入IP地址" clearable />
            </el-form-item>
            <el-form-item label="时间范围">
              <el-date-picker
                v-model="loginSearchForm.timeRange"
                type="datetimerange"
                range-separator="至"
                start-placeholder="开始时间"
                end-placeholder="结束时间"
                value-format="YYYY-MM-DD HH:mm:ss"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="searchLoginLogs">
                <el-icon><Search /></el-icon>
                查询
              </el-button>
              <el-button @click="resetLoginSearch">
                <el-icon><Refresh /></el-icon>
                重置
              </el-button>
              <el-button type="danger" @click="handleClearLoginLogs">
                <el-icon><Delete /></el-icon>
                清空日志
              </el-button>
            </el-form-item>
          </el-form>
        </div>

        <el-table
          v-loading="loginLoading"
          :data="loginLogs"
          border
          style="width: 100%"
        >
          <el-table-column prop="user.username" label="用户名" width="120" />
          <el-table-column prop="ipAddress" label="IP地址" width="120" />
          <el-table-column prop="deviceInfo" label="设备信息" width="200" />
          <el-table-column prop="createdTime" label="登录时间" width="180" />
          <el-table-column prop="status" label="状态" width="100">
            <template #default="scope">
              <el-tag :type="getStatusType(scope.row.status)">
                {{ getStatusText(scope.row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="actionDescription" label="登录描述" />
        </el-table>

        <div class="pagination">
          <el-pagination
            v-model:current-page="loginCurrentPage"
            v-model:page-size="loginPageSize"
            :total="loginTotal"
            :page-sizes="[10, 20, 30, 50]"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleLoginSizeChange"
            @current-change="handleLoginCurrentChange"
          />
        </div>
      </el-tab-pane>

      <el-tab-pane label="异常日志" name="error">
        <div class="search-bar">
          <el-form :inline="true" :model="errorSearchForm">
            <el-form-item label="用户名">
              <el-input v-model="errorSearchForm.username" placeholder="请输入用户名" clearable />
            </el-form-item>
            <el-form-item label="错误信息">
              <el-input v-model="errorSearchForm.errorInfo" placeholder="请输入错误信息" clearable />
            </el-form-item>
            <el-form-item label="时间范围">
              <el-date-picker
                v-model="errorSearchForm.timeRange"
                type="datetimerange"
                range-separator="至"
                start-placeholder="开始时间"
                end-placeholder="结束时间"
                value-format="YYYY-MM-DD HH:mm:ss"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="searchErrorLogs">
                <el-icon><Search /></el-icon>
                查询
              </el-button>
              <el-button @click="resetErrorSearch">
                <el-icon><Refresh /></el-icon>
                重置
              </el-button>
              <el-button type="danger" @click="handleClearErrorLogs">
                <el-icon><Delete /></el-icon>
                清空日志
              </el-button>
              <el-button type="success" @click="handleExportErrorLogs">
                <el-icon><Download /></el-icon>
                导出日志
              </el-button>
            </el-form-item>
          </el-form>
        </div>

        <el-table
          v-loading="errorLoading"
          :data="errorLogs"
          border
          style="width: 100%"
        >
          <el-table-column prop="user.username" label="用户名" width="120" />
          <el-table-column prop="actionDescription" label="错误信息" />
          <el-table-column prop="objectType" label="错误对象" width="120" />
          <el-table-column prop="ipAddress" label="IP地址" width="120" />
          <el-table-column prop="deviceInfo" label="设备信息" width="200" />
          <el-table-column prop="createdTime" label="发生时间" width="180" />
          <el-table-column prop="status" label="状态" width="100">
            <template #default="scope">
              <el-tag :type="getStatusType(scope.row.status)">
                {{ getStatusText(scope.row.status) }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>

        <div class="pagination">
          <el-pagination
            v-model:current-page="errorCurrentPage"
            v-model:page-size="errorPageSize"
            :total="errorTotal"
            :page-sizes="[10, 20, 30, 50]"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleErrorSizeChange"
            @current-change="handleErrorCurrentChange"
          />
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Search, Refresh, Delete, Download } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getOperationLogs,
  getLoginLogs,
  getErrorLogs,
  clearOperationLogs,
  clearLoginLogs,
  clearErrorLogs,
  exportAllLogs,
  exportErrorLogs
} from '@/api/systemLog'

// 当前激活的标签页
const activeTab = ref('operation')

// 操作日志相关数据
const operationLogs = ref([])
const operationLoading = ref(false)
const operationTotal = ref(0)
const operationCurrentPage = ref(1)
const operationPageSize = ref(10)
const operationSearchForm = ref({
  username: '',
  operation: '',
  timeRange: []
})

// 登录日志相关数据
const loginLogs = ref([])
const loginLoading = ref(false)
const loginTotal = ref(0)
const loginCurrentPage = ref(1)
const loginPageSize = ref(10)
const loginSearchForm = ref({
  username: '',
  ip: '',
  timeRange: []
})

// 异常日志相关数据
const errorLogs = ref([])
const errorLoading = ref(false)
const errorTotal = ref(0)
const errorCurrentPage = ref(1)
const errorPageSize = ref(10)
const errorSearchForm = ref({
  username: '',
  errorInfo: '',
  timeRange: []
})

// 获取操作类型文本
const getActionTypeText = (type) => {
  const types = {
    0: '新增',
    1: '更新',
    2: '删除',
    3: '登录',
    4: '提交考试',
    5: '系统异常',
    6: '模拟登陆',
    7: '信息查询',
    8: '表格导出'
  }
  return types[type] || '未知'
}

// 状态显示相关方法
const getStatusType = (status) => {
  if (!status) return 'info'
  const upperStatus = status.toUpperCase()
  if (upperStatus === 'SUCCESS' || upperStatus === '成功') return 'success'
  if (upperStatus === 'FAILED' || upperStatus === '失败') return 'danger'
  return 'info'
}

const getStatusText = (status) => {
  if (!status) return '未知'
  const upperStatus = status.toUpperCase()
  if (upperStatus === 'SUCCESS') return '成功'
  if (upperStatus === 'FAILED') return '失败'
  return status
}

// 操作日志相关方法
const searchOperationLogs = async () => {
  operationLoading.value = true
  try {
    const params = {
      pageNum: operationCurrentPage.value,
      pageSize: operationPageSize.value,
      username: operationSearchForm.value.username,
      operation: operationSearchForm.value.operation,
      startTime: operationSearchForm.value.timeRange?.[0],
      endTime: operationSearchForm.value.timeRange?.[1]
    }
    const res = await getOperationLogs(params)
    operationLogs.value = res.data.records
    operationTotal.value = res.data.total
  } catch (error) {
    ElMessage.error('获取操作日志失败')
  } finally {
    operationLoading.value = false
  }
}

const resetOperationSearch = () => {
  operationSearchForm.value = {
    username: '',
    operation: '',
    timeRange: []
  }
  operationCurrentPage.value = 1
  searchOperationLogs()
}

const handleOperationSizeChange = (val) => {
  operationPageSize.value = val
  searchOperationLogs()
}

const handleOperationCurrentChange = (val) => {
  operationCurrentPage.value = val
  searchOperationLogs()
}

// 登录日志相关方法
const searchLoginLogs = async () => {
  loginLoading.value = true
  try {
    const params = {
      pageNum: loginCurrentPage.value,
      pageSize: loginPageSize.value,
      username: loginSearchForm.value.username,
      ip: loginSearchForm.value.ip,
      startTime: loginSearchForm.value.timeRange?.[0],
      endTime: loginSearchForm.value.timeRange?.[1]
    }
    const res = await getLoginLogs(params)
    loginLogs.value = res.data.records
    loginTotal.value = res.data.total
  } catch (error) {
    ElMessage.error('获取登录日志失败')
  } finally {
    loginLoading.value = false
  }
}

const resetLoginSearch = () => {
  loginSearchForm.value = {
    username: '',
    ip: '',
    timeRange: []
  }
  loginCurrentPage.value = 1
  searchLoginLogs()
}

const handleLoginSizeChange = (val) => {
  loginPageSize.value = val
  searchLoginLogs()
}

const handleLoginCurrentChange = (val) => {
  loginCurrentPage.value = val
  searchLoginLogs()
}

// 异常日志相关方法
const searchErrorLogs = async () => {
  errorLoading.value = true
  try {
    const params = {
      pageNum: errorCurrentPage.value,
      pageSize: errorPageSize.value,
      username: errorSearchForm.value.username,
      errorInfo: errorSearchForm.value.errorInfo,
      startTime: errorSearchForm.value.timeRange?.[0],
      endTime: errorSearchForm.value.timeRange?.[1]
    }
    const res = await getErrorLogs(params)
    errorLogs.value = res.data.records
    errorTotal.value = res.data.total
  } catch (error) {
    ElMessage.error('获取异常日志失败')
  } finally {
    errorLoading.value = false
  }
}

const resetErrorSearch = () => {
  errorSearchForm.value = {
    username: '',
    errorInfo: '',
    timeRange: []
  }
  errorCurrentPage.value = 1
  searchErrorLogs()
}

const handleErrorSizeChange = (val) => {
  errorPageSize.value = val
  searchErrorLogs()
}

const handleErrorCurrentChange = (val) => {
  errorCurrentPage.value = val
  searchErrorLogs()
}

// 清空日志相关方法
const handleClearOperationLogs = () => {
  ElMessageBox.confirm('确定要清空操作日志吗？此操作不可恢复！', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await clearOperationLogs()
      ElMessage.success('清空操作日志成功')
      searchOperationLogs()
    } catch (error) {
      ElMessage.error('清空操作日志失败')
    }
  })
}

const handleClearLoginLogs = () => {
  ElMessageBox.confirm('确定要清空登录日志吗？此操作不可恢复！', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await clearLoginLogs()
      ElMessage.success('清空登录日志成功')
      searchLoginLogs()
    } catch (error) {
      ElMessage.error('清空登录日志失败')
    }
  })
}

const handleClearErrorLogs = () => {
  ElMessageBox.confirm('确定要清空异常日志吗？此操作不可恢复！', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await clearErrorLogs()
      ElMessage.success('清空异常日志成功')
      searchErrorLogs()
    } catch (error) {
      ElMessage.error('清空异常日志失败')
    }
  })
}

// 导出日志相关方法
const handleExportOperationLogs = async () => {
  try {
    const params = {
      startTime: operationSearchForm.value.timeRange?.[0],
      endTime: operationSearchForm.value.timeRange?.[1]
    }
    const res = await exportAllLogs(params)
    // 从响应头中获取文件名
    const contentDisposition = res.headers['content-disposition']
    let fileName = '操作日志.xlsx'
    if (contentDisposition) {
      const fileNameMatch = contentDisposition.match(/filename\*=UTF-8''(.+)/)
      if (fileNameMatch) {
        fileName = decodeURIComponent(fileNameMatch[1])
      }
    }
    
    // 创建Blob对象并下载
    const blob = new Blob([res.data], { 
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
    })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = fileName
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
  } catch (error) {
    console.error('导出操作日志失败:', error)
    ElMessage.error('导出操作日志失败')
  }
}

const handleExportErrorLogs = async () => {
  try {
    const params = {
      startTime: errorSearchForm.value.timeRange?.[0],
      endTime: errorSearchForm.value.timeRange?.[1]
    }
    const res = await exportErrorLogs(params)
    // 从响应头中获取文件名
    const contentDisposition = res.headers['content-disposition']
    let fileName = '异常日志.xlsx'
    if (contentDisposition) {
      const fileNameMatch = contentDisposition.match(/filename\*=UTF-8''(.+)/)
      if (fileNameMatch) {
        fileName = decodeURIComponent(fileNameMatch[1])
      }
    }
    
    // 创建Blob对象并下载
    const blob = new Blob([res.data], { 
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
    })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = fileName
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
  } catch (error) {
    console.error('导出异常日志失败:', error)
    ElMessage.error('导出异常日志失败')
  }
}

// 标签页切换
const handleTabClick = (tab) => {
  switch (tab.props.name) {
    case 'operation':
      searchOperationLogs()
      break
    case 'login':
      searchLoginLogs()
      break
    case 'error':
      searchErrorLogs()
      break
  }
}

// 初始化
onMounted(() => {
  searchOperationLogs()
})
</script>

<style scoped>
.system-log-list {
  padding: 20px;
}

.search-bar {
  margin-bottom: 20px;
  padding: 20px;
  background-color: #fff;
  border-radius: 4px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

:deep(.el-table) {
  margin-top: 20px;
}

:deep(.el-form-item) {
  margin-bottom: 0;
}

:deep(.el-select) {
  width: 160px;
}
</style> 
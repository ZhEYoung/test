<template>
  <div class="dashboard-container">
    <!-- 统计数据 -->
    <el-row :gutter="20">
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>待考试数</span>
            </div>
          </template>
          <div class="card-body">
            <el-statistic :value="statistics.upcomingExamCount">
              <template #prefix>
                <el-icon><Calendar /></el-icon>
              </template>
            </el-statistic>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>已完成考试</span>
            </div>
          </template>
          <div class="card-body">
            <el-statistic :value="statistics.completedExamCount">
              <template #prefix>
                <el-icon><Check /></el-icon>
              </template>
            </el-statistic>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 考试列表 -->
    <el-row :gutter="20" class="mt-4">
      <el-col :span="24">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>最近考试</span>
            </div>
          </template>
          <el-table :data="examList" stripe v-loading="loading">
            <el-table-column prop="exam.examName" label="考试名称" min-width="200">
              <template #default="{ row }">
                <span>
                  {{ row.exam.examType === 0 ? '普通考试-' : '重考考试-' }}{{ row.exam.examName }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="paperName" label="试卷名称" min-width="180" />
            <el-table-column label="考试时间" min-width="300">
              <template #default="{ row }">
                {{ formatDateTime(row.exam.examStartTime) }} 至 {{ formatDateTime(row.exam.examEndTime) }}
              </template>
            </el-table-column>
            <el-table-column prop="examDuration" label="考试时长" width="100">
              <template #default="{ row }">
                {{ row.examDuration }}分钟
              </template>
            </el-table-column>
            <el-table-column label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="getStatusType(row)">
                  {{ getStatusText(row) }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
          <div class="pagination-container">
            <el-pagination
              v-model:current-page="currentPage"
              v-model:page-size="pageSize"
              :total="total"
              :page-sizes="[5, 10, 20, 50]"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="handleSizeChange"
              @current-change="handleCurrentChange"
            />
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Calendar, Check } from '@element-plus/icons-vue'
import axios from 'axios'

// 统计数据
const statistics = ref({
  upcomingExamCount: 0,
  completedExamCount: 0
})

// 考试列表数据
const examList = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const loading = ref(false)

// 获取考试列表
const fetchExamList = async () => {
  loading.value = true
  try {
    const response = await axios.get('/api/student/exam/list', {
      params: {
        pageNum: currentPage.value,
        pageSize: pageSize.value
      }
    })
    
    if (response.data.code === 200) {
      const { list, total: totalCount } = response.data.data
      examList.value = list
      total.value = totalCount
      
      // 更新统计数据
      statistics.value.upcomingExamCount = list.filter(item => 
        !item.submitted && !item.disciplinary && !item.absent && 
        (item.exam.examStatus === 0 || item.exam.examStatus === 1)
      ).length

      statistics.value.completedExamCount = list.filter(item => 
        item.submitted || item.exam.examStatus === 2
      ).length
    } else {
      ElMessage.error(response.data.message || '获取考试列表失败')
    }
  } catch (error) {
    console.error('获取考试列表失败:', error)
    ElMessage.error('获取考试列表失败')
  } finally {
    loading.value = false
  }
}

// 格式化日期时间
const formatDateTime = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 获取状态类型
const getStatusType = (row) => {
  if (row.disciplinary) return 'danger'
  if (row.absent) return 'info'
  if (row.submitted) return 'success'
  if (row.exam.examStatus === 2) return 'info'
  if (row.started) return 'warning'
  return 'primary'
}

// 获取状态文本
const getStatusText = (row) => {
  if (row.disciplinary) return '违纪'
  if (row.absent) return '缺考'
  if (row.submitted) return '已提交'
  if (row.exam.examStatus === 2) return '已结束'
  if (row.started) return '进行中'
  return '未开始'
}

// 分页处理
const handleSizeChange = (val) => {
  pageSize.value = val
  fetchExamList()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchExamList()
}

onMounted(() => {
  fetchExamList()
})
</script>

<style lang="scss" scoped>
.dashboard-container {
  padding: 20px;
  
  .mt-4 {
    margin-top: 16px;
  }
  
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
  
  .card-body {
    text-align: center;
    padding: 20px 0;
    
    .el-icon {
      font-size: 24px;
      margin-right: 8px;
    }
  }
  
  .pagination-container {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
  }
}
</style> 
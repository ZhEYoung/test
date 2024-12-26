<template>
  <div class="dashboard-container">
    <!-- 统计数据 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>考试总数</span>
            </div>
          </template>
          <div class="card-body">
            <el-statistic :value="statistics.total_exams">
              <template #prefix>
                <el-icon><Document /></el-icon>
              </template>
            </el-statistic>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>学科数量</span>
            </div>
          </template>
          <div class="card-body">
            <el-statistic :value="statistics.total_subjects">
              <template #prefix>
                <el-icon><Collection /></el-icon>
              </template>
            </el-statistic>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>班级数量</span>
            </div>
          </template>
          <div class="card-body">
            <el-statistic :value="statistics.total_classes">
              <template #prefix>
                <el-icon><School /></el-icon>
              </template>
            </el-statistic>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>学生数量</span>
            </div>
          </template>
          <div class="card-body">
            <el-statistic :value="statistics.total_students">
              <template #prefix>
                <el-icon><User /></el-icon>
              </template>
            </el-statistic>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 考试列表 -->
    <el-card class="exam-list-card">
      <template #header>
        <div class="card-header">
          <span>我的考试</span>
        </div>
      </template>
      <el-table :data="examList" border style="width: 100%">
        <el-table-column prop="examName" label="考试名称" />
        <el-table-column prop="subjectName" label="学科" />
        <el-table-column label="考试类型" width="100">
          <template #default="scope">
            {{ scope.row.examType === 0 ? '正常考试' : '重考' }}
          </template>
        </el-table-column>
        <el-table-column label="班级数量" width="100">
          <template #default="scope">
            {{ scope.row.examType === 0 ? scope.row.classCount : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="studentCount" label="考生人数" width="100" />
        <el-table-column prop="examTime" label="考试时间" width="200" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ scope.row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="平均分" width="100">
          <template #default="scope">
            {{ scope.row.avgScore ? scope.row.avgScore.toFixed(2) : '-' }}
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Document, Collection, School, User } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'

// 统计数据
const statistics = ref({
  total_exams: 0,
  total_subjects: 0,
  total_classes: 0,
  total_students: 0
})

// 考试列表
const examList = ref([])

// 获取统计数据
const fetchStatistics = async () => {
  try {
    const response = await axios.get('/api/teacher/exam/stats')
    if (response.data.code === 200) {
      statistics.value = response.data.data
    } else {
      ElMessage.error(response.data.message || '获取统计信息失败')
    }
  } catch (error) {
    console.error('获取统计信息失败:', error)
    ElMessage.error('获取统计信息失败')
  }
}

// 获取考试列表
const fetchExamList = async () => {
  try {
    const response = await axios.get('/api/teacher/exams')
    if (response.data.code === 200) {
      // 转换数据格式
      examList.value = response.data.data.map(exam => ({
        examId: exam.exam_id,
        examName: exam.exam_name,
        examType: exam.exam_type,
        subjectName: exam.subject_name,
        studentCount: exam.student_count,
        classCount: exam.class_count,
        examTime: formatExamTime(exam.exam_start_time, exam.exam_end_time),
        status: getExamStatus(exam.exam_status),
        avgScore: exam.avg_score
      }))
    } else {
      ElMessage.error(response.data.message || '获取考试列表失败')
    }
  } catch (error) {
    console.error('获取考试列表失败:', error)
    ElMessage.error('获取考试列表失败')
  }
}

// 格式化考试时间
const formatExamTime = (startTime, endTime) => {
  const start = new Date(startTime)
  const end = new Date(endTime)
  return `${start.toLocaleDateString()} ${start.toLocaleTimeString()} - ${end.toLocaleTimeString()}`
}

// 获取考试状态文本
const getExamStatus = (status) => {
  switch (status) {
    case 0:
      return '未开始'
    case 1:
      return '进行中'
    case 2:
      return '已结束'
    default:
      return '未知'
  }
}

// 获取状态类型
const getStatusType = (status) => {
  switch (status) {
    case '未开始':
      return 'info'
    case '进行中':
      return 'success'
    case '已结束':
      return 'warning'
    default:
      return 'info'
  }
}

onMounted(() => {
  fetchStatistics()
  fetchExamList()
})
</script>

<style lang="scss" scoped>
.dashboard-container {
  padding: 20px;
  
  .stats-row {
    margin-bottom: 20px;
  }
  
  .exam-list-card {
    margin-bottom: 20px;
  }
  
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
  
  .card-body {
    display: flex;
    justify-content: center;
    align-items: center;
    height: 100px;
    
    .el-icon {
      font-size: 24px;
      margin-right: 8px;
    }
  }
}
</style> 
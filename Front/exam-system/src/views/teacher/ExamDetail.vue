<template>
  <div class="exam-detail">
    <el-card class="detail-card">
      <template #header>
        <div class="card-header">
          <span>考试详情</span>
          <div class="header-actions">
            <el-button 
              v-if="examInfo.exam?.examStatus === 0"
              type="primary" 
              @click="handleEdit"
            >
              编辑考试
            </el-button>
            <el-button @click="handleBack">返回</el-button>
          </div>
        </div>
      </template>

      <el-descriptions
        :column="2"
        border
        class="exam-info"
      >
        <el-descriptions-item label="考试名称">{{ examInfo.exam?.examName }}</el-descriptions-item>
        <el-descriptions-item label="所属学科">{{ examInfo.paper?.subject?.subjectName }}</el-descriptions-item>
        <el-descriptions-item label="考试类型">
          <el-tag :type="getExamTypeTag(examInfo.exam?.examType)">
            {{ getExamTypeText(examInfo.exam?.examType) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="考试状态">
          <el-tag :type="getStatusType(examInfo.exam?.examStatus)">
            {{ getStatusText(examInfo.exam?.examStatus) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="考试时间">
          {{ formatExamTime(examInfo.exam?.examStartTime, examInfo.exam?.examEndTime) }}
        </el-descriptions-item>
        <el-descriptions-item label="考试时长">{{ examInfo.exam?.examDuration }}分钟</el-descriptions-item>
        <el-descriptions-item label="及格分数">
          {{ examInfo.exam?.passScore || 60 }}分
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">
          {{ formatDateTime(examInfo.exam?.createdTime) }}
        </el-descriptions-item>
      </el-descriptions>

      <!-- 试卷信息 -->
      <div class="paper-info">
        <h3>试卷信息</h3>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="试卷名称">{{ examInfo.paper?.paperName }}</el-descriptions-item>
          <el-descriptions-item label="试卷状态">{{ examInfo.paper?.statusName }}</el-descriptions-item>
          <el-descriptions-item label="试卷总分">{{ examInfo.paper?.totalScore }}分</el-descriptions-item>
          <el-descriptions-item label="试卷难度">
            <el-tag :type="getDifficultyTag(examInfo.paper?.paperDifficulty)">
              {{ getDifficultyText(examInfo.paper?.paperDifficulty) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="出题教师">{{ examInfo.paper?.teacher?.name }}</el-descriptions-item>
          <el-descriptions-item label="所属学院">{{ examInfo.paper?.subject?.college?.collegeName }}</el-descriptions-item>
        </el-descriptions>

        <!-- 试题列表 -->
        <div class="questions-list">
          <h4>试题列表</h4>
          <el-table :data="examInfo.paper?.questions" border style="width: 100%">
            <el-table-column label="题号" type="index" width="80" />
            <el-table-column label="题型" width="120">
              <template #default="scope">
                {{ getQuestionTypeName(scope.row.type) }}
              </template>
            </el-table-column>
            <el-table-column prop="content" label="题目内容" show-overflow-tooltip />
            <el-table-column label="难度" width="100">
              <template #default="scope">
                <el-tag :type="getDifficultyTag(scope.row.difficulty)">
                  {{ getDifficultyText(scope.row.difficulty) }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>

      <!-- 考试参与情况 -->
      <div class="participation-info">
        <h3>考试参与情况</h3>
        <el-row :gutter="20">
          <el-col :span="6">
            <el-card shadow="hover">
              <template #header>
                <div class="stat-header">
                  <span>总人数</span>
                </div>
              </template>
              <div class="stat-content">
                <div class="stat-number">{{ examInfo.totalStudents }}</div>
                <div class="stat-label">参考总人数</div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card shadow="hover">
              <template #header>
                <div class="stat-header">
                  <span>已开始</span>
                </div>
              </template>
              <div class="stat-content">
                <div class="stat-number">{{ examInfo.startedStudents }}</div>
                <div class="stat-label">开始考试人数</div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card shadow="hover">
              <template #header>
                <div class="stat-header">
                  <span>已提交</span>
                </div>
              </template>
              <div class="stat-content">
                <div class="stat-number">{{ examInfo.submittedStudents }}</div>
                <div class="stat-label">提交试卷人数</div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card shadow="hover">
              <template #header>
                <div class="stat-header">
                  <span>参与率</span>
                </div>
              </template>
              <div class="stat-content">
                <div class="stat-number">{{ examInfo.participationRate }}</div>
                <div class="stat-label">缺考人数: {{ examInfo.absentStudents }}</div>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>

      <!-- 考试统计信息 -->
      <div class="exam-stats" v-if="examInfo.examStatus === 2">
        <h3>考试统计</h3>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-card shadow="hover">
              <template #header>
                <div class="stat-header">
                  <span>参考人数</span>
                </div>
              </template>
              <div class="stat-content">
                <div class="stat-number">{{ examStats.totalStudents }}</div>
                <div class="stat-label">总人数</div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="8">
            <el-card shadow="hover">
              <template #header>
                <div class="stat-header">
                  <span>平均分</span>
                </div>
              </template>
              <div class="stat-content">
                <div class="stat-number">{{ examStats.averageScore?.toFixed(1) || 0 }}</div>
                <div class="stat-label">分</div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="8">
            <el-card shadow="hover">
              <template #header>
                <div class="stat-header">
                  <span>及格率</span>
                </div>
              </template>
              <div class="stat-content">
                <div class="stat-number">{{ examStats.passRate || '0%' }}</div>
                <div class="stat-label">及格人数: {{ examStats.passCount || 0 }}</div>
              </div>
            </el-card>
          </el-col>
        </el-row>

        <!-- 分数分布图 -->
        <div class="score-distribution">
          <h3>分数分布</h3>
          <div ref="chartRef" style="width: 100%; height: 400px"></div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import axios from 'axios'
import * as echarts from 'echarts'

const route = useRoute()
const router = useRouter()
const chartRef = ref(null)
let chart = null

// 数据
const examInfo = ref({
  exam: null,
  paper: null,
  totalStudents: 0,
  startedStudents: 0,
  submittedStudents: 0,
  absentStudents: 0,
  participationRate: '0%'
})
const examStats = ref({
  totalStudents: 0,
  averageScore: 0,
  passCount: 0,
  passRate: '0%',
  scoreDistribution: {}
})

// 获取考试详情
const fetchExamDetail = async () => {
  try {
    const examId = route.params.id
    const response = await axios.get(`/api/teacher/exam/${examId}`)
    if (response.data.code === 200) {
      examInfo.value = response.data.data
    } else {
      ElMessage.error(response.data.message || '获取考试详情失败')
    }
  } catch (error) {
    console.error('获取考试详情失败:', error)
    ElMessage.error('获取考试详情失败')
  }
}

// 获取考试统计信息
const fetchExamStats = async () => {
  try {
    const examId = route.params.id
    const response = await axios.get(`/api/teacher/exam/${examId}/stats`)
    if (response.data.code === 200) {
      examStats.value = response.data.data
      if (examStats.value.scoreDistribution) {
        initChart(examStats.value.scoreDistribution)
      }
    } else {
      ElMessage.error(response.data.message || '获取考试统计信息失败')
    }
  } catch (error) {
    console.error('获取考试统计信息失败:', error)
    ElMessage.error('获取考试统计信息失败')
  }
}

// 初始化图表
const initChart = (distribution) => {
  if (!chartRef.value) return

  if (!chart) {
    chart = echarts.init(chartRef.value)
  }

  const scores = Object.keys(distribution).sort((a, b) => Number(a) - Number(b))
  const counts = scores.map(score => distribution[score])

  const option = {
    title: {
      text: '分数分布图'
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    xAxis: {
      type: 'category',
      data: scores,
      name: '分数'
    },
    yAxis: {
      type: 'value',
      name: '人数'
    },
    series: [
      {
        name: '人数',
        type: 'bar',
        data: counts,
        itemStyle: {
          color: '#409EFF'
        }
      }
    ]
  }

  chart.setOption(option)
}

// 格式化考试时间
const formatExamTime = (startTime, endTime) => {
  if (!startTime || !endTime) return '-'
  const start = new Date(startTime)
  const end = new Date(endTime)
  return `${start.toLocaleDateString()} ${start.toLocaleTimeString()} - ${end.toLocaleTimeString()}`
}

// 格式化日期时间
const formatDateTime = (time) => {
  if (!time) return '-'
  const date = new Date(time)
  return `${date.toLocaleDateString()} ${date.toLocaleTimeString()}`
}

// 获取考试类型文本
const getExamTypeText = (type) => {
  const typeMap = {
    0: '普通考试',
    1: '期末考试',
    2: '重考'
  }
  return typeMap[type] || '未知'
}

// 获取考试类型标签样式
const getExamTypeTag = (type) => {
  const typeMap = {
    0: '',
    1: 'success',
    2: 'warning'
  }
  return typeMap[type] || 'info'
}

// 获取考试状态文本
const getStatusText = (status) => {
  const statusMap = {
    0: '未开始',
    1: '进行中',
    2: '已结束'
  }
  return statusMap[status] || '未知'
}

// 获取考试状态类型
const getStatusType = (status) => {
  const typeMap = {
    0: 'info',
    1: 'success',
    2: 'warning'
  }
  return typeMap[status] || 'info'
}

// 编辑考试
const handleEdit = () => {
  router.push(`/teacher/exams/${route.params.id}/edit`)
}

// 返回
const handleBack = () => {
  router.back()
}

// 获取题型名称
const getQuestionTypeName = (type) => {
  const typeMap = {
    0: '单选题',
    1: '多选题',
    2: '判断题',
    3: '填空题',
    4: '简答题'
  }
  return typeMap[type] || '未知'
}

// 获取难度文本
const getDifficultyText = (difficulty) => {
  if (difficulty === undefined || difficulty === null) return '未知'
  if (difficulty <= 0.2) return '简单'
  if (difficulty <= 0.4) return '较易'
  if (difficulty <= 0.6) return '中等'
  if (difficulty <= 0.8) return '较难'
  return '困难'
}

// 获取难度标签类型
const getDifficultyTag = (difficulty) => {
  if (difficulty === undefined || difficulty === null) return 'info'
  if (difficulty <= 0.2) return 'success'
  if (difficulty <= 0.4) return ''
  if (difficulty <= 0.6) return 'warning'
  if (difficulty <= 0.8) return 'danger'
  return 'danger'
}

onMounted(async () => {
  await fetchExamDetail()
  if (examInfo.value.exam?.examStatus === 2) {
    await fetchExamStats()
  }

  // 监听窗口大小变化，重绘图表
  window.addEventListener('resize', () => {
    chart?.resize()
  })
})
</script>

<style lang="scss" scoped>
.exam-detail {
  padding: 20px;
  
  .detail-card {
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
    
    .exam-info {
      margin-bottom: 30px;
    }
    
    .paper-info {
      margin: 30px 0;
      
      h3 {
        margin-bottom: 20px;
        font-weight: 500;
      }

      .questions-list {
        margin-top: 20px;

        h4 {
          margin-bottom: 15px;
          font-weight: 500;
        }
      }
    }
    
    .participation-info {
      margin: 30px 0;
      
      h3 {
        margin-bottom: 20px;
        font-weight: 500;
      }
      
      .stat-content {
        text-align: center;
        
        .stat-number {
          font-size: 24px;
          font-weight: bold;
          color: #409EFF;
          margin-bottom: 8px;
        }
        
        .stat-label {
          color: #666;
        }
      }
    }
    
    .exam-stats {
      margin: 30px 0;
      
      h3 {
        margin-bottom: 20px;
        font-weight: 500;
      }
      
      .stat-content {
        text-align: center;
        
        .stat-number {
          font-size: 24px;
          font-weight: bold;
          color: #409EFF;
          margin-bottom: 8px;
        }
        
        .stat-label {
          color: #666;
        }
      }
    }
    
    .score-distribution {
      margin: 30px 0;
      
      h3 {
        margin-bottom: 20px;
        font-weight: 500;
      }
    }
  }
}
</style> 
<template>
  <div class="score-detail">
    <!-- 考试信息 -->
    <el-card class="exam-info-card">
      <template #header>
        <div class="card-header">
          <span>考试信息</span>
        </div>
      </template>
      
      <el-descriptions :column="3" border>
        <el-descriptions-item label="考试名称">{{ examInfo.examName }}</el-descriptions-item>
        <el-descriptions-item label="考试类型">{{ examTypeMap[examInfo.examType] }}</el-descriptions-item>
        <el-descriptions-item label="班级">{{ examInfo.className }}</el-descriptions-item>
        <el-descriptions-item label="开始时间">{{ formatDateTime(examInfo.startTime) }}</el-descriptions-item>
        <el-descriptions-item label="结束时间">{{ formatDateTime(examInfo.endTime) }}</el-descriptions-item>
        <el-descriptions-item label="考试时长">{{ examInfo.duration }}分钟</el-descriptions-item>
        <el-descriptions-item label="总分">{{ examInfo.totalScore }}分</el-descriptions-item>
        <el-descriptions-item label="及格分">{{ examInfo.passScore }}分</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getExamStatusType(examInfo.status)">
            {{ examStatusMap[examInfo.status] }}
          </el-tag>
        </el-descriptions-item>
      </el-descriptions>
    </el-card>

    <!-- 成绩统计 -->
    <el-card class="stats-card">
      <template #header>
        <div class="card-header">
          <span>成绩统计</span>
        </div>
      </template>
      
      <el-row :gutter="20">
        <el-col :span="6">
          <div class="stat-item">
            <div class="label">平均分</div>
            <div class="value" :style="{ color: getAverageScoreColor(stats.averageScore) }">
              {{ stats.averageScore }}
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-item">
            <div class="label">及格率</div>
            <div class="value" :style="{ color: getPassRateColor(stats.passRate) }">
              {{ stats.passRate }}%
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-item">
            <div class="label">最高分</div>
            <div class="value" :style="{ color: getScoreColor(stats.maxScore) }">
              {{ stats.maxScore }}
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-item">
            <div class="label">最低分</div>
            <div class="value" :style="{ color: getScoreColor(stats.minScore) }">
              {{ stats.minScore }}
            </div>
          </div>
        </el-col>
      </el-row>

      <!-- 成绩分布图 -->
      <div class="chart-container">
        <div class="score-chart" ref="scoreChartRef"></div>
        <div class="question-chart" ref="questionChartRef"></div>
      </div>
    </el-card>

    <!-- 成绩列表 -->
    <el-card class="list-card">
      <template #header>
        <div class="card-header">
          <span>成绩列表</span>
          <div class="header-right">
            <el-button type="primary" @click="handleExport">
              <el-icon><Download /></el-icon>
              导出成绩
            </el-button>
          </div>
        </div>
      </template>

      <el-table
        v-loading="loading"
        :data="scoreList"
        border
        style="width: 100%"
      >
        <el-table-column prop="student.studentId" label="学号" width="120" />
        <el-table-column prop="studentName" label="姓名" width="120" />
        <el-table-column prop="score" label="总分" width="100">
          <template #default="scope">
            <span :style="{ color: getScoreColor(scope.row.score) }">
              {{ scope.row.score }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="rank" label="排名" width="100" />
        <el-table-column label="题目得分" min-width="300">
          <template #default="scope">
            <el-tooltip
              v-for="question in scope.row.questions"
              :key="question.questionId"
              :content="question.questionContent"
              placement="top"
            >
              <el-tag 
                :type="getQuestionScoreType(question.score, question.totalScore)"
                style="margin-right: 8px; margin-bottom: 8px"
              >
                {{ `Q${question.questionId}: ${question.score}/${question.totalScore}` }}
              </el-tag>
            </el-tooltip>
          </template>
        </el-table-column>
        <el-table-column prop="submitTime" label="提交时间" width="180">
          <template #default="scope">
            {{ formatDateTime(scope.row.submitTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="teacherComment" label="教师评语" min-width="200" show-overflow-tooltip>
          <template #default="scope">
            <template v-if="scope.row.editingComment">
              <el-input
                v-model="scope.row.editingCommentText"
                type="textarea"
                :rows="2"
                placeholder="请输入评语"
              />
              <div class="comment-actions">
                <el-button
                  type="primary"
                  link
                  size="small"
                  @click="handleSaveComment(scope.row)"
                >
                  保存
                </el-button>
                <el-button
                  link
                  size="small"
                  @click="handleCancelComment(scope.row)"
                >
                  取消
                </el-button>
              </div>
            </template>
            <template v-else>
              <div class="comment-content">
                <span>{{ scope.row.teacherComment || '-' }}</span>
                <el-button
                  type="primary"
                  link
                  size="small"
                  @click="handleEditComment(scope.row)"
                >
                  修改
                </el-button>
              </div>
            </template>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { useRoute } from 'vue-router'
import { Download } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import axios from 'axios'
import dayjs from 'dayjs'

const route = useRoute()
const examId = route.params.examId
const classId = route.params.classId

// 考试信息
const examInfo = ref({
  examId: '',
  examName: '',
  examType: '',
  className: '',
  startTime: '',
  endTime: '',
  duration: 0,
  totalScore: 0,
  passScore: 0,
  status: ''
})

// 考试类型映射
const examTypeMap = {
  1: '普通考试',
  2: '期末考试',
  3: '补考'
}

// 考试状态映射
const examStatusMap = {
  1: '未开始',
  2: '进行中',
  3: '已结束',
  4: '已批改'
}

// 列表数据
const scoreList = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 统计数据
const stats = ref({
  averageScore: 0,
  passRate: 0,
  maxScore: 0,
  minScore: 0,
  scoreDistribution: [],
  questionStats: []
})

// 图表相关
const scoreChartRef = ref(null)
const questionChartRef = ref(null)
let scoreChart = null
let questionChart = null

// 获取考试信息
const fetchExamInfo = async () => {
  try {
    const response = await axios.get(`/api/teacher/exams/${examId}`)
    if (response.data.code === 200) {
      examInfo.value = response.data.data
    } else {
      ElMessage.error(response.data.message || '获取考试信息失败')
    }
  } catch (error) {
    console.error('获取考试信息失败:', error)
    ElMessage.error('获取考试信息失败')
  }
}

// 获取成绩列表
const fetchScores = async () => {
  loading.value = true
  try {
    const params = {
      examId,
      classId,
      pageNum: currentPage.value,
      pageSize: pageSize.value
    }

    const response = await axios.get('/api/teacher/scores', { params })
    if (response.data.code === 200) {
      scoreList.value = response.data.data.list.map(item => ({
        ...item,
        editingComment: false,
        editingCommentText: item.teacherComment
      }))
      total.value = response.data.data.total
    } else {
      ElMessage.error(response.data.message || '获取成绩列表失败')
    }
  } catch (error) {
    console.error('获取成绩列表失败:', error)
    ElMessage.error('获取成绩列表失败')
  } finally {
    loading.value = false
  }
}

// 获取成绩统计
const fetchStats = async () => {
  try {
    const response = await axios.get(`/api/teacher/scores/stats/${examId}/${classId}`)
    if (response.data.code === 200) {
      stats.value = response.data.data
      renderCharts()
    } else {
      ElMessage.error(response.data.message || '获取成绩统计失败')
    }
  } catch (error) {
    console.error('获取成绩统计失败:', error)
    ElMessage.error('获取成绩统计失败')
  }
}

// 渲染图表
const renderCharts = () => {
  renderScoreChart()
  renderQuestionChart()
}

// 渲染成绩分布图表
const renderScoreChart = () => {
  if (!scoreChartRef.value || !stats.value.scoreDistribution) return
  
  if (scoreChart) {
    scoreChart.dispose()
  }
  
  scoreChart = echarts.init(scoreChartRef.value)
  const option = {
    title: {
      text: '成绩分布',
      left: 'center'
    },
    tooltip: {
      trigger: 'axis',
      formatter: '{b}: {c}人'
    },
    xAxis: {
      type: 'category',
      data: stats.value.scoreDistribution.map(item => item.score_range + '分'),
      axisLabel: {
        interval: 0,
        rotate: 30
      }
    },
    yAxis: {
      type: 'value',
      name: '人数',
      minInterval: 1
    },
    series: [
      {
        data: stats.value.scoreDistribution.map(item => item.count),
        type: 'bar',
        barWidth: '40%',
        itemStyle: {
          color: function(params) {
            const range = parseInt(params.name)
            if (range >= 90) return '#67C23A'  // 优秀
            if (range >= 80) return '#409EFF'  // 良好
            if (range >= 70) return '#E6A23C'  // 中等
            if (range >= 60) return '#F56C6C'  // 及格
            return '#909399'                    // 不及格
          }
        }
      }
    ]
  }
  scoreChart.setOption(option)
}

// 渲染题目得分图表
const renderQuestionChart = () => {
  if (!questionChartRef.value || !stats.value.questionStats) return
  
  if (questionChart) {
    questionChart.dispose()
  }
  
  questionChart = echarts.init(questionChartRef.value)
  const option = {
    title: {
      text: '题目得分情况',
      left: 'center'
    },
    tooltip: {
      trigger: 'axis',
      formatter: function(params) {
        const data = params[0]
        return `${data.name}<br/>平均得分：${data.value}分<br/>得分率：${data.rate}%`
      }
    },
    xAxis: {
      type: 'category',
      data: stats.value.questionStats.map(item => `Q${item.questionId}`),
      axisLabel: {
        interval: 0
      }
    },
    yAxis: {
      type: 'value',
      name: '平均得分',
      max: function(value) {
        return Math.ceil(value.max)
      }
    },
    series: [
      {
        data: stats.value.questionStats.map(item => ({
          value: item.averageScore,
          rate: ((item.averageScore / item.totalScore) * 100).toFixed(1),
          itemStyle: {
            color: getQuestionScoreColor(item.averageScore, item.totalScore)
          }
        })),
        type: 'bar',
        barWidth: '40%'
      }
    ]
  }
  questionChart.setOption(option)
}

// 分页
const handleSizeChange = (val) => {
  pageSize.value = val
  fetchScores()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchScores()
}

// 编辑评语
const handleEditComment = (row) => {
  row.editingComment = true
  row.editingCommentText = row.teacherComment
}

// 保存评语
const handleSaveComment = async (row) => {
  try {
    const response = await axios.put('/api/teacher/scores/comment', {
      examId,
      studentId: row.student.studentId,
      comment: row.editingCommentText
    })
    
    if (response.data.code === 200) {
      row.teacherComment = row.editingCommentText
      row.editingComment = false
      ElMessage.success('评语更新成功')
    } else {
      ElMessage.error(response.data.message || '评语更新失败')
    }
  } catch (error) {
    console.error('保存评语失败:', error)
    ElMessage.error('保存评语失败')
  }
}

// 取消编辑评语
const handleCancelComment = (row) => {
  row.editingComment = false
}

// 导出成绩
const handleExport = async () => {
  try {
    const response = await axios.get('/api/teacher/scores/export', {
      params: {
        examId,
        classId
      },
      responseType: 'blob'
    })
    
    const url = window.URL.createObjectURL(new Blob([response.data]))
    const link = document.createElement('a')
    link.href = url
    link.setAttribute('download', `${examInfo.value.examName}-${examInfo.value.className}-成绩表.xlsx`)
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    
    ElMessage.success('成绩导出成功')
  } catch (error) {
    console.error('成绩导出失败:', error)
    ElMessage.error('成绩导出失败')
  }
}

// 获取考试状态类型
const getExamStatusType = (status) => {
  switch (status) {
    case 1: return 'info'
    case 2: return 'warning'
    case 3: return 'success'
    case 4: return ''
    default: return 'info'
  }
}

// 获取成绩颜色
const getScoreColor = (score) => {
  if (score >= 90) return '#67C23A'
  if (score >= 80) return '#409EFF'
  if (score >= 70) return '#E6A23C'
  if (score >= 60) return '#F56C6C'
  return '#909399'
}

// 获取平均分颜色
const getAverageScoreColor = (score) => {
  return getScoreColor(score)
}

// 获取及格率颜色
const getPassRateColor = (rate) => {
  if (rate >= 90) return '#67C23A'
  if (rate >= 80) return '#409EFF'
  if (rate >= 70) return '#E6A23C'
  if (rate >= 60) return '#F56C6C'
  return '#909399'
}

// 获取题目得分类型
const getQuestionScoreType = (score, totalScore) => {
  const rate = (score / totalScore) * 100
  if (rate >= 90) return 'success'
  if (rate >= 70) return 'warning'
  if (rate >= 60) return ''
  return 'danger'
}

// 获取题目得分颜色
const getQuestionScoreColor = (score, totalScore) => {
  const rate = (score / totalScore) * 100
  if (rate >= 90) return '#67C23A'
  if (rate >= 70) return '#E6A23C'
  if (rate >= 60) return '#F56C6C'
  return '#909399'
}

// 格式化日期时间
const formatDateTime = (datetime) => {
  if (!datetime) return '-'
  return dayjs(datetime).format('YYYY-MM-DD HH:mm:ss')
}

// 监听窗口大小变化
const handleResize = () => {
  if (scoreChart) {
    scoreChart.resize()
  }
  if (questionChart) {
    questionChart.resize()
  }
}

window.addEventListener('resize', handleResize)

onMounted(() => {
  Promise.all([
    fetchExamInfo(),
    fetchScores(),
    fetchStats()
  ])
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  if (scoreChart) {
    scoreChart.dispose()
  }
  if (questionChart) {
    questionChart.dispose()
  }
})
</script>

<style lang="scss" scoped>
.score-detail {
  padding: 20px;
  
  .exam-info-card,
  .stats-card,
  .list-card {
    margin-bottom: 20px;
  }
  
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    
    .header-right {
      display: flex;
      gap: 16px;
    }
  }
  
  .stats-card {
    .stat-item {
      text-align: center;
      padding: 20px;
      
      .label {
        font-size: 14px;
        color: #909399;
        margin-bottom: 8px;
      }
      
      .value {
        font-size: 24px;
        font-weight: bold;
      }
    }
    
    .chart-container {
      display: flex;
      gap: 20px;
      margin-top: 20px;
      
      .score-chart,
      .question-chart {
        flex: 1;
        height: 400px;
      }
    }
  }
  
  .list-card {
    .comment-content {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
    
    .comment-actions {
      margin-top: 8px;
      text-align: right;
    }
    
    .pagination {
      margin-top: 20px;
      display: flex;
      justify-content: flex-end;
    }
  }
}
</style> 
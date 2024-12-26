<template>
  <div class="paper-detail">
    <el-card>
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <el-button
              type="text"
              class="back-button"
              @click="handleBack"
            >
              <el-icon><ArrowLeft /></el-icon>
              返回
            </el-button>
            <el-divider direction="vertical" />
            <span class="title">试卷详情</span>
          </div>
        </div>
      </template>

      <!-- 基本信息 -->
      <el-descriptions title="基本信息" :column="2" border>
        <el-descriptions-item label="试卷名称">{{ paper.paperName }}</el-descriptions-item>
        <el-descriptions-item label="所属学科">{{ paper.subject?.subjectName }}</el-descriptions-item>
        <el-descriptions-item label="考试类型">
          <el-tag :type="paper.examType === 0 ? 'primary' : 'warning'">
            {{ paper.examTypeName }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="试卷状态">
          <el-tag :type="paper.paperStatus === 0 ? 'info' : 'success'">
            {{ paper.statusName }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="学年">{{ paper.year }} 学年</el-descriptions-item>
        <el-descriptions-item label="学期">{{ paper.semester === 1 ? '第一学期' : '第二学期' }}</el-descriptions-item>
        <el-descriptions-item label="试卷难度">
          <el-progress
            :percentage="paper.paperDifficulty * 100"
            :status="getDifficultyStatus(paper.paperDifficulty)"
          />
        </el-descriptions-item>
        <el-descriptions-item label="总分">{{ paper.totalScore }}</el-descriptions-item>
      </el-descriptions>

      <!-- 题目列表 -->
      <div class="questions-section">
        <div class="section-header">
          <h3>试题列表</h3>
        </div>

        <el-table :data="questions" border style="width: 100%">
          <el-table-column type="index" label="序号" width="60" />
          <el-table-column prop="content" label="题目内容" show-overflow-tooltip />
          <el-table-column prop="typeName" label="题型" width="100" />
          <el-table-column prop="score" label="分值" width="80" />
          <el-table-column label="难度" width="120">
            <template #default="scope">
              <el-progress
                :percentage="scope.row.difficulty * 100"
                :status="getDifficultyStatus(scope.row.difficulty)"
              />
            </template>
          </el-table-column>
          <el-table-column label="操作" width="100" fixed="right">
            <template #default="scope">
              <el-button
                type="primary"
                link
                @click="handleViewQuestion(scope.row)"
              >
                查看
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-card>

    <!-- 题目详情对话框 -->
    <el-dialog
      v-model="questionDialogVisible"
      title="题目详情"
      width="800px"
    >
      <el-descriptions :column="2" border>
        <el-descriptions-item label="题目类型">{{ currentQuestion.typeName }}</el-descriptions-item>
        <el-descriptions-item label="分值">{{ currentQuestion.score }} 分</el-descriptions-item>
        <el-descriptions-item label="难度">
          <el-progress
            :percentage="(currentQuestion.difficulty || 0) * 100"
            :status="getDifficultyStatus(currentQuestion.difficulty || 0)"
          />
        </el-descriptions-item>
      </el-descriptions>
      
      <div class="question-content">
        <h3>题目内容：</h3>
        <p>{{ currentQuestion.content }}</p>
      </div>

      <div class="question-options" v-if="[0, 1, 2].includes(currentQuestion.type)">
        <h3>选项：</h3>
        <!-- 单选题 -->
        <el-radio-group v-if="currentQuestion.type === 0" v-model="currentQuestion.answer" disabled>
          <el-radio
            v-for="option in currentQuestion.options"
            :key="option.optionId"
            :label="option.optionId"
            border
            :class="{ 'correct-option': option.isCorrect }"
          >
            {{ option.content }}
            <el-tag
              v-if="option.isCorrect"
              type="success"
              size="small"
              style="margin-left: 8px"
            >
              正确答案
            </el-tag>
          </el-radio>
        </el-radio-group>

        <!-- 多选题 -->
        <el-checkbox-group v-else-if="currentQuestion.type === 1" v-model="currentQuestion.answer" disabled>
          <el-checkbox
            v-for="option in currentQuestion.options"
            :key="option.optionId"
            :label="option.optionId"
            border
            :class="{ 'correct-option': option.isCorrect }"
          >
            {{ option.content }}
            <el-tag
              v-if="option.isCorrect"
              type="success"
              size="small"
              style="margin-left: 8px"
            >
              正确答案
            </el-tag>
          </el-checkbox>
        </el-checkbox-group>

        <!-- 判断题 -->
        <el-radio-group v-else v-model="currentQuestion.answer" disabled>
          <el-radio
            v-for="option in currentQuestion.options"
            :key="option.optionId"
            :label="option.optionId"
            border
            :class="{ 'correct-option': option.isCorrect }"
          >
            {{ option.content }}
            <el-tag
              v-if="option.isCorrect"
              type="success"
              size="small"
              style="margin-left: 8px"
            >
              正确答案
            </el-tag>
          </el-radio>
        </el-radio-group>
      </div>

      <div class="question-answer" v-if="[3, 4].includes(currentQuestion.type)">
        <h3>答案：</h3>
        <p>{{ currentQuestion.answer }}</p>
      </div>
    </el-dialog>

    <!-- 成绩分布对话框 -->
    <el-dialog
      v-model="scoreDialogVisible"
      title="成绩分布"
      width="800px"
    >
      <el-descriptions :column="2" border>
        <el-descriptions-item label="最高分">{{ scoreDistribution.highestScore }}</el-descriptions-item>
        <el-descriptions-item label="最低分">{{ scoreDistribution.lowestScore }}</el-descriptions-item>
        <el-descriptions-item label="平均分">
          <span :style="{ color: getAverageScoreColor(averageScore) }">
            {{ averageScore ? averageScore.toFixed(2) : '暂无' }}
          </span>
        </el-descriptions-item>
      </el-descriptions>

      <div class="score-chart" ref="chartRef"></div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from 'axios'
import * as echarts from 'echarts'
import { ArrowLeft } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()

// 试卷数据
const paper = ref({})
const questions = ref([])

// 对话框控制
const questionDialogVisible = ref(false)
const scoreDialogVisible = ref(false)
const currentQuestion = ref({})

// 成绩分布相关
const scoreDistribution = ref({
  distribution: [],
  highestScore: 0,
  lowestScore: 0
})
const chartRef = ref(null)
let chart = null

// 添加平均分数据
const averageScore = ref(null)

// 获取平均分颜色
const getAverageScoreColor = (score) => {
  if (!score) return '#909399'
  if (score >= 90) return '#67C23A'
  if (score >= 80) return '#409EFF'
  if (score >= 70) return '#E6A23C'
  if (score >= 60) return '#F56C6C'
  return '#909399'
}

// 获取试卷详情
const fetchPaperDetail = async () => {
  try {
    const response = await axios.get(`/api/teacher/papers/${route.params.id}`)
    if (response.data.code === 200) {
      const paperData = response.data.data.paper
      // 从academicTerm解析学年和学期
      const academicDate = new Date(paperData.academicTerm)
      paperData.year = academicDate.getFullYear()
      paperData.semester = academicDate.getMonth() === 0 ? 1 : 2 // 1月为第一学期，6月为第二学期
      
      paper.value = paperData
      // 处理试题数据
      questions.value = response.data.data.questions.map(item => {
        const question = item.question || {}
        return {
          ...question,
          score: item.score,
          typeName: getQuestionTypeName(question.type),
          options: question.options || []
        }
      })
    } else {
      ElMessage.error(response.data.message || '获取试卷详情失败')
    }
  } catch (error) {
    console.error('获取试卷详情失败:', error)
    ElMessage.error('获取试卷详情失败')
  }
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
  return typeMap[type] || '未知题型'
}

// 获取难度状态
const getDifficultyStatus = (difficulty) => {
  if (difficulty <= 0.3) return 'success'
  if (difficulty <= 0.7) return 'warning'
  return 'exception'
}

// 查看题目详情
const handleViewQuestion = (question) => {
  currentQuestion.value = question
  questionDialogVisible.value = true
}

// 编辑试卷
const handleEdit = () => {
  router.push(`/teacher/papers/${route.params.id}/edit`)
}

// 查看成绩分布
const handleViewScore = async () => {
  try {
    // 并行请求成绩分布和平均分
    const [distributionRes, averageRes] = await Promise.all([
      axios.get(`/api/teacher/papers/${route.params.id}/score-distribution`),
      axios.get(`/api/teacher/papers/${route.params.id}/average-score`)
    ])

    if (distributionRes.data.code === 200) {
      scoreDistribution.value = distributionRes.data.data
      scoreDialogVisible.value = true
      
      // 检查是否有成绩数据
      if (!scoreDistribution.value.distribution || scoreDistribution.value.distribution.length === 0) {
        ElMessage.info('暂无考试成绩数据')
        return
      }
      
      // 设置平均分
      if (averageRes.data.code === 200) {
        averageScore.value = averageRes.data.data.averageScore
      }

      // 在下一个 tick 渲染图表
      setTimeout(() => {
        renderChart()
      }, 0)
    } else {
      ElMessage.error(distributionRes.data.message || '获取成绩分布失败')
    }
  } catch (error) {
    console.error('获取成绩数据失败:', error)
    ElMessage.error('获取成绩数据失败')
  }
}

// 渲染成绩分布图表
const renderChart = () => {
  if (!chartRef.value) return
  
  if (chart) {
    chart.dispose()
  }
  
  // 对分数段进行排序
  const sortedData = [...scoreDistribution.value.distribution].sort((a, b) => {
    const aStart = parseInt(a.score_range.split('-')[0])
    const bStart = parseInt(b.score_range.split('-')[0])
    return bStart - aStart // 降序排列
  })
  
  chart = echarts.init(chartRef.value)
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
      data: sortedData.map(item => item.score_range + '分'),
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
        data: sortedData.map(item => item.count),
        type: 'bar',
        barWidth: '40%',
        itemStyle: {
          color: function(params) {
            const range = parseInt(params.name.split('-')[0])
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
  chart.setOption(option)
}

// 监听对话框关闭
watch(scoreDialogVisible, (val) => {
  if (!val) {
    if (chart) {
      chart.dispose()
      chart = null
    }
    // 重置平均分
    averageScore.value = null
  }
})

// 返回上一页
const handleBack = () => {
  router.push('/teacher/papers')
}

onMounted(() => {
  fetchPaperDetail()
})
</script>

<style lang="scss" scoped>
.paper-detail {
  padding: 20px;
  
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    
    .header-left {
      display: flex;
      align-items: center;
      gap: 8px;
      
      .back-button {
        display: flex;
        align-items: center;
        font-size: 14px;
        color: #409EFF;
        padding: 0;
        
        .el-icon {
          margin-right: 4px;
        }
        
        &:hover {
          color: #79bbff;
        }
      }
      
      .title {
        font-size: 16px;
        font-weight: 500;
        color: #303133;
      }
    }
  }
  
  .questions-section {
    margin-top: 20px;
    
    .section-header {
      margin-bottom: 16px;
      
      h3 {
        margin: 0;
      }
    }
  }
  
  .question-content,
  .question-options,
  .question-answer {
    margin: 20px 0;
    
    h3 {
      margin-bottom: 10px;
      font-weight: bold;
    }
    
    .el-radio,
    .el-checkbox {
      margin: 8px 0;
      width: 100%;
      
      &.correct-option {
        :deep(.el-radio__label),
        :deep(.el-checkbox__label) {
          color: #67C23A;
          font-weight: bold;
        }
        
        :deep(.el-radio__input.is-disabled.is-checked .el-radio__inner),
        :deep(.el-checkbox__input.is-disabled.is-checked .el-checkbox__inner) {
          background-color: #67C23A;
          border-color: #67C23A;
        }
      }
    }
  }
  
  .score-chart {
    height: 400px;
    margin-top: 20px;
  }
}
</style> 
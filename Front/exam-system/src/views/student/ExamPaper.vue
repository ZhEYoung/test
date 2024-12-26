<template>
  <div class="exam-paper">
    <!-- 考试信息和计时器 -->
    <div class="exam-header">
      <div class="exam-info">
        <h2>{{ examData.exam?.examName }}</h2>
        <div class="exam-meta">
          <span>总分：{{ examData.paper?.totalScore || 100 }}分</span>
          <span>时长：{{ examData.exam?.examDuration }}分钟</span>
        </div>
      </div>
      <div class="timer" v-if="remainingTime > 0">
        <el-alert
          :title="`剩余时间：${formatTime(remainingTime)}`"
          :type="remainingTime < 300 ? 'warning' : 'info'"
          :closable="false"
          center
        />
      </div>
    </div>

    <!-- 答题区域 -->
    <div class="exam-content" v-loading="loading">
      <el-tabs v-model="activeQuestion" type="card" class="question-tabs">
        <el-tab-pane
          v-for="(question, index) in examData.paper?.questions"
          :key="question.questionId"
          :label="`第${index + 1}题`"
          :name="question.questionId.toString()"
        >
          <div class="question-content">
            <!-- 题目内容 -->
            <div class="question-title">
              <span class="question-type">
                {{ getQuestionType(question.type) }}
                ({{ question.score }}分)
              </span>
              <div class="question-text" v-html="question.content"></div>
            </div>

            <!-- 答题区域 -->
            <div class="answer-area">
              <!-- 单选题 -->
              <template v-if="question.type === 0">
                <el-radio-group 
                  v-model="answers[question.questionId]"
                  @change="val => handleAnswerChange(question, val)"
                >
                  <el-radio
                    v-for="option in question.options"
                    :key="option.optionId"
                    :label="option.optionId.toString()"
                  >
                    {{ option.content }}
                  </el-radio>
                </el-radio-group>
              </template>

              <!-- 多选题 -->
              <template v-else-if="question.type === 1">
                <el-checkbox-group
                  v-model="answers[question.questionId]"
                  @change="val => handleAnswerChange(question, val)"
                >
                  <el-checkbox
                    v-for="option in question.options"
                    :key="option.optionId"
                    :label="option.optionId.toString()"
                  >
                    {{ option.content }}
                  </el-checkbox>
                </el-checkbox-group>
              </template>

              <!-- 判断题 -->
              <template v-else-if="question.type === 2">
                <el-radio-group
                  v-model="answers[question.questionId]"
                  @change="val => handleAnswerChange(question, val)"
                >
                  <el-radio label="1">是</el-radio>
                  <el-radio label="2">否</el-radio>
                </el-radio-group>
              </template>

              <!-- 填空题 -->
              <template v-else-if="question.type === 3">
                <el-input
                  v-model="answers[question.questionId]"
                  type="textarea"
                  :rows="3"
                  placeholder="请输入答案"
                  @change="val => handleAnswerChange(question, val)"
                />
              </template>

              <!-- 简答题 -->
              <template v-else-if="question.type === 4">
                <el-input
                  v-model="answers[question.questionId]"
                  type="textarea"
                  :rows="6"
                  placeholder="请输入答案"
                  @change="val => handleAnswerChange(question, val)"
                />
              </template>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>

      <!-- 答题卡 -->
      <div class="answer-card">
        <h3>答题卡</h3>
        <el-row :gutter="10">
          <el-col 
            v-for="(question, index) in examData.paper?.questions"
            :key="question.questionId"
            :span="4"
          >
            <el-button
              :type="answers[question.questionId] ? 'primary' : 'info'"
              @click="activeQuestion = question.questionId.toString()"
            >
              {{ index + 1 }}
            </el-button>
          </el-col>
        </el-row>
      </div>
    </div>

    <!-- 底部操作栏 -->
    <div class="exam-footer">
      <el-button @click="prevQuestion" :disabled="!canGoPrev">上一题</el-button>
      <el-button @click="nextQuestion" :disabled="!canGoNext">下一题</el-button>
      <el-button type="primary" @click="handleSubmit">提交试卷</el-button>
    </div>

    <!-- 提交确认对话框 -->
    <el-dialog
      v-model="submitDialogVisible"
      title="确认提交"
      width="30%"
    >
      <span>确定要提交试卷吗？提交后将无法修改答案。</span>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="submitDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitExam">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from 'axios'

const route = useRoute()
const router = useRouter()
const examId = route.params.examId

// 数据
const loading = ref(false)
const examData = ref({})
const answers = ref({})
const activeQuestion = ref('')
const remainingTime = ref(0)
const submitDialogVisible = ref(false)
let timer = null

// 获取考试数据
const fetchExamData = async () => {
  loading.value = true
  try {
    const response = await axios.post(`/api/student/exam/${examId}/start`)
    if (response.data.code === 200) {
      examData.value = response.data.data
      // 初始化第一题
      if (examData.value.paper?.questions?.length > 0) {
        activeQuestion.value = examData.value.paper.questions[0].questionId.toString()
        // 初始化答案，特别处理多选题
        examData.value.paper.questions.forEach(question => {
          if (question.type === 1) { // 多选题
            answers.value[question.questionId] = []
          }
        })
      }
    } else {
      ElMessage.error(response.data.message || '获取考试数据失败')
      router.push('/student/exams')
    }
  } catch (error) {
    console.error('获取考试数据失败:', error)
    ElMessage.error('获取考试数据失败')
    router.push('/student/exams')
  } finally {
    loading.value = false
  }
}

// 获取剩余时间
const fetchRemainingTime = async () => {
  try {
    const response = await axios.get(`/api/student/exam/${examId}/remaining-time`)
    if (response.data.code === 200) {
      const { status, remainingTime: time } = response.data.data
      if (status === 'TIME_UP' || status === 'ENDED') {
        clearInterval(timer)
        ElMessageBox.alert('考试时间已到，系统将自动提交试卷', '提示', {
          confirmButtonText: '确定',
          callback: () => {
            submitExam()
          }
        })
      } else {
        remainingTime.value = time
      }
    }
  } catch (error) {
    console.error('获取剩余时间失败:', error)
  }
}

// 提交答案
const submitAnswer = async (question, answer) => {
  try {
    const url = question.type <= 2 
      ? `/api/student/exam/${examId}/objective-answer`
      : `/api/student/exam/${examId}/subjective-answer`
    
    const response = await axios.post(url, {
      questionId: question.questionId,
      answer
    })
    
    if (response.data.code !== 200) {
      ElMessage.error(response.data.message || '保存答案失败')
    }
  } catch (error) {
    console.error('保存答案失败:', error)
    ElMessage.error('保存答案失败')
  }
}

// 答案变化处理
const handleAnswerChange = (question, value) => {
  if (question.type === 1) { // 多选题
    // 确保answers中的值是数组
    if (!Array.isArray(answers.value[question.questionId])) {
      answers.value[question.questionId] = []
    }
    answers.value[question.questionId] = Array.isArray(value) ? value : value.split(',').filter(Boolean)
    // 提交时将数组转换为逗号分隔的字符串
    submitAnswer(question, answers.value[question.questionId].join(','))
  } else {
    answers.value[question.questionId] = value
    submitAnswer(question, value)
  }
}

// 提交试卷
const submitExam = async () => {
  try {
    const response = await axios.post(`/api/student/exam/${examId}/submit`)
    if (response.data.code === 200) {
      ElMessage.success('试卷提交成功')
      router.push('/student/exams')
    } else {
      ElMessage.error(response.data.message || '提交试卷失败')
    }
  } catch (error) {
    console.error('提交试卷失败:', error)
    ElMessage.error('提交试卷失败')
  }
}

// 上一题
const prevQuestion = () => {
  const questions = examData.value.paper?.questions || []
  const currentIndex = questions.findIndex(q => q.questionId.toString() === activeQuestion.value)
  if (currentIndex > 0) {
    activeQuestion.value = questions[currentIndex - 1].questionId.toString()
  }
}

// 下一题
const nextQuestion = () => {
  const questions = examData.value.paper?.questions || []
  const currentIndex = questions.findIndex(q => q.questionId.toString() === activeQuestion.value)
  if (currentIndex < questions.length - 1) {
    activeQuestion.value = questions[currentIndex + 1].questionId.toString()
  }
}

// 获取题目类型
const getQuestionType = (type) => {
  const typeMap = {
    0: '单选题',
    1: '多选题',
    2: '判断题',
    3: '填空题',
    4: '简答题'
  }
  return typeMap[type] || '未知类型'
}

// 格式化时间
const formatTime = (seconds) => {
  const hours = Math.floor(seconds / 3600)
  const minutes = Math.floor((seconds % 3600) / 60)
  const secs = seconds % 60
  
  return `${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`
}

// 计算属性
const canGoPrev = computed(() => {
  const questions = examData.value.paper?.questions || []
  const currentIndex = questions.findIndex(q => q.questionId.toString() === activeQuestion.value)
  return currentIndex > 0
})

const canGoNext = computed(() => {
  const questions = examData.value.paper?.questions || []
  const currentIndex = questions.findIndex(q => q.questionId.toString() === activeQuestion.value)
  return currentIndex < questions.length - 1
})

// 提交试卷处理
const handleSubmit = () => {
  const questions = examData.value.paper?.questions || []
  const unansweredQuestions = questions.filter(q => !answers.value[q.questionId])
  
  if (unansweredQuestions.length > 0) {
    const unansweredInfo = unansweredQuestions
      .map((q, index) => `第${questions.indexOf(q) + 1}题`)
      .join('、')
    
    ElMessageBox.confirm(
      `还有${unansweredQuestions.length}道题目未作答（${unansweredInfo}），确定要提交吗？`,
      '提示',
      {
        confirmButtonText: '确定提交',
        cancelButtonText: '继续答题',
        type: 'warning'
      }
    ).then(() => {
      submitDialogVisible.value = true
    }).catch(() => {})
  } else {
    submitDialogVisible.value = true
  }
}

// 生命周期钩子
onMounted(() => {
  fetchExamData()
  fetchRemainingTime()
  // 每秒更新剩余时间
  timer = setInterval(() => {
    if (remainingTime.value > 0) {
      remainingTime.value--
    }
    // 每分钟从服务器获取一次剩余时间
    if (remainingTime.value % 60 === 0) {
      fetchRemainingTime()
    }
  }, 1000)
})

onBeforeUnmount(() => {
  if (timer) {
    clearInterval(timer)
  }
})
</script>

<style lang="scss" scoped>
.exam-paper {
  padding: 20px;
  height: calc(100vh - 100px);
  display: flex;
  flex-direction: column;
  
  .exam-header {
    background-color: #fff;
    padding: 20px;
    border-radius: 4px;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
    margin-bottom: 20px;
    display: flex;
    justify-content: space-between;
    align-items: center;
    
    .exam-info {
      h2 {
        margin: 0 0 10px 0;
        font-size: 20px;
      }
      
      .exam-meta {
        color: #606266;
        font-size: 14px;
        
        span {
          margin-right: 20px;
        }
      }
    }
    
    .timer {
      width: 200px;
    }
  }
  
  .exam-content {
    flex: 1;
    background-color: #fff;
    padding: 20px;
    border-radius: 4px;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
    overflow-y: auto;
    margin-bottom: 20px;
    
    .question-content {
      padding: 20px;
      
      .question-title {
        margin-bottom: 20px;
        
        .question-type {
          display: inline-block;
          padding: 2px 8px;
          background-color: #f0f9eb;
          color: #67c23a;
          border-radius: 4px;
          margin-bottom: 10px;
        }
        
        .question-text {
          font-size: 16px;
          line-height: 1.6;
          color: #303133;
        }
      }
      
      .answer-area {
        padding: 20px;
        background-color: #f5f7fa;
        border-radius: 4px;
        
        :deep(.el-radio), :deep(.el-checkbox) {
          margin-bottom: 10px;
          display: block;
        }
      }
    }
  }
  
  .answer-card {
    margin-top: 20px;
    padding: 20px;
    background-color: #f5f7fa;
    border-radius: 4px;
    
    h3 {
      margin: 0 0 15px 0;
      font-size: 16px;
      color: #303133;
    }
    
    .el-button {
      margin: 5px;
      min-width: 40px;
    }
  }
  
  .exam-footer {
    padding: 20px;
    background-color: #fff;
    border-radius: 4px;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
    display: flex;
    justify-content: center;
    gap: 20px;
  }
}

:deep(.question-tabs .el-tabs__content) {
  padding: 20px;
}
</style> 
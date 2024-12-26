<template>
  <div class="grading-detail">
    <!-- 考试信息 -->
    <el-card class="exam-info">
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <el-button type="primary" link @click="router.back()">
              <el-icon><ArrowLeft /></el-icon>
              返回
            </el-button>
            <el-divider direction="vertical" />
            <span>考试信息</span>
          </div>
        </div>
      </template>
      <el-descriptions :column="3" border>
        <el-descriptions-item label="考试名称">{{ examInfo.examName }}</el-descriptions-item>
        <el-descriptions-item label="考试类型">
          <el-tag :type="examInfo.examType === 0 ? 'primary' : 'warning'">
            {{ examInfo.examType === 0 ? '普通考试' : '重考考试' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="考试时间">
          {{ formatExamTime(examInfo.examStartTime, examInfo.examEndTime) }}
        </el-descriptions-item>
        <el-descriptions-item label="考试状态">
          <el-tag :type="getStatusType(examInfo.examStatus)">
            {{ getStatusText(examInfo.examStatus) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="待批改数量">
          <el-tag :type="getUngradedCount > 0 ? 'danger' : 'success'" effect="plain">
            {{ getUngradedCount }}
          </el-tag>
        </el-descriptions-item>
      </el-descriptions>
    </el-card>

    <!-- 待批改试题列表 -->
    <el-card class="questions-list" v-loading="loading">
      <template #header>
        <div class="card-header">
          <span>待批改试题</span>
        </div>
      </template>

      <div v-if="pendingQuestions.length === 0" class="empty-state">
        <el-empty description="暂无待批改试题" />
      </div>

      <div v-else class="questions-container">
        <div v-for="question in pendingQuestions" :key="question.recordId" class="question-item">
          <div class="question-header">
            <div class="student-info">
 

              <span>状态：<el-tag size="small" :type="question.status === 1 ? 'success' : 'warning'">{{ question.statusName }}</el-tag></span>
            </div>
          </div>
          <div class="question-content">
            <div class="question-text">
              <div class="label">试题内容：</div>
              <div class="content" v-html="question.questionContent"></div>
            </div>

            <div class="answer-text">
              <div class="label">标准答案：</div>
              <div class="content" v-html="question.correctAnswer"></div>
            </div>

            <div class="answer-text">
              <div class="label">学生答案：</div>
              <div class="content" :class="{ 'empty-answer': !question.answer?.trim() }">
                <template v-if="question.answer?.trim()">
                  {{ question.answer }}
                </template>
                <template v-else>
                  <el-alert
                    title="学生未作答"
                    type="warning"
                    :closable="false"
                    show-icon
                  />
                </template>
              </div>
            </div>

            <div class="score-input">
              <span class="label">得分：</span>
              <el-input-number
                v-model="question.score"
                :min="0"
                :max="question.fullScore"
                :step="0.5"
                size="small"
                style="width: 120px"
              />
              <span class="full-score">满分：{{ question.fullScore }}分</span>
              <el-button
                type="primary"
                size="small"
                @click="handleGrade(question)"
                :loading="question.grading"
              >
                {{ question.status === 1 ? '修改评分' : '提交评分' }}
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from 'axios'
import { ArrowLeft } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()

// 数据
const examInfo = ref({})
const pendingQuestions = ref([])
const loading = ref(false)

// 获取考试信息
const fetchExamInfo = async () => {
  try {
    const response = await axios.get(`/api/teacher/exam/${route.params.examId}`)
    if (response.data.code === 200) {
      examInfo.value = response.data.data.exam
    } else {
      ElMessage.error(response.data.message || '获取考试信息失败')
    }
  } catch (error) {
    console.error('获取考试信息失败:', error)
    ElMessage.error('获取考试信息失败')
  }
}

// 获取待批改试题
const fetchPendingQuestions = async () => {
  loading.value = true
  try {
    const response = await axios.get(`/api/teacher/grading/pending/${route.params.examId}`)
    if (response.data.code === 200) {
      pendingQuestions.value = response.data.data.map(item => ({
        ...item,
        grading: false
      }))
    } else {
      ElMessage.error(response.data.message || '获取待批改试题失败')
    }
  } catch (error) {
    console.error('获取待批改试题失败:', error)
    ElMessage.error('获取待批改试题失败')
  } finally {
    loading.value = false
  }
}

// 提交评分
const handleGrade = async (question) => {
  if (question.status === 1) {
    // 如果是已批改的题目，弹出确认框
    try {
      await ElMessageBox.confirm(
        '确定要修改该题目的分数吗？',
        '提示',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }
      )
    } catch (error) {
      return // 用户取消修改
    }
  }

  question.grading = true
  try {
    const response = await axios.post(`/api/teacher/grading/grade/${question.recordId}`, null, {
      params: {
        score: question.score,
        status: '1'
      }
    })
    
    if (response.data.code === 200) {
      ElMessage.success(question.status === 1 ? '修改成功' : '评分成功')
      // 更新题目状态
      question.status = 1
      question.statusName = '已批改'
    } else {
      ElMessage.error(response.data.message || '操作失败')
    }
  } catch (error) {
    console.error('操作失败:', error)
    ElMessage.error('操作失败')
  } finally {
    question.grading = false
  }
}

// 格式化考试时间
const formatExamTime = (startTime, endTime) => {
  if (!startTime || !endTime) return ''
  const start = new Date(startTime)
  const end = new Date(endTime)
  return `${start.toLocaleDateString()} ${start.toLocaleTimeString()} - ${end.toLocaleTimeString()}`
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

// 计算未批改的题目数量
const getUngradedCount = computed(() => {
  return pendingQuestions.value.filter(q => q.status !== 1).length
})

onMounted(() => {
  fetchExamInfo()
  fetchPendingQuestions()
})
</script>

<style lang="scss" scoped>
.grading-detail {
  padding: 20px;
  
  .exam-info {
    margin-bottom: 20px;

    .card-header {
      .header-left {
        display: flex;
        align-items: center;
        gap: 8px;
      }
    }
  }
  
  .questions-list {
    margin-bottom: 20px;
    
    .empty-state {
      padding: 40px 0;
    }
    
    .questions-container {
      .question-item {
        border: 1px solid #dcdfe6;
        border-radius: 4px;
        margin-bottom: 20px;
        padding: 20px;
        
        &:last-child {
          margin-bottom: 0;
        }
        
        .question-header {
          margin-bottom: 16px;
          padding-bottom: 16px;
          border-bottom: 1px solid #ebeef5;
          
          .student-info {
            display: flex;
            align-items: center;
            gap: 8px;
            color: #606266;
            font-size: 14px;
          }
        }
        
        .question-content {
          .question-text,
          .answer-text {
            margin-bottom: 16px;
            
            .label {
              font-weight: bold;
              margin-bottom: 8px;
            }
            
            .content {
              padding: 12px;
              background-color: #f5f7fa;
              border-radius: 4px;
              
              :deep(img) {
                max-width: 100%;
              }

              &.empty-answer {
                padding: 0;
                background-color: transparent;

                .el-alert {
                  margin: 0;
                }
              }
            }
          }
          
          .score-input {
            display: flex;
            align-items: center;
            gap: 12px;
            
            .label {
              font-weight: bold;
            }
            
            .full-score {
              color: #909399;
            }
          }
        }
      }
    }
  }
}
</style>
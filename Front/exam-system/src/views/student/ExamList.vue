<template>
  <div class="exam-list">
    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-form :inline="true" :model="searchForm" class="form">
        <el-form-item label="考试类型">
          <el-select v-model="searchForm.examType" placeholder="全部类型" clearable>
            <el-option label="普通考试" :value="0" />
            <el-option label="重考考试" :value="1" />
          </el-select>
        </el-form-item>
        <el-form-item label="考试状态">
          <el-select v-model="searchForm.examStatus" placeholder="全部状态" clearable>
            <el-option label="未开始" :value="0" />
            <el-option label="进行中" :value="1" />
            <el-option label="已结束" :value="2" />
          </el-select>
        </el-form-item>
      </el-form>
    </div>

    <!-- 考试列表 -->
    <el-table
      v-loading="loading"
      :data="filteredExamList"
      border
      style="width: 100%"
    >
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
      <el-table-column label="距离开考" width="180">
        <template #default="{ row }">
          <template v-if="row.remainingTime !== null">
            {{ formatRemainingTime(row.remainingTime) }}
          </template>
          <template v-else>
            -
          </template>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button
            v-if="!row.submitted && !row.disciplinary && !row.absent && row.exam.examStatus !== 2"
            type="primary"
            link
            @click="handleStartExam(row)"
          >
            {{ row.started ? '继续考试' : '开始考试' }}
          </el-button>
          <el-button
            v-if="row.submitted && row.exam.examStatus === 2"
            type="success"
            link
            @click="handleViewScore(row)"
          >
            查看成绩
          </el-button>
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

    <!-- 成绩详情对话框 -->
    <el-dialog
      v-model="scoreDialogVisible"
      :title="currentExam?.exam?.examName + ' - 成绩详情'"
      width="800px"
    >
      <template v-if="scoreDetail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="考试名称">{{ scoreDetail.exam?.examName }}</el-descriptions-item>
          <el-descriptions-item label="考试类型">
            {{ scoreDetail.exam?.examType === 0 ? '普通考试' : '重考考试' }}
          </el-descriptions-item>
          <el-descriptions-item label="考试时间">
            {{ formatDateTime(scoreDetail.exam?.examStartTime) }} 至 {{ formatDateTime(scoreDetail.exam?.examEndTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="考试时长">{{ scoreDetail.exam?.examDuration }}分钟</el-descriptions-item>
          <el-descriptions-item label="总分">{{ scoreDetail.obtainedScore }}/{{ scoreDetail.totalScore }}</el-descriptions-item>
          <el-descriptions-item label="状态" :span="2">
            <el-tag :type="scoreDetail.obtainedScore >= 60 ? 'success' : 'danger'">
              {{ scoreDetail.obtainedScore >= 60 ? '及格' : '不及格' }}
            </el-tag>
          </el-descriptions-item>
        </el-descriptions>

        <div class="teacher-comment" v-if="scoreDetail.teacherComment">
          <h3>教师评语</h3>
          <p>{{ scoreDetail.teacherComment }}</p>
        </div>

        <div class="question-scores">
          <h3>题目得分详情</h3>
          <el-table :data="scoreDetail.questionScores" border style="width: 100%">
            <el-table-column label="题目" min-width="300" show-overflow-tooltip>
              <template #default="{ row }">
                <div>
                  <div>{{ row.question.content }}</div>
                  <div v-if="row.question.options && row.question.options.length > 0" class="options">
                    <div v-for="option in row.question.options" :key="option.optionId" class="option">
                      {{ option.content }}
                      <el-tag 
                        v-if="option.isCorrect" 
                        type="success" 
                        size="small" 
                        effect="plain"
                      >
                        正确答案
                      </el-tag>
                    </div>
                  </div>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="题型" width="100">
              <template #default="{ row }">
                {{ getQuestionType(row.question.type) }}
              </template>
            </el-table-column>
            <el-table-column label="你的答案" min-width="200" show-overflow-tooltip>
              <template #default="{ row }">
                <template v-if="row.question.type <= 2">
                  <span v-if="row.answer === '-1'" class="no-answer">未作答</span>
                  <template v-else>
                    <template v-if="row.question.type === 2">
                      {{ row.answer === '2' ? '否' : '是' }}
                    </template>
                    <template v-else>
                      {{ formatOptionAnswer(row.answer, row.question.options) }}
                    </template>
                  </template>
                </template>
                <template v-else>
                  {{ row.answer || '未作答' }}
                </template>
              </template>
            </el-table-column>
            <el-table-column label="得分" width="100">
              <template #default="{ row }">
                <span :class="{ 'full-score': row.score === row.question.score }">
                  {{ row.score }}
                </span>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import axios from 'axios'

const router = useRouter()

// 列表数据
const examList = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 当前选中的考试
const currentExam = ref(null)

// 成绩详情相关
const scoreDialogVisible = ref(false)
const scoreDetail = ref(null)

// 搜索表单数据
const searchForm = ref({
  examType: null,
  examStatus: null
})

// 获取考试列表
const fetchExamList = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: currentPage.value,
      pageSize: pageSize.value
    }

    const response = await axios.get('/api/student/exam/list', { params })
    if (response.data.code === 200) {
      examList.value = response.data.data.list
      total.value = response.data.data.total
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

// 开始考试
const handleStartExam = async (row) => {
  try {
    const response = await axios.post(`/api/student/exam/${row.exam.examId}/start`)
    if (response.data.code === 200) {
      router.push(`/student/exam-paper/${row.exam.examId}`)
    } else {
      ElMessage.error(response.data.message || '开始考试失败')
    }
  } catch (error) {
    console.error('开始考试失败:', error)
    ElMessage.error('开始考试失败')
  }
}

// 查看成绩详情
const handleViewScore = async (row) => {
  currentExam.value = row
  try {
    const response = await axios.get('/api/student/score/question-scores', {
      params: { examId: row.exam.examId }
    })
    if (response.data.code === 200) {
      scoreDetail.value = response.data.data
      scoreDialogVisible.value = true
    } else {
      ElMessage.error(response.data.message || '获取成绩详情失败')
    }
  } catch (error) {
    console.error('获取成绩详情失败:', error)
    ElMessage.error('获取成绩详情失败')
  }
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

// 计算属性：根据筛选条件过滤考试列表
const filteredExamList = computed(() => {
  return examList.value.filter(exam => {
    // 考试类型筛选
    if (searchForm.value.examType !== null && exam.exam.examType !== searchForm.value.examType) {
      return false
    }
    
    // 考试状态筛选
    if (searchForm.value.examStatus !== null && exam.exam.examStatus !== searchForm.value.examStatus) {
      return false
    }
    
    return true
  })
})

// 获取题目类型文本
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

// 格式化选项答案
const formatOptionAnswer = (answer, options) => {
  if (!answer || !options) return '未作答'
  const answerIds = answer.split(',')
  return options
    .filter(option => answerIds.includes(option.optionId.toString()))
    .map(option => option.content)
    .join('、') || '未作答'
}

// 格式化剩余时间
const formatRemainingTime = (minutes) => {
  if (minutes === null) return '-'
  const days = Math.floor(minutes / (24 * 60))
  const hours = Math.floor((minutes % (24 * 60)) / 60)
  const mins = minutes % 60
  
  let result = ''
  if (days > 0) result += `${days}天`
  if (hours > 0) result += `${hours}小时`
  if (mins > 0) result += `${mins}分钟`
  
  return result || '即将开始'
}

onMounted(() => {
  fetchExamList()
})
</script>

<style lang="scss" scoped>
.exam-list {
  padding: 20px;
  
  .search-bar {
    margin-bottom: 20px;
    background-color: #fff;
    padding: 20px;
    border-radius: 4px;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
    
    .form {
      display: flex;
      align-items: center;
      flex-wrap: wrap;
      gap: 16px;

      .el-form-item {
        margin-bottom: 0;
        margin-right: 0;
      }

      .el-select {
        width: 160px;
      }
    }
  }
  
  .pagination {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
  }

  .teacher-comment {
    margin: 20px 0;
    
    h3 {
      margin-bottom: 10px;
      font-weight: 500;
      color: #303133;
    }
    
    p {
      color: #606266;
      line-height: 1.6;
    }
  }

  .question-scores {
    margin-top: 20px;
    
    h3 {
      margin-bottom: 16px;
      font-weight: 500;
      color: #303133;
    }
  }

  .options {
    margin-top: 8px;
    font-size: 14px;
    color: #606266;
    
    .option {
      margin: 4px 0;
      display: flex;
      align-items: center;
      gap: 8px;
    }
  }

  .full-score {
    color: #67C23A;
    font-weight: bold;
  }

  .no-answer {
    color: #909399;
    font-style: italic;
  }
}
</style> 
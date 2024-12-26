<template>
  <div class="paper-form">
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
            <span class="title">{{ isEdit ? '编辑试卷' : '创建试卷' }}</span>
          </div>
        </div>
      </template>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="120px"
      >
        <el-form-item label="试卷名称" prop="paperName">
          <el-input v-model="form.paperName" placeholder="请输入试卷名称" />
        </el-form-item>

        <el-form-item label="所属学科" prop="subjectId">
          <el-select v-model="form.subjectId" placeholder="请选择学科" @change="handleSubjectChange">
            <el-option
              v-for="item in subjectOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="考试类型" prop="examType">
          <el-radio-group v-model="form.examType">
            <el-radio :label="0">期末考试</el-radio>
            <el-radio :label="1">普通考试</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="试卷难度" prop="difficulty" v-if="!isEdit">
          <el-slider
            v-model="form.difficulty"
            :min="0"
            :max="1"
            :step="0.1"
            style="width: 300px"
          >
            <template #tooltip="{ value }">
              难度: {{ value.toFixed(1) }}
            </template>
          </el-slider>
        </el-form-item>

        <el-form-item label="学年" prop="year">
          <el-date-picker
            v-model="form.year"
            type="year"
            placeholder="选择学年"
            value-format="YYYY"
          />
        </el-form-item>

        <el-form-item label="学期" prop="semester">
          <el-radio-group v-model="form.semester">
            <el-radio :label="1">第一学期</el-radio>
            <el-radio :label="2">第二学期</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="组卷方式" prop="generateMode" v-if="!isEdit">
          <el-radio-group v-model="form.generateMode">
            <el-radio :label="'manual'">手动组卷</el-radio>
            <el-radio :label="'auto'">自动组卷</el-radio>
          </el-radio-group>
        </el-form-item>

        <!-- 手动组卷 -->
        <template v-if="form.generateMode === 'manual' && !isEdit">
          <el-form-item label="筛选条件">
            <div class="filter-container">
              <el-input
                v-model="filters.keyword"
                placeholder="搜索题目内容"
                clearable
                @clear="handleFilterChange"
                @keyup.enter="handleFilterChange"
                style="width: 200px"
              />
              <el-select
                v-model="filters.bankId"
                placeholder="选择题库"
                clearable
                @change="handleFilterChange"
                style="width: 200px"
              >
                <el-option
                  v-for="item in bankOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
              <el-select
                v-model="filters.type"
                placeholder="选择题型"
                clearable
                @change="handleFilterChange"
                style="width: 200px"
              >
                <el-option
                  v-for="(name, type) in {
                    '0': '单选题',
                    '1': '多选题',
                    '2': '判断题',
                    '3': '填空题',
                    '4': '简答题'
                  }"
                  :key="type"
                  :label="name"
                  :value="Number(type)"
                />
              </el-select>
              <div class="difficulty-filter">
                <span>难度范围：</span>
                <el-slider
                  v-model="filters.difficultyRange"
                  range
                  :min="0"
                  :max="1"
                  :step="0.1"
                  @change="handleDifficultyChange"
                  style="width: 200px"
                />
              </div>
            </div>
          </el-form-item>

          <el-form-item label="题型分数" prop="questionScores">
            <div class="score-inputs">
              <div v-for="(score, type) in form.questionScores" :key="type" class="score-item">
                <span class="score-label">{{ getQuestionTypeName(type) }}：</span>
                <el-input-number
                  v-model="form.questionScores[type]"
                  :min="0"
                  :max="100"
                  :step="5"
                  @change="handleScoreChange"
                />
                <span class="score-unit">分</span>
              </div>
            </div>
            <div class="total-score">
              总分：{{ getTotalScore() }} 分
              <span v-if="getTotalScore() !== 100" class="score-warning">
                （总分必须为100分）
              </span>
            </div>
          </el-form-item>

          <!-- 题目选择区域 -->
          <el-form-item 
            v-for="(questions, type) in questionsByType" 
            :key="type"
            :label="getQuestionTypeName(type)"
          >
            <div v-if="form.questionScores[type] > 0">
              <el-table
                :data="questions"
                border
                :data-type="type"
                @selection-change="(val) => handleQuestionSelection(type, val)"
                style="width: 100%"
              >
                <el-table-column type="selection" width="55" fixed="left" />
                <el-table-column prop="content" label="题目内容" min-width="400" show-overflow-tooltip />
                <el-table-column prop="questionBank.qbName" label="所属题库" width="180" show-overflow-tooltip />
                <el-table-column label="难度" width="150">
                  <template #default="scope">
                    <el-progress
                      :percentage="scope.row.difficulty * 100"
                      :status="getDifficultyStatus(scope.row.difficulty)"
                    />
                  </template>
                </el-table-column>
                <el-table-column label="分值" width="150" fixed="right">
                  <template #default="scope">
                    <el-input-number
                      v-model="scope.row.score"
                      :min="0"
                      :max="form.questionScores[type]"
                      :step="1"
                      @change="handleQuestionScoreChange"
                      style="width: 120px"
                    />
                  </template>
                </el-table-column>
              </el-table>
              
              <!-- 分页器 -->
              <div class="pagination-container">
                <el-pagination
                  v-model:current-page="pagination.pageNum"
                  v-model:page-size="pagination.pageSize"
                  :page-sizes="[10, 20, 50, 100]"
                  :total="pagination.total"
                  layout="sizes, prev, pager, next"
                  @size-change="handleSizeChange"
                  @current-change="handlePageChange"
                />
              </div>

              <div class="type-score-info">
                已选择 {{ selectedQuestions[type]?.length || 0 }} 题，
                总分 {{ getTypeSelectedScore(type) }} / {{ form.questionScores[type] }} 分
              </div>
            </div>
            <div v-else>请先设置该题型分数</div>
          </el-form-item>
        </template>

        <!-- 自动组卷 -->
        <template v-if="form.generateMode === 'auto' && !isEdit">
          <el-form-item label="题型数量" prop="questionTypeCount">
            <div class="count-inputs">
              <div v-for="type in [0, 1, 2, 3, 4]" :key="type" class="count-item">
                <span class="count-label">{{ getQuestionTypeName(type) }}：</span>
                <el-input-number
                  v-model="form.questionTypeCount[type]"
                  :min="0"
                  :max="20"
                  :step="1"
                />
                <span class="count-unit">题</span>
              </div>
            </div>
          </el-form-item>
        </template>

        <el-form-item>
          <el-button type="primary" @click="handleSubmit">保存</el-button>
          <el-button @click="handleCancel">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import axios from 'axios'

const route = useRoute()
const router = useRouter()

// 判断是否是编辑模式
const isEdit = computed(() => route.name === 'TeacherPaperEdit')

// 表单数据
const formRef = ref(null)
const form = ref({
  paperName: '',
  subjectId: null,
  examType: 0,
  difficulty: 0.7,
  year: new Date().getFullYear().toString(),
  semester: 1,
  generateMode: 'manual', // 'manual' 或 'auto'
  questionScores: {
    '0': 30, // 单选题
    '1': 20, // 多选题
    '2': 20, // 判断题
    '3': 15, // 填空题
    '4': 15  // 简答题
  },
  questionTypeCount: {
    '0': 0,
    '1': 0,
    '2': 0,
    '3': 0,
    '4': 0
  }
})

// 学科选项
const subjectOptions = ref([])

// 按题型分类的题目列表
const questionsByType = ref({})

// 已选择的题目
const selectedQuestions = ref({})

// 分页参数
const pagination = ref({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

// 筛选条件
const filters = ref({
  keyword: '',
  bankId: null,
  type: null,
  minDifficulty: null,
  maxDifficulty: null
})

// 题库选项
const bankOptions = ref([])

// 表单验证规则
const rules = {
  paperName: [
    { required: true, message: '请输入试卷名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  subjectId: [
    { required: true, message: '请选择所属学科', trigger: 'change' }
  ],
  examType: [
    { required: true, message: '请选择考试类型', trigger: 'change' }
  ],
  difficulty: [
    { required: true, message: '请设置试卷难度', trigger: 'change' }
  ],
  year: [
    { required: true, message: '请选择学年', trigger: 'change' }
  ],
  semester: [
    { required: true, message: '请选择学期', trigger: 'change' }
  ]
}

// 获取题型名称
const getQuestionTypeName = (type) => {
  const typeMap = {
    '0': '单选题',
    '1': '多选题',
    '2': '判断题',
    '3': '填空题',
    '4': '简答题'
  }
  return typeMap[type] || '未知题型'
}

// 获取难度状态
const getDifficultyStatus = (difficulty) => {
  if (difficulty <= 0.3) return 'success'
  if (difficulty <= 0.7) return 'warning'
  return 'exception'
}

// 计算总分
const getTotalScore = () => {
  return Object.values(form.value.questionScores).reduce((sum, score) => sum + score, 0)
}

// 获取某题型已选题目的总分
const getTypeSelectedScore = (type) => {
  return (selectedQuestions.value[type] || [])
    .reduce((sum, q) => sum + (q.score || 0), 0)
}

// 处理题型分数变化
const handleScoreChange = () => {
  // 重置该题型的已选题目
  for (const type in selectedQuestions.value) {
    if (form.value.questionScores[type] === 0) {
      selectedQuestions.value[type] = []
    }
  }
}

// 处理题目分数变化
const handleQuestionScoreChange = () => {
  // 更新所有题型的总分
  for (const type in selectedQuestions.value) {
    const typeScore = getTypeSelectedScore(type)
    if (typeScore > form.value.questionScores[type]) {
      ElMessage.warning(`${getQuestionTypeName(type)}总分超出限制`)
    }
  }
}

// 处理题目选择
const handleQuestionSelection = (type, selection) => {
  selectedQuestions.value[type] = selection
  // 为新选择的题目设置默认分数
  const defaultScore = Math.floor(form.value.questionScores[type] / selection.length)
  selection.forEach(question => {
    if (!question.score) {
      question.score = defaultScore
    }
  })
}

// 获取学科列表
const fetchSubjects = async () => {
  try {
    const response = await axios.get('/api/teacher/subjects')
    if (response.data.code === 200) {
      subjectOptions.value = response.data.data.map(subject => ({
        value: subject.subjectId,
        label: subject.subjectName
      }))
    } else {
      ElMessage.error(response.data.message || '获取学科列表失败')
    }
  } catch (error) {
    console.error('获取学科列表失败:', error)
    ElMessage.error('获取学科列表失败')
  }
}

// 获取题库列表
const fetchBanks = async () => {
  try {
    const response = await axios.get('/api/teacher/questions/banks')
    if (response.data.code === 200) {
      bankOptions.value = response.data.data.list.map(bank => ({
        value: bank.qbId,
        label: bank.qbName
      }))
    } else {
      ElMessage.error(response.data.message || '获取题库列表失败')
    }
  } catch (error) {
    console.error('获取题库列表失败:', error)
    ElMessage.error('获取题库列表失败')
  }
}

// 获取题目列表
const fetchQuestions = async () => {
  if (!form.value.subjectId) return
  
  try {
    const params = {
      keyword: filters.value.keyword,
      bankId: filters.value.bankId,
      type: filters.value.type,
      minDifficulty: filters.value.minDifficulty,
      maxDifficulty: filters.value.maxDifficulty,
      pageNum: pagination.value.pageNum,
      pageSize: pagination.value.pageSize
    }
    
    const response = await axios.get('/api/teacher/questions', { params })
    if (response.data.code === 200) {
      const { list, total } = response.data.data
      pagination.value.total = total
      
      // 按题型分类题目
      const newQuestionsByType = {}
      list.forEach(q => {
        if (!newQuestionsByType[q.type]) {
          newQuestionsByType[q.type] = []
        }
        // 保持已选题目的分数
        const existingQuestion = (questionsByType.value[q.type] || [])
          .find(eq => eq.questionId === q.questionId)
        newQuestionsByType[q.type].push({
          ...q,
          score: existingQuestion?.score || 0
        })
      })
      questionsByType.value = newQuestionsByType
      
      // 恢复选中状态
      for (const type in selectedQuestions.value) {
        const selectedIds = selectedQuestions.value[type].map(q => q.questionId)
        const tableRef = document.querySelector(`el-table[data-type="${type}"]`)
        if (tableRef) {
          const questions = newQuestionsByType[type] || []
          questions.forEach(q => {
            if (selectedIds.includes(q.questionId)) {
              tableRef.toggleRowSelection(q, true)
            }
          })
        }
      }
    } else {
      ElMessage.error(response.data.message || '获取题目列表失败')
    }
  } catch (error) {
    console.error('获取题目列表失败:', error)
    ElMessage.error('获取题目列表失败')
  }
}

// 处理分页变化
const handlePageChange = (page) => {
  pagination.value.pageNum = page
  fetchQuestions()
}

// 处理每页数量变化
const handleSizeChange = (size) => {
  pagination.value.pageSize = size
  pagination.value.pageNum = 1
  fetchQuestions()
}

// 处理筛选条件变化
const handleFilterChange = () => {
  pagination.value.pageNum = 1
  fetchQuestions()
}

// 处理学科变化
const handleSubjectChange = () => {
  fetchQuestions()
}

// 获取试卷详情
const fetchPaperDetail = async (paperId) => {
  try {
    const response = await axios.get(`/api/teacher/papers/${paperId}`)
    if (response.data.code === 200) {
      const paper = response.data.data.paper
      form.value = {
        paperName: paper.paperName,
        subjectId: paper.subjectId,
        examType: paper.examType,
        difficulty: paper.difficulty,
        year: paper.year,
        semester: paper.semester,
        questionScores: paper.questionScores || form.value.questionScores
      }
    } else {
      ElMessage.error(response.data.message || '获取试卷详情失败')
    }
  } catch (error) {
    console.error('获取试卷详情失败:', error)
    ElMessage.error('获取试卷详情失败')
  }
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    
    if (isEdit.value) {
      // 编辑模式
      const submitData = {
        paperId: parseInt(route.params.id),
        paperName: form.value.paperName,
        subjectId: form.value.subjectId,
        examType: form.value.examType,
        paperDifficulty: form.value.difficulty,
        academicTerm: (() => {
          const calendar = new Date()
          calendar.setFullYear(parseInt(form.value.year))
          calendar.setMonth(form.value.semester === 1 ? 0 : 5) // 0代表1月，5代表6月
          calendar.setDate(1)
          calendar.setHours(0, 0, 0, 0)
          return calendar.toISOString()
        })(),
        paperStatus: 0  // 默认为未发布状态
      }

      const response = await axios.put(`/api/teacher/papers/${route.params.id}`, submitData)
      
      if (response.data.code === 200) {
        ElMessage.success('更新成功')
        router.push('/teacher/papers')
      } else {
        ElMessage.error(response.data.message || '更新失败')
      }
    } else if (form.value.generateMode === 'manual') {
      // 检查总分是否为100分
      if (getTotalScore() !== 100) {
        ElMessage.error('试题总分必须为100分')
        return
      }

      // 检查每种题型的分数是否符合要求
      for (const type in form.value.questionScores) {
        if (form.value.questionScores[type] > 0) {
          const typeScore = getTypeSelectedScore(type)
          if (typeScore !== form.value.questionScores[type]) {
            ElMessage.error(`${getQuestionTypeName(type)}总分必须等于设定分数`)
            return
          }
        }
      }

      // 转换提交数据格式
      const submitData = {
        paperName: form.value.paperName,
        subjectId: form.value.subjectId,
        examType: form.value.examType,
        difficulty: form.value.difficulty,
        year: form.value.year,
        semester: form.value.semester,
        questionScores: {} // 重置为按题目ID的分数结构
      }

      // 将所有已选题目的分数按题目ID存储
      for (const type in selectedQuestions.value) {
        selectedQuestions.value[type].forEach(question => {
          submitData.questionScores[question.questionId] = question.score
        })
      }

      const response = await axios.post('/api/teacher/papers/generate-manual', submitData)
      
      if (response.data.code === 200) {
        ElMessage.success('创建成功')
        router.push('/teacher/papers')
      } else {
        ElMessage.error(response.data.message || '创建失败')
      }
    } else {
      // 自动组卷模式
      const submitData = {
        paperName: form.value.paperName,
        subjectId: form.value.subjectId,
        examType: form.value.examType,
        difficulty: form.value.difficulty,
        year: form.value.year,
        semester: form.value.semester,
        questionTypeCount: form.value.questionTypeCount
      }

      const response = await axios.post('/api/teacher/papers/generate', submitData)
      if (response.data.code === 200) {
        ElMessage.success('创建成功')
        router.push('/teacher/papers')
      } else {
        ElMessage.error(response.data.message || '创建失败')
      }
    }
  } catch (error) {
    console.error(isEdit.value ? '更新试卷失败:' : '创建试卷失败:', error)
    ElMessage.error(error.response?.data?.message || (isEdit.value ? '更新失败' : '创建失败'))
  }
}

// 取消
const handleCancel = () => {
  router.back()
}

// 处理难度范围变化
const handleDifficultyChange = ([min, max]) => {
  filters.value.minDifficulty = min
  filters.value.maxDifficulty = max
  handleFilterChange()
}

// 监听学科变化
watch(() => form.value.subjectId, (newVal) => {
  if (newVal) {
    fetchQuestions()
  }
})

// 监听组卷方式变化
watch(() => form.value.generateMode, (newMode) => {
  if (newMode === 'manual') {
    // 重置题型数量
    form.value.questionTypeCount = {
      '0': 0,
      '1': 0,
      '2': 0,
      '3': 0,
      '4': 0
    }
    // 获取题目列表
    fetchQuestions()
  } else {
    // 重置题型分数和已选题目
    form.value.questionScores = {
      '0': 0,
      '1': 0,
      '2': 0,
      '3': 0,
      '4': 0
    }
    selectedQuestions.value = {}
    questionsByType.value = {}
  }
})

// 返回上一页
const handleBack = () => {
  router.push('/teacher/papers')
}

onMounted(() => {
  fetchSubjects()
  fetchBanks()
  if (isEdit.value) {
    fetchPaperDetail(route.params.id)
  }
})
</script>

<style lang="scss" scoped>
.paper-form {
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
  
  .score-inputs,
  .count-inputs {
    display: flex;
    flex-wrap: wrap;
    gap: 20px;
    
    .score-item,
    .count-item {
      display: flex;
      align-items: center;
      
      .score-label,
      .count-label {
        margin-right: 8px;
      }
      
      .score-unit,
      .count-unit {
        margin-left: 8px;
      }
    }
  }
  
  .total-score {
    margin-top: 16px;
    font-size: 16px;
    
    .score-warning {
      color: #f56c6c;
      margin-left: 8px;
    }
  }

  .type-score-info {
    margin-top: 8px;
    color: #606266;
    font-size: 14px;
  }

  .filter-container {
    display: flex;
    flex-wrap: wrap;
    gap: 16px;
    margin-bottom: 16px;
    
    .difficulty-filter {
      display: flex;
      align-items: center;
      gap: 8px;
    }
  }
  
  .pagination-container {
    margin-top: 16px;
    display: flex;
    justify-content: center;
  }
}
</style> 
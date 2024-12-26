<template>
  <div class="question-list">
    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-form :inline="true" :model="searchForm" class="form">
        <el-form-item>
          <el-input
            v-model="searchForm.keyword"
            placeholder="请输入题目关键词"
            clearable
            @clear="handleSearch"
          >
            <template #append>
              <el-button @click="handleSearch">
                <el-icon><Search /></el-icon>
              </el-button>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item>
          <el-select 
            v-model="searchForm.bankId" 
            placeholder="选择题库" 
            clearable
            class="question-bank-select"
            @change="handleBankSelect"
          >
            <el-option
              v-for="item in questionBanks"
              :key="item.qbId"
              :label="item.qbName"
              :value="item.qbId"
            />
          </el-select>
        </el-form-item>

        <el-form-item>
          <el-select 
            v-model="searchForm.type" 
            placeholder="题目类型" 
            clearable
            class="question-type-select"
          >
            <el-option label="单选题" :value="0" />
            <el-option label="多选题" :value="1" />
            <el-option label="判断题" :value="2" />
            <el-option label="填空题" :value="3" />
            <el-option label="简答题" :value="4" />
          </el-select>
        </el-form-item>

        <el-form-item>
          <el-slider
            v-model="searchForm.difficulty"
            range
            :min="0"
            :max="1"
            :step="0.1"
            style="width: 200px"
          >
            <template #tooltip="{ value }">
              难度: {{ value.toFixed(1) }}
            </template>
          </el-slider>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            添加题目
          </el-button>
          <el-button type="success" @click="handleAddBank">
            <el-icon><FolderAdd /></el-icon>
            创建题库
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 题目列表 -->
    <el-table
      v-loading="loading"
      :data="questionList"
      border
      style="width: 100%"
    >
      <el-table-column prop="questionId" label="题目ID" width="80" />
      <el-table-column prop="content" label="题目内容" show-overflow-tooltip />
      <el-table-column prop="typeName" label="题目类型" width="100" />
      <el-table-column prop="questionBank.qbName" label="所属题库" width="150" />
      <el-table-column prop="questionBank.subject.subjectName" label="所属学科" width="150" />
      <el-table-column label="难度" width="100">
        <template #default="scope">
          <el-progress
            :percentage="scope.row.difficulty * 100"
            :status="getDifficultyStatus(scope.row.difficulty)"
          />
        </template>
      </el-table-column>
      <el-table-column label="操作" width="120" fixed="right">
        <template #default="scope">
          <el-button
            type="primary"
            link
            @click="handleView(scope.row)"
          >
            查看/编辑
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

    <!-- 题目详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="题目详情"
      width="800px"
    >
      <el-descriptions :column="2" border>
        <el-descriptions-item label="题目类型">{{ currentQuestion.typeName }}</el-descriptions-item>
        <el-descriptions-item label="所属题库">{{ currentQuestion.questionBank?.qbName }}</el-descriptions-item>
        <el-descriptions-item label="所属学科">{{ currentQuestion.questionBank?.subject?.subjectName }}</el-descriptions-item>
        <el-descriptions-item label="难度">
          <el-progress
            :percentage="currentQuestion.difficulty * 100"
            :status="getDifficultyStatus(currentQuestion.difficulty)"
          />
        </el-descriptions-item>
      </el-descriptions>
      
      <div class="question-content">
        <h3>题目内容：</h3>
        <p>{{ currentQuestion.content }}</p>
      </div>

      <div class="question-options" v-if="currentQuestion.type <= 1">
        <h3>选项：</h3>
        <el-radio-group v-if="currentQuestion.type === 0" v-model="currentQuestion.answer" disabled>
          <el-radio
            v-for="option in currentQuestion.options"
            :key="option.optionId"
            :label="option.optionId"
            border
          >
            {{ option.content }}
          </el-radio>
        </el-radio-group>
        <el-checkbox-group v-else v-model="currentQuestion.answer" disabled>
          <el-checkbox
            v-for="option in currentQuestion.options"
            :key="option.optionId"
            :label="option.optionId"
            border
          >
            {{ option.content }}
          </el-checkbox>
        </el-checkbox-group>
      </div>

      <div class="question-answer" v-else>
        <h3>答案：</h3>
        <p>{{ currentQuestion.answer }}</p>
      </div>
    </el-dialog>

    <!-- 创建题库对话框 -->
    <el-dialog
      v-model="bankDialogVisible"
      title="创建题库"
      width="500px"
    >
      <el-form
        ref="bankFormRef"
        :model="bankForm"
        :rules="bankRules"
        label-width="100px"
      >
        <el-form-item label="题库名称" prop="qbName">
          <el-input v-model="bankForm.qbName" placeholder="请输入题库名称" />
        </el-form-item>
        <el-form-item label="所属学科" prop="subjectId">
          <el-select v-model="bankForm.subjectId" placeholder="请选择学科">
            <el-option
              v-for="item in subjectOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="bankForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入题库描述"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="bankDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveBank">确定</el-button>
      </template>
    </el-dialog>

    <!-- 题库详情对话框 -->
    <el-dialog
      v-model="bankDetailDialogVisible"
      title="题库详情"
      width="600px"
    >
      <el-descriptions :column="2" border>
        <el-descriptions-item label="题库名称">{{ currentBank.qbName }}</el-descriptions-item>
        <el-descriptions-item label="所属学科">{{ currentBank.subject?.subjectName }}</el-descriptions-item>
        <el-descriptions-item label="所属学院">{{ currentBank.subject?.college?.collegeName }}</el-descriptions-item>
        <el-descriptions-item label="题目数量">{{ currentBank.questions?.length || 0 }}</el-descriptions-item>
      </el-descriptions>

      <div class="bank-description" v-if="currentBank.description">
        <h3>题库描述：</h3>
        <p>{{ currentBank.description }}</p>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Search, Plus, FolderAdd } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from 'axios'
import { useRouter } from 'vue-router'

const router = useRouter()

// 搜索表单
const searchForm = ref({
  keyword: '',
  bankId: null,
  type: null,
  difficulty: [0, 1]
})

// 列表数据
const questionList = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 题库选项
const questionBanks = ref([])
const subjectOptions = ref([])

// 对话框控制
const detailDialogVisible = ref(false)
const bankDialogVisible = ref(false)
const currentQuestion = ref({})

// 题库表单
const bankFormRef = ref(null)
const bankForm = ref({
  qbName: '',
  subjectId: null,
  description: ''
})

// 题库表单验证规则
const bankRules = {
  qbName: [
    { required: true, message: '请输入题库名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  subjectId: [
    { required: true, message: '请选择所属学科', trigger: 'change' }
  ]
}

// 题库详情相关
const bankDetailDialogVisible = ref(false)
const currentBank = ref({})

// 获取题目列表
const fetchQuestionList = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: currentPage.value,
      pageSize: pageSize.value,
      keyword: searchForm.value.keyword || undefined,
      bankId: searchForm.value.bankId || undefined,
      type: searchForm.value.type,
      minDifficulty: searchForm.value.difficulty[0],
      maxDifficulty: searchForm.value.difficulty[1]
    }

    const response = await axios.get('/api/teacher/questions', { params })
    if (response.data.code === 200) {
      questionList.value = response.data.data.list
      total.value = response.data.data.total
    } else {
      ElMessage.error(response.data.message || '获取题目列表失败')
    }
  } catch (error) {
    console.error('获取题目列表失败:', error)
    ElMessage.error('获取题目列表失败')
  } finally {
    loading.value = false
  }
}

// 获取题库列表
const fetchQuestionBanks = async () => {
  try {
    const response = await axios.get('/api/teacher/questions/banks')
    if (response.data.code === 200) {
      questionBanks.value = response.data.data.list || []
    } else {
      ElMessage.error(response.data.message || '获取题库列表失败')
    }
  } catch (error) {
    console.error('获取题库列表失败:', error)
    ElMessage.error('获取题库列表失败')
  }
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

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  fetchQuestionList()
}

// 分页
const handleSizeChange = (val) => {
  pageSize.value = val
  fetchQuestionList()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchQuestionList()
}

// 查看题目详情
const handleView = (row) => {
  router.push(`/teacher/questions/${row.questionId}`)
}

// 添加题目
const handleAdd = () => {
  router.push('/teacher/questions/add')
}

// 创建题库
const handleAddBank = () => {
  bankForm.value = {
    qbName: '',
    subjectId: null,
    description: ''
  }
  bankDialogVisible.value = true
}

// 保存题库
const handleSaveBank = async () => {
  if (!bankFormRef.value) return
  
  try {
    await bankFormRef.value.validate()
    const response = await axios.post('/api/teacher/questions/banks', bankForm.value)
    
    if (response.data.code === 200) {
      ElMessage.success('创建成功')
      bankDialogVisible.value = false
      fetchQuestionBanks()
    } else {
      ElMessage.error(response.data.message || '创建失败')
    }
  } catch (error) {
    console.error('创建题库失败:', error)
    ElMessage.error(error.response?.data?.message || '创建失败')
  }
}

// 获取难度状态
const getDifficultyStatus = (difficulty) => {
  if (difficulty <= 0.3) return 'success'
  if (difficulty <= 0.7) return 'warning'
  return 'exception'
}

// 题库详情相关
const handleBankClick = async (bankId) => {
  try {
    const response = await axios.get(`/api/teacher/questions/banks/${bankId}`)
    if (response.data.code === 200) {
      currentBank.value = response.data.data
      bankDetailDialogVisible.value = true
    } else {
      ElMessage.error(response.data.message || '获取题库详情失败')
    }
  } catch (error) {
    console.error('获取题库详情失败:', error)
    ElMessage.error('获取题库详情失败')
  }
}

// 修改题库选项的点击事件处理
const handleBankSelect = (value) => {
  if (value) {
    handleBankClick(value)
  }
  searchForm.value.bankId = value
  handleSearch()
}

onMounted(() => {
  fetchQuestionList()
  fetchQuestionBanks()
  fetchSubjects()
})
</script>

<style lang="scss" scoped>
.question-list {
  padding: 20px;
  
  .search-bar {
    margin-bottom: 20px;
    background-color: #fff;
    padding: 20px;
    border-radius: 4px;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
    
    .form {
      display: flex;
      align-items: flex-start;
      flex-wrap: wrap;
      gap: 16px;

      .el-form-item {
        margin-bottom: 0;
        margin-right: 0;

        :deep(.el-select) {
          width: 100%;
        }

        :deep(.el-select.question-bank-select) {
          width: 240px !important;
        }

        :deep(.el-select.question-type-select) {
          width: 160px !important;
        }
      }
    }
  }
  
  .pagination {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
  }
  
  .question-content {
    margin: 20px 0;
    
    h3 {
      margin-bottom: 10px;
      font-weight: bold;
    }
  }
  
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
    }
  }

  .bank-description,
  .bank-questions {
    margin-top: 20px;
    
    h3 {
      margin-bottom: 10px;
      font-weight: bold;
    }
  }
}
</style> 
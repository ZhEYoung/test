<template>
  <div class="question-form">
    <div class="page-header">
      <el-button @click="handleBack">
        <el-icon><ArrowLeft /></el-icon>
        返回题库管理
      </el-button>
    </div>

    <el-card class="form-card">
      <template #header>
        <div class="card-header">
          <span>{{ isEdit ? '题目详情' : '添加题目' }}</span>
          <el-button v-if="isEdit && !isEditing" type="primary" @click="startEditing">编辑</el-button>
        </div>
      </template>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
        class="question-form"
      >
        <el-form-item label="题目类型" prop="type">
          <el-select 
            v-model="form.type" 
            placeholder="请选择题目类型" 
            @change="handleTypeChange"
            :disabled="isEdit && !isEditing"
          >
            <el-option label="单选题" :value="0" />
            <el-option label="多选题" :value="1" />
            <el-option label="判断题" :value="2" />
            <el-option label="填空题" :value="3" />
            <el-option label="简答题" :value="4" />
          </el-select>
        </el-form-item>

        <el-form-item label="所属题库" prop="qbId">
          <el-select 
            v-model="form.qbId" 
            placeholder="请选择题库"
            :disabled="isEdit && !isEditing"
          >
            <el-option
              v-for="item in questionBanks"
              :key="item.qbId"
              :label="item.qbName"
              :value="item.qbId"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="题目内容" prop="content">
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="4"
            placeholder="请输入题目内容"
            :disabled="isEdit && !isEditing"
          />
        </el-form-item>

        <el-form-item label="难度" prop="difficulty">
          <el-slider
            v-model="form.difficulty"
            :min="0"
            :max="1"
            :step="0.1"
            style="width: 300px"
            :disabled="isEdit && !isEditing"
          >
            <template #tooltip="{ value }">
              {{ value.toFixed(1) }}
            </template>
          </el-slider>
        </el-form-item>

        <!-- 选择题选项 -->
        <template v-if="form.type <= 1 || form.type === 2">
          <el-divider content-position="left">选项</el-divider>
          <div
            v-for="(option, index) in form.options"
            :key="index"
            class="option-item"
          >
            <el-form-item
              :label="'选项' + String.fromCharCode(65 + index)"
              :prop="'options.' + index + '.content'"
              :rules="{ required: true, message: '请输入选项内容', trigger: 'blur' }"
            >
              <div class="option-content">
                <el-input 
                  v-model="option.content" 
                  placeholder="请输入选项内容"
                  :disabled="form.type === 2"
                />
                <el-checkbox
                  v-if="form.type === 1"
                  v-model="option.isCorrect"
                  label="正确答案"
                />
                <el-radio
                  v-else
                  v-model="form.answer"
                  :label="index"
                  border
                >
                  正确答案
                </el-radio>
                <el-button
                  type="danger"
                  link
                  @click="removeOption(index)"
                  :disabled="form.options.length <= 2 || form.type === 2"
                >
                  删除
                </el-button>
              </div>
            </el-form-item>
          </div>
          <el-button
            type="primary"
            plain
            @click="addOption"
            :disabled="form.options.length >= 6 || form.type === 2"
          >
            添加选项
          </el-button>
        </template>

        <!-- 填空题答案 -->
        <el-form-item v-if="form.type === 3" label="正确答案" prop="answer">
          <el-input
            v-model="form.answer"
            type="textarea"
            :rows="2"
            placeholder="请输入正确答案，多个答案请用分号(;)分隔"
          />
        </el-form-item>

        <!-- 简答题答案 -->
        <el-form-item v-if="form.type === 4" label="参考答案" prop="answer">
          <el-input
            v-model="form.answer"
            type="textarea"
            :rows="4"
            placeholder="请输入参考答案"
          />
        </el-form-item>

        <el-form-item>
          <template v-if="!isEdit">
            <el-button type="primary" @click="handleSubmit">保存</el-button>
            <el-button @click="handleCancel">取消</el-button>
          </template>
          <template v-else>
            <el-button v-if="isEditing" type="primary" @click="handleSubmit">保存</el-button>
            <el-button v-if="isEditing" @click="cancelEditing">取消</el-button>
            <el-button v-if="!isEditing" @click="handleCancel">返回</el-button>
          </template>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import axios from 'axios'

const route = useRoute()
const router = useRouter()
const formRef = ref(null)
const isEdit = computed(() => route.params.id !== undefined)
const isEditing = ref(false)

// 表单数据
const form = ref({
  type: null,
  qbId: null,
  content: '',
  difficulty: 0.5,
  answer: '',
  options: [
    { content: '', isCorrect: false },
    { content: '', isCorrect: false }
  ]
})

// 题库列表
const questionBanks = ref([])

// 表单验证规则
const rules = {
  type: [
    { required: true, message: '请选择题目类型', trigger: 'change' }
  ],
  qbId: [
    { required: true, message: '请选择所属题库', trigger: 'change' }
  ],
  content: [
    { required: true, message: '请输入题目内容', trigger: 'blur' },
    { min: 2, max: 1000, message: '长度在 2 到 1000 个字符', trigger: 'blur' }
  ],
  difficulty: [
    { required: true, message: '请设置题目难度', trigger: 'change' }
  ],
  answer: [
    { required: true, message: '请设置正确答案', trigger: 'blur' }
  ]
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

// 题目类型变更处理
const handleTypeChange = (val) => {
  form.value.answer = ''
  if (val <= 1) {
    form.value.options = [
      { content: '', isCorrect: false },
      { content: '', isCorrect: false }
    ]
  } else if (val === 2) {
    // 判断题固定两个选项
    form.value.options = [
      { content: '正确', isCorrect: false },
      { content: '错误', isCorrect: false }
    ]
  }
}

// 添加选项
const addOption = () => {
  if (form.value.options.length < 6) {
    form.value.options.push({ content: '', isCorrect: false })
  }
}

// 删除选项
const removeOption = (index) => {
  if (form.value.options.length > 2) {
    form.value.options.splice(index, 1)
  }
}

// 开始编辑
const startEditing = () => {
  isEditing.value = true
}

// 取消编辑
const cancelEditing = () => {
  isEditing.value = false
  // 重新加载题目数据
  fetchQuestionDetail()
}

// 获取题目详情
const fetchQuestionDetail = async () => {
  if (!isEdit.value) return

  try {
    const response = await axios.get(`/api/teacher/questions/${route.params.id}`)
    if (response.data.code === 200) {
      const questionData = response.data.data
      form.value = {
        type: questionData.type,
        qbId: questionData.qbId,
        content: questionData.content,
        difficulty: questionData.difficulty,
        answer: questionData.answer,
        options: questionData.options || []
      }
    } else {
      ElMessage.error(response.data.message || '获取题目详情失败')
    }
  } catch (error) {
    console.error('获取题目详情失败:', error)
    ElMessage.error('获取题目详情失败')
  }
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()

    // 处理选择题和判断题答案
    if (form.value.type <= 2) {
      if (form.value.type === 0 || form.value.type === 2) {
        // 单选题和判断题：将radio的index转换为选项的答案
        const correctOption = form.value.options[form.value.answer]
        form.value.options.forEach(option => option.isCorrect = false)
        if (correctOption) {
          correctOption.isCorrect = true
        }
      }
      // 多选题：直接使用options中的isCorrect
    }

    const data = {
      type: form.value.type,
      qbId: form.value.qbId,
      content: form.value.content,
      difficulty: form.value.difficulty,
      answer: form.value.answer
    }

    if (form.value.type <= 2) {
      data.options = form.value.options
    }

    let response
    if (isEdit.value) {
      response = await axios.put(`/api/teacher/questions/${route.params.id}`, data)
    } else {
      response = await axios.post('/api/teacher/questions', data)
    }
    
    if (response.data.code === 200) {
      ElMessage.success('保存成功')
      if (isEdit.value) {
        isEditing.value = false
        fetchQuestionDetail()
      } else {
        router.push('/teacher/questions')
      }
    } else {
      ElMessage.error(response.data.message || '保存失败')
    }
  } catch (error) {
    console.error('保存题目失败:', error)
    ElMessage.error(error.response?.data?.message || '保存失败')
  }
}

// 取消
const handleCancel = () => {
  router.back()
}

// 返回题库管理页面
const handleBack = () => {
  router.push('/teacher/questions')
}

onMounted(() => {
  fetchQuestionBanks()
  if (isEdit.value) {
    fetchQuestionDetail()
  }
})
</script>

<style lang="scss" scoped>
.question-form {
  padding: 20px;
  
  .page-header {
    margin-bottom: 20px;
  }
  
  .form-card {
    max-width: 1000px;
    margin: 0 auto;
  }
  
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
  
  .option-item {
    .option-content {
      display: flex;
      align-items: center;
      gap: 16px;
      
      .el-input {
        flex: 1;
      }
    }
  }
  
  :deep(.el-form-item__content) {
    flex-wrap: nowrap;
  }
}
</style> 
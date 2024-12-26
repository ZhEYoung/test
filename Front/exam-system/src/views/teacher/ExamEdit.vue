<template>
  <div class="exam-edit">
    <el-card class="form-card">
      <template #header>
        <div class="card-header">
          <span>编辑考试</span>
        </div>
      </template>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="120px"
        class="edit-form"
      >
        <el-form-item label="考试名称" prop="examName">
          <el-input
            v-model="form.examName"
            placeholder="请输入考试名称"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="所属学科" prop="subjectId">
          <el-select 
            v-model="form.subjectId" 
            placeholder="请选择学科"
            style="width: 100%"
            disabled
          >
            <el-option
              v-for="item in subjectOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="试卷" prop="paperId">
          <el-select
            v-model="form.paperId"
            placeholder="请选择试卷"
            style="width: 100%"
            disabled
          >
            <el-option
              v-for="item in paperOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="开始时间" prop="examStartTime">
          <el-date-picker
            v-model="form.examStartTime"
            type="datetime"
            placeholder="选择开始时间"
            style="width: 100%"
            :disabled-date="disabledDate"
            :disabled-hours="disabledHours"
          />
        </el-form-item>

        <el-form-item label="考试时长" prop="examDuration">
          <el-input-number
            v-model="form.examDuration"
            :min="10"
            :max="180"
            style="width: 100%"
          >
            <template #append>分钟</template>
          </el-input-number>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSubmit">保存修改</el-button>
          <el-button @click="handleCancel">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import axios from 'axios'

const route = useRoute()
const router = useRouter()
const formRef = ref(null)

// 表单数据
const form = ref({
  examName: '',
  subjectId: null,
  paperId: null,
  examStartTime: null,
  examDuration: 60,
  examType: 0
})

// 选项数据
const subjectOptions = ref([])
const paperOptions = ref([])

// 表单验证规则
const rules = {
  examName: [
    { required: true, message: '请输入考试名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  examStartTime: [
    { required: true, message: '请选择开始时间', trigger: 'change' },
    {
      validator: (rule, value, callback) => {
        if (value && value < new Date()) {
          callback(new Error('开始时间不能早于当前时间'))
        } else {
          callback()
        }
      },
      trigger: 'change'
    }
  ],
  examDuration: [
    { required: true, message: '请设置考试时长', trigger: 'blur' },
    { type: 'number', min: 10, max: 180, message: '考试时长必须在10-180分钟之间', trigger: 'blur' }
  ]
}

// 获取考试详情
const fetchExamDetail = async () => {
  try {
    const examId = route.params.id
    const response = await axios.get(`/api/teacher/exam/${examId}`)
    if (response.data.code === 200) {
      const { exam, paper } = response.data.data
      
      // 设置表单数据
      form.value = {
        examName: exam.examName,
        subjectId: exam.subjectId,
        paperId: exam.paperId,
        examStartTime: new Date(exam.examStartTime),
        examDuration: exam.examDuration,
        examType: exam.examType
      }

      // 设置学科选项
      if (paper?.subject) {
        subjectOptions.value = [{
          value: paper.subject.subjectId,
          label: paper.subject.subjectName
        }]
      }

      // 设置试卷选项
      paperOptions.value = [{
        value: paper.paperId,
        label: paper.paperName
      }]

    } else {
      ElMessage.error(response.data.message || '获取考试详情失败')
    }
  } catch (error) {
    console.error('获取考试详情失败:', error)
    ElMessage.error('获取考试详情失败')
  }
}

// 日期禁用
const disabledDate = (time) => {
  return time.getTime() < Date.now()
}

// 小时禁用
const disabledHours = () => {
  const hours = []
  for (let i = 0; i < 24; i++) {
    if (i < 6 || i > 22) { // 禁用6点前和22点后
      hours.push(i)
    }
  }
  return hours
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    
    const examId = route.params.id
    const startTime = form.value.examStartTime
    
    // 格式化时间，保持本地时区
    const formatDateTime = (date) => {
      const pad = (num) => String(num).padStart(2, '0')
      return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`
    }

    const requestBody = {
      examId: Number(examId),
      examName: form.value.examName,
      examStartTime: formatDateTime(startTime),
      examDuration: form.value.examDuration
    }

    const response = await axios.put(`/api/teacher/exam/${examId}`, requestBody)
    
    if (response.data.code === 200) {
      ElMessage.success('修改考试成功')
      router.push(`/teacher/exams/${examId}`)
    } else {
      ElMessage.error(response.data.message || '修改考试失败')
    }
  } catch (error) {
    console.error('修改考试失败:', error)
    ElMessage.error('修改考试失败')
  }
}

// 取消
const handleCancel = () => {
  router.back()
}

onMounted(() => {
  fetchExamDetail()
})
</script>

<style lang="scss" scoped>
.exam-edit {
  padding: 20px;
  
  .form-card {
    max-width: 800px;
    margin: 0 auto;
    
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
    
    .edit-form {
      padding: 20px;
      
      :deep(.el-form-item__content) {
        flex-wrap: nowrap;
      }
    }
  }
}
</style> 
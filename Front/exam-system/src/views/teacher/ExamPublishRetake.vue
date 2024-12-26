<template>
  <div class="exam-publish">
    <el-card class="form-card">
      <template #header>
        <div class="card-header">
          <span>发布重考考试</span>
        </div>
      </template>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="120px"
        class="publish-form"
      >
        <!-- 选择原考试 -->
        <el-form-item label="原考试" prop="originalExamId">
          <el-select
            v-model="form.originalExamId"
            placeholder="请选择原考试"
            style="width: 100%"
            @change="handleOriginalExamChange"
          >
            <el-option
              v-for="item in originalExamOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>

        <!-- 显示重考学生统计 -->
        <el-form-item v-if="retakeStats.totalRetakeStudents > 0">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="需要重考人数">
              {{ retakeStats.totalRetakeStudents }}
            </el-descriptions-item>
            <el-descriptions-item label="重考率">
              {{ retakeStats.retakeRate }}
            </el-descriptions-item>
          </el-descriptions>
        </el-form-item>

        <!-- 重考学生列表 -->
        <el-form-item v-if="retakeStudentsList.length > 0">
          <el-table :data="retakeStudentsList" border style="width: 100%">
            <el-table-column prop="student.studentId" label="学号" width="120" />
            <el-table-column prop="student.name" label="姓名" width="120" />
            <el-table-column label="重考原因" show-overflow-tooltip>
              <template #default="scope">
                {{ scope.row.disciplinaryReason || '成绩不合格' }}
              </template>
            </el-table-column>
          </el-table>
        </el-form-item>

        <el-form-item label="试卷" prop="paperId">
          <el-select
            v-model="form.paperId"
            placeholder="请选择试卷"
            style="width: 100%"
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
            :min="60"
            :max="180"
            style="width: 100%"
          >
            <template #append>分钟</template>
          </el-input-number>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSubmit">发布重考</el-button>
          <el-button @click="handleCancel">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import axios from 'axios'

const router = useRouter()
const formRef = ref(null)

// 表单数据
const form = ref({
  originalExamId: null,
  paperId: null,
  examStartTime: null,
  examDuration: 120
})

// 选项数据
const originalExamOptions = ref([])
const paperOptions = ref([])
const originalExamList = ref([]) // 存储原始考试列表数据

// 重考统计数据
const retakeStats = ref({
  totalRetakeStudents: 0,
  retakeRate: '0%'
})

// 重考学生列表
const retakeStudentsList = ref([])

// 表单验证规则
const rules = {
  originalExamId: [
    { required: true, message: '请选择原考试', trigger: 'change' }
  ],
  paperId: [
    { required: true, message: '请选择试卷', trigger: 'change' }
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
    { type: 'number', min: 60, max: 180, message: '考试时长必须在60-180分钟之间', trigger: 'blur' }
  ]
}

// 获取已结束的考试列表
const fetchOriginalExams = async () => {
  try {
    const response = await axios.get('/api/teacher/exam/list', {
      params: {
        pageNum: 1,
        pageSize: 100 // 获取足够多的考试
      }
    })
    if (response.data.code === 200) {
      // 存储原始考试列表数据
      originalExamList.value = response.data.data.list.filter(item => item.examStatus === 2)
      
      // 使用Map去重，以examId为key
      const uniqueExams = new Map()
      originalExamList.value.forEach(item => {
        if (!uniqueExams.has(item.examId)) {
          uniqueExams.set(item.examId, {
            value: item.examId,
            label: `${item.examName} (${item.subject?.subjectName || '未知学科'})`
          })
        }
      })
      
      originalExamOptions.value = Array.from(uniqueExams.values())
    } else {
      ElMessage.error(response.data.message || '获取考试列表失败')
    }
  } catch (error) {
    console.error('获取考试列表失败:', error)
    ElMessage.error('获取考试列表失败')
  }
}

// 获取试卷列表
const fetchPapers = async () => {
  try {
    const response = await axios.get('/api/teacher/papers', {
      params: {
        status: 0 // 未发布的试卷
      }
    })
    if (response.data.code === 200) {
      paperOptions.value = response.data.data.list.map(item => ({
        value: item.paperId,
        label: item.paperName
      }))
    } else {
      ElMessage.error(response.data.message || '获取试卷列表失败')
    }
  } catch (error) {
    console.error('获取试卷列表失败:', error)
    ElMessage.error('获取试卷列表失败')
  }
}

// 获取重考学生列表
const handleOriginalExamChange = async (examId) => {
  if (!examId) {
    retakeStats.value = { totalRetakeStudents: 0, retakeRate: '0%' }
    retakeStudentsList.value = []
    return
  }

  try {
    const response = await axios.get(`/api/teacher/exam/${examId}/retake-students`)
    if (response.data.code === 200) {
      retakeStudentsList.value = response.data.data.retakeStudents
      retakeStats.value = response.data.data.statistics
    } else {
      ElMessage.error(response.data.message || '获取重考学生列表失败')
    }
  } catch (error) {
    console.error('获取重考学生列表失败:', error)
    ElMessage.error('获取重考学生列表失败')
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
    
    // 获取需要重考的学生ID列表
    const studentIds = retakeStudentsList.value
      .map(item => item.student.studentId)
      .join(',')

    // 获取原考试的学科ID
    const originalExam = originalExamList.value.find(
      exam => exam.examId === form.value.originalExamId
    )
    const subjectId = originalExam?.subjectId

    if (!subjectId) {
      ElMessage.error('获取学科信息失败')
      return
    }

    // 构建查询参数
    const queryParams = new URLSearchParams()
    queryParams.append('subjectId', subjectId)
    queryParams.append('studentIds', studentIds)
    queryParams.append('paperId', form.value.paperId)
    // 使用本地时间格式化
    const localDate = new Date(form.value.examStartTime)
    const formattedDateTime = `${localDate.getFullYear()}-${String(localDate.getMonth() + 1).padStart(2, '0')}-${String(localDate.getDate()).padStart(2, '0')} ${String(localDate.getHours()).padStart(2, '0')}:${String(localDate.getMinutes()).padStart(2, '0')}:${String(localDate.getSeconds()).padStart(2, '0')}`
    queryParams.append('examStartTime', formattedDateTime)
    queryParams.append('examDuration', form.value.examDuration)

    const response = await axios.post(`/api/teacher/exam/publish-retake?${queryParams.toString()}`)
    
    if (response.data.code === 200) {
      ElMessage.success('发布重考成功')
      router.push('/teacher/exams')
    } else {
      ElMessage.error(response.data.message || '发布重考失败')
    }
  } catch (error) {
    console.error('发布重考失败:', error)
    ElMessage.error('发布重考失败')
  }
}

// 取消
const handleCancel = () => {
  router.back()
}

onMounted(() => {
  fetchOriginalExams()
  fetchPapers()
})
</script>

<style lang="scss" scoped>
.exam-publish {
  padding: 20px;
  
  .form-card {
    max-width: 800px;
    margin: 0 auto;
    
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
    
    .publish-form {
      padding: 20px;
      
      :deep(.el-form-item__content) {
        flex-wrap: nowrap;
      }
    }
  }
}
</style> 
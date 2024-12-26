<template>
  <div class="exam-publish">
    <el-card class="form-card">
      <template #header>
        <div class="card-header">
          <span>发布普通考试</span>
        </div>
      </template>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="120px"
        class="publish-form"
      >
        <el-form-item label="所属学科" prop="subjectId">
          <el-select 
            v-model="form.subjectId" 
            placeholder="请选择学科"
            style="width: 100%"
          >
            <el-option
              v-for="item in subjectOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="考试班级" prop="classIds">
          <el-select
            v-model="form.classIds"
            multiple
            placeholder="请选择班级"
            style="width: 100%"
          >
            <el-option
              v-for="item in filteredClassOptions"
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
            @change="handlePaperChange"
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
          <el-button type="primary" @click="handleSubmit">发布考试</el-button>
          <el-button @click="handleCancel">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import axios from 'axios'

const router = useRouter()
const formRef = ref(null)

// 表单数据
const form = ref({
  subjectId: null,
  classIds: [],
  paperId: null,
  examStartTime: null,
  examDuration: 60
})

// 选项数据
const subjectOptions = ref([])
const classOptions = ref([])
const paperOptions = ref([])
const allClasses = ref([]) // 存储所有班级数据

// 计算属性：根据所选学科过滤班级
const filteredClassOptions = computed(() => {
  if (!form.value.subjectId) {
    return allClasses.value
  }
  return allClasses.value.filter(item => {
    const [className, subjectName] = item.label.split(' (')
    return item.subjectId === form.value.subjectId
  })
})

// 监听学科变化
watch(() => form.value.subjectId, (newVal) => {
  // 清空已选班级
  form.value.classIds = []
})

// 表单验证规则
const rules = {
  subjectId: [
    { required: true, message: '请选择学科', trigger: 'change' }
  ],
  classIds: [
    { required: true, message: '请选择班级', trigger: 'change' },
    { type: 'array', min: 1, message: '至少选择一个班级', trigger: 'change' }
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
    { type: 'number', min: 10, max: 180, message: '考试时长必须在10-180分钟之间', trigger: 'blur' }
  ]
}

// 获取学科列表
const fetchSubjects = async () => {
  try {
    const response = await axios.get('/api/teacher/subjects')
    if (response.data.code === 200) {
      subjectOptions.value = response.data.data.map(item => ({
        value: item.subjectId,
        label: item.subjectName
      }))
    } else {
      ElMessage.error(response.data.message || '获取学科列表失败')
    }
  } catch (error) {
    console.error('获取学科列表失败:', error)
    ElMessage.error('获取学科列表失败')
  }
}

// 获取班级列表
const fetchClasses = async () => {
  try {
    const response = await axios.get('/api/teacher/classes')
    if (response.data.code === 200) {
      // 添加学科名称和ID到班级选项中
      allClasses.value = response.data.data.list.map(item => ({
        value: item.classId,
        label: `${item.className} (${item.subject.subjectName})`,
        subjectId: item.subjectId
      }))
    } else {
      ElMessage.error(response.data.message || '获取班级列表失败')
    }
  } catch (error) {
    console.error('获取班级列表失败:', error)
    ElMessage.error('获取班级列表失败')
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
    
    // 构建查询参数
    const queryParams = new URLSearchParams()
    queryParams.append('subjectId', form.value.subjectId)
    form.value.classIds.forEach(classId => {
      queryParams.append('classIds', classId)
    })
    queryParams.append('paperId', form.value.paperId)
    // 使用本地时间格式化
    const localDate = new Date(form.value.examStartTime)
    const formattedDateTime = `${localDate.getFullYear()}-${String(localDate.getMonth() + 1).padStart(2, '0')}-${String(localDate.getDate()).padStart(2, '0')} ${String(localDate.getHours()).padStart(2, '0')}:${String(localDate.getMinutes()).padStart(2, '0')}:${String(localDate.getSeconds()).padStart(2, '0')}`
    queryParams.append('examStartTime', formattedDateTime)
    queryParams.append('examDuration', form.value.examDuration)

    const response = await axios.post(`/api/teacher/exam/publish?${queryParams.toString()}`)
    
    if (response.data.code === 200) {
      ElMessage.success('发布考试成功')
      router.push('/teacher/exams')
    } else {
      ElMessage.error(response.data.message || '发布考试失败')
    }
  } catch (error) {
    console.error('发布考试失败:', error)
    ElMessage.error('发布考试失败')
  }
}

// 取消
const handleCancel = () => {
  router.back()
}

// 试卷选择变更
const handlePaperChange = async (paperId) => {
  if (!paperId) return
  
  try {
    const response = await axios.get(`/api/teacher/papers/${paperId}`)
    if (response.data.code === 200) {
      const paper = response.data.data.paper
      // 自动选择试卷对应的学科
      form.value.subjectId = paper.subjectId
    }
  } catch (error) {
    console.error('获取试卷详情失败:', error)
  }
}

onMounted(() => {
  fetchSubjects()
  fetchClasses()
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
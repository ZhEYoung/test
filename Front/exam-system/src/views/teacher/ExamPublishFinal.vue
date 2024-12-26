<template>
  <div class="exam-publish">
    <el-card class="form-card">
      <template #header>
        <div class="card-header">
          <span>发布期末考试</span>
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

        <el-form-item label="学年" prop="year">
          <el-select
            v-model="form.year"
            placeholder="请选择学年"
            style="width: 100%"
          >
            <el-option
              v-for="year in yearOptions"
              :key="year"
              :label="year + '学年'"
              :value="year"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="学期" prop="semester">
          <el-select
            v-model="form.semester"
            placeholder="请选择学期"
            style="width: 100%"
          >
            <el-option label="第一学期" :value="1" />
            <el-option label="第二学期" :value="2" />
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
  examStartTime: null,
  examDuration: 120,
  year: new Date().getFullYear(),
  semester: 1
})

// 选项数据
const subjectOptions = ref([])
const classOptions = ref([])

// 计算属性：根据所选学科过滤班级
const filteredClassOptions = computed(() => {
  if (!form.value.subjectId) {
    return classOptions.value
  }
  return classOptions.value.filter(item => item.subjectId === form.value.subjectId)
})

// 监听学科变化
watch(() => form.value.subjectId, (newVal) => {
  // 清空已选班级
  form.value.classIds = []
})

// 生成学年选项（前后5年）
const yearOptions = computed(() => {
  const currentYear = new Date().getFullYear()
  const years = []
  for (let i = currentYear - 5; i <= currentYear + 5; i++) {
    years.push(i)
  }
  return years
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
    { type: 'number', min: 60, max: 180, message: '期末考试时长必须在60-180分钟之间', trigger: 'blur' }
  ],
  year: [
    { required: true, message: '请选择学年', trigger: 'change' }
  ],
  semester: [
    { required: true, message: '请选择学期', trigger: 'change' }
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
    const response = await axios.get('/api/teacher/classes/college/classes', {
      params: {
        pageNum: 1,
        pageSize: 100 // 获取足够多的班级
      }
    })
    if (response.data.code === 200) {
      // 过滤掉重复的班级，并且只显示未进行期末考试的班级
      const uniqueClasses = new Map()
      response.data.data.list.forEach(item => {
        // 使用班级ID作为key来去重
        if (!uniqueClasses.has(item.classId) && !item.finalExam) {
          uniqueClasses.set(item.classId, {
            value: item.classId,
            label: `${item.className} (${item.subject.subjectName})`,
            subjectId: item.subjectId
          })
        }
      })
      classOptions.value = Array.from(uniqueClasses.values())
    } else {
      ElMessage.error(response.data.message || '获取班级列表失败')
    }
  } catch (error) {
    console.error('获取班级列表失败:', error)
    ElMessage.error('获取班级列表失败')
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
    queryParams.append('year', form.value.year)
    queryParams.append('semester', form.value.semester)
    // 使用本地时间格式化
    const localDate = new Date(form.value.examStartTime)
    const formattedDateTime = `${localDate.getFullYear()}-${String(localDate.getMonth() + 1).padStart(2, '0')}-${String(localDate.getDate()).padStart(2, '0')} ${String(localDate.getHours()).padStart(2, '0')}:${String(localDate.getMinutes()).padStart(2, '0')}:${String(localDate.getSeconds()).padStart(2, '0')}`
    queryParams.append('examStartTime', formattedDateTime)
    queryParams.append('examDuration', form.value.examDuration)

    const response = await axios.post(`/api/teacher/exam/publish-final?${queryParams.toString()}`)
    
    if (response.data.code === 200) {
      ElMessage.success('发布期末考试成功')
      router.push('/teacher/exams')
    } else {
      ElMessage.error(response.data.message || '发布期末考试失败')
    }
  } catch (error) {
    console.error('发布期末考试失败:', error)
    ElMessage.error('发布期末考试失败')
  }
}

// 取消
const handleCancel = () => {
  router.back()
}

onMounted(() => {
  fetchSubjects()
  fetchClasses()
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
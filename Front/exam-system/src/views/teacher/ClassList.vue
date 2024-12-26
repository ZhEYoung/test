<template>
  <div class="class-list">
    <div class="search-bar">
      <el-input
        v-model="searchKeyword"
        placeholder="请输入班级名称搜索"
        style="width: 200px"
        clearable
        @clear="handleSearch"
      >
        <template #append>
          <el-button @click="handleSearch">
            <el-icon><Search /></el-icon>
          </el-button>
        </template>
      </el-input>

      <el-button type="primary" @click="handleAdd" style="margin-left: 16px">
        <el-icon><Plus /></el-icon>
        创建班级
      </el-button>
    </div>

    <el-table
      v-loading="loading"
      :data="classList"
      border
      style="width: 100%"
    >
      <el-table-column prop="classId" label="班级ID" width="80" />
      <el-table-column prop="className" label="班级名称" width="200" />
      <el-table-column prop="subject.subjectName" label="所属学科" width="150" />
      <el-table-column label="期末考试" width="100">
        <template #default="scope">
          <el-tag :type="scope.row.finalExam ? 'success' : 'info'">
            {{ scope.row.finalExam ? '已进行' : '未进行' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="280" fixed="right">
        <template #default="scope">
          <el-button
            type="primary"
            size="small"
            @click="handleEdit(scope.row)"
          >
            编辑
          </el-button>
          <el-button
            type="success"
            size="small"
            @click="handleViewStudents(scope.row)"
          >
            查看学生
          </el-button>
          <el-button
            type="info"
            size="small"
            @click="handleViewExams(scope.row)"
          >
            查看考试
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 编辑对话框 -->
    <el-dialog
      v-model="editDialogVisible"
      :title="isEdit ? '编辑班级' : '创建班级'"
      width="500px"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="formRules"
        label-width="100px"
      >
        <el-form-item label="班级名称" prop="className">
          <el-input 
            v-model="form.className" 
            placeholder="请输入班级名称"
          />
        </el-form-item>
        <el-form-item label="所属学科" prop="subjectId" v-if="!isEdit">
          <el-select 
            v-model="form.subjectId" 
            placeholder="请选择学科"
          >
            <el-option
              v-for="item in subjectOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="期末考试" prop="finalExam">
          <el-switch
            v-model="form.finalExam"
            :active-value="true"
            :inactive-value="false"
          />
        </el-form-item>
        <el-form-item label="备注" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="3"
            placeholder="请输入备注信息"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>

    <!-- 学生列表对话框 -->
    <el-dialog
      v-model="studentsDialogVisible"
      title="班级学生列表"
      width="800px"
    >
      <el-table :data="studentsList" border style="width: 100%">
        <el-table-column prop="user.username" label="学号" width="120" />
        <el-table-column prop="name" label="姓名" width="120" />
        <el-table-column prop="user.phone" label="手机号" width="120" />
        <el-table-column prop="user.email" label="邮箱" />
        <el-table-column label="操作" width="120">
          <template #default="scope">
            <el-button
              type="danger"
              size="small"
              @click="handleRemoveStudent(scope.row)"
            >
              移出班级
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <!-- 考试列表对话框 -->
    <el-dialog
      v-model="examsDialogVisible"
      title="班级考试安排"
      width="1200px"
    >
      <el-table :data="examsList" border style="width: 100%">
        <el-table-column prop="examId" label="考试编号" width="80" />
        <el-table-column prop="examName" label="考试名称" width="200" />
        <el-table-column prop="subject.subjectName" label="学科名称" width="120" />
        <el-table-column prop="paper.paperName" label="试卷名称" width="200" />
        <el-table-column label="考试时间" width="180">
          <template #default="scope">
            {{ formatExamTime(scope.row.examStartTime, scope.row.examEndTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="examDuration" label="考试时长" width="100">
          <template #default="scope">
            {{ scope.row.examDuration }}分钟
          </template>
        </el-table-column>
        <el-table-column label="考试状态" width="100">
          <template #default="scope">
            <el-tag :type="getExamStatusType(scope.row.examStatus)">
              {{ getExamStatusText(scope.row.examStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="考试类型" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.examType === 0 ? 'primary' : 'warning'">
              {{ scope.row.examType === 0 ? '正常考试' : '重考' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Search, Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from 'axios'

// 数据列表
const classList = ref([])
const loading = ref(false)
const searchKeyword = ref('')

// 对话框控制
const editDialogVisible = ref(false)
const studentsDialogVisible = ref(false)
const examsDialogVisible = ref(false)
const isEdit = ref(false)

// 表单相关
const formRef = ref(null)
const form = ref({
  className: '',
  subjectId: null,
  finalExam: false,
  description: ''
})

// 学科选项
const subjectOptions = ref([])

// 列表数据
const studentsList = ref([])
const examsList = ref([])

// 表单验证规则
const formRules = {
  className: [
    { required: true, message: '请输入班级名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  subjectId: [
    { required: true, message: '请选择所属学科', trigger: 'change' }
  ],
  description: [
    { max: 200, message: '长度不能超过 200 个字符', trigger: 'blur' }
  ]
}

// 获取班级列表
const fetchClassList = async () => {
  loading.value = true
  try {
    const response = await axios.get('/api/teacher/classes')
    if (response.data.code === 200) {
      classList.value = response.data.data.list || []
    } else {
      ElMessage.error(response.data.message || '获取班级列表失败')
    }
  } catch (error) {
    console.error('获取班级列表失败:', error)
    ElMessage.error(error.response?.data?.message || '获取班级列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  fetchClassList()
}

// 添加班级
const handleAdd = () => {
  isEdit.value = false
  form.value = {
    className: '',
    subjectId: null,
    finalExam: false,
    description: ''
  }
  editDialogVisible.value = true
}

// 编辑班级
const handleEdit = (row) => {
  isEdit.value = true
  form.value = {
    classId: row.classId,
    className: row.className,
    description: row.description,
    finalExam: row.finalExam
  }
  editDialogVisible.value = true
}

// 查看学生
const handleViewStudents = async (row) => {
  try {
    const response = await axios.get(`/api/teacher/classes/${row.classId}/students`)
    if (response.data.code === 200) {
      studentsList.value = response.data.data.list || []
      studentsDialogVisible.value = true
    } else {
      ElMessage.error(response.data.message || '获取班级学生列表失败')
    }
  } catch (error) {
    console.error('获取班级学生列表失败:', error)
    ElMessage.error('获取班级学生列表失败')
  }
}

// 查看考试
const handleViewExams = async (row) => {
  try {
    const response = await axios.get(`/api/teacher/classes/${row.classId}/exam-schedule`)
    if (response.data.code === 200) {
      examsList.value = response.data.data || []
      examsDialogVisible.value = true
    } else {
      ElMessage.error(response.data.message || '获取班级考试安排失败')
    }
  } catch (error) {
    console.error('获取班级考试安排失败:', error)
    ElMessage.error('获取班级考试安排失败')
  }
}

// 移除学生
const handleRemoveStudent = async (student) => {
  try {
    await ElMessageBox.confirm(
      '确认将该学生移出班级吗？',
      '警告',
      {
        type: 'warning',
        confirmButtonText: '确定',
        cancelButtonText: '取消'
      }
    )
    
    const response = await axios.delete(`/api/teacher/classes/${form.value.classId}/students/${student.studentId}`)
    if (response.data.code === 200) {
      ElMessage.success('移除成功')
      studentsList.value = studentsList.value.filter(item => item.studentId !== student.studentId)
    } else {
      ElMessage.error(response.data.message || '移除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('移除学生失败:', error)
      ElMessage.error('移除失败')
    }
  }
}

// 保存班级
const handleSave = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    const url = isEdit.value 
      ? `/api/teacher/classes/${form.value.classId}`
      : '/api/teacher/classes/create'
    const method = isEdit.value ? 'put' : 'post'
    
    const response = await axios[method](url, form.value)
    
    if (response.data.code === 200) {
      ElMessage.success(isEdit.value ? '更新成功' : '创建成功')
      editDialogVisible.value = false
      fetchClassList()
    } else {
      ElMessage.error(response.data.message || (isEdit.value ? '更新失败' : '创建失败'))
    }
  } catch (error) {
    console.error(isEdit.value ? '更新班级失败:' : '创建班级失败:', error)
    ElMessage.error(error.response?.data?.message || (isEdit.value ? '更新失败' : '创建失败'))
  }
}

// 考试状态处理方法
const getExamStatusText = (status) => {
  const statusMap = {
    0: '未开始',
    1: '进行中',
    2: '已结束'
  }
  return statusMap[status] || '未知状态'
}

const getExamStatusType = (status) => {
  const typeMap = {
    0: 'info',
    1: 'success',
    2: 'warning'
  }
  return typeMap[status] || 'info'
}

// 获取学科列表
const fetchSubjectList = async () => {
  try {
    const response = await axios.get('/api/teacher/classes/subjects')
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

// 格式化考试时间
const formatExamTime = (startTime, endTime) => {
  const start = new Date(startTime)
  const end = new Date(endTime)
  return `${start.toLocaleDateString()} ${start.toLocaleTimeString()} - ${end.toLocaleTimeString()}`
}

// 初始化
onMounted(() => {
  fetchClassList()
  fetchSubjectList()
})
</script>

<style lang="scss" scoped>
.class-list {
  padding: 20px;
  
  .search-bar {
    margin-bottom: 20px;
    display: flex;
    align-items: center;
  }
}
</style> 
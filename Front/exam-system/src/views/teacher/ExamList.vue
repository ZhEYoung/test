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
        <el-form-item label="所属学科">
          <el-select v-model="searchForm.subjectId" placeholder="全部学科" clearable>
            <el-option
              v-for="item in subjectOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="考试状态">
          <el-select v-model="searchForm.examStatus" placeholder="全部状态" clearable>
            <el-option label="未开始" :value="0" />
            <el-option label="进行中" :value="1" />
            <el-option label="已结束" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handlePublishNormal">
            <el-icon><Plus /></el-icon>
            发布普通考试
          </el-button>
          <el-button type="success" @click="handlePublishFinal">
            <el-icon><Plus /></el-icon>
            发布期末考试
          </el-button>
          <el-button type="warning" @click="handlePublishRetake">
            <el-icon><Plus /></el-icon>
            发布重考考试
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 考试列表 -->
    <el-table
      v-loading="loading"
      :data="examList"
      border
      style="width: 100%"
    >
      <el-table-column prop="examId" label="考试ID" width="80" />
      <el-table-column prop="examName" label="考试名称" show-overflow-tooltip />
      <el-table-column prop="subject.subjectName" label="所属学科" width="120" />
      <el-table-column label="考试类型" width="100">
        <template #default="scope">
          <el-tag :type="scope.row.examType === 0 ? 'primary' : 'warning'">
            {{ scope.row.examType === 0 ? '普通考试' : '重考考试' }}
          </el-tag>
        </template>
      </el-table-column>
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
          <el-tag :type="getStatusType(scope.row.examStatus)">
            {{ getStatusText(scope.row.examStatus) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="300" fixed="right">
        <template #default="scope">
          <el-button
            type="primary"
            link
            @click="handleView(scope.row)"
          >
            查看
          </el-button>
          <el-button
            type="primary"
            link
            :disabled="scope.row.examStatus !== 0"
            @click="handleEdit(scope.row)"
          >
            编辑
          </el-button>
          <el-button
            type="info"
            link
            @click="handleViewStudents(scope.row)"
          >
            考生管理
          </el-button>
          <el-button
            type="warning"
            link
            v-if="scope.row.examStatus === 2"
            @click="handleRetakeStudents(scope.row)"
          >
            重考管理
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

    <!-- 考生列表对话框 -->
    <el-dialog
      v-model="studentsDialogVisible"
      :title="currentExam.examName + ' - 考生列表'"
      width="1000px"
    >
      <el-table :data="studentsList" border style="width: 100%">
        <el-table-column prop="student.studentId" label="学号" width="120" />
        <el-table-column prop="student.name" label="姓名" width="120" />
        <el-table-column label="考试状态" width="100">
          <template #default="scope">
            <el-tag :type="getExamStudentStatusType(scope.row)">
              {{ getExamStudentStatusText(scope.row) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="开始时间" width="180">
          <template #default="scope">
            {{ scope.row.studentStartTime ? formatDateTime(scope.row.studentStartTime) : '-' }}
          </template>
        </el-table-column>
        <el-table-column label="提交时间" width="180">
          <template #default="scope">
            {{ scope.row.studentSubmitTime ? formatDateTime(scope.row.studentSubmitTime) : '-' }}
          </template>
        </el-table-column>
        <el-table-column label="违纪原因" show-overflow-tooltip>
          <template #default="scope">
            {{ scope.row.teacherComment || '-' }}
          </template>
        </el-table-column>
        <el-table-column 
          label="操作" 
          width="120"
          v-if="currentExam.examStatus === 1"
        >
          <template #default="scope">
            <el-button
              type="danger"
              link
              v-if="scope.row.studentStartTime && !scope.row.studentSubmitTime"
              @click="handleMarkDisciplinary(scope.row)"
            >
              标记违纪
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <!-- 重考学生列表对话框 -->
    <el-dialog
      v-model="retakeDialogVisible"
      :title="currentExam.examName + ' - 重考学生'"
      width="1000px"
    >
      <div class="retake-stats">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="需要重考人数">{{ retakeStats.totalRetakeStudents }}</el-descriptions-item>
          <el-descriptions-item label="重考率">{{ retakeStats.retakeRate }}</el-descriptions-item>
        </el-descriptions>
      </div>

      <el-table :data="retakeStudentsList" border style="width: 100%">
        <el-table-column prop="student.studentId" label="学号" width="120" />
        <el-table-column prop="student.name" label="姓名" width="120" />
        <el-table-column label="重考原因" show-overflow-tooltip>
          <template #default="scope">
            {{ scope.row.disciplinaryReason || '成绩不合格' }}
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { Plus, Calendar } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import axios from 'axios'

const router = useRouter()

// 列表数据
const examList = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 当前选中的考试
const currentExam = ref({})

// 考生列表相关
const studentsDialogVisible = ref(false)
const studentsList = ref([])

// 重考学生相关
const retakeDialogVisible = ref(false)
const retakeStudentsList = ref([])
const retakeStats = ref({
  totalRetakeStudents: 0,
  retakeRate: '0%'
})

// 搜索表单数据
const searchForm = ref({
  examType: null,
  subjectId: null,
  examStatus: null
})

// 学科选项
const subjectOptions = ref([])

// 获取考试列表
const fetchExamList = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: currentPage.value,
      pageSize: pageSize.value,
      examType: searchForm.value.examType,
      subjectId: searchForm.value.subjectId,
      examStatus: searchForm.value.examStatus
    }

    const response = await axios.get('/api/teacher/exam/list', { params })
    if (response.data.code === 200) {
      // 使用Map进行去重，以examId为键
      const uniqueExams = new Map()
      response.data.data.list.forEach(exam => {
        if (!uniqueExams.has(exam.examId)) {
          uniqueExams.set(exam.examId, exam)
        }
      })
      examList.value = Array.from(uniqueExams.values())
      total.value = examList.value.length
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

// 格式化考试时间
const formatExamTime = (startTime, endTime) => {
  const start = new Date(startTime)
  const end = new Date(endTime)
  return `${start.toLocaleDateString()} ${start.toLocaleTimeString()} - ${end.toLocaleTimeString()}`
}

// 格式化日期时间
const formatDateTime = (time) => {
  const date = new Date(time)
  return `${date.toLocaleDateString()} ${date.toLocaleTimeString()}`
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

// 获取考生考试状态文本
const getExamStudentStatusText = (student) => {
  if (student.absent) return '缺考'
  if (student.disciplinary) return '违纪'
  if (!student.studentStartTime) return '未开始'
  if (!student.studentSubmitTime) return '进行中'
  if (student.retakeNeeded) return '需要重考'
  return '已完成'
}

// 获取考生考试状态类型
const getExamStudentStatusType = (student) => {
  if (student.absent) return 'danger'
  if (student.disciplinary) return 'danger'
  if (!student.studentStartTime) return 'info'
  if (!student.studentSubmitTime) return 'warning'
  if (student.retakeNeeded) return 'warning'
  return 'success'
}

// 分页
const handleSizeChange = (val) => {
  pageSize.value = val
  fetchExamList()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchExamList()
}

// 查看考试
const handleView = (row) => {
  router.push(`/teacher/exams/${row.examId}`)
}

// 编辑考试
const handleEdit = (row) => {
  router.push(`/teacher/exams/${row.examId}/edit`)
}

// 查看考生
const handleViewStudents = async (row) => {
  currentExam.value = row
  try {
    const response = await axios.get(`/api/teacher/exam/${row.examId}/students`)
    if (response.data.code === 200) {
      studentsList.value = response.data.data
      studentsDialogVisible.value = true
    } else {
      ElMessage.error(response.data.message || '获取考生列表失败')
    }
  } catch (error) {
    console.error('获取考生列表失败:', error)
    ElMessage.error('获取考生列表失败')
  }
}

// 标记违纪
const handleMarkDisciplinary = async (student) => {
  try {
    const { value: reason } = await ElMessageBox.prompt('请输入违纪原因', '标记违纪', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputValidator: (value) => {
        if (!value) {
          return '违纪原因不能为空'
        }
        return true
      }
    })

    const response = await axios.post(
      `/api/teacher/exam/${currentExam.value.examId}/students/${student.studentId}/disciplinary`,
      null,
      { params: { comment: reason } }
    )

    if (response.data.code === 200) {
      ElMessage.success('标记违纪成功')
      // 刷新考生列表
      handleViewStudents(currentExam.value)
    } else {
      ElMessage.error(response.data.message || '标记违纪失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('标记违纪失败:', error)
      ElMessage.error('标记违纪失败')
    }
  }
}

// 查看重考学生
const handleRetakeStudents = async (row) => {
  currentExam.value = row
  try {
    const response = await axios.get(`/api/teacher/exam/${row.examId}/retake-students`)
    if (response.data.code === 200) {
      retakeStudentsList.value = response.data.data.retakeStudents
      retakeStats.value = response.data.data.statistics
      retakeDialogVisible.value = true
    } else {
      ElMessage.error(response.data.message || '获取重考学生列表失败')
    }
  } catch (error) {
    console.error('获取重考学生列表失败:', error)
    ElMessage.error('获取重考学生列表失败')
  }
}

// 发布普通考试
const handlePublishNormal = () => {
  router.push('/teacher/exams/publish-normal')
}

// 发布期末考试
const handlePublishFinal = () => {
  router.push('/teacher/exams/publish-final')
}

// 发布重考考试
const handlePublishRetake = () => {
  router.push('/teacher/exams/publish-retake')
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

onMounted(() => {
  fetchExamList()
  fetchSubjects()
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

  .retake-stats {
    margin-bottom: 20px;
  }
}
</style> 
<template>
  <div class="class-list">
    <!-- 加入班级按钮 -->
    <div class="action-bar">
      <el-button type="primary" @click="handleJoinClass">
        <el-icon><Plus /></el-icon>
        加入班级
      </el-button>
    </div>

    <!-- 班级列表 -->
    <el-table
      v-loading="loading"
      :data="classList"
      border
      style="width: 100%"
    >
      <el-table-column prop="className" label="班级名称" min-width="180" />
      <el-table-column prop="subjectName" label="所属学科" min-width="150" />
      <el-table-column prop="teacherName" label="班主任" min-width="120" />
      <el-table-column label="班级统计" min-width="300">
        <template #default="{ row }">
          <div class="statistics-info">
            <el-tag type="info" effect="plain">
              总人数: {{ row.statistics?.studentCount || 0 }}
            </el-tag>
            <el-tag type="success" effect="plain">
              考试数: {{ row.statistics?.examCount || 0 }}
            </el-tag>
            <el-tag v-if="row.finalExam" type="warning" effect="plain">
              期末考试
            </el-tag>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="120" fixed="right">
        <template #default="{ row }">
          <el-button
            type="primary"
            link
            @click="handleViewDetail(row)"
          >
            查看详情
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pagination-container">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 30, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <!-- 加入班级对话框 -->
    <el-dialog
      v-model="joinDialogVisible"
      title="加入班级"
      width="400px"
    >
      <el-form
        ref="joinFormRef"
        :model="joinForm"
        :rules="joinFormRules"
        label-width="100px"
      >
        <el-form-item label="班级ID" prop="classId">
          <el-input
            v-model.number="joinForm.classId"
            placeholder="请输入班级ID"
            type="number"
          />
        </el-form-item>
        <el-form-item label="班级名称" prop="className">
          <el-input
            v-model="joinForm.className"
            placeholder="请输入班级名称"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="joinDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleJoinSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 班级详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="班级详情"
      width="800px"
    >
      <template v-if="classDetail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="班级名称">
            {{ classDetail.classInfo?.className }}
          </el-descriptions-item>
          <el-descriptions-item label="所属学科">
            {{ classDetail.classInfo?.subject?.subjectName }}
          </el-descriptions-item>
          <el-descriptions-item label="班主任">
            {{ classDetail.classInfo?.teacher?.name }}
          </el-descriptions-item>
          <el-descriptions-item label="是否有期末考试">
            {{ classDetail.classInfo?.finalExam ? '是' : '否' }}
          </el-descriptions-item>
        </el-descriptions>

        <div class="detail-section">
          <h3>班级统计</h3>
          <div class="statistics-cards">
            <el-card shadow="hover">
              <template #header>
                <div class="card-header">
                  <span>总人数</span>
                </div>
              </template>
              <div class="card-content">
                {{ classDetail.studentCount || 0 }}
              </div>
            </el-card>
            <el-card shadow="hover">
              <template #header>
                <div class="card-header">
                  <span>考试数量</span>
                </div>
              </template>
              <div class="card-content">
                {{ classDetail.examCount || 0 }}
              </div>
            </el-card>
          </div>
        </div>

        <div class="detail-section">
          <h3>即将到来的考试</h3>
          <el-table :data="classDetail.upcomingExams" border style="width: 100%">
            <el-table-column prop="examName" label="考试名称" min-width="200">
              <template #default="{ row }">
                {{ row.examType === 0 ? '普通考试-' : '重考考试-' }}{{ row.examName }}
              </template>
            </el-table-column>
            <el-table-column label="考试时间" min-width="300">
              <template #default="{ row }">
                {{ formatDateTime(row.examStartTime) }} 至 {{ formatDateTime(row.examEndTime) }}
              </template>
            </el-table-column>
            <el-table-column prop="examDuration" label="考试时长" width="100">
              <template #default="{ row }">
                {{ row.examDuration }}分钟
              </template>
            </el-table-column>
            <el-table-column label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="getExamStatusType(row.examStatus)">
                  {{ getExamStatusText(row.examStatus) }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'

// 列表数据
const loading = ref(false)
const classList = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 对话框控制
const joinDialogVisible = ref(false)
const detailDialogVisible = ref(false)

// 表单数据
const joinFormRef = ref(null)
const joinForm = ref({
  classId: '',
  className: ''
})

// 班级详情数据
const classDetail = ref(null)

// 表单验证规则
const joinFormRules = {
  classId: [
    { required: true, message: '请输入班级ID', trigger: 'blur' },
    { type: 'number', message: '班级ID必须为数字', trigger: 'blur' }
  ],
  className: [
    { required: true, message: '请输入班级名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ]
}

// 获取班级列表
const fetchClassList = async () => {
  loading.value = true
  try {
    const response = await axios.get('/api/student/class/list', {
      params: {
        pageNum: currentPage.value,
        pageSize: pageSize.value
      }
    })
    
    if (response.data.code === 200) {
      const { list, total: totalCount } = response.data.data
      classList.value = list.map(item => ({
        ...item,
        statistics: item.statistics || {}
      }))
      total.value = totalCount
    } else {
      ElMessage.error(response.data.message || '获取班级列表失败')
    }
  } catch (error) {
    console.error('获取班级列表失败:', error)
    ElMessage.error('获取班级列表失败')
  } finally {
    loading.value = false
  }
}

// 加入班级
const handleJoinClass = () => {
  joinForm.value = {
    classId: '',
    className: ''
  }
  joinDialogVisible.value = true
}

// 提交加入班级
const handleJoinSubmit = async () => {
  if (!joinFormRef.value) return
  
  try {
    await joinFormRef.value.validate()
    const response = await axios.post('/api/student/class/join', joinForm.value)
    
    if (response.data.code === 200) {
      ElMessage.success('加入班级成功')
      joinDialogVisible.value = false
      fetchClassList()
    } else {
      ElMessage.error(response.data.message || '加入班级失败')
    }
  } catch (error) {
    console.error('加入班级失败:', error)
    ElMessage.error(error.response?.data?.message || '加入班级失败')
  }
}

// 查看班级详情
const handleViewDetail = async (row) => {
  try {
    const response = await axios.get(`/api/student/class/detail/${row.classId}`)
    if (response.data.code === 200) {
      classDetail.value = response.data.data.statistics
      detailDialogVisible.value = true
    } else {
      ElMessage.error(response.data.message || '获取班级详情失败')
    }
  } catch (error) {
    console.error('获取班级详情失败:', error)
    ElMessage.error('获取班级详情失败')
  }
}

// 分页处理
const handleSizeChange = (val) => {
  pageSize.value = val
  fetchClassList()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchClassList()
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

// 获取考试状态类型
const getExamStatusType = (status) => {
  const typeMap = {
    0: 'info',
    1: 'success',
    2: 'warning'
  }
  return typeMap[status] || 'info'
}

// 获取考试状态文本
const getExamStatusText = (status) => {
  const statusMap = {
    0: '未开始',
    1: '进行中',
    2: '已结束'
  }
  return statusMap[status] || '未知状态'
}

onMounted(() => {
  fetchClassList()
})
</script>

<style lang="scss" scoped>
.class-list {
  padding: 20px;
  
  .action-bar {
    margin-bottom: 20px;
  }
  
  .statistics-info {
    display: flex;
    gap: 8px;
    
    .el-tag {
      margin-right: 8px;
    }
  }
  
  .pagination-container {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
  }
  
  .detail-section {
    margin-top: 20px;
    
    h3 {
      margin-bottom: 16px;
      font-weight: 500;
      color: #303133;
    }
    
    .statistics-cards {
      display: flex;
      gap: 16px;
      margin-bottom: 20px;
      
      .el-card {
        flex: 1;
        
        .card-header {
          display: flex;
          justify-content: space-between;
          align-items: center;
        }
        
        .card-content {
          text-align: center;
          font-size: 24px;
          color: #303133;
          padding: 20px 0;
        }
      }
    }
  }
}
</style> 
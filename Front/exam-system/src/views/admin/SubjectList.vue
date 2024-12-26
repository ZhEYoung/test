<template>
  <div class="subject-list">
    <div class="search-bar">
      <el-input
        v-model="searchKeyword"
        placeholder="请输入学科名称搜索"
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

      <el-select
        v-model="selectedCollege"
        placeholder="选择学院"
        clearable
        style="margin-left: 16px; width: 200px"
        @change="handleSearch"
      >
        <el-option
          v-for="item in collegeOptions"
          :key="item.value"
          :label="item.label"
          :value="item.value"
        />
      </el-select>

      <el-button type="primary" @click="handleAdd" style="margin-left: 16px">
        <el-icon><Plus /></el-icon>
        添加学科
      </el-button>
    </div>

    <el-table
      v-loading="loading"
      :data="subjectList"
      border
      style="width: 100%; margin-top: 20px;"
    >
      <el-table-column prop="subjectName" label="学科名称" width="150" />
      <el-table-column label="所属学院" width="150">
        <template #default="scope">
          {{ scope.row.college?.collegeName || '未分配' }}
        </template>
      </el-table-column>
      <el-table-column prop="description" label="学科描述" />
      <el-table-column label="操作" width="380" fixed="right">
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
            @click="handleViewDetail(scope.row)"
          >
            查看详情
          </el-button>
          <el-button
            type="info"
            size="small"
            @click="handleViewStatistics(scope.row)"
          >
            统计信息
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination">
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

    <!-- 编辑对话框 -->
    <el-dialog
      v-model="editDialogVisible"
      :title="isEdit ? '编辑学科' : '添加学科'"
      width="500px"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="formRules"
        label-width="100px"
      >
        <el-form-item label="学科名称" prop="subjectName">
          <el-input 
            v-model="form.subjectName" 
            placeholder="请输入学科名称"
          />
        </el-form-item>
        <el-form-item label="所属学院" prop="collegeId">
          <el-select 
            v-model="form.collegeId" 
            placeholder="请选择学院"
          >
            <el-option
              v-for="item in collegeOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="学科描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="3"
            placeholder="请输入学科描述"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="学科详情"
      width="600px"
    >
      <el-descriptions :column="2" border>
        <el-descriptions-item label="学科名称">{{ detail.subjectName }}</el-descriptions-item>
        <el-descriptions-item label="所属学院">{{ detail.college?.collegeName || '未分配' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">
          {{ detail.createdTime ? new Date(detail.createdTime).toLocaleString() : '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="教师数量">{{ detail.teacherCount || 0 }}</el-descriptions-item>
        <el-descriptions-item label="学生数量">{{ detail.studentCount || 0 }}</el-descriptions-item>
        <el-descriptions-item label="考试数量">{{ detail.examCount || 0 }}</el-descriptions-item>
        <el-descriptions-item label="学科描述" :span="2">{{ detail.description }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <!-- 统计信息对话框 -->
    <el-dialog
      v-model="statisticsDialogVisible"
      title="学科统计信息"
      width="800px"
    >
      <el-tabs v-model="activeStatTab">
        <el-tab-pane label="考试统计" name="exam">
          <div class="statistics-content">
            <el-row :gutter="20">
              <el-col :span="8">
                <div class="stat-item">
                  <div class="stat-label">总考试次数</div>
                  <div class="stat-value">{{ statistics.totalExams || 0 }}</div>
                </div>
              </el-col>
              <el-col :span="8">
                <div class="stat-item">
                  <div class="stat-label">平均分</div>
                  <div class="stat-value">{{ statistics.avgScore?.toFixed(2) || 0 }}</div>
                </div>
              </el-col>
              <el-col :span="8">
                <div class="stat-item">
                  <div class="stat-label">通过率</div>
                  <div class="stat-value">{{ (statistics.passRate * 100)?.toFixed(2) || 0 }}%</div>
                </div>
              </el-col>
            </el-row>
            <el-divider />
            <div class="chart-container">
              <!-- 这里可以添加图表展示 -->
            </div>
          </div>
        </el-tab-pane>
        <el-tab-pane label="学生统计" name="student">
          <div class="statistics-content">
            <el-row :gutter="20">
              <el-col :span="8">
                <div class="stat-item">
                  <div class="stat-label">总学生数</div>
                  <div class="stat-value">{{ statistics.totalStudents || 0 }}</div>
                </div>
              </el-col>
              <el-col :span="8">
                <div class="stat-item">
                  <div class="stat-label">优秀学生数</div>
                  <div class="stat-value">{{ statistics.excellentStudents || 0 }}</div>
                </div>
              </el-col>
              <el-col :span="8">
                <div class="stat-item">
                  <div class="stat-label">不及格学生数</div>
                  <div class="stat-value">{{ statistics.failedStudents || 0 }}</div>
                </div>
              </el-col>
            </el-row>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, onActivated } from 'vue'
import { Search, Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from 'axios'

// 数据列表
const subjectList = ref([])
const total = ref(0)
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const searchKeyword = ref('')
const selectedCollege = ref(null)

// 选择器数据
const collegeOptions = ref([])

// 对话框控制
const editDialogVisible = ref(false)
const detailDialogVisible = ref(false)
const statisticsDialogVisible = ref(false)
const isEdit = ref(false)
const activeStatTab = ref('exam')

// 表单相关
const formRef = ref(null)
const form = ref({
  subjectName: '',
  collegeId: null,
  description: ''
})

// 详情和统计数据
const detail = ref({})
const statistics = ref({})

// 表单验证规则
const formRules = {
  subjectName: [
    { required: true, message: '请输入学科名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  collegeId: [
    { required: true, message: '请选择所属学院', trigger: 'change' }
  ],
  description: [
    { required: true, message: '请输入学科描述', trigger: 'blur' },
    { max: 200, message: '长度不能超过 200 个字符', trigger: 'blur' }
  ]
}

// 获取学科列表
const fetchSubjectList = async () => {
  loading.value = true
  try {
    const response = await axios.get('/api/admin/subjects', {
      params: {
        keyword: searchKeyword.value,
        collegeId: selectedCollege.value,
        pageNum: currentPage.value,
        pageSize: pageSize.value
      }
    })
    
    if (response.data.code === 200) {
      subjectList.value = response.data.data.list
      total.value = response.data.data.total
    } else {
      ElMessage.error(response.data.message || '获取学科列表失败')
    }
  } catch (error) {
    console.error('获取学科列表失败:', error)
    ElMessage.error('获取学科列表失败')
  } finally {
    loading.value = false
  }
}

// 获取学院列表
const fetchCollegeList = async () => {
  try {
    const response = await axios.get('/api/admin/colleges')
    if (response.data.code === 200) {
      collegeOptions.value = [
        { value: null, label: '不限' },
        ...response.data.data.list.map(item => ({
          value: item.collegeId,
          label: item.collegeName
        }))
      ]
    }
  } catch (error) {
    console.error('获取学院列表失败:', error)
  }
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  fetchSubjectList()
}

// 分页大小改变
const handleSizeChange = (val) => {
  pageSize.value = val
  fetchSubjectList()
}

// 页码改变
const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchSubjectList()
}

// 添加学科
const handleAdd = () => {
  isEdit.value = false
  form.value = {
    subjectName: '',
    collegeId: null,
    description: ''
  }
  editDialogVisible.value = true
}

// 编辑学科
const handleEdit = (row) => {
  isEdit.value = true
  form.value = {
    subjectId: row.subjectId,
    subjectName: row.subjectName,
    collegeId: row.collegeId,
    description: row.description
  }
  editDialogVisible.value = true
}

// 查看详情
const handleViewDetail = async (row) => {
  try {
    const response = await axios.get(`/api/admin/subjects/${row.subjectId}`)
    if (response.data.code === 200) {
      detail.value = response.data.data
      detailDialogVisible.value = true
    } else {
      ElMessage.error(response.data.message || '获取学科详情失败')
    }
  } catch (error) {
    console.error('获取学科详情失败:', error)
    ElMessage.error('获取学科详情失败')
  }
}

// 查看统计信息
const handleViewStatistics = async (row) => {
  try {
    const response = await axios.get(`/api/admin/subjects/${row.subjectId}/statistics`)
    if (response.data.code === 200) {
      statistics.value = response.data.data
      statisticsDialogVisible.value = true
    } else {
      ElMessage.error(response.data.message || '获取统计信息失败')
    }
  } catch (error) {
    console.error('获取统计信息失败:', error)
    ElMessage.error('获取统计信息失败')
  }
}

// 保存
const handleSave = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    const url = isEdit.value 
      ? `/api/admin/subjects/${form.value.subjectId}`
      : '/api/admin/subjects'
    const method = isEdit.value ? 'put' : 'post'
    
    const response = await axios[method](url, {
      subjectName: form.value.subjectName,
      collegeId: form.value.collegeId,
      description: form.value.description
    })
    
    if (response.data.code === 200) {
      ElMessage.success(isEdit.value ? '更新成功' : '添加成功')
      editDialogVisible.value = false
      fetchSubjectList()
    } else {
      ElMessage.error(response.data.message || (isEdit.value ? '更新失败' : '添加失败'))
    }
  } catch (error) {
    console.error(isEdit.value ? '更新学科失败:' : '添加学科失败:', error)
    ElMessage.error(error.response?.data?.message || (isEdit.value ? '更新失败' : '添加失败'))
  }
}

// 初始化
onMounted(() => {
  fetchSubjectList()
  fetchCollegeList()
})

// 当组件被激活时
onActivated(() => {
  fetchSubjectList()
})
</script>

<style lang="scss" scoped>
.subject-list {
  padding: 20px;
  
  .search-bar {
    margin-bottom: 20px;
    display: flex;
    align-items: center;
  }
  
  .pagination {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
  }

  .statistics-content {
    padding: 20px;

    .stat-item {
      text-align: center;
      padding: 20px;
      background-color: #f5f7fa;
      border-radius: 4px;

      .stat-label {
        color: #606266;
        margin-bottom: 10px;
      }

      .stat-value {
        font-size: 24px;
        color: #303133;
        font-weight: bold;
      }
    }

    .chart-container {
      margin-top: 20px;
      height: 300px;
    }
  }
}
</style> 
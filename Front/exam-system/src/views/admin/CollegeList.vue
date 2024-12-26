<template>
  <div class="college-list">
    <div class="search-bar">
      <el-input
        v-model="searchKeyword"
        placeholder="请输入学院名称搜索"
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
        添加学院
      </el-button>
    </div>

    <el-table
      v-loading="loading"
      :data="collegeList"
      border
      style="width: 100%"
    >
      <el-table-column prop="collegeName" label="学院名称" width="200" />
      <el-table-column prop="description" label="学院描述" />
      <el-table-column label="创建时间" width="180">
        <template #default="scope">
          {{ new Date(scope.row.createdTime).toLocaleString() }}
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
            @click="handleViewDetail(scope.row)"
          >
            查看详情
          </el-button>
          <el-button
            type="info"
            size="small"
            @click="handleViewSubjects(scope.row)"
          >
            查看学科
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
      :title="isEdit ? '编辑学院' : '添加学院'"
      width="500px"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="formRules"
        label-width="100px"
      >
        <el-form-item label="学院名称" prop="collegeName">
          <el-input 
            v-model="form.collegeName" 
            placeholder="请输入学院名称"
          />
        </el-form-item>
        <el-form-item label="学院描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="3"
            placeholder="请输入学院描述"
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
      title="学院详情"
      width="600px"
    >
      <el-descriptions :column="2" border>
        <el-descriptions-item label="学院名称">{{ detail.collegeName }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">
          {{ detail.createdTime ? new Date(detail.createdTime).toLocaleString() : '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="教师数量">{{ detail.teacherCount || 0 }}</el-descriptions-item>
        <el-descriptions-item label="学生数量">{{ detail.studentCount || 0 }}</el-descriptions-item>
        <el-descriptions-item label="学科数量">{{ detail.subjectCount || 0 }}</el-descriptions-item>
        <el-descriptions-item label="学院描述" :span="2">{{ detail.description }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <!-- 学科列表对话框 -->
    <el-dialog
      v-model="subjectsDialogVisible"
      title="学科列表"
      width="600px"
    >
      <el-table :data="subjectsList" border style="width: 100%">
        <el-table-column prop="subjectName" label="学科名称" />
        <el-table-column prop="description" label="描述" />
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, onActivated } from 'vue'
import { Search, Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from 'axios'

// 数据列表
const collegeList = ref([])
const total = ref(0)
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const searchKeyword = ref('')

// 对话框控制
const editDialogVisible = ref(false)
const detailDialogVisible = ref(false)
const subjectsDialogVisible = ref(false)
const isEdit = ref(false)

// 表单相关
const formRef = ref(null)
const form = ref({
  collegeName: '',
  description: ''
})

// 详情数据
const detail = ref({})
const subjectsList = ref([])

// 表单验证规则
const formRules = {
  collegeName: [
    { required: true, message: '请输入学院名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  description: [
    { required: true, message: '请输入学院描述', trigger: 'blur' },
    { max: 200, message: '长度不能超过 200 个字符', trigger: 'blur' }
  ]
}

// 获取学院列表
const fetchCollegeList = async () => {
  loading.value = true
  try {
    const response = await axios.get('/api/admin/colleges', {
      params: {
        keyword: searchKeyword.value,
        pageNum: currentPage.value,
        pageSize: pageSize.value
      }
    })
    
    if (response.data.code === 200) {
      collegeList.value = response.data.data.list
      total.value = response.data.data.total
    } else {
      ElMessage.error(response.data.message || '获取学院列表失败')
    }
  } catch (error) {
    console.error('获取学院列表失败:', error)
    ElMessage.error('获取学院列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  fetchCollegeList()
}

// 分页大小改变
const handleSizeChange = (val) => {
  pageSize.value = val
  fetchCollegeList()
}

// 页码改变
const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchCollegeList()
}

// 添加学院
const handleAdd = () => {
  isEdit.value = false
  form.value = {
    collegeName: '',
    description: ''
  }
  editDialogVisible.value = true
}

// 编辑学院
const handleEdit = (row) => {
  isEdit.value = true
  form.value = {
    collegeId: row.collegeId,
    collegeName: row.collegeName,
    description: row.description
  }
  editDialogVisible.value = true
}

// 保存（添加/编辑）
const handleSave = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    const url = isEdit.value 
      ? `/api/admin/colleges/${form.value.collegeId}`
      : '/api/admin/colleges'
    const method = isEdit.value ? 'put' : 'post'
    
    const response = await axios[method](url, {
      collegeName: form.value.collegeName,
      description: form.value.description
    })
    
    if (response.data.code === 200) {
      ElMessage.success(isEdit.value ? '更新成功' : '添加成功')
      editDialogVisible.value = false
      fetchCollegeList()
    } else {
      ElMessage.error(response.data.message || (isEdit.value ? '更新失败' : '添加失败'))
    }
  } catch (error) {
    console.error(isEdit.value ? '更新学院失败:' : '添加学院失败:', error)
    ElMessage.error(error.response?.data?.message || (isEdit.value ? '更新失败' : '添加失败'))
  }
}

// 查看详情
const handleViewDetail = async (row) => {
  try {
    const response = await axios.get(`/api/admin/colleges/${row.collegeId}`)
    if (response.data.code === 200) {
      detail.value = {
        ...row,
        ...response.data.data
      }
      detailDialogVisible.value = true
    } else {
      ElMessage.error(response.data.message || '获取学院详情失败')
    }
  } catch (error) {
    console.error('获取学院详情失败:', error)
    ElMessage.error('获取学院详情失败')
  }
}

// 查看学科
const handleViewSubjects = async (row) => {
  try {
    const response = await axios.get(`/api/admin/colleges/${row.collegeId}/subjects`)
    if (response.data.code === 200) {
      subjectsList.value = response.data.data
      subjectsDialogVisible.value = true
    } else {
      ElMessage.error(response.data.message || '获取学科列表失败')
    }
  } catch (error) {
    console.error('获取学科列表失败:', error)
    ElMessage.error('获取学科列表失败')
  }
}

// 初始化
onMounted(() => {
  fetchCollegeList()
})

// 当组件被激活时
onActivated(() => {
  fetchCollegeList()
})
</script>

<style lang="scss" scoped>
.college-list {
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
}
</style> 
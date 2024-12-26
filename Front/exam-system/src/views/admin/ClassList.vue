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
        添加班级
      </el-button>
    </div>

    <el-table
      v-loading="loading"
      :data="classList"
      border
      style="width: 100%"
    >
      <el-table-column prop="className" label="班级名称" width="200" />
      <el-table-column label="所属学院" width="150">
        <template #default="scope">
          {{ scope.row.subject?.college?.collegeName || '未分配' }}
        </template>
      </el-table-column>
      <el-table-column prop="subject.subjectName" label="所属学科" width="150" />
      <el-table-column prop="teacher.name" label="班主任" width="120" />
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
      :title="isEdit ? '编辑班级' : '添加班级'"
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
        <el-form-item label="所属学院" prop="collegeId">
          <el-select 
            v-model="form.collegeId" 
            placeholder="请选择学院"
            @change="handleCollegeChange"
          >
            <el-option
              v-for="item in collegeOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="所属学科" prop="subjectId">
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
        <el-form-item label="班主任" prop="teacherId">
          <el-select 
            v-model="form.teacherId" 
            placeholder="请选择班主任"
          >
            <el-option
              v-for="item in teacherOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
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
      title="班级考试列表"
      width="1200px"
    >
      <el-table :data="examsList" border style="width: 100%">
        <el-table-column prop="examId" label="考试编号" width="80" />
        <el-table-column prop="examName" label="考试名称" width="200" />
        <el-table-column prop="subject.subjectName" label="学科名称" width="120" />
        <el-table-column prop="paper.paperName" label="试卷名称" width="200" />
        <el-table-column prop="teacher.name" label="创建教师" width="100" />
        <el-table-column label="考试时间" width="160">
          <template #default="scope">
            {{ new Date(scope.row.examStartTime).toLocaleString() }}
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
import { ref, onMounted, onActivated } from 'vue'
import { Search, Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from 'axios'

// 数据列表
const classList = ref([])
const total = ref(0)
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const searchKeyword = ref('')

// 选择器数据
const collegeOptions = ref([])
const subjectOptions = ref([])
const teacherOptions = ref([])

// 对话框控制
const editDialogVisible = ref(false)
const studentsDialogVisible = ref(false)
const examsDialogVisible = ref(false)
const isEdit = ref(false)

// 表单相关
const formRef = ref(null)
const form = ref({
  className: '',
  collegeId: null,
  subjectId: null,
  teacherId: null
})

// 列表数据
const studentsList = ref([])
const examsList = ref([])

// 表单验证规则
const formRules = {
  className: [
    { required: true, message: '请输入班级名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  collegeId: [
    { required: true, message: '请选择所属学院', trigger: 'change' }
  ],
  subjectId: [
    { required: true, message: '请选择所属学科', trigger: 'change' }
  ],
  teacherId: [
    { required: true, message: '请选择班主任', trigger: 'change' }
  ]
}

// 添加一个变量来保存当前操作的班级ID
const currentClassId = ref(null)

// 获取班级列表
const fetchClassList = async () => {
  loading.value = true
  try {
    const response = await axios.get('/api/admin/classes', {
      params: {
        keyword: searchKeyword.value,
        pageNum: currentPage.value,
        pageSize: pageSize.value
      }
    })
    
    if (response.data.code === 200) {
      const data = response.data.data
      classList.value = data.list || []
      total.value = parseInt(data.total) || 0

      console.log('班级列表数据:', data)
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

// 获取学院列表
const fetchCollegeList = async () => {
  try {
    const response = await axios.get('/api/admin/colleges')
    if (response.data.code === 200) {
      const data = response.data.data
      collegeOptions.value = (data.list || []).map(item => ({
        value: parseInt(item.collegeId),
        label: item.collegeName
      }))
      console.log('学院列表数据:', collegeOptions.value)
    }
  } catch (error) {
    console.error('获取学院列表失败:', error)
  }
}

// 获取学科列表
const fetchSubjectList = async (collegeId) => {
  try {
    const response = await axios.get(`/api/admin/colleges/${collegeId}/subjects`)
    if (response.data.code === 200) {
      const data = response.data.data
      subjectOptions.value = (data || []).map(item => ({
        value: parseInt(item.subjectId),
        label: item.subjectName
      }))
      console.log('学科列表数据:', subjectOptions.value)
    }
  } catch (error) {
    console.error('获取学科列表失败:', error)
  }
}

// 获取教师列表
const fetchTeacherList = async () => {
  try {
    const response = await axios.get('/api/admin/teachers')
    if (response.data.code === 200) {
      const data = response.data.data
      teacherOptions.value = (data.list || []).map(item => ({
        value: parseInt(item.teacherId),
        label: item.name
      }))
      console.log('教师列表数据:', teacherOptions.value)
    }
  } catch (error) {
    console.error('获取教师列表失败:', error)
  }
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  fetchClassList()
}

// 分页大小改变
const handleSizeChange = (val) => {
  pageSize.value = val
  fetchClassList()
}

// 页码改变
const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchClassList()
}

// 学院选择改变
const handleCollegeChange = (value) => {
  form.value.subjectId = null
  if (value) {
    fetchSubjectList(value)
  } else {
    subjectOptions.value = []
  }
}

// 添加班级
const handleAdd = () => {
  isEdit.value = false
  form.value = {
    className: '',
    collegeId: null,
    subjectId: null,
    teacherId: null
  }
  editDialogVisible.value = true
}

// 编辑班级
const handleEdit = (row) => {
  isEdit.value = true
  form.value = {
    classId: row.classId,
    className: row.className,
    collegeId: row.subject?.college?.collegeId,
    subjectId: row.subjectId,
    teacherId: row.teacherId
  }
  if (row.subject?.college?.collegeId) {
    fetchSubjectList(row.subject.college.collegeId)
  }
  editDialogVisible.value = true
}

// 查看学生
const handleViewStudents = async (row) => {
  try {
    const response = await axios.get(`/api/admin/classes/${row.classId}/students`)
    if (response.data.code === 200) {
      // 保存当前班级ID
      currentClassId.value = response.data.data.classId
      studentsList.value = response.data.data.list
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
    const response = await axios.get(`/api/admin/classes/${row.classId}/exams`)
    if (response.data.code === 200) {
      examsList.value = response.data.data.list
      examsDialogVisible.value = true
    } else {
      ElMessage.error(response.data.message || '获取班级考试列表失败')
    }
  } catch (error) {
    console.error('获取班级考试列表失败:', error)
    ElMessage.error('获取班级考试列表失败')
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
    
    // 使用保存的班级ID
    const response = await axios.delete(`/api/admin/classes/${currentClassId.value}/students/${student.studentId}`)
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
      ? `/api/admin/classes/${form.value.classId}`
      : '/api/admin/classes'
    const method = isEdit.value ? 'put' : 'post'
    
    const response = await axios[method](url, form.value)
    
    if (response.data.code === 200) {
      ElMessage.success(isEdit.value ? '更新成功' : '添加成功')
      editDialogVisible.value = false
      fetchClassList()
    } else {
      ElMessage.error(response.data.message || (isEdit.value ? '更新失败' : '添加失败'))
    }
  } catch (error) {
    console.error(isEdit.value ? '更新班级失败:' : '添加班级失败:', error)
    ElMessage.error(error.response?.data?.message || (isEdit.value ? '更新失败' : '添加失败'))
  }
}

// 初始化
onMounted(() => {
  fetchClassList()
  fetchCollegeList()
  fetchTeacherList()
})

// 当组件被激活时
onActivated(() => {
  fetchClassList()
})

// 在 script setup 中添加状态处理方法
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
</script>

<style lang="scss" scoped>
.class-list {
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
<template>
  <div class="teacher-list">
    <div class="search-bar">
      <el-input
        v-model="searchKeyword"
        placeholder="请输入教师姓名搜索"
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
        添加教师
      </el-button>
    </div>

    <el-table
      v-loading="loading"
      :data="teacherList"
      border
      style="width: 100%"
    >
      <el-table-column prop="user.username" label="用户名" width="120" />
      <el-table-column prop="name" label="姓名" width="120" />
      <el-table-column prop="user.phone" label="手机号" width="120" />
      <el-table-column prop="user.email" label="邮箱" width="180" />
      <el-table-column label="性别" width="80">
        <template #default="scope">
          {{ scope.row.user.sex ? '男' : '女' }}
        </template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="scope">
          <el-tag :type="scope.row.user.status ? 'success' : 'danger'">
            {{ scope.row.user.status ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="所属学院" width="150">
        <template #default="scope">
          {{ scope.row.college?.collegeName || '未分配' }}
        </template>
      </el-table-column>
      <el-table-column label="权限" width="120">
        <template #default="scope">
          {{ getPermissionLabel(scope.row.permission) }}
        </template>
      </el-table-column>
      <el-table-column prop="other" label="备注" />
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
            :type="scope.row.user.status ? 'danger' : 'success'"
            size="small"
            @click="handleToggleStatus(scope.row)"
          >
            {{ scope.row.user.status ? '禁用' : '启用' }}
          </el-button>
          <el-button
            type="warning"
            size="small"
            @click="handleResetPassword(scope.row)"
          >
            重置密码
          </el-button>
          <el-button
            type="info"
            size="small"
            @click="handleSimulateLogin(scope.row)"
          >
            模拟登录
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
      title="编辑教师"
      width="500px"
    >
      <el-form
        ref="editFormRef"
        :model="editForm"
        :rules="formRules"
        label-width="100px"
      >
        <el-form-item label="用户名" prop="username">
          <el-input v-model="editForm.username" disabled />
        </el-form-item>
        <el-form-item label="姓名" prop="name">
          <el-input 
            v-model="editForm.name" 
            placeholder="请输入2-20位中文姓名"
          />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input 
            v-model="editForm.phone" 
            placeholder="请输入11位手机号码"
          />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input 
            v-model="editForm.email" 
            placeholder="请输入有效的邮箱地址"
          />
        </el-form-item>
        <el-form-item label="性别" prop="sex">
          <el-radio-group v-model="editForm.sex">
            <el-radio :label="true">男</el-radio>
            <el-radio :label="false">女</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="所属学院" prop="collegeId">
          <el-select v-model="editForm.collegeId" placeholder="请选择学院">
            <el-option
              v-for="item in collegeOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="权限" prop="permission">
          <el-select v-model="editForm.permission" placeholder="请选择权限">
            <el-option label="可以组卷与发布所有考试" :value="0" />
            <el-option label="可以组卷与发布普通考试" :value="1" />
            <el-option label="可以组卷" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注" prop="other">
          <el-input
            v-model="editForm.other"
            type="textarea"
            :rows="3"
            placeholder="请输入备注信息（不超过100字）"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveEdit">保存</el-button>
      </template>
    </el-dialog>

    <!-- 添加教师对话框 -->
    <el-dialog
      v-model="addDialogVisible"
      title="添加教师"
      width="500px"
    >
      <el-form
        ref="addFormRef"
        :model="addForm"
        :rules="addFormRules"
        label-width="100px"
      >
        <el-form-item label="用户名" prop="username">
          <el-input 
            v-model="addForm.username" 
            placeholder="请输入4-10位字母、数字或下划线"
          />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="addForm.password"
            type="password"
            show-password
            placeholder="6-20位，必须包含大小写字母和数字"
          />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="addForm.confirmPassword"
            type="password"
            show-password
            placeholder="请再次输入密码"
          />
        </el-form-item>
        <el-form-item label="姓名" prop="name">
          <el-input 
            v-model="addForm.name" 
            placeholder="请输入2-20位中文姓名"
          />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input 
            v-model="addForm.phone" 
            placeholder="请输入11位手机号码"
          />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input 
            v-model="addForm.email" 
            placeholder="请输入有效的邮箱地址"
          />
        </el-form-item>
        <el-form-item label="性别" prop="sex">
          <el-radio-group v-model="addForm.sex">
            <el-radio :label="true">男</el-radio>
            <el-radio :label="false">女</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="所属学院" prop="collegeId">
          <el-select v-model="addForm.collegeId" placeholder="请选择学院">
            <el-option
              v-for="item in collegeOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="权限" prop="permission">
          <el-select v-model="addForm.permission" placeholder="请选择权限">
            <el-option label="可以组卷与发布所有考试" :value="0" />
            <el-option label="可以组卷与发布普通考试" :value="1" />
            <el-option label="可以组卷" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注" prop="other">
          <el-input
            v-model="addForm.other"
            type="textarea"
            :rows="3"
            placeholder="请输入备注信息（不超过100字）"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveAdd">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, watch, onActivated } from 'vue'
import { Search, Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from 'axios'
import { useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'

// 数据列表
const route = useRoute()
const teacherList = ref([])
const total = ref(0)
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const searchKeyword = ref('')
const selectedCollege = ref(null)

// 学院选项（这里需要从后端获取）
const collegeOptions = ref([])

// 获取学院列表
const fetchCollegeList = async () => {
  try {
    const response = await axios.get('/api/admin/teachers', {
      params: {
        keyword: '',
        pageNum: 1,
        pageSize: 10
      }
    })
    
    if (response.data.code === 200 && response.data.data.colleges) {
      collegeOptions.value = [
        { value: null, label: '不限' },
        ...response.data.data.colleges.map(college => ({
          value: college.collegeId,
          label: college.collegeName
        }))
      ]
    } else {
      ElMessage.error('获取学院列表失败')
    }
  } catch (error) {
    console.error('获取学院列表失败:', error)
    ElMessage.error('获取学院列表失败')
  }
}

// 编辑对话框相关
const editDialogVisible = ref(false)
const editFormRef = ref(null)
const editForm = ref({
  teacherId: null,
  userId: null,
  username: '',
  name: '',
  phone: '',
  email: '',
  sex: true,
  collegeId: null,
  permission: 0,
  other: ''
})

// 表单验证规则
const formRules = {
  name: [
    { required: true, message: '请输入姓名', trigger: 'blur' },
    { min: 2, max: 20, message: '姓名长度必须在2-20之间', trigger: 'blur' },
    { pattern: /^[\u4e00-\u9fa5]{2,20}$/, message: '姓名必须为中文', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  sex: [
    { required: true, message: '请选择性别', trigger: 'change' }
  ],
  collegeId: [
    { required: true, message: '请选择所属学院', trigger: 'change' }
  ],
  permission: [
    { required: true, message: '请选择权限', trigger: 'change' }
  ],
  other: [
    { max: 100, message: '备注长度不能超过100个字符', trigger: 'blur' }
  ]
}

// 添加教师相关
const addDialogVisible = ref(false)
const addFormRef = ref(null)
const addForm = ref({
  username: '',
  password: '',
  confirmPassword: '',
  name: '',
  phone: '',
  email: '',
  sex: true,
  collegeId: null,
  permission: 1,
  other: '',
  role: 1 // 1 表示教师
})

// 添加表单验证规则
const addFormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 4, max: 10, message: '用户名长度必须在4-10之间', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_]+$/, message: '用户名只能包含字母、数字和下划线', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度必须在6-20之间', trigger: 'blur' },
    { 
      pattern: /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)[a-zA-Z\d]{6,20}$/,
      message: '密码必须包含大小写字母和数字',
      trigger: 'blur'
    }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== addForm.value.password) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  ...formRules
}

// 获取教师列表
const fetchTeacherList = async () => {
  loading.value = true
  try {
    const response = await axios.get('/api/admin/teachers', {
      params: {
        keyword: searchKeyword.value,
        collegeId: selectedCollege.value,
        pageNum: currentPage.value,
        pageSize: pageSize.value
      }
    })
    
    if (response.data.code === 200) {
      teacherList.value = response.data.data.list
      total.value = response.data.data.total
    } else {
      ElMessage.error(response.data.message || '获取教师列表失败')
    }
  } catch (error) {
    console.error('获取教师列表失败:', error)
    ElMessage.error('获取教师列表失败')
  } finally {
    loading.value = false
  }
}

// 获取权限标签
const getPermissionLabel = (permission) => {
  switch (permission) {
    case 0:
      return '可以组卷与发布所有考试'
    case 1:
      return '可以组卷与发布普通考试'
    case 2:
      return '可以组卷'
    default:
      return '未知权限'
  }
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  fetchTeacherList()
}

// 分页大小改变
const handleSizeChange = (val) => {
  pageSize.value = val
  fetchTeacherList()
}

// 页码改变
const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchTeacherList()
}

// 编辑教师
const handleEdit = (row) => {
  editForm.value = {
    teacherId: row.teacherId,
    userId: row.userId,
    username: row.user.username,
    name: row.name,
    phone: row.user.phone,
    email: row.user.email,
    sex: row.user.sex,
    collegeId: row.collegeId,
    permission: row.permission,
    other: row.other
  }
  editDialogVisible.value = true
}

// 保存编辑
const handleSaveEdit = async () => {
  if (!editFormRef.value) return
  
  try {
    await editFormRef.value.validate()
    const response = await axios.put(`/api/admin/teachers/${editForm.value.teacherId}`, {
      name: editForm.value.name,
      phone: editForm.value.phone,
      email: editForm.value.email,
      sex: editForm.value.sex,
      collegeId: editForm.value.collegeId,
      permission: editForm.value.permission,
      other: editForm.value.other
    })
    
    if (response.data.code === 200) {
      ElMessage.success('更新成功')
      editDialogVisible.value = false
      fetchTeacherList()
    } else {
      ElMessage.error(response.data.message || '更新失败')
    }
  } catch (error) {
    console.error('更新教师失败:', error.response || error)
    ElMessage.error(error.response?.data?.message || '更新失败')
  }
}

// 切换状态
const handleToggleStatus = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确认${row.user.status ? '禁用' : '启用'}该教师吗？`,
      '提示',
      {
        type: 'warning'
      }
    )
    
    const response = await axios.put(`/api/admin/teachers/${row.teacherId}/status`, null, {
      params: {
        status: !row.user.status
      }
    })
    
    if (response.data.code === 200) {
      ElMessage.success(`${row.user.status ? '禁用' : '启用'}成功`)
      fetchTeacherList()
    } else {
      ElMessage.error(response.data.message || '操作失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('更新状态失败:', error)
      ElMessage.error('操作失败')
    }
  }
}

// 重置密码
const handleResetPassword = async (row) => {
  try {
    await ElMessageBox.confirm(
      '确认重置该教师的密码吗？',
      '警告',
      {
        type: 'warning',
        confirmButtonText: '确定重置',
        cancelButtonText: '取消'
      }
    )
    
    const response = await axios.put(`/api/admin/teachers/${row.teacherId}/password/reset`)
    
    if (response.data.code === 200) {
      ElMessage.success('密码已重置为：1234567')
    } else {
      ElMessage.error(response.data.message || '密码重置失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('重置密码失败:', error)
      ElMessage.error('密码重置失败')
    }
  }
}

// 处理添加按钮点击
const handleAdd = () => {
  addForm.value = {
    username: '',
    password: '',
    confirmPassword: '',
    name: '',
    phone: '',
    email: '',
    sex: true,
    collegeId: null,
    permission: 1,
    other: '',
    role: 1
  }
  addDialogVisible.value = true
}

// 处理保存添加
const handleSaveAdd = async () => {
  if (!addFormRef.value) return
  
  try {
    await addFormRef.value.validate()
    const response = await axios.post('/api/admin/teachers', {
      username: addForm.value.username,
      password: addForm.value.password,
      name: addForm.value.name,
      phone: addForm.value.phone,
      email: addForm.value.email,
      sex: addForm.value.sex,
      collegeId: addForm.value.collegeId,
      permission: addForm.value.permission,
      other: addForm.value.other
    })
    
    if (response.data.code === 200) {
      ElMessage.success('添加成功')
      addDialogVisible.value = false
      fetchTeacherList()
    } else {
      ElMessage.error(response.data.message || '添加失败')
    }
  } catch (error) {
    console.error('添加教师失败:', error.response || error)
    ElMessage.error(error.response?.data?.message || '添加失败')
  }
}

// 初始化和路由监听
onMounted(() => {
  fetchCollegeList()
  fetchTeacherList()
})

// 当组件被激活时（从缓存中恢复）
onActivated(() => {
  console.log('TeacherList activated')
  fetchCollegeList()
  fetchTeacherList()
})

// 监听路由变化
watch(
  () => route.path,
  (newPath, oldPath) => {
    console.log('Route changed:', oldPath, '->', newPath)
    if (newPath.includes('/admin/teachers')) {
      fetchTeacherList()
    }
  }
)

// 在 script setup 中初始化 userStore
const userStore = useUserStore()

// 修改模拟登录处理方法
const handleSimulateLogin = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确认要以 ${row.user.username} 的身份登录吗？`,
      '模拟登录确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const success = await userStore.simulateLogin(row.user.userId)
    if (!success) {
      ElMessage.error('模拟登录失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('模拟登录失败:', error)
      ElMessage.error('模拟登录失败')
    }
  }
}
</script>

<style lang="scss" scoped>
.teacher-list {
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
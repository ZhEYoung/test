<template>
  <div class="admin-list">
    <div class="search-bar">
      <el-input
        v-model="searchKeyword"
        placeholder="请输入管理员姓名搜索"
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
        添加管理员
      </el-button>
    </div>

    <el-table
      v-loading="loading"
      :data="adminList"
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
      <el-table-column prop="other" label="备注" />
      <el-table-column label="操作" width="200" fixed="right">
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
  </div>

  <!-- 编辑对话框 -->
  <el-dialog
    v-model="editDialogVisible"
    title="编辑管理员"
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
      <el-form-item label="状态" prop="status">
        <el-switch
          v-model="editForm.status"
          :active-value="true"
          :inactive-value="false"
          active-text="启用"
          inactive-text="禁用"
        />
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

  <!-- 添加管理员对话框 -->
  <el-dialog
    v-model="addDialogVisible"
    title="添加管理员"
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
const adminList = ref([])
const total = ref(0)
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const searchKeyword = ref('')

// 编辑对话框相关
const editDialogVisible = ref(false)
const editFormRef = ref(null)
const editForm = ref({
  adminId: null,
  userId: null,
  username: '',
  name: '',
  phone: '',
  email: '',
  sex: true,
  status: true,
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
  other: [
    { max: 100, message: '备注长度不能超过100个字符', trigger: 'blur' }
  ]
}

// 添加管理员相关
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
  other: '',
  role: 0 // 0 表示管理员
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
  other: [
    { max: 100, message: '备注长度不能超过100个字符', trigger: 'blur' }
  ]
}

// 获取管理员列表
const fetchAdminList = async () => {
  loading.value = true
  try {
    const response = await axios.get('/api/admin/admins', {
      params: {
        keyword: searchKeyword.value,
        pageNum: currentPage.value,
        pageSize: pageSize.value
      }
    })
    
    if (response.data.code === 200) {
      adminList.value = response.data.data.list
      total.value = response.data.data.total
    } else {
      ElMessage.error(response.data.message || '获取管理员列表失败')
    }
  } catch (error) {
    console.error('获取管理员列表失败:', error)
    ElMessage.error('获取管理员列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  fetchAdminList()
}

// 分页大小改变
const handleSizeChange = (val) => {
  pageSize.value = val
  fetchAdminList()
}

// 页码改变
const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchAdminList()
}

// 编辑管理员
const handleEdit = (row) => {
  editForm.value = {
    adminId: row.adminId,
    userId: row.userId,
    username: row.user.username,
    name: row.name,
    phone: row.user.phone,
    email: row.user.email,
    sex: row.user.sex,
    status: row.user.status,
    other: row.other
  }
  editDialogVisible.value = true
}

// 保存编辑
const handleSaveEdit = async () => {
  if (!editFormRef.value) return
  
  try {
    await editFormRef.value.validate()
    const response = await axios.put(`/api/admin/admins/${editForm.value.adminId}`, {
      name: editForm.value.name,
      phone: editForm.value.phone,
      email: editForm.value.email,
      sex: editForm.value.sex,
      status: editForm.value.status,
      other: editForm.value.other
    })
    
    if (response.data.code === 200) {
      ElMessage.success('更新成功')
      editDialogVisible.value = false
      fetchAdminList() // 刷新列表
    } else {
      ElMessage.error(response.data.message || '更新失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('更新管理员失败:', error.response || error)
      ElMessage.error(error.response?.data?.message || '更新失败')
    }
  }
}

// 切换状态
const handleToggleStatus = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确认${row.user.status ? '禁用' : '启用'}该管理员吗？`,
      '提示',
      {
        type: 'warning'
      }
    )
    
    const response = await axios.put(`/api/admin/admins/${row.adminId}/status`, null, {
      params: {
        status: !row.user.status
      }
    })
    
    if (response.data.code === 200) {
      ElMessage.success(`${row.user.status ? '禁用' : '启用'}成功`)
      fetchAdminList()
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

// 删除管理员
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      '确认删除该管理员吗？此操作不可恢复！',
      '警告',
      {
        type: 'warning',
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        confirmButtonClass: 'el-button--danger'
      }
    )
    
    const response = await axios.delete(`/api/admin/admins/${row.adminId}`)
    
    if (response.data.code === 200) {
      ElMessage.success('删除成功')
      if (adminList.value.length === 1 && currentPage.value > 1) {
        currentPage.value--
      }
      fetchAdminList()
    } else {
      ElMessage.error(response.data.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除管理员失败:', error)
      ElMessage.error('删除失败')
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
    other: '',
    role: 0
  }
  addDialogVisible.value = true
}

// 处理保存添加
const handleSaveAdd = async () => {
  if (!addFormRef.value) return
  
  try {
    await addFormRef.value.validate()
    const response = await axios.post('/api/auth/register/staff', {
      username: addForm.value.username,
      password: addForm.value.password,
      name: addForm.value.name,
      phone: addForm.value.phone,
      email: addForm.value.email,
      sex: addForm.value.sex,
      other: addForm.value.other,
      role: addForm.value.role
    })
    
    if (response.data.code === 200) {
      ElMessage.success('添加成功')
      addDialogVisible.value = false
      fetchAdminList() // 刷新列表
    } else {
      ElMessage.error(response.data.message || '添加失败')
    }
  } catch (error) {
    console.error('添加管理员失败:', error.response || error)
    ElMessage.error(error.response?.data?.message || '添加失败')
  }
}

// 初始化和路由监听
onMounted(() => {
  fetchAdminList()
})

onActivated(() => {
  fetchAdminList()
})

// 监听路由变化
watch(
  () => route.path,
  () => {
    fetchAdminList()
  }
)
</script>

<style lang="scss" scoped>
.admin-list {
  padding: 20px;
  
  .search-bar {
    margin-bottom: 20px;
  }
  
  .pagination {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
  }
}
</style> 
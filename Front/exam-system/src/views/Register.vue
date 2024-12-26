<template>
  <div class="register-container">
    <el-card class="register-card">
      <template #header>
        <div class="card-header">
          <h2>学生注册</h2>
          <p class="subtitle">请填写注册信息</p>
        </div>
      </template>
      
      <el-form
        ref="registerFormRef"
        :model="registerForm"
        :rules="registerRules"
        label-width="80px"
        @keyup.enter="handleRegister"
      >
        <el-form-item label="用户名" prop="username">
          <el-input
            v-model="registerForm.username"
            placeholder="4-10位字母、数字或下划线"
            clearable
            :disabled="loading"
          />
        </el-form-item>
        
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="registerForm.password"
            type="password"
            placeholder="6-20位，必须包含大小写字母和数字"
            show-password
            clearable
            :disabled="loading"
          />
        </el-form-item>
        
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="registerForm.confirmPassword"
            type="password"
            placeholder="请再次输入相同的密码"
            show-password
            clearable
            :disabled="loading"
          />
        </el-form-item>

        <el-form-item label="姓名" prop="name">
          <el-input
            v-model="registerForm.name"
            placeholder="请输入2-20位中文姓名"
            clearable
            :disabled="loading"
          />
        </el-form-item>

        <el-form-item label="性别" prop="sex">
          <el-radio-group v-model="registerForm.sex" :disabled="loading">
            <el-radio :label="true">男</el-radio>
            <el-radio :label="false">女</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="手机号" prop="phone">
          <el-input
            v-model="registerForm.phone"
            placeholder="请输入11位手机号码"
            clearable
            :disabled="loading"
          />
        </el-form-item>

        <el-form-item label="邮箱" prop="email">
          <el-input
            v-model="registerForm.email"
            placeholder="请输入有效的邮箱地址（如：example@email.com）"
            clearable
            :disabled="loading"
          />
        </el-form-item>

        <el-form-item label="年级" prop="grade">
          <el-select
            v-model="registerForm.grade"
            placeholder="请选择年级"
            clearable
            :disabled="loading"
          >
            <el-option
              v-for="year in gradeYears"
              :key="year"
              :label="year"
              :value="year"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="学院" prop="collegeId">
          <el-select
            v-model="registerForm.collegeId"
            placeholder="请选择学院"
            clearable
            :disabled="loading"
          >
            <el-option
              v-for="item in collegeList"
              :key="item.collegeId"
              :label="item.collegeName"
              :value="item.collegeId"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="备注" prop="other">
          <el-input
            v-model="registerForm.other"
            type="textarea"
            :rows="3"
            placeholder="请输入备注信息（选填，不超过100字）"
            clearable
            :disabled="loading"
          />
        </el-form-item>
        
        <el-form-item>
          <div class="button-group">
            <el-button
              type="primary"
              :loading="loading"
              class="register-button"
              @click="handleRegister"
            >
              {{ loading ? '注册中...' : '注册' }}
            </el-button>
            <el-button 
              @click="handleBack" 
              :disabled="loading"
              class="back-button"
            >
              返回登录
            </el-button>
          </div>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import axios from 'axios'

const router = useRouter()

// 注册表单
const registerFormRef = ref(null)
const registerForm = ref({
  username: '',
  password: '',
  confirmPassword: '',
  name: '',
  sex: true,
  phone: '',
  email: '',
  grade: '',
  collegeId: '',
  other: '',
  status: true
})

// 验证手机号的正则表达式
const validatePhone = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请输入手机号'))
  } else if (!/^1[3-9]\d{9}$/.test(value)) {
    callback(new Error('请输入正确的手机号'))
  } else {
    callback()
  }
}

// 验证邮箱的正则表达式
const validateEmail = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请输入邮箱'))
  } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value)) {
    callback(new Error('请输入正确的邮箱格式'))
  } else {
    callback()
  }
}

// 验证确认密码
const validateConfirmPassword = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请再次输入密码'))
  } else if (value !== registerForm.value.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

// 学院列表
const collegeList = ref([])

// 获取学院列表
const fetchCollegeList = async () => {
  try {
    const response = await axios.get('/api/auth/college')
    if (response.data.code === 200) {
      collegeList.value = response.data.data
    } else {
      ElMessage.error(response.data.msg || '获取学院列表失败')
    }
  } catch (error) {
    console.error('获取学院列表失败:', error)
    ElMessage.error('获取学院列表失败')
  }
}

// 生成年级年份选项
const gradeYears = ref([])
const generateGradeYears = () => {
  const currentYear = new Date().getFullYear()
  gradeYears.value = [
    currentYear.toString(),
    (currentYear + 1).toString(),
    (currentYear + 2).toString(),
    (currentYear + 3).toString()
  ]
}

// 表单验证规则
const registerRules = {
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
    { validator: validateConfirmPassword, trigger: 'blur' }
  ],
  name: [
    { required: true, message: '请输入姓名', trigger: 'blur' },
    { min: 2, max: 20, message: '姓名长度必须在2-20之间', trigger: 'blur' },
    { pattern: /^[\u4e00-\u9fa5]{2,20}$/, message: '姓名必须为中文', trigger: 'blur' }
  ],
  sex: [
    { required: true, message: '请选择性别', trigger: 'change' }
  ],
  phone: [
    { validator: validatePhone, trigger: 'blur' }
  ],
  email: [
    { validator: validateEmail, trigger: 'blur' }
  ],
  grade: [
    { required: true, message: '请选择年级', trigger: 'change' }
  ],
  collegeId: [
    { required: true, message: '请选择学院', trigger: 'change' }
  ],
  other: [
    { max: 100, message: '备注长度不能超过100个字符', trigger: 'blur' }
  ]
}

// 加载状态
const loading = ref(false)

// 在组件挂载时获取学院列表和生成年级选项
onMounted(() => {
  fetchCollegeList()
  generateGradeYears()
})

// 处理注册
const handleRegister = async () => {
  if (!registerFormRef.value) return
  
  try {
    // 表单验证
    await registerFormRef.value.validate()
    loading.value = true
    
    // 构造注册数据
    const registerData = {
      username: registerForm.value.username,
      password: registerForm.value.password,
      name: registerForm.value.name,
      sex: registerForm.value.sex,
      phone: registerForm.value.phone,
      email: registerForm.value.email,
      grade: registerForm.value.grade,
      collegeId: registerForm.value.collegeId,
      other: registerForm.value.other,
      status: true
    }
    
    // 调用注册接口
    const response = await axios.post('/api/auth/register/student', registerData)
    if (response.data.code === 200) {
      ElMessage.success('注册成功')
      router.push('/login')
    } else {
      ElMessage.error(response.data.msg || '注册失败')
    }
  } catch (error) {
    console.error('Register error:', error)
    ElMessage.error(error.response?.data?.msg || '注册失败')
  } finally {
    loading.value = false
  }
}

// 返回登录页
const handleBack = () => {
  if (loading.value) return
  router.push('/login')
}
</script>

<style lang="scss" scoped>
.register-container {
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #8BC6EC 0%, #9599E2 100%);
  
  .register-card {
    width: 500px;
    
    .card-header {
      text-align: center;
      
      h2 {
        margin: 0;
        font-size: 24px;
        color: #303133;
      }
      
      .subtitle {
        margin-top: 8px;
        color: #909399;
        font-size: 14px;
      }
    }

    .button-group {
      display: flex;
      justify-content: center;
      gap: 20px;
      margin-top: 10px;

      .register-button,
      .back-button {
        width: 120px;
        height: 40px;
        font-size: 16px;
        border-radius: 4px;
      }

      .register-button {
        background: #409EFF;
        border-color: #409EFF;
        &:hover {
          background: #66b1ff;
          border-color: #66b1ff;
        }
      }

      .back-button {
        background: #ffffff;
        border: 1px solid #dcdfe6;
        color: #606266;
        &:hover {
          color: #409EFF;
          border-color: #c6e2ff;
          background-color: #ecf5ff;
        }
      }
    }
  }
}
</style> 
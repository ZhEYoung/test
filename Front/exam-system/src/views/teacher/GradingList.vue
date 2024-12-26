<template>
  <div class="grading-list">
    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-form :inline="true" :model="searchForm" class="form">
        <el-form-item label="考试状态">
          <el-select v-model="searchForm.examStatus" placeholder="全部状态" clearable>
            <el-option label="未开始" :value="0" />
            <el-option label="进行中" :value="1" />
            <el-option label="已结束" :value="2" />
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
      <el-table-column label="考试状态" width="100">
        <template #default="scope">
          <el-tag :type="getStatusType(scope.row.examStatus)">
            {{ getStatusText(scope.row.examStatus) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="scope">
          <el-button
            type="primary"
            link
            :disabled="scope.row.examStatus !== 2"
            @click="handleGrade(scope.row)"
          >
            批改试题
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
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import axios from 'axios'

const router = useRouter()

// 搜索表单
const searchForm = ref({
  examStatus: null,
  subjectId: null
})

// 列表数据
const examList = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 学科选项
const subjectOptions = ref([])

// 获取考试列表
const fetchExamList = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: currentPage.value,
      pageSize: pageSize.value,
      examStatus: searchForm.value.examStatus,
      subjectId: searchForm.value.subjectId
    }

    const response = await axios.get('/api/teacher/exam/list', { params })
    if (response.data.code === 200) {
      examList.value = response.data.data.list
      total.value = response.data.data.total
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

// 格式化考试时间
const formatExamTime = (startTime, endTime) => {
  const start = new Date(startTime)
  const end = new Date(endTime)
  return `${start.toLocaleDateString()} ${start.toLocaleTimeString()} - ${end.toLocaleTimeString()}`
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

// 分页
const handleSizeChange = (val) => {
  pageSize.value = val
  fetchExamList()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchExamList()
}

// 批改试题
const handleGrade = (row) => {
  router.push(`/teacher/grading/${row.examId}`)
}

onMounted(() => {
  fetchExamList()
  fetchSubjects()
})
</script>

<style lang="scss" scoped>
.grading-list {
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
}
</style>
<template>
  <div class="paper-list">
    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-form :inline="true" :model="searchForm" class="form">
        <el-form-item>
          <el-input
            v-model="searchForm.keyword"
            placeholder="请输入试卷名称"
            clearable
            @clear="handleSearch"
          >
            <template #append>
              <el-button @click="handleSearch">
                <el-icon><Search /></el-icon>
              </el-button>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item>
          <el-select 
            v-model="searchForm.subjectId" 
            placeholder="选择学科" 
            clearable
            @change="handleSearch"
          >
            <el-option
              v-for="item in subjectOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>

        <el-form-item>
          <el-select 
            v-model="searchForm.status" 
            placeholder="试卷状态" 
            clearable
            @change="handleSearch"
          >
            <el-option label="未发布" :value="0" />
            <el-option label="已发布" :value="1" />
          </el-select>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            创建试卷
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 试卷列表 -->
    <el-table
      v-loading="loading"
      :data="paperList"
      border
      style="width: 100%"
    >
      <el-table-column prop="paperId" label="试卷ID" width="80" />
      <el-table-column prop="paperName" label="试卷名称" show-overflow-tooltip />
      <el-table-column prop="subject.subjectName" label="所属学科" width="150" />
      <el-table-column prop="examTypeName" label="考试类型" width="100">
        <template #default="scope">
          <el-tag :type="scope.row.examType === 0 ? 'primary' : 'warning'">
            {{ scope.row.examTypeName }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="totalScore" label="总分" width="80" />
      <el-table-column label="状态" width="100">
        <template #default="scope">
          <el-tag :type="scope.row.paperStatus === 0 ? 'info' : 'success'">
            {{ scope.row.statusName }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="250" fixed="right">
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
            :disabled="scope.row.paperStatus === 1"
            @click="handleEdit(scope.row)"
          >
            编辑
          </el-button>
          <el-button
            type="info"
            link
            v-if="scope.row.paperStatus === 1"
            @click="handleViewScore(scope.row)"
          >
            查看成绩
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

    <!-- 成绩分布对话框 -->
    <el-dialog
      v-model="scoreDialogVisible"
      title="成绩分布"
      width="800px"
    >
      <el-descriptions :column="2" border>
        <el-descriptions-item label="试卷名称">{{ currentPaper.paperName }}</el-descriptions-item>
        <el-descriptions-item label="所属学科">{{ currentPaper.subject?.subjectName }}</el-descriptions-item>
        <el-descriptions-item label="最高分">{{ scoreDistribution.highestScore }}</el-descriptions-item>
        <el-descriptions-item label="最低分">{{ scoreDistribution.lowestScore }}</el-descriptions-item>
        <el-descriptions-item label="平均分">
          <span :style="{ color: getAverageScoreColor(averageScore) }">
            {{ averageScore ? averageScore.toFixed(2) : '暂无' }}
          </span>
        </el-descriptions-item>
      </el-descriptions>

      <div class="score-chart" ref="chartRef"></div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { Search, Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import axios from 'axios'
import * as echarts from 'echarts'

const router = useRouter()

// 搜索表单
const searchForm = ref({
  keyword: '',
  subjectId: null,
  status: null
})

// 列表数据
const paperList = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 学科选项
const subjectOptions = ref([])

// 成绩分布相关
const scoreDialogVisible = ref(false)
const currentPaper = ref({})
const scoreDistribution = ref({
  distribution: [],
  highestScore: 0,
  lowestScore: 0
})
const chartRef = ref(null)
let chart = null

// 添加平均分数据
const averageScore = ref(null)

// 获取平均分颜色
const getAverageScoreColor = (score) => {
  if (!score) return '#909399'
  if (score >= 90) return '#67C23A'
  if (score >= 80) return '#409EFF'
  if (score >= 70) return '#E6A23C'
  if (score >= 60) return '#F56C6C'
  return '#909399'
}

// 获取试卷列表
const fetchPaperList = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: currentPage.value,
      pageSize: pageSize.value,
      keyword: searchForm.value.keyword || undefined,
      subjectId: searchForm.value.subjectId || undefined,
      status: searchForm.value.status
    }

    const response = await axios.get('/api/teacher/papers', { params })
    if (response.data.code === 200) {
      paperList.value = response.data.data.list
      total.value = response.data.data.total
    } else {
      ElMessage.error(response.data.message || '获取试卷列表失败')
    }
  } catch (error) {
    console.error('获取试卷列表失败:', error)
    ElMessage.error('获取试卷列表失败')
  } finally {
    loading.value = false
  }
}

// 获取学科列表
const fetchSubjects = async () => {
  try {
    const response = await axios.get('/api/teacher/subjects')
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

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  fetchPaperList()
}

// 分页
const handleSizeChange = (val) => {
  pageSize.value = val
  fetchPaperList()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchPaperList()
}

// 查看试卷
const handleView = (row) => {
  console.log('查看试卷:', row)
  try {
    router.push({
      name: 'TeacherPaperDetail',
      params: { id: row.paperId }
    })
  } catch (error) {
    console.error('路由跳转失败:', error)
    ElMessage.error('页面跳转失败')
  }
}

// 编辑试卷
const handleEdit = (row) => {
  router.push(`/teacher/papers/${row.paperId}/edit`)
}

// 创建试卷
const handleAdd = () => {
  router.push('/teacher/papers/add')
}

// 查看成绩分布
const handleViewScore = async (row) => {
  currentPaper.value = row
  try {
    // 并行请求成绩分布和平均分
    const [distributionRes, averageRes] = await Promise.all([
      axios.get(`/api/teacher/papers/${row.paperId}/score-distribution`),
      axios.get(`/api/teacher/papers/${row.paperId}/average-score`)
    ])

    if (distributionRes.data.code === 200) {
      scoreDistribution.value = distributionRes.data.data
      scoreDialogVisible.value = true
      
      // 检查是否有成绩数据
      if (!scoreDistribution.value.distribution || scoreDistribution.value.distribution.length === 0) {
        ElMessage.info('暂无考试成绩数据')
        return
      }
      
      // 设置平均分
      if (averageRes.data.code === 200) {
        averageScore.value = averageRes.data.data.averageScore
      }

      // 在下一个 tick 渲染图表
      setTimeout(() => {
        renderChart()
      }, 0)
    } else {
      ElMessage.error(distributionRes.data.message || '获取成绩分布失败')
    }
  } catch (error) {
    console.error('获取成绩数据失败:', error)
    ElMessage.error('获取成绩数据失败')
  }
}

// 渲染成绩分布图表
const renderChart = () => {
  if (!chartRef.value) return
  
  if (chart) {
    chart.dispose()
  }
  
  // 对分数段进行排序
  const sortedData = [...scoreDistribution.value.distribution].sort((a, b) => {
    const aStart = parseInt(a.score_range.split('-')[0])
    const bStart = parseInt(b.score_range.split('-')[0])
    return bStart - aStart // 降序排列
  })
  
  chart = echarts.init(chartRef.value)
  const option = {
    title: {
      text: '成绩分布',
      left: 'center'
    },
    tooltip: {
      trigger: 'axis',
      formatter: '{b}: {c}人'
    },
    xAxis: {
      type: 'category',
      data: sortedData.map(item => item.score_range + '分'),
      axisLabel: {
        interval: 0,
        rotate: 30
      }
    },
    yAxis: {
      type: 'value',
      name: '人数',
      minInterval: 1
    },
    series: [
      {
        data: sortedData.map(item => item.count),
        type: 'bar',
        barWidth: '40%',
        itemStyle: {
          color: function(params) {
            const range = parseInt(params.name.split('-')[0])
            if (range >= 90) return '#67C23A'  // 优秀
            if (range >= 80) return '#409EFF'  // 良好
            if (range >= 70) return '#E6A23C'  // 中等
            if (range >= 60) return '#F56C6C'  // 及格
            return '#909399'                    // 不及格
          }
        }
      }
    ]
  }
  chart.setOption(option)
}

// 监听对话框关闭
watch(scoreDialogVisible, (val) => {
  if (!val) {
    if (chart) {
      chart.dispose()
      chart = null
    }
    // 重置平均分
    averageScore.value = null
  }
})

// 添加错误处理拦截器
axios.interceptors.response.use(
  response => response,
  error => {
    console.error('请求错误:', error)
    ElMessage.error(error.response?.data?.message || '请求失败')
    return Promise.reject(error)
  }
)

onMounted(() => {
  fetchPaperList()
  fetchSubjects()
})
</script>

<style lang="scss" scoped>
.paper-list {
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

  .score-chart {
    height: 400px;
    margin-top: 20px;
  }
}
</style> 
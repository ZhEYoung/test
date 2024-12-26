<template>
  <div class="score-list">
    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-form :inline="true" :model="searchForm" class="form">
        <el-form-item label="考试">
          <el-select 
            v-model="searchForm.examId" 
            placeholder="选择考试"
            clearable
            @change="handleExamChange"
          >
            <el-option
              v-for="item in examOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="班级">
          <el-select 
            v-model="searchForm.classId" 
            placeholder="选择班级"
            clearable
            :disabled="!searchForm.examId || isRetakeExam"
            @change="handleSearch"
          >
            <el-option
              v-for="item in classOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>

        <el-form-item>
          <el-button 
            type="primary" 
            :disabled="!searchForm.examId || (!isRetakeExam && !searchForm.classId)"
            @click="handleExport"
          >
            <el-icon><Download /></el-icon>
            导出成绩
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 成绩统计 -->
    <el-card v-if="showStats" class="stats-card">
      <template #header>
        <div class="card-header">
          <span>考试成绩统计</span>
        </div>
      </template>
      
      <el-row :gutter="20">
        <el-col :span="6">
          <div class="stat-item">
            <div class="label">平均分</div>
            <div class="value" :style="{ color: getAverageScoreColor(stats.averageScore) }">
              {{ stats.averageScore }}
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-item">
            <div class="label">及格率</div>
            <div class="value" :style="{ color: getPassRateColor(stats.passRate) }">
              {{ stats.passRate }}%
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-item">
            <div class="label">及格人数</div>
            <div class="value">{{ stats.passedStudents }}/{{ stats.totalStudents }}</div>
          </div>
        </el-col>
      </el-row>

      <!-- 成绩分布图 -->
      <div class="score-chart" ref="chartRef"></div>
    </el-card>

    <!-- 成绩列表 -->
    <el-card v-if="showList" class="list-card">
      <template #header>
        <div class="card-header">
          <span>班级成绩列表</span>
        </div>
      </template>

      <el-table
        v-loading="loading"
        :data="scoreList"
        border
        style="width: 100%"
      >
        <el-table-column prop="student.studentId" label="学号" width="120" />
        <el-table-column prop="studentName" label="姓名" width="120" />
        <el-table-column prop="score" label="成绩" width="100">
          <template #default="scope">
            <template v-if="scope.row.editing">
              <el-input-number
                v-model="scope.row.editingScore"
                :min="0"
                :max="100"
                :precision="2"
                size="small"
                style="width: 100px"
              />
            </template>
            <template v-else>
              <span :style="{ color: getScoreColor(scope.row.score) }">
                {{ scope.row.score }}
              </span>
            </template>
          </template>
        </el-table-column>
        <el-table-column prop="rank" label="排名" width="100" />
        <el-table-column label="题目得分" min-width="300">
          <template #default="scope">
            <el-tooltip
              v-for="question in getSortedQuestions(scope.row.questions)"
              :key="question.questionId"
              :content="question.questionContent"
              placement="top"
            >
              <el-tag 
                :type="getQuestionScoreType(question.score, question.totalScore)"
                style="margin-right: 8px; margin-bottom: 8px"
              >
                {{ `Q${question.questionId}: ${question.score}` }}
              </el-tag>
            </el-tooltip>
          </template>
        </el-table-column>
        <el-table-column prop="teacherComment" label="教师评语" show-overflow-tooltip>
          <template #default="scope">
            <template v-if="scope.row.editingComment">
              <el-input
                v-model="scope.row.editingCommentText"
                type="textarea"
                :rows="2"
                placeholder="请输入评语"
              />
            </template>
            <template v-else>
              {{ scope.row.teacherComment || '-' }}
            </template>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <template v-if="scope.row.editing || scope.row.editingComment">
              <el-button
                type="primary"
                link
                @click="handleSave(scope.row)"
              >
                保存
              </el-button>
              <el-button
                link
                @click="handleCancel(scope.row)"
              >
                取消
              </el-button>
            </template>
            <template v-else>
              <el-button
                type="primary"
                link
                @click="handleEdit(scope.row)"
              >
                修改成绩
              </el-button>
              <el-button
                type="info"
                link
                @click="handleEditComment(scope.row)"
              >
                修改评语
              </el-button>
            </template>
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
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, watch, computed } from 'vue'
import { Download, DataLine } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import axios from 'axios'

// 搜索表单
const searchForm = ref({
  examId: null,
  classId: null
})

// 列表数据
const scoreList = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 选项数据
const examOptions = ref([])
const classOptions = ref([])

// 统计数据
const stats = ref({
  averageScore: 0,
  passRate: 0,
  passedStudents: 0,
  totalStudents: 0,
  scoreDistribution: []
})

// 控制显示
const showStats = ref(false)
const showList = ref(false)

// 图表相关
const chartRef = ref(null)
let chart = null

// 新增计算属性
const isRetakeExam = computed(() => {
  const selectedExam = examOptions.value.find(exam => exam.value === searchForm.value.examId)
  return selectedExam?.label.includes('重考考试')
})

// 获取考试列表
const fetchExams = async () => {
  try {
    const response = await axios.get('/api/teacher/exam/list', {
      params: {
        pageNum: 1,
        pageSize: 100
      }
    })
    if (response.data.code === 200) {
      // 只显示已结束的考试（状态为2-已结束）
      const examList = response.data.data.list
        .filter(exam => exam.examStatus === 2)
        .map(exam => {
          let typeName = ''
          switch (exam.examType) {
            case 0:
              typeName = '普通考试'
              break
            case 1:
              typeName = '重考考试'
              break
            default:
              typeName = '未知类型'
          }
          return {
            value: exam.examId,
            label: `${exam.examName} (${typeName}) - ${exam.subject.subjectName}`,
            uniqueKey: `${exam.examId}-${exam.examName}-${exam.examType}-${exam.subject.subjectId}`
          }
        })
      
      // 去重处理
      const uniqueExams = Array.from(new Map(examList.map(exam => [exam.uniqueKey, exam])).values())
      examOptions.value = uniqueExams
    } else {
      ElMessage.error(response.data.message || '获取考试列表失败')
    }
  } catch (error) {
    console.error('获取考试列表失败:', error)
    ElMessage.error('获取考试列表失败')
  }
}

// 获取班级列表
const fetchClasses = async (examId) => {
  try {
    const response = await axios.get(`/api/teacher/exam/${examId}/classes`)
    if (response.data.code === 200) {
      classOptions.value = response.data.data.list.map(classItem => ({
        value: classItem.classId,
        label: classItem.className
      }))
    } else {
      ElMessage.error(response.data.message || '获取班级列表失败')
    }
  } catch (error) {
    console.error('获取班级列表失败:', error)
    ElMessage.error('获取班级列表失败')
  }
}

// 获取成绩列表
const fetchScores = async () => {
  if (!searchForm.value.examId) return
  
  loading.value = true
  try {
    const selectedExam = examOptions.value.find(exam => exam.value === searchForm.value.examId)
    const isRetakeExam = selectedExam?.label.includes('重考考试')
    
    const params = {
      examId: searchForm.value.examId,
      pageNum: currentPage.value,
      pageSize: pageSize.value
    }

    // 如果是重考考试，使用 /all 接口，否则使用普通接口
    const url = isRetakeExam ? '/api/teacher/scores/all' : '/api/teacher/scores'
    if (!isRetakeExam) {
      params.classId = searchForm.value.classId
    }

    const response = await axios.get(url, { params })
    if (response.data.code === 200) {
      scoreList.value = response.data.data.list.map(item => ({
        ...item,
        editing: false,
        editingScore: item.score,
        editingComment: false,
        editingCommentText: item.teacherComment
      }))
      total.value = response.data.data.total
      showList.value = true
    } else {
      ElMessage.error(response.data.message || '获取成绩列表失败')
    }
  } catch (error) {
    console.error('获取成绩列表失败:', error)
    ElMessage.error('获取成绩列表失败')
  } finally {
    loading.value = false
  }
}

// 获取成绩统计
const fetchStats = async () => {
  if (!searchForm.value.examId || !searchForm.value.classId) return

  try {
    const response = await axios.get(`/api/teacher/scores/stats/${searchForm.value.examId}/${searchForm.value.classId}`)
    if (response.data.code === 200) {
      stats.value = response.data.data
      showStats.value = true
      renderChart()
    } else {
      ElMessage.error(response.data.message || '获取成绩统计失败')
    }
  } catch (error) {
    console.error('获取成绩统计失败:', error)
    ElMessage.error('获取成绩统计失败')
  }
}

// 渲染成绩分布图表
const renderChart = () => {
  if (!chartRef.value || !stats.value.scoreDistribution) return
  
  if (chart) {
    chart.dispose()
  }
  
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
      data: stats.value.scoreDistribution.map(item => item.score_range + '分'),
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
        data: stats.value.scoreDistribution.map(item => item.count),
        type: 'bar',
        barWidth: '40%',
        itemStyle: {
          color: function(params) {
            const range = parseInt(params.name)
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

// 考试变更
const handleExamChange = async (examId) => {
  searchForm.value.classId = null
  showStats.value = false
  showList.value = false
  if (examId) {
    const selectedExam = examOptions.value.find(exam => exam.value === examId)
    const isRetakeExam = selectedExam?.label.includes('重考考试')
    
    if (!isRetakeExam) {
      // 只有非重考考试才需要选择班级
      await fetchClasses(examId)
    } else {
      // 重考考试直接获取成绩
      await handleSearch()
    }
  } else {
    classOptions.value = []
  }
}

// 搜索
const handleSearch = async () => {
  currentPage.value = 1
  const selectedExam = examOptions.value.find(exam => exam.value === searchForm.value.examId)
  const isRetakeExam = selectedExam?.label.includes('重考考试')
  
  // 如果是重考考试，或者已经选择了班级，才进行搜索
  if (isRetakeExam || searchForm.value.classId) {
    await Promise.all([
      fetchScores(),
      fetchStats()
    ])
  }
}

// 分页
const handleSizeChange = (val) => {
  pageSize.value = val
  fetchScores()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchScores()
}

// 编辑成绩
const handleEdit = (row) => {
  row.editing = true
  row.editingScore = row.score
}

// 编辑评语
const handleEditComment = (row) => {
  row.editingComment = true
  row.editingCommentText = row.teacherComment
}

// 保存
const handleSave = async (row) => {
  try {
    if (row.editing) {
      // 保存成绩
      const response = await axios.put(`/api/teacher/scores/${row.scoreId}`, {
        score: row.editingScore
      })
      
      if (response.data.code === 200) {
        row.score = row.editingScore
        row.editing = false
        ElMessage.success('成绩更新成功')
        // 刷新统计数据
        await fetchStats()
      } else {
        ElMessage.error(response.data.message || '成绩更新失败')
      }
    }
    
    if (row.editingComment) {
      // 保存评语
      const response = await axios.put('/api/teacher/scores/comment', {
        examId: searchForm.value.examId,
        studentId: row.student.studentId,
        comment: row.editingCommentText
      })
      
      if (response.data.code === 200) {
        row.teacherComment = row.editingCommentText
        row.editingComment = false
        ElMessage.success('评语更新成功')
      } else {
        ElMessage.error(response.data.message || '评语更新失败')
      }
    }
  } catch (error) {
    console.error('保存失败:', error)
    ElMessage.error('保存失败')
  }
}

// 取消编辑
const handleCancel = (row) => {
  row.editing = false
  row.editingComment = false
}

// 导出成绩
const handleExport = async () => {
  try {
    const response = await axios.get('/api/teacher/scores/export', {
      params: {
        examId: searchForm.value.examId,
        classId: searchForm.value.classId
      },
      responseType: 'blob'
    })
    
    // 创建下载链接
    const url = window.URL.createObjectURL(new Blob([response.data]))
    const link = document.createElement('a')
    link.href = url
    link.setAttribute('download', '成绩表.xlsx')
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    
    ElMessage.success('成绩导出成功')
  } catch (error) {
    console.error('成绩导出失败:', error)
    ElMessage.error('成绩导出失败')
  }
}

// 获取成绩颜色
const getScoreColor = (score) => {
  if (score >= 90) return '#67C23A'
  if (score >= 80) return '#409EFF'
  if (score >= 70) return '#E6A23C'
  if (score >= 60) return '#F56C6C'
  return '#909399'
}

// 获取平均分颜色
const getAverageScoreColor = (score) => {
  return getScoreColor(score)
}

// 获取及格率颜色
const getPassRateColor = (rate) => {
  if (rate >= 90) return '#67C23A'
  if (rate >= 80) return '#409EFF'
  if (rate >= 70) return '#E6A23C'
  if (rate >= 60) return '#F56C6C'
  return '#909399'
}

// 获取题目得分类型
const getQuestionScoreType = (score, totalScore) => {
  const rate = (score / totalScore) * 100
  if (rate >= 90) return 'success'
  if (rate >= 70) return 'warning'
  if (rate >= 60) return ''
  return 'danger'
}

// 获取排序后的题目列表
const getSortedQuestions = (questions) => {
  if (!questions) return []
  return [...questions].sort((a, b) => a.questionId - b.questionId)
}

// 监听窗口大小变化
window.addEventListener('resize', () => {
  if (chart) {
    chart.resize()
  }
})

onMounted(() => {
  fetchExams()
})
</script>

<style lang="scss" scoped>
.score-list {
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
        width: 200px;
      }
    }
  }
  
  .stats-card {
    margin-bottom: 20px;
    
    .stat-item {
      text-align: center;
      padding: 20px;
      
      .label {
        font-size: 14px;
        color: #909399;
        margin-bottom: 8px;
      }
      
      .value {
        font-size: 24px;
        font-weight: bold;
      }
    }
    
    .score-chart {
      height: 400px;
      margin-top: 20px;
    }
  }
  
  .list-card {
    .pagination {
      margin-top: 20px;
      display: flex;
      justify-content: flex-end;
    }
  }
}
</style> 
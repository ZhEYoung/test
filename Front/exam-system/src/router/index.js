import { createRouter, createWebHistory } from 'vue-router'

// 路由配置
const routes = [
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue')
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/Register.vue')
  },
  {
    path: '/admin',
    name: 'AdminLayout',
    component: () => import('../layouts/AdminLayout.vue'),
    redirect: '/admin/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'AdminDashboard',
        component: () => import('../views/admin/Dashboard.vue'),
        meta: { title: '控制台' }
      },
      {
        path: 'admins',
        name: 'AdminList',
        component: () => import('../views/admin/AdminList.vue'),
        meta: { title: '管理员列表' }
      },
      {
        path: 'teachers',
        name: 'TeacherList',
        component: () => import('../views/admin/TeacherList.vue'),
        meta: { title: '教师列表' }
      },
      {
        path: 'students',
        name: 'StudentList',
        component: () => import('../views/admin/StudentList.vue'),
        meta: { title: '学生列表' }
      },
      {
        path: 'colleges',
        component: () => import('@/views/admin/CollegeList.vue'),
        meta: { 
          title: '学院管理',
          requiresAuth: true,
          roles: ['admin']
        }
      },
      {
        path: 'subjects',
        component: () => import('@/views/admin/SubjectList.vue'),
        meta: { 
          title: '学科管理',
          requiresAuth: true,
          roles: ['admin']
        }
      },
      {
        path: 'classes',
        component: () => import('@/views/admin/ClassList.vue'),
        meta: { 
          title: '班级管理',
          requiresAuth: true,
          roles: ['admin']
        }
      },
      {
        path: 'logs',
        component: () => import('@/views/admin/SystemLogList.vue'),
        meta: { 
          title: '系统日志',
          requiresAuth: true,
          roles: ['admin']
        }
      }
    ]
  },
  {
    path: '/teacher',
    name: 'TeacherLayout',
    component: () => import('../layouts/TeacherLayout.vue'),
    children: [
      {
        path: 'dashboard',
        name: 'TeacherDashboard',
        component: () => import('../views/teacher/Dashboard.vue'),
        meta: { title: '控制台' }
      },
      {
        path: 'exams',
        name: 'TeacherExamList',
        component: () => import('../views/teacher/ExamList.vue'),
        meta: { 
          title: '考试管理',
          requiresAuth: true,
          roles: ['teacher']
        }
      },
      {
        path: 'exams/publish-normal',
        name: 'TeacherExamPublishNormal',
        component: () => import('../views/teacher/ExamPublishNormal.vue'),
        meta: { 
          title: '发布普通考试',
          requiresAuth: true,
          roles: ['teacher']
        }
      },
      {
        path: 'exams/publish-final',
        name: 'TeacherExamPublishFinal',
        component: () => import('../views/teacher/ExamPublishFinal.vue'),
        meta: { 
          title: '发布期末考试',
          requiresAuth: true,
          roles: ['teacher']
        }
      },
      {
        path: 'exams/publish-retake',
        name: 'TeacherExamPublishRetake',
        component: () => import('../views/teacher/ExamPublishRetake.vue'),
        meta: { 
          title: '发布重考考试',
          requiresAuth: true,
          roles: ['teacher']
        }
      },
      {
        path: 'papers',
        name: 'TeacherPaperList',
        component: () => import('../views/teacher/PaperList.vue'),
        meta: { 
          title: '试卷管理',
          requiresAuth: true,
          roles: ['teacher']
        }
      },
      {
        path: 'papers/add',
        name: 'TeacherPaperAdd',
        component: () => import('../views/teacher/PaperForm.vue'),
        meta: { 
          title: '创建试卷',
          requiresAuth: true,
          roles: ['teacher']
        }
      },
      {
        path: 'papers/:id',
        name: 'TeacherPaperDetail',
        component: () => import('../views/teacher/PaperDetail.vue'),
        meta: { 
          title: '试卷详情',
          requiresAuth: true,
          roles: ['teacher']
        }
      },
      {
        path: 'papers/:id/edit',
        name: 'TeacherPaperEdit',
        component: () => import('../views/teacher/PaperForm.vue'),
        meta: { 
          title: '编辑试卷',
          requiresAuth: true,
          roles: ['teacher']
        }
      },
      {
        path: 'classes',
        name: 'TeacherClassList',
        component: () => import('../views/teacher/ClassList.vue'),
        meta: { 
          title: '班级管理',
          requiresAuth: true,
          roles: ['teacher']
        }
      },
      {
        path: 'questions',
        name: 'TeacherQuestionList',
        component: () => import('../views/teacher/QuestionList.vue'),
        meta: { 
          title: '题库管理',
          requiresAuth: true,
          roles: ['teacher']
        }
      },
      {
        path: 'questions/add',
        name: 'TeacherQuestionAdd',
        component: () => import('../views/teacher/QuestionForm.vue'),
        meta: { 
          title: '添加题目',
          requiresAuth: true,
          roles: ['teacher']
        }
      },
      {
        path: 'questions/:id',
        name: 'TeacherQuestionEdit',
        component: () => import('../views/teacher/QuestionForm.vue'),
        meta: { 
          title: '题目详情',
          requiresAuth: true,
          roles: ['teacher']
        }
      },
      {
        path: 'exams/:id',
        name: 'TeacherExamDetail',
        component: () => import('../views/teacher/ExamDetail.vue'),
        meta: { 
          title: '考试详情',
          requiresAuth: true,
          roles: ['teacher']
        }
      },
      {
        path: 'exams/:id/edit',
        name: 'TeacherExamEdit',
        component: () => import('../views/teacher/ExamEdit.vue'),
        meta: { 
          title: '编辑考试',
          requiresAuth: true,
          roles: ['teacher']
        }
      },
      {
        path: 'scores',
        name: 'TeacherScoreList',
        component: () => import('../views/teacher/ScoreList.vue'),
        meta: { 
          title: '成绩管理',
          requiresAuth: true,
          roles: ['teacher']
        }
      },
      {
        path: 'scores/:examId/:classId',
        name: 'TeacherScoreDetail',
        component: () => import('../views/teacher/ScoreDetail.vue'),
        meta: { 
          title: '成绩详情',
          requiresAuth: true,
          roles: ['teacher']
        }
      },
      {
        path: 'grading',
        name: 'TeacherGradingList',
        component: () => import('../views/teacher/GradingList.vue'),
        meta: { 
          title: '试题批改',
          requiresAuth: true,
          roles: ['teacher']
        }
      },
      {
        path: 'grading/:examId',
        name: 'TeacherGradingDetail',
        component: () => import('../views/teacher/GradingDetail.vue'),
        meta: { 
          title: '批改详情',
          requiresAuth: true,
          roles: ['teacher']
        }
      }
    ]
  },
  {
    path: '/student',
    name: 'StudentLayout',
    component: () => import('../layouts/StudentLayout.vue'),
    children: [
      {
        path: 'dashboard',
        name: 'StudentDashboard',
        component: () => import('../views/student/Dashboard.vue'),
        meta: { title: '控制台' }
      },
      {
        path: 'classes',
        name: 'StudentClassList',
        component: () => import('../views/student/ClassList.vue'),
        meta: { 
          title: '班级管理',
          requiresAuth: true,
          roles: ['student']
        }
      },
      {
        path: 'exams',
        name: 'StudentExamList',
        component: () => import('../views/student/ExamList.vue'),
        meta: { 
          title: '我的考试',
          requiresAuth: true,
          roles: ['student']
        }
      },
      {
        path: 'exam-paper/:examId',
        name: 'StudentExamPaper',
        component: () => import('../views/student/ExamPaper.vue'),
        meta: { 
          title: '考试答题',
          requiresAuth: true,
          roles: ['student']
        }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  // 允许访问登录和注册页面
  if (to.path === '/login' || to.path === '/register') {
    if (token) {
      next('/')
    } else {
      next()
    }
  } else {
    if (!token) {
      next('/login')
    } else {
      next()
    }
  }
})

export default router 
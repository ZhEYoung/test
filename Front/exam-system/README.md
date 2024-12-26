# 在线考试系统前端

## 项目介绍
这是一个基于Vue 3 + Vite + Element Plus开发的在线考试系统前端项目。

## 技术栈
- Vue 3 - 渐进式JavaScript框架
- Vite - 下一代前端构建工具
- Element Plus - 基于Vue 3的组件库
- Vue Router - 路由管理
- Pinia - 状态管理
- Axios - HTTP客户端
- Sass - CSS预处理器

## 项目结构
```
src/
├── api/              # API接口
├── assets/           # 静态资源
├── components/       # 公共组件
├── composables/      # 组合式函数
├── layouts/          # 布局组件
├── router/           # 路由配置
├── stores/           # 状态管理
├── styles/           # 样式文件
├── utils/            # 工具函数
└── views/            # 页面视图
    ├── admin/        # 管理员页面
    ├── teacher/      # 教师页面
    └── student/      # 学生页面
```

## 功能模块
1. 用户认证
   - 登录
   - 注册
   - 找回密码

2. 管理员模块
   - 学生管理
   - 教师管理
   - 班级管理
   - 学院管理
   - 科目管理
   - 系统日志

3. 教师模块
   - 班级管理
   - 考试管理
   - 试卷管理
   - 题目管理
   - 成绩管理
   - 评分管理

4. 学生模块
   - 考试列表
   - 在线考试
   - 成绩查询
   - 个人信息

## 开发指南
1. 安装依赖
```bash
npm install
```

2. 启动开发服务器
```bash
npm run dev
```

3. 构建生产版本
```bash
npm run build
```

## 代码规范
- 使用ESLint进行代码检查
- 使用Prettier进行代码格式化
- 遵循Vue 3官方风格指南
- 使用TypeScript进行类型检查

## 浏览器支持
- Chrome
- Firefox
- Safari
- Edge

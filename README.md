# 考试管理系统

基于SSM (Spring + SpringMVC + MyBatis) 框架开发的在线考试管理系统。

## 技术栈

- 后端框架：Spring Boot 2.7.5
- 持久层框架：MyBatis 2.2.2
- 数据库：MySQL 8.0.31
- 数据库连接池：Druid 1.2.15
- 工具库：
  - Lombok：简化实体类开发
  - Hutool：提供常用工具类
  - JWT：用户认证授权

## 项目结构

```
ExamSystem/
├── src/                           # 源代码目录
│   ├── main/
│   │   ├── java/
│   │   │   └── com/exam/
│   │   │       ├── common/       # 公共组件
│   │   │       │   ├── config/   # 全局配置
│   │   │       │   ├── constant/ # 常量定义
│   │   │       │   └── exception/# 异常处理
│   │   │       ├── config/       # 配置类
│   │   │       │   ├── security/ # 安全配置
│   │   │       │   └── web/      # Web配置
│   │   │       ├── controller/   # 控制器层
│   │   │       │   ├── admin/    # 管理员接口
│   │   │       │   ├── student/  # 学生接口
│   │   │       │   └── teacher/  # 教师接口
│   │   │       ├── entity/       # 实体类
│   │   │       │   ├── dto/      # 数据传输对象
│   │   │       │   └── vo/       # 视图对象
│   │   │       ├── mapper/       # MyBatis映射层
│   │   │       ├── service/      # 服务层
│   │   │       │   └── impl/     # 服务实现
│   │   │       └── utils/        # 工具类
│   │   └── resources/            # 资源文件目录
│   │       ├── mapper/           # MyBatis映射文件
│   │       ├── static/           # 静态资源
│   │       └── application.yml   # 应用配置文件
│   └── test/                     # 测试代码目录
├── .mvn/                         # Maven包装器目录
├── target/                       # 编译输出目录
└── pom.xml                       # Maven项目配置文件
```

## 功能模块

1. 用户管理模块
   - 用户注册：支持学生、教师注册
   - 用户登录：JWT token认证
   - 用户信息管理：个人信息维护

2. 考试管理模块
   - 试卷管理：创建、编辑、删除试卷
   - 题目管理：题库维护、题目分类
   - 考试安排：考试时间设置、考场分配

3. 考试功能模块
   - 在线考试：实时答题、自动提交
   - 自动判卷：客观题自动评分
   - 成绩管理：成绩统计、成绩分析

4. 系统管理模块
   - 权限管理：角色分配、权限控制
   - 系统配置：基础参数设置
   - 日志管理：操作日志、登录日志

## 代码索引

### 控制器层 (Controller)
- `UserController`: 用户相关接口
- `ExamController`: 考试相关接口
- `QuestionController`: 题目相关接口
- `ScoreController`: 成绩相关接口
- `AdminController`: 管理员相关接口

### 服务层 (Service)
- `UserService`: 用户服务
- `ExamService`: 考试服务
- `QuestionService`: 题目服务
- `ScoreService`: 成绩服务
- `AdminService`: 管理服务

### 数据访问层 (Mapper)
- `UserMapper`: 用户数据访问
- `ExamMapper`: 考试数据访问
- `QuestionMapper`: 题目数据访问
- `ScoreMapper`: 成绩数据访问

### 实体类 (Entity)
- `User`: 用户实体
- `Exam`: 考试实体
- `Question`: 题目实体
- `Score`: 成绩实体

### 工具类 (Utils)
- `JwtUtil`: JWT工具类
- `SecurityUtil`: 安全工具类
- `DateUtil`: 日期工具类
- `FileUtil`: 文件工具类

## 开发环境

- JDK 1.8
- Maven 3.x
- MySQL 8.x
- IDE: IntelliJ IDEA / Eclipse

## 部署说明

1. 克隆项目到本地
```bash
git clone [项目地址]
```

2. 导入数据库脚本
```bash
mysql -u root -p < exam_system.sql
```

3. 修改配置文件
编辑 `src/main/resources/application.yml`，配置数据库连接信息：
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/exam_system
    username: root
    password: 123456
```

4. 启动项目
```bash
mvn spring-boot:run
```

## 注意事项

1. 确保MySQL服务已启动
2. 确保数据库账号密码配置正确
3. 确保Maven依赖已正确下载
4. 首次运行需要执行数据库初始化脚本
5. 默认管理员账号：admin/admin123

## 更新日志

### v1.0.0 (2023-12-09)
- 初始版本发布
- 实现基础功能模块
- 完成用户认证授权
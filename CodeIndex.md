# 考试系统代码索引

## 项目结构
```
ExamSystem
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/exam/
│   │   │       ├── controller/    # 控制器层：处理HTTP请求
│   │   │       ├── service/       # 服务层：业务逻辑实现
│   │   │       ├── mapper/        # 数据访问层：与数据库交互
│   │   │       ├── entity/        # 实体类：数据模型
│   │   │       ├── common/        # 公共组件：通用工具和配置
│   │   │       ├── config/        # 配置类：系统配置
│   │   │       ├── utils/         # 工具类：辅助功能
│   │   │       └── ExamSystemApplication.java  # 应用程序入口
│   │   ├── resources/    # 配置文件和静态资源
│   │   └── webapp/       # Web资源
│   └── test/            # 测试代码
├── pom.xml             # Maven项目配置文件
└── README.md           # 项目说明文档
```

## 核心组件说明

### 1. 控制器层 (Controller)
位置：`src/main/java/com/exam/controller/`
- 处理前端HTTP请求
- 实现RESTful API接口
- 数据校验和请求转发

### 2. 服务层 (Service)
位置：`src/main/java/com/exam/service/`
- 实现核心业务逻辑
- 处理事务管理
- 调用数据访问层

### 3. 数据访问层 (Mapper)
位置：`src/main/java/com/exam/mapper/`
- 定义数据库操作接口
- 实现与数据库的交互
- MyBatis映射配置

### 4. 实体层 (Entity)
位置：`src/main/java/com/exam/entity/`
- 定义数据模型
- 实现数据传输对象(DTO)
- 数据库表映射类

### 5. 公共组件 (Common)
位置：`src/main/java/com/exam/common/`
- 通用工具类
- 常量定义
- 异常处理

### 6. 配置类 (Config)
位置：`src/main/java/com/exam/config/`
- 系统配置类
- 安全配置
- 数据源配置

### 7. 工具类 (Utils)
位置：`src/main/java/com/exam/utils/`
- 辅助功能实现
- 通用工具方法
- 帮助类

## 主要功能模块

1. 用户管理
   - 用户注册
   - 用户登录
   - 权限控制

2. 考试管理
   - 试题管理
   - 考试安排
   - 成绩管理

3. 系统管理
   - 系统配置
   - 日志管理
   - 权限管理

## 技术栈

- 后端框架：Spring Boot
- 数据库：MySQL
- ORM框架：MyBatis
- 项目管理：Maven

## 开发规范

1. 命名规范
   - 类名：驼峰命名，首字母大写
   - 方法名：驼峰命名，首字母小写
   - 变量名：驼峰命名，首字母小写
   - 数据库表名：下划线命名，单数形式

2. 代码规范
   - 遵循阿里巴巴Java开发手册
   - 使用统一的代码格式化工具
   - 必要的注释和文档

3. 提交规范
   - 清晰的提交信息
   - 遵循Git Flow工作流
   - 代码审查流程

## 部署说明

1. 环境要求
   - JDK 8+
   - Maven 3.6+
   - MySQL 5.7+

2. 配置文件
   - application.properties/yml：主配置文件
   - logback-spring.xml：日志配置

3. 部署步骤
   - Maven打包
   - 环境配置
   - 启动应用 
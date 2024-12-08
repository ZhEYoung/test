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
src/main/java/com/exam/
├── common/         # 公共组件
├── config/         # 配置类
├── controller/     # 控制器层
├── entity/        # 实体类
├── mapper/        # MyBatis映射层
├── service/       # 服务层
└── utils/         # 工具类
```

## 功能模块

1. 用户管理模块
   - 用户注册
   - 用户登录
   - 用户信息管理

2. 考试管理模块
   - 试卷管理
   - 题目管理
   - 考试安排

3. 考试功能模块
   - 在线考试
   - 自动判卷
   - 成绩管理

4. 系统管理模块
   - 权限管理
   - 系统配置
   - 日志管理

## 数据库设计

详细的数据库设计请参考 [Database.md](Database.md)

## 开发环境

- JDK 1.8
- Maven 3.x
- MySQL 8.x

## 部署说明

1. 克隆项目到本地
2. 导入数据库脚本
3. 修改 `application.yml` 中的数据库配置
4. 运行 `ExamSystemApplication.java`

## 接口文档

待完善...

## 注意事项

1. 确保MySQL服务已启动
2. 确保数据库账号密码配置正确
3. 确保Maven依赖已正确下载 
# 在线考试系统API文档

## 1. 认证相关接口 (AuthController)
### 1.1 用户登录
- 请求路径：`/auth/login`
- 请求方式：POST
- 请求参数：
  ```json
  {
    "username": "string",
    "password": "string"
  }
  ```
- 返回数据：
  ```json
  {
    "code": 200,
    "message": "登录成功",
    "data": {
      "token": "string",
      "userInfo": {
        "id": "number",
        "username": "string",
        "role": "string"
      }
    }
  }
  ```

## 2. 管理员相关接口
### 2.1 学生管理 (AdminStudentManageController)
- 增加学生
- 删除学生
- 修改学生信息
- 查询学生列表

### 2.2 教师管理 (AdminTeacherManageController)
- 增加教师
- 删除教师
- 修改教师信息
- 查询教师列表

### 2.3 班级管理 (AdminClassManageController)
- 创建班级
- 删除班级
- 修改班级信息
- 查询班级列表

### 2.4 学院管理 (AdminCollegeController)
- 创建学院
- 删除学院
- 修改学院信息
- 查询学院列表

### 2.5 科目管理 (AdminSubjectController)
- 创建科目
- 删除科目
- 修改科目信息
- 查询科目列表

## 3. 教师相关接口
### 3.1 班级管理 (TeacherClassManageController)
- 查看班级列表
- 管理班级学生

### 3.2 考试管理 (TeacherExamManageController)
- 创建考试
- 修改考试信息
- 删除考试
- 查询考试列表

### 3.3 试卷管理 (TeacherExamPaperManageController)
- 创建试卷
- 修改试卷
- 删除试卷
- 查询试卷列表

### 3.4 题目管理 (TeacherQuestionManageController)
- 添加题目
- 修改题目
- 删除题目
- 查询题目列表

### 3.5 成绩管理 (TeacherScoreManageController)
- 查看成绩列表
- 导出成绩
- 成绩统计

### 3.6 评分管理 (TeacherGradingController)
- 评分
- 查看待评分列表

## 4. 学生相关接口
### 4.1 考试相关 (StudentExamController)
- 查看可参加的考试
- 开始考试
- 提交答案
- 查看考试历史

### 4.2 成绩查询 (StudentScoreController)
- 查看个人成绩
- 查看成绩详情

### 4.3 班级相关 (StudentClassController)
- 查看所在班级
- 查看班级信息

### 4.4 个人信息 (StudentUserController)
- 查看个人信息
- 修改个人信息

## 5. 通用接口
### 5.1 文件上传
- 上传图片
- 上传文档

### 5.2 系统日志 (AdminSystemLogController)
- 查看系统日志
- 导出日志

注意：
1. 所有接口都需要在请求头中携带token进行身份验证
2. 接口返回数据格式统一为：
```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```
3. 错误码说明：
   - 200: 成功
   - 401: 未授权
   - 403: 禁止访问
   - 404: 资源不存在
   - 500: 服务器错误 
package com.exam.common;

/**
 * 常量类
 */
public interface Constants {
    // 用户角色
    String ROLE_ADMIN = "0";    // 管理员
    String ROLE_TEACHER = "1";  // 教师
    String ROLE_STUDENT = "2";  // 学生

    // 考试状态
    String EXAM_NOT_STARTED = "0";  // 未开始
    String EXAM_IN_PROGRESS = "1";  // 进行中
    String EXAM_FINISHED = "2";     // 已结束

    // 试卷状态
    Integer PAPER_NOT_PUBLISHED = 0;  // 未发布
    Integer PAPER_PUBLISHED = 1;      // 已发布

    // 题目类型
    String QUESTION_SINGLE_CHOICE = "0";  // 单选
    String QUESTION_MULTIPLE_CHOICE = "1"; // 多选
    String QUESTION_JUDGE = "2";          // 判断
    String QUESTION_FILL = "3";           // 填空
    String QUESTION_ESSAY = "4";          // 简答

    // 考试类型
    String EXAM_TYPE_FINAL = "0";     // 期末考试
    String EXAM_TYPE_NORMAL = "1";    // 普通考试

    // 操作类型
    String ACTION_INSERT = "0";       // 插入
    String ACTION_UPDATE = "1";       // 更新
    String ACTION_DELETE = "2";       // 删除
    String ACTION_LOGIN = "3";        // 登录
    String ACTION_SUBMIT_TEST = "4";  // 提交考试
} 
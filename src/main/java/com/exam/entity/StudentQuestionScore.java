package com.exam.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

/**
 * 学生题目成绩实体类
 */
@Data
public class StudentQuestionScore {
    private Integer recordId;    // 记录ID
    private Integer examId;      // 考试ID
    private Integer studentId;   // 学生ID
    private Integer questionId;  // 题目ID
    private Integer scoreId;     // 成绩ID
    private String answer;       // 学生答案
    private BigDecimal score;    // 得分
    private Integer status;      // 批改状态 0: 未批改；1: 已批改
    
    // 扩展字段，不映射到数据库
    private transient String examName;        // 考试名称
    private transient String studentName;     // 学生姓名
    private transient String questionContent; // 题目内容
    private transient String questionType;    // 题目类型
    private transient String correctAnswer;   // 正确答案
    private transient String statusName;      // 状态名称
    private transient BigDecimal fullScore;   // 满分
    private transient Question question;       // 题目
} 
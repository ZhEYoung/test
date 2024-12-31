package com.exam.entity;

import lombok.Data;
import java.util.Date;
import java.util.List;
import java.math.BigDecimal;

/**
 * 学生成绩实体类
 */
@Data
public class StudentScore {
    private Integer scoreId;     // 成绩ID
    private Integer studentId;   // 学生ID
    private Integer examId;      // 考试ID
    private BigDecimal score;    // 成绩
    private Date uploadTime;     // 成绩上传时间
    
    // 关联信息
    private Student student;     // 学生信息
    private Exam exam;          // 考试信息
    
    // 扩展字段，不映射到数据库
    private transient List<Question> questions;     // 题目列表（包含学生答案和得分）
    private transient String examName;              // 考试名称
    private transient String studentName;           // 学生姓名
    private transient BigDecimal scoreRate;         // 得分率
    private transient Integer rank;                 // 排名
    private transient String teacherComment;        // 教师评语
} 
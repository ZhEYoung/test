package com.exam.entity;

import lombok.Data;
import java.util.Date;
import java.util.List;
import java.math.BigDecimal;

/**
 * 试卷实体类
 */
@Data
public class ExamPaper {
    private Integer paperId;        // 试卷ID
    private String paperName;       // 试卷名称
    private Integer paperStatus;    // 试卷状态 0: 未发布，1: 已发布
    private Integer subjectId;      // 学科编号
    private Integer teacherId;      // 创建教师ID
    private Date createdTime;       // 创建时间
    private Integer examType;       // 考试类型 0: 期末，1: 普通考试
    private Date academicTerm;      // 学年学期
    private BigDecimal paperDifficulty;// 试卷难度
    
    // 关联信息
    private Subject subject;        // 学科信息
    private Teacher teacher;        // 教师信息
    
    // 扩展字段，不映射到数据库
    private transient List<Question> questions;  // 试卷题目列表（包含分值等信息）
    private transient BigDecimal totalScore;    // 试卷总分
    private transient String statusName;         // 状态名称
    private transient String examTypeName;       // 考试类型名称
} 
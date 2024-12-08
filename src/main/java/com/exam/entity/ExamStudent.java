package com.exam.entity;

import lombok.Data;
import java.util.Date;

/**
 * 考试-学生关联实体类
 */
@Data
public class ExamStudent {
    private Integer esId;                // 关联ID
    private Integer examId;              // 考试ID
    private Integer studentId;           // 学生ID
    private Date studentStartTime;       // 参加时间
    private Date studentSubmitTime;      // 提交时间
    private Boolean absent;              // 缺考标记
    private Boolean retakeNeeded;        // 是否需要重考
    private Boolean disciplinary;        // 违纪标记
    private String teacherComment;       // 教师评语
    
    // 关联信息
    private Exam exam;                   // 考试信息
    private Student student;             // 学生信息
} 
package com.exam.entity;

import lombok.Data;

/**
 * 班级实体类
 */
@Data
public class Class {
    private Integer classId;      // 班级ID
    private Integer teacherId;    // 教师ID
    private String className;     // 课程名
    private Integer subjectId;    // 学科编号
    private Boolean finalExam;    // 是否有期末考试
    
    // 关联信息
    private Teacher teacher;      // 教师信息
    private Subject subject;      // 学科信息
} 
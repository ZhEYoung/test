package com.exam.entity;

import lombok.Data;

/**
 * 教师实体类
 */
@Data
public class Teacher {
    private Integer teacherId;  // 教师ID
    private Integer userId;     // 用户ID
    private String name;        // 姓名
    private Integer permission; // 权限 0: 可以组卷与发布所有考试；1: 可以组卷与发布普通考试；2: 可以组卷
    private Integer collegeId;  // 学院ID
    private String other;       // 备注
    
    // 关联信息
    private User user;          // 用户信息
    private College college;    // 学院信息
} 
package com.exam.entity;

import lombok.Data;

/**
 * 学生实体类
 */
@Data
public class Student {
    private Integer studentId;  // 学生ID
    private Integer userId;     // 用户ID
    private String name;        // 姓名
    private String grade;       // 年级
    private Integer collegeId;  // 学院ID
    private String other;       // 备注
    
    // 关联信息
    private User user;          // 用户信息
    private College college;    // 学院信息

} 
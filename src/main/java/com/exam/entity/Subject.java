package com.exam.entity;

import lombok.Data;

/**
 * 学科实体类
 */
@Data
public class Subject {
    private Integer subjectId;     // 学科ID
    private String subjectName;    // 学科名称
    private String description;    // 学科描述
    private Integer collegeId;     // 学院ID
    
    // 关联信息
    private College college;       // 学院信息
} 
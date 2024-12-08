package com.exam.entity;

import lombok.Data;
import java.util.Date;

/**
 * 学院实体类
 */
@Data
public class College {
    private Integer collegeId;      // 学院ID
    private String collegeName;     // 学院名称
    private String description;     // 学院描述
    private Date createdTime;       // 创建时间
} 
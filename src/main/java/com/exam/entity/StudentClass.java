package com.exam.entity;

import lombok.Data;
import java.util.Date;

/**
 * 学生-班级关联实体类
 */
@Data
public class StudentClass {
    private Integer scId;        // 关联ID
    private Integer studentId;   // 学生ID
    private Integer classId;     // 班级ID
    private Boolean status;      // 状态
    private Date joinTime;       // 加入时间
    private Date leftTime;       // 退出时间
    
    // 关联信息
    private Student student;     // 学生信息
    private Class clazz;        // 班级信息
} 
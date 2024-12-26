package com.exam.entity;

import lombok.Data;

/**
 * 管理员实体类
 */
@Data
public class Admin {
    private Integer adminId;    // 管理员ID
    private Integer userId;     // 用户ID
    private String name;        // 姓名
    private String other;       // 备注
    
    // 关联信息
    private User user;          // 用户信息
} 
package com.exam.entity;

import lombok.Data;
import java.util.Date;

/**
 * 用户实体类
 */
@Data
public class User {
    private Integer userId;     // 用户ID
    private String username;    // 用户名
    private String password;    // 密码
    private Integer role;       // 角色 0: 管理员；1: 教师；2: 学生
    private Boolean status;     // 账号状态
    private Boolean sex;        // 性别 0: 女；1: 男
    private String phone;       // 联系方式
    private String email;       // 邮箱
    private Date createdTime;   // 注册时间
} 
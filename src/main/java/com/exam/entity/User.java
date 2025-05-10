package com.exam.entity;

import lombok.Data;
import java.util.Date;

/**
 * 用户实体类
 */
@Data
public class User {
    public static final int USERNAME_MAX_LENGTH = 10;  // 用户名最大长度
    
    // 角色常量
    public static final int ROLE_ADMIN = 0;    // 管理员
    public static final int ROLE_TEACHER = 1;  // 教师
    public static final int ROLE_STUDENT = 2;  // 学生

    private Integer userId;     // 用户ID
    private String username;    // 用户名
    private String password;    // 密码
    private Integer role;       // 角色 0: 管理员；1: 教师；2: 学生
    private Boolean status;     // 账号状态
    private Boolean sex;        // 性别 0: 女；1: 男
    private String phone;       // 联系方式
    private String email;       // 邮箱
    private Date createdTime;   // 注册时间

    /**
     * 验证用户名长度是否合法
     * @return true 如果用户名长度合法，false 如果用户名长度超出限制
     */
    public boolean isUsernameValid() {
        return username != null && username.length() <= USERNAME_MAX_LENGTH;
    }

    /**
     * 验证角色是否合法
     * @return true 如果角色合法，false 如果角色无效
     */
    public boolean isRoleValid() {
        return role != null && (role == ROLE_ADMIN || role == ROLE_TEACHER || role == ROLE_STUDENT);
    }
} 
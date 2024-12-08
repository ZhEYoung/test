package com.exam.service;

import com.exam.entity.User;
import java.util.List;
import java.util.Date;

/**
 * 用户服务接口
 */
public interface UserService extends BaseService<User> {
    
    /**
     * 用户登录
     * @param username 用户名
     * @param password 密码
     * @return 用户信息
     */
    User login(String username, String password);
    
    /**
     * 用户注册
     * @param user 用户信息
     * @return 注册结果
     */
    int register(User user);
    
    /**
     * 修改密码
     * @param userId 用户ID
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @return 修改结果
     */
    int updatePassword(Integer userId, String oldPassword, String newPassword);
    
    /**
     * 更新用户状态
     * @param userId 用户ID
     * @param status 状态
     * @return 更新结果
     */
    int updateStatus(Integer userId, Boolean status);
    
    /**
     * 批量更新用户状态
     */
    int batchUpdateStatus(List<Integer> userIds, Boolean status);
    
    /**
     * 根据用户名查询用户
     */
    User getByUsername(String username);
    
    /**
     * 根据角色查询用户列表
     * @param role 角色（0: 管理员；1: 教师；2: 学生）
     */
    List<User> getByRole(Integer role);
    
    /**
     * 更新用户联系方式
     */
    int updateContact(Integer userId, String phone, String email);
    
    /**
     * 根据邮箱查询用户
     */
    User getByEmail(String email);
    
    /**
     * 根据手机号查询用户
     */
    User getByPhone(String phone);
    
    /**
     * 更新用户注册时间
     */
    int updateCreatedTime(Integer userId, Date createdTime);
} 
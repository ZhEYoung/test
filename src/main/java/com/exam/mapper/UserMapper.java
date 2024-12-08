package com.exam.mapper;

import com.exam.entity.User;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Date;

/**
 * 用户Mapper接口
 */
public interface UserMapper extends BaseMapper<User> {
    /**
     * 根据用户名查询用户
     */
    User selectByUsername(@Param("username") String username);

    /**
     * 根据角色查询用户列表
     * @param role 角色（0: 管理员；1: 教师；2: 学生）
     */
    List<User> selectByRole(@Param("role") Integer role);

    /**
     * 更新用户状态
     */
    int updateStatus(@Param("userId") Integer userId, @Param("status") Boolean status);

    /**
     * 批量更新用户状态
     */
    int batchUpdateStatus(@Param("userIds") List<Integer> userIds, @Param("status") Boolean status);

    /**
     * 修改密码
     */
    int updatePassword(@Param("userId") Integer userId, @Param("newPassword") String newPassword);

    /**
     * 更新用户联系方式
     */
    int updateContact(@Param("userId") Integer userId, 
                     @Param("phone") String phone,
                     @Param("email") String email);

    /**
     * 更新用户性别
     */
    int updateSex(@Param("userId") Integer userId, @Param("sex") Boolean sex);

    /**
     * 根据邮箱查询用户
     */
    User selectByEmail(@Param("email") String email);

    /**
     * 根据手机号查询用户
     */
    User selectByPhone(@Param("phone") String phone);

    /**
     * 更新用户注册时间
     */
    int updateCreatedTime(@Param("userId") Integer userId, @Param("createdTime") Date createdTime);
} 
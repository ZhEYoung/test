package com.exam.mapper;

import com.exam.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;
import java.util.Date;

/**
 * 用户Mapper接口
 */
@Mapper
public interface UserMapper {
    /**
     * 插入用户记录
     * @param user 用户实体
     * @return 影响的行数
     */
    int insert(User user);

    /**
     * 批量插入用户记录
     * @param list 用户列表
     * @return 影响的行数
     */
    int batchInsert(@Param("list") List<User> list);

    /**
     * 根据ID删除用户
     * @param userId 用户ID
     * @return 影响的行数
     */
    int deleteById(@Param("userId") Integer userId);

    /**
     * 批量删除用户
     * @param userIds ID列表
     * @return 影响的行数
     */
    int batchDelete(@Param("userIds") List<Integer> userIds);

    /**
     * 更新用户信息
     * @param user 用户实体
     * @return 影响的行数
     */
    int updateById(User user);

    /**
     * 批量更新用户
     * @param list 用户列表
     * @return 影响的行数
     */
    int batchUpdate(@Param("list") List<User> list);

    /**
     * 根据ID查询用户
     * @param userId 用户ID
     * @return 用户实体
     */
    User selectById(@Param("userId") Integer userId);

    /**
     * 查询所有用户
     * @return 用户列表
     */
    List<User> selectAll();

    /**
     * 分页查询用户
     * @param offset 偏移量
     * @param limit 每页记录数
     * @return 用户列表
     */
    List<User> selectPage(@Param("offset") Integer offset, @Param("limit") Integer limit);

    /**
     * 查询用户总数
     * @return 总记录数
     */
    Long selectCount();

    /**
     * 条件查询用户
     * @param condition 查询条件
     * @return 用户列表
     */
    List<User> selectByCondition(@Param("condition") Map<String, Object> condition);

    /**
     * 条件查询用户数量
     * @param condition 查询条件
     * @return 记录数
     */
    Long selectCountByCondition(@Param("condition") Map<String, Object> condition);

    /**
     * 条件分页查询用户
     * @param condition 查询条件
     * @param offset 偏移量
     * @param limit 每页记录数
     * @return 用户列表
     */
    List<User> selectPageByCondition(
        @Param("condition") Map<String, Object> condition,
        @Param("offset") Integer offset,
        @Param("limit") Integer limit
    );

    /**
     * 根据用户名查询用户
     * @param username 用户名
     * @return 用户实体
     */
    User selectByUsername(@Param("username") String username);

    /**
     * 根据角色查询用户列表
     * @param role 角色（0: 管理员；1: 教师；2: 学生）
     * @return 用户列表
     */
    List<User> selectByRole(@Param("role") Integer role);

    /**
     * 更新用户状态
     * @param userId 用户ID
     * @param status 状态
     * @return 影响的行数
     */
    int updateStatus(@Param("userId") Integer userId, @Param("status") Boolean status);

    /**
     * 批量更新用户状态
     * @param userIds 用户ID列表
     * @param status 状态
     * @return 影响的行数
     */
    int batchUpdateStatus(@Param("userIds") List<Integer> userIds, @Param("status") Boolean status);

    /**
     * 修改密码
     * @param userId 用户ID
     * @param newPassword 新密码
     * @return 影响的行数
     */
    int updatePassword(@Param("userId") Integer userId, @Param("newPassword") String newPassword);

    /**
     * 更新用户联系方式
     * @param userId 用户ID
     * @param phone 手机号
     * @param email 邮箱
     * @return 影响的行数
     */
    int updateContact(@Param("userId") Integer userId, 
                     @Param("phone") String phone,
                     @Param("email") String email);

    /**
     * 更新用户性别
     * @param userId 用户ID
     * @param sex 性别（0: 女；1: 男）
     * @return 影响的行数
     */
    int updateSex(@Param("userId") Integer userId, @Param("sex") Boolean sex);

    /**
     * 根据邮箱查询用户
     * @param email 邮箱
     * @return 用户实体
     */
    User selectByEmail(@Param("email") String email);

    /**
     * 根据手机号查询用户
     * @param phone 手机号
     * @return 用户实体
     */
    User selectByPhone(@Param("phone") String phone);

    /**
     * 更新用户注册时间
     * @param userId 用户ID
     * @param createdTime 注册时间
     * @return 影响的行数
     */
    int updateCreatedTime(@Param("userId") Integer userId, @Param("createdTime") Date createdTime);
} 
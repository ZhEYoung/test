package com.exam.service;

import com.exam.entity.Admin;
import com.exam.entity.User;

import java.util.List;
import java.util.Map;
import java.util.Date;

/**
 * 管理员服务接口
 */
public interface AdminService {
    
    /**
     * 插入一条记录
     */
    int insert(Admin record);

    /**
     * 根据ID删除
     */
    int deleteById(Integer id);

    /**
     * 根据ID更新
     */
    int updateById(Admin record);

    /**
     * 根据ID查询
     */
    Admin getById(Integer id);

    /**
     * 查询所有记录
     */
    List<Admin> getAll();

    /**
     * 分页查询
     */
    List<Admin> getPage(Integer pageNum, Integer pageSize);

    /**
     * 查询总记录数
     */
    Long getCount();

    /**
     * 条件查询
     */
    List<Admin> getByCondition(Map<String, Object> condition);

    /**
     * 条件查询记录数
     */
    Long getCountByCondition(Map<String, Object> condition);

    /**
     * 条件分页查询
     */
    List<Admin> getPageByCondition(Map<String, Object> condition, Integer pageNum, Integer pageSize);
    
    /**
     * 根据用户ID查询管理员信息
     */
    Admin getByUserId(Integer userId);
    
    /**
     * 根据管理员姓名查询
     */
    Admin getByName(String name);
    
    /**
     * 更新管理员备注信息
     */
    int updateOther(Integer adminId, String other);
    
    /**
     * 批量更新管理员备注信息
     */
    int batchUpdateOther(List<Integer> adminIds, String other);
    
    /**
     * 查询管理员的操作日志
     */
    List<Map<String, Object>> getAdminLogs(Integer adminId, Date startTime, Date endTime);
    
    /**
     * 统计系统用户数量
     * @return 用户统计信息
     */
    Map<String, Long> countSystemUsers();
    
    /**
     * 管理员模拟登录其他用户
     * @param adminId 管理员ID
     * @param targetUserId 目标用户ID
     * @param ipAddress IP地址
     * @param deviceInfo 设备信息
     * @return 模拟登录的用户信息，如果失败返回null
     */
    User impersonateUser(Integer adminId, Integer targetUserId, String ipAddress, String deviceInfo);
    
    /**
     * 统计系统资源使用情况
     * @return 资源使用统计信息
     */
    Map<String, Object> getSystemResourceStats();
} 
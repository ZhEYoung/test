package com.exam.service.impl;

import com.exam.entity.Admin;
import com.exam.mapper.AdminMapper;
import com.exam.mapper.UserMapper;
import com.exam.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

/**
 * 管理员服务实现类
 */
@Service
@Transactional
public class AdminServiceImpl implements AdminService {

    @Autowired
    private AdminMapper adminMapper;

    @Autowired
    private UserMapper userMapper;

    // 基础CRUD方法
    @Override
    public int insert(Admin record) {
        return adminMapper.insert(record);
    }

    @Override
    public int deleteById(Integer id) {
        return adminMapper.deleteById(id);
    }

    @Override
    public int updateById(Admin record) {
        return adminMapper.updateById(record);
    }

    @Override
    public Admin selectById(Integer id) {
        return adminMapper.selectById(id);
    }

    @Override
    public List<Admin> selectAll() {
        return adminMapper.selectAll();
    }

    @Override
    public List<Admin> selectPage(Integer pageNum, Integer pageSize) {
        int offset = (pageNum - 1) * pageSize;
        return adminMapper.selectPage(offset, pageSize);
    }

    @Override
    public Long selectCount() {
        return adminMapper.selectCount();
    }

    @Override
    public List<Admin> selectByCondition(Map<String, Object> condition) {
        return adminMapper.selectByCondition(condition);
    }

    @Override
    public Long selectCountByCondition(Map<String, Object> condition) {
        return adminMapper.selectCountByCondition(condition);
    }

    @Override
    public List<Admin> selectPageByCondition(Map<String, Object> condition, Integer pageNum, Integer pageSize) {
        int offset = (pageNum - 1) * pageSize;
        return adminMapper.selectPageByCondition(condition, offset, pageSize);
    }

    @Override
    public Admin getByUserId(Integer userId) {
        return adminMapper.selectByUserId(userId);
    }

    @Override
    public Admin getByName(String name) {
        return adminMapper.selectByName(name);
    }

    @Override
    public int updateOther(Integer adminId, String other) {
        return adminMapper.updateOther(adminId, other);
    }

    @Override
    public int batchUpdateOther(List<Integer> adminIds, String other) {
        return adminMapper.batchUpdateOther(adminIds, other);
    }

    @Override
    public List<Map<String, Object>> getAdminLogs(Integer adminId, Date startTime, Date endTime) {
        return adminMapper.selectAdminLogs(adminId, startTime, endTime);
    }

    @Override
    public Map<String, Long> countSystemUsers() {
        Map<String, Long> userStats = new HashMap<>();
        
        // 统计各角色用户数量
        userStats.put("adminCount", (long) userMapper.selectByRole(0).size());
        userStats.put("teacherCount", (long) userMapper.selectByRole(1).size());
        userStats.put("studentCount", (long) userMapper.selectByRole(2).size());
        
        // 计算总用户数
        Long totalUsers = userStats.values().stream().mapToLong(Long::longValue).sum();
        userStats.put("totalUsers", totalUsers);
        
        return userStats;
    }

    @Override
    public Map<String, Object> getSystemResourceStats() {
        Map<String, Object> resourceStats = new HashMap<>();
        
        // 获取系统运行时信息
        Runtime runtime = Runtime.getRuntime();
        
        // 内存使用情况
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        
        resourceStats.put("totalMemory", totalMemory);
        resourceStats.put("usedMemory", usedMemory);
        resourceStats.put("freeMemory", freeMemory);
        resourceStats.put("memoryUsagePercent", (double) usedMemory / totalMemory * 100);
        
        // CPU使用情况
        int processors = runtime.availableProcessors();
        resourceStats.put("processors", processors);
        
        return resourceStats;
    }
} 
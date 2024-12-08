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
public class AdminServiceImpl extends BaseServiceImpl<Admin, AdminMapper> implements AdminService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public Admin getByUserId(Integer userId) {
        return baseMapper.selectByUserId(userId);
    }

    @Override
    public Admin getByName(String name) {
        return baseMapper.selectByName(name);
    }

    @Override
    public int updateOther(Integer adminId, String other) {
        return baseMapper.updateOther(adminId, other);
    }

    @Override
    public int batchUpdateOther(List<Integer> adminIds, String other) {
        return baseMapper.batchUpdateOther(adminIds, other);
    }

    @Override
    public List<Map<String, Object>> getAdminLogs(Integer adminId, Date startTime, Date endTime) {
        return baseMapper.selectAdminLogs(adminId, startTime, endTime);
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
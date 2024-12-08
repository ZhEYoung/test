package com.exam.service.impl;

import com.exam.entity.Log;
import com.exam.mapper.LogMapper;
import com.exam.mapper.UserMapper;
import com.exam.service.LogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

/**
 * 日志服务实现类
 */
@Service
@Transactional
public class LogServiceImpl extends BaseServiceImpl<Log, LogMapper> implements LogService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public List<Log> getByUserId(Integer userId) {
        return baseMapper.selectByUserId(userId);
    }

    @Override
    public List<Log> getByOperationType(String operationType) {
        return baseMapper.selectByOperationType(operationType);
    }

    @Override
    public List<Log> getByTimeRange(Date startTime, Date endTime) {
        return baseMapper.selectByTimeRange(startTime, endTime);
    }

    @Override
    public List<Log> getByIpAddress(String ipAddress) {
        return baseMapper.selectByIpAddress(ipAddress);
    }

    @Override
    public List<Log> getByStatus(String status) {
        return baseMapper.selectByStatus(status);
    }

    @Override
    public List<Log> getRecentLogs(Integer userId, Integer limit) {
        return baseMapper.selectRecentLogs(userId, limit);
    }

    @Override
    public List<Log> getExceptionLogs(Date startTime, Date endTime) {
        return baseMapper.selectExceptionLogs(startTime, endTime);
    }

    @Override
    public Long countUserOperations(Integer userId) {
        return baseMapper.countUserOperations(userId);
    }

    @Override
    public List<Map<String, Object>> countByOperationType() {
        return baseMapper.countByOperationType();
    }

    @Override
    public List<Map<String, Object>> countByStatus() {
        return baseMapper.countByStatus();
    }

    @Override
    public List<Map<String, Object>> countByIpAddress() {
        return baseMapper.countByIpAddress();
    }

    @Override
    public List<Map<String, Object>> countDailyOperations(Date startTime, Date endTime) {
        return baseMapper.countDailyOperations(startTime, endTime);
    }

    @Override
    public List<Map<String, Object>> getFrequentUsers(Integer limit) {
        return baseMapper.selectFrequentUsers(limit);
    }

    @Override
    public List<Log> getSuspiciousLogs() {
        return baseMapper.selectSuspiciousLogs();
    }

    @Override
    public int logLogin(Integer userId, String ipAddress, String deviceInfo) {
        Log log = new Log();
        log.setUserId(userId);
        log.setOperationType("LOGIN");
        log.setDescription("用户登录系统");
        log.setIpAddress(ipAddress);
        log.setDeviceInfo(deviceInfo);
        log.setOperationTime(new Date());
        log.setStatus("SUCCESS");
        
        return baseMapper.insert(log);
    }

    @Override
    public int logOperation(Integer userId, String operationType, String description, String ipAddress) {
        Log log = new Log();
        log.setUserId(userId);
        log.setOperationType(operationType);
        log.setDescription(description);
        log.setIpAddress(ipAddress);
        log.setOperationTime(new Date());
        log.setStatus("SUCCESS");
        
        return baseMapper.insert(log);
    }

    @Override
    public int logException(String module, String error, String stackTrace) {
        Log log = new Log();
        log.setOperationType("EXCEPTION");
        log.setModule(module);
        log.setDescription(error);
        log.setStackTrace(stackTrace);
        log.setOperationTime(new Date());
        log.setStatus("ERROR");
        
        return baseMapper.insert(log);
    }

    @Override
    public int cleanExpiredLogs(Integer days) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -days);
        Date expireDate = calendar.getTime();
        
        return baseMapper.deleteExpiredLogs(expireDate);
    }

    @Override
    public Map<String, Object> exportOperationLogs(Date startTime, Date endTime) {
        Map<String, Object> data = new HashMap<>();
        
        // 获取操作日志列表
        List<Log> logs = getByTimeRange(startTime, endTime);
        data.put("logs", logs);
        
        // 统计操作类型分布
        data.put("operationTypeStats", countByOperationType());
        
        // 统计操作状态分布
        data.put("statusStats", countByStatus());
        
        // 统计IP访问次数
        data.put("ipStats", countByIpAddress());
        
        // 统计每日操作次数
        data.put("dailyStats", countDailyOperations(startTime, endTime));
        
        // 获取高频操作用户
        data.put("frequentUsers", getFrequentUsers(10));
        
        return data;
    }

    @Override
    public Map<String, Object> exportExceptionLogs(Date startTime, Date endTime) {
        Map<String, Object> data = new HashMap<>();
        
        // 获取异常日志列表
        List<Log> logs = getExceptionLogs(startTime, endTime);
        data.put("logs", logs);
        
        // 统计异常模块分布
        List<Map<String, Object>> moduleStats = baseMapper.countByModule();
        data.put("moduleStats", moduleStats);
        
        // 统计每日异常次数
        List<Map<String, Object>> dailyStats = baseMapper.countDailyExceptions(startTime, endTime);
        data.put("dailyStats", dailyStats);
        
        return data;
    }
} 
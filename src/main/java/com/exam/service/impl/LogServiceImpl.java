package com.exam.service.impl;

import com.exam.entity.Log;
import com.exam.mapper.LogMapper;
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
public class LogServiceImpl implements LogService {

    @Autowired
    private LogMapper logMapper;

    @Override
    public int insert(Log record) {
        return logMapper.insert(record);
    }

    @Override
    public int deleteById(Integer id) {
        return logMapper.deleteById(id);
    }

    @Override
    public int updateById(Log record) {
        return logMapper.update(record);
    }

    @Override
    public Log getById(Integer id) {
        return logMapper.selectById(id);
    }

    @Override
    public List<Log> getAll() {
        return logMapper.selectAll();
    }

    @Override
    public List<Log> getPage(Integer pageNum, Integer pageSize) {
        int offset = (pageNum - 1) * pageSize;
        return logMapper.selectByConditions(null, null, null, null, null, null,null, offset, pageSize);
    }

    @Override
    public Long getCount() {
        return (long) logMapper.selectAll().size();
    }

    @Override
    public List<Log> getByCondition(Map<String, Object> condition) {
        Integer userId = (Integer) condition.get("userId");
        Integer actionType = (Integer) condition.get("actionType");
        String objectType = (String) condition.get("objectType");
        String actionDescription = (String) condition.get("actionDescription");
        Date startTime = (Date) condition.get("startTime");
        Date endTime = (Date) condition.get("endTime");
        String status = (String) condition.get("status");
        return logMapper.selectByConditions(userId, actionType, objectType, actionDescription,startTime, endTime, status, null, null);
    }

    @Override
    public Long getCountByCondition(Map<String, Object> condition) {
        List<Log> logs = getByCondition(condition);
        return (long) logs.size();
    }

    @Override
    public List<Log> getPageByCondition(Map<String, Object> condition, Integer pageNum, Integer pageSize) {
        int offset = (pageNum - 1) * pageSize;
        Integer userId = (Integer) condition.get("userId");
        Integer actionType = (Integer) condition.get("actionType");
        String objectType = (String) condition.get("objectType");
        String actionDescription = (String) condition.get("actionDescription");
        Date startTime = (Date) condition.get("startTime");
        Date endTime = (Date) condition.get("endTime");
        String status = (String) condition.get("status");
        return logMapper.selectByConditions(userId, actionType, objectType, actionDescription,startTime, endTime, status, offset, pageSize);
    }

    @Override
    public List<Log> getByUserId(Integer userId) {
        return logMapper.selectByUserId(userId);
    }

    @Override
    public List<Log> getByActionType(Integer actionType) {
        return logMapper.selectByActionType(actionType);
    }

    @Override
    public List<Log> getByTimeRange(Date startTime, Date endTime) {
        return logMapper.selectByTimeRange(startTime, endTime);
    }

    @Override
    public List<Log> getByIpAddress(String ipAddress) {
        return logMapper.selectByIpAddress(ipAddress);
    }

    @Override
    public List<Log> getByStatus(String status) {
        return logMapper.selectByStatus(status);
    }

    @Override
    public List<Log> getRecentLogs(Integer userId, Integer limit) {
        return logMapper.selectRecentLogs(userId, limit);
    }

    @Override
    public List<Log> getExceptionLogs(Date startTime, Date endTime) {
        return logMapper.selectExceptionLogs(startTime, endTime);
    }

    @Override
    public Long countUserOperations(Integer userId) {
        return logMapper.countUserOperations(userId);
    }

    @Override
    public List<Map<String, Object>> countByOperationType() {
        return logMapper.countByOperationType();
    }

    @Override
    public List<Map<String, Object>> countByStatus() {
        return logMapper.countByStatus();
    }

    @Override
    public List<Map<String, Object>> countByIpAddress() {
        return logMapper.countByIpAddress();
    }

    @Override
    public List<Map<String, Object>> countDailyOperations(Date startTime, Date endTime) {
        return logMapper.countDailyOperations(startTime, endTime);
    }

    @Override
    public List<Map<String, Object>> getFrequentUsers(Integer limit) {
        return logMapper.selectFrequentUsers(limit);
    }

    @Override
    public List<Log> getSuspiciousLogs() {
        return logMapper.selectSuspiciousLogs();
    }

    @Override
    public int logLogin(Integer userId, String ipAddress, String deviceInfo) {
        Log log = new Log();
        log.setUserId(userId);
        log.setActionType(3); // LOGIN
        log.setActionDescription("用户登录");
        log.setIpAddress(ipAddress);
        log.setDeviceInfo(deviceInfo);
        log.setObjectType("USER");
        log.setStatus("SUCCESS");
        log.setCreatedTime(new Date());
        return logMapper.insert(log);
    }

    @Override
    public int logOperation(Integer userId, Integer operationType, String description, String ipAddress) {
        Log log = new Log();
        log.setUserId(userId);
        log.setActionType(operationType);
        log.setActionDescription(description);
        log.setIpAddress(ipAddress);
        log.setObjectType("SYSTEM");
        log.setStatus("SUCCESS");
        log.setCreatedTime(new Date());
        return logMapper.insert(log);
    }

    @Override
    public int logException(String error) {
        Log log = new Log();
        log.setActionType(5); // ERROR
        log.setActionDescription(error);
        log.setObjectType("SYSTEM");
        log.setStatus("ERROR");
        log.setCreatedTime(new Date());
        return logMapper.insert(log);
    }

    @Override
    public int cleanExpiredLogs(Integer days) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -days);
        return logMapper.deleteExpiredLogs(calendar.getTime());
    }

    @Override
    public Map<String, Object> exportOperationLogs(Date startTime, Date endTime) {
        Map<String, Object> result = new HashMap<>();
        
        // 获取操作日志
        List<Log> logs = logMapper.selectByTimeRange(startTime, endTime);
        result.put("logs", logs);
        
        // 获取操作类型统计
        List<Map<String, Object>> actionStats = logMapper.countActionTypes(startTime, endTime);
        result.put("actionStats", actionStats);
        
        // 获取用户操作统计
        List<Map<String, Object>> userStats = logMapper.countUserActions(startTime, endTime);
        result.put("userStats", userStats);
        
        // 获取系统操作统计
        List<Map<String, Object>> systemStats = logMapper.selectSystemOperations(startTime, endTime);
        result.put("systemStats", systemStats);
        
        return result;
    }

    @Override
    public Map<String, Object> exportExceptionLogs(Date startTime, Date endTime) {
        Map<String, Object> result = new HashMap<>();
        
        // 获取异常日志
        List<Log> logs = logMapper.selectExceptionLogs(startTime, endTime);
        result.put("logs", logs);
        
        // 获取异常统计
        List<Map<String, Object>> exceptionStats = logMapper.countExceptionLogs(startTime, endTime);
        result.put("exceptionStats", exceptionStats);
        
        // 获取每日异常统计
        List<Map<String, Object>> dailyStats = logMapper.countDailyExceptions(startTime, endTime);
        result.put("dailyStats", dailyStats);
        
        return result;
    }
} 
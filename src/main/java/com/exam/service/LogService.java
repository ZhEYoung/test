package com.exam.service;

import com.exam.entity.Log;
import java.util.List;
import java.util.Map;
import java.util.Date;

/**
 * 日志服务接口
 */
public interface LogService {
    
    /**
     * 插入一条记录
     */
    int insert(Log record);

    /**
     * 根据ID删除
     */
    int deleteById(Integer id);

    /**
     * 根据ID更新
     */
    int updateById(Log record);

    /**
     * 根据ID查询
     */
    Log selectById(Integer id);

    /**
     * 查询所有记录
     */
    List<Log> selectAll();

    /**
     * 分页查询
     */
    List<Log> selectPage(Integer pageNum, Integer pageSize);

    /**
     * 查询总记录数
     */
    Long selectCount();

    /**
     * 条件查询
     */
    List<Log> selectByCondition(Map<String, Object> condition);

    /**
     * 条件查询记录数
     */
    Long selectCountByCondition(Map<String, Object> condition);

    /**
     * 条件分页查询
     */
    List<Log> selectPageByCondition(Map<String, Object> condition, Integer pageNum, Integer pageSize);
    
    /**
     * 根据用户ID查询日志
     */
    List<Log> getByUserId(Integer userId);
    
    /**
     * 根据操作类型查询日志
     */
    List<Log> getByOperationType(String operationType);
    
    /**
     * 根据时间范围查询日志
     */
    List<Log> getByTimeRange(Date startTime, Date endTime);
    
    /**
     * 根据IP地址查询日志
     */
    List<Log> getByIpAddress(String ipAddress);

    /**
     * 根据操作状态查询日志
     */
    List<Log> getByStatus(String status);

    /**
     * 查询用户最近的操作日志
     */
    List<Log> getRecentLogs(Integer userId, Integer limit);

    /**
     * 查询系统异常日志
     */
    List<Log> getExceptionLogs(Date startTime, Date endTime);

    /**
     * 统计用户操作次数
     */
    Long countUserOperations(Integer userId);

    /**
     * 统计操作类型分布
     */
    List<Map<String, Object>> countByOperationType();

    /**
     * 统计操作状态分布
     */
    List<Map<String, Object>> countByStatus();

    /**
     * 统计IP访问次数
     */
    List<Map<String, Object>> countByIpAddress();

    /**
     * 统计每日操作次数
     */
    List<Map<String, Object>> countDailyOperations(Date startTime, Date endTime);
    
    /**
     * 查询高频操作用户
     */
    List<Map<String, Object>> getFrequentUsers(Integer limit);
    
    /**
     * 查询可疑操作日志
     */
    List<Log> getSuspiciousLogs();
    
    /**
     * 记录用户登录日志
     * @param userId 用户ID
     * @param ipAddress IP地址
     * @param deviceInfo 设备信息
     * @return 记录结果
     */
    int logLogin(Integer userId, String ipAddress, String deviceInfo);
    
    /**
     * 记录用户操作日志
     * @param userId 用户ID
     * @param operationType 操作类型
     * @param description 操作描述
     * @param ipAddress IP地址
     * @return 记录结果
     */
    int logOperation(Integer userId, Integer operationType, String description, String ipAddress);
    
    /**
     * 记录系统异常日志
     * @param error 异常信息
     * @return 记录结果
     */
    int logException(String error);

    /**
     * 清理过期日志
     * @param days 保留天数
     * @return 清理结果
     */
    int cleanExpiredLogs(Integer days);
    
    /**
     * 导出操作日志
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 日志数据
     */
    Map<String, Object> exportOperationLogs(Date startTime, Date endTime);
    
    /**
     * 导出异常日志
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 日志数据
     */
    Map<String, Object> exportExceptionLogs(Date startTime, Date endTime);
} 
package com.exam.mapper;

import com.exam.entity.Log;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;
import java.util.Date;

/**
 * 日志Mapper接口
 */
@Mapper
public interface LogMapper {
    /**
     * 插入日志记录
     * @param log 日志实体
     * @return 影响行数
     */
    int insert(Log log);

    /**
     * 根据ID删除日志
     * @param logId 日志ID
     * @return 影响行数
     */
    int deleteById(@Param("logId") Integer logId);

    /**
     * 更新日志信息
     * @param log 日志实体
     * @return 影响行数
     */
    int update(Log log);

    /**
     * 根据ID查询日志
     * @param logId 日志ID
     * @return 日志实体
     */
    Log selectById(@Param("logId") Integer logId);

    /**
     * 查询所有日志
     * @return 日志列表
     */
    List<Log> selectAll();

    /**
     * 根据用户ID查询日志列表
     * @param userId 用户ID
     * @return 日志列表
     */
    List<Log> selectByUserId(@Param("userId") Integer userId);

    /**
     * 根据操作类型查询日志列表
     * @param actionType 操作类型
     * @return 日志列表
     */
    List<Log> selectByActionType(@Param("actionType") String actionType);

    /**
     * 根据时间范围查询日志列表
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 日志列表
     */
    List<Log> selectByTimeRange(
        @Param("startTime") Date startTime,
        @Param("endTime") Date endTime
    );

    /**
     * 根据操作对象查询日志列表
     * @param objectType 操作对象类型
     * @return 日志列表
     */
    List<Log> selectByObjectType(@Param("objectType") String objectType);

    /**
     * 批量插入日志
     * @param list 日志列表
     * @return 影响行数
     */
    int batchInsert(@Param("list") List<Log> list);

    /**
     * 高级查询日志
     * @param userId 用户ID
     * @param actionType 操作类型
     * @param objectType 操作对象类型
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param status 状态
     * @param offset 偏移量
     * @param limit 每页记录数
     * @return 日志列表
     */
    List<Log> selectByConditions(
        @Param("userId") Integer userId,
        @Param("actionType") Integer actionType,
        @Param("objectType") String objectType,
        @Param("startTime") Date startTime,
        @Param("endTime") Date endTime,
        @Param("status") String status,
        @Param("offset") Integer offset,
        @Param("limit") Integer limit
    );

    /**
     * 统计用户操作频率
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 用户操作统计信息
     */
    List<Map<String, Object>> countUserActions(
        @Param("startTime") Date startTime,
        @Param("endTime") Date endTime
    );

    /**
     * 统计操作类型分布
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 操作类型统计信息
     */
    List<Map<String, Object>> countActionTypes(
        @Param("startTime") Date startTime,
        @Param("endTime") Date endTime
    );

    /**
     * 统计异常操作
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 异常操作统计信息
     */
    List<Map<String, Object>> countExceptionLogs(
        @Param("startTime") Date startTime,
        @Param("endTime") Date endTime
    );

    /**
     * 分析用户行为模式
     * @param userId 用户ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 用户行为分析信息
     */
    List<Map<String, Object>> analyzeUserBehavior(
        @Param("userId") Integer userId,
        @Param("startTime") Date startTime,
        @Param("endTime") Date endTime
    );

    /**
     * 清理过期日志
     * @param expireTime 过期时间
     * @return 影响行数
     */
    int deleteExpiredLogs(@Param("expireTime") Date expireTime);

    /**
     * 批量清理日志
     * @param logIds 日志ID列表
     * @return 影响行数
     */
    int batchCleanLogs(@Param("logIds") List<Integer> logIds);

    /**
     * 查询系统操作记录
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 系统操作统计信息
     */
    List<Map<String, Object>> selectSystemOperations(
        @Param("startTime") Date startTime,
        @Param("endTime") Date endTime
    );


    /**
     * 查询关键操作日志
     * @param actionTypes 操作类型列表
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 关键操作日志列表
     */
    List<Log> selectCriticalLogs(
        @Param("actionTypes") List<String> actionTypes,
        @Param("startTime") Date startTime,
        @Param("endTime") Date endTime
    );

    /**
     * 统计日志存储情况
     * @return 日志存储统计信息
     */
    Map<String, Object> countLogStorage();

    /**
     * 查询用户登录记录
     * @param userId 用户ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 用户登录记录列表
     */
    List<Map<String, Object>> selectLoginHistory(
        @Param("userId") Integer userId,
        @Param("startTime") Date startTime,
        @Param("endTime") Date endTime
    );

    /**
     * 根据IP地址查询日志
     * @param ipAddress IP地址
     * @return 日志列表
     */
    List<Log> selectByIpAddress(@Param("ipAddress") String ipAddress);

    /**
     * 根据状态查询日志
     * @param status 状态
     * @return 日志列表
     */
    List<Log> selectByStatus(@Param("status") String status);

    /**
     * 查询用户最近的操作日志
     * @param userId 用户ID
     * @param limit 限制数量
     * @return 最近的操作日志列表
     */
    List<Log> selectRecentLogs(
        @Param("userId") Integer userId,
        @Param("limit") Integer limit
    );

    /**
     * 查询异常日志
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 异常日志列表
     */
    List<Log> selectExceptionLogs(
        @Param("startTime") Date startTime,
        @Param("endTime") Date endTime
    );

    /**
     * 统计用户操作次数
     * @param userId 用户ID
     * @return 操作次数
     */
    Long countUserOperations(@Param("userId") Integer userId);

    /**
     * 统计操作类型分布
     * @return 操作类型分布统计信息
     */
    List<Map<String, Object>> countByOperationType();

    /**
     * 统计操作状态分布
     * @return 操作状态分布统计信息
     */
    List<Map<String, Object>> countByStatus();

    /**
     * 统计IP访问次数
     * @return IP访问次数统计信息
     */
    List<Map<String, Object>> countByIpAddress();

    /**
     * 统计每日操作次数
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 每日操作次数统计信息
     */
    List<Map<String, Object>> countDailyOperations(
        @Param("startTime") Date startTime,
        @Param("endTime") Date endTime
    );

    /**
     * 查询高频操作用户
     * @param limit 限制数量
     * @return 高频操作用户统计信息
     */
    List<Map<String, Object>> selectFrequentUsers(@Param("limit") Integer limit);

    /**
     * 查询可疑操作日志
     * @return 可疑操作日志列表
     */
    List<Log> selectSuspiciousLogs();

    /**
     * 统计每日异常日志数量
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 每日异常统计数据
     */
    List<Map<String, Object>> countDailyExceptions(
        @Param("startTime") Date startTime,
        @Param("endTime") Date endTime
    );
} 
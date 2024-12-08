package com.exam.mapper;

import com.exam.entity.Log;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;
import java.util.Date;

/**
 * 日志Mapper接口
 */
public interface LogMapper extends BaseMapper<Log> {
    /**
     * 根据用户ID查询日志列表
     */
    List<Log> selectByUserId(@Param("userId") Integer userId);

    /**
     * 根据操作类型查询日志列表
     */
    List<Log> selectByActionType(@Param("actionType") String actionType);

    /**
     * 根据时间范围查询日志列表
     */
    List<Log> selectByTimeRange(
        @Param("startTime") Date startTime,
        @Param("endTime") Date endTime
    );

    /**
     * 根据操作对象查询日志列表
     */
    List<Log> selectByObjectType(@Param("objectType") String objectType);

    /**
     * 批量插入日志
     */
    int batchInsert(@Param("list") List<Log> list);

    /**
     * 高级查询日志
     */
    List<Log> selectByConditions(
        @Param("userId") Integer userId,
        @Param("actionType") String actionType,
        @Param("objectType") String objectType,
        @Param("startTime") Date startTime,
        @Param("endTime") Date endTime,
        @Param("status") String status,
        @Param("offset") Integer offset,
        @Param("limit") Integer limit
    );

    /**
     * 统计用户操作频率
     */
    List<Map<String, Object>> countUserActions(
        @Param("startTime") Date startTime,
        @Param("endTime") Date endTime
    );

    /**
     * 统计操作类型分布
     */
    List<Map<String, Object>> countActionTypes(
        @Param("startTime") Date startTime,
        @Param("endTime") Date endTime
    );

    /**
     * 统计异常操作
     */
    List<Map<String, Object>> countExceptionLogs(
        @Param("startTime") Date startTime,
        @Param("endTime") Date endTime
    );

    /**
     * 分析用户行为模式
     */
    List<Map<String, Object>> analyzeUserBehavior(
        @Param("userId") Integer userId,
        @Param("startTime") Date startTime,
        @Param("endTime") Date endTime
    );

    /**
     * 清理过期日志
     */
    int cleanExpiredLogs(@Param("expireTime") Date expireTime);

    /**
     * 批量清理日志
     */
    int batchCleanLogs(@Param("logIds") List<Integer> logIds);

    /**
     * 查询系统操作记录
     */
    List<Map<String, Object>> selectSystemOperations(
        @Param("startTime") Date startTime,
        @Param("endTime") Date endTime
    );

    /**
     * 统计操作响应时间
     */
    List<Map<String, Object>> analyzeResponseTime(
        @Param("startTime") Date startTime,
        @Param("endTime") Date endTime
    );

    /**
     * 查询关键操作日志
     */
    List<Log> selectCriticalLogs(
        @Param("actionTypes") List<String> actionTypes,
        @Param("startTime") Date startTime,
        @Param("endTime") Date endTime
    );

    /**
     * 统计日志存储情况
     */
    Map<String, Object> countLogStorage();

    /**
     * 查询用户登录记录
     */
    List<Map<String, Object>> selectLoginHistory(
        @Param("userId") Integer userId,
        @Param("startTime") Date startTime,
        @Param("endTime") Date endTime
    );
} 
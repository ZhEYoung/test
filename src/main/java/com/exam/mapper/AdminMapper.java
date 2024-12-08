package com.exam.mapper;

import com.exam.entity.Admin;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;
import java.util.Date;

/**
 * 管理员Mapper接口
 */
public interface AdminMapper extends BaseMapper<Admin> {
    /**
     * 根据用户ID查询管理员信息
     */
    Admin selectByUserId(@Param("userId") Integer userId);

    /**
     * 根据管理员姓名查询
     */
    Admin selectByName(@Param("name") String name);

    /**
     * 更新管理员备注信息
     */
    int updateOther(@Param("adminId") Integer adminId, @Param("other") String other);

    /**
     * 批量更新管理员备注信息
     */
    int batchUpdateOther(@Param("adminIds") List<Integer> adminIds, @Param("other") String other);

    /**
     * 查询管理员的操作日志
     */
    List<Map<String, Object>> selectAdminLogs(
        @Param("adminId") Integer adminId,
        @Param("startTime") Date startTime,
        @Param("endTime") Date endTime
    );
} 
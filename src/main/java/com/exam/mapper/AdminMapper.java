package com.exam.mapper;

import com.exam.entity.Admin;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;
import java.util.Date;

/**
 * 管理员Mapper接口
 */
@Mapper
public interface AdminMapper {
    /**
     * 插入管理员记录
     * @param admin 管理员实体
     * @return 影响的行数
     */
    int insert(Admin admin);

    /**
     * 批量插入管理员记录
     * @param list 管理员列表
     * @return 影响的行数
     */
    int batchInsert(@Param("list") List<Admin> list);

    /**
     * 根据ID删除管理员
     * @param adminId 管理员ID
     * @return 影响的行数
     */
    int deleteById(@Param("adminId") Integer adminId);

    /**
     * 批量删除管理员
     * @param adminIds ID列表
     * @return 影响的行数
     */
    int batchDelete(@Param("adminIds") List<Integer> adminIds);

    /**
     * 更新管理员信息
     * @param admin 管理员实体
     * @return 影响的行数
     */
    int updateById(Admin admin);

    /**
     * 批量更新管理员
     * @param list 管理员列表
     * @return 影响的行数
     */
    int batchUpdate(@Param("list") List<Admin> list);

    /**
     * 根据ID查询管理员
     * @param adminId 管理员ID
     * @return 管理员实体
     */
    Admin selectById(@Param("adminId") Integer adminId);

    /**
     * 查询所有管理员
     * @return 管理员列表
     */
    List<Admin> selectAll();

    /**
     * 分页查询管理员
     * @param offset 偏移量
     * @param limit 每页记录数
     * @return 管理员列表
     */
    List<Admin> selectPage(@Param("offset") Integer offset, @Param("limit") Integer limit);

    /**
     * 查询管理员总数
     * @return 总记录数
     */
    Long selectCount();

    /**
     * 条件查询管理员
     * @param condition 查询条件
     * @return 管理员列表
     */
    List<Admin> selectByCondition(@Param("condition") Map<String, Object> condition);

    /**
     * 条件查询管理员数量
     * @param condition 查询条件
     * @return 记录数
     */
    Long selectCountByCondition(@Param("condition") Map<String, Object> condition);

    /**
     * 条件分页查询管理员
     * @param condition 查询条件
     * @param offset 偏移量
     * @param limit 每页记录数
     * @return 管理员列表
     */
    List<Admin> selectPageByCondition(
        @Param("condition") Map<String, Object> condition,
        @Param("offset") Integer offset,
        @Param("limit") Integer limit
    );

    /**
     * 根据用户ID查询管理员信息
     * @param userId 用户ID
     * @return 管理员实体
     */
    Admin selectByUserId(@Param("userId") Integer userId);

    /**
     * 根据管理员姓名查询
     * @param name 管理员姓名
     * @return 管理员实体
     */
    Admin selectByName(@Param("name") String name);

    /**
     * 更新管理员备注信息
     * @param adminId 管理员ID
     * @param other 备注信息
     * @return 影响的行数
     */
    int updateOther(@Param("adminId") Integer adminId, @Param("other") String other);

    /**
     * 批量更新管理员备注信息
     * @param adminIds 管理员ID列表
     * @param other 备注信息
     * @return 影响的行数
     */
    int batchUpdateOther(@Param("adminIds") List<Integer> adminIds, @Param("other") String other);

    /**
     * 查询管理员的操作日志
     * @param adminId 管理员ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 日志列表
     */
    List<Map<String, Object>> selectAdminLogs(
        @Param("adminId") Integer adminId,
        @Param("startTime") Date startTime,
        @Param("endTime") Date endTime
    );
} 
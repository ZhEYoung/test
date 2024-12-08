package com.exam.mapper;

import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

/**
 * 基础Mapper接口
 * @param <T> 实体类型
 */
public interface BaseMapper<T> {
    /**
     * 插入一条记录
     * @param record 记录
     * @return 影响行数
     */
    int insert(T record);

    /**
     * 批量插入记录
     * @param list 记录列表
     * @return 影响行数
     */
    int batchInsert(@Param("list") List<T> list);

    /**
     * 根据ID删除
     * @param id 主键ID
     * @return 影响行数
     */
    int deleteById(@Param("id") Integer id);

    /**
     * 批量删除
     * @param ids ID列表
     * @return 影响行数
     */
    int batchDelete(@Param("ids") List<Integer> ids);

    /**
     * 根据ID更新
     * @param record 记录
     * @return 影响行数
     */
    int updateById(T record);

    /**
     * 批量更新
     * @param list 记录列表
     * @return 影响行数
     */
    int batchUpdate(@Param("list") List<T> list);

    /**
     * 根据ID查询
     * @param id 主键ID
     * @return 记录
     */
    T selectById(@Param("id") Integer id);

    /**
     * 查询所有记录
     * @return 记录列表
     */
    List<T> selectAll();

    /**
     * 分页查询
     * @param offset 偏移量
     * @param limit 每页记录数
     * @return 记录列表
     */
    List<T> selectPage(@Param("offset") Integer offset, @Param("limit") Integer limit);

    /**
     * 查询总记录数
     * @return 总记录数
     */
    Long selectCount();

    /**
     * 条件查询
     * @param condition 查询条件
     * @return 记录列表
     */
    List<T> selectByCondition(@Param("condition") Map<String, Object> condition);

    /**
     * 条件查询记录数
     * @param condition 查询条件
     * @return 记录数
     */
    Long selectCountByCondition(@Param("condition") Map<String, Object> condition);

    /**
     * 条件分页查询
     * @param condition 查询条件
     * @param offset 偏移量
     * @param limit 每页记录数
     * @return 记录列表
     */
    List<T> selectPageByCondition(
        @Param("condition") Map<String, Object> condition,
        @Param("offset") Integer offset,
        @Param("limit") Integer limit
    );

    /**
     * 根据字段查询
     * @param column 字段名
     * @param value 字段值
     * @return 记录列表
     */
    List<T> selectByColumn(@Param("column") String column, @Param("value") Object value);

    /**
     * 根据字段查询单条记录
     * @param column 字段名
     * @param value 字段值
     * @return 记录
     */
    T selectOneByColumn(@Param("column") String column, @Param("value") Object value);

    /**
     * 根据多个字段查询
     * @param columns 字段名和值的Map
     * @return 记录列表
     */
    List<T> selectByColumns(@Param("columns") Map<String, Object> columns);
} 
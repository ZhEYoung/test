package com.exam.service;

import java.util.List;
import java.util.Map;

/**
 * 基础Service接口
 * @param <T> 实体类型
 */
public interface BaseService<T> {
    /**
     * 插入一条记录
     */
    int insert(T record);

    /**
     * 批量插入记录
     */
    int batchInsert(List<T> list);

    /**
     * 根据ID删除
     */
    int deleteById(Integer id);

    /**
     * 批量删除
     */
    int batchDelete(List<Integer> ids);

    /**
     * 根据ID更新
     */
    int updateById(T record);

    /**
     * 批量更新
     */
    int batchUpdate(List<T> list);

    /**
     * 根据ID查询
     */
    T selectById(Integer id);

    /**
     * 查询所有记录
     */
    List<T> selectAll();

    /**
     * 分页查询
     */
    List<T> selectPage(Integer pageNum, Integer pageSize);

    /**
     * 查询总记录数
     */
    Long selectCount();

    /**
     * 条件查询
     */
    List<T> selectByCondition(Map<String, Object> condition);

    /**
     * 条件查询记录数
     */
    Long selectCountByCondition(Map<String, Object> condition);

    /**
     * 条件分页查询
     */
    List<T> selectPageByCondition(Map<String, Object> condition, Integer pageNum, Integer pageSize);
} 
package com.exam.service.impl;

import com.exam.mapper.BaseMapper;
import com.exam.service.BaseService;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import java.util.Map;

/**
 * 基础Service实现类
 * @param <T> 实体类型
 * @param <M> Mapper类型
 */
public abstract class BaseServiceImpl<T, M extends BaseMapper<T>> implements BaseService<T> {

    @Autowired
    protected M baseMapper;

    @Override
    public int insert(T record) {
        return baseMapper.insert(record);
    }

    @Override
    public int batchInsert(List<T> list) {
        return baseMapper.batchInsert(list);
    }

    @Override
    public int deleteById(Integer id) {
        return baseMapper.deleteById(id);
    }

    @Override
    public int batchDelete(List<Integer> ids) {
        return baseMapper.batchDelete(ids);
    }

    @Override
    public int updateById(T record) {
        return baseMapper.updateById(record);
    }

    @Override
    public int batchUpdate(List<T> list) {
        return baseMapper.batchUpdate(list);
    }

    @Override
    public T selectById(Integer id) {
        return baseMapper.selectById(id);
    }

    @Override
    public List<T> selectAll() {
        return baseMapper.selectAll();
    }

    @Override
    public List<T> selectPage(Integer pageNum, Integer pageSize) {
        int offset = (pageNum - 1) * pageSize;
        return baseMapper.selectPage(offset, pageSize);
    }

    @Override
    public Long selectCount() {
        return baseMapper.selectCount();
    }

    @Override
    public List<T> selectByCondition(Map<String, Object> condition) {
        return baseMapper.selectByCondition(condition);
    }

    @Override
    public Long selectCountByCondition(Map<String, Object> condition) {
        return baseMapper.selectCountByCondition(condition);
    }

    @Override
    public List<T> selectPageByCondition(Map<String, Object> condition, Integer pageNum, Integer pageSize) {
        int offset = (pageNum - 1) * pageSize;
        return baseMapper.selectPageByCondition(condition, offset, pageSize);
    }
} 
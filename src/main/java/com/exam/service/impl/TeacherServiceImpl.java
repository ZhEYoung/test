package com.exam.service.impl;

import com.exam.entity.Teacher;
import com.exam.entity.Class;
import com.exam.mapper.TeacherMapper;
import com.exam.service.TeacherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

/**
 * 教师服务实现类
 */
@Service
@Transactional
public class TeacherServiceImpl implements TeacherService {

    @Autowired
    private TeacherMapper teacherMapper;

    @Override
    public int insert(Teacher record) {
        return teacherMapper.insert(record);
    }

    @Override
    public int deleteById(Integer id) {
        return teacherMapper.deleteById(id);
    }

    @Override
    public int updateById(Teacher record) {
        return teacherMapper.updateById(record);
    }

    @Override
    public Teacher selectById(Integer id) {
        return teacherMapper.selectById(id);
    }

    @Override
    public List<Teacher> selectAll() {
        return teacherMapper.selectAll();
    }

    @Override
    public List<Teacher> selectPage(Integer pageNum, Integer pageSize) {
        int offset = (pageNum - 1) * pageSize;
        return teacherMapper.selectPage(offset, pageSize);
    }

    @Override
    public Long selectCount() {
        return teacherMapper.selectCount();
    }

    @Override
    public List<Teacher> selectByCondition(Map<String, Object> condition) {
        return teacherMapper.selectByCondition(condition);
    }

    @Override
    public Long selectCountByCondition(Map<String, Object> condition) {
        return teacherMapper.selectCountByCondition(condition);
    }

    @Override
    public List<Teacher> selectPageByCondition(Map<String, Object> condition, Integer pageNum, Integer pageSize) {
        int offset = (pageNum - 1) * pageSize;
        return teacherMapper.selectPageByCondition(condition, offset, pageSize);
    }

    @Override
    public Teacher getByUserId(Integer userId) {
        return teacherMapper.selectByUserId(userId);
    }

    @Override
    public List<Teacher> getByCollegeId(Integer collegeId) {
        return teacherMapper.selectByCollegeId(collegeId);
    }

    @Override
    public List<Teacher> getByName(String name) {
        return teacherMapper.selectByName(name);
    }

    @Override
    public List<Teacher> getByPermission(Integer permission) {
        return teacherMapper.selectByPermission(permission);
    }

    @Override
    public int updateOther(Integer teacherId, String other) {
        return teacherMapper.updateOther(teacherId, other);
    }

    @Override
    public int updatePermission(Integer teacherId, Integer permission) {
        return teacherMapper.updatePermission(teacherId, permission);
    }

    @Override
    public int batchUpdatePermission(List<Integer> teacherIds, Integer permission) {
        return teacherMapper.batchUpdatePermission(teacherIds, permission);
    }

    @Override
    public List<Class> getTeacherClasses(Integer teacherId) {
        return teacherMapper.selectTeacherClasses(teacherId);
    }

    @Override
    public Long countTeacherClasses(Integer teacherId) {
        return teacherMapper.countTeacherClasses(teacherId);
    }

    @Override
    public int batchImport(List<Teacher> teachers) {
        // 批量导入前进行数据验证
        for (Teacher teacher : teachers) {
            if (teacher.getTeacherId() == null || teacher.getName() == null) {
                return 0;
            }
        }
        return teacherMapper.batchInsert(teachers);
    }
} 
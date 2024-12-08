package com.exam.service.impl;

import com.exam.entity.Teacher;
import com.exam.entity.Class;
import com.exam.mapper.TeacherMapper;
import com.exam.service.TeacherService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

/**
 * 教师服务实现类
 */
@Service
@Transactional
public class TeacherServiceImpl extends BaseServiceImpl<Teacher, TeacherMapper> implements TeacherService {

    @Override
    public Teacher getByUserId(Integer userId) {
        return baseMapper.selectByUserId(userId);
    }

    @Override
    public List<Teacher> getByCollegeId(Integer collegeId) {
        return baseMapper.selectByCollegeId(collegeId);
    }

    @Override
    public List<Teacher> getByName(String name) {
        return baseMapper.selectByName(name);
    }

    @Override
    public List<Teacher> getByPermission(Integer permission) {
        return baseMapper.selectByPermission(permission);
    }

    @Override
    public int updateOther(Integer teacherId, String other) {
        return baseMapper.updateOther(teacherId, other);
    }

    @Override
    public int updatePermission(Integer teacherId, Integer permission) {
        return baseMapper.updatePermission(teacherId, permission);
    }

    @Override
    public int batchUpdatePermission(List<Integer> teacherIds, Integer permission) {
        return baseMapper.batchUpdatePermission(teacherIds, permission);
    }

    @Override
    public List<Class> getTeacherClasses(Integer teacherId) {
        return baseMapper.selectTeacherClasses(teacherId);
    }


    @Override
    public Long countTeacherClasses(Integer teacherId) {
        return baseMapper.countTeacherClasses(teacherId);
    }

    @Override
    public int batchImport(List<Teacher> teachers) {
        // 批量导入前进行数据验证
        for (Teacher teacher : teachers) {
            if (teacher.getTeacherId() == null || teacher.getName() == null) {
                return 0;
            }
        }
        return baseMapper.batchInsert(teachers);
    }


} 
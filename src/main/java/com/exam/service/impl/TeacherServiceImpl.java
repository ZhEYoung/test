package com.exam.service.impl;

import com.exam.entity.Teacher;
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
    public int insert(Teacher teacher) {
        // 验证教师数据
        if (!validateTeacher(teacher)) {
            return 0;
        }
        
        // 检查用户ID是否已经被使用
        Teacher existingTeacher = teacherMapper.selectByUserId(teacher.getUserId());
        if (existingTeacher != null) {
            System.out.println("用户ID已被使用: " + teacher.getUserId());
            return 0;
        }

        return teacherMapper.insert(teacher);
    }

    /**
     * 验证教师数据
     * @param teacher 教师信息
     * @return true 如果数据有效，false 如果数据无效
     */
    private boolean validateTeacher(Teacher teacher) {
        if (teacher == null) {
            System.out.println("教师对象为空");
            return false;
        }

        // 验证名称
        if (teacher.getName() == null || teacher.getName().trim().isEmpty()) {
            System.out.println("教师姓名为空");
            return false;
        }

        // 验证用户ID
        if (teacher.getUserId() == null || teacher.getUserId() <= 0) {
            System.out.println("用户ID无效: " + teacher.getUserId());
            return false;
        }

        // 验证学院ID
        if (teacher.getCollegeId() == null || teacher.getCollegeId() <= 0) {
            System.out.println("学院ID无效: " + teacher.getCollegeId());
            return false;
        }

        // 验证权限值（允许为null，默认为0）
        if (teacher.getPermission() == null) {
            teacher.setPermission(0);
        } else if (teacher.getPermission() < 0) {
            System.out.println("权限值无效: " + teacher.getPermission());
            return false;
        }

        return true;
    }

    @Override
    public int batchInsert(List<Teacher> teachers) {
        return teacherMapper.batchInsert(teachers);
    }

    @Override
    public int deleteById(Integer teacherId) {
        return teacherMapper.deleteById(teacherId);
    }

    @Override
    public int batchDelete(List<Integer> teacherIds) {
        return teacherMapper.batchDelete(teacherIds);
    }

    @Override
    public int updateById(Teacher teacher) {
        return teacherMapper.updateById(teacher);
    }

    @Override
    public int batchUpdate(List<Teacher> teachers) {
        return teacherMapper.batchUpdate(teachers);
    }

    @Override
    public Teacher getById(Integer teacherId) {
        return teacherMapper.selectById(teacherId);
    }

    @Override
    public List<Teacher> getAll() {
        return teacherMapper.selectAll();
    }

    @Override
    public List<Teacher> getPage(Integer pageNum, Integer pageSize) {
        int offset = (pageNum - 1) * pageSize;
        return teacherMapper.selectPage(offset, pageSize);
    }

    @Override
    public int getCount() {
        return teacherMapper.selectCount();
    }

    @Override
    public List<Teacher> getByCondition(Teacher teacher) {
        return teacherMapper.selectByCondition(teacher);
    }

    @Override
    public Teacher getByUserId(Integer userId) {
        return teacherMapper.selectByUserId(userId);
    }

    @Override
    public Integer getTeacherIdByUserId(Integer userId) {
        Teacher teacher = teacherMapper.selectByUserId(userId);
        return teacher == null ? null : teacher.getTeacherId();
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
    public int countByCollege(Integer collegeId) {
        return teacherMapper.countByCollege(collegeId);
    }

    @Override
    public List<Map<String, Object>> countByPermission() {
        return teacherMapper.countByPermission();
    }

    @Override
    public Map<String, Object> getExamStats(Integer teacherId) {
        return teacherMapper.selectExamStats(teacherId);
    }

    @Override
    public List<Map<String, Object>> getTeacherExams(Integer teacherId) {
        return teacherMapper.selectTeacherExams(teacherId);
    }
} 
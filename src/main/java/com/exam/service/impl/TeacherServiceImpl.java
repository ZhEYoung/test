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
    public List<Class> getTeacherClassesByTerm(Integer teacherId, String academicTerm) {
        return baseMapper.selectTeacherClassesByTerm(teacherId, academicTerm);
    }

    @Override
    public Long countTeacherClasses(Integer teacherId) {
        return baseMapper.countTeacherClasses(teacherId);
    }

    @Override
    public int batchImport(List<Teacher> teachers) {
        // 批量导入前进行数据验证
        for (Teacher teacher : teachers) {
            if (teacher.getTeacherNo() == null || teacher.getName() == null) {
                return 0;
            }
        }
        return baseMapper.batchInsert(teachers);
    }

    @Override
    public Map<String, Object> getWorkloadStatistics(Integer teacherId) {
        Map<String, Object> statistics = new HashMap<>();
        
        // 获取教师所教授的班级数量
        Long classCount = countTeacherClasses(teacherId);
        statistics.put("classCount", classCount);
        
        // 获取教师所教授的班级列表
        List<Class> classes = getTeacherClasses(teacherId);
        
        // 统计学生总数
        int totalStudents = 0;
        for (Class clazz : classes) {
            totalStudents += clazz.getStudentCount();
        }
        statistics.put("totalStudents", totalStudents);
        
        // 计算平均班级规模
        statistics.put("averageClassSize", classCount > 0 ? totalStudents / classCount : 0);
        
        return statistics;
    }

    @Override
    public List<Map<String, Object>> getInvigilationSchedule(Integer teacherId) {
        List<Map<String, Object>> schedule = new ArrayList<>();
        
        // 获取教师所教授的班级
        List<Class> classes = getTeacherClasses(teacherId);
        
        // 获取每个班级的考试安排
        for (Class clazz : classes) {
            Map<String, Object> examSchedule = new HashMap<>();
            examSchedule.put("className", clazz.getClassName());
            examSchedule.put("examTime", clazz.getExamTime());
            examSchedule.put("location", clazz.getExamLocation());
            examSchedule.put("studentCount", clazz.getStudentCount());
            schedule.add(examSchedule);
        }
        
        return schedule;
    }
} 
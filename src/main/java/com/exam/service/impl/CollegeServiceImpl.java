package com.exam.service.impl;

import com.exam.entity.College;
import com.exam.entity.Subject;
import com.exam.entity.Teacher;
import com.exam.entity.Student;
import com.exam.mapper.CollegeMapper;
import com.exam.service.CollegeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * 学院服务实现类
 */
@Service
public class CollegeServiceImpl implements CollegeService {

    @Autowired
    private CollegeMapper collegeMapper;

    @Override
    public int insert(College record) {
        return collegeMapper.insert(record);
    }

    @Override
    public int deleteById(Integer id) {
        return collegeMapper.deleteById(id);
    }

    @Override
    public int updateById(College record) {
        return collegeMapper.updateById(record);
    }

    @Override
    public College selectById(Integer id) {
        return collegeMapper.selectById(id);
    }

    @Override
    public List<College> selectAll() {
        return collegeMapper.selectAll();
    }

    @Override
    public List<College> selectPage(Integer pageNum, Integer pageSize) {
        int offset = (pageNum - 1) * pageSize;
        return collegeMapper.selectPage(offset, pageSize);
    }

    @Override
    public Long selectCount() {
        return collegeMapper.selectCount();
    }

    @Override
    public List<College> selectByCondition(Map<String, Object> condition) {
        return collegeMapper.selectByCondition(condition);
    }

    @Override
    public Long selectCountByCondition(Map<String, Object> condition) {
        return collegeMapper.selectCountByCondition(condition);
    }

    @Override
    public List<College> selectPageByCondition(Map<String, Object> condition, Integer pageNum, Integer pageSize) {
        int offset = (pageNum - 1) * pageSize;
        return collegeMapper.selectPageByCondition(condition, offset, pageSize);
    }

    @Override
    public College getByCollegeName(String collegeName) {
        return collegeMapper.selectByCollegeName(collegeName);
    }

    @Override
    public int updateDescription(Integer collegeId, String description) {
        return collegeMapper.updateDescription(collegeId, description);
    }

    @Override
    public List<Subject> getCollegeSubjects(Integer collegeId) {
        return collegeMapper.selectCollegeSubjects(collegeId);
    }

    @Override
    public Long countSubjects(Integer collegeId) {
        return collegeMapper.countSubjects(collegeId);
    }

    @Override
    public Long countStudents(Integer collegeId) {
        return collegeMapper.countStudents(collegeId);
    }

    @Override
    public Long countTeachers(Integer collegeId) {
        return collegeMapper.countTeachers(collegeId);
    }

    @Override
    @Transactional
    public int createCollege(College college, List<Integer> subjectIds) {
        // 插入学院记录
        int result = collegeMapper.insert(college);
        
        // 如果插入成功且有学科列表，则建立关联
        if (result > 0 && subjectIds != null && !subjectIds.isEmpty()) {
            result = collegeMapper.associateSubjects(college.getCollegeId(), subjectIds);
        }
        
        return result;
    }

    @Override
    @Transactional
    public int deleteCollege(Integer collegeId) {
        // 检查是否有关联的学科、教师和学生
        Long subjectCount = collegeMapper.countSubjects(collegeId);
        Long teacherCount = collegeMapper.countTeachers(collegeId);
        Long studentCount = collegeMapper.countStudents(collegeId);
        
        // 如果有关联数据，不能删除
        if (subjectCount > 0 || teacherCount > 0 || studentCount > 0) {
            return 0;
        }
        
        // 删除学院记录
        return collegeMapper.deleteById(collegeId);
    }

    @Override
    public Map<String, Object> exportStudentList(Integer collegeId) {
        Map<String, Object> exportData = new HashMap<>();
        
        // 获取学院信息
        College college = collegeMapper.selectById(collegeId);
        exportData.put("college", college);
        
        // 获取学生列表
        List<Student> students = collegeMapper.selectCollegeStudents(collegeId);
        exportData.put("students", students);
        
        // 获取教师列表
        List<Teacher> teachers = collegeMapper.selectCollegeTeachers(collegeId);
        exportData.put("teachers", teachers);
        
        // 获取学科列表
        List<Subject> subjects = collegeMapper.selectCollegeSubjects(collegeId);
        exportData.put("subjects", subjects);
        
        // 获取统计信息
        Map<String, Long> stats = new HashMap<>();
        stats.put("studentCount", collegeMapper.countStudents(collegeId));
        stats.put("teacherCount", collegeMapper.countTeachers(collegeId));
        stats.put("subjectCount", collegeMapper.countSubjects(collegeId));
        exportData.put("statistics", stats);
        
        return exportData;
    }
} 
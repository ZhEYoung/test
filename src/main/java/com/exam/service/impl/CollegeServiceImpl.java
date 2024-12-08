package com.exam.service.impl;

import com.exam.entity.College;
import com.exam.entity.Subject;
import com.exam.entity.Teacher;
import com.exam.entity.Student;
import com.exam.mapper.CollegeMapper;
import com.exam.mapper.SubjectMapper;
import com.exam.mapper.TeacherMapper;
import com.exam.mapper.StudentMapper;
import com.exam.mapper.ExamMapper;
import com.exam.service.CollegeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

/**
 * 学院服务实现类
 */
@Service
@Transactional
public class CollegeServiceImpl extends BaseServiceImpl<College, CollegeMapper> implements CollegeService {

    @Autowired
    private SubjectMapper subjectMapper;
    
    @Autowired
    private TeacherMapper teacherMapper;
    
    @Autowired
    private StudentMapper studentMapper;
    
    @Autowired
    private ExamMapper examMapper;
    @Autowired
    private CollegeMapper collegeMapper;

    @Override
    public College getByCollegeName(String collegeName) {
        return baseMapper.selectByCollegeName(collegeName);
    }

    @Override
    public int updateDescription(Integer collegeId, String description) {
        return baseMapper.updateDescription(collegeId, description);
    }

    @Override
    public List<Subject> getCollegeSubjects(Integer collegeId) {
        return subjectMapper.selectByCollegeId(collegeId);
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
    public int createCollege(College college, List<Integer> subjectIds) {
        // 插入学院记录
        int result = baseMapper.insert(college);
        if (result == 0) {
            return 0;
        }
        
        // 建立学院和学科的关联
        if (!subjectIds.isEmpty()) {
            baseMapper.updateSubjectsCollege(college.getCollegeId(), subjectIds);
        }
        
        return result;
    }

    @Override
    public int deleteCollege(Integer collegeId) {
        // 检查是否有关联的学科
        Long subjectCount = countSubjects(collegeId);
        if (subjectCount > 0) {
            return 0; // 有关联的学科，不能删除
        }
        
        // 检查是否有关联的教师
        Long teacherCount = countTeachers(collegeId);
        if (teacherCount > 0) {
            return 0; // 有关联的教师，不能删除
        }
        
        // 检查是否有关联的学生
        Long studentCount = countStudents(collegeId);
        if (studentCount > 0) {
            return 0; // 有关联的学生，不能删除
        }
        
        // 删除学院记录
        return baseMapper.deleteById(collegeId);
    }

    @Override
    public Map<String, Object> exportStudentList(Integer collegeId) {
        Map<String, Object> data = new HashMap<>();
        
        // 获取学院信息
        College college = baseMapper.selectById(collegeId);
        if (college == null) {
            return data;
        }
        data.put("college", college);
        
        // 获取学生列表
        List<Student> students = studentMapper.selectByCollegeId(collegeId);;
        data.put("students", students);
        
        // 统计学生性别分布
        List<Map<String, Object>> genderStats = studentMapper.countByGender(collegeId);
        data.put("genderStats", genderStats);
        
        // 统计学生年级分布
        List<Map<String, Object>> gradeStats = studentMapper.countByGrade(collegeId);
        data.put("gradeStats", gradeStats);
        
        return data;
    }
} 
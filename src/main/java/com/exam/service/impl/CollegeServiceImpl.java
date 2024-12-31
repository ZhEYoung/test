package com.exam.service.impl;

import com.exam.entity.College;
import com.exam.entity.Subject;
import com.exam.entity.Teacher;
import com.exam.entity.Student;
import com.exam.mapper.CollegeMapper;
import com.exam.service.CollegeService;
import com.exam.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
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

    @Autowired
    private StudentService studentService;

    @Override
    public int insert(College record) {
        // 验证学院名称
        if (record == null || record.getCollegeName() == null || 
            record.getCollegeName().trim().isEmpty() || 
            record.getCollegeName().length() > 50) {
            System.out.println("学院名称无效");
            return 0;
        }

        // 检查学院名称是否已存在
        College existingCollege = collegeMapper.selectByCollegeName(record.getCollegeName());
        if (existingCollege != null) {
            System.out.println("学院名称已存在");
            return 0;
        }

        // 设置默认描述
        if (record.getDescription() == null) {
            record.setDescription("");
        }

        return collegeMapper.insert(record);
    }

    @Override
    @Transactional
    public int deleteById(Integer id) {
        // 学院不允许删除
        return 0;
    }

    @Override
    public int updateById(College record) {
        return collegeMapper.updateById(record);
    }

    @Override
    public College getById(Integer id) {
        return collegeMapper.selectById(id);
    }

    @Override
    @Cacheable(value = "college_list")
    public List<College> getAll() {
        return collegeMapper.selectAll();
    }

    @Override
    public List<College> getPage(Integer pageNum, Integer pageSize) {
        int offset = (pageNum - 1) * pageSize;
        return collegeMapper.selectPage(offset, pageSize);
    }

    @Override
    public Long getCount() {
        return collegeMapper.selectCount();
    }

    @Override
    public List<College> getByCondition(Map<String, Object> condition) {
        return collegeMapper.selectByCondition(condition);
    }

    @Override
    public Long getCountByCondition(Map<String, Object> condition) {
        return collegeMapper.selectCountByCondition(condition);
    }

    @Override
    public List<College> getPageByCondition(Map<String, Object> condition, Integer pageNum, Integer pageSize) {
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
        // 学院不允许删除
        return 0;
    }

    @Override
    public Map<String, Object> exportStudentList(Integer collegeId) {
        Map<String, Object> result = new HashMap<>();
        
        // 获取学院信息
        College college = collegeMapper.selectById(collegeId);
        if (college == null) {
            return result;
        }
        
        // 获取学生列表
        List<Student> students = studentService.getByCollegeId(collegeId);
        
        // 组装数据
        result.put("college", college);
        result.put("students", students);
        result.put("totalCount", students.size());
        
        return result;
    }
} 
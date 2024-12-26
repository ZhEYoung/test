package com.exam.service.impl;

import com.exam.entity.Student;
import com.exam.entity.StudentClass;
import com.exam.entity.StudentScore;
import com.exam.mapper.StudentMapper;
import com.exam.mapper.UserMapper;
import com.exam.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

/**
 * 学生服务实现类
 */
@Service
@Transactional
public class StudentServiceImpl implements StudentService {

    @Autowired
    private StudentMapper studentMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    @CacheEvict(value = {"student", "student_list"}, allEntries = true)
    public int insert(Student record) {
        // 验证学生数据
        if (!validateStudent(record)) {
            return 0;
        }
        return studentMapper.insert(record);
    }

    /**
     * 验证学生数据
     * @param student 学生信息
     * @return true 如果数据有效，false 如果数据无效
     */
    private boolean validateStudent(Student student) {
        if (student == null) {
            return false;
        }

        // 验证名称
        if (student.getName() == null || student.getName().trim().isEmpty()) {
            return false;
        }

        // 验证用户ID
        if (student.getUserId() == null || student.getUserId() <= 0) {
            return false;
        }

        // 验证学院ID
        if (student.getCollegeId() == null || student.getCollegeId() <= 0) {
            return false;
        }

        // 验证年级格式（假设年级应该是4位数字）
        if (student.getGrade() == null || !student.getGrade().matches("\\d{4}")) {
            return false;
        }

        return true;
    }

    @Override
    @CacheEvict(value = {"student", "student_list"}, allEntries = true)
    public int deleteById(Integer id) {
        // 1. 获取要删除的学生信息
        Student student = studentMapper.selectById(id);
        if (student == null) {
            return 0;
        }

        // 2. 禁用关联的用户账号
        userMapper.updateStatus(student.getUserId(), false);

        // 3. 删除学生记录
        return studentMapper.deleteById(id);
    }

    @Override
    @CacheEvict(value = {"student", "student_list","student_by_condition","student_count_by_condition"}, allEntries = true)
    public int updateById(Student record) {
        return studentMapper.updateById(record);
    }

    @Override
    @Cacheable(value = "student", key = "#id")
    public Student getById(Integer id) {
        return studentMapper.selectById(id);
    }

    @Override
    @Cacheable(value = "student_list")
    public List<Student> getAll() {
        return studentMapper.selectAll();
    }

    @Override
    @Cacheable(value = "student_page", key = "#pageNum + '_' + #pageSize")
    public List<Student> getPage(Integer pageNum, Integer pageSize) {
        int offset = (pageNum - 1) * pageSize;
        return studentMapper.selectPage(offset, pageSize);
    }

    @Override
    @Cacheable(value = "student_count")
    public Long getCount() {
        return studentMapper.selectCount();
    }

    @Override
    @Cacheable(value = "student_by_condition", 
               key = "'userId_'+#condition.get('userId')+'_name_'+#condition.get('name')+'_grade_'+#condition.get('grade')+'_college_'+#condition.get('collegeId')")
    public List<Student> getByCondition(Map<String, Object> condition) {
        return studentMapper.selectByCondition(new Student() {{
            if (condition.get("userId") != null) setUserId((Integer) condition.get("userId"));
            if (condition.get("name") != null) setName((String) condition.get("name"));
            if (condition.get("grade") != null) setGrade((String) condition.get("grade"));
            if (condition.get("collegeId") != null) setCollegeId((Integer) condition.get("collegeId"));
        }});
    }

    @Override
    @Cacheable(value = "student_count_by_condition",
               key = "'condition_'+#condition.hashCode()")
    public Long getCountByCondition(Map<String, Object> condition) {
        return studentMapper.selectCountByCondition(condition);
    }

    @Override
    public List<Student> getPageByCondition(Map<String, Object> condition, Integer pageNum, Integer pageSize) {
        int offset = (pageNum - 1) * pageSize;
        return studentMapper.selectPageByCondition(condition, offset, pageSize);
    }

    @Override
    @Cacheable(value = "student_by_userid", key = "#userId")
    public Student getByUserId(Integer userId) {
        return studentMapper.selectByUserId(userId);
    }

    @Override
    @Cacheable(value = "student_by_college", key = "#collegeId")
    public List<Student> getByCollegeId(Integer collegeId) {
        return studentMapper.selectByCollegeId(collegeId);
    }

    @Override
    @Cacheable(value = "student_by_grade", key = "#grade")
    public List<Student> getByGrade(String grade) {
        return studentMapper.selectByGrade(grade);
    }

    @Override
    @Cacheable(value = "student_by_name", key = "#name")
    public List<Student> getByName(String name) {
        return studentMapper.selectByName(name);
    }

    @Override
    @CacheEvict(value = {"student", "student_list", "student_by_condition"}, allEntries = true)
    public int updateOther(Integer studentId, String other) {
        return studentMapper.updateOther(studentId, other);
    }

    @Override
    @Cacheable(value = "student_classes", key = "#studentId")
    public List<StudentClass> getStudentClasses(Integer studentId) {
        return studentMapper.selectStudentClasses(studentId);
    }

    @Override
    public int joinClass(Integer studentId, Integer classId) {
        return studentMapper.joinClass(studentId, classId);
    }

    @Override
    @CacheEvict(value = {"student", "student_list", "student_classes"}, allEntries = true)
    public int leaveClass(Integer studentId, Integer classId, Date leftTime) {
        return studentMapper.leaveClass(studentId, classId, leftTime);
    }

    @Override
    @Cacheable(value = "student_scores", key = "'exam_'+#examId+'_student_'+#studentId")
    public StudentScore getScore(Integer studentId, Integer examId) {
        return studentMapper.selectScore(studentId, examId);
    }

    @Override
    @Cacheable(value = "student_all_scores", key = "#studentId")
    public List<StudentScore> getAllScores(Integer studentId) {
        return studentMapper.selectAllScores(studentId);
    }

    @Override
    @CacheEvict(value = {"student_scores", "student_all_scores", "student_score_statistics"}, 
                key = "'exam_'+#examId+'_student_'+#studentId")
    public int insertScore(Integer studentId, Integer examId, Double score) {
        return studentMapper.insertScore(studentId, examId, score);
    }

    @Override
    @CacheEvict(value = {"student_scores", "student_all_scores", "student_score_statistics"}, 
                key = "'exam_'+#examId+'_student_'+#studentId")
    public int updateScore(Integer studentId, Integer examId, Double score) {
        return studentMapper.updateScore(studentId, examId, score);
    }

    @Override
    @CacheEvict(value = {"student", "student_list", "student_by_condition", "student_count"}, 
                allEntries = true)
    public int batchImport(List<Student> students) {
        // 批量导入前进行数据验证
        for (Student student : students) {
            if (student.getStudentId() == null || student.getName() == null) {
                return 0;
            }
        }
        return studentMapper.batchInsert(students);
    }

    @Override
    @Cacheable(value = "student_count_by_class", key = "#classId")
    public Long countStudentsByClass(Integer classId) {
        Map<String, Object> condition = new HashMap<>();
        condition.put("classId", classId);
        return studentMapper.selectCountByCondition(condition);
    }

    @Override
    @Cacheable(value = "student_score_statistics", key = "#studentId")
    public Map<String, Object> getScoreStatistics(Integer studentId) {
        Map<String, Object> statistics = new HashMap<>();
        List<StudentScore> scores = getAllScores(studentId);
        
        if (scores.isEmpty()) {
            return statistics;
        }

        // 计算平均分
        double totalScore = 0;
        double maxScore = Double.MIN_VALUE;
        double minScore = Double.MAX_VALUE;
        
        for (StudentScore score : scores) {
            double currentScore = score.getScore().doubleValue();
            totalScore += currentScore;
            maxScore = Math.max(maxScore, currentScore);
            minScore = Math.min(minScore, currentScore);
        }
        
        statistics.put("averageScore", totalScore / scores.size());
        statistics.put("maxScore", maxScore);
        statistics.put("minScore", minScore);
        statistics.put("examCount", scores.size());
        
        return statistics;
    }
} 
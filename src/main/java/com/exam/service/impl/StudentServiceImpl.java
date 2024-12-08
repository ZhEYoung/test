package com.exam.service.impl;

import com.exam.entity.Student;
import com.exam.entity.StudentClass;
import com.exam.entity.StudentScore;
import com.exam.mapper.StudentMapper;
import com.exam.service.StudentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

/**
 * 学生服务实现类
 */
@Service
@Transactional
public class StudentServiceImpl extends BaseServiceImpl<Student, StudentMapper> implements StudentService {

    @Override
    public Student getByUserId(Integer userId) {
        return baseMapper.selectByUserId(userId);
    }

    @Override
    public List<Student> getByCollegeId(Integer collegeId) {
        return baseMapper.selectByCollegeId(collegeId);
    }

    @Override
    public List<Student> getByGrade(String grade) {
        return baseMapper.selectByGrade(grade);
    }

    @Override
    public List<Student> getByName(String name) {
        return baseMapper.selectByName(name);
    }

    @Override
    public int updateOther(Integer studentId, String other) {
        return baseMapper.updateOther(studentId, other);
    }

    @Override
    public List<StudentClass> getStudentClasses(Integer studentId) {
        return baseMapper.selectStudentClasses(studentId);
    }

    @Override
    public int joinClass(Integer studentId, Integer classId) {
        return baseMapper.joinClass(studentId, classId);
    }

    @Override
    public int leaveClass(Integer studentId, Integer classId, Date leftTime) {
        return baseMapper.leaveClass(studentId, classId, leftTime);
    }

    @Override
    public StudentScore getScore(Integer studentId, Integer examId) {
        return baseMapper.selectScore(studentId, examId);
    }

    @Override
    public List<StudentScore> getAllScores(Integer studentId) {
        return baseMapper.selectAllScores(studentId);
    }

    @Override
    public int insertScore(Integer studentId, Integer examId, Double score) {
        return baseMapper.insertScore(studentId, examId, score);
    }

    @Override
    public int updateScore(Integer studentId, Integer examId, Double score) {
        return baseMapper.updateScore(studentId, examId, score);
    }

    @Override
    public int batchImport(List<Student> students) {
        // 批量导入前进行数据验证
        for (Student student : students) {
            if (student.getStudentNo() == null || student.getName() == null) {
                return 0;
            }
        }
        return baseMapper.batchInsert(students);
    }

    @Override
    public Long countStudentsByClass(Integer classId) {
        Map<String, Object> condition = new HashMap<>();
        condition.put("classId", classId);
        return baseMapper.selectCountByCondition(condition);
    }

    @Override
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
            double currentScore = score.getScore();
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
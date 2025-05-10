package com.exam.service.impl;

import com.exam.entity.Exam;
import com.exam.entity.StudentScore;
import com.exam.mapper.StudentScoreMapper;
import com.exam.mapper.ExamMapper;
import com.exam.mapper.StudentMapper;
import com.exam.service.StudentScoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.math.BigDecimal;

/**
 * 学生成绩服务实现类
 */
@Service
@Transactional
public class StudentScoreServiceImpl implements StudentScoreService {

    @Autowired
    private StudentScoreMapper studentScoreMapper;
    
    @Autowired
    private ExamMapper examMapper;
    
    @Autowired
    private StudentMapper studentMapper;

    @Override
    public int insert(StudentScore record) {
        // 验证必要字段
        if (record.getStudentId() == null || record.getExamId() == null || record.getScore() == null) {
            throw new IllegalArgumentException("学生ID、考试ID和成绩不能为空");
        }
        
        // 验证学生和考试是否存在
        if (studentMapper.selectById(record.getStudentId()) == null) {
            throw new IllegalArgumentException("学生ID不存在: " + record.getStudentId());
        }
        if (examMapper.selectById(record.getExamId()) == null) {
            throw new IllegalArgumentException("考试ID不存在: " + record.getExamId());
        }
        
        return studentScoreMapper.insert(record);
    }

    @Override
    public int deleteById(Integer id) {
        return studentScoreMapper.deleteById(id);
    }

    @Override
    public int updateById(StudentScore record) {
        return studentScoreMapper.updateById(record);
    }

    @Override
    public StudentScore getById(Integer id) {
        return studentScoreMapper.selectById(id);
    }

    @Override
    public List<StudentScore> getAll() {
        return studentScoreMapper.selectAll();
    }

    @Override
    public List<StudentScore> getPage(Integer pageNum, Integer pageSize) {
        int offset = (pageNum - 1) * pageSize;
        return studentScoreMapper.selectPage(offset, pageSize);
    }

    @Override
    public Long getCount() {
        return studentScoreMapper.selectCount();
    }

    @Override
    public List<StudentScore> getByCondition(Map<String, Object> condition) {
        return studentScoreMapper.selectByCondition(condition);
    }

    @Override
    public Long getCountByCondition(Map<String, Object> condition) {
        return studentScoreMapper.selectCountByCondition(condition);
    }

    @Override
    public List<StudentScore> getPageByCondition(Map<String, Object> condition, Integer pageNum, Integer pageSize) {
        int offset = (pageNum - 1) * pageSize;
        return studentScoreMapper.selectPageByCondition(condition, offset, pageSize);
    }

    @Override
    public List<StudentScore> getByStudentId(Integer studentId) {
        return studentScoreMapper.selectByStudentId(studentId);
    }

    @Override
    public List<StudentScore> getByExamId(Integer examId) {
        return studentScoreMapper.selectByExamId(examId);
    }

    @Override
    public StudentScore getByExamAndStudent(Integer examId, Integer studentId) {
        return studentScoreMapper.selectByExamAndStudent(examId, studentId);
    }

    @Override
    public List<StudentScore> getByScoreRange(Integer examId, BigDecimal minScore, BigDecimal maxScore) {
        return studentScoreMapper.selectByScoreRange(examId, minScore, maxScore);
    }

    @Override
    public List<StudentScore> getExamScoreStats(Integer examId) {
        return studentScoreMapper.selectExamScoreStats(examId);
    }

    @Override
    public int batchInsertScores(List<StudentScore> scores) {
        return studentScoreMapper.batchInsertScores(scores);
    }

    @Override
    public int batchUpdateScores(List<StudentScore> scores) {
        return studentScoreMapper.batchUpdateScores(scores);
    }

    @Override
    public Integer getStudentRank(Integer examId, Integer studentId) {
        return studentScoreMapper.selectStudentRank(examId, studentId);
    }

    @Override
    public List<Map<String, Object>> getClassRanking(Integer classId, Integer examId) {
        return studentScoreMapper.selectClassRanking(classId, examId);
    }

    @Override
    public List<Map<String, Object>> getGradeRanking(String grade, Integer examId) {
        return studentScoreMapper.selectGradeRanking(grade, examId);
    }

    @Override
    public List<Map<String, Object>> getScoreTrend(Integer studentId, Integer subjectId, 
                                                  Date startTime, Date endTime) {
        return studentScoreMapper.selectScoreTrend(studentId, subjectId, startTime, endTime);
    }

    @Override
    public List<Map<String, Object>> getSubjectAverages(Integer studentId) {
        return studentScoreMapper.selectSubjectAverages(studentId);
    }

    @Override
    public List<Map<String, Object>> getScoreDistribution(Integer examId, Integer classId) {
        return studentScoreMapper.selectScoreDistribution(examId, classId);
    }

    @Override
    public List<Map<String, Object>> getTopStudents(Integer examId, Integer limit) {
        return studentScoreMapper.selectTopStudents(examId, limit);
    }

    @Override
    public List<Map<String, Object>> getRetakeStudents(Integer examId, BigDecimal passScore) {
        return studentScoreMapper.selectRetakeStudents(examId, passScore);
    }

    @Override
    public List<Map<String, Object>> analyzeScoreImprovement(Integer studentId, Integer subjectId) {
        return studentScoreMapper.analyzeScoreImprovement(studentId, subjectId);
    }

    @Override
    public Map<String, Object> exportScoreReport(Integer examId, Integer studentId) {
        Map<String, Object> report = new HashMap<>();
        
        // 获取考试信息
        Exam exam = examMapper.selectById(examId);
        report.put("exam", examMapper.selectById(examId));
        
        // 获取学生信息
        report.put("student", studentMapper.selectById(studentId));
        
        // 获取成绩信息
        StudentScore score = getByExamAndStudent(examId, studentId);
        report.put("score", score);
        
        if (score != null) {
            // 获取排名信息
            report.put("rank", getStudentRank(examId, studentId));
            
            // 获取成绩分布
            report.put("distribution", getScoreDistribution(examId, null));
            
            // 获取历史成绩趋势
            report.put("trend", getScoreTrend(studentId, exam.getSubjectId(), null, null));
            
            // 获取进步情况分析
            report.put("improvement", analyzeScoreImprovement(studentId, exam.getSubjectId()));
        }
        return report;
    }

    @Override
    public int importScores(List<StudentScore> scores) {
        // 批量导入前进行数据验证
        for (StudentScore score : scores) {
            if (score.getExamId() == null || score.getStudentId() == null || score.getScore() == null) {
                return 0;
            }
            // 检查考试和学生是否存在
            if (examMapper.selectById(score.getExamId()) == null || 
                studentMapper.selectById(score.getStudentId()) == null) {
                return 0;
            }
        }
        
        return batchInsertScores(scores);
    }
} 
package com.exam.service.impl;

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
public class StudentScoreServiceImpl extends BaseServiceImpl<StudentScore, StudentScoreMapper> implements StudentScoreService {

    @Autowired
    private ExamMapper examMapper;
    
    @Autowired
    private StudentMapper studentMapper;

    @Override
    public List<StudentScore> getByStudentId(Integer studentId) {
        return baseMapper.selectByStudentId(studentId);
    }

    @Override
    public List<StudentScore> getByExamId(Integer examId) {
        return baseMapper.selectByExamId(examId);
    }

    @Override
    public StudentScore getByExamAndStudent(Integer examId, Integer studentId) {
        return baseMapper.selectByExamAndStudent(examId, studentId);
    }

    @Override
    public List<StudentScore> getByScoreRange(Integer examId, BigDecimal minScore, BigDecimal maxScore) {
        return baseMapper.selectByScoreRange(examId, minScore, maxScore);
    }

    @Override
    public List<StudentScore> getExamScoreStats(Integer examId) {
        return baseMapper.selectExamScoreStats(examId);
    }

    @Override
    public int batchInsertScores(List<StudentScore> scores) {
        return baseMapper.batchInsertScores(scores);
    }

    @Override
    public int batchUpdateScores(List<StudentScore> scores) {
        return baseMapper.batchUpdateScores(scores);
    }

    @Override
    public Integer getStudentRank(Integer examId, Integer studentId) {
        return baseMapper.selectStudentRank(examId, studentId);
    }

    @Override
    public List<Map<String, Object>> getClassRanking(Integer classId, Integer examId) {
        return baseMapper.selectClassRanking(classId, examId);
    }

    @Override
    public List<Map<String, Object>> getGradeRanking(String grade, Integer examId) {
        return baseMapper.selectGradeRanking(grade, examId);
    }

    @Override
    public List<Map<String, Object>> getScoreTrend(Integer studentId, Integer subjectId, 
                                                  Date startTime, Date endTime) {
        return baseMapper.selectScoreTrend(studentId, subjectId, startTime, endTime);
    }

    @Override
    public List<Map<String, Object>> getSubjectAverages(Integer studentId) {
        return baseMapper.selectSubjectAverages(studentId);
    }

    @Override
    public List<Map<String, Object>> getScoreDistribution(Integer examId, Integer classId) {
        return baseMapper.selectScoreDistribution(examId, classId);
    }

    @Override
    public List<Map<String, Object>> getTopStudents(Integer examId, Integer limit) {
        return baseMapper.selectTopStudents(examId, limit);
    }

    @Override
    public List<Map<String, Object>> getRetakeStudents(Integer examId, BigDecimal passScore) {
        return baseMapper.selectRetakeStudents(examId, passScore);
    }

    @Override
    public List<Map<String, Object>> analyzeScoreImprovement(Integer studentId, Integer subjectId) {
        return baseMapper.analyzeScoreImprovement(studentId, subjectId);
    }

    @Override
    public Map<String, Object> getScoreSummary(Integer studentId, String academicTerm) {
        return baseMapper.selectScoreSummary(studentId, academicTerm);
    }

    @Override
    public Map<String, Object> exportScoreReport(Integer examId, Integer studentId) {
        Map<String, Object> report = new HashMap<>();
        
        // 获取考试信息
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
            report.put("trend", getScoreTrend(studentId, score.getSubjectId(), null, null));
            
            // 获取进步情况分析
            report.put("improvement", analyzeScoreImprovement(studentId, score.getSubjectId()));
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
package com.exam.service.impl;

import com.exam.entity.StudentQuestionScore;
import com.exam.mapper.StudentQuestionScoreMapper;
import com.exam.mapper.ExamMapper;
import com.exam.mapper.QuestionMapper;
import com.exam.service.StudentQuestionScoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.math.BigDecimal;

/**
 * 学生题目得分服务实现类
 */
@Service
@Transactional
public class StudentQuestionScoreServiceImpl extends BaseServiceImpl<StudentQuestionScore, StudentQuestionScoreMapper> implements StudentQuestionScoreService {

    @Autowired
    private ExamMapper examMapper;
    
    @Autowired
    private QuestionMapper questionMapper;

    @Override
    public List<StudentQuestionScore> getByScoreId(Integer scoreId) {
        return baseMapper.selectByScoreId(scoreId);
    }

    @Override
    public List<StudentQuestionScore> getByExamAndStudent(Integer examId, Integer studentId) {
        return baseMapper.selectByExamAndStudent(examId, studentId);
    }

    @Override
    public List<StudentQuestionScore> getByQuestionId(Integer questionId) {
        return baseMapper.selectByQuestionId(questionId);
    }

    @Override
    public int batchInsert(List<StudentQuestionScore> list) {
        return baseMapper.batchInsert(list);
    }

    @Override
    public int updateScoreAndStatus(Integer recordId, BigDecimal score, String status) {
        return baseMapper.updateScoreAndStatus(recordId, score, status);
    }

    @Override
    public int batchUpdateScore(List<Map<String, Object>> records) {
        return baseMapper.batchUpdateScore(records);
    }

    @Override
    public Map<String, Object> calculateQuestionCorrectRate(Integer questionId, Integer examId) {
        return baseMapper.calculateQuestionCorrectRate(questionId, examId);
    }

    @Override
    public List<Map<String, Object>> analyzeScoreDistribution(Integer questionId, Integer examId) {
        return baseMapper.analyzeScoreDistribution(questionId, examId);
    }

    @Override
    public List<Map<String, Object>> analyzeAnswerTimeDistribution(Integer questionId, Integer examId) {
        return baseMapper.analyzeAnswerTimeDistribution(questionId, examId);
    }

    @Override
    public BigDecimal calculateAverageScore(Integer questionId, Integer examId) {
        return baseMapper.calculateAverageScore(questionId, examId);
    }

    @Override
    public List<Map<String, Object>> getDifficultQuestions(Integer examId, Integer limit) {
        return baseMapper.selectDifficultQuestions(examId, limit);
    }

    @Override
    public List<Map<String, Object>> getEasyQuestions(Integer examId, Integer limit) {
        return baseMapper.selectEasyQuestions(examId, limit);
    }

    @Override
    public List<Map<String, Object>> analyzeAnswerPattern(Integer studentId, Integer examId) {
        return baseMapper.analyzeAnswerPattern(studentId, examId);
    }

    @Override
    public List<Map<String, Object>> analyzeScoreByQuestionType(Integer studentId, Integer examId) {
        return baseMapper.analyzeScoreByQuestionType(studentId, examId);
    }

    @Override
    public List<StudentQuestionScore> getNeedManualGrading(Integer examId, Integer teacherId) {
        return baseMapper.selectNeedManualGrading(examId, teacherId);
    }

    @Override
    public int batchUpdateGradingStatus(List<Integer> recordIds, String status, Integer graderId) {
        return baseMapper.batchUpdateGradingStatus(recordIds, status, graderId);
    }

    @Override
    public Map<String, Object> countGradingProgress(Integer examId) {
        return baseMapper.countGradingProgress(examId);
    }

    @Override
    public Map<String, Object> exportQuestionScoreReport(Integer examId, Integer questionId) {
        Map<String, Object> report = new HashMap<>();
        
        // 获取考试信息
        report.put("exam", examMapper.selectById(examId));
        
        // 获取题目信息
        report.put("question", questionMapper.selectById(questionId));
        
        // 获取得分分布
        report.put("scoreDistribution", analyzeScoreDistribution(questionId, examId));
        
        // 获取答题时间分布
        report.put("timeDistribution", analyzeAnswerTimeDistribution(questionId, examId));
        
        // 获取正确率
        report.put("correctRate", calculateQuestionCorrectRate(questionId, examId));
        
        // 获取平均分
        report.put("averageScore", calculateAverageScore(questionId, examId));
        
        return report;
    }

    @Override
    public int importQuestionScores(List<StudentQuestionScore> scores) {
        // 批量导入前进行数据验证
        for (StudentQuestionScore score : scores) {
            if (score.getExamId() == null || score.getStudentId() == null || 
                score.getQuestionId() == null || score.getScore() == null) {
                return 0;
            }
            // 检查考试和题目是否存在
            if (examMapper.selectById(score.getExamId()) == null || 
                questionMapper.selectById(score.getQuestionId()) == null) {
                return 0;
            }
        }
        
        return batchInsert(scores);
    }
} 
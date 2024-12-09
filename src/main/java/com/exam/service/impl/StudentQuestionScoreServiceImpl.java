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
public class StudentQuestionScoreServiceImpl implements StudentQuestionScoreService {

    @Autowired
    private StudentQuestionScoreMapper studentQuestionScoreMapper;
    
    @Autowired
    private ExamMapper examMapper;
    
    @Autowired
    private QuestionMapper questionMapper;

    @Override
    public int insert(StudentQuestionScore record) {
        return studentQuestionScoreMapper.insert(record);
    }

    @Override
    public int deleteById(Integer id) {
        return studentQuestionScoreMapper.deleteById(id);
    }

    @Override
    public int updateById(StudentQuestionScore record) {
        return studentQuestionScoreMapper.updateById(record);
    }

    @Override
    public StudentQuestionScore selectById(Integer id) {
        return studentQuestionScoreMapper.selectById(id);
    }

    @Override
    public List<StudentQuestionScore> selectAll() {
        return studentQuestionScoreMapper.selectAll();
    }

    @Override
    public List<StudentQuestionScore> selectPage(Integer pageNum, Integer pageSize) {
        int offset = (pageNum - 1) * pageSize;
        return studentQuestionScoreMapper.selectPage(offset, pageSize);
    }

    @Override
    public Long selectCount() {
        return studentQuestionScoreMapper.selectCount();
    }

    @Override
    public List<StudentQuestionScore> selectByCondition(Map<String, Object> condition) {
        return studentQuestionScoreMapper.selectByCondition(condition);
    }

    @Override
    public Long selectCountByCondition(Map<String, Object> condition) {
        return studentQuestionScoreMapper.selectCountByCondition(condition);
    }

    @Override
    public List<StudentQuestionScore> selectPageByCondition(Map<String, Object> condition, Integer pageNum, Integer pageSize) {
        int offset = (pageNum - 1) * pageSize;
        return studentQuestionScoreMapper.selectPageByCondition(condition, offset, pageSize);
    }

    @Override
    public List<StudentQuestionScore> getByScoreId(Integer scoreId) {
        return studentQuestionScoreMapper.selectByScoreId(scoreId);
    }

    @Override
    public List<StudentQuestionScore> getByExamAndStudent(Integer examId, Integer studentId) {
        return studentQuestionScoreMapper.selectByExamAndStudent(examId, studentId);
    }

    @Override
    public List<StudentQuestionScore> getByQuestionId(Integer questionId) {
        return studentQuestionScoreMapper.selectByQuestionId(questionId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchInsert(List<StudentQuestionScore> list) {
        if (list == null || list.isEmpty()) {
            return 0;
        }

        // 数据验证
        for (StudentQuestionScore record : list) {
            // 必填字段验证
            if (record.getExamId() == null || record.getStudentId() == null || 
                record.getQuestionId() == null || record.getScoreId() == null) {
                throw new IllegalArgumentException("考试ID、学生ID、题目ID和成绩ID不能为空");
            }

            // 设置默认状态为未批改
            if (record.getStatus() == null) {
                record.setStatus(0);
            }

            // 如果分数为空，设置为0
            if (record.getScore() == null) {
                record.setScore(BigDecimal.ZERO);
            }
        }

        try {
            return studentQuestionScoreMapper.batchInsert(list);
        } catch (Exception e) {
            throw new RuntimeException("批量插入题目得分记录失败", e);
        }
    }

    @Override
    public int updateScoreAndStatus(Integer recordId, BigDecimal score, String status) {
        return studentQuestionScoreMapper.updateScoreAndStatus(recordId, score, status);
    }

    @Override
    public int batchUpdateScore(List<Map<String, Object>> records) {
        return studentQuestionScoreMapper.batchUpdateScore(records);
    }

    @Override
    public Map<String, Object> calculateQuestionCorrectRate(Integer questionId, Integer examId) {
        return studentQuestionScoreMapper.calculateQuestionCorrectRate(questionId, examId);
    }

    @Override
    public List<Map<String, Object>> analyzeScoreDistribution(Integer questionId, Integer examId) {
        return studentQuestionScoreMapper.analyzeScoreDistribution(questionId, examId);
    }

    @Override
    public List<Map<String, Object>> analyzeAnswerTimeDistribution(Integer questionId, Integer examId) {
        return studentQuestionScoreMapper.analyzeAnswerTimeDistribution(questionId, examId);
    }

    @Override
    public BigDecimal calculateAverageScore(Integer questionId, Integer examId) {
        return studentQuestionScoreMapper.calculateAverageScore(questionId, examId);
    }

    @Override
    public List<Map<String, Object>> getDifficultQuestions(Integer examId, Integer limit) {
        return studentQuestionScoreMapper.selectDifficultQuestions(examId, limit);
    }

    @Override
    public List<Map<String, Object>> getEasyQuestions(Integer examId, Integer limit) {
        return studentQuestionScoreMapper.selectEasyQuestions(examId, limit);
    }

    @Override
    public List<Map<String, Object>> analyzeAnswerPattern(Integer studentId, Integer examId) {
        return studentQuestionScoreMapper.analyzeAnswerPattern(studentId, examId);
    }

    @Override
    public List<Map<String, Object>> analyzeScoreByQuestionType(Integer studentId, Integer examId) {
        return studentQuestionScoreMapper.analyzeScoreByQuestionType(studentId, examId);
    }

    @Override
    public List<StudentQuestionScore> getNeedManualGrading(Integer examId, Integer teacherId) {
        return studentQuestionScoreMapper.selectNeedManualGrading(examId, teacherId);
    }

    @Override
    public int batchUpdateGradingStatus(List<Integer> recordIds, String status, Integer graderId) {
        return studentQuestionScoreMapper.batchUpdateGradingStatus(recordIds, status, graderId);
    }

    @Override
    public Map<String, Object> countGradingProgress(Integer examId) {
        return studentQuestionScoreMapper.countGradingProgress(examId);
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

    @Override
    public int insertStudentQuestionScore(StudentQuestionScore record) {
        // 设置初始状态为未批改
        if (record.getStatus() == null) {
            record.setStatus(0);
        }
        return studentQuestionScoreMapper.insert(record);
    }
} 

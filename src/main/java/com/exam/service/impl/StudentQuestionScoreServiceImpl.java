package com.exam.service.impl;

import com.exam.entity.ExamPaperQuestion;
import com.exam.entity.Question;
import com.exam.entity.StudentQuestionScore;
import com.exam.mapper.StudentQuestionScoreMapper;
import com.exam.service.ExamPaperQuestionService;
import com.exam.service.StudentQuestionScoreService;
import com.exam.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.math.BigDecimal;
import java.util.HashMap;

/**
 * 学生题目得分服务实现类
 */
@Service
public class StudentQuestionScoreServiceImpl implements StudentQuestionScoreService {

    @Autowired
    private StudentQuestionScoreMapper studentQuestionScoreMapper;

    @Autowired
    private QuestionService questionService;

    @Autowired
    private ExamPaperQuestionService examPaperQuestionService;


    @Override
    @Transactional
    public int insert(StudentQuestionScore record) {
        return studentQuestionScoreMapper.insert(record);
    }

    @Override
    @Transactional
    public int deleteById(Integer id) {
        return studentQuestionScoreMapper.deleteById(id);
    }

    @Override
    @Transactional
    public int updateById(StudentQuestionScore record) {
        return studentQuestionScoreMapper.update(record);
    }

    @Override
    public StudentQuestionScore getById(Integer id) {
        return studentQuestionScoreMapper.selectById(id);
    }

    @Override
    public List<StudentQuestionScore> getAll() {
        return studentQuestionScoreMapper.selectAll();
    }

    @Override
    public List<StudentQuestionScore> getPage(Integer pageNum, Integer pageSize) {
        int offset = (pageNum - 1) * pageSize;
        return studentQuestionScoreMapper.selectPage(offset, pageSize);
    }

    @Override
    public Long getCount() {
        return studentQuestionScoreMapper.countTotal().longValue();
    }

    @Override
    public List<StudentQuestionScore> getByCondition(Map<String, Object> condition) {
        return studentQuestionScoreMapper.selectByCondition(condition);
    }

    @Override
    public Long getCountByCondition(Map<String, Object> condition) {
        return studentQuestionScoreMapper.selectCountByCondition(condition);
    }

    @Override
    public List<StudentQuestionScore> getPageByCondition(Map<String, Object> condition, Integer pageNum, Integer pageSize) {
        int offset = (pageNum - 1) * pageSize;
        return studentQuestionScoreMapper.selectPageByCondition(condition, offset, pageSize);
    }

    @Override
    @Transactional
    public int insertStudentQuestionScore(StudentQuestionScore record) {
        return studentQuestionScoreMapper.insert(record);
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
    @Transactional
    public int batchInsert(List<StudentQuestionScore> list) {
        return studentQuestionScoreMapper.batchInsert(list);
    }

    @Override
    @Transactional
    public int updateScoreAndStatus(Integer recordId, BigDecimal score, String status) {
        return studentQuestionScoreMapper.updateScoreAndStatus(recordId, score, status);
    }

    @Override
    @Transactional
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
    public List<StudentQuestionScore> getNeedManualGrading(Integer examId, Integer teacherId) {
        return studentQuestionScoreMapper.selectNeedManualGrading(examId, teacherId);
    }

    @Override
    @Transactional
    public int batchUpdateGradingStatus(List<Integer> recordIds, String status, Integer graderId) {
        return studentQuestionScoreMapper.batchUpdateGradingStatus(recordIds, status, graderId);
    }


    @Override
    public Map<String, Object> exportQuestionScoreReport(Integer examId, Integer questionId) {
        Map<String, Object> report = studentQuestionScoreMapper.calculateQuestionCorrectRate(questionId, examId);
        report.put("scoreDistribution", studentQuestionScoreMapper.analyzeScoreDistribution(questionId, examId));
        report.put("timeDistribution", studentQuestionScoreMapper.analyzeAnswerTimeDistribution(questionId, examId));
        report.put("averageScore", studentQuestionScoreMapper.calculateAverageScore(questionId, examId));
        return report;
    }

    @Override
    @Transactional
    public int importQuestionScores(List<StudentQuestionScore> scores) {
        return studentQuestionScoreMapper.batchInsert(scores);
    }

    @Override
    public Map<String, Object> getSubjectiveAnswer(Integer examId, Integer questionId, Integer studentId) {
        // 获取学生答案记录
        StudentQuestionScore questionScore = studentQuestionScoreMapper.selectByExamQuestionAndStudent(
            examId, questionId, studentId);
        
        if (questionScore == null) {
            return null;
        }

        // 获取题目信息
        Question question = questionService.getById(questionId);
        if (question == null) {
            return null;
        }

        // 检查是否是主观题（填空题或简答题）
        if (question.getType() != 3 && question.getType() != 4) {
            return null;
        }

        // 获取题目在试卷中的分数
        ExamPaperQuestion examPaperQuestion = examPaperQuestionService.getByExamAndQuestionId(
            examId, questionId);
        if (examPaperQuestion == null) {
            return null;
        }

        // 构建返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("questionId", questionId);
        result.put("studentId", studentId);
        result.put("examId", examId);
        result.put("questionContent", question.getContent());
        result.put("questionType", question.getType());
        result.put("studentAnswer", questionScore.getAnswer());
        result.put("standardAnswer", question.getAnswer());
        result.put("score", questionScore.getScore());
        result.put("fullScore", examPaperQuestion.getQuestionScore());
        result.put("status", questionScore.getStatus());

        return result;
    }
} 

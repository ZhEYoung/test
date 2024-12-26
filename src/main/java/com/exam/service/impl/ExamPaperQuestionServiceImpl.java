package com.exam.service.impl;

import com.exam.entity.ExamPaperQuestion;
import com.exam.mapper.ExamPaperQuestionMapper;
import com.exam.service.ExamPaperQuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.math.BigDecimal;

/**
 * 试卷-题目关联服务实现类
 */
@Service
@Transactional
public class ExamPaperQuestionServiceImpl implements ExamPaperQuestionService {

    @Autowired
    private ExamPaperQuestionMapper examPaperQuestionMapper;

    @Override
    public int insert(ExamPaperQuestion examPaperQuestion) {
        return examPaperQuestionMapper.insert(examPaperQuestion);
    }

    @Override
    public int deleteById(Integer epqId) {
        return examPaperQuestionMapper.deleteById(epqId);
    }

    @Override
    public int update(ExamPaperQuestion examPaperQuestion) {
        return examPaperQuestionMapper.update(examPaperQuestion);
    }

    @Override
    public ExamPaperQuestion getById(Integer epqId) {
        return examPaperQuestionMapper.selectById(epqId);
    }

    @Override
    public List<ExamPaperQuestion> getAll() {
        return examPaperQuestionMapper.selectAll();
    }

    @Override
    public List<ExamPaperQuestion> getByPaperId(Integer paperId) {
        return examPaperQuestionMapper.selectByPaperId(paperId);
    }

    @Override
    public List<ExamPaperQuestion> getByQuestionId(Integer questionId) {
        return examPaperQuestionMapper.selectByQuestionId(questionId);
    }

    @Override
    public int deleteByPaperId(Integer paperId) {
        return examPaperQuestionMapper.deleteByPaperId(paperId);
    }

    @Override
    public int batchInsert(List<ExamPaperQuestion> list) {
        if (list == null || list.isEmpty()) {
            return 0;
        }
        return examPaperQuestionMapper.batchInsert(list);
    }

    @Override
    public int updateScore(Integer epqId, BigDecimal score) {
        if (epqId == null || score == null) {
            return 0;
        }
        return examPaperQuestionMapper.updateScore(epqId, score);
    }

    @Override
    public int batchUpdateScore(List<Map<String, Object>> scores) {
        if (scores == null || scores.isEmpty()) {
            return 0;
        }
        return examPaperQuestionMapper.batchUpdateScore(scores);
    }

    @Override
    public int updateQuestionOrder(Integer paperId, Integer questionId, Integer newOrder) {
        if (paperId == null || questionId == null || newOrder == null) {
            return 0;
        }
        return examPaperQuestionMapper.updateQuestionOrder(paperId, questionId, newOrder);
    }

    @Override
    public int batchUpdateOrder(List<Map<String, Object>> orders) {
        if (orders == null || orders.isEmpty()) {
            return 0;
        }
        return examPaperQuestionMapper.batchUpdateOrder(orders);
    }

    @Override
    public BigDecimal calculateTotalScore(Integer paperId) {
        if (paperId == null) {
            return BigDecimal.ZERO;
        }
        return examPaperQuestionMapper.calculateTotalScore(paperId);
    }

    @Override
    public List<Map<String, Object>> analyzeScoreDistribution(Integer paperId) {
        return examPaperQuestionMapper.analyzeScoreDistribution(paperId);
    }

    @Override
    public List<Map<String, Object>> analyzeDifficultyDistribution(Integer paperId) {
        return examPaperQuestionMapper.analyzeDifficultyDistribution(paperId);
    }

    @Override
    public List<Map<String, Object>> getCoveredKnowledgePoints(Integer paperId) {
        return examPaperQuestionMapper.selectCoveredKnowledgePoints(paperId);
    }

    @Override
    public List<Map<String, Object>> checkDuplicateQuestions(Integer paperId) {
        return examPaperQuestionMapper.checkDuplicateQuestions(paperId);
    }

    @Override
    public Map<String, Object> checkPaperCompleteness(Integer paperId) {
        return examPaperQuestionMapper.checkPaperCompleteness(paperId);
    }

    @Override
    public List<Map<String, Object>> analyzeQuestionUsage(String startTime, String endTime) {
        return examPaperQuestionMapper.analyzeQuestionUsage(startTime, endTime);
    }

    @Override
    public int batchUpdateGroup(List<Map<String, Object>> groups) {
        if (groups == null || groups.isEmpty()) {
            return 0;
        }
        return examPaperQuestionMapper.batchUpdateGroup(groups);
    }

    /**
     * 根据考试ID和题目ID查询试卷题目关联
     * @param examId 考试ID
     * @param questionId 题目ID
     * @return 试卷题目关联信息
     */
    @Override
    public ExamPaperQuestion getByExamAndQuestionId(Integer examId, Integer questionId) {
        if (examId == null || questionId == null) {
            throw new IllegalArgumentException("考试ID和题目ID不能为空");
        }
        return examPaperQuestionMapper.selectByExamAndQuestionId(examId, questionId);
    }
} 
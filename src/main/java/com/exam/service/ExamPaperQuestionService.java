package com.exam.service;

import com.exam.entity.ExamPaperQuestion;
import java.util.List;
import java.util.Map;
import java.math.BigDecimal;

/**
 * 试卷-题目关联服务接口
 */
public interface ExamPaperQuestionService {
    
    /**
     * 插入试卷题目关联记录
     */
    int insert(ExamPaperQuestion examPaperQuestion);

    /**
     * 根据ID删除试卷题目关联
     */
    int deleteById(Integer epqId);

    /**
     * 更新试卷题目关联信息
     */
    int update(ExamPaperQuestion examPaperQuestion);

    /**
     * 根据ID查询试卷题目关联
     */
    ExamPaperQuestion selectById(Integer epqId);

    /**
     * 查询所有试卷题目关联
     */
    List<ExamPaperQuestion> selectAll();
    
    /**
     * 根据试卷ID查询题目关联列表
     */
    List<ExamPaperQuestion> getByPaperId(Integer paperId);
    
    /**
     * 根据题目ID查询试卷关联列表
     */
    List<ExamPaperQuestion> getByQuestionId(Integer questionId);
    
    /**
     * 根据试卷ID删除所有题目关联
     */
    int deleteByPaperId(Integer paperId);
    
    /**
     * 批量插入试卷题目关联
     */
    int batchInsert(List<ExamPaperQuestion> list);
    
    /**
     * 更新题目分值
     */
    int updateScore(Integer epqId, BigDecimal score);
    
    /**
     * 批量更新题目分值
     */
    int batchUpdateScore(List<Map<String, Object>> scores);
    
    /**
     * 更新题目顺序
     */
    int updateQuestionOrder(Integer paperId, Integer questionId, Integer newOrder);
    
    /**
     * 批量更新题目顺序
     */
    int batchUpdateOrder(List<Map<String, Object>> orders);
    
    /**
     * 统计试卷总分
     */
    BigDecimal calculateTotalScore(Integer paperId);
    
    /**
     * 统计各题型分值分布
     */
    List<Map<String, Object>> analyzeScoreDistribution(Integer paperId);
    
    /**
     * 统计题目难度分布
     */
    List<Map<String, Object>> analyzeDifficultyDistribution(Integer paperId);
    
    /**
     * 查询试卷题目覆盖的知识点
     */
    List<Map<String, Object>> getCoveredKnowledgePoints(Integer paperId);
    
    /**
     * 检查试卷题目重复
     */
    List<Map<String, Object>> checkDuplicateQuestions(Integer paperId);
    
    /**
     * 查询试卷题目完整性
     */
    Map<String, Object> checkPaperCompleteness(Integer paperId);
    
    /**
     * 统计题目使用频率
     */
    List<Map<String, Object>> analyzeQuestionUsage(String startTime, String endTime);
    
    /**
     * 批量更新题目分组
     */
    int batchUpdateGroup(List<Map<String, Object>> groups);
} 
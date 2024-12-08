package com.exam.service;

import com.exam.entity.ExamPaper;
import com.exam.entity.Question;
import java.util.List;
import java.util.Map;
import java.math.BigDecimal;

/**
 * 试卷服务接口
 */
public interface ExamPaperService extends BaseService<ExamPaper> {
    
    /**
     * 根据学科ID查询试卷列表
     */
    List<ExamPaper> getBySubjectId(Integer subjectId);
    
    /**
     * 根据教师ID查询试卷列表
     */
    List<ExamPaper> getByTeacherId(Integer teacherId);
    
    /**
     * 根据试卷名称查询
     */
    ExamPaper getByName(String paperName);
    
    /**
     * 根据试卷状态查询
     */
    List<ExamPaper> getByStatus(Integer paperStatus);
    
    /**
     * 根据考试类型查询
     */
    List<ExamPaper> getByExamType(Integer examType);
    
    /**
     * 更新试卷状态
     */
    int updateStatus(Integer paperId, Integer status);
    
    /**
     * 根据难度范围查询试卷
     */
    List<ExamPaper> getByDifficultyRange(BigDecimal minDifficulty, BigDecimal maxDifficulty);
    
    /**
     * 查询试卷中的所有题目
     */
    List<Question> getPaperQuestions(Integer paperId);
    
    /**
     * 查询试卷题目及分值
     */
    List<Map<String, Object>> getPaperQuestionsWithScore(Integer paperId);
    
    /**
     * 更新试卷题目分值
     */
    int updateQuestionScore(Integer paperId, Integer questionId, BigDecimal score);
    
    /**
     * 批量更新试卷题目分值
     */
    int batchUpdateQuestionScores(Integer paperId, List<Integer> questionIds, List<BigDecimal> scores);
    
    /**
     * 统计试卷总分
     */
    BigDecimal calculateTotalScore(Integer paperId);
    
    /**
     * 统计试卷平均分
     */
    BigDecimal calculateAverageScore(Integer paperId);
    
    /**
     * 统计试卷及格率
     */
    BigDecimal calculatePassRate(Integer paperId, BigDecimal passScore);
    
    /**
     * 查询试卷成绩分布
     */
    List<Map<String, Object>> getScoreDistribution(Integer paperId);
    
    /**
     * 查询最高分
     */
    BigDecimal getHighestScore(Integer paperId);
    
    /**
     * 查询最低分
     */
    BigDecimal getLowestScore(Integer paperId);
    
    /**
     * 批量发布试卷
     */
    int batchPublish(List<Integer> paperIds);
    
    /**
     * 批量更新试卷状态
     */
    int batchUpdateStatus(List<Integer> paperIds, Integer status);
    
    /**
     * 复制试卷（包含题目）
     */
    int copyPaper(Integer sourcePaperId, String newPaperName);
    
    /**
     * 自动组卷
     * @param subjectId 学科ID
     * @param paperName 试卷名称
     * @param difficulty 难度系数
     * @param questionTypeCount 各题型数量
     * @return 组卷结果
     */
    ExamPaper generatePaper(Integer subjectId, String paperName, 
                          BigDecimal difficulty, Map<Integer, Integer> questionTypeCount);
} 
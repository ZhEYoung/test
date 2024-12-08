package com.exam.service;

import com.exam.entity.Question;
import com.exam.entity.QuestionOption;
import java.util.List;
import java.util.Map;
import java.math.BigDecimal;

/**
 * 题目服务接口
 */
public interface QuestionService extends BaseService<Question> {
    
    /**
     * 根据题库ID查询题目列表
     */
    List<Question> getByBankId(Integer qbId);
    
    /**
     * 根据题目类型查询
     */
    List<Question> getByType(Integer type);
    
    /**
     * 根据难度范围查询
     */
    List<Question> getByDifficultyRange(BigDecimal minDifficulty, BigDecimal maxDifficulty);
    
    /**
     * 根据题目内容模糊查询
     */
    List<Question> getByContent(String content);
    
    /**
     * 批量查询题目
     */
    List<Question> getByIds(List<Integer> questionIds);
    
    /**
     * 查询题目的所有选项
     */
    List<QuestionOption> getOptions(Integer questionId);
    
    /**
     * 添加题目选项
     */
    int addOption(Integer questionId, QuestionOption option);
    
    /**
     * 批量添加题目选项
     */
    int batchAddOptions(Integer questionId, List<QuestionOption> options);
    
    /**
     * 更新题目选项
     */
    int updateOption(Integer optionId, QuestionOption option);
    
    /**
     * 删除题目选项
     */
    int deleteOption(Integer optionId);
    
    /**
     * 查询试卷中的题目
     */
    List<Question> getByPaperId(Integer paperId);
    
    /**
     * 批量添加题目到试卷
     */
    int batchAddToPaper(Integer paperId, List<Integer> questionIds, List<BigDecimal> scores);
    
    /**
     * 从试卷中移除题目
     */
    int removeFromPaper(Integer paperId, Integer questionId);
    
    /**
     * 统计题目使用次数
     */
    Long countUsage(Integer questionId);
    
    /**
     * 统计题目正确率
     */
    BigDecimal calculateCorrectRate(Integer questionId);
    
    /**
     * 统计各类型题目数量
     */
    List<Map<String, Object>> countByType();
    
    /**
     * 统计各难度等级题目数量
     */
    List<Map<String, Object>> countByDifficulty();
    
    /**
     * 查询最常错的题目
     */
    List<Question> getMostMistakes(Integer limit);
    
    /**
     * 更新题目难度
     */
    int updateDifficulty(Integer questionId, BigDecimal difficulty);
    
    /**
     * 批量更新题目难度
     */
    int batchUpdateDifficulty(List<Integer> questionIds, List<BigDecimal> difficulties);
    
    /**
     * 批量导入题目
     * @param questions 题目列表
     * @return 导入结果
     */
    int batchImport(List<Question> questions);
    
    /**
     * 智能推荐相似题目
     * @param questionId 题目ID
     * @param limit 推荐数量
     * @return 推荐题目列表
     */
    List<Question> recommendSimilarQuestions(Integer questionId, Integer limit);
} 
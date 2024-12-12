package com.exam.service;

import com.exam.entity.Question;
import com.exam.entity.QuestionOption;
import java.util.List;
import java.util.Map;
import java.math.BigDecimal;

/**
 * 题目服务接口
 */
public interface QuestionService {
    
    /**
     * 插入一条记录
     */
    int insert(Question record);

    /**
     * 根据ID删除
     */
    int deleteById(Integer id);

    /**
     * 根据ID更新
     */
    int updateById(Question record);

    /**
     * 根据ID查询
     */
    Question getById(Integer id);

    /**
     * 查询所有记录
     */
    List<Question> getAll();
    
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
     * 分页查询题目
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 题目列表
     */
    List<Question> getPage(Integer pageNum, Integer pageSize);

    /**
     * 查询题目总数
     * @return 总记录数
     */
    Long getCount();

    /**
     * 条件查询题目
     * @param condition 查询条件
     * @return 题目列表
     */
    List<Question> getByCondition(Map<String, Object> condition);

    /**
     * 条件查询题目总数
     * @param condition 查询条件
     * @return 记录数
     */
    Long getCountByCondition(Map<String, Object> condition);

    /**
     * 条件分页查询题目
     * @param condition 查询条件
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 题目列表
     */
    List<Question> getPageByCondition(Map<String, Object> condition, Integer pageNum, Integer pageSize);
} 
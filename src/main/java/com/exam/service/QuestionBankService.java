package com.exam.service;

import com.exam.entity.QuestionBank;
import com.exam.entity.Question;
import java.util.List;
import java.util.Map;
import java.math.BigDecimal;

/**
 * 题库服务接口
 */
public interface QuestionBankService {
    
    /**
     * 插入题库记录
     */
    int insert(QuestionBank questionBank);

    /**
     * 根据ID删除题库
     */
    int deleteById(Integer qbId);

    /**
     * 更新题库信息
     */
    int update(QuestionBank questionBank);

    /**
     * 根据ID查询题库
     */
    QuestionBank getById(Integer qbId);

    /**
     * 查询所有题库
     */
    List<QuestionBank> getAll();
    
    /**
     * 分页查询题库
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 题库列表
     */
    List<QuestionBank> getPage(Integer pageNum, Integer pageSize);
    
    /**
     * 条件查询题库
     * @param condition 查询条件
     * @return 题库列表
     */
    List<QuestionBank> getByCondition(Map<String, Object> condition);
    
    /**
     * 条件分页查询题库
     * @param condition 查询条件
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 题库列表
     */
    List<QuestionBank> getPageByCondition(Map<String, Object> condition, Integer pageNum, Integer pageSize);
    
    /**
     * 查询题库总数
     * @return 总记录数
     */
    Long getCount();
    
    /**
     * 条件查询题库总数
     * @param condition 查询条件
     * @return 记录数
     */
    Long getCountByCondition(Map<String, Object> condition);
    
    /**
     * 根据学科ID查询题库列表
     */
    List<QuestionBank> getBySubjectId(Integer subjectId);
    
    /**
     * 根据题库名称查询
     */
    QuestionBank getByName(String qbName);
    
    /**
     * 统计题库中的题目数量
     */
    Integer countQuestions(Integer qbId);
    
    /**
     * 根据学科ID统计题库数量
     */
    Integer countBySubjectId(Integer subjectId);
    
    /**
     * 添加题目到题库
     */
    int addQuestion(Integer qbId, Integer questionId);
    
    /**
     * 批量添加题目到题库
     */
    int batchAddQuestions(Integer qbId, List<Integer> questionIds);
    
    /**
     * 从题库移除题目
     */
    int removeQuestion(Integer qbId, Integer questionId);
    
    /**
     * 批量移除题目
     */
    int batchRemoveQuestions(Integer qbId, List<Integer> questionIds);
    
    /**
     * 查询题库中的所有题目
     */
    List<Question> getQuestions(Integer qbId);
    
    /**
     * 按条件查询题库中的题目
     */
    List<Question> getQuestionsByCondition(Integer qbId, Integer type, 
                                         BigDecimal minDifficulty, BigDecimal maxDifficulty);
    
    /**
     * 统计题库中各类型题目数量
     */
    List<Map<String, Object>> countQuestionsByType(Integer qbId);
    
    /**
     * 统计题库中各难度题目数量
     */
    List<Map<String, Object>> countQuestionsByDifficulty(Integer qbId);
    
    /**
     * 统计题库使用情况
     */
    List<Map<String, Object>> countBankUsage(Integer qbId);
    
    /**
     * 复制题库（包含题目）
     */
    int copyBank(Integer sourceQbId, String newBankName, Integer subjectId);
    
    /**
     * 合并题库
     */
    int mergeBanks(Integer targetQbId, List<Integer> sourceQbIds);
    
    /**
     * 查询最近使用的题库
     */
    List<QuestionBank> getRecentUsed(Integer limit);
    
    /**
     * 查询热门题库
     */
    List<QuestionBank> getHotBanks(Integer limit);
} 
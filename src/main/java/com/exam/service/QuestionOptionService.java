package com.exam.service;

import com.exam.entity.QuestionOption;
import java.util.List;

/**
 * 题目选项服务接口
 */
public interface QuestionOptionService {
    
    /**
     * 插入选项记录
     * @param option 选项实体
     * @return 影响行数
     */
    int insert(QuestionOption option);

    /**
     * 根据ID删除选项
     * @param optionId 选项ID
     * @return 影响行数
     */
    int deleteById(Integer optionId);

    /**
     * 更新选项信息
     * @param option 选项实体
     * @return 影响行数
     */
    int update(QuestionOption option);

    /**
     * 根据ID查询选项
     * @param optionId 选项ID
     * @return 选项实体
     */
    QuestionOption selectById(Integer optionId);

    /**
     * 查询所有选项
     * @return 选项列表
     */
    List<QuestionOption> selectAll();

    /**
     * 根据题目ID查询选项列表
     * @param questionId 题目ID
     * @return 选项列表
     */
    List<QuestionOption> getByQuestionId(Integer questionId);

    /**
     * 批量插入选项
     * @param options 选项列表
     * @return 影响行数
     */
    int batchInsert(List<QuestionOption> options);

    /**
     * 根据题目ID删除选项
     * @param questionId 题目ID
     * @return 影响行数
     */
    int deleteByQuestionId(Integer questionId);

    /**
     * 批量更新选项
     * @param options 选项列表
     * @return 影响行数
     */
    int batchUpdate(List<QuestionOption> options);

    /**
     * 根据题目ID统计选项数量
     * @param questionId 题目ID
     * @return 选项数量
     */
    int countByQuestionId(Integer questionId);

    /**
     * 根据题目ID统计正确选项数量
     * @param questionId 题目ID
     * @return 正确选项数量
     */
    int countCorrectOptions(Integer questionId);

    /**
     * 检查选项是否存在
     * @param optionId 选项ID
     * @return 是否存在
     */
    boolean checkExists(Integer optionId);

    /**
     * 批量删除选项
     * @param optionIds 选项ID列表
     * @return 影响行数
     */
    int batchDelete(List<Integer> optionIds);

    /**
     * 根据内容模糊查询选项
     * @param content 选项内容
     * @return 选项列表
     */
    List<QuestionOption> getByContent(String content);
    
    /**
     * 验证选项数据
     * @param option 选项实体
     * @return 是否有效
     */
    boolean validateOption(QuestionOption option);
    
    /**
     * 批量验证选项数据
     * @param options 选项列表
     * @return 是否全部有效
     */
    boolean validateOptions(List<QuestionOption> options);
} 
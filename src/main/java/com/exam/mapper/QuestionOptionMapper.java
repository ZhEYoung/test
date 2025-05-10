package com.exam.mapper;

import com.exam.entity.QuestionOption;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 题目选项Mapper接口
 */
@Mapper
public interface QuestionOptionMapper {
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
    int deleteById(@Param("optionId") Integer optionId);

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
    QuestionOption selectById(@Param("optionId") Integer optionId);

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
    List<QuestionOption> selectByQuestionId(@Param("questionId") Integer questionId);

    /**
     * 批量插入选项
     * @param options 选项列表
     * @return 影响行数
     */
    int batchInsert(@Param("options") List<QuestionOption> options);

    /**
     * 根据题目ID删除选项
     * @param questionId 题目ID
     * @return 影响行数
     */
    int deleteByQuestionId(@Param("questionId") Integer questionId);

    /**
     * 批量更新选项
     * @param options 选项列表
     * @return 影响行数
     */
    int batchUpdate(@Param("options") List<QuestionOption> options);

    /**
     * 根据题目ID统计选项数量
     * @param questionId 题目ID
     * @return 选项数量
     */
    int countByQuestionId(@Param("questionId") Integer questionId);

    /**
     * 根据题目ID统计正确选项数量
     * @param questionId 题目ID
     * @return 正确选项数量
     */
    int countCorrectOptions(@Param("questionId") Integer questionId);

    /**
     * 检查选项是否存在
     * @param optionId 选项ID
     * @return 是否存在
     */
    boolean checkExists(@Param("optionId") Integer optionId);

    /**
     * 批量删除选项
     * @param optionIds 选项ID列表
     * @return 影响行数
     */
    int batchDelete(@Param("optionIds") List<Integer> optionIds);

    /**
     * 根据内容模糊查询选项
     * @param content 选项内容
     * @return 选项列表
     */
    List<QuestionOption> selectByContent(@Param("content") String content);
} 
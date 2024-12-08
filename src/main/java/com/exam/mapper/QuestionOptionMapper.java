package com.exam.mapper;

import com.exam.entity.QuestionOption;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 题目选项Mapper接口
 */
public interface QuestionOptionMapper extends BaseMapper<QuestionOption> {
    /**
     * 根据题目ID查询选项列表
     */
    List<QuestionOption> selectByQuestionId(@Param("questionId") Integer questionId);

    /**
     * 批量插入选项
     */
    int batchInsert(@Param("options") List<QuestionOption> options);

    /**
     * 根据题目ID删除选项
     */
    int deleteByQuestionId(@Param("questionId") Integer questionId);

    /**
     * 批量更新选项
     */
    int batchUpdate(@Param("options") List<QuestionOption> options);
} 
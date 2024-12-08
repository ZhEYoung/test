package com.exam.mapper;

import com.exam.entity.Question;
import com.exam.entity.QuestionOption;
import org.apache.ibatis.annotations.Param;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 题目Mapper接口
 */
public interface QuestionMapper extends BaseMapper<Question> {
    /**
     * 根据题库ID查询题目列表
     */
    List<Question> selectByBankId(@Param("qbId") Integer qbId);

    /**
     * 根据题目类型查询
     */
    List<Question> selectByType(@Param("type") Integer type);

    /**
     * 根据难度范围查询
     */
    List<Question> selectByDifficultyRange(
        @Param("minDifficulty") BigDecimal minDifficulty, 
        @Param("maxDifficulty") BigDecimal maxDifficulty
    );

    /**
     * 根据题目内容模糊查询
     */
    List<Question> selectByContent(@Param("content") String content);

    /**
     * 批量查询题目
     */
    List<Question> selectByIds(@Param("questionIds") List<Integer> questionIds);

    /**
     * 查询题目的所有选项
     */
    List<QuestionOption> selectOptions(@Param("questionId") Integer questionId);

    /**
     * 添加题目选项
     */
    int insertOption(@Param("questionId") Integer questionId, @Param("option") QuestionOption option);

    /**
     * 批量添加题目选项
     */
    int batchInsertOptions(@Param("questionId") Integer questionId, @Param("options") List<QuestionOption> options);

    /**
     * 更新题目选项
     */
    int updateOption(@Param("optionId") Integer optionId, @Param("option") QuestionOption option);

    /**
     * 删除题目选项
     */
    int deleteOption(@Param("optionId") Integer optionId);

    /**
     * 查询试卷中的题目
     */
    List<Question> selectByPaperId(@Param("paperId") Integer paperId);

    /**
     * 批量添加题目到试卷
     */
    int batchAddToPaper(
        @Param("paperId") Integer paperId,
        @Param("questionIds") List<Integer> questionIds,
        @Param("scores") List<BigDecimal> scores
    );

    /**
     * 从试卷中移除题目
     */
    int removeFromPaper(
        @Param("paperId") Integer paperId,
        @Param("questionId") Integer questionId
    );

    /**
     * 统计题目使用次数
     */
    Long countUsage(@Param("questionId") Integer questionId);

    /**
     * 统计题目正确率
     */
    BigDecimal calculateCorrectRate(@Param("questionId") Integer questionId);

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
    List<Question> selectMostMistakes(@Param("limit") Integer limit);

    /**
     * 更新题目难度
     */
    int updateDifficulty(@Param("questionId") Integer questionId, @Param("difficulty") BigDecimal difficulty);

    /**
     * 批量更新题目难度
     */
    int batchUpdateDifficulty(
        @Param("questionIds") List<Integer> questionIds,
        @Param("difficulties") List<BigDecimal> difficulties
    );
} 
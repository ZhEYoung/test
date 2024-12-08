package com.exam.mapper;

import com.exam.entity.QuestionBank;
import com.exam.entity.Question;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;
import java.math.BigDecimal;

/**
 * 题库Mapper接口
 */
public interface QuestionBankMapper extends BaseMapper<QuestionBank> {
    /**
     * 根据学科ID查询题库列表
     */
    List<QuestionBank> selectBySubjectId(@Param("subjectId") Integer subjectId);

    /**
     * 根据题库名称查询
     */
    QuestionBank selectByName(@Param("qbName") String qbName);

    /**
     * 统计题库中的题目数量
     */
    Integer countQuestions(@Param("qbId") Integer qbId);

    /**
     * 根据学科ID统计题库数量
     */
    Integer countBySubjectId(@Param("subjectId") Integer subjectId);

    /**
     * 添加题目到题库
     */
    int addQuestion(@Param("qbId") Integer qbId, @Param("questionId") Integer questionId);

    /**
     * 批量添加题目到题库
     */
    int batchAddQuestions(@Param("qbId") Integer qbId, @Param("questionIds") List<Integer> questionIds);

    /**
     * 从题库移除题目
     */
    int removeQuestion(@Param("qbId") Integer qbId, @Param("questionId") Integer questionId);

    /**
     * 批量移除题目
     */
    int batchRemoveQuestions(@Param("qbId") Integer qbId, @Param("questionIds") List<Integer> questionIds);

    /**
     * 查询题库中的所有题目
     */
    List<Question> selectQuestions(@Param("qbId") Integer qbId);

    /**
     * 按条件查询题库中的题目
     */
    List<Question> selectQuestionsByCondition(
        @Param("qbId") Integer qbId,
        @Param("type") Integer type,
        @Param("minDifficulty") BigDecimal minDifficulty,
        @Param("maxDifficulty") BigDecimal maxDifficulty
    );

    /**
     * 统计题库中各类型题目数量
     */
    List<Map<String, Object>> countQuestionsByType(@Param("qbId") Integer qbId);

    /**
     * 统计题库中各难度题目数量
     */
    List<Map<String, Object>> countQuestionsByDifficulty(@Param("qbId") Integer qbId);

    /**
     * 统计题库使用情况
     */
    List<Map<String, Object>> countBankUsage(@Param("qbId") Integer qbId);

    /**
     * 复制题库（包含题目）
     */
    int copyBank(
        @Param("sourceQbId") Integer sourceQbId,
        @Param("newBankName") String newBankName,
        @Param("subjectId") Integer subjectId
    );

    /**
     * 合并题库
     */
    int mergeBanks(
        @Param("targetQbId") Integer targetQbId,
        @Param("sourceQbIds") List<Integer> sourceQbIds
    );

    /**
     * 查询最近使用的题库
     */
    List<QuestionBank> selectRecentUsed(@Param("limit") Integer limit);

    /**
     * 查询热门题库
     */
    List<QuestionBank> selectHotBanks(@Param("limit") Integer limit);
} 
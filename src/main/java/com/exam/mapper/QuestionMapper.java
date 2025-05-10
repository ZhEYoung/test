package com.exam.mapper;

import com.exam.entity.Question;
import com.exam.entity.QuestionOption;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 题目Mapper接口
 */
@Mapper
public interface QuestionMapper {
    /**
     * 插入题目记录
     * @param question 题目实体
     * @return 影响行数
     */
    int insert(Question question);

    /**
     * 根据ID删除题目
     * @param questionId 题目ID
     * @return 影响行数
     */
    int deleteById(@Param("questionId") Integer questionId);

    /**
     * 更新题目信息
     * @param question 题目实体
     * @return 影响行数
     */
    int update(Question question);

    /**
     * 根据ID查询题目
     * @param questionId 题目ID
     * @return 题目实体
     */
    Question selectById(@Param("questionId") Integer questionId);

    /**
     * 查询所有题目
     * @return 题目列表
     */
    List<Question> selectAll();

    /**
     * 根据题库ID查询题目列表
     * @param qbId 题库ID
     * @return 题目列表
     */
    List<Question> selectByBankId(@Param("qbId") Integer qbId);

    /**
     * 根据题目类型查询
     * @param type 题目类型
     * @return 题目列表
     */
    List<Question> selectByType(@Param("type") Integer type);

    /**
     * 根据难度范围查询
     * @param minDifficulty 最小难度
     * @param maxDifficulty 最大难度
     * @return 题目列表
     */
    List<Question> selectByDifficultyRange(
        @Param("minDifficulty") BigDecimal minDifficulty, 
        @Param("maxDifficulty") BigDecimal maxDifficulty
    );

    /**
     * 根据题目内容模糊查询
     * @param content 题目内容
     * @return 题目列表
     */
    List<Question> selectByContent(@Param("content") String content);

    /**
     * 批量查询题目
     * @param questionIds 题目ID列表
     * @return 题目列表
     */
    List<Question> selectByIds(@Param("questionIds") List<Integer> questionIds);

    /**
     * 查询题目的所有选项
     * @param questionId 题目ID
     * @return 选项列表
     */
    List<QuestionOption> selectOptions(@Param("questionId") Integer questionId);

    /**
     * 添加题目选项
     * @param questionId 题目ID
     * @param option 选项实体
     * @return 影响行数
     */
    int insertOption(@Param("questionId") Integer questionId, @Param("option") QuestionOption option);

    /**
     * 批量添加题目选项
     * @param questionId 题目ID
     * @param options 选项列表
     * @return 影响行数
     */
    int batchInsertOptions(@Param("questionId") Integer questionId, @Param("options") List<QuestionOption> options);

    /**
     * 更新题目选项
     * @param optionId 选项ID
     * @param option 选项实体
     * @return 影响行数
     */
    int updateOption(@Param("optionId") Integer optionId, @Param("option") QuestionOption option);

    /**
     * 删除题目选项
     * @param optionId 选项ID
     * @return 影响行数
     */
    int deleteOption(@Param("optionId") Integer optionId);

    /**
     * 查询试卷中的题目
     * @param paperId 试卷ID
     * @return 题目列表
     */
    List<Question> selectByPaperId(@Param("paperId") Integer paperId);

    /**
     * 批量添加题目到试卷
     * @param paperId 试卷ID
     * @param questionIds 题目ID列表
     * @param scores 分数列表
     * @return 影响行数
     */
    int batchAddToPaper(
        @Param("paperId") Integer paperId,
        @Param("questionIds") List<Integer> questionIds,
        @Param("scores") List<BigDecimal> scores
    );

    /**
     * 从试卷中移除题目
     * @param paperId 试卷ID
     * @param questionId 题目ID
     * @return 影响行数
     */
    int removeFromPaper(
        @Param("paperId") Integer paperId,
        @Param("questionId") Integer questionId
    );

    /**
     * 统计题目使用次数
     * @param questionId 题目ID
     * @return 使用次数
     */
    Long countUsage(@Param("questionId") Integer questionId);

    /**
     * 统计题目正确率
     * @param questionId 题目ID
     * @return 正确率
     */
    BigDecimal calculateCorrectRate(@Param("questionId") Integer questionId);

    /**
     * 统计各类型题目数量
     * @return 统计结果
     */
    List<Map<String, Object>> countByType();

    /**
     * 统计各难度等级题目数量
     * @return 统计结果
     */
    List<Map<String, Object>> countByDifficulty();

    /**
     * 查询最常错的题目
     * @param limit 限制数量
     * @return 题目列表
     */
    List<Question> selectMostMistakes(@Param("limit") Integer limit);

    /**
     * 更新题目难度
     * @param questionId 题目ID
     * @param difficulty 难度值
     * @return 影响行数
     */
    int updateDifficulty(@Param("questionId") Integer questionId, @Param("difficulty") BigDecimal difficulty);

    /**
     * 批量更新题目难度
     * @param questionIds 题目ID列表
     * @param difficulties 难度值列表
     * @return 影响行数
     */
    int batchUpdateDifficulty(
        @Param("questionIds") List<Integer> questionIds,
        @Param("difficulties") List<BigDecimal> difficulties
    );
} 
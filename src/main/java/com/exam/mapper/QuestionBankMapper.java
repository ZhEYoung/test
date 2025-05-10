package com.exam.mapper;

import com.exam.entity.QuestionBank;
import com.exam.entity.Question;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;
import java.math.BigDecimal;

/**
 * 题库Mapper接口
 */
@Mapper
public interface QuestionBankMapper {
    /**
     * 插入题库记录
     * @param questionBank 题库实体
     * @return 影响行数
     */
    int insert(QuestionBank questionBank);

    /**
     * 根据ID删除题库
     * @param qbId 题库ID
     * @return 影响行数
     */
    int deleteById(@Param("qbId") Integer qbId);

    /**
     * 更新题库信息
     * @param questionBank 题库实体
     * @return 影响行数
     */
    int update(QuestionBank questionBank);

    /**
     * 根据ID查询题库
     * @param qbId 题库ID
     * @return 题库实体
     */
    QuestionBank selectById(@Param("qbId") Integer qbId);

    /**
     * 查询所有题库
     * @return 题库列表
     */
    List<QuestionBank> selectAll();

    /**
     * 分页查询题库
     * @param params 分页参数（offset, limit）
     * @return 题库列表
     */
    List<QuestionBank> selectPage(@Param("params") Map<String, Object> params);

    /**
     * 条件查询题库
     * @param condition 查询条件
     * @return 题库列表
     */
    List<QuestionBank> selectByCondition(@Param("condition") Map<String, Object> condition);

    /**
     * 条件分页查询题库
     * @param condition 查询条件（包含offset, limit）
     * @return 题库列表
     */
    List<QuestionBank> selectPageByCondition(@Param("condition") Map<String, Object> condition);

    /**
     * 查询题库总数
     * @return 总记录数
     */
    Long selectCount();

    /**
     * 条件查询题库总数
     * @param condition 查询条件
     * @return 记录数
     */
    Long selectCountByCondition(@Param("condition") Map<String, Object> condition);

    /**
     * 根据学科ID查询题库列表
     * @param subjectId 学科ID
     * @return 题库列表
     */
    List<QuestionBank> selectBySubjectId(@Param("subjectId") Integer subjectId);

    /**
     * 根据题库名称查询
     * @param qbName 题库名称
     * @return 题库实体
     */
    QuestionBank selectByName(@Param("qbName") String qbName);

    /**
     * 统计题库中的题目数量
     * @param qbId 题库ID
     * @return 题目数量
     */
    Integer countQuestions(@Param("qbId") Integer qbId);

    /**
     * 根据学科ID统计题库数量
     * @param subjectId 学科ID
     * @return 题库数量
     */
    Integer countBySubjectId(@Param("subjectId") Integer subjectId);

    /**
     * 添加题目到题库
     * @param qbId 题库ID
     * @param questionId 题目ID
     * @return 影响行数
     */
    int addQuestion(@Param("qbId") Integer qbId, @Param("questionId") Integer questionId);

    /**
     * 批量添加题目到题库
     * @param qbId 题库ID
     * @param questionIds 题目ID列表
     * @return 影响行数
     */
    int batchAddQuestions(@Param("qbId") Integer qbId, @Param("questionIds") List<Integer> questionIds);

    /**
     * 从题库移除题目
     * @param qbId 题库ID
     * @param questionId 题目ID
     * @return 影响行数
     */
    int removeQuestion(@Param("qbId") Integer qbId, @Param("questionId") Integer questionId);

    /**
     * 批量移除题目
     * @param qbId 题库ID
     * @param questionIds 题目ID列表
     * @return 影响行数
     */
    int batchRemoveQuestions(@Param("qbId") Integer qbId, @Param("questionIds") List<Integer> questionIds);

    /**
     * 查询题库中的所有题目
     * @param qbId 题库ID
     * @return 题目列表
     */
    List<Question> selectQuestions(@Param("qbId") Integer qbId);

    /**
     * 按条件查询题库中的题目
     * @param qbId 题库ID
     * @param type 题目类型
     * @param minDifficulty 最小难度
     * @param maxDifficulty 最大难度
     * @return 题目列表
     */
    List<Question> selectQuestionsByCondition(
        @Param("qbId") Integer qbId,
        @Param("type") Integer type,
        @Param("minDifficulty") BigDecimal minDifficulty,
        @Param("maxDifficulty") BigDecimal maxDifficulty
    );

    /**
     * 统计题库中各类型题目数量
     * @param qbId 题库ID
     * @return 各类型题目统计信息
     */
    List<Map<String, Object>> countQuestionsByType(@Param("qbId") Integer qbId);

    /**
     * 统计题库中各难度题目数量
     * @param qbId 题库ID
     * @return 各难度题目统计信息
     */
    List<Map<String, Object>> countQuestionsByDifficulty(@Param("qbId") Integer qbId);

    /**
     * 统计题库使用情况
     * @param qbId 题库ID
     * @return 题库使用统计信息
     */
    List<Map<String, Object>> countBankUsage(@Param("qbId") Integer qbId);

    /**
     * 复制题库（包含题目）
     * @param sourceQbId 源题库ID
     * @param newBankName 新题库名称
     * @param subjectId 学科ID
     * @return 影响行数
     */
    int copyBank(
        @Param("sourceQbId") Integer sourceQbId,
        @Param("newBankName") String newBankName,
        @Param("subjectId") Integer subjectId
    );

    /**
     * 合并题库
     * @param targetQbId 目标题库ID
     * @param sourceQbIds 源题库ID列表
     * @return 影响行数
     */
    int mergeBanks(
        @Param("targetQbId") Integer targetQbId,
        @Param("sourceQbIds") List<Integer> sourceQbIds
    );

    /**
     * 查询最近使用的题库
     * @param limit 限制数量
     * @return 最近使用的题库列表
     */
    List<QuestionBank> selectRecentUsed(@Param("limit") Integer limit);

    /**
     * 查询热门题库
     * @param limit 限制数量
     * @return 热门题库列表
     */
    List<QuestionBank> selectHotBanks(@Param("limit") Integer limit);

    /**
     * 根据学科ID删除题库
     * @param subjectId 学科ID
     * @return 影响行数
     */
    int deleteBySubjectId(@Param("subjectId") Integer subjectId);
} 
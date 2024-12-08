package com.exam.mapper;

import com.exam.entity.ExamPaperQuestion;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;
import java.math.BigDecimal;

/**
 * 试卷-题目关联Mapper接口
 */
public interface ExamPaperQuestionMapper extends BaseMapper<ExamPaperQuestion> {
    /**
     * 根据试卷ID查询题目关联列表
     */
    List<ExamPaperQuestion> selectByPaperId(@Param("paperId") Integer paperId);

    /**
     * 根据题目ID查询试卷关联列表
     */
    List<ExamPaperQuestion> selectByQuestionId(@Param("questionId") Integer questionId);

    /**
     * 根据试卷ID删除所有题目关联
     */
    int deleteByPaperId(@Param("paperId") Integer paperId);

    /**
     * 批量插入试卷题目关联
     */
    int batchInsert(@Param("list") List<ExamPaperQuestion> list);

    /**
     * 更新题目分值
     */
    int updateScore(@Param("epqId") Integer epqId, @Param("score") BigDecimal score);

    /**
     * 批量更新题目分值
     */
    int batchUpdateScore(@Param("scores") List<Map<String, Object>> scores);

    /**
     * 更新题目顺序
     */
    int updateQuestionOrder(
        @Param("paperId") Integer paperId,
        @Param("questionId") Integer questionId,
        @Param("newOrder") Integer newOrder
    );

    /**
     * 批量更新题目顺序
     */
    int batchUpdateOrder(@Param("orders") List<Map<String, Object>> orders);

    /**
     * 统计试卷总分
     */
    BigDecimal calculateTotalScore(@Param("paperId") Integer paperId);

    /**
     * 统计各题型分值分布
     */
    List<Map<String, Object>> analyzeScoreDistribution(@Param("paperId") Integer paperId);

    /**
     * 统计题目难度分布
     */
    List<Map<String, Object>> analyzeDifficultyDistribution(@Param("paperId") Integer paperId);

    /**
     * 查询试卷题目覆盖的知识点
     */
    List<Map<String, Object>> selectCoveredKnowledgePoints(@Param("paperId") Integer paperId);

    /**
     * 检查试卷题目重复
     */
    List<Map<String, Object>> checkDuplicateQuestions(@Param("paperId") Integer paperId);

    /**
     * 查询试卷题目完整性
     */
    Map<String, Object> checkPaperCompleteness(@Param("paperId") Integer paperId);

    /**
     * 统计题目使用频率
     */
    List<Map<String, Object>> analyzeQuestionUsage(
        @Param("startTime") String startTime,
        @Param("endTime") String endTime
    );

    /**
     * 查询题目组合分析
     */
    List<Map<String, Object>> analyzeQuestionCombination(@Param("paperId") Integer paperId);

    /**
     * 更新题目分组
     */
    int updateQuestionGroup(
        @Param("paperId") Integer paperId,
        @Param("questionId") Integer questionId,
        @Param("groupId") Integer groupId
    );

    /**
     * 批量更新题目分组
     */
    int batchUpdateGroup(@Param("groups") List<Map<String, Object>> groups);
} 
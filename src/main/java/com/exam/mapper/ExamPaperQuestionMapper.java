package com.exam.mapper;

import com.exam.entity.ExamPaperQuestion;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;
import java.math.BigDecimal;

/**
 * 试卷-题目关联Mapper接口
 */
@Mapper
public interface ExamPaperQuestionMapper {
    /**
     * 插入试卷题目关联记录
     * @param examPaperQuestion 试卷题目关联实体
     * @return 影响行数
     */
    int insert(ExamPaperQuestion examPaperQuestion);

    /**
     * 根据ID删除试卷题目关联
     * @param epqId 关联ID
     * @return 影响行数
     */
    int deleteById(@Param("epqId") Integer epqId);

    /**
     * 更新试卷题目关联信息
     * @param examPaperQuestion 试卷题目关联实体
     * @return 影响行数
     */
    int update(ExamPaperQuestion examPaperQuestion);

    /**
     * 根据ID查询试卷题目关联
     * @param epqId 关联ID
     * @return 试卷题目关联实体
     */
    ExamPaperQuestion selectById(@Param("epqId") Integer epqId);

    /**
     * 查询所有试卷题目关联
     * @return 试卷题目关联列表
     */
    List<ExamPaperQuestion> selectAll();

    /**
     * 根据试卷ID查询题目关联列表
     * @param paperId 试卷ID
     * @return 试卷题目关联列表
     */
    List<ExamPaperQuestion> selectByPaperId(@Param("paperId") Integer paperId);

    /**
     * 根据题目ID查询试卷关联列表
     * @param questionId 题目ID
     * @return 试卷题目关联列表
     */
    List<ExamPaperQuestion> selectByQuestionId(@Param("questionId") Integer questionId);

    /**
     * 根据试卷ID删除所有题目关联
     * @param paperId 试卷ID
     * @return 影响行数
     */
    int deleteByPaperId(@Param("paperId") Integer paperId);

    /**
     * 批量插入试卷题目关联
     * @param list 试卷题目关联列表
     * @return 影响行数
     */
    int batchInsert(@Param("list") List<ExamPaperQuestion> list);

    /**
     * 更新题目分值
     * @param epqId 关联ID
     * @param score 分值
     * @return 影响行数
     */
    int updateScore(@Param("epqId") Integer epqId, @Param("score") BigDecimal score);

    /**
     * 批量更新题目分值
     * @param scores 分值更新列表
     * @return 影响行数
     */
    int batchUpdateScore(@Param("scores") List<Map<String, Object>> scores);

    /**
     * 更新题目顺序
     * @param paperId 试卷ID
     * @param questionId 题目ID
     * @param newOrder 新顺序
     * @return 影响行数
     */
    int updateQuestionOrder(
        @Param("paperId") Integer paperId,
        @Param("questionId") Integer questionId,
        @Param("newOrder") Integer newOrder
    );

    /**
     * 批量更新题目顺序
     * @param orders 顺序更新列表
     * @return 影响行数
     */
    int batchUpdateOrder(@Param("orders") List<Map<String, Object>> orders);

    /**
     * 统计试卷总分
     * @param paperId 试卷ID
     * @return 总分
     */
    BigDecimal calculateTotalScore(@Param("paperId") Integer paperId);

    /**
     * 统计各题型分值分布
     * @param paperId 试卷ID
     * @return 分值分布信息
     */
    List<Map<String, Object>> analyzeScoreDistribution(@Param("paperId") Integer paperId);

    /**
     * 统计题目难度分布
     * @param paperId 试卷ID
     * @return 难度分布信息
     */
    List<Map<String, Object>> analyzeDifficultyDistribution(@Param("paperId") Integer paperId);

    /**
     * 查询试卷题目覆盖的知识点
     * @param paperId 试卷ID
     * @return 知识点覆盖信息
     */
    List<Map<String, Object>> selectCoveredKnowledgePoints(@Param("paperId") Integer paperId);

    /**
     * 检查试卷题目重复
     * @param paperId 试卷ID
     * @return 重复题目信息
     */
    List<Map<String, Object>> checkDuplicateQuestions(@Param("paperId") Integer paperId);

    /**
     * 查询试卷题目完整性
     * @param paperId 试卷ID
     * @return 完整性检查结果
     */
    Map<String, Object> checkPaperCompleteness(@Param("paperId") Integer paperId);

    /**
     * 统计题目使用频率
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 使用频率统计信息
     */
    List<Map<String, Object>> analyzeQuestionUsage(
        @Param("startTime") String startTime,
        @Param("endTime") String endTime
    );

    /**
     * 批量更新题目分组
     * @param groups 分组更新列表
     * @return 影响行数
     */
    int batchUpdateGroup(@Param("groups") List<Map<String, Object>> groups);

    /**
     * 根据考试ID和题目ID查询试卷题目关联
     * @param examId 考试ID
     * @param questionId 题目ID
     * @return 试卷题目关联信息
     */
    ExamPaperQuestion selectByExamAndQuestionId(Integer examId, Integer questionId);
} 
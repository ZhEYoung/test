package com.exam.mapper;

import com.exam.entity.StudentQuestionScore;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 学生题目成绩Mapper接口
 */
public interface StudentQuestionScoreMapper extends BaseMapper<StudentQuestionScore> {
    /**
     * 根据成绩ID查询题目得分列表
     */
    List<StudentQuestionScore> selectByScoreId(@Param("scoreId") Integer scoreId);

    /**
     * 根据考试ID和学生ID查询题目得分列表
     */
    List<StudentQuestionScore> selectByExamAndStudent(
        @Param("examId") Integer examId, 
        @Param("studentId") Integer studentId
    );

    /**
     * 根据题目ID查询所有学生的得分列表
     */
    List<StudentQuestionScore> selectByQuestionId(@Param("questionId") Integer questionId);

    /**
     * 批量插入题目得分记录
     */
    int batchInsert(@Param("list") List<StudentQuestionScore> list);

    /**
     * 更新题目得分和批改状态
     */
    int updateScoreAndStatus(
        @Param("recordId") Integer recordId, 
        @Param("score") BigDecimal score, 
        @Param("status") String status
    );

    /**
     * 批量更新题目得分
     */
    int batchUpdateScore(@Param("records") List<Map<String, Object>> records);

    /**
     * 统计题目正确率
     */
    Map<String, Object> calculateQuestionCorrectRate(
        @Param("questionId") Integer questionId,
        @Param("examId") Integer examId
    );

    /**
     * 统计题目得分分布
     */
    List<Map<String, Object>> analyzeScoreDistribution(
        @Param("questionId") Integer questionId,
        @Param("examId") Integer examId
    );

    /**
     * 分析答题时间分布
     */
    List<Map<String, Object>> analyzeAnswerTimeDistribution(
        @Param("questionId") Integer questionId,
        @Param("examId") Integer examId
    );

    /**
     * 查询题目平均得分
     */
    BigDecimal calculateAverageScore(
        @Param("questionId") Integer questionId,
        @Param("examId") Integer examId
    );

    /**
     * 查询最难题目列表
     */
    List<Map<String, Object>> selectDifficultQuestions(
        @Param("examId") Integer examId,
        @Param("limit") Integer limit
    );

    /**
     * 查询最容易题目列表
     */
    List<Map<String, Object>> selectEasyQuestions(
        @Param("examId") Integer examId,
        @Param("limit") Integer limit
    );

    /**
     * 分析学生答题模式
     */
    List<Map<String, Object>> analyzeAnswerPattern(
        @Param("studentId") Integer studentId,
        @Param("examId") Integer examId
    );

    /**
     * 统计题目类型得分情况
     */
    List<Map<String, Object>> analyzeScoreByQuestionType(
        @Param("studentId") Integer studentId,
        @Param("examId") Integer examId
    );

    /**
     * 查询需要人工批改的题目
     */
    List<StudentQuestionScore> selectNeedManualGrading(
        @Param("examId") Integer examId,
        @Param("teacherId") Integer teacherId
    );

    /**
     * 批量更新批改状态
     */
    int batchUpdateGradingStatus(
        @Param("recordIds") List<Integer> recordIds,
        @Param("status") String status,
        @Param("graderId") Integer graderId
    );

    /**
     * 统计批改进度
     */
    Map<String, Object> countGradingProgress(@Param("examId") Integer examId);
} 
package com.exam.mapper;

import com.exam.entity.StudentQuestionScore;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 学生题目成绩Mapper接口
 */
@Mapper
public interface StudentQuestionScoreMapper {
    /**
     * 插入单条题目得分记录
     * @param record 得分记录
     * @return 影响行数
     */
    int insert(StudentQuestionScore record);

    /**
     * 根据ID删除得分记录
     * @param recordId 记录ID
     * @return 影响行数
     */
    int deleteById(@Param("recordId") Integer recordId);

    /**
     * 更新得分记录
     * @param record 得分记录
     * @return 影响行数
     */
    int update(StudentQuestionScore record);

    /**
     * 根据ID查询得分记录
     * @param recordId 记录ID
     * @return 得分记录
     */
    StudentQuestionScore selectById(@Param("recordId") Integer recordId);

    /**
     * 查询所有得分记录
     * @return 得分记录列表
     */
    List<StudentQuestionScore> selectAll();

    /**
     * 根据成绩ID查询题目得分列表
     * @param scoreId 成绩ID
     * @return 得分记录列表
     */
    List<StudentQuestionScore> selectByScoreId(@Param("scoreId") Integer scoreId);

    /**
     * 根据考试ID和学生ID查询题目得分列表
     * @param examId 考试ID
     * @param studentId 学生ID
     * @return 得分记录列表
     */
    List<StudentQuestionScore> selectByExamAndStudent(
        @Param("examId") Integer examId, 
        @Param("studentId") Integer studentId
    );

    /**
     * 根据题目ID查询所有学生的得分列表
     * @param questionId 题目ID
     * @return 得分记录列表
     */
    List<StudentQuestionScore> selectByQuestionId(@Param("questionId") Integer questionId);

    /**
     * 批量插入题目得分记录
     * @param list 得分记录列表
     * @return 影响行数
     */
    int batchInsert(@Param("list") List<StudentQuestionScore> list);

    /**
     * 更新题目得分和批改状态
     * @param recordId 记录ID
     * @param score 得分
     * @param status 状态
     * @return 影响行数
     */
    int updateScoreAndStatus(
        @Param("recordId") Integer recordId, 
        @Param("score") BigDecimal score, 
        @Param("status") String status
    );

    /**
     * 批量更新题目得分
     * @param records 更新记录列表
     * @return 影响行数
     */
    int batchUpdateScore(@Param("records") List<Map<String, Object>> records);

    /**
     * 统计题目正确率
     * @param questionId 题目ID
     * @param examId 考试ID
     * @return 统计结果
     */
    Map<String, Object> calculateQuestionCorrectRate(
        @Param("questionId") Integer questionId,
        @Param("examId") Integer examId
    );

    /**
     * 统计题目得分分布
     * @param questionId 题目ID
     * @param examId 考试ID
     * @return 分布统计
     */
    List<Map<String, Object>> analyzeScoreDistribution(
        @Param("questionId") Integer questionId,
        @Param("examId") Integer examId
    );

    /**
     * 分析答题时间分布
     * @param questionId 题目ID
     * @param examId 考试ID
     * @return 时间分布
     */
    List<Map<String, Object>> analyzeAnswerTimeDistribution(
        @Param("questionId") Integer questionId,
        @Param("examId") Integer examId
    );

    /**
     * 查询题目平均得分
     * @param questionId 题目ID
     * @param examId 考试ID
     * @return 平均得分
     */
    BigDecimal calculateAverageScore(
        @Param("questionId") Integer questionId,
        @Param("examId") Integer examId
    );

    /**
     * 查询最难题目列表
     * @param examId 考试ID
     * @param limit 限制数量
     * @return 题目列表
     */
    List<Map<String, Object>> selectDifficultQuestions(
        @Param("examId") Integer examId,
        @Param("limit") Integer limit
    );

    /**
     * 查询最容易题目列表
     * @param examId 考试ID
     * @param limit 限制数量
     * @return 题目列表
     */
    List<Map<String, Object>> selectEasyQuestions(
        @Param("examId") Integer examId,
        @Param("limit") Integer limit
    );

    /**
     * 分析学生答题模式
     * @param studentId 学生ID
     * @param examId 考试ID
     * @return 答题模式分析
     */
    List<Map<String, Object>> analyzeAnswerPattern(
        @Param("studentId") Integer studentId,
        @Param("examId") Integer examId
    );

    /**
     * 统计题目类型得分情况
     * @param studentId 学生ID
     * @param examId 考试ID
     * @return 得分统计
     */
    List<Map<String, Object>> analyzeScoreByQuestionType(
        @Param("studentId") Integer studentId,
        @Param("examId") Integer examId
    );

    /**
     * 查询需要人工批改的题目
     * @param examId 考试ID
     * @param teacherId 教师ID
     * @return 题目列表
     */
    List<StudentQuestionScore> selectNeedManualGrading(
        @Param("examId") Integer examId,
        @Param("teacherId") Integer teacherId
    );

    /**
     * 批量更新批改状态
     * @param recordIds 记录ID列表
     * @param status 状态
     * @param graderId 批改人ID
     * @return 影响行数
     */
    int batchUpdateGradingStatus(
        @Param("recordIds") List<Integer> recordIds,
        @Param("status") String status,
        @Param("graderId") Integer graderId
    );

    /**
     * 统计批改进度
     * @param examId 考试ID
     * @return 进度统计
     */
    Map<String, Object> countGradingProgress(@Param("examId") Integer examId);

    /**
     * 分页查询得分记录
     * @param offset 偏移量
     * @param limit 每页记录数
     * @return 得分记录列表
     */
    List<StudentQuestionScore> selectPage(
        @Param("offset") Integer offset,
        @Param("limit") Integer limit
    );

    /**
     * 统计总记录数
     * @return 记录总数
     */
    int countTotal();

    /**
     * 批量删除得分记录
     * @param recordIds 记录ID列表
     * @return 影响行数
     */
    int batchDelete(@Param("recordIds") List<Integer> recordIds);

    /**
     * 查询学生答题详情
     * @param studentId 学生ID
     * @param examId 考试ID
     * @return 答题详情
     */
    List<Map<String, Object>> selectAnswerDetails(
        @Param("studentId") Integer studentId,
        @Param("examId") Integer examId
    );

    /**
     * 统计错题分布
     * @param studentId 学生ID
     * @param examId 考试ID
     * @return 错题分布
     */
    List<Map<String, Object>> analyzeMistakeDistribution(
        @Param("studentId") Integer studentId,
        @Param("examId") Integer examId
    );
} 
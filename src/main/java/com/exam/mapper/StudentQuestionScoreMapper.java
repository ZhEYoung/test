package com.exam.mapper;

import com.exam.entity.StudentQuestionScore;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;
import java.math.BigDecimal;

/**
 * 学生题目得分Mapper接口
 */
@Mapper
public interface StudentQuestionScoreMapper {
    
    /**
     * 插入单条题目得分记录
     */
    int insert(StudentQuestionScore record);
    
    /**
     * 根据ID删除得分记录
     */
    int deleteById(@Param("recordId") Integer recordId);
    
    /**
     * 更新得分记录
     */
    int update(StudentQuestionScore record);
    
    /**
     * 根据ID查询得分记录
     */
    StudentQuestionScore selectById(@Param("recordId") Integer recordId);
    
    /**
     * 查询所有得分记录
     */
    List<StudentQuestionScore> selectAll();
    
    /**
     * 根据成绩ID查询题目得分列表
     */
    List<StudentQuestionScore> selectByScoreId(@Param("scoreId") Integer scoreId);
    
    /**
     * 根据考试ID和学生ID查询题目得分列表
     */
    List<StudentQuestionScore> selectByExamAndStudent(@Param("examId") Integer examId, @Param("studentId") Integer studentId);
    
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
    int updateScoreAndStatus(@Param("recordId") Integer recordId, @Param("score") BigDecimal score, @Param("status") String status);
    
    /**
     * 批量更新题目得分
     */
    int batchUpdateScore(@Param("records") List<Map<String, Object>> records);
    
    /**
     * 统计题目正确率
     */
    Map<String, Object> calculateQuestionCorrectRate(@Param("questionId") Integer questionId, @Param("examId") Integer examId);
    
    /**
     * 统计题目得分分布
     */
    List<Map<String, Object>> analyzeScoreDistribution(@Param("questionId") Integer questionId, @Param("examId") Integer examId);
    
    /**
     * 分析答题时间分布
     */
    List<Map<String, Object>> analyzeAnswerTimeDistribution(@Param("questionId") Integer questionId, @Param("examId") Integer examId);
    
    /**
     * 查询题目平均得分
     */
    BigDecimal calculateAverageScore(@Param("questionId") Integer questionId, @Param("examId") Integer examId);
    
    /**
     * 查询最难题目列表
     */
    List<Map<String, Object>> selectDifficultQuestions(@Param("examId") Integer examId, @Param("limit") Integer limit);
    
    /**
     * 查询最容易题目列表
     */
    List<Map<String, Object>> selectEasyQuestions(@Param("examId") Integer examId, @Param("limit") Integer limit);
    
    /**
     * 分析学生答题模式
     */
    List<Map<String, Object>> analyzeAnswerPattern(@Param("studentId") Integer studentId, @Param("examId") Integer examId);
    
    /**
     * 分页查询
     */
    List<StudentQuestionScore> selectPage(@Param("offset") Integer offset, @Param("limit") Integer limit);
    
    /**
     * 统计总记录数
     */
    Integer countTotal();
    
    /**
     * 条件查询
     */
    List<StudentQuestionScore> selectByCondition(Map<String, Object> condition);
    
    /**
     * 条件查询记录数
     */
    Long selectCountByCondition(Map<String, Object> condition);
    
    /**
     * 条件分页查询
     */
    List<StudentQuestionScore> selectPageByCondition(
        @Param("condition") Map<String, Object> condition,
        @Param("offset") Integer offset,
        @Param("pageSize") Integer pageSize
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
     * 查询需要人工批改的题目
     */
    List<StudentQuestionScore> selectNeedManualGrading(
        @Param("examId") Integer examId,
        @Param("teacherId") Integer teacherId
    );
    

} 
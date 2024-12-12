package com.exam.service;

import com.exam.entity.StudentQuestionScore;
import java.util.List;
import java.util.Map;
import java.math.BigDecimal;

/**
 * 学生题目得分服务接口
 */
public interface StudentQuestionScoreService {
    
    /**
     * 插入一条记录
     */
    int insert(StudentQuestionScore record);

    /**
     * 根据ID删除
     */
    int deleteById(Integer id);

    /**
     * 根据ID更新
     */
    int updateById(StudentQuestionScore record);

    /**
     * 根据ID查询
     */
    StudentQuestionScore getById(Integer id);

    /**
     * 查询所有记录
     */
    List<StudentQuestionScore> getAll();

    /**
     * 分页查询
     */
    List<StudentQuestionScore> getPage(Integer pageNum, Integer pageSize);

    /**
     * 查询总记录数
     */
    Long getCount();

    /**
     * 条件查询
     */
    List<StudentQuestionScore> getByCondition(Map<String, Object> condition);

    /**
     * 条件查询记录数
     */
    Long getCountByCondition(Map<String, Object> condition);

    /**
     * 条件分页查询
     */
    List<StudentQuestionScore> getPageByCondition(Map<String, Object> condition, Integer pageNum, Integer pageSize);
    
    /**
     * 插入单条学生题目得分记录
     * @param record 学生题目得分记录
     * @return 影响的行数
     */
    int insertStudentQuestionScore(StudentQuestionScore record);
    
    /**
     * 根据成绩ID查询题目得分列表
     */
    List<StudentQuestionScore> getByScoreId(Integer scoreId);
    
    /**
     * 根据考试ID和学生ID查询题目得分列表
     */
    List<StudentQuestionScore> getByExamAndStudent(Integer examId, Integer studentId);
    
    /**
     * 根据题目ID查询所有学生的得分列表
     */
    List<StudentQuestionScore> getByQuestionId(Integer questionId);
    
    /**
     * 批量插入题目得分记录
     */
    int batchInsert(List<StudentQuestionScore> list);
    
    /**
     * 更新题目得分和批改状态
     */
    int updateScoreAndStatus(Integer recordId, BigDecimal score, String status);
    
    /**
     * 批量更新题目得分
     */
    int batchUpdateScore(List<Map<String, Object>> records);
    
    /**
     * 统计题目正确率
     */
    Map<String, Object> calculateQuestionCorrectRate(Integer questionId, Integer examId);
    
    /**
     * 统计题目得分分布
     */
    List<Map<String, Object>> analyzeScoreDistribution(Integer questionId, Integer examId);
    
    /**
     * 分析答题时间分布
     */
    List<Map<String, Object>> analyzeAnswerTimeDistribution(Integer questionId, Integer examId);
    
    /**
     * 查询题目平均得分
     */
    BigDecimal calculateAverageScore(Integer questionId, Integer examId);
    
    /**
     * 查询最难题目列表
     */
    List<Map<String, Object>> getDifficultQuestions(Integer examId, Integer limit);
    
    /**
     * 查询最容易题目列表
     */
    List<Map<String, Object>> getEasyQuestions(Integer examId, Integer limit);
    
    /**
     * 分析学生答题模式
     */
    List<Map<String, Object>> analyzeAnswerPattern(Integer studentId, Integer examId);

    
    /**
     * 查询需要人工批改的题目
     */
    List<StudentQuestionScore> getNeedManualGrading(Integer examId, Integer teacherId);
    
    /**
     * 批量更新批改状态
     */
    int batchUpdateGradingStatus(List<Integer> recordIds, String status, Integer graderId);

    
    /**
     * 导出题目得分报告
     * @param examId 考试ID
     * @param questionId 题目ID
     * @return 题目得分报告数据
     */
    Map<String, Object> exportQuestionScoreReport(Integer examId, Integer questionId);
    
    /**
     * 批量导入题目得分
     * @param scores 题目得分列表
     * @return 导入结果
     */
    int importQuestionScores(List<StudentQuestionScore> scores);

    /**
     * 获取学生主观题答案
     * @param examId 考试ID
     * @param questionId 题目ID
     * @param studentId 学生ID
     * @return 学生答案信息
     */
    Map<String, Object> getSubjectiveAnswer(Integer examId, Integer questionId, Integer studentId);
} 
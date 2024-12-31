package com.exam.service;

import com.exam.entity.StudentScore;
import java.util.List;
import java.util.Map;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 学生成绩服务接口
 */
public interface StudentScoreService {
    
    /**
     * 插入一条记录
     */
    int insert(StudentScore record);

    /**
     * 根据ID删除
     */
    int deleteById(Integer id);

    /**
     * 根据ID更新
     */
    int updateById(StudentScore record);

    /**
     * 根据ID查询
     */
    StudentScore getById(Integer id);

    /**
     * 查询所有记录
     */
    List<StudentScore> getAll();

    /**
     * 分页查询
     */
    List<StudentScore> getPage(Integer pageNum, Integer pageSize);

    /**
     * 查询总记录数
     */
    Long getCount();

    /**
     * 条件查询
     */
    List<StudentScore> getByCondition(Map<String, Object> condition);

    /**
     * 条件查询记录数
     */
    Long getCountByCondition(Map<String, Object> condition);

    /**
     * 条件分页查询
     */
    List<StudentScore> getPageByCondition(Map<String, Object> condition, Integer pageNum, Integer pageSize);
    
    /**
     * 根据学生ID查询成绩列表
     */
    List<StudentScore> getByStudentId(Integer studentId);
    
    /**
     * 根据考试ID查询成绩列表
     */
    List<StudentScore> getByExamId(Integer examId);
    
    /**
     * 查询学生在指定考试的成绩
     */
    StudentScore getByExamAndStudent(Integer examId, Integer studentId);
    
    /**
     * 根据分数范围查询成绩列表
     */
    List<StudentScore> getByScoreRange(Integer examId, BigDecimal minScore, BigDecimal maxScore);
    
    /**
     * 统计考试的成绩信息
     */
    List<StudentScore> getExamScoreStats(Integer examId);
    
    /**
     * 批量插入成绩
     */
    int batchInsertScores(List<StudentScore> scores);
    
    /**
     * 批量更新成绩
     */
    int batchUpdateScores(List<StudentScore> scores);
    
    /**
     * 查询学生成绩排名
     */
    Integer getStudentRank(Integer examId, Integer studentId);
    
    /**
     * 查询班级成绩排名
     */
    List<Map<String, Object>> getClassRanking(Integer classId, Integer examId);
    
    /**
     * 查询年级成绩排名
     */
    List<Map<String, Object>> getGradeRanking(String grade, Integer examId);
    
    /**
     * 查询学生成绩趋势
     */
    List<Map<String, Object>> getScoreTrend(Integer studentId, Integer subjectId, 
                                          Date startTime, Date endTime);
    
    /**
     * 查询学生各科目平均分
     */
    List<Map<String, Object>> getSubjectAverages(Integer studentId);
    
    /**
     * 查询成绩分布
     */
    List<Map<String, Object>> getScoreDistribution(Integer examId, Integer classId);
    
    /**
     * 查询优秀学生名单
     */
    List<Map<String, Object>> getTopStudents(Integer examId, Integer limit);
    
    /**
     * 查询需要补考的学生
     */
    List<Map<String, Object>> getRetakeStudents(Integer examId, BigDecimal passScore);
    
    /**
     * 统计学生成绩进步情况
     */
    List<Map<String, Object>> analyzeScoreImprovement(Integer studentId, Integer subjectId);
    
    /**
     * 导出成绩报告
     * @param examId 考试ID
     * @param studentId 学生ID
     * @return 成绩报告数据
     */
    Map<String, Object> exportScoreReport(Integer examId, Integer studentId);
    
    /**
     * 批量导入成绩
     * @param scores 成绩列表
     * @return 导入结果
     */
    int importScores(List<StudentScore> scores);
} 
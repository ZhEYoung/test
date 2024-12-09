package com.exam.mapper;

import com.exam.entity.StudentScore;
import org.apache.ibatis.annotations.Param;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Date;

/**
 * 学生成绩Mapper接口
 */
public interface StudentScoreMapper {
    /**
     * 插入学生成绩
     */
    int insert(StudentScore record);

    /**
     * 根据主键删除成绩记录
     */
    int deleteByPrimaryKey(Integer scoreId);

    /**
     * 更新成绩记录
     */
    int updateByPrimaryKey(StudentScore record);

    /**
     * 根据主键查询成绩记录
     */
    StudentScore selectByPrimaryKey(Integer scoreId);

    /**
     * 查询所有成绩记录
     */
    List<StudentScore> selectAll();

    /**
     * 根据条件查询成绩记录
     */
    List<StudentScore> selectByCondition(StudentScore record);

    /**
     * 根据学生ID查询成绩列表
     */
    List<StudentScore> selectByStudentId(@Param("studentId") Integer studentId);

    /**
     * 根据考试ID查询成绩列表
     */
    List<StudentScore> selectByExamId(@Param("examId") Integer examId);

    /**
     * 查询学生在指定考试的成绩
     */
    StudentScore selectByExamAndStudent(@Param("examId") Integer examId, @Param("studentId") Integer studentId);

    /**
     * 根据分数范围查询成绩列表
     */
    List<StudentScore> selectByScoreRange(
        @Param("examId") Integer examId, 
        @Param("minScore") BigDecimal minScore, 
        @Param("maxScore") BigDecimal maxScore
    );

    /**
     * 统计考试的成绩信息
     */
    List<StudentScore> selectExamScoreStats(@Param("examId") Integer examId);

    /**
     * 批量插入成绩
     */
    int batchInsertScores(@Param("scores") List<StudentScore> scores);

    /**
     * 批量更新成绩
     */
    int batchUpdateScores(@Param("scores") List<StudentScore> scores);

    /**
     * 查询学生成绩排名
     */
    Integer selectStudentRank(
        @Param("examId") Integer examId,
        @Param("studentId") Integer studentId
    );

    /**
     * 查询班级成绩排名
     */
    List<Map<String, Object>> selectClassRanking(
        @Param("classId") Integer classId,
        @Param("examId") Integer examId
    );

    /**
     * 查询年级成绩排名
     */
    List<Map<String, Object>> selectGradeRanking(
        @Param("grade") String grade,
        @Param("examId") Integer examId
    );

    /**
     * 查询学生成绩趋势
     */
    List<Map<String, Object>> selectScoreTrend(
        @Param("studentId") Integer studentId,
        @Param("subjectId") Integer subjectId,
        @Param("startTime") Date startTime,
        @Param("endTime") Date endTime
    );

    /**
     * 查询学生各科目平均分
     */
    List<Map<String, Object>> selectSubjectAverages(
        @Param("studentId") Integer studentId
    );

    /**
     * 查询成绩分布
     */
    List<Map<String, Object>> selectScoreDistribution(
        @Param("examId") Integer examId,
        @Param("classId") Integer classId
    );

    /**
     * 查询优秀学生名单
     */
    List<Map<String, Object>> selectTopStudents(
        @Param("examId") Integer examId,
        @Param("limit") Integer limit
    );

    /**
     * 查询需要补考的学生
     */
    List<Map<String, Object>> selectRetakeStudents(
        @Param("examId") Integer examId,
        @Param("passScore") BigDecimal passScore
    );

    /**
     * 统计学生成绩进步情况
     */
    List<Map<String, Object>> analyzeScoreImprovement(
        @Param("studentId") Integer studentId,
        @Param("subjectId") Integer subjectId
    );
} 
package com.exam.mapper;

import com.exam.entity.ExamClass;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 考试-班级关联Mapper接口
 */
public interface ExamClassMapper extends BaseMapper<ExamClass> {
    /**
     * 根据考试ID查询班级关联列表
     */
    List<ExamClass> selectByExamId(@Param("examId") Integer examId);

    /**
     * 根据班级ID查询考试关联列表
     */
    List<ExamClass> selectByClassId(@Param("classId") Integer classId);

    /**
     * 根据考试ID删除所有班级关联
     */
    int deleteByExamId(@Param("examId") Integer examId);

    /**
     * 批量插入考试班级关联
     */
    int batchInsert(@Param("list") List<ExamClass> list);

    /**
     * 批量删除考试班级关联
     */
    int batchDelete(@Param("examId") Integer examId, @Param("classIds") List<Integer> classIds);

    /**
     * 查询班级考试时间安排
     */
    List<Map<String, Object>> selectClassExamSchedule(
        @Param("classId") Integer classId,
        @Param("startTime") Date startTime,
        @Param("endTime") Date endTime
    );

    /**
     * 统计班级考试数量
     */
    Map<String, Object> countClassExams(
        @Param("classId") Integer classId,
        @Param("startTime") Date startTime,
        @Param("endTime") Date endTime
    );

    /**
     * 统计班级平均成绩
     */
    BigDecimal calculateClassAvgScore(@Param("classId") Integer classId, @Param("examId") Integer examId);

    /**
     * 统计班级成绩分布
     */
    List<Map<String, Object>> analyzeClassScoreDistribution(
        @Param("classId") Integer classId,
        @Param("examId") Integer examId
    );

    /**
     * 查询班级排名
     */
    List<Map<String, Object>> selectClassRanking(@Param("examId") Integer examId);

    /**
     * 统计班级及格率
     */
    Map<String, Object> calculateClassPassRate(
        @Param("classId") Integer classId,
        @Param("examId") Integer examId,
        @Param("passScore") BigDecimal passScore
    );

    /**
     * 查询班级考试完成情况
     */
    Map<String, Object> selectClassExamCompletion(
        @Param("classId") Integer classId,
        @Param("examId") Integer examId
    );

    /**
     * 查询班级考试时间冲突
     */
    List<Map<String, Object>> checkTimeConflict(
        @Param("classId") Integer classId,
        @Param("examTime") Date examTime,
        @Param("duration") Integer duration
    );

    /**
     * 统计教师监考班级
     */
    List<Map<String, Object>> countTeacherInvigilateClasses(
        @Param("teacherId") Integer teacherId,
        @Param("startTime") Date startTime,
        @Param("endTime") Date endTime
    );

    /**
     * 查询班级考试历史
     */
    List<Map<String, Object>> selectClassExamHistory(
        @Param("classId") Integer classId,
        @Param("subjectId") Integer subjectId
    );
} 
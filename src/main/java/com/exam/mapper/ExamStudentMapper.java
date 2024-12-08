package com.exam.mapper;

import com.exam.entity.ExamStudent;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;
import java.util.Date;

/**
 * 考试-学生关联Mapper接口
 */
public interface ExamStudentMapper extends BaseMapper<ExamStudent> {
    /**
     * 根据考试ID查询学生关联列表
     */
    List<ExamStudent> selectByExamId(@Param("examId") Integer examId);

    /**
     * 根据学生ID查询考试关联列表
     */
    List<ExamStudent> selectByStudentId(@Param("studentId") Integer studentId);

    /**
     * 查询学生在指定考试的关联记录
     */
    ExamStudent selectByExamAndStudent(@Param("examId") Integer examId, @Param("studentId") Integer studentId);

    /**
     * 更新考试状态（缺考、违纪等）
     */
    int updateStatus(
        @Param("esId") Integer esId, 
        @Param("absent") Boolean absent,
        @Param("retakeNeeded") Boolean retakeNeeded,
        @Param("disciplinary") Boolean disciplinary,
        @Param("teacherComments") String teacherComments
    );

    /**
     * 批量插入考试学生关联
     */
    int batchInsert(@Param("list") List<ExamStudent> list);

    /**
     * 更新学生考试开始和提交时间
     */
    int updateExamTime(
        @Param("examId") Integer examId, 
        @Param("studentId") Integer studentId,
        @Param("startTime") Date startTime,
        @Param("submitTime") Date submitTime
    );

    /**
     * 查询需要重考的学生列表
     */
    List<ExamStudent> selectNeedRetakeStudents(@Param("examId") Integer examId);

    /**
     * 查询学生的重考记录
     */
    List<ExamStudent> selectStudentRetakeExams(@Param("studentId") Integer studentId);

    /**
     * 按学科查询需要重考的学生列表
     */
    List<Map<String, Object>> selectRetakeStudentsBySubject(
        @Param("subjectId") Integer subjectId,
        @Param("teacherId") Integer teacherId,
        @Param("studentName") String studentName,
        @Param("studentNo") String studentNo,
        @Param("examTimeStart") Date examTimeStart,
        @Param("examTimeEnd") Date examTimeEnd
    );

    /**
     * 统计某学科的重考情况
     */
    Map<String, Object> countRetakeBySubject(
        @Param("subjectId") Integer subjectId,
        @Param("teacherId") Integer teacherId
    );

    /**
     * 批量更新考试状态
     */
    int batchUpdateStatus(
        @Param("examId") Integer examId,
        @Param("studentIds") List<Integer> studentIds,
        @Param("status") Map<String, Boolean> status
    );

    /**
     * 查询考试进行状态
     */
    List<Map<String, Object>> selectExamProgress(@Param("examId") Integer examId);

    /**
     * 查询考试时间异常的学生
     */
    List<Map<String, Object>> selectAbnormalTimeStudents(
        @Param("examId") Integer examId,
        @Param("timeThreshold") Integer timeThreshold
    );

    /**
     * 统计考试参与情况
     */
    Map<String, Object> countExamParticipation(@Param("examId") Integer examId);

    /**
     * 查询学生考试时长统计
     */
    List<Map<String, Object>> selectExamDurationStats(@Param("examId") Integer examId);

    /**
     * 查询考试违纪情况
     */
    List<Map<String, Object>> selectDisciplinaryStats(
        @Param("examId") Integer examId,
        @Param("startTime") Date startTime,
        @Param("endTime") Date endTime
    );

    /**
     * 更新考试监考记录
     */
    int updateInvigilationRecord(
        @Param("examId") Integer examId,
        @Param("studentId") Integer studentId,
        @Param("record") String record
    );

    /**
     * 批量更新考试时间
     */
    int batchUpdateExamTime(
        @Param("examId") Integer examId,
        @Param("studentTimes") List<Map<String, Object>> studentTimes
    );

    /**
     * 查询考试完成率
     */
    Map<String, Object> selectExamCompletionRate(
        @Param("examId") Integer examId,
        @Param("classId") Integer classId
    );
} 
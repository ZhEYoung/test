package com.exam.mapper;

import com.exam.entity.ExamStudent;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;
import java.util.Date;

/**
 * 考试-学生关联Mapper接口
 */
@Mapper
public interface ExamStudentMapper {
    /**
     * 插入考试学生关联记录
     * @param examStudent 考试学生关联实体
     * @return 影响行数
     */
    int insert(ExamStudent examStudent);

    /**
     * 根据ID删除考试学生关联
     * @param esId 关联ID
     * @return 影响行数
     */
    int deleteById(@Param("esId") Integer esId);

    /**
     * 更新考试学生关联信息
     * @param examStudent 考试学生关联实体
     * @return 影响行数
     */
    int update(ExamStudent examStudent);

    /**
     * 根据ID查询考试学生关联
     * @param esId 关联ID
     * @return 考试学生关联实体
     */
    ExamStudent selectById(@Param("esId") Integer esId);

    /**
     * 查询所有考试学生关联
     * @return 考试学生关联列表
     */
    List<ExamStudent> selectAll();

    /**
     * 根据考试ID查询学生关联列表
     * @param examId 考试ID
     * @return 考试学生关联列表
     */
    List<ExamStudent> selectByExamId(@Param("examId") Integer examId);

    /**
     * 根据学生ID查询考试关联列表
     * @param studentId 学生ID
     * @return 考试学生关联列表
     */
    List<ExamStudent> selectByStudentId(@Param("studentId") Integer studentId);

    /**
     * 查询学生在指定考试的关联记录
     * @param examId 考试ID
     * @param studentId 学生ID
     * @return 考试学生关联实体
     */
    ExamStudent selectByExamAndStudent(@Param("examId") Integer examId, @Param("studentId") Integer studentId);

    /**
     * 更新考试状态（缺考、违纪等）
     * @param esId 关联ID
     * @param absent 缺考标记
     * @param retakeNeeded 是否需要重考
     * @param disciplinary 违纪标记
     * @param teacherComments 教师评语
     * @return 影响行数
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
     * @param list 考试学生关联列表
     * @return 影响行数
     */
    int batchInsert(@Param("list") List<ExamStudent> list);

    /**
     * 更新学生考试开始和提交时间
     * @param examId 考试ID
     * @param studentId 学生ID
     * @param startTime 开始时间
     * @param submitTime 提交时间
     * @return 影响行数
     */
    int updateExamTime(
        @Param("examId") Integer examId, 
        @Param("studentId") Integer studentId,
        @Param("startTime") Date startTime,
        @Param("submitTime") Date submitTime
    );

    /**
     * 查询需要重考的学生列表
     * @param examId 考试ID
     * @return 需要重考的学生列表
     */
    List<ExamStudent> selectNeedRetakeStudents(@Param("examId") Integer examId);

    /**
     * 查询学生的重考记录
     * @param studentId 学生ID
     * @return 重考记录列表
     */
    List<ExamStudent> selectStudentRetakeExams(@Param("studentId") Integer studentId);

    /**
     * 按学科查询需要重考的学生列表
     * @param subjectId 学科ID
     * @param teacherId 教师ID
     * @param studentName 学生姓名
     * @param studentNo 学号
     * @param examTimeStart 考试开始时间
     * @param examTimeEnd 考试结束时间
     * @return 需要重考的学生信息��表
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
     * @param subjectId 学科ID
     * @param teacherId 教师ID
     * @return 重考统计信息
     */
    Map<String, Object> countRetakeBySubject(
        @Param("subjectId") Integer subjectId,
        @Param("teacherId") Integer teacherId
    );

    /**
     * 批量更新考试状态
     * @param examId 考试ID
     * @param studentIds 学生ID列表
     * @param status 状态信息
     * @return 影响行数
     */
    int batchUpdateStatus(
        @Param("examId") Integer examId,
        @Param("studentIds") List<Integer> studentIds,
        @Param("status") Map<String, Boolean> status
    );

    /**
     * 查询考试进行状态
     * @param examId 考试ID
     * @return 考试进行状态信息
     */
    List<Map<String, Object>> selectExamProgress(@Param("examId") Integer examId);

    /**
     * 查询考试时间异常的学生
     * @param examId ��试ID
     * @param timeThreshold 时间阈值（分钟）
     * @return 时间异常的学生信息列表
     */
    List<Map<String, Object>> selectAbnormalTimeStudents(
        @Param("examId") Integer examId,
        @Param("timeThreshold") Integer timeThreshold
    );

    /**
     * 统计考试参与情况
     * @param examId 考试ID
     * @return 考试参与统计信息
     */
    Map<String, Object> countExamParticipation(@Param("examId") Integer examId);

    /**
     * 查询学生考试时长统计
     * @param examId 考试ID
     * @return 考试时长统计信息
     */
    List<Map<String, Object>> selectExamDurationStats(@Param("examId") Integer examId);

    /**
     * 查询考试违纪情况
     * @param examId 考试ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 违纪情况统计信息
     */
    List<Map<String, Object>> selectDisciplinaryStats(
        @Param("examId") Integer examId,
        @Param("startTime") Date startTime,
        @Param("endTime") Date endTime
    );

    /**
     * 更新考试监考记录
     * @param examId 考试ID
     * @param studentId 学生ID
     * @param record 监考记录
     * @return 影响行数
     */
    int updateInvigilationRecord(
        @Param("examId") Integer examId,
        @Param("studentId") Integer studentId,
        @Param("record") String record
    );

    /**
     * 批量更新考试时间
     * @param examId 考试ID
     * @param studentTimes 学生时间信息列表
     * @return 影响行数
     */
    int batchUpdateExamTime(
        @Param("examId") Integer examId,
        @Param("studentTimes") List<Map<String, Object>> studentTimes
    );




    /**
     * 更新学生开始考试时间
     */
    int updateStartTime(Map<String, Object> params);
    
    /**
     * 更新学生提交考试时间
     */
    int updateSubmitTime(Map<String, Object> params);
    
    /**
     * 更新缺考状态
     */
    int updateAbsentStatus(Map<String, Object> params);
    
    /**
     * 更新违纪状态
     */
    int updateDisciplinaryStatus(Map<String, Object> params);

    
    /**
     * 批量更新考试时间
     */
    int batchUpdateExamTime(Map<String, Object> params);
    
    /**
     * 查询考试完成率
     */
    Map<String, Object> selectExamCompletionRate(@Param("examId") Integer examId, 
                                               @Param("classId") Integer classId);

    
    /**
     * 查询违纪记录
     */
    List<Map<String, Object>> selectDisciplinaryRecords(@Param("startTime") Date startTime, 
                                                      @Param("endTime") Date endTime);
    
    /**
     * 统计考试总人数
     */
    int countByExamId(@Param("examId") Integer examId);
    
    /**
     * 统计已开始考试的学生人数
     */
    int countStartedStudents(@Param("examId") Integer examId);
    
    /**
     * 统计已提交考试的学生人数
     */
    int countSubmittedStudents(@Param("examId") Integer examId);
    
    /**
     * 统计缺考学生人数
     */
    int countAbsentStudents(@Param("examId") Integer examId);
}
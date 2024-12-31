package com.exam.service;

import com.exam.entity.ExamStudent;

import java.util.List;
import java.util.Map;
import java.util.Date;

/**
 * 考试-学生服务接口
 */
public interface ExamStudentService {
    
    /**
     * 根据考试ID查询所有考试-学生记录
     * @param examId 考试ID
     * @return 考试-学生记录列表
     */
    List<ExamStudent> getByExamId(Integer examId);

    /**
     * 根据学生ID查询所有考试-学生记录
     * @param studentId 学生ID
     * @return 考试-学生记录列表
     */
    List<ExamStudent> getByStudentId(Integer studentId);

    /**
     * 根据考试ID和学生ID查询考试-学生记录
     * @param examId 考试ID
     * @param studentId 学生ID
     * @return 考试-学生记录
     */
    ExamStudent getByExamIdAndStudentId(Integer examId, Integer studentId);
    
    /**
     * 根据主键删除考试-学生记录
     * @param esId 考试-学生记录ID
     * @return 影响行数
     */
    int deleteById(Integer esId);
    
    /**
     * 记录学生开始考试
     * @param examId 考试ID
     * @param studentId 学生ID
     * @return 操作结果
     */
    int recordStartExam(Integer examId, Integer studentId);
    
    /**
     * 记录学生提交考试
     * @param examId 考试ID
     * @param studentId 学生ID
     * @return 操作结果
     */
    int recordSubmitExam(Integer examId, Integer studentId);
    
    /**
     * 标记学生缺考
     * @param examId 考试ID
     * @param studentId 学生ID
     * @return 操作结果
     */
    int markAbsent(Integer examId, Integer studentId);
    
    /**
     * 标记学生违纪
     * @param examId 考试ID
     * @param studentId 学生ID
     * @param comment 违纪说明
     * @return 操作结果
     */
    int markDisciplinary(Integer examId, Integer studentId, String comment);
    
    /**
     * 添加教师评语
     * @param examId 考试ID
     * @param studentId 学生ID
     * @param comment 评语内容
     * @return 操作结果
     */
    int addTeacherComment(Integer examId, Integer studentId, String comment);
    
    /**
     * 查询考试的学生参与情况
     * @param examId 考试ID
     * @return 参与情况统计
     */
    Map<String, Object> getExamParticipation(Integer examId);
    
    /**
     * 查询考试完成率
     * @param examId 考试ID
     * @param classId 班级ID（可选）
     * @return 完成率统计
     */
    Map<String, Object> getExamCompletionRate(Integer examId, Integer classId);
    
    /**
     * 查询考试时间异常的学生
     * @param examId 考试ID
     * @param timeThreshold 时间阈值（分钟）
     * @return 异常学生列表
     */
    List<Map<String, Object>> getAbnormalTimeStudents(Integer examId, Integer timeThreshold);
    
    /**
     * 查询违纪学生
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 违纪记录
     */
    List<Map<String, Object>> getDisciplinaryRecords(Date startTime, Date endTime);
    
    /**
     * 批量更新考试时间
     * @param examId 考试ID
     * @param studentTimes 学生考试时间信息
     * @return 更新结果
     */
    int batchUpdateExamTime(Integer examId, List<Map<String, Object>> studentTimes);
    
    /**
     * 统计考试总人数
     * @param examId 考试ID
     * @return 总人数
     */
    int countTotalStudents(Integer examId);
    
    /**
     * 统计已开始考试人数
     * @param examId 考试ID
     * @return 已开始人数
     */
    int countStartedStudents(Integer examId);
    
    /**
     * 统计已提交考试人数
     * @param examId 考试ID
     * @return 已提交人数
     */
    int countSubmittedStudents(Integer examId);
    
    /**
     * 统计缺考人数
     * @param examId 考试ID
     * @return 缺考人数
     */
    int countAbsentStudents(Integer examId);
    
    /**
     * 更新学生考试开始和提交时间
     * @param examId 考试ID
     * @param studentId 学生ID
     * @param startTime 开始时间
     * @param submitTime 提交时间
     * @return 影响行数
     */
    int updateExamTime(Integer examId, Integer studentId, Date startTime, Date submitTime);
    
    /**
     * 查询需要重考的学生列表
     * @param examId 考试ID
     * @return 需要重考的学生列表
     */
    List<ExamStudent> getNeedRetakeStudents(Integer examId);
    
    /**
     * 查询学生的重考记录
     * @param studentId 学生ID
     * @return 重考记录列表
     */
    List<ExamStudent> getStudentRetakeExams(Integer studentId);
    
    /**
     * 按学科查询需要重考的学生列表
     * @param subjectId 学科ID
     * @param teacherId 教师ID
     * @param studentName 学生姓名
     * @param examTimeStart 考试开始时间
     * @param examTimeEnd 考试结束时间
     * @return 需要重考的学生信息列表
     */
    List<Map<String, Object>> getRetakeStudentsBySubject(
        Integer subjectId,
        Integer teacherId,
        String studentName,
        Date examTimeStart,
        Date examTimeEnd
    );
    
    /**
     * 统计某学科的重考情况
     * @param subjectId 学科ID
     * @param teacherId 教师ID
     * @return 重考统计信息
     */
    Map<String, Object> countRetakeBySubject(Integer subjectId, Integer teacherId);

    /**
     * 标记学生需要重考
     * @param examId 考试ID
     * @param studentId 学生ID
     * @return 操作结果
     */
    int markRetakeNeeded(Integer examId, Integer studentId);

    /**
     * 标记学生不需要重考
     * @param examId 考试ID
     * @param studentId 学生ID
     * @return 操作结果
     */
    int markRetakeNotNeeded(Integer examId, Integer studentId);

    /**
     * 获取未提交考试的学生列表
     * @param examId 考试ID
     * @return 未提交考试的学生列表
     */
    List<ExamStudent> getUnsubmittedStudents(Integer examId);

    /**
     * 获取未开始考试的学生列表
     * @param examId 考试ID
     * @return 未开始考试的学生列表
     */
    List<ExamStudent> getNotStartedStudents(Integer examId);
} 
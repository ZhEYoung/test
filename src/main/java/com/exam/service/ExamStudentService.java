package com.exam.service;

import java.util.List;
import java.util.Map;
import java.util.Date;

/**
 * 考试-学生服务接口
 */
public interface ExamStudentService {
    
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
} 
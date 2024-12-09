package com.exam.service;

import com.exam.entity.Exam;
import java.util.List;
import java.util.Map;
import java.util.Date;

/**
 * 考试服务接口
 */
public interface ExamService {
    
    /**
     * 插入一条记录
     */
    int insert(Exam record);

    /**
     * 根据ID删除
     */
    int deleteById(Integer id);

    /**
     * 根据ID更新
     */
    int updateById(Exam record);

    /**
     * 根据ID查询
     */
    Exam selectById(Integer id);

    /**
     * 查询所有记录
     */
    List<Exam> selectAll();

    /**
     * 分页查询
     */
    List<Exam> selectPage(Integer pageNum, Integer pageSize);

    /**
     * 查询总记录数
     */
    Long selectCount();

    /**
     * 条件查询
     */
    List<Exam> selectByCondition(Map<String, Object> condition);

    /**
     * 条件查询记录数
     */
    Long selectCountByCondition(Map<String, Object> condition);

    /**
     * 条件分页查询
     */
    List<Exam> selectPageByCondition(Map<String, Object> condition, Integer pageNum, Integer pageSize);
    
    /**
     * 根据学科ID查询考试列表
     */
    List<Exam> getBySubjectId(Integer subjectId);
    
    /**
     * 根据试卷ID查询考试列表
     */
    List<Exam> getByPaperId(Integer paperId);
    
    /**
     * 根据教师ID查询考试列表
     */
    List<Exam> getByTeacherId(Integer teacherId);
    
    /**
     * 根据考试状态查询
     * @param examStatus 0: 未开始；1: 进行中；2: 已结束
     */
    List<Exam> getByStatus(Integer examStatus);
    
    /**
     * 根据考试类型查询
     * @param examType 0: 正常考试；1: 重考
     */
    List<Exam> getByType(Integer examType);
    
    /**
     * 更新考试状态
     */
    int updateStatus(Integer examId, Integer status);
    
    /**
     * 查询学生参加的考试列表
     */
    List<Exam> getByStudentId(Integer studentId);
    
    /**
     * 查询班级的考试列表
     */
    List<Exam> getByClassId(Integer classId);
    
    /**
     * 根据时间范围查询考试
     */
    List<Exam> getByTimeRange(Date startTime, Date endTime);
    
    /**
     * 高级查询（支持多条件组合）
     */
    List<Exam> getByConditions(Integer subjectId, Integer teacherId, 
                             Integer examType, Integer examStatus,
                             Date startTime, Date endTime,
                             Integer pageNum, Integer pageSize);
    
    /**
     * 统计某个时间段内的考试数量
     */
    Long countByTimeRange(Date startTime, Date endTime);
    
    /**
     * 更新考试时长
     */
    int updateDuration(Integer examId, Integer duration);
    
    /**
     * 批量添加考试班级关联
     */
    int batchAddExamClass(Integer examId, List<Integer> classIds);
    
    /**
     * 删除考试班级关联
     */
    int removeExamClass(Integer examId, Integer classId);
    
    /**
     * 发布考试
     * @param examId 考试ID
     * @return 发布结果
     */
    int publishExam(Integer examId);
    
    /**
     * 开始考试
     * @param examId 考试ID
     * @return 开始结果
     */
    int startExam(Integer examId);
    
    /**
     * 结束考试
     * @param examId 考试ID
     * @return 结束结果
     */
    int endExam(Integer examId);
    
    /**
     * 获取考试统计信息
     * @param examId 考试ID
     * @return 统计信息
     */
    Map<String, Object> getExamStatistics(Integer examId);
    
    /**
     * 获取考试进行状态
     * @param examId 考试ID
     * @return 进行状态
     */
    Map<String, Object> getExamProgress(Integer examId);
} 
package com.exam.mapper;

import com.exam.entity.Exam;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Date;
import java.util.Map;

/**
 * 考试管理Mapper接口
 */
@Mapper
public interface ExamMapper {
    
    /**
     * 插入考试记录
     * @param exam 考试实体
     * @return 影响行数
     */
    int insert(Exam exam);

    /**
     * 根据ID删除考试记录
     * @param examId 考试ID
     * @return 影响行数
     */
    int deleteById(@Param("examId") Integer examId);

    /**
     * 更新考试记录
     * @param exam 考试实体
     * @return 影响行数
     */
    int update(Exam exam);

    /**
     * 根据ID查询考试
     * @param examId 考试ID
     * @return 考试实体
     */
    Exam selectById(@Param("examId") Integer examId);

    /**
     * 查询所有考试
     * @return 考试列表
     */
    List<Exam> selectAll();
    
    /**
     * 根据学科ID查询考试列表
     * @param subjectId 学科ID
     * @return 考试列表
     */
    List<Exam> selectBySubjectId(@Param("subjectId") Integer subjectId);
    
    /**
     * 根据试卷ID查询考试列表
     * @param paperId 试卷ID
     * @return 考试列表
     */
    List<Exam> selectByPaperId(@Param("paperId") Integer paperId);
    
    /**
     * 根据教师ID查询考试列表
     * @param teacherId 教师ID
     * @return 考试列表
     */
    List<Exam> selectByTeacherId(@Param("teacherId") Integer teacherId);
    
    /**
     * 根据考试状态查询
     * @param examStatus 考试状态
     * @return 考试列表
     */
    List<Exam> selectByStatus(@Param("examStatus") Integer examStatus);
    
    /**
     * 根据考试类型查询
     * @param examType 考试类型
     * @return 考试列表
     */
    List<Exam> selectByType(@Param("examType") Integer examType);
    
    /**
     * 更新考试状态
     * @param examId 考试ID
     * @param status 状态
     * @return 更新结果
     */
    int updateStatus(@Param("examId") Integer examId, @Param("status") Integer status);
    
    /**
     * 查询学生参加的考试列表
     * @param studentId 学生ID
     * @return 考试列表
     */
    List<Exam> selectByStudentId(@Param("studentId") Integer studentId);
    
    /**
     * 查询班级的考试列表
     * @param classId 班级ID
     * @return 考试列表
     */
    List<Exam> selectByClassId(@Param("classId") Integer classId);
    
    /**
     * 根据时间范围查询考试
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 考试列表
     */
    List<Exam> selectByTimeRange(@Param("startTime") Date startTime, @Param("endTime") Date endTime);
    
    /**
     * 高级查询（支持多条件组合）
     * @param subjectId 学科ID
     * @param teacherId 教师ID
     * @param examType 考试类型
     * @param examStatus 考试状态
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param offset 偏移量
     * @param limit 每页记录数
     * @return 考试列表
     */
    List<Exam> selectByConditions(
        @Param("subjectId") Integer subjectId,
        @Param("teacherId") Integer teacherId,
        @Param("examType") Integer examType,
        @Param("examStatus") Integer examStatus,
        @Param("startTime") Date startTime,
        @Param("endTime") Date endTime,
        @Param("offset") Integer offset,
        @Param("limit") Integer limit
    );
    
    /**
     * 统计某个时间段内的考试数量
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 考试数量
     */
    Long countByTimeRange(@Param("startTime") Date startTime, @Param("endTime") Date endTime);
    
    /**
     * 更新考试时长
     * @param examId 考试ID
     * @param duration 考试时长（分钟）
     * @return 更新结果
     */
    int updateDuration(@Param("examId") Integer examId, @Param("duration") Integer duration);
    
    /**
     * 批量插入考试班级关联
     * @param examId 考试ID
     * @param classIds 班级ID列表
     * @return 插入结果
     */
    int batchInsertExamClass(@Param("examId") Integer examId, @Param("classIds") List<Integer> classIds);


    /**
     * 删除考试班级关联
     * @param examId 考试ID
     * @param classId 班级ID
     * @return 删除结果
     */
    int deleteExamClass(@Param("examId") Integer examId, @Param("classId") Integer classId);


    /**
     * 查询教师权限
     * @param teacherId 教师ID
     * @return 教师权限 0-超级管理员
     */
    Integer selectTeacherPermission(@Param("teacherId") Integer teacherId);

    /**
     * 查询教师信息（权限和所属学院）
     * @param teacherId 教师ID
     * @return 教师信息
     */
    Map<String, Object> selectTeacherInfo(@Param("teacherId") Integer teacherId);

    /**
     * 查询科目所属学院ID
     * @param subjectId 科目ID
     * @return 学院ID
     */
    Integer selectSubjectCollegeId(@Param("subjectId") Integer subjectId);

    /**
     * 插入考试-班级关联记录
     * @param params 包含examId和classId的Map
     * @return 影响的行数
     */
    int insertExamClass(Map<String, Object> params);

    /**
     * 获取考试剩余时间信息
     */
    Map<String, Object> getRemainingTime(Integer examId);

    /**
     * 获取刚刚结束的考试
     */
    List<Exam> selectRecentlyEndedExams(Map<String, Object> params);

    /**
     * 获取最近开始的考试
     */
    List<Exam> selectRecentlyStartedExams(Map<String, Object> params);
}
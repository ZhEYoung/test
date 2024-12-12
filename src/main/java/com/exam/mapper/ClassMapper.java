package com.exam.mapper;

import com.exam.entity.Class;
import com.exam.entity.Student;
import com.exam.entity.Exam;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;
import java.util.Date;

/**
 * 班级Mapper接口
 */
@Mapper
public interface ClassMapper {
    /**
     * 插入班级记录
     * @param clazz 班级实体
     * @return 影响的行数
     */
    int insert(Class clazz);

    /**
     * 批量插入班级记录
     * @param list 班级列表
     * @return 影响的行数
     */
    int batchInsert(@Param("list") List<Class> list);

    /**
     * 根据ID删除班级
     * @param classId 班级ID
     * @return 影响的行数
     */
    int deleteById(@Param("classId") Integer classId);

    /**
     * 批量删除班级
     * @param classIds ID列表
     * @return 影响的行数
     */
    int batchDelete(@Param("classIds") List<Integer> classIds);

    /**
     * 更新班级信息
     * @param clazz 班级实体
     * @return 影响的行数
     */
    int updateById(Class clazz);

    /**
     * 批量更新班级
     * @param list 班级列表
     * @return 影响的行数
     */
    int batchUpdate(@Param("list") List<Class> list);

    /**
     * 根据ID查询班级
     * @param classId 班级ID
     * @return 班级实体
     */
    Class selectById(@Param("classId") Integer classId);

    /**
     * 查询所有班级
     * @return 班级列表
     */
    List<Class> selectAll();

    /**
     * 分页查询班级
     * @param offset 偏移量
     * @param limit 每页记录数
     * @return 班级列表
     */
    List<Class> selectPage(@Param("offset") Integer offset, @Param("limit") Integer limit);

    /**
     * 查询班级总数
     * @return 总记录数
     */
    Long selectCount();

    /**
     * 条件查询班级
     * @param condition 查询条件
     * @return 班级列表
     */
    List<Class> selectByCondition(@Param("condition") Map<String, Object> condition);

    /**
     * 条件查询班级数量
     * @param condition 查询条件
     * @return 记录数
     */
    Long selectCountByCondition(@Param("condition") Map<String, Object> condition);

    /**
     * 条件分页查询班级
     * @param condition 查询条件
     * @param offset 偏移量
     * @param limit 每页记录数
     * @return 班级列表
     */
    List<Class> selectPageByCondition(
        @Param("condition") Map<String, Object> condition,
        @Param("offset") Integer offset,
        @Param("limit") Integer limit
    );

    /**
     * 根据教师ID查询班级列表
     * @param teacherId 教师ID
     * @return 班级列表
     */
    List<Class> selectByTeacherId(@Param("teacherId") Integer teacherId);

    /**
     * 根据学科ID查询班级列表
     * @param subjectId 学科ID
     * @return 班级列表
     */
    List<Class> selectBySubjectId(@Param("subjectId") Integer subjectId);

    /**
     * 根据班级名称查询
     * @param className 班级名称
     * @return 班级实体
     */
    Class selectByClassName(@Param("className") String className);

    /**
     * 更新期末考试状态
     * @param classId 班级ID
     * @param finalExam 是否有期末考试
     * @return 影响的行数
     */
    int updateFinalExam(@Param("classId") Integer classId, @Param("finalExam") Boolean finalExam);

    /**
     * 查询班级学生列表
     * @param classId 班级ID
     * @return 学生列表
     */
    List<Student> selectClassStudents(@Param("classId") Integer classId);

    /**
     * 查询班级考试列表
     * @param classId 班级ID
     * @return 考试列表
     */
    List<Exam> selectClassExams(@Param("classId") Integer classId);

    /**
     * 查询班级期末考试
     * @param classId 班级ID
     * @return 考试实体
     */
    Exam selectFinalExam(@Param("classId") Integer classId);

    /**
     * 统计班级学生数量
     * @param classId 班级ID
     * @return 学生数量
     */
    Long countStudents(@Param("classId") Integer classId);

    /**
     * 统计班级考试数量
     * @param classId 班级ID
     * @return 考试数量
     */
    Long countExams(@Param("classId") Integer classId);

    /**
     * 查询班级平均成绩
     * @param classId 班级ID
     * @param examId 考试ID
     * @return 平均成绩
     */
    Double selectAvgScore(@Param("classId") Integer classId, @Param("examId") Integer examId);

    /**
     * 查询班级成绩分布
     * @param classId 班级ID
     * @param examId 考试ID
     * @return 成绩分布列表，格式：[{score_range: "90-100", count: 10}, ...]
     */
    List<Map<String, Object>> selectScoreDistribution(
        @Param("classId") Integer classId,
        @Param("examId") Integer examId
    );

    /**
     * 查询班级考试日程
     * @param classId 班级ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 考试列表
     */
    List<Exam> selectExamSchedule(
        @Param("classId") Integer classId,
        @Param("startTime") Date startTime,
        @Param("endTime") Date endTime
    );

    /**
     * 批量添加学生到班级
     * @param classId 班级ID
     * @param studentIds 学生ID列表
     * @return 影响的行数
     */
    int batchAddStudents(
        @Param("classId") Integer classId,
        @Param("studentIds") List<Integer> studentIds
    );

    /**
     * 批量移除班级学生
     * @param classId 班级ID
     * @param studentIds 学生ID列表
     * @return 影响的行数
     */
    int batchRemoveStudents(
        @Param("classId") Integer classId,
        @Param("studentIds") List<Integer> studentIds
    );
} 
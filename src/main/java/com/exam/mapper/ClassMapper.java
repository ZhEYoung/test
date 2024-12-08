package com.exam.mapper;

import com.exam.entity.Class;
import com.exam.entity.Student;
import com.exam.entity.Exam;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;
import java.util.Date;

/**
 * 班级Mapper接口
 */
public interface ClassMapper extends BaseMapper<Class> {
    /**
     * 根据教师ID查询班级列表
     */
    List<Class> selectByTeacherId(@Param("teacherId") Integer teacherId);

    /**
     * 根据学科ID查询班级列表
     */
    List<Class> selectBySubjectId(@Param("subjectId") Integer subjectId);

    /**
     * 根据班级名称查询
     */
    Class selectByClassName(@Param("className") String className);

    /**
     * 更新期末考试状态
     */
    int updateFinalExam(@Param("classId") Integer classId, @Param("finalExam") Boolean finalExam);

    /**
     * 查询班级学生列表
     */
    List<Student> selectClassStudents(@Param("classId") Integer classId);

    /**
     * 查询班级考试列表
     */
    List<Exam> selectClassExams(@Param("classId") Integer classId);

    /**
     * 查询班级期末考试
     */
    Exam selectFinalExam(@Param("classId") Integer classId);

    /**
     * 统计班级学生数量
     */
    Long countStudents(@Param("classId") Integer classId);

    /**
     * 统计班级考试数量
     */
    Long countExams(@Param("classId") Integer classId);

    /**
     * 查询班级平均成绩
     */
    Double selectAvgScore(@Param("classId") Integer classId, @Param("examId") Integer examId);

    /**
     * 查询班级成绩分布
     * @return 返回格式 [{score_range: "90-100", count: 10}, ...]
     */
    List<Map<String, Object>> selectScoreDistribution(
        @Param("classId") Integer classId,
        @Param("examId") Integer examId
    );

    /**
     * 查询班级考试日程
     */
    List<Exam> selectExamSchedule(
        @Param("classId") Integer classId,
        @Param("startTime") Date startTime,
        @Param("endTime") Date endTime
    );

    /**
     * 批量添加学生到班级
     */
    int batchAddStudents(
        @Param("classId") Integer classId,
        @Param("studentIds") List<Integer> studentIds
    );

    /**
     * 批量移除班级学生
     */
    int batchRemoveStudents(
        @Param("classId") Integer classId,
        @Param("studentIds") List<Integer> studentIds
    );
} 
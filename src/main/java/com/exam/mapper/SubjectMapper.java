package com.exam.mapper;

import com.exam.entity.Subject;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

/**
 * 学科Mapper接口
 */
public interface SubjectMapper extends BaseMapper<Subject> {
    /**
     * 根据学院ID查询学科列表
     */
    List<Subject> selectByCollegeId(@Param("collegeId") Integer collegeId);

    /**
     * 根据学科名称查询
     */
    Subject selectBySubjectName(@Param("subjectName") String subjectName);

    /**
     * 更新学科描述
     */
    int updateDescription(@Param("subjectId") Integer subjectId, @Param("description") String description);

    /**
     * 批量更新学科描述
     */
    int batchUpdateDescription(@Param("subjectIds") List<Integer> subjectIds, @Param("description") String description);

    /**
     * 查询有考试的学科列表
     */
    List<Subject> selectSubjectsWithExams();

    /**
     * 查询指定教师教授的学科列表
     */
    List<Subject> selectByTeacherId(@Param("teacherId") Integer teacherId);

    /**
     * 查询指定学生学习的学科列表
     */
    List<Subject> selectByStudentId(@Param("studentId") Integer studentId);

    /**
     * 统计各学科考试数量
     */
    List<Map<String, Object>> countExamsBySubject();

    /**
     * 统计各学科平均成绩
     */
    List<Map<String, Object>> avgScoreBySubject();

    /**
     * 查询热门学科（按学生数量排序）
     */
    List<Subject> selectHotSubjects(@Param("limit") Integer limit);

    /**
     * 查询难度较高的学科（按平均分排序）
     */
    List<Subject> selectDifficultSubjects(@Param("limit") Integer limit);
} 
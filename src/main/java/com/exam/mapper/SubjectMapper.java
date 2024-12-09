package com.exam.mapper;

import com.exam.entity.Subject;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

/**
 * 学科Mapper接口
 */
@Mapper
public interface SubjectMapper {
    /**
     * 插入学科记录
     * @param subject 学科实体
     * @return 影响行数
     */
    int insert(Subject subject);

    /**
     * 批量插入学科记录
     * @param subjects 学科列表
     * @return 影响行数
     */
    int batchInsert(@Param("subjects") List<Subject> subjects);

    /**
     * 根据ID删除学科
     * @param subjectId 学科ID
     * @return 影响行数
     */
    int deleteById(@Param("subjectId") Integer subjectId);

    /**
     * 批量删除学科
     * @param subjectIds 学科ID列表
     * @return 影响行数
     */
    int batchDelete(@Param("subjectIds") List<Integer> subjectIds);

    /**
     * 更新学科信息
     * @param subject 学科实体
     * @return 影响行数
     */
    int updateById(Subject subject);

    /**
     * 批量更新学科信息
     * @param subjects 学科列表
     * @return 影响行数
     */
    int batchUpdate(@Param("subjects") List<Subject> subjects);

    /**
     * 根据ID查询学科
     * @param subjectId 学科ID
     * @return 学科实体
     */
    Subject selectById(@Param("subjectId") Integer subjectId);

    /**
     * 查询所有学科
     * @return 学科列表
     */
    List<Subject> selectAll();

    /**
     * 分页查询学科列表
     * @param offset 偏移量
     * @param limit 每页记录数
     * @return 学科列表
     */
    List<Subject> selectPage(@Param("offset") Integer offset, @Param("limit") Integer limit);

    /**
     * 统计学科总数
     * @return 学科总数
     */
    int selectCount();

    /**
     * 根据条件查询学科
     * @param subject 查询条件
     * @return 学科列表
     */
    List<Subject> selectByCondition(Subject subject);

    /**
     * 根据学院ID查询学科列表
     * @param collegeId 学院ID
     * @return 学科列表
     */
    List<Subject> selectByCollegeId(@Param("collegeId") Integer collegeId);

    /**
     * 根据学科名称查询
     * @param subjectName 学科名称
     * @return 学科实体
     */
    Subject selectBySubjectName(@Param("subjectName") String subjectName);

    /**
     * 更新学科描述
     * @param subjectId 学科ID
     * @param description 描述
     * @return 影响行数
     */
    int updateDescription(@Param("subjectId") Integer subjectId, @Param("description") String description);

    /**
     * 批量更新学科描述
     * @param subjectIds 学科ID列表
     * @param description 描述
     * @return 影响行数
     */
    int batchUpdateDescription(@Param("subjectIds") List<Integer> subjectIds, @Param("description") String description);

    /**
     * 查询有考试的学科列表
     * @return 学科列表
     */
    List<Subject> selectSubjectsWithExams();

    /**
     * 查询指定教师教授的学科列表
     * @param teacherId 教师ID
     * @return 学科列表
     */
    List<Subject> selectByTeacherId(@Param("teacherId") Integer teacherId);

    /**
     * 查询指定学生学习的学科列表
     * @param studentId 学生ID
     * @return 学科列表
     */
    List<Subject> selectByStudentId(@Param("studentId") Integer studentId);

    /**
     * 统计各学科考试数量
     * @return 统计结果
     */
    List<Map<String, Object>> countExamsBySubject();

    /**
     * 统计各学科平均成绩
     * @return 统计结果
     */
    List<Map<String, Object>> avgScoreBySubject();

    /**
     * 查询热门学科（按学生数量排序）
     * @param limit 限制数量
     * @return 学科列表
     */
    List<Subject> selectHotSubjects(@Param("limit") Integer limit);

    /**
     * 查询难度较高的学科（按平均分排序）
     * @param limit 限制数量
     * @return 学科列表
     */
    List<Subject> selectDifficultSubjects(@Param("limit") Integer limit);

    /**
     * 统计学科关联的题库数量
     * @param subjectId 学科ID
     * @return 题库数量
     */
    Long countQuestionBanks(@Param("subjectId") Integer subjectId);

    /**
     * 删除指定学科关联的所有题库
     * @param subjectId 学科ID
     * @return 影响行数
     */
    int deleteQuestionBanks(@Param("subjectId") Integer subjectId);
} 
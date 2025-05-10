package com.exam.mapper;

import com.exam.entity.College;
import com.exam.entity.Teacher;
import com.exam.entity.Student;
import com.exam.entity.Subject;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

/**
 * 学院Mapper接口
 */
@Mapper
public interface CollegeMapper {
    /**
     * 插入学院记录
     * @param college 学院实体
     * @return 影响的行数
     */
    int insert(College college);

    /**
     * 批量插入学院记录
     * @param list 学院列表
     * @return 影响的行数
     */
    int batchInsert(@Param("list") List<College> list);

    /**
     * 根据ID删除学院
     * @param collegeId 学院ID
     * @return 影响的行数
     */
    int deleteById(@Param("collegeId") Integer collegeId);

    /**
     * 批量删除学院
     * @param collegeIds ID列表
     * @return 影响的行数
     */
    int batchDelete(@Param("collegeIds") List<Integer> collegeIds);

    /**
     * 更新学院信息
     * @param college 学院实体
     * @return 影响的行数
     */
    int updateById(College college);

    /**
     * 批量更新学院
     * @param list 学院列表
     * @return 影响的行数
     */
    int batchUpdate(@Param("list") List<College> list);

    /**
     * 根据ID查询学院
     * @param collegeId 学院ID
     * @return 学院实体
     */
    College selectById(@Param("collegeId") Integer collegeId);

    /**
     * 查询所有学院
     * @return 学院列表
     */
    List<College> selectAll();

    /**
     * 分页查询学院
     * @param offset 偏移量
     * @param limit 每页记录数
     * @return 学院列表
     */
    List<College> selectPage(@Param("offset") Integer offset, @Param("limit") Integer limit);

    /**
     * 查询学院总数
     * @return 总记录数
     */
    Long selectCount();

    /**
     * 条件查询学院
     * @param condition 查询条件
     * @return 学院列表
     */
    List<College> selectByCondition(@Param("condition") Map<String, Object> condition);

    /**
     * 条件查询学院数量
     * @param condition 查询条件
     * @return 记录数
     */
    Long selectCountByCondition(@Param("condition") Map<String, Object> condition);

    /**
     * 条件分页查询学院
     * @param condition 查询条件
     * @param offset 偏移量
     * @param limit 每页记录数
     * @return 学院列表
     */
    List<College> selectPageByCondition(
        @Param("condition") Map<String, Object> condition,
        @Param("offset") Integer offset,
        @Param("limit") Integer limit
    );

    /**
     * 根据学院名称查询
     * @param collegeName 学院名称
     * @return 学院实体
     */
    College selectByCollegeName(@Param("collegeName") String collegeName);

    /**
     * 查询学院教师列表
     * @param collegeId 学院ID
     * @return 教师列表
     */
    List<Teacher> selectCollegeTeachers(@Param("collegeId") Integer collegeId);

    /**
     * 查询学院学生列表
     * @param collegeId 学院ID
     * @return 学生列表
     */
    List<Student> selectCollegeStudents(@Param("collegeId") Integer collegeId);

    /**
     * 查询学院学科列表
     * @param collegeId 学院ID
     * @return 学科列表
     */
    List<Subject> selectCollegeSubjects(@Param("collegeId") Integer collegeId);

    /**
     * 统计学院教师数量
     * @param collegeId 学院ID
     * @return 教师数量
     */
    Long countTeachers(@Param("collegeId") Integer collegeId);

    /**
     * 统计学院学生数量
     * @param collegeId 学院ID
     * @return 学生数量
     */
    Long countStudents(@Param("collegeId") Integer collegeId);

    /**
     * 统计学院学科数量
     * @param collegeId 学院ID
     * @return 学科数量
     */
    Long countSubjects(@Param("collegeId") Integer collegeId);

    /**
     * 更新学院描述
     * @param collegeId 学院ID
     * @param description 描述
     * @return 影响的行数
     */
    int updateDescription(@Param("collegeId") Integer collegeId, @Param("description") String description);

    /**
     * 关联学院和学科
     * @param collegeId 学院ID
     * @param subjectIds 学科ID列表
     * @return 影响的行数
     */
    int associateSubjects(@Param("collegeId") Integer collegeId, @Param("subjectIds") List<Integer> subjectIds);
} 
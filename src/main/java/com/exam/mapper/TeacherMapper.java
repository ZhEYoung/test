package com.exam.mapper;

import com.exam.entity.Teacher;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

/**
 * 教师Mapper接口
 */
@Mapper
public interface TeacherMapper {
    /**
     * 插入教师记录
     * @param teacher 教师实体
     * @return 影响行数
     */
    int insert(Teacher teacher);

    /**
     * 批量插入教师记录
     * @param teachers 教师列表
     * @return 影响行数
     */
    int batchInsert(@Param("teachers") List<Teacher> teachers);

    /**
     * 根据ID删除教师
     * @param teacherId 教师ID
     * @return 影响行数
     */
    int deleteById(@Param("teacherId") Integer teacherId);

    /**
     * 批量删除教师
     * @param teacherIds 教师ID列表
     * @return 影响行数
     */
    int batchDelete(@Param("teacherIds") List<Integer> teacherIds);

    /**
     * 更新教师信息
     * @param teacher 教师实体
     * @return 影响行数
     */
    int updateById(Teacher teacher);

    /**
     * 批量更新教师信息
     * @param teachers 教师列表
     * @return 影响行数
     */
    int batchUpdate(@Param("teachers") List<Teacher> teachers);

    /**
     * 根据ID查询教师
     * @param teacherId 教师ID
     * @return 教师实体
     */
    Teacher selectById(@Param("teacherId") Integer teacherId);

    /**
     * 查询所有教师
     * @return 教师列表
     */
    List<Teacher> selectAll();

    /**
     * 分页查询教师列表
     * @param offset 偏移量
     * @param limit 每页记录数
     * @return 教师列表
     */
    List<Teacher> selectPage(@Param("offset") Integer offset, @Param("limit") Integer limit);

    /**
     * 统计教师总数
     * @return 教师总数
     */
    int selectCount();

    /**
     * 根据条件查询教师
     * @param teacher 查询条件
     * @return 教师列表
     */
    List<Teacher> selectByCondition(Teacher teacher);

    /**
     * 根据用户ID查询教师信息
     * @param userId 用户ID
     * @return 教师实体
     */
    Teacher selectByUserId(@Param("userId") Integer userId);

    /**
     * 根据学院ID查询教师列表
     * @param collegeId 学院ID
     * @return 教师列表
     */
    List<Teacher> selectByCollegeId(@Param("collegeId") Integer collegeId);

    /**
     * 根据教师姓名查询
     * @param name 教师姓名
     * @return 教师列表
     */
    List<Teacher> selectByName(@Param("name") String name);

    /**
     * 根据权限等级查询教师列表
     * @param permission 权限等级
     * @return 教师列表
     */
    List<Teacher> selectByPermission(@Param("permission") Integer permission);

    /**
     * 更新教师备注信息
     * @param teacherId 教师ID
     * @param other 备注信息
     * @return 影响行数
     */
    int updateOther(@Param("teacherId") Integer teacherId, @Param("other") String other);

    /**
     * 更新教师权限
     * @param teacherId 教师ID
     * @param permission 权限等级
     * @return 影响行数
     */
    int updatePermission(@Param("teacherId") Integer teacherId, @Param("permission") Integer permission);

    /**
     * 批量更新教师权限
     * @param teacherIds 教师ID列表
     * @param permission 权限等级
     * @return 影响行数
     */
    int batchUpdatePermission(@Param("teacherIds") List<Integer> teacherIds, @Param("permission") Integer permission);

    /**
     * 统计学院教师数量
     * @param collegeId 学院ID
     * @return 教师数量
     */
    int countByCollege(@Param("collegeId") Integer collegeId);

    /**
     * 统计各权限等级教师数量
     * @return 权限等级分布统计
     */
    List<Map<String, Object>> countByPermission();

    /**
     * 查询教师考试统计信息
     * @param teacherId 教师ID
     * @return 考试统计信息
     */
    Map<String, Object> selectExamStats(@Param("teacherId") Integer teacherId);

    /**
     * 查询教师发布的考试列表
     * @param teacherId 教师ID
     * @return 考试列表
     */
    List<Map<String, Object>> selectTeacherExams(@Param("teacherId") Integer teacherId);
} 
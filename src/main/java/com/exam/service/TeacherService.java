package com.exam.service;

import com.exam.entity.Teacher;
import java.util.List;
import java.util.Map;

/**
 * 教师服务接口
 */
public interface TeacherService {
    
    /**
     * 新增教师
     */
    int insert(Teacher teacher);

    /**
     * 批量新增教师
     */
    int batchInsert(List<Teacher> teachers);

    /**
     * 根据ID删除教师
     */
    int deleteById(Integer teacherId);

    /**
     * 批量删除教师
     */
    int batchDelete(List<Integer> teacherIds);

    /**
     * 更新教师信息
     */
    int updateById(Teacher teacher);

    /**
     * 批量更新教师信息
     */
    int batchUpdate(List<Teacher> teachers);

    /**
     * 根据ID查询教师
     */
    Teacher selectById(Integer teacherId);

    /**
     * 查询所有教师
     */
    List<Teacher> selectAll();

    /**
     * 分页查询教师列表
     */
    List<Teacher> selectPage(Integer pageNum, Integer pageSize);

    /**
     * 查询教师总数
     */
    int selectCount();

    /**
     * 根据条件查询教师
     */
    List<Teacher> selectByCondition(Teacher teacher);

    /**
     * 根据用户ID查询教师信息
     */
    Teacher selectByUserId(Integer userId);

    /**
     * 根据学院ID查询教师列表
     */
    List<Teacher> selectByCollegeId(Integer collegeId);

    /**
     * 根据教师姓名查询
     */
    List<Teacher> selectByName(String name);

    /**
     * 根据权限等级查询教师列表
     */
    List<Teacher> selectByPermission(Integer permission);

    /**
     * 更新教师备注信息
     */
    int updateOther(Integer teacherId, String other);

    /**
     * 更新教师权限
     */
    int updatePermission(Integer teacherId, Integer permission);

    /**
     * 批量更新教师权限
     */
    int batchUpdatePermission(List<Integer> teacherIds, Integer permission);

    /**
     * 统计学院教师数量
     */
    int countByCollege(Integer collegeId);

    /**
     * 统计各权限等级教师数量
     */
    List<Map<String, Object>> countByPermission();

    /**
     * 查询教师考试统计信息
     */
    Map<String, Object> selectExamStats(Integer teacherId);

    /**
     * 查询教师发布的考试列表
     */
    List<Map<String, Object>> selectTeacherExams(Integer teacherId);
} 
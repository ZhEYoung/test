package com.exam.service;

import com.exam.entity.Teacher;
import com.exam.entity.Class;
import java.util.List;
import java.util.Map;

/**
 * 教师服务接口
 */
public interface TeacherService extends BaseService<Teacher> {
    
    /**
     * 根据用户ID查询教师信息
     */
    Teacher getByUserId(Integer userId);
    
    /**
     * 根据学院ID查询教师列表
     */
    List<Teacher> getByCollegeId(Integer collegeId);
    
    /**
     * 根据教师姓名查询
     */
    List<Teacher> getByName(String name);
    
    /**
     * 根据权限等级查询
     * @param permission 权限等级（0: 可以组卷与发布所有考试；1: 可以组卷与发布普通考试；2: 可以组卷）
     */
    List<Teacher> getByPermission(Integer permission);
    
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
     * 查询教师所教授的班级
     */
    List<Class> getTeacherClasses(Integer teacherId);
    
    /**
     * 查询教师在指定学期教授的班级
     */
    List<Class> getTeacherClassesByTerm(Integer teacherId, String academicTerm);
    
    /**
     * 统计教师所教授的班级数量
     */
    Long countTeacherClasses(Integer teacherId);
    
    /**
     * 批量导入教师信息
     * @param teachers 教师信息列表
     * @return 导入结果
     */
    int batchImport(List<Teacher> teachers);
    
    /**
     * 获取教师工作量统计
     * @param teacherId 教师ID
     * @return 工作量统计信息
     */
    Map<String, Object> getWorkloadStatistics(Integer teacherId);
    
    /**
     * 查询教师监考安排
     * @param teacherId 教师ID
     * @return 监考安排列表
     */
    List<Map<String, Object>> getInvigilationSchedule(Integer teacherId);
} 
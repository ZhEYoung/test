package com.exam.service;

import com.exam.entity.Teacher;
import com.exam.entity.Class;
import java.util.List;
import java.util.Map;

/**
 * 教师服务接口
 */
public interface TeacherService {
    
    /**
     * 插入一条记录
     */
    int insert(Teacher record);

    /**
     * 根据ID删除
     */
    int deleteById(Integer id);

    /**
     * 根据ID更新
     */
    int updateById(Teacher record);

    /**
     * 根据ID查询
     */
    Teacher selectById(Integer id);

    /**
     * 查询所有记录
     */
    List<Teacher> selectAll();

    /**
     * 分页查询
     */
    List<Teacher> selectPage(Integer pageNum, Integer pageSize);

    /**
     * 查询总记录数
     */
    Long selectCount();

    /**
     * 条件查询
     */
    List<Teacher> selectByCondition(Map<String, Object> condition);

    /**
     * 条件查询记录数
     */
    Long selectCountByCondition(Map<String, Object> condition);

    /**
     * 条件分页查询
     */
    List<Teacher> selectPageByCondition(Map<String, Object> condition, Integer pageNum, Integer pageSize);
    
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
     * 统计教师所教授的班级数量
     */
    Long countTeacherClasses(Integer teacherId);
    
    /**
     * 批量导入教师信息
     * @param teachers 教师信息列表
     * @return 导入结果
     */
    int batchImport(List<Teacher> teachers);
} 
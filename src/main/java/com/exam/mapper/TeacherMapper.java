package com.exam.mapper;

import com.exam.entity.Exam;
import com.exam.entity.Teacher;
import com.exam.entity.Class;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

/**
 * 教师Mapper接口
 */
public interface TeacherMapper extends BaseMapper<Teacher> {
    /**
     * 根据用户ID查询教师信息
     */
    Teacher selectByUserId(@Param("userId") Integer userId);

    /**
     * 根据学院ID查询教师列表
     */
    List<Teacher> selectByCollegeId(@Param("collegeId") Integer collegeId);

    /**
     * 根据教师姓名查询
     */
    List<Teacher> selectByName(@Param("name") String name);

    /**
     * 根据权限等级查询
     * @param permission 权限等级（0: 可以组卷与发布所有考试；1: 可以组卷与发布普通考试；2: 可以组卷）
     */
    List<Teacher> selectByPermission(@Param("permission") Integer permission);

    /**
     * 更新教师备注信息
     */
    int updateOther(@Param("teacherId") Integer teacherId, @Param("other") String other);

    /**
     * 更新教师权限
     */
    int updatePermission(@Param("teacherId") Integer teacherId, @Param("permission") Integer permission);

    /**
     * 批量更新教师权限
     */
    int batchUpdatePermission(@Param("teacherIds") List<Integer> teacherIds, @Param("permission") Integer permission);

    /**
     * 查询教师所教授的班级
     */
    List<Class> selectTeacherClasses(@Param("teacherId") Integer teacherId);


    /**
     * 统计教师所教授的班级数量
     */
    Long countTeacherClasses(@Param("teacherId") Integer teacherId);



    /**
     * 根据学科ID查询教师列表
     */
    List<Map<String, Object>> selectBySubjectId(@Param("subjectId") Integer subjectId);
} 
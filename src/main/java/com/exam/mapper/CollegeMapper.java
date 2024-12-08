package com.exam.mapper;

import com.exam.entity.College;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 学院Mapper接口
 */
public interface CollegeMapper extends BaseMapper<College> {
    /**
     * 根据学院名称查询
     */
    College selectByCollegeName(@Param("collegeName") String collegeName);

    /**
     * 更新学院描述
     */
    int updateDescription(@Param("collegeId") Integer collegeId, @Param("description") String description);

    /**
     * 统计学院学生数量
     */
    Long countStudents(@Param("collegeId") Integer collegeId);

    /**
     * 批量更新学科所属学院
     */
    int updateSubjectsCollege(@Param("collegeId") Integer collegeId, @Param("subjectIds") List<Integer> subjectIds);

    /**
     * 统计学院学科数量
     */
    Long countSubjects(@Param("collegeId") Integer collegeId);

    /**
     * 统计学院教师数量
     */
    Long countTeachers(@Param("collegeId") Integer collegeId);
} 
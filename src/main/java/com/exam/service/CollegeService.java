package com.exam.service;

import com.exam.entity.College;
import com.exam.entity.Subject;
import com.exam.entity.Teacher;
import com.exam.entity.Student;
import java.util.List;
import java.util.Map;

/**
 * 学院服务接口
 */
public interface CollegeService {
    
    /**
     * 插入一条记录
     */
    int insert(College record);

    /**
     * 根据ID删除
     */
    int deleteById(Integer id);

    /**
     * 根据ID更新
     */
    int updateById(College record);

    /**
     * 根据ID查询
     */
    College selectById(Integer id);

    /**
     * 查询所有记录
     */
    List<College> selectAll();

    /**
     * 分页查询
     */
    List<College> selectPage(Integer pageNum, Integer pageSize);

    /**
     * 查询总记录数
     */
    Long selectCount();

    /**
     * 条件查询
     */
    List<College> selectByCondition(Map<String, Object> condition);

    /**
     * 条件查询记录数
     */
    Long selectCountByCondition(Map<String, Object> condition);

    /**
     * 条件分页查询
     */
    List<College> selectPageByCondition(Map<String, Object> condition, Integer pageNum, Integer pageSize);
    
    /**
     * 根据学院名称查询
     */
    College getByCollegeName(String collegeName);
    
    /**
     * 更新学院描述
     */
    int updateDescription(Integer collegeId, String description);
    
    /**
     * 查询学院下的所有学科
     */
    List<Subject> getCollegeSubjects(Integer collegeId);

    /**
     * 统计学院学科数量
     */
    Long countSubjects(Integer collegeId);

    /**
     * 统计学院学生数量
     */
    Long countStudents(Integer collegeId);

    /**
     * 统计学院教师数量
     */
    Long countTeachers(Integer collegeId);

    /**
     * 创建学院
     * @param college 学院信息
     * @param subjectIds 学科ID列表
     * @return 创建结果
     */
    int createCollege(College college, List<Integer> subjectIds);
    
    /**
     * 删除学院
     * @param collegeId 学院ID
     * @return 删除结果
     */
    int deleteCollege(Integer collegeId);
    
    /**
     * 导出学院学生名册
     * @param collegeId 学院ID
     * @return 学生名册数据
     */
    Map<String, Object> exportStudentList(Integer collegeId);
} 
package com.exam.service;

import com.exam.entity.Student;
import com.exam.entity.StudentClass;
import com.exam.entity.StudentScore;
import java.util.List;
import java.util.Date;
import java.util.Map;

/**
 * 学生服务接口
 */
public interface StudentService extends BaseService<Student> {
    
    /**
     * 根据用户ID查询学生信息
     */
    Student getByUserId(Integer userId);
    
    /**
     * 根据学院ID查询学生列表
     */
    List<Student> getByCollegeId(Integer collegeId);
    
    /**
     * 根据年级查询学生列表
     */
    List<Student> getByGrade(String grade);
    
    /**
     * 根据学生姓名查询
     */
    List<Student> getByName(String name);
    
    /**
     * 更新学生备注信息
     */
    int updateOther(Integer studentId, String other);
    
    /**
     * 查询学生所在的班级列表
     */
    List<StudentClass> getStudentClasses(Integer studentId);
    
    /**
     * 加入班级
     */
    int joinClass(Integer studentId, Integer classId);
    
    /**
     * 退出班级
     */
    int leaveClass(Integer studentId, Integer classId, Date leftTime);
    
    /**
     * 查询学生在某个考试中的成绩
     */
    StudentScore getScore(Integer studentId, Integer examId);
    
    /**
     * 查询学生的所有成绩
     */
    List<StudentScore> getAllScores(Integer studentId);
    
    /**
     * 插入学生成绩
     */
    int insertScore(Integer studentId, Integer examId, Double score);
    
    /**
     * 更新学生成绩
     */
    int updateScore(Integer studentId, Integer examId, Double score);
    
    /**
     * 批量导入学生信息
     * @param students 学生信息列表
     * @return 导入结果
     */
    int batchImport(List<Student> students);
    
    /**
     * 统计班级学生数量
     * @param classId 班级ID
     * @return 学生数量
     */
    Long countStudentsByClass(Integer classId);
    
    /**
     * 获取学生成绩统计信息
     * @param studentId 学生ID
     * @return 成绩统计信息
     */
    Map<String, Object> getScoreStatistics(Integer studentId);
} 
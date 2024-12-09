package com.exam.service;

import com.exam.entity.Class;
import com.exam.entity.Student;
import com.exam.entity.Exam;
import java.util.List;
import java.util.Map;
import java.util.Date;

/**
 * 班级服务接口
 */
public interface ClassService {
    
    /**
     * 插入一条记录
     */
    int insert(Class record);

    /**
     * 根据ID删除
     */
    int deleteById(Integer id);

    /**
     * 根据ID更新
     */
    int updateById(Class record);

    /**
     * 根据ID查询
     */
    Class selectById(Integer id);

    /**
     * 查询所有记录
     */
    List<Class> selectAll();

    /**
     * 分页查询
     */
    List<Class> selectPage(Integer pageNum, Integer pageSize);

    /**
     * 查询总记录数
     */
    Long selectCount();

    /**
     * 条件查询
     */
    List<Class> selectByCondition(Map<String, Object> condition);

    /**
     * 条件查询记录数
     */
    Long selectCountByCondition(Map<String, Object> condition);

    /**
     * 条件分页查询
     */
    List<Class> selectPageByCondition(Map<String, Object> condition, Integer pageNum, Integer pageSize);
    
    /**
     * 根据教师ID查询班级列表
     */
    List<Class> getByTeacherId(Integer teacherId);
    
    /**
     * 根据学科ID查询班级列表
     */
    List<Class> getBySubjectId(Integer subjectId);
    
    /**
     * 根据班级名称查询
     */
    Class getByClassName(String className);
    
    /**
     * 更新期末考试状态
     */
    int updateFinalExam(Integer classId, Boolean finalExam);
    
    /**
     * 查询班级学生列表
     */
    List<Student> getClassStudents(Integer classId);
    
    /**
     * 查询班级考试列表
     */
    List<Exam> getClassExams(Integer classId);
    
    /**
     * 查询班级期末考试
     */
    Exam getFinalExam(Integer classId);
    
    /**
     * 统计班级学生数量
     */
    Long countStudents(Integer classId);
    
    /**
     * 统计班级考试数量
     */
    Long countExams(Integer classId);
    
    /**
     * 查询班级平均成绩
     */
    Double getAvgScore(Integer classId, Integer examId);
    
    /**
     * 查询班级成绩分布
     */
    List<Map<String, Object>> getScoreDistribution(Integer classId, Integer examId);
    
    /**
     * 查询班级考试日程
     */
    List<Exam> getExamSchedule(Integer classId);
    
    /**
     * 批量添加学生到班级
     */
    int batchAddStudents(Integer classId, List<Integer> studentIds);
    
    /**
     * 批量移除班级学生
     */
    int batchRemoveStudents(Integer classId, List<Integer> studentIds);
    
    /**
     * 创建班级
     * @param classInfo 班级信息
     * @param teacherId 教师ID
     * @param studentIds 学生ID列表
     * @return 创建结果
     */
    int createClass(Class classInfo, Integer teacherId, List<Integer> studentIds);
    
    /**
     * 解散班级
     * @param classId 班级ID
     * @return 解散结果
     */
    int dissolveClass(Integer classId);
    
    /**
     * 获取班级统计信息
     * @param classId 班级ID
     * @return 统计信息
     */
    Map<String, Object> getClassStatistics(Integer classId);
    
    /**
     * 导出班级学生名单
     * @param classId 班级ID
     * @return 学生名单数据
     */
    Map<String, Object> exportStudentList(Integer classId);
    
    /**
     * 导入班级学生
     * @param classId 班级ID
     * @param students 学生列表
     * @return 导入结果
     */
    int importStudents(Integer classId, List<Student> students);
} 
package com.exam.service;

import com.exam.entity.StudentClass;
import com.exam.entity.Student;
import com.exam.entity.Class;
import java.util.List;
import java.util.Map;
import java.util.Date;

/**
 * 学生-班级关联服务接口
 */
public interface StudentClassService {
    
    /**
     * 插入关联记录
     * @param studentClass 关联实体
     * @return 影响行数
     */
    int insert(StudentClass studentClass);

    /**
     * 根据ID删除关联记录
     * @param scId 关联ID
     * @return 影响行数
     */
    int deleteById(Integer scId);

    /**
     * 更新关联信息
     * @param studentClass 关联实体
     * @return 影响行数
     */
    int update(StudentClass studentClass);

    /**
     * 根据ID查询关联记录
     * @param scId 关联ID
     * @return 关联实体
     */
    StudentClass getById(Integer scId);

    /**
     * 查询所有关联记录
     * @return 关联列表
     */
    List<StudentClass> getAll();

    /**
     * 根据学生ID查询关联记录
     * @param studentId 学生ID
     * @return 关联列表
     */
    List<StudentClass> getByStudentId(Integer studentId);

    /**
     * 根据班级ID查询关联记录
     * @param classId 班级ID
     * @return 关联列表
     */
    List<StudentClass> getByClassId(Integer classId);

    /**
     * 查询学生在指定班级的关联记录
     * @param studentId 学生ID
     * @param classId 班级ID
     * @return 关联记录
     */
    StudentClass getByStudentAndClass(Integer studentId, Integer classId);

    /**
     * 更新学生状态和时间
     * @param scId 关联ID
     * @param status 状态
     * @param joinTime 加入时间
     * @param leftTime 退出时间
     * @return 影响行数
     */
    int updateStatusAndTime(Integer scId, Boolean status, Date joinTime, Date leftTime);

    /**
     * 批量插入学生班级关联
     * @param list 关联列表
     * @return 影响行数
     */
    int batchInsert(List<StudentClass> list);

    /**
     * 批量更新学生状态
     * @param studentIds 学生ID列表
     * @param classId 班级ID
     * @param status 状态
     * @return 影响行数
     */
    int batchUpdateStatus(List<Integer> studentIds, Integer classId, Boolean status);

    /**
     * 统计班级学生数量
     * @param classId 班级ID
     * @return 学生数量
     */
    int countStudentsByClass(Integer classId);

    /**
     * 统计学生所在班级数量
     * @param studentId 学生ID
     * @return 班级数量
     */
    int countClassesByStudent(Integer studentId);

    /**
     * 查询班级的活跃学生
     * @param classId 班级ID
     * @return 学生列表
     */
    List<Student> getActiveStudents(Integer classId);

    /**
     * 查询学生的所有班级
     * @param studentId 学生ID
     * @return 班级列表
     */
    List<Class> getStudentClasses(Integer studentId);

    /**
     * 统计班级学生加入退出情况
     * @param classId 班级ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 统计信息
     */
    List<Map<String, Object>> analyzeClassStudentFlow(Integer classId, Date startTime, Date endTime);

    /**
     * 批量删除关联记录
     * @param scIds 关联ID列表
     * @return 影响行数
     */
    int batchDelete(List<Integer> scIds);

    /**
     * 检查学生是否在班级中
     * @param studentId 学生ID
     * @param classId 班级ID
     * @return 是否存在且有效
     */
    boolean checkStudentInClass(Integer studentId, Integer classId);
    
    /**
     * 学生加入班级
     * @param studentId 学生ID
     * @param classId 班级ID
     * @return 影响行数
     */
    int joinClass(Integer studentId, Integer classId);
    
    /**
     * 学生退出班级
     * @param studentId 学生ID
     * @param classId 班级ID
     * @return 影响行数
     */
    int leaveClass(Integer studentId, Integer classId);
    
    /**
     * 验证关联数据
     * @param studentClass 关联实体
     * @return 是否有效
     */
    boolean validateStudentClass(StudentClass studentClass);
} 
package com.exam.mapper;

import com.exam.entity.StudentClass;
import com.exam.entity.Student;
import com.exam.entity.Class;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Date;
import java.util.Map;

/**
 * 学生-班级关联Mapper接口
 */
@Mapper
public interface StudentClassMapper {
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
    int deleteById(@Param("scId") Integer scId);

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
    StudentClass selectById(@Param("scId") Integer scId);

    /**
     * 查询所有关联记录
     * @return 关联列表
     */
    List<StudentClass> selectAll();

    /**
     * 根据学生ID查询关联记录
     * @param studentId 学生ID
     * @return 关联列表
     */
    List<StudentClass> selectByStudentId(@Param("studentId") Integer studentId);

    /**
     * 根据班级ID查询关联记录
     * @param classId 班级ID
     * @return 关联列表
     */
    List<StudentClass> selectByClassId(@Param("classId") Integer classId);

    /**
     * 查询学生在指定班级的关联记录
     * @param studentId 学生ID
     * @param classId 班级ID
     * @return 关联记录
     */
    StudentClass selectByStudentAndClass(@Param("studentId") Integer studentId, @Param("classId") Integer classId);

    /**
     * 更新学生状态和时间
     * @param scId 关联ID
     * @param status 状态
     * @param joinedAt 加入时间
     * @param leftAt 退出时间
     * @return 影响行数
     */
    int updateStatusAndTime(@Param("scId") Integer scId, 
                          @Param("status") Boolean status,
                          @Param("joinedAt") Date joinedAt,
                          @Param("leftAt") Date leftAt);

    /**
     * 批量插入学生班级关联
     * @param list 关联列表
     * @return 影响行数
     */
    int batchInsert(@Param("list") List<StudentClass> list);

    /**
     * 批量更新学生状态
     * @param studentIds 学生ID列表
     * @param classId 班级ID
     * @param status 状态
     * @return 影响行数
     */
    int batchUpdateStatus(
        @Param("studentIds") List<Integer> studentIds,
        @Param("classId") Integer classId,
        @Param("status") Boolean status
    );

    /**
     * 统计班级学生数量
     * @param classId 班级ID
     * @return 学生数量
     */
    int countStudentsByClass(@Param("classId") Integer classId);

    /**
     * 统计学生所在班级数量
     * @param studentId 学生ID
     * @return 班级数量
     */
    int countClassesByStudent(@Param("studentId") Integer studentId);

    /**
     * 查询班级的活跃学生
     * @param classId 班级ID
     * @return 学生列表
     */
    List<Student> selectActiveStudents(@Param("classId") Integer classId);

    /**
     * 查询学生的所有班级
     * @param studentId 学生ID
     * @return 班级列表
     */
    List<Class> selectStudentClasses(@Param("studentId") Integer studentId);

    /**
     * 统计班级学生加入退出情况
     * @param classId 班级ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 统计信息
     */
    List<Map<String, Object>> analyzeClassStudentFlow(
        @Param("classId") Integer classId,
        @Param("startTime") Date startTime,
        @Param("endTime") Date endTime
    );

    /**
     * 批量删除关联记录
     * @param scIds 关联ID列表
     * @return 影响行数
     */
    int batchDelete(@Param("scIds") List<Integer> scIds);

    /**
     * 检查学生是否在班级中
     * @param studentId 学生ID
     * @param classId 班级ID
     * @return 是否存在且有效
     */
    boolean checkStudentInClass(
        @Param("studentId") Integer studentId,
        @Param("classId") Integer classId
    );
} 
package com.exam.mapper;

import com.exam.entity.Student;
import com.exam.entity.StudentClass;
import com.exam.entity.StudentScore;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;
import java.util.Date;

/**
 * 学生Mapper接口
 */
@Mapper
public interface StudentMapper {
    /**
     * 插入学生记录
     * @param student 学生实体
     * @return 影响行数
     */
    int insert(Student student);

    /**
     * 批量插入学生记录
     * @param students 学生列表
     * @return 影响行数
     */
    int batchInsert(@Param("students") List<Student> students);

    /**
     * 根据ID删除学生
     * @param studentId 学生ID
     * @return 影响行数
     */
    int deleteById(@Param("studentId") Integer studentId);

    /**
     * 批量删除学生
     * @param studentIds 学生ID列表
     * @return 影响行数
     */
    int batchDelete(@Param("studentIds") List<Integer> studentIds);

    /**
     * 更新学生信息
     * @param student 学生实体
     * @return 影响行数
     */
    int updateById(Student student);

    /**
     * 批量更新学生信息
     * @param students 学生列表
     * @return 影响行数
     */
    int batchUpdate(@Param("students") List<Student> students);

    /**
     * 根据ID查询学生
     * @param studentId 学生ID
     * @return 学生实体
     */
    Student selectById(@Param("studentId") Integer studentId);

    /**
     * 查询所有学生
     * @return 学生列表
     */
    List<Student> selectAll();

    /**
     * 分页查询学生列表
     * @param offset 偏移量
     * @param limit 每页记录数
     * @return 学生列表
     */
    List<Student> selectPage(@Param("offset") Integer offset, @Param("limit") Integer limit);

    /**
     * 统计学生总数
     * @return 学生总数
     */
    int selectCount();

    /**
     * 根据条件查询学生
     * @param student 查询条件
     * @return 学生列表
     */
    List<Student> selectByCondition(Student student);

    /**
     * 根据用户ID查询学生信息
     * @param userId 用户ID
     * @return 学生实体
     */
    Student selectByUserId(@Param("userId") Integer userId);

    /**
     * 根据学院ID查询学生列表
     * @param collegeId 学院ID
     * @return 学生列表
     */
    List<Student> selectByCollegeId(@Param("collegeId") Integer collegeId);

    /**
     * 根据年级查询学生列表
     * @param grade 年级
     * @return 学生列表
     */
    List<Student> selectByGrade(@Param("grade") String grade);

    /**
     * 根据学生姓名查询
     * @param name 学生姓名
     * @return 学生列表
     */
    List<Student> selectByName(@Param("name") String name);

    /**
     * 更新学生备注信息
     * @param studentId 学生ID
     * @param other 备注信息
     * @return 影响行数
     */
    int updateOther(@Param("studentId") Integer studentId, @Param("other") String other);

    /**
     * 查询学生所在的班级列表
     * @param studentId 学生ID
     * @return 班级关联列表
     */
    List<StudentClass> selectStudentClasses(@Param("studentId") Integer studentId);

    /**
     * 加入班级
     * @param studentId 学生ID
     * @param classId 班级ID
     * @return 影响行数
     */
    int joinClass(@Param("studentId") Integer studentId, @Param("classId") Integer classId);

    /**
     * 退出班级
     * @param studentId 学生ID
     * @param classId 班级ID
     * @param leftTime 退出时间
     * @return 影响行数
     */
    int leaveClass(
        @Param("studentId") Integer studentId, 
        @Param("classId") Integer classId,
        @Param("leftTime") Date leftTime
    );

    /**
     * 查询学生在某个考试中的成绩
     * @param studentId 学生ID
     * @param examId 考试ID
     * @return 成绩信息
     */
    StudentScore selectScore(
        @Param("studentId") Integer studentId,
        @Param("examId") Integer examId
    );

    /**
     * 查询学生的所有成绩
     * @param studentId 学生ID
     * @return 成绩列表
     */
    List<StudentScore> selectAllScores(@Param("studentId") Integer studentId);

    /**
     * 插入学生成绩
     * @param studentId 学生ID
     * @param examId 考试ID
     * @param score 成绩
     * @return 影响行数
     */
    int insertScore(
        @Param("studentId") Integer studentId,
        @Param("examId") Integer examId,
        @Param("score") Double score
    );

    /**
     * 更新学生成绩
     * @param studentId 学生ID
     * @param examId 考试ID
     * @param score 成绩
     * @return 影响行数
     */
    int updateScore(
        @Param("studentId") Integer studentId,
        @Param("examId") Integer examId,
        @Param("score") Double score
    );

    /**
     * 统计学院学生性别分布
     * @param collegeId 学院ID
     * @return 性别分布统计
     */
    List<Map<String, Object>> countByGender(@Param("collegeId") Integer collegeId);

    /**
     * 统计学院学生年级分布
     * @param collegeId 学院ID
     * @return 年级分布统计
     */
    List<Map<String, Object>> countByGrade(@Param("collegeId") Integer collegeId);

    /**
     * 查询学生考试统计信息
     * @param studentId 学生ID
     * @return 考试统计信息
     */
    Map<String, Object> selectExamStats(@Param("studentId") Integer studentId);

    /**
     * 查询学生成绩趋势
     * @param studentId 学生ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 成绩趋势数据
     */
    List<Map<String, Object>> selectScoreTrend(
        @Param("studentId") Integer studentId,
        @Param("startTime") Date startTime,
        @Param("endTime") Date endTime
    );
} 
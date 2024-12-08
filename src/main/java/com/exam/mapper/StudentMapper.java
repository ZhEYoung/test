package com.exam.mapper;

import com.exam.entity.Student;
import com.exam.entity.StudentClass;
import com.exam.entity.StudentScore;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;
import java.util.Date;

/**
 * 学生Mapper接口
 */
public interface StudentMapper extends BaseMapper<Student> {
    /**
     * 根据用户ID查询学生信息
     */
    Student selectByUserId(@Param("userId") Integer userId);

    /**
     * 根据学院ID查询学生列表
     */
    List<Student> selectByCollegeId(@Param("collegeId") Integer collegeId);

    /**
     * 根据年级查询学生列表
     */
    List<Student> selectByGrade(@Param("grade") String grade);

    /**
     * 根据学生姓名查询
     */
    List<Student> selectByName(@Param("name") String name);

    /**
     * 更新学生备注信息
     */
    int updateOther(@Param("studentId") Integer studentId, @Param("other") String other);

    /**
     * 查询学生所在的班级列表
     */
    List<StudentClass> selectStudentClasses(@Param("studentId") Integer studentId);

    /**
     * 加入班级
     */
    int joinClass(@Param("studentId") Integer studentId, @Param("classId") Integer classId);

    /**
     * 退出班级
     */
    int leaveClass(
        @Param("studentId") Integer studentId, 
        @Param("classId") Integer classId,
        @Param("leftTime") Date leftTime
    );

    /**
     * 查询学生在某个考试中的成绩
     */
    StudentScore selectScore(
        @Param("studentId") Integer studentId,
        @Param("examId") Integer examId
    );

    /**
     * 查询学生的所有成绩
     */
    List<StudentScore> selectAllScores(@Param("studentId") Integer studentId);

    /**
     * 插入学生成绩
     */
    int insertScore(
        @Param("studentId") Integer studentId,
        @Param("examId") Integer examId,
        @Param("score") Double score
    );

    /**
     * 更新学生成绩
     */
    int updateScore(
        @Param("studentId") Integer studentId,
        @Param("examId") Integer examId,
        @Param("score") Double score
    );

    /**
     * 统计学院学生性别分布
     * @param collegeId 学院ID
     * @return 包含性别和对应人数的列表，每个Map包含gender(性别)和count(人数)
     */
    List<Map<String, Object>> countByGender(@Param("collegeId") Integer collegeId);

    /**
     * 统计学院学生年级分布
     * @param collegeId 学院ID
     * @return 包含年级和对应人数的列表，每个Map包含grade(年级)和count(人数)
     */
    List<Map<String, Object>> countByGrade(@Param("collegeId") Integer collegeId);
} 
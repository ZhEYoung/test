package com.exam.mapper;

import com.exam.entity.StudentClass;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Date;

/**
 * 学生-班级关联Mapper接口
 */
public interface StudentClassMapper extends BaseMapper<StudentClass> {
    /**
     * 根据学生ID查询关联记录
     */
    List<StudentClass> selectByStudentId(@Param("studentId") Integer studentId);

    /**
     * 根据班级ID查询关联记录
     */
    List<StudentClass> selectByClassId(@Param("classId") Integer classId);

    /**
     * 查询学生在指定班级的关联记录
     */
    StudentClass selectByStudentAndClass(@Param("studentId") Integer studentId, @Param("classId") Integer classId);

    /**
     * 更新学生状态和时间
     */
    int updateStatusAndTime(@Param("scId") Integer scId, 
                          @Param("status") Boolean status,
                          @Param("joinedAt") Date joinedAt,
                          @Param("leftAt") Date leftAt);

    /**
     * 批量插入学生班级关联
     */
    int batchInsert(@Param("list") List<StudentClass> list);
} 
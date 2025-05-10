package com.exam.mapper;

import com.exam.entity.ExamClass;
import com.exam.entity.Class;
import com.exam.entity.Exam;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;
import java.util.Date;

/**
 * 考试-班级关联Mapper接口
 */
@Mapper
public interface ExamClassMapper {
    /**
     * 插入考试-班级关联记录
     * @param examClass 考试-班级关联实体
     * @return 影响的行数
     */
    int insert(ExamClass examClass);

    /**
     * 批量插入考试-班级关联记录
     * @param list 考试-班级关联列表
     * @return 影响的行数
     */
    int batchInsert(@Param("list") List<ExamClass> list);

    /**
     * 根据ID删除考试-班级关联
     * @param ecId 关联ID
     * @return 影响的行数
     */
    int deleteById(@Param("ecId") Integer ecId);

    /**
     * 批量删除考试-班级关联
     * @param ecIds ID列表
     * @return 影响的行数
     */
    int batchDelete(@Param("ecIds") List<Integer> ecIds);

    /**
     * 更新考试-班级关联信息
     * @param examClass 考试-班级关联实体
     * @return 影响的行数
     */
    int updateById(ExamClass examClass);

    /**
     * 批量更新考试-班级关联
     * @param list 考试-班级关联列表
     * @return 影响的行数
     */
    int batchUpdate(@Param("list") List<ExamClass> list);

    /**
     * 根据ID查询考试-班级关联
     * @param ecId 关联ID
     * @return 考试-班级关联实体
     */
    ExamClass selectById(@Param("ecId") Integer ecId);

    /**
     * 查询所有考试-班级关联
     * @return 考试-班级关联列表
     */
    List<ExamClass> selectAll();

    /**
     * 分页查询考试-班级关联
     * @param offset 偏移量
     * @param limit 每页记录数
     * @return 考试-班级关联列表
     */
    List<ExamClass> selectPage(@Param("offset") Integer offset, @Param("limit") Integer limit);

    /**
     * 查询考试-班级关联总数
     * @return 总记录数
     */
    Long selectCount();

    /**
     * 条件查询考试-班级关联
     * @param condition 查询条件
     * @return 考试-班级关联列表
     */
    List<ExamClass> selectByCondition(@Param("condition") Map<String, Object> condition);

    /**
     * 条件查询考试-班级关联数量
     * @param condition 查询条件
     * @return 记录数
     */
    Long selectCountByCondition(@Param("condition") Map<String, Object> condition);

    /**
     * 条件分页查询考试-班级关联
     * @param condition 查询条件
     * @param offset 偏移量
     * @param limit 每页记录数
     * @return 考试-班级关联列表
     */
    List<ExamClass> selectPageByCondition(
        @Param("condition") Map<String, Object> condition,
        @Param("offset") Integer offset,
        @Param("limit") Integer limit
    );

    /**
     * 根据考试ID查询关联的班级列表
     * @param examId 考试ID
     * @return 班级列表
     */
    List<Class> selectClassesByExamId(@Param("examId") Integer examId);

    /**
     * 根据班级ID查询关联的考试列表
     * @param classId 班级ID
     * @return 考试列表
     */
    List<Exam> selectExamsByClassId(@Param("classId") Integer classId);

    /**
     * 根据考试ID和班级ID查询关联记录
     * @param examId 考试ID
     * @param classId 班级ID
     * @return 考试-班级关联实体
     */
    ExamClass selectByExamIdAndClassId(
        @Param("examId") Integer examId,
        @Param("classId") Integer classId
    );

    /**
     * 批量添加班级到考试
     * @param examId 考试ID
     * @param classIds 班级ID列表
     * @return 影响的行数
     */
    int batchAddClasses(
        @Param("examId") Integer examId,
        @Param("classIds") List<Integer> classIds
    );

    /**
     * 批量从考试中移除班级
     * @param examId 考试ID
     * @param classIds 班级ID列表
     * @return 影响的行数
     */
    int batchRemoveClasses(
        @Param("examId") Integer examId,
        @Param("classIds") List<Integer> classIds
    );

    /**
     * 查询指定时间范围内班级的考试
     * @param classId 班级ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 考试列表
     */
    List<Exam> selectExamsByTimeRange(
        @Param("classId") Integer classId,
        @Param("startTime") Date startTime,
        @Param("endTime") Date endTime
    );

    /**
     * 统计考试关联的班级数量
     * @param examId 考试ID
     * @return 班级数量
     */
    Long countClassesByExamId(@Param("examId") Integer examId);

    /**
     * 统计班级关联的考试数量
     * @param classId 班级ID
     * @return 考试数量
     */
    Long countExamsByClassId(@Param("classId") Integer classId);
} 
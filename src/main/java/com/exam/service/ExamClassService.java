package com.exam.service;

import com.exam.entity.ExamClass;
import com.exam.entity.Class;
import com.exam.entity.Exam;
import java.util.List;
import java.util.Map;
import java.util.Date;

/**
 * 考试-班级关联服务接口
 */
public interface ExamClassService {
    
    /**
     * 插入一条记录
     */
    int insert(ExamClass record);

    /**
     * 根据ID删除
     */
    int deleteById(Integer id);

    /**
     * 根据ID更新
     */
    int updateById(ExamClass record);

    /**
     * 根据ID查询
     */
    ExamClass selectById(Integer id);

    /**
     * 查询所有记录
     */
    List<ExamClass> selectAll();

    /**
     * 分页查询
     */
    List<ExamClass> selectPage(Integer pageNum, Integer pageSize);

    /**
     * 查询总记录数
     */
    Long selectCount();

    /**
     * 条件查询
     */
    List<ExamClass> selectByCondition(Map<String, Object> condition);

    /**
     * 条件查询记录数
     */
    Long selectCountByCondition(Map<String, Object> condition);

    /**
     * 条件分页查询
     */
    List<ExamClass> selectPageByCondition(Map<String, Object> condition, Integer pageNum, Integer pageSize);
    
    /**
     * 批量插入考试-班级关联记录
     */
    int batchInsert(List<ExamClass> list);
    
    /**
     * 批量删除考试-班级关联
     */
    int batchDelete(List<Integer> ecIds);
    
    /**
     * 批量更新考试-班级关联
     */
    int batchUpdate(List<ExamClass> list);
    
    /**
     * 根据考试ID查询关联的班级列表
     */
    List<Class> getClassesByExamId(Integer examId);
    
    /**
     * 根据班级ID查询关联的考试列表
     */
    List<Exam> getExamsByClassId(Integer classId);
    
    /**
     * 根据考试ID和班级ID查询关联记录
     */
    ExamClass getByExamIdAndClassId(Integer examId, Integer classId);
    
    /**
     * 批量添加班级到考试
     */
    int batchAddClasses(Integer examId, List<Integer> classIds);
    
    /**
     * 批量从考试中移除班级
     */
    int batchRemoveClasses(Integer examId, List<Integer> classIds);
    
    /**
     * 查询指定时间范围内班级的考试
     */
    List<Exam> getExamsByTimeRange(Integer classId, Date startTime, Date endTime);
    
    /**
     * 统计考试关联的班级数量
     */
    Long countClassesByExamId(Integer examId);
    
    /**
     * 统计班级关联的考试数量
     */
    Long countExamsByClassId(Integer classId);
} 
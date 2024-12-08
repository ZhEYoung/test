package com.exam.service;

import com.exam.entity.Subject;
import java.util.List;
import java.util.Map;

/**
 * 学科服务接口
 */
public interface SubjectService extends BaseService<Subject> {
    
    /**
     * 根据学院ID查询学科列表
     */
    List<Subject> getByCollegeId(Integer collegeId);
    
    /**
     * 根据学科名称查询
     */
    Subject getBySubjectName(String subjectName);
    
    /**
     * 更新学科描述
     */
    int updateDescription(Integer subjectId, String description);
    
    /**
     * 批量更新学科描述
     */
    int batchUpdateDescription(List<Integer> subjectIds, String description);
    
    /**
     * 查询有考试的学科列表
     */
    List<Subject> getSubjectsWithExams();
    
    /**
     * 查询指定教师教授的学科列表
     */
    List<Subject> getByTeacherId(Integer teacherId);
    
    /**
     * 查询指定学生学习的学科列表
     */
    List<Subject> getByStudentId(Integer studentId);
    
    /**
     * 统计各学科考试数量
     */
    List<Map<String, Object>> countExamsBySubject();
    
    /**
     * 统计各学科平均成绩
     */
    List<Map<String, Object>> getAvgScoreBySubject();
    
    /**
     * 查询热门学科（按学生数量排序）
     */
    List<Subject> getHotSubjects(Integer limit);
    
    /**
     * 查询难度较高的学科（按平均分排序）
     */
    List<Subject> getDifficultSubjects(Integer limit);
    
    /**
     * 创建学科
     * @param subject 学科信息
     * @param teacherIds 教师ID列表
     * @return 创建结果
     */
    int createSubject(Subject subject, List<Integer> teacherIds);
    
    /**
     * 删除学科
     * @param subjectId 学科ID
     * @return 删除结果
     */
    int deleteSubject(Integer subjectId);
    
    /**
     * 获取学科统计信息
     * @param subjectId 学科ID
     * @return 统计信息
     */
    Map<String, Object> getSubjectStatistics(Integer subjectId);
    

} 
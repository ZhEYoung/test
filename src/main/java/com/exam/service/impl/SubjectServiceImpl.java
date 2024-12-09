package com.exam.service.impl;

import com.exam.entity.Exam;
import com.exam.entity.Subject;
import com.exam.mapper.SubjectMapper;
import com.exam.mapper.TeacherMapper;
import com.exam.mapper.ExamMapper;
import com.exam.service.SubjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

/**
 * 学科服务实现类
 */
@Service
@Transactional
public class SubjectServiceImpl implements SubjectService {

    @Autowired
    private SubjectMapper subjectMapper;
    
    @Autowired
    private TeacherMapper teacherMapper;
    
    @Autowired
    private ExamMapper examMapper;

    @Override
    public int insert(Subject record) {
        return subjectMapper.insert(record);
    }

    @Override
    public int deleteById(Integer id) {
        return subjectMapper.deleteById(id);
    }

    @Override
    public int updateById(Subject record) {
        return subjectMapper.updateById(record);
    }

    @Override
    public Subject selectById(Integer id) {
        return subjectMapper.selectById(id);
    }

    @Override
    public List<Subject> selectAll() {
        return subjectMapper.selectAll();
    }

    @Override
    public List<Subject> selectPage(Integer pageNum, Integer pageSize) {
        int offset = (pageNum - 1) * pageSize;
        return subjectMapper.selectPage(offset, pageSize);
    }

    @Override
    public Long selectCount() {
        return subjectMapper.selectCount();
    }

    @Override
    public List<Subject> selectByCondition(Map<String, Object> condition) {
        return subjectMapper.selectByCondition(condition);
    }

    @Override
    public Long selectCountByCondition(Map<String, Object> condition) {
        return subjectMapper.selectCountByCondition(condition);
    }

    @Override
    public List<Subject> selectPageByCondition(Map<String, Object> condition, Integer pageNum, Integer pageSize) {
        int offset = (pageNum - 1) * pageSize;
        return subjectMapper.selectPageByCondition(condition, offset, pageSize);
    }

    @Override
    public List<Subject> getByCollegeId(Integer collegeId) {
        return subjectMapper.selectByCollegeId(collegeId);
    }

    @Override
    public Subject getBySubjectName(String subjectName) {
        return subjectMapper.selectBySubjectName(subjectName);
    }

    @Override
    public int updateDescription(Integer subjectId, String description) {
        return subjectMapper.updateDescription(subjectId, description);
    }

    @Override
    public int batchUpdateDescription(List<Integer> subjectIds, String description) {
        return subjectMapper.batchUpdateDescription(subjectIds, description);
    }

    @Override
    public List<Subject> getSubjectsWithExams() {
        return subjectMapper.selectSubjectsWithExams();
    }

    @Override
    public List<Subject> getByTeacherId(Integer teacherId) {
        return subjectMapper.selectByTeacherId(teacherId);
    }

    @Override
    public List<Subject> getByStudentId(Integer studentId) {
        return subjectMapper.selectByStudentId(studentId);
    }

    @Override
    public List<Map<String, Object>> countExamsBySubject() {
        return subjectMapper.countExamsBySubject();
    }

    @Override
    public List<Map<String, Object>> getAvgScoreBySubject() {
        return subjectMapper.avgScoreBySubject();
    }

    @Override
    public List<Subject> getHotSubjects(Integer limit) {
        return subjectMapper.selectHotSubjects(limit);
    }

    @Override
    public List<Subject> getDifficultSubjects(Integer limit) {
        return subjectMapper.selectDifficultSubjects(limit);
    }

    @Override
    public int createSubject(Subject subject, List<Integer> teacherIds) {
        // 插入学科记录
        int result = subjectMapper.insert(subject);
        if (result == 0) {
            return 0;
        }
        
        // 建立教师和学科的关联
        if (!teacherIds.isEmpty()) {
            // TODO: 实现教师和学科的关联
            // 需要在TeacherMapper中添加相关方法
        }
        
        return result;
    }

    @Override
    public int deleteSubject(Integer subjectId) {
        // 检查是否有关联的考试
        List<Map<String, Object>> examStats = countExamsBySubject();
        for (Map<String, Object> stat : examStats) {
            if (subjectId.equals(stat.get("subjectId")) && (Long)stat.get("examCount") > 0) {
                return 0; // 有关联的考试，不能删除
            }
        }
        
        // 删除教师和学科的关联
        // TODO: 实现删除教师和学科的关联
        // 需要在TeacherMapper中添加相关方法
        
        // 删除学科记录
        return subjectMapper.deleteById(subjectId);
    }

    @Override
    public Map<String, Object> getSubjectStatistics(Integer subjectId) {
        Map<String, Object> statistics = new HashMap<>();
        
        // 获取学科基本信息
        Subject subject = subjectMapper.selectById(subjectId);
        if (subject == null) {
            return statistics;
        }
        statistics.put("subject", subject);
        
        // 获取教师列表
        List<Map<String, Object>> teachers = teacherMapper.selectBySubjectId(subjectId);
        statistics.put("teachers", teachers);
        
        // 统计考试信息
        List<Map<String, Object>> examStats = countExamsBySubject();
        for (Map<String, Object> stat : examStats) {
            if (subjectId.equals(stat.get("subjectId"))) {
                statistics.put("examStats", stat);
                break;
            }
        }
        
        // 统计成绩信息
        List<Map<String, Object>> scoreStats = getAvgScoreBySubject();
        for (Map<String, Object> stat : scoreStats) {
            if (subjectId.equals(stat.get("subjectId"))) {
                statistics.put("scoreStats", stat);
                break;
            }
        }
        
        return statistics;
    }
} 
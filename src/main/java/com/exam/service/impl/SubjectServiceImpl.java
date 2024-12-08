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
public class SubjectServiceImpl extends BaseServiceImpl<Subject, SubjectMapper> implements SubjectService {

    @Autowired
    private TeacherMapper teacherMapper;
    
    @Autowired
    private ExamMapper examMapper;

    @Override
    public List<Subject> getByCollegeId(Integer collegeId) {
        return baseMapper.selectByCollegeId(collegeId);
    }

    @Override
    public Subject getBySubjectName(String subjectName) {
        return baseMapper.selectBySubjectName(subjectName);
    }

    @Override
    public int updateDescription(Integer subjectId, String description) {
        return baseMapper.updateDescription(subjectId, description);
    }

    @Override
    public int batchUpdateDescription(List<Integer> subjectIds, String description) {
        return baseMapper.batchUpdateDescription(subjectIds, description);
    }

    @Override
    public List<Subject> getSubjectsWithExams() {
        return baseMapper.selectSubjectsWithExams();
    }

    @Override
    public List<Subject> getByTeacherId(Integer teacherId) {
        return baseMapper.selectByTeacherId(teacherId);
    }

    @Override
    public List<Subject> getByStudentId(Integer studentId) {
        return baseMapper.selectByStudentId(studentId);
    }

    @Override
    public List<Map<String, Object>> countExamsBySubject() {
        return baseMapper.countExamsBySubject();
    }

    @Override
    public List<Map<String, Object>> getAvgScoreBySubject() {
        return baseMapper.avgScoreBySubject();
    }

    @Override
    public List<Subject> getHotSubjects(Integer limit) {
        return baseMapper.selectHotSubjects(limit);
    }

    @Override
    public List<Subject> getDifficultSubjects(Integer limit) {
        return baseMapper.selectDifficultSubjects(limit);
    }

    @Override
    public int createSubject(Subject subject, List<Integer> teacherIds) {
        // 插入学科记录
        int result = baseMapper.insert(subject);
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
        return baseMapper.deleteById(subjectId);
    }

    @Override
    public Map<String, Object> getSubjectStatistics(Integer subjectId) {
        Map<String, Object> statistics = new HashMap<>();
        
        // 获取学科基本信息
        Subject subject = baseMapper.selectById(subjectId);
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
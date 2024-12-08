package com.exam.service.impl;

import com.exam.entity.Class;
import com.exam.entity.Student;
import com.exam.entity.Exam;
import com.exam.mapper.ClassMapper;
import com.exam.mapper.StudentMapper;
import com.exam.mapper.ExamMapper;
import com.exam.service.ClassService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

/**
 * 班级服务实现类
 */
@Service
@Transactional
public class ClassServiceImpl extends BaseServiceImpl<Class, ClassMapper> implements ClassService {

    @Autowired
    private StudentMapper studentMapper;
    
    @Autowired
    private ExamMapper examMapper;

    @Override
    public List<Class> getByTeacherId(Integer teacherId) {
        return baseMapper.selectByTeacherId(teacherId);
    }

    @Override
    public List<Class> getBySubjectId(Integer subjectId) {
        return baseMapper.selectBySubjectId(subjectId);
    }

    @Override
    public Class getByClassName(String className) {
        return baseMapper.selectByClassName(className);
    }

    @Override
    public int updateFinalExam(Integer classId, Boolean finalExam) {
        return baseMapper.updateFinalExam(classId, finalExam);
    }

    @Override
    public List<Student> getClassStudents(Integer classId) {
        return baseMapper.selectClassStudents(classId);
    }

    @Override
    public List<Exam> getClassExams(Integer classId) {
        return baseMapper.selectClassExams(classId);
    }

    @Override
    public Exam getFinalExam(Integer classId) {
        return baseMapper.selectFinalExam(classId);
    }

    @Override
    public Long countStudents(Integer classId) {
        return baseMapper.countStudents(classId);
    }

    @Override
    public Long countExams(Integer classId) {
        return baseMapper.countExams(classId);
    }

    @Override
    public Double getAvgScore(Integer classId, Integer examId) {
        return baseMapper.selectAvgScore(classId, examId);
    }

    @Override
    public List<Map<String, Object>> getScoreDistribution(Integer classId, Integer examId) {
        return baseMapper.selectScoreDistribution(classId, examId);
    }

    @Override
    public List<Exam> getExamSchedule(Integer classId, Date startTime, Date endTime) {
        return baseMapper.selectExamSchedule(classId, startTime, endTime);
    }

    @Override
    public int batchAddStudents(Integer classId, List<Integer> studentIds) {
        return baseMapper.batchAddStudents(classId, studentIds);
    }

    @Override
    public int batchRemoveStudents(Integer classId, List<Integer> studentIds) {
        return baseMapper.batchRemoveStudents(classId, studentIds);
    }

    @Override
    public int createClass(Class classInfo, Integer teacherId, List<Integer> studentIds) {
        // 设置班级信息
        classInfo.setTeacherId(teacherId);
        
        // 插入班级记录
        int result = baseMapper.insert(classInfo);
        if (result == 0) {
            return 0;
        }
        
        // 批量添加学生
        if (!studentIds.isEmpty()) {
            baseMapper.batchAddStudents(classInfo.getClassId(), studentIds);
        }
        
        return result;
    }

    @Override
    public int dissolveClass(Integer classId) {
        // 先移除所有学生
        List<Student> students = getClassStudents(classId);
        if (!students.isEmpty()) {
            List<Integer> studentIds = new ArrayList<>();
            for (Student student : students) {
                studentIds.add(student.getStudentId());
            }
            batchRemoveStudents(classId, studentIds);
        }
        
        // 删除班级记录
        return baseMapper.deleteById(classId);
    }

    @Override
    public Map<String, Object> getClassStatistics(Integer classId) {
        Map<String, Object> statistics = new HashMap<>();
        
        // 获取班级基本信息
        Class classInfo = baseMapper.selectById(classId);
        if (classInfo == null) {
            return statistics;
        }
        statistics.put("classInfo", classInfo);
        
        // 统计学生数量
        statistics.put("studentCount", countStudents(classId));
        
        // 统计考试数量
        statistics.put("examCount", countExams(classId));
        
        // 获取考试列表
        List<Exam> exams = getClassExams(classId);
        statistics.put("exams", exams);
        
        // 统计各次考试平均分
        List<Map<String, Object>> examScores = new ArrayList<>();
        for (Exam exam : exams) {
            Map<String, Object> examScore = new HashMap<>();
            examScore.put("examId", exam.getExamId());
            examScore.put("examName", exam.getExamName());
            examScore.put("avgScore", getAvgScore(classId, exam.getExamId()));
            examScore.put("distribution", getScoreDistribution(classId, exam.getExamId()));
            examScores.add(examScore);
        }
        statistics.put("examScores", examScores);
        
        return statistics;
    }

    @Override
    public Map<String, Object> exportStudentList(Integer classId) {
        Map<String, Object> data = new HashMap<>();
        
        // 获取班级信息
        Class classInfo = baseMapper.selectById(classId);
        if (classInfo == null) {
            return data;
        }
        data.put("classInfo", classInfo);
        
        // 获取学生列表
        List<Student> students = getClassStudents(classId);
        data.put("students", students);
        
        // 获取考试信息
        List<Exam> exams = getClassExams(classId);
        data.put("exams", exams);
        
        return data;
    }

    @Override
    public int importStudents(Integer classId, List<Student> students) {
        // 检查班级是否存在
        if (baseMapper.selectById(classId) == null) {
            return 0;
        }
        
        // 批量插入学生
        int result = studentMapper.batchInsert(students);
        if (result == 0) {
            return 0;
        }
        
        // 获取学生ID列表
        List<Integer> studentIds = new ArrayList<>();
        for (Student student : students) {
            studentIds.add(student.getStudentId());
        }
        
        // 建立班级和学生的关联
        return batchAddStudents(classId, studentIds);
    }
} 
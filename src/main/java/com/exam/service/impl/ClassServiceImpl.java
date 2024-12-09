package com.exam.service.impl;

import com.exam.entity.Class;
import com.exam.entity.Student;
import com.exam.entity.Exam;
import com.exam.service.ClassService;
import com.exam.mapper.ClassMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Date;
import java.util.HashMap;

/**
 * 班级服务实现类
 */
@Service
public class ClassServiceImpl implements ClassService {

    @Autowired
    private ClassMapper classMapper;

    @Override
    public int insert(Class record) {
        return classMapper.insert(record);
    }

    @Override
    public int deleteById(Integer id) {
        return classMapper.deleteById(id);
    }

    @Override
    public int updateById(Class record) {
        return classMapper.updateById(record);
    }

    @Override
    public Class selectById(Integer id) {
        return classMapper.selectById(id);
    }

    @Override
    public List<Class> selectAll() {
        return classMapper.selectAll();
    }

    @Override
    public List<Class> selectPage(Integer pageNum, Integer pageSize) {
        int offset = (pageNum - 1) * pageSize;
        return classMapper.selectPage(offset, pageSize);
    }

    @Override
    public Long selectCount() {
        return classMapper.selectCount();
    }

    @Override
    public List<Class> selectByCondition(Map<String, Object> condition) {
        return classMapper.selectByCondition(condition);
    }

    @Override
    public Long selectCountByCondition(Map<String, Object> condition) {
        return classMapper.selectCountByCondition(condition);
    }

    @Override
    public List<Class> selectPageByCondition(Map<String, Object> condition, Integer pageNum, Integer pageSize) {
        int offset = (pageNum - 1) * pageSize;
        return classMapper.selectPageByCondition(condition, offset, pageSize);
    }

    @Override
    public List<Class> getByTeacherId(Integer teacherId) {
        return classMapper.selectByTeacherId(teacherId);
    }

    @Override
    public List<Class> getBySubjectId(Integer subjectId) {
        return classMapper.selectBySubjectId(subjectId);
    }

    @Override
    public Class getByClassName(String className) {
        return classMapper.selectByClassName(className);
    }

    @Override
    public int updateFinalExam(Integer classId, Boolean finalExam) {
        return classMapper.updateFinalExam(classId, finalExam);
    }

    @Override
    public List<Student> getClassStudents(Integer classId) {
        return classMapper.selectClassStudents(classId);
    }

    @Override
    public List<Exam> getClassExams(Integer classId) {
        return classMapper.selectClassExams(classId);
    }

    @Override
    public Exam getFinalExam(Integer classId) {
        return classMapper.selectFinalExam(classId);
    }

    @Override
    public Long countStudents(Integer classId) {
        return classMapper.countStudents(classId);
    }

    @Override
    public Long countExams(Integer classId) {
        return classMapper.countExams(classId);
    }

    @Override
    public Double getAvgScore(Integer classId, Integer examId) {
        return classMapper.selectAvgScore(classId, examId);
    }

    @Override
    public List<Map<String, Object>> getScoreDistribution(Integer classId, Integer examId) {
        return classMapper.selectScoreDistribution(classId, examId);
    }

    @Override
    public List<Exam> getExamSchedule(Integer classId) {
        // 默认查询未来一个月的考试安排
        Date startTime = new Date();
        Date endTime = new Date(startTime.getTime() + 30L * 24 * 60 * 60 * 1000);
        return classMapper.selectExamSchedule(classId, startTime, endTime);
    }

    @Override
    @Transactional
    public int batchAddStudents(Integer classId, List<Integer> studentIds) {
        return classMapper.batchAddStudents(classId, studentIds);
    }

    @Override
    @Transactional
    public int batchRemoveStudents(Integer classId, List<Integer> studentIds) {
        return classMapper.batchRemoveStudents(classId, studentIds);
    }

    @Override
    @Transactional
    public int createClass(Class classInfo, Integer teacherId, List<Integer> studentIds) {
        // 设置教师ID
        classInfo.setTeacherId(teacherId);
        
        // 插入班级信息
        int result = classMapper.insert(classInfo);
        if (result > 0 && studentIds != null && !studentIds.isEmpty()) {
            // 批量添加学生
            classMapper.batchAddStudents(classInfo.getClassId(), studentIds);
        }
        return result;
    }

    @Override
    @Transactional
    public int dissolveClass(Integer classId) {
        // 先移除所有学生
        Map<String, Object> condition = new HashMap<>();
        condition.put("classId", classId);
        List<Student> students = classMapper.selectClassStudents(classId);
        if (!students.isEmpty()) {
            List<Integer> studentIds = students.stream()
                .map(Student::getStudentId)
                .toList();
            classMapper.batchRemoveStudents(classId, studentIds);
        }
        
        // 删除班级
        return classMapper.deleteById(classId);
    }

    @Override
    public Map<String, Object> getClassStatistics(Integer classId) {
        Map<String, Object> statistics = new HashMap<>();
        
        // 获取班级基本信息
        Class classInfo = classMapper.selectById(classId);
        statistics.put("classInfo", classInfo);
        
        // 获取学生数量
        Long studentCount = classMapper.countStudents(classId);
        statistics.put("studentCount", studentCount);
        
        // 获取考试数量
        Long examCount = classMapper.countExams(classId);
        statistics.put("examCount", examCount);
        
        // 获取考试安排
        List<Exam> examSchedule = getExamSchedule(classId);
        statistics.put("upcomingExams", examSchedule);
        
        // 获取期末考试信息
        Exam finalExam = classMapper.selectFinalExam(classId);
        statistics.put("finalExam", finalExam);
        
        return statistics;
    }

    @Override
    public Map<String, Object> exportStudentList(Integer classId) {
        Map<String, Object> exportData = new HashMap<>();
        
        // 获取班级信息
        Class classInfo = classMapper.selectById(classId);
        exportData.put("classInfo", classInfo);
        
        // 获取学生列表
        List<Student> students = classMapper.selectClassStudents(classId);
        exportData.put("students", students);
        
        return exportData;
    }

    @Override
    @Transactional
    public int importStudents(Integer classId, List<Student> students) {
        if (students == null || students.isEmpty()) {
            return 0;
        }
        
        // 提取学生ID列表
        List<Integer> studentIds = students.stream()
            .map(Student::getStudentId)
            .toList();
            
        // 批量添加学生到班级
        return classMapper.batchAddStudents(classId, studentIds);
    }
} 
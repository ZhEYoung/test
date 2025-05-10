package com.exam.service.impl;

import com.exam.service.ExamStudentService;
import com.exam.mapper.ExamStudentMapper;
import com.exam.mapper.ExamMapper;
import com.exam.mapper.StudentScoreMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

/**
 * 考试-学生服务实现类
 */
@Service
@Transactional
public class ExamStudentServiceImpl implements ExamStudentService {

    @Autowired
    private ExamStudentMapper examStudentMapper;
    
    @Autowired
    private ExamMapper examMapper;
    
    @Autowired
    private StudentScoreMapper studentScoreMapper;

    @Override
    public int recordStartExam(Integer examId, Integer studentId) {
        Map<String, Object> params = new HashMap<>();
        params.put("examId", examId);
        params.put("studentId", studentId);
        params.put("startTime", new Date());
        return examStudentMapper.updateStartTime(params);
    }

    @Override
    public int recordSubmitExam(Integer examId, Integer studentId) {
        Map<String, Object> params = new HashMap<>();
        params.put("examId", examId);
        params.put("studentId", studentId);
        params.put("submitTime", new Date());
        return examStudentMapper.updateSubmitTime(params);
    }

    @Override
    public int markAbsent(Integer examId, Integer studentId) {
        Map<String, Object> params = new HashMap<>();
        params.put("examId", examId);
        params.put("studentId", studentId);
        params.put("absent", true);
        return examStudentMapper.updateAbsentStatus(params);
    }

    @Override
    public int markDisciplinary(Integer examId, Integer studentId, String comment) {
        Map<String, Object> params = new HashMap<>();
        params.put("examId", examId);
        params.put("studentId", studentId);
        params.put("disciplinary", true);
        params.put("comment", comment);
        return examStudentMapper.updateDisciplinaryStatus(params);
    }

    @Override
    public int addTeacherComment(Integer examId, Integer studentId, String comment) {
        return examStudentMapper.updateInvigilationRecord(examId, studentId, comment);
    }

    @Override
    public Map<String, Object> getExamParticipation(Integer examId) {
        Map<String, Object> participation = new HashMap<>();
        
        // 统计各类考试人数
        participation.put("totalStudents", countTotalStudents(examId));
        participation.put("startedStudents", countStartedStudents(examId));
        participation.put("submittedStudents", countSubmittedStudents(examId));
        participation.put("absentStudents", countAbsentStudents(examId));
        
        // 计算参与率
        int total = (int) participation.get("totalStudents");
        int started = (int) participation.get("startedStudents");
        if (total > 0) {
            double participationRate = (double) started / total * 100;
            participation.put("participationRate", String.format("%.2f%%", participationRate));
        } else {
            participation.put("participationRate", "0.00%");
        }
        
        return participation;
    }

    @Override
    public Map<String, Object> getExamCompletionRate(Integer examId, Integer classId) {
        return examStudentMapper.selectExamCompletionRate(examId, classId);
    }

    @Override
    public List<Map<String, Object>> getAbnormalTimeStudents(Integer examId, Integer timeThreshold) {
        return examStudentMapper.selectAbnormalTimeStudents(examId, timeThreshold);
    }

    @Override
    public List<Map<String, Object>> getDisciplinaryRecords(Date startTime, Date endTime) {
        return examStudentMapper.selectDisciplinaryRecords(startTime, endTime);
    }

    @Override
    public int batchUpdateExamTime(Integer examId, List<Map<String, Object>> studentTimes) {
        Map<String, Object> params = new HashMap<>();
        params.put("examId", examId);
        params.put("studentTimes", studentTimes);
        return examStudentMapper.batchUpdateExamTime(params);
    }

    @Override
    public int countTotalStudents(Integer examId) {
        return examStudentMapper.countByExamId(examId);
    }

    @Override
    public int countStartedStudents(Integer examId) {
        return examStudentMapper.countStartedStudents(examId);
    }

    @Override
    public int countSubmittedStudents(Integer examId) {
        return examStudentMapper.countSubmittedStudents(examId);
    }

    @Override
    public int countAbsentStudents(Integer examId) {
        return examStudentMapper.countAbsentStudents(examId);
    }
} 
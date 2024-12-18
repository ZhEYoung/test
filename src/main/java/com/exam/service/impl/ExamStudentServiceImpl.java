package com.exam.service.impl;

import com.exam.entity.Exam;
import com.exam.entity.ExamStudent;
import com.exam.entity.StudentScore;
import com.exam.service.ExamStudentService;
import com.exam.mapper.ExamStudentMapper;
import com.exam.mapper.ExamMapper;
import com.exam.mapper.StudentScoreMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.math.BigDecimal;

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
        // 获取考试信息
        Exam exam = examMapper.selectById(examId);
        if (exam == null) {
            throw new RuntimeException("考试不存在");
        }
        // 记录开始考试时间
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
        params.put("retakeNeeded", true);// 缺考的学生默认需要重考
        params.put("teacherComment", "缺考");
        params.put("studentStartTime", new Date());
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

    @Override
    public int updateExamTime(Integer examId, Integer studentId, Date startTime, Date submitTime) {
        return examStudentMapper.updateExamTime(examId, studentId, startTime, submitTime);
    }

    @Override
    public List<ExamStudent> getNeedRetakeStudents(Integer examId) {
        return examStudentMapper.selectNeedRetakeStudents(examId);
    }

    @Override
    public List<ExamStudent> getStudentRetakeExams(Integer studentId) {
        return examStudentMapper.selectStudentRetakeExams(studentId);
    }

    @Override
    public List<Map<String, Object>> getRetakeStudentsBySubject(
            Integer subjectId,
            Integer teacherId,
            String studentName,
            Date examTimeStart,
            Date examTimeEnd) {
        // 查询教师权限和所属学院
        Map<String, Object> teacherInfo = examMapper.selectTeacherInfo(teacherId);
        if (teacherInfo == null) {
            return Collections.emptyList();
        }
        
        Integer teacherPermission = (Integer) teacherInfo.get("permission");
        Integer collegeId = (Integer) teacherInfo.get("college_id");
        
        // 如果教师权限为0，可以查看本学院所有需要重考的学生
        if (teacherPermission != null && teacherPermission == 0) {
            // 检查科目是否属于教师所在学院
            Integer subjectCollegeId = examMapper.selectSubjectCollegeId(subjectId);
            if (!collegeId.equals(subjectCollegeId)) {
                return Collections.emptyList();
            }
            
            return examStudentMapper.selectRetakeStudentsBySubject(
                subjectId,
                null, // 不限制教师ID
                collegeId, // 限制学院
                studentName,
                examTimeStart,
                examTimeEnd
            );
        }
        
        // 其他权限的教师只能查看自己负责的考试的重考学生
        return examStudentMapper.selectRetakeStudentsBySubject(
            subjectId,
            teacherId,
            null, // 不限制学院
            studentName,
            examTimeStart,
            examTimeEnd
        );
    }

    @Override
    public Map<String, Object> countRetakeBySubject(Integer subjectId, Integer teacherId) {
        return examStudentMapper.countRetakeBySubject(subjectId, teacherId);
    }

    @Override
    public int markRetakeNeeded(Integer examId, Integer studentId) {
        // 先查询考试-学生关联记录
        ExamStudent examStudent = examStudentMapper.selectByExamAndStudent(examId, studentId);
        if (examStudent == null) {
            // 如果不存在关联记录，需要先创建一个
            examStudent = new ExamStudent();
            examStudent.setExamId(examId);
            examStudent.setStudentId(studentId);
            examStudent.setRetakeNeeded(true);
            return examStudentMapper.insert(examStudent);
        }
        
        // 更新重考状态，保持其他状态不变
        return examStudentMapper.updateStatus(
            examStudent.getEsId(),      
            examStudent.getAbsent(),    
            true,                       // 设置需要重考
            examStudent.getDisciplinary(),
            examStudent.getTeacherComment()
        );
    }

    @Override
    public int markRetakeNotNeeded(Integer examId, Integer studentId) {
        // 先查询考试-学生关联记录
        ExamStudent examStudent = examStudentMapper.selectByExamAndStudent(examId, studentId);
        if (examStudent == null) {
            // 如果不存在关联记录，需要先创建一个
            examStudent = new ExamStudent();
            examStudent.setExamId(examId);
            examStudent.setStudentId(studentId);
            examStudent.setRetakeNeeded(true);
            return examStudentMapper.insert(examStudent);
        }

        // 更新重考状态，保持其他状态不变
        return examStudentMapper.updateStatus(
                examStudent.getEsId(),
                examStudent.getAbsent(),
                false,           // 设置不需要重考
                examStudent.getDisciplinary(),
                examStudent.getTeacherComment()
        );
    }

    @Override
    public List<ExamStudent> getByExamId(Integer examId) {
        return examStudentMapper.selectByExamId(examId);
    }

    @Override
    public int deleteById(Integer esId) {
        return examStudentMapper.deleteById(esId);
    }


    @Override
    public List<ExamStudent> getByStudentId(Integer studentId) {
        return examStudentMapper.selectByStudentId(studentId);
    }

    @Override
    public ExamStudent getByExamIdAndStudentId(Integer examId, Integer studentId){
        return examStudentMapper.selectByExamAndStudent(examId, studentId);
    }

    @Override
    public List<ExamStudent> getUnsubmittedStudents(Integer examId) {
        return examStudentMapper.selectUnsubmittedStudents(examId);
    }

    @Override
    public List<ExamStudent> getNotStartedStudents(Integer examId) {
        return examStudentMapper.selectNotStartedStudents(examId);
    }

} 
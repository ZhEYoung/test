package com.exam.service.impl;

import com.exam.entity.Exam;
import com.exam.mapper.ExamMapper;
import com.exam.mapper.ExamStudentMapper;
import com.exam.mapper.StudentScoreMapper;
import com.exam.service.ExamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

/**
 * 考试服务实现类
 */
@Service
@Transactional
public class ExamServiceImpl extends BaseServiceImpl<Exam, ExamMapper> implements ExamService {

    @Autowired
    private ExamStudentMapper examStudentMapper;
    
    @Autowired
    private StudentScoreMapper studentScoreMapper;

    @Override
    public List<Exam> getBySubjectId(Integer subjectId) {
        return baseMapper.selectBySubjectId(subjectId);
    }

    @Override
    public List<Exam> getByPaperId(Integer paperId) {
        return baseMapper.selectByPaperId(paperId);
    }

    @Override
    public List<Exam> getByTeacherId(Integer teacherId) {
        return baseMapper.selectByTeacherId(teacherId);
    }

    @Override
    public List<Exam> getByStatus(Integer examStatus) {
        return baseMapper.selectByStatus(examStatus);
    }

    @Override
    public List<Exam> getByType(Integer examType) {
        return baseMapper.selectByType(examType);
    }

    @Override
    public int updateStatus(Integer examId, Integer status) {
        return baseMapper.updateStatus(examId, status);
    }

    @Override
    public List<Exam> getByStudentId(Integer studentId) {
        return baseMapper.selectByStudentId(studentId);
    }

    @Override
    public List<Exam> getByClassId(Integer classId) {
        return baseMapper.selectByClassId(classId);
    }

    @Override
    public List<Exam> getByTimeRange(Date startTime, Date endTime) {
        return baseMapper.selectByTimeRange(startTime, endTime);
    }

    @Override
    public List<Exam> getByConditions(Integer subjectId, Integer teacherId, 
                                    Integer examType, Integer examStatus,
                                    Date startTime, Date endTime,
                                    Integer pageNum, Integer limit) {
        int offset = (pageNum - 1) * limit;
        return baseMapper.selectByConditions(subjectId, teacherId, examType, examStatus,
                startTime, endTime, offset, limit);
    }

    @Override
    public Long countByTimeRange(Date startTime, Date endTime) {
        return baseMapper.countByTimeRange(startTime, endTime);
    }

    @Override
    public int updateDuration(Integer examId, Integer duration) {
        return baseMapper.updateDuration(examId, duration);
    }

    @Override
    public int batchAddExamClass(Integer examId, List<Integer> classIds) {
        return baseMapper.batchInsertExamClass(examId, classIds);
    }

    @Override
    public int removeExamClass(Integer examId, Integer classId) {
        return baseMapper.deleteExamClass(examId, classId);
    }

    @Override
    public int publishExam(Integer examId) {
        // 检查考试是否可以发布
        Exam exam = baseMapper.selectById(examId);
        if (exam == null || exam.getExamStatus() != 0) {
            return 0;
        }
        // 更新考试状态为已发布
        return updateStatus(examId, 1);
    }

    @Override
    public int startExam(Integer examId) {
        // 检查考试是否可以开始
        Exam exam = baseMapper.selectById(examId);
        if (exam == null || exam.getExamStatus() != 1) {
            return 0;
        }
        // 更新考试状态为进行中
        return updateStatus(examId, 2);
    }

    @Override
    public int endExam(Integer examId) {
        // 检查考试是否可以结束
        Exam exam = baseMapper.selectById(examId);
        if (exam == null || exam.getExamStatus() != 2) {
            return 0;
        }
        // 更新考试状态为已结束
        return updateStatus(examId, 3);
    }

    @Override
    public Map<String, Object> getExamStatistics(Integer examId) {
        Map<String, Object> statistics = new HashMap<>();
        
        // 获取考试信息
        Exam exam = baseMapper.selectById(examId);
        if (exam == null) {
            return statistics;
        }
        
        // 统计考试参与情况
        Map<String, Object> participation = examStudentMapper.countExamParticipation(examId);
        statistics.putAll(participation);
        
        // 统计考试成绩分布
        List<Map<String, Object>> scoreDistribution = studentScoreMapper.selectScoreDistribution(examId, null);
        statistics.put("scoreDistribution", scoreDistribution);
        
        // 获取最高分、最低分、平均分
        Map<String, Object> scoreStats = new HashMap<>();
        scoreStats.put("maxScore", studentScoreMapper.selectTopStudents(examId, 1));
        scoreStats.put("avgScore", studentScoreMapper.selectByExamId(examId).stream()
                .mapToDouble(score -> score.getScore().doubleValue())
                .average()
                .orElse(0.0));
        statistics.put("scoreStats", scoreStats);
        
        return statistics;
    }

    @Override
    public Map<String, Object> getExamProgress(Integer examId) {
        Map<String, Object> progress = new HashMap<>();
        
        // 获取考试进行状态
        List<Map<String, Object>> examProgress = examStudentMapper.selectExamProgress(examId);
        progress.put("progress", examProgress);
        
        // 获取考试时间异常的学生
        List<Map<String, Object>> abnormalStudents = examStudentMapper.selectAbnormalTimeStudents(examId, 30);
        progress.put("abnormalStudents", abnormalStudents);
        
        // 获取考试完成率
        Map<String, Object> completionRate = examStudentMapper.selectExamCompletionRate(examId, null);
        progress.put("completionRate", completionRate);
        
        return progress;
    }
} 
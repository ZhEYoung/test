package com.exam.task;

import com.exam.entity.*;
import com.exam.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 考试自动处理任务
 * 1. 考试时间到，强制交卷
 * 2. 考试开始10分钟后标记缺考
 */
@Slf4j
@Component
public class ExamAutoProcessTask {

    @Autowired
    private ExamService examService;

    @Autowired
    private ExamStudentService examStudentService;

    @Autowired
    private StudentQuestionScoreService studentQuestionScoreService;

    @Autowired
    private ExamPaperService examPaperService;

    @Autowired
    private StudentScoreService studentScoreService;

    /**
     * 每分钟执行一次，处理考试自动提交和缺考标记
     */
    @Scheduled(cron = "0 * * * * ?")
    public void processExams() {
        try {
            Date now = new Date();
            
            // 处理已结束的考试
            processEndedExams(now);
            
            // 处理需要标记缺考的考试
            processAbsentStudents(now);
        } catch (Exception e) {
            log.error("考试自动处理任务执行失败", e);
        }
    }

    /**
     * 处理已结束的考试
     */
    @Transactional
    public void processEndedExams(Date now) {
        // 获取刚刚结束的考试（结束时间在当前时间前后1分钟内）
        List<Exam> endedExams = examService.getRecentlyEndedExams(now);
        
        for (Exam exam : endedExams) {
            try {
                // 获取未提交的学生
                List<ExamStudent> unsubmittedStudents = examStudentService.getUnsubmittedStudents(exam.getExamId());
                
                for (ExamStudent examStudent : unsubmittedStudents) {
                    // 强制提交考试
                    forceSubmitExam(exam, examStudent);
                }
                
                log.info("考试[{}]已结束，已处理{}名未提交学生的试卷", 
                    exam.getExamId(), unsubmittedStudents.size());
            } catch (Exception e) {
                log.error("处理已结束考试[{}]失败", exam.getExamId(), e);
            }
        }
    }

    /**
     * 处理需要标记缺考的学生
     */
    @Transactional
    public void processAbsentStudents(Date now) {
        // 获取开始超过10分钟的考试
        List<Exam> startedExams = examService.getRecentlyStartedExams(now);
        
        for (Exam exam : startedExams) {
            try {
                // 获取未开始考试的学生
                List<ExamStudent> notStartedStudents = examStudentService.getNotStartedStudents(exam.getExamId());
                
                for (ExamStudent examStudent : notStartedStudents) {
                    // 标记为缺考
                    markAsAbsent(exam, examStudent);
                }

                for (ExamStudent examStudent : notStartedStudents) {
                    // 强制提交考试
                    forceSubmitExam(exam, examStudent);
                }

                
                log.info("考试[{}]开始超过10分钟，已将{}名未开始考试的学生标记为缺考", 
                    exam.getExamId(), notStartedStudents.size());
            } catch (Exception e) {
                log.error("处理考试[{}]缺考学生失败", exam.getExamId(), e);
            }
        }
    }

    /**
     * 强制提交考试
     */
    public void forceSubmitExam(Exam exam, ExamStudent examStudent) {
        // 获取试卷所有题目
        ExamPaper paper = examPaperService.getById(exam.getPaperId());
        List<Question> questions = examPaperService.getPaperQuestions(paper.getPaperId());
        
        // 获取学生已答题目
        List<StudentQuestionScore> answeredQuestions = studentQuestionScoreService
            .getByExamAndStudent(exam.getExamId(), examStudent.getStudentId());
        
        // 获取未答题目
        Set<Integer> answeredQuestionIds = answeredQuestions.stream()
            .map(StudentQuestionScore::getQuestionId)
            .collect(Collectors.toSet());
        
        List<Question> unansweredQuestions = questions.stream()
            .filter(q -> !answeredQuestionIds.contains(q.getQuestionId()))
            .collect(Collectors.toList());

        // 创建或获取学生成绩记录
        StudentScore studentScore = studentScoreService.getByExamAndStudent(
            exam.getExamId(), examStudent.getStudentId());
        if (studentScore == null) {
            studentScore = new StudentScore();
            studentScore.setStudentId(examStudent.getStudentId());
            studentScore.setExamId(exam.getExamId());
            studentScore.setScore(BigDecimal.ZERO);
            studentScore.setUploadTime(new Date());
            studentScoreService.insert(studentScore);
        }

        // 为未答题目创建默认答案记录
        for (Question question : unansweredQuestions) {
            if(question.getType() == 3||question.getType() == 4) {
                StudentQuestionScore questionScore = new StudentQuestionScore();
                questionScore.setStudentId(examStudent.getStudentId());
                questionScore.setExamId(exam.getExamId());
                questionScore.setQuestionId(question.getQuestionId());
                questionScore.setScoreId(studentScore.getScoreId());
                questionScore.setAnswer(""); // 默认空答案
                questionScore.setScore(BigDecimal.ZERO); // 0分
                questionScore.setStatus(1); // 已批改状态
                studentQuestionScoreService.insert(questionScore);
            }else{
                StudentQuestionScore questionScore = new StudentQuestionScore();
                questionScore.setStudentId(examStudent.getStudentId());
                questionScore.setExamId(exam.getExamId());
                questionScore.setQuestionId(question.getQuestionId());
                questionScore.setScoreId(studentScore.getScoreId());
                questionScore.setAnswer("-1"); // 默认答案
                questionScore.setScore(BigDecimal.ZERO); // 0分
                questionScore.setStatus(1); // 已批改状态
                studentQuestionScoreService.insert(questionScore);
            }

        }

        // 计算总分（包括已答题目和未答题目）
        BigDecimal totalScore = answeredQuestions.stream()
            .map(StudentQuestionScore::getScore)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 更新总分
        studentScore.setScore(totalScore);
        studentScore.setUploadTime(new Date());
        studentScoreService.updateById(studentScore);

        // 记录提交时间
        examStudentService.recordSubmitExam(exam.getExamId(), examStudent.getStudentId());
    }

    /**
     * 标记学生为缺考
     */
    private void markAsAbsent(Exam exam, ExamStudent examStudent) {
        // 标记为缺考
        examStudentService.markAbsent(exam.getExamId(), examStudent.getStudentId());

        //标记需要重考
        examStudentService.markRetakeNeeded(exam.getExamId(), examStudent.getStudentId());

        // 创建成绩记录（0分）
        StudentScore studentScore = new StudentScore();
        studentScore.setStudentId(examStudent.getStudentId());
        studentScore.setExamId(exam.getExamId());
        studentScore.setScore(BigDecimal.ZERO);
        studentScore.setUploadTime(new Date());
        studentScoreService.insert(studentScore);
    }
} 
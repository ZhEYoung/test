package com.exam.controller.student;

import com.exam.entity.*;
import com.exam.service.*;
import com.exam.utils.TokenUtils;
import com.exam.common.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Slf4j
@RestController
@RequestMapping("/student/score")
@Api(tags = "学生成绩功能接口")
public class StudentScoreController {

    @Autowired
    private ExamService examService;

    @Autowired
    private ExamPaperService examPaperService;

    @Autowired
    private StudentQuestionScoreService studentQuestionScoreService;

    @Autowired
    private LogService logService;

    @Autowired
    private StudentScoreService studentScoreService;

    @Autowired
    private ExamStudentService examStudentService;

    @Autowired
    private ExamClassService examClassService;

    @Autowired
    private QuestionService questionService;

    @Autowired
    private QuestionOptionService questionOptionService;

    @Autowired
    private StudentService studentService;

    @ApiOperation("查看考试成绩信息")
    @GetMapping("/scores")
    public Result getExamScores(
            @RequestParam(required = false) Integer examId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            HttpServletRequest request) {
        User currentUser = TokenUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.error("用户未登录");
        }

        try {
            // 获取当前学生ID
            Integer UserId = currentUser.getUserId();
            Integer studentId = studentService.getByUserId(UserId).getStudentId();

            List<StudentScore> scores = new ArrayList<>();
            
            if (examId != null) {
                // 如果指定了考试ID，只查询该考试的成绩
                StudentScore score = studentScoreService.getByExamAndStudent(examId, studentId);
                if (score != null) {
                    // 获取考试信息
                    Exam exam = examService.getById(examId);
                    score.setExam(exam);
                    score.setExamName(exam.getExamName());

                    // 获取教师评语
                    ExamStudent examStudent = examStudentService.getByExamIdAndStudentId(examId, studentId);
                    if (examStudent != null) {
                        score.setTeacherComment(examStudent.getTeacherComment());
                    }

                    // 获取排名
                    Integer rank = studentScoreService.getStudentRank(examId, studentId);
                    score.setRank(rank);

                    scores.add(score);
                }
            } else {
                // 如果没有指定考试ID，查询该学生参加的所有考试成绩
                scores = studentScoreService.getByStudentId(studentId);
                for (StudentScore score : scores) {
                    // 获取考试信息
                    Exam exam = examService.getById(score.getExamId());
                    score.setExam(exam);
                    score.setExamName(exam.getExamName());

                    // 获取教师评语
                    ExamStudent examStudent = examStudentService.getByExamIdAndStudentId(score.getExamId(), studentId);
                    if (examStudent != null) {
                        score.setTeacherComment(examStudent.getTeacherComment());
                    }

                    // 获取排名
                    Integer rank = studentScoreService.getStudentRank(score.getExamId(), studentId);
                    score.setRank(rank);
                }
            }

            // 手动进行分页处理
            int start = (pageNum - 1) * pageSize;
            int end = Math.min(start + pageSize, scores.size());
            List<StudentScore> pagedScores = start < scores.size() ? scores.subList(start, end) : new ArrayList<>();

            Map<String, Object> data = new HashMap<>();
            data.put("list", pagedScores);
            data.put("total", (long) scores.size());

            // 记录操作日志
            Log operationLog = new Log();
            operationLog.setUserId(currentUser.getUserId());
            operationLog.setActionType(7); // SELECT
            operationLog.setActionDescription("学生[" + currentUser.getUsername() + "]查看了" + 
                (examId != null ? "考试[" + examId + "]的" : "所有") + "成绩信息");
            operationLog.setObjectType("SCORE");
            operationLog.setIpAddress(request.getRemoteAddr());
            operationLog.setDeviceInfo(request.getHeader("User-Agent"));
            operationLog.setStatus("SUCCESS");
            operationLog.setCreatedTime(new Date());
            logService.insert(operationLog);

            return Result.success(data);
        } catch (Exception e) {
            // 记录异常日志
            Log operationLog = new Log();
            operationLog.setUserId(currentUser.getUserId());
            operationLog.setActionType(5); // EXCEPTION
            operationLog.setActionDescription("学生[" + currentUser.getUsername() + "]查看成绩信息时发生异常：" + e.getMessage());
            operationLog.setObjectType("SCORE");
            operationLog.setIpAddress(request.getRemoteAddr());
            operationLog.setDeviceInfo(request.getHeader("User-Agent"));
            operationLog.setStatus("ERROR");
            operationLog.setCreatedTime(new Date());
            logService.insert(operationLog);

            log.error("获取考试成绩失败", e);
            return Result.error("获取考试成绩失败：" + e.getMessage());
        }
    }

    @ApiOperation("查看题目成绩信息")
    @GetMapping("/question-scores")
    public Result getQuestionScores(
            @RequestParam Integer examId,
            HttpServletRequest request) {
        User currentUser = TokenUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.error("用户未登录");
        }

        try {
            // 获取当前学生ID
            Integer userId = currentUser.getUserId();
            Integer studentId = studentService.getByUserId(userId).getStudentId();

            // 验证考试是否存在
            Exam exam = examService.getById(examId);
            if (exam == null) {
                return Result.error("考试不存在");
            }

            // 验证学生是否参加了这次考试
            StudentScore studentScore = studentScoreService.getByExamAndStudent(examId, studentId);
            if (studentScore == null) {
                return Result.error("未找到该考试的成绩记录");
            }

            // 获取题目得分情况
            List<StudentQuestionScore> questionScores = studentQuestionScoreService.getByExamAndStudent(examId, studentId);
            if (questionScores == null || questionScores.isEmpty()) {
                return Result.error("未找到题目得分记录");
            }

            for(StudentQuestionScore questionScore : questionScores) {
                // 获取题目信息
                Question question = questionService.getById(questionScore.getQuestionId());
                question.setOptions(questionOptionService.getByQuestionId(question.getQuestionId()));
                questionScore.setQuestion(question);
            }

            // 获取考试总分和得分率
            BigDecimal totalScore = BigDecimal.valueOf(100);
            BigDecimal obtainedScore = BigDecimal.ZERO;
            for (StudentQuestionScore questionScore : questionScores) {
                obtainedScore = obtainedScore.add(questionScore.getScore());
            }
            
            // 计算得分率
            BigDecimal scoreRate = totalScore.compareTo(BigDecimal.ZERO) > 0 
                ? obtainedScore.divide(totalScore, 4, RoundingMode.HALF_UP).multiply(new BigDecimal(100))
                : BigDecimal.ZERO;

            String teacherComment = examStudentService.getByExamIdAndStudentId(examId, studentId).getTeacherComment();

            Map<String, Object> data = new HashMap<>();
            data.put("questionScores", questionScores);
            data.put("totalScore", totalScore);
            data.put("obtainedScore", obtainedScore);
            data.put("scoreRate", scoreRate);
            data.put("exam", exam);
            data.put("teacherComment", teacherComment);

            // 记录操作日志
            Log operationLog = new Log();
            operationLog.setUserId(currentUser.getUserId());
            operationLog.setActionType(7); // SELECT
            operationLog.setActionDescription("学生[" + currentUser.getUsername() + "]查看了考试[" + examId + "]的题目得分情况");
            operationLog.setObjectType("QUESTION_SCORE");
            operationLog.setIpAddress(request.getRemoteAddr());
            operationLog.setDeviceInfo(request.getHeader("User-Agent"));
            operationLog.setStatus("SUCCESS");
            operationLog.setCreatedTime(new Date());
            logService.insert(operationLog);

            return Result.success(data);
        } catch (Exception e) {
            // 记录异常日志
            Log operationLog = new Log();
            operationLog.setUserId(currentUser.getUserId());
            operationLog.setActionType(5); // EXCEPTION
            operationLog.setActionDescription("学生[" + currentUser.getUsername() + "]查看题目得分情况时发生异常：" + e.getMessage());
            operationLog.setObjectType("QUESTION_SCORE");
            operationLog.setIpAddress(request.getRemoteAddr());
            operationLog.setDeviceInfo(request.getHeader("User-Agent"));
            operationLog.setStatus("ERROR");
            operationLog.setCreatedTime(new Date());
            logService.insert(operationLog);

            log.error("获取题目成绩失败", e);
            return Result.error("获取题目成绩失败：" + e.getMessage());
        }
    }
}

package com.exam.controller.teacher;

import com.exam.common.Result;
import com.exam.entity.ExamStudent;
import com.exam.entity.StudentScore;
import com.exam.service.ExamStudentService;
import com.exam.service.StudentQuestionScoreService;
import com.exam.service.StudentScoreService;
import com.exam.entity.StudentQuestionScore;
import com.exam.service.TeacherService;
import com.exam.utils.TokenUtils;
import com.exam.entity.User;
import com.exam.entity.Log;
import com.exam.service.LogService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Slf4j
@Api(tags = "教师改卷接口")
@RestController
@RequestMapping("/teacher/grading")
public class TeacherGradingController {

    @Autowired
    private StudentQuestionScoreService studentQuestionScoreService;

    @Autowired
    private StudentScoreService studentScoreService;

    @Autowired
    private TeacherService teacherService;

    @Autowired
    private ExamStudentService examStudentService;

    @Autowired
    private LogService logService;

    @ApiOperation("获取需要批改的试题列表")
    @GetMapping("/pending/{examId}")
    public Result<List<StudentQuestionScore>> getPendingGrading(@PathVariable Integer examId, HttpServletRequest request) {
        User currentUser = TokenUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.error("用户未登录");
        }

        try {
            Integer teacherId = teacherService.getTeacherIdByUserId(currentUser.getUserId());
            List<StudentQuestionScore> pendingQuestions = studentQuestionScoreService.getNeedManualGrading(examId, teacherId);
            for (StudentQuestionScore question : pendingQuestions) {
                String status = question.getStatus() == 0 ? "未批改" : "已批改";
                question.setStatusName(status);
            }

            // 记录查询日志
            Log operationLog = new Log();
            operationLog.setUserId(currentUser.getUserId());
            operationLog.setActionType(7); // SELECT
            operationLog.setActionDescription("教师[" + currentUser.getUsername() + "]查看了考试[" + examId + "]的待批改试题列表");
            operationLog.setObjectType("PENDING_GRADING");
            operationLog.setIpAddress(request.getRemoteAddr());
            operationLog.setDeviceInfo(request.getHeader("User-Agent"));
            operationLog.setStatus("SUCCESS");
            operationLog.setCreatedTime(new Date());
            logService.insert(operationLog);

            return Result.success(pendingQuestions);
        } catch (Exception e) {
            // 记录异常日志
            Log operationLog = new Log();
            operationLog.setUserId(currentUser.getUserId());
            operationLog.setActionType(5); // EXCEPTION
            operationLog.setActionDescription("教师[" + currentUser.getUsername() + "]查看考试[" + examId + "]待批改试题列表异常：" + e.getMessage());
            operationLog.setObjectType("PENDING_GRADING");
            operationLog.setIpAddress(request.getRemoteAddr());
            operationLog.setDeviceInfo(request.getHeader("User-Agent"));
            operationLog.setStatus("ERROR");
            operationLog.setCreatedTime(new Date());
            logService.insert(operationLog);
            log.error("获取待批改试题列表失败", e);
            return Result.error("获取待批改试题列表失败：" + e.getMessage());
        }
    }

//        result.put("recordId", questionScore.getRecordId());
//        result.put("questionId", questionId);
//        result.put("studentId", studentId);
//        result.put("examId", examId);
//        result.put("questionContent", question.getContent());
//        result.put("questionType", question.getType());
//        result.put("studentAnswer", questionScore.getAnswer());
//        result.put("standardAnswer", question.getAnswer());
//        result.put("score", questionScore.getScore());
//        result.put("fullScore", examPaperQuestion.getQuestionScore());
//        result.put("status", questionScore.getStatus());

    @ApiOperation("获取学生主观题答案")
    @GetMapping("/answer")
    public Result<Map<String, Object>> getStudentAnswer(
            @RequestParam Integer examId,
            @RequestParam Integer questionId,
            @RequestParam Integer studentId,
            HttpServletRequest request) {
        User currentUser = TokenUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.error("用户未登录");
        }

        try {
            Map<String, Object> answer = studentQuestionScoreService.getSubjectiveAnswer(examId, questionId, studentId);
            if (answer == null) {
                // 记录失败日志
                Log operationLog = new Log();
                operationLog.setUserId(currentUser.getUserId());
                operationLog.setActionType(7); // SELECT
                operationLog.setActionDescription("教师[" + currentUser.getUsername() + "]查看考试[" + examId + "]学生[" + studentId + "]的答案失败：未找到记录");
                operationLog.setObjectType("STUDENT_ANSWER");
                operationLog.setIpAddress(request.getRemoteAddr());
                operationLog.setDeviceInfo(request.getHeader("User-Agent"));
                operationLog.setStatus("FAILED");
                operationLog.setCreatedTime(new Date());
                logService.insert(operationLog);

                return Result.error("未找到答案记录");
            }

            // 记录查询日志
            Log operationLog = new Log();
            operationLog.setUserId(currentUser.getUserId());
            operationLog.setActionType(7); // SELECT
            operationLog.setActionDescription("教师[" + currentUser.getUsername() + "]查看了考试[" + examId + "]学生[" + studentId + "]的答案");
            operationLog.setObjectType("STUDENT_ANSWER");
            operationLog.setIpAddress(request.getRemoteAddr());
            operationLog.setDeviceInfo(request.getHeader("User-Agent"));
            operationLog.setStatus("SUCCESS");
            operationLog.setCreatedTime(new Date());
            logService.insert(operationLog);

            return Result.success(answer);
        } catch (Exception e) {
            // 记录异常日志
            Log operationLog = new Log();
            operationLog.setUserId(currentUser.getUserId());
            operationLog.setActionType(5); // EXCEPTION
            operationLog.setActionDescription("教师[" + currentUser.getUsername() + "]查看考试[" + examId + "]学生[" + studentId + "]答案异常：" + e.getMessage());
            operationLog.setObjectType("STUDENT_ANSWER");
            operationLog.setIpAddress(request.getRemoteAddr());
            operationLog.setDeviceInfo(request.getHeader("User-Agent"));
            operationLog.setStatus("ERROR");
            operationLog.setCreatedTime(new Date());
            logService.insert(operationLog);

            log.error("获取学生答案失败", e);
            return Result.error("获取学生答案失败：" + e.getMessage());
        }
    }

    @ApiOperation("批改试题")
    @PostMapping("/grade/{recordId}")
    public Result<String> gradeQuestion(
            @PathVariable Integer recordId,
            @RequestParam BigDecimal score,
            @RequestParam(defaultValue = "1") String status,
            HttpServletRequest request) {
        User currentUser = TokenUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.error("用户未登录");
        }

        try {
            int result = studentQuestionScoreService.updateScoreAndStatus(recordId, score, status);

            StudentQuestionScore studentQuestionScore = studentQuestionScoreService.getById(recordId);
            Integer examId = studentQuestionScore.getExamId();
            Integer studentId = studentQuestionScore.getStudentId();

        List<StudentQuestionScore> answeredQuestions = studentQuestionScoreService.getByExamAndStudent(examId, studentId);

        Boolean allGraded = answeredQuestions.stream()
                .allMatch(q -> q.getStatus() == 1);//没有未答题的题目，且都已经批改,则可以直接判断是否需要重考
        // 计算总分
        BigDecimal totalScore = answeredQuestions.stream()
                .map(StudentQuestionScore::getScore)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if(allGraded){//没有未答题的题目，且都已经批改,则可以直接判断是否需要重考
            // 如果总分低于60分，标记为需要重考
            if (totalScore.compareTo(new BigDecimal("60")) < 0) {
                examStudentService.markRetakeNeeded(examId, studentId);
            }
        }

        Date now = new Date();
        // 更新学生成绩
        StudentScore studentScore = studentScoreService.getByExamAndStudent(examId, studentId);
        if (studentScore == null) {
            studentScore = new StudentScore();
            studentScore.setStudentId(studentId);
            studentScore.setExamId(examId);
            studentScore.setScore(totalScore);
            studentScore.setUploadTime(now);
            studentScoreService.insert(studentScore);
        } else {
            studentScore.setScore(totalScore);
            studentScore.setUploadTime(now);
            studentScoreService.updateById(studentScore);
        }

            if (result > 0) {
                // 记录成功日志
                Log operationLog = new Log();
                operationLog.setUserId(currentUser.getUserId());
                operationLog.setActionType(1); // UPDATE
                operationLog.setActionDescription("教师[" + currentUser.getUsername() + "]批改了考试[" + examId + "]学生[" + studentId + "]的试题，得分：" + score);
                operationLog.setObjectType("GRADE_QUESTION");
                operationLog.setIpAddress(request.getRemoteAddr());
                operationLog.setDeviceInfo(request.getHeader("User-Agent"));
                operationLog.setStatus("SUCCESS");
                operationLog.setCreatedTime(new Date());
                logService.insert(operationLog);

                return Result.success("批改成功");
            }

            // 记录失败日志
            Log operationLog = new Log();
            operationLog.setUserId(currentUser.getUserId());
            operationLog.setActionType(1); // UPDATE
            operationLog.setActionDescription("教师[" + currentUser.getUsername() + "]批改考试[" + examId + "]学生[" + studentId + "]的试题失败");
            operationLog.setObjectType("GRADE_QUESTION");
            operationLog.setIpAddress(request.getRemoteAddr());
            operationLog.setDeviceInfo(request.getHeader("User-Agent"));
            operationLog.setStatus("FAILED");
            operationLog.setCreatedTime(new Date());
            logService.insert(operationLog);

            return Result.error("批改失败");
        } catch (Exception e) {
            // 记录异常日志
            Log operationLog = new Log();
            operationLog.setUserId(currentUser.getUserId());
            operationLog.setActionType(5); // EXCEPTION
            operationLog.setActionDescription("教师[" + currentUser.getUsername() + "]批改试题异常：" + e.getMessage());
            operationLog.setObjectType("GRADE_QUESTION");
            operationLog.setIpAddress(request.getRemoteAddr());
            operationLog.setDeviceInfo(request.getHeader("User-Agent"));
            operationLog.setStatus("ERROR");
            operationLog.setCreatedTime(new Date());
            logService.insert(operationLog);

            log.error("批改试题失败", e);
            return Result.error("批改试题失败：" + e.getMessage());
        }
    }

    @ApiOperation("获取题目得分统计")
    @GetMapping("/statistics/{examId}/{questionId}")
    public Result<Map<String, Object>> getQuestionStatistics(
            @PathVariable Integer examId,
            @PathVariable Integer questionId,
            HttpServletRequest request) {
        User currentUser = TokenUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.error("用户未登录");
        }

        try {
            Map<String, Object> statistics = studentQuestionScoreService.exportQuestionScoreReport(examId, questionId);

            // 记录查询日志
            Log operationLog = new Log();
            operationLog.setUserId(currentUser.getUserId());
            operationLog.setActionType(7); // SELECT
            operationLog.setActionDescription("教师[" + currentUser.getUsername() + "]查看了考试[" + examId + "]题目[" + questionId + "]的得分统计");
            operationLog.setObjectType("QUESTION_STATISTICS");
            operationLog.setIpAddress(request.getRemoteAddr());
            operationLog.setDeviceInfo(request.getHeader("User-Agent"));
            operationLog.setStatus("SUCCESS");
            operationLog.setCreatedTime(new Date());
            logService.insert(operationLog);

            return Result.success(statistics);
        } catch (Exception e) {
            // 记录异常日志
            Log operationLog = new Log();
            operationLog.setUserId(currentUser.getUserId());
            operationLog.setActionType(5); // EXCEPTION
            operationLog.setActionDescription("教师[" + currentUser.getUsername() + "]查看考试[" + examId + "]题目[" + questionId + "]得分统计异常：" + e.getMessage());
            operationLog.setObjectType("QUESTION_STATISTICS");
            operationLog.setIpAddress(request.getRemoteAddr());
            operationLog.setDeviceInfo(request.getHeader("User-Agent"));
            operationLog.setStatus("ERROR");
            operationLog.setCreatedTime(new Date());
            logService.insert(operationLog);

            log.error("获取题目得分统计失败", e);
            return Result.error("获取题目得分统计失败：" + e.getMessage());
        }
    }
} 
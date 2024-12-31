package com.exam.controller.teacher;

import com.exam.entity.*;
import com.exam.service.*;
import com.exam.task.ExamAutoProcessTask;
import com.exam.utils.TokenUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import com.exam.common.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;

import javax.servlet.http.HttpServletRequest;
import java.lang.Class;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Date;
import java.util.stream.Collectors;
import java.util.Calendar;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@RestController
@RequestMapping("/teacher/exam")
@Api(tags = "教师考试管理接口")
public class TeacherExamManageController {

    @Autowired
    private ExamService examService;

    @Autowired
    private ExamPaperService examPaperService;

    @Autowired
    private LogService logService;

    @Autowired
    private TeacherService teacherService;

    @Autowired
    private StudentQuestionScoreService studentQuestionScoreService;

    @Autowired
    private ExamStudentService examStudentService;

    @Autowired
    private ExamClassService examClassService;

    @Autowired
    private ClassService classService;

    @Autowired
    private SubjectService subjectService;

    @Autowired
    private CollegeService collegeService;

    @Autowired
    private StudentService studentService;

    @Autowired
    private ExamAutoProcessTask examAutoProcessTask;

    @Autowired
    private StudentScoreService studentScoreService;

    @Autowired
    private ExamPaperQuestionService examPaperQuestionService;

    @Autowired
    private QuestionService questionService;

    private Integer getTeacherIdByUserId(Integer userId) {
        Teacher teacher = teacherService.getByUserId(userId);
        return teacher != null ? teacher.getTeacherId() : null;
    }

    @ApiOperation("获取考试列表")
    @GetMapping("/list")
    public Result getExamList(
                            @RequestParam(defaultValue = "1") Integer pageNum,
                            @RequestParam(defaultValue = "10") Integer pageSize,
                            HttpServletRequest request) {
        User currentUser = TokenUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.error("用户未登录");
        }

        try {
            Integer teacherId = getTeacherIdByUserId(currentUser.getUserId());
            if (teacherId == null) {
                return Result.error("未找到教师信息");
            }

            // 获取教师发布的考试
            List<Exam> teacherExams = examService.getByTeacherId(teacherId);

            
            // 获取教师的全部班级
            List<com.exam.entity.Class> classes = classService.getByTeacherId(teacherId);

            // 获取班级对应的考试
            Set<Exam> classExams = new HashSet<>();
            for (com.exam.entity.Class clazz : classes) {
                List<Exam> exams = examClassService.getExamsByClassId(clazz.getClassId());
                if (exams != null) {
                    classExams.addAll(exams);
                }
            }

            // 合并两个列表，去重
            Set<Exam> uniqueExams = new HashSet<>(teacherExams);
            uniqueExams.addAll(classExams);
            List<Exam> allExams = new ArrayList<>(uniqueExams);

            // 按照考试开始时间降序排序
            allExams.sort((e1, e2) -> e2.getExamStartTime().compareTo(e1.getExamStartTime()));

            // 分页处理
            int start = (pageNum - 1) * pageSize;
            int end = Math.min(start + pageSize, allExams.size());
            List<Exam> pagedExams = allExams.subList(start, end);
            for(Exam exam : pagedExams) {
                exam.setSubject(subjectService.getById(exam.getSubjectId()));
                exam.setPaper(examPaperService.getById(exam.getPaperId()));
                exam.setTeacher(teacherService.getById(exam.getTeacherId()));
            }

            Map<String, Object> data = new HashMap<>();
            data.put("list", pagedExams);
            data.put("total", (long) allExams.size());

            // 记录操作日志
            Log operationLog = new Log();
            operationLog.setUserId(currentUser.getUserId());
            operationLog.setActionType(7); // SELECT
            operationLog.setActionDescription("教师[" + currentUser.getUsername() + "]查询了考试列表");
            operationLog.setObjectType("EXAM");
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
            operationLog.setActionDescription("教师[" + currentUser.getUsername() + "]查询考试列表异常：" + e.getMessage());
            operationLog.setObjectType("EXAM");
            operationLog.setIpAddress(request.getRemoteAddr());
            operationLog.setDeviceInfo(request.getHeader("User-Agent"));
            operationLog.setStatus("ERROR");
            operationLog.setCreatedTime(new Date());
            logService.insert(operationLog);

            log.error("获取考试列表失败", e);
            return Result.error("获取考试列表失败：" + e.getMessage());
        }
    }

    //命令格式：curl -X POST "http://localhost:8080/api/teacher/exam/publish?subjectId=1&classIds=1&classIds=2&paperId=162&examStartTime=2024-12-17%2010:55:00&examDuration=10" -H "token:eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJhdWQiOiIyIiwic2lnbiI6IjhkOTY5ZWVmNmVjYWQzYzI5YTNhNjI5MjgwZTY4NmNmMGMzZjVkNWE4NmFmZjNjYTEyMDIwYzkyM2FkYzZjOTIiLCJleHAiOjE3MzQ0ODg4MzQsImlhdCI6MTczNDQwMjQzNH0.QCVXmgHKm9SjIFNN7c9tponTxs8fVElYC7qE4OWjanw"
    @ApiOperation("发布普通考试")
    @PostMapping("/publish")
    public Result createExam(@RequestParam Integer subjectId,
                           @RequestParam List<Integer> classIds,
                           @RequestParam Integer paperId,
                           @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date examStartTime,
                           @RequestParam Integer examDuration,
                           HttpServletRequest request) {
        User currentUser = TokenUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.error("用户未登录");
        }

        try {
            Integer teacherId = getTeacherIdByUserId(currentUser.getUserId());
            if (teacherId == null) {
                return Result.error("未找到教师信息");
            }

            // 获取当前教师信息并验证权限
            Teacher teacher = teacherService.getByUserId(currentUser.getUserId());
            if (teacher == null) {
                return Result.error("未找到教师信息");
            }
            
            // 验证教师权限（0为管理员教师，1为普通教师）
            if (teacher.getPermission() != 0 && teacher.getPermission() != 1) {
                return Result.error("权限不足，只有管理员教师或普通教师可以发布普通考试");
            }

            // 检查试卷是否存在且已发布
            ExamPaper paper = examPaperService.getById(paperId);
            if (paper == null) {
                return Result.error("试卷不存在");
            }
            if (paper.getPaperStatus() != 0) {
                return Result.error("试卷已发布，不能创建考试");
            }

            // 调用服务层方法发布普通考试，使用当前教师ID
            Exam normalExam = examService.publishNormalExam(
                teacherId,
                subjectId,
                classIds,
                paperId,
                examStartTime,
                examDuration
            );

            // 记录操作日志
            Log operationLog = new Log();
            operationLog.setUserId(currentUser.getUserId());
            operationLog.setActionType(0); // INSERT
            operationLog.setActionDescription("教师[" + currentUser.getUsername() + "]创建了新考试：" + normalExam.getExamName());
            operationLog.setObjectType("EXAM");
            operationLog.setIpAddress(request.getRemoteAddr());
            operationLog.setDeviceInfo(request.getHeader("User-Agent"));
            operationLog.setStatus("SUCCESS");
            operationLog.setCreatedTime(new Date());
            logService.insert(operationLog);

            return Result.success(normalExam);
        } catch (Exception e) {
            // 记录异常日志
            Log operationLog = new Log();
            operationLog.setUserId(currentUser.getUserId());
            operationLog.setActionType(5); // EXCEPTION
            operationLog.setActionDescription("教师[" + currentUser.getUsername() + "]创建考试异常：" + e.getMessage());
            operationLog.setObjectType("EXAM");
            operationLog.setIpAddress(request.getRemoteAddr());
            operationLog.setDeviceInfo(request.getHeader("User-Agent"));
            operationLog.setStatus("ERROR");
            operationLog.setCreatedTime(new Date());
            logService.insert(operationLog);

            log.error("创建考试失败", e);
            return Result.error("创建考试失败：" + e.getMessage());
        }
    }

    @ApiOperation("更新考试")
    @PutMapping("/{examId}")
    public Result updateExam(@PathVariable Integer examId,
                           @RequestBody Exam exam,
                           HttpServletRequest request) {
        User currentUser = TokenUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.error("用户未登录");
        }

        try {
            Exam existingExam = examService.getById(examId);
            if (existingExam == null) {
                return Result.error("考试不存在");
            }

            // 检查是否是考试的创建者
            Integer teacherId = getTeacherIdByUserId(currentUser.getUserId());
            if (teacherId == null) {
                return Result.error("未找到教师信息");
            }
            if (!existingExam.getTeacherId().equals(teacherId)) {
                return Result.error("无权修改该考试");
            }

            // 检查考试状态
            if (existingExam.getExamStatus() != 0) {
                return Result.error("考试已开始或已结束，不能修改");
            }

            exam.setExamId(examId);
            int result = examService.updateById(exam);
            if (result <= 0) {
                return Result.error("更新考试失败");
            }

            // 记录操作日志
            Log operationLog = new Log();
            operationLog.setUserId(currentUser.getUserId());
            operationLog.setActionType(1); // UPDATE
            operationLog.setActionDescription("教师[" + currentUser.getUsername() + "]更新了考试，考试ID：" + examId);
            operationLog.setObjectType("EXAM");
            operationLog.setIpAddress(request.getRemoteAddr());
            operationLog.setDeviceInfo(request.getHeader("User-Agent"));
            operationLog.setStatus("SUCCESS");
            operationLog.setCreatedTime(new Date());
            logService.insert(operationLog);

            return Result.success();
        } catch (Exception e) {
            // 记录异常日志
            Log operationLog = new Log();
            operationLog.setUserId(currentUser.getUserId());
            operationLog.setActionType(5); // EXCEPTION
            operationLog.setActionDescription("教师[" + currentUser.getUsername() + "]更新考试异常，考试ID：" + examId + "，错误：" + e.getMessage());
            operationLog.setObjectType("EXAM");
            operationLog.setIpAddress(request.getRemoteAddr());
            operationLog.setDeviceInfo(request.getHeader("User-Agent"));
            operationLog.setStatus("ERROR");
            operationLog.setCreatedTime(new Date());
            logService.insert(operationLog);

            log.error("更新考试失败", e);
            return Result.error("更新考试失败：" + e.getMessage());
        }
    }


    @ApiOperation("获取考试详情")
    @GetMapping("/{examId}")
    public Result getExamDetail(@PathVariable Integer examId, HttpServletRequest request) {
        User currentUser = TokenUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.error("用户未登录");
        }

        try {
            Exam exam = examService.getById(examId);
            if (exam == null) {
                return Result.error("考试不存在");
            }

            // 获取考试相关的试卷信息
            ExamPaper paper = examPaperService.getById(exam.getPaperId());

            Subject subject = subjectService.getById(paper.getSubjectId());
            subject.setCollege(collegeService.getById(subject.getCollegeId()));
            paper.setSubject(subject);

            paper.setTeacher(teacherService.getById(paper.getTeacherId()));
            paper.setTotalScore(BigDecimal.valueOf(100));
            paper.setStatusName(paper.getPaperStatus() == 0 ? "未发布" : "已发布");
            paper.setExamTypeName(paper.getExamType() == 0 ? "期末考试" : "普通考试");


            // 获取试卷题目
            List<Question> questions = examPaperService.getPaperQuestions(exam.getPaperId());
            for(Question question : questions) {
                List<QuestionOption> options = questionService.getOptions(question.getQuestionId());
                question.setOptions(options);
            }


            paper.setQuestions(questions);

            
            // 获取考试参与情况统计
            Map<String, Object> participation = examStudentService.getExamParticipation(examId);

            // 获取需要重考的学生数量
            List<ExamStudent> retakeStudents = examStudentService.getNeedRetakeStudents(examId);
            int retakeCount = retakeStudents != null ? retakeStudents.size() : 0;

            Map<String, Object> data = new HashMap<>();
            data.put("exam", exam);
            data.put("paper", paper);
            data.put("totalStudents", participation.get("totalStudents"));
            data.put("startedStudents", participation.get("startedStudents"));
            data.put("submittedStudents", participation.get("submittedStudents"));
            data.put("absentStudents", participation.get("absentStudents"));
            data.put("participationRate", participation.get("participationRate"));
            data.put("retakeCount", retakeCount);

            // 记录操作日志
            Log operationLog = new Log();
            operationLog.setUserId(currentUser.getUserId());
            operationLog.setActionType(7); // SELECT
            operationLog.setActionDescription("教师[" + currentUser.getUsername() + "]查看了考试详情，考试ID：" + examId);
            operationLog.setObjectType("EXAM");
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
            operationLog.setActionDescription("教师[" + currentUser.getUsername() + "]查看考试详情异常，考试ID：" + examId + "，错误：" + e.getMessage());
            operationLog.setObjectType("EXAM");
            operationLog.setIpAddress(request.getRemoteAddr());
            operationLog.setDeviceInfo(request.getHeader("User-Agent"));
            operationLog.setStatus("ERROR");
            operationLog.setCreatedTime(new Date());
            logService.insert(operationLog);

            log.error("获取考试详情失败", e);
            return Result.error("获取考试详情失败：" + e.getMessage());
        }
    }


    @ApiOperation("获取考试剩余时间")
    @GetMapping("/{examId}/remaining-time")
    public Result getExamRemainingTime(@PathVariable Integer examId, HttpServletRequest request) {
        User currentUser = TokenUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.error("用户未登录");
        }

        try {
            Exam exam = examService.getById(examId);
            if (exam == null) {
                return Result.error("考试不存在");
            }

            // 检查是否是考试的创建者
            Integer teacherId = getTeacherIdByUserId(currentUser.getUserId());
            if (teacherId == null) {
                return Result.error("未找到教师信息");
            }
            if (!exam.getTeacherId().equals(teacherId)) {
                return Result.error("无权查看该考试信息");
            }

            Map<String, Object> timeInfo = examService.getRemainingTime(examId);


            /**
             * 获取考试剩余时间信息
             *         - status: 考试状态（未开始/进行中/已结束）
             *         - remainingToStart: 距离开始的剩余分钟数（未开始状态）
             *         - remainingToEnd: 距离结束的剩余分钟数（进行中状态）
             *         - totalDuration: 考试总时长（进行中状态）
             *         - usedTime: 已用时间（进行中状态）
             *         - progress: 考试进度百分比（进行中状态）
             *         - endTime: 结束时间（已结束状态）
             */

            Map<String, Object> data = new HashMap<>();
            data.put("examName", exam.getExamName());
            data.put("status", timeInfo.get("status"));
            data.put("remainingToStart", timeInfo.get("remainingToStart"));
            data.put("remainingToEnd", timeInfo.get("remainingToEnd"));
            data.put("totalDuration",timeInfo.get("totalDuration"));
            data.put("usedTime", timeInfo.get("usedTime"));
            data.put("progress", timeInfo.get("progress"));
            data.put("endTime", timeInfo.get("endTime"));



            // 记录操作日志
            Log operationLog = new Log();
            operationLog.setUserId(currentUser.getUserId());
            operationLog.setActionType(7); // SELECT
            operationLog.setActionDescription("教师[" + currentUser.getUsername() + "]查看了考试剩余时间，考试ID：" + examId);
            operationLog.setObjectType("EXAM");
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
            operationLog.setActionDescription("教师[" + currentUser.getUsername() + "]查看考试剩余时间异常，考试ID：" + examId + "，错误：" + e.getMessage());
            operationLog.setObjectType("EXAM");
            operationLog.setIpAddress(request.getRemoteAddr());
            operationLog.setDeviceInfo(request.getHeader("User-Agent"));
            operationLog.setStatus("ERROR");
            operationLog.setCreatedTime(new Date());
            logService.insert(operationLog);

            log.error("获取考试剩余时间失败", e);
            return Result.error("获取考试剩余时间失败：" + e.getMessage());
        }
    }

    @ApiOperation("发布期末考试")
    @PostMapping("/publish-final")
    public Result publishFinalExam(@RequestParam Integer subjectId,
                                 @RequestParam List<Integer> classIds,
                                 @RequestParam Integer year,
                                 @RequestParam Integer semester,
                                 @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date examStartTime,
                                 @RequestParam Integer examDuration,
                                 HttpServletRequest request) {
        User currentUser = TokenUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.error("用户未登录");
        }

        try {
            Integer teacherId = getTeacherIdByUserId(currentUser.getUserId());
            if (teacherId == null) {
                return Result.error("未找到教师信息");
            }

            // 获取当前教师信息并验证权限
            Teacher teacher = teacherService.getByUserId(currentUser.getUserId());
            if (teacher == null) {
                return Result.error("未找到教师信息");
            }
            
            // 验证教师权限是否为0（管理员教师）
            if (teacher.getPermission() != 0) {
                return Result.error("权限不足，只有管理员教师可以发布期末考试");
            }

            // 将年份和学期转换为Date类型
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.YEAR, year);
            // 根据学期设置月份：上学期设为1月1日，下学期设为6月1日
            calendar.set(Calendar.MONTH, semester == 1 ? Calendar.JANUARY : Calendar.JUNE);
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            Date academicTerm = calendar.getTime();

            // 调用服务层方法发布期末考试，使用当前教师ID
            Exam exam = examService.publishFinalExam(
                teacherId,
                subjectId,
                classIds,
                academicTerm,
                examStartTime,
                examDuration
            );
            if (exam == null) {
                return Result.error("发布期末考试失败");
            }

            // 记录操作日志
            Log operationLog = new Log();
            operationLog.setUserId(currentUser.getUserId());
            operationLog.setActionType(0); // INSERT
            operationLog.setActionDescription("教师[" + currentUser.getUsername() + "]发布了期末考试：" + exam.getExamName());
            operationLog.setObjectType("EXAM");
            operationLog.setIpAddress(request.getRemoteAddr());
            operationLog.setDeviceInfo(request.getHeader("User-Agent"));
            operationLog.setStatus("SUCCESS");
            operationLog.setCreatedTime(new Date());
            logService.insert(operationLog);

            return Result.success(exam);
        } catch (Exception e) {
            // 记录异常日志
            Log operationLog = new Log();
            operationLog.setUserId(currentUser.getUserId());
            operationLog.setActionType(5); // EXCEPTION
            operationLog.setActionDescription("教师[" + currentUser.getUsername() + "]发布期末考试异常：" + e.getMessage());
            operationLog.setObjectType("EXAM");
            operationLog.setIpAddress(request.getRemoteAddr());
            operationLog.setDeviceInfo(request.getHeader("User-Agent"));
            operationLog.setStatus("ERROR");
            operationLog.setCreatedTime(new Date());
            logService.insert(operationLog);

            log.error("发布期末考试失败", e);
            return Result.error("发布期末考试失败：" + e.getMessage());
        }
    }


    @ApiOperation("查询题目平均得分")
    @GetMapping("/questions/{questionId}/average-score")
    public Result getQuestionAverageScore(@PathVariable Integer questionId,
                                        @RequestParam(required = false) Integer examId,
                                        HttpServletRequest request) {
        User currentUser = TokenUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.error("用户未登录");
        }

        try {
            // 如果提供了examId，验证考试是否存在且属于当前教师
            if (examId != null) {
                Exam exam = examService.getById(examId);
                if (exam == null) {
                    return Result.error("考试不存在");
                }
                Integer teacherId = getTeacherIdByUserId(currentUser.getUserId());
                if (teacherId == null) {
                    return Result.error("未找到教师信息");
                }
                if (!exam.getTeacherId().equals(teacherId)) {
                    return Result.error("无权查看该考试的统计信息");
                }
            }

            BigDecimal averageScore = studentQuestionScoreService.calculateAverageScore(questionId, examId);
            if (averageScore == null) {
                return Result.error("获取题目平均分失败");
            }
            ExamPaperQuestion examPaperQuestion = examPaperQuestionService.getByExamAndQuestionId(examId,questionId);
            BigDecimal fullScore = examPaperQuestion.getQuestionScore();
            Map<String, Object> result = new HashMap<>();
            result.put("questionId", questionId);
            result.put("averageScore", averageScore);
            result.put("fullScore", fullScore);
            if (examId != null) {
                result.put("examId", examId);
            }

            // 记录操作日志
            Log operationLog = new Log();
            operationLog.setUserId(currentUser.getUserId());
            operationLog.setActionType(7); // SELECT
            operationLog.setActionDescription("教师[" + currentUser.getUsername() + "]查看了题目平均分，题目ID：" + questionId + 
                (examId != null ? "，考试ID：" + examId : ""));
            operationLog.setObjectType("QUESTION_STATS");
            operationLog.setIpAddress(request.getRemoteAddr());
            operationLog.setDeviceInfo(request.getHeader("User-Agent"));
            operationLog.setStatus("SUCCESS");
            operationLog.setCreatedTime(new Date());
            logService.insert(operationLog);

            return Result.success(result);
        } catch (Exception e) {
            // 记录异常日志
            Log operationLog = new Log();
            operationLog.setUserId(currentUser.getUserId());
            operationLog.setActionType(5); // EXCEPTION
            operationLog.setActionDescription("教师[" + currentUser.getUsername() + "]查看题目平均分异常，题目ID：" + questionId + 
                (examId != null ? "，考试ID：" + examId : "") + "，错误：" + e.getMessage());
            operationLog.setObjectType("QUESTION_STATS");
            operationLog.setIpAddress(request.getRemoteAddr());
            operationLog.setDeviceInfo(request.getHeader("User-Agent"));
            operationLog.setStatus("ERROR");
            operationLog.setCreatedTime(new Date());
            logService.insert(operationLog);

            log.error("获取题目平均分失败", e);
            return Result.error("获取题目平均分失败：" + e.getMessage());
        }
    }

    @ApiOperation("获取考试的考生列表")
    @GetMapping("/{examId}/students")
    public Result getExamStudents(@PathVariable Integer examId,
                                 HttpServletRequest request) {
        User currentUser = TokenUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.error("用户未登录");
        }
        try {
            // 验证考试是否存在且属于当前教师
            Exam exam = examService.getById(examId);
            if (exam == null) {
                return Result.error("考试不存在");
            }
            Integer teacherId = getTeacherIdByUserId(currentUser.getUserId());
            if (teacherId == null) {
                return Result.error("未找到教师信息");
            }
            if (!exam.getTeacherId().equals(teacherId)) {
                return Result.error("无权查看该考试的考生信息");
            }

            List<ExamStudent> examStudents = examStudentService.getByExamId(examId);
            for (ExamStudent student : examStudents) {
                student.setExam(exam);
                student.setStudent(studentService.getById(student.getStudentId()));
            }

            // 记录操作日志
            Log operationLog = new Log();
            operationLog.setUserId(currentUser.getUserId());
            operationLog.setActionType(7); // SELECT
            operationLog.setActionDescription("教师[" + currentUser.getUsername() + "]查看了考试考生列表，考试ID：" + examId);
            operationLog.setObjectType("EXAM_STUDENT");
            operationLog.setIpAddress(request.getRemoteAddr());
            operationLog.setDeviceInfo(request.getHeader("User-Agent"));
            operationLog.setStatus("SUCCESS");
            operationLog.setCreatedTime(new Date());
            logService.insert(operationLog);

            return Result.success(examStudents);
        } catch (Exception e) {
            // 记录异常日志
            Log operationLog = new Log();
            operationLog.setUserId(currentUser.getUserId());
            operationLog.setActionType(5); // EXCEPTION
            operationLog.setActionDescription("教师[" + currentUser.getUsername() + "]查看考试考生列表异常，考试ID：" + examId + "，错误：" + e.getMessage());
            operationLog.setObjectType("EXAM_STUDENT");
            operationLog.setIpAddress(request.getRemoteAddr());
            operationLog.setDeviceInfo(request.getHeader("User-Agent"));
            operationLog.setStatus("ERROR");
            operationLog.setCreatedTime(new Date());
            logService.insert(operationLog);

            log.error("获取考试考生列表失败", e);
            return Result.error("获取考试考生列表失败：" + e.getMessage());
        }
    }


    @ApiOperation("获取指定考试的班级列表")
    @GetMapping("/{examId}/classes")
    public Result getClassesList(@PathVariable Integer examId,
                                 @RequestParam(defaultValue = "1") Integer pageNum,
                                 @RequestParam(defaultValue = "10") Integer pageSize,
                                 HttpServletRequest request) {
        User currentUser = TokenUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.error("用户未登录");
        }

        try {
            // 验证考试是否存在
            Exam exam = examService.getById(examId);
            if (exam == null) {
                return Result.error("考试不存在");
            }


            // 获取考试的班级列表
            List<com.exam.entity.Class> classes = examClassService.getClassesByExamId(examId);
            if(classes == null || classes.isEmpty()){
                return Result.error("该考试暂无班级,可能是重考考试");
            }
            for(com.exam.entity.Class clazz : classes) {
                clazz.setTeacher(teacherService.getById(clazz.getTeacherId()));
                clazz.setSubject(subjectService.getById(clazz.getSubjectId()));
            }


            // 手动进行分页处理
            int start = (pageNum - 1) * pageSize;
            int end = Math.min(start + pageSize, classes.size());
            List<com.exam.entity.Class> pagedClasses = start < classes.size() ? classes.subList(start, end) : new ArrayList<>();

            Map<String, Object> data = new HashMap<>();
            data.put("list", pagedClasses);
            data.put("total", (long) classes.size());

            // 记录操作日志
            Log operationLog = new Log();
            operationLog.setUserId(currentUser.getUserId());
            operationLog.setActionType(7); // SELECT
            operationLog.setActionDescription("教师[" + currentUser.getUsername() + "]查询了考试[" + examId + "]的班级列表");
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
            operationLog.setActionDescription("教师[" + currentUser.getUsername() + "]查询考试的班级列表异常：" + e.getMessage());
            operationLog.setObjectType("SCORE");
            operationLog.setIpAddress(request.getRemoteAddr());
            operationLog.setDeviceInfo(request.getHeader("User-Agent"));
            operationLog.setStatus("ERROR");
            operationLog.setCreatedTime(new Date());
            logService.insert(operationLog);

            log.error("获取考试班级列表失败", e);
            return Result.error("获取考试班级列表失败：" + e.getMessage());
        }
    }


    @ApiOperation("标记考生违纪")
    @PostMapping("/{examId}/students/{studentId}/disciplinary")
    public Result markStudentDisciplinary(@PathVariable Integer examId,
                                        @PathVariable Integer studentId,
                                        @RequestParam String comment,
                                        HttpServletRequest request) {
        User currentUser = TokenUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.error("用户未登录");
        }

        try {

            Exam studentExam = examService.getById(examId);
            ExamStudent studentExamStudent = examStudentService.getByExamIdAndStudentId(examId, studentId);

            // 验证考试是否存在且属于当前教师
            Exam exam = examService.getById(examId);
            if (exam == null) {
                return Result.error("考试不存在");
            }
            Integer teacherId = getTeacherIdByUserId(currentUser.getUserId());
            if (teacherId == null) {
                return Result.error("未找到教师信息");
            }
            if (!exam.getTeacherId().equals(teacherId)) {
                return Result.error("无权操作该考试");
            }
            if(studentExamStudent == null){
                return Result.error("考生不存在");
            }
            if(studentExam == null){
                return Result.error("考试不存在");
            }
            if(studentExamStudent.getStudentStartTime() == null){
                return Result.error("考生未开始考试");
            }

            int result = examStudentService.markDisciplinary(examId, studentId, comment);

            // 强制提交考试
            examAutoProcessTask.forceSubmitExam(studentExam, studentExamStudent);

            //考试违纪，设置分数为0
            studentScoreService.getByExamAndStudent(
                    exam.getExamId(), studentId).setScore(BigDecimal.ZERO);

            //标记需要重考
            examStudentService.markRetakeNeeded(examId, studentId);



            if (result <= 0) {
                return Result.error("标记违纪失败");
            }

            // 记录操作日志
            Log operationLog = new Log();
            operationLog.setUserId(currentUser.getUserId());
            operationLog.setActionType(1); // UPDATE
            operationLog.setActionDescription("教师[" + currentUser.getUsername() + "]标记了考生违纪，考试ID：" + examId + "，学生ID：" + studentId);
            operationLog.setObjectType("EXAM_STUDENT");
            operationLog.setIpAddress(request.getRemoteAddr());
            operationLog.setDeviceInfo(request.getHeader("User-Agent"));
            operationLog.setStatus("SUCCESS");
            operationLog.setCreatedTime(new Date());
            logService.insert(operationLog);

            return Result.success();
        } catch (Exception e) {
            // 记录异常日志
            Log operationLog = new Log();
            operationLog.setUserId(currentUser.getUserId());
            operationLog.setActionType(5); // EXCEPTION
            operationLog.setActionDescription("教师[" + currentUser.getUsername() + "]标记考生违纪异常，考试ID：" + examId + 
                "，学生ID：" + studentId + "，错误：" + e.getMessage());
            operationLog.setObjectType("EXAM_STUDENT");
            operationLog.setIpAddress(request.getRemoteAddr());
            operationLog.setDeviceInfo(request.getHeader("User-Agent"));
            operationLog.setStatus("ERROR");
            operationLog.setCreatedTime(new Date());
            logService.insert(operationLog);

            log.error("标记考生违纪失败", e);
            return Result.error("标记考生违纪失败：" + e.getMessage());
        }
    }

    @ApiOperation("查询指定考试的重考考生列表")
    @GetMapping("/{examId}/retake-students")
    public Result getRetakeStudents(@PathVariable Integer examId,
                                   HttpServletRequest request) {
        User currentUser = TokenUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.error("用户未登录");
        }

        try {
            // 验证考试是否存在且属于当前教师
            Exam exam = examService.getById(examId);
            if (exam == null) {
                return Result.error("考试不存在");
            }
            Integer teacherId = getTeacherIdByUserId(currentUser.getUserId());
            if (teacherId == null) {
                return Result.error("未找到教师信息");
            }
            if (!exam.getTeacherId().equals(teacherId)) {
                return Result.error("无权查看该考试信息");
            }

            // 获取需要重考的学生列表
            List<ExamStudent> retakeStudents = examStudentService.getNeedRetakeStudents(examId);
            if (retakeStudents == null) {
                retakeStudents = new ArrayList<>();
            }

            for(ExamStudent student : retakeStudents) {
                student.setStudent(studentService.getById(student.getStudentId()));
            }

            // 统计信息
            Map<String, Object> statistics = new HashMap<>();
            statistics.put("totalRetakeStudents", retakeStudents.size());
            
            // 获取考试的总生数，用于计算重考率
            int totalStudents = examStudentService.countTotalStudents(examId);
            double retakeRate = totalStudents > 0 ? 
                (double) retakeStudents.size() / totalStudents * 100 : 0;
            statistics.put("retakeRate", String.format("%.2f%%", retakeRate));

            Map<String, Object> data = new HashMap<>();
            data.put("examInfo", exam);
            data.put("retakeStudents", retakeStudents);
            data.put("statistics", statistics);

            // 记录操作日志
            Log operationLog = new Log();
            operationLog.setUserId(currentUser.getUserId());
            operationLog.setActionType(7); // SELECT
            operationLog.setActionDescription("教师[" + currentUser.getUsername() + "]查询了考试重考考生列表，考试ID：" + examId);
            operationLog.setObjectType("RETAKE_STUDENTS");
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
            operationLog.setActionDescription("教师[" + currentUser.getUsername() + "]查询考试重考考生列表异常，考试ID：" + examId + "，错误：" + e.getMessage());
            operationLog.setObjectType("RETAKE_STUDENTS");
            operationLog.setIpAddress(request.getRemoteAddr());
            operationLog.setDeviceInfo(request.getHeader("User-Agent"));
            operationLog.setStatus("ERROR");
            operationLog.setCreatedTime(new Date());
            logService.insert(operationLog);

            log.error("查询考试重考考生列表失败", e);
            return Result.error("查询考试重考考生列表失败：" + e.getMessage());
        }
    }

    @ApiOperation("发布重考考试")
    @PostMapping("/publish-retake")
    public Result publishRetakeExam(@RequestParam Integer subjectId,
                                  @RequestParam List<Integer> studentIds,
                                  @RequestParam Integer paperId,
                                  @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date examStartTime,
                                  @RequestParam Integer examDuration,
                                  HttpServletRequest request) {
        User currentUser = TokenUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.error("用户未登录");
        }

        try {
            // 获取当前教师信息并验证权限
            Teacher teacher = teacherService.getByUserId(currentUser.getUserId());
            if (teacher == null) {
                return Result.error("未找到教师信息");
            }
            
            // 验证教师权限（0为管理员教师，1为普通教师）
            if (teacher.getPermission() != 0 && teacher.getPermission() != 1) {
                return Result.error("权限不足，只有管理员教师或普通教师可以发布重考考试");
            }

            // 检查试卷是否存在且已发布
            ExamPaper paper = examPaperService.getById(paperId);
            if (paper == null) {
                return Result.error("试卷不存在");
            }
            if (paper.getPaperStatus() != 0) {
                return Result.error("试卷已发布，不能创建考试");
            }

            // 调用服务层方法发布重考考试，使用当前教师ID
            Exam retakeExam = examService.publishRetakeExam(
                teacher.getTeacherId(),
                subjectId,
                studentIds,
                paperId,
                examStartTime,
                examDuration
            );

            // 记录操作日志
            Log operationLog = new Log();
            operationLog.setUserId(currentUser.getUserId());
            operationLog.setActionType(0); // INSERT
            operationLog.setActionDescription("教师[" + currentUser.getUsername() + "]发布重考考试，考试名称：" + retakeExam.getExamName());
            operationLog.setObjectType("EXAM");
            operationLog.setIpAddress(request.getRemoteAddr());
            operationLog.setDeviceInfo(request.getHeader("User-Agent"));
            operationLog.setStatus("SUCCESS");
            operationLog.setCreatedTime(new Date());
            logService.insert(operationLog);

            return Result.success(retakeExam);
        } catch (Exception e) {
            // 记录异常日志
            Log operationLog = new Log();
            operationLog.setUserId(currentUser.getUserId());
            operationLog.setActionType(5); // EXCEPTION
            operationLog.setActionDescription("教师[" + currentUser.getUsername() + "]发布重考考试异常：" + e.getMessage());
            operationLog.setObjectType("EXAM");
            operationLog.setIpAddress(request.getRemoteAddr());
            operationLog.setDeviceInfo(request.getHeader("User-Agent"));
            operationLog.setStatus("ERROR");
            operationLog.setCreatedTime(new Date());
            logService.insert(operationLog);

            log.error("发布重考考试失败", e);
            return Result.error("发布重考考试失败：" + e.getMessage());
        }
    }



} 
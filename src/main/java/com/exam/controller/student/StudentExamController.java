package com.exam.controller.student;

import com.exam.entity.*;
import com.exam.entity.Class;
import com.exam.service.*;
import com.exam.service.impl.StudentClassServiceImpl;
import com.exam.utils.TokenUtils;
import com.exam.common.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/student/exam")
@Api(tags = "学生考试功能接口")
public class StudentExamController {

    @Autowired
    private ExamService examService;

    @Autowired
    private ExamPaperService examPaperService;

    @Autowired
    private StudentQuestionScoreService studentQuestionScoreService;

    @Autowired
    private LogService logService;

    @Autowired
    private StudentService studentService;

    @Autowired
    private ExamStudentService examStudentService;
    @Autowired
    private StudentClassService studentClassService;
    @Autowired
    private ClassService classService;
    @Autowired
    private ExamPaperQuestionService examPaperQuestionService;
    @Autowired
    private QuestionOptionService questionOptionService;
    @Autowired
    private QuestionService questionService;

    @Autowired
    private AutomaticGradingService automaticGradingService;
    @Autowired
    private StudentScoreService studentScoreService;

    @ApiOperation("查看考试信息")
    @GetMapping("/list")
    public Result getExams(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            HttpServletRequest request) {
        User currentUser = TokenUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.error("用户未登录");
        }

        try {
            // 获取当前学生信息
            Student student = studentService.getByUserId(currentUser.getUserId());
            if (student == null) {
                return Result.error("未找到学生信息");
            }

            // 获取学生的考试列表

            List<ExamStudent> examStudents = examStudentService.getByStudentId(student.getStudentId());

            List<Exam> classExams = new ArrayList<>();

            for(ExamStudent examStudent:examStudents){
                Exam exam = examService.getById(examStudent.getExamId());
                if(exam != null){
                    classExams.add(exam);
                }
            }


            // 按开始时间降序排序
            List<Exam> availableExams = classExams.stream()
                    .sorted((e1, e2) -> e2.getExamStartTime().compareTo(e1.getExamStartTime()))
                    .collect(Collectors.toList());

            // 分页处理
            int start = (pageNum - 1) * pageSize;
            int end = Math.min(start + pageSize, availableExams.size());
            List<Exam> pagedExams = availableExams.subList(start, end);

            // 获取每个考试的详细信息
            List<Map<String, Object>> examInfoList = new ArrayList<>();
            for (Exam exam : pagedExams) {
                Map<String, Object> examInfo = new HashMap<>();
                examInfo.put("exam", exam);
                
                // 获取试卷信息
                ExamPaper paper = examPaperService.getById(exam.getPaperId());
                if (paper != null) {
                    examInfo.put("paperName", paper.getPaperName());
                    examInfo.put("totalScore", paper.getTotalScore());
                    examInfo.put("examDuration", exam.getExamDuration());
                }

                // 获取考试状态和剩余时间信息
                Map<String, Object> timeInfo = examService.getRemainingTime(exam.getExamId());
                examInfo.put("status", timeInfo.get("status"));
                examInfo.put("remainingTime", timeInfo.get("remainingToStart"));

                // 获取学生在此考试中的状态
                ExamStudent examStudent = examStudentService.getByExamIdAndStudentId(exam.getExamId(), student.getStudentId());
                if (examStudent != null) {
                    examInfo.put("started", examStudent.getStudentStartTime() != null);
                    examInfo.put("submitted", examStudent.getStudentSubmitTime() != null);
                    examInfo.put("absent", examStudent.getAbsent());
                    examInfo.put("disciplinary", examStudent.getDisciplinary());
                }

                examInfoList.add(examInfo);
            }

            Map<String, Object> data = new HashMap<>();
            data.put("list", examInfoList);
            data.put("total", (long) availableExams.size());

            // 记录操作日志
            Log operationLog = new Log();
            operationLog.setUserId(currentUser.getUserId());
            operationLog.setActionType(7); // SELECT
            operationLog.setActionDescription("学生[" + currentUser.getUsername() + "]查看了考试列表");
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
            operationLog.setActionDescription("学生[" + currentUser.getUsername() + "]查看考试列表异常：" + e.getMessage());
            operationLog.setObjectType("EXAM");
            operationLog.setIpAddress(request.getRemoteAddr());
            operationLog.setDeviceInfo(request.getHeader("User-Agent"));
            operationLog.setStatus("ERROR");
            operationLog.setCreatedTime(new Date());
            logService.insert(operationLog);

            log.error("获取考试信息失败", e);
            return Result.error("获取考试信息失败：" + e.getMessage());
        }
    }

    @ApiOperation("参加考试")
    @PostMapping("/{examId}/start")
    public Result startExam(
            @PathVariable Integer examId,
            HttpServletRequest request) {
        User currentUser = TokenUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.error("用户未登录");
        }

        try {
            // 获取当前学生信息
            Student student = studentService.getByUserId(currentUser.getUserId());
            if (student == null) {
                return Result.error("未找到学生信息");
            }

            // 验证考试是否存在
            Exam exam = examService.getById(examId);
            if (exam == null) {
                return Result.error("考试不存在");
            }

            // 验证考试时间
            Date now = new Date();
            if (now.before(exam.getExamStartTime())) {
                return Result.error("考试还未开始");
            }
            if (now.after(exam.getExamEndTime())) {
                return Result.error("考试已结束");
            }

            // 验证学生是否有权限参加考试
            ExamStudent examStudent = examStudentService.getByExamIdAndStudentId(examId, student.getStudentId());
            if (examStudent == null) {
                return Result.error("您没有参加此考试的权限");
            }
            //开始考试后未提交前允许多次进入考试界面

            // 检查是否已提交考试
            if (examStudent.getStudentSubmitTime() != null) {
                return Result.error("您已经提交考试，不能重复参加");
            }

            // 检查是否被标记为缺考
            if (examStudent.getAbsent()) {
                return Result.error("您已被标记为缺考，不能参加考试");
            }

            // 检查是否被标记为违纪
            if (examStudent.getDisciplinary()) {
                return Result.error("您已被标记为违纪，不能参加考试");
            }

            // 获取试卷信息
            ExamPaper paper = examPaperService.getById(exam.getPaperId());
            if (paper == null) {
                return Result.error("试卷信息不存在");
            }
            //获取试卷题目信息
            List<Map<String, Object>> questionList=examPaperService.getPaperQuestionsWithScore(paper.getPaperId());
            List<Question> questions = new ArrayList<>();
            for(Map<String, Object> question:questionList){
                Question question1 = (Question) question.get("question");
                BigDecimal score = (BigDecimal) question.get("score");
                question1.setScore(score);
                question1.setOptions(questionOptionService.getByQuestionId(question1.getQuestionId()));
                questions.add(question1);
            }

            List<ExamPaperQuestion> examPaperQuestions = examPaperQuestionService.getByPaperId(paper.getPaperId());

            // 创建一个Map用于存储题目顺序
            Map<Integer, Integer> questionOrderMap = new HashMap<>();
            for (int i = 0; i < examPaperQuestions.size(); i++) {
                questionOrderMap.put(examPaperQuestions.get(i).getQuestionId(), examPaperQuestions.get(i).getQuestionOrder());
            }

            // 根据试卷题目顺序对questions进行排序
            questions.sort((q1, q2) -> {
                Integer order1 = questionOrderMap.get(q1.getQuestionId());
                Integer order2 = questionOrderMap.get(q2.getQuestionId());
                
                // 如果找不到顺序，将题目放到最后
                if (order1 == null) return 1;
                if (order2 == null) return -1;
                
                return order1.compareTo(order2);
            });

            paper.setQuestions(questions);

            // 更新考试开始时间
            if (examStudent.getStudentStartTime() == null){
                examStudentService.recordStartExam(examId, student.getStudentId());
            }

            // 获取或创建学生成绩记录
            StudentScore studentScore = studentScoreService.getByExamAndStudent(examId, student.getStudentId());
            if (studentScore == null) {
                studentScore = new StudentScore();
                studentScore.setStudentId(student.getStudentId());
                studentScore.setExamId(examId);
                studentScore.setScore(BigDecimal.ZERO);
                studentScore.setUploadTime(new Date());
                studentScoreService.insert(studentScore);
            }


            // 准备返回数据
            Map<String, Object> data = new HashMap<>();
            data.put("exam", exam);
            data.put("paper", paper);
            data.put("remainingTime", exam.getExamDuration()); // 剩余时间（分钟）
            data.put("startTime", examStudent.getStudentStartTime());

            // 记录操作日志
            Log operationLog = new Log();
            operationLog.setUserId(currentUser.getUserId());
            operationLog.setActionType(1); // UPDATE
            operationLog.setActionDescription("学生[" + currentUser.getUsername() + "]开始参加考试，考试ID：" + examId);
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
            operationLog.setActionDescription("学生[" + currentUser.getUsername() + "]开始考试异常，考试ID：" + examId + "，错误：" + e.getMessage());
            operationLog.setObjectType("EXAM");
            operationLog.setIpAddress(request.getRemoteAddr());
            operationLog.setDeviceInfo(request.getHeader("User-Agent"));
            operationLog.setStatus("ERROR");
            operationLog.setCreatedTime(new Date());
            logService.insert(operationLog);

            log.error("开始考试失败", e);
            return Result.error("开始考试失败：" + e.getMessage());
        }
    }

    @ApiOperation("提交客观题答案")
    @PostMapping("/{examId}/objective-answer")
    public Result submitObjectiveAnswer(
            @PathVariable Integer examId,
            @RequestBody Map<String, Object> answerForm,
            HttpServletRequest request) {
        User currentUser = TokenUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.error("用户未登录");
        }

        try {
            // 获取当前学生信息
            Student student = studentService.getByUserId(currentUser.getUserId());
            if (student == null) {
                return Result.error("未找到学生信息");
            }

            // 验证考试是否存在
            Exam exam = examService.getById(examId);
            if (exam == null) {
                return Result.error("考试不存在");
            }

            // 验证考试时间
            Date now = new Date();
            if (now.before(exam.getExamStartTime())) {
                return Result.error("考试还未开始");
            }
            if (now.after(exam.getExamEndTime())) {
                return Result.error("考试已结束");
            }

            // 验证学生是否有权限参加考试
            ExamStudent examStudent = examStudentService.getByExamIdAndStudentId(examId, student.getStudentId());
            if (examStudent == null) {
                return Result.error("您没有参加此考试的权限");
            }

            // 检查是否已提交考试
            if (examStudent.getStudentSubmitTime() != null) {
                return Result.error("您已经提交考试，不能再提交答案");
            }

            // 检查是否被标记为缺考
            if (examStudent.getAbsent()) {
                return Result.error("您已被标记为缺考，不能提交答案");
            }

            // 检查是否被标记为违纪
            if (examStudent.getDisciplinary()) {
                return Result.error("您已被标记为违纪，不能提交答案");
            }

            // 获取题目ID和答案
            Integer questionId = (Integer) answerForm.get("questionId");
            String answer = (String) answerForm.get("answer");

            if (questionId == null) {
                return Result.error("题目ID不能为空");
            }

            // 验证题目是否属于该考试
            ExamPaper paper = examPaperService.getById(exam.getPaperId());
            if (paper == null) {
                return Result.error("试卷不存在");
            }

            List<Question> questions = examPaperService.getPaperQuestions(paper.getPaperId());
            boolean questionExists = questions.stream()
                    .anyMatch(q -> q.getQuestionId().equals(questionId));
            if (!questionExists) {
                return Result.error("题目不属于该考试");
            }

            // 获取或创建学生成绩记录
            StudentScore studentScore = studentScoreService.getByExamAndStudent(examId, student.getStudentId());
            if (studentScore == null) {
                studentScore = new StudentScore();
                studentScore.setStudentId(student.getStudentId());
                studentScore.setExamId(examId);
                studentScore.setScore(BigDecimal.ZERO);
                studentScore.setUploadTime(new Date());
                studentScoreService.insert(studentScore);
            }

            // 创建或更新题目得分记录
            StudentQuestionScore questionScore = new StudentQuestionScore();
            questionScore.setStudentId(student.getStudentId());
            questionScore.setExamId(examId);
            questionScore.setQuestionId(questionId);
            questionScore.setScoreId(studentScore.getScoreId());
            questionScore.setAnswer(answer);
            questionScore.setScore(BigDecimal.ZERO); // 初始分数为0
            questionScore.setStatus(0); // 未批改状态

            // 检查是否已存在答案记录
            StudentQuestionScore existingScore = studentQuestionScoreService.getByExamAndStudent(examId, student.getStudentId())
                    .stream()
                    .filter(qs -> qs.getQuestionId().equals(questionId))
                    .findFirst()
                    .orElse(null);

            if (existingScore != null) {
                questionScore.setRecordId(existingScore.getRecordId());
                studentQuestionScoreService.updateById(questionScore);
            } else {
                studentQuestionScoreService.insert(questionScore);
            }

            Question question = questionService.getById(questionId);
            List<QuestionOption> questionOptions = questionOptionService.getByQuestionId(questionId);

            // 检查答案内容是否是正确的选择题选项ID
            if (question.getType() != 3 && question.getType() != 4) { // 不是主观题
                if (answer == null || answer.trim().isEmpty()) {
                    return Result.error("答案不能为空");
                }

                // 检查答案格式
                try {
                    if (question.getType() == 1) { // 多选题
                        // 多选题答案格式：optionId1,optionId2,...
                        String[] selectedOptionIds = answer.split(",");
                        Set<Integer> selectedIds = Arrays.stream(selectedOptionIds)
                                .map(String::trim)
                                .map(Integer::parseInt)
                                .collect(Collectors.toSet());

                        // 验证所有选项ID是否存在于题目选项中
                        boolean allValid = selectedIds.stream()
                                .allMatch(id -> questionOptions.stream()
                                        .anyMatch(opt -> opt.getOptionId().equals(id)));

                        if (!allValid) {
                            return Result.error("存在无效的选项ID");
                        }
                    } else { // 单选题或判断题
                        // 单选题和判断题答案格式：optionId
                        Integer selectedId = Integer.parseInt(answer.trim());
                        boolean isValid = questionOptions.stream()
                                .anyMatch(opt -> opt.getOptionId().equals(selectedId));

                        if (!isValid) {
                            return Result.error("无效的选项ID");
                        }
                    }
                } catch (NumberFormatException e) {
                    return Result.error("答案格式不正确");
                }
            }

            if(question.getType()==3||question.getType()==4){
                // 手动评分
            }else {
                // 自动评分
                automaticGradingService.gradeQuestion(questionScore);
            }

            // 记录操作日志
            Log operationLog = new Log();
            operationLog.setUserId(currentUser.getUserId());
            operationLog.setActionType(0); // INSERT
            operationLog.setActionDescription("学生[" + currentUser.getUsername() + "]提交客观题答案，考试ID：" + examId + "，题目ID：" + questionId);
            operationLog.setObjectType("QUESTION_ANSWER");
            operationLog.setIpAddress(request.getRemoteAddr());
            operationLog.setDeviceInfo(request.getHeader("User-Agent"));
            operationLog.setStatus("SUCCESS");
            operationLog.setCreatedTime(new Date());
            logService.insert(operationLog);

            return Result.success("答案提交成功");
        } catch (Exception e) {
            // 记录异常日志
            Log operationLog = new Log();
            operationLog.setUserId(currentUser.getUserId());
            operationLog.setActionType(5); // EXCEPTION
            operationLog.setActionDescription("学生[" + currentUser.getUsername() + "]提交客观题答案失败：" + e.getMessage());
            operationLog.setObjectType("QUESTION_ANSWER");
            operationLog.setIpAddress(request.getRemoteAddr());
            operationLog.setDeviceInfo(request.getHeader("User-Agent"));
            operationLog.setStatus("ERROR");
            operationLog.setCreatedTime(new Date());
            logService.insert(operationLog);

            log.error("提交客观题答案失败", e);
            return Result.error("提交答案失败：" + e.getMessage());
        }
    }

    @ApiOperation("提交主观题答案")
    @PostMapping("/{examId}/subjective-answer")
    public Result submitSubjectiveAnswer(
            @PathVariable Integer examId,
            @RequestBody Map<String, Object> answerForm,
            HttpServletRequest request) {
        User currentUser = TokenUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.error("用户未登录");
        }

        try {
            // 获取当前学生信息
            Student student = studentService.getByUserId(currentUser.getUserId());
            if (student == null) {
                return Result.error("未找到学生信息");
            }

            // 验证考试是否存在
            Exam exam = examService.getById(examId);
            if (exam == null) {
                return Result.error("考试不存在");
            }

            // 验证考试时间
            Date now = new Date();
            if (now.before(exam.getExamStartTime())) {
                return Result.error("考试还未开始");
            }
            if (now.after(exam.getExamEndTime())) {
                return Result.error("考试已结束");
            }

            // 验证学生是否有权限参加考试
            ExamStudent examStudent = examStudentService.getByExamIdAndStudentId(examId, student.getStudentId());
            if (examStudent == null) {
                return Result.error("您没有参加此考试的权限");
            }

            // 检查是否已提交考试
            if (examStudent.getStudentSubmitTime() != null) {
                return Result.error("您已经提交考试，不能再提交答案");
            }

            // 检查是否被标记为缺考
            if (examStudent.getAbsent()) {
                return Result.error("您已被标记为缺考，不能提交答案");
            }

            // 检查是否被标记为违纪
            if (examStudent.getDisciplinary()) {
                return Result.error("您已被标记为违纪，不能提交答案");
            }

            // 获取题目ID和答案
            Integer questionId = (Integer) answerForm.get("questionId");
            String answer = (String) answerForm.get("answer");

            if (questionId == null) {
                return Result.error("题目ID不能为空");
            }

            // 验证题目是否属于该考试
            ExamPaper paper = examPaperService.getById(exam.getPaperId());
            if (paper == null) {
                return Result.error("试卷不存在");
            }

            List<Question> questions = examPaperService.getPaperQuestions(paper.getPaperId());
            boolean questionExists = questions.stream()
                    .anyMatch(q -> q.getQuestionId().equals(questionId));
            if (!questionExists) {
                return Result.error("题目不属于该考试");
            }

            // 验证题目类型是否为主观题
            Question question = questionService.getById(questionId);
            if (question == null) {
                return Result.error("题目不存在");
            }
            if (question.getType() != 3 && question.getType() != 4) { // 3:填空题 4:简答题
                return Result.error("该题目不是主观题");
            }

            // 获取或创建学生成绩记录
            StudentScore studentScore = studentScoreService.getByExamAndStudent(examId, student.getStudentId());
            if (studentScore == null) {
                studentScore = new StudentScore();
                studentScore.setStudentId(student.getStudentId());
                studentScore.setExamId(examId);
                studentScore.setScore(BigDecimal.ZERO);
                studentScore.setUploadTime(new Date());
                studentScoreService.insert(studentScore);
            }

            // 创建或更新题目得分记录
            StudentQuestionScore questionScore = new StudentQuestionScore();
            questionScore.setStudentId(student.getStudentId());
            questionScore.setExamId(examId);
            questionScore.setQuestionId(questionId);
            questionScore.setScoreId(studentScore.getScoreId());
            questionScore.setAnswer(answer);
            questionScore.setScore(BigDecimal.ZERO); // 初始分数为0
            questionScore.setStatus(0); // 未批改状态

            // 检查是否已存在答案记录
            StudentQuestionScore existingScore = studentQuestionScoreService.getByExamAndStudent(examId, student.getStudentId())
                    .stream()
                    .filter(qs -> qs.getQuestionId().equals(questionId))
                    .findFirst()
                    .orElse(null);

            if (existingScore != null) {
                questionScore.setRecordId(existingScore.getRecordId());
                studentQuestionScoreService.updateById(questionScore);
            } else {
                studentQuestionScoreService.insert(questionScore);
            }

            // 记录操作日志
            Log operationLog = new Log();
            operationLog.setUserId(currentUser.getUserId());
            operationLog.setActionType(0); // INSERT
            operationLog.setActionDescription("学生[" + currentUser.getUsername() + "]提交主观题答案，考试ID：" + examId + "，题目ID：" + questionId);
            operationLog.setObjectType("QUESTION_ANSWER");
            operationLog.setIpAddress(request.getRemoteAddr());
            operationLog.setDeviceInfo(request.getHeader("User-Agent"));
            operationLog.setStatus("SUCCESS");
            operationLog.setCreatedTime(new Date());
            logService.insert(operationLog);

            return Result.success("答案提交成功");
        } catch (Exception e) {
            // 记录异常日志
            Log operationLog = new Log();
            operationLog.setUserId(currentUser.getUserId());
            operationLog.setActionType(5); // EXCEPTION
            operationLog.setActionDescription("学生[" + currentUser.getUsername() + "]提交主观题答案失败：" + e.getMessage());
            operationLog.setObjectType("QUESTION_ANSWER");
            operationLog.setIpAddress(request.getRemoteAddr());
            operationLog.setDeviceInfo(request.getHeader("User-Agent"));
            operationLog.setStatus("ERROR");
            operationLog.setCreatedTime(new Date());
            logService.insert(operationLog);

            log.error("提交主观题答案失败", e);
            return Result.error("提交答案失败：" + e.getMessage());
        }
    }

    @ApiOperation("提交试卷")
    @PostMapping("/{examId}/submit")
    public Result submitExam(
            @PathVariable Integer examId,
            HttpServletRequest request) {
        User currentUser = TokenUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.error("用户未登录");
        }

        try {
            // 获取当前学生信息
            Student student = studentService.getByUserId(currentUser.getUserId());
            if (student == null) {
                return Result.error("未找到学生信息");
            }

            // 验证考试是否存在
            Exam exam = examService.getById(examId);
            if (exam == null) {
                return Result.error("考试不存在");
            }

            // 验证考试时间
            Date now = new Date();
            if (now.before(exam.getExamStartTime())) {
                return Result.error("考试还未开始");
            }
            if (now.after(exam.getExamEndTime())) {
                return Result.error("考试已结束");
            }

            // 验证学生是否有权限参加考试
            ExamStudent examStudent = examStudentService.getByExamIdAndStudentId(examId, student.getStudentId());
            if (examStudent == null) {
                return Result.error("您没有参加此考试的权限");
            }

            // 检查是否已提交考试
            if (examStudent.getStudentSubmitTime() != null) {
                return Result.error("您已经提交过试卷");
            }

            // 检查是否被标记为缺考
            if (examStudent.getAbsent()) {
                return Result.error("您已被标记为缺考");
            }

            // 检查是否被标记为违纪
            if (examStudent.getDisciplinary()) {
                return Result.error("您已被标记为违纪");
            }

            // 获取试卷信息
            ExamPaper paper = examPaperService.getById(exam.getPaperId());
            if (paper == null) {
                return Result.error("试卷不存在");
            }

            // 获取试卷所有题目
            List<Question> questions = examPaperService.getPaperQuestions(paper.getPaperId());
            if (questions.isEmpty()) {
                return Result.error("试卷题目为空");
            }

            // 获取学生已答题目记录
            List<StudentQuestionScore> answeredQuestions = studentQuestionScoreService.getByExamAndStudent(examId, student.getStudentId());
            
            // 检查是否所有题目都已作答
            Set<Integer> answeredQuestionIds = answeredQuestions.stream()
                    .map(StudentQuestionScore::getQuestionId)
                    .collect(Collectors.toSet());

            List<Question> unansweredQuestions = questions.stream()
                    .filter(q -> !answeredQuestionIds.contains(q.getQuestionId()))
                    .collect(Collectors.toList());

            boolean allGraded=false;

            if (!unansweredQuestions.isEmpty()) {
                // 构建未答题目的提示信息
                String unansweredInfo = unansweredQuestions.stream()
                        .map(q -> "第" + (questions.indexOf(q) + 1) + "题")
                        .collect(Collectors.joining("、"));
                return Result.error("还有题目未作答：" + unansweredInfo);
            }else{
                allGraded = answeredQuestions.stream()
                        .allMatch(q -> q.getStatus() == 1);//没有未答题的题目，且都已经批改,则可以直接判断是否需要重考
            }

            // 计算总分
            BigDecimal totalScore = answeredQuestions.stream()
                    .map(StudentQuestionScore::getScore)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            if(allGraded){//没有未答题的题目，且都已经批改,则可以直接判断是否需要重考
                // 如果总分低于60分，标记为需要重考
                if (totalScore.compareTo(new BigDecimal("60")) < 0) {
                    examStudentService.markRetakeNeeded(examId, student.getStudentId());
                }
            }

            // 更新学生成绩
            StudentScore studentScore = studentScoreService.getByExamAndStudent(examId, student.getStudentId());
            if (studentScore == null) {
                studentScore = new StudentScore();
                studentScore.setStudentId(student.getStudentId());
                studentScore.setExamId(examId);
                studentScore.setScore(totalScore);
                studentScore.setUploadTime(now);
                studentScoreService.insert(studentScore);
            } else {
                studentScore.setScore(totalScore);
                studentScore.setUploadTime(now);
                studentScoreService.updateById(studentScore);
            }

            // 记录提交时间
            examStudentService.recordSubmitExam(examId, student.getStudentId());

            // 记录操作日志
            Log operationLog = new Log();
            operationLog.setUserId(currentUser.getUserId());
            operationLog.setActionType(1); // UPDATE
            operationLog.setActionDescription("学生[" + currentUser.getUsername() + "]提交试卷，考试ID：" + examId);
            operationLog.setObjectType("EXAM");
            operationLog.setIpAddress(request.getRemoteAddr());
            operationLog.setDeviceInfo(request.getHeader("User-Agent"));
            operationLog.setStatus("SUCCESS");
            operationLog.setCreatedTime(new Date());
            logService.insert(operationLog);

            Map<String, Object> data = new HashMap<>();
            data.put("totalScore", totalScore);
            data.put("submitTime", now);

            return Result.success("试卷提交成功", data);
        } catch (Exception e) {
            // 记录异常日志
            Log operationLog = new Log();
            operationLog.setUserId(currentUser.getUserId());
            operationLog.setActionType(5); // EXCEPTION
            operationLog.setActionDescription("学生[" + currentUser.getUsername() + "]提交试卷失败：" + e.getMessage());
            operationLog.setObjectType("EXAM");
            operationLog.setIpAddress(request.getRemoteAddr());
            operationLog.setDeviceInfo(request.getHeader("User-Agent"));
            operationLog.setStatus("ERROR");
            operationLog.setCreatedTime(new Date());
            logService.insert(operationLog);

            log.error("提交试卷失败", e);
            return Result.error("提交试卷失败：" + e.getMessage());
        }
    }

    @ApiOperation("获取剩余考试时间")
    @GetMapping("/{examId}/remaining-time")
    public Result getRemainingTime(
            @PathVariable Integer examId,
            HttpServletRequest request) {
        User currentUser = TokenUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.error("用户未登录");
        }

        try {
            // 获取当前学生信息
            Student student = studentService.getByUserId(currentUser.getUserId());
            if (student == null) {
                return Result.error("未找到学生信息");
            }

            // 验证考试是否存在
            Exam exam = examService.getById(examId);
            if (exam == null) {
                return Result.error("考试不存在");
            }

            // 验证学生是否有权限参加考试
            ExamStudent examStudent = examStudentService.getByExamIdAndStudentId(examId, student.getStudentId());
            if (examStudent == null) {
                return Result.error("您没有参加此考试的权限");
            }

            // 检查是否已提交考试
            if (examStudent.getStudentSubmitTime() != null) {
                return Result.error("您已经提交试卷");
            }

            // 检查是否被标记为缺考
            if (examStudent.getAbsent()) {
                return Result.error("您已被标记为缺考");
            }

            // 检查是否被标记为违纪
            if (examStudent.getDisciplinary()) {
                return Result.error("您已被标记为违纪");
            }

            Date now = new Date();
            Map<String, Object> data = new HashMap<>();

            // 如果考试还未开始
            if (now.before(exam.getExamStartTime())) {
                long remainingToStart = exam.getExamStartTime().getTime() - now.getTime();
                data.put("status", "NOT_STARTED");
                data.put("remainingToStart", remainingToStart / 1000); // 转换为秒
                data.put("examDuration", exam.getExamDuration() * 60); // 转换为秒
                return Result.success("考试未开始", data);
            }

            // 如果考试已结束
            if (now.after(exam.getExamEndTime())) {
                data.put("status", "ENDED");
                data.put("remainingTime", 0);
                return Result.success("考试已结束", data);
            }

            // 如果学生还未开始考试
            if (examStudent.getStudentStartTime() == null) {
                long remainingToEnd = exam.getExamEndTime().getTime() - now.getTime();
                data.put("status", "NOT_STARTED_BY_STUDENT");
                data.put("remainingToEnd", remainingToEnd / 1000); // 转换为秒
                data.put("examDuration", exam.getExamDuration() * 60); // 转换为秒
                return Result.success("未开始答题", data);
            }

            // 计算剩余时间
            long usedTime = now.getTime() - examStudent.getStudentStartTime().getTime();
            long totalTime = exam.getExamDuration() * 60 * 1000; // 将分钟转换为毫秒
            long remainingTime = totalTime - usedTime;

            // 如果超过考试时间，则以考试结束时间为准
            long timeToEnd = exam.getExamEndTime().getTime() - now.getTime();
            remainingTime = Math.min(remainingTime, timeToEnd);

            // 如果剩余时间小于0，说明考试时间已到
            if (remainingTime <= 0) {
                data.put("status", "TIME_UP");
                data.put("remainingTime", 0);
                return Result.success("考试时间已到", data);
            }

            data.put("status", "IN_PROGRESS");
            data.put("remainingTime", remainingTime / 1000); // 转换为秒
            data.put("usedTime", usedTime / 1000); // 转换为秒
            data.put("examDuration", exam.getExamDuration() * 60); // 转换为秒

            // 记录操作日志
            Log operationLog = new Log();
            operationLog.setUserId(currentUser.getUserId());
            operationLog.setActionType(7); // SELECT
            operationLog.setActionDescription("学生[" + currentUser.getUsername() + "]查看考试剩余时间，考试ID：" + examId);
            operationLog.setObjectType("EXAM_TIME");
            operationLog.setIpAddress(request.getRemoteAddr());
            operationLog.setDeviceInfo(request.getHeader("User-Agent"));
            operationLog.setStatus("SUCCESS");
            operationLog.setCreatedTime(new Date());
            logService.insert(operationLog);

            return Result.success("获取剩余时间成功", data);
        } catch (Exception e) {
            // 记录异常日志
            Log operationLog = new Log();
            operationLog.setUserId(currentUser.getUserId());
            operationLog.setActionType(5); // EXCEPTION
            operationLog.setActionDescription("学生[" + currentUser.getUsername() + "]获取考试剩余时间失败：" + e.getMessage());
            operationLog.setObjectType("EXAM_TIME");
            operationLog.setIpAddress(request.getRemoteAddr());
            operationLog.setDeviceInfo(request.getHeader("User-Agent"));
            operationLog.setStatus("ERROR");
            operationLog.setCreatedTime(new Date());
            logService.insert(operationLog);

            log.error("获取剩余考试时间失败", e);
            return Result.error("获取剩余考试时间失败：" + e.getMessage());
        }
    }
} 
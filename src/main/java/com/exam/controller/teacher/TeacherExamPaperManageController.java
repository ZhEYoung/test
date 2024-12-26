package com.exam.controller.teacher;

import com.exam.entity.*;
import com.exam.service.*;
import com.exam.utils.TokenUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import com.exam.common.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Date;
import java.math.BigDecimal;
import java.util.Calendar;

@Slf4j
@RestController
@RequestMapping("/teacher/papers")
@Api(tags = "教师试卷管理接口")
public class TeacherExamPaperManageController {

    @Autowired
    private ExamPaperService examPaperService;

    @Autowired
    private QuestionService questionService;

    @Autowired
    private LogService logService;

    @Autowired
    private TeacherService teacherService;

    @Autowired
    private SubjectService subjectService;

    @Autowired
    private CollegeService collegeService;

    @Autowired
    private QuestionOptionService questionOptionService;

    private Integer getTeacherIdByUserId(Integer userId) {
        Teacher teacher = teacherService.getByUserId(userId);
        return teacher != null ? teacher.getTeacherId() : null;
    }

    //试卷难度系数计算：所有选中题目的难度之和 / 题目总数

    @ApiOperation("获取试卷列表")
    @GetMapping
    public Result getPaperList(@RequestParam(required = false) String keyword,
                             @RequestParam(required = false) Integer subjectId,
                             @RequestParam(required = false) Integer status,
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize,
                             HttpServletRequest request) {
        User currentUser = TokenUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.error("用户未登录");
        }

        try {
            Map<String, Object> condition = new HashMap<>();
            if (keyword != null && !keyword.trim().isEmpty()) {
                condition.put("keyword", keyword);
            }
            if (subjectId != null) {
                condition.put("subjectId", subjectId);
            }
            if (status != null) {
                condition.put("paperStatus", status);
            }
            Integer teacherId = getTeacherIdByUserId(currentUser.getUserId());
            if (teacherId == null) {
                return Result.error("未找到教师信息");
            }
            condition.put("teacherId", teacherId);

            List<ExamPaper> papers = examPaperService.getPageByCondition(condition, pageNum, pageSize);
            Long total = examPaperService.getCountByCondition(condition);

            for (ExamPaper paper : papers) {
                Subject subject = subjectService.getById(paper.getSubjectId());
                subject.setCollege(collegeService.getById(subject.getCollegeId()));
                paper.setSubject(subject);

                paper.setTeacher(teacherService.getById(paper.getTeacherId()));
                paper.setTotalScore(BigDecimal.valueOf(100));
                paper.setStatusName(paper.getPaperStatus() == 0 ? "未发布" : "已发布");
                paper.setExamTypeName(paper.getExamType() == 0 ? "期末考试" : "普通考试");

            }

            Map<String, Object> data = new HashMap<>();
            data.put("list", papers);
            data.put("total", total);

            // 记录操作日志
            Log operationLog = new Log();
            operationLog.setUserId(currentUser.getUserId());
            operationLog.setActionType(7); // SELECT
            operationLog.setActionDescription("教师[" + currentUser.getUsername() + "]查询了试卷列表");
            operationLog.setObjectType("EXAM_PAPER");
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
            operationLog.setActionDescription("教师[" + currentUser.getUsername() + "]查询试卷列表异常：" + e.getMessage());
            operationLog.setObjectType("EXAM_PAPER");
            operationLog.setIpAddress(request.getRemoteAddr());
            operationLog.setDeviceInfo(request.getHeader("User-Agent"));
            operationLog.setStatus("ERROR");
            operationLog.setCreatedTime(new Date());
            logService.insert(operationLog);

            log.error("获取试卷列表失败", e);
            return Result.error("获取试卷列表失败：" + e.getMessage());
        }
    }


    @ApiOperation("更新试卷")
    @PutMapping("/{paperId}")
    public Result updatePaper(@PathVariable Integer paperId,
                            @RequestBody ExamPaper paper,
                            HttpServletRequest request) {
        User currentUser = TokenUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.error("用户未登录");
        }

        try {
            ExamPaper existingPaper = examPaperService.getById(paperId);
            if (existingPaper == null) {
                return Result.error("试卷不存在");
            }

            // 检查是否是试卷的创建者
            Integer teacherId = getTeacherIdByUserId(currentUser.getUserId());
            if (teacherId == null) {
                return Result.error("未找到教师信息");
            }
            if (!existingPaper.getTeacherId().equals(teacherId)) {
                return Result.error("无权修改该试卷");
            }

            paper.setPaperId(paperId);
            int result = examPaperService.updateById(paper);
            if (result <= 0) {
                return Result.error("更新试卷失败");
            }

            // 记录操作日志
            Log operationLog = new Log();
            operationLog.setUserId(currentUser.getUserId());
            operationLog.setActionType(1); // UPDATE
            operationLog.setActionDescription("教师[" + currentUser.getUsername() + "]更新了试卷，试卷ID：" + paperId);
            operationLog.setObjectType("EXAM_PAPER");
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
            operationLog.setActionDescription("教师[" + currentUser.getUsername() + "]更新试卷异常，试卷ID：" + paperId + "，错误：" + e.getMessage());
            operationLog.setObjectType("EXAM_PAPER");
            operationLog.setIpAddress(request.getRemoteAddr());
            operationLog.setDeviceInfo(request.getHeader("User-Agent"));
            operationLog.setStatus("ERROR");
            operationLog.setCreatedTime(new Date());
            logService.insert(operationLog);

            log.error("更新试卷失败", e);
            return Result.error("更新试卷失败：" + e.getMessage());
        }
    }


    @ApiOperation("获取试卷详情")
    @GetMapping("/{paperId}")
    public Result getPaperDetail(@PathVariable Integer paperId, HttpServletRequest request) {
        User currentUser = TokenUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.error("用户未登录");
        }

        try {
            ExamPaper paper = examPaperService.getById(paperId);
            if (paper == null) {
                return Result.error("试卷不存在");
            }
            Subject subject = subjectService.getById(paper.getSubjectId());
            subject.setCollege(collegeService.getById(subject.getCollegeId()));
            paper.setSubject(subject);

            paper.setTeacher(teacherService.getById(paper.getTeacherId()));
            paper.setTotalScore(BigDecimal.valueOf(100));
            paper.setStatusName(paper.getPaperStatus() == 0 ? "未发布" : "已发布");
            paper.setExamTypeName(paper.getExamType() == 0 ? "期末考试" : "普通考试");


            // 获取试卷题目
            List<Map<String, Object>> questions = examPaperService.getPaperQuestionsWithScore(paperId);

            if(!questions.isEmpty()){
                BigDecimal paperDifficulty = BigDecimal.ZERO;
                Integer geshu = questions.size();
                for (Map<String, Object> question : questions) {
                    Question q = (Question) question.get("question");

                    if(q.getType() == 1 || q.getType() == 0 || q.getType() == 2){
                        List<QuestionOption> options = questionOptionService.getByQuestionId(q.getQuestionId());
                        q.setOptions(options);
                    }

                    question.put("question", q);


                    paperDifficulty = paperDifficulty.add(new BigDecimal(String.valueOf(q.getDifficulty())));
                }
                paperDifficulty = paperDifficulty.divide(BigDecimal.valueOf(geshu), 2, BigDecimal.ROUND_HALF_UP);
                paper.setPaperDifficulty(paperDifficulty);
            }



            Map<String, Object> data = new HashMap<>();
            data.put("paper", paper);
            data.put("questions", questions);

            // 记录操作日志
            Log operationLog = new Log();
            operationLog.setUserId(currentUser.getUserId());
            operationLog.setActionType(7); // SELECT
            operationLog.setActionDescription("教师[" + currentUser.getUsername() + "]查看了试卷详情，试卷ID：" + paperId);
            operationLog.setObjectType("EXAM_PAPER");
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
            operationLog.setActionDescription("教师[" + currentUser.getUsername() + "]查看试卷详情异常，试卷ID：" + paperId + "，错误：" + e.getMessage());
            operationLog.setObjectType("EXAM_PAPER");
            operationLog.setIpAddress(request.getRemoteAddr());
            operationLog.setDeviceInfo(request.getHeader("User-Agent"));
            operationLog.setStatus("ERROR");
            operationLog.setCreatedTime(new Date());
            logService.insert(operationLog);

            log.error("获取试卷详情失败", e);
            return Result.error("获取试卷详情失败：" + e.getMessage());
        }
    }

    @ApiOperation("自动组卷")
    @PostMapping("/generate")
    public Result generatePaper(@RequestBody Map<String, Object> params,
                              HttpServletRequest request) {
        User currentUser = TokenUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.error("用户未登录");
        }

        try {
            // 从参数中获取值并进行类型转换
            Integer subjectId = Integer.valueOf(params.get("subjectId").toString());
            String paperName = (String) params.get("paperName");
            BigDecimal difficulty = new BigDecimal(params.get("difficulty").toString());
            Integer year = Integer.valueOf(params.get("year").toString());
            Integer semester = Integer.valueOf(params.get("semester").toString());
            Integer examType = Integer.valueOf(params.get("examType").toString());
            
            // 处理题型数量
            @SuppressWarnings("unchecked")
            Map<String, Object> rawTypeCount = (Map<String, Object>) params.get("questionTypeCount");
            Map<Integer, Integer> questionTypeCount = new HashMap<>();
            for (Map.Entry<String, Object> entry : rawTypeCount.entrySet()) {
                questionTypeCount.put(
                    Integer.valueOf(entry.getKey()),
                    Integer.valueOf(entry.getValue().toString())
                );
            }
            
            // 处理题型分数比例（可选）
            Map<Integer, BigDecimal> typeScoreRatio = null;
            if (params.containsKey("typeScoreRatio")) {
                @SuppressWarnings("unchecked")
                Map<String, Object> rawScoreRatio = (Map<String, Object>) params.get("typeScoreRatio");
                typeScoreRatio = new HashMap<>();
                for (Map.Entry<String, Object> entry : rawScoreRatio.entrySet()) {
                    typeScoreRatio.put(
                        Integer.valueOf(entry.getKey()),
                        new BigDecimal(entry.getValue().toString())
                    );
                }
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

            // 获取教师ID
            Integer teacherId = getTeacherIdByUserId(currentUser.getUserId());
            if (teacherId == null) {
                return Result.error("未找到教师信息");
            }

            ExamPaper paper = examPaperService.generatePaper(
                subjectId,
                paperName,
                difficulty,
                questionTypeCount,
                typeScoreRatio,
                teacherId,
                academicTerm,
                examType
            );

            if (paper == null) {
                return Result.error("自动组卷失败");
            }
            paper.setTotalScore(BigDecimal.valueOf(100));
            paper.setExamTypeName(paper.getExamType() == 0 ? "期末考试" : "普通考试");
            paper.setSubject(subjectService.getById(paper.getSubjectId()));

            // 记录操作日志
            Log operationLog = new Log();
            operationLog.setUserId(currentUser.getUserId());
            operationLog.setActionType(0); // INSERT
            operationLog.setActionDescription("教师[" + currentUser.getUsername() + "]进行了自动组卷，试卷名称：" + paperName);
            operationLog.setObjectType("EXAM_PAPER");
            operationLog.setIpAddress(request.getRemoteAddr());
            operationLog.setDeviceInfo(request.getHeader("User-Agent"));
            operationLog.setStatus("SUCCESS");
            operationLog.setCreatedTime(new Date());
            logService.insert(operationLog);

            return Result.success(paper);
        } catch (Exception e) {
            // 记录异常日志
            Log operationLog = new Log();
            operationLog.setUserId(currentUser.getUserId());
            operationLog.setActionType(5); // EXCEPTION
            operationLog.setActionDescription("教师[" + currentUser.getUsername() + "]自动组卷异常：" + e.getMessage());
            operationLog.setObjectType("EXAM_PAPER");
            operationLog.setIpAddress(request.getRemoteAddr());
            operationLog.setDeviceInfo(request.getHeader("User-Agent"));
            operationLog.setStatus("ERROR");
            operationLog.setCreatedTime(new Date());
            logService.insert(operationLog);

            log.error("自动组卷失败", e);
            return Result.error("自动组卷失败：" + e.getMessage());
        }
    }


    //命令格式：curl -X POST "http://localhost:8080/api/teacher/papers/generate-manual" -H "token:eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJhdWQiOiIyIiwic2lnbiI6IjhkOTY5ZWVmNmVjYWQzYzI5YTNhNjI5MjgwZTY4NmNmMGMzZjVkNWE4NmFmZjNjYTEyMDIwYzkyM2FkYzZjOTIiLCJleHAiOjE3MzQ0ODg4MzQsImlhdCI6MTczNDQwMjQzNH0.QCVXmgHKm9SjIFNN7c9tponTxs8fVElYC7qE4OWjanw" -H "Content-Type: application/json" -d "{\"paperName\":\"Java程序设计期末考试\",\"subjectId\":1,\"description\":\"本试卷包含单选题、判断题和简答题，总分100分\",\"examType\":0,\"questionScores\":{\"4\":\"20\",\"2\":\"20\",\"1\":\"30\",\"3\":\"30\"},\"difficulty\":0.7,\"year\":2024,\"semester\":1}"
    @ApiOperation("手动组卷")
    @PostMapping("/generate-manual")
    public Result generatePaperManually(@RequestBody Map<String, Object> params,
                                      HttpServletRequest request) {
        User currentUser = TokenUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.error("用户未登录");
        }

        try {
            // 从参数中获取值并进行类型转换
            Integer subjectId = Integer.valueOf(params.get("subjectId").toString());
            String paperName = (String) params.get("paperName");

            // 处理questionScores

            Map<String, Object> rawScores = (Map<String, Object>) params.get("questionScores");
            Map<Integer, BigDecimal> questionScores = new HashMap<>();
            for (Map.Entry<String, Object> entry : rawScores.entrySet()) {
                questionScores.put(
                    Integer.valueOf(entry.getKey()),
                    new BigDecimal(entry.getValue().toString())
                );
            }
            
            BigDecimal difficulty = new BigDecimal(params.get("difficulty").toString());
            Integer examType = Integer.valueOf(params.get("examType").toString());
            Integer year = Integer.valueOf(params.get("year").toString());
            Integer semester = Integer.valueOf(params.get("semester").toString());

            // 验证分值总和是否为100分
            BigDecimal totalScore = questionScores.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            if (totalScore.compareTo(new BigDecimal("100")) != 0) {
                return Result.error("试题总分必须为100分，当前总分为: " + totalScore);
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

            // 获取教师ID
            Integer teacherId = getTeacherIdByUserId(currentUser.getUserId());
            if (teacherId == null) {
                return Result.error("未找到教师信息");
            }

            ExamPaper paper = examPaperService.generatePaperManually(
                subjectId,
                paperName,
                questionScores,
                difficulty,
                examType,
                academicTerm,
                teacherId
            );

            if (paper == null) {
                return Result.error("手动组卷失败");
            }

            // 记录操作日志
            Log operationLog = new Log();
            operationLog.setUserId(currentUser.getUserId());
            operationLog.setActionType(0); // INSERT
            operationLog.setActionDescription("教师[" + currentUser.getUsername() + "]进行了手动组卷，试卷名称：" + paperName);
            operationLog.setObjectType("EXAM_PAPER");
            operationLog.setIpAddress(request.getRemoteAddr());
            operationLog.setDeviceInfo(request.getHeader("User-Agent"));
            operationLog.setStatus("SUCCESS");
            operationLog.setCreatedTime(new Date());
            logService.insert(operationLog);

            return Result.success(paper);
        } catch (Exception e) {
            // 记录异常日志
            Log operationLog = new Log();
            operationLog.setUserId(currentUser.getUserId());
            operationLog.setActionType(5); // EXCEPTION
            operationLog.setActionDescription("教师[" + currentUser.getUsername() + "]手动组卷异常：" + e.getMessage());
            operationLog.setObjectType("EXAM_PAPER");
            operationLog.setIpAddress(request.getRemoteAddr());
            operationLog.setDeviceInfo(request.getHeader("User-Agent"));
            operationLog.setStatus("ERROR");
            operationLog.setCreatedTime(new Date());
            logService.insert(operationLog);

            log.error("手动组卷失败", e);
            return Result.error("手动组卷失败：" + e.getMessage());
        }
    }

    @ApiOperation("获取试卷平均分")
    @GetMapping("/{paperId}/average-score")
    public Result getPaperAverageScore(@PathVariable Integer paperId, HttpServletRequest request) {
        User currentUser = TokenUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.error("用户未登录");
        }

        try {
            ExamPaper paper = examPaperService.getById(paperId);
            if (paper == null) {
                return Result.error("试卷不存在");
            }

            // 检查是否是试卷的创建者
            Integer teacherId = getTeacherIdByUserId(currentUser.getUserId());
            if (teacherId == null) {
                return Result.error("未找到教师信息");
            }
            if (!paper.getTeacherId().equals(teacherId)) {
                return Result.error("无权查看该试卷数据");
            }

            BigDecimal averageScore = examPaperService.calculateAverageScore(paperId);
            
            // 记录操作日志
            Log operationLog = new Log();
            operationLog.setUserId(currentUser.getUserId());
            operationLog.setActionType(7); // SELECT
            operationLog.setActionDescription("教师[" + currentUser.getUsername() + "]查看了试卷平均分，试卷ID：" + paperId);
            operationLog.setObjectType("EXAM_PAPER");
            operationLog.setIpAddress(request.getRemoteAddr());
            operationLog.setDeviceInfo(request.getHeader("User-Agent"));
            operationLog.setStatus("SUCCESS");
            operationLog.setCreatedTime(new Date());
            logService.insert(operationLog);

            Map<String, Object> data = new HashMap<>();
            data.put("averageScore", averageScore);
            data.put("paperName", paper.getPaperName());
            return Result.success(data);
        } catch (Exception e) {
            // 记录异常日志
            Log operationLog = new Log();
            operationLog.setUserId(currentUser.getUserId());
            operationLog.setActionType(5); // EXCEPTION
            operationLog.setActionDescription("教师[" + currentUser.getUsername() + "]查看试卷平均分异常，试卷ID：" + paperId + "，错误：" + e.getMessage());
            operationLog.setObjectType("EXAM_PAPER");
            operationLog.setIpAddress(request.getRemoteAddr());
            operationLog.setDeviceInfo(request.getHeader("User-Agent"));
            operationLog.setStatus("ERROR");
            operationLog.setCreatedTime(new Date());
            logService.insert(operationLog);

            log.error("获取试卷平均分失败", e);
            return Result.error("获取试卷平均分失败：" + e.getMessage());
        }
    }

    @ApiOperation("获取试卷成绩分布")
    @GetMapping("/{paperId}/score-distribution")
    public Result getPaperScoreDistribution(@PathVariable Integer paperId, HttpServletRequest request) {
        User currentUser = TokenUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.error("用户未登录");
        }

        try {
            ExamPaper paper = examPaperService.getById(paperId);
            if (paper == null) {
                return Result.error("试卷不存在");
            }

            // 检查是否是试卷的创建者
            Integer teacherId = getTeacherIdByUserId(currentUser.getUserId());
            if (teacherId == null) {
                return Result.error("未找到教师信息");
            }
            if (!paper.getTeacherId().equals(teacherId)) {
                return Result.error("无权查看该试卷数据");
            }

            List<Map<String, Object>> distribution = examPaperService.getScoreDistribution(paperId);
            
            // 获取最高分和最低分
            BigDecimal highestScore = examPaperService.getHighestScore(paperId);
            BigDecimal lowestScore = examPaperService.getLowestScore(paperId);
            
            // 记录操作日志
            Log operationLog = new Log();
            operationLog.setUserId(currentUser.getUserId());
            operationLog.setActionType(7); // SELECT
            operationLog.setActionDescription("教师[" + currentUser.getUsername() + "]查看了试卷成绩分布，试卷ID：" + paperId);
            operationLog.setObjectType("EXAM_PAPER");
            operationLog.setIpAddress(request.getRemoteAddr());
            operationLog.setDeviceInfo(request.getHeader("User-Agent"));
            operationLog.setStatus("SUCCESS");
            operationLog.setCreatedTime(new Date());
            logService.insert(operationLog);

            Map<String, Object> data = new HashMap<>();
            data.put("distribution", distribution);
            data.put("paperName", paper.getPaperName());
            data.put("highestScore", highestScore);
            data.put("lowestScore", lowestScore);
            return Result.success(data);
        } catch (Exception e) {
            // 记录异常日志
            Log operationLog = new Log();
            operationLog.setUserId(currentUser.getUserId());
            operationLog.setActionType(5); // EXCEPTION
            operationLog.setActionDescription("教师[" + currentUser.getUsername() + "]查看试卷成绩分布异常，试卷ID：" + paperId + "，错误：" + e.getMessage());
            operationLog.setObjectType("EXAM_PAPER");
            operationLog.setIpAddress(request.getRemoteAddr());
            operationLog.setDeviceInfo(request.getHeader("User-Agent"));
            operationLog.setStatus("ERROR");
            operationLog.setCreatedTime(new Date());
            logService.insert(operationLog);

            log.error("获取试卷成绩分布失败", e);
            return Result.error("获取试卷成绩分布失败：" + e.getMessage());
        }
    }

} 
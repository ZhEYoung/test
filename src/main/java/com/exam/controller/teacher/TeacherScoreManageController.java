package com.exam.controller.teacher;

import com.exam.entity.*;
import com.exam.mapper.ExamStudentMapper;
import com.exam.service.*;
import com.exam.utils.TokenUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import com.exam.common.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Date;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;

@Slf4j
@RestController
@RequestMapping("/teacher/scores")
@Api(tags = "教师成绩管理接口")
public class TeacherScoreManageController {

    @Autowired
    private StudentScoreService studentScoreService;

    @Autowired
    private LogService logService;

    @Autowired
    private ExamService examService;

    @Autowired
    private ClassService classService;

    @Autowired
    private StudentClassService studentClassService;

    @Autowired
    private StudentQuestionScoreService studentQuestionScoreService;

    @Autowired
    private QuestionService questionService;

    @Autowired
    private ExamStudentService examStudentService;

    @Autowired
    private StudentService studentService;
    @Autowired
    private ExamStudentMapper examStudentMapper;
    @Autowired
    private ExamClassService examClassService;


    @ApiOperation("获取指定考试指定班级的全部学生成绩")
    @GetMapping
    public Result getScoreList(@RequestParam Integer examId,
                             @RequestParam Integer classId,
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

            // 验证班级是否存在
            com.exam.entity.Class examClass = classService.getById(classId);
            if (examClass == null) {
                return Result.error("班级不存在");
            }

            // 获取班级所有学生
            List<StudentClass> studentClasses = studentClassService.getByClassId(classId);
            if (studentClasses == null || studentClasses.isEmpty()) {
                return Result.error("该班级暂无学生");
            }

            // 获取所有学生的成绩
            List<StudentScore> scores = new ArrayList<>();
            for (StudentClass studentClass : studentClasses) {

                StudentScore score = studentScoreService.getByExamAndStudent(examId, studentClass.getStudentId());

                score.setStudent(studentService.getById(studentClass.getStudentId()));

                score.setExam(examService.getById(examId));

                String studentName = studentService.getById(studentClass.getStudentId()).getName();
                score.setStudentName(studentName);

                String examName = examService.getById(examId).getExamName();
                score.setExamName(examName);

                String teacherComment = examStudentService.getByExamIdAndStudentId(examId, studentClass.getStudentId()).getTeacherComment();
                score.setTeacherComment(teacherComment);

                Integer rank = studentScoreService.getStudentRank(examId, studentClass.getStudentId());
                score.setRank(rank);

                List<StudentQuestionScore> studentQuestionScores  = studentQuestionScoreService.getByExamAndStudent(examId, studentClass.getStudentId());
                List<Question> questions = new ArrayList<>();

                for(StudentQuestionScore studentQuestionScore : studentQuestionScores){
                    Integer questionId = studentQuestionScore.getQuestionId();
                    Question question = questionService.getById(questionId);
                    question.setScore(studentQuestionScore.getScore());
                    questions.add(question);
                }
                score.setQuestions(questions);
                if (score != null) {
                    scores.add(score);
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
            operationLog.setActionDescription("教师[" + currentUser.getUsername() + "]查询了考试[" + examId + "]班级[" + classId + "]的全部学生成绩");
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
            operationLog.setActionDescription("教师[" + currentUser.getUsername() + "]查询考试成绩列表异常：" + e.getMessage());
            operationLog.setObjectType("SCORE");
            operationLog.setIpAddress(request.getRemoteAddr());
            operationLog.setDeviceInfo(request.getHeader("User-Agent"));
            operationLog.setStatus("ERROR");
            operationLog.setCreatedTime(new Date());
            logService.insert(operationLog);

            log.error("获取考试成绩列表失败", e);
            return Result.error("获取考试成绩列表失败：" + e.getMessage());
        }
    }

    @ApiOperation("获取指定考试全部学生成绩")
    @GetMapping("/all")
    public Result getAllScoreList(@RequestParam Integer examId,
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

            List<ExamStudent> examStudents = examStudentService.getByExamId(examId);

            // 获取所有学生的成绩
            List<StudentScore> scores = new ArrayList<>();
            for (ExamStudent examStudent : examStudents) {
                StudentScore score = studentScoreService.getByExamAndStudent(examId, examStudent.getStudentId());
                if (score != null) {
                    score.setStudent(studentService.getById(examStudent.getStudentId()));
                    score.setExam(examService.getById(examId));
                    score.setTeacherComment(examStudent.getTeacherComment());
                    score.setRank(studentScoreService.getStudentRank(examId, examStudent.getStudentId()));
                    score.setStudentName(studentService.getById(examStudent.getStudentId()).getName());

                    List<StudentQuestionScore> studentQuestionScores  = studentQuestionScoreService.getByExamAndStudent(examId, examStudent.getStudentId());
                    List<Question> questions = new ArrayList<>();

                    for(StudentQuestionScore studentQuestionScore : studentQuestionScores){
                        Integer questionId = studentQuestionScore.getQuestionId();
                        Question question = questionService.getById(questionId);
                        question.setScore(studentQuestionScore.getScore());
                        questions.add(question);
                    }
                    score.setQuestions(questions);

                    scores.add(score);
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
            operationLog.setActionDescription("教师[" + currentUser.getUsername() + "]查询了考试[" + examId + "]的全部学生成绩");
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
            operationLog.setActionDescription("教师[" + currentUser.getUsername() + "]查询考试成绩列表异常：" + e.getMessage());
            operationLog.setObjectType("SCORE");
            operationLog.setIpAddress(request.getRemoteAddr());
            operationLog.setDeviceInfo(request.getHeader("User-Agent"));
            operationLog.setStatus("ERROR");
            operationLog.setCreatedTime(new Date());
            logService.insert(operationLog);

            log.error("获取考试成绩列表失败", e);
            return Result.error("获取考试成绩列表失败：" + e.getMessage());
        }
    }


    @ApiOperation("更新学生成绩")
    @PutMapping("/{scoreId}")
    public Result updateScore(@PathVariable Integer scoreId,
                        @RequestBody Map<String, BigDecimal> scoreMap,
                        HttpServletRequest request) {
        User currentUser = TokenUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.error("用户未登录");
        }

        try {
            StudentScore studentScore = studentScoreService.getById(scoreId);
            if (studentScore == null) {
                return Result.error("成绩不存在");
            }
            
            BigDecimal score = scoreMap.get("score");
            if (score == null) {
                return Result.error("成绩不能为空");
            }
            
            studentScore.setScore(score);
            List<StudentScore> scores = new ArrayList<>();
            scores.add(studentScore);
            int result = studentScoreService.batchUpdateScores(scores);

            // 更新是否需要补考状态
            if (score.compareTo(new BigDecimal("60")) < 0) {
                examStudentService.markRetakeNeeded(studentScore.getExamId(), studentScore.getStudentId());
            } else {
                examStudentService.markRetakeNotNeeded(studentScore.getExamId(), studentScore.getStudentId());
            }

            if (result <= 0) {
                return Result.error("更新成绩失败");
            }

            // 记录操作日志
            Log operationLog = new Log();
            operationLog.setUserId(currentUser.getUserId());
            operationLog.setActionType(1); // UPDATE
            operationLog.setActionDescription("教师[" + currentUser.getUsername() + "]更新了学生成绩，成绩ID：" + scoreId);
            operationLog.setObjectType("SCORE");
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
            operationLog.setActionDescription("教师[" + currentUser.getUsername() + "]更新学生成绩异常，成绩ID：" + scoreId + "，错误：" + e.getMessage());
            operationLog.setObjectType("SCORE");
            operationLog.setIpAddress(request.getRemoteAddr());
            operationLog.setDeviceInfo(request.getHeader("User-Agent"));
            operationLog.setStatus("ERROR");
            operationLog.setCreatedTime(new Date());
            logService.insert(operationLog);

            log.error("更新学生成绩失败", e);
            return Result.error("更新学生成绩失败：" + e.getMessage());
        }
    }

    @ApiOperation("获取班级考试成绩统计")
    @GetMapping("/stats/{examId}/{classId}")
    public Result getExamStats(@PathVariable Integer examId, 
                             @PathVariable Integer classId,
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

            // 验证班级是否存在
            com.exam.entity.Class examClass = classService.getById(classId);
            if (examClass == null) {
                return Result.error("班级不存在");
            }

            Map<String, Object> stats = new HashMap<>();

            // 获取平均分
            Double avgScore = classService.getAvgScore(classId, examId);
            stats.put("averageScore", avgScore != null ? new BigDecimal(avgScore).setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO);

            // 获取成绩分布
            List<Map<String, Object>> distribution = classService.getScoreDistribution(classId, examId);
            
            // 处理成绩分布数据，确保类型正确
            List<Map<String, Object>> processedDistribution = new ArrayList<>();
            for (Map<String, Object> range : distribution) {
                Map<String, Object> processedRange = new HashMap<>();
                processedRange.put("score_range", range.get("score_range"));
                // 确保count是Integer类型
                Object countObj = range.get("count");
                if (countObj instanceof Long) {
                    processedRange.put("count", ((Long) countObj).intValue());
                } else if (countObj instanceof Integer) {
                    processedRange.put("count", countObj);
                } else {
                    processedRange.put("count", 0);
                }
                processedDistribution.add(processedRange);
            }
            stats.put("scoreDistribution", processedDistribution);

            // 统计及格人数和总人数
            int totalStudents = 0;
            int passedStudents = 0;
            BigDecimal passScore = new BigDecimal("60"); // 及格分数线

            for (Map<String, Object> range : processedDistribution) {
                String scoreRange = (String) range.get("score_range");
                Integer count = (Integer) range.get("count");
                
                // 解析分数范围
                String[] parts = scoreRange.split("-");
                BigDecimal rangeStart = new BigDecimal(parts[0]);
                
                // 统计及格人数
                if (rangeStart.compareTo(passScore) >= 0) {
                    passedStudents += count;
                }
                totalStudents += count;
            }
            
            // 计算及格率
            BigDecimal passRate = totalStudents > 0 
                ? new BigDecimal(passedStudents).divide(new BigDecimal(totalStudents), 4, RoundingMode.HALF_UP).multiply(new BigDecimal(100))
                : BigDecimal.ZERO;
            stats.put("passRate", passRate);

            // 添加总人数信息
            stats.put("totalStudents", totalStudents);
            stats.put("passedStudents", passedStudents);

            // 记录操作日志
            Log operationLog = new Log();
            operationLog.setUserId(currentUser.getUserId());
            operationLog.setActionType(7); // SELECT
            operationLog.setActionDescription("教师[" + currentUser.getUsername() + "]查看了班级[" + classId + "]在考试[" + examId + "]中的成绩统计");
            operationLog.setObjectType("EXAM_STATS");
            operationLog.setIpAddress(request.getRemoteAddr());
            operationLog.setDeviceInfo(request.getHeader("User-Agent"));
            operationLog.setStatus("SUCCESS");
            operationLog.setCreatedTime(new Date());
            logService.insert(operationLog);

            return Result.success(stats);
        } catch (Exception e) {
            // 记录异常日志
            Log operationLog = new Log();
            operationLog.setUserId(currentUser.getUserId());
            operationLog.setActionType(5); // EXCEPTION
            operationLog.setActionDescription("教师[" + currentUser.getUsername() + "]查看班级成绩统计时发生异常：" + e.getMessage());
            operationLog.setObjectType("EXAM_STATS");
            operationLog.setIpAddress(request.getRemoteAddr());
            operationLog.setDeviceInfo(request.getHeader("User-Agent"));
            operationLog.setStatus("ERROR");
            operationLog.setCreatedTime(new Date());
            logService.insert(operationLog);

            log.error("获取考试统计信息失败", e);
            return Result.error("获取考试统计信息失败：" + e.getMessage());
        }
    }

    @ApiOperation("导出指定考试指定班级的全部学生成绩")
    @GetMapping("/export")
    public ResponseEntity<byte[]> exportScores(@RequestParam Integer examId,
                                             @RequestParam Integer classId,
                                             HttpServletRequest request) {
        User currentUser = TokenUtils.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.badRequest().build();
        }

        try {
            // 验证考试是否存在
            Exam exam = examService.getById(examId);
            if (exam == null) {
                return ResponseEntity.badRequest().build();
            }

            // 验证班级是否存在
            com.exam.entity.Class examClass = classService.getById(classId);
            if (examClass == null) {
                return ResponseEntity.badRequest().build();
            }

            // 获取班级所有学生
            List<StudentClass> studentClasses = studentClassService.getByClassId(classId);
            if (studentClasses == null || studentClasses.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            // 获取所有学生的成绩
            List<StudentScore> scores = new ArrayList<>();
            for (StudentClass studentClass : studentClasses) {
                StudentScore score = studentScoreService.getByExamAndStudent(examId, studentClass.getStudentId());
                if (score != null) {
                    scores.add(score);
                }
            }

            // 创建Excel工作簿
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("成绩表");

            // 创建标题样式
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            // 创建标题行
            Row headerRow = sheet.createRow(0);
            String[] headers = {"学生ID", "姓名", "考试名称", "班级", "成绩"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
                sheet.setColumnWidth(i, 15 * 256); // 设置列宽
            }

            // 填充数据
            int rowNum = 1;
            for (StudentScore score : scores) {
                Student student = studentService.getById(score.getStudentId());
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(score.getStudentId());
                row.createCell(1).setCellValue(student.getName());
                row.createCell(2).setCellValue(exam.getExamName());
                row.createCell(3).setCellValue(examClass.getClassName());
                row.createCell(4).setCellValue(score.getScore() != null ? score.getScore().toString() : "");
            }

            // 写入Excel文件
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            workbook.close();

            // 记录操作日志
            Log operationLog = new Log();
            operationLog.setUserId(currentUser.getUserId());
            operationLog.setActionType(8); // EXPORT
            operationLog.setActionDescription("教师[" + currentUser.getUsername() + "]导出了考试[" + examId + "]班级[" + classId + "]的全部学生成绩");
            operationLog.setObjectType("SCORE");
            operationLog.setIpAddress(request.getRemoteAddr());
            operationLog.setDeviceInfo(request.getHeader("User-Agent"));
            operationLog.setStatus("SUCCESS");
            operationLog.setCreatedTime(new Date());
            logService.insert(operationLog);

            // 设置响应头
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            String filename = exam.getExamName() + "_" + examClass.getClassName() + "_成绩表.xlsx";
            responseHeaders.setContentDispositionFormData("attachment", new String(filename.getBytes("UTF-8"), "ISO-8859-1"));

            return ResponseEntity.ok()
                    .headers(responseHeaders)
                    .body(outputStream.toByteArray());

        } catch (Exception e) {
            // 记录异常日志
            Log operationLog = new Log();
            operationLog.setUserId(currentUser.getUserId());
            operationLog.setActionType(5); // EXCEPTION
            operationLog.setActionDescription("教师[" + currentUser.getUsername() + "]导出成绩表异常：" + e.getMessage());
            operationLog.setObjectType("SCORE");
            operationLog.setIpAddress(request.getRemoteAddr());
            operationLog.setDeviceInfo(request.getHeader("User-Agent"));
            operationLog.setStatus("ERROR");
            operationLog.setCreatedTime(new Date());
            logService.insert(operationLog);

            log.error("导出成绩表失败", e);
            return ResponseEntity.badRequest().build();
        }
    }



    @ApiOperation("更新学生考试评语")
    @PutMapping("/comment")
    public Result updateComment(@RequestBody Map<String, Object> params,
                              HttpServletRequest request) {
        User currentUser = TokenUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.error("用户未登录");
        }

        try {
            // 获取参数
            Integer examId = (Integer) params.get("examId");
            Integer studentId = (Integer) params.get("studentId");
            String comment = (String) params.get("comment");

            // 参数验证
            if (examId == null || studentId == null || comment == null) {
                return Result.error("参数不完整");
            }

            // 验证考试是否存在
            Exam exam = examService.getById(examId);
            if (exam == null) {
                return Result.error("考试不存在");
            }

            // 验证学生是否存在
            Student student = studentService.getById(studentId);
            if (student == null) {
                return Result.error("学生不存在");
            }

            // 验证学生是否参加了这次考试
            StudentScore studentScore = studentScoreService.getByExamAndStudent(examId, studentId);
            if (studentScore == null) {
                return Result.error("未找到该学生的考试记录");
            }

            // 获取考试-学生记录
            ExamStudent examStudent = examStudentService.getByExamIdAndStudentId(examId, studentId);
            if (examStudent == null) {
                return Result.error("未找到该学生的考试记录");
            }

            // 更新评语
            examStudent.setTeacherComment(comment);
            int result = examStudentMapper.update(examStudent);

            if (result <= 0) {
                return Result.error("更新评语失败");
            }

            // 记录操作日志
            Log operationLog = new Log();
            operationLog.setUserId(currentUser.getUserId());
            operationLog.setActionType(1); // UPDATE
            operationLog.setActionDescription("教师[" + currentUser.getUsername() + "]更新了学生[" + studentId + "]在考试[" + examId + "]中的评语");
            operationLog.setObjectType("EXAM_COMMENT");
            operationLog.setIpAddress(request.getRemoteAddr());
            operationLog.setDeviceInfo(request.getHeader("User-Agent"));
            operationLog.setStatus("SUCCESS");
            operationLog.setCreatedTime(new Date());
            logService.insert(operationLog);

            return Result.success();
        } catch (Exception e) {
            // 记录错误日志
            Log operationLog = new Log();
            operationLog.setUserId(currentUser.getUserId());
            operationLog.setActionType(5); // EXCEPTION
            operationLog.setActionDescription("教师[" + currentUser.getUsername() + "]更新学生评语异常：" + e.getMessage());
            operationLog.setObjectType("EXAM_COMMENT");
            operationLog.setIpAddress(request.getRemoteAddr());
            operationLog.setDeviceInfo(request.getHeader("User-Agent"));
            operationLog.setStatus("ERROR");
            operationLog.setCreatedTime(new Date());
            logService.insert(operationLog);

            log.error("更新学生评语失败", e);
            return Result.error("更新学生评语失败：" + e.getMessage());
        }
    }



} 
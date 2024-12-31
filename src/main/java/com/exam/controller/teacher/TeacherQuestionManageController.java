package com.exam.controller.teacher;

import com.exam.entity.*;
import com.exam.mapper.QuestionBankMapper;
import com.exam.service.*;
import com.exam.service.impl.CollegeServiceImpl;
import com.exam.service.impl.QuestionOptionServiceImpl;
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

@Slf4j
@RestController
@RequestMapping("/teacher/questions")
@Api(tags = "教师题库管理接口")
public class TeacherQuestionManageController {

    @Autowired
    private QuestionService questionService;

    @Autowired
    private QuestionBankService questionBankService;

    @Autowired
    private QuestionOptionService questionOptionService;

    @Autowired
    private LogService logService;

    @Autowired
    private SubjectService subjectService;

    @Autowired
    private CollegeService collegeService;

    @Autowired
    private QuestionBankMapper questionBankMapper;

    @Autowired
    private StudentQuestionScoreService studentQuestionScoreService;
    @Autowired
    private CollegeServiceImpl collegeServiceImpl;

    @ApiOperation("获取题目列表")
    @GetMapping
    public Result getQuestionList(@RequestParam(required = false) String keyword,
                                @RequestParam(required = false) Integer bankId,
                                @RequestParam(required = false) Integer type,
                                @RequestParam(required = false) BigDecimal minDifficulty,
                                @RequestParam(required = false) BigDecimal maxDifficulty,
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
                condition.put("content", keyword);
            }
            if (bankId != null) {
                condition.put("qbId", bankId);
            }
            if (type != null) {
                condition.put("type", type);
            }
            if (minDifficulty != null) {
                condition.put("minDifficulty", minDifficulty);
            }
            if (maxDifficulty != null) {
                condition.put("maxDifficulty", maxDifficulty);
            }

            List<Question> questions = questionService.getPageByCondition(condition, pageNum, pageSize);
            Long total = questionService.getCountByCondition(condition);

            for (Question question : questions) {
                // 查询题目所属题库
                QuestionBank bank = questionBankService.getById(question.getQbId());
                if (bank != null) {
                    Subject subject = subjectService.getById(bank.getSubjectId());
                    subject.setCollege(collegeService.getById(subject.getCollegeId()));
                    bank.setSubject(subject);
                }
                question.setQuestionBank(bank);
                List<QuestionOption> options = questionOptionService.getByQuestionId(question.getQuestionId());
                for (QuestionOption option: options) {
                    option.setQuestion(question);
                }

                String typeName = "";
                switch (question.getType()) {
                    case 0:
                        typeName = "单选题";
                        break;
                    case 1:
                        typeName = "多选题";
                        break;
                    case 2:
                        typeName = "判断题";
                        break;
                    case 3:
                        typeName = "填空题";
                        break;
                    case 4:
                        typeName = "简答题";
                        break;
                    default:
                        typeName = "未知";
                }
                question.setTypeName(typeName);
            }


            Map<String, Object> data = new HashMap<>();
            data.put("list", questions);
            data.put("total", total);

            // 记录操作日志
            Log operationLog = new Log();
            operationLog.setUserId(currentUser.getUserId());
            operationLog.setActionType(7); // SELECT
            operationLog.setActionDescription("教师[" + currentUser.getUsername() + "]查询了题目列表");
            operationLog.setObjectType("QUESTION");
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
            operationLog.setActionDescription("教师[" + currentUser.getUsername() + "]查询题目列表异常：" + e.getMessage());
            operationLog.setObjectType("QUESTION");
            operationLog.setIpAddress(request.getRemoteAddr());
            operationLog.setDeviceInfo(request.getHeader("User-Agent"));
            operationLog.setStatus("ERROR");
            operationLog.setCreatedTime(new Date());
            logService.insert(operationLog);

            log.error("获取题目列表失败", e);
            return Result.error("获取题目列表失败：" + e.getMessage());
        }
    }

    @ApiOperation("添加题目")
    @PostMapping
    public Result addQuestion(@RequestBody Question question, HttpServletRequest request) {
        User currentUser = TokenUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.error("用户未登录");
        }

        try {
            // 验证难度值
            if (question.getDifficulty() == null) {
                return Result.error("题目难度不能为空");
            }
            if (question.getDifficulty().compareTo(BigDecimal.ZERO) < 0 || 
                question.getDifficulty().compareTo(new BigDecimal("1")) > 0) {
                return Result.error("题目难度必须在0-1之间");
            }

            int result = questionService.insert(question);
            if (result <= 0) {
                return Result.error("添加题目失败");
            }

            // 记录操作日志
            Log operationLog = new Log();
            operationLog.setUserId(currentUser.getUserId());
            operationLog.setActionType(0); // INSERT
            operationLog.setActionDescription("教师[" + currentUser.getUsername() + "]添加了新题目");
            operationLog.setObjectType("QUESTION");
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
            operationLog.setActionDescription("教师[" + currentUser.getUsername() + "]添加题目异常：" + e.getMessage());
            operationLog.setObjectType("QUESTION");
            operationLog.setIpAddress(request.getRemoteAddr());
            operationLog.setDeviceInfo(request.getHeader("User-Agent"));
            operationLog.setStatus("ERROR");
            operationLog.setCreatedTime(new Date());
            logService.insert(operationLog);

            log.error("添加题目失败", e);
            return Result.error("添加题目失败：" + e.getMessage());
        }
    }

    @ApiOperation("更新题目")
    @PutMapping("/{questionId}")
    public Result updateQuestion(@PathVariable Integer questionId, 
                               @RequestBody Question question,
                               HttpServletRequest request) {
        User currentUser = TokenUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.error("用户未登录");
        }

        try {
            // 验证难度值
            if (question.getDifficulty() != null) {
                if (question.getDifficulty().compareTo(BigDecimal.ZERO) < 0 || 
                    question.getDifficulty().compareTo(new BigDecimal("1")) > 0) {
                    return Result.error("题目难度必须在0-1之间");
                }
            }

            question.setQuestionId(questionId);
            int result = questionService.updateById(question);

            Question question1 = questionService.getById(questionId);
            List<QuestionOption> options1 = question1.getOptions();
            List<QuestionOption> options = question.getOptions();


            for (QuestionOption option : options) {
                int result1 = questionService.updateOption(option.getOptionId(), option);
                if (result1 <= 0) {
                    return Result.error("修改题目选项失败");
                }
            }

            //对比两个选项列表，删除多余的选项
            for (QuestionOption option : options1) {
                boolean flag = false;
                for (QuestionOption option1 : options) {
                    if (option.getOptionId().equals(option1.getOptionId())) {
                        flag = true;
                        break;
                    }
                }
                if (!flag) {
                    int result1 = questionService.deleteOption(option.getOptionId());
                    if (result1 <= 0) {
                        return Result.error("删除题目选项失败");
                    }
                }
            }

            if (result <= 0) {
                return Result.error("更新题目失败");
            }

            // 记录操作日志
            Log operationLog = new Log();
            operationLog.setUserId(currentUser.getUserId());
            operationLog.setActionType(1); // UPDATE
            operationLog.setActionDescription("教师[" + currentUser.getUsername() + "]更新了题目，题目ID：" + questionId);
            operationLog.setObjectType("QUESTION");
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
            operationLog.setActionDescription("教师[" + currentUser.getUsername() + "]更新题目异常，题目ID：" + questionId + "，错误：" + e.getMessage());
            operationLog.setObjectType("QUESTION");
            operationLog.setIpAddress(request.getRemoteAddr());
            operationLog.setDeviceInfo(request.getHeader("User-Agent"));
            operationLog.setStatus("ERROR");
            operationLog.setCreatedTime(new Date());
            logService.insert(operationLog);

            log.error("更新题目失败", e);
            return Result.error("更新题目失败：" + e.getMessage());
        }
    }


    @ApiOperation("获取题目详情")
    @GetMapping("/{questionId}")
    public Result getQuestionDetail(@PathVariable Integer questionId, HttpServletRequest request) {
        User currentUser = TokenUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.error("用户未登录");
        }

        try {
            Question question = questionService.getById(questionId);
            if (question == null) {
                return Result.error("题目不存在");
            }
            List<QuestionOption> options = questionOptionService.getByQuestionId(questionId);
            question.setOptions(options);

            QuestionBank bank = questionBankService.getById(question.getQbId());
            bank.setSubject(subjectService.getById(bank.getSubjectId()));
            question.setQuestionBank(bank);

            String typeName = "";
            switch (question.getType()) {
                case 0:
                    typeName = "单选题";
                    break;
                case 1:
                    typeName = "多选题";
                    break;
                case 2:
                    typeName = "判断题";
                    break;
                case 3:
                    typeName = "填空题";
                    break;
                case 4:
                    typeName = "简答题";
                    break;
                default:
                    typeName = "未知";
            }
            question.setTypeName(typeName);

            // 记录操作日志
            Log operationLog = new Log();
            operationLog.setUserId(currentUser.getUserId());
            operationLog.setActionType(7); // SELECT
            operationLog.setActionDescription("教师[" + currentUser.getUsername() + "]查看了题目详情，题目ID：" + questionId);
            operationLog.setObjectType("QUESTION");
            operationLog.setIpAddress(request.getRemoteAddr());
            operationLog.setDeviceInfo(request.getHeader("User-Agent"));
            operationLog.setStatus("SUCCESS");
            operationLog.setCreatedTime(new Date());
            logService.insert(operationLog);

            return Result.success(question);
        } catch (Exception e) {
            // 记录异常日志
            Log operationLog = new Log();
            operationLog.setUserId(currentUser.getUserId());
            operationLog.setActionType(5); // EXCEPTION
            operationLog.setActionDescription("教师[" + currentUser.getUsername() + "]查看题目详情异常，题目ID：" + questionId + "，错误：" + e.getMessage());
            operationLog.setObjectType("QUESTION");
            operationLog.setIpAddress(request.getRemoteAddr());
            operationLog.setDeviceInfo(request.getHeader("User-Agent"));
            operationLog.setStatus("ERROR");
            operationLog.setCreatedTime(new Date());
            logService.insert(operationLog);

            log.error("获取题目详情失败", e);
            return Result.error("获取题目详情失败：" + e.getMessage());
        }
    }

    @ApiOperation("添加题目选项")
    @PostMapping("/{questionId}/options")
    public Result addQuestionOption(@PathVariable Integer questionId,
                                  @RequestBody QuestionOption option,
                                  HttpServletRequest request) {
        User currentUser = TokenUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.error("用户未登录");
        }

        try {
            int result = questionService.addOption(questionId, option);
            if (result <= 0) {
                return Result.error("添加题目选项失败");
            }

            // 记录操作日志
            Log operationLog = new Log();
            operationLog.setUserId(currentUser.getUserId());
            operationLog.setActionType(0); // INSERT
            operationLog.setActionDescription("教师[" + currentUser.getUsername() + "]为题目添加了选项，题目ID：" + questionId);
            operationLog.setObjectType("QUESTION_OPTION");
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
            operationLog.setActionDescription("教师[" + currentUser.getUsername() + "]添加题目选项异常，题目ID：" + questionId + "，错误：" + e.getMessage());
            operationLog.setObjectType("QUESTION_OPTION");
            operationLog.setIpAddress(request.getRemoteAddr());
            operationLog.setDeviceInfo(request.getHeader("User-Agent"));
            operationLog.setStatus("ERROR");
            operationLog.setCreatedTime(new Date());
            logService.insert(operationLog);

            log.error("添加题目选项失败", e);
            return Result.error("添加题目选项失败：" + e.getMessage());
        }
    }

    @ApiOperation("获取题目统计信息")
    @GetMapping("/stats")
    public Result getQuestionStats(HttpServletRequest request) {
        User currentUser = TokenUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.error("用户未登录");
        }

        try {
            Map<String, Object> stats = new HashMap<>();
            stats.put("typeDistribution", questionService.countByType());
            stats.put("difficultyDistribution", questionService.countByDifficulty());
            stats.put("mostMistakes", questionService.getMostMistakes(10));

            // 记录操作日志
            Log operationLog = new Log();
            operationLog.setUserId(currentUser.getUserId());
            operationLog.setActionType(7); // SELECT
            operationLog.setActionDescription("教师[" + currentUser.getUsername() + "]查看了题目统计信息");
            operationLog.setObjectType("QUESTION_STATS");
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
            operationLog.setActionDescription("教师[" + currentUser.getUsername() + "]查看题目统计信息异常：" + e.getMessage());
            operationLog.setObjectType("QUESTION_STATS");
            operationLog.setIpAddress(request.getRemoteAddr());
            operationLog.setDeviceInfo(request.getHeader("User-Agent"));
            operationLog.setStatus("ERROR");
            operationLog.setCreatedTime(new Date());
            logService.insert(operationLog);

            log.error("获取题目统计信息失败", e);
            return Result.error("获取题目统计信息失败：" + e.getMessage());
        }
    }

    @ApiOperation("获取题目所有选项")
    @GetMapping("/{questionId}/options")
    public Result getQuestionOptions(@PathVariable Integer questionId, HttpServletRequest request) {
        User currentUser = TokenUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.error("用户未登录");
        }

        try {
            List<QuestionOption> options = questionService.getOptions(questionId);
            if (options == null) {
                return Result.error("获取题目选项失败");
            }

            // 记录操作日志
            Log operationLog = new Log();
            operationLog.setUserId(currentUser.getUserId());
            operationLog.setActionType(7); // SELECT
            operationLog.setActionDescription("教师[" + currentUser.getUsername() + "]查看了题目选项，题目ID：" + questionId);
            operationLog.setObjectType("QUESTION_OPTION");
            operationLog.setIpAddress(request.getRemoteAddr());
            operationLog.setDeviceInfo(request.getHeader("User-Agent"));
            operationLog.setStatus("SUCCESS");
            operationLog.setCreatedTime(new Date());
            logService.insert(operationLog);

            return Result.success(options);
        } catch (Exception e) {
            // 记录异常日志
            Log operationLog = new Log();
            operationLog.setUserId(currentUser.getUserId());
            operationLog.setActionType(5); // EXCEPTION
            operationLog.setActionDescription("教师[" + currentUser.getUsername() + "]查看题目选项异常，题目ID：" + questionId + "，错误：" + e.getMessage());
            operationLog.setObjectType("QUESTION_OPTION");
            operationLog.setIpAddress(request.getRemoteAddr());
            operationLog.setDeviceInfo(request.getHeader("User-Agent"));
            operationLog.setStatus("ERROR");
            operationLog.setCreatedTime(new Date());
            logService.insert(operationLog);

            log.error("获取题目选项失败", e);
            return Result.error("获取题目选项失败：" + e.getMessage());
        }
    }

    @ApiOperation("修改题目选项")
    @PutMapping("/options/{optionId}")
    public Result updateQuestionOption(@PathVariable Integer optionId,
                                     @RequestBody QuestionOption option,
                                     HttpServletRequest request) {
        User currentUser = TokenUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.error("用户未登录");
        }

        try {
            option.setOptionId(optionId);
            int result = questionService.updateOption(optionId, option);
            if (result <= 0) {
                return Result.error("修改题目选项失败");
            }

            // 记录操作日志
            Log operationLog = new Log();
            operationLog.setUserId(currentUser.getUserId());
            operationLog.setActionType(1); // UPDATE
            operationLog.setActionDescription("教师[" + currentUser.getUsername() + "]修改了题目["+option.getQuestionId() +"[的选项，选项ID：" + optionId);
            operationLog.setObjectType("QUESTION_OPTION");
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
            operationLog.setActionDescription("教师[" + currentUser.getUsername() + "]修改题目["+option.getQuestionId() +"[选项异常，选项ID：" + optionId + "，错误：" + e.getMessage());
            operationLog.setObjectType("QUESTION_OPTION");
            operationLog.setIpAddress(request.getRemoteAddr());
            operationLog.setDeviceInfo(request.getHeader("User-Agent"));
            operationLog.setStatus("ERROR");
            operationLog.setCreatedTime(new Date());
            logService.insert(operationLog);

            log.error("修改题目选项失败", e);
            return Result.error("修改题目选项失败：" + e.getMessage());
        }
    }

    @ApiOperation("删除题目选项")
    @DeleteMapping("/options/{optionId}")
    public Result deleteQuestionOption(@PathVariable Integer optionId, HttpServletRequest request) {
        User currentUser = TokenUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.error("用户未登录");
        }

        QuestionOption option = questionOptionService.getById(optionId);

        try {

            int result = questionService.deleteOption(optionId);
            if (result <= 0) {
                return Result.error("删除题目选项失败");
            }

            // 记录操作日志
            Log operationLog = new Log();
            operationLog.setUserId(currentUser.getUserId());
            operationLog.setActionType(2); // DELETE
            operationLog.setActionDescription("教师[" + currentUser.getUsername() + "]删除了题目["+option.getQuestionId() +"[的选项，选项ID：" + optionId);
            operationLog.setObjectType("QUESTION_OPTION");
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
            operationLog.setActionDescription("教师[" + currentUser.getUsername() + "]删除题目["+option.getQuestionId() +"[选项异常，选项ID：" + optionId + "，错误：" + e.getMessage());
            operationLog.setObjectType("QUESTION_OPTION");
            operationLog.setIpAddress(request.getRemoteAddr());
            operationLog.setDeviceInfo(request.getHeader("User-Agent"));
            operationLog.setStatus("ERROR");
            operationLog.setCreatedTime(new Date());
            logService.insert(operationLog);

            log.error("删除题目选项失败", e);
            return Result.error("删除题目选项失败：" + e.getMessage());
        }
    }

    @ApiOperation("根据选项内容模糊查询")
    @GetMapping("/options/search")
    public Result searchOptionsByContent(@RequestParam String content, HttpServletRequest request) {
        User currentUser = TokenUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.error("用户未登录");
        }

        try {
            List<QuestionOption> options = questionOptionService.getByContent(content);
            if (options == null) {
                return Result.error("查询题目选项失败");
            }

            // 记录操作日志
            Log operationLog = new Log();
            operationLog.setUserId(currentUser.getUserId());
            operationLog.setActionType(7); // SELECT
            operationLog.setActionDescription("教师[" + currentUser.getUsername() + "]根据内容搜索题目选项，关键词：" + content);
            operationLog.setObjectType("QUESTION_OPTION");
            operationLog.setIpAddress(request.getRemoteAddr());
            operationLog.setDeviceInfo(request.getHeader("User-Agent"));
            operationLog.setStatus("SUCCESS");
            operationLog.setCreatedTime(new Date());
            logService.insert(operationLog);

            return Result.success(options);
        } catch (Exception e) {
            // 记录异常日志
            Log operationLog = new Log();
            operationLog.setUserId(currentUser.getUserId());
            operationLog.setActionType(5); // EXCEPTION
            operationLog.setActionDescription("教师[" + currentUser.getUsername() + "]搜索题目选项异常，关键词：" + content + "，错误：" + e.getMessage());
            operationLog.setObjectType("QUESTION_OPTION");
            operationLog.setIpAddress(request.getRemoteAddr());
            operationLog.setDeviceInfo(request.getHeader("User-Agent"));
            operationLog.setStatus("ERROR");
            operationLog.setCreatedTime(new Date());
            logService.insert(operationLog);

            log.error("搜索题目选项失败", e);
            return Result.error("搜索题目选项失败：" + e.getMessage());
        }
    }

    @ApiOperation("创建新题库")
    @PostMapping("/banks")
    public Result createQuestionBank(@RequestBody QuestionBank questionBank, HttpServletRequest request) {
        User currentUser = TokenUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.error("用户未登录");
        }

        try {
            // 检查题库名称是否已存在
            QuestionBank existingBank = questionBankService.getByName(questionBank.getQbName());
            if (existingBank != null) {
                return Result.error("题库名称已存在");
            }

            int result = questionBankService.insert(questionBank);
            if (result <= 0) {
                return Result.error("创建题库失败");
            }

            // 记录操作日志
            Log operationLog = new Log();
            operationLog.setUserId(currentUser.getUserId());
            operationLog.setActionType(0); // INSERT
            operationLog.setActionDescription("教师[" + currentUser.getUsername() + "]创建了新题库：" + questionBank.getQbName());
            operationLog.setObjectType("QUESTION_BANK");
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
            operationLog.setActionDescription("教师[" + currentUser.getUsername() + "]创建题库异常：" + e.getMessage());
            operationLog.setObjectType("QUESTION_BANK");
            operationLog.setIpAddress(request.getRemoteAddr());
            operationLog.setDeviceInfo(request.getHeader("User-Agent"));
            operationLog.setStatus("ERROR");
            operationLog.setCreatedTime(new Date());
            logService.insert(operationLog);

            log.error("创建题库失败", e);
            return Result.error("创建题库失败：" + e.getMessage());
        }
    }

    @ApiOperation("获取题库统计信息")
    @GetMapping("/banks/{bankId}/stats")
    public Result getQuestionBankStats(@PathVariable Integer bankId, HttpServletRequest request) {
        User currentUser = TokenUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.error("用户未登录");
        }

        try {
            Map<String, Object> stats = new HashMap<>();
            // 获取题目总数
            Integer totalQuestions = questionBankService.countQuestions(bankId);
            // 获取各类型题目数量
            List<Map<String, Object>> typeStats = questionBankService.countQuestionsByType(bankId);
            // 获取各难度题目数量
            List<Map<String, Object>> difficultyStats = questionBankService.countQuestionsByDifficulty(bankId);

            stats.put("totalQuestions", totalQuestions);
            stats.put("typeDistribution", typeStats);
            stats.put("difficultyDistribution", difficultyStats);

            // 记录操作日志
            Log operationLog = new Log();
            operationLog.setUserId(currentUser.getUserId());
            operationLog.setActionType(7); // SELECT
            operationLog.setActionDescription("教师[" + currentUser.getUsername() + "]查看了题库统计信息，题库ID：" + bankId);
            operationLog.setObjectType("QUESTION_BANK");
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
            operationLog.setActionDescription("教师[" + currentUser.getUsername() + "]查看题库统计信息异常，题库ID：" + bankId + "，错误：" + e.getMessage());
            operationLog.setObjectType("QUESTION_BANK");
            operationLog.setIpAddress(request.getRemoteAddr());
            operationLog.setDeviceInfo(request.getHeader("User-Agent"));
            operationLog.setStatus("ERROR");
            operationLog.setCreatedTime(new Date());
            logService.insert(operationLog);

            log.error("获取题库统计信息失败", e);
            return Result.error("获取题库统计信息失败：" + e.getMessage());
        }
    }

    @ApiOperation("更新题库信息")
    @PutMapping("/banks/{bankId}")
    public Result updateQuestionBank(@PathVariable Integer bankId, 
                                   @RequestBody QuestionBank questionBank,
                                   HttpServletRequest request) {
        User currentUser = TokenUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.error("用户未登录");
        }

        try {
            // 检查题库是否存在
            QuestionBank existingBank = questionBankService.getById(bankId);
            if (existingBank == null) {
                return Result.error("题库不存在");
            }

            // 如果更新了题库名称，检查新名称是否已存在
            if (!existingBank.getQbName().equals(questionBank.getQbName())) {
                QuestionBank bankWithSameName = questionBankService.getByName(questionBank.getQbName());
                if (bankWithSameName != null) {
                    return Result.error("题库名称已存在");
                }
            }

            questionBank.setQbId(bankId);
            int result = questionBankService.update(questionBank);
            if (result <= 0) {
                return Result.error("更新题库失败");
            }

            // 记录操作日志
            Log operationLog = new Log();
            operationLog.setUserId(currentUser.getUserId());
            operationLog.setActionType(1); // UPDATE
            operationLog.setActionDescription("教师[" + currentUser.getUsername() + "]更新了题库信息，题库ID：" + bankId);
            operationLog.setObjectType("QUESTION_BANK");
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
            operationLog.setActionDescription("教师[" + currentUser.getUsername() + "]更新题库信息异常，题库ID：" + bankId + "，错误：" + e.getMessage());
            operationLog.setObjectType("QUESTION_BANK");
            operationLog.setIpAddress(request.getRemoteAddr());
            operationLog.setDeviceInfo(request.getHeader("User-Agent"));
            operationLog.setStatus("ERROR");
            operationLog.setCreatedTime(new Date());
            logService.insert(operationLog);

            log.error("更新题库失败", e);
            return Result.error("更新题库失败：" + e.getMessage());
        }
    }

    @ApiOperation("查询题库列表")
    @GetMapping("/banks")
    public Result getQuestionBanks(@RequestParam(required = false) String keyword,
                                 @RequestParam(required = false) Integer subjectId,
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

            List<QuestionBank> banks = questionBankService.getPageByCondition(condition, pageNum, pageSize);
            for (QuestionBank bank : banks) {
                Subject subject = subjectService.getById(bank.getSubjectId());
                subject.setCollege(collegeService.getById(subject.getCollegeId()));
                bank.setSubject(subject);
            }
            Long total = questionBankMapper.selectCountByCondition(condition);

            Map<String, Object> data = new HashMap<>();
            data.put("list", banks);
            data.put("total", total);

            // 记录操作日志
            Log operationLog = new Log();
            operationLog.setUserId(currentUser.getUserId());
            operationLog.setActionType(7); // SELECT
            operationLog.setActionDescription("教师[" + currentUser.getUsername() + "]查询了题库列表");
            operationLog.setObjectType("QUESTION_BANK");
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
            operationLog.setActionDescription("教师[" + currentUser.getUsername() + "]查询题库列表异常：" + e.getMessage());
            operationLog.setObjectType("QUESTION_BANK");
            operationLog.setIpAddress(request.getRemoteAddr());
            operationLog.setDeviceInfo(request.getHeader("User-Agent"));
            operationLog.setStatus("ERROR");
            operationLog.setCreatedTime(new Date());
            logService.insert(operationLog);

            log.error("查询题库列表失败", e);
            return Result.error("查询题库列表失败：" + e.getMessage());
        }
    }

    @ApiOperation("获取题库详情")
    @GetMapping("/banks/{bankId}")
    public Result getQuestionBankDetail(@PathVariable Integer bankId, HttpServletRequest request) {
        User currentUser = TokenUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.error("用户未登录");
        }


        try {
            QuestionBank bank = questionBankService.getById(bankId);
            if (bank == null) {
                return Result.error("题库不存在");
            }

            Subject subject = subjectService.getById(bank.getSubjectId());
            subject.setCollege(collegeService.getById(subject.getCollegeId()));
            bank.setSubject(subject);
            bank.setQuestions(questionBankService.getQuestions(bankId));

            // 记录操作日志
            Log operationLog = new Log();
            operationLog.setUserId(currentUser.getUserId());
            operationLog.setActionType(7); // SELECT
            operationLog.setActionDescription("教师[" + currentUser.getUsername() + "]查看了题库详情，题库ID：" + bankId);
            operationLog.setObjectType("QUESTION_BANK");
            operationLog.setIpAddress(request.getRemoteAddr());
            operationLog.setDeviceInfo(request.getHeader("User-Agent"));
            operationLog.setStatus("SUCCESS");
            operationLog.setCreatedTime(new Date());
            logService.insert(operationLog);

            return Result.success(bank);
        } catch (Exception e) {
            // 记录异常日志
            Log operationLog = new Log();
            operationLog.setUserId(currentUser.getUserId());
            operationLog.setActionType(5); // EXCEPTION
            operationLog.setActionDescription("教师[" + currentUser.getUsername() + "]查看题库详情异常，题库ID：" + bankId + "，错误：" + e.getMessage());
            operationLog.setObjectType("QUESTION_BANK");
            operationLog.setIpAddress(request.getRemoteAddr());
            operationLog.setDeviceInfo(request.getHeader("User-Agent"));
            operationLog.setStatus("ERROR");
            operationLog.setCreatedTime(new Date());
            logService.insert(operationLog);

            log.error("获取题库详情失败", e);
            return Result.error("获取题库详情失败：" + e.getMessage());
        }
    }

} 
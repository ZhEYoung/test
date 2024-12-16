package com.exam.controller.admin;

import com.exam.entity.dto.SubjectDTO;
import com.exam.entity.Subject;
import com.exam.service.SubjectService;
import com.exam.service.LogService;
import com.exam.entity.User;
import com.exam.entity.Log;
import com.exam.utils.TokenUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import com.exam.common.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Date;
import org.springframework.beans.BeanUtils;

@Slf4j
@RestController
@RequestMapping("/admin/subjects")
@Api(tags = "学科管理接口")
public class SubjectController {

    @Autowired
    private SubjectService subjectService;

    @Autowired
    private LogService logService;

    @ApiOperation("获取学科列表")
    @GetMapping
    public Result getSubjectList(@RequestParam(required = false) String keyword,
                                @RequestParam(required = false) Integer collegeId,
                                @RequestParam(defaultValue = "1") Integer pageNum,
                                @RequestParam(defaultValue = "10") Integer pageSize,
                                HttpServletRequest request) {
        // 获取当前操作的用户
        User currentUser = null;
        try {
            currentUser = TokenUtils.getCurrentUser();
            if (currentUser == null) {
                return Result.error("用户未登录");
            }

            Map<String, Object> condition = new HashMap<>();
            if (keyword != null && !keyword.trim().isEmpty()) {
                condition.put("subjectName", keyword);
            }
            if (collegeId != null) {
                condition.put("collegeId", collegeId);
            }
            
            List<Subject> subjects = subjectService.getPageByCondition(condition, pageNum, pageSize);
            Long total = subjectService.getCountByCondition(condition);
            
            // 转换为DTO
            List<SubjectDTO> subjectDTOs = subjects.stream()
                .map(subject -> {
                    SubjectDTO dto = new SubjectDTO();
                    BeanUtils.copyProperties(subject, dto);
                    return dto;
                })
                .collect(java.util.stream.Collectors.toList());
            
            Map<String, Object> data = new HashMap<>();
            data.put("list", subjectDTOs);
            data.put("total", total);

            // 记录查询日志
            Log operationLog = new Log();
            operationLog.setUserId(currentUser.getUserId());
            operationLog.setActionType(7); // SELECT
            operationLog.setActionDescription("用户[" + currentUser.getUsername() + "]查询了学科列表");
            operationLog.setObjectType("SUBJECT");
            operationLog.setIpAddress(request.getRemoteAddr());
            operationLog.setDeviceInfo(request.getHeader("User-Agent"));
            operationLog.setStatus("SUCCESS");
            operationLog.setCreatedTime(new Date());
            logService.insert(operationLog);
            
            return Result.success(data);
        } catch (Exception e) {
            // 记录异常日志
            Log operationLog = new Log();
            if (currentUser != null) {
                operationLog.setUserId(currentUser.getUserId());
                operationLog.setActionDescription("用户[" + currentUser.getUsername() + "]查询学科列表异常：" + e.getMessage());
            } else {
                operationLog.setActionDescription("未登录用户查询学科列表异常：" + e.getMessage());
            }
            operationLog.setActionType(5); // EXCEPTION
            operationLog.setObjectType("SUBJECT");
            operationLog.setIpAddress(request.getRemoteAddr());
            operationLog.setDeviceInfo(request.getHeader("User-Agent"));
            operationLog.setStatus("ERROR");
            operationLog.setCreatedTime(new Date());
            logService.insert(operationLog);
            
            log.error("获取学科列表失败", e);
            return Result.error("获取学科列表失败: " + e.getMessage());
        }
    }

    @ApiOperation("添加学科")
    @PostMapping
    public Result addSubject(@RequestBody SubjectDTO subjectDTO, HttpServletRequest request) {
        User currentUser = null;
        try {
            currentUser = TokenUtils.getCurrentUser();
            if (currentUser == null) {
                return Result.error("用户未登录");
            }

            // 检查学科名称是否已存在
            Subject existingSubject = subjectService.getBySubjectName(subjectDTO.getSubjectName());
            if (existingSubject != null) {
                // 记录失败日志
                Log operationLog = new Log();
                operationLog.setUserId(currentUser.getUserId());
                operationLog.setActionType(0); // INSERT
                operationLog.setActionDescription("用户[" + currentUser.getUsername() + "]添加学科[" + subjectDTO.getSubjectName() + "]失败：学科名称已存在");
                operationLog.setObjectType("SUBJECT");
                operationLog.setIpAddress(request.getRemoteAddr());
                operationLog.setDeviceInfo(request.getHeader("User-Agent"));
                operationLog.setStatus("FAILED");
                operationLog.setCreatedTime(new Date());
                logService.insert(operationLog);
                
                return Result.error("学科名称已存在");
            }

            // DTO转换为实体
            Subject subject = new Subject();
            BeanUtils.copyProperties(subjectDTO, subject);

            int result = subjectService.insert(subject);
            if (result > 0) {
                // 记录成功日志
                Log operationLog = new Log();
                operationLog.setUserId(currentUser.getUserId());
                operationLog.setActionType(0); // INSERT
                operationLog.setActionDescription("用户[" + currentUser.getUsername() + "]添加了学科[" + subject.getSubjectName() + "]");
                operationLog.setObjectType("SUBJECT");
                operationLog.setIpAddress(request.getRemoteAddr());
                operationLog.setDeviceInfo(request.getHeader("User-Agent"));
                operationLog.setStatus("SUCCESS");
                operationLog.setCreatedTime(new Date());
                logService.insert(operationLog);
                
                return Result.success("添加学科成功");
            }
            
            // 记录失败日志
            Log operationLog = new Log();
            operationLog.setUserId(currentUser.getUserId());
            operationLog.setActionType(0); // INSERT
            operationLog.setActionDescription("用户[" + currentUser.getUsername() + "]添加学科[" + subject.getSubjectName() + "]失败");
            operationLog.setObjectType("SUBJECT");
            operationLog.setIpAddress(request.getRemoteAddr());
            operationLog.setDeviceInfo(request.getHeader("User-Agent"));
            operationLog.setStatus("FAILED");
            operationLog.setCreatedTime(new Date());
            logService.insert(operationLog);
            
            return Result.error("添加学科失败");
        } catch (Exception e) {
            // 记录异常日志
            Log operationLog = new Log();
            if (currentUser != null) {
                operationLog.setUserId(currentUser.getUserId());
                operationLog.setActionDescription("用户[" + currentUser.getUsername() + "]添加学科异常：" + e.getMessage());
            } else {
                operationLog.setActionDescription("未登录用户添加学科异常：" + e.getMessage());
            }
            operationLog.setActionType(5); // EXCEPTION
            operationLog.setObjectType("SUBJECT");
            operationLog.setIpAddress(request.getRemoteAddr());
            operationLog.setDeviceInfo(request.getHeader("User-Agent"));
            operationLog.setStatus("ERROR");
            operationLog.setCreatedTime(new Date());
            logService.insert(operationLog);
            
            log.error("添加学科失败", e);
            return Result.error("添加学科失败: " + e.getMessage());
        }
    }

    @ApiOperation("更新学科信息")
    @PutMapping("/{subjectId}")
    public Result updateSubject(@PathVariable Integer subjectId, @RequestBody SubjectDTO subjectDTO, HttpServletRequest request) {
        User currentUser = null;
        try {
            currentUser = TokenUtils.getCurrentUser();
            if (currentUser == null) {
                return Result.error("用户未登录");
            }

            // 检查学科是否存在
            Subject existingSubject = subjectService.getById(subjectId);
            if (existingSubject == null) {
                // 记录失败日志
                Log operationLog = new Log();
                operationLog.setUserId(currentUser.getUserId());
                operationLog.setActionType(1); // UPDATE
                operationLog.setActionDescription("用户[" + currentUser.getUsername() + "]更新学科信息失败：学科不存在");
                operationLog.setObjectType("SUBJECT");
                operationLog.setIpAddress(request.getRemoteAddr());
                operationLog.setDeviceInfo(request.getHeader("User-Agent"));
                operationLog.setStatus("FAILED");
                operationLog.setCreatedTime(new Date());
                logService.insert(operationLog);
                
                return Result.error("学科不存在");
            }

            // 如果修改了学科名称，检查新名称是否已存在
            if (!existingSubject.getSubjectName().equals(subjectDTO.getSubjectName())) {
                Subject subjectWithSameName = subjectService.getBySubjectName(subjectDTO.getSubjectName());
                if (subjectWithSameName != null) {
                    // 记录失败日志
                    Log operationLog = new Log();
                    operationLog.setUserId(currentUser.getUserId());
                    operationLog.setActionType(1); // UPDATE
                    operationLog.setActionDescription("用户[" + currentUser.getUsername() + "]更新学科[" + existingSubject.getSubjectName() + 
                        "]信息失败：新学科名称[" + subjectDTO.getSubjectName() + "]已存在");
                    operationLog.setObjectType("SUBJECT");
                    operationLog.setIpAddress(request.getRemoteAddr());
                    operationLog.setDeviceInfo(request.getHeader("User-Agent"));
                    operationLog.setStatus("FAILED");
                    operationLog.setCreatedTime(new Date());
                    logService.insert(operationLog);
                    
                    return Result.error("学科名称已存在");
                }
            }

            // DTO转换为实体
            Subject subject = new Subject();
            BeanUtils.copyProperties(subjectDTO, subject);
            subject.setSubjectId(subjectId);

            int result = subjectService.updateById(subject);
            if (result > 0) {
                // 记录成功日志
                Log operationLog = new Log();
                operationLog.setUserId(currentUser.getUserId());
                operationLog.setActionType(1); // UPDATE
                operationLog.setActionDescription("用户[" + currentUser.getUsername() + "]更新了学科[" + subject.getSubjectName() + "]的信息");
                operationLog.setObjectType("SUBJECT");
                operationLog.setIpAddress(request.getRemoteAddr());
                operationLog.setDeviceInfo(request.getHeader("User-Agent"));
                operationLog.setStatus("SUCCESS");
                operationLog.setCreatedTime(new Date());
                logService.insert(operationLog);
                
                return Result.success("更新学科信息成功");
            }
            
            // 记录失败日志
            Log operationLog = new Log();
            operationLog.setUserId(currentUser.getUserId());
            operationLog.setActionType(1); // UPDATE
            operationLog.setActionDescription("用户[" + currentUser.getUsername() + "]更新学科[" + subject.getSubjectName() + "]信息失败");
            operationLog.setObjectType("SUBJECT");
            operationLog.setIpAddress(request.getRemoteAddr());
            operationLog.setDeviceInfo(request.getHeader("User-Agent"));
            operationLog.setStatus("FAILED");
            operationLog.setCreatedTime(new Date());
            logService.insert(operationLog);
            
            return Result.error("更新学科信息失败");
        } catch (Exception e) {
            // 记录异常日志
            Log operationLog = new Log();
            if (currentUser != null) {
                operationLog.setUserId(currentUser.getUserId());
                operationLog.setActionDescription("用户[" + currentUser.getUsername() + "]更新学科信息异常：" + e.getMessage());
            } else {
                operationLog.setActionDescription("未登录用户更新学科信息异常：" + e.getMessage());
            }
            operationLog.setActionType(5); // EXCEPTION
            operationLog.setObjectType("SUBJECT");
            operationLog.setIpAddress(request.getRemoteAddr());
            operationLog.setDeviceInfo(request.getHeader("User-Agent"));
            operationLog.setStatus("ERROR");
            operationLog.setCreatedTime(new Date());
            logService.insert(operationLog);
            
            log.error("更新学科信息失败", e);
            return Result.error("更新学科信息失败: " + e.getMessage());
        }
    }

    @ApiOperation("获取学科详情")
    @GetMapping("/{subjectId}")
    public Result getSubjectDetail(@PathVariable Integer subjectId, HttpServletRequest request) {
        User currentUser = null;
        try {
            currentUser = TokenUtils.getCurrentUser();
            if (currentUser == null) {
                return Result.error("用户未登录");
            }

            Subject subject = subjectService.getById(subjectId);
            if (subject != null) {
                SubjectDTO subjectDTO = new SubjectDTO();
                BeanUtils.copyProperties(subject, subjectDTO);
                
                // 记录查询日志
                Log operationLog = new Log();
                operationLog.setUserId(currentUser.getUserId());
                operationLog.setActionType(7); // SELECT
                operationLog.setActionDescription("用户[" + currentUser.getUsername() + "]查看了学科[" + subject.getSubjectName() + "]的详细信息");
                operationLog.setObjectType("SUBJECT");
                operationLog.setIpAddress(request.getRemoteAddr());
                operationLog.setDeviceInfo(request.getHeader("User-Agent"));
                operationLog.setStatus("SUCCESS");
                operationLog.setCreatedTime(new Date());
                logService.insert(operationLog);
                
                return Result.success(subjectDTO);
            }
            
            // 记录失败日志
            Log operationLog = new Log();
            operationLog.setUserId(currentUser.getUserId());
            operationLog.setActionType(7); // SELECT
            operationLog.setActionDescription("用户[" + currentUser.getUsername() + "]查询学科详情失败：学科不存在");
            operationLog.setObjectType("SUBJECT");
            operationLog.setIpAddress(request.getRemoteAddr());
            operationLog.setDeviceInfo(request.getHeader("User-Agent"));
            operationLog.setStatus("FAILED");
            operationLog.setCreatedTime(new Date());
            logService.insert(operationLog);
            
            return Result.error("学科不存在");
        } catch (Exception e) {
            // 记录异常日志
            Log operationLog = new Log();
            if (currentUser != null) {
                operationLog.setUserId(currentUser.getUserId());
                operationLog.setActionDescription("用户[" + currentUser.getUsername() + "]查询学科详情异常：" + e.getMessage());
            } else {
                operationLog.setActionDescription("未登录用户查询学科详情异常：" + e.getMessage());
            }
            operationLog.setActionType(5); // EXCEPTION
            operationLog.setObjectType("SUBJECT");
            operationLog.setIpAddress(request.getRemoteAddr());
            operationLog.setDeviceInfo(request.getHeader("User-Agent"));
            operationLog.setStatus("ERROR");
            operationLog.setCreatedTime(new Date());
            logService.insert(operationLog);
            
            log.error("获取学科详情失败", e);
            return Result.error("获取学科详情失败: " + e.getMessage());
        }
    }

    @ApiOperation("获取学科统计信息")
    @GetMapping("/{subjectId}/statistics")
    public Result getSubjectStatistics(@PathVariable Integer subjectId, HttpServletRequest request) {
        User currentUser = null;
        try {
            currentUser = TokenUtils.getCurrentUser();
            if (currentUser == null) {
                return Result.error("用户未登录");
            }

            Map<String, Object> statistics = subjectService.getSubjectStatistics(subjectId);
            if (statistics != null) {
                // 记录成功日志
                Log operationLog = new Log();
                operationLog.setUserId(currentUser.getUserId());
                operationLog.setActionType(7); // SELECT
                operationLog.setActionDescription("用户[" + currentUser.getUsername() + "]查看了学科[ID=" + subjectId + "]的统计信息");
                operationLog.setObjectType("SUBJECT_STATISTICS");
                operationLog.setIpAddress(request.getRemoteAddr());
                operationLog.setDeviceInfo(request.getHeader("User-Agent"));
                operationLog.setStatus("SUCCESS");
                operationLog.setCreatedTime(new Date());
                logService.insert(operationLog);
                
                return Result.success(statistics);
            }
            
            // 记录失败日志
            Log operationLog = new Log();
            operationLog.setUserId(currentUser.getUserId());
            operationLog.setActionType(7); // SELECT
            operationLog.setActionDescription("用户[" + currentUser.getUsername() + "]查看学科统计信息失败：学科不存在");
            operationLog.setObjectType("SUBJECT_STATISTICS");
            operationLog.setIpAddress(request.getRemoteAddr());
            operationLog.setDeviceInfo(request.getHeader("User-Agent"));
            operationLog.setStatus("FAILED");
            operationLog.setCreatedTime(new Date());
            logService.insert(operationLog);
            
            return Result.error("获取学科统计信息失败");
        } catch (Exception e) {
            // 记录异常日志
            Log operationLog = new Log();
            if (currentUser != null) {
                operationLog.setUserId(currentUser.getUserId());
                operationLog.setActionDescription("用户[" + currentUser.getUsername() + "]查看学科统计信息异常：" + e.getMessage());
            } else {
                operationLog.setActionDescription("未登录用户查看学科统计信息异常：" + e.getMessage());
            }
            operationLog.setActionType(5); // EXCEPTION
            operationLog.setObjectType("SUBJECT_STATISTICS");
            operationLog.setIpAddress(request.getRemoteAddr());
            operationLog.setDeviceInfo(request.getHeader("User-Agent"));
            operationLog.setStatus("ERROR");
            operationLog.setCreatedTime(new Date());
            logService.insert(operationLog);
            
            log.error("获取学科统计信息失败", e);
            return Result.error("获取学科统计信息失败: " + e.getMessage());
        }
    }

    @ApiOperation("获取热门学科")
    @GetMapping("/hot")
    public Result getHotSubjects(@RequestParam(defaultValue = "10") Integer limit, HttpServletRequest request) {
        User currentUser = null;
        try {
            currentUser = TokenUtils.getCurrentUser();
            if (currentUser == null) {
                return Result.error("用户未登录");
            }

            List<Subject> subjects = subjectService.getHotSubjects(limit);
            List<SubjectDTO> subjectDTOs = subjects.stream()
                .map(subject -> {
                    SubjectDTO dto = new SubjectDTO();
                    BeanUtils.copyProperties(subject, dto);
                    return dto;
                })
                .collect(java.util.stream.Collectors.toList());

            // 记录成功日志
            Log operationLog = new Log();
            operationLog.setUserId(currentUser.getUserId());
            operationLog.setActionType(7); // SELECT
            operationLog.setActionDescription("用户[" + currentUser.getUsername() + "]查看了热门学科列表");
            operationLog.setObjectType("HOT_SUBJECTS");
            operationLog.setIpAddress(request.getRemoteAddr());
            operationLog.setDeviceInfo(request.getHeader("User-Agent"));
            operationLog.setStatus("SUCCESS");
            operationLog.setCreatedTime(new Date());
            logService.insert(operationLog);

            return Result.success(subjectDTOs);
        } catch (Exception e) {
            // 记录异常日志
            Log operationLog = new Log();
            if (currentUser != null) {
                operationLog.setUserId(currentUser.getUserId());
                operationLog.setActionDescription("用户[" + currentUser.getUsername() + "]查看热门学科列表异常：" + e.getMessage());
            } else {
                operationLog.setActionDescription("未登录用户查看热门学科列表异常：" + e.getMessage());
            }
            operationLog.setActionType(5); // EXCEPTION
            operationLog.setObjectType("HOT_SUBJECTS");
            operationLog.setIpAddress(request.getRemoteAddr());
            operationLog.setDeviceInfo(request.getHeader("User-Agent"));
            operationLog.setStatus("ERROR");
            operationLog.setCreatedTime(new Date());
            logService.insert(operationLog);
            
            log.error("获取热门学科失败", e);
            return Result.error("获取热门学科失败: " + e.getMessage());
        }
    }

    @ApiOperation("获取难度较高的学科")
    @GetMapping("/difficult")
    public Result getDifficultSubjects(@RequestParam(defaultValue = "10") Integer limit, HttpServletRequest request) {
        User currentUser = null;
        try {
            currentUser = TokenUtils.getCurrentUser();
            if (currentUser == null) {
                return Result.error("用户未登录");
            }

            List<Subject> subjects = subjectService.getDifficultSubjects(limit);
            List<SubjectDTO> subjectDTOs = subjects.stream()
                .map(subject -> {
                    SubjectDTO dto = new SubjectDTO();
                    BeanUtils.copyProperties(subject, dto);
                    return dto;
                })
                .collect(java.util.stream.Collectors.toList());

            // 记录成功日志
            Log operationLog = new Log();
            operationLog.setUserId(currentUser.getUserId());
            operationLog.setActionType(7); // SELECT
            operationLog.setActionDescription("用户[" + currentUser.getUsername() + "]查看了难度较高的学科列表");
            operationLog.setObjectType("DIFFICULT_SUBJECTS");
            operationLog.setIpAddress(request.getRemoteAddr());
            operationLog.setDeviceInfo(request.getHeader("User-Agent"));
            operationLog.setStatus("SUCCESS");
            operationLog.setCreatedTime(new Date());
            logService.insert(operationLog);

            return Result.success(subjectDTOs);
        } catch (Exception e) {
            // 记录异常日志
            Log operationLog = new Log();
            if (currentUser != null) {
                operationLog.setUserId(currentUser.getUserId());
                operationLog.setActionDescription("用户[" + currentUser.getUsername() + "]查看难度较高的学科列表异常：" + e.getMessage());
            } else {
                operationLog.setActionDescription("未登录用户查看难度较高的学科列表异常：" + e.getMessage());
            }
            operationLog.setActionType(5); // EXCEPTION
            operationLog.setObjectType("DIFFICULT_SUBJECTS");
            operationLog.setIpAddress(request.getRemoteAddr());
            operationLog.setDeviceInfo(request.getHeader("User-Agent"));
            operationLog.setStatus("ERROR");
            operationLog.setCreatedTime(new Date());
            logService.insert(operationLog);
            
            log.error("获取难度较高的学科失败", e);
            return Result.error("获取难度较高的学科失败: " + e.getMessage());
        }
    }

    @ApiOperation("获取学科考试统计")
    @GetMapping("/exam-statistics")
    public Result getExamStatistics(HttpServletRequest request) {
        User currentUser = null;
        try {
            currentUser = TokenUtils.getCurrentUser();
            if (currentUser == null) {
                return Result.error("用户未登录");
            }

            List<Map<String, Object>> examStats = subjectService.countExamsBySubject();
            
            // 记录成功日志
            Log operationLog = new Log();
            operationLog.setUserId(currentUser.getUserId());
            operationLog.setActionType(7); // SELECT
            operationLog.setActionDescription("用户[" + currentUser.getUsername() + "]查看了学科考试统计信息");
            operationLog.setObjectType("SUBJECT_EXAM_STATISTICS");
            operationLog.setIpAddress(request.getRemoteAddr());
            operationLog.setDeviceInfo(request.getHeader("User-Agent"));
            operationLog.setStatus("SUCCESS");
            operationLog.setCreatedTime(new Date());
            logService.insert(operationLog);

            return Result.success(examStats);
        } catch (Exception e) {
            // 记录异常日志
            Log operationLog = new Log();
            if (currentUser != null) {
                operationLog.setUserId(currentUser.getUserId());
                operationLog.setActionDescription("用户[" + currentUser.getUsername() + "]查看学科考试统计信息异常：" + e.getMessage());
            } else {
                operationLog.setActionDescription("未登录用户查看学科考试统计信息异常：" + e.getMessage());
            }
            operationLog.setActionType(5); // EXCEPTION
            operationLog.setObjectType("SUBJECT_EXAM_STATISTICS");
            operationLog.setIpAddress(request.getRemoteAddr());
            operationLog.setDeviceInfo(request.getHeader("User-Agent"));
            operationLog.setStatus("ERROR");
            operationLog.setCreatedTime(new Date());
            logService.insert(operationLog);
            
            log.error("获取学科考试统计失败", e);
            return Result.error("获取学科考试统计失败: " + e.getMessage());
        }
    }

    @ApiOperation("获取学科平均成绩统计")
    @GetMapping("/score-statistics")
    public Result getScoreStatistics(HttpServletRequest request) {
        User currentUser = null;
        try {
            currentUser = TokenUtils.getCurrentUser();
            if (currentUser == null) {
                return Result.error("用户未登录");
            }

            List<Map<String, Object>> scoreStats = subjectService.getAvgScoreBySubject();
            
            // 记录成功日志
            Log operationLog = new Log();
            operationLog.setUserId(currentUser.getUserId());
            operationLog.setActionType(7); // SELECT
            operationLog.setActionDescription("用户[" + currentUser.getUsername() + "]查看了学科平均成绩统计信息");
            operationLog.setObjectType("SUBJECT_SCORE_STATISTICS");
            operationLog.setIpAddress(request.getRemoteAddr());
            operationLog.setDeviceInfo(request.getHeader("User-Agent"));
            operationLog.setStatus("SUCCESS");
            operationLog.setCreatedTime(new Date());
            logService.insert(operationLog);

            return Result.success(scoreStats);
        } catch (Exception e) {
            // 记录异常日志
            Log operationLog = new Log();
            if (currentUser != null) {
                operationLog.setUserId(currentUser.getUserId());
                operationLog.setActionDescription("用户[" + currentUser.getUsername() + "]查看学科平均成绩统计信息异常：" + e.getMessage());
            } else {
                operationLog.setActionDescription("未登录用户查看学科平均成绩统计信息异常：" + e.getMessage());
            }
            operationLog.setActionType(5); // EXCEPTION
            operationLog.setObjectType("SUBJECT_SCORE_STATISTICS");
            operationLog.setIpAddress(request.getRemoteAddr());
            operationLog.setDeviceInfo(request.getHeader("User-Agent"));
            operationLog.setStatus("ERROR");
            operationLog.setCreatedTime(new Date());
            logService.insert(operationLog);
            
            log.error("获取学科平均成绩统计失败", e);
            return Result.error("获取学科平均成绩统计失败: " + e.getMessage());
        }
    }
} 
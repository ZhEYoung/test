package com.exam.controller.admin;

import com.exam.entity.College;
import com.exam.entity.Subject;
import com.exam.entity.User;
import com.exam.entity.Log;
import com.exam.entity.dto.CollegeDTO;
import com.exam.service.CollegeService;
import com.exam.service.LogService;
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

@Slf4j
@RestController
@RequestMapping("/admin/colleges")
@Api(tags = "学院管理接口")
public class AdminCollegeController {

    @Autowired
    private CollegeService collegeService;

    @Autowired
    private LogService logService;

    @ApiOperation("获取学院列表")
    @GetMapping
    public Result getCollegeList(@RequestParam(required = false) String keyword,
                                @RequestParam(defaultValue = "1") Integer pageNum,
                                @RequestParam(defaultValue = "10") Integer pageSize,
                                HttpServletRequest request) {
        // 获取当前操作的用户
        User currentUser = TokenUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.error("用户未登录");
        }

        try {
            Map<String, Object> condition = new HashMap<>();
            if (keyword != null && !keyword.trim().isEmpty()) {
                condition.put("collegeName", keyword);
            }
            List<College> colleges = collegeService.getPageByCondition(condition, pageNum, pageSize);
            Long total = collegeService.getCountByCondition(condition);
            Map<String, Object> data = new HashMap<>();
            data.put("list", colleges);
            data.put("total", total);

            // 记录查询日志
            Log operationLog = new Log();
            operationLog.setUserId(currentUser.getUserId());
            operationLog.setActionType(7); // SELECT
            operationLog.setActionDescription("用户[" + currentUser.getUsername() + "]查询了学院列表");
            operationLog.setObjectType("COLLEGE");
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
            operationLog.setActionDescription("用户[" + currentUser.getUsername() + "]查询学院列表异常：" + e.getMessage());
            operationLog.setObjectType("COLLEGE");
            operationLog.setIpAddress(request.getRemoteAddr());
            operationLog.setDeviceInfo(request.getHeader("User-Agent"));
            operationLog.setStatus("ERROR");
            operationLog.setCreatedTime(new Date());
            logService.insert(operationLog);

            log.error("获取学院列表失败", e);
            return Result.error("获取学院列表失败：" + e.getMessage());
        }
    }

    @ApiOperation("添加学院")
    @PostMapping
    public Result addCollege(@RequestBody CollegeDTO collegeDTO, HttpServletRequest request) {
        // 获取当前操作的用户
        User currentUser = TokenUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.error("用户未登录");
        }

        try {
            College college = new College();
            college.setCollegeName(collegeDTO.getCollegeName());
            college.setDescription(collegeDTO.getDescription());
            int result = collegeService.insert(college);

            if (result <= 0) {
                // 记录失败日志
                Log operationLog = new Log();
                operationLog.setUserId(currentUser.getUserId());
                operationLog.setActionType(0); // INSERT
                operationLog.setActionDescription("用户[" + currentUser.getUsername() + "]添加学院[" + collegeDTO.getCollegeName() + "]失败");
                operationLog.setObjectType("COLLEGE");
                operationLog.setIpAddress(request.getRemoteAddr());
                operationLog.setDeviceInfo(request.getHeader("User-Agent"));
                operationLog.setStatus("FAILED");
                operationLog.setCreatedTime(new Date());
                logService.insert(operationLog);

                return Result.error("添加学院失败");
            }

            // 记录成功日志
            Log operationLog = new Log();
            operationLog.setUserId(currentUser.getUserId());
            operationLog.setActionType(0); // INSERT
            operationLog.setActionDescription("用户[" + currentUser.getUsername() + "]添加了学院[" + collegeDTO.getCollegeName() + "]");
            operationLog.setObjectType("COLLEGE");
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
            operationLog.setActionDescription("用户[" + currentUser.getUsername() + "]添加学院异常：" + e.getMessage());
            operationLog.setObjectType("COLLEGE");
            operationLog.setIpAddress(request.getRemoteAddr());
            operationLog.setDeviceInfo(request.getHeader("User-Agent"));
            operationLog.setStatus("ERROR");
            operationLog.setCreatedTime(new Date());
            logService.insert(operationLog);

            log.error("添加学院失败", e);
            return Result.error("添加学院失败：" + e.getMessage());
        }
    }

    @ApiOperation("更新学院信息")
    @PutMapping("/{collegeId}")
    public Result updateCollege(@PathVariable Integer collegeId, @RequestBody CollegeDTO collegeDTO, HttpServletRequest request) {
        // 获取当前操作的用户
        User currentUser = TokenUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.error("用户未登录");
        }

        try {
            College college = collegeService.getById(collegeId);
            if (college == null) {
                return Result.error("学院不存在");
            }

            college.setCollegeName(collegeDTO.getCollegeName());
            college.setDescription(collegeDTO.getDescription());
            int result = collegeService.updateById(college);

            if (result <= 0) {
                // 记录失败日志
                Log operationLog = new Log();
                operationLog.setUserId(currentUser.getUserId());
                operationLog.setActionType(1); // UPDATE
                operationLog.setActionDescription("用户[" + currentUser.getUsername() + "]更新学院[" + college.getCollegeName() + "]信息失败");
                operationLog.setObjectType("COLLEGE");
                operationLog.setIpAddress(request.getRemoteAddr());
                operationLog.setDeviceInfo(request.getHeader("User-Agent"));
                operationLog.setStatus("FAILED");
                operationLog.setCreatedTime(new Date());
                logService.insert(operationLog);

                return Result.error("更新学院信息失败");
            }

            // 记录成功日志
            Log operationLog = new Log();
            operationLog.setUserId(currentUser.getUserId());
            operationLog.setActionType(1); // UPDATE
            operationLog.setActionDescription("用户[" + currentUser.getUsername() + "]更新了学院[" + college.getCollegeName() + "]的信息");
            operationLog.setObjectType("COLLEGE");
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
            operationLog.setActionDescription("用户[" + currentUser.getUsername() + "]更新学院信息异常：" + e.getMessage());
            operationLog.setObjectType("COLLEGE");
            operationLog.setIpAddress(request.getRemoteAddr());
            operationLog.setDeviceInfo(request.getHeader("User-Agent"));
            operationLog.setStatus("ERROR");
            operationLog.setCreatedTime(new Date());
            logService.insert(operationLog);

            log.error("更新学院信息失败", e);
            return Result.error("更新学院信息失败：" + e.getMessage());
        }
    }

    @ApiOperation("获取学院详情")
    @GetMapping("/{collegeId}")
    public Result getCollegeDetail(@PathVariable Integer collegeId, HttpServletRequest request) {
        // 获取当前操作的用户
        User currentUser = TokenUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.error("用户未登录");
        }

        try {
            College college = collegeService.getById(collegeId);
            if (college == null) {
                // 记录失败日志
                Log operationLog = new Log();
                operationLog.setUserId(currentUser.getUserId());
                operationLog.setActionType(7); // SELECT
                operationLog.setActionDescription("用户[" + currentUser.getUsername() + "]查询学院详情失败，学院不存在");
                operationLog.setObjectType("COLLEGE");
                operationLog.setIpAddress(request.getRemoteAddr());
                operationLog.setDeviceInfo(request.getHeader("User-Agent"));
                operationLog.setStatus("FAILED");
                operationLog.setCreatedTime(new Date());
                logService.insert(operationLog);

                return Result.error("学院不存在");
            }

            Map<String, Object> data = new HashMap<>();
            data.put("college", college);
            data.put("studentCount", collegeService.countStudents(collegeId));
            data.put("teacherCount", collegeService.countTeachers(collegeId));
            data.put("subjectCount", collegeService.countSubjects(collegeId));

            // 记录成功日志
            Log operationLog = new Log();
            operationLog.setUserId(currentUser.getUserId());
            operationLog.setActionType(7); // SELECT
            operationLog.setActionDescription("用户[" + currentUser.getUsername() + "]查询了学院[" + college.getCollegeName() + "]的详情");
            operationLog.setObjectType("COLLEGE");
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
            operationLog.setActionDescription("用户[" + currentUser.getUsername() + "]查询学院详情异常：" + e.getMessage());
            operationLog.setObjectType("COLLEGE");
            operationLog.setIpAddress(request.getRemoteAddr());
            operationLog.setDeviceInfo(request.getHeader("User-Agent"));
            operationLog.setStatus("ERROR");
            operationLog.setCreatedTime(new Date());
            logService.insert(operationLog);

            log.error("获取学院详情失败", e);
            return Result.error("获取学院详情失败：" + e.getMessage());
        }
    }

    @ApiOperation("获取学院下的所有学科")
    @GetMapping("/{collegeId}/subjects")
    public Result getCollegeSubjects(@PathVariable Integer collegeId, HttpServletRequest request) {
        // 获取当前操作的用户
        User currentUser = TokenUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.error("用户未登录");
        }

        try {
            College college = collegeService.getById(collegeId);
            if (college == null) {
                return Result.error("学院不存在");
            }

            List<Subject> subjects = collegeService.getCollegeSubjects(collegeId);

            // 记录成功日志
            Log operationLog = new Log();
            operationLog.setUserId(currentUser.getUserId());
            operationLog.setActionType(7); // SELECT
            operationLog.setActionDescription("用户[" + currentUser.getUsername() + "]查询了学院[" + college.getCollegeName() + "]的学科列表");
            operationLog.setObjectType("COLLEGE");
            operationLog.setIpAddress(request.getRemoteAddr());
            operationLog.setDeviceInfo(request.getHeader("User-Agent"));
            operationLog.setStatus("SUCCESS");
            operationLog.setCreatedTime(new Date());
            logService.insert(operationLog);

            return Result.success(subjects);
        } catch (Exception e) {
            // 记录异常日志
            Log operationLog = new Log();
            operationLog.setUserId(currentUser.getUserId());
            operationLog.setActionType(5); // EXCEPTION
            operationLog.setActionDescription("用户[" + currentUser.getUsername() + "]查询学院学科列表异常：" + e.getMessage());
            operationLog.setObjectType("COLLEGE");
            operationLog.setIpAddress(request.getRemoteAddr());
            operationLog.setDeviceInfo(request.getHeader("User-Agent"));
            operationLog.setStatus("ERROR");
            operationLog.setCreatedTime(new Date());
            logService.insert(operationLog);

            log.error("获取学院学科列表失败", e);
            return Result.error("获取学院学科列表失败：" + e.getMessage());
        }
    }

    @ApiOperation("获取学院下的所有教师")
    @GetMapping("/{collegeId}/teachers")
    public Result getCollegeTeachers(@PathVariable Integer collegeId, HttpServletRequest request) {
        // 获取当前操作的用户
        User currentUser = TokenUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.error("用户未登录");
        }

        try {
            College college = collegeService.getById(collegeId);
            if (college == null) {
                return Result.error("学院不存在");
            }

            Map<String, Object> data = new HashMap<>();
            data.put("count", collegeService.countTeachers(collegeId));

            // 记录成功日志
            Log operationLog = new Log();
            operationLog.setUserId(currentUser.getUserId());
            operationLog.setActionType(7); // SELECT
            operationLog.setActionDescription("用户[" + currentUser.getUsername() + "]查询了学院[" + college.getCollegeName() + "]的教师数量");
            operationLog.setObjectType("COLLEGE");
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
            operationLog.setActionDescription("用户[" + currentUser.getUsername() + "]查询学院教师数量异常：" + e.getMessage());
            operationLog.setObjectType("COLLEGE");
            operationLog.setIpAddress(request.getRemoteAddr());
            operationLog.setDeviceInfo(request.getHeader("User-Agent"));
            operationLog.setStatus("ERROR");
            operationLog.setCreatedTime(new Date());
            logService.insert(operationLog);

            log.error("获取学院教师数量失败", e);
            return Result.error("获取学院教师数量失败：" + e.getMessage());
        }
    }

    @ApiOperation("获取学院下的所有学生")
    @GetMapping("/{collegeId}/students")
    public Result getCollegeStudents(@PathVariable Integer collegeId, HttpServletRequest request) {
        // 获取当前操作的用户
        User currentUser = TokenUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.error("用户未登录");
        }

        try {
            College college = collegeService.getById(collegeId);
            if (college == null) {
                return Result.error("学院不存在");
            }

            Map<String, Object> studentList = collegeService.exportStudentList(collegeId);

            // 记录成功日志
            Log operationLog = new Log();
            operationLog.setUserId(currentUser.getUserId());
            operationLog.setActionType(7); // SELECT
            operationLog.setActionDescription("用户[" + currentUser.getUsername() + "]导出了学院[" + college.getCollegeName() + "]的学生名册");
            operationLog.setObjectType("COLLEGE");
            operationLog.setIpAddress(request.getRemoteAddr());
            operationLog.setDeviceInfo(request.getHeader("User-Agent"));
            operationLog.setStatus("SUCCESS");
            operationLog.setCreatedTime(new Date());
            logService.insert(operationLog);

            return Result.success(studentList);
        } catch (Exception e) {
            // 记录异常日志
            Log operationLog = new Log();
            operationLog.setUserId(currentUser.getUserId());
            operationLog.setActionType(5); // EXCEPTION
            operationLog.setActionDescription("用户[" + currentUser.getUsername() + "]导出学院学生名册异常：" + e.getMessage());
            operationLog.setObjectType("COLLEGE");
            operationLog.setIpAddress(request.getRemoteAddr());
            operationLog.setDeviceInfo(request.getHeader("User-Agent"));
            operationLog.setStatus("ERROR");
            operationLog.setCreatedTime(new Date());
            logService.insert(operationLog);

            log.error("获取学院学生名册失败", e);
            return Result.error("获取学院学生名册失败：" + e.getMessage());
        }
    }
} 
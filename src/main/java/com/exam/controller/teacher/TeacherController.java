package com.exam.controller.teacher;

import com.exam.entity.*;
import com.exam.entity.dto.AdminDTO;
import com.exam.entity.dto.TeacherDTO;
import com.exam.service.CollegeService;
import com.exam.service.TeacherService;
import com.exam.service.UserService;
import com.exam.service.LogService;
import com.exam.utils.TokenUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import com.exam.common.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Date;

@Slf4j
@RestController
@RequestMapping("/teacher")
@Api(tags = "教师功能接口")
public class TeacherController {

    @Autowired
    private TeacherService teacherService;

    @Autowired
    private UserService userService;

    @Autowired
    private LogService logService;

    @Autowired
    private CollegeService collegeService;

    @ApiOperation("获取教师个人信息")
    @GetMapping("/teachers/{userId}")
    public Result getTeacherProfile(@PathVariable Integer userId, HttpServletRequest request) {
        User currentUser = userService.getById(userId);
        if (currentUser == null) {
            return Result.error("用户未登录");
        }

        try {
            User user = userService.getById(currentUser.getUserId());
            Teacher teacher = teacherService.getByUserId(currentUser.getUserId());
            if (teacher == null) {
                return Result.error("未找到教师信息");
            }
            College college = collegeService.getById(teacher.getCollegeId());
            String collegeName = college == null ? "" : college.getCollegeName();

            TeacherDTO teacherDTO = new TeacherDTO();
            teacherDTO.setTeacherId(teacher.getTeacherId());
            teacherDTO.setName(teacher.getName());
            teacherDTO.setOther(teacher.getOther());
            teacherDTO.setUsername(user.getUsername());
            teacherDTO.setPhone(user.getPhone());
            teacherDTO.setEmail(user.getEmail());
            teacherDTO.setSex(user.getSex());
            teacherDTO.setStatus(user.getStatus());
            teacherDTO.setPermission(teacher.getPermission());
            teacherDTO.setCollegeName(collegeName);


            // 记录查询日志
            Log operationLog = new Log();
            operationLog.setUserId(currentUser.getUserId());
            operationLog.setActionType(7); // SELECT
            operationLog.setActionDescription("教师[" + currentUser.getUsername() + "]查看了个人信息");
            operationLog.setObjectType("TEACHER");
            operationLog.setIpAddress(request.getRemoteAddr());
            operationLog.setDeviceInfo(request.getHeader("User-Agent"));
            operationLog.setStatus("SUCCESS");
            operationLog.setCreatedTime(new Date());
            logService.insert(operationLog);

            return Result.success(teacherDTO);
        } catch (Exception e) {
            // 记录异常日志
            Log operationLog = new Log();
            operationLog.setUserId(currentUser.getUserId());
            operationLog.setActionType(5); // EXCEPTION
            operationLog.setActionDescription("教师[" + currentUser.getUsername() + "]查看个人信息异常：" + e.getMessage());
            operationLog.setObjectType("TEACHER");
            operationLog.setIpAddress(request.getRemoteAddr());
            operationLog.setDeviceInfo(request.getHeader("User-Agent"));
            operationLog.setStatus("ERROR");
            operationLog.setCreatedTime(new Date());
            logService.insert(operationLog);

            log.error("获取教师信息失败", e);
            return Result.error("获取教师信息失败：" + e.getMessage());
        }
    }

    @ApiOperation("更新教师个人信息")
    @PutMapping("/profile")
    public Result updateTeacherProfile(@RequestBody TeacherDTO teacherDTO, HttpServletRequest request) {
        User currentUser = TokenUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.error("用户未登录");
        }

        try {
            Teacher teacher = teacherService.getByUserId(currentUser.getUserId());
            if (teacher == null) {
                return Result.error("未找到教师信息");
            }

            // 更新教师信息
            teacher.setName(teacherDTO.getName());
            teacher.setOther(teacherDTO.getOther());
            teacher.setPermission(teacherDTO.getPermission());


            // 更新用户信息
            User user = userService.getById(teacher.getUserId());
            user.setPhone(teacherDTO.getPhone());
            user.setEmail(teacherDTO.getEmail());
            user.setSex(teacherDTO.getSex());
            user.setStatus(teacherDTO.getStatus());

            
            int result = teacherService.updateById(teacher);
            if (result <= 0) {
                // 记录失败日志
                Log operationLog = new Log();
                operationLog.setUserId(currentUser.getUserId());
                operationLog.setActionType(1); // UPDATE
                operationLog.setActionDescription("教师[" + currentUser.getUsername() + "]更新教师[" + user.getUsername() + "]信息失败");
                operationLog.setObjectType("TEACHER");
                operationLog.setIpAddress(request.getRemoteAddr());
                operationLog.setDeviceInfo(request.getHeader("User-Agent"));
                operationLog.setStatus("FAILED");
                operationLog.setCreatedTime(new Date());
                logService.insert(operationLog);

                return Result.error("更新教师信息失败");
            }

            result = userService.updateById(user);
            if (result <= 0) {
                // 记录失败日志
                Log operationLog = new Log();
                operationLog.setUserId(currentUser.getUserId());
                operationLog.setActionType(1); // UPDATE
                operationLog.setActionDescription("教师[\" + currentUser.getUsername() + \"]更新教师[\" + user.getUsername() + \"]用户信息失败");
                operationLog.setObjectType("TEACHER");
                operationLog.setIpAddress(request.getRemoteAddr());
                operationLog.setDeviceInfo(request.getHeader("User-Agent"));
                operationLog.setStatus("FAILED");
                operationLog.setCreatedTime(new Date());
                logService.insert(operationLog);

                return Result.error("更新用户信息失败");
            }

            // 记录操作日志
            Log operationLog = new Log();
            operationLog.setUserId(currentUser.getUserId());
            operationLog.setActionType(2); // UPDATE
            operationLog.setActionDescription("教师[" + currentUser.getUsername() + "]更新了个人信息");
            operationLog.setObjectType("TEACHER");
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
            operationLog.setActionDescription("教师[" + currentUser.getUsername() + "]更新个人信息异常：" + e.getMessage());
            operationLog.setObjectType("TEACHER");
            operationLog.setIpAddress(request.getRemoteAddr());
            operationLog.setDeviceInfo(request.getHeader("User-Agent"));
            operationLog.setStatus("ERROR");
            operationLog.setCreatedTime(new Date());
            logService.insert(operationLog);

            log.error("更新教师信息失败", e);
            return Result.error("更新教师信息失败：" + e.getMessage());
        }
    }


//    <!-- 查询教师考试统计信息 -->
//    <select id="selectExamStats" resultType="java.util.Map">
//    SELECT
//    COUNT(DISTINCT e.exam_id) as total_exams,
//    COUNT(DISTINCT e.subject_id) as total_subjects,
//    COUNT(DISTINCT ec.class_id) as total_classes,
//    COUNT(DISTINCT es.student_id) as total_students
//    FROM teacher t
//    LEFT JOIN exam e ON t.teacher_id = e.teacher_id
//    LEFT JOIN exam_class ec ON e.exam_id = ec.exam_id
//    LEFT JOIN exam_student es ON e.exam_id = es.exam_id
//    WHERE t.teacher_id = #{teacherId}
//    </select>

    @ApiOperation("获取教师考试统计信息")
    @GetMapping("/exam/stats")
    public Result getExamStats(HttpServletRequest request) {
        User currentUser = TokenUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.error("用户未登录");
        }

        try {
            Teacher teacher = teacherService.getByUserId(currentUser.getUserId());
            if (teacher == null) {
                return Result.error("未找到教师信息");
            }

            Map<String, Object> stats = teacherService.getExamStats(teacher.getTeacherId());

            // 记录查询日志
            Log operationLog = new Log();
            operationLog.setUserId(currentUser.getUserId());
            operationLog.setActionType(7); // SELECT
            operationLog.setActionDescription("教师[" + currentUser.getUsername() + "]查看了考试统计信息");
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
            operationLog.setActionDescription("教师[" + currentUser.getUsername() + "]查看考试统计信息异常：" + e.getMessage());
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

    @ApiOperation("获取教师发布的考试列表")
    @GetMapping("/exams")
    public Result getTeacherExams(HttpServletRequest request) {
        User currentUser = TokenUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.error("用户未登录");
        }

        try {
            Teacher teacher = teacherService.getByUserId(currentUser.getUserId());
            if (teacher == null) {
                return Result.error("未找到教师信息");
            }

            List<Map<String, Object>> exams = teacherService.getTeacherExams(teacher.getTeacherId());

            // 记录查询日志
            Log operationLog = new Log();
            operationLog.setUserId(currentUser.getUserId());
            operationLog.setActionType(7); // SELECT
            operationLog.setActionDescription("教师[" + currentUser.getUsername() + "]查看了发布的考试列表");
            operationLog.setObjectType("TEACHER_EXAM");
            operationLog.setIpAddress(request.getRemoteAddr());
            operationLog.setDeviceInfo(request.getHeader("User-Agent"));
            operationLog.setStatus("SUCCESS");
            operationLog.setCreatedTime(new Date());
            logService.insert(operationLog);

            return Result.success(exams);
        } catch (Exception e) {
            // 记录异常日志
            Log operationLog = new Log();
            operationLog.setUserId(currentUser.getUserId());
            operationLog.setActionType(5); // EXCEPTION
            operationLog.setActionDescription("教师[" + currentUser.getUsername() + "]查看发布的考试列表异常：" + e.getMessage());
            operationLog.setObjectType("TEACHER_EXAM");
            operationLog.setIpAddress(request.getRemoteAddr());
            operationLog.setDeviceInfo(request.getHeader("User-Agent"));
            operationLog.setStatus("ERROR");
            operationLog.setCreatedTime(new Date());
            logService.insert(operationLog);

            log.error("获取教师考试列表失败", e);
            return Result.error("获取教师考试列表失败：" + e.getMessage());
        }
    }

    @ApiOperation("获取系统所有学科")
    @GetMapping("/subjects")
    public Result getCollegeSubjects(HttpServletRequest request) {
        // 获取当前操作的用户
        User currentUser = TokenUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.error("用户未登录");
        }

        try {
            Teacher teacher = teacherService.getByUserId(currentUser.getUserId());
            Integer collegeId = teacher.getCollegeId();
            College college = collegeService.getById(collegeId);
            if (college == null) {
                return Result.error("学院不存在");
            }

            List<College>   colleges = collegeService.getAll();
            List<Subject> subjects = new ArrayList<>();
            for (College c : colleges) {
                List<Subject> list = collegeService.getCollegeSubjects(c.getCollegeId());
                subjects.addAll(list);
            }

            // 记录成功日志
            Log operationLog = new Log();
            operationLog.setUserId(currentUser.getUserId());
            operationLog.setActionType(7); // SELECT
            operationLog.setActionDescription("用户[" + currentUser.getUsername() + "]查询了系统的学科列表");
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
} 
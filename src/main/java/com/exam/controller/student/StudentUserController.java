package com.exam.controller.student;

import com.exam.entity.*;
import com.exam.entity.dto.StudentDTO;
import com.exam.entity.dto.TeacherDTO;
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

@Slf4j
@RestController
@RequestMapping("/student")
@Api(tags = "学生用户功能接口")
public class StudentUserController {

    @Autowired
    private StudentService studentService;

    @Autowired
    private UserService userService;

    @Autowired
    private LogService logService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private CollegeService collegeService;

    @ApiOperation("学生修改密码")
    @PutMapping("/password")
    public Result updatePassword(@RequestBody Map<String, String> passwordForm, HttpServletRequest request) {
        User currentUser = TokenUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.error("用户未登录");
        }

        try {
            String oldPassword = passwordForm.get("oldPassword");
            String newPassword = passwordForm.get("newPassword");

            // 验证旧密码
            User user = userService.login(currentUser.getUsername(), oldPassword);
            if (user == null) {
                return Result.error("原密码错误");
            }

            // 更新密码
            int result = userService.updatePassword(currentUser.getUserId(), oldPassword, newPassword);
            if (result <= 0) {
                // 记录失败日志
                Log operationLog = new Log();
                operationLog.setUserId(currentUser.getUserId());
                operationLog.setActionType(1); // UPDATE
                operationLog.setActionDescription("学生[" + currentUser.getUsername() + "]修改密码失败");
                operationLog.setObjectType("STUDENT");
                operationLog.setIpAddress(request.getRemoteAddr());
                operationLog.setDeviceInfo(request.getHeader("User-Agent"));
                operationLog.setStatus("FAILED");
                operationLog.setCreatedTime(new Date());
                logService.insert(operationLog);

                return Result.error("修改密码失败");
            }

            // 记录成功日志
            Log operationLog = new Log();
            operationLog.setUserId(currentUser.getUserId());
            operationLog.setActionType(1); // UPDATE
            operationLog.setActionDescription("学生[" + currentUser.getUsername() + "]修改密码成功");
            operationLog.setObjectType("STUDENT");
            operationLog.setIpAddress(request.getRemoteAddr());
            operationLog.setDeviceInfo(request.getHeader("User-Agent"));
            operationLog.setStatus("SUCCESS");
            operationLog.setCreatedTime(new Date());
            logService.insert(operationLog);

            return Result.success("密码修改成功");
        } catch (Exception e) {
            log.error("修改密码失败", e);
            return Result.error("修改密码失败：" + e.getMessage());
        }
    }


    @ApiOperation("获取学生个人信息")
    @GetMapping("/students/{userId}")
    public Result getStudentProfile(@PathVariable Integer userId, HttpServletRequest request) {
        User currentUser = userService.getById(userId);
        if (currentUser == null) {
            return Result.error("用户未登录");
        }

        try {
            User user = userService.getById(currentUser.getUserId());
            Student student = studentService.getByUserId(currentUser.getUserId());
            if (student == null) {
                return Result.error("未找到教师信息");
            }
            College college = collegeService.getById(student.getCollegeId());
            String collegeName = college == null ? "" : college.getCollegeName();

            TeacherDTO teacherDTO = new TeacherDTO();
            teacherDTO.setTeacherId(student.getStudentId());
            teacherDTO.setName(student.getName());
            teacherDTO.setOther(student.getOther());
            teacherDTO.setUsername(user.getUsername());
            teacherDTO.setPhone(user.getPhone());
            teacherDTO.setEmail(user.getEmail());
            teacherDTO.setSex(user.getSex());
            teacherDTO.setStatus(user.getStatus());
            teacherDTO.setCollegeName(collegeName);


            // 记录查询日志
            Log operationLog = new Log();
            operationLog.setUserId(currentUser.getUserId());
            operationLog.setActionType(7); // SELECT
            operationLog.setActionDescription("学生[" + currentUser.getUsername() + "]查看了个人信息");
            operationLog.setObjectType("STUDENT");
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
            operationLog.setActionDescription("学生[" + currentUser.getUsername() + "]查看个人信息异常：" + e.getMessage());
            operationLog.setObjectType("STUDENT");
            operationLog.setIpAddress(request.getRemoteAddr());
            operationLog.setDeviceInfo(request.getHeader("User-Agent"));
            operationLog.setStatus("ERROR");
            operationLog.setCreatedTime(new Date());
            logService.insert(operationLog);

            log.error("获取学生信息失败", e);
            return Result.error("获取学生信息失败：" + e.getMessage());
        }
    }

    @ApiOperation("更新学生个人信息")
    @PutMapping("/profile")
    public Result updateStudentProfile(@RequestBody StudentDTO studentDTO, HttpServletRequest request) {
        User currentUser = TokenUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.error("用户未登录");
        }

        try {
            Student student = studentService.getByUserId(currentUser.getUserId());
            if (student == null) {
                return Result.error("未找到学生信息");
            }

            // 更新教师信息
            student.setName(studentDTO.getName());
            student.setOther(studentDTO.getOther());


            // 更新用户信息
            User user = userService.getById(student.getUserId());
            user.setPhone(studentDTO.getPhone());
            user.setEmail(studentDTO.getEmail());
            user.setSex(studentDTO.getSex());
            user.setStatus(studentDTO.getStatus());


            int result = studentService.updateById(student);
            if (result <= 0) {
                // 记录失败日志
                Log operationLog = new Log();
                operationLog.setUserId(currentUser.getUserId());
                operationLog.setActionType(1); // UPDATE
                operationLog.setActionDescription("学生[" + currentUser.getUsername() + "]更新信息失败");
                operationLog.setObjectType("STUDENT");
                operationLog.setIpAddress(request.getRemoteAddr());
                operationLog.setDeviceInfo(request.getHeader("User-Agent"));
                operationLog.setStatus("FAILED");
                operationLog.setCreatedTime(new Date());
                logService.insert(operationLog);

                return Result.error("更新学生信息失败");
            }

            result = userService.updateById(user);
            if (result <= 0) {
                // 记录失败日志
                Log operationLog = new Log();
                operationLog.setUserId(currentUser.getUserId());
                operationLog.setActionType(1); // UPDATE
                operationLog.setActionDescription("学生[\" + currentUser.getUsername() + \"]更新用户信息失败");
                operationLog.setObjectType("STUDENT");
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
            operationLog.setActionDescription("学生[" + currentUser.getUsername() + "]更新了个人信息");
            operationLog.setObjectType("STUDENT");
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
            operationLog.setActionDescription("学生[" + currentUser.getUsername() + "]更新个人信息异常：" + e.getMessage());
            operationLog.setObjectType("STUDENT");
            operationLog.setIpAddress(request.getRemoteAddr());
            operationLog.setDeviceInfo(request.getHeader("User-Agent"));
            operationLog.setStatus("ERROR");
            operationLog.setCreatedTime(new Date());
            logService.insert(operationLog);

            log.error("更新学生信息失败", e);
            return Result.error("更新学生信息失败：" + e.getMessage());
        }
    }


} 
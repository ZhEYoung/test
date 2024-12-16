package com.exam.controller.admin;

import cn.hutool.crypto.SecureUtil;
import com.exam.entity.dto.TeacherDTO;
import com.exam.entity.Teacher;
import com.exam.entity.User;
import com.exam.entity.Log;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.BeanUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Date;

@Slf4j
@RestController
@RequestMapping("/admin/teachers")
@Api(tags = "教师管理接口")
public class TeacherManageController {

    @Autowired
    private TeacherService teacherService;

    @Autowired
    private UserService userService;

    @Autowired
    private LogService logService;

    @ApiOperation("获取教师列表")
    @GetMapping
    public Result getTeacherList(@RequestParam(required = false) String keyword,
                                @RequestParam(required = false) Integer collegeId,
                                @RequestParam(defaultValue = "1") Integer pageNum,
                                @RequestParam(defaultValue = "10") Integer pageSize,
                                HttpServletRequest request) {
        User currentUser = TokenUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.error("用户未登录");
        }

        try {
            // 构建查询条件
            Teacher condition = new Teacher();
            if (keyword != null && !keyword.trim().isEmpty()) {
                condition.setName(keyword);
            }
            if (collegeId != null) {
                condition.setCollegeId(collegeId);
            }

            // 获取教师列表
            List<Teacher> teachers = teacherService.getByCondition(condition);
            int total = teacherService.getCount();

            Map<String, Object> data = new HashMap<>();
            data.put("list", teachers);
            data.put("total", total);

            // 记录查询日志
            Log operationLog = new Log();
            operationLog.setUserId(currentUser.getUserId());
            operationLog.setActionType(7); // SELECT
            operationLog.setActionDescription("管理员[" + currentUser.getUsername() + "]查询了教师列表");
            operationLog.setObjectType("TEACHER");
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
            operationLog.setActionDescription("管理员[" + currentUser.getUsername() + "]查询教师列表异常：" + e.getMessage());
            operationLog.setObjectType("TEACHER");
            operationLog.setIpAddress(request.getRemoteAddr());
            operationLog.setDeviceInfo(request.getHeader("User-Agent"));
            operationLog.setStatus("ERROR");
            operationLog.setCreatedTime(new Date());
            logService.insert(operationLog);

            log.error("获取教师列表失败", e);
            return Result.error("获取教师列表失败：" + e.getMessage());
        }
    }

    @ApiOperation("添加教师")
    @PostMapping
    public Result addTeacher(@RequestBody TeacherDTO teacherDTO, HttpServletRequest request) {
        User currentUser = TokenUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.error("用户未登录");
        }

        try {
            // 创建用户账号
            User user = new User();
            user.setUsername(teacherDTO.getUsername());
            user.setPassword(teacherDTO.getPassword());
            user.setPhone(teacherDTO.getPhone());
            user.setEmail(teacherDTO.getEmail());
            user.setSex(teacherDTO.getSex());
            user.setStatus(teacherDTO.getStatus());
            user.setRole(User.ROLE_TEACHER);
            
            int result = userService.insert(user);
            if (result <= 0) {
                return Result.error("创建用户账号失败");
            }

            // 创建教师信息
            Teacher teacher = new Teacher();
            teacher.setUserId(user.getUserId());
            teacher.setName(teacherDTO.getName());
            teacher.setCollegeId(teacherDTO.getCollegeId());
            teacher.setPermission(teacherDTO.getPermission());
            teacher.setOther(teacherDTO.getOther());

            result = teacherService.insert(teacher);
            if (result <= 0) {
                // 如果教师信息创建失败，回滚用户账号
                userService.deleteById(user.getUserId());
                return Result.error("创建教师信息失败");
            }

            // 记录成功日志
            Log operationLog = new Log();
            operationLog.setUserId(currentUser.getUserId());
            operationLog.setActionType(2); // INSERT
            operationLog.setActionDescription("管理员[" + currentUser.getUsername() + "]添加了教师[" + teacherDTO.getUsername() + "]");
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
            operationLog.setActionDescription("管理员[" + currentUser.getUsername() + "]添加教师异常：" + e.getMessage());
            operationLog.setObjectType("TEACHER");
            operationLog.setIpAddress(request.getRemoteAddr());
            operationLog.setDeviceInfo(request.getHeader("User-Agent"));
            operationLog.setStatus("ERROR");
            operationLog.setCreatedTime(new Date());
            logService.insert(operationLog);

            log.error("添加教师失败", e);
            return Result.error("添加教师失败：" + e.getMessage());
        }
    }

    @ApiOperation("更新教师信息")
    @PutMapping("/{teacherId}")
    public Result updateTeacher(@PathVariable Integer teacherId, 
                              @RequestBody TeacherDTO teacherDTO,
                              HttpServletRequest request) {
        User currentUser = TokenUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.error("用户未登录");
        }

        try {
            // 获取教师信息
            Teacher teacher = teacherService.getById(teacherId);
            if (teacher == null) {
                return Result.error("教师不存在");
            }

            // 更新用户信息
            User user = userService.getById(teacher.getUserId());
            if (user == null) {
                return Result.error("用户账号不存在");
            }

            user.setPhone(teacherDTO.getPhone());
            user.setEmail(teacherDTO.getEmail());
            user.setSex(teacherDTO.getSex());
            
            int result = userService.updateById(user);
            if (result <= 0) {
                return Result.error("更新用户信息失败");
            }

            // 更新教师信息
            teacher.setName(teacherDTO.getName());
            teacher.setCollegeId(teacherDTO.getCollegeId());
            teacher.setPermission(teacherDTO.getPermission());
            teacher.setOther(teacherDTO.getOther());

            result = teacherService.updateById(teacher);
            if (result <= 0) {
                return Result.error("更新教师信息失败");
            }

            // 记录成功日志
            Log operationLog = new Log();
            operationLog.setUserId(currentUser.getUserId());
            operationLog.setActionType(1); // UPDATE
            operationLog.setActionDescription("管理员[" + currentUser.getUsername() + "]更新了教师[" + user.getUsername() + "]的信息");
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
            operationLog.setActionDescription("管理员[" + currentUser.getUsername() + "]更新教师信息异常：" + e.getMessage());
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

    @ApiOperation("获取教师详情")
    @GetMapping("/{teacherId}")
    public Result getTeacherDetail(@PathVariable Integer teacherId, HttpServletRequest request) {
        User currentUser = TokenUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.error("用户未登录");
        }

        try {
            Teacher teacher = teacherService.getById(teacherId);
            if (teacher == null) {
                return Result.error("教师不存在");
            }

            User user = userService.getById(teacher.getUserId());
            if (user == null) {
                return Result.error("用户账号不存在");
            }

            // 构建返回的DTO对象
            TeacherDTO teacherDTO = new TeacherDTO();
            teacherDTO.setTeacherId(teacher.getTeacherId());
            teacherDTO.setName(teacher.getName());
            teacherDTO.setCollegeId(teacher.getCollegeId());
            teacherDTO.setPermission(teacher.getPermission());
            teacherDTO.setOther(teacher.getOther());
            teacherDTO.setUsername(user.getUsername());
            teacherDTO.setPhone(user.getPhone());
            teacherDTO.setEmail(user.getEmail());
            teacherDTO.setSex(user.getSex());
            teacherDTO.setStatus(user.getStatus());

            // 记录查询日志
            Log operationLog = new Log();
            operationLog.setUserId(currentUser.getUserId());
            operationLog.setActionType(7); // SELECT
            operationLog.setActionDescription("管理员[" + currentUser.getUsername() + "]查看了教师[" + user.getUsername() + "]的详细信息");
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
            operationLog.setActionDescription("管理员[" + currentUser.getUsername() + "]查看教师详情异常：" + e.getMessage());
            operationLog.setObjectType("TEACHER");
            operationLog.setIpAddress(request.getRemoteAddr());
            operationLog.setDeviceInfo(request.getHeader("User-Agent"));
            operationLog.setStatus("ERROR");
            operationLog.setCreatedTime(new Date());
            logService.insert(operationLog);

            log.error("获取教师详情失败", e);
            return Result.error("获取教师详情失败：" + e.getMessage());
        }
    }

    @ApiOperation("重置教师密码")
    @PutMapping("/{teacherId}/password/reset")
    public Result resetPassword(@PathVariable Integer teacherId, HttpServletRequest request) {
        User currentUser = TokenUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.error("用户未登录");
        }

        try {
            Teacher teacher = teacherService.getById(teacherId);
            if (teacher == null) {
                return Result.error("教师不存在");
            }

            User user = userService.getById(teacher.getUserId());
            if (user == null) {
                return Result.error("用户账号不存在");
            }

            // 重置密码为默认密码
            String password = "1234567";
            user.setPassword(SecureUtil.sha256(password)); // 这里应该使用加密后的密码
            int result = userService.updateById(user);
            if (result <= 0) {
                return Result.error("重置密码失败");
            }

            // 记录成功日志
            Log operationLog = new Log();
            operationLog.setUserId(currentUser.getUserId());
            operationLog.setActionType(1); // UPDATE
            operationLog.setActionDescription("管理员[" + currentUser.getUsername() + "]重置了教师[" + user.getUsername() + "]的密码");
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
            operationLog.setActionDescription("管理员[" + currentUser.getUsername() + "]重置教师密码异常：" + e.getMessage());
            operationLog.setObjectType("TEACHER");
            operationLog.setIpAddress(request.getRemoteAddr());
            operationLog.setDeviceInfo(request.getHeader("User-Agent"));
            operationLog.setStatus("ERROR");
            operationLog.setCreatedTime(new Date());
            logService.insert(operationLog);

            log.error("重置教师密码失败", e);
            return Result.error("重置教师密码失败：" + e.getMessage());
        }
    }

    @ApiOperation("修改教师状态")
    @PutMapping("/{teacherId}/status")
    public Result updateStatus(@PathVariable Integer teacherId, 
                             @RequestParam Boolean status,
                             HttpServletRequest request) {
        User currentUser = TokenUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.error("用户未登录");
        }

        try {
            Teacher teacher = teacherService.getById(teacherId);
            if (teacher == null) {
                return Result.error("教师不存在");
            }

            User user = userService.getById(teacher.getUserId());
            if (user == null) {
                return Result.error("用户账号不存在");
            }

            // 更新用户状态
            user.setStatus(status);
            int result = userService.updateById(user);
            if (result <= 0) {
                return Result.error("更新状态失败");
            }

            // 记录成功日志
            Log operationLog = new Log();
            operationLog.setUserId(currentUser.getUserId());
            operationLog.setActionType(1); // UPDATE
            operationLog.setActionDescription("管理员[" + currentUser.getUsername() + "]" + 
                (status ? "启用" : "禁用") + "了教师[" + user.getUsername() + "]的账号");
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
            operationLog.setActionDescription("管理员[" + currentUser.getUsername() + "]修改教师状态异常：" + e.getMessage());
            operationLog.setObjectType("TEACHER");
            operationLog.setIpAddress(request.getRemoteAddr());
            operationLog.setDeviceInfo(request.getHeader("User-Agent"));
            operationLog.setStatus("ERROR");
            operationLog.setCreatedTime(new Date());
            logService.insert(operationLog);

            log.error("修改教师状态失败", e);
            return Result.error("修改教师状态失败：" + e.getMessage());
        }
    }
} 
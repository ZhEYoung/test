package com.exam.controller.admin;

import com.exam.entity.dto.PermissionDTO;
import com.exam.service.AdminService;
import com.exam.service.LogService;
import com.exam.service.UserService;
import com.exam.service.TeacherService;
import com.exam.entity.User;
import com.exam.entity.Teacher;
import com.exam.entity.Admin;
import com.exam.entity.Log;
import com.exam.utils.TokenUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import com.exam.common.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.Date;

@Slf4j
@RestController
@RequestMapping("/admin")
@Api(tags = "管理员核心功能接口")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private UserService userService;

    @Autowired
    private TeacherService teacherService;

    @Autowired
    private LogService logService;

    @Autowired
    private TokenUtils tokenUtils;

    @ApiOperation("系统用户统计")
    @GetMapping("/permissions")
    public Result getPermissions(HttpServletRequest request) {
        User currentUser = null;
        try {
            currentUser = tokenUtils.getCurrentUser();
            if (currentUser == null || currentUser.getRole() != User.ROLE_ADMIN) {
                return Result.error("无权限执行此操作");
            }

            Map<String, Long> userStats = adminService.countSystemUsers();
            
            // 记录成功日志
            Log operationLog = new Log();
            operationLog.setUserId(currentUser.getUserId());
            operationLog.setActionType(7); // SELECT
            operationLog.setActionDescription("管理员[" + currentUser.getUsername() + "]查看了用户统计");
            operationLog.setObjectType("PERMISSION");
            operationLog.setIpAddress(request.getRemoteAddr());
            operationLog.setDeviceInfo(request.getHeader("User-Agent"));
            operationLog.setStatus("SUCCESS");
            operationLog.setCreatedTime(new Date());
            logService.insert(operationLog);
            
            return Result.success(userStats);
        } catch (Exception e) {
            // 记录异常日志
            Log operationLog = new Log();
            if (currentUser != null) {
                operationLog.setUserId(currentUser.getUserId());
                operationLog.setActionDescription("管理员[" + currentUser.getUsername() + "]查看用户统计异常：" + e.getMessage());
            } else {
                operationLog.setActionDescription("未登录用户查看用户统计异常：" + e.getMessage());
            }
            operationLog.setActionType(5); // EXCEPTION
            operationLog.setObjectType("PERMISSION");
            operationLog.setIpAddress(request.getRemoteAddr());
            operationLog.setDeviceInfo(request.getHeader("User-Agent"));
            operationLog.setStatus("ERROR");
            operationLog.setCreatedTime(new Date());
            logService.insert(operationLog);
            
            log.error("获取用户统计失败", e);
            return Result.error("获取用户统计失败: " + e.getMessage());
        }
    }


    @ApiOperation("模拟用户登录")
    @PostMapping("/simulate/login/{userId}")
    public Result simulateLogin(@PathVariable Integer userId, HttpServletRequest request) {
        User currentUser = null;
        try {
            currentUser = tokenUtils.getCurrentUser();
            if (currentUser == null || currentUser.getRole() != User.ROLE_ADMIN) {
                return Result.error("无权限执行此操作");
            }
            Admin currentAdmin = adminService.getByUserId(currentUser.getUserId());
            if (currentAdmin == null) {
                return Result.error("管理员信息不存在");
            }

            // 获取目标用户信息
            User targetUser = userService.getById(userId);
            if (targetUser == null) {
                // 记录失败日志
                Log operationLog = new Log();
                operationLog.setUserId(currentUser.getUserId());
                operationLog.setActionType(6); // SIMULATE_LOGIN
                operationLog.setActionDescription("管理员[" + currentUser.getUsername() + "]模拟登录失败：目标用户不存在");
                operationLog.setObjectType("SIMULATE_LOGIN");
                operationLog.setIpAddress(request.getRemoteAddr());
                operationLog.setDeviceInfo(request.getHeader("User-Agent"));
                operationLog.setStatus("FAILED");
                operationLog.setCreatedTime(new Date());
                logService.insert(operationLog);
                
                return Result.error("目标用户不存在");
            }
            
            // 调用模拟登录服务
            User user = adminService.impersonateUser(currentAdmin.getAdminId(), userId, request.getRemoteAddr(), request.getHeader("User-Agent"));
            if (user != null) {
                // 记录成功日志
                Log operationLog = new Log();
                operationLog.setUserId(currentUser.getUserId());
                operationLog.setActionType(6); // SIMULATE_LOGIN
                operationLog.setActionDescription("管理员[" + currentUser.getUsername() + "]模拟登录用户[" + user.getUsername() + "]成功");
                operationLog.setObjectType("SIMULATE_LOGIN");
                operationLog.setIpAddress(request.getRemoteAddr());
                operationLog.setDeviceInfo(request.getHeader("User-Agent"));
                operationLog.setStatus("SUCCESS");
                operationLog.setCreatedTime(new Date());
                logService.insert(operationLog);
                
                // 生成新的token
                String token = TokenUtils.genToken(String.valueOf(user.getUserId()), user.getPassword());
                Map<String, Object> data = new HashMap<>();
                data.put("user", user);
                data.put("token", token);
                
                return Result.success(data);
            }
            
            // 记录失败日志
            Log operationLog = new Log();
            operationLog.setUserId(currentUser.getUserId());
            operationLog.setActionType(6); // SIMULATE_LOGIN
            operationLog.setActionDescription("管理员[" + currentUser.getUsername() + "]模拟登录用户[" + targetUser.getUsername() + "]失败");
            operationLog.setObjectType("SIMULATE_LOGIN");
            operationLog.setIpAddress(request.getRemoteAddr());
            operationLog.setDeviceInfo(request.getHeader("User-Agent"));
            operationLog.setStatus("FAILED");
            operationLog.setCreatedTime(new Date());
            logService.insert(operationLog);
            
            return Result.error("模拟登录失败");
        } catch (Exception e) {
            // 记录异常日志
            Log operationLog = new Log();
            if (currentUser != null) {
                operationLog.setUserId(currentUser.getUserId());
                operationLog.setActionDescription("管理员[" + currentUser.getUsername() + "]模拟登录异常：" + e.getMessage());
            } else {
                operationLog.setActionDescription("未登录用户模拟登录异常：" + e.getMessage());
            }
            operationLog.setActionType(5); // EXCEPTION
            operationLog.setObjectType("SIMULATE_LOGIN");
            operationLog.setIpAddress(request.getRemoteAddr());
            operationLog.setDeviceInfo(request.getHeader("User-Agent"));
            operationLog.setStatus("ERROR");
            operationLog.setCreatedTime(new Date());
            logService.insert(operationLog);
            
            log.error("模拟用户登录失败", e);
            return Result.error("模拟用户登录失败: " + e.getMessage());
        }
    }

    @ApiOperation("获取系统概览数据")
    @GetMapping("/dashboard")
    public Result getDashboardData(HttpServletRequest request) {
        User currentUser = null;
        try {
            currentUser = tokenUtils.getCurrentUser();
            if (currentUser == null || currentUser.getRole() != User.ROLE_ADMIN) {
                return Result.error("无权限访问此接口");
            }

            // 获取系统资源统计
            Map<String, Object> resourceStats = adminService.getSystemResourceStats();
            // 获取用户统计
            Map<String, Long> userStats = adminService.countSystemUsers();
            
            // 合并统计数据
            Map<String, Object> dashboardData = new HashMap<>();
            dashboardData.put("resourceStats", resourceStats);
            dashboardData.put("userStats", userStats);
            
            // 记录成功日志
            Log operationLog = new Log();
            operationLog.setUserId(currentUser.getUserId());
            operationLog.setActionType(7); // SELECT
            operationLog.setActionDescription("管理员[" + currentUser.getUsername() + "]查看了系统概览数据");
            operationLog.setObjectType("DASHBOARD");
            operationLog.setIpAddress(request.getRemoteAddr());
            operationLog.setDeviceInfo(request.getHeader("User-Agent"));
            operationLog.setStatus("SUCCESS");
            operationLog.setCreatedTime(new Date());
            logService.insert(operationLog);
            
            return Result.success(dashboardData);
        } catch (Exception e) {
            // 记录异常日志
            Log operationLog = new Log();
            if (currentUser != null) {
                operationLog.setUserId(currentUser.getUserId());
                operationLog.setActionDescription("管理员[" + currentUser.getUsername() + "]查看系统概览数据异常：" + e.getMessage());
            } else {
                operationLog.setActionDescription("未登录用户查看系统概览数据异常：" + e.getMessage());
            }
            operationLog.setActionType(5); // EXCEPTION
            operationLog.setObjectType("DASHBOARD");
            operationLog.setIpAddress(request.getRemoteAddr());
            operationLog.setDeviceInfo(request.getHeader("User-Agent"));
            operationLog.setStatus("ERROR");
            operationLog.setCreatedTime(new Date());
            logService.insert(operationLog);
            
            log.error("获取系统概览数据失败", e);
            return Result.error("获取系统概览数据失败: " + e.getMessage());
        }
    }
} 
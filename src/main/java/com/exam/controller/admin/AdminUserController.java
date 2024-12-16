package com.exam.controller.admin;

import com.exam.entity.Admin;
import com.exam.entity.User;
import com.exam.entity.Log;
import com.exam.entity.dto.AdminDTO;
import com.exam.service.AdminService;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Date;

@Slf4j
@RestController
@RequestMapping("/admin/admins")
@Api(tags = "管理员用户管理接口")
public class AdminUserController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private UserService userService;
    
    @Autowired
    private LogService logService;

    @ApiOperation("获取管理员列表")
    @GetMapping
    public Result getAdminList(@RequestParam(required = false) String keyword,
                              @RequestParam(defaultValue = "1") Integer pageNum,
                              @RequestParam(defaultValue = "10") Integer pageSize,
                              HttpServletRequest request) {
        // 获取当前操作的管理员
        User currentUser = TokenUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.error("用户未登录");
        }
        
        try {
            Map<String, Object> condition = new HashMap<>();
            if (keyword != null && !keyword.trim().isEmpty()) {
                condition.put("name", keyword);
            }
            
            List<Admin> admins = adminService.getPageByCondition(condition, pageNum, pageSize);
            Long total = adminService.getCountByCondition(condition);
            
            Map<String, Object> data = new HashMap<>();
            data.put("list", admins);
            data.put("total", total);
            
            // 记录查询日志
            Log operationLog = new Log();
            operationLog.setUserId(currentUser.getUserId());
            operationLog.setActionType(7); // SELECT
            operationLog.setActionDescription("管理员[" + currentUser.getUsername() + "]查询了管理员列表");
            operationLog.setObjectType("ADMIN");
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
            operationLog.setActionDescription("管理员[" + currentUser.getUsername() + "]查询管理员列表异常：" + e.getMessage());
            operationLog.setObjectType("ADMIN");
            operationLog.setIpAddress(request.getRemoteAddr());
            operationLog.setDeviceInfo(request.getHeader("User-Agent"));
            operationLog.setStatus("ERROR");
            operationLog.setCreatedTime(new Date());
            logService.insert(operationLog);
            
            // 使用Slf4j记录错误日志
            log.error("获取管理员列表失败", e);
            return Result.error("获取管理员列表失败");
        }
    }

    @ApiOperation("更新管理员信息")
    @PutMapping("/{adminId}")
    public Result updateAdmin(@PathVariable Integer adminId, @RequestBody AdminDTO adminDTO, HttpServletRequest request) {
        // 获取当前操作的管理员
        User currentUser = TokenUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.error("用户未登录");
        }
        
        try {
            Admin admin = adminService.getById(adminId);
            if (admin == null) {
                return Result.error("管理员不存在");
            }
            
            // 更新管理员信息
            admin.setName(adminDTO.getName());
            admin.setOther(adminDTO.getOther());
            
            // 更新用户信息
            User user = userService.getById(admin.getUserId());
            user.setPhone(adminDTO.getPhone());
            user.setEmail(adminDTO.getEmail());
            user.setSex(adminDTO.getSex());
            user.setStatus(adminDTO.getStatus());
            
            int result = adminService.updateById(admin);
            if (result <= 0) {
                // 记录失败日志
                Log operationLog = new Log();
                operationLog.setUserId(currentUser.getUserId());
                operationLog.setActionType(1); // UPDATE
                operationLog.setActionDescription("管理员[" + currentUser.getUsername() + "]更新管理员[" + user.getUsername() + "]信息失败");
                operationLog.setObjectType("ADMIN");
                operationLog.setIpAddress(request.getRemoteAddr());
                operationLog.setDeviceInfo(request.getHeader("User-Agent"));
                operationLog.setStatus("FAILED");
                operationLog.setCreatedTime(new Date());
                logService.insert(operationLog);
                
                return Result.error("更新管理员信息失败");
            }
            
            result = userService.updateById(user);
            if (result <= 0) {
                // 记录失败日志
                Log operationLog = new Log();
                operationLog.setUserId(currentUser.getUserId());
                operationLog.setActionType(1); // UPDATE
                operationLog.setActionDescription("管理员[" + currentUser.getUsername() + "]更新管理员[" + user.getUsername() + "]用户信息失败");
                operationLog.setObjectType("ADMIN");
                operationLog.setIpAddress(request.getRemoteAddr());
                operationLog.setDeviceInfo(request.getHeader("User-Agent"));
                operationLog.setStatus("FAILED");
                operationLog.setCreatedTime(new Date());
                logService.insert(operationLog);
                
                return Result.error("更新用户信息失败");
            }
            
            // 记录成功日志
            Log operationLog = new Log();
            operationLog.setUserId(currentUser.getUserId());
            operationLog.setActionType(1); // UPDATE
            operationLog.setActionDescription("管理员[" + currentUser.getUsername() + "]更新了管理员[" + user.getUsername() + "]的信息");
            operationLog.setObjectType("ADMIN");
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
            operationLog.setActionDescription("管理员[" + currentUser.getUsername() + "]更新管理员信息异常：" + e.getMessage());
            operationLog.setObjectType("ADMIN");
            operationLog.setIpAddress(request.getRemoteAddr());
            operationLog.setDeviceInfo(request.getHeader("User-Agent"));
            operationLog.setStatus("ERROR");
            operationLog.setCreatedTime(new Date());
            logService.insert(operationLog);
            
            // 使用Slf4j记录错误日志
            log.error("更新管理员信息失败", e);
            return Result.error("更新管理员信息失败: " + e.getMessage());
        }
    }

    @ApiOperation("删除管理员")
    @DeleteMapping("/{adminId}")
    public Result deleteAdmin(@PathVariable Integer adminId, HttpServletRequest request) {
        // 获取当前操作的管理员
        User currentUser = TokenUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.error("用户未登录");
        }
        
        try {
            Admin admin = adminService.getById(adminId);
            if (admin == null) {
                return Result.error("管理员不存在");
            }
            
            // 获取要删除的管理员用户信息（用于日志记录）
            User targetUser = userService.getById(admin.getUserId());
            
            // 删除管理员记录
            int result = adminService.deleteById(adminId);
            if (result <= 0) {
                // 记录失败日志
                Log operationLog = new Log();
                operationLog.setUserId(currentUser.getUserId());
                operationLog.setActionType(2); // DELETE
                operationLog.setActionDescription("管理员[" + currentUser.getUsername() + "]删除管理员[" + targetUser.getUsername() + "]失败");
                operationLog.setObjectType("ADMIN");
                operationLog.setIpAddress(request.getRemoteAddr());
                operationLog.setDeviceInfo(request.getHeader("User-Agent"));
                operationLog.setStatus("FAILED");
                operationLog.setCreatedTime(new Date());
                logService.insert(operationLog);
                
                return Result.error("删除管理员失败");
            }
            
            // 删除对应的用户记录
            result = userService.deleteById(admin.getUserId());
            if (result <= 0) {
                // 记录失败日志
                Log operationLog = new Log();
                operationLog.setUserId(currentUser.getUserId());
                operationLog.setActionType(2); // DELETE
                operationLog.setActionDescription("管理员[" + currentUser.getUsername() + "]删除管理员[" + targetUser.getUsername() + "]用户信息失败");
                operationLog.setObjectType("ADMIN");
                operationLog.setIpAddress(request.getRemoteAddr());
                operationLog.setDeviceInfo(request.getHeader("User-Agent"));
                operationLog.setStatus("FAILED");
                operationLog.setCreatedTime(new Date());
                logService.insert(operationLog);
                
                return Result.error("删除用户失败");
            }
            
            // 记录成功日志
            Log operationLog = new Log();
            operationLog.setUserId(currentUser.getUserId());
            operationLog.setActionType(2); // DELETE
            operationLog.setActionDescription("管理员[" + currentUser.getUsername() + "]删除了管理员[" + targetUser.getUsername() + "]");
            operationLog.setObjectType("ADMIN");
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
            operationLog.setActionDescription("管理员[" + currentUser.getUsername() + "]删除管理员异常：" + e.getMessage());
            operationLog.setObjectType("ADMIN");
            operationLog.setIpAddress(request.getRemoteAddr());
            operationLog.setDeviceInfo(request.getHeader("User-Agent"));
            operationLog.setStatus("ERROR");
            operationLog.setCreatedTime(new Date());
            logService.insert(operationLog);
            
            // 使用Slf4j记录错误日志
            log.error("删除管理员失败", e);
            return Result.error("删除管理员失败: " + e.getMessage());
        }
    }

    @ApiOperation("获取管理员详情")
    @GetMapping("/{adminId}")
    public Result getAdminDetail(@PathVariable Integer adminId, HttpServletRequest request) {
        // 获取当前操作的管理员
        User currentUser = TokenUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.error("用户未登录");
        }
        
        try {
            Admin admin = adminService.getById(adminId);
            if (admin == null) {
                return Result.error("管理员不存在");
            }
            
            // 获取关联的用户信息
            User user = userService.getById(admin.getUserId());
            
            // 构建返回的DTO对象
            AdminDTO adminDTO = new AdminDTO();
            adminDTO.setAdminId(admin.getAdminId());
            adminDTO.setName(admin.getName());
            adminDTO.setOther(admin.getOther());
            adminDTO.setUsername(user.getUsername());
            adminDTO.setPhone(user.getPhone());
            adminDTO.setEmail(user.getEmail());
            adminDTO.setSex(user.getSex());
            adminDTO.setStatus(user.getStatus());
            
            // 记录查询日志
            Log operationLog = new Log();
            operationLog.setUserId(currentUser.getUserId());
            operationLog.setActionType(7); // SELECT
            operationLog.setActionDescription("管理员[" + currentUser.getUsername() + "]查看了管理员[" + user.getUsername() + "]的详细信息");
            operationLog.setObjectType("ADMIN");
            operationLog.setIpAddress(request.getRemoteAddr());
            operationLog.setDeviceInfo(request.getHeader("User-Agent"));
            operationLog.setStatus("SUCCESS");
            operationLog.setCreatedTime(new Date());
            logService.insert(operationLog);
            
            return Result.success(adminDTO);
        } catch (Exception e) {
            // 记录异常日志
            Log operationLog = new Log();
            operationLog.setUserId(currentUser.getUserId());
            operationLog.setActionType(5); // EXCEPTION
            operationLog.setActionDescription("管理员[" + currentUser.getUsername() + "]查看管理员详情异常：" + e.getMessage());
            operationLog.setObjectType("ADMIN");
            operationLog.setIpAddress(request.getRemoteAddr());
            operationLog.setDeviceInfo(request.getHeader("User-Agent"));
            operationLog.setStatus("ERROR");
            operationLog.setCreatedTime(new Date());
            logService.insert(operationLog);
            
            // 使用Slf4j记录错误日志
            log.error("获取管理员详情失败", e);
            return Result.error("获取管理员详情失败: " + e.getMessage());
        }
    }

    @ApiOperation("修改管理员状态")
    @PutMapping("/{adminId}/status")
    public Result updateStatus(@PathVariable Integer adminId, @RequestParam Boolean status, HttpServletRequest request) {
        // 获取当前操作的管理员
        User currentUser = TokenUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.error("用户未登录");
        }
        
        try {
            Admin admin = adminService.getById(adminId);
            if (admin == null) {
                return Result.error("管理员不存在");
            }
            
            // 获取要修改状态的管理员用户信息
            User targetUser = userService.getById(admin.getUserId());
            
            // 更新用户状态
            targetUser.setStatus(status);
            int result = userService.updateById(targetUser);
            if (result <= 0) {
                // 记录失败日志
                Log operationLog = new Log();
                operationLog.setUserId(currentUser.getUserId());
                operationLog.setActionType(1); // UPDATE
                operationLog.setActionDescription("管理员[" + currentUser.getUsername() + "]" + 
                    (status ? "启用" : "禁用") + "管理员[" + targetUser.getUsername() + "]失败");
                operationLog.setObjectType("ADMIN");
                operationLog.setIpAddress(request.getRemoteAddr());
                operationLog.setDeviceInfo(request.getHeader("User-Agent"));
                operationLog.setStatus("FAILED");
                operationLog.setCreatedTime(new Date());
                logService.insert(operationLog);
                
                return Result.error("更新状态失败");
            }
            
            // 记录成功日志
            Log operationLog = new Log();
            operationLog.setUserId(currentUser.getUserId());
            operationLog.setActionType(1); // UPDATE
            operationLog.setActionDescription("管理员[" + currentUser.getUsername() + "]" + 
                (status ? "启用" : "禁用") + "了管理员[" + targetUser.getUsername() + "]");
            operationLog.setObjectType("ADMIN");
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
            operationLog.setActionDescription("管理员[" + currentUser.getUsername() + "]修改管理员状态异常：" + e.getMessage());
            operationLog.setObjectType("ADMIN");
            operationLog.setIpAddress(request.getRemoteAddr());
            operationLog.setDeviceInfo(request.getHeader("User-Agent"));
            operationLog.setStatus("ERROR");
            operationLog.setCreatedTime(new Date());
            logService.insert(operationLog);
            
            // 使用Slf4j记录错误日志
            log.error("修改管理员状态失败", e);
            return Result.error("修改管理员状态失败: " + e.getMessage());
        }
    }
} 
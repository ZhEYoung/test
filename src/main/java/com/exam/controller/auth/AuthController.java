package com.exam.controller.auth;

import cn.hutool.crypto.SecureUtil;
import com.exam.common.Constants;
import com.exam.common.Result;
import com.exam.entity.User;
import com.exam.entity.Log;
import com.exam.entity.dto.LoginDTO;
import com.exam.entity.dto.StaffRegisterDTO;
import com.exam.entity.dto.StudentDTO;
import com.exam.service.LogService;
import com.exam.service.UserService;
import com.exam.utils.TokenUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@Api(tags = "认证接口")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private LogService logService;

    @Autowired
    private TokenUtils tokenUtils;

    @PostMapping("/login")
    @ApiOperation("用户登录")
    public Result login(@Valid @RequestBody LoginDTO loginDTO, HttpServletRequest request) {
        try {
            // 1. 验证用户名和密码
            User user = userService.login(loginDTO.getUsername(), loginDTO.getPassword());
            if (user == null) {
                // 记录登录失败日志
                Log log = new Log();
                log.setActionType(3); // LOGIN
                log.setActionDescription("用户[" + loginDTO.getUsername() + "]登录失败：用户名或密码错误");
                log.setObjectType("USER");
                log.setIpAddress(request.getRemoteAddr());
                log.setDeviceInfo(request.getHeader("User-Agent"));
                log.setStatus("FAILED");
                log.setCreatedTime(new Date());
                logService.insert(log);
                
                return Result.error("用户名或密码错误");
            }

            // 2. 生成 token
            String token = TokenUtils.genToken(user.getUserId().toString(), SecureUtil.sha256(loginDTO.getPassword()));

            // 3. 记录登录成功日志
            Log log = new Log();
            log.setUserId(user.getUserId());
            log.setActionType(3); // LOGIN
            log.setActionDescription("用户[" + user.getUsername() + "]登录成功");
            log.setObjectType("USER");
            log.setIpAddress(request.getRemoteAddr());
            log.setDeviceInfo(request.getHeader("User-Agent"));
            log.setStatus("SUCCESS");
            log.setCreatedTime(new Date());
            logService.insert(log);

            // 4. 返回用户信息和 token
            Map<String, Object> data = new HashMap<>();
            data.put("user", user);
            data.put("token", token);
            return Result.success(data);
        } catch (Exception e) {
            // 记录异常日志
            Log log = new Log();
            log.setActionType(5); // EXCEPTION
            log.setActionDescription("用户[" + loginDTO.getUsername() + "]登录异常：" + e.getMessage());
            log.setObjectType("USER");
            log.setIpAddress(request.getRemoteAddr());
            log.setDeviceInfo(request.getHeader("User-Agent"));
            log.setStatus("ERROR");
            log.setCreatedTime(new Date());
            logService.insert(log);
            
            return Result.error("登录失败：" + e.getMessage());
        }
    }

    @PostMapping("/register/student")
    @ApiOperation("学生自主注册")
    public Result registerStudent(@Valid @RequestBody StudentDTO studentDTO, HttpServletRequest request) {
        try {
            // 1. 创建用户对象
            User user = new User();
            user.setUsername(studentDTO.getUsername());
            user.setPassword(studentDTO.getPassword());
            user.setRole(User.ROLE_STUDENT);
            user.setStatus(true);
            user.setSex(studentDTO.getSex());
            user.setPhone(studentDTO.getPhone());
            user.setEmail(studentDTO.getEmail());
            user.setCreatedTime(new Date());

            // 2. 注册学生
            int result = userService.registerStudent(user, studentDTO);
            if (result > 0) {
                // 记录注册成功日志
                Log log = new Log();
                log.setUserId(user.getUserId());
                log.setActionType(0); // INSERT
                log.setActionDescription("新学生[" + user.getUsername() + "]注册成功");
                log.setObjectType("STUDENT");
                log.setIpAddress(request.getRemoteAddr());
                log.setDeviceInfo(request.getHeader("User-Agent"));
                log.setStatus("SUCCESS");
                log.setCreatedTime(new Date());
                logService.insert(log);
                
                return Result.success("注册成功");
            } else {
                return Result.error("注册失败");
            }
        } catch (Exception e) {
            // 记录注册异常日志
            Log log = new Log();
            log.setActionType(5); // EXCEPTION
            log.setActionDescription("学生[" + studentDTO.getUsername() + "]注册异常：" + e.getMessage());
            log.setObjectType("STUDENT");
            log.setIpAddress(request.getRemoteAddr());
            log.setDeviceInfo(request.getHeader("User-Agent"));
            log.setStatus("ERROR");
            log.setCreatedTime(new Date());
            logService.insert(log);
            
            return Result.error("注册失败：" + e.getMessage());
        }
    }

    @PostMapping("/register/staff")
    @ApiOperation("管理员创建教师或管理员账号")
    public Result registerStaff(@Valid @RequestBody StaffRegisterDTO staffDTO, HttpServletRequest request) {
        // 1. 验证当前用户是否为管理员
        User currentUser = TokenUtils.getCurrentUser();
        if (currentUser == null || !currentUser.getRole().equals(User.ROLE_ADMIN)) {
            return Result.error("无权限创建教师或管理员账号");
        }

        try {
            // 2. 创建用户对象
            User user = new User();
            user.setUsername(staffDTO.getUsername());
            user.setPassword(staffDTO.getPassword());
            user.setRole(staffDTO.getRole());
            user.setStatus(true);
            user.setSex(staffDTO.getSex());
            user.setPhone(staffDTO.getPhone());
            user.setEmail(staffDTO.getEmail());
            user.setCreatedTime(new Date());

            // 3. 注册教师或管理员
            int result = userService.registerStaff(user, staffDTO);
            if (result > 0) {
                // 记录创建成功日志
                Log log = new Log();
                log.setUserId(currentUser.getUserId());
                log.setActionType(0); // INSERT
                log.setActionDescription("管理员[" + currentUser.getUsername() + "]创建了" + 
                    (user.getRole() == User.ROLE_ADMIN ? "管理员" : "教师") + "[" + user.getUsername() + "]账号");
                log.setObjectType(user.getRole() == User.ROLE_ADMIN ? "ADMIN" : "TEACHER");
                log.setIpAddress(request.getRemoteAddr());
                log.setDeviceInfo(request.getHeader("User-Agent"));
                log.setStatus("SUCCESS");
                log.setCreatedTime(new Date());
                logService.insert(log);
                
                return Result.success("创建成功");
            } else {
                return Result.error("创建失败");
            }
        } catch (Exception e) {
            // 记录创建异常日志
            Log log = new Log();
            log.setUserId(currentUser.getUserId());
            log.setActionType(5); // EXCEPTION
            log.setActionDescription("管理员[" + currentUser.getUsername() + "]创建" + 
                (staffDTO.getRole() == User.ROLE_ADMIN ? "管理员" : "教师") + "账号异常：" + e.getMessage());
            log.setObjectType(staffDTO.getRole() == User.ROLE_ADMIN ? "ADMIN" : "TEACHER");
            log.setIpAddress(request.getRemoteAddr());
            log.setDeviceInfo(request.getHeader("User-Agent"));
            log.setStatus("ERROR");
            log.setCreatedTime(new Date());
            logService.insert(log);
            
            return Result.error("创建失败：" + e.getMessage());
        }
    }

    @PostMapping("/logout")
    @ApiOperation("用户登出")
    public Result logout(HttpServletRequest request) {
        // 1. 获取当前用户
        User currentUser = TokenUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.error("用户未登录");
        }

        try {
            // 2. 记录登出日志
            Log log = new Log();
            log.setUserId(currentUser.getUserId());
            log.setActionType(3); // LOGIN
            log.setActionDescription("用户[" + currentUser.getUsername() + "]登出成功");
            log.setObjectType("USER");
            log.setIpAddress(request.getRemoteAddr());
            log.setDeviceInfo(request.getHeader("User-Agent"));
            log.setStatus("SUCCESS");
            log.setCreatedTime(new Date());
            logService.insert(log);

            return Result.success("登出成功");
        } catch (Exception e) {
            // 记录登出异常日志
            Log log = new Log();
            log.setUserId(currentUser.getUserId());
            log.setActionType(5); // EXCEPTION
            log.setActionDescription("用户[" + currentUser.getUsername() + "]登出异常：" + e.getMessage());
            log.setObjectType("USER");
            log.setIpAddress(request.getRemoteAddr());
            log.setDeviceInfo(request.getHeader("User-Agent"));
            log.setStatus("ERROR");
            log.setCreatedTime(new Date());
            logService.insert(log);
            
            return Result.error("登出失败：" + e.getMessage());
        }
    }

    @GetMapping("/current-user")
    @ApiOperation("获取当前登录用户信息")
    public Result getCurrentUser() {
        User currentUser = TokenUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.error("用户未登录");
        }
        return Result.success(currentUser);
    }

    @PostMapping("/change-password")
    @ApiOperation("修改密码")
    public Result changePassword(@RequestParam String oldPassword,
                               @RequestParam String newPassword,
                               HttpServletRequest request) {
        // 1. 获取当前用户
        User currentUser = TokenUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.error("用户未登录");
        }

        try {
            // 2. 修改密码
            int result = userService.updatePassword(currentUser.getUserId(), oldPassword, newPassword);
            if (result > 0) {
                // 记录密码修改成功日志
                Log log = new Log();
                log.setUserId(currentUser.getUserId());
                log.setActionType(1); // UPDATE
                log.setActionDescription("用户[" + currentUser.getUsername() + "]修改密码成功");
                log.setObjectType("USER");
                log.setIpAddress(request.getRemoteAddr());
                log.setDeviceInfo(request.getHeader("User-Agent"));
                log.setStatus("SUCCESS");
                log.setCreatedTime(new Date());
                logService.insert(log);
                
                return Result.success("密码修改成功");
            } else {
                // 记录密码修改失败日志
                Log log = new Log();
                log.setUserId(currentUser.getUserId());
                log.setActionType(1); // UPDATE
                log.setActionDescription("用户[" + currentUser.getUsername() + "]修改密码失败：原密码错误");
                log.setObjectType("USER");
                log.setIpAddress(request.getRemoteAddr());
                log.setDeviceInfo(request.getHeader("User-Agent"));
                log.setStatus("FAILED");
                log.setCreatedTime(new Date());
                logService.insert(log);
                
                return Result.error("原密码错误");
            }
        } catch (Exception e) {
            // 记录密码修改异常日志
            Log log = new Log();
            log.setUserId(currentUser.getUserId());
            log.setActionType(5); // EXCEPTION
            log.setActionDescription("用户[" + currentUser.getUsername() + "]修改密码异常：" + e.getMessage());
            log.setObjectType("USER");
            log.setIpAddress(request.getRemoteAddr());
            log.setDeviceInfo(request.getHeader("User-Agent"));
            log.setStatus("ERROR");
            log.setCreatedTime(new Date());
            logService.insert(log);
            
            return Result.error("密码修改失败：" + e.getMessage());
        }
    }
} 
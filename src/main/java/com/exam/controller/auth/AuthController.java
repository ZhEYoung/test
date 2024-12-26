package com.exam.controller.auth;

import cn.hutool.crypto.SecureUtil;
import com.exam.common.Constants;
import com.exam.common.Result;
import com.exam.common.ServiceException;
import com.exam.entity.College;
import com.exam.entity.User;
import com.exam.entity.Log;
import com.exam.entity.dto.LoginDTO;
import com.exam.entity.dto.StaffRegisterDTO;
import com.exam.entity.dto.StudentDTO;
import com.exam.service.CollegeService;
import com.exam.service.EmailService;
import com.exam.service.LogService;
import com.exam.service.UserService;
import com.exam.utils.TokenUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.*;

@Slf4j
@RestController
@RequestMapping(value = "/auth", produces = "application/json;charset=UTF-8")
@Api(tags = "认证接口")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private LogService logService;

    @Autowired
    private TokenUtils tokenUtils;

    @Autowired
    private CollegeService collegeService;

    @Autowired
    private EmailService emailService;

    @PostMapping("/login")
    @ApiOperation("用户登录")
    public Result login(@Valid @RequestBody LoginDTO loginDTO, HttpServletRequest request) {
        try {
            // 1. 验证用户名和密码
            User user = userService.login(loginDTO.getUsername(), loginDTO.getPassword());
            if (user == null) {

                User user1 = userService.getByUsername(loginDTO.getUsername());
                // 记录登录失败日志
                Log log = new Log();
                log.setUserId(user1.getUserId());
                log.setActionType(3); // LOGIN
                log.setActionDescription("用户[" + loginDTO.getUsername() + "]登录失败：用户名或密码错误");
                log.setObjectType("USER");
                log.setIpAddress(request.getRemoteAddr());
                log.setDeviceInfo(request.getHeader("User-Agent"));
                log.setStatus("FAILED");
                log.setCreatedTime(new Date());
                logService.insert(log);

                return Result.error(501,"用户名或密码错误");
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
            User user1 = userService.getByUsername(loginDTO.getUsername());
            // 记录异常日志
            Log log = new Log();
            log.setUserId(user1.getUserId());
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

            if(userService.getByUsername(user.getUsername()) != null){
                return Result.error("用户名已存在");
            }
            if(userService.getByEmail(user.getEmail()) != null){
                return Result.error("邮箱已存在");
            }

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

    @GetMapping("/college")
    @ApiOperation("获取当前学院信息")
    public Result getCollege() {
        List<College> colleges = collegeService.getAll();
        return Result.success(colleges);
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

    @ApiOperation("找回学生密码")
    @PostMapping("/password/reset")
    public Result resetPassword(@RequestParam String username,
                                @RequestParam String email,
                                @RequestParam String phone,
             HttpServletRequest request) {
        try {

            // 验证用户信息
            User user = userService.getByUsername(username);
            if (user == null) {
                return Result.error("用户不存在");
            }

            // 验证邮箱和手机号
            if (!email.equals(user.getEmail()) || !phone.equals(user.getPhone())) {
                return Result.error("邮箱或手机号验证失败");
            }

            // 生成随机密码
            String newPassword = UUID.randomUUID().toString().substring(0, 8);

            // 更新密码
            int result = userService.updatePasswordWithoutPassword(user.getUserId(), newPassword);

            if (result <= 0) {
                // 记录失败日志
                Log operationLog = new Log();
                operationLog.setUserId(user.getUserId());
                operationLog.setActionType(1); // UPDATE
                operationLog.setActionDescription("学生[" + username + "]重置密码失败");
                operationLog.setObjectType("STUDENT");
                operationLog.setIpAddress(request.getRemoteAddr());
                operationLog.setDeviceInfo(request.getHeader("User-Agent"));
                operationLog.setStatus("FAILED");
                operationLog.setCreatedTime(new Date());
                logService.insert(operationLog);

                return Result.error("重置密码失败");
            }

            // 发送新密码到邮箱
            String subject = "考试系统 - 密码重置通知";
            String content = String.format(
                    "尊敬的%s：\n\n" +
                            "您的密码已经重置。新密码为：%s\n\n" +
                            "为了您的账户安全，请登录后立即修改密码。\n\n" +
                            "如果这不是您本人的操作，请立即联系管理员。\n\n" +
                            "此致\n" +
                            "考试系统管理团队",
                    username, newPassword
            );

            boolean emailSent = emailService.sendSimpleMail(email, subject, content);
            if (!emailSent) {
                log.error("发送重置密码邮件失败，用户：{}", username);
            }

            // 记录成功日志
            Log operationLog = new Log();
            operationLog.setUserId(user.getUserId());
            operationLog.setActionType(1); // UPDATE
            operationLog.setActionDescription("学生[" + username + "]重置密码成功");
            operationLog.setObjectType("STUDENT");
            operationLog.setIpAddress(request.getRemoteAddr());
            operationLog.setDeviceInfo(request.getHeader("User-Agent"));
            operationLog.setStatus("SUCCESS");
            operationLog.setCreatedTime(new Date());
            logService.insert(operationLog);

            Map<String, String> data = new HashMap<>();
            data.put("newPassword", newPassword);
            return Result.success(data);
        } catch (Exception e) {
            log.error("找回密码失败", e);
            return Result.error("找回密码失败：" + e.getMessage());
        }
    }
} 
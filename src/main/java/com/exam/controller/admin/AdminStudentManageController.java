package com.exam.controller.admin;

import com.exam.entity.*;
import com.exam.entity.Class;
import com.exam.entity.dto.StudentDTO;
import com.exam.mapper.StudentMapper;
import com.exam.service.*;
import com.exam.utils.TokenUtils;
import org.springframework.web.bind.annotation.*;
import com.exam.common.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Date;



@Slf4j
@RestController
@RequestMapping("/admin/students")
@Api(tags = "学生管理接口")
public class AdminStudentManageController {

    @Autowired
    private StudentService studentService;

    @Autowired
    private LogService logService;

    @Autowired
    private UserService userService;

    @Autowired
    private CollegeService collegeService;

    @Autowired
    private StudentMapper studentMapper;

    @Autowired
    private ClassService classService;

    @ApiOperation("获取学生列表")
    @GetMapping
    public Result getStudentList(@RequestParam(required = false) String keyword,
                                @RequestParam(required = false) Integer collegeId,
                                @RequestParam(required = false) String grade,
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
                condition.put("name", keyword);
            }
            if (collegeId != null) {
                condition.put("collegeId", collegeId);
            }
            if (grade != null && !grade.trim().isEmpty()) {
                condition.put("grade", grade);
            }

            List<Student> students = studentService.getPageByCondition(condition, pageNum, pageSize);
            for(Student student: students){
                User user = userService.getById(student.getUserId());
                student.setUser(user);
                College college = collegeService.getById(student.getCollegeId());
                student.setCollege(college);
            }
            Long total = studentMapper.selectCountByCondition(condition);
            List<College> colleges = collegeService.getAll();


            Map<String, Object> data = new HashMap<>();
            data.put("records", students);
            data.put("colleges", colleges);
            data.put("total", total);


            // 记录操作日志
            Log operationLog = new Log();
            operationLog.setUserId(currentUser.getUserId());
            operationLog.setActionType(7); // SELECT
            operationLog.setActionDescription("管理员[" + currentUser.getUsername() + "]查看了学生列表");
            operationLog.setObjectType("STUDENT_LIST");
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
            operationLog.setActionDescription("管理员[" + currentUser.getUsername() + "]查看学生列表异常：" + e.getMessage());
            operationLog.setObjectType("STUDENT_LIST");
            operationLog.setIpAddress(request.getRemoteAddr());
            operationLog.setDeviceInfo(request.getHeader("User-Agent"));
            operationLog.setStatus("ERROR");
            operationLog.setCreatedTime(new Date());
            logService.insert(operationLog);

            log.error("获取学生列表失败", e);
            return Result.error("获取学生列表失败：" + e.getMessage());
        }
    }

    @ApiOperation("添加学生")
    @PostMapping
    public Result addStudent(@RequestBody StudentDTO studentDTO, HttpServletRequest request) {
        User currentUser = TokenUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.error("用户未登录");
        }

        try {
            // 1. 先创建用户
            User user = new User();
            user.setUsername(studentDTO.getUsername());
            user.setPassword(studentDTO.getPassword());
            user.setSex(studentDTO.getSex());
            user.setRole(2); // 学生角色
            user.setEmail(studentDTO.getEmail());
            user.setPhone(studentDTO.getPhone());
            user.setStatus(studentDTO.getStatus());
            user.setCreatedTime(new Date());

            int userResult = userService.insert(user);
            if (userResult <= 0) {
                return Result.error("用户创建失败");
            }

            // 2. 再创建学生信息
            Student student = new Student();
            student.setUserId(user.getUserId());  // 使用新创建的用户ID
            student.setGrade(studentDTO.getGrade());
            student.setCollegeId(studentDTO.getCollegeId());
            student.setOther(studentDTO.getOther());
            student.setName(studentDTO.getName());

            int studentResult = studentService.insert(student);
            if (studentResult <= 0) {
                // 如果学生信息创建失败，需要回滚用户创建
                userService.deleteById(user.getUserId());
                return Result.error("学生信息创建失败");
            }

            // 记录成功日志
            Log operationLog = new Log();
            operationLog.setUserId(currentUser.getUserId());
            operationLog.setActionType(0); // INSERT
            operationLog.setActionDescription("管理员[" + currentUser.getUsername() + "]添加了学生[" + student.getName() + "]");
            operationLog.setObjectType("STUDENT");
            operationLog.setIpAddress(request.getRemoteAddr());
            operationLog.setDeviceInfo(request.getHeader("User-Agent"));
            operationLog.setStatus("SUCCESS");
            operationLog.setCreatedTime(new Date());
            logService.insert(operationLog);

            return Result.success("添加成功");
        } catch (Exception e) {
            // 记录异常日志
            Log operationLog = new Log();
            operationLog.setUserId(currentUser.getUserId());
            operationLog.setActionType(5); // EXCEPTION
            operationLog.setActionDescription("管理员[" + currentUser.getUsername() + "]添加学生异常：" + e.getMessage());
            operationLog.setObjectType("STUDENT");
            operationLog.setIpAddress(request.getRemoteAddr());
            operationLog.setDeviceInfo(request.getHeader("User-Agent"));
            operationLog.setStatus("ERROR");
            operationLog.setCreatedTime(new Date());
            logService.insert(operationLog);

            log.error("添加学生失败", e);
            return Result.error("添加学生失败：" + e.getMessage());
        }
    }

    @ApiOperation("更新学生信息")
    @PutMapping("/{studentId}")
    public Result updateStudent(@PathVariable Integer studentId, 
                              @RequestBody StudentDTO studentDTO,
                              HttpServletRequest request) {
        User currentUser = TokenUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.error("用户未登录");
        }

        try {
            Student student = studentService.getById(studentId);
            if (student == null) {
                return Result.error("学生不存在");
            }

            // 1. 更新用户信息
            User user = userService.getById(student.getUserId());
            if (user == null) {
                return Result.error("用户信息不存在");
            }

            // 只更新允许修改的字段
            if (studentDTO.getEmail() != null) {
                user.setEmail(studentDTO.getEmail());
            }
            if (studentDTO.getPhone() != null) {
                user.setPhone(studentDTO.getPhone());
            }
            if (studentDTO.getSex() != null) {
                user.setSex(studentDTO.getSex());
            }
            if (studentDTO.getStatus() != null) {
                user.setStatus(studentDTO.getStatus());
            }

            int userResult = userService.updateById(user);
            if (userResult <= 0) {
                return Result.error("用户信息更新失败");
            }

            // 2. 更新学生信息
            if (studentDTO.getName() != null) {
                student.setName(studentDTO.getName());
            }
            if (studentDTO.getGrade() != null) {
                student.setGrade(studentDTO.getGrade());
            }
            if (studentDTO.getCollegeId() != null) {
                student.setCollegeId(studentDTO.getCollegeId());
            }
            if (studentDTO.getOther() != null) {
                student.setOther(studentDTO.getOther());
            }

            int studentResult = studentService.updateById(student);
            if (studentResult <= 0) {
                return Result.error("学生信息更新失败");
            }

            // 记录成功日志
            Log operationLog = new Log();
            operationLog.setUserId(currentUser.getUserId());
            operationLog.setActionType(1); // UPDATE
            operationLog.setActionDescription("管理员[" + currentUser.getUsername() + "]更新了学生[" + student.getName() + "]的信息");
            operationLog.setObjectType("STUDENT");
            operationLog.setIpAddress(request.getRemoteAddr());
            operationLog.setDeviceInfo(request.getHeader("User-Agent"));
            operationLog.setStatus("SUCCESS");
            operationLog.setCreatedTime(new Date());
            logService.insert(operationLog);

            return Result.success("更新成功");
        } catch (Exception e) {
            // 记录异常日志
            Log operationLog = new Log();
            operationLog.setUserId(currentUser.getUserId());
            operationLog.setActionType(5); // EXCEPTION
            operationLog.setActionDescription("管理员[" + currentUser.getUsername() + "]更新学生信息异常：" + e.getMessage());
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

    @ApiOperation("获取学生详情")
    @GetMapping("/{studentId}")
    public Result getStudentDetail(@PathVariable Integer studentId, HttpServletRequest request) {
        User currentUser = TokenUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.error("用户未登录");
        }

        try {
            Student student = studentService.getById(studentId);
            if (student == null) {
                return Result.error("学生不存在");
            }
            student.setUser(userService.getById(student.getUserId()));
            student.setCollege(collegeService.getById(student.getCollegeId()));

            // 记录操作日志
            Log operationLog = new Log();
            operationLog.setUserId(currentUser.getUserId());
            operationLog.setActionType(7); // SELECT
            operationLog.setActionDescription("管理员[" + currentUser.getUsername() + "]查看了学生[" + student.getName() + "]的详细信息");
            operationLog.setObjectType("STUDENT_DETAIL");
            operationLog.setIpAddress(request.getRemoteAddr());
            operationLog.setDeviceInfo(request.getHeader("User-Agent"));
            operationLog.setStatus("SUCCESS");
            operationLog.setCreatedTime(new Date());
            logService.insert(operationLog);

            return Result.success(student);
        } catch (Exception e) {
            // 记录异常日志
            Log operationLog = new Log();
            operationLog.setUserId(currentUser.getUserId());
            operationLog.setActionType(5); // EXCEPTION
            operationLog.setActionDescription("管理员[" + currentUser.getUsername() + "]查看学生详情异常：" + e.getMessage());
            operationLog.setObjectType("STUDENT_DETAIL");
            operationLog.setIpAddress(request.getRemoteAddr());
            operationLog.setDeviceInfo(request.getHeader("User-Agent"));
            operationLog.setStatus("ERROR");
            operationLog.setCreatedTime(new Date());
            logService.insert(operationLog);

            log.error("获取学生详情失败", e);
            return Result.error("获取学生详情失败：" + e.getMessage());
        }
    }


    @ApiOperation("获取学生所在班级列表")
    @GetMapping("/{studentId}/classes")
    public Result getStudentClasses(@PathVariable Integer studentId, HttpServletRequest request) {
        User currentUser = TokenUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.error("用户未登录");
        }

        try {
            Student student = studentService.getById(studentId);
            if (student == null) {
                return Result.error("学生不存在");
            }

            List<StudentClass> classes = studentService.getStudentClasses(studentId);

            for(StudentClass studentClass: classes){
                Class class1 = classService.getById(studentClass.getClassId());
                studentClass.setClazz(class1);
            }

            // 记录操作日志
            Log operationLog = new Log();
            operationLog.setUserId(currentUser.getUserId());
            operationLog.setActionType(7); // SELECT
            operationLog.setActionDescription("管理员[" + currentUser.getUsername() + "]查看了学生[" + student.getName() + "]的班级列表");
            operationLog.setObjectType("STUDENT_CLASSES");
            operationLog.setIpAddress(request.getRemoteAddr());
            operationLog.setDeviceInfo(request.getHeader("User-Agent"));
            operationLog.setStatus("SUCCESS");
            operationLog.setCreatedTime(new Date());
            logService.insert(operationLog);

            return Result.success(classes);
        } catch (Exception e) {
            // 记录异常日志
            Log operationLog = new Log();
            operationLog.setUserId(currentUser.getUserId());
            operationLog.setActionType(5); // EXCEPTION
            operationLog.setActionDescription("管理员[" + currentUser.getUsername() + "]查看学生班级列表异常：" + e.getMessage());
            operationLog.setObjectType("STUDENT_CLASSES");
            operationLog.setIpAddress(request.getRemoteAddr());
            operationLog.setDeviceInfo(request.getHeader("User-Agent"));
            operationLog.setStatus("ERROR");
            operationLog.setCreatedTime(new Date());
            logService.insert(operationLog);

            log.error("获取学生班级列表失败", e);
            return Result.error("获取学生班级列表失败：" + e.getMessage());
        }
    }

    @ApiOperation("修改学生状态")
    @PutMapping("/{studentId}/status")
    public Result updateStatus(@PathVariable Integer studentId,
                               @RequestParam Boolean status,
                               HttpServletRequest request) {
        User currentUser = TokenUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.error("用户未登录");
        }

        try {
            Student student = studentService.getById(studentId);
            if (student == null) {
                return Result.error("学生不存在");
            }

            User user = userService.getById(student.getUserId());
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
                    (status ? "启用" : "禁用") + "了学生[" + user.getUsername() + "]的账号");
            operationLog.setObjectType("STUDENT_STATUS");
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
            operationLog.setActionDescription("管理员[" + currentUser.getUsername() + "]修改学生状态异常：" + e.getMessage());
            operationLog.setObjectType("STUDENT_STATUS");
            operationLog.setIpAddress(request.getRemoteAddr());
            operationLog.setDeviceInfo(request.getHeader("User-Agent"));
            operationLog.setStatus("ERROR");
            operationLog.setCreatedTime(new Date());
            logService.insert(operationLog);

            log.error("修改学生状态失败", e);
            return Result.error("修改学生状态失败：" + e.getMessage());
        }
    }
} 
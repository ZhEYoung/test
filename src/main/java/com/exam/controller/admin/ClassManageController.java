package com.exam.controller.admin;

import com.exam.entity.dto.ClassDTO;
import com.exam.entity.Class;
import com.exam.entity.Student;
import com.exam.service.ClassService;
import com.exam.service.StudentClassService;
import com.exam.service.ExamClassService;
import com.exam.service.LogService;
import com.exam.entity.Exam;
import com.exam.entity.User;
import com.exam.entity.Log;
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
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Date;

@Slf4j
@RestController
@RequestMapping("/admin/classes")
@Api(tags = "班级管理接口")
public class ClassManageController {

    @Autowired
    private ClassService classService;

    @Autowired
    private StudentClassService studentClassService;

    @Autowired
    private ExamClassService examClassService;

    @Autowired
    private LogService logService;

    @ApiOperation("获取班级列表")
    @GetMapping
    public Result getClassList(@RequestParam(required = false) String keyword,
                              @RequestParam(required = false) Integer collegeId,
                              @RequestParam(required = false) Integer subjectId,
                              @RequestParam(required = false) Integer teacherId,
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
                condition.put("keyword", keyword);
            }
            if (collegeId != null) {
                condition.put("collegeId", collegeId);
            }
            if (subjectId != null) {
                condition.put("subjectId", subjectId);
            }
            if (teacherId != null) {
                condition.put("teacherId", teacherId);
            }
            
            List<Class> classes = classService.getPageByCondition(condition, pageNum, pageSize);
            Long total = classService.getCountByCondition(condition);
            
            Map<String, Object> data = new HashMap<>();
            data.put("list", classes);
            data.put("total", total);

            // 记录查询日志
            Log operationLog = new Log();
            operationLog.setUserId(currentUser.getUserId());
            operationLog.setActionType(7); // SELECT
            operationLog.setActionDescription("用户[" + currentUser.getUsername() + "]查询了班级列表");
            operationLog.setObjectType("CLASS");
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
            operationLog.setActionDescription("用户[" + currentUser.getUsername() + "]查询班级列表异常：" + e.getMessage());
            operationLog.setObjectType("CLASS");
            operationLog.setIpAddress(request.getRemoteAddr());
            operationLog.setDeviceInfo(request.getHeader("User-Agent"));
            operationLog.setStatus("ERROR");
            operationLog.setCreatedTime(new Date());
            logService.insert(operationLog);

            log.error("获取班级列表失败", e);
            return Result.error("获取班级列表失败：" + e.getMessage());
        }
    }

    @ApiOperation("获取班级学生列表")
    @GetMapping("/{classId}/students")
    public Result getClassStudents(@PathVariable Integer classId,
                                  @RequestParam(defaultValue = "1") Integer pageNum,
                                  @RequestParam(defaultValue = "10") Integer pageSize,
                                  HttpServletRequest request) {
        User currentUser = TokenUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.error("用户未登录");
        }

        try {
            List<Student> students = studentClassService.getActiveStudents(classId);
            int total = studentClassService.countStudentsByClass(classId);
            
            Map<String, Object> data = new HashMap<>();
            data.put("list", students);
            data.put("total", total);

            // 记录查询日志
            Log operationLog = new Log();
            operationLog.setUserId(currentUser.getUserId());
            operationLog.setActionType(7); // SELECT
            operationLog.setActionDescription("用户[" + currentUser.getUsername() + "]查询了班级学生列表，班级ID：" + classId);
            operationLog.setObjectType("CLASS_STUDENT");
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
            operationLog.setActionDescription("用户[" + currentUser.getUsername() + "]查询班级学生列表异常，班级ID：" + classId + "，错误：" + e.getMessage());
            operationLog.setObjectType("CLASS_STUDENT");
            operationLog.setIpAddress(request.getRemoteAddr());
            operationLog.setDeviceInfo(request.getHeader("User-Agent"));
            operationLog.setStatus("ERROR");
            operationLog.setCreatedTime(new Date());
            logService.insert(operationLog);

            log.error("获取班级学生列表失败", e);
            return Result.error("获取班级学生列表失败：" + e.getMessage());
        }
    }

    @ApiOperation("获取班级考试列表")
    @GetMapping("/{classId}/exams")
    public Result getClassExams(@PathVariable Integer classId,
                               @RequestParam(defaultValue = "1") Integer pageNum,
                               @RequestParam(defaultValue = "10") Integer pageSize,
                               HttpServletRequest request) {
        User currentUser = TokenUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.error("用户未登录");
        }

        try {
            List<Exam> exams = examClassService.getExamsByClassId(classId);
            Long total = examClassService.countExamsByClassId(classId);
            
            Map<String, Object> data = new HashMap<>();
            data.put("list", exams);
            data.put("total", total);

            // 记录查询日志
            Log operationLog = new Log();
            operationLog.setUserId(currentUser.getUserId());
            operationLog.setActionType(7); // SELECT
            operationLog.setActionDescription("用户[" + currentUser.getUsername() + "]查询了班级考试列表，班级ID：" + classId);
            operationLog.setObjectType("CLASS_EXAM");
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
            operationLog.setActionDescription("用户[" + currentUser.getUsername() + "]查询班级考试列表异常，班级ID：" + classId + "，错误：" + e.getMessage());
            operationLog.setObjectType("CLASS_EXAM");
            operationLog.setIpAddress(request.getRemoteAddr());
            operationLog.setDeviceInfo(request.getHeader("User-Agent"));
            operationLog.setStatus("ERROR");
            operationLog.setCreatedTime(new Date());
            logService.insert(operationLog);

            log.error("获取班级考试列表失败", e);
            return Result.error("获取班级考试列表失败：" + e.getMessage());
        }
    }

    @ApiOperation("添加班级")
    @PostMapping
    public Result addClass(@RequestBody ClassDTO classDTO, HttpServletRequest request) {
        User currentUser = TokenUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.error("用户未登录");
        }

        try {
            Class classEntity = new Class();
            BeanUtils.copyProperties(classDTO, classEntity);
            classService.insert(classEntity);

            // 记录操作日志
            Log operationLog = new Log();
            operationLog.setUserId(currentUser.getUserId());
            operationLog.setActionType(1); // INSERT
            operationLog.setActionDescription("用户[" + currentUser.getUsername() + "]添加了班级");
            operationLog.setObjectType("CLASS");
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
            operationLog.setActionDescription("用户[" + currentUser.getUsername() + "]添加班级异常：" + e.getMessage());
            operationLog.setObjectType("CLASS");
            operationLog.setIpAddress(request.getRemoteAddr());
            operationLog.setDeviceInfo(request.getHeader("User-Agent"));
            operationLog.setStatus("ERROR");
            operationLog.setCreatedTime(new Date());
            logService.insert(operationLog);

            log.error("添加班级失败", e);
            return Result.error("添加班级失败：" + e.getMessage());
        }
    }

    @ApiOperation("更新班级信息")
    @PutMapping("/{classId}")
    public Result updateClass(@PathVariable Integer classId, @RequestBody ClassDTO classDTO, HttpServletRequest request) {
        User currentUser = TokenUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.error("用户未登录");
        }

        try {
            Class classEntity = new Class();
            BeanUtils.copyProperties(classDTO, classEntity);
            classEntity.setClassId(classId);
            classService.updateById(classEntity);

            // 记录操作日志
            Log operationLog = new Log();
            operationLog.setUserId(currentUser.getUserId());
            operationLog.setActionType(2); // UPDATE
            operationLog.setActionDescription("用户[" + currentUser.getUsername() + "]更新了班级信息，班级ID：" + classId);
            operationLog.setObjectType("CLASS");
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
            operationLog.setActionDescription("用户[" + currentUser.getUsername() + "]更新班级信息异常，班级ID：" + classId + "，错误：" + e.getMessage());
            operationLog.setObjectType("CLASS");
            operationLog.setIpAddress(request.getRemoteAddr());
            operationLog.setDeviceInfo(request.getHeader("User-Agent"));
            operationLog.setStatus("ERROR");
            operationLog.setCreatedTime(new Date());
            logService.insert(operationLog);

            log.error("更新班级信息失败", e);
            return Result.error("更新班级信息失败：" + e.getMessage());
        }
    }


    @ApiOperation("获取班级详情")
    @GetMapping("/{classId}")
    public Result getClassDetail(@PathVariable Integer classId, HttpServletRequest request) {
        User currentUser = TokenUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.error("用户未登录");
        }

        try {
            Class classInfo = classService.getById(classId);
            if (classInfo == null) {
                // 记录查询失败日志
                Log operationLog = new Log();
                operationLog.setUserId(currentUser.getUserId());
                operationLog.setActionType(7); // SELECT
                operationLog.setActionDescription("用户[" + currentUser.getUsername() + "]查询班级详情失败，班级ID：" + classId + " 不存在");
                operationLog.setObjectType("CLASS");
                operationLog.setIpAddress(request.getRemoteAddr());
                operationLog.setDeviceInfo(request.getHeader("User-Agent"));
                operationLog.setStatus("FAIL");
                operationLog.setCreatedTime(new Date());
                logService.insert(operationLog);

                return Result.error("班级不存在");
            }

            // 记录查询日志
            Log operationLog = new Log();
            operationLog.setUserId(currentUser.getUserId());
            operationLog.setActionType(7); // SELECT
            operationLog.setActionDescription("用户[" + currentUser.getUsername() + "]查询了班级详情，班级ID：" + classId);
            operationLog.setObjectType("CLASS");
            operationLog.setIpAddress(request.getRemoteAddr());
            operationLog.setDeviceInfo(request.getHeader("User-Agent"));
            operationLog.setStatus("SUCCESS");
            operationLog.setCreatedTime(new Date());
            logService.insert(operationLog);

            return Result.success(classInfo);
        } catch (Exception e) {
            // 记录异常日志
            Log operationLog = new Log();
            operationLog.setUserId(currentUser.getUserId());
            operationLog.setActionType(5); // EXCEPTION
            operationLog.setActionDescription("用户[" + currentUser.getUsername() + "]查询班级详情异常，班级ID：" + classId + "，错误：" + e.getMessage());
            operationLog.setObjectType("CLASS");
            operationLog.setIpAddress(request.getRemoteAddr());
            operationLog.setDeviceInfo(request.getHeader("User-Agent"));
            operationLog.setStatus("ERROR");
            operationLog.setCreatedTime(new Date());
            logService.insert(operationLog);

            log.error("获取班级详情失败", e);
            return Result.error("获取班级详情失败：" + e.getMessage());
        }
    }

    @ApiOperation("添加学生到班级")
    @PostMapping("/{classId}/students")
    public Result addStudentsToClass(@PathVariable Integer classId, 
                                    @RequestBody List<Integer> studentIds, 
                                    HttpServletRequest request) {
        User currentUser = TokenUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.error("用户未登录");
        }

        try {
            for (Integer studentId : studentIds) {
                studentClassService.joinClass(studentId, classId);
            }

            // 记录操作日志
            Log operationLog = new Log();
            operationLog.setUserId(currentUser.getUserId());
            operationLog.setActionType(1); // INSERT
            operationLog.setActionDescription("用户[" + currentUser.getUsername() + "]添加学生到班级，班级ID：" + classId + 
            "，添加了 " + studentIds.size() + " 名学生，学生ID列表：" + studentIds);
            operationLog.setObjectType("CLASS_STUDENT");
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
            operationLog.setActionDescription("用户[" + currentUser.getUsername() + "]添加学生到班级异常，班级ID：" + classId + 
                                           "，学生ID列表：" + studentIds + "，错误：" + e.getMessage());
            operationLog.setObjectType("CLASS_STUDENT");
            operationLog.setIpAddress(request.getRemoteAddr());
            operationLog.setDeviceInfo(request.getHeader("User-Agent"));
            operationLog.setStatus("ERROR");
            operationLog.setCreatedTime(new Date());
            logService.insert(operationLog);

            log.error("添加学生到班级失败", e);
            return Result.error("添加学生到班级失败：" + e.getMessage());
        }
    }

    @ApiOperation("从班级移除学生")
    @DeleteMapping("/{classId}/students/{studentId}")
    public Result removeStudentFromClass(@PathVariable Integer classId, 
                                       @PathVariable Integer studentId, 
                                       HttpServletRequest request) {
        User currentUser = TokenUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.error("用户未登录");
        }

        try {
            studentClassService.leaveClass(studentId, classId);

            // 记录操作日志
            Log operationLog = new Log();
            operationLog.setUserId(currentUser.getUserId());
            operationLog.setActionType(2); // DELETE
            operationLog.setActionDescription("用户[" + currentUser.getUsername() + "]从班级移除学生，班级ID：" + classId +
             "，学生ID：" + studentId);
            operationLog.setObjectType("CLASS_STUDENT");
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
            operationLog.setActionDescription("用户[" + currentUser.getUsername() + "]从班级移除学生异常，班级ID：" + classId +
             "，学生ID：" + studentId + "，错误：" + e.getMessage());
            operationLog.setObjectType("CLASS_STUDENT");
            operationLog.setIpAddress(request.getRemoteAddr());
            operationLog.setDeviceInfo(request.getHeader("User-Agent"));
            operationLog.setStatus("ERROR");
            operationLog.setCreatedTime(new Date());
            logService.insert(operationLog);

            log.error("从班级移除学生失败", e);
            return Result.error("从班级移除学生失败：" + e.getMessage());
        }
    }

} 
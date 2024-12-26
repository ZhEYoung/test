package com.exam.controller.student;

import com.exam.entity.*;
import com.exam.entity.Class;
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
@RequestMapping("/student/class")
@Api(tags = "学生班级功能接口")
public class StudentClassController {

    @Autowired
    private ClassService classService;

    @Autowired
    private StudentClassService studentClassService;

    @Autowired
    private LogService logService;

    @Autowired
    private ExamClassService examClassService;

    @Autowired
    private StudentService studentService;

    @ApiOperation("加入班级")
    @PostMapping("/join")
    public Result joinClass(
            @RequestBody Map<String, Object> joinForm,
            HttpServletRequest request) {
        User currentUser = TokenUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.error("用户未登录");
        }

        try {
            Integer classId = (Integer) joinForm.get("classId");
            String joinCode = (String) joinForm.get("className");
            
            if (classId == null || joinCode == null) {
                return Result.error("班级ID和班级名称不能为空");
            }

            // 1. 验证班级是否存在
            Class classInfo = classService.getById(classId);
            if (classInfo == null) {
                return Result.error("班级不存在");
            }

            // 2. 验证班级名称是否正确
            if (!joinCode.equals(classInfo.getClassName())) {
                return Result.error("班级名称不正确");
            }

            // 3. 验证学生是否已在班级中
            List<Student> classStudents = classService.getClassStudents(classId);
            if (classStudents != null) {
                for (Student student : classStudents) {
                    if (student.getUserId().equals(currentUser.getUserId())) {
                        return Result.error("您已经在该班级中");
                    }
                }
            }

            // 4. 将学生加入班级
            List<Integer> studentIds = new ArrayList<>();
            studentIds.add(studentService.getByUserId(currentUser.getUserId()).getStudentId());
            classService.batchAddStudents(classId, studentIds);

            // 记录操作日志
            Log operationLog = new Log();
            operationLog.setUserId(currentUser.getUserId());
            operationLog.setActionType(0); // INSERT
            operationLog.setActionDescription("学生[" + currentUser.getUsername() + "]加入班级，班级ID：" + classId);
            operationLog.setObjectType("CLASS");
            operationLog.setIpAddress(request.getRemoteAddr());
            operationLog.setDeviceInfo(request.getHeader("User-Agent"));
            operationLog.setStatus("SUCCESS");
            operationLog.setCreatedTime(new Date());
            logService.insert(operationLog);

            return Result.success("加入班级成功");
        } catch (Exception e) {
            // 记录异常日志
            Log operationLog = new Log();
            operationLog.setUserId(currentUser.getUserId());
            operationLog.setActionType(5); // EXCEPTION
            operationLog.setActionDescription("学生[" + currentUser.getUsername() + "]加入班级失败：" + e.getMessage());
            operationLog.setObjectType("CLASS");
            operationLog.setIpAddress(request.getRemoteAddr());
            operationLog.setDeviceInfo(request.getHeader("User-Agent"));
            operationLog.setStatus("ERROR");
            operationLog.setCreatedTime(new Date());
            logService.insert(operationLog);

            log.error("加入班级失败", e);
            return Result.error("加入班级失败：" + e.getMessage());
        }
    }

    @ApiOperation("查看我的班级列表")
    @GetMapping("/list")
    public Result getMyClasses(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            HttpServletRequest request) {
        User currentUser = TokenUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.error("用户未登录");
        }

        try {
            // 1. 获取学生所在的所有班级
            Student student = studentService.getByUserId(currentUser.getUserId());
            if (student == null) {
                return Result.error("未找到学生信息");
            }
            
            Map<String, Object> condition = new HashMap<>();
            condition.put("student_id", student.getStudentId());  // 使用下划线命名，匹配数据库字段
            List<StudentClass> studentClasses = studentClassService.getByStudentId(student.getStudentId());
            List<Class> classes = new ArrayList<>();
            for (StudentClass sc : studentClasses) {
                if(sc.getStatus() == false) {
                    continue;
                }
                Class classItem = classService.getById(sc.getClassId());
                classes.add(classItem);
            }

            Long total = (long) classes.size();

            // 2. 获取每个班级的基本信息和统计数据
            List<Map<String, Object>> classInfoList = new ArrayList<>();
            for (Class classItem : classes) {
                Map<String, Object> classInfo = new HashMap<>();
                classInfo.put("classId", classItem.getClassId());
                classInfo.put("className", classItem.getClassName());
                classInfo.put("subjectName", classItem.getSubject().getSubjectName());
                classInfo.put("teacherName", classItem.getTeacher().getName());
                classInfo.put("finalExam", classItem.getFinalExam());
                
                // 获取班级统计信息
                Map<String, Object> statistics = classService.getClassStatistics(classItem.getClassId());
                classInfo.put("statistics", statistics);
                
                classInfoList.add(classInfo);
            }

            Map<String, Object> data = new HashMap<>();
            data.put("list", classInfoList);
            data.put("total", total);

            // 记录操作日志
            Log operationLog = new Log();
            operationLog.setUserId(currentUser.getUserId());
            operationLog.setActionType(7); // SELECT
            operationLog.setActionDescription("学生[" + currentUser.getUsername() + "]查看班级列表");
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
            operationLog.setActionDescription("学生[" + currentUser.getUsername() + "]查看班级列表失败：" + e.getMessage());
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

    @ApiOperation("查看班级详情")
    @GetMapping("/detail/{classId}")
    public Result getClassDetail(
            @PathVariable Integer classId,
            HttpServletRequest request) {
        User currentUser = TokenUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.error("用户未登录");
        }

        try {
            // 1. 验证学生是否在班级中
            List<Student> classStudents = classService.getClassStudents(classId);
            boolean isInClass = false;
            if (classStudents != null) {
                for (Student student : classStudents) {
                    if (student.getUserId().equals(currentUser.getUserId())) {
                        isInClass = true;
                        break;
                    }
                }
            }
            if (!isInClass) {
                return Result.error("您不是该班级的学生");
            }

            // 2. 获取班级详细信息
            Class classInfo = classService.getById(classId);
            if (classInfo == null) {
                return Result.error("班级不存在");
            }

            // 3. 获取班级统计信息
            Map<String, Object> statistics = classService.getClassStatistics(classId);
            
            // 4. 获取班级考试日程
            List<Exam> examSchedule = examClassService.getExamsByClassId(classId);

            Map<String, Object> data = new HashMap<>();
            data.put("classInfo", classInfo);
            data.put("statistics", statistics);
            data.put("examSchedule", examSchedule);

            // 记录操作日志
            Log operationLog = new Log();
            operationLog.setUserId(currentUser.getUserId());
            operationLog.setActionType(7); // SELECT
            operationLog.setActionDescription("学生[" + currentUser.getUsername() + "]查看班级详情，班级ID：" + classId);
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
            operationLog.setActionDescription("学生[" + currentUser.getUsername() + "]查看班级详情失败，班级ID：" + classId + "，错误：" + e.getMessage());
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
} 
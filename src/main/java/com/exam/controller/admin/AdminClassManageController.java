package com.exam.controller.admin;

import com.exam.entity.*;
import com.exam.entity.Class;
import com.exam.entity.dto.ClassDTO;
import com.exam.mapper.ClassMapper;
import com.exam.service.*;
import com.exam.service.impl.SubjectServiceImpl;
import com.exam.utils.TokenUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import com.exam.common.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
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
public class AdminClassManageController {

    @Autowired
    private ClassService classService;

    @Autowired
    private StudentClassService studentClassService;

    @Autowired
    private ExamClassService examClassService;

    @Autowired
    private LogService logService;
    @Autowired
    private ClassMapper classMapper;
    @Autowired
    private SubjectService subjectService;
    @Autowired
    private ExamPaperService examPaperService;
    @Autowired
    private TeacherService teacherService;
    @Autowired
    private CollegeService collegeService;
    @Autowired
    private UserService userService;

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
            for (Class classEntity : classes) {
                Subject subject = subjectService.getById(classEntity.getSubjectId());
                subject.setCollege(collegeService.getById(subject.getCollegeId()));
                classEntity.setSubject(subject);
            }
            Long total = classMapper.selectCountByCondition(condition);
            
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
            for (Student student : students) {
                student.setCollege(collegeService.getById(student.getCollegeId()));
                student.setUser(userService.getById(student.getUserId()));
            }
            int total = studentClassService.countStudentsByClass(classId);
            
            Map<String, Object> data = new HashMap<>();
            data.put("list", students);
            data.put("classId", classId);
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
            for (Exam exam : exams) {
                exam.setSubject(subjectService.getById(exam.getSubjectId()));
                exam.setPaper(examPaperService.getById(exam.getPaperId()));
                exam.setTeacher(teacherService.getById(exam.getTeacherId()));
            }

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
            classEntity.setFinalExam(false);
            classService.insert(classEntity);

            // 记录操作日志
            Log operationLog = new Log();
            operationLog.setUserId(currentUser.getUserId());
            operationLog.setActionType(0); // INSERT
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
            operationLog.setActionType(1); // UPDATE
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

    @ApiOperation("删除班级")
    @DeleteMapping("/{classId}")
    public Result deleteClass(@PathVariable Integer classId, HttpServletRequest request) {
        User currentUser = TokenUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.error("用户未登录");
        }

        try {
            // 1. 检查班级是否存在
            Class classInfo = classService.getById(classId);
            if (classInfo == null) {
                return Result.error("班级不存在");
            }

            // 2. 检查班级是否有关联的学生
            List<Student> students = studentClassService.getActiveStudents(classId);
            if (students != null && !students.isEmpty()) {
                // 记录预检查日志
                Log operationLog = new Log();
                operationLog.setUserId(currentUser.getUserId());
                operationLog.setActionType(2); // DELETE
                operationLog.setActionDescription("用户[" + currentUser.getUsername() + "]尝试删除班级失败，班级ID：" + classId + "，原因：班级中还有" + students.size() + "名学生");
                operationLog.setObjectType("CLASS");
                operationLog.setIpAddress(request.getRemoteAddr());
                operationLog.setDeviceInfo(request.getHeader("User-Agent"));
                operationLog.setStatus("FAILED");
                operationLog.setCreatedTime(new Date());
                logService.insert(operationLog);

                return Result.error("删除失败：班级中还有" + students.size() + "名学生，请先移除所有学生");
            }

            // 3. 检查班级是否有关联的考试
            List<Exam> exams = examClassService.getExamsByClassId(classId);
            if (exams != null && !exams.isEmpty()) {
                // 记录预检查日志
                Log operationLog = new Log();
                operationLog.setUserId(currentUser.getUserId());
                operationLog.setActionType(2); // DELETE
                operationLog.setActionDescription("用户[" + currentUser.getUsername() + "]尝试删除班级失败，班级ID：" + classId + "，原因：班级中还有" + exams.size() + "个考试");
                operationLog.setObjectType("CLASS");
                operationLog.setIpAddress(request.getRemoteAddr());
                operationLog.setDeviceInfo(request.getHeader("User-Agent"));
                operationLog.setStatus("FAILED");
                operationLog.setCreatedTime(new Date());
                logService.insert(operationLog);

                return Result.error("删除失败：班级中还有" + exams.size() + "个考试，请先删除相关考试");
            }

            // 4. 执行解散班级操作（不包括级联删除）
            classService.deleteById(classId);

            // 记录操作日志
            Log operationLog = new Log();
            operationLog.setUserId(currentUser.getUserId());
            operationLog.setActionType(2); // DELETE
            operationLog.setActionDescription("用户[" + currentUser.getUsername() + "]删除了班级，班级ID：" + classId);
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
            operationLog.setActionDescription("用户[" + currentUser.getUsername() + "]删除班级异常，班级ID：" + classId + "，错误：" + e.getMessage());
            operationLog.setObjectType("CLASS");
            operationLog.setIpAddress(request.getRemoteAddr());
            operationLog.setDeviceInfo(request.getHeader("User-Agent"));
            operationLog.setStatus("ERROR");
            operationLog.setCreatedTime(new Date());
            logService.insert(operationLog);

            log.error("删除班级失败", e);
            return Result.error("删除班级失败：" + e.getMessage());
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
            operationLog.setActionType(0); // INSERT
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
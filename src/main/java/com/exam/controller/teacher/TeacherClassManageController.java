package com.exam.controller.teacher;

import com.exam.entity.*;
import com.exam.entity.Class;
import com.exam.entity.dto.ClassDTO;
import com.exam.service.*;
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
@RequestMapping("/teacher/classes")
@Api(tags = "教师班级管理接口")
public class TeacherClassManageController {

    @Autowired
    private ClassService classService;

    @Autowired
    private StudentClassService studentClassService;

    @Autowired
    private ExamClassService examClassService;

    @Autowired
    private LogService logService;

    @Autowired
    private TeacherService teacherService;

    @Autowired
    private SubjectService subjectService;

    @Autowired
    private CollegeService collegeService;

    @Autowired
    private UserService userservice;

    @Autowired
    private ExamPaperService examPaperService;

    @ApiOperation("获取教师的班级列表")
    @GetMapping
    public Result getClassList(
                               @RequestParam(defaultValue = "1") Integer pageNum,
                               @RequestParam(defaultValue = "10") Integer pageSize,
                               HttpServletRequest request) {
        User currentUser = TokenUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.error("用户未登录");
        }

        try {

            // 获取教师的全部班级
            List<Class> classes = classService.getByTeacherId(teacherService.getTeacherIdByUserId(TokenUtils.getCurrentUser().getUserId()));

            Map<String, Object> data = new HashMap<>();
            data.put("list", classes);

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

    @ApiOperation("获取教师所在学院的全部班级列表")
    @GetMapping("/college/classes")
    public Result getCollegeClassList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            HttpServletRequest request) {
        User currentUser = TokenUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.error("用户未登录");
        }

        try {

            // 获取教师所在学院的的全部班级
            List<Class> classes = classService.getByTeacherId(teacherService.getTeacherIdByUserId(TokenUtils.getCurrentUser().getUserId()));

            College college = collegeService.getById(teacherService.getByUserId(currentUser.getUserId()).getCollegeId());
            List<Subject> subjects = collegeService.getCollegeSubjects(college.getCollegeId());
            for(Subject subject: subjects){
                List<Class> classList = classService.getBySubjectId(subject.getSubjectId());
                classes.addAll(classList);
            }

            Map<String, Object> data = new HashMap<>();
            data.put("list", classes);

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
            for(Student student: students){
                student.setUser(userservice.getById(student.getUserId()));
            }


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

    @ApiOperation("获取班级考试安排")
    @GetMapping("/{classId}/exam-schedule")
    public Result getExamSchedule(@PathVariable Integer classId, HttpServletRequest request) {
        User currentUser = TokenUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.error("用户未登录");
        }

        try {
            List<Exam> examList = examClassService.getExamsByClassId(classId);
            for(Exam exam: examList){
                exam.setSubject(subjectService.getById(exam.getSubjectId()));
                exam.setPaper(examPaperService.getById(exam.getPaperId()));
            }

            // 记录查询日志
            Log operationLog = new Log();
            operationLog.setUserId(currentUser.getUserId());
            operationLog.setActionType(7); // SELECT
            operationLog.setActionDescription("用户[" + currentUser.getUsername() + "]查询了班级考试安排，班级ID：" + classId);
            operationLog.setObjectType("CLASS_EXAM_SCHEDULE");
            operationLog.setIpAddress(request.getRemoteAddr());
            operationLog.setDeviceInfo(request.getHeader("User-Agent"));
            operationLog.setStatus("SUCCESS");
            operationLog.setCreatedTime(new Date());
            logService.insert(operationLog);

            return Result.success(examList);
        } catch (Exception e) {
            // 记录异常日志
            Log operationLog = new Log();
            operationLog.setUserId(currentUser.getUserId());
            operationLog.setActionType(5); // EXCEPTION
            operationLog.setActionDescription("用户[" + currentUser.getUsername() + "]查询班级考试安排异常，班级ID：" + classId + "，错误：" + e.getMessage());
            operationLog.setObjectType("CLASS_EXAM_SCHEDULE");
            operationLog.setIpAddress(request.getRemoteAddr());
            operationLog.setDeviceInfo(request.getHeader("User-Agent"));
            operationLog.setStatus("ERROR");
            operationLog.setCreatedTime(new Date());
            logService.insert(operationLog);

            log.error("获取班级考试安排失败", e);
            return Result.error("获取班级考试安排失败：" + e.getMessage());
        }
    }

    @ApiOperation("创建新班级")
    @PostMapping("/create")
    public Result createClass(@RequestBody ClassDTO classDTO, HttpServletRequest request) {
        // 获取当前登录用户
        User currentUser = TokenUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.error("用户未登录");
        }

        try {
            // 获取教师信息
            Teacher teacher = teacherService.getByUserId(currentUser.getUserId());
            if (teacher == null) {
                return Result.error("未找到教师信息");
            }

            // 验证学科是否属于教师所在的学院
            Subject subject = subjectService.getById(classDTO.getSubjectId());
            if (subject == null) {
                return Result.error("未找到指定学科");
            }
            if (!subject.getCollegeId().equals(teacher.getCollegeId())) {
                return Result.error("只能创建本学院的学科班级");
            }

            // 创建班级实体
            Class classEntity = new Class();
            BeanUtils.copyProperties(classDTO, classEntity);
            classEntity.setTeacherId(teacher.getTeacherId());
            
            // 设置默认值
            if (classEntity.getFinalExam() == null) {
                classEntity.setFinalExam(false);
            }

            // 插入班级记录
            int result = classService.insert(classEntity);
            if (result <= 0) {
                return Result.error("创建班级失败");
            }

            // 记录操作日志
            Log operationLog = new Log();
            operationLog.setUserId(currentUser.getUserId());
            operationLog.setActionType(0); // INSERT
            operationLog.setActionDescription("教师[" + currentUser.getUsername() + "]创建了新班级[" + classDTO.getClassName() + "]");
            operationLog.setObjectType("CLASS");
            operationLog.setIpAddress(request.getRemoteAddr());
            operationLog.setDeviceInfo(request.getHeader("User-Agent"));
            operationLog.setStatus("SUCCESS");
            operationLog.setCreatedTime(new Date());
            logService.insert(operationLog);

            return Result.success(classEntity);
        } catch (Exception e) {
            // 记录异常日志
            Log operationLog = new Log();
            operationLog.setUserId(currentUser.getUserId());
            operationLog.setActionType(5); // EXCEPTION
            operationLog.setActionDescription("教师[" + currentUser.getUsername() + "]创建班级异常：" + e.getMessage());
            operationLog.setObjectType("CLASS");
            operationLog.setIpAddress(request.getRemoteAddr());
            operationLog.setDeviceInfo(request.getHeader("User-Agent"));
            operationLog.setStatus("ERROR");
            operationLog.setCreatedTime(new Date());
            logService.insert(operationLog);

            log.error("创建班级失败", e);
            return Result.error("创建班级失败：" + e.getMessage());
        }
    }

    @ApiOperation("获取教师学院下的所有学科")
    @GetMapping("/subjects")
    public Result getCollegeSubjects(HttpServletRequest request) {
        // 获取当前操作的用户
        User currentUser = TokenUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.error("用户未登录");
        }

        try {
            Teacher teacher = teacherService.getByUserId(currentUser.getUserId());
            Integer collegeId = teacher.getCollegeId();
            College college = collegeService.getById(collegeId);
            if (college == null) {
                return Result.error("学院不存在");
            }

            List<Subject> subjects = collegeService.getCollegeSubjects(collegeId);

            // 记录成功日志
            Log operationLog = new Log();
            operationLog.setUserId(currentUser.getUserId());
            operationLog.setActionType(7); // SELECT
            operationLog.setActionDescription("用户[" + currentUser.getUsername() + "]查询了学院[" + college.getCollegeName() + "]的学科列表");
            operationLog.setObjectType("COLLEGE");
            operationLog.setIpAddress(request.getRemoteAddr());
            operationLog.setDeviceInfo(request.getHeader("User-Agent"));
            operationLog.setStatus("SUCCESS");
            operationLog.setCreatedTime(new Date());
            logService.insert(operationLog);

            return Result.success(subjects);
        } catch (Exception e) {
            // 记录异常日志
            Log operationLog = new Log();
            operationLog.setUserId(currentUser.getUserId());
            operationLog.setActionType(5); // EXCEPTION
            operationLog.setActionDescription("用户[" + currentUser.getUsername() + "]查询学院学科列表异常：" + e.getMessage());
            operationLog.setObjectType("COLLEGE");
            operationLog.setIpAddress(request.getRemoteAddr());
            operationLog.setDeviceInfo(request.getHeader("User-Agent"));
            operationLog.setStatus("ERROR");
            operationLog.setCreatedTime(new Date());
            logService.insert(operationLog);

            log.error("获取学院学科列表失败", e);
            return Result.error("获取学院学科列表失败：" + e.getMessage());
        }
    }

} 
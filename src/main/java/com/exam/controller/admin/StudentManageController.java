package com.exam.controller.admin;

import com.exam.entity.dto.StudentDTO;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import com.exam.common.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;

@Slf4j
@RestController
@RequestMapping("/admin/students")
@Api(tags = "学生管理接口")
public class StudentManageController {

    @ApiOperation("获取学生列表")
    @GetMapping
    public Result getStudentList(@RequestParam(required = false) String keyword,
                                @RequestParam(required = false) Integer collegeId,
                                @RequestParam(required = false) String grade,
                                @RequestParam(defaultValue = "1") Integer pageNum,
                                @RequestParam(defaultValue = "10") Integer pageSize) {
        // TODO: 实现获取学生列表
        return Result.success();
    }

    @ApiOperation("添加学生")
    @PostMapping
    public Result addStudent(@RequestBody StudentDTO studentDTO) {
        // TODO: 实现添加学生
        return Result.success();
    }

    @ApiOperation("更新学生信息")
    @PutMapping("/{studentId}")
    public Result updateStudent(@PathVariable Integer studentId, @RequestBody StudentDTO studentDTO) {
        // TODO: 实现更新学生信息
        return Result.success();
    }

    @ApiOperation("删除学生")
    @DeleteMapping("/{studentId}")
    public Result deleteStudent(@PathVariable Integer studentId) {
        // TODO: 实现删除学生
        return Result.success();
    }

    @ApiOperation("获取学生详情")
    @GetMapping("/{studentId}")
    public Result getStudentDetail(@PathVariable Integer studentId) {
        // TODO: 实现获取学生详情
        return Result.success();
    }

    @ApiOperation("重置学生密码")
    @PutMapping("/{studentId}/password/reset")
    public Result resetPassword(@PathVariable Integer studentId) {
        // TODO: 实现重置学生密码
        return Result.success();
    }

    @ApiOperation("修改学生状态")
    @PutMapping("/{studentId}/status")
    public Result updateStatus(@PathVariable Integer studentId, @RequestParam Boolean status) {
        // TODO: 实现修改学生状态
        return Result.success();
    }

    @ApiOperation("批量导入学生")
    @PostMapping("/import")
    public Result importStudents(@RequestParam("file") MultipartFile file) {
        // TODO: 实现批量导入学生
        return Result.success();
    }

    @ApiOperation("导出学生信息")
    @GetMapping("/export")
    public void exportStudents(HttpServletResponse response) {
        // TODO: 实现导出学生信息
    }

    @ApiOperation("获取学生所在班级列表")
    @GetMapping("/{studentId}/classes")
    public Result getStudentClasses(@PathVariable Integer studentId) {
        // TODO: 实现获取学生所在班级列表
        return Result.success();
    }
} 
package com.exam.service;

import com.exam.entity.Teacher;
import com.exam.entity.User;
import com.exam.entity.College;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class TeacherServiceTest {

    @Autowired
    private TeacherService teacherService;

    @Autowired
    private UserService userService;

    @Autowired
    private CollegeService collegeService;

    private Teacher testTeacher;
    private User testUser;
    private College testCollege;

    @BeforeEach
    public void setUp() {
        // 创建测试学院，使用随机名称避免冲突
        String randomSuffix = UUID.randomUUID().toString().substring(0, 8);
        testCollege = new College();
        testCollege.setCollegeName("T" + randomSuffix);
        testCollege.setDescription("Test College Description");
        int result = collegeService.insert(testCollege);
        assertTrue(result > 0, "Failed to insert college");
        assertNotNull(testCollege.getCollegeId(), "College ID should not be null");

        // 创建测试用户，使用随机用户名避免冲突
        testUser = new User();
        testUser.setUsername("t" + randomSuffix);
        testUser.setPassword("123456");
        testUser.setRole(1); // 假设1是教师角色
        testUser.setStatus(true);
        result = userService.insert(testUser);
        assertTrue(result > 0, "Failed to insert user");
        assertNotNull(testUser.getUserId(), "User ID should not be null");

        // 创建测试教师
        testTeacher = new Teacher();
        testTeacher.setUserId(testUser.getUserId());
        testTeacher.setName("T" + randomSuffix);
        testTeacher.setPermission(0); // 可以组卷与发布所有考试
        testTeacher.setCollegeId(testCollege.getCollegeId());
        testTeacher.setOther("Test Other Info"); // 添加其他信息
    }

    @Test
    public void testBasicOperations() {
        // 测试插入
        int result = teacherService.insert(testTeacher);
        assertEquals(1, result);
        assertNotNull(testTeacher.getTeacherId());

        // 测试查询
        Teacher queryTeacher = teacherService.getById(testTeacher.getTeacherId());
        assertNotNull(queryTeacher);
        assertEquals(testTeacher.getName(), queryTeacher.getName());
        assertEquals(testTeacher.getUserId(), queryTeacher.getUserId());
        assertEquals(testTeacher.getCollegeId(), queryTeacher.getCollegeId());
        assertEquals(testTeacher.getPermission(), queryTeacher.getPermission());

        // 测试更新
        testTeacher.setName("Updated Teacher");
        testTeacher.setPermission(1);
        result = teacherService.updateById(testTeacher);
        assertEquals(1, result);

        // 验证更新结果
        queryTeacher = teacherService.getById(testTeacher.getTeacherId());
        assertEquals("Updated Teacher", queryTeacher.getName());
        assertEquals(1, queryTeacher.getPermission());
    }

    @Test
    public void testQueryOperations() {
        // 先插入测试数据
        teacherService.insert(testTeacher);

        // 测试按用户ID查询
        Teacher queryTeacher = teacherService.getByUserId(testUser.getUserId());
        assertNotNull(queryTeacher);
        assertEquals(testTeacher.getName(), queryTeacher.getName());

        // 测试按学院ID查询
        List<Teacher> collegeTeachers = teacherService.getByCollegeId(testCollege.getCollegeId());
        assertFalse(collegeTeachers.isEmpty());
        assertTrue(collegeTeachers.stream().anyMatch(t -> 
            t.getName().equals(testTeacher.getName())));

        // 测试查询所有记录
        List<Teacher> allTeachers = teacherService.getAll();
        assertFalse(allTeachers.isEmpty());
        assertTrue(allTeachers.stream().anyMatch(t -> 
            t.getName().equals(testTeacher.getName())));
    }

    @Test
    public void testUserStatusChange() {
        // 先插入测试数据
        teacherService.insert(testTeacher);

        // 确保用户是启用状态
        User activeUser = userService.getById(testUser.getUserId());
        assertNotNull(activeUser);
        assertTrue(activeUser.getStatus());

        // 测试禁用用户
        int result = userService.deleteById(testUser.getUserId());
        assertEquals(1, result);

        // 验证用户被禁用
        User queryUser = userService.getById(testUser.getUserId());
        assertNotNull(queryUser);
        assertFalse(queryUser.getStatus());

        // 验证教师记录被保留
        Teacher queryTeacher = teacherService.getById(testTeacher.getTeacherId());
        assertNotNull(queryTeacher);

        // 测试重复禁用
        result = userService.deleteById(testUser.getUserId());
        assertEquals(0, result); // 已经禁用的用户再次禁用应返回0
    }

    @Test
    public void testValidation() {
        // 测试空名称
        Teacher invalidTeacher = new Teacher();
        invalidTeacher.setUserId(testUser.getUserId());
        invalidTeacher.setName("");
        invalidTeacher.setPermission(0);
        invalidTeacher.setCollegeId(testCollege.getCollegeId());
        
        int result = teacherService.insert(invalidTeacher);
        assertEquals(0, result);

        // 测试无效的用户ID
        invalidTeacher.setName("Valid Name");
        invalidTeacher.setUserId(-1);
        result = teacherService.insert(invalidTeacher);
        assertEquals(0, result);

        // 测试无效的学院ID
        invalidTeacher.setUserId(testUser.getUserId());
        invalidTeacher.setCollegeId(-1);
        result = teacherService.insert(invalidTeacher);
        assertEquals(0, result);

        // 测试无效的权限值
        invalidTeacher.setCollegeId(testCollege.getCollegeId());
        invalidTeacher.setPermission(-1);
        result = teacherService.insert(invalidTeacher);
        assertEquals(0, result);
    }

    @Test
    public void testCollegeAssociation() {
        // 先插入测试数据
        teacherService.insert(testTeacher);

        // 测试删除学院（应该返回0，因为学院不允许删除）
        int result = collegeService.deleteById(testCollege.getCollegeId());
        assertEquals(0, result);

        // 验证学院和教师都还存在
        College queryCollege = collegeService.getById(testCollege.getCollegeId());
        assertNotNull(queryCollege);
        
        Teacher queryTeacher = teacherService.getById(testTeacher.getTeacherId());
        assertNotNull(queryTeacher);
        assertEquals(testCollege.getCollegeId(), queryTeacher.getCollegeId());
    }
} 
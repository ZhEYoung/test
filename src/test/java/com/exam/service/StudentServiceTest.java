package com.exam.service;

import com.exam.entity.Student;
import com.exam.entity.User;
import com.exam.entity.College;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class StudentServiceTest {

    @Autowired
    private StudentService studentService;

    @Autowired
    private UserService userService;

    @Autowired
    private CollegeService collegeService;

    private Student testStudent;
    private User testUser;
    private College testCollege;

    @BeforeEach
    public void setUp() {
        // 创建测试学院
        testCollege = new College();
        testCollege.setCollegeName("Test College");
        testCollege.setDescription("Test College Description");
        collegeService.insert(testCollege);

        // 创建测试用户
        testUser = new User();
        testUser.setUsername("teststud");
        testUser.setPassword("123456");
        testUser.setRole(2); // 假设2是学生角色
        testUser.setStatus(true);
        userService.insert(testUser);

        // 创建测试学生
        testStudent = new Student();
        testStudent.setUserId(testUser.getUserId());
        testStudent.setName("Test Student");
        testStudent.setGrade("2024");
        testStudent.setCollegeId(testCollege.getCollegeId());
    }

    @Test
    public void testBasicOperations() {
        // 测试插入
        int result = studentService.insert(testStudent);
        assertEquals(1, result);
        assertNotNull(testStudent.getStudentId());

        // 测试查询
        Student queryStudent = studentService.getById(testStudent.getStudentId());
        assertNotNull(queryStudent);
        assertEquals(testStudent.getName(), queryStudent.getName());
        assertEquals(testStudent.getUserId(), queryStudent.getUserId());
        assertEquals(testStudent.getCollegeId(), queryStudent.getCollegeId());
        assertEquals(testStudent.getGrade(), queryStudent.getGrade());

        // 测试更新
        testStudent.setName("Updated Student");
        testStudent.setGrade("2025");
        result = studentService.updateById(testStudent);
        assertEquals(1, result);

        // 验证更新结果
        queryStudent = studentService.getById(testStudent.getStudentId());
        assertEquals("Updated Student", queryStudent.getName());
        assertEquals("2025", queryStudent.getGrade());
    }

    @Test
    public void testQueryOperations() {
        // 先插入测试数据
        studentService.insert(testStudent);

        // 测试按用户ID查询
        Student queryStudent = studentService.getByUserId(testUser.getUserId());
        assertNotNull(queryStudent);
        assertEquals(testStudent.getName(), queryStudent.getName());

        // 测试按学院ID查询
        List<Student> collegeStudents = studentService.getByCollegeId(testCollege.getCollegeId());
        assertFalse(collegeStudents.isEmpty());
        assertTrue(collegeStudents.stream().anyMatch(s -> 
            s.getName().equals(testStudent.getName())));

        // 测试按年级查询
        List<Student> gradeStudents = studentService.getByGrade(testStudent.getGrade());
        assertFalse(gradeStudents.isEmpty());
        assertTrue(gradeStudents.stream().anyMatch(s -> 
            s.getName().equals(testStudent.getName())));

        // 测试查询所有记录
        List<Student> allStudents = studentService.getAll();
        assertFalse(allStudents.isEmpty());
        assertTrue(allStudents.stream().anyMatch(s -> 
            s.getName().equals(testStudent.getName())));
    }

    @Test
    public void testDeleteOperation() {
        // 先插入测试数据
        studentService.insert(testStudent);

        // 测试禁用用户
        int result = userService.deleteById(testUser.getUserId());
        assertEquals(1, result);

        // 验证用户被禁用
        User queryUser = userService.getById(testUser.getUserId());
        assertNotNull(queryUser);
        assertFalse(queryUser.getStatus());

        // 验证学生记录被保留
        Student queryStudent = studentService.getById(testStudent.getStudentId());
        assertNotNull(queryStudent);
    }

    @Test
    public void testValidation() {
        // 测试空名称
        Student invalidStudent = new Student();
        invalidStudent.setUserId(testUser.getUserId());
        invalidStudent.setName("");
        invalidStudent.setGrade("2024");
        invalidStudent.setCollegeId(testCollege.getCollegeId());
        
        int result = studentService.insert(invalidStudent);
        assertEquals(0, result);

        // 测试无效的用户ID
        invalidStudent.setName("Valid Name");
        invalidStudent.setUserId(-1);
        result = studentService.insert(invalidStudent);
        assertEquals(0, result);

        // 测试无效的学院ID
        invalidStudent.setUserId(testUser.getUserId());
        invalidStudent.setCollegeId(-1);
        result = studentService.insert(invalidStudent);
        assertEquals(0, result);

        // 测试无效的年级格式
        invalidStudent.setCollegeId(testCollege.getCollegeId());
        invalidStudent.setGrade("invalid");
        result = studentService.insert(invalidStudent);
        assertEquals(0, result);
    }

    @Test
    public void testUserAssociation() {
        // 先插入测试数据
        studentService.insert(testStudent);

        // 测试禁用用户
        userService.deleteById(testUser.getUserId());

        // 验证用户是否被禁用
        User queryUser = userService.getById(testUser.getUserId());
        assertNotNull(queryUser);
        assertFalse(queryUser.getStatus());

        // 验证学生记录是否保留
        Student queryStudent = studentService.getById(testStudent.getStudentId());
        assertNotNull(queryStudent); // 学生记录应该被保留
    }

    @Test
    public void testCollegeAssociation() {
        // 先插入测试数据
        studentService.insert(testStudent);

        // 测试删除学院（应该返回0，因为学院不允许删除）
        int result = collegeService.deleteById(testCollege.getCollegeId());
        assertEquals(0, result);

        // 验证学院和学生都还存在
        College queryCollege = collegeService.getById(testCollege.getCollegeId());
        assertNotNull(queryCollege);
        
        Student queryStudent = studentService.getById(testStudent.getStudentId());
        assertNotNull(queryStudent);
        assertEquals(testCollege.getCollegeId(), queryStudent.getCollegeId());
    }
}
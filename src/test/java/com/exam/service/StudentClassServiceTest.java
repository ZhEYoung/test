package com.exam.service;

import com.exam.entity.StudentClass;
import com.exam.entity.Student;
import com.exam.entity.Class;
import com.exam.entity.Subject;
import com.exam.entity.User;
import com.exam.entity.College;
import com.exam.entity.Teacher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class StudentClassServiceTest {

    @Autowired
    private StudentClassService studentClassService;
    
    @Autowired
    private StudentService studentService;
    
    @Autowired
    private ClassService classService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private SubjectService subjectService;
    
    @Autowired
    private TeacherService teacherService;
    
    @Autowired
    private CollegeService collegeService;

    private StudentClass testStudentClass;
    private Student testStudent;
    private Class testClass;
    private User testStudentUser;
    private User testTeacherUser;
    private Teacher testTeacher;
    private Subject testSubject;
    private College testCollege;

    @BeforeEach
    public void setUp() {
        // 创建测试学院
        testCollege = new College();
        testCollege.setCollegeName("Test College");
        testCollege.setDescription("Test College Description");
        collegeService.insert(testCollege);

        // 创建测试教师用户
        testTeacherUser = new User();
        testTeacherUser.setUsername("teacher1");
        testTeacherUser.setPassword("123456");
        testTeacherUser.setRole(1); // 教师角色
        userService.insert(testTeacherUser);

        // 创建测试教师
        testTeacher = new Teacher();
        testTeacher.setUserId(testTeacherUser.getUserId());
        testTeacher.setName("Test Teacher");
        testTeacher.setPermission(0); // 可以组卷与发布所有考试
        testTeacher.setCollegeId(testCollege.getCollegeId());
        teacherService.insert(testTeacher);

        // 创建测试学生用户
        testStudentUser = new User();
        testStudentUser.setUsername("student1");
        testStudentUser.setPassword("123456");
        testStudentUser.setRole(2); // 学生角色
        userService.insert(testStudentUser);

        // 创建测试学生
        testStudent = new Student();
        testStudent.setUserId(testStudentUser.getUserId());
        testStudent.setName("Test Student");
        testStudent.setGrade("2024");
        testStudent.setCollegeId(testCollege.getCollegeId());
        studentService.insert(testStudent);

        // 创建测试学科
        testSubject = new Subject();
        testSubject.setSubjectName("Test Subject");
        testSubject.setCollegeId(testCollege.getCollegeId());
        testSubject.setDescription("Test Subject Description");
        subjectService.insert(testSubject);

        // 创建测试班级
        testClass = new Class();
        testClass.setTeacherId(testTeacher.getTeacherId());
        testClass.setClassName("Test Class");
        testClass.setSubjectId(testSubject.getSubjectId());
        classService.insert(testClass);

        // 创建测试学生-班级关联
        testStudentClass = new StudentClass();
        testStudentClass.setStudentId(testStudent.getStudentId());
        testStudentClass.setClassId(testClass.getClassId());
        testStudentClass.setStatus(true);
        testStudentClass.setJoinTime(new Date());
    }

    @Test
    public void testBasicOperations() {
        // 测试插入关联记录
        int result = studentClassService.insert(testStudentClass);
        assertEquals(1, result);
        assertNotNull(testStudentClass.getScId());

        // 测试查询关联记录
        StudentClass queryStudentClass = studentClassService.selectById(testStudentClass.getScId());
        assertNotNull(queryStudentClass);
        assertEquals(testStudentClass.getStudentId(), queryStudentClass.getStudentId());
        assertEquals(testStudentClass.getClassId(), queryStudentClass.getClassId());

        // 测试更新关联记录
        testStudentClass.setStatus(false);
        result = studentClassService.update(testStudentClass);
        assertEquals(1, result);

        // 验证更新结果
        queryStudentClass = studentClassService.selectById(testStudentClass.getScId());
        assertFalse(queryStudentClass.getStatus());
    }

    @Test
    public void testQueryOperations() {
        // 先插入测试数据
        studentClassService.insert(testStudentClass);

        // 测试按学生ID查询
        List<StudentClass> studentClasses = studentClassService.getByStudentId(testStudent.getStudentId());
        assertFalse(studentClasses.isEmpty());
        assertTrue(studentClasses.stream().anyMatch(sc -> sc.getStudentId().equals(testStudent.getStudentId())));

        // 测试按班级ID查询
        List<StudentClass> classStudents = studentClassService.getByClassId(testClass.getClassId());
        assertFalse(classStudents.isEmpty());
        assertTrue(classStudents.stream().anyMatch(sc -> sc.getClassId().equals(testClass.getClassId())));

        // 测试查询所有记录
        List<StudentClass> allRecords = studentClassService.selectAll();
        assertFalse(allRecords.isEmpty());
        assertTrue(allRecords.stream().anyMatch(sc -> 
            sc.getStudentId().equals(testStudent.getStudentId()) && 
            sc.getClassId().equals(testClass.getClassId())));
    }

    @Test
    public void testDeleteOperation() {
        // 先插入测试数据
        studentClassService.insert(testStudentClass);

        // 测试删除
        int result = studentClassService.deleteById(testStudentClass.getScId());
        assertEquals(1, result);

        // 验证删除结果
        StudentClass deletedRecord = studentClassService.selectById(testStudentClass.getScId());
        assertNull(deletedRecord);
    }

    @Test
    public void testBatchOperations() {
        // 创建第二个测试学生用户
        User secondStudentUser = new User();
        secondStudentUser.setUsername("student2");
        secondStudentUser.setPassword("123456");
        secondStudentUser.setRole(2);
        userService.insert(secondStudentUser);

        // 创建第二个测试学生
        Student secondStudent = new Student();
        secondStudent.setUserId(secondStudentUser.getUserId());
        secondStudent.setName("Test Student 2");
        secondStudent.setGrade("2024");
        secondStudent.setCollegeId(testCollege.getCollegeId());
        studentService.insert(secondStudent);

        // 创建第二个学生-班级关联
        StudentClass secondStudentClass = new StudentClass();
        secondStudentClass.setStudentId(secondStudent.getStudentId());
        secondStudentClass.setClassId(testClass.getClassId());
        secondStudentClass.setStatus(true);
        secondStudentClass.setJoinTime(new Date());

        // 测试批量插入
        List<StudentClass> batchList = Arrays.asList(testStudentClass, secondStudentClass);
        int result = studentClassService.batchInsert(batchList);
        assertEquals(2, result);

        // 验证批量插入结果
        List<StudentClass> insertedRecords = studentClassService.getByClassId(testClass.getClassId());
        assertEquals(2, insertedRecords.size());
    }

    @Test
    public void testJoinAndLeaveClass() {
        // 测试加入班级
        int result = studentClassService.joinClass(testStudent.getStudentId(), testClass.getClassId());
        assertEquals(1, result);

        // 验证是否成功加入
        assertTrue(studentClassService.checkStudentInClass(testStudent.getStudentId(), testClass.getClassId()));

        // 测试退出班级
        result = studentClassService.leaveClass(testStudent.getStudentId(), testClass.getClassId());
        assertEquals(1, result);

        // 验证是否成功退出
        assertFalse(studentClassService.checkStudentInClass(testStudent.getStudentId(), testClass.getClassId()));
    }

    @Test
    public void testValidation() {
        // 测试无效的学生ID
        StudentClass invalidStudentClass = new StudentClass();
        invalidStudentClass.setStudentId(-1);
        invalidStudentClass.setClassId(testClass.getClassId());
        invalidStudentClass.setStatus(true);
        invalidStudentClass.setJoinTime(new Date());
        
        int result = studentClassService.insert(invalidStudentClass);
        assertEquals(0, result);

        // 测试无效的班级ID
        invalidStudentClass.setStudentId(testStudent.getStudentId());
        invalidStudentClass.setClassId(-1);
        
        result = studentClassService.insert(invalidStudentClass);
        assertEquals(0, result);
    }
} 
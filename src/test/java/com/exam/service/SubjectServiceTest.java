package com.exam.service;

import com.exam.entity.Subject;
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
public class SubjectServiceTest {

    @Autowired
    private SubjectService subjectService;

    @Autowired
    private CollegeService collegeService;

    private Subject testSubject;
    private College testCollege;

    @BeforeEach
    public void setUp() {
        // 创建测试学院
        testCollege = new College();
        testCollege.setCollegeName("Test College");
        testCollege.setDescription("Test College Description");
        collegeService.insert(testCollege);

        // 创建测试学科
        testSubject = new Subject();
        testSubject.setSubjectName("Test Subject");
        testSubject.setCollegeId(testCollege.getCollegeId());
        testSubject.setDescription("Test Subject Description");
    }

    @Test
    public void testBasicOperations() {
        // 测试插入
        int result = subjectService.insert(testSubject);
        assertEquals(1, result);
        assertNotNull(testSubject.getSubjectId());

        // 测试查询
        Subject querySubject = subjectService.selectById(testSubject.getSubjectId());
        assertNotNull(querySubject);
        assertEquals(testSubject.getSubjectName(), querySubject.getSubjectName());
        assertEquals(testSubject.getCollegeId(), querySubject.getCollegeId());
        assertEquals(testSubject.getDescription(), querySubject.getDescription());

        // 测试更新
        testSubject.setSubjectName("Updated Subject");
        testSubject.setDescription("Updated Description");
        result = subjectService.updateById(testSubject);
        assertEquals(1, result);

        // 验证更新结果
        querySubject = subjectService.selectById(testSubject.getSubjectId());
        assertEquals("Updated Subject", querySubject.getSubjectName());
        assertEquals("Updated Description", querySubject.getDescription());
    }

    @Test
    public void testQueryOperations() {
        // 先插入测试数据
        subjectService.insert(testSubject);

        // 测试按名称查询
        Subject querySubject = subjectService.getBySubjectName(testSubject.getSubjectName());
        assertNotNull(querySubject);
        assertEquals(testSubject.getSubjectName(), querySubject.getSubjectName());

        // 测试按学院ID查询
        List<Subject> collegeSubjects = subjectService.getByCollegeId(testCollege.getCollegeId());
        assertFalse(collegeSubjects.isEmpty());
        assertTrue(collegeSubjects.stream().anyMatch(s -> 
            s.getSubjectName().equals(testSubject.getSubjectName())));

        // 测试查询所有记录
        List<Subject> allSubjects = subjectService.selectAll();
        assertFalse(allSubjects.isEmpty());
        assertTrue(allSubjects.stream().anyMatch(s -> 
            s.getSubjectName().equals(testSubject.getSubjectName())));
    }

    @Test
    public void testDeleteOperation() {
        // 先插入测试数据
        subjectService.insert(testSubject);

        // 测试删除
        int result = subjectService.deleteById(testSubject.getSubjectId());
        assertEquals(1, result);

        // 验证删除结果
        Subject deletedSubject = subjectService.selectById(testSubject.getSubjectId());
        assertNull(deletedSubject);
    }

    @Test
    public void testValidation() {
        // 测试空名称
        Subject invalidSubject = new Subject();
        invalidSubject.setSubjectName("");
        invalidSubject.setCollegeId(testCollege.getCollegeId());
        invalidSubject.setDescription("Invalid Subject");
        
        int result = subjectService.insert(invalidSubject);
        assertEquals(0, result);

        // 测试名称过长（假设数据库限制为50个字符）
        invalidSubject.setSubjectName("A".repeat(51));
        result = subjectService.insert(invalidSubject);
        assertEquals(0, result);

        // 测试无效的学院ID
        invalidSubject.setSubjectName("Valid Name");
        invalidSubject.setCollegeId(-1);
        result = subjectService.insert(invalidSubject);
        assertEquals(0, result);
    }

    @Test
    public void testCollegeAssociation() {
        // 先插入测试数据
        subjectService.insert(testSubject);

        // 测试删除学院（应该返回0，因为学院不允许删除）
        int result = collegeService.deleteById(testCollege.getCollegeId());
        assertEquals(0, result);

        // 验证学院和学科都还存在
        College queryCollege = collegeService.selectById(testCollege.getCollegeId());
        assertNotNull(queryCollege);
        
        Subject querySubject = subjectService.selectById(testSubject.getSubjectId());
        assertNotNull(querySubject);
        assertEquals(testCollege.getCollegeId(), querySubject.getCollegeId());
    }
} 
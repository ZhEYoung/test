package com.exam.service;

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
public class CollegeServiceTest {

    @Autowired
    private CollegeService collegeService;

    private College testCollege;

    @BeforeEach
    public void setUp() {
        // 创建测试学院
        testCollege = new College();
        testCollege.setCollegeName("Test College");
        testCollege.setDescription("Test College Description");
    }

    @Test
    public void testBasicOperations() {
        // 测试插入
        int result = collegeService.insert(testCollege);
        assertEquals(1, result);
        assertNotNull(testCollege.getCollegeId());

        // 测试查询
        College queryCollege = collegeService.getById(testCollege.getCollegeId());
        assertNotNull(queryCollege);
        assertEquals(testCollege.getCollegeName(), queryCollege.getCollegeName());
        assertEquals(testCollege.getDescription(), queryCollege.getDescription());

        // 测试更新
        testCollege.setCollegeName("Updated College");
        testCollege.setDescription("Updated Description");
        result = collegeService.updateById(testCollege);
        assertEquals(1, result);

        // 验证更新结果
        queryCollege = collegeService.getById(testCollege.getCollegeId());
        assertEquals("Updated College", queryCollege.getCollegeName());
        assertEquals("Updated Description", queryCollege.getDescription());
    }

    @Test
    public void testQueryOperations() {
        // 先插入测试数据
        collegeService.insert(testCollege);

        // 测试按名称查询
        College queryCollege = collegeService.getByCollegeName(testCollege.getCollegeName());
        assertNotNull(queryCollege);
        assertEquals(testCollege.getCollegeName(), queryCollege.getCollegeName());

        // 测试查询所有记录
        List<College> allColleges = collegeService.getAll();
        assertFalse(allColleges.isEmpty());
        assertTrue(allColleges.stream().anyMatch(c -> 
            c.getCollegeName().equals(testCollege.getCollegeName())));
    }

    @Test
    public void testDeleteOperation() {
        // 先插入测试数据
        collegeService.insert(testCollege);

        // 测试删除（应该返回0，因为学院不允许删除）
        int result = collegeService.deleteById(testCollege.getCollegeId());
        assertEquals(0, result);

        // 验证学院仍然存在
        College queryCollege = collegeService.getById(testCollege.getCollegeId());
        assertNotNull(queryCollege);
        assertEquals(testCollege.getCollegeName(), queryCollege.getCollegeName());
    }



    @Test
    public void testValidation() {
        // 测试空名称
        College invalidCollege = new College();
        invalidCollege.setCollegeName("");
        invalidCollege.setDescription("Invalid College");
        
        int result = collegeService.insert(invalidCollege);
        assertEquals(0, result);

        // 测试名称过长（假设数据库限制为50个字符）
        invalidCollege.setCollegeName("A".repeat(51));
        result = collegeService.insert(invalidCollege);
        assertEquals(0, result);
    }
} 
package com.exam.service;

import com.exam.entity.QuestionBank;
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
public class QuestionBankServiceTest {

    @Autowired
    private QuestionBankService questionBankService;

    @Autowired
    private SubjectService subjectService;

    @Autowired
    private CollegeService collegeService;

    private QuestionBank testQuestionBank;
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
        subjectService.insert(testSubject);

        // 创建测试题库
        testQuestionBank = new QuestionBank();
        testQuestionBank.setSubjectId(testSubject.getSubjectId());
        testQuestionBank.setQbName("Test Question Bank");
    }

    @Test
    public void testBasicOperations() {
        // 测试插入
        int result = questionBankService.insert(testQuestionBank);
        assertEquals(1, result);
        assertNotNull(testQuestionBank.getQbId());

        // 测试查询
        QuestionBank queryBank = questionBankService.selectById(testQuestionBank.getQbId());
        assertNotNull(queryBank);
        assertEquals(testQuestionBank.getQbName(), queryBank.getQbName());
        assertEquals(testQuestionBank.getSubjectId(), queryBank.getSubjectId());

        // 测试更新
        testQuestionBank.setQbName("Updated QB");
        result = questionBankService.update(testQuestionBank);
        assertEquals(1, result);

        // 验证更新结果
        queryBank = questionBankService.selectById(testQuestionBank.getQbId());
        assertEquals("Updated QB", queryBank.getQbName());
    }

    @Test
    public void testQueryOperations() {
        // 先插入测试数据
        questionBankService.insert(testQuestionBank);

        // 测试按名称查询
        QuestionBank queryBank = questionBankService.getByName(testQuestionBank.getQbName());
        assertNotNull(queryBank);
        assertEquals(testQuestionBank.getQbName(), queryBank.getQbName());

        // 测试按学科ID查询
        List<QuestionBank> subjectBanks = questionBankService.getBySubjectId(testSubject.getSubjectId());
        assertFalse(subjectBanks.isEmpty());
        assertTrue(subjectBanks.stream().anyMatch(b -> 
            b.getQbName().equals(testQuestionBank.getQbName())));

        // 测试查询所有记录
        List<QuestionBank> allBanks = questionBankService.selectAll();
        assertFalse(allBanks.isEmpty());
        assertTrue(allBanks.stream().anyMatch(b -> 
            b.getQbName().equals(testQuestionBank.getQbName())));
    }

    @Test
    public void testDeleteOperation() {
        // 先插入测试数据
        questionBankService.insert(testQuestionBank);

        // 测试删除
        int result = questionBankService.deleteById(testQuestionBank.getQbId());
        assertEquals(1, result);

        // 验证删除结果
        QuestionBank deletedBank = questionBankService.selectById(testQuestionBank.getQbId());
        assertNull(deletedBank);
    }

    @Test
    public void testValidation() {
        // 测试空名称
        QuestionBank invalidBank = new QuestionBank();
        invalidBank.setSubjectId(testSubject.getSubjectId());
        invalidBank.setQbName("");
        
        int result = questionBankService.insert(invalidBank);
        assertEquals(0, result);

        // 测试名称过长（数据库限制为20个字符）
        invalidBank.setQbName("A".repeat(21));
        result = questionBankService.insert(invalidBank);
        assertEquals(0, result);

        // 测试无效的学科ID
        invalidBank.setQbName("Valid Name");
        invalidBank.setSubjectId(-1);
        result = questionBankService.insert(invalidBank);
        assertEquals(0, result);
    }

    @Test
    public void testSubjectAssociation() {
        // 先插入测试数据
        questionBankService.insert(testQuestionBank);

        // 测试删除学科时相关题库的处理
        subjectService.deleteById(testSubject.getSubjectId());

        // 验证相关题库是否被正确处理
        QuestionBank queryBank = questionBankService.selectById(testQuestionBank.getQbId());
        // 根据业务需求，可能是删除题库或将题库的subjectId设为null
        if (queryBank != null) {
            assertNull(queryBank.getSubjectId());
        } else {
            assertNull(queryBank);
        }
    }
} 
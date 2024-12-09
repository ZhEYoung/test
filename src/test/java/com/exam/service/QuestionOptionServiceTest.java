package com.exam.service;

import com.exam.entity.QuestionOption;
import com.exam.entity.Question;
import com.exam.entity.QuestionBank;
import com.exam.entity.Subject;
import com.exam.entity.College;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class QuestionOptionServiceTest {

    @Autowired
    private QuestionOptionService questionOptionService;

    @Autowired
    private QuestionService questionService;

    @Autowired
    private QuestionBankService questionBankService;

    @Autowired
    private SubjectService subjectService;

    @Autowired
    private CollegeService collegeService;

    private QuestionOption testOption;
    private Question testQuestion;
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
        questionBankService.insert(testQuestionBank);

        // 创建测试题目
        testQuestion = new Question();
        testQuestion.setQbId(testQuestionBank.getQbId());
        testQuestion.setContent("Test Question Content");
        testQuestion.setType(1); // 假设1是单选题
        testQuestion.setAnswer("A");
        testQuestion.setDifficulty(new BigDecimal("0.7")); // 设置难度为0.7
        testQuestion.setScore(new BigDecimal("5.0")); // 设置分值为5分
        questionService.insert(testQuestion);

        // 创建测试选项
        testOption = new QuestionOption();
        testOption.setQuestionId(testQuestion.getQuestionId());
        testOption.setContent("Test Option Content");
        testOption.setIsCorrect(true);
    }

    @Test
    public void testBasicOperations() {
        // 测试插入
        int result = questionOptionService.insert(testOption);
        assertEquals(1, result);
        assertNotNull(testOption.getOptionId());

        // 测试查询
        QuestionOption queryOption = questionOptionService.selectById(testOption.getOptionId());
        assertNotNull(queryOption);
        assertEquals(testOption.getQuestionId(), queryOption.getQuestionId());
        assertEquals(testOption.getContent(), queryOption.getContent());
        assertEquals(testOption.getIsCorrect(), queryOption.getIsCorrect());

        // 测试更新
        testOption.setContent("Updated Option Content");
        testOption.setIsCorrect(false);
        result = questionOptionService.update(testOption);
        assertEquals(1, result);

        // 验证更新结果
        queryOption = questionOptionService.selectById(testOption.getOptionId());
        assertEquals("Updated Option Content", queryOption.getContent());
        assertFalse(queryOption.getIsCorrect());
    }

    @Test
    public void testQueryOperations() {
        // 先插入测试数据
        questionOptionService.insert(testOption);

        // 测试按题目ID查询
        List<QuestionOption> questionOptions = questionOptionService.getByQuestionId(testQuestion.getQuestionId());
        assertFalse(questionOptions.isEmpty());
        assertTrue(questionOptions.stream().anyMatch(o -> 
            o.getContent().equals(testOption.getContent())));

        // 测试查询所有记录
        List<QuestionOption> allOptions = questionOptionService.selectAll();
        assertFalse(allOptions.isEmpty());
        assertTrue(allOptions.stream().anyMatch(o -> 
            o.getContent().equals(testOption.getContent())));
    }

    @Test
    public void testDeleteOperation() {
        // 先插入测试数据
        questionOptionService.insert(testOption);

        // 测试删除
        int result = questionOptionService.deleteById(testOption.getOptionId());
        assertEquals(1, result);

        // 验证删除结果
        QuestionOption deletedOption = questionOptionService.selectById(testOption.getOptionId());
        assertNull(deletedOption);
    }

    @Test
    public void testValidation() {
        // 测试空内容
        QuestionOption invalidOption = new QuestionOption();
        invalidOption.setQuestionId(testQuestion.getQuestionId());
        invalidOption.setContent("");
        invalidOption.setIsCorrect(true);
        
        int result = questionOptionService.insert(invalidOption);
        assertEquals(0, result);

        // 测试内容过长（假设数据库限制为500个字符）
        invalidOption.setContent("A".repeat(501));
        result = questionOptionService.insert(invalidOption);
        assertEquals(0, result);

        // 测试无效的题目ID
        invalidOption.setContent("Valid Content");
        invalidOption.setQuestionId(-1);
        result = questionOptionService.insert(invalidOption);
        assertEquals(0, result);
    }

    @Test
    public void testQuestionAssociation() {
        // 先插入测试数据
        questionOptionService.insert(testOption);

        // 测试删除题目时相关选项的处理
        questionService.deleteById(testQuestion.getQuestionId());

        // 验证相关选项是否被正确处理
        QuestionOption queryOption = questionOptionService.selectById(testOption.getOptionId());
        assertNull(queryOption); // 选项应该被删除
    }

    @Test
    public void testQuestionBankAssociation() {
        // 先插入测试数据
        questionOptionService.insert(testOption);

        // 测试删除题库时相关选项的处理
        questionBankService.deleteById(testQuestionBank.getQbId());

        // 验证相关选项是否被正确处理
        QuestionOption queryOption = questionOptionService.selectById(testOption.getOptionId());
        assertNull(queryOption); // 选项应该被删除，因为题目也会被删除
    }

    @Test
    public void testSubjectAssociation() {
        // 先插入测试数据
        questionOptionService.insert(testOption);

        // 测试删除学科时相关选项的处理
        subjectService.deleteById(testSubject.getSubjectId());

        // 验证相关选项是否被正确处理
        QuestionOption queryOption = questionOptionService.selectById(testOption.getOptionId());
        assertNull(queryOption); // 选项应该被删除，因为题库和题目都会被删除
    }
} 
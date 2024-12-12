package com.exam.service;

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
public class QuestionServiceTest {

    @Autowired
    private QuestionService questionService;

    @Autowired
    private QuestionBankService questionBankService;

    @Autowired
    private SubjectService subjectService;

    @Autowired
    private CollegeService collegeService;

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
        testQuestion.setDifficulty(new BigDecimal("0.70")); // 设置难度为0.70，确保使用字符串构造器
        testQuestion.setScore(new BigDecimal("5.00")); // 设置分值为5.00，确保使用字符串构造器
    }

    @Test
    public void testBasicOperations() {
        // 测试插入
        int result = questionService.insert(testQuestion);
        assertEquals(1, result);
        assertNotNull(testQuestion.getQuestionId());

        // 测试查询
        Question queryQuestion = questionService.getById(testQuestion.getQuestionId());
        assertNotNull(queryQuestion, "Query result should not be null");
        assertEquals(testQuestion.getContent(), queryQuestion.getContent());
        assertEquals(testQuestion.getQbId(), queryQuestion.getQbId());
        assertEquals(testQuestion.getType(), queryQuestion.getType());
        assertEquals(testQuestion.getAnswer(), queryQuestion.getAnswer());
        
        // 检查difficulty字段
        assertNotNull(queryQuestion.getDifficulty(), "Difficulty should not be null");
        assertNotNull(testQuestion.getDifficulty(), "Test question difficulty should not be null");
        assertEquals(0, testQuestion.getDifficulty().compareTo(queryQuestion.getDifficulty()), 
            String.format("Difficulty values should be equal: expected %s but was %s", 
                testQuestion.getDifficulty(), queryQuestion.getDifficulty()));
        
        // score是transient字段，不会被持久化到数据库中，所以不需要检查

        // 测试更新
        testQuestion.setContent("Updated Question Content");
        testQuestion.setAnswer("B");
        testQuestion.setDifficulty(new BigDecimal("0.80")); // 使用字符串构造器
        result = questionService.updateById(testQuestion);
        assertEquals(1, result);

        // 验证更新结果
        queryQuestion = questionService.getById(testQuestion.getQuestionId());
        assertNotNull(queryQuestion);
        assertEquals("Updated Question Content", queryQuestion.getContent());
        assertEquals("B", queryQuestion.getAnswer());
        assertEquals(0, new BigDecimal("0.80").compareTo(queryQuestion.getDifficulty()));
    }

    @Test
    public void testQueryOperations() {
        // 先插入测试数据
        questionService.insert(testQuestion);

        // 测试按题库ID查询
        List<Question> bankQuestions = questionService.getByBankId(testQuestionBank.getQbId());
        assertFalse(bankQuestions.isEmpty());
        assertTrue(bankQuestions.stream().anyMatch(q -> 
            q.getContent().equals(testQuestion.getContent())));

        // 测试按题目类型查询
        List<Question> typeQuestions = questionService.getByType(testQuestion.getType());
        assertFalse(typeQuestions.isEmpty());
        assertTrue(typeQuestions.stream().anyMatch(q -> 
            q.getContent().equals(testQuestion.getContent())));

        // 测试查询所有记录
        List<Question> allQuestions = questionService.getAll();
        assertFalse(allQuestions.isEmpty());
        assertTrue(allQuestions.stream().anyMatch(q -> 
            q.getContent().equals(testQuestion.getContent())));
    }

    @Test
    public void testDeleteOperation() {
        // 先插入测试数据
        questionService.insert(testQuestion);

        // 测试删除
        int result = questionService.deleteById(testQuestion.getQuestionId());
        assertEquals(1, result);

        // 验证删除结果
        Question deletedQuestion = questionService.getById(testQuestion.getQuestionId());
        assertNull(deletedQuestion);
    }

    @Test
    public void testValidation() {
        // 测试空内容
        Question invalidQuestion = new Question();
        invalidQuestion.setQbId(testQuestionBank.getQbId());
        invalidQuestion.setContent("");
        invalidQuestion.setType(1);
        invalidQuestion.setAnswer("A");
        invalidQuestion.setDifficulty(new BigDecimal("0.7"));
        invalidQuestion.setScore(new BigDecimal("5.0"));
        
        int result = questionService.insert(invalidQuestion);
        assertEquals(0, result);

        // 测试内容过长（假设数据库限制为1000个字符）
        invalidQuestion.setContent("A".repeat(1001));
        result = questionService.insert(invalidQuestion);
        assertEquals(0, result);

        // 测试无效的题库ID
        invalidQuestion.setContent("Valid Content");
        invalidQuestion.setQbId(-1);
        result = questionService.insert(invalidQuestion);
        assertEquals(0, result);

        // 测试无效的题目类型
        invalidQuestion.setQbId(testQuestionBank.getQbId());
        invalidQuestion.setType(-1);
        result = questionService.insert(invalidQuestion);
        assertEquals(0, result);

        // 测试无效的难度值
        invalidQuestion.setType(1);
        invalidQuestion.setDifficulty(new BigDecimal("-0.1"));
        result = questionService.insert(invalidQuestion);
        assertEquals(0, result);

    }

    @Test
    public void testQuestionBankAssociation() {
        // 先插入测试数据
        questionService.insert(testQuestion);

        // 测试删除题库时相关题目的处理
        questionBankService.deleteById(testQuestionBank.getQbId());

        // 验证相关题目是否被正确处理
        Question queryQuestion = questionService.getById(testQuestion.getQuestionId());
        assertNull(queryQuestion); // 题目应该被删除
    }

    @Test
    public void testSubjectAssociation() {
        // 先插入测试数据
        questionService.insert(testQuestion);

        // 测试删除学科时相关题目的处理
        subjectService.deleteById(testSubject.getSubjectId());

        // 验证相关题目是否被正确处理
        Question queryQuestion = questionService.getById(testQuestion.getQuestionId());
        assertNull(queryQuestion); // 题目应该被删除，因为题库也会被删除
    }
} 
package com.exam.service;

import com.exam.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class ExamPaperQuestionServiceTest {

    @Autowired
    private ExamPaperQuestionService examPaperQuestionService;

    @Autowired
    private ExamPaperService examPaperService;

    @Autowired
    private QuestionService questionService;

    @Autowired
    private SubjectService subjectService;

    @Autowired
    private QuestionBankService questionBankService;

    @Autowired
    private CollegeService collegeService;

    @Autowired
    private TeacherService teacherService;
    @Autowired
    private UserService userService;

    private ExamPaperQuestion testExamPaperQuestion;
    private ExamPaper testPaper;
    private Question testQuestion;
    private Subject testSubject;
    private QuestionBank testQuestionBank;
    private College testCollege;
    private Teacher testTeacher;
    private User testUser;

    @BeforeEach
    public void setUp() {
        // 创建测试学院
        testCollege = new College();
        testCollege.setCollegeName("Test College");
        testCollege.setDescription("Test College Description");
        collegeService.insert(testCollege);
        
        // 确保college_id已被设置
        assertNotNull(testCollege.getCollegeId(), "College ID should not be null after insert");

        // 创建测试用户
        testUser = new User();
        testUser.setUsername("testeacher");
        testUser.setPassword("password");
        testUser.setRole(1); // 1是教师角色
        testUser.setStatus(true);
        userService.insert(testUser);
        assertNotNull(testUser.getUserId(), "User ID should not be null after insert");

        // 创建测试教师
        testTeacher = new Teacher();
        testTeacher.setName("TestTeacher");
        testTeacher.setUserId(testUser.getUserId());
        testTeacher.setCollegeId(testCollege.getCollegeId()); // 使用新创建的学院ID
        teacherService.insert(testTeacher);
        assertNotNull(testTeacher.getTeacherId(), "Teacher ID should not be null after insert");
        
        // 确保teacher_id已被设置
        assertNotNull(testTeacher.getTeacherId(), "Teacher ID should not be null after insert");

        // 创建测试科目
        testSubject = new Subject();
        testSubject.setSubjectName("Test Subject");
        testSubject.setDescription("Test Subject Description");
        testSubject.setCollegeId(testCollege.getCollegeId());
        subjectService.insert(testSubject);
        
        // 确保subject_id已被设置
        assertNotNull(testSubject.getSubjectId(), "Subject ID should not be null after insert");

        // 创建测试题库
        testQuestionBank = new QuestionBank();
        testQuestionBank.setQbName("Test Question Bank");
        testQuestionBank.setSubjectId(testSubject.getSubjectId());
        questionBankService.insert(testQuestionBank);

        // 创建测试题目
        testQuestion = new Question();
        testQuestion.setQbId(testQuestionBank.getQbId());
        testQuestion.setContent("Test Question");
        testQuestion.setType(1); // 单选题
        testQuestion.setAnswer("A"); // 设置答案
        testQuestion.setDifficulty(new BigDecimal("0.7"));
        testQuestion.setScore(new BigDecimal("5.0")); // 设置分数
        int result = questionService.insert(testQuestion);
        assertTrue(result > 0, "Question insert should succeed");
        assertNotNull(testQuestion.getQuestionId(), "Question ID should not be null after insert");

        // 创建测试试卷
        testPaper = new ExamPaper();
        testPaper.setPaperName("Test Paper");
        testPaper.setSubjectId(testSubject.getSubjectId());
        testPaper.setTeacherId(testTeacher.getTeacherId());
        testPaper.setPaperStatus(0); // 未发布
        testPaper.setExamType(0); // 期末考试
        testPaper.setPaperDifficulty(new BigDecimal("0.7"));
        examPaperService.insert(testPaper);
        assertNotNull(testPaper.getPaperId(), "Paper ID should not be null after insert");

        // 创建试卷题目关联
        testExamPaperQuestion = new ExamPaperQuestion();
        testExamPaperQuestion.setPaperId(testPaper.getPaperId());
        testExamPaperQuestion.setQuestionId(testQuestion.getQuestionId());
        testExamPaperQuestion.setQuestionOrder(1);
        testExamPaperQuestion.setQuestionScore(new BigDecimal("10.0"));
        
        // 插入并验证
        int insertResult = examPaperQuestionService.insert(testExamPaperQuestion);
        assertTrue(insertResult > 0, "ExamPaperQuestion insert should succeed");
        assertNotNull(testExamPaperQuestion.getEpqId(), "EPQ ID should not be null after insert");
    }

    @Test
    public void testBasicOperations() {
        // 测试插入
        int result = examPaperQuestionService.insert(testExamPaperQuestion);
        assertTrue(result > 0);
        assertNotNull(testExamPaperQuestion.getEpqId());

        // 测试查询
        ExamPaperQuestion queried = examPaperQuestionService.selectById(testExamPaperQuestion.getEpqId());
        assertNotNull(queried);
        assertEquals(0, testExamPaperQuestion.getQuestionScore().compareTo(queried.getQuestionScore()), "Question scores should be equal regardless of scale");

        // 测试更新
        BigDecimal newScore = new BigDecimal("15.0");
        testExamPaperQuestion.setQuestionScore(newScore);
        result = examPaperQuestionService.update(testExamPaperQuestion);
        assertTrue(result > 0);
        queried = examPaperQuestionService.selectById(testExamPaperQuestion.getEpqId());
        assertEquals(0, newScore.compareTo(queried.getQuestionScore()), "Updated question scores should be equal regardless of scale");

        // 测试查询所有
        List<ExamPaperQuestion> all = examPaperQuestionService.selectAll();
        assertFalse(all.isEmpty());
    }

    @Test
    public void testPaperQuestionQueries() {
        // 插入试卷题目关联
        examPaperQuestionService.insert(testExamPaperQuestion);

        // 测试根据试卷ID查询
        List<ExamPaperQuestion> paperQuestions = examPaperQuestionService.getByPaperId(testPaper.getPaperId());
        assertFalse(paperQuestions.isEmpty());

        // 测试根据题目ID查询
        List<ExamPaperQuestion> questionPapers = examPaperQuestionService.getByQuestionId(testQuestion.getQuestionId());
        assertFalse(questionPapers.isEmpty());
    }

    @Test
    public void testScoreOperations() {
        // 插入试卷题目关联
        examPaperQuestionService.insert(testExamPaperQuestion);

        // 测试更新分值
        BigDecimal newScore = new BigDecimal("20.0");
        int result = examPaperQuestionService.updateScore(testExamPaperQuestion.getEpqId(), newScore);
        assertTrue(result > 0);

        // 测试批量更新分值
        List<Map<String, Object>> scores = new ArrayList<>();
        Map<String, Object> scoreMap = new HashMap<>();
        scoreMap.put("epqId", testExamPaperQuestion.getEpqId());
        scoreMap.put("score", new BigDecimal("25.0"));
        scores.add(scoreMap);
        result = examPaperQuestionService.batchUpdateScore(scores);
        assertTrue(result > 0);

        // 测试计算总分
        BigDecimal totalScore = examPaperQuestionService.calculateTotalScore(testPaper.getPaperId());
        assertTrue(totalScore.compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    public void testOrderOperations() {
        // 插入试卷题目关联
        examPaperQuestionService.insert(testExamPaperQuestion);

        // 测试更新题目顺序
        int newOrder = 2;
        int result = examPaperQuestionService.updateQuestionOrder(
            testPaper.getPaperId(), 
            testQuestion.getQuestionId(), 
            newOrder
        );
        assertTrue(result > 0);

        // 测试批量更新顺序
        List<Map<String, Object>> orders = new ArrayList<>();
        Map<String, Object> orderMap = new HashMap<>();
        orderMap.put("paperId", testPaper.getPaperId());
        orderMap.put("questionId", testQuestion.getQuestionId());
        orderMap.put("newOrder", 3);
        orders.add(orderMap);
        result = examPaperQuestionService.batchUpdateOrder(orders);
        assertTrue(result > 0);
    }

    @Test
    public void testAnalysisOperations() {
        // 插入试卷题目关联
        examPaperQuestionService.insert(testExamPaperQuestion);

        // 测试分析分值分布
        List<Map<String, Object>> scoreDistribution = examPaperQuestionService.analyzeScoreDistribution(testPaper.getPaperId());
        assertNotNull(scoreDistribution);

        // 测试分析难度分布
        List<Map<String, Object>> difficultyDistribution = examPaperQuestionService.analyzeDifficultyDistribution(testPaper.getPaperId());
        assertNotNull(difficultyDistribution);

        // 测试获取覆盖的知识点
        List<Map<String, Object>> knowledgePoints = examPaperQuestionService.getCoveredKnowledgePoints(testPaper.getPaperId());
        assertNotNull(knowledgePoints);

        // 测试检查重复题目
        List<Map<String, Object>> duplicates = examPaperQuestionService.checkDuplicateQuestions(testPaper.getPaperId());
        assertNotNull(duplicates);

        // 测试检查试卷完整性
        Map<String, Object> completeness = examPaperQuestionService.checkPaperCompleteness(testPaper.getPaperId());
        assertNotNull(completeness);
    }

    @Test
    public void testBatchOperations() {
        // 测试批量插入
        List<ExamPaperQuestion> batchList = new ArrayList<>();
        batchList.add(testExamPaperQuestion);
        
        ExamPaperQuestion secondQuestion = new ExamPaperQuestion();
        secondQuestion.setPaperId(testPaper.getPaperId());
        secondQuestion.setQuestionId(testQuestion.getQuestionId());
        secondQuestion.setQuestionScore(new BigDecimal("5.0"));
        secondQuestion.setQuestionOrder(2);
        batchList.add(secondQuestion);

        int result = examPaperQuestionService.batchInsert(batchList);
        assertTrue(result > 0);

        // 测试删除试卷所有题目
        result = examPaperQuestionService.deleteByPaperId(testPaper.getPaperId());
        assertTrue(result > 0);
    }
} 
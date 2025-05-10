package com.exam.service;

import com.exam.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class AutomaticGradingServiceTest {

    @Autowired
    private QuestionService questionService;
    @Autowired
    private QuestionBankService questionBankService;
    @Autowired
    private SubjectService subjectService;
    @Autowired
    private CollegeService collegeService;
    @Autowired
    private UserService userService;
    @Autowired
    private TeacherService teacherService;
    @Autowired
    private StudentService studentService;
    @Autowired
    private ExamService examService;
    @Autowired
    private ExamPaperService examPaperService;
    @Autowired
    private ExamPaperQuestionService examPaperQuestionService;
    @Autowired
    private StudentQuestionScoreService studentQuestionScoreService;
    @Autowired
    private StudentScoreService studentScoreService;
    @Autowired
    private AutomaticGradingService automaticGradingService;

    private College testCollege;
    private Subject testSubject;
    private QuestionBank testQuestionBank;
    private User testTeacherUser;
    private User testStudentUser;
    private Teacher testTeacher;
    private Student testStudent;
    private Exam testExam;
    private ExamPaper testPaper;
    private Question testSingleChoiceQuestion;
    private Question testMultiChoiceQuestion;
    private Question testTrueFalseQuestion;
    private List<QuestionOption> singleChoiceOptions;
    private List<QuestionOption> multiChoiceOptions;

    @BeforeEach
    public void setUp() {
        // 创建测试学院
        testCollege = new College();
        testCollege.setCollegeName("Test College");
        testCollege.setDescription("Test College Description");
        collegeService.insert(testCollege);
        assertNotNull(testCollege.getCollegeId(), "College ID should not be null");

        // 创建测试学科
        testSubject = new Subject();
        testSubject.setSubjectName("Test Subject");
        testSubject.setCollegeId(testCollege.getCollegeId());
        testSubject.setDescription("Test Subject Description");
        subjectService.insert(testSubject);
        assertNotNull(testSubject.getSubjectId(), "Subject ID should not be null");

        // 创建测试题库
        testQuestionBank = new QuestionBank();
        testQuestionBank.setQbName("Test Question Bank");
        testQuestionBank.setSubjectId(testSubject.getSubjectId());
        questionBankService.insert(testQuestionBank);
        assertNotNull(testQuestionBank.getQbId(), "Question Bank ID should not be null");

        // 创建教师用户
        testTeacherUser = new User();
        testTeacherUser.setUsername("teteacher");
        testTeacherUser.setPassword("password");
        testTeacherUser.setRole(1); // 教师角色
        testTeacherUser.setStatus(Boolean.TRUE); // 启用状态
        userService.insert(testTeacherUser);
        assertNotNull(testTeacherUser.getUserId(), "Teacher user ID should not be null");

        // 创建学生用户
        testStudentUser = new User();
        testStudentUser.setUsername("testudent");
        testStudentUser.setPassword("password");
        testStudentUser.setRole(2); // 学生角色
        testStudentUser.setStatus(Boolean.TRUE); // 启用状态
        userService.insert(testStudentUser);
        assertNotNull(testStudentUser.getUserId(), "Student user ID should not be null");

        // 创建教师
        testTeacher = new Teacher();
        testTeacher.setUserId(testTeacherUser.getUserId());
        testTeacher.setName("TestTeacher");
        testTeacher.setCollegeId(testCollege.getCollegeId());
        teacherService.insert(testTeacher);
        assertNotNull(testTeacher.getTeacherId(), "Teacher ID should not be null");

        // 创建学生
        testStudent = new Student();
        testStudent.setUserId(testStudentUser.getUserId());
        testStudent.setName("TestStudent");
        testStudent.setGrade("2023");
        testStudent.setCollegeId(testCollege.getCollegeId());
        studentService.insert(testStudent);
        assertNotNull(testStudent.getStudentId(), "Student ID should not be null");

        // 创建试卷
        testPaper = new ExamPaper();
        testPaper.setPaperName("Test Paper");
        testPaper.setTeacherId(testTeacher.getTeacherId());
        testPaper.setSubjectId(testSubject.getSubjectId());
        testPaper.setPaperStatus(1); // 设置为已发布状态
        examPaperService.insert(testPaper);
        assertNotNull(testPaper.getPaperId(), "Paper ID should not be null");

        // 创建单选题
        testSingleChoiceQuestion = createSingleChoiceQuestion();

        // 添加题目到试卷
        ExamPaperQuestion epq = new ExamPaperQuestion();
        epq.setPaperId(testPaper.getPaperId());
        epq.setQuestionId(testSingleChoiceQuestion.getQuestionId());
        epq.setQuestionOrder(1);
        epq.setQuestionScore(new BigDecimal("10.0")); // 10分的单选题
        examPaperQuestionService.insert(epq);

        // 创建考试
        testExam = new Exam();
        testExam.setPaperId(testPaper.getPaperId());
        testExam.setExamName("Test Exam");
        Date startTime = new Date();
        testExam.setExamStartTime(startTime);
        testExam.setExamDuration(120);
        // 计算结束时间：开始时间 + 考试时长（分钟）
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startTime);
        calendar.add(Calendar.MINUTE, 120);
        testExam.setExamEndTime(calendar.getTime());
        testExam.setTeacherId(testTeacher.getTeacherId());
        testExam.setSubjectId(testSubject.getSubjectId());
        testExam.setExamStatus(0); // 设置考试状态为未开始
        testExam.setExamType(0); // 设置考试类型为正式考试
        examService.insert(testExam);
        assertNotNull(testExam.getExamId(), "Exam ID should not be null");

        // 创建多选题
        testMultiChoiceQuestion = createMultiChoiceQuestion();

        // 添加多选题到试卷
        ExamPaperQuestion epq2 = new ExamPaperQuestion();
        epq2.setPaperId(testPaper.getPaperId());
        epq2.setQuestionId(testMultiChoiceQuestion.getQuestionId());
        epq2.setQuestionOrder(2);
        epq2.setQuestionScore(new BigDecimal("15.0")); // 15分的多选题
        examPaperQuestionService.insert(epq2);

        // 创建判断题
        testTrueFalseQuestion = createTrueFalseQuestion();

        // 添加判断题到试卷
        ExamPaperQuestion epq3 = new ExamPaperQuestion();
        epq3.setPaperId(testPaper.getPaperId());
        epq3.setQuestionId(testTrueFalseQuestion.getQuestionId());
        epq3.setQuestionOrder(3);
        epq3.setQuestionScore(new BigDecimal("5.0")); // 5分的判断题
        examPaperQuestionService.insert(epq3);
    }

    private Question createSingleChoiceQuestion() {
        Question question = new Question();
        question.setQbId(testQuestionBank.getQbId()); // 设置题库ID
        question.setContent("Which is the correct option?");
        question.setType(0); // 单选题
        question.setDifficulty(new BigDecimal("0.7"));
        question.setAnswer("A"); // 正确答案是A
        question.setScore(new BigDecimal("10.0")); // 设置分数为10分
        int result = questionService.insert(question);
        if (result <= 0) {
            throw new RuntimeException("Failed to insert question");
        }
        assertNotNull(question.getQuestionId(), "Question ID should not be null");

        // 创建选项
        singleChoiceOptions = new ArrayList<>();
        String[] optionContents = {"Correct Answer", "Wrong Answer 1", "Wrong Answer 2", "Wrong Answer 3"};
        for (int i = 0; i < optionContents.length; i++) {
            QuestionOption option = new QuestionOption();
            option.setQuestionId(question.getQuestionId());
            option.setContent(optionContents[i]);
            option.setIsCorrect(i == 0); // 第一个选项是正确答案
            singleChoiceOptions.add(option);
        }
        questionService.batchAddOptions(question.getQuestionId(), singleChoiceOptions);

        return question;
    }

    private Question createMultiChoiceQuestion() {
        Question question = new Question();
        question.setQbId(testQuestionBank.getQbId()); // 设置题库ID
        question.setContent("Which options are correct? (Multiple answers)");
        question.setType(1); // 多选题
        question.setDifficulty(new BigDecimal("0.8"));
        question.setAnswer("A,C"); // 正确答案是A和C
        question.setScore(new BigDecimal("15.0")); // 设置分数为15分
        int result = questionService.insert(question);
        if (result <= 0) {
            throw new RuntimeException("Failed to insert question");
        }
        assertNotNull(question.getQuestionId(), "Question ID should not be null");

        // 创建选项
        multiChoiceOptions = new ArrayList<>();
        String[] optionContents = {"Correct Answer 1", "Wrong Answer", "Correct Answer 2", "Wrong Answer"};
        boolean[] isCorrect = {true, false, true, false}; // A和C是正确答案
        for (int i = 0; i < optionContents.length; i++) {
            QuestionOption option = new QuestionOption();
            option.setQuestionId(question.getQuestionId());
            option.setContent(optionContents[i]);
            option.setIsCorrect(isCorrect[i]);
            multiChoiceOptions.add(option);
        }
        questionService.batchAddOptions(question.getQuestionId(), multiChoiceOptions);

        return question;
    }

    private Question createTrueFalseQuestion() {
        Question question = new Question();
        question.setQbId(testQuestionBank.getQbId()); // 设置题库ID
        question.setContent("Is this statement true?");
        question.setType(2); // 判断题
        question.setDifficulty(new BigDecimal("0.6"));
        question.setAnswer("1"); // 1表示正确，0表示错误
        question.setScore(new BigDecimal("5.0")); // 设置分数为5分
        int result = questionService.insert(question);
        if (result <= 0) {
            throw new RuntimeException("Failed to insert question");
        }
        assertNotNull(question.getQuestionId(), "Question ID should not be null");
        return question;
    }

    @Test
    public void testGradeSingleChoiceQuestion() {
        // 创建学生成绩记录
        StudentScore studentScore = new StudentScore();
        studentScore.setStudentId(testStudent.getStudentId());
        studentScore.setExamId(testExam.getExamId());
        studentScore.setScore(BigDecimal.ZERO);
        studentScore.setUploadTime(new Date());
        studentScoreService.insert(studentScore);
        assertNotNull(studentScore.getScoreId(), "Score ID should not be null");

        // 创建学生题目成绩记录
        StudentQuestionScore questionScore = new StudentQuestionScore();
        questionScore.setStudentId(testStudent.getStudentId());
        questionScore.setExamId(testExam.getExamId());
        questionScore.setQuestionId(testSingleChoiceQuestion.getQuestionId());
        questionScore.setScoreId(studentScore.getScoreId()); // 设置成绩ID
        questionScore.setScore(BigDecimal.ZERO); // 初始分数为0
        questionScore.setStatus(0); // 未批改状态

        // 先插入记录
        int result = studentQuestionScoreService.insert(questionScore);
        assertEquals(1, result, "Question score record should be inserted successfully");
        assertNotNull(questionScore.getRecordId(), "Record ID should not be null after insertion");

        // 测试正确答案
        questionScore.setAnswer("A"); // 设置正确答案
        automaticGradingService.gradeQuestion(questionScore);
        StudentQuestionScore gradedScore = studentQuestionScoreService.getById(questionScore.getRecordId());
        assertEquals(new BigDecimal("10.00"), gradedScore.getScore(), "Should get full score for correct answer");
        assertEquals(1, gradedScore.getStatus(), "Question should be marked as graded");

        // 测试错误答案
        questionScore.setAnswer("B"); // 设置错误答案
        automaticGradingService.gradeQuestion(questionScore);
        gradedScore = studentQuestionScoreService.getById(questionScore.getRecordId());
        assertEquals(new BigDecimal("0.00"), gradedScore.getScore(), "Should get zero score for wrong answer");
        assertEquals(1, gradedScore.getStatus(), "Question should be marked as graded");

        // 测试空答案
        questionScore.setAnswer(null); // 设置空答案
        automaticGradingService.gradeQuestion(questionScore);
        gradedScore = studentQuestionScoreService.getById(questionScore.getRecordId());
        assertEquals(new BigDecimal("0.00"), gradedScore.getScore(), "Should get zero score for null answer");
        assertEquals(1, gradedScore.getStatus(), "Question should be marked as graded");
    }

    @Test
    public void testGradeMultipleChoiceQuestion() {
        // 创建学生成绩记录
        StudentScore studentScore = new StudentScore();
        studentScore.setStudentId(testStudent.getStudentId());
        studentScore.setExamId(testExam.getExamId());
        studentScore.setScore(BigDecimal.ZERO);
        studentScore.setUploadTime(new Date());
        studentScoreService.insert(studentScore);
        assertNotNull(studentScore.getScoreId(), "Score ID should not be null");

        // 创建学生题目成绩记录
        StudentQuestionScore questionScore = new StudentQuestionScore();
        questionScore.setStudentId(testStudent.getStudentId());
        questionScore.setExamId(testExam.getExamId());
        questionScore.setQuestionId(testMultiChoiceQuestion.getQuestionId());
        questionScore.setScoreId(studentScore.getScoreId()); // 设置成绩ID
        questionScore.setScore(BigDecimal.ZERO); // 初始分数为0
        questionScore.setStatus(0); // 未批改状态
        
        // 先插入记录
        int result = studentQuestionScoreService.insert(questionScore);
        assertEquals(1, result, "Question score record should be inserted successfully");
        assertNotNull(questionScore.getRecordId(), "Record ID should not be null after insertion");
        
        // 测试完全正确答案
        questionScore.setAnswer("A,C"); // 设置完全正确答案
        automaticGradingService.gradeQuestion(questionScore);
        StudentQuestionScore gradedScore = studentQuestionScoreService.getById(questionScore.getRecordId());
        assertEquals(new BigDecimal("15.00"), gradedScore.getScore(), "Should get full score for correct answer");
        assertEquals(1, gradedScore.getStatus(), "Question should be marked as graded");

        // 测试部分正确答案
        questionScore.setAnswer("A"); // 设置部分正确答案
        automaticGradingService.gradeQuestion(questionScore);
        gradedScore = studentQuestionScoreService.getById(questionScore.getRecordId());
        assertEquals(new BigDecimal("0.00"), gradedScore.getScore(), "Should get zero score for partially correct answer");
        assertEquals(1, gradedScore.getStatus(), "Question should be marked as graded");

        // 测试错误答案
        questionScore.setAnswer("B,D"); // 设置错误答案
        automaticGradingService.gradeQuestion(questionScore);
        gradedScore = studentQuestionScoreService.getById(questionScore.getRecordId());
        assertEquals(new BigDecimal("0.00"), gradedScore.getScore(), "Should get zero score for wrong answer");
        assertEquals(1, gradedScore.getStatus(), "Question should be marked as graded");

        // 测试空答案
        questionScore.setAnswer(null); // 设置空答案
        automaticGradingService.gradeQuestion(questionScore);
        gradedScore = studentQuestionScoreService.getById(questionScore.getRecordId());
        assertEquals(new BigDecimal("0.00"), gradedScore.getScore(), "Should get zero score for null answer");
        assertEquals(1, gradedScore.getStatus(), "Question should be marked as graded");
    }

    @Test
    public void testGradeTrueFalseQuestion() {
        // 创建学生成绩记录
        StudentScore studentScore = new StudentScore();
        studentScore.setStudentId(testStudent.getStudentId());
        studentScore.setExamId(testExam.getExamId());
        studentScore.setScore(BigDecimal.ZERO);
        studentScore.setUploadTime(new Date());
        studentScoreService.insert(studentScore);
        assertNotNull(studentScore.getScoreId(), "Score ID should not be null");

        // 创建学生题目成绩记录
        StudentQuestionScore questionScore = new StudentQuestionScore();
        questionScore.setStudentId(testStudent.getStudentId());
        questionScore.setExamId(testExam.getExamId());
        questionScore.setQuestionId(testTrueFalseQuestion.getQuestionId());
        questionScore.setScoreId(studentScore.getScoreId()); // 设置成绩ID
        questionScore.setScore(BigDecimal.ZERO); // 初始分数为0
        questionScore.setStatus(0); // 未批改状态
        
        // 先插入记录
        int result = studentQuestionScoreService.insert(questionScore);
        assertEquals(1, result, "Question score record should be inserted successfully");
        assertNotNull(questionScore.getRecordId(), "Record ID should not be null after insertion");
        
        // 测试正确答案
        questionScore.setAnswer("1"); // 设置正确答案
        automaticGradingService.gradeQuestion(questionScore);
        StudentQuestionScore gradedScore = studentQuestionScoreService.getById(questionScore.getRecordId());
        assertEquals(new BigDecimal("5.00"), gradedScore.getScore(), "Should get full score for correct answer");
        assertEquals(1, gradedScore.getStatus(), "Question should be marked as graded");

        // 测试错误答案
        questionScore.setAnswer("0"); // 设置错误答案
        automaticGradingService.gradeQuestion(questionScore);
        gradedScore = studentQuestionScoreService.getById(questionScore.getRecordId());
        assertEquals(new BigDecimal("0.00"), gradedScore.getScore(), "Should get zero score for wrong answer");
        assertEquals(1, gradedScore.getStatus(), "Question should be marked as graded");

        // 测试空答案
        questionScore.setAnswer(null); // 设置空答案
        automaticGradingService.gradeQuestion(questionScore);
        gradedScore = studentQuestionScoreService.getById(questionScore.getRecordId());
        assertEquals(new BigDecimal("0.00"), gradedScore.getScore(), "Should get zero score for null answer");
        assertEquals(1, gradedScore.getStatus(), "Question should be marked as graded");
    }
} 
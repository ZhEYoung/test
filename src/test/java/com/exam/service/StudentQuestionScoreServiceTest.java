package com.exam.service;

import com.exam.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;
import java.util.Calendar;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

@SpringJUnitConfig
@SpringBootTest
@Transactional
public class StudentQuestionScoreServiceTest {

    @Autowired
    private StudentQuestionScoreService studentQuestionScoreService;

    @Autowired
    private QuestionService questionService;

    @Autowired
    private ExamPaperQuestionService examPaperQuestionService;

    @Autowired
    private StudentScoreService studentScoreService;

    @Autowired
    private CollegeService collegeService;

    @Autowired
    private SubjectService subjectService;

    @Autowired
    private QuestionBankService questionBankService;

    @Autowired
    private TeacherService teacherService;

    @Autowired
    private StudentService studentService;

    @Autowired
    private ExamPaperService examPaperService;

    @Autowired
    private ExamService examService;

    @Autowired
    private UserService userService;

    private College testCollege;
    private Subject testSubject;
    private QuestionBank testQuestionBank;
    private User testTeacherUser;
    private Teacher testTeacher;
    private User testStudentUser;
    private Student testStudent;
    private ExamPaper testPaper;
    private Exam testExam;

    @BeforeEach
    public void setUp() {
        // 创建测试学院
        testCollege = new College();
        testCollege.setCollegeName("Test College");
        collegeService.insert(testCollege);
        assertNotNull(testCollege.getCollegeId());

        // 创建测试学科
        testSubject = new Subject();
        testSubject.setSubjectName("Test Subject");
        testSubject.setCollegeId(testCollege.getCollegeId());
        testSubject.setDescription("Test Subject Description");
        subjectService.insert(testSubject);
        assertNotNull(testSubject.getSubjectId());

        // 创建测试题库
        testQuestionBank = new QuestionBank();
        testQuestionBank.setQbName("Test Question Bank");
        testQuestionBank.setSubjectId(testSubject.getSubjectId());
        questionBankService.insert(testQuestionBank);
        assertNotNull(testQuestionBank.getQbId());

        // 创建教师用户
        testTeacherUser = new User();
        testTeacherUser.setUsername("teteacher");
        testTeacherUser.setPassword("password");
        testTeacherUser.setRole(1); // 教师角色
        testTeacherUser.setStatus(true);
        userService.insert(testTeacherUser);
        assertNotNull(testTeacherUser.getUserId());

        // 创建教师信息
        testTeacher = new Teacher();
        testTeacher.setUserId(testTeacherUser.getUserId());
        testTeacher.setName("Test Teacher");
        testTeacher.setCollegeId(testCollege.getCollegeId());
        teacherService.insert(testTeacher);
        assertNotNull(testTeacher.getTeacherId());

        // 创建学生用户
        testStudentUser = new User();
        testStudentUser.setUsername("testudent");
        testStudentUser.setPassword("password");
        testStudentUser.setRole(2); // 学生角色
        testStudentUser.setStatus(true);
        userService.insert(testStudentUser);
        assertNotNull(testStudentUser.getUserId());

        // 创建学生信息
        testStudent = new Student();
        testStudent.setUserId(testStudentUser.getUserId());
        testStudent.setName("Test Student");
        testStudent.setCollegeId(testCollege.getCollegeId());
        testStudent.setGrade("2023");
        studentService.insert(testStudent);
        assertNotNull(testStudent.getStudentId());

        // 创建试卷
        testPaper = new ExamPaper();
        testPaper.setPaperName("Test Paper");
        testPaper.setTeacherId(testTeacher.getTeacherId());
        testPaper.setSubjectId(testSubject.getSubjectId());
        testPaper.setPaperStatus(1); // 设置为已发布状态
        examPaperService.insert(testPaper);
        assertNotNull(testPaper.getPaperId());

        // 创建考试
        testExam = new Exam();
        testExam.setPaperId(testPaper.getPaperId());
        testExam.setExamName("Test Exam");
        Date startTime = new Date();
        testExam.setExamStartTime(startTime);
        testExam.setExamDuration(120);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startTime);
        calendar.add(Calendar.MINUTE, 120);
        testExam.setExamEndTime(calendar.getTime());
        testExam.setTeacherId(testTeacher.getTeacherId());
        testExam.setSubjectId(testSubject.getSubjectId());
        testExam.setExamStatus(0);
        testExam.setExamType(0);
        examService.insert(testExam);
        assertNotNull(testExam.getExamId());
    }

    @Test
    public void testGetSubjectiveAnswer() {
        // 创建一个简答题
        Question essayQuestion = new Question();
        essayQuestion.setQbId(testQuestionBank.getQbId());
        essayQuestion.setContent("测试简答题");
        essayQuestion.setType(4); // 简答题
        essayQuestion.setAnswer("标准答案");
        essayQuestion.setDifficulty(new BigDecimal("0.8"));
        questionService.insert(essayQuestion);

        // 添加题目到试卷
        ExamPaperQuestion epq = new ExamPaperQuestion();
        epq.setPaperId(testPaper.getPaperId());
        epq.setQuestionId(essayQuestion.getQuestionId());
        epq.setQuestionOrder(1);
        epq.setQuestionScore(new BigDecimal("20.0")); // 20分的简答题
        examPaperQuestionService.insert(epq);

        // 创建学生成绩记录
        StudentScore studentScore = new StudentScore();
        studentScore.setStudentId(testStudent.getStudentId());
        studentScore.setExamId(testExam.getExamId());
        studentScore.setScore(BigDecimal.ZERO);
        studentScore.setUploadTime(new Date());
        studentScoreService.insert(studentScore);

        // 创建学生答题记录
        StudentQuestionScore questionScore = new StudentQuestionScore();
        questionScore.setStudentId(testStudent.getStudentId());
        questionScore.setExamId(testExam.getExamId());
        questionScore.setQuestionId(essayQuestion.getQuestionId());
        questionScore.setScoreId(studentScore.getScoreId());
        questionScore.setAnswer("学生的答案");
        questionScore.setScore(new BigDecimal("15.0")); // 得了15分
        questionScore.setStatus(1); // 已批改
        studentQuestionScoreService.insert(questionScore);

        // 测试获取答案
        Map<String, Object> answer = studentQuestionScoreService.getSubjectiveAnswer(
            testExam.getExamId(),
            essayQuestion.getQuestionId(),
            testStudent.getStudentId()
        );

        // 验证结果
        assertNotNull(answer);
        assertEquals(essayQuestion.getQuestionId(), answer.get("questionId"));
        assertEquals(testStudent.getStudentId(), answer.get("studentId"));
        assertEquals(testExam.getExamId(), answer.get("examId"));
        assertEquals("测试简答题", answer.get("questionContent"));
        assertEquals(4, answer.get("questionType"));
        assertEquals("学生的答案", answer.get("studentAnswer"));
        assertEquals("标准答案", answer.get("standardAnswer"));
        assertTrue(new BigDecimal("15.0").compareTo((BigDecimal) answer.get("score")) == 0, "Score should be equal");
        assertTrue(new BigDecimal("20.0").compareTo((BigDecimal) answer.get("fullScore")) == 0, "Full score should be equal");
        assertEquals(1, answer.get("status"));

        // 测试获取不存在的答案
        Map<String, Object> notFoundAnswer = studentQuestionScoreService.getSubjectiveAnswer(
            testExam.getExamId(),
            999999, // 不存在的题目ID
            testStudent.getStudentId()
        );
        assertNull(notFoundAnswer);

        // 测试获取非主观题的答案
        Question multiChoiceQuestion = createMultiChoiceQuestion();
        assertNull(studentQuestionScoreService.getSubjectiveAnswer(
            testExam.getExamId(),
            multiChoiceQuestion.getQuestionId(),
            testStudent.getStudentId()
        ));
    }

    private Question createMultiChoiceQuestion() {
        Question question = new Question();
        question.setQbId(testQuestionBank.getQbId());
        question.setContent("测试多选题");
        question.setType(1); // 多选题
        question.setAnswer("A,C");
        question.setDifficulty(new BigDecimal("0.7"));
        questionService.insert(question);
        return question;
    }

} 
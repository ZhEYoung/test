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
public class RetakeExamServiceTest {

    @Autowired
    private ExamService examService;
    @Autowired
    private ExamPaperService examPaperService;
    @Autowired
    private TeacherService teacherService;
    @Autowired
    private UserService userService;
    @Autowired
    private CollegeService collegeService;
    @Autowired
    private SubjectService subjectService;
    @Autowired
    private StudentService studentService;
    @Autowired
    private StudentScoreService studentScoreService;
    @Autowired
    private ExamStudentService examStudentService;

    private College testCollege1;
    private College testCollege2;
    private Subject testSubject1;
    private Subject testSubject2;
    private Teacher testTeacher;
    private User testTeacherUser;
    private Student testStudent1;
    private Student testStudent2;
    private User testStudentUser1;
    private User testStudentUser2;
    private ExamPaper testPaper;
    private Exam testExam;

    @BeforeEach
    public void setUp() {
        // 创建两个测试学院
        testCollege1 = new College();
        testCollege1.setCollegeName("Test College 1");
        testCollege1.setDescription("Test College 1 Description");
        collegeService.insert(testCollege1);
        assertNotNull(testCollege1.getCollegeId(), "College 1 ID should not be null");

        testCollege2 = new College();
        testCollege2.setCollegeName("Test College 2");
        testCollege2.setDescription("Test College 2 Description");
        collegeService.insert(testCollege2);
        assertNotNull(testCollege2.getCollegeId(), "College 2 ID should not be null");

        // 创建两个测试科目（分属不同学院）
        testSubject1 = new Subject();
        testSubject1.setSubjectName("Test Subject 1");
        testSubject1.setDescription("Test Subject 1 Description");
        testSubject1.setCollegeId(testCollege1.getCollegeId());
        subjectService.insert(testSubject1);
        assertNotNull(testSubject1.getSubjectId(), "Subject 1 ID should not be null");

        testSubject2 = new Subject();
        testSubject2.setSubjectName("Test Subject 2");
        testSubject2.setDescription("Test Subject 2 Description");
        testSubject2.setCollegeId(testCollege2.getCollegeId());
        subjectService.insert(testSubject2);
        assertNotNull(testSubject2.getSubjectId(), "Subject 2 ID should not be null");

        // 创建测试教师用户
        testTeacherUser = new User();
        testTeacherUser.setUsername("teteacher");
        testTeacherUser.setPassword("password");
        testTeacherUser.setRole(1); // 教师角色
        testTeacherUser.setEmail("testteacher@example.com");
        testTeacherUser.setStatus(true);
        userService.insert(testTeacherUser);
        assertNotNull(testTeacherUser.getUserId(), "Teacher user ID should not be null");

        testTeacher = new Teacher();
        testTeacher.setUserId(testTeacherUser.getUserId());
        testTeacher.setName("TestTeacher");
        testTeacher.setPermission(0); // 可以组卷与发布所有考试
        testTeacher.setCollegeId(testCollege1.getCollegeId());
        teacherService.insert(testTeacher);
        assertNotNull(testTeacher.getTeacherId(), "Teacher ID should not be null");

        // 创建两个测试学生
        testStudentUser1 = new User();
        testStudentUser1.setUsername("testudent1");
        testStudentUser1.setPassword("password");
        testStudentUser1.setRole(2); // 学生角色
        testStudentUser1.setStatus(true);
        userService.insert(testStudentUser1);
        assertNotNull(testStudentUser1.getUserId(), "Student user 1 ID should not be null");

        testStudent1 = new Student();
        testStudent1.setUserId(testStudentUser1.getUserId());
        testStudent1.setName("TeStudent1");
        testStudent1.setGrade("2023");
        testStudent1.setCollegeId(testCollege1.getCollegeId());
        studentService.insert(testStudent1);
        assertNotNull(testStudent1.getStudentId(), "Student 1 ID should not be null");

        testStudentUser2 = new User();
        testStudentUser2.setUsername("testudent2");
        testStudentUser2.setPassword("password");
        testStudentUser2.setRole(2); // 学生角色
        testStudentUser2.setStatus(true);
        userService.insert(testStudentUser2);
        assertNotNull(testStudentUser2.getUserId(), "Student user 2 ID should not be null");

        testStudent2 = new Student();
        testStudent2.setUserId(testStudentUser2.getUserId());
        testStudent2.setName("Test Student 2");
        testStudent2.setGrade("2023");
        testStudent2.setCollegeId(testCollege2.getCollegeId());
        studentService.insert(testStudent2);
        assertNotNull(testStudent2.getStudentId(), "Student 2 ID should not be null");

        // 创建原始考试试卷
        testPaper = new ExamPaper();
        testPaper.setPaperName("Original Test Paper");
        testPaper.setPaperStatus(1); // 已发布
        testPaper.setSubjectId(testSubject1.getSubjectId());
        testPaper.setTeacherId(testTeacher.getTeacherId());
        testPaper.setCreatedTime(new Date());
        testPaper.setExamType(0); // 正常考试
        examPaperService.insert(testPaper);
        assertNotNull(testPaper.getPaperId(), "Paper ID should not be null");

        // 创建原始考试
        testExam = new Exam();
        testExam.setExamName("Original Test Exam");
        testExam.setSubjectId(testSubject1.getSubjectId());
        testExam.setPaperId(testPaper.getPaperId());
        testExam.setTeacherId(testTeacher.getTeacherId());
        testExam.setExamStatus(2); // 已结束
        testExam.setExamType(0); // 正常考试
        testExam.setExamDuration(120);
        
        // 设置考试时间为过去的时间
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -7);
        testExam.setExamStartTime(calendar.getTime());
        calendar.add(Calendar.HOUR, 2);
        testExam.setExamEndTime(calendar.getTime());
        testExam.setCreatedTime(new Date());
        examService.insert(testExam);
        assertNotNull(testExam.getExamId(), "Exam ID should not be null");

        // 创建考试-学生关联记录
        examStudentService.recordStartExam(testExam.getExamId(), testStudent1.getStudentId());
        examStudentService.recordSubmitExam(testExam.getExamId(), testStudent1.getStudentId());
        
        examStudentService.recordStartExam(testExam.getExamId(), testStudent2.getStudentId());
        examStudentService.recordSubmitExam(testExam.getExamId(), testStudent2.getStudentId());
    }

    @Test
    public void testTeacherPermissionForRetakeExam() {
        // 为两个学生添加不及格成绩
        StudentScore score1 = new StudentScore();
        score1.setExamId(testExam.getExamId());
        score1.setStudentId(testStudent1.getStudentId());
        score1.setScore(new BigDecimal("50.00")); // 不及格成绩
        studentScoreService.insert(score1);

        StudentScore score2 = new StudentScore();
        score2.setExamId(testExam.getExamId());
        score2.setStudentId(testStudent2.getStudentId());
        score2.setScore(new BigDecimal("45.00")); // 不及格成绩
        studentScoreService.insert(score2);

        // 标记需要重考
        examStudentService.markRetakeNeeded(testExam.getExamId(), testStudent1.getStudentId());
        examStudentService.markRetakeNeeded(testExam.getExamId(), testStudent2.getStudentId());

        // 测试权限为0的教师只能查看自己学院的需要重考的学生
        List<Map<String, Object>> retakeStudents1 = examStudentService.getRetakeStudentsBySubject(
            testSubject1.getSubjectId(),
            testTeacher.getTeacherId(),
            null,
            null,
            null
        );
        assertFalse(retakeStudents1.isEmpty(), "Should find retake students in subject 1 (same college)");
        
        // 验证只返回了同一学院的学生
        boolean onlySameCollege = retakeStudents1.stream()
            .allMatch(student -> {
                Integer studentCollegeId = Integer.valueOf(student.get("college_id").toString());
                return testCollege1.getCollegeId().equals(studentCollegeId);
            });
        assertTrue(onlySameCollege, "Should only find students from the same college");

        List<Map<String, Object>> retakeStudents2 = examStudentService.getRetakeStudentsBySubject(
            testSubject2.getSubjectId(),
            testTeacher.getTeacherId(),
            null,
            null,
            null
        );
        assertTrue(retakeStudents2.isEmpty(), "Should not find retake students in subject 2 (different college)");
    }

    @Test
    public void testCompleteRetakeExamProcess() {
        // 1. 为学生添加不及格成绩
        StudentScore score = new StudentScore();
        score.setExamId(testExam.getExamId());
        score.setStudentId(testStudent1.getStudentId());
        score.setScore(new BigDecimal("50.00")); // 不及格成绩
        studentScoreService.insert(score);

        // 2. 标记需要重考
        examStudentService.markRetakeNeeded(testExam.getExamId(), testStudent1.getStudentId());

        // 3. 查询需要重考的学生
        List<Map<String, Object>> retakeStudents = examStudentService.getRetakeStudentsBySubject(
            testSubject1.getSubjectId(),
            testTeacher.getTeacherId(),
            null,
            null,
            null
        );
        assertFalse(retakeStudents.isEmpty(), "Should find students who need to retake");
        assertEquals(1, retakeStudents.size(), "Should find exactly one student");

        // 4. 创建重考试卷
        ExamPaper retakePaper = new ExamPaper();
        retakePaper.setPaperName("Retake Test Paper");
        retakePaper.setPaperStatus(1); // 已发布
        retakePaper.setSubjectId(testSubject1.getSubjectId());
        retakePaper.setTeacherId(testTeacher.getTeacherId());
        retakePaper.setCreatedTime(new Date());
        retakePaper.setExamType(1); // 重考试卷
        examPaperService.insert(retakePaper);

        // 5. 发布重考考试
        Exam retakeExam = new Exam();
        retakeExam.setExamName("Retake Test Exam");
        retakeExam.setSubjectId(testSubject1.getSubjectId());
        retakeExam.setPaperId(retakePaper.getPaperId());
        retakeExam.setTeacherId(testTeacher.getTeacherId());
        retakeExam.setExamStatus(0); // 未开始
        retakeExam.setExamType(1); // 重考
        retakeExam.setExamDuration(120);
        
        // 设置考试时间
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        retakeExam.setExamStartTime(calendar.getTime());
        calendar.add(Calendar.HOUR, 2);
        retakeExam.setExamEndTime(calendar.getTime());
        retakeExam.setCreatedTime(new Date());
        examService.insert(retakeExam);

        // 6. 验证重考考试
        Exam queriedRetakeExam = examService.getById(retakeExam.getExamId());
        assertNotNull(queriedRetakeExam, "Retake exam should exist");
        assertEquals(1, queriedRetakeExam.getExamType(), "Should be a retake exam");
        
        // 7. 为重考考试添加学生并记录开始时间
        examStudentService.recordStartExam(retakeExam.getExamId(), testStudent1.getStudentId());

        // 8. 验证只有不及格的学生被添加到重考
        List<ExamStudent> examStudents = examStudentService.getNeedRetakeStudents(retakeExam.getExamId());
        assertEquals(1, examStudents.size(), "Only failed student should be in retake exam");
        assertEquals(testStudent1.getStudentId(), examStudents.get(0).getStudentId(), 
                    "Failed student should be in retake exam");
    }

    @Test
    public void testRetakeExamRestrictions() {
        // 1. 添加一个及格和一个不及格的成绩
        StudentScore failedScore = new StudentScore();
        failedScore.setExamId(testExam.getExamId());
        failedScore.setStudentId(testStudent1.getStudentId());
        failedScore.setScore(new BigDecimal("50.00")); // 不及格成绩
        studentScoreService.insert(failedScore);

        StudentScore passedScore = new StudentScore();
        passedScore.setExamId(testExam.getExamId());
        passedScore.setStudentId(testStudent2.getStudentId());
        passedScore.setScore(new BigDecimal("75.00")); // 及格成绩
        studentScoreService.insert(passedScore);

        // 2. 只标记不及格学生需要重考
        examStudentService.markRetakeNeeded(testExam.getExamId(), testStudent1.getStudentId());

        // 3. 查询需要重考的学生
        List<Map<String, Object>> retakeStudents = examStudentService.getRetakeStudentsBySubject(
            testSubject1.getSubjectId(),
            testTeacher.getTeacherId(),
            null,
            null,
            null
        );

        // 4. 验证只有不及格的学生在重考列表中
        assertEquals(1, retakeStudents.size(), "Only failed student should be in retake list");
        assertEquals(testStudent1.getStudentId(), 
                    Integer.valueOf(retakeStudents.get(0).get("student_id").toString()),
                    "Only failed student should be in retake list");

        // 5. 创建重考试卷和考试
        ExamPaper retakePaper = new ExamPaper();
        retakePaper.setPaperName("Retake Restriction Test Paper");
        retakePaper.setPaperStatus(1);
        retakePaper.setSubjectId(testSubject1.getSubjectId());
        retakePaper.setTeacherId(testTeacher.getTeacherId());
        retakePaper.setCreatedTime(new Date());
        retakePaper.setExamType(1);
        examPaperService.insert(retakePaper);

        Exam retakeExam = new Exam();
        retakeExam.setExamName("Retake Restriction Test Exam");
        retakeExam.setSubjectId(testSubject1.getSubjectId());
        retakeExam.setPaperId(retakePaper.getPaperId());
        retakeExam.setTeacherId(testTeacher.getTeacherId());
        retakeExam.setExamStatus(0);
        retakeExam.setExamType(1);
        retakeExam.setExamDuration(120);
        
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        retakeExam.setExamStartTime(calendar.getTime());
        calendar.add(Calendar.HOUR, 2);
        retakeExam.setExamEndTime(calendar.getTime());
        retakeExam.setCreatedTime(new Date());
        examService.insert(retakeExam);

        // 6. 尝试为及格学生添加重考记录（这应该是不允许的业务逻辑）
        try {
            examStudentService.recordStartExam(retakeExam.getExamId(), testStudent2.getStudentId());
            fail("Should not allow passed student to take retake exam");
        } catch (Exception e) {
            // 期望抛出异常
            assertTrue(true, "Passed student should not be allowed in retake exam");
        }

        // 7. 为不及格学生添加重考记录（这是允许的）
        int result = examStudentService.recordStartExam(retakeExam.getExamId(), testStudent1.getStudentId());
        assertTrue(result > 0, "Failed student should be allowed in retake exam");

        // 8. 验证重考成绩记录
        StudentScore retakeScore = new StudentScore();
        retakeScore.setExamId(retakeExam.getExamId());
        retakeScore.setStudentId(testStudent1.getStudentId());
        retakeScore.setScore(new BigDecimal("65.00")); // 重考及格成绩
        studentScoreService.insert(retakeScore);

        // 9. 验证可以查询到学生的所有考试记录（包括原始考试和重考）
        List<ExamStudent> studentExams = examStudentService.getStudentRetakeExams(testStudent1.getStudentId());
        assertTrue(studentExams.size() >= 2, "Should find both original and retake exam records");
    }
}
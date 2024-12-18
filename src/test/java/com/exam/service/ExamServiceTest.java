package com.exam.service;

import com.exam.entity.Exam;
import com.exam.entity.ExamPaper;
import com.exam.entity.Subject;
import com.exam.entity.Teacher;
import com.exam.entity.User;
import com.exam.entity.College;
import com.exam.entity.Class;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class ExamServiceTest {

    @Autowired
    private ExamService examService;

    @Autowired
    private ExamPaperService examPaperService;

    @Autowired
    private SubjectService subjectService;

    @Autowired
    private TeacherService teacherService;

    @Autowired
    private UserService userService;

    @Autowired
    private CollegeService collegeService;

    @Autowired
    private ClassService classService;

    private Exam testExam;
    private ExamPaper testPaper;
    private Subject testSubject;
    private Teacher testTeacher;
    private User testUser;
    private College testCollege;
    private Class testClass;

    @BeforeEach
    public void setUp() {
        // 创建测试学院
        testCollege = new College();
        testCollege.setCollegeName("Test College");
        testCollege.setDescription("Test College Description");
        collegeService.insert(testCollege);
        assertNotNull(testCollege.getCollegeId(), "College ID should not be null after insert");

        // 创建测试科目
        testSubject = new Subject();
        testSubject.setSubjectName("TestSubject");
        testSubject.setDescription("TestSubjectDescription");
        testSubject.setCollegeId(testCollege.getCollegeId());
        subjectService.insert(testSubject);
        assertNotNull(testSubject.getSubjectId(), "Subject ID should not be null after insert");

        // 创建测试用户
        testUser = new User();
        testUser.setUsername("testeacher");
        testUser.setPassword("password");
        testUser.setRole(1); // 教师角色
        testUser.setEmail("test@example.com");
        testUser.setStatus(true);
        userService.insert(testUser);

        // 创建测试教师
        testTeacher = new Teacher();
        testTeacher.setUserId(testUser.getUserId());
        testTeacher.setName("TestTeacher");
        testTeacher.setPermission(0); // 可以组卷与发布所有考试
        testTeacher.setCollegeId(testCollege.getCollegeId());
        teacherService.insert(testTeacher);
        assertNotNull(testTeacher.getTeacherId(), "Teacher ID should not be null after insert");

        // 创建测试班级
        testClass = new Class();
        testClass.setTeacherId(testTeacher.getTeacherId());
        testClass.setClassName("Test Class");
        testClass.setSubjectId(testSubject.getSubjectId());
        testClass.setFinalExam(false);
        classService.insert(testClass);
        assertNotNull(testClass.getClassId(), "Class ID should not be null after insert");

        // 创建测试试卷
        testPaper = new ExamPaper();
        testPaper.setPaperName("Test Paper");
        testPaper.setPaperStatus(1); // 已发布
        testPaper.setSubjectId(testSubject.getSubjectId());
        testPaper.setTeacherId(testTeacher.getTeacherId());
        testPaper.setCreatedTime(new Date());
        examPaperService.insert(testPaper);

        // 创建测试考试
        testExam = new Exam();
        testExam.setExamName("Test Exam");
        testExam.setSubjectId(testSubject.getSubjectId());
        testExam.setPaperId(testPaper.getPaperId());
        testExam.setTeacherId(testTeacher.getTeacherId());
        testExam.setExamStatus(0); // 未开始
        testExam.setExamType(0); // 正常考试
        testExam.setExamDuration(120); // 120分钟
        
        // 设置考试时间
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, 30);  // 设置开始时间为30分钟后
        testExam.setExamStartTime(calendar.getTime());
        calendar.add(Calendar.HOUR, 2);      // 设置结束时间为2小时后
        testExam.setExamEndTime(calendar.getTime());
        testExam.setCreatedTime(new Date());
    }

    @Test
    public void testBasicOperations() {
        // 测试插入
        int result = examService.insert(testExam);
        assertTrue(result > 0);
        assertNotNull(testExam.getExamId());

        // 测试查询
        Exam queried = examService.getById(testExam.getExamId());
        assertNotNull(queried);
        assertEquals(testExam.getExamName(), queried.getExamName());

        // 测试更新
        String newName = "Updated Test Exam";
        testExam.setExamName(newName);
        result = examService.updateById(testExam);
        assertTrue(result > 0);
        queried = examService.getById(testExam.getExamId());
        assertEquals(newName, queried.getExamName());

        // 测试查询所有
        List<Exam> exams = examService.getAll();
        assertFalse(exams.isEmpty());

        // 测试分页查询
        List<Exam> pagedExams = examService.getPage(1, 10);
        assertNotNull(pagedExams);

        // 测试条件查询
        Map<String, Object> condition = new HashMap<>();
        condition.put("subjectId", testSubject.getSubjectId());
        List<Exam> conditionExams = examService.getByCondition(condition);
        assertFalse(conditionExams.isEmpty());
    }

    @Test
    public void testStatusOperations() {
        // 插入考试
        int insertResult = examService.insert(testExam);
        assertTrue(insertResult > 0, "Exam should be inserted successfully");
        assertNotNull(testExam.getExamId(), "Exam ID should not be null after insert");

        // 验证初始状态
        Exam initialExam = examService.getById(testExam.getExamId());
        assertNotNull(initialExam, "Should be able to find the exam");
        assertEquals(0, initialExam.getExamStatus(), "Initial exam status should be 0 (not started)");

        // 测试更新状态
        int updateResult = examService.updateStatus(testExam.getExamId(), 1); // 进行中
        assertTrue(updateResult > 0, "Status update should be successful");

        // 验证状态更新
        Exam updated = examService.getById(testExam.getExamId());
        assertNotNull(updated, "Should be able to find the exam after update");
        assertEquals(1, updated.getExamStatus(), "Exam status should be updated to 1 (in progress)");

        // 测试根据状态查询
        List<Exam> ongoingExams = examService.getByStatus(1);
        assertFalse(ongoingExams.isEmpty(), "Should find exams with status 1");
        assertTrue(ongoingExams.stream().allMatch(exam -> exam.getExamStatus() == 1), 
                  "All found exams should have status 1");
        
        // 验证查询结果包含我们的考试
        boolean containsOurExam = ongoingExams.stream()
            .anyMatch(exam -> exam.getExamId().equals(testExam.getExamId()));
        assertTrue(containsOurExam, "Query result should contain our updated exam");
    }

    @Test
    public void testExamQueries() {
        // 插入考试
        examService.insert(testExam);

        // 测试根据科目查询
        List<Exam> subjectExams = examService.getBySubjectId(testSubject.getSubjectId());
        assertFalse(subjectExams.isEmpty());

        // 测试根据试卷查询
        List<Exam> paperExams = examService.getByPaperId(testPaper.getPaperId());
        assertFalse(paperExams.isEmpty());

        // 测试根据教师查询
        List<Exam> teacherExams = examService.getByTeacherId(testTeacher.getTeacherId());
        assertFalse(teacherExams.isEmpty());

        // 测试根据考试类型查询
        List<Exam> typeExams = examService.getByType(0);
        assertFalse(typeExams.isEmpty());
    }

    @Test
    public void testTimeOperations() {
        // 插入考试
        examService.insert(testExam);

        // 测试更新考试时长
        int newDuration = 180;
        int result = examService.updateDuration(testExam.getExamId(), newDuration);
        assertTrue(result > 0);

        // 验证时长更新
        Exam updated = examService.getById(testExam.getExamId());
        assertEquals(newDuration, updated.getExamDuration());

        // 测试时间范围查询
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR, -1);
        Date startTime = calendar.getTime();
        calendar.add(Calendar.HOUR, 4);
        Date endTime = calendar.getTime();

        List<Exam> timeRangeExams = examService.getByTimeRange(startTime, endTime);
        assertFalse(timeRangeExams.isEmpty());

        // 测试统计时间范围内的考试数量
        Long count = examService.countByTimeRange(startTime, endTime);
        assertTrue(count > 0);
    }

    @Test
    public void testClassOperations() {
        // 插入考试
        examService.insert(testExam);

        // 测试添加考试班级
        List<Integer> classIds = Arrays.asList(testClass.getClassId());
        int result = examService.batchAddExamClass(testExam.getExamId(), classIds);
        assertTrue(result > 0);

        // 测试查询班级的考试
        List<Exam> classExams = examService.getByClassId(testClass.getClassId());
        assertFalse(classExams.isEmpty());

        // 测试移除考试班级
        result = examService.removeExamClass(testExam.getExamId(), testClass.getClassId());
        assertTrue(result > 0);
    }

    @Test
    public void testAdvancedQueries() {
        // 插入考试
        int result = examService.insert(testExam);
        assertTrue(result > 0, "Exam should be inserted successfully");
        assertNotNull(testExam.getExamId(), "Exam ID should not be null after insert");

        // 测试高级查询 - 只使用必要的条件
        List<Exam> advancedExams = examService.getByConditions(
            testSubject.getSubjectId(),
            testTeacher.getTeacherId(),
            null, // examType
            null, // examStatus
            null, // startTime
            null, // endTime
            1, // pageNum
            10 // pageSize
        );
        assertTrue(!advancedExams.isEmpty(), "Advanced query should return the test exam");
        
        // 验证查询结果
        Exam foundExam = advancedExams.get(0);
        assertEquals(testExam.getExamId(), foundExam.getExamId(), "Should find the correct exam");
        assertEquals(testExam.getExamName(), foundExam.getExamName(), "Exam name should match");
        assertEquals(testExam.getSubjectId(), foundExam.getSubjectId(), "Subject ID should match");
        assertEquals(testExam.getTeacherId(), foundExam.getTeacherId(), "Teacher ID should match");
    }
}
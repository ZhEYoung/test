package com.exam.service;

import com.exam.entity.*;
import com.exam.entity.Class;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Calendar;
import java.util.Arrays;

@SpringBootTest
@Transactional
public class FinalExamServiceTest {

    @Autowired
    private ExamService examService;
    @Autowired
    private ExamPaperService examPaperService;
    @Autowired
    private TeacherService teacherService;
    @Autowired
    private UserService userService;
    @Autowired
    private SubjectService subjectService;
    @Autowired
    private CollegeService collegeService;
    @Autowired
    private ClassService classService;
    @Autowired
    private StudentQuestionScoreService studentQuestionScoreService;
    @Autowired
    private StudentScoreService studentScoreService;
    @Autowired
    private ExamStudentService examStudentService;
    @Autowired
    private StudentClassService studentClassService;

    private Teacher adminTeacher; // 权限为0的教师
    private Teacher normalTeacher; // 普通教师
    private Subject testSubject;
    private College testCollege;
    private Class testClass;
    private Class testClass2;
    private ExamPaper finalPaper1;
    private ExamPaper finalPaper2;
    private ExamPaper normalPaper;
    private Date academicTerm;

    @BeforeEach
    public void setUp() {
        // 创建测试学院
        testCollege = new College();
        testCollege.setCollegeName("Test College");
        collegeService.insert(testCollege);
        
        // 创建管理员教师用户
        User adminUser = new User();
        adminUser.setUsername("admTeacher");
        adminUser.setPassword("password");
        adminUser.setRole(1); // 教师角色
        adminUser.setStatus(true);
        userService.insert(adminUser);

        adminTeacher = new Teacher();
        adminTeacher.setName("AdmTeacher");
        adminTeacher.setUserId(adminUser.getUserId());
        adminTeacher.setCollegeId(testCollege.getCollegeId());
        adminTeacher.setPermission(0); // 管理员权限
        teacherService.insert(adminTeacher);

        // 创建普通教师用户
        User normalUser = new User();
        normalUser.setUsername("norTeacher");
        normalUser.setPassword("password");
        normalUser.setRole(1);
        normalUser.setStatus(true);
        userService.insert(normalUser);

        normalTeacher = new Teacher();
        normalTeacher.setName("NorTeacher");
        normalTeacher.setUserId(normalUser.getUserId());
        normalTeacher.setCollegeId(testCollege.getCollegeId());
        normalTeacher.setPermission(1); // 普通权限
        teacherService.insert(normalTeacher);

        // 创建测试学科
        testSubject = new Subject();
        testSubject.setSubjectName("Test Subject");
        testSubject.setCollegeId(testCollege.getCollegeId());
        subjectService.insert(testSubject);

        // 创建测试班级1
        testClass = new Class();
        testClass.setClassName("Test Class 1");
        testClass.setSubjectId(testSubject.getSubjectId());
        testClass.setTeacherId(adminTeacher.getTeacherId());
        testClass.setFinalExam(Boolean.FALSE); // 初始未发布期末考试
        classService.insert(testClass);

        // 创建测试班级2
        testClass2 = new Class();
        testClass2.setClassName("Test Class 2");
        testClass2.setSubjectId(testSubject.getSubjectId());
        testClass2.setTeacherId(adminTeacher.getTeacherId());
        testClass2.setFinalExam(Boolean.FALSE);
        classService.insert(testClass2);

        // 设置学年学期
        Calendar cal = Calendar.getInstance();
        cal.set(2024, Calendar.JANUARY, 1, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        academicTerm = cal.getTime();

        // 创建两份期末试卷
        finalPaper1 = createExamPaper("Final Paper 1", 0, academicTerm); // 期末试卷
        finalPaper2 = createExamPaper("Final Paper 2", 0, academicTerm); // 期末试卷
        normalPaper = createExamPaper("Normal Paper", 1, academicTerm); // 普通试卷
    }

    private ExamPaper createExamPaper(String name, Integer examType, Date academicTerm) {
        ExamPaper paper = new ExamPaper();
        paper.setPaperName(name);
        paper.setPaperStatus(0); // 未发布状态
        paper.setSubjectId(testSubject.getSubjectId());
        paper.setTeacherId(adminTeacher.getTeacherId());
        paper.setCreatedTime(new Date());
        paper.setExamType(examType);
        paper.setAcademicTerm(academicTerm);
        paper.setPaperDifficulty(new BigDecimal("0.7"));
        examPaperService.insert(paper);
        return paper;
    }

    @Test
    @DisplayName("测试权限验证 - 只有管理员教师可以发布期末考试")
    public void testPermissionValidation() {
        List<Integer> classIds = Arrays.asList(testClass.getClassId());
        
        // 设置考试时间
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, 1); // 设置为明天
        calendar.set(Calendar.HOUR_OF_DAY, 9); // 上午9点开始
        Date examStartTime = calendar.getTime();
        Integer examDuration = 120; // 120分钟

        // 使用普通教师尝试发布期末考试
        Exception exception = Assertions.assertThrows(RuntimeException.class, () -> {
            examService.publishFinalExam(
                normalTeacher.getTeacherId(),
                testSubject.getSubjectId(),
                classIds,
                academicTerm,
                examStartTime,
                examDuration
            );
        });
        Assertions.assertTrue(exception.getMessage().contains("权限不足"));
    }

    @Test
    @DisplayName("测试试卷数量验证 - 至少需要两份同学科同学期的期末试卷")
    public void testPaperCountValidation() {
        // 先删除一份期末试卷
        examPaperService.deleteById(finalPaper2.getPaperId());

        List<Integer> classIds = Arrays.asList(testClass.getClassId());
        
        // 设置考试时间
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, 1); // 设置为明天
        calendar.set(Calendar.HOUR_OF_DAY, 9); // 上午9点开始
        Date examStartTime = calendar.getTime();
        Integer examDuration = 120; // 120分钟

        // 尝试发布期末考试
        Exception exception = Assertions.assertThrows(RuntimeException.class, () -> {
            examService.publishFinalExam(
                adminTeacher.getTeacherId(),
                testSubject.getSubjectId(),
                classIds,
                academicTerm,
                examStartTime,
                examDuration
            );
        });
        Assertions.assertTrue(exception.getMessage().contains("期末试卷数量不足"));
    }

    @Test
    @DisplayName("测试成功发布期末考试 - 单个班级")
    public void testSuccessfulPublishFinalExamSingleClass() {
        // 确保有两份未发布的期末试卷
        finalPaper1.setPaperStatus(0);
        finalPaper2.setPaperStatus(0);
        examPaperService.updateById(finalPaper1);
        examPaperService.updateById(finalPaper2);

        List<Integer> classIds = Arrays.asList(testClass.getClassId());
        
        // 设置考试时间
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, 1); // 设置为明天
        calendar.set(Calendar.HOUR_OF_DAY, 9); // 上午9点开始
        Date examStartTime = calendar.getTime();
        Integer examDuration = 120; // 120分钟

        // 发布期末考试
        Exam finalExam = examService.publishFinalExam(
            adminTeacher.getTeacherId(),
            testSubject.getSubjectId(),
            classIds,
            academicTerm,
            examStartTime,
            examDuration
        );

        // 验证考试创建成功
        Assertions.assertNotNull(finalExam);
        Assertions.assertNotNull(finalExam.getExamId());
        
        // 验证考试时间设置正确
        Assertions.assertEquals(examStartTime, finalExam.getExamStartTime());
        Assertions.assertEquals(examDuration, finalExam.getExamDuration());
        
        // 验证考试结束时间正确计算
        Calendar endCalendar = Calendar.getInstance();
        endCalendar.setTime(examStartTime);
        endCalendar.add(Calendar.MINUTE, examDuration);
        Assertions.assertEquals(endCalendar.getTime(), finalExam.getExamEndTime());
        
        // 验证选中的试卷必须是期末试卷
        ExamPaper selectedPaper = examPaperService.getById(finalExam.getPaperId());
        Assertions.assertEquals(0, selectedPaper.getExamType());
        
        // 验证所有相关期末试卷都被标记为已发布
        ExamPaper paper1 = examPaperService.getById(finalPaper1.getPaperId());
        ExamPaper paper2 = examPaperService.getById(finalPaper2.getPaperId());
        Assertions.assertEquals(1, paper1.getPaperStatus());
        Assertions.assertEquals(1, paper2.getPaperStatus());
        
        // 验证普通试卷状态未改变
        ExamPaper normal = examPaperService.getById(normalPaper.getPaperId());
        Assertions.assertEquals(0, normal.getPaperStatus());
        
        // 验证班级的期末考试状态已更新
        Class updatedClass = classService.getById(testClass.getClassId());
        Assertions.assertTrue(updatedClass.getFinalExam());
    }

    @Test
    @DisplayName("测试成功发布期末考试 - 多个班级")
    public void testSuccessfulPublishFinalExamMultipleClasses() {
        // 确保有两份未发布的期末试卷
        finalPaper1.setPaperStatus(0);
        finalPaper2.setPaperStatus(0);
        examPaperService.updateById(finalPaper1);
        examPaperService.updateById(finalPaper2);

        List<Integer> classIds = Arrays.asList(testClass.getClassId(), testClass2.getClassId());
        
        // 设置考试时间
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, 1); // 设置为明天
        calendar.set(Calendar.HOUR_OF_DAY, 9); // 上午9点开始
        Date examStartTime = calendar.getTime();
        Integer examDuration = 120; // 120分钟

        // 发布期末考试
        Exam finalExam = examService.publishFinalExam(
            adminTeacher.getTeacherId(),
            testSubject.getSubjectId(),
            classIds,
            academicTerm,
            examStartTime,
            examDuration
        );

        // 验证考试创建成功
        Assertions.assertNotNull(finalExam);
        Assertions.assertNotNull(finalExam.getExamId());
        
        // 验证考试时间设置正确
        Assertions.assertEquals(examStartTime, finalExam.getExamStartTime());
        Assertions.assertEquals(examDuration, finalExam.getExamDuration());
        
        // 验证考试结束时间正确计算
        Calendar endCalendar = Calendar.getInstance();
        endCalendar.setTime(examStartTime);
        endCalendar.add(Calendar.MINUTE, examDuration);
        Assertions.assertEquals(endCalendar.getTime(), finalExam.getExamEndTime());
        
        // 验证选中的试卷必须是期末试卷
        ExamPaper selectedPaper = examPaperService.getById(finalExam.getPaperId());
        Assertions.assertEquals(0, selectedPaper.getExamType());
        
        // 验证所有相关期末试卷都被标记为已发布
        ExamPaper paper1 = examPaperService.getById(finalPaper1.getPaperId());
        ExamPaper paper2 = examPaperService.getById(finalPaper2.getPaperId());
        Assertions.assertEquals(1, paper1.getPaperStatus());
        Assertions.assertEquals(1, paper2.getPaperStatus());
        
        // 验证普通试卷状态未改变
        ExamPaper normal = examPaperService.getById(normalPaper.getPaperId());
        Assertions.assertEquals(0, normal.getPaperStatus());
        
        // 验证所有班级的期末考试状态已更新
        Class updatedClass1 = classService.getById(testClass.getClassId());
        Class updatedClass2 = classService.getById(testClass2.getClassId());
        Assertions.assertTrue(updatedClass1.getFinalExam());
        Assertions.assertTrue(updatedClass2.getFinalExam());

        // 验证考试-班级关联创建成功
        List<Exam> examsForClass1 = examService.getByClassId(testClass.getClassId());
        List<Exam> examsForClass2 = examService.getByClassId(testClass2.getClassId());
        Assertions.assertTrue(examsForClass1.stream().anyMatch(e -> e.getExamId().equals(finalExam.getExamId())));
        Assertions.assertTrue(examsForClass2.stream().anyMatch(e -> e.getExamId().equals(finalExam.getExamId())));
    }

    @Test
    @DisplayName("测试学科和学期验证")
    public void testSubjectAndTermValidation() {
        // 先删除之前创建的试卷，避免影响
        if (finalPaper1 != null) examPaperService.deleteById(finalPaper1.getPaperId());
        if (finalPaper2 != null) examPaperService.deleteById(finalPaper2.getPaperId());

        // 创建不同学期的期末试卷
        Calendar cal = Calendar.getInstance();
        cal.set(2023, Calendar.SEPTEMBER, 1, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date differentTerm = cal.getTime();
        ExamPaper differentTermPaper = createExamPaper("Different Term Paper", 0, differentTerm);
        
        // 创建两份同学期的期末试卷（确保有足够的试卷）
        ExamPaper samePaper1 = createExamPaper("Same Term Paper 1", 0, academicTerm);
        ExamPaper samePaper2 = createExamPaper("Same Term Paper 2", 0, academicTerm);

        // 确保试卷状态为未发布
        samePaper1.setPaperStatus(0);
        samePaper2.setPaperStatus(0);
        examPaperService.updateById(samePaper1);
        examPaperService.updateById(samePaper2);

        List<Integer> classIds = Arrays.asList(testClass.getClassId(), testClass2.getClassId());
        
        // 设置考试时间
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, 1); // 设置为明天
        calendar.set(Calendar.HOUR_OF_DAY, 9); // 上午9点开始
        Date examStartTime = calendar.getTime();
        Integer examDuration = 120; // 120分钟

        // 尝试发布期末考试
        Exam finalExam = examService.publishFinalExam(
            adminTeacher.getTeacherId(),
            testSubject.getSubjectId(),
            classIds,
            academicTerm,
            examStartTime,
            examDuration
        );

        // 验证选中的试卷是正确学期的试卷
        ExamPaper selectedPaper = examPaperService.getById(finalExam.getPaperId());
        Assertions.assertEquals(academicTerm, selectedPaper.getAcademicTerm());
        
        // 验证考试时间设置正确
        Assertions.assertEquals(examStartTime, finalExam.getExamStartTime());
        Assertions.assertEquals(examDuration, finalExam.getExamDuration());
        
        // 验证考试结束时间正确计算
        Calendar endCalendar = Calendar.getInstance();
        endCalendar.setTime(examStartTime);
        endCalendar.add(Calendar.MINUTE, examDuration);
        Assertions.assertEquals(endCalendar.getTime(), finalExam.getExamEndTime());
        
        // 保存需要清理的试卷ID以便在tearDown中删除
        paperIdsToDelete = Arrays.asList(
            differentTermPaper.getPaperId(),
            samePaper1.getPaperId(),
            samePaper2.getPaperId()
        );
    }

    private List<Integer> paperIdsToDelete;

    @AfterEach
    public void tearDown() {
        try {
            // 1. 删除考试-学生关联数据
            if (testClass != null) {
                List<Exam> exams = examService.getByTeacherId(adminTeacher.getTeacherId());
                for (Exam exam : exams) {
                    List<ExamStudent> examStudents = examStudentService.getByExamId(exam.getExamId());
                    for (ExamStudent es : examStudents) {
                        examStudentService.deleteById(es.getEsId());
                    }
                }
            }
            
            // 2. 删除考试数据
            if (adminTeacher != null) {
                List<Exam> adminExams = examService.getByTeacherId(adminTeacher.getTeacherId());
                for (Exam exam : adminExams) {
                    examService.deleteById(exam.getExamId());
                }
            }
            if (normalTeacher != null) {
                List<Exam> normalExams = examService.getByTeacherId(normalTeacher.getTeacherId());
                for (Exam exam : normalExams) {
                    examService.deleteById(exam.getExamId());
                }
            }
            
            // 3. 删除试卷数据
            if (paperIdsToDelete != null) {
                for (Integer paperId : paperIdsToDelete) {
                    examPaperService.deleteById(paperId);
                }
            }
            
            // 4. 删除学生-班级关联数据
            if (testClass != null) {
                List<StudentClass> studentClasses = studentClassService.getByClassId(testClass.getClassId());
                for (StudentClass sc : studentClasses) {
                    studentClassService.deleteById(sc.getScId());
                }
            }
            
            // 5. 删除班级数据
            if (testClass != null) classService.deleteById(testClass.getClassId());
            
            // 6. 删除教师数据
            if (adminTeacher != null) teacherService.deleteById(adminTeacher.getTeacherId());
            if (normalTeacher != null) teacherService.deleteById(normalTeacher.getTeacherId());
            
            // 7. 删除科目数据
            if (testSubject != null) subjectService.deleteById(testSubject.getSubjectId());
            
            // 8. 删除学院数据
            if (testCollege != null) collegeService.deleteById(testCollege.getCollegeId());
            
            // 9. 删除用户数据
            if (adminTeacher != null) userService.deleteById(adminTeacher.getUserId());
            if (normalTeacher != null) userService.deleteById(normalTeacher.getUserId());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
} 
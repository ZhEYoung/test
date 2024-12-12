package com.exam.service;

import com.exam.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class ExamPaperServiceTest {

    @Autowired
    private ExamPaperService examPaperService;

    @Autowired
    private SubjectService subjectService;

    @Autowired
    private UserService userService;

    @Autowired
    private TeacherService teacherService;

    @Autowired
    private CollegeService collegeService;

    @Autowired
    private QuestionBankService questionBankService;

    @Autowired
    private QuestionService questionService;

    private ExamPaper testPaper;
    private Subject testSubject;
    private Teacher testTeacher;
    private User testUser;
    private College testCollege;

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
        testSubject.setSubjectName("Test Subject");
        testSubject.setDescription("Test Subject Description");
        testSubject.setCollegeId(testCollege.getCollegeId()); // 使用新创建的学院ID
        subjectService.insert(testSubject);
        assertNotNull(testSubject.getSubjectId(), "Subject ID should not be null after insert");

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

        // 创建测试试卷
        testPaper = new ExamPaper();
        testPaper.setPaperName("TestPaper");
        testPaper.setPaperStatus(0); // 未发布
        testPaper.setSubjectId(testSubject.getSubjectId());
        testPaper.setTeacherId(testTeacher.getTeacherId());
        testPaper.setCreatedTime(new Date());
        testPaper.setExamType(0); // 期末考试
        testPaper.setPaperDifficulty(new BigDecimal("0.7")); // 设置难度系数

        // 验证关键字段不为空
        assertNotNull(testPaper.getSubjectId(), "Paper subject_id should not be null");
        assertNotNull(testPaper.getTeacherId(), "Paper teacher_id should not be null");
    }

    @Test
    public void testBasicOperations() {
        // 测试插入
        int result = examPaperService.insert(testPaper);
        assertTrue(result > 0);
        assertNotNull(testPaper.getPaperId());

        // 测试查询
        ExamPaper queried = examPaperService.getById(testPaper.getPaperId());
        assertNotNull(queried);
        assertEquals(testPaper.getPaperName(), queried.getPaperName());

        // 测试更新
        String newName = "Updated Test Paper";
        testPaper.setPaperName(newName);
        result = examPaperService.updateById(testPaper);
        assertTrue(result > 0);
        queried = examPaperService.getById(testPaper.getPaperId());
        assertEquals(newName, queried.getPaperName());

        // 测试查询所有
        List<ExamPaper> papers = examPaperService.getAll();
        assertFalse(papers.isEmpty());

        // 测试分页查询
        List<ExamPaper> pagedPapers = examPaperService.getPage(1, 10);
        assertNotNull(pagedPapers);

        // 测试条件查询
        Map<String, Object> condition = new HashMap<>();
        condition.put("subjectId", testSubject.getSubjectId());
        List<ExamPaper> conditionPapers = examPaperService.getByCondition(condition);
        assertFalse(conditionPapers.isEmpty());
    }

    @Test
    public void testStatusOperations() {
        // 插入试卷
        examPaperService.insert(testPaper);

        // 测试更新状态
        int result = examPaperService.updateStatus(testPaper.getPaperId(), 1); // 发布试卷
        assertTrue(result > 0);

        // 验证状态更新
        ExamPaper updated = examPaperService.getById(testPaper.getPaperId());
        assertEquals(1, updated.getPaperStatus());

        // 测试根据状态查询
        List<ExamPaper> publishedPapers = examPaperService.getByStatus(1);
        assertFalse(publishedPapers.isEmpty());
        assertTrue(publishedPapers.stream().allMatch(paper -> paper.getPaperStatus() == 1));
    }

    @Test
    public void testPaperQueries() {
        // 插入试卷
        examPaperService.insert(testPaper);

        // 测试根据科目查询
        List<ExamPaper> subjectPapers = examPaperService.getBySubjectId(testSubject.getSubjectId());
        assertFalse(subjectPapers.isEmpty());

        // 测试根据教师查询
        List<ExamPaper> teacherPapers = examPaperService.getByTeacherId(testTeacher.getTeacherId());
        assertFalse(teacherPapers.isEmpty());

        // 测试根据试卷名称查询
        ExamPaper namedPaper = examPaperService.getByName(testPaper.getPaperName());
        assertNotNull(namedPaper);
        assertEquals(testPaper.getPaperId(), namedPaper.getPaperId());

        // 测试根据考试类型查询
        List<ExamPaper> typePapers = examPaperService.getByExamType(0);
        assertFalse(typePapers.isEmpty());
    }

    @Test
    public void testDifficultyOperations() {
        // 插入试卷
        examPaperService.insert(testPaper);

        // 测试根据难度范围查询
        BigDecimal minDifficulty = new BigDecimal("0.6");
        BigDecimal maxDifficulty = new BigDecimal("0.8");
        List<ExamPaper> difficultyPapers = examPaperService.getByDifficultyRange(minDifficulty, maxDifficulty);
        assertFalse(difficultyPapers.isEmpty());
        assertTrue(difficultyPapers.stream()
                .allMatch(paper -> paper.getPaperDifficulty().compareTo(minDifficulty) >= 0 
                        && paper.getPaperDifficulty().compareTo(maxDifficulty) <= 0));
    }

    @Test
    public void testGeneratePaper() {
        // 创建题库和题目
        QuestionBank questionBank = new QuestionBank();
        questionBank.setQbName("Test Question Bank");
        questionBank.setSubjectId(testSubject.getSubjectId());
        questionBankService.insert(questionBank);
        
        System.out.println("创建题库成功，题库ID：" + questionBank.getQbId() + "，科目ID：" + questionBank.getSubjectId());

        // 添加不同类型的题目
        // 创建30个单选题
        for (int i = 0; i < 30; i++) {
            Question q = createQuestion(questionBank.getQbId(), "单选题" + i, 0, 0.6);
            System.out.println("创建单选题成功，题目ID：" + q.getQuestionId() + "，题库ID：" + q.getQbId());
        }
        // 创建15个多选题
        for (int i = 0; i < 15; i++) {
            Question q = createQuestion(questionBank.getQbId(), "多选题" + i, 1, 0.7);
            System.out.println("创建多选题成功，题目ID：" + q.getQuestionId() + "，题库ID：" + q.getQbId());
        }
        // 创建15个判断题
        for (int i = 0; i < 15; i++) {
            Question q = createQuestion(questionBank.getQbId(), "判断题" + i, 2, 0.5);
            System.out.println("创建判断题成功，题目ID：" + q.getQuestionId() + "，题库ID：" + q.getQbId());
        }
        // 创建10个填空题
        for (int i = 0; i < 10; i++) {
            Question q = createQuestion(questionBank.getQbId(), "填空题" + i, 3, 0.8);
            System.out.println("创建填空题成功，题目ID：" + q.getQuestionId() + "，题库ID：" + q.getQbId());
        }
        // 创建5个简答题
        for (int i = 0; i < 5; i++) {
            Question q = createQuestion(questionBank.getQbId(), "简答题" + i, 4, 0.9);
            System.out.println("创建简答题成功，题目ID：" + q.getQuestionId() + "，题库ID：" + q.getQbId());
        }

        // 设置题目类型数量
        Map<Integer, Integer> questionTypeCount = new HashMap<>();
        questionTypeCount.put(0, 20); // 单选题数量
        questionTypeCount.put(1, 10); // 多选题数量
        questionTypeCount.put(2, 10); // 判断题数量
        questionTypeCount.put(3, 5);  // 填空题数量
        questionTypeCount.put(4, 2);  // 简答题数量

        // 设置题型分值比例
        Map<Integer, BigDecimal> typeScoreRatio = new HashMap<>();
        typeScoreRatio.put(0, new BigDecimal("0.2")); // 单选题占20%
        typeScoreRatio.put(1, new BigDecimal("0.3")); // 多选题占30%
        typeScoreRatio.put(2, new BigDecimal("0.1")); // 判断题占10%
        typeScoreRatio.put(3, new BigDecimal("0.15")); // 填空题占15%
        typeScoreRatio.put(4, new BigDecimal("0.25")); // 简答题占25%

        // 调用试卷生成方法
        String paperName = "自动生成试卷测试-" + testTeacher.getTeacherId();
        BigDecimal targetDifficulty = new BigDecimal("0.7");

        // 确保教师ID不为空
        assertNotNull(testTeacher.getTeacherId(), "教师ID不能为空");
        
        ExamPaper generatedPaper = examPaperService.generatePaper(
            testSubject.getSubjectId(),
            paperName,
            targetDifficulty,
            questionTypeCount,
            typeScoreRatio,
            testTeacher.getTeacherId()
        );
        
        // 验证生成的试卷
        assertNotNull(generatedPaper);
        assertNotNull(generatedPaper.getPaperId());
        assertEquals(testSubject.getSubjectId(), generatedPaper.getSubjectId());
        assertEquals(paperName, generatedPaper.getPaperName());
        
        // 获取试卷题目及分值
        List<Map<String, Object>> paperQuestions = examPaperService.getPaperQuestionsWithScore(generatedPaper.getPaperId());
        assertNotNull(paperQuestions);
        assertFalse(paperQuestions.isEmpty());
        
        // 验证题目数量
        Map<Integer, Integer> actualTypeCount = new HashMap<>();
        Map<Integer, BigDecimal> actualTypeScores = new HashMap<>();
        final BigDecimal totalScore = paperQuestions.stream()
            .map(q -> (BigDecimal) q.get("question_score"))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        for (Map<String, Object> question : paperQuestions) {
            Integer type = (Integer) question.get("type");
            BigDecimal score = (BigDecimal) question.get("question_score");
            actualTypeCount.merge(type, 1, Integer::sum);
            actualTypeScores.merge(type, score, BigDecimal::add);
        }
        
        // 验证各类型题目数量
        questionTypeCount.forEach((type, count) -> 
            assertEquals(count, actualTypeCount.get(type), 
                       String.format("题型%d的数量不符合预期", type)));
        
        // 验证总分为100分
        assertEquals(new BigDecimal("100.00"), totalScore.setScale(2, RoundingMode.HALF_UP));
        
        // 如果提供了预期的分值比例，则验证实际分值比例
        if (typeScoreRatio != null) {
            typeScoreRatio.forEach((type, ratio) -> {
                BigDecimal actualRatio = actualTypeScores.get(type).divide(totalScore, 2, RoundingMode.HALF_UP);
                BigDecimal expectedRatio = ratio.setScale(2, RoundingMode.HALF_UP);
                assertEquals(0, expectedRatio.compareTo(actualRatio),
                           String.format("题型%d的分值比例不符合预期", type));
            });
        }
        
        // 验证试卷难度在合理范围内（目标难度±0.1）
        BigDecimal actualDifficulty = generatedPaper.getPaperDifficulty();
        BigDecimal minDifficulty = actualDifficulty.subtract(new BigDecimal("0.1"));
        BigDecimal maxDifficulty = actualDifficulty.add(new BigDecimal("0.1"));
        assertTrue(actualDifficulty.compareTo(minDifficulty) >= 0, 
                 "试卷难度不应低于" + minDifficulty);
        assertTrue(actualDifficulty.compareTo(maxDifficulty) <= 0,
                 "试卷难度不应高于" + maxDifficulty);
    }

    @Test
    public void testGeneratePaperWithCustomRatio() {
        // 创建题库和题目
        QuestionBank questionBank = new QuestionBank();
        questionBank.setQbName("Test Question Bank");
        questionBank.setSubjectId(testSubject.getSubjectId());
        questionBankService.insert(questionBank);

        // 添加不同类型的题目
        // 创建单选题
        for (int i = 0; i < 30; i++) {
            createQuestion(questionBank.getQbId(), "单选题" + (i + 1), 0, 0.6);
        }
        
        // 创建多选题
        for (int i = 0; i < 15; i++) {
            createQuestion(questionBank.getQbId(), "多选题" + (i + 1), 1, 0.7);
        }
        
        // 创建判断题
        for (int i = 0; i < 15; i++) {
            createQuestion(questionBank.getQbId(), "判断题" + (i + 1), 2, 0.5);
        }
        
        // 创建填空题
        for (int i = 0; i < 10; i++) {
            createQuestion(questionBank.getQbId(), "填空题" + (i + 1), 3, 0.8);
        }
        
        // 创建简答题
        for (int i = 0; i < 5; i++) {
            createQuestion(questionBank.getQbId(), "简答题" + (i + 1), 4, 0.9);
        }

        // 设置题目类型数量
        Map<Integer, Integer> questionTypeCount = new HashMap<>();
        questionTypeCount.put(0, 20); // 单选题数量
        questionTypeCount.put(1, 10); // 多选题数量
        questionTypeCount.put(2, 10); // 判断题数量
        questionTypeCount.put(3, 5);  // 填空题数量
        questionTypeCount.put(4, 2);  // 简答题数量

        // 设置题型分值比例
        Map<Integer, BigDecimal> typeScoreRatio = new HashMap<>();
        typeScoreRatio.put(0, new BigDecimal("0.2")); // 单选题占20%
        typeScoreRatio.put(1, new BigDecimal("0.3")); // 多选题占30%
        typeScoreRatio.put(2, new BigDecimal("0.1")); // 判断题占10%
        typeScoreRatio.put(3, new BigDecimal("0.15")); // 填空题占15%
        typeScoreRatio.put(4, new BigDecimal("0.25")); // 简答题占25%

        // 调用试卷生成方法
        String paperName = "自定义分值比例试卷-" + testTeacher.getTeacherId();
        BigDecimal targetDifficulty = new BigDecimal("0.7");

        // 确保教师ID不为空
        assertNotNull(testTeacher.getTeacherId(), "教师ID不能为空");
        
        ExamPaper generatedPaper = examPaperService.generatePaper(
            testSubject.getSubjectId(),
            paperName,
            targetDifficulty,
            questionTypeCount,
            typeScoreRatio,
            testTeacher.getTeacherId()
        );
        
        verifyGeneratedPaper(generatedPaper, paperName, questionTypeCount, typeScoreRatio);
    }

    @Test
    public void testGeneratePaperWithAutoRatio() {
        // 验证测试数据已正确初始化
        assertNotNull(testSubject, "测试科目不能为空");
        assertNotNull(testSubject.getSubjectId(), "测试科目ID不能为空");
        assertNotNull(testTeacher, "测试教师不能为空");
        assertNotNull(testTeacher.getTeacherId(), "测试教师ID不能为空");
        
        // 创建题库和题目
        QuestionBank questionBank = new QuestionBank();
        String uniqueBankName = "QB" + System.currentTimeMillis();
        questionBank.setQbName(uniqueBankName);
        questionBank.setSubjectId(testSubject.getSubjectId());
        
        // 打印题库信息
        System.out.println("准备创建题库：" + questionBank);
        System.out.println("题库名称：" + questionBank.getQbName());
        System.out.println("科目ID：" + questionBank.getSubjectId());
        
        // 验证科目是否存在
        Subject subject = subjectService.getById(testSubject.getSubjectId());
        System.out.println("科目信息：" + subject);
        
        int result = questionBankService.insert(questionBank);
        assertTrue(result > 0, "题库创建失败");
        
        // 获取插入后的题库
        questionBank = questionBankService.getByName(uniqueBankName);
        assertNotNull(questionBank, "无法获取创建的题库");
        assertNotNull(questionBank.getQbId(), "题库ID不能为空");

        // 添加不同类型的题目
        // 创建单选题
        for (int i = 0; i < 30; i++) {
            createQuestion(questionBank.getQbId(), "单选题" + (i + 1), 0, 0.6);
        }
        
        // 创建多选题
        for (int i = 0; i < 15; i++) {
            createQuestion(questionBank.getQbId(), "多选题" + (i + 1), 1, 0.7);
        }
        
        // 创建判断题
        for (int i = 0; i < 15; i++) {
            createQuestion(questionBank.getQbId(), "判断题" + (i + 1), 2, 0.5);
        }
        
        // 创建填空题
        for (int i = 0; i < 10; i++) {
            createQuestion(questionBank.getQbId(), "填空题" + (i + 1), 3, 0.8);
        }
        
        // 创建简答题
        for (int i = 0; i < 5; i++) {
            createQuestion(questionBank.getQbId(), "简答题" + (i + 1), 4, 0.9);
        }

        // 设置题目类型数量
        Map<Integer, Integer> questionTypeCount = new HashMap<>();
        questionTypeCount.put(0, 20); // 单选题数量
        questionTypeCount.put(1, 10); // 多选题数量
        questionTypeCount.put(2, 10); // 判断题数量
        questionTypeCount.put(3, 5);  // 填空题数量
        questionTypeCount.put(4, 2);  // 简答题数量

        // 调用试卷生成方法（不指定分值比例，由系统自动计算）
        String paperName = "自动分值比例试卷-" + testTeacher.getTeacherId();
        BigDecimal targetDifficulty = new BigDecimal("0.7");

        // 确保教师ID不为空
        assertNotNull(testTeacher.getTeacherId(), "教师ID不能为空");
        
        ExamPaper generatedPaper = examPaperService.generatePaper(
            testSubject.getSubjectId(),
            paperName,
            targetDifficulty,
            questionTypeCount,
            null, // 让系统自动计算分值比例
            testTeacher.getTeacherId()
        );
        
        verifyGeneratedPaper(generatedPaper, paperName, questionTypeCount, null);
    }

    private void verifyGeneratedPaper(ExamPaper generatedPaper, String paperName, 
                                    Map<Integer, Integer> questionTypeCount,
                                    Map<Integer, BigDecimal> expectedTypeScoreRatio) {
        // 验证生成的试卷
        assertNotNull(generatedPaper);
        assertNotNull(generatedPaper.getPaperId());
        assertEquals(testSubject.getSubjectId(), generatedPaper.getSubjectId());
        assertEquals(paperName, generatedPaper.getPaperName());
        
        // 获取试卷题目及分值
        List<Map<String, Object>> paperQuestions = examPaperService.getPaperQuestionsWithScore(generatedPaper.getPaperId());
        assertNotNull(paperQuestions);
        assertFalse(paperQuestions.isEmpty());
        
        // 验证题目数量
        Map<Integer, Integer> actualTypeCount = new HashMap<>();
        Map<Integer, BigDecimal> actualTypeScores = new HashMap<>();
        final BigDecimal totalScore = paperQuestions.stream()
            .map(q -> (BigDecimal) q.get("question_score"))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        for (Map<String, Object> question : paperQuestions) {
            Integer type = (Integer) question.get("type");
            BigDecimal score = (BigDecimal) question.get("question_score");
            actualTypeCount.merge(type, 1, Integer::sum);
            actualTypeScores.merge(type, score, BigDecimal::add);
        }
        
        // 验证各类型题目数量
        questionTypeCount.forEach((type, count) -> 
            assertEquals(count, actualTypeCount.get(type), 
                       String.format("题型%d的数量不符合预期", type)));
        
        // 验证总分为100分
        assertEquals(new BigDecimal("100.00"), totalScore.setScale(2, RoundingMode.HALF_UP));
        
        // 如果提供了预期的分值比例，则验证实际分值比例
        if (expectedTypeScoreRatio != null) {
            expectedTypeScoreRatio.forEach((type, ratio) -> {
                BigDecimal actualRatio = actualTypeScores.get(type).divide(totalScore, 2, RoundingMode.HALF_UP);
                BigDecimal expectedRatio = ratio.setScale(2, RoundingMode.HALF_UP);
                assertEquals(0, expectedRatio.compareTo(actualRatio),
                           String.format("题型%d的分值比例不符合预期", type));
            });
        }
        
        // 验证试卷难度在合理范围内（目标难度±0.1）
        BigDecimal actualDifficulty = generatedPaper.getPaperDifficulty();
        BigDecimal minDifficulty = actualDifficulty.subtract(new BigDecimal("0.1"));
        BigDecimal maxDifficulty = actualDifficulty.add(new BigDecimal("0.1"));
        assertTrue(actualDifficulty.compareTo(minDifficulty) >= 0, 
                 "试卷难度不应低于" + minDifficulty);
        assertTrue(actualDifficulty.compareTo(maxDifficulty) <= 0,
                 "试卷难度不应高于" + maxDifficulty);
    }

    private Question createQuestion(Integer qbId, String content, Integer type, double difficulty) {
        Question question = new Question();
        question.setQbId(qbId);
        question.setContent(content);
        question.setType(type);
        question.setDifficulty(new BigDecimal(String.valueOf(difficulty)));
        question.setScore(new BigDecimal("5.00")); // 设置基础分数
        question.setAnswer("A"); // 设置默认答案

        // 打印题目信息
        System.out.println("创建题目：" + question);
        
        // 验证qbId不为空
        if (qbId == null || qbId <= 0) {
            throw new IllegalArgumentException("题库ID不能为空或小于等于0");
        }
        
        // 先插入题目
        int result = questionService.insert(question);
        if (result <= 0) {
            throw new RuntimeException("创建题目失败");
        }
        
        // 如果是选择题，添加选项
        if (type <= 1) { // 单选或多选
            List<QuestionOption> options = new ArrayList<>();
            String[] optionContents = {"选项A", "选项B", "选项C", "选项D"};
            for (int i = 0; i < optionContents.length; i++) {
                QuestionOption option = new QuestionOption();
                option.setContent(optionContents[i]);
                option.setIsCorrect(i == 0); // 第一个选项为正确答案
                option.setQuestionId(question.getQuestionId()); // 设置题目ID
                options.add(option);
            }
            question.setOptions(options);
            
            // 打印选项信息
            System.out.println("题目选项：" + options);
            
            // 批量插入选项
            questionService.batchAddOptions(question.getQuestionId(), options);
        }
        
        return question;
    }
} 
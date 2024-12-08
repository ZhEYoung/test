package com.exam.mapper;

import com.exam.entity.Question;
import com.exam.entity.QuestionOption;
import com.exam.util.TestDataGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
public class QuestionMapperTest {

    @Autowired
    private QuestionMapper questionMapper;

    @Test
    void testSelectByBankId() {
        // 准备测试数据
        Question question = createQuestion(1);
        questionMapper.insert(question);
        
        // 执行测试
        List<Question> questions = questionMapper.selectByBankId(1);
        
        // 验证结果
        assertNotNull(questions);
        assertFalse(questions.isEmpty());
        questions.forEach(q -> assertEquals(1, q.getQbId()));
    }

    @Test
    void testSelectByType() {
        // 准备测试数据
        Question question1 = createQuestion(1);
        question1.setType(0); // 单选题
        Question question2 = createQuestion(1);
        question2.setType(1); // 多选题
        
        questionMapper.insert(question1);
        questionMapper.insert(question2);
        
        // 执行测试
        List<Question> singleChoiceQuestions = questionMapper.selectByType(0);
        
        // 验证结果
        assertNotNull(singleChoiceQuestions);
        assertFalse(singleChoiceQuestions.isEmpty());
        singleChoiceQuestions.forEach(q -> assertEquals(0, q.getType()));
    }

    @Test
    void testSelectByDifficultyRange() {
        // 准备测试数据
        Question easyQuestion = createQuestion(1);
        easyQuestion.setDifficulty(new BigDecimal("1.5"));
        Question hardQuestion = createQuestion(1);
        hardQuestion.setDifficulty(new BigDecimal("4.5"));
        
        questionMapper.insert(easyQuestion);
        questionMapper.insert(hardQuestion);
        
        // 执行测试
        List<Question> mediumQuestions = questionMapper.selectByDifficultyRange(new BigDecimal("2.0"), new BigDecimal("4.0"));
        
        // 验证结果
        assertNotNull(mediumQuestions);
        mediumQuestions.forEach(q -> {
            assertTrue(q.getDifficulty().compareTo(new BigDecimal("2.0")) >= 0);
            assertTrue(q.getDifficulty().compareTo(new BigDecimal("4.0")) <= 0);
        });
    }

    @Test
    void testSelectByContent() {
        // 准备测试数据
        Question question = createQuestion(1);
        question.setContent("Java多线程编程");
        questionMapper.insert(question);
        
        // 执行测试
        List<Question> questions = questionMapper.selectByContent("多线程");
        
        // 验证结果
        assertNotNull(questions);
        assertFalse(questions.isEmpty());
        questions.forEach(q -> assertTrue(q.getContent().contains("多线程")));
    }

    @Test
    void testCalculateCorrectRate() {
        // 准备测试数据
        Question question = createQuestion(1);
        questionMapper.insert(question);
        
        // 执行测试
        BigDecimal correctRate = questionMapper.calculateCorrectRate(question.getQuestionId());
        
        // 验证结果
        assertNotNull(correctRate);
        assertTrue(correctRate.compareTo(BigDecimal.ZERO) >= 0 && correctRate.compareTo(BigDecimal.valueOf(100)) <= 0);
    }

    @Test
    void testCountByType() {
        // 准备测试数据
        Question singleChoice = createQuestion(1);
        singleChoice.setType(0);
        Question multiChoice = createQuestion(1);
        multiChoice.setType(1);
        
        questionMapper.insert(singleChoice);
        questionMapper.insert(multiChoice);
        
        // 执行测试
        List<Map<String, Object>> typeCounts = questionMapper.countByType();
        
        // 验证结果
        assertNotNull(typeCounts);
        assertFalse(typeCounts.isEmpty());
    }

    @Test
    void testSelectMostMistakes() {
        // 准备测试数据
        Question question1 = createQuestion(1);
        Question question2 = createQuestion(1);
        
        questionMapper.insert(question1);
        questionMapper.insert(question2);
        
        // 执行测试
        List<Question> questions = questionMapper.selectMostMistakes(5);
        
        // 验证结果
        assertNotNull(questions);
        assertTrue(questions.size() <= 5);
    }

    // 辅助方法
    private Question createQuestion(int qbId) {
        Question question = new Question();
        question.setQbId(qbId);
        question.setContent(TestDataGenerator.generateQuestionContent());
        question.setAnswer(TestDataGenerator.generateAnswer());
        question.setType(0); // 默认单选题
        question.setDifficulty(new BigDecimal(String.valueOf(TestDataGenerator.generateDifficulty())));
        return question;
    }
} 
package com.exam.mapper;

import com.exam.entity.StudentScore;
import com.exam.util.TestDataGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
public class StudentScoreMapperTest {

    @Autowired
    private StudentScoreMapper studentScoreMapper;

    @Test
    void testSelectScoreDistribution() {
        // 准备测试数据
        List<StudentScore> scores = new ArrayList<>();
        scores.add(createScore(1, 1, 95.0)); // 90-100
        scores.add(createScore(2, 1, 85.0)); // 80-89
        scores.add(createScore(3, 1, 75.0)); // 70-79
        scores.add(createScore(4, 1, 65.0)); // 60-69
        scores.add(createScore(5, 1, 55.0)); // 0-59
        
        scores.forEach(score -> studentScoreMapper.insert(score));
        
        // 执行测试
        List<Map<String, Object>> distribution = studentScoreMapper.selectScoreDistribution(1, 1);
        
        // 验证结果
        assertNotNull(distribution);
        assertFalse(distribution.isEmpty());
        assertEquals(5, distribution.size()); // 应该有5个分数段
        
        // 验证每个分数段都有一个学生
        distribution.forEach(range -> {
            assertEquals(1L, ((Number) range.get("count")).longValue());
        });
    }

    @Test
    void testSelectTopStudents() {
        // 准备测试数据
        List<StudentScore> scores = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            scores.add(createScore(i, 1, 90.0 + i)); // 创建10个高分学生
        }
        scores.forEach(score -> studentScoreMapper.insert(score));
        
        // 执行测试
        List<Map<String, Object>> topStudents = studentScoreMapper.selectTopStudents(1, 5);
        
        // 验证结果
        assertNotNull(topStudents);
        assertEquals(5, topStudents.size()); // 应该只返回前5名
        
        // 验证是否按分数降序排列
        double previousScore = Double.MAX_VALUE;
        for (Map<String, Object> student : topStudents) {
            double currentScore = ((Number) student.get("score")).doubleValue();
            assertTrue(currentScore <= previousScore);
            previousScore = currentScore;
        }
    }

    @Test
    void testSelectScoreSummary() {
        // 准备测试数据
        List<StudentScore> scores = new ArrayList<>();
        scores.add(createScore(1, 1, 100.0));
        scores.add(createScore(1, 2, 90.0));
        scores.add(createScore(1, 3, 80.0));
        scores.add(createScore(1, 4, 70.0));
        scores.add(createScore(1, 5, 60.0));
        
        scores.forEach(score -> studentScoreMapper.insert(score));
        
        // 执行测试
        Map<String, Object> summary = studentScoreMapper.selectScoreSummary(1, "2024-1");
        
        // 验证结果
        assertNotNull(summary);
        assertEquals(5L, ((Number) summary.get("total_exams")).longValue());
        assertEquals(80.0, ((Number) summary.get("avg_score")).doubleValue(), 0.01);
        assertEquals(100.0, ((Number) summary.get("highest_score")).doubleValue());
        assertEquals(60.0, ((Number) summary.get("lowest_score")).doubleValue());
        assertEquals(100.0, ((Number) summary.get("pass_rate")).doubleValue());
    }

    @Test
    void testAnalyzeScoreImprovement() {
        // 准备测试数据
        List<StudentScore> scores = new ArrayList<>();
        scores.add(createScore(1, 1, 70.0)); // 第一次考试
        scores.add(createScore(1, 2, 80.0)); // 第二次考试
        scores.add(createScore(1, 3, 90.0)); // 第三次考试
        
        scores.forEach(score -> studentScoreMapper.insert(score));
        
        // 执行测试
        List<Map<String, Object>> improvements = studentScoreMapper.analyzeScoreImprovement(1, 1);
        
        // 验证结果
        assertNotNull(improvements);
        assertEquals(2, improvements.size()); // 应该有两次进步记录
        
        improvements.forEach(improvement -> {
            double currentScore = ((Number) improvement.get("current_score")).doubleValue();
            double previousScore = ((Number) improvement.get("previous_score")).doubleValue();
            double improvementValue = ((Number) improvement.get("improvement")).doubleValue();
            assertEquals(currentScore - previousScore, improvementValue, 0.01);
            assertTrue(improvementValue > 0); // 验证成绩是否在提高
        });
    }

    @Test
    void testSelectSubjectAverages() {
        // 准备测试数据
        List<StudentScore> scores = new ArrayList<>();
        // 为同一个学生添加不同科目的成绩
        scores.add(createScore(1, 1, 90.0)); // 科目1
        scores.add(createScore(1, 2, 85.0)); // 科目1
        scores.add(createScore(1, 3, 80.0)); // 科目2
        scores.add(createScore(1, 4, 75.0)); // 科目2
        
        scores.forEach(score -> studentScoreMapper.insert(score));
        
        // 执行测试
        List<Map<String, Object>> averages = studentScoreMapper.selectSubjectAverages(1);
        
        // 验证结果
        assertNotNull(averages);
        assertFalse(averages.isEmpty());
        
        averages.forEach(average -> {
            assertNotNull(average.get("subject_name"));
            assertTrue(((Number) average.get("avg_score")).doubleValue() > 0);
            assertTrue(((Number) average.get("exam_count")).intValue() > 0);
        });
    }

    // 辅助方法
    private StudentScore createScore(int studentId, int examId, double score) {
        StudentScore studentScore = new StudentScore();
        studentScore.setStudentId(studentId);
        studentScore.setExamId(examId);
        studentScore.setScore(score);
        return studentScore;
    }
} 
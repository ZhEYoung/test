package com.exam.service.impl;

import com.exam.entity.ExamPaper;
import com.exam.entity.Question;
import com.exam.mapper.ExamPaperMapper;
import com.exam.mapper.QuestionMapper;
import com.exam.service.ExamPaperService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.math.BigDecimal;

/**
 * 试卷服务实现类
 */
@Service
@Transactional
public class ExamPaperServiceImpl extends BaseServiceImpl<ExamPaper, ExamPaperMapper> implements ExamPaperService {

    @Autowired
    private QuestionMapper questionMapper;

    @Override
    public List<ExamPaper> getBySubjectId(Integer subjectId) {
        return baseMapper.selectBySubjectId(subjectId);
    }

    @Override
    public List<ExamPaper> getByTeacherId(Integer teacherId) {
        return baseMapper.selectByTeacherId(teacherId);
    }

    @Override
    public ExamPaper getByName(String paperName) {
        return baseMapper.selectByName(paperName);
    }

    @Override
    public List<ExamPaper> getByStatus(Integer paperStatus) {
        return baseMapper.selectByStatus(paperStatus);
    }

    @Override
    public List<ExamPaper> getByExamType(Integer examType) {
        return baseMapper.selectByExamType(examType);
    }

    @Override
    public int updateStatus(Integer paperId, Integer status) {
        return baseMapper.updateStatus(paperId, status);
    }

    @Override
    public List<ExamPaper> getByDifficultyRange(BigDecimal minDifficulty, BigDecimal maxDifficulty) {
        return baseMapper.selectByDifficultyRange(minDifficulty, maxDifficulty);
    }

    @Override
    public List<Question> getPaperQuestions(Integer paperId) {
        return baseMapper.selectPaperQuestions(paperId);
    }

    @Override
    public List<Map<String, Object>> getPaperQuestionsWithScore(Integer paperId) {
        return baseMapper.selectPaperQuestionsWithScore(paperId);
    }

    @Override
    public int updateQuestionScore(Integer paperId, Integer questionId, BigDecimal score) {
        return baseMapper.updateQuestionScore(paperId, questionId, score);
    }

    @Override
    public int batchUpdateQuestionScores(Integer paperId, List<Integer> questionIds, List<BigDecimal> scores) {
        return baseMapper.batchUpdateQuestionScores(paperId, questionIds, scores);
    }

    @Override
    public BigDecimal calculateTotalScore(Integer paperId) {
        return baseMapper.calculateTotalScore(paperId);
    }

    @Override
    public BigDecimal calculateAverageScore(Integer paperId) {
        return baseMapper.calculateAverageScore(paperId);
    }

    @Override
    public BigDecimal calculatePassRate(Integer paperId, BigDecimal passScore) {
        return baseMapper.calculatePassRate(paperId, passScore);
    }

    @Override
    public List<Map<String, Object>> getScoreDistribution(Integer paperId) {
        return baseMapper.selectScoreDistribution(paperId);
    }

    @Override
    public BigDecimal getHighestScore(Integer paperId) {
        return baseMapper.selectHighestScore(paperId);
    }

    @Override
    public BigDecimal getLowestScore(Integer paperId) {
        return baseMapper.selectLowestScore(paperId);
    }

    @Override
    public int batchPublish(List<Integer> paperIds) {
        return baseMapper.batchPublish(paperIds);
    }

    @Override
    public int batchUpdateStatus(List<Integer> paperIds, Integer status) {
        return baseMapper.batchUpdateStatus(paperIds, status);
    }

    @Override
    public int copyPaper(Integer sourcePaperId, String newPaperName) {
        return baseMapper.copyPaper(sourcePaperId, newPaperName);
    }

    @Override
    public ExamPaper generatePaper(Integer subjectId, String paperName, 
                                 BigDecimal difficulty, Map<Integer, Integer> questionTypeCount) {
        // 创建新试卷
        ExamPaper paper = new ExamPaper();
        paper.setSubjectId(subjectId);
        paper.setPaperName(paperName);
        paper.setPaperDifficulty(difficulty);
        paper.setPaperStatus(0); // 未发布
        baseMapper.insert(paper);
        
        // 根据题型和数量选择题目
        List<Question> selectedQuestions = new ArrayList<>();
        BigDecimal totalScore = BigDecimal.ZERO;
        
        for (Map.Entry<Integer, Integer> entry : questionTypeCount.entrySet()) {
            Integer questionType = entry.getKey();
            Integer count = entry.getValue();
            
            // 根据难度范围和题型查询题目
            BigDecimal minDifficulty = difficulty.subtract(new BigDecimal("0.2"));
            BigDecimal maxDifficulty = difficulty.add(new BigDecimal("0.2"));
            Map<String, Object> condition = new HashMap<>();
            condition.put("type", questionType);
            condition.put("minDifficulty", minDifficulty);
            condition.put("maxDifficulty", maxDifficulty);
            condition.put("subjectId", subjectId);
            
            List<Question> questions = questionMapper.selectByCondition(condition);
            if (questions.size() < count) {
                // 题目数量不足
                return null;
            }
            
            // 随机选择指定数量的题目
            Collections.shuffle(questions);
            selectedQuestions.addAll(questions.subList(0, count));
            
            // 计算题目分值
            BigDecimal questionScore;
            switch (questionType) {
                case 1: // 单选题
                    questionScore = new BigDecimal("2");
                    break;
                case 2: // 多选题
                    questionScore = new BigDecimal("4");
                    break;
                case 3: // 判断题
                    questionScore = new BigDecimal("1");
                    break;
                case 4: // 填空题
                    questionScore = new BigDecimal("3");
                    break;
                case 5: // 简答题
                    questionScore = new BigDecimal("10");
                    break;
                default:
                    questionScore = new BigDecimal("2");
            }
            
            totalScore = totalScore.add(questionScore.multiply(new BigDecimal(count)));
        }
        
        // 添加题目到试卷
        List<Integer> questionIds = new ArrayList<>();
        List<BigDecimal> scores = new ArrayList<>();
        for (Question question : selectedQuestions) {
            questionIds.add(question.getQuestionId());
            // 根据题型设置分值
            BigDecimal score;
            switch (question.getType()) {
                case 1: scores.add(new BigDecimal("2")); break;
                case 2: scores.add(new BigDecimal("4")); break;
                case 3: scores.add(new BigDecimal("1")); break;
                case 4: scores.add(new BigDecimal("3")); break;
                case 5: scores.add(new BigDecimal("10")); break;
                default: scores.add(new BigDecimal("2"));
            }
        }
        
        baseMapper.batchUpdateQuestionScores(paper.getPaperId(), questionIds, scores);
        
        // 更新试卷总分
        paper.setTotalScore(totalScore);
        baseMapper.updateById(paper);
        
        return paper;
    }
} 
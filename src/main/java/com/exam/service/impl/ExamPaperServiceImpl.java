package com.exam.service.impl;

import com.exam.entity.ExamPaper;
import com.exam.entity.Question;
import com.exam.mapper.ExamPaperMapper;
import com.exam.mapper.QuestionMapper;
import com.exam.service.ExamPaperService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.math.BigDecimal;
import java.util.Date;
import java.util.ArrayList;
import java.util.Collections;

/**
 * 试卷服务实现类
 */
@Service
public class ExamPaperServiceImpl implements ExamPaperService {

    @Autowired
    private ExamPaperMapper examPaperMapper;

    @Autowired
    private QuestionMapper questionMapper;

    @Override
    public int insert(ExamPaper record) {
        return examPaperMapper.insert(record);
    }

    @Override
    public int deleteById(Integer id) {
        return examPaperMapper.deleteById(id);
    }

    @Override
    public int updateById(ExamPaper record) {
        return examPaperMapper.update(record);
    }

    @Override
    public ExamPaper selectById(Integer id) {
        return examPaperMapper.selectById(id);
    }

    @Override
    public List<ExamPaper> selectAll() {
        return examPaperMapper.selectAll();
    }

    @Override
    public List<ExamPaper> selectPage(Integer pageNum, Integer pageSize) {
        Map<String, Object> condition = new HashMap<>();
        condition.put("offset", (pageNum - 1) * pageSize);
        condition.put("limit", pageSize);
        return examPaperMapper.selectPageByCondition(condition);
    }

    @Override
    public Long selectCount() {
        return examPaperMapper.selectCount();
    }

    @Override
    public List<ExamPaper> selectByCondition(Map<String, Object> condition) {
        return examPaperMapper.selectByCondition(condition);
    }

    @Override
    public Long selectCountByCondition(Map<String, Object> condition) {
        return examPaperMapper.selectCountByCondition(condition);
    }

    @Override
    public List<ExamPaper> selectPageByCondition(Map<String, Object> condition, Integer pageNum, Integer pageSize) {
        condition.put("offset", (pageNum - 1) * pageSize);
        condition.put("limit", pageSize);
        return examPaperMapper.selectPageByCondition(condition);
    }

    @Override
    public List<ExamPaper> getBySubjectId(Integer subjectId) {
        return examPaperMapper.selectBySubjectId(subjectId);
    }

    @Override
    public List<ExamPaper> getByTeacherId(Integer teacherId) {
        return examPaperMapper.selectByTeacherId(teacherId);
    }

    @Override
    public ExamPaper getByName(String paperName) {
        return examPaperMapper.selectByName(paperName);
    }

    @Override
    public List<ExamPaper> getByStatus(Integer paperStatus) {
        return examPaperMapper.selectByStatus(paperStatus);
    }

    @Override
    public List<ExamPaper> getByExamType(Integer examType) {
        return examPaperMapper.selectByExamType(examType);
    }

    @Override
    public int updateStatus(Integer paperId, Integer status) {
        return examPaperMapper.updateStatus(paperId, status);
    }

    @Override
    public List<ExamPaper> getByDifficultyRange(BigDecimal minDifficulty, BigDecimal maxDifficulty) {
        return examPaperMapper.selectByDifficultyRange(minDifficulty, maxDifficulty);
    }

    @Override
    public List<Question> getPaperQuestions(Integer paperId) {
        return examPaperMapper.selectPaperQuestions(paperId);
    }

    @Override
    public List<Map<String, Object>> getPaperQuestionsWithScore(Integer paperId) {
        return examPaperMapper.selectPaperQuestionsWithScore(paperId);
    }

    @Override
    public int updateQuestionScore(Integer paperId, Integer questionId, BigDecimal score) {
        return examPaperMapper.updateQuestionScore(paperId, questionId, score);
    }

    @Override
    public int batchUpdateQuestionScores(Integer paperId, List<Integer> questionIds, List<BigDecimal> scores) {
        return examPaperMapper.batchUpdateQuestionScores(paperId, questionIds, scores);
    }

    @Override
    public BigDecimal calculateTotalScore(Integer paperId) {
        return examPaperMapper.calculateTotalScore(paperId);
    }

    @Override
    public BigDecimal calculateAverageScore(Integer paperId) {
        return examPaperMapper.calculateAverageScore(paperId);
    }

    @Override
    public BigDecimal calculatePassRate(Integer paperId, BigDecimal passScore) {
        return examPaperMapper.calculatePassRate(paperId, passScore);
    }

    @Override
    public List<Map<String, Object>> getScoreDistribution(Integer paperId) {
        return examPaperMapper.selectScoreDistribution(paperId);
    }

    @Override
    public BigDecimal getHighestScore(Integer paperId) {
        return examPaperMapper.selectHighestScore(paperId);
    }

    @Override
    public BigDecimal getLowestScore(Integer paperId) {
        return examPaperMapper.selectLowestScore(paperId);
    }

    @Override
    @Transactional
    public int batchPublish(List<Integer> paperIds) {
        return examPaperMapper.batchPublish(paperIds);
    }

    @Override
    @Transactional
    public int batchUpdateStatus(List<Integer> paperIds, Integer status) {
        return examPaperMapper.batchUpdateStatus(paperIds, status);
    }

    @Override
    @Transactional
    public int copyPaper(Integer sourcePaperId, String newPaperName) {
        return examPaperMapper.copyPaper(sourcePaperId, newPaperName);
    }

    @Override
    @Transactional
    public ExamPaper generatePaper(Integer subjectId, String paperName, 
                                 BigDecimal difficulty, Map<Integer, Integer> questionTypeCount,
                                 Map<Integer, BigDecimal> typeScoreRatio) {
        // 验证题型数量和分数比例
        Map<Integer, BigDecimal> finalScoreRatio = new HashMap<>();
        BigDecimal totalRatio = BigDecimal.ZERO;
        
        if (typeScoreRatio != null) {
            // 使用传入的分数比例
            for (Map.Entry<Integer, BigDecimal> entry : typeScoreRatio.entrySet()) {
                Integer type = entry.getKey();
                BigDecimal ratio = entry.getValue();
                
                // 检查是否有对应的题目数量
                if (!questionTypeCount.containsKey(type) || questionTypeCount.get(type) <= 0) {
                    continue;
                }
                
                finalScoreRatio.put(type, ratio);
                totalRatio = totalRatio.add(ratio);
            }
            
            // 验证分数比例总和是否为1
            if (totalRatio.compareTo(BigDecimal.ONE) != 0) {
                throw new IllegalArgumentException("题型分数比例之和必须为1");
            }
        } else {
            // 根据题型数量自动计算分数比例
            for (Map.Entry<Integer, Integer> entry : questionTypeCount.entrySet()) {
                Integer type = entry.getKey();
                Integer count = entry.getValue();
                
                if (count <= 0) {
                    continue;
                }
                
                // 计算该题型在总题数中的占比作为分数比例
                BigDecimal ratio = new BigDecimal(count)
                    .divide(new BigDecimal(questionTypeCount.values().stream()
                        .mapToInt(Integer::intValue).sum()), 2, BigDecimal.ROUND_HALF_UP);
                finalScoreRatio.put(type, ratio);
                totalRatio = totalRatio.add(ratio);
            }
            
            // 调整最后一个题型的比例，确保总和为1
            if (totalRatio.compareTo(BigDecimal.ONE) != 0 && !finalScoreRatio.isEmpty()) {
                Integer lastType = finalScoreRatio.keySet().stream().max(Integer::compareTo).get();
                finalScoreRatio.put(lastType, finalScoreRatio.get(lastType)
                    .add(BigDecimal.ONE.subtract(totalRatio)));
            }
        }
        
        // 创建新试卷
        ExamPaper paper = new ExamPaper();
        paper.setPaperName(paperName);
        paper.setSubjectId(subjectId);
        paper.setPaperStatus(0); // 未发布
        paper.setCreatedTime(new Date());
        
        // 插入试卷记录
        int result = examPaperMapper.insert(paper);
        if (result == 0) {
            return null;
        }
        
        BigDecimal totalDifficultyScore = BigDecimal.ZERO;
        BigDecimal totalScore = BigDecimal.ZERO;
        
        // 为每种题型选择题目
        for (Map.Entry<Integer, Integer> entry : questionTypeCount.entrySet()) {
            Integer type = entry.getKey();
            Integer count = entry.getValue();
            
            if (count <= 0 || !finalScoreRatio.containsKey(type)) {
                continue;
            }
            
            // 计算该题型的总分
            BigDecimal typeScore = new BigDecimal("100").multiply(finalScoreRatio.get(type));
            // 计算每道题的分���
            BigDecimal questionScore = typeScore.divide(new BigDecimal(count), 2, BigDecimal.ROUND_HALF_UP);
            
            // 根据难度范围查询题目
            List<Question> questions = questionMapper.selectByType(type);
            questions = questions.stream()
                .filter(q -> q.getDifficulty().compareTo(difficulty.subtract(new BigDecimal("0.2"))) >= 0
                    && q.getDifficulty().compareTo(difficulty.add(new BigDecimal("0.2"))) <= 0)
                .collect(java.util.stream.Collectors.toList());
            
            // 如果题目数量不足，返回null
            if (questions.size() < count) {
                return null;
            }
            
            // 随机打乱题目列表
            Collections.shuffle(questions);
            
            // 随机选择指定数量的题目
            List<Question> selectedQuestions = questions.subList(0, count);
            
            // 计算选中题目的平均难度
            BigDecimal avgDifficulty = selectedQuestions.stream()
                .map(Question::getDifficulty)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(new BigDecimal(count), 2, BigDecimal.ROUND_HALF_UP);
                
            // 如果随机选择的题目平均难度偏离目标难度太多，重新选择
            if (avgDifficulty.subtract(difficulty).abs().compareTo(new BigDecimal("0.2")) > 0) {
                // 重新按难度排序并选择
                selectedQuestions = questions.stream()
                    .sorted((q1, q2) -> {
                        BigDecimal diff1 = q1.getDifficulty().subtract(difficulty).abs();
                        BigDecimal diff2 = q2.getDifficulty().subtract(difficulty).abs();
                        return diff1.compareTo(diff2);
                    })
                    .limit(count)
                    .collect(java.util.stream.Collectors.toList());
            }
            
            List<Integer> questionIds = selectedQuestions.stream()
                .map(Question::getQuestionId)
                .toList();
            
            // 计算该题型的加权难度
            BigDecimal typeDifficultyScore = selectedQuestions.stream()
                .map(q -> q.getDifficulty().multiply(questionScore))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            totalDifficultyScore = totalDifficultyScore.add(typeDifficultyScore);
            totalScore = totalScore.add(typeScore);
            
            // 批量添加题目到试卷
            List<BigDecimal> scores = new ArrayList<>(Collections.nCopies(count, questionScore));
            questionMapper.batchAddToPaper(paper.getPaperId(), questionIds, scores);
        }
        
        // 计算试卷的平均难度
        BigDecimal paperDifficulty = totalDifficultyScore.divide(totalScore, 2, BigDecimal.ROUND_HALF_UP);
        
        // 检查试卷难度是否在允许范围内
        if (paperDifficulty.subtract(difficulty).abs().compareTo(new BigDecimal("0.02")) > 0) {
            // 如果难度不符合要求，删除试卷
            examPaperMapper.deleteById(paper.getPaperId());
            return null;
        }
        
        // 更新试卷难度
        paper.setPaperDifficulty(paperDifficulty);
        examPaperMapper.update(paper);
        
        return paper;
    }
} 
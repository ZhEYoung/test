package com.exam.service.impl;

import com.exam.entity.Question;
import com.exam.entity.QuestionOption;
import com.exam.mapper.QuestionMapper;
import com.exam.mapper.QuestionOptionMapper;
import com.exam.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.math.BigDecimal;

/**
 * 题目服务实现类
 */
@Service
@Transactional
public class QuestionServiceImpl extends BaseServiceImpl<Question, QuestionMapper> implements QuestionService {

    @Autowired
    private QuestionOptionMapper optionMapper;

    @Override
    public List<Question> getByBankId(Integer qbId) {
        return baseMapper.selectByBankId(qbId);
    }

    @Override
    public List<Question> getByType(Integer type) {
        return baseMapper.selectByType(type);
    }

    @Override
    public List<Question> getByDifficultyRange(BigDecimal minDifficulty, BigDecimal maxDifficulty) {
        return baseMapper.selectByDifficultyRange(minDifficulty, maxDifficulty);
    }

    @Override
    public List<Question> getByContent(String content) {
        return baseMapper.selectByContent(content);
    }

    @Override
    public List<Question> getByIds(List<Integer> questionIds) {
        return baseMapper.selectByIds(questionIds);
    }

    @Override
    public List<QuestionOption> getOptions(Integer questionId) {
        return baseMapper.selectOptions(questionId);
    }

    @Override
    public int addOption(Integer questionId, QuestionOption option) {
        return baseMapper.insertOption(questionId, option);
    }

    @Override
    public int batchAddOptions(Integer questionId, List<QuestionOption> options) {
        return baseMapper.batchInsertOptions(questionId, options);
    }

    @Override
    public int updateOption(Integer optionId, QuestionOption option) {
        return baseMapper.updateOption(optionId, option);
    }

    @Override
    public int deleteOption(Integer optionId) {
        return baseMapper.deleteOption(optionId);
    }

    @Override
    public List<Question> getByPaperId(Integer paperId) {
        return baseMapper.selectByPaperId(paperId);
    }

    @Override
    public int batchAddToPaper(Integer paperId, List<Integer> questionIds, List<BigDecimal> scores) {
        return baseMapper.batchAddToPaper(paperId, questionIds, scores);
    }

    @Override
    public int removeFromPaper(Integer paperId, Integer questionId) {
        return baseMapper.removeFromPaper(paperId, questionId);
    }

    @Override
    public Long countUsage(Integer questionId) {
        return baseMapper.countUsage(questionId);
    }

    @Override
    public BigDecimal calculateCorrectRate(Integer questionId) {
        return baseMapper.calculateCorrectRate(questionId);
    }

    @Override
    public List<Map<String, Object>> countByType() {
        return baseMapper.countByType();
    }

    @Override
    public List<Map<String, Object>> countByDifficulty() {
        return baseMapper.countByDifficulty();
    }

    @Override
    public List<Question> getMostMistakes(Integer limit) {
        return baseMapper.selectMostMistakes(limit);
    }

    @Override
    public int updateDifficulty(Integer questionId, BigDecimal difficulty) {
        return baseMapper.updateDifficulty(questionId, difficulty);
    }

    @Override
    public int batchUpdateDifficulty(List<Integer> questionIds, List<BigDecimal> difficulties) {
        return baseMapper.batchUpdateDifficulty(questionIds, difficulties);
    }

    @Override
    public int batchImport(List<Question> questions) {
        // 批量导入前进行数据验证
        for (Question question : questions) {
            if (question.getContent() == null || question.getType() == null) {
                return 0;
            }
            // 如果是选择题，验证选项
            if (question.getType() <= 2 && 
                (question.getOptions() == null || question.getOptions().isEmpty())) {
                return 0;
            }
        }
        
        int result = baseMapper.batchInsert(questions);
        
        // 批量插入选项
        for (Question question : questions) {
            if (question.getOptions() != null && !question.getOptions().isEmpty()) {
                optionMapper.batchInsert(question.getOptions());
            }
        }
        
        return result;
    }

    @Override
    public List<Question> recommendSimilarQuestions(Integer questionId, Integer limit) {
        // 获取原题目信息
        Question question = baseMapper.selectById(questionId);
        if (question == null) {
            return new ArrayList<>();
        }
        
        // 根据题目类型和难度范围查找相似题目
        BigDecimal difficulty = question.getDifficulty();
        BigDecimal minDifficulty = difficulty.subtract(new BigDecimal("0.2"));
        BigDecimal maxDifficulty = difficulty.add(new BigDecimal("0.2"));
        
        Map<String, Object> condition = new HashMap<>();
        condition.put("type", question.getType());
        condition.put("minDifficulty", minDifficulty);
        condition.put("maxDifficulty", maxDifficulty);
        condition.put("subjectId", question.getSubjectId());
        
        List<Question> similarQuestions = baseMapper.selectByCondition(condition);
        
        // 排除原题目
        similarQuestions.removeIf(q -> q.getId().equals(questionId));
        
        // 按使用次数和正确率排序
        similarQuestions.sort((q1, q2) -> {
            Long usage1 = countUsage(q1.getId());
            Long usage2 = countUsage(q2.getId());
            if (usage1.equals(usage2)) {
                BigDecimal rate1 = calculateCorrectRate(q1.getId());
                BigDecimal rate2 = calculateCorrectRate(q2.getId());
                return rate2.compareTo(rate1);
            }
            return usage2.compareTo(usage1);
        });
        
        // 返回指定数量的题目
        return similarQuestions.size() > limit ? 
               similarQuestions.subList(0, limit) : similarQuestions;
    }
} 
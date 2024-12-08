package com.exam.service.impl;

import com.exam.entity.QuestionBank;
import com.exam.entity.Question;
import com.exam.mapper.QuestionBankMapper;
import com.exam.mapper.QuestionMapper;
import com.exam.service.QuestionBankService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.math.BigDecimal;

/**
 * 题库服务实现类
 */
@Service
@Transactional
public class QuestionBankServiceImpl extends BaseServiceImpl<QuestionBank, QuestionBankMapper> implements QuestionBankService {

    @Autowired
    private QuestionMapper questionMapper;

    @Override
    public List<QuestionBank> getBySubjectId(Integer subjectId) {
        return baseMapper.selectBySubjectId(subjectId);
    }

    @Override
    public QuestionBank getByName(String qbName) {
        return baseMapper.selectByName(qbName);
    }

    @Override
    public Integer countQuestions(Integer qbId) {
        return baseMapper.countQuestions(qbId);
    }

    @Override
    public Integer countBySubjectId(Integer subjectId) {
        return baseMapper.countBySubjectId(subjectId);
    }

    @Override
    public int addQuestion(Integer qbId, Integer questionId) {
        return baseMapper.addQuestion(qbId, questionId);
    }

    @Override
    public int batchAddQuestions(Integer qbId, List<Integer> questionIds) {
        return baseMapper.batchAddQuestions(qbId, questionIds);
    }

    @Override
    public int removeQuestion(Integer qbId, Integer questionId) {
        return baseMapper.removeQuestion(qbId, questionId);
    }

    @Override
    public int batchRemoveQuestions(Integer qbId, List<Integer> questionIds) {
        return baseMapper.batchRemoveQuestions(qbId, questionIds);
    }

    @Override
    public List<Question> getQuestions(Integer qbId) {
        return baseMapper.selectQuestions(qbId);
    }

    @Override
    public List<Question> getQuestionsByCondition(Integer qbId, Integer type, 
                                                BigDecimal minDifficulty, BigDecimal maxDifficulty) {
        return baseMapper.selectQuestionsByCondition(qbId, type, minDifficulty, maxDifficulty);
    }

    @Override
    public List<Map<String, Object>> countQuestionsByType(Integer qbId) {
        return baseMapper.countQuestionsByType(qbId);
    }

    @Override
    public List<Map<String, Object>> countQuestionsByDifficulty(Integer qbId) {
        return baseMapper.countQuestionsByDifficulty(qbId);
    }

    @Override
    public List<Map<String, Object>> countBankUsage(Integer qbId) {
        return baseMapper.countBankUsage(qbId);
    }

    @Override
    public int copyBank(Integer sourceQbId, String newBankName, Integer subjectId) {
        return baseMapper.copyBank(sourceQbId, newBankName, subjectId);
    }

    @Override
    public int mergeBanks(Integer targetQbId, List<Integer> sourceQbIds) {
        return baseMapper.mergeBanks(targetQbId, sourceQbIds);
    }

    @Override
    public List<QuestionBank> getRecentUsed(Integer limit) {
        return baseMapper.selectRecentUsed(limit);
    }

    @Override
    public List<QuestionBank> getHotBanks(Integer limit) {
        return baseMapper.selectHotBanks(limit);
    }

    @Override
    public int importBank(QuestionBank bank, List<Question> questions) {
        // 先插入题库
        int result = baseMapper.insert(bank);
        if (result == 0) {
            return 0;
        }
        
        // 批量插入题目
        if (!questions.isEmpty()) {
            questionMapper.batchInsert(questions);
            
            // 获取题目ID列表
            List<Integer> questionIds = new ArrayList<>();
            for (Question question : questions) {
                questionIds.add(question.getQuestionId());
            }
            
            // 建立题库和题目的关联
            baseMapper.batchAddQuestions(bank.getQbId(), questionIds);
        }
        
        return result;
    }

    @Override
    public Map<String, Object> exportBank(Integer qbId) {
        Map<String, Object> data = new HashMap<>();
        
        // 获取题库信息
        QuestionBank bank = baseMapper.selectById(qbId);
        if (bank == null) {
            return data;
        }
        data.put("bank", bank);
        
        // 获取题库中的所有题目
        List<Question> questions = baseMapper.selectQuestions(qbId);
        data.put("questions", questions);
        
        // 获取题目统计信息
        data.put("questionTypeStats", countQuestionsByType(qbId));
        data.put("questionDifficultyStats", countQuestionsByDifficulty(qbId));
        data.put("usageStats", countBankUsage(qbId));
        
        return data;
    }
} 
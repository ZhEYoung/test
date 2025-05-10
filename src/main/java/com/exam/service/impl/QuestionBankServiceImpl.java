package com.exam.service.impl;

import com.exam.entity.QuestionBank;
import com.exam.entity.Question;
import com.exam.mapper.QuestionBankMapper;
import com.exam.mapper.QuestionMapper;
import com.exam.mapper.QuestionOptionMapper;
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
public class QuestionBankServiceImpl implements QuestionBankService {

    @Autowired
    private QuestionBankMapper questionBankMapper;

    @Autowired
    private QuestionMapper questionMapper;

    @Autowired
    private QuestionOptionMapper optionMapper;

    @Override
    public int insert(QuestionBank questionBank) {
        // 验证题库名称
        if (questionBank == null || questionBank.getQbName() == null || 
            questionBank.getQbName().trim().isEmpty() || 
            questionBank.getQbName().length() > 20) {
            return 0;
        }
        
        // 验证学科ID
        if (questionBank.getSubjectId() == null || questionBank.getSubjectId() <= 0) {
            return 0;
        }
        
        return questionBankMapper.insert(questionBank);
    }

    @Override
    @Transactional
    public int deleteById(Integer qbId) {
        // 1. 获取题库下的所有题目
        List<Question> questions = questionBankMapper.selectQuestions(qbId);
        
        // 2. 删除每个题目的选项
        for (Question question : questions) {
            optionMapper.deleteByQuestionId(question.getQuestionId());
        }
        
        // 3. 删除题库下的所有题目
        questionMapper.deleteByBankId(qbId);
        
        // 4. 最后删除题库
        return questionBankMapper.deleteById(qbId);
    }

    @Override
    public int update(QuestionBank questionBank) {
        // 验证基本参数
        if (questionBank == null || questionBank.getQbId() == null) {
            return 0;
        }
        
        // 如果要更新题库名称，进行验证
        if (questionBank.getQbName() != null) {
            if (questionBank.getQbName().trim().isEmpty() || 
                questionBank.getQbName().length() > 20) {
                return 0;
            }
        }
        
        // 如果要更新学科ID，进行验证
        if (questionBank.getSubjectId() != null && questionBank.getSubjectId() <= 0) {
            return 0;
        }
        
        return questionBankMapper.update(questionBank);
    }

    @Override
    public QuestionBank selectById(Integer qbId) {
        return questionBankMapper.selectById(qbId);
    }

    @Override
    public List<QuestionBank> selectAll() {
        return questionBankMapper.selectAll();
    }
    
    @Override
    public List<QuestionBank> selectPage(Integer pageNum, Integer pageSize) {
        if (pageNum == null || pageSize == null || pageNum < 1 || pageSize < 1) {
            return new ArrayList<>();
        }
        Map<String, Object> params = new HashMap<>();
        params.put("offset", (pageNum - 1) * pageSize);
        params.put("limit", pageSize);
        return questionBankMapper.selectPage(params);
    }
    
    @Override
    public List<QuestionBank> selectByCondition(Map<String, Object> condition) {
        if (condition == null) {
            return new ArrayList<>();
        }
        return questionBankMapper.selectByCondition(condition);
    }
    
    @Override
    public List<QuestionBank> selectPageByCondition(Map<String, Object> condition, Integer pageNum, Integer pageSize) {
        if (condition == null || pageNum == null || pageSize == null || pageNum < 1 || pageSize < 1) {
            return new ArrayList<>();
        }
        condition.put("offset", (pageNum - 1) * pageSize);
        condition.put("limit", pageSize);
        return questionBankMapper.selectPageByCondition(condition);
    }
    
    @Override
    public Long selectCount() {
        return questionBankMapper.selectCount();
    }
    
    @Override
    public Long selectCountByCondition(Map<String, Object> condition) {
        if (condition == null) {
            return 0L;
        }
        return questionBankMapper.selectCountByCondition(condition);
    }

    @Override
    public List<QuestionBank> getBySubjectId(Integer subjectId) {
        return questionBankMapper.selectBySubjectId(subjectId);
    }

    @Override
    public QuestionBank getByName(String qbName) {
        return questionBankMapper.selectByName(qbName);
    }

    @Override
    public Integer countQuestions(Integer qbId) {
        return questionBankMapper.countQuestions(qbId);
    }

    @Override
    public Integer countBySubjectId(Integer subjectId) {
        return questionBankMapper.countBySubjectId(subjectId);
    }

    @Override
    public int addQuestion(Integer qbId, Integer questionId) {
        if (qbId == null || questionId == null) {
            return 0;
        }
        return questionBankMapper.addQuestion(qbId, questionId);
    }

    @Override
    public int batchAddQuestions(Integer qbId, List<Integer> questionIds) {
        if (qbId == null || questionIds == null || questionIds.isEmpty()) {
            return 0;
        }
        return questionBankMapper.batchAddQuestions(qbId, questionIds);
    }

    @Override
    public int removeQuestion(Integer qbId, Integer questionId) {
        if (qbId == null || questionId == null) {
            return 0;
        }
        return questionBankMapper.removeQuestion(qbId, questionId);
    }

    @Override
    public int batchRemoveQuestions(Integer qbId, List<Integer> questionIds) {
        if (qbId == null || questionIds == null || questionIds.isEmpty()) {
            return 0;
        }
        return questionBankMapper.batchRemoveQuestions(qbId, questionIds);
    }

    @Override
    public List<Question> getQuestions(Integer qbId) {
        return questionBankMapper.selectQuestions(qbId);
    }

    @Override
    public List<Question> getQuestionsByCondition(Integer qbId, Integer type, 
                                                BigDecimal minDifficulty, BigDecimal maxDifficulty) {
        return questionBankMapper.selectQuestionsByCondition(qbId, type, minDifficulty, maxDifficulty);
    }

    @Override
    public List<Map<String, Object>> countQuestionsByType(Integer qbId) {
        return questionBankMapper.countQuestionsByType(qbId);
    }

    @Override
    public List<Map<String, Object>> countQuestionsByDifficulty(Integer qbId) {
        return questionBankMapper.countQuestionsByDifficulty(qbId);
    }

    @Override
    public List<Map<String, Object>> countBankUsage(Integer qbId) {
        return questionBankMapper.countBankUsage(qbId);
    }

    @Override
    public int copyBank(Integer sourceQbId, String newBankName, Integer subjectId) {
        if (sourceQbId == null || newBankName == null || subjectId == null) {
            return 0;
        }
        return questionBankMapper.copyBank(sourceQbId, newBankName, subjectId);
    }

    @Override
    public int mergeBanks(Integer targetQbId, List<Integer> sourceQbIds) {
        if (targetQbId == null || sourceQbIds == null || sourceQbIds.isEmpty()) {
            return 0;
        }
        return questionBankMapper.mergeBanks(targetQbId, sourceQbIds);
    }

    @Override
    public List<QuestionBank> getRecentUsed(Integer limit) {
        return questionBankMapper.selectRecentUsed(limit);
    }

    @Override
    public List<QuestionBank> getHotBanks(Integer limit) {
        return questionBankMapper.selectHotBanks(limit);
    }
} 
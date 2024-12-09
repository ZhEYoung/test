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
public class QuestionBankServiceImpl implements QuestionBankService {

    @Autowired
    private QuestionBankMapper questionBankMapper;
    
    @Autowired
    private QuestionMapper questionMapper;

    // 基础CRUD方法
    @Override
    public int insert(QuestionBank record) {
        return questionBankMapper.insert(record);
    }

    @Override
    public int deleteById(Integer id) {
        return questionBankMapper.deleteById(id);
    }

    @Override
    public int updateById(QuestionBank record) {
        return questionBankMapper.updateById(record);
    }

    @Override
    public QuestionBank selectById(Integer id) {
        return questionBankMapper.selectById(id);
    }

    @Override
    public List<QuestionBank> selectAll() {
        return questionBankMapper.selectAll();
    }

    @Override
    public List<QuestionBank> selectPage(Integer pageNum, Integer pageSize) {
        int offset = (pageNum - 1) * pageSize;
        return questionBankMapper.selectPage(offset, pageSize);
    }

    @Override
    public Long selectCount() {
        return questionBankMapper.selectCount();
    }

    @Override
    public List<QuestionBank> selectByCondition(Map<String, Object> condition) {
        return questionBankMapper.selectByCondition(condition);
    }

    @Override
    public Long selectCountByCondition(Map<String, Object> condition) {
        return questionBankMapper.selectCountByCondition(condition);
    }

    @Override
    public List<QuestionBank> selectPageByCondition(Map<String, Object> condition, Integer pageNum, Integer pageSize) {
        int offset = (pageNum - 1) * pageSize;
        return questionBankMapper.selectPageByCondition(condition, offset, pageSize);
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
        return questionBankMapper.addQuestion(qbId, questionId);
    }

    @Override
    public int batchAddQuestions(Integer qbId, List<Integer> questionIds) {
        return questionBankMapper.batchAddQuestions(qbId, questionIds);
    }

    @Override
    public int removeQuestion(Integer qbId, Integer questionId) {
        return questionBankMapper.removeQuestion(qbId, questionId);
    }

    @Override
    public int batchRemoveQuestions(Integer qbId, List<Integer> questionIds) {
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
        return questionBankMapper.copyBank(sourceQbId, newBankName, subjectId);
    }

    @Override
    public int mergeBanks(Integer targetQbId, List<Integer> sourceQbIds) {
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

    @Override
    public int importBank(QuestionBank bank, List<Question> questions) {
        // 先插入题库
        int result = insert(bank);
        if (result == 0) {
            return 0;
        }
        
        // 批量插入题目
        if (questions != null && !questions.isEmpty()) {
            questionMapper.batchInsert(questions);
            
            // 获取题目ID列表
            List<Integer> questionIds = new ArrayList<>();
            for (Question question : questions) {
                questionIds.add(question.getQuestionId());
            }
            
            // 建立题库和题目的关联
            batchAddQuestions(bank.getQbId(), questionIds);
        }
        
        return result;
    }

    @Override
    public Map<String, Object> exportBank(Integer qbId) {
        Map<String, Object> data = new HashMap<>();
        
        // 获取题库信息
        QuestionBank bank = selectById(qbId);
        if (bank == null) {
            return data;
        }
        data.put("bank", bank);
        
        // 获取题库中的所有题目
        List<Question> questions = getQuestions(qbId);
        data.put("questions", questions);
        
        // 获取题目统计信息
        data.put("questionTypeStats", countQuestionsByType(qbId));
        data.put("questionDifficultyStats", countQuestionsByDifficulty(qbId));
        data.put("usageStats", countBankUsage(qbId));
        
        return data;
    }
} 
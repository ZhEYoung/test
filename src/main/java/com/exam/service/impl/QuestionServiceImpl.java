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
public class QuestionServiceImpl implements QuestionService {

    @Autowired
    private QuestionMapper questionMapper;
    
    @Autowired
    private QuestionOptionMapper optionMapper;

    // 基础CRUD方法
    @Override
    public int insert(Question record) {
        return questionMapper.insert(record);
    }

    @Override
    public int deleteById(Integer id) {
        return questionMapper.deleteById(id);
    }

    @Override
    public int updateById(Question record) {
        return questionMapper.updateById(record);
    }

    @Override
    public Question selectById(Integer id) {
        return questionMapper.selectById(id);
    }

    @Override
    public List<Question> selectAll() {
        return questionMapper.selectAll();
    }

    @Override
    public List<Question> selectPage(Integer pageNum, Integer pageSize) {
        int offset = (pageNum - 1) * pageSize;
        return questionMapper.selectPage(offset, pageSize);
    }

    @Override
    public Long selectCount() {
        return questionMapper.selectCount();
    }

    @Override
    public List<Question> selectByCondition(Map<String, Object> condition) {
        return questionMapper.selectByCondition(condition);
    }

    @Override
    public Long selectCountByCondition(Map<String, Object> condition) {
        return questionMapper.selectCountByCondition(condition);
    }

    @Override
    public List<Question> selectPageByCondition(Map<String, Object> condition, Integer pageNum, Integer pageSize) {
        int offset = (pageNum - 1) * pageSize;
        return questionMapper.selectPageByCondition(condition, offset, pageSize);
    }

    @Override
    public List<Question> getByBankId(Integer qbId) {
        return questionMapper.selectByBankId(qbId);
    }

    @Override
    public List<Question> getByType(Integer type) {
        return questionMapper.selectByType(type);
    }

    @Override
    public List<Question> getByDifficultyRange(BigDecimal minDifficulty, BigDecimal maxDifficulty) {
        return questionMapper.selectByDifficultyRange(minDifficulty, maxDifficulty);
    }

    @Override
    public List<Question> getByContent(String content) {
        return questionMapper.selectByContent(content);
    }

    @Override
    public List<Question> getByIds(List<Integer> questionIds) {
        return questionMapper.selectByIds(questionIds);
    }

    @Override
    public List<QuestionOption> getOptions(Integer questionId) {
        return questionMapper.selectOptions(questionId);
    }

    @Override
    public int addOption(Integer questionId, QuestionOption option) {
        return questionMapper.insertOption(questionId, option);
    }

    @Override
    public int batchAddOptions(Integer questionId, List<QuestionOption> options) {
        return questionMapper.batchInsertOptions(questionId, options);
    }

    @Override
    public int updateOption(Integer optionId, QuestionOption option) {
        return questionMapper.updateOption(optionId, option);
    }

    @Override
    public int deleteOption(Integer optionId) {
        return questionMapper.deleteOption(optionId);
    }

    @Override
    public List<Question> getByPaperId(Integer paperId) {
        return questionMapper.selectByPaperId(paperId);
    }

    @Override
    public int batchAddToPaper(Integer paperId, List<Integer> questionIds, List<BigDecimal> scores) {
        return questionMapper.batchAddToPaper(paperId, questionIds, scores);
    }

    @Override
    public int removeFromPaper(Integer paperId, Integer questionId) {
        return questionMapper.removeFromPaper(paperId, questionId);
    }

    @Override
    public Long countUsage(Integer questionId) {
        return questionMapper.countUsage(questionId);
    }

    @Override
    public BigDecimal calculateCorrectRate(Integer questionId) {
        return questionMapper.calculateCorrectRate(questionId);
    }

    @Override
    public List<Map<String, Object>> countByType() {
        return questionMapper.countByType();
    }

    @Override
    public List<Map<String, Object>> countByDifficulty() {
        return questionMapper.countByDifficulty();
    }

    @Override
    public List<Question> getMostMistakes(Integer limit) {
        return questionMapper.selectMostMistakes(limit);
    }

    @Override
    public int updateDifficulty(Integer questionId, BigDecimal difficulty) {
        return questionMapper.updateDifficulty(questionId, difficulty);
    }

    @Override
    public int batchUpdateDifficulty(List<Integer> questionIds, List<BigDecimal> difficulties) {
        return questionMapper.batchUpdateDifficulty(questionIds, difficulties);
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
        
        int result = questionMapper.batchInsert(questions);
        
        // 批量插入选项
        for (Question question : questions) {
            if (question.getOptions() != null && !question.getOptions().isEmpty()) {
                optionMapper.batchInsert(question.getOptions());
            }
        }
        
        return result;
    }
} 
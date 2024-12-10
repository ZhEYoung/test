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

    @Override
    public int insert(Question record) {
        // 验证基本数据
        if (record == null || 
            record.getContent() == null || 
            record.getContent().trim().isEmpty() ||
            record.getContent().length() > 1000) {
            return 0;
        }

        // 验证题库ID
        if (record.getQbId() == null || record.getQbId() <= 0) {
            return 0;
        }

        // 验证题目类型
        if (record.getType() == null || record.getType() < 0) {
            return 0;
        }

        // 验证难度值
        if (record.getDifficulty() == null || 
            record.getDifficulty().compareTo(BigDecimal.ZERO) < 0 || 
            record.getDifficulty().compareTo(BigDecimal.ONE) > 0) {
            return 0;
        }

        // 验证分数
        if (record.getScore() == null || record.getScore().compareTo(BigDecimal.ZERO) <= 0) {
            return 0;
        }

        int result = questionMapper.insert(record);
        if (result > 0 && record.getOptions() != null && !record.getOptions().isEmpty()) {
            for (QuestionOption option : record.getOptions()) {
                option.setQuestionId(record.getQuestionId());
            }
            optionMapper.batchInsert(record.getOptions());
        }
        return result;
    }

    @Override
    public int deleteById(Integer id) {
        // 先删除题目的所有选项
        optionMapper.deleteByQuestionId(id);
        // 再删除题目
        return questionMapper.deleteById(id);
    }

    @Override
    public int updateById(Question record) {
        return questionMapper.update(record);
    }

    @Override
    public Question selectById(Integer id) {
        Question question = questionMapper.selectById(id);
        if (question != null) {
            // 加载题目选项
            question.setOptions(optionMapper.selectByQuestionId(id));
        }
        return question;
    }

    @Override
    public List<Question> selectAll() {
        List<Question> questions = questionMapper.selectAll();
        // 加载所有题目的选项
        for (Question question : questions) {
            question.setOptions(optionMapper.selectByQuestionId(question.getQuestionId()));
        }
        return questions;
    }

    @Override
    public List<Question> getByBankId(Integer qbId) {
        List<Question> questions = questionMapper.selectByBankId(qbId);
        // 加载所有题目的选项
        for (Question question : questions) {
            question.setOptions(optionMapper.selectByQuestionId(question.getQuestionId()));
        }
        return questions;
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
        List<Question> questions = questionMapper.selectByIds(questionIds);
        // 加载所有题目的选项
        for (Question question : questions) {
            question.setOptions(optionMapper.selectByQuestionId(question.getQuestionId()));
        }
        return questions;
    }

    @Override
    public List<QuestionOption> getOptions(Integer questionId) {
        return optionMapper.selectByQuestionId(questionId);
    }

    @Override
    public int addOption(Integer questionId, QuestionOption option) {
        option.setQuestionId(questionId);
        return optionMapper.insert(option);
    }

    @Override
    public int batchAddOptions(Integer questionId, List<QuestionOption> options) {
        for (QuestionOption option : options) {
            option.setQuestionId(questionId);
        }
        return optionMapper.batchInsert(options);
    }

    @Override
    public int updateOption(Integer optionId, QuestionOption option) {
        option.setOptionId(optionId);
        return optionMapper.update(option);
    }

    @Override
    public int deleteOption(Integer optionId) {
        return optionMapper.deleteById(optionId);
    }

    @Override
    public List<Question> getByPaperId(Integer paperId) {
        List<Question> questions = questionMapper.selectByPaperId(paperId);
        // 加载所有题目的选项
        for (Question question : questions) {
            question.setOptions(optionMapper.selectByQuestionId(question.getQuestionId()));
        }
        return questions;
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
        List<Question> questions = questionMapper.selectMostMistakes(limit);
        // 加载所有题目的选项
        for (Question question : questions) {
            question.setOptions(optionMapper.selectByQuestionId(question.getQuestionId()));
        }
        return questions;
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
        
        // 逐个插入题目，以便获取自增主键
        int successCount = 0;
        for (Question question : questions) {
            int result = questionMapper.insert(question);
            if (result > 0) {
                successCount++;
                // 插入选项
                if (question.getOptions() != null && !question.getOptions().isEmpty()) {
                    for (QuestionOption option : question.getOptions()) {
                        option.setQuestionId(question.getQuestionId());
                    }
                    optionMapper.batchInsert(question.getOptions());
                }
            }
        }
        
        return successCount;
    }

    @Override
    public List<Question> selectPage(Integer pageNum, Integer pageSize) {
        int offset = (pageNum - 1) * pageSize;
        List<Question> questions = questionMapper.selectPage(offset, pageSize);
        // 加载所有题目的选项
        for (Question question : questions) {
            question.setOptions(optionMapper.selectByQuestionId(question.getQuestionId()));
        }
        return questions;
    }

    @Override
    public Long selectCount() {
        return questionMapper.selectCount();
    }

    @Override
    public List<Question> selectByCondition(Map<String, Object> condition) {
        List<Question> questions = questionMapper.selectByCondition(condition);
        // 加载所有题目的选项
        for (Question question : questions) {
            question.setOptions(optionMapper.selectByQuestionId(question.getQuestionId()));
        }
        return questions;
    }

    @Override
    public Long selectCountByCondition(Map<String, Object> condition) {
        return questionMapper.selectCountByCondition(condition);
    }

    @Override
    public List<Question> selectPageByCondition(Map<String, Object> condition, Integer pageNum, Integer pageSize) {
        int offset = (pageNum - 1) * pageSize;
        List<Question> questions = questionMapper.selectPageByCondition(condition, offset, pageSize);
        // 加载所有题目的选项
        for (Question question : questions) {
            question.setOptions(optionMapper.selectByQuestionId(question.getQuestionId()));
        }
        return questions;
    }
} 
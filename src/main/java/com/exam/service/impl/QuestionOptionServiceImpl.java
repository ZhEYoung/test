package com.exam.service.impl;

import com.exam.entity.QuestionOption;
import com.exam.mapper.QuestionOptionMapper;
import com.exam.service.QuestionOptionService;
import com.exam.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

/**
 * 题目选项服务实现类
 */
@Service
@Transactional
public class QuestionOptionServiceImpl implements QuestionOptionService {

    @Autowired
    private QuestionOptionMapper optionMapper;

    @Autowired
    private QuestionService questionService;

    @Override
    public int insert(QuestionOption option) {
        if (!validateOption(option)) {
            return 0;
        }
        return optionMapper.insert(option);
    }

    @Override
    public int deleteById(Integer optionId) {
        if (!checkExists(optionId)) {
            return 0;
        }
        return optionMapper.deleteById(optionId);
    }

    @Override
    public int update(QuestionOption option) {
        if (!validateOption(option) || !checkExists(option.getOptionId())) {
            return 0;
        }
        return optionMapper.update(option);
    }

    @Override
    public QuestionOption getById(Integer optionId) {
        return optionMapper.selectById(optionId);
    }

    @Override
    public List<QuestionOption> getAll() {
        return optionMapper.selectAll();
    }

    @Override
    public List<QuestionOption> getByQuestionId(Integer questionId) {
        return optionMapper.selectByQuestionId(questionId);
    }

    @Override
    public int batchInsert(List<QuestionOption> options) {
        if (!validateOptions(options)) {
            return 0;
        }
        return optionMapper.batchInsert(options);
    }

    @Override
    public int deleteByQuestionId(Integer questionId) {
        return optionMapper.deleteByQuestionId(questionId);
    }

    @Override
    public int batchUpdate(List<QuestionOption> options) {
        if (!validateOptions(options)) {
            return 0;
        }
        return optionMapper.batchUpdate(options);
    }

    @Override
    public int countByQuestionId(Integer questionId) {
        return optionMapper.countByQuestionId(questionId);
    }

    @Override
    public int countCorrectOptions(Integer questionId) {
        return optionMapper.countCorrectOptions(questionId);
    }

    @Override
    public boolean checkExists(Integer optionId) {
        return optionMapper.checkExists(optionId);
    }

    @Override
    public int batchDelete(List<Integer> optionIds) {
        if (optionIds == null || optionIds.isEmpty()) {
            return 0;
        }
        return optionMapper.batchDelete(optionIds);
    }

    @Override
    public List<QuestionOption> getByContent(String content) {
        return optionMapper.selectByContent(content);
    }

    @Override
    public boolean validateOption(QuestionOption option) {
        if (option == null) {
            return false;
        }
        // 验证必填字段
        if (option.getQuestionId() == null || 
            option.getContent() == null || 
            option.getContent().trim().isEmpty()) {
            return false;
        }
        // 验证内容长度不超过255个字符
        if (option.getContent().length() > 255) {
            return false;
        }
        // 验证题目ID是否存在
        if (questionService.getById(option.getQuestionId()) == null) {
            return false;
        }
        // 如果设置了选项ID，确保它存在
        if (option.getOptionId() != null && !checkExists(option.getOptionId())) {
            return false;
        }
        // 如果是新选项，isCorrect必须有值
        if (option.getOptionId() == null && option.getIsCorrect() == null) {
            return false;
        }
        return true;
    }

    @Override
    public boolean validateOptions(List<QuestionOption> options) {
        if (options == null || options.isEmpty()) {
            return false;
        }
        // 验证每个选项
        for (QuestionOption option : options) {
            if (!validateOption(option)) {
                return false;
            }
        }
        // 确保所有选项属于同一个题目
        Integer questionId = options.get(0).getQuestionId();
        for (QuestionOption option : options) {
            if (!questionId.equals(option.getQuestionId())) {
                return false;
            }
        }
        return true;
    }
} 
package com.exam.service.impl;

import com.exam.entity.QuestionBank;
import com.exam.entity.Question;
import com.exam.mapper.QuestionBankMapper;
import com.exam.mapper.QuestionMapper;
import com.exam.mapper.QuestionOptionMapper;
import com.exam.service.QuestionBankService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
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
    @CacheEvict(value = {"qbank", "qbank_list", "qbank_by_subject","qbank_page_by_condition"}, allEntries = true)
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
        
        // 插入题库并获取生成的ID
        int result = questionBankMapper.insert(questionBank);
        if (result > 0) {
            // 确保qbId已经被设置
            if (questionBank.getQbId() == null || questionBank.getQbId() <= 0) {
                // 如果没有自动设置ID，则查询获取
                QuestionBank inserted = questionBankMapper.selectByName(questionBank.getQbName());
                if (inserted != null) {
                    questionBank.setQbId(inserted.getQbId());
                } else {
                    return 0;
                }
            }
        }
        return result;
    }

    @Override
    @CacheEvict(value = {"qbank", "qbank_list", "qbank_by_subject", "qbank_questions"}, allEntries = true)
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
    @CachePut(value = "qbank", key = "#questionBank.qbId", unless = "#result == 0")
    @CacheEvict(value = {"qbank_list", "qbank_by_subject","qbank_page_by_condition"}, allEntries = true)
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
    @Cacheable(value = "qbank", key = "#qbId", unless = "#result == null")
    public QuestionBank getById(Integer qbId) {
        return questionBankMapper.selectById(qbId);
    }

    @Override
    @Cacheable(value = "qbank_list", unless = "#result == null || #result.isEmpty()")
    public List<QuestionBank> getAll() {
        return questionBankMapper.selectAll();
    }
    
    @Override
    @Cacheable(value = "qbank_page", key = "#pageNum + '_' + #pageSize")
    public List<QuestionBank> getPage(Integer pageNum, Integer pageSize) {
        if (pageNum == null || pageSize == null || pageNum < 1 || pageSize < 1) {
            return new ArrayList<>();
        }
        Map<String, Object> params = new HashMap<>();
        params.put("offset", (pageNum - 1) * pageSize);
        params.put("limit", pageSize);
        return questionBankMapper.selectPage(params);
    }
    
    @Override
    @Cacheable(value = "qbank_by_condition", key = "'condition_'+#condition.hashCode()")
    public List<QuestionBank> getByCondition(Map<String, Object> condition) {
        if (condition == null) {
            return new ArrayList<>();
        }
        return questionBankMapper.selectByCondition(condition);
    }
    
    @Override
    @Cacheable(value = "qbank_page_by_condition", 
               key = "'condition_'+#condition.hashCode()+'_page_'+#pageNum+'_'+#pageSize")
    public List<QuestionBank> getPageByCondition(Map<String, Object> condition, Integer pageNum, Integer pageSize) {
        if (condition == null || pageNum == null || pageSize == null || pageNum < 1 || pageSize < 1) {
            return new ArrayList<>();
        }
        condition.put("offset", (pageNum - 1) * pageSize);
        condition.put("limit", pageSize);
        return questionBankMapper.selectPageByCondition(condition);
    }
    
    @Override
    @Cacheable(value = "qbank_count")
    public Long getCount() {
        return questionBankMapper.selectCount();
    }
    
    @Override
    @Cacheable(value = "qbank_count_by_condition", key = "'condition_'+#condition.hashCode()")
    public Long getCountByCondition(Map<String, Object> condition) {
        if (condition == null) {
            return 0L;
        }
        return questionBankMapper.selectCountByCondition(condition);
    }

    @Override
    @Cacheable(value = "qbank_by_subject", key = "#subjectId", unless = "#result == null || #result.isEmpty()")
    public List<QuestionBank> getBySubjectId(Integer subjectId) {
        return questionBankMapper.selectBySubjectId(subjectId);
    }

    @Override
    @Cacheable(value = "qbank_by_name", key = "#qbName", unless = "#result == null")
    public QuestionBank getByName(String qbName) {
        return questionBankMapper.selectByName(qbName);
    }

    @Override
    @Cacheable(value = "qbank_question_count", key = "#qbId")
    public Integer countQuestions(Integer qbId) {
        return questionBankMapper.countQuestions(qbId);
    }

    @Override
    @Cacheable(value = "qbank_count_by_subject", key = "#subjectId")
    public Integer countBySubjectId(Integer subjectId) {
        return questionBankMapper.countBySubjectId(subjectId);
    }

    @Override
    @CacheEvict(value = {"qbank_questions", "qbank_question_count"}, key = "#qbId")
    public int addQuestion(Integer qbId, Integer questionId) {
        if (qbId == null || questionId == null) {
            return 0;
        }
        return questionBankMapper.addQuestion(qbId, questionId);
    }

    @Override
    @CacheEvict(value = {"qbank_questions", "qbank_question_count"}, key = "#qbId")
    public int batchAddQuestions(Integer qbId, List<Integer> questionIds) {
        if (qbId == null || questionIds == null || questionIds.isEmpty()) {
            return 0;
        }
        return questionBankMapper.batchAddQuestions(qbId, questionIds);
    }

    @Override
    @CacheEvict(value = {"qbank_questions", "qbank_question_count"}, key = "#qbId")
    public int removeQuestion(Integer qbId, Integer questionId) {
        if (qbId == null || questionId == null) {
            return 0;
        }
        return questionBankMapper.removeQuestion(qbId, questionId);
    }

    @Override
    @CacheEvict(value = {"qbank_questions", "qbank_question_count"}, key = "#qbId")
    public int batchRemoveQuestions(Integer qbId, List<Integer> questionIds) {
        if (qbId == null || questionIds == null || questionIds.isEmpty()) {
            return 0;
        }
        return questionBankMapper.batchRemoveQuestions(qbId, questionIds);
    }

    @Override
    @Cacheable(value = "qbank_questions", key = "#qbId")
    public List<Question> getQuestions(Integer qbId) {
        return questionBankMapper.selectQuestions(qbId);
    }

    @Override
    @Cacheable(value = "qbank_questions_by_condition", 
               key = "'qb_'+#qbId+'_type_'+#type+'_diff_'+#minDifficulty+'_'+#maxDifficulty")
    public List<Question> getQuestionsByCondition(Integer qbId, Integer type, 
                                                BigDecimal minDifficulty, BigDecimal maxDifficulty) {
        return questionBankMapper.selectQuestionsByCondition(qbId, type, minDifficulty, maxDifficulty);
    }

    @Override
    @Cacheable(value = "qbank_questions_by_type", key = "#qbId")
    public List<Map<String, Object>> countQuestionsByType(Integer qbId) {
        return questionBankMapper.countQuestionsByType(qbId);
    }

    @Override
    @Cacheable(value = "qbank_questions_by_difficulty", key = "#qbId")
    public List<Map<String, Object>> countQuestionsByDifficulty(Integer qbId) {
        return questionBankMapper.countQuestionsByDifficulty(qbId);
    }

    @Override
    @Cacheable(value = "qbank_usage", key = "#qbId")
    public List<Map<String, Object>> countBankUsage(Integer qbId) {
        return questionBankMapper.countBankUsage(qbId);
    }

    @Override
    @CacheEvict(value = {"qbank", "qbank_list", "qbank_by_subject"}, allEntries = true)
    public int copyBank(Integer sourceQbId, String newBankName, Integer subjectId) {
        if (sourceQbId == null || newBankName == null || subjectId == null) {
            return 0;
        }
        return questionBankMapper.copyBank(sourceQbId, newBankName, subjectId);
    }

    @Override
    @CacheEvict(value = {"qbank", "qbank_list", "qbank_questions", "qbank_question_count"}, allEntries = true)
    public int mergeBanks(Integer targetQbId, List<Integer> sourceQbIds) {
        if (targetQbId == null || sourceQbIds == null || sourceQbIds.isEmpty()) {
            return 0;
        }
        return questionBankMapper.mergeBanks(targetQbId, sourceQbIds);
    }

    @Override
    @Cacheable(value = "qbank_recent", key = "#limit")
    public List<QuestionBank> getRecentUsed(Integer limit) {
        return questionBankMapper.selectRecentUsed(limit);
    }

    @Override
    @Cacheable(value = "qbank_hot", key = "#limit")
    public List<QuestionBank> getHotBanks(Integer limit) {
        return questionBankMapper.selectHotBanks(limit);
    }
} 
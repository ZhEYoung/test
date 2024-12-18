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
import java.math.RoundingMode;

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
    public ExamPaper getById(Integer id) {
        return examPaperMapper.selectById(id);
    }

    @Override
    public List<ExamPaper> getAll() {
        return examPaperMapper.selectAll();
    }

    @Override
    public List<ExamPaper> getPage(Integer pageNum, Integer pageSize) {
        Map<String, Object> condition = new HashMap<>();
        condition.put("offset", (pageNum - 1) * pageSize);
        condition.put("limit", pageSize);
        return examPaperMapper.selectPageByCondition(condition);
    }

    @Override
    public Long getCount() {
        return examPaperMapper.selectCount();
    }

    @Override
    public List<ExamPaper> getByCondition(Map<String, Object> condition) {
        return examPaperMapper.selectByCondition(condition);
    }

    @Override
    public Long getCountByCondition(Map<String, Object> condition) {
        return examPaperMapper.selectCountByCondition(condition);
    }

    @Override
    public List<ExamPaper> getPageByCondition(Map<String, Object> condition, Integer pageNum, Integer pageSize) {
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
        List<Map<String, Object>> questions = examPaperMapper.selectPaperQuestionsWithScore(paperId);
        for (Map<String, Object> question : questions) {
            question.put("question",questionMapper.selectById((Integer) question.get("questionId")));
        }
        return questions;
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
                                 Map<Integer, BigDecimal> typeScoreRatio, Integer teacherId,
                                 Date academicTerm, Integer examType) {
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
            
            // 如果总比例不等于1，调整最后一个题型的比例
            if (totalRatio.compareTo(BigDecimal.ONE) != 0) {
                Integer lastType = finalScoreRatio.keySet().stream().max(Integer::compareTo).get();
                finalScoreRatio.put(lastType, finalScoreRatio.get(lastType)
                    .add(BigDecimal.ONE.subtract(totalRatio)));
            }
        } else {
            // 自动计算分值比例
            // 计算总题目数量
            int totalQuestions = questionTypeCount.values().stream().mapToInt(Integer::intValue).sum();
            
            // 根据每种题型的题目数量和难度系数计算分值比例
            for (Map.Entry<Integer, Integer> entry : questionTypeCount.entrySet()) {
                Integer type = entry.getKey();
                Integer count = entry.getValue();
                
                if (count <= 0) {
                    continue;
                }
                
                // 基础分值比例 = 题目数量 / 总题目数量
                BigDecimal baseRatio = new BigDecimal(count)
                    .divide(new BigDecimal(totalQuestions), 4, BigDecimal.ROUND_HALF_UP);
                
                // 根据题型调整权重
                BigDecimal weight;
                switch (type) {
                    case 0: // 单选题
                        weight = new BigDecimal("1.0");
                        break;
                    case 1: // 多选题
                        weight = new BigDecimal("1.5");
                        break;
                    case 2: // 判断题
                        weight = new BigDecimal("0.8");
                        break;
                    case 3: // 填空题
                        weight = new BigDecimal("1.2");
                        break;
                    case 4: // 简答题
                        weight = new BigDecimal("2.0");
                        break;
                    default:
                        weight = BigDecimal.ONE;
                }
                
                BigDecimal ratio = baseRatio.multiply(weight);
                finalScoreRatio.put(type, ratio);
                totalRatio = totalRatio.add(ratio);
            }
            
            // 归一化处理，确保总分值比例为1
            for (Integer type : finalScoreRatio.keySet()) {
                BigDecimal normalizedRatio = finalScoreRatio.get(type)
                    .divide(totalRatio, 4, BigDecimal.ROUND_HALF_UP);
                finalScoreRatio.put(type, normalizedRatio);
            }
        }
        
        // 创建新试卷
        ExamPaper paper = new ExamPaper();
        paper.setPaperName(paperName);
        paper.setSubjectId(subjectId);
        paper.setTeacherId(teacherId);
        paper.setPaperStatus(0); // 未发布
        paper.setCreatedTime(new Date());
        paper.setPaperDifficulty(difficulty); // 设置目标难度系数
        paper.setAcademicTerm(academicTerm); // 设置学年学期
        paper.setExamType(examType); // 普通考试
        
        // 插入试卷记录
        if (examPaperMapper.insert(paper) <= 0) {
            throw new RuntimeException("创建试卷失败");
        }
        
        // 为每种题型选择题目
        BigDecimal totalDifficulty = BigDecimal.ZERO;
        int totalSelectedQuestions = 0;
        int currentOrder = 1; // 题目顺序从1开始
        
        // 用于存储简答题和当前总分
        List<Map<String, Object>> essayQuestions = new ArrayList<>();
        BigDecimal currentTotalScore = BigDecimal.ZERO;
        
        // 按题型顺序添加题目
        List<Integer> sortedTypes = new ArrayList<>(questionTypeCount.keySet());
        Collections.sort(sortedTypes); // 确保题型按顺序处理
        
        for (Integer type : sortedTypes) {
            Integer count = questionTypeCount.get(type);
            if (count <= 0) {
                continue;
            }
            
            // 查找符合条件的题目
            Map<String, Object> params = new HashMap<>();
            params.put("subjectId", subjectId);
            params.put("type", type);
            params.put("limit", count * 1); // 多查一些题目以便随机选择
            
            System.out.println("开始查询题型" + type + "的题目");
            System.out.println("查询参数：" + params);
            System.out.println("科目ID：" + subjectId);
            
            // 先查询该科目下的所有题库
            List<Map<String, Object>> questionBanks = questionMapper.selectQuestionBanksBySubject(subjectId);
            System.out.println("该科目下的题库：" + questionBanks);
            
            List<Question> questions = questionMapper.selectByTypeAndSubject(params);
            System.out.println("查询到题型" + type + "的题目数量：" + questions.size() + "，需要数量：" + count);
            if (!questions.isEmpty()) {
                System.out.println("第一道题目信息：" + questions.get(0));
            }
            
            if (questions.size() < count) {
                throw new RuntimeException("题型" + type + "的题目数量不足");
            }
            
            // 随机选择指定数量的题目
            Collections.shuffle(questions);
            List<Question> selectedQuestions = questions.subList(0, count);
            
            // 计算每道题的分值
            BigDecimal typeScore = finalScoreRatio.get(type).multiply(new BigDecimal("100"));
            
            // 特殊处理简答题（type=4），将其放到最后处理
            if (type == 4) {
                // 先记录简答题信息，稍后处理
                for (int i = 0; i < selectedQuestions.size(); i++) {
                    Question question = selectedQuestions.get(i);
                    Map<String, Object> paperQuestion = new HashMap<>();
                    paperQuestion.put("paperId", paper.getPaperId());
                    paperQuestion.put("questionId", question.getQuestionId());
                    paperQuestion.put("questionOrder", currentOrder++);
                    essayQuestions.add(paperQuestion);
                }
                continue;
            }
            
            // 处理其他题型
            BigDecimal baseScore = typeScore.divide(new BigDecimal(count), 4, RoundingMode.HALF_UP);
            BigDecimal questionScore = baseScore.setScale(2, RoundingMode.HALF_UP);
            
            // 为每道题设置分值并添加到试卷
            for (int i = 0; i < selectedQuestions.size(); i++) {
                Question question = selectedQuestions.get(i);
                
                // 添加试题到试卷
                Map<String, Object> paperQuestion = new HashMap<>();
                paperQuestion.put("paperId", paper.getPaperId());
                paperQuestion.put("questionId", question.getQuestionId());
                paperQuestion.put("questionScore", questionScore);
                paperQuestion.put("questionOrder", currentOrder++);
                
                if (examPaperMapper.insertPaperQuestion(paperQuestion) <= 0) {
                    throw new RuntimeException("添加试题失败");
                }
                
                currentTotalScore = currentTotalScore.add(questionScore);
            }
        }
        
        // 最后处理简答题分值
        if (!essayQuestions.isEmpty()) {
            BigDecimal remainingScore = new BigDecimal("100").subtract(currentTotalScore);
            BigDecimal essayBaseScore = remainingScore.divide(new BigDecimal(essayQuestions.size()), 2, RoundingMode.HALF_UP);
            
            // 为每道简答题设置分值
            for (int i = 0; i < essayQuestions.size(); i++) {
                Map<String, Object> paperQuestion = essayQuestions.get(i);
                if (i == essayQuestions.size() - 1) {
                    // 最后一道简答题分值 = 剩余总分 - 前面简答题总分
                    BigDecimal lastScore = remainingScore.subtract(
                        essayBaseScore.multiply(new BigDecimal(essayQuestions.size() - 1))
                    );
                    paperQuestion.put("questionScore", lastScore);
                } else {
                    paperQuestion.put("questionScore", essayBaseScore);
                }
                
                if (examPaperMapper.insertPaperQuestion(paperQuestion) <= 0) {
                    throw new RuntimeException("添加试题失败");
                }
            }
        }
        
        // 计算实际难度系数
        if (totalSelectedQuestions > 0) {
            BigDecimal actualDifficulty = totalDifficulty.divide(new BigDecimal(totalSelectedQuestions), 2, BigDecimal.ROUND_HALF_UP);
            paper.setPaperDifficulty(actualDifficulty);
            examPaperMapper.update(paper);
        }
        
        return paper;
    }

    @Override
    @Transactional
    public ExamPaper generatePaperManually(Integer subjectId, String paperName,
                                         Map<Integer, BigDecimal> questionScores,
                                         BigDecimal difficulty, Integer examType,
                                         Date academicTerm, Integer teacherId) {
        // 创建新试卷
        ExamPaper paper = new ExamPaper();
        paper.setPaperName(paperName);
        paper.setSubjectId(subjectId);
        paper.setTeacherId(teacherId);
        paper.setPaperStatus(0); // 未发布
        paper.setCreatedTime(new Date());
        paper.setPaperDifficulty(difficulty);
        paper.setExamType(examType);
        paper.setAcademicTerm(academicTerm);
        
        // 插入试卷记录
        if (examPaperMapper.insert(paper) <= 0) {
            throw new RuntimeException("创建试卷失败");
        }
        
        // 验证总分是否为100分
        BigDecimal totalScore = questionScores.values().stream()
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (totalScore.compareTo(new BigDecimal("100")) != 0) {
            throw new RuntimeException("试题总分必须为100分，当前总分为: " + totalScore);
        }
        
        // 获取所有题目信息并验证
        List<Integer> questionIds = new ArrayList<>(questionScores.keySet());
        List<Question> questions = questionMapper.selectByIds(questionIds);
        
        if (questions.size() != questionIds.size()) {
            throw new RuntimeException("部分题目不存在");
        }

        
        // 计算实际难度系数（所有题目难度的平均值）
        BigDecimal totalDifficulty = questions.stream()
            .map(Question::getDifficulty)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal actualDifficulty = totalDifficulty.divide(new BigDecimal(questions.size()), 2, RoundingMode.HALF_UP);
        
        // 更新试卷实际难度系数
        paper.setPaperDifficulty(actualDifficulty);
        examPaperMapper.update(paper);
        
        // 添加试题到试卷
        int currentOrder = 1;
        for (Question question : questions) {
            Map<String, Object> paperQuestion = new HashMap<>();
            paperQuestion.put("paperId", paper.getPaperId());
            paperQuestion.put("questionId", question.getQuestionId());
            paperQuestion.put("questionScore", questionScores.get(question.getQuestionId()));
            paperQuestion.put("questionOrder", currentOrder++);
            
            if (examPaperMapper.insertPaperQuestion(paperQuestion) <= 0) {
                throw new RuntimeException("添加试题失败");
            }
        }
        
        return paper;
    }
} 
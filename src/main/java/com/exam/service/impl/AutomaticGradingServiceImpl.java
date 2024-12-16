package com.exam.service.impl;

import com.exam.entity.ExamPaperQuestion;
import com.exam.entity.Question;
import com.exam.entity.QuestionOption;
import com.exam.entity.StudentQuestionScore;
import com.exam.service.AutomaticGradingService;
import com.exam.service.ExamPaperQuestionService;
import com.exam.service.QuestionService;
import com.exam.service.StudentQuestionScoreService;
import com.exam.service.QuestionOptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 自动评分服务实现类
 */
@Service
public class AutomaticGradingServiceImpl implements AutomaticGradingService {

    @Autowired
    private QuestionService questionService;

    @Autowired
    private StudentQuestionScoreService studentQuestionScoreService;

    @Autowired
    private ExamPaperQuestionService examPaperQuestionService;

    @Autowired
    private QuestionOptionService questionOptionService;

    /**
     * 自动评分
     * @param questionScore 学生题目成绩记录
     */
    @Override
    @Transactional
    public void gradeQuestion(StudentQuestionScore questionScore) {
        // 获取题目信息
        Question question = questionService.getById(questionScore.getQuestionId());
        if (question == null) {
            throw new IllegalArgumentException("题目不存在");
        }

        // 获取题目在试卷中的分数
        ExamPaperQuestion examPaperQuestion = examPaperQuestionService.getByExamAndQuestionId(
            questionScore.getExamId(), questionScore.getQuestionId());
        if (examPaperQuestion == null) {
            throw new IllegalArgumentException("题目不在试卷中");
        }

        // 根据题目类型进行评分
        switch (question.getType()) {
            case 0: // 单选题
                gradeSingleChoiceQuestion(questionScore, examPaperQuestion.getQuestionScore());
                break;
            case 1: // 多选题
                gradeMultipleChoiceQuestion(questionScore, examPaperQuestion.getQuestionScore());
                break;
            case 2: // 判断题
                gradeTrueFalseQuestion(questionScore,  examPaperQuestion.getQuestionScore());
                break;
            default:
                // 其他类型题目（填空题、简答题等）需要人工评分
                questionScore.setScore(BigDecimal.ZERO);
                questionScore.setStatus(0); // 未批改状态
        }

        // 更新题目成绩记录
        studentQuestionScoreService.updateById(questionScore);
    }

    /**
     * 评分单选题
     */
    private void gradeSingleChoiceQuestion(StudentQuestionScore questionScore, BigDecimal fullScore) {
        if (questionScore.getAnswer() == null) {
            questionScore.setScore(BigDecimal.ZERO);
        } else {
            try {
                // 获取学生选择的选项
                QuestionOption selectedOption = questionOptionService.getById(Integer.parseInt(questionScore.getAnswer()));
                if (selectedOption != null && selectedOption.getIsCorrect()) {
                    questionScore.setScore(fullScore); // 答案正确，得满分
                } else {
                    questionScore.setScore(BigDecimal.ZERO); // 答案错误，得0分
                }
            } catch (NumberFormatException e) {
                questionScore.setScore(BigDecimal.ZERO); // 答案格式错误，得0分
            }
        }
        questionScore.setStatus(1); // 已批改状态
    }

    /**
     * 评分多选题
     */
    private void gradeMultipleChoiceQuestion(StudentQuestionScore questionScore, BigDecimal fullScore) {
        if (questionScore.getAnswer() == null) {
            questionScore.setScore(BigDecimal.ZERO);
        } else {
            try {
                // 将学生答案字符串转换为选项ID集合
                Set<Integer> studentAnswerIds = Arrays.stream(questionScore.getAnswer().split(","))
                    .map(Integer::parseInt)
                    .collect(Collectors.toSet());

                // 获取所有选项
                List<QuestionOption> allOptions = questionOptionService.getByQuestionId(questionScore.getQuestionId());
                
                // 获取正确选项的ID集合
                Set<Integer> correctOptionIds = allOptions.stream()
                    .filter(QuestionOption::getIsCorrect)
                    .map(QuestionOption::getOptionId)
                    .collect(Collectors.toSet());

                // 计算得分
                if (studentAnswerIds.equals(correctOptionIds)) {
                    questionScore.setScore(fullScore); // 答案完全正确，得满分
                } else if (isPartiallyCorrect(studentAnswerIds, correctOptionIds)) {
                    questionScore.setScore(fullScore.divide(new BigDecimal("2"))); // 部分正确，得一半分数
                } else {
                    questionScore.setScore(BigDecimal.ZERO); // 答案完全错误，得0分
                }
            } catch (NumberFormatException e) {
                questionScore.setScore(BigDecimal.ZERO); // 答案格式错误，得0分
            }
        }
        questionScore.setStatus(1); // 已批改状态
    }

    /**
     * 判断多选题答案是否部分正确
     * 部分正确的条件：
     * 1. 学生选择的所有选项中至少有一个是正确的
     * 2. 学生没有选择错误的选项
     * @param studentAnswerIds 学生选择的选项ID集合
     * @param correctOptionIds 正确选项ID集合
     * @return 是否部分正确
     */
    private boolean isPartiallyCorrect(Set<Integer> studentAnswerIds, Set<Integer> correctOptionIds) {
        // 检查学生是否选择了至少一个正确选项
        boolean hasCorrectAnswer = studentAnswerIds.stream()
            .anyMatch(correctOptionIds::contains);

        // 检查学生是否选择了错误选项
        boolean hasWrongAnswer = studentAnswerIds.stream()
            .anyMatch(id -> !correctOptionIds.contains(id));

        // 部分正确：有正确选项且没有选错误选项
        return hasCorrectAnswer && !hasWrongAnswer;
    }

    /**
     * 评分判断题
     */
    private void gradeTrueFalseQuestion(StudentQuestionScore questionScore, BigDecimal fullScore) {
        if (questionScore.getAnswer() == null) {
            questionScore.setScore(BigDecimal.ZERO);
        } else {
            try {
                // 获取学生选择的选项
                QuestionOption selectedOption = questionOptionService.getById(Integer.parseInt(questionScore.getAnswer()));
                if (selectedOption != null && selectedOption.getIsCorrect()) {
                    questionScore.setScore(fullScore); // 答案正确，得满分
                } else {
                    questionScore.setScore(BigDecimal.ZERO); // 答案错误，得0分
                }
            } catch (NumberFormatException e) {
                questionScore.setScore(BigDecimal.ZERO); // 答案格式错误，得0分
            }
        }
        questionScore.setStatus(1); // 已批改状态
    }
} 
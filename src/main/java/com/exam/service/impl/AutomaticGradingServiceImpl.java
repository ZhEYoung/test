package com.exam.service.impl;

import com.exam.entity.ExamPaperQuestion;
import com.exam.entity.Question;
import com.exam.entity.StudentQuestionScore;
import com.exam.service.AutomaticGradingService;
import com.exam.service.ExamPaperQuestionService;
import com.exam.service.QuestionService;
import com.exam.service.StudentQuestionScoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

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
                gradeSingleChoiceQuestion(questionScore, question, examPaperQuestion.getQuestionScore());
                break;
            case 1: // 多选题
                gradeMultipleChoiceQuestion(questionScore, question, examPaperQuestion.getQuestionScore());
                break;
            case 2: // 判断题
                gradeTrueFalseQuestion(questionScore, question, examPaperQuestion.getQuestionScore());
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
    private void gradeSingleChoiceQuestion(StudentQuestionScore questionScore, Question question, BigDecimal fullScore) {
        if (questionScore.getAnswer() == null || question.getAnswer() == null) {
            questionScore.setScore(BigDecimal.ZERO);
        } else if (questionScore.getAnswer().equals(question.getAnswer())) {
            questionScore.setScore(fullScore); // 答案完全正确，得满分
        } else {
            questionScore.setScore(BigDecimal.ZERO); // 答案错误，得0分
        }
        questionScore.setStatus(1); // 已批改状态
    }

    /**
     * 评分多选题
     */
    private void gradeMultipleChoiceQuestion(StudentQuestionScore questionScore, Question question, BigDecimal fullScore) {
        if (questionScore.getAnswer() == null || question.getAnswer() == null) {
            questionScore.setScore(BigDecimal.ZERO);
        } else {
            // 将答案转换为选项集合
            Set<String> studentAnswers = new HashSet<>(Arrays.asList(questionScore.getAnswer().split(",")));
            Set<String> correctAnswers = new HashSet<>(Arrays.asList(question.getAnswer().split(",")));

            if (studentAnswers.equals(correctAnswers)) {
                questionScore.setScore(fullScore); // 答案完全正确，得满分
            } else {
                questionScore.setScore(BigDecimal.ZERO); // 答案部分正确或错误，得0分
            }
        }
        questionScore.setStatus(1); // 已批改状态
    }

    /**
     * 评分判断题
     */
    private void gradeTrueFalseQuestion(StudentQuestionScore questionScore, Question question, BigDecimal fullScore) {
        if (questionScore.getAnswer() == null || question.getAnswer() == null) {
            questionScore.setScore(BigDecimal.ZERO);
        } else if (questionScore.getAnswer().equals(question.getAnswer())) {
            questionScore.setScore(fullScore); // 答案正确，得满分
        } else {
            questionScore.setScore(BigDecimal.ZERO); // 答案错误，得0分
        }
        questionScore.setStatus(1); // 已批改状态
    }
} 
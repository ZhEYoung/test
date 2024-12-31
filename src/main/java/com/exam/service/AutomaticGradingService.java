package com.exam.service;

import com.exam.entity.StudentQuestionScore;

/**
 * 自动评分服务接口
 */
public interface AutomaticGradingService {
    
    /**
     * 自动评分
     * @param questionScore 学生题目成绩记录
     */
    void gradeQuestion(StudentQuestionScore questionScore);
} 
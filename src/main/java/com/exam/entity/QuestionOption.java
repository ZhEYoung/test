package com.exam.entity;

import lombok.Data;

/**
 * 题目选项实体类
 */
@Data
public class QuestionOption {
    private Integer optionId;     // 选项ID
    private Integer questionId;   // 题目ID
    private String content;       // 选项内容
    private Boolean isCorrect;    // 是否正确
    
    // 关联信息
    private Question question;   // 题目信息
} 
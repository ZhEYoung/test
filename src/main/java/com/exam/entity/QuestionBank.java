package com.exam.entity;

import lombok.Data;
import java.util.List;

/**
 * 题库实体类
 */
@Data
public class QuestionBank {
    private Integer qbId;           // 题库ID
    private String qbName;          // 题库名称
    private Integer subjectId;      // 学科编号
    
    // 关联信息
    private Subject subject;        // 学科信息
    private List<Question> questions; // 题目列表
} 
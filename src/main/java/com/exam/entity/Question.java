package com.exam.entity;

import lombok.Data;
import java.util.List;
import java.math.BigDecimal;

/**
 * 题目实体类
 */
@Data
public class Question {
    private Integer questionId;  // 题目ID
    private Integer qbId;        // 题库ID
    private String content;      // 题目内容
    private String answer;       // 答案
    private Integer type;        // 题目类型 0: 单选；1: 多选；2: 判断；3: 填空；4: 简答
    private BigDecimal difficulty;  // 难度
    
    // 关联信息
    private QuestionBank questionBank;  // 题库信息
    private List<QuestionOption> options;  // 选项列表（仅用于展示，不参与数据库映射）
    
    // 扩展字段，不映射到数据库
    private transient String typeName;  // 题目类型名称
    private transient BigDecimal score;    // 题目分值（用于试卷中）
} 
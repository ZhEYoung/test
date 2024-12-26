package com.exam.entity;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 试卷-题目关联实体类
 */
@Data
public class ExamPaperQuestion {
    private Integer epqId;          // 关联ID
    private Integer paperId;        // 试卷ID
    private Integer questionId;     // 题目ID
    private Integer questionOrder;  // 题目顺序
    private BigDecimal questionScore;  // 题目分值
    
    // 扩展字段，不映射到数据库
    private transient String paperName;     // 试卷名称（用于展示）
    private transient String questionContent; // 题目内容（用于展示）
    private transient String questionType;    // 题目类型（用于展示）
} 
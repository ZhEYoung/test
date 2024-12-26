package com.exam.entity;

import java.util.Date;

/**
 * 考试-班级关联实体类
 */
public class ExamClass {
    private Integer ecId;
    private Integer examId;
    private Integer classId;
    private Date createTime;

    private Exam exam; // 考试信息
    private Class clazz; // 班级信息



    public Integer getEcId() {
        return ecId;
    }

    public void setEcId(Integer ecId) {
        this.ecId = ecId;
    }

    public Integer getExamId() {
        return examId;
    }

    public void setExamId(Integer examId) {
        this.examId = examId;
    }

    public Integer getClassId() {
        return classId;
    }

    public void setClassId(Integer classId) {
        this.classId = classId;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
} 
package com.exam.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 考试实体类
 */
public class Exam {
    @Setter
    @Getter
    private Integer examId;
    @Setter
    @Getter
    private String examName;
    @Setter
    @Getter
    private Integer subjectId;
    @Setter
    @Getter
    private Integer paperId;
    @Setter
    @Getter
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date examStartTime;
    @Setter
    @Getter
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date examEndTime;
    @Setter
    @Getter
    private Integer examDuration;
    @Setter
    @Getter
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createdTime;
    @Setter
    @Getter
    private Integer teacherId;
    @Setter
    @Getter
    private Integer examStatus;
    @Setter
    @Getter
    private Integer examType;

    private Subject subject; // 科目信息
    private ExamPaper paper; // 试卷信息
    private Teacher teacher; // 教师信息

}
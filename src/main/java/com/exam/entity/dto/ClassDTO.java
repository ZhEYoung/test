package com.exam.entity.dto;

import lombok.Data;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@Data
@ApiModel(description = "班级DTO")
public class ClassDTO {
    @ApiModelProperty("班级ID")
    private Integer classId;

    @ApiModelProperty("教师ID")
    private Integer teacherId;

    @ApiModelProperty("课程名")
    private String className;

    @ApiModelProperty("学科ID")
    private Integer subjectId;

    @ApiModelProperty("期末考试")
    private Boolean finalExam;
} 
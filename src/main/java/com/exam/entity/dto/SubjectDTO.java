package com.exam.entity.dto;

import lombok.Data;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@Data
@ApiModel(description = "学科DTO")
public class SubjectDTO {
    @ApiModelProperty("学科ID")
    private Integer subjectId;

    @ApiModelProperty("学科名称")
    private String subjectName;

    @ApiModelProperty("学科描述")
    private String description;

    @ApiModelProperty("学院ID")
    private Integer collegeId;
} 
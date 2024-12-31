package com.exam.entity.dto;

import lombok.Data;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@Data
@ApiModel(description = "学院DTO")
public class CollegeDTO {
    @ApiModelProperty("学院ID")
    private Integer collegeId;

    @ApiModelProperty("学院名称")
    private String collegeName;

    @ApiModelProperty("学院描述")
    private String description;
} 
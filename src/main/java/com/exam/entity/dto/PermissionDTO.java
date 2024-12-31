package com.exam.entity.dto;

import lombok.Data;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * 权限更新数据传输对象
 */
@Data
@ApiModel(description = "权限更新请求")
public class PermissionDTO {
    @ApiModelProperty(value = "权限等级 0:可以组卷与发布所有考试; 1:可以组卷与发布普通考试; 2:可以组卷", required = true)
    private Integer permission;
    
    @ApiModelProperty(value = "更新原因", required = false)
    private String reason;
} 
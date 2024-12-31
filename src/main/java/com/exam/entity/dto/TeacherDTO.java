package com.exam.entity.dto;

import lombok.Data;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@Data
@ApiModel(description = "教师DTO")
public class TeacherDTO {
    @ApiModelProperty("教师ID")
    private Integer teacherId;

    @ApiModelProperty("用户名")
    private String username;

    @ApiModelProperty("密码")
    private String password;

    @ApiModelProperty("姓名")
    private String name;

    @ApiModelProperty("性别")
    private Boolean sex;

    @ApiModelProperty("联系方式")
    private String phone;

    @ApiModelProperty("邮箱")
    private String email;

    @ApiModelProperty("账号状态")
    private Boolean status;

    @ApiModelProperty("权限")
    private Integer permission;

    @ApiModelProperty("学院名称")
    private String collegeName;

    @ApiModelProperty("学院ID")
    private Integer collegeId;

    @ApiModelProperty("备注")
    private String other;
} 
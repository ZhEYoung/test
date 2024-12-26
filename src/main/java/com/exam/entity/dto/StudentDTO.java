package com.exam.entity.dto;

import lombok.Data;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@Data
@ApiModel(description = "学生DTO")
public class StudentDTO {
    @ApiModelProperty("学生ID")
    private Integer studentId;

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

    @ApiModelProperty("年级")
    private String grade;

    @ApiModelProperty("学院ID")
    private Integer collegeId;

    @ApiModelProperty("备注")
    private String other;
} 
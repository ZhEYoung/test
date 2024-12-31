package com.exam.entity.dto;

import lombok.Data;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.*;

@Data
@ApiModel(description = "管理员/教师创建DTO")
public class StaffRegisterDTO {
    @NotBlank(message = "用户名不能为空")
    @Size(min = 4, max = 10, message = "用户名长度必须在4-10之间")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "用户名只能包含字母、数字和下划线")
    @ApiModelProperty(value = "用户名", required = true, example = "teacher1")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度必须在6-20之间")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d]{6,20}$",
            message = "密码必须包含大小写字母和数字")
    @ApiModelProperty(value = "密码", required = true, example = "Password123")
    private String password;

    @NotNull(message = "角色不能为空")
    @Min(value = 0, message = "角色值必须为0或1")
    @Max(value = 1, message = "角色值必须为0或1")
    @ApiModelProperty(value = "角色(0:管理员/1:教师)", required = true, example = "1")
    private Integer role;

    @NotBlank(message = "姓名不能为空")
    @Size(min = 2, max = 20, message = "姓名长度必须在2-20之间")
    @Pattern(regexp = "^[\\u4e00-\\u9fa5]{2,20}$", message = "姓名必须为中文")
    @ApiModelProperty(value = "姓名", required = true, example = "张三")
    private String name;

    @NotNull(message = "性别不能为空")
    @ApiModelProperty(value = "性别(false:女/true:男)", required = true, example = "true")
    private Boolean sex;

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "请输入正确的手机号")
    @ApiModelProperty(value = "联系方式", required = true, example = "13800138000")
    private String phone;

    @NotBlank(message = "邮箱不能为空")
    @Email(message = "请输入正确的邮箱格式")
    @ApiModelProperty(value = "邮箱", required = true, example = "example@email.com")
    private String email;

    @ApiModelProperty(value = "学院ID(教师必填)", example = "1")
    private Integer collegeId;

    @ApiModelProperty(value = "权限等级(教师必填,0:可以组卷与发布所有考试/1:可以组卷与发布普通考试/2:可以组卷)", example = "1")
    private Integer permission;

    @Size(max = 100, message = "备注长度不能超过100")
    @ApiModelProperty(value = "备注", example = "备注信息")
    private String other;
}

package com.exam.common;

import lombok.Data;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * 通用响应类
 */
@Data
@ApiModel(description = "统一返回结果")
public class Result<T> {
    @ApiModelProperty("状态码")
    private Integer code; // 状态码

    @ApiModelProperty("返回消息")
    private String message; // 提示信息

    @ApiModelProperty("返回数据")
    private T data; // 数据

    private Result() {}

    public static <T> Result<T> success() {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage("操作成功");
        return result;
    }

    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage("操作成功");
        result.setData(data);
        return result;
    }

    public static <T> Result<T> success(String message, T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage(message);
        result.setData(data);
        return result;
    }

    public static <T> Result<T> error() {
        Result<T> result = new Result<>();
        result.setCode(500);
        result.setMessage("操作失败");
        return result;
    }

    public static <T> Result<T> error(String message) {
        Result<T> result = new Result<>();
        result.setCode(500);
        result.setMessage(message);
        return result;
    }

    public static <T> Result<T> error(Integer code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }
} 
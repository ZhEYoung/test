package com.exam.entity;

import lombok.Data;
import java.util.Date;

/**
 * 日志实体类
 */
@Data
public class Log {
    private Integer logId;           // 日志ID
    private Integer userId;          // 用户ID
    private Integer actionType;      // 操作类型 0: INSERT、1: UPDATE、2: DELETE、3: LOGIN、4: SUBMIT_TEST
    private String actionDescription;// 操作描述
    private Date createdTime;        // 操作时间
    private String objectType;       // 操作对象
    private String ipAddress;        // IP地址
    private String deviceInfo;       // 设备信息
    private String status;           // 操作状态
    
    // 关联信息
    private User user;               // 用户信息
} 
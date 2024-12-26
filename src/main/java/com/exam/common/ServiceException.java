package com.exam.common;

/**
 * 业务异常
 */
public class ServiceException extends RuntimeException {
    public ServiceException(String message) {
        super(message);
    }
} 
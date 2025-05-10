package com.exam;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
@MapperScan("com.exam.mapper")
public class ExamSystemApplication {
    public static void main(String[] args) {
        SpringApplication.run(ExamSystemApplication.class, args);
    }
} 
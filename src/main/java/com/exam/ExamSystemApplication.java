package com.exam;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableTransactionManagement
@MapperScan("com.exam.mapper")
@EnableScheduling
public class ExamSystemApplication {
    public static void main(String[] args) {
        SpringApplication.run(ExamSystemApplication.class, args);
    }
} 
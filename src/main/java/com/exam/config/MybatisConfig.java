package com.exam.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis配置
 */
@Configuration
@MapperScan("com.exam.mapper")
public class MybatisConfig {
} 
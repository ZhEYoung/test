package com.exam.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.oas.annotations.EnableOpenApi;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

@Configuration
@EnableOpenApi
public class SwaggerConfig extends WebMvcConfigurationSupport {
    @Bean
    public Docket createRestApi() {
        return new Docket(DocumentationType.OAS_30)
                .enable(true)
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.exam.controller"))
                .paths(PathSelectors.any())
                .build();
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("考试管理系统接口文档")
                .description("基于Spring Boot + Vue3的在线考试系统")
                .contact(new Contact("ExamSystem", "http://localhost:8080", "admin@exam.com"))
                .version("1.0")
                .build();
    }
} 
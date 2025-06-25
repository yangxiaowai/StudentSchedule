package com.example.learning.learning_habit_plan_backend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("学习养成计划 API 文档")
                        .version("1.0")
                        .description("本系统用于管理学生学习任务，包括注册登录、任务管理、AI分析等功能"));
    }
}

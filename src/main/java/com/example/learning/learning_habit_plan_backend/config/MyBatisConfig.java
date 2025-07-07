package com.example.learning.learning_habit_plan_backend.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("com.example.learning.learning_habit_plan_backend.mapper")
public class MyBatisConfig {
    // MyBatis配置类
}
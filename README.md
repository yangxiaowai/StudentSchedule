# 学习养成计划

## 项目背景

学习养成计划主要是针对学生考试任务的管理系统，通过可视化的图表展示数据，使用者能够有完成任务的成就感。

## 项目需求描述

学习养成计划用户可以添加学习任务，每个学习任务都有自己的主题、完成时间和优先级，根据设定的完成时间和优先级对用户进行不同频率的提醒，直到其完成该任务，任务可以设定提醒频率和时间期限，避免导致任务越堆积越多，除了用户自己可以设定任务以外，本软件后台也会针对不同科目来进行推送任务，软件还提供任务分析功能，针对用户每天的任务添加数和完成率进行统计，让使用者更好的知道自己的复习进度和完成率。

## 项目基础功能

1. 注册、登录功能；
2. 添加和管理学习任务；
3. 任务提醒功能；
4. 任务分析功能，有一些统计分析图表；
5. 5.学习进度跟踪以及内容记录；
6. 6.学习资料检索、收集以及整理；

## 项目拓展功能

1. 利用 AI 大模型生成任务完成情况报告；
2. 基于用户学习成果进行 AI 评估并能基于此提供改进建议；

## 数据库搭建

## 项目概述

本项目是一个基于Spring Boot + Vue.js的学习养成计划管理系统，使用MySQL作为主数据库，Redis作为缓存数据库。

## 数据库环境要求

- MySQL : 8.0+
- Redis : 6.0+
- 字符集 : UTF-8
- 时区 : Asia/Shanghai

## 数据库配置信息

根据项目配置文件，数据库连接信息如下：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/learning_plan?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
    username: root
    password: 使用自己数据库的密码
    driver-class-name: com.mysql.cj.jdbc.Driver
  redis:
    host: localhost
    port: 6379
    timeout: 2000ms
    database: 0
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQL8Dialect
        format_sql: true

mybatis-plus:
  mapper-locations: classpath:mapper/*.xml
  type-aliases-package: com.example.learning.learning_habit_plan_backend.entity
  configuration:
    map-underscore-to-camel-case: true
```

## 数据库创建步骤

### 1. 创建数据库

```sql
CREATE DATABASE learning_plan 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

USE learning_plan;
```

### 2. 创建用户表 (user)

根据项目中的User实体类，需要创建以下用户表：

```sql
CREATE TABLE user (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(50) NOT NULL UNIQUE,
  password VARCHAR(255) NOT NULL,
  email VARCHAR(255) NOT NULL UNIQUE,
  is_active BOOLEAN DEFAULT TRUE,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

## 表结构说明

### user 表字段详解

| 字段名 | 数据类型 | 约束 | 说明 |
| :---: | :---: | :---: | :---: |
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | 用户唯一标识 |
| username | VARCHAR(50) | NOT NULL, UNIQUE | 用户名，长度3-20字符 |
| password | VARCHAR(255) | NOT NULL | 加密后的密码 |
| email | VARCHAR(255) | NOT NULL, UNIQUE | 用户邮箱 |
| is_active | BOOLEAN | DEFAULT TRUE | 账户激活状态 |
| created_at | DATETIME | DEFAULT CURRENT_TIMESTAMP | 账户创建时间 |
| updated_at | DATETIME | DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 最后更新时间 |

## 索引设计

- 主键索引 : id 字段
- 唯一索引 : username , email 字段
- 普通索引 : 为常用查询字段 username , email 创建索引以提高查询性能

## 数据库用户权限设置

## Redis 配置

确保Redis服务运行在默认端口6379，项目将使用database 0。

```bash
# 启动Redis服务
redis-server

# 测试连接
redis-cli ping
```

## 验证数据库设置

创建完成后，可以通过以下SQL验证表结构：

```sql
-- 查看表结构
DESC user;

-- 查看表创建语句
SHOW CREATE TABLE user;

-- 插入测试数据
INSERT INTO user (username, password, email) 
VALUES ('testuser', 'encrypted_password', 
'test@example.com');

-- 查询测试
SELECT * FROM user WHERE username = 'testuser';
```

## 注意事项

1. 密码安全 : 项目使用BCrypt加密存储密码，请确保在生产环境中使用强密码策略
2. 字符集 : 使用utf8mb4字符集以支持完整的Unicode字符
3. 时区设置 : 确保MySQL时区设置为Asia/Shanghai
4. 备份策略 : 建议定期备份数据库
5. 性能优化 : 根据实际使用情况调整索引和配置

## 扩展说明

当前项目主要包含用户管理功能，后续可能会添加以下表：

- 学习计划表 (learning_plan)
- 学习任务表 (learning_task)
- 学习记录表 (learning_record)
- AI分析结果表 (ai_analysis)
建议在添加新功能时，按照相同的命名规范和设计原则创建相应的数据表。
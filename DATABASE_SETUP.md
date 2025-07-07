# 数据库设置说明

## 概述
本文档说明如何为学习养成计划系统设置数据库表结构。

## 数据库信息
- **数据库名称**: `learning_plan`
- **字符集**: `utf8mb4`
- **排序规则**: `utf8mb4_unicode_ci`
- **数据库引擎**: InnoDB

## 表结构说明

### 核心表
1. **user** - 用户表
2. **task** - 任务表
3. **user_stats** - 用户统计表
4. **study_record** - 学习记录表

### 社交功能表
5. **study_group** - 学习小组表
6. **group_member** - 小组成员表
7. **study_share** - 学习分享表
8. **qa_question** - 问答问题表
9. **qa_answer** - 问答回答表
10. **comment** - 评论表
11. **like_record** - 点赞记录表

### 成就系统表
12. **achievement** - 成就表
13. **user_achievement** - 用户成就表

## 安装步骤

### 方法一：使用MySQL命令行

1. **登录MySQL**
   ```bash
   mysql -u root -p
   ```

2. **执行初始化脚本**
   ```sql
   source /path/to/init_database.sql
   ```

### 方法二：分步执行

1. **创建数据库**
   ```sql
   CREATE DATABASE IF NOT EXISTS `learning_plan` 
   DEFAULT CHARACTER SET utf8mb4 
   DEFAULT COLLATE utf8mb4_unicode_ci;
   
   USE `learning_plan`;
   ```

2. **执行表结构脚本**
   ```sql
   source /path/to/database_tables.sql
   ```

### 方法三：使用MySQL Workbench

1. 打开MySQL Workbench
2. 连接到MySQL服务器
3. 打开 `database_tables.sql` 文件
4. 执行脚本

```sql
mysql -u root -p < /Users/yangjun/Downloads/StudentSchedule-main/init_database.sql
```

## 验证安装

执行以下SQL验证表是否创建成功：

```sql
USE learning_plan;
SHOW TABLES;

-- 检查初始数据
SELECT COUNT(*) as user_count FROM user;
SELECT COUNT(*) as group_count FROM study_group;
SELECT COUNT(*) as achievement_count FROM achievement;
```

## 初始数据

脚本会自动插入以下测试数据：

### 测试用户
- **admin** (admin@example.com)
- **student1** (student1@example.com) 
- **student2** (student2@example.com)

### 学习小组
- 数学学习小组
- 英语口语练习
- 编程入门

### 成就系统
- 初学者 (完成第一个任务)
- 勤奋学习者 (连续学习7天)
- 知识分享者 (发布第一个学习分享)
- 问答达人 (获得10个最佳答案)
- 学习之星 (累计学习100小时)

## 配置说明

确保 `application.yml` 中的数据库配置正确：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/learning_plan?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
    username: root
    password: your_password
    driver-class-name: com.mysql.cj.jdbc.Driver
  jpa:
    hibernate:
      ddl-auto: update
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQL8Dialect
```

## 注意事项

1. **密码安全**: 测试用户的密码已加密，原始密码为 `password`
2. **外键约束**: 所有表都设置了适当的外键约束
3. **索引优化**: 为常用查询字段添加了索引
4. **数据完整性**: 设置了必要的唯一约束和检查约束

## 故障排除

### 常见问题

1. **字符集问题**
   - 确保MySQL服务器支持utf8mb4字符集
   - 检查客户端连接字符集设置

2. **权限问题**
   - 确保MySQL用户有创建数据库和表的权限
   - 检查GRANT语句是否正确执行

3. **外键约束错误**
   - 按照脚本顺序执行，先创建主表再创建从表
   - 检查引用的主键是否存在

### 重置数据库

如需重置数据库：

```sql
DROP DATABASE IF EXISTS learning_plan;
-- 然后重新执行初始化脚本
```

## 维护建议

1. **定期备份**: 建议每日备份数据库
2. **性能监控**: 监控慢查询和索引使用情况
3. **数据清理**: 定期清理过期的临时数据
4. **统计更新**: 定期更新用户统计数据
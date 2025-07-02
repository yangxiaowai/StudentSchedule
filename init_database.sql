-- 数据库初始化脚本
-- 创建数据库和用户

-- 创建数据库
CREATE DATABASE IF NOT EXISTS `learning_plan` 
DEFAULT CHARACTER SET utf8mb4 
DEFAULT COLLATE utf8mb4_unicode_ci;

-- 使用数据库
USE `learning_plan`;

-- 创建数据库用户(可选)
-- CREATE USER IF NOT EXISTS 'learning_user'@'localhost' IDENTIFIED BY 'learning_password';
-- GRANT ALL PRIVILEGES ON learning_plan.* TO 'learning_user'@'localhost';
-- FLUSH PRIVILEGES;

-- 执行表结构创建
source database_tables.sql;

-- 显示创建的表
SHOW TABLES;

-- 验证数据
SELECT 'Users:' as info, COUNT(*) as count FROM user
UNION ALL
SELECT 'Study Groups:', COUNT(*) FROM study_group
UNION ALL
SELECT 'Group Members:', COUNT(*) FROM group_member
UNION ALL
SELECT 'Achievements:', COUNT(*) FROM achievement
UNION ALL
SELECT 'User Stats:', COUNT(*) FROM user_stats;
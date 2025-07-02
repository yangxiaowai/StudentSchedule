# 数据库外键约束问题修复指南

## 问题描述
在执行 `database_tables.sql` 脚本时遇到外键约束错误：
```
ERROR 1452 (23000) at line 201 in file: 'database_tables.sql': Cannot add or update a child row: a foreign key constraint fails
```

## 问题原因
原始脚本中使用了硬编码的用户ID（1, 2, 3），但由于用户表使用自增主键，实际生成的ID可能不是连续的1, 2, 3，导致外键约束失败。

## 修复内容

### 1. 用户统计数据插入修复
**修复前：**
```sql
INSERT INTO `user_stats` (`user_id`) VALUES (1), (2), (3);
```

**修复后：**
```sql
INSERT INTO `user_stats` (`user_id`) 
SELECT `id` FROM `user` WHERE `username` IN ('admin', 'student1', 'student2');
```

### 2. 学习小组创建者ID修复
**修复前：**
```sql
INSERT INTO `study_group` (`name`, `description`, `subject`, `study_goal`, `max_members`, `creator_id`) VALUES
('数学学习小组', '...', '数学', '...', 20, 1),
('英语口语练习', '...', '英语', '...', 15, 2),
('编程入门', '...', '计算机科学', '...', 25, 1);
```

**修复后：**
```sql
INSERT INTO `study_group` (`name`, `description`, `subject`, `study_goal`, `max_members`, `creator_id`) VALUES
('数学学习小组', '...', '数学', '...', 20, (SELECT `id` FROM `user` WHERE `username` = 'admin')),
('英语口语练习', '...', '英语', '...', 15, (SELECT `id` FROM `user` WHERE `username` = 'student1')),
('编程入门', '...', '计算机科学', '...', 25, (SELECT `id` FROM `user` WHERE `username` = 'admin'));
```

### 3. 小组成员插入修复
**修复前：**
```sql
INSERT INTO `group_member` (`group_id`, `user_id`, `role`) VALUES
(1, 1, 'creator'),
(1, 2, 'member'),
...
```

**修复后：**
```sql
INSERT INTO `group_member` (`group_id`, `user_id`, `role`) 
SELECT 
    (SELECT `id` FROM `study_group` WHERE `name` = '数学学习小组'),
    (SELECT `id` FROM `user` WHERE `username` = 'admin'),
    'creator'
UNION ALL
...
```

## 使用修复后的脚本

### 方法1：使用 MySQL 命令行
```bash
# 进入项目目录
cd /Users/yangjun/Downloads/StudentSchedule-main

# 连接到 MySQL
mysql -u root -p

# 在 MySQL 中执行
source init_database.sql
```

### 方法2：直接执行修复后的脚本
```bash
# 创建数据库
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS learning_plan CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# 执行表结构和数据脚本
mysql -u root -p learning_plan < database_tables.sql
```

## 验证修复结果
执行以下SQL验证数据是否正确插入：

```sql
-- 检查用户数据
SELECT * FROM user;

-- 检查用户统计数据
SELECT us.*, u.username FROM user_stats us 
JOIN user u ON us.user_id = u.id;

-- 检查学习小组数据
SELECT sg.*, u.username as creator_name FROM study_group sg 
JOIN user u ON sg.creator_id = u.id;

-- 检查小组成员数据
SELECT gm.*, u.username, sg.name as group_name FROM group_member gm 
JOIN user u ON gm.user_id = u.id 
JOIN study_group sg ON gm.group_id = sg.id;
```

## 注意事项
1. 确保在执行脚本前数据库为空，避免主键冲突
2. 如果需要重新执行脚本，请先删除数据库：`DROP DATABASE learning_plan;`
3. 修复后的脚本使用子查询确保外键关系正确，避免硬编码ID问题
4. 所有外键约束现在都基于实际的数据库记录，而不是假设的ID值

## 故障排除
如果仍然遇到问题：
1. 检查MySQL版本兼容性
2. 确认用户权限足够
3. 查看MySQL错误日志获取详细信息
4. 确保字符集设置正确（utf8mb4）
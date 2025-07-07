# 数据库表与实体类映射对比

## 概述
本文档记录了MySQL数据库中的表与Java实体类的映射关系，以及相应的调整情况。

## 数据库表列表
根据 `SHOW TABLES` 查询结果，当前数据库包含以下表：

### 已有对应实体类的表
| 数据库表名 | 实体类名 | 状态 | 备注 |
|-----------|----------|------|------|
| `user` | `User.java` | ✅ 已存在 | 用户基础信息表 |
| `task` | `Task.java` | ✅ 已存在 | 任务管理表 |
| `study_group` | `StudyGroup.java` | ✅ 已存在 | 学习小组表 |
| `group_member` | `GroupMember.java` | ✅ 已存在 | 小组成员表（简化版本） |
| `qa_question` | `QaQuestion.java` | ✅ 已存在 | 问答问题表 |
| `qa_answer` | `QaAnswer.java` | ✅ 已存在 | 问答答案表 |
| `study_share` | `StudyShare.java` | ✅ 已存在 | 学习分享表 |
| `achievement` | `Achievement.java` | ✅ 已存在 | 成就表 |
| `user_stats` | `UserStats.java` | ✅ 已存在 | 用户统计表 |

### 新创建的实体类
| 数据库表名 | 实体类名 | 状态 | 备注 |
|-----------|----------|------|------|
| `learning_materials` | `LearningMaterials.java` | 🆕 新创建 | 学习资料表 |
| `study_group_member` | `StudyGroupMember.java` | 🆕 新创建 | 学习小组成员表（完整版本） |
| ~~`user_learning_stats`~~ | ~~`UserLearningStats.java`~~ | ❌ 已删除 | ~~用户学习统计表~~ |
| `like_record` | `LikeRecord.java` | 🆕 新创建 | 点赞记录表 |
| `comment` | `Comment.java` | 🆕 新创建 | 评论表 |
| `user_achievement` | `UserAchievement.java` | 🆕 新创建 | 用户成就关联表 |
| `study_record` | `StudyRecord.java` | 🆕 新创建 | 学习记录表 |

## 表结构对比分析

### 重复功能的表

#### 1. group_member vs study_group_member
- **group_member**: 简化版本，包含基础字段（id, group_id, user_id, role, joined_at）
- **study_group_member**: 完整版本，包含额外字段（contribution_score, is_active, study_hours, tasks_completed）
- **建议**: 根据业务需求选择使用其中一个表

#### 2. user_stats
- **user_stats**: 包含基础统计信息
- ~~**user_learning_stats**: 已删除（排行榜功能相关）~~

## 实体类字段映射

### LearningMaterials 字段映射
```java
id -> id (bigint, auto_increment)
contentType -> content_type (varchar(50))
fileName -> file_name (varchar(255))
filePath -> file_path (varchar(500))
fileSize -> file_size (bigint)
fileType -> file_type (varchar(50))
subject -> subject (varchar(100))
uploadTime -> upload_time (datetime)
userId -> user_id (bigint)
```

### StudyGroupMember 字段映射
```java
id -> id (bigint, auto_increment)
groupId -> group_id (bigint)
userId -> user_id (bigint)
role -> role (enum: LEADER, MEMBER)
joinedAt -> joined_at (datetime)
isActive -> is_active (tinyint(1))
contributionScore -> contribution_score (int)
studyHours -> study_hours (decimal(5,2))
tasksCompleted -> tasks_completed (int)
```

### ~~UserLearningStats 字段映射~~
~~已删除（排行榜功能相关）~~

## 数据库约束和索引建议

### 外键约束
- 所有包含 `user_id` 的表应该添加对 `user(id)` 的外键约束
- 所有包含 `group_id` 的表应该添加对 `study_group(id)` 的外键约束
- `user_achievement` 表应该添加对 `achievement(id)` 的外键约束

### 索引建议
- 为经常查询的字段添加索引：`user_id`, `group_id`, `target_type`, `target_id`
- 为时间字段添加索引：`created_at`, `updated_at`, `upload_time`
- 为复合查询添加复合索引

## 下一步建议

1. **数据库清理**: 决定是否保留重复功能的表，或者合并相似表的功能
2. **关系映射**: 在实体类中添加 JPA 关系注解（@OneToMany, @ManyToOne 等）
3. **Repository 层**: 为新创建的实体类创建对应的 Repository 接口
4. **Service 层**: 创建业务逻辑服务类
5. **数据迁移**: 如果需要合并表，创建数据迁移脚本
6. **测试**: 为新的实体类和功能编写单元测试

## 注意事项

- 所有实体类都使用了 `javax.persistence` 包，确保版本一致性
- 时间字段统一使用 `LocalDateTime` 类型
- 主键统一使用 `Long` 类型和自增策略
- 字符串字段都指定了合适的长度限制
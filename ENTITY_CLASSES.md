# 实体类说明文档

## 概述
本文档说明学习养成计划系统中所有实体类的结构和用途。

## 实体类列表

### 1. User.java (已存在)
**用户实体类**
- **表名**: `user`
- **主要字段**:
  - `id`: 用户ID (主键)
  - `username`: 用户名 (唯一)
  - `password`: 密码 (加密存储)
  - `email`: 邮箱 (唯一)
  - `isActive`: 是否激活
  - `createdAt`: 创建时间
  - `updatedAt`: 更新时间

### 2. Task.java (已存在)
**任务实体类**
- **表名**: `task`
- **主要字段**:
  - `id`: 任务ID (主键)
  - `name`: 任务名称
  - `subject`: 学科
  - `content`: 任务内容
  - `startTime`: 开始时间
  - `endTime`: 截止时间
  - `progress`: 完成进度 (0-100)
  - `completed`: 是否完成
  - `fileUrl`: 文件访问路径
  - `fileName`: 文件名称
  - `filePath`: 文件路径

### 3. StudyGroup.java (新增)
**学习小组实体类**
- **表名**: `study_group`
- **主要字段**:
  - `id`: 小组ID (主键)
  - `name`: 小组名称
  - `description`: 小组描述
  - `subject`: 学科
  - `studyGoal`: 学习目标
  - `maxMembers`: 最大成员数
  - `currentMembers`: 当前成员数
  - `isPublic`: 是否公开
  - `creatorId`: 创建者ID
- **关联关系**:
  - 一对多关联 `GroupMember` (小组成员)

### 4. GroupMember.java (新增)
**小组成员实体类**
- **表名**: `group_member`
- **主要字段**:
  - `id`: 成员记录ID (主键)
  - `groupId`: 小组ID
  - `userId`: 用户ID
  - `role`: 角色 (creator/admin/member)
  - `joinedAt`: 加入时间
- **约束**: `groupId` + `userId` 唯一

### 5. UserStats.java (新增)
**用户统计实体类**
- **表名**: `user_stats`
- **主要字段**:
  - `id`: 统计ID (主键)
  - `userId`: 用户ID (唯一)
  - `studyHoursTotal`: 总学习时长
  - `studyHoursWeekly`: 本周学习时长
  - `studyHoursMonthly`: 本月学习时长
  - `points`: 积分
  - `experience`: 经验值
  - `level`: 等级
  - `streakDays`: 连续学习天数
  - `tasksCompleted`: 完成任务数
  - `bestAnswersCount`: 最佳答案数
  - `sharesCount`: 分享数
  - `likesReceived`: 获得点赞数
- **业务方法**:
  - `addStudyHours()`: 增加学习时长
  - `addPoints()`: 增加积分
  - `addExperience()`: 增加经验值
  - `incrementXxx()`: 各种计数器增加方法

### 6. Achievement.java (新增)
**成就实体类**
- **表名**: `achievement`
- **主要字段**:
  - `id`: 成就ID (主键)
  - `name`: 成就名称
  - `description`: 成就描述
  - `icon`: 成就图标
  - `type`: 成就类型
  - `conditionValue`: 达成条件值
  - `pointsReward`: 积分奖励
- **成就类型**:
  - `task_completion`: 任务完成
  - `study_streak`: 学习连续天数
  - `share_creation`: 分享创建
  - `best_answers`: 最佳答案
  - `study_hours`: 学习时长

### 7. StudyShare.java (新增)
**学习分享实体类**
- **表名**: `study_share`
- **主要字段**:
  - `id`: 分享ID (主键)
  - `title`: 分享标题
  - `content`: 分享内容
  - `subject`: 学科
  - `tags`: 标签 (JSON格式)
  - `authorId`: 作者ID
  - `likesCount`: 点赞数
  - `viewsCount`: 浏览数
  - `commentsCount`: 评论数
  - `isPublic`: 是否公开
- **业务方法**:
  - `incrementViews()`: 增加浏览数
  - `incrementLikes()`: 增加点赞数
  - `decrementLikes()`: 减少点赞数
  - `incrementComments()`: 增加评论数

## 待创建的实体类

基于数据库表结构，还需要创建以下实体类：

### 8. QAQuestion.java
**问答问题实体类**
- 对应表: `qa_question`
- 用于问答功能的问题管理

### 9. QAAnswer.java
**问答回答实体类**
- 对应表: `qa_answer`
- 用于问答功能的回答管理

### 10. Comment.java
**评论实体类**
- 对应表: `comment`
- 用于分享和回答的评论功能

### 11. LikeRecord.java
**点赞记录实体类**
- 对应表: `like_record`
- 用于记录用户点赞行为

### 12. UserAchievement.java
**用户成就实体类**
- 对应表: `user_achievement`
- 用于记录用户获得的成就

### 13. StudyRecord.java
**学习记录实体类**
- 对应表: `study_record`
- 用于记录用户的学习活动

## 注解说明

### JPA注解
- `@Entity`: 标记为JPA实体
- `@Table`: 指定数据库表名
- `@Id`: 标记主键字段
- `@GeneratedValue`: 主键生成策略
- `@Column`: 列属性配置
- `@OneToMany`: 一对多关联
- `@ManyToOne`: 多对一关联
- `@PreUpdate`: 更新前回调

### 约束注解
- `@UniqueConstraint`: 唯一约束
- `nullable = false`: 非空约束
- `unique = true`: 唯一约束
- `length = n`: 字符串长度限制

## 使用建议

### 1. 数据验证
建议在实体类中添加Bean Validation注解：
```java
@NotNull
@NotBlank
@Size(min = 1, max = 100)
@Email
```

### 2. 审计字段
对于需要审计的实体，建议使用JPA审计功能：
```java
@CreatedDate
@LastModifiedDate
@CreatedBy
@LastModifiedBy
```

### 3. 软删除
对于重要数据，建议实现软删除：
```java
@Column(name = "deleted")
private Boolean deleted = false;
```

### 4. 版本控制
对于需要乐观锁的实体，添加版本字段：
```java
@Version
private Long version;
```

## 关联关系图

```
User (1) -----> (*) Task
User (1) -----> (1) UserStats
User (1) -----> (*) StudyGroup (as creator)
User (*) <-----> (*) StudyGroup (via GroupMember)
User (1) -----> (*) StudyShare
User (1) -----> (*) QAQuestion
User (1) -----> (*) QAAnswer
User (1) -----> (*) Comment
User (1) -----> (*) LikeRecord
User (*) <-----> (*) Achievement (via UserAchievement)
User (1) -----> (*) StudyRecord
```

## 数据库配置

确保在 `application.yml` 中正确配置JPA：

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: update  # 开发环境使用，生产环境建议使用validate
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQL8Dialect
        show_sql: true
        format_sql: true
```
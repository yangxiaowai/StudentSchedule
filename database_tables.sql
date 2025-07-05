-- 学习养成计划系统数据库表结构
-- 数据库名: learning_plan

USE `learning_plan`;

-- 1. 用户表 (已存在User实体)
CREATE TABLE IF NOT EXISTS `user` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `username` VARCHAR(50) UNIQUE NOT NULL COMMENT '用户名',
    `password` VARCHAR(255) NOT NULL COMMENT '密码',
    `email` VARCHAR(100) UNIQUE NOT NULL COMMENT '邮箱',
    `is_active` BOOLEAN DEFAULT TRUE COMMENT '是否激活',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 2. 任务表 (已存在Task实体)
CREATE TABLE IF NOT EXISTS `task` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(100) NOT NULL COMMENT '任务名称',
    `subject` VARCHAR(100) COMMENT '学科',
    `content` TEXT COMMENT '任务内容',
    `start_time` DATETIME COMMENT '开始时间',
    `end_time` DATETIME COMMENT '截止时间',
    `progress` INT DEFAULT 0 COMMENT '完成进度(0-100)',
    `completed` BOOLEAN DEFAULT FALSE COMMENT '是否完成',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `content_type` VARCHAR(100) COMMENT '内容类型',
    `remark` TEXT COMMENT '备注',
    `file_url` VARCHAR(500) COMMENT '文件访问路径或URL',
    `file_name` VARCHAR(255) COMMENT '文件原始名称',
    `file_path` VARCHAR(500) COMMENT '文件存储路径',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务表';

-- 3. 学习小组表
CREATE TABLE IF NOT EXISTS `study_group` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(100) NOT NULL COMMENT '小组名称',
    `description` TEXT COMMENT '小组描述',
    `subject` VARCHAR(100) COMMENT '学科',
    `study_goal` TEXT COMMENT '学习目标',
    `max_members` INT DEFAULT 10 COMMENT '最大成员数',
    `current_members` INT DEFAULT 0 COMMENT '当前成员数',
    `is_public` BOOLEAN DEFAULT TRUE COMMENT '是否公开',
    `creator_id` BIGINT NOT NULL COMMENT '创建者ID',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (`creator_id`) REFERENCES `user`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学习小组表';

-- 4. 小组成员表
CREATE TABLE IF NOT EXISTS `group_member` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `group_id` BIGINT NOT NULL COMMENT '小组ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `role` VARCHAR(20) DEFAULT 'member' COMMENT '角色(creator/admin/member)',
    `joined_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
    UNIQUE KEY `uk_group_user` (`group_id`, `user_id`),
    FOREIGN KEY (`group_id`) REFERENCES `study_group`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='小组成员表';

-- 学习小组成员表（完整版本）
CREATE TABLE IF NOT EXISTS `study_group_member` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `group_id` BIGINT NOT NULL COMMENT '小组ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `role` VARCHAR(20) DEFAULT 'MEMBER' COMMENT '角色(CREATOR/ADMIN/MEMBER)',
    `contribution_score` INT DEFAULT 0 COMMENT '贡献分数',
    `study_hours` DOUBLE DEFAULT 0.0 COMMENT '学习时长',
    `tasks_completed` INT DEFAULT 0 COMMENT '完成任务数',
    `is_active` BOOLEAN DEFAULT TRUE COMMENT '是否活跃',
    `joined_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
    UNIQUE KEY `uk_study_group_user` (`group_id`, `user_id`),
    FOREIGN KEY (`group_id`) REFERENCES `study_group`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学习小组成员表';

-- 5. 学习分享表
CREATE TABLE IF NOT EXISTS `study_share` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `title` VARCHAR(200) NOT NULL COMMENT '分享标题',
    `content` TEXT NOT NULL COMMENT '分享内容',
    `subject` VARCHAR(100) COMMENT '学科',
    `tags` VARCHAR(500) COMMENT '标签(JSON格式)',
    `author_id` BIGINT NOT NULL COMMENT '作者ID',
    `likes_count` INT DEFAULT 0 COMMENT '点赞数',
    `views_count` INT DEFAULT 0 COMMENT '浏览数',
    `comments_count` INT DEFAULT 0 COMMENT '评论数',
    `is_public` BOOLEAN DEFAULT TRUE COMMENT '是否公开',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (`author_id`) REFERENCES `user`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学习分享表';

-- 6. 问答表
CREATE TABLE IF NOT EXISTS `qa_question` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `title` VARCHAR(200) NOT NULL COMMENT '问题标题',
    `content` TEXT NOT NULL COMMENT '问题内容',
    `subject` VARCHAR(100) COMMENT '学科',
    `tags` VARCHAR(500) COMMENT '标签(JSON格式)',
    `asker_id` BIGINT NOT NULL COMMENT '提问者ID',
    `views_count` INT DEFAULT 0 COMMENT '浏览数',
    `answers_count` INT DEFAULT 0 COMMENT '回答数',
    `is_solved` BOOLEAN DEFAULT FALSE COMMENT '是否已解决',
    `best_answer_id` BIGINT COMMENT '最佳答案ID',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (`asker_id`) REFERENCES `user`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='问答问题表';

-- 7. 问答回答表
CREATE TABLE IF NOT EXISTS `qa_answer` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `question_id` BIGINT NOT NULL COMMENT '问题ID',
    `content` TEXT NOT NULL COMMENT '回答内容',
    `answerer_id` BIGINT NOT NULL COMMENT '回答者ID',
    `likes_count` INT DEFAULT 0 COMMENT '点赞数',
    `is_best` BOOLEAN DEFAULT FALSE COMMENT '是否最佳答案',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (`question_id`) REFERENCES `qa_question`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`answerer_id`) REFERENCES `user`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='问答回答表';

-- 8. 点赞表
CREATE TABLE IF NOT EXISTS `like_record` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `target_type` VARCHAR(50) NOT NULL COMMENT '目标类型(SHARE/QUESTION/ANSWER)',
    `target_id` BIGINT NOT NULL COMMENT '目标ID',
    `created_at` DATETIME COMMENT '创建时间',
    UNIQUE KEY `uk_user_target` (`user_id`, `target_type`, `target_id`),
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='点赞记录表';

-- 9. 评论表
CREATE TABLE IF NOT EXISTS `comment` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `target_type` VARCHAR(20) NOT NULL COMMENT '目标类型(share/answer)',
    `target_id` BIGINT NOT NULL COMMENT '目标ID',
    `content` TEXT NOT NULL COMMENT '评论内容',
    `commenter_id` BIGINT NOT NULL COMMENT '评论者ID',
    `parent_id` BIGINT COMMENT '父评论ID(回复)',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (`commenter_id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`parent_id`) REFERENCES `comment`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论表';

-- 10. 用户统计表
CREATE TABLE IF NOT EXISTS `user_stats` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL UNIQUE COMMENT '用户ID',
    `study_hours_total` DECIMAL(10,2) DEFAULT 0.00 COMMENT '总学习时长',
    `study_hours_weekly` DECIMAL(10,2) DEFAULT 0.00 COMMENT '周学习时长',
    `study_hours_monthly` DECIMAL(10,2) DEFAULT 0.00 COMMENT '月学习时长',
    `points` INT DEFAULT 0 COMMENT '积分',
    `experience` INT DEFAULT 0 COMMENT '经验值',
    `level` INT DEFAULT 1 COMMENT '等级',
    `streak_days` INT DEFAULT 0 COMMENT '连续学习天数',
    `tasks_completed` INT DEFAULT 0 COMMENT '完成任务数',
    `best_answers_count` INT DEFAULT 0 COMMENT '最佳答案数',
    `shares_count` INT DEFAULT 0 COMMENT '分享数',
    `likes_received` INT DEFAULT 0 COMMENT '获赞数',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户统计表';

-- 11. 成就表
CREATE TABLE IF NOT EXISTS `achievement` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(100) NOT NULL COMMENT '成就名称',
    `description` TEXT COMMENT '成就描述',
    `icon` VARCHAR(100) COMMENT '成就图标',
    `type` VARCHAR(50) NOT NULL COMMENT '成就类型',
    `condition_value` INT COMMENT '达成条件值',
    `points_reward` INT DEFAULT 0 COMMENT '积分奖励',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='成就表';

-- 12. 用户成就表
CREATE TABLE IF NOT EXISTS `user_achievement` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `achievement_id` BIGINT NOT NULL COMMENT '成就ID',
    `unlocked_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '解锁时间',
    UNIQUE KEY `uk_user_achievement` (`user_id`, `achievement_id`),
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`achievement_id`) REFERENCES `achievement`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户成就表';

-- 13. 学习记录表
CREATE TABLE IF NOT EXISTS `study_record` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `subject` VARCHAR(100) NOT NULL COMMENT '学科',
    `study_duration` INT NOT NULL COMMENT '学习时长(分钟)',
    `study_date` DATETIME NOT NULL COMMENT '学习日期',
    `notes` TEXT COMMENT '学习笔记',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学习记录表';

-- 14. 用户学习统计表
CREATE TABLE IF NOT EXISTS `user_learning_stats` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL UNIQUE COMMENT '用户ID',
    `total_study_hours` DOUBLE COMMENT '总学习时长',
    `weekly_study_hours` DOUBLE COMMENT '周学习时长',
    `monthly_study_hours` DOUBLE COMMENT '月学习时长',
    `total_points` INT COMMENT '总积分',
    `current_level` INT COMMENT '当前等级',
    `current_streak` INT COMMENT '当前连续天数',
    `longest_streak` INT COMMENT '最长连续天数',
    `total_tasks_completed` INT COMMENT '总完成任务数',
    `best_answers_count` INT COMMENT '最佳答案数',
    `shares_count` INT COMMENT '分享数',
    `likes_received` INT COMMENT '获赞数',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户学习统计表';

-- 插入初始数据

-- 清理现有数据（按外键依赖顺序）
DELETE FROM `qa_answer`;
DELETE FROM `qa_question`;
DELETE FROM `study_share`;
DELETE FROM `task`;
DELETE FROM `group_member`;
DELETE FROM `study_group_member`;
DELETE FROM `user_stats`;
DELETE FROM `user_learning_stats`;
DELETE FROM `user` WHERE `username` IN ('admin', 'student1', 'student2');
DELETE FROM `study_group` WHERE `name` IN ('数学学习小组', '英语口语练习', '编程入门');
DELETE FROM `achievement` WHERE `name` IN ('初学者', '勤奋学习者', '学习之星');

-- 插入示例用户
INSERT INTO `user` (`username`, `password`, `email`) VALUES
('admin', '$2b$10$f7Uc83ncHh.U04FhuMHdVuNKpK5NQlyBv0DwzCCl4siaykJbefIYS', 'admin@example.com'),
('student1', '$2b$10$f7Uc83ncHh.U04FhuMHdVuNKpK5NQlyBv0DwzCCl4siaykJbefIYS', 'student1@example.com'),
('student2', '$2b$10$f7Uc83ncHh.U04FhuMHdVuNKpK5NQlyBv0DwzCCl4siaykJbefIYS', 'student2@example.com');

-- 插入用户统计数据
INSERT INTO `user_stats` (`user_id`) 
SELECT `id` FROM `user` WHERE `username` IN ('admin', 'student1', 'student2');

-- 插入用户学习统计数据
INSERT INTO `user_learning_stats` (`user_id`, `total_study_hours`, `weekly_study_hours`, `monthly_study_hours`, `total_points`, `current_level`, `current_streak`, `longest_streak`, `total_tasks_completed`, `best_answers_count`, `shares_count`, `likes_received`) VALUES
((SELECT `id` FROM `user` WHERE `username` = 'admin'), 55.5, 12.0, 45.0, 300, 3, 7, 15, 18, 3, 2, 47),
((SELECT `id` FROM `user` WHERE `username` = 'student1'), 35.0, 8.5, 28.0, 180, 2, 5, 10, 11, 0, 1, 20),
((SELECT `id` FROM `user` WHERE `username` = 'student2'), 22.5, 6.0, 18.0, 120, 1, 3, 8, 5, 1, 1, 12);

-- 插入成就数据
INSERT INTO `achievement` (`name`, `description`, `type`, `condition_value`, `points_reward`) VALUES
('初学者', '完成第一个任务', 'task_completion', 1, 10),
('勤奋学习者', '连续学习7天', 'study_streak', 7, 50),
('知识分享者', '发布第一个学习分享', 'share_creation', 1, 20),
('问答达人', '获得10个最佳答案', 'best_answers', 10, 100),
('学习之星', '累计学习100小时', 'study_hours', 100, 200);

-- 插入示例学习小组
INSERT INTO `study_group` (`name`, `description`, `subject`, `study_goal`, `max_members`, `creator_id`) VALUES
('数学学习小组', '一起学习高等数学，互相帮助解决难题', '数学', '掌握微积分基础知识', 20, (SELECT `id` FROM `user` WHERE `username` = 'admin')),
('英语口语练习', '提高英语口语水平，每日练习', '英语', '流利的英语口语交流', 15, (SELECT `id` FROM `user` WHERE `username` = 'student1')),
('编程入门', '从零开始学习编程，分享学习心得', '计算机科学', '掌握基础编程技能', 25, (SELECT `id` FROM `user` WHERE `username` = 'admin'));

-- 插入小组成员
INSERT INTO `group_member` (`group_id`, `user_id`, `role`) 
SELECT 
    (SELECT `id` FROM `study_group` WHERE `name` = '数学学习小组'),
    (SELECT `id` FROM `user` WHERE `username` = 'admin'),
    'creator'
UNION ALL
SELECT 
    (SELECT `id` FROM `study_group` WHERE `name` = '数学学习小组'),
    (SELECT `id` FROM `user` WHERE `username` = 'student1'),
    'member'
UNION ALL
SELECT 
    (SELECT `id` FROM `study_group` WHERE `name` = '英语口语练习'),
    (SELECT `id` FROM `user` WHERE `username` = 'student1'),
    'creator'
UNION ALL
SELECT 
    (SELECT `id` FROM `study_group` WHERE `name` = '英语口语练习'),
    (SELECT `id` FROM `user` WHERE `username` = 'student2'),
    'member'
UNION ALL
SELECT 
    (SELECT `id` FROM `study_group` WHERE `name` = '编程入门'),
    (SELECT `id` FROM `user` WHERE `username` = 'admin'),
    'creator'
UNION ALL
SELECT 
    (SELECT `id` FROM `study_group` WHERE `name` = '编程入门'),
    (SELECT `id` FROM `user` WHERE `username` = 'student2'),
    'member';

-- 更新小组当前成员数
UPDATE `study_group` SET `current_members` = (
    SELECT COUNT(*) FROM `group_member` WHERE `group_member`.`group_id` = `study_group`.`id`
);

-- 确保study_share表存在且结构正确
DROP TABLE IF EXISTS `study_share`;
CREATE TABLE `study_share` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `title` VARCHAR(200) NOT NULL COMMENT '分享标题',
    `content` TEXT NOT NULL COMMENT '分享内容',
    `subject` VARCHAR(100) COMMENT '学科',
    `tags` VARCHAR(500) COMMENT '标签(JSON格式)',
    `author_id` BIGINT NOT NULL COMMENT '作者ID',
    `likes_count` INT DEFAULT 0 COMMENT '点赞数',
    `views_count` INT DEFAULT 0 COMMENT '浏览数',
    `comments_count` INT DEFAULT 0 COMMENT '评论数',
    `is_public` BOOLEAN DEFAULT TRUE COMMENT '是否公开',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (`author_id`) REFERENCES `user`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学习分享表';

-- 插入学习分享测试数据
INSERT INTO `study_share` (`title`, `content`, `subject`, `tags`, `author_id`, `likes_count`, `views_count`, `is_public`) VALUES
('高效学习方法分享', '分享一些我在学习过程中总结的高效学习方法：1. 番茄工作法 2. 费曼学习法 3. 思维导图...', '学习方法', '["学习技巧", "效率提升"]', (SELECT `id` FROM `user` WHERE `username` = 'admin'), 15, 120, TRUE),
('数学解题技巧总结', '整理了一些常见的数学解题技巧和思路，希望对大家有帮助...', '数学', '["数学", "解题技巧"]', (SELECT `id` FROM `user` WHERE `username` = 'student1'), 8, 85, TRUE),
('英语口语练习心得', '分享我练习英语口语的一些经验和方法...', '英语', '["英语", "口语练习"]', (SELECT `id` FROM `user` WHERE `username` = 'student2'), 12, 95, TRUE),
('编程学习路线推荐', '给初学者推荐的编程学习路线和资源...', '编程', '["编程", "学习路线"]', (SELECT `id` FROM `user` WHERE `username` = 'admin'), 20, 150, TRUE);

-- 6. 问答问题表
CREATE TABLE IF NOT EXISTS `qa_question` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `asker_id` BIGINT NOT NULL COMMENT '提问者ID',
    `group_id` BIGINT COMMENT '小组ID(可选)',
    `title` VARCHAR(200) NOT NULL COMMENT '问题标题',
    `content` TEXT NOT NULL COMMENT '问题内容',
    `subject` VARCHAR(50) COMMENT '学科',
    `tags` VARCHAR(200) COMMENT '标签(逗号分隔)',
    `image_url` VARCHAR(500) COMMENT '问题配图',
    `difficulty_level` VARCHAR(20) DEFAULT 'MEDIUM' COMMENT '难度等级',
    `status` VARCHAR(20) DEFAULT 'OPEN' COMMENT '问题状态',
    `best_answer_id` BIGINT COMMENT '最佳答案ID',
    `answer_count` INT DEFAULT 0 COMMENT '回答数',
    `view_count` INT DEFAULT 0 COMMENT '浏览数',
    `like_count` INT DEFAULT 0 COMMENT '点赞数',
    `reward_points` INT DEFAULT 0 COMMENT '悬赏积分',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (`asker_id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`group_id`) REFERENCES `study_group`(`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='问答问题表';

-- 插入问答测试数据
INSERT INTO `qa_question` (`title`, `content`, `subject`, `tags`, `asker_id`, `view_count`, `answer_count`) VALUES
('如何提高数学成绩？', '我的数学成绩一直不太好，想请教一下大家有什么好的学习方法吗？', '数学', '["数学", "学习方法"]', (SELECT `id` FROM `user` WHERE `username` = 'student1'), 45, 3),
('英语听力怎么练习？', '英语听力一直是我的弱项，有什么好的练习方法推荐吗？', '英语', '["英语", "听力练习"]', (SELECT `id` FROM `user` WHERE `username` = 'student2'), 32, 2),
('编程入门应该学什么语言？', '刚开始学编程，不知道应该选择哪种编程语言入门，求建议！', '编程', '["编程", "入门"]', (SELECT `id` FROM `user` WHERE `username` = 'student1'), 28, 4),
('如何制定有效的学习计划？', '想要制定一个有效的学习计划，但不知道从何入手，请大家给点建议。', '学习方法', '["学习计划", "时间管理"]', (SELECT `id` FROM `user` WHERE `username` = 'student2'), 38, 2);

-- 7. 问答回答表
CREATE TABLE IF NOT EXISTS `qa_answer` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `question_id` BIGINT NOT NULL COMMENT '问题ID',
    `answerer_id` BIGINT NOT NULL COMMENT '回答者ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `content` TEXT NOT NULL COMMENT '回答内容',
    `image_url` VARCHAR(500) COMMENT '回答配图',
    `like_count` INT DEFAULT 0 COMMENT '点赞数',
    `is_best_answer` BOOLEAN DEFAULT FALSE COMMENT '是否最佳答案',
    `is_helpful` BOOLEAN DEFAULT FALSE COMMENT '是否有帮助',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (`question_id`) REFERENCES `qa_question`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`answerer_id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='问答回答表';

-- 插入问答回答测试数据
INSERT INTO `qa_answer` (`question_id`, `content`, `answerer_id`, `user_id`, `is_best_answer`) VALUES
((SELECT `id` FROM `qa_question` WHERE `title` = '如何提高数学成绩？'), '建议多做练习题，理解概念比死记硬背更重要。可以尝试费曼学习法。', (SELECT `id` FROM `user` WHERE `username` = 'admin'), (SELECT `id` FROM `user` WHERE `username` = 'admin'), TRUE),
((SELECT `id` FROM `qa_question` WHERE `title` = '如何提高数学成绩？'), '我觉得找到适合自己的学习方法很重要，可以试试不同的方法。', (SELECT `id` FROM `user` WHERE `username` = 'student2'), (SELECT `id` FROM `user` WHERE `username` = 'student2'), FALSE),
((SELECT `id` FROM `qa_question` WHERE `title` = '英语听力怎么练习？'), '推荐多听英语播客和看英文电影，从简单的开始。', (SELECT `id` FROM `user` WHERE `username` = 'admin'), (SELECT `id` FROM `user` WHERE `username` = 'admin'), TRUE),
((SELECT `id` FROM `qa_question` WHERE `title` = '编程入门应该学什么语言？'), 'Python是很好的入门语言，语法简单易懂。', (SELECT `id` FROM `user` WHERE `username` = 'admin'), (SELECT `id` FROM `user` WHERE `username` = 'admin'), TRUE),
((SELECT `id` FROM `qa_question` WHERE `title` = '编程入门应该学什么语言？'), 'JavaScript也不错，可以做网页开发。', (SELECT `id` FROM `user` WHERE `username` = 'student2'), (SELECT `id` FROM `user` WHERE `username` = 'student2'), FALSE);

-- 插入学习小组成员数据（完整版本）
INSERT INTO `study_group_member` (`group_id`, `user_id`, `role`, `contribution_score`, `study_hours`, `tasks_completed`) 
SELECT 
    (SELECT `id` FROM `study_group` WHERE `name` = '数学学习小组'),
    (SELECT `id` FROM `user` WHERE `username` = 'admin'),
    'CREATOR',
    100,
    25.5,
    8
UNION ALL
SELECT 
    (SELECT `id` FROM `study_group` WHERE `name` = '数学学习小组'),
    (SELECT `id` FROM `user` WHERE `username` = 'student1'),
    'MEMBER',
    60,
    15.0,
    5
UNION ALL
SELECT 
    (SELECT `id` FROM `study_group` WHERE `name` = '英语口语练习'),
    (SELECT `id` FROM `user` WHERE `username` = 'student1'),
    'CREATOR',
    80,
    20.0,
    6
UNION ALL
SELECT 
    (SELECT `id` FROM `study_group` WHERE `name` = '英语口语练习'),
    (SELECT `id` FROM `user` WHERE `username` = 'student2'),
    'MEMBER',
    45,
    12.5,
    3
UNION ALL
SELECT 
    (SELECT `id` FROM `study_group` WHERE `name` = '编程入门'),
    (SELECT `id` FROM `user` WHERE `username` = 'admin'),
    'CREATOR',
    120,
    30.0,
    10
UNION ALL
SELECT 
    (SELECT `id` FROM `study_group` WHERE `name` = '编程入门'),
    (SELECT `id` FROM `user` WHERE `username` = 'student2'),
    'MEMBER',
    35,
    10.0,
    2;

-- 更新问题的回答数和最佳答案
UPDATE `qa_question` SET 
    `answer_count` = (SELECT COUNT(*) FROM `qa_answer` WHERE `qa_answer`.`question_id` = `qa_question`.`id`),
    `best_answer_id` = (SELECT `id` FROM `qa_answer` WHERE `qa_answer`.`question_id` = `qa_question`.`id` AND `is_best_answer` = TRUE LIMIT 1);

-- 插入任务测试数据
INSERT INTO `task` (`name`, `subject`, `content`, `start_time`, `end_time`, `progress`, `completed`, `user_id`) VALUES
('完成数学作业', '数学', '完成第三章的练习题1-20', NOW(), DATE_ADD(NOW(), INTERVAL 3 DAY), 60, FALSE, (SELECT `id` FROM `user` WHERE `username` = 'admin')),
('背诵英语单词', '英语', '背诵Unit 5的新单词', NOW(), DATE_ADD(NOW(), INTERVAL 1 DAY), 80, FALSE, (SELECT `id` FROM `user` WHERE `username` = 'student1')),
('编程练习', '编程', '完成Python基础练习题', NOW(), DATE_ADD(NOW(), INTERVAL 5 DAY), 30, FALSE, (SELECT `id` FROM `user` WHERE `username` = 'student2')),
('复习物理知识点', '物理', '复习力学相关知识点', NOW(), DATE_ADD(NOW(), INTERVAL 2 DAY), 100, TRUE, (SELECT `id` FROM `user` WHERE `username` = 'admin'));
-- 启用学习小组的任务共享和资料共享功能
UPDATE study_group SET task_sharing_enabled = TRUE, resource_sharing_enabled = TRUE WHERE id IN (1, 2, 3);

-- 添加一些测试任务数据（如果不存在的话）
INSERT IGNORE INTO task (user_id, name, content, subject, progress, completed, start_time, end_time) VALUES
(1, '数学练习题1', '完成代数方程练习', '数学', 0, FALSE, NOW(), '2024-12-31 23:59:59'),
(1, '数学练习题2', '完成几何证明题', '数学', 0, FALSE, NOW(), '2024-12-31 23:59:59'),
(2, '英语口语练习', '练习日常对话', '英语', 0, FALSE, NOW(), '2024-12-31 23:59:59'),
(3, 'Java编程作业', '完成面向对象编程练习', '编程', 0, FALSE, NOW(), '2024-12-31 23:59:59');

-- 添加一些测试学习资料数据（如果不存在的话）
INSERT IGNORE INTO learning_materials (user_id, file_name, file_path, file_type, file_size, subject, content_type, upload_time) VALUES
(1, '数学公式大全.pdf', '/materials/math_formulas.pdf', 'pdf', 1024000, '数学', 'application/pdf', NOW()),
(1, '数学解题技巧.docx', '/materials/math_techniques.docx', 'docx', 512000, '数学', 'application/vnd.openxmlformats-officedocument.wordprocessingml.document', NOW()),
(2, '英语语法手册.pdf', '/materials/english_grammar.pdf', 'pdf', 2048000, '英语', 'application/pdf', NOW()),
(3, 'Java编程指南.pdf', '/materials/java_guide.pdf', 'pdf', 3072000, '编程', 'application/pdf', NOW());

-- 查看更新结果
SELECT id, name, task_sharing_enabled, resource_sharing_enabled FROM study_group WHERE id IN (1, 2, 3);
SELECT COUNT(*) as task_count FROM task;
SELECT COUNT(*) as material_count FROM learning_materials;
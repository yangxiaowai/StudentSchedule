package com.example.learning.learning_habit_plan_backend.repository;
import com.example.learning.learning_habit_plan_backend.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository

public interface TaskRepository extends JpaRepository<Task, Long> {

    // 根据用户ID查询任务
    List<Task> findByUserId(Long userId);

    // 根据用户ID列表查询任务（用于小组任务共享）
    List<Task> findByUserIdIn(List<Long> userIds);

    // 根据学科查询任务
    List<Task> findBySubject(String subject);

    // 根据用户ID和学科查询任务
    List<Task> findByUserIdAndSubject(Long userId, String subject);
}
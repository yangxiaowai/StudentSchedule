package com.example.learning.learning_habit_plan_backend.service;

import com.example.learning.learning_habit_plan_backend.entity.Task;
import com.example.learning.learning_habit_plan_backend.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskService {
    @Autowired
    private TaskRepository taskRepository;

    public Task save(Task task) {
        return taskRepository.save(task);
    }

    public List<Task> getAll() {
        return taskRepository.findAll();
    }

    public Task findById(Long id) {
        return taskRepository.findById(id).orElse(null);
    }

    public void deleteById(Long id) {
        taskRepository.deleteById(id);
    }

    // 新增方法：根据ID列表批量查询任务
    public List<Task> getTasksByIds(List<Long> taskIds) {
        return taskRepository.findAllById(taskIds);
    }

    // 根据用户ID查询任务
    public List<Task> getTasksByUserId(Long userId) {
        return taskRepository.findByUserId(userId);
    }

    // 根据用户ID列表查询任务（用于小组任务共享）
    public List<Task> getTasksByUserIds(List<Long> userIds) {
        return taskRepository.findByUserIdIn(userIds);
    }

    // 根据学科查询任务
    public List<Task> getTasksBySubject(String subject) {
        return taskRepository.findBySubject(subject);
    }

    // 根据用户ID和学科查询任务
    public List<Task> getTasksByUserIdAndSubject(Long userId, String subject) {
        return taskRepository.findByUserIdAndSubject(userId, subject);
    }
}
